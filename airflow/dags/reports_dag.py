from datetime import datetime, timedelta

from airflow import DAG
from airflow.operators.python import PythonOperator
from airflow.providers.postgres.hooks.postgres import PostgresHook

import clickhouse_connect
import pandas as pd

CLICKHOUSE_HOST = "olap_db"
CLICKHOUSE_PORT = 8123

POSTGRES_CONN_ID = "crm_postgres"

def extract_crm(**context):
    hook = PostgresHook(postgres_conn_id=POSTGRES_CONN_ID)

    query = """
        SELECT
            id as user_id,
            name,
            email,
            country
        FROM customers;
    """

    df = hook.get_pandas_df(query)

    context['ti'].xcom_push(
        key="crm_data",
        value=df.to_json()
    )


def aggregate_telemetry(**context):
    client = clickhouse_connect.get_client(
        host=CLICKHOUSE_HOST,
        port=CLICKHOUSE_PORT
    )

    query = """
        SELECT
            user_id,
            toDate(signal_time) as report_day,
            count(*) as signals_count,
            avg(signal_amplitude) as avg_amplitude,
            avg(signal_duration) as avg_duration,
            sum(signal_duration) as total_duration
        FROM emg_sensor_data
        GROUP BY user_id, report_day
    """

    df = client.query_df(query)

    context['ti'].xcom_push(
        key="telemetry_data",
        value=df.to_json()
    )

def build_report(**context):

    ti = context['ti']

    crm_df = pd.read_json(ti.xcom_pull(key="crm_data"))
    telemetry_df = pd.read_json(ti.xcom_pull(key="telemetry_data"))
    telemetry_df["report_day"] = pd.to_datetime(
        telemetry_df["report_day"]
    ).dt.date

    mart = crm_df.merge(
        telemetry_df,
        on="user_id",
        how="inner"
    )

    client = clickhouse_connect.get_client(
        host=CLICKHOUSE_HOST,
        port=CLICKHOUSE_PORT
    )

    client.command("TRUNCATE TABLE user_daily_reports_mart")

    client.insert_df(
        "user_daily_reports_mart",
        mart
    )

default_args = {
    "owner": "bionicpro",
    "retries": 2,
    "retry_delay": timedelta(minutes=5),
}

with DAG(
    dag_id="reports_etl",
    start_date=datetime(2026, 1, 1),
    schedule_interval="0 0 * * *",
    catchup=False,
    default_args=default_args,
    tags=["bionicpro", "reports"],
) as dag:

    extract_crm_task = PythonOperator(
        task_id="extract_crm",
        python_callable=extract_crm
    )

    aggregate_telemetry_task = PythonOperator(
        task_id="aggregate_telemetry",
        python_callable=aggregate_telemetry
    )

    build_mart_task = PythonOperator(
        task_id="build_report",
        python_callable=build_report
    )

    [extract_crm_task, aggregate_telemetry_task] >> build_mart_task