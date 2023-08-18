package com.dineshsawant.pg.cdc.producer;

import com.google.common.flogger.FluentLogger;
import com.dineshsawant.pg.cdc.config.Configuration;
import com.dineshsawant.pg.cdc.exception.PgCDCException;
import com.dineshsawant.pg.cdc.transform.MaxwellBinlogKeys;
import io.vertx.core.json.JsonObject;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordResponse;

public class KinesisCDCProducerSDK extends AbstractCDCProducer {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private final String kinesisStreamName;
    private final KinesisClient kinesisClient;

    public KinesisCDCProducerSDK(Configuration configuration) {
        super(configuration);

        kinesisClient = KinesisClient.builder()
            .region(Region.of(configuration.getKinesisAwsRegion()))
            .build();
        this.kinesisStreamName = configuration.getKinesisStreamName();
    }

    @Override
    public void pushTransaction(JsonObject transactionJsonObject) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void pushRow(JsonObject rowJsonObject) {
        final String partitionKey = rowJsonObject.getString(MaxwellBinlogKeys.TABLE);

        final PutRecordRequest putRecordRequest = PutRecordRequest.builder()
            .partitionKey(partitionKey)
            .streamName(this.kinesisStreamName)
            .data(SdkBytes.fromUtf8String(rowJsonObject.encode()))
            .build();
        final PutRecordResponse putRecordResponse = kinesisClient.putRecord(putRecordRequest);

        if (putRecordResponse.sdkHttpResponse().isSuccessful()) {
            logger.atInfo().log("Successfully sent. Shard = %s", putRecordResponse.shardId());
        } else {
            throw new PgCDCException("Error when sending to kinesis : " + putRecordResponse.sdkHttpResponse().statusText());
        }

    }
}
