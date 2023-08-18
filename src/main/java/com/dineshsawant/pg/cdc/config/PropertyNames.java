package com.dineshsawant.pg.cdc.config;

class PropertyNames {

    public static final String PG_SERVER = "pg.server",
            PG_PORT = "pg.port",
            PG_DATABASE = "pg.database",
            PG_USER = "pg.user",
            PG_PASSWORD = "pg.password",
            PG_ASSUME_MINIMUM_SERVER_VERSION = "pg.assumeMinServerVersion";

    public static final String REPLICATION_SLOT_NAME = "replication.slot.name",
            REPLICATION_SLEEP_IN_MILLIS_AFTER_NULL_LOG = "replication.sleepInMillisAfterNullLog",
            REPLICATION_ADD_TABLES = "replication.add-tables";
    public static final String REPLICATOR_TYPE = "replication.type";

    public static final String PRODUCER_TYPE = "producer.type";
    public static final String PRODUCER_WEBHOOK_URL = "producer.webhook.url";

    public static final String LOG_LEVEL = "log.level";
    public static final String REPLICATION_SNAPSHOT_TABLE = "replication.snapshot.table";
    public static final String REPLICATION_SNAPSHOT_ORDER_BY_COLUMN = "replication.snapshot.orderByColumn";
    public static final String REPLICATION_SNAPSHOT_RECORDS_PER_QUERY = "replication.snapshot.recordsPerQuery";
    public static final String STATE_STORE_TYPE = "state.store.type";
    public static final String STATE_STORE_JSONFILE_PATH = "state.store.jsonfile.path";
    public static final String PRODUCER_KINESIS_AWS_REGION = "producer.kinesis.awsRegion";
    public static final String PRODUCER_KINESIS_STREAM_NAME = "producer.kinesis.streamName";
    public static final String PRODUCER_KINESIS_METRIC_LEVEL = "producer.kinesis.metric.level";

    public static final String PRODUCER_KAFKA_BOOTSTRAP_SERVERS = "producer.kafka.bootstrap.servers";
    public static final String PRODUCER_KAFKA_RETRIES = "producer.kafka.retries";
    public static final String PRODUCER_KAFKA_LINGER_MS = "producer.kafka.linger.ms";
    public static final String PRODUCER_KAFKA_TOPIC = "producer.kafka.topic";
}
