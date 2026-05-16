## Задание 1. Повышение безопасности системы

### Задача 1. Архитектурное решение 

Авторизацией занимается bionicpro-auth на бэкенде, на фронтенд access_token и refresh_token не уходят. Frontend взаимодействует с системой через защищённую HTTP-only cookie.
bionicpro-auth взаимодействует с keycloack, который может подключать внешние серверы аутентификации(в том числе для пользователей из других регионов)
bionicpro-auth также отвечает за обновление access_token по refresh_token, и ротацию сессии.
Для хранения сессионой инфоррмации keycloack использует postgres (в демо версии пока сессионные данные храняться в RAM сервиса bionicpro-auth, но это не целевое решенеи)
Используется PKCE для лучшей безопасности (keycloack может потвердить доступ, только если был предоставлен верный verification_code, который хранится только в bionicpro-auth)

[Диаграмма в drawio](./BionicPRO_C4_model.drawio)
[Диаграмма в drawio](./keycloak/keycloak-results-export.json)

// TODO: схропнуть
Настройки keycloack:
- локально отключить требование https:
docker exec -it ya-arch-bionicpro-keycloak-1 bash
/opt/keycloak/bin/kcadm.sh config credentials \
  --server http://localhost:8080 \
  --realm master \
  --user admin \
  --password admin
/opt/keycloak/bin/kcadm.sh update realms/master -s sslRequired=NONE
- Возможно надо будет поправить для reports-realm: Realm settings -> General -> Require SSL -> None

- переименовываем клиента bionicpro-auth: Clients -> reports-frontend -> 
  Client ID: bionicpro-auth
  Valid redirect URI: http://localhost:8081/*
  Web origin: http://localhost:8081
  Require PKCE: true

- Realm settings -> Tokens:
 Access token: 2 min
 Revike token: Enabled

- настройка ldap:
User Federation -> Add ldap provider
  Vendor: Other
  Connection URL: ldap://openldap:389
  Bind DN: cn=admin,dc=example,dc=com
  Bind Credential: admin
  Edit mode: READ_ONLY
  Users DN: ou=People,dc=example,dc=com
  User object classes: inetOrgPerson

Action -> Synchronize all users

ldap -> Mapper -> Add mapper
  Mapper Type: group-ldap-mapper
  LDAP Roles DN: ou=Groups,dc=example,dc=com
  Mode:	READ_ONLY

- настройка OTP:
Authentication -> Flows -> Browser
  OTP form(и все выше по цепочке): Required

- настройка Яндекс ID
Identity providers -> Add OAuth v2 provider
  Alias: yandex 
  ClientId/ClientSecret из yandex auth
  В yandex auth: http://localhost:8080/realms/reports-realm/broker/yandex/endpoint
  Authorization URL: https://oauth.yandex.ru/authorize
  Token URL: https://oauth.yandex.ru/token
  User Info URL: https://login.yandex.ru/info
  ID Claim: id
  Username Claim: login
  default_email
  first_name
  first_name
  last_name
  Scope: login:email login:info
  First login flow override: first broker login

## Задание 2. Разработка сервиса отчётов

TODO: архитектура

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

## Задание 3. Снижение нагрузки на базу данных

- создать bucket
  http://localhost:9001 (minio_user/minio_password): create bucket -> reports


## Задание 4. Повышение оперативности и стабильности работы CRM

- настраиваем debezium
  curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d '{
  "name": "crm-connector",
  "config": {
  "connector.class": "io.debezium.connector.postgresql.PostgresConnector",

  "database.hostname": "crm_db",
  "database.port": "5432",
  "database.user": "debezium",
  "database.password": "dbz",
  "database.dbname": "crm_db",
  "transforms": "unwrap",
  "transforms.unwrap.type": "io.debezium.transforms.ExtractNewRecordState",
  "transforms.unwrap.drop.tombstones": "true",
  "transforms.unwrap.delete.handling.mode": "rewrite",
  "transforms.unwrap.add.fields": "op,table,lsn",
  "value.converter": "org.apache.kafka.connect.json.JsonConverter",
  "value.converter.schemas.enable": "false",
  "decimal.handling.mode": "double",
  "topic.prefix": "crm",

  "plugin.name": "pgoutput",

  "slot.name": "debezium",

  "publication.autocreate.mode": "filtered",

  "table.include.list": "public.customers",

  "snapshot.mode": "always"
  }
  }'

- пересоздаем crm_db
docker compose down crm_db
удаляем postgres-crm-data
docker compose up -d crm_db

docker compose down olap_db
удаляем clickhouse-data
docker compose up -d olap_db

curl -X DELETE http://localhost:8083/connectors/crm-connector