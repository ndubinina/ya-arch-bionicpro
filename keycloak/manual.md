
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
