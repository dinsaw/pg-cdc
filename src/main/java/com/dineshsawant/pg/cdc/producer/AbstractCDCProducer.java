package com.dineshsawant.pg.cdc.producer;

import com.dineshsawant.pg.cdc.config.Configuration;
import com.dineshsawant.pg.cdc.exception.PgCDCException;
import io.vertx.core.json.JsonObject;

public abstract class AbstractCDCProducer {
    protected Configuration configuration;

    public AbstractCDCProducer(Configuration configuration) {
        this.configuration = configuration;
    }

    public static AbstractCDCProducer createProducer(Configuration configuration) {
        final String producerType = configuration.getProducerType();
        switch (producerType) {
            case "webhook":
                return new WebhookCDCProducer(configuration);
            case "kinesis":
                return new KinesisCDCProducerSDK(configuration);
            case "kafka":
                return new KafkaCDCProducer(configuration);
            default:
                throw new PgCDCException("Unable to create producer for type " + producerType);
        }
    }

    abstract public void pushTransaction(JsonObject transactionJsonObject);
    abstract public void pushRow(JsonObject rowJsonObject);
}
