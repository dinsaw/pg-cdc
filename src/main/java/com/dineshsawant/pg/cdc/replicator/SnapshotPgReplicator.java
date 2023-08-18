package com.dineshsawant.pg.cdc.replicator;

import com.google.common.flogger.FluentLogger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.dineshsawant.pg.cdc.config.Configuration;
import com.dineshsawant.pg.cdc.exception.PgCDCException;
import com.dineshsawant.pg.cdc.producer.AbstractCDCProducer;
import com.dineshsawant.pg.cdc.transform.MaxwellBinlogKeys;
import com.dineshsawant.pg.cdc.util.Constants;
import com.dineshsawant.pg.cdc.util.DateTimeUtil;
import io.vertx.core.json.JsonObject;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

public class SnapshotPgReplicator implements PgReplicator {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private final HikariDataSource dataSource;
    private Configuration configuration;
    private AbstractCDCProducer producer;
    private volatile boolean shutdownRequested;
    private CountDownLatch shutdownCountDownLatch = new CountDownLatch(1);

    public SnapshotPgReplicator(Configuration configuration, AbstractCDCProducer producer) {
        if (Objects.isNull(configuration.getReplicationSnapshotTable())
                && Objects.isNull(configuration.getReplicationSnapshotOrderByColumn())) {
            throw new PgCDCException("Replication Snapshot Table property is required.");
        }

        this.configuration = configuration;
        this.producer = producer;

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format(Constants.JDBC_CONNECTION_URL,
                configuration.getPgServer(),
                configuration.getPgPort(),
                configuration.getPgDatabase()));
        config.setUsername(configuration.getPgUser());
        config.setPassword(configuration.getPgPassword());

        this.dataSource = new HikariDataSource(config);
    }

    @Override
    public void start() throws SQLException, InterruptedException {
//        throw new UnsupportedOperationException();

        try (Connection connection = dataSource.getConnection()) {

            connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            connection.setReadOnly(true);

            final String schemaTable = configuration.getReplicationSnapshotTable();
            final String minMaxSql = String.format("select min(%s) as min, max(%s) as max from %s", configuration.getReplicationSnapshotOrderByColumn(), configuration.getReplicationSnapshotOrderByColumn(), schemaTable);

            long min;
            long max;
            try (final Statement statement = connection.createStatement()) {
                try (final ResultSet resultSet = statement.executeQuery(minMaxSql);) {
                    resultSet.next();
                    min = resultSet.getLong("min");
                    max = resultSet.getLong("max");
                }
            }
            logger.atInfo().log("Min=%s, Max=%s", min, max);


            String fetchSql = String.format("select row_to_json(tb) from %s tb where tb.%s between ? and ?", schemaTable, configuration.getReplicationSnapshotOrderByColumn());

            logger.atInfo().log("Sql query = %s", fetchSql);

            final int recordsPerQuery = configuration.getSnapshotReplicationRecordsPerQuery();

            for (long start = min, end = recordsPerQuery; (start <= max && !shutdownRequested); start = end, end += recordsPerQuery) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(fetchSql)) {
                    logger.atInfo().log("Querying for start = %s and end = %s", start, end);

                    preparedStatement.setFetchSize(recordsPerQuery);
                    preparedStatement.setLong(1, start);
                    preparedStatement.setLong(2, end);

                    try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                        while (resultSet.next()) {
                            final String jsonStr = resultSet.getString("row_to_json");
                            final JsonObject data = new JsonObject(jsonStr);

                            final JsonObject insertCDC = new JsonObject()
                                    .put(MaxwellBinlogKeys.DATABASE, schemaTable.split("\\.")[0])
                                    .put(MaxwellBinlogKeys.TABLE, schemaTable.split("\\.")[1])
                                    .put(MaxwellBinlogKeys.TYPE, "insert")
                                    .put(MaxwellBinlogKeys.TS, DateTimeUtil.convertToEpoch(LocalDateTime.now()))
                                    .put(MaxwellBinlogKeys.XID, -1)
                                    .put(MaxwellBinlogKeys.COMMIT, true)
                                    .put(MaxwellBinlogKeys.DATA, data);

                            producer.pushRow(insertCDC);
                        }
                    }
                }
            }

            shutdownCountDownLatch.countDown();
        }


    }

    @Override
    public CountDownLatch shutdown() {
        logger.atInfo().log("Shutdown requested");
        this.shutdownRequested = true;
        return shutdownCountDownLatch;
    }
}
