## Задание 1. Повышение безопасности системы

### Задача 1. Архитектурное решение 

Авторизацией занимается bionicpro-auth на бэкенде, на фронтенд access_token и refresh_token не уходят. Frontend взаимодействует с системой через защищённую HTTP-only cookie.
bionicpro-auth взаимодействует с keycloack, который может подключать внешние серверы аутентификации(в том числе для пользователей из других регионов)
bionicpro-auth также отвечает за обновление access_token по refresh_token, и ротацию сессии.
Для хранения сессионой инфоррмации keycloack использует postgres (в демо версии пока сессионные данные храняться в RAM сервиса bionicpro-auth, но это не целевое решенеи)
Используется PKCE для лучшей безопасности (keycloack может потвердить доступ, только если был предоставлен верный verification_code, который хранится только в bionicpro-auth)

[Диаграмма в drawio](./BionicPRO_C4_model.drawio)

[keycloak-results-export.json](./keycloak/keycloak-results-export.json)

[Ручные настройки keycloack](./keycloak/manual.md) 

## Задание 2. Разработка сервиса отчётов

[Диаграмма в drawio](./etl.drawio)

[Ручные настройки airflow](./airflow/manual.md)

## Задание 3. Снижение нагрузки на базу данных

- создать bucket: http://localhost:9001 (minio_user/minio_password): create bucket -> reports


## Задание 4. Повышение оперативности и стабильности работы CRM

- настраиваем debezium, выполнив curl [отсюда](./debezium/debezium-connector)