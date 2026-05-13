package org.example.ash.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.converter.RsaKeyConverters;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Loads the RSA key pair from PEM files on the classpath.
 *
 * <p>Keys live in {@code src/main/resources/keys/}.
 * In production replace these files (or override via environment variables /
 * external config) — never commit real private keys to source control.
 *
 * <pre>
 *   Signing  (token generation) → RSAPrivateKey  (server only)
 *   Verifying (token validation) → RSAPublicKey  (can be shared / published)
 * </pre>
 */
@Configuration
public class RsaKeyConfig {

    @Value("${jwt.private-key}")
    private Resource privateKeyResource;

    @Value("${jwt.public-key}")
    private Resource publicKeyResource;

    @Bean
    public RSAPrivateKey rsaPrivateKey() throws Exception {
        try (var stream = privateKeyResource.getInputStream()) {
            return RsaKeyConverters.pkcs8().convert(stream);
        }
    }

    @Bean
    public RSAPublicKey rsaPublicKey() throws Exception {
        try (var stream = publicKeyResource.getInputStream()) {
            return RsaKeyConverters.x509().convert(stream);
        }
    }
}
