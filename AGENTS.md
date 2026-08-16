# AGENTS.md

Spring Boot 4 (parent 4.1.0) reactive backend on **Java 25** (enforced by the maven-enforcer plugin).
Single Maven module, package `guru.springframework.spring6reactivemongo`. It is an OAuth2 JWT
resource server exposing a reactive REST API backed by MongoDB (Reactive Mongo), packaged as Docker
image and Helm chart.

## Build & test commands

- Full build: `./mvnw clean verify` — format checks, unit (`*Test`, surefire) + IT (`*IT`, failsafe)
  tests, Helm lint/template, OpenAPI generation. `./mvnw verify` also runs the unit tests.
- Unit tests only: `./mvnw test`. Single test: `./mvnw test -Dtest=BeerRestTest#methodName`.
- `./mvnw clean install` additionally builds the Docker image and packages the Helm chart into
  `target/helm/repo/`. Skip the Docker build with `-Dskip.docker.build=true` /
  `-Dskip.start.stop.springboot=true` (also skips the spring-boot app boot and the springdoc OpenAPI
  generation that depends on it).
- Start locally: `./mvnw spring-boot:run` (app on `:8083`, requires auth-server on `:9000`).

After changing code, always verify: run the relevant Maven goal above and report its output
(evidence, not just "done").

## Sandbox build quirk (required)

This sandbox mounts the repo via filesystem passthrough, which blocks symlinks. Spotless's
`npm install` (prettier) therefore fails with `EPERM` unless npm skips bin links:

```bash
export npm_config_bin_links=false    # export BEFORE running ./mvnw
```

Without it, `./mvnw validate` / `./mvnw verify` fail in the spotless step. On a normal host
(Windows/CI) this is not needed.

## Formatting is enforced (fails the `validate` phase)

- Java: Spring Java Format → fix with `./mvnw spring-javaformat:apply`.
- Everything else (pom.xml, `**/*.md`, json, `src/main/resources/application*.yaml`, `**/*.sh`):
  Spotless → fix with `./mvnw spotless:apply`.
- Spotless flexmark also formats markdown, so this file and any `.md` edits must stay flexmark-clean;
  run `./mvnw spotless:apply` after editing markdown.

## External dependency gotcha

- The auth-server (`spring-6-auth-server`) is resolved from the auth-server project's GitHub Packages
  (`maven.pkg.github.com`) / Helm repo (`repo.repsy.io/user08694146/helm-dboeckli`). Without a PAT in
  `~/.m2/settings.xml` (server id `github`) the build cannot resolve the snapshot dependency.

## Test conventions

- Naming matters: `*Test` = unit (surefire), `*IT` = integration (failsafe). A `*Test` class will
  not run during `verify`'s failsafe phase and vice versa.
- Tests use Testcontainers for MongoDB (`TestMongoDockerContainer`, e.g. `mongo:8.2.1`); Docker is
  required. `verify` needs Docker and a reachable auth-server.
- Assertions are reactive (WebTestClient / StepVerifier); do not block reactor chains.

## Architecture

- Layered reactive flow: `web/rest` (router/handler) → `service` → `repository` (reactive Mongo).
- Bootstrap data in `bootstrap/`, health indicators in `config/health/`.
- The app is an OAuth2 JWT resource server; issuer URI `http://localhost:9000`.

## Helm / Deploy

- Chart in `helm-charts/`, packaged to `target/helm/repo/<artifactId>-chart-<version>.tgz`, release
  name = chart dir name, namespace `spring-6-reactive-mongo`.
- CI (`.github/workflows/`): `maven-build.yml` builds + deploys snapshots and triggers
  `deploy-and-test-cluster.yml` (in-cluster, using the auth-server chart from Repro).
- Dependency updates are managed via `.github/dependabot.yml` and `.github/renovate.json`; validate
  changes with `renovate-config-validator`.