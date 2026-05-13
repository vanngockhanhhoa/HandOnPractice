#!/usr/bin/env bash
# Seed initial users into Keycloak via Admin REST API.
# Run once after a fresh docker compose up (or whenever you need to recreate users).
#
# Usage:
#   ./scripts/seed-keycloak-users.sh
#   ./scripts/seed-keycloak-users.sh --keycloak-url http://localhost:8180

set -euo pipefail

KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8180}"
REALM="ash"
ADMIN_USER="admin"
ADMIN_PASS="admin"

# ── Users to create ────────────────────────────────────────────────────────
# Format: "username|password|roles(comma-separated)"
USERS=(
  "mobile_user|mobile123|user"
  "mobile_admin|admin123|user,admin"
)

# ── Get admin token ────────────────────────────────────────────────────────
echo "→ Authenticating as Keycloak admin..."
ADMIN_TOKEN=$(curl -sf -X POST \
  "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" \
  -d "grant_type=password&client_id=admin-cli&username=$ADMIN_USER&password=$ADMIN_PASS" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['access_token'])")

AUTH_HEADER="Authorization: Bearer $ADMIN_TOKEN"

# ── Disable VERIFY_PROFILE (Keycloak 24 default) ──────────────────────────
echo "→ Disabling VERIFY_PROFILE required action..."
curl -sf -X PUT \
  "$KEYCLOAK_URL/admin/realms/$REALM/authentication/required-actions/VERIFY_PROFILE" \
  -H "$AUTH_HEADER" -H "Content-Type: application/json" \
  -d '{"alias":"VERIFY_PROFILE","providerId":"VERIFY_PROFILE","enabled":false,"defaultAction":false,"priority":90,"config":{}}' \
  > /dev/null

# ── Create users ───────────────────────────────────────────────────────────
for entry in "${USERS[@]}"; do
  IFS='|' read -r USERNAME PASSWORD ROLES_CSV <<< "$entry"

  # Skip if user already exists
  EXISTING=$(curl -sf \
    "$KEYCLOAK_URL/admin/realms/$REALM/users?username=$USERNAME&exact=true" \
    -H "$AUTH_HEADER" | python3 -c "import sys,json; print(len(json.load(sys.stdin)))")

  if [[ "$EXISTING" -gt 0 ]]; then
    echo "  ✓ $USERNAME already exists — skipped"
    continue
  fi

  echo "  + Creating user: $USERNAME"
  curl -sf -X POST "$KEYCLOAK_URL/admin/realms/$REALM/users" \
    -H "$AUTH_HEADER" -H "Content-Type: application/json" \
    -d "{
      \"username\": \"$USERNAME\",
      \"enabled\": true,
      \"emailVerified\": true,
      \"credentials\": [{\"type\":\"password\",\"value\":\"$PASSWORD\",\"temporary\":false}]
    }" > /dev/null

  # Get the new user's id
  USER_ID=$(curl -sf \
    "$KEYCLOAK_URL/admin/realms/$REALM/users?username=$USERNAME&exact=true" \
    -H "$AUTH_HEADER" | python3 -c "import sys,json; print(json.load(sys.stdin)[0]['id'])")

  # Assign roles
  IFS=',' read -ra ROLES <<< "$ROLES_CSV"
  ROLE_PAYLOAD="["
  for ROLE in "${ROLES[@]}"; do
    ROLE_JSON=$(curl -sf "$KEYCLOAK_URL/admin/realms/$REALM/roles/$ROLE" -H "$AUTH_HEADER")
    ROLE_PAYLOAD+="$ROLE_JSON,"
  done
  ROLE_PAYLOAD="${ROLE_PAYLOAD%,}]"

  curl -sf -X POST \
    "$KEYCLOAK_URL/admin/realms/$REALM/users/$USER_ID/role-mappings/realm" \
    -H "$AUTH_HEADER" -H "Content-Type: application/json" \
    -d "$ROLE_PAYLOAD" > /dev/null

  echo "    roles: $ROLES_CSV"
done

echo ""
echo "Done. Users in realm '$REALM':"
curl -sf "$KEYCLOAK_URL/admin/realms/$REALM/users" -H "$AUTH_HEADER" \
  | python3 -c "
import sys, json
for u in json.load(sys.stdin):
    print(f\"  {u['username']} (enabled={u['enabled']})\")
"