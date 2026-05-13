# ASH – Spring Boot Multi-Datasource Training Project

Spring Boot 3 · Java 21 · Oracle · Kong Gateway · Keycloak · Redis · Kafka · MapStruct

---

## Tech Stack

| | |
|-|-|
| Framework | Spring Boot 3.5.7 |
| Gateway | Kong 3.7 (DB-less, declarative config) |
| Auth Server | Keycloak 24 (OAuth2 / OpenID Connect) |
| Security | Spring Security 6 – claim-based, no local JWT verification |
| ORM | Spring Data JPA / Hibernate, HikariCP |
| Cache | Redis |
| Messaging | Kafka |
| HTTP Client | OpenFeign |
| Mapping | MapStruct 1.5.5 · Lombok 1.18.30 |

---

## Architecture

```
┌─────────┐        ┌───────────────────────────────────────────┐
│   FE    │        │              Kong Gateway :8000            │
│ (client)│──────► │                                           │
└─────────┘        │  1. Authenticate  ──────► Keycloak :8180  │
                   │     (get token)   ◄──────  (issues JWT)   │
                   │                                           │
                   │  2. Call API      ──────► Verify JWT       │
                   │     + Bearer JWT           (RS256 + exp)  │
                   │                                           │
                   │  3. Route request ──────► Spring Boot :8080│
                   │     (if valid)              (RBAC only)   │
                   └───────────────────────────────────────────┘
```

**Responsibilities:**

| Layer | Responsibility |
|-------|---------------|
| **Kong Gateway** | JWT signature verification, token expiry, routing |
| **Keycloak** | User management, token issuance, roles, OAuth2/OIDC |
| **Spring Boot** | Business logic, role-based authorization (RBAC) |

Kong is the **single entry point** for all traffic. Requests that fail JWT verification are rejected at the gateway and never reach the application.

---

## Quick Start

```bash
# Start all infrastructure
docker compose up -d

# Wait for Keycloak to be healthy, then start the app
./mvnw spring-boot:run
```

