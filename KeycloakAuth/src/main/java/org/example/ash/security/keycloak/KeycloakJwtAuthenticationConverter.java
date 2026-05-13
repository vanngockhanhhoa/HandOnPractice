package org.example.ash.security.keycloak;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Maps Keycloak JWT claims to Spring Security {@link GrantedAuthority} objects.
 *
 * <p>Keycloak places roles in two locations inside the token:
 * <pre>
 *   realm_access.roles                          – realm-level roles
 *   resource_access.&lt;client-id&gt;.roles      – client-level roles
 * </pre>
 *
 * Both are extracted and prefixed with {@code ROLE_} so Spring Security's
 * {@code hasRole} / {@code hasAuthority} expressions work consistently with
 * the local JWT roles ({@code ROLE_USER}, {@code ROLE_ADMIN}).
 */
@Component
public class KeycloakJwtAuthenticationConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    @Value("${keycloak.client-id}")
    private String clientId;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        // "preferred_username" is the human-readable login name in Keycloak
        String username = jwt.getClaimAsString("preferred_username");
        return new JwtAuthenticationToken(jwt, authorities, username);
    }

    // ── Role extraction ────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // 1. Realm-level roles
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null) {
            toAuthorities((List<String>) realmAccess.get("roles"), authorities);
        }

        // 2. Client-level roles  (resource_access.<client-id>.roles)
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        if (resourceAccess != null) {
            Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(clientId);
            if (clientAccess != null) {
                toAuthorities((List<String>) clientAccess.get("roles"), authorities);
            }
        }

        return authorities;
    }

    private void toAuthorities(List<String> roles, Set<GrantedAuthority> target) {
        if (roles == null) return;
        roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .forEach(target::add);
    }
}
