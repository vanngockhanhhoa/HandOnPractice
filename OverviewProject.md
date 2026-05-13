# DoDoDo — Project Overview

## Architecture

This application follows a **microservice architecture**.

---

## Services

### KeycloakAuth
- Hosts a **Keycloak** instance for identity and access management
- Responsible for token issuance, login flows, and user management
- Exposes JWKS endpoint consumed by Kong for token verification

### KongGateway
- Standalone **Kong API Gateway** — single entry point for all client requests
- Verifies JWT tokens via Keycloak's JWKS endpoint
- Routes requests to downstream services
- Enforces cross-cutting concerns: rate limiting, CORS, request filtering

### OrderService
- Handles all **Order** domain logic (create, read, update, cancel orders, etc.)
- Accessed exclusively through KongGateway

---

## Folder Structure

```
DoDoDo/
├── KeycloakAuth/       # Keycloak config, realm exports, auth logic
│   ├── docker-compose.yml   # Oracle, Redis, Kafka, Keycloak — defines ash-network
│   └── src/
├── KongGateway/        # Kong declarative config, plugins, routing rules
│   ├── docker-compose.yml   # Kong service — joins ash-network (external)
│   └── kong.yml             # DB-less declarative config (routes, JWT plugin)
└── OrderService/       # Order domain
    └── docker-compose.yml
```

## Startup Order

1. `KeycloakAuth/` — starts first; creates the shared Docker network `ash-network`
2. `KongGateway/` — starts after Keycloak is healthy; joins `ash-network` to reach `keycloak:8080`
3. `OrderService/` — starts independently