> Oracle needs manual user setup after first boot — see [Oracle setup](#oracle-setup).

All API calls go through Kong on port **8000**. Port 8080 (direct app) is for local development only.

---

## Kong Gateway

Config file: `kong/kong.yml` (declarative, DB-less mode)

### Routing table

| Path prefix | Upstream | JWT required |
|-------------|----------|:---:|
| `/realms/**` | Keycloak `:8080` | No |
| `/auth/**` | Spring Boot `:8080` | No |
| `/actuator/**` | Spring Boot `:8080` | No |
| `/health` | Spring Boot `:8080` | No |
| `/**` | Spring Boot `:8080` | **Yes** |

### JWT verification

Kong uses the built-in **JWT plugin** on all protected routes:
- Algorithm: `RS256`
- Key lookup: `iss` claim must equal `http://localhost:8180/realms/ash`
- Claims verified: `exp`
- Public key: Keycloak `ash` realm RSA key (stored in `kong/kong.yml`)

If verification fails, Kong returns `401 Unauthorized` before the request touches the app.

### Admin API

```bash
# Inspect loaded routes
curl http://localhost:8001/routes

# Inspect loaded consumers / JWT secrets
curl http://localhost:8001/consumers/keycloak/jwt
```

---

## Authentication & Authorization

### Flow

```
1. FE  ──►  POST /realms/ash/protocol/openid-connect/token
                 (via Kong → Keycloak)
            ◄──  { access_token: "<keycloak-jwt>" }

2. FE  ──►  GET /categories
                Authorization: Bearer <keycloak-jwt>
                (via Kong)
            Kong: verify RS256 signature + exp  ──►  401 if invalid
            Kong: route to Spring Boot          ──►  app reads claims, applies RBAC
```

### Keycloak test users

Auto-imported from `src/main/resources/keycloak/ash-realm.json`:

| Username | Password | Realm roles |
|----------|----------|-------------|
| `mobile_user` | `mobile123` | `user` |
| `mobile_admin` | `admin123` | `user`, `admin` |

Roles are mapped from `realm_access.roles` and `resource_access.ash-mobile.roles` in the JWT to Spring `ROLE_USER` / `ROLE_ADMIN` authorities.

### Access rules

| Path | Rule |
|------|------|
| `/auth/**`, `/health`, `/actuator/**` | Public |
| `DELETE /**` | `ROLE_ADMIN` |
| everything else | authenticated (any valid Keycloak token) |

### Spring Boot security

The app **does not verify** JWT signatures — Kong already did. `KeycloakAuthenticationFilter` only decodes the forwarded token payload to populate the Spring `SecurityContext` with the user's identity and roles.

---

## Project Structure

```
kong/
└── kong.yml             # Declarative gateway config (routes, JWT plugin, consumers)

src/main/
├── resources/
│   ├── application.properties   # server, Redis, Kafka, Keycloak
│   ├── application.yml          # imports config/datasource.yml
│   ├── config/datasource.yml    # DB URLs + HikariCP pool
│   ├── keys/private.pem         # RSA-2048 private key (local dev use only)
│   ├── keys/public.pem          # RSA-2048 public key
│   ├── keycloak/ash-realm.json  # Keycloak realm auto-import
│   └── logback-spring.xml       # Plain (default) or JSON (profile: log-json)
│
└── java/org/example/ash/
    ├── aop/              # @ValidateProduct, @Signature aspects
    ├── client/           # Feign → ServiceB
    ├── config/
    │   ├── datasource/
    │   │   ├── BaseJpaConfig.java
    │   │   ├── OracleJpaConfig.java        # @Primary, @MultiDataSource(ORACLE)
    │   │   ├── PostgresJpaConfig.java      # @MultiDataSource(POSTGRESQL)
    │   │   └── annotation/MultiDataSource.java
    │   ├── DynamicDataSourceConfig.java    # oracleJdbcTemplate / postgresJdbcTemplate beans
    │   └── RedisConfig.java
    ├── configuration/    # GlobalExceptionHandler, RequestContext, WebConfig
    ├── controller/       # AuthController, CategoryController, ProductController
    ├── dto/              # request/ · response/ (BaseResponse, LoginResponse)
    ├── entity/oracle/    # Product, Category, User, Role
    ├── exception/        # AppException, AppCode, ThrottleException…
    ├── mapper/           # EntityMapper<D,E,R>, CategoryMapper
    ├── repository/oracle/
    ├── security/
    │   ├── PasswordEncoderConfig.java
    │   ├── RsaKeyConfig.java
    │   ├── JwtTokenProvider.java            # local RS256 (dev / direct-access only)
    │   ├── JwtAuthenticationFilter.java     # not in filter chain; Kong owns JWT auth
    │   ├── SecurityConfig.java              # filter chain: Keycloak claims + RBAC rules
    │   └── keycloak/
    │       ├── KeycloakJwtAuthenticationConverter.java  # realm_access + resource_access → GrantedAuthority
    │       └── KeycloakAuthenticationFilter.java        # decodes forwarded token, sets SecurityContext
    └── service/          # AuthService, CategoryService, ProductService, CacheService
```

---

## API Endpoints

All requests go through Kong (`http://localhost:8000`).

| Method | Path | Auth | Notes |
|--------|------|------|-------|
| POST | `/auth/register` | public | register local user |
| POST | `/auth/login` | public | local JWT (direct app only) |
| GET | `/categories` | ✓ | |
| GET | `/categories/{id}` | ✓ | |
| POST | `/categories` | ✓ | |
| GET | `/products` | ✓ | |
| GET | `/products/{id}` | ✓ | Redis cached |
| POST | `/products` | ✓ | AOP name validation |
| DELETE | `/products` | ADMIN | cache evicted |
| POST | `/products/dynamic-db` | ✓ | Oracle JdbcTemplate |

### Examples

```bash
# 1. Get a Keycloak token (through Kong)
TOKEN=$(curl -s -X POST \
  http://localhost:8000/realms/ash/protocol/openid-connect/token \
  -d "grant_type=password&client_id=ash-mobile&username=mobile_admin&password=admin123" \
  | jq -r '.access_token')

# 2. Call a protected endpoint through Kong
curl http://localhost:8000/categories \
  -H "Authorization: Bearer $TOKEN"

# 3. Admin-only delete through Kong
curl -X DELETE http://localhost:8000/products/1 \
  -H "Authorization: Bearer $TOKEN"
```

---

## Infrastructure (docker-compose)

| Container | Port | Credentials |
|-----------|------|-------------|
| `ash-kong` | 8000 (proxy) · 8001 (admin) | — |
| `ash-keycloak` | 8180 | admin / admin |
| `ash-keycloak-postgres` | 5433 → container 5432 | `keycloak_user` / `keycloak_pass` / `keycloak_db` |
| `ash-oracle` | 1521 | SYS/SYSTEM: `demo_pass` |
| `ash-redis` | 6379 | — |
| `ash-kafka` | 9092 (host) · 29092 (internal Docker) | — |
| `ash-kafka-ui` | 8090 | — · open `http://localhost:8090` |

### Oracle setup

```bash
docker exec -it ash-oracle sqlplus sys/demo_pass@FREEPDB1 as sysdba
```
```sql
CREATE USER demo_user IDENTIFIED BY demo_pass;
GRANT CONNECT, RESOURCE TO demo_user;
ALTER USER demo_user QUOTA UNLIMITED ON USERS;
```

---

## Multi-Datasource Config

```
application.properties  → imports config/datasource.yml
config/datasource.yml   → custom.multi-databases.{oracle,postgres}  +  datasource.pool.{oracle,postgres}

BaseJpaConfig (abstract)
├── OracleJpaConfig   @Primary  → repository.oracle  / entity.oracle
└── PostgresJpaConfig           → repository.postgres / entity.postgres

DynamicDataSourceConfig → @Bean oracleJdbcTemplate / postgresJdbcTemplate
```

---

## Mapper Contract

```
EntityMapper<D,E,R>  (D=DTO · E=Entity · R=Request)

toDto(E)→D   toEntity(R)→E   fromDto(D)→E
toListDto / toListEntity
toDto(Optional<E>) / toEntity(Optional<R>) / fromDto(Optional<D>)
partialUpdate(@MappingTarget E, D)   ← null-safe merge
```

---

## BaseResponse

```json
{ "status": 200, "data": {…} }
{ "status": 400, "error": "message" }
```
Factories: `BaseResponse.ok(data)` · `.created(data)` · `.badRequest(msg)` · `.internalError(msg)` · `.error(HttpStatus, msg)`

---

## Logging

Configured in `logback-spring.xml` using `<springProfile>`:

| Active profile | Console | File |
|----------------|---------|------|
| _(default)_ | Colored plain text | `logs/ash.log` (plain) |
| `log-json` | Logstash JSON | `logs/ash-json.log` |

Enable JSON logging:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=log-json
```
