package com.dineshsawant.pg.cdc.producer;

import com.amazonaws.services.kinesis.producer.KinesisProducer;
import com.amazonaws.services.kinesis.producer.KinesisProducerConfiguration;
import com.amazonaws.services.kinesis.producer.UserRecordResult;
import com.google.common.flogger.FluentLogger;
import com.dineshsawant.pg.cdc.config.Configuration;
import com.dineshsawant.pg.cdc.transform.MaxwellBinlogKeys;
import io.vertx.core.json.JsonObject;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.google.common.flogger.LazyArgs.lazy;

public class KinesisCDCProducer extends AbstractCDCProducer {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private final KinesisProducer kinesisProducer;
    private final String kinesisStreamName;

    public KinesisCDCProducer(Configuration configuration) {
        super(configuration);

        KinesisProducerConfiguration kinesisProducerConfiguration = new KinesisProducerConfiguration()
            .setRecordMaxBufferedTime(3000)
            .setMaxConnections(1)
            .setRequestTimeout(60000)
            .setRegion(configuration.getKinesisAwsRegion())
            .setMetricsLevel(configuration.getKinesisMetricLevel())
            .setLogLevel("info");

        this.kinesisProducer = new KinesisProducer(kinesisProducerConfiguration);
        this.kinesisStreamName = configuration.getKinesisStreamName();
    }

    @Override
    public void pushTransaction(JsonObject transactionJsonObject) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void pushRow(JsonObject rowJsonObject) {
        ByteBuffer data = ByteBuffer.wrap(rowJsonObject.encode().getBytes(StandardCharsets.UTF_8));
        final String partitionKey = rowJsonObject.getString(MaxwellBinlogKeys.TABLE);
        final Future<UserRecordResult> future = kinesisProducer.addUserRecord(kinesisStreamName, partitionKey, data);

        try {
            // Wait for puts to finish and check the results
            // this does block
            UserRecordResult result = future.get();
            if (result.isSuccessful()) {
                logger.atInfo().every(100).log("Put record into shard %s", lazy(result::getShardId));
            } else {
                logger.atSevere().log("Failed to put record. Attempts = %s", result.getAttempts());
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.atSevere().withCause(e).log("Failed to get result from put record.");
        }

    }
}
