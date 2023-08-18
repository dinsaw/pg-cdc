package com.dineshsawant.pg.cdc.producer;

import com.dineshsawant.pg.cdc.config.Configuration;
import com.dineshsawant.pg.cdc.exception.PgCDCException;
import com.dineshsawant.pg.cdc.transform.MaxwellBinlogKeys;
import com.google.common.flogger.FluentLogger;
import io.vertx.core.json.JsonObject;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.google.common.flogger.LazyArgs.lazy;

public class KafkaCDCProducer extends AbstractCDCProducer {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private Producer<String, String> kafkaProducer;
    private String topic;

    public KafkaCDCProducer(Configuration configuration) {
        super(configuration);
        Properties props = new Properties();
        props.put("bootstrap.servers", configuration.getKafkaBoostrapServers());
        props.put("acks", "all");
        props.put("retries", 1);
        props.put("linger.ms", 1);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        this.kafkaProducer = new KafkaProducer<>(props);
        this.topic = configuration.getKafkaTopic();
    }

    @Override
    public void pushTransaction(JsonObject binlogTransactionJson) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void pushRow(JsonObject rowJsonObject)  {
        try {
            final String partitionKey = rowJsonObject.getString(MaxwellBinlogKeys.TABLE);
            final ProducerRecord<String, String> record = new ProducerRecord<>(this.topic, partitionKey, rowJsonObject.encode());
            final Future<RecordMetadata> future = this.kafkaProducer.send(record);
            final RecordMetadata recordMetadata = future.get();
            logger.atInfo().log("Sent record to kafka partition %d at offset %d",
                lazy(recordMetadata::partition), lazy(recordMetadata::offset));
        } catch (ExecutionException | InterruptedException e) {
            throw new PgCDCException("Error when sending to Kafka.", e);
        }
    }
}