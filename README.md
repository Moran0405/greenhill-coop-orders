# Greenhill Food Co-op - Weekly Ordering System

A small web application for a **volunteer-run community food co-operative** in
Moorooka, Brisbane. The co-op organises one food pick-up per week: ordering
opens on **Monday**, closes on **Sunday at 8 pm**, and members collect their
food on **Thursday**. This system replaces the paper forms, spreadsheets and
email threads previously used to collect and add up every household's order.

## Core features

| User story | Where |
|---|---|
| As a member I can view this week's catalogue (name + price) | Member page |
| As a member I can submit an order while the round is open | Member page |
| As a member I can change or cancel my order before the cut-off | Member page |
| As a member I cannot see other members' orders | Enforced server side |
| As the coordinator I can maintain the catalogue (add / edit / deactivate) | Coordinator desk |
| As the coordinator I can open / close / pack weekly rounds | Coordinator desk |
| As the coordinator I can view all orders and the total purchase quantity per product | Coordinator desk |

Products support two pricing modes:

* **Whole unit** - e.g. a box of eggs, a jar of olive oil (whole numbers only)
* **By weight** - e.g. oats, rice, vegetables (decimal quantities such as 1.5 kg)

Out of scope by design: payments, invoices, notifications, complex reporting
and any external system integration.

## Technology stack

| Layer | Choice |
|---|---|
| Language | Java 8 (JDK 1.8) |
| Framework | Spring Boot 2.7.18 (Web, Data JPA, Validation) |
| Database | MySQL 8.0 (H2 in-memory for tests only) |
| Front end | Plain HTML / CSS / JavaScript served by Spring Boot |
| Build tool | Apache Maven 3.6.1 |
| Version control | Git with feature-branch workflow |

## Getting started

### 1. Database

Create the MySQL schema and user:

```sql
CREATE DATABASE greenhill_coop CHARACTER SET utf8mb4;
CREATE USER 'coop_user'@'localhost' IDENTIFIED BY 'coop_pass';
GRANT ALL PRIVILEGES ON greenhill_coop.* TO 'coop_user'@'localhost';
```

Optionally pre-create the schema and load the sample data (members,
catalogue, two weekly rounds and example orders) from
`src/main/resources/db/seed.sql`; otherwise Hibernate creates the
tables on first start-up.

### 2. Build and run

```bash
mvn clean package
java -jar target/greenhill-coop-orders-1.0.0.jar
```

The application starts on <http://localhost:8080>.

* Member page: `http://localhost:8080/index.html` (sign in with a registered email)
* Coordinator desk: `http://localhost:8080/coordinator.html`
  (default pass phrase `coop-admin-2026`, see Configuration)

### 3. Run the automated tests

```bash
mvn test
```

Tests use an H2 in-memory database, so no MySQL server is required.

## Configuration management

Configuration is separated per environment:

* `application.properties` - development defaults, database credentials can be
  overridden with the `DB_USERNAME` / `DB_PASSWORD` environment variables
* `application-prod.properties` - production profile
  (`--spring.profiles.active=prod`), reads every secret from environment
  variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `COORDINATOR_PASSWORD`

See `docs/CONFIGURATION.md` for the full variable list.

## Documentation

* `CHANGELOG.md` - notable changes per release
* `docs/CONFIGURATION.md` - deployment configuration reference

## Limitations

* Sign-in is intentionally simple (email based for members, one shared pass
  phrase for the coordinator); it is not a full security system.
* No payment, notification or accounting integration.
* One open round at a time is the expected operating mode.
