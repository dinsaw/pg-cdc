## pg-cdc

pg-cdc consumes Change Data Capture(CDC) events using Postgresql's [Logical replication API](https://jdbc.postgresql.org/documentation/head/replication.html) and [wal2json](https://github.com/eulerto/wal2json). This service sends them to different configurable destinations like webhook, queue.

pg-cdc's output format is json.

#### Features
- Sends Postgres Binlog to webhook or other configurable destinations.
- Destinations Supported
  - Kinesis
  - Kafka
  - Webhook

### Supported PostgreSQL Versions 
I did my best to test this on following versions. 
- 9
- 10
- 11
- 12
- 13
- 14

#### Installation
- Make sure [wal2json](https://github.com/eulerto/wal2json is installed in postgres.
- Run `sudo ./buildAndInstall.sh demo`. This will install `pg-cdc.service` as a systemd service.
- Use `sudo systemctl start pg-cdc.service` to start the service.

#### Misc
- Find out current replication slots on Postgres.
```
SELECT
    slot_name,
    pg_size_pretty(pg_xlog_location_diff(pg_current_xlog_location(), restart_lsn)) AS replicationSlotLag,
    active
FROM
    pg_replication_slots;
```
- Delete replication slot.
    - `select pg_drop_replication_slot(<slot-name>);`
- PG Configs to look for.
```sqlite-psql
SHOW rds.logical_replication;
SHOW max_wal_senders;
SHOW wal_level;
SHOW max_replication_slots;
SHOW max_connections;
```
- Get PG activity
```sqlite-psql
SELECT * FROM pg_stat_activity WHERE datname = '<database_name?';
```