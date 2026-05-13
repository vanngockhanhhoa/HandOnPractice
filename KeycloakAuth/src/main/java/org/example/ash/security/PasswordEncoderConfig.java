package org.example.ash.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Isolated config for {@link PasswordEncoder} to avoid a circular dependency:
 *
 * <pre>
 *   SecurityConfig → JwtAuthenticationFilter
 *                         ↓
 *                   UserDetailsServiceImpl
 *                         ↓
 *                   PasswordEncoder  ←  was in SecurityConfig  (cycle!)
 * </pre>
 *
 * Moving the bean here breaks the cycle: {@code UserDetailsServiceImpl} depends
 * on this class only, which has no upstream dependencies.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
