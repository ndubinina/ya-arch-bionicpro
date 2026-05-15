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

INSERT INTO emg_sensor_data
SELECT *
FROM file('olap.csv', 'CSV');

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