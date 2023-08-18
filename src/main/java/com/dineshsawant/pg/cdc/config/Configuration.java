package com.dineshsawant.pg.cdc.config;

import com.dineshsawant.pg.cdc.state.StateStore;
import com.fasterxml.jackson.databind.annotation.JsonAppend;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration {
    private Properties configProperties;

    public Configuration(String configFilePath) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(configFilePath);
        this.configProperties = new Properties();
        this.configProperties.load(fileInputStream);
    }

    public String getProducerType() {
        return configProperties.getProperty(PropertyNames.PRODUCER_TYPE);
    }

    public long getReplicationSleepInMillis() {
        return Long.parseLong(configProperties.getProperty(PropertyNames.REPLICATION_SLEEP_IN_MILLIS_AFTER_NULL_LOG, "10"));
    }

    public String getPgServer() {
        return configProperties.getProperty(PropertyNames.PG_SERVER);
    }

    public String getPgPort() {
        return configProperties.getProperty(PropertyNames.PG_PORT);
    }

    public String getPgDatabase() {
        return configProperties.getProperty(PropertyNames.PG_DATABASE);
    }

    public String getPgUser() {
        return configProperties.getProperty(PropertyNames.PG_USER);
    }

    public String getPgPassword() {
        return configProperties.getProperty(PropertyNames.PG_PASSWORD);
    }

    public String getPgAssumeMinServerVersion() {
        return configProperties.getProperty(PropertyNames.PG_ASSUME_MINIMUM_SERVER_VERSION);
    }

    public String getReplicationSlotName() {
        return configProperties.getProperty(PropertyNames.REPLICATION_SLOT_NAME);
    }

    public String getLogLevel() {
        return configProperties.getProperty(PropertyNames.LOG_LEVEL);
    }

    public String getWebhookUrl() {
        return configProperties.getProperty(PropertyNames.PRODUCER_WEBHOOK_URL);
    }

    public String getReplicationAddTables() {
        return configProperties.getProperty(PropertyNames.REPLICATION_ADD_TABLES);
    }

    public String getReplicatorType() {
        return configProperties.getProperty(PropertyNames.REPLICATOR_TYPE);
    }

    public String getReplicationSnapshotTable() {
        return configProperties.getProperty(PropertyNames.REPLICATION_SNAPSHOT_TABLE);
    }

    public String getReplicationSnapshotOrderByColumn() {
        return configProperties.getProperty(PropertyNames.REPLICATION_SNAPSHOT_ORDER_BY_COLUMN);
    }

    public int getSnapshotReplicationRecordsPerQuery() {
        return Integer.parseInt(configProperties.getProperty(PropertyNames.REPLICATION_SNAPSHOT_RECORDS_PER_QUERY, "1000"));
    }

    public String getStateStoreType() {
        return configProperties.getProperty(PropertyNames.STATE_STORE_TYPE, StateStore.JSONFILE);
    }

    public String getStateStoreJsonFilePath() {
        return configProperties.getProperty(PropertyNames.STATE_STORE_JSONFILE_PATH, "state.json");
    }

    public String getKinesisAwsRegion() {
        return configProperties.getProperty(PropertyNames.PRODUCER_KAFKA_RETRIES);
    }

    public String getKinesisStreamName() {
        return configProperties.getProperty(PropertyNames.PRODUCER_KAFKA_LINGER_MS);
    }

    public String getKinesisMetricLevel() {
        return configProperties.getProperty(PropertyNames.PRODUCER_KINESIS_METRIC_LEVEL);
    }

    public String getKafkaTopic() {
        return configProperties.getProperty(PropertyNames.PRODUCER_KAFKA_TOPIC);
    }

    public String getKafkaBoostrapServers() {
        return configProperties.getProperty(PropertyNames.PRODUCER_KAFKA_BOOTSTRAP_SERVERS);
    }
}
