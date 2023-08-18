package com.dineshsawant.pg.cdc.replicator;

import com.dineshsawant.pg.cdc.config.Configuration;
import com.dineshsawant.pg.cdc.exception.PgCDCException;
import com.dineshsawant.pg.cdc.producer.AbstractCDCProducer;
import com.dineshsawant.pg.cdc.state.StateStore;

import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;

public interface PgReplicator {
    void start() throws SQLException, InterruptedException;
    CountDownLatch shutdown();

    static PgReplicator createPagReplicator(Configuration configuration, AbstractCDCProducer abstractCDCProducer) {
        switch (configuration.getReplicatorType()) {
            case "snapshot":
                return new SnapshotPgReplicator(configuration, abstractCDCProducer);
            case "cdc":
                return new CDCPgReplicator(configuration, abstractCDCProducer, StateStore.createStore(configuration));
            default:
                throw new PgCDCException("Unable to find replicator for type " + configuration.getReplicatorType());
        }

    }
}
