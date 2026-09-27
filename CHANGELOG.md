# Changelog

All notable changes to the Greenhill Food Co-op Weekly Ordering System are
documented here. This project follows [Keep a Changelog](https://keepachangelog.com/)
principles; each merge into `main` corresponds to one feature branch.

## [1.0.0] - 2026-09-27

### Added
- Project skeleton: Spring Boot 2.7.18 on Java 8, MySQL 8 driver, Maven build,
  per-environment configuration files (`application.properties`,
  `application-prod.properties`).
- **Catalogue management** (`feature/product-catalog`): products with whole-unit
  and by-weight pricing, add / update / deactivate, coordinator only.
- **Weekly rounds** (`feature/order-rounds`): OPEN -> CLOSED -> PACKED lifecycle
  driven by the coordinator; ordering is read only after the cut-off.
- **Member ordering** (`feature/member-ordering`): member sign-in, registration,
  one editable order per round, fractional quantities for weight products,
  whole numbers for unit products, cancellation while open.
- **Coordinator summary** (`feature/coordinator-summary`): all orders of a round
  plus aggregated purchase totals and estimated cost per product.
- **Web front end** (`feature/frontend`): warm market-style member page and
  coordinator desk, plain HTML/CSS/JS.
- **Automated tests** (`feature/automated-tests`): unit tests for the ordering,
  round, product and summary services plus a full MockMvc integration test of
  the weekly flow.
