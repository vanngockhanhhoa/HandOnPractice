package org.example.ash.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

/**
 * JWT provider using RS256 (RSA-2048).
 *
 * <ul>
 *   <li><b>Sign</b>   – {@link RSAPrivateKey}  (kept secret on the server)</li>
 *   <li><b>Verify</b> – {@link RSAPublicKey}   (can be published / shared with consumers)</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey  publicKey;

    @Value("${jwt.expiration}")
    private long expiration;

    // ── Token generation ───────────────────────────────────────────────────────

    public String generate(UserDetails user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("roles", user.getAuthorities().stream()
                        .map(a -> a.getAuthority()).toList())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(privateKey)           // RS256 – sign with private key
                .compact();
    }

    // ── Token validation ───────────────────────────────────────────────────────

    public boolean isValid(String token) {
        try {
            claims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    // ── Claims extraction ──────────────────────────────────────────────────────

    public String extractUsername(String token) {
        return claims(token).getSubject();
    }

    // ── Internal ───────────────────────────────────────────────────────────────

    private Claims claims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)          // RS256 – verify with public key
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
