# Deployment configuration reference

## Profiles

| Profile | File | Purpose |
|---|---|---|
| (default) | `application.properties` | Local development against localhost MySQL 8 |
| `prod` | `application-prod.properties` | Production deployment, all secrets externalised |

Activate the production profile:

```bash
java -jar greenhill-coop-orders-1.0.0.jar --spring.profiles.active=prod
```

## Environment variables

| Variable | Used by | Description |
|---|---|---|
| `DB_URL` | prod | Full JDBC URL, e.g. `jdbc:mysql://dbhost:3306/greenhill_coop?useSSL=false&serverTimezone=Australia/Brisbane` |
| `DB_USERNAME` | both | MySQL user name |
| `DB_PASSWORD` | both | MySQL password |
| `COORDINATOR_PASSWORD` | both | Shared pass phrase for the coordinator desk |

## Notes

* Database tables are created/updated by Hibernate (`ddl-auto=update` in dev,
  `validate` in prod). The manual seed catalogue is in `db/seed.sql`.
* Server port defaults to 8080; override with `--server.port=...`.
* Tests run against H2 (`src/test/resources/application.properties`) and never
  touch a real database.
