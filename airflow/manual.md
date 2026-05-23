Настройка Airflow:
- docker exec -it ya-arch-bionicpro-airflow-1 bash
- airflow users create \
  --username admin1 \
  --firstname admin1 \
  --lastname admin1 \
  --role Admin \
  --email admin1@example.com \
  --password admin
- http://localhost:8080 (admin1 - admin)
-
- Admin -> Connections -> Add
- добавляем коннект к Postgres CRM:
  Conn Id: crm_postgres
  Conn Type: Postgres
  Host: crm_db
  Schema: crm_db
  Login: crm_user
  Password: crm_password
  Port: 5432

- запускаем выгрузку - DAGs -> reports_etl -> Trigger DAG
- проверяем, что данные прогрузились - SELECT * from user_daily_reports_mart в http://localhost:8123
    - добавляем пользователя в keycloack
      alexis_moore
      alexis.moore@example.com
      password