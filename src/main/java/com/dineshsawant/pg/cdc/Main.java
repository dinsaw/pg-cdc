package com.dineshsawant.pg.cdc;

import com.google.common.flogger.FluentLogger;
import com.dineshsawant.pg.cdc.config.Configuration;
import com.dineshsawant.pg.cdc.producer.AbstractCDCProducer;
import com.dineshsawant.pg.cdc.replicator.PgReplicator;

import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    public static void main(String[] args) {
        try {
            String configFilePath = args[0];
            logger.atInfo().log("Using configuration from %s", configFilePath);

            final Configuration configuration = new Configuration(configFilePath);

            final AbstractCDCProducer producer = AbstractCDCProducer.createProducer(configuration);
            final PgReplicator pgReplicator = PgReplicator.createPagReplicator(configuration, producer);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    pgReplicator.shutdown().await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }));

            pgReplicator.start();
        } catch (Exception e) {
            logger.atSevere().withCause(e).log("Exception in main");
        }
    }

    public static void setLevel(Level targetLevel) {
        Logger root = Logger.getLogger("");
        root.setLevel(targetLevel);
        for (Handler handler : root.getHandlers()) {
            handler.setLevel(targetLevel);
        }
    }
}
