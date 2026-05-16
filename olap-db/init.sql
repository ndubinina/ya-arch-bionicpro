CREATE TABLE IF NOT EXISTS emg_sensor_data (
    user_id UInt32,
    prosthesis_type String,
    muscle_group String,
    signal_frequency UInt32,
    signal_duration UInt32,
    signal_amplitude Decimal(5,2),
    signal_time DateTime
) ENGINE = MergeTree()
ORDER BY (user_id, prosthesis_type, signal_time);

CREATE TABLE IF NOT EXISTS user_daily_reports_mart
(
    user_id UInt32,
    name String,
    email String,
    country String,
    report_day Date,
    signals_count UInt32,
    avg_amplitude Float64,
    avg_duration Float64,
    total_duration UInt64
)
ENGINE = MergeTree()
ORDER BY (email, report_day);


CREATE TABLE kafka_customers
(
    id UInt32,
    name String,
    email String,
    country String,
    __op LowCardinality(String)
)
ENGINE = Kafka
SETTINGS
    kafka_broker_list = 'kafka:9092',
    kafka_topic_list = 'crm.public.customers',
    kafka_group_name = 'clickhouse_customers',
    kafka_format = 'JSONEachRow',
    kafka_num_consumers = 1;

CREATE TABLE customers_cdc
(
    user_id UInt32,
    name String,
    email String,
    country String,
    is_deleted UInt8
)
ENGINE = ReplacingMergeTree()
ORDER BY user_id;


CREATE MATERIALIZED VIEW mv_customers_cdc
TO customers_cdc
AS
SELECT
    id AS user_id,
    name,
    email,
    country,
    (__op = 'd') AS is_deleted
FROM kafka_customers;


CREATE TABLE IF NOT EXISTS reports_agg
(
    user_id UInt32,
    report_day Date,
    signals_count UInt32,
    avg_amplitude Float64,
    avg_duration Float64,
    total_duration UInt64
)
ENGINE = MergeTree()
ORDER BY (user_id, report_day);


CREATE MATERIALIZED VIEW mv_reports_agg
TO reports_agg
AS
SELECT
    user_id,
    toDate(signal_time) AS report_day,
    count() AS signals_count,
    avg(signal_amplitude) AS avg_amplitude,
    avg(signal_duration) AS avg_duration,
    sum(signal_duration) AS total_duration
FROM emg_sensor_data
GROUP BY
    user_id,
    report_day;

INSERT INTO emg_sensor_data
SELECT *
FROM file('olap.csv', 'CSV');