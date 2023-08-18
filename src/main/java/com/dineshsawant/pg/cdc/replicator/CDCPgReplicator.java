package com.dineshsawant.pg.cdc.replicator;

import com.dineshsawant.pg.cdc.transform.Format2ToMaxwellBinlogTransformer;
import com.google.common.flogger.FluentLogger;
import com.dineshsawant.pg.cdc.exception.PgCDCException;
import com.dineshsawant.pg.cdc.state.State;
import com.dineshsawant.pg.cdc.state.StateStore;
import com.dineshsawant.pg.cdc.transform.BinlogTransformer;
import com.dineshsawant.pg.cdc.transform.Format1ToMaxwellBinlogTransformer;
import com.dineshsawant.pg.cdc.util.Constants;
import com.dineshsawant.pg.cdc.config.Configuration;
import com.dineshsawant.pg.cdc.producer.AbstractCDCProducer;
import io.vertx.core.json.JsonObject;
import org.postgresql.PGConnection;
import org.postgresql.PGProperty;
import org.postgresql.replication.LogSequenceNumber;
import org.postgresql.replication.PGReplicationStream;
import org.postgresql.replication.ReplicationSlotInfo;
import org.postgresql.replication.fluent.logical.ChainedLogicalStreamBuilder;
import org.postgresql.util.PSQLException;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CDCPgReplicator implements PgReplicator {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    public static final String PG_DUPLICATE_OBJECT_ERROR_CODE = "42710";

    private Configuration configuration;
    private AbstractCDCProducer producer;
    private volatile boolean stopRequested;
    private BinlogTransformer binlogTransformer = new Format1ToMaxwellBinlogTransformer();
    private volatile CountDownLatch stopCountDownLatch = new CountDownLatch(1);
    private final StateStore stateStore;
    private LogSequenceNumber lastReceiveLSN;
    private LogSequenceNumber lastSavedLSN;

    public CDCPgReplicator(Configuration configuration, AbstractCDCProducer producer, StateStore stateStore) {
        this.configuration = configuration;
        this.producer = producer;
        this.stateStore = stateStore;
    }

    @Override
    public void start() throws SQLException, InterruptedException {
        PGReplicationStream replicationStream = createReplicationStream();

        while (!stopRequested) {
            //non blocking receive message
            ByteBuffer msg = replicationStream.readPending();

            if (msg == null) {
                saveState();
                logger.atInfo().every(10).log("Sleeping for %d millis", configuration.getReplicationSleepInMillis());
                TimeUnit.MILLISECONDS.sleep(configuration.getReplicationSleepInMillis());
                continue;
            }

            int offset = msg.arrayOffset();
            byte[] source = msg.array();
            int length = source.length - offset;

            final String binLog = new String(source, offset, length);

            transformAndPush(binLog);
            sendFeedback(replicationStream);
        }

        saveState();
        replicationStream.close();
        stopCountDownLatch.countDown();
        logger.atInfo().log("Closed replication stream.");
    }

    private void saveState() {
        if (Objects.nonNull(lastReceiveLSN) && !Objects.equals(lastSavedLSN, lastReceiveLSN)) {
            State state = new State(configuration.getReplicationSlotName(), lastReceiveLSN.asString());
            stateStore.createOrUpdateState(state);
            logger.atInfo().log("Saved state successfully");
            this.lastSavedLSN = lastReceiveLSN;
        }
    }

    private void sendFeedback(PGReplicationStream replicationStream) {
        this.lastReceiveLSN = replicationStream.getLastReceiveLSN();
        logger.atInfo().log("Sending feedback Last Received LSN = %s", this.lastReceiveLSN);
        replicationStream.setAppliedLSN(this.lastReceiveLSN);
        replicationStream.setFlushedLSN(this.lastReceiveLSN);
    }

    private void transformAndPush(String binLog) {
        logger.atInfo().log("Got binlog %s", binLog);

        final JsonObject binlogJson = new JsonObject(binLog);
        binlogTransformer.transform(binlogJson).forEach(this.producer::pushRow);
    }

    public void setStopRequested(boolean stopRequested) {
        this.stopRequested = stopRequested;
    }

    protected PGReplicationStream createReplicationStream() throws SQLException {
         PGConnection pgConnection = getSqlConnection().unwrap(PGConnection.class);

        final String replicationSlotName = configuration.getReplicationSlotName();
        createReplicationSlot(replicationSlotName, pgConnection);

        final ChainedLogicalStreamBuilder chainedLogicalStreamBuilder = pgConnection.getReplicationAPI()
                .replicationStream()
                .logical()
                .withSlotName(replicationSlotName)
                .withSlotOption("include-xids", true)
                .withSlotOption("include-timestamp", true)
                .withSlotOption("pretty-print", false)
                .withSlotOption("add-tables", configuration.getReplicationAddTables())
                // Should be set to (wal_sender_timeout / 3)
                .withStatusInterval(10, TimeUnit.SECONDS);

        stateStore.fetchState(replicationSlotName).ifPresent(state -> {
            final String lastLogSequenceNumber = state.getLastLogSequenceNumber();
            logger.atInfo().log("Starting from last sequence number = %s", lastLogSequenceNumber);
            chainedLogicalStreamBuilder.withStartPosition(LogSequenceNumber.valueOf(lastLogSequenceNumber));
        });

        return chainedLogicalStreamBuilder.start();
    }

    private Connection getSqlConnection() throws SQLException {
        String connectionUrl = String.format(Constants.JDBC_CONNECTION_URL,
                configuration.getPgServer(),
                configuration.getPgPort(),
                configuration.getPgDatabase());

        Properties connectionProperties = new Properties();
        PGProperty.USER.set(connectionProperties, configuration.getPgUser());
        PGProperty.PASSWORD.set(connectionProperties, configuration.getPgPassword());
        PGProperty.ASSUME_MIN_SERVER_VERSION.set(connectionProperties, configuration.getPgAssumeMinServerVersion());
        PGProperty.REPLICATION.set(connectionProperties, "database");
        PGProperty.PREFER_QUERY_MODE.set(connectionProperties, "simple");

        return DriverManager.getConnection(connectionUrl, connectionProperties);
    }

    private void createReplicationSlot(String replicationSlotName, PGConnection pgConnection) throws SQLException {
        try {
            final ReplicationSlotInfo replicationSlotInfo = pgConnection.getReplicationAPI()
                    .createReplicationSlot()
                    .logical()
                    .withSlotName(replicationSlotName)
                    .withOutputPlugin(Constants.WAL_2_JSON)
                    .make();

            logger.atInfo().log("Replication slot created %s, consistent point = %s, snapshotName = %s ",
                    replicationSlotInfo.getSlotName(),
                    replicationSlotInfo.getConsistentPoint(), replicationSlotInfo.getSnapshotName());

        } catch (PSQLException pse) {
            if (Objects.equals(PG_DUPLICATE_OBJECT_ERROR_CODE, pse.getSQLState())) {
                logger.atInfo().log("Slot %s is already exist. Hence using the existing slot.", replicationSlotName);
            } else {
                throw new PgCDCException(pse);
            }
        }
    }

    @Override
    public CountDownLatch shutdown() {
        logger.atInfo().log("Shutdown requested");
        setStopRequested(true);
        return stopCountDownLatch;
    }
}
