package org.example.ash.security.keycloak;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ash.configuration.RequestContext;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Reads Keycloak-issued Bearer tokens for mobile clients.
 *
 * <p>Kong Gateway verifies the RS256 signature and expiry before the request
 * reaches this filter, so no JWKS call is needed here — we only decode the
 * claims to build the Spring Security context.
 *
 * <pre>
 *   Kong (JWT plugin)  →  validates signature + exp
 *       └──►  This filter  →  decodes claims, sets SecurityContext
 * </pre>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER        = "Bearer ";

    private final KeycloakJwtAuthenticationConverter converter;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/auth/")
            || path.startsWith("/actuator/");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        Authentication existing = SecurityContextHolder.getContext().getAuthentication();
        if (existing != null && existing.isAuthenticated()) {
            chain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            // Kong already verified the signature — just parse claims
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

            // Convert Date → Instant so Spring's Jwt handles them correctly
            Map<String, Object> claims = new HashMap<>();
            for (var entry : claimsSet.getClaims().entrySet()) {
                Object v = entry.getValue();
                claims.put(entry.getKey(), v instanceof Date ? ((Date) v).toInstant() : v);
            }

            Jwt jwt = Jwt.withTokenValue(token)
                    .headers(h -> h.putAll(signedJWT.getHeader().toJSONObject()))
                    .claims(c -> c.putAll(claims))
                    .build();

            var auth = converter.convert(jwt);
            SecurityContextHolder.getContext().setAuthentication(auth);

            String username = claimsSet.getStringClaim("preferred_username");
            RequestContext.set(username != null ? username : claimsSet.getSubject());

            log.debug("Keycloak authentication successful for user: {}", username);
        } catch (ParseException e) {
            log.warn("Failed to parse Keycloak JWT: {}", e.getMessage());
        }

        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION);
        if (StringUtils.hasText(header) && header.startsWith(BEARER)) {
            return header.substring(BEARER.length());
        }
        return null;
    }
}