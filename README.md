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

Скриншоты:
пользователь из keycloack
- [Логин под пользователем из keycloack](./screenshots/login/1.png)
- [ввод OTP кода](./screenshots/login/2.png)
- [успешный логин](./screenshots/login/3.png)
- [Загрузка отчета](./screenshots/login/4.png)
- [Попадание отчета в minio](./screenshots/login/5.png)
- [логи bioreport-auth](./screenshots/login/6.png)

пользователь из ldap
- [Логин под пользователем из LDAP](./screenshots/loginLDAP/1.png)
- [Пароль под пользователем из LDAP](./screenshots/loginLDAP/2.png)
- [Требование OTP кода](./screenshots/loginLDAP/3.png)
- [ввод OTP кода](./screenshots/loginLDAP/4.png)
- [Попадание отчета в minio](./screenshots/loginLDAP/5.png)

пользователь из яндекс
- [Логин под пользователем яндекс](./screenshots/loginLDAP/1.png)
- [Пароль под пользователем яндекс](./screenshots/loginLDAP/2.png)
- [Попадание отчета в minio](./screenshots/loginLDAP/3.png)

## Задание 2. Разработка сервиса отчётов

[Диаграмма в drawio](./etl.drawio)

[Ручные настройки airflow](./airflow/manual.md)

Скриншоты:
- [пустая таблицы с отчета в clickhouse](./screenshots/airflow/1.png)
- [Запуск airflow](./screenshots/airflow/2.png)
- [Заполненная таблица с отчетами в clickhouse](./screenshots/airflow/3.png)
- [Вход под пользователем с имеющимся отчетом](./screenshots/airflow/4.png)
- [Отчет в браузере](./screenshots/airflow/5.png)
- [Отчет в minio](./screenshots/airflow/6.png)

## Задание 3. Снижение нагрузки на базу данных

- создать bucket: http://localhost:9001 (minio_user/minio_password): create bucket -> reports

Видно по скриншотам из пункта 1

## Задание 4. Повышение оперативности и стабильности работы CRM

- настраиваем debezium, выполнив curl [отсюда](./debezium/debezium-connector)

Скриншоты:
- [События в кафке](./screenshots/debezium/1.png)
- [Конкретное событие в кафке](./screenshots/debezium/2.png)
- [Оффсет пользователя](./screenshots/debezium/3.png)
- [Таблица с отчетом в clickhouse](./screenshots/debezium/4.png)
- [Удалила из minio отчет пользователя alexis_moore](./screenshots/debezium/5.png)
- [Логин под alexis_moore после переключения на чтение из свежей таблице отчетов](./screenshots/debezium/6.png)
- [Отчет в браузере](./screenshots/debezium/7.png)
- [Отчет в minio](./screenshots/debezium/8.png)