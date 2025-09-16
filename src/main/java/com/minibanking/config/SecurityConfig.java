package com.minibanking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration for Mini Banking System
 * 
 * Development mode: Allows access to all API endpoints without authentication
 * Production mode: Should be configured with proper authentication
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for API development (enable in production)
            .csrf(csrf -> csrf.disable())
            
            // Allow all requests without authentication
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/**").permitAll()           // All API endpoints
                .requestMatchers("/swagger-ui/**").permitAll()    // Swagger UI
                .requestMatchers("/api-docs/**").permitAll()      // API docs
                .requestMatchers("/actuator/**").permitAll()      // Health checks
                .requestMatchers("/h2-console/**").permitAll()    // H2 Console (if used)
                .anyRequest().permitAll()                         // Everything else
            )
            
            // Disable HTTP Basic authentication
            .httpBasic(httpBasic -> httpBasic.disable())
            
            // Disable form login
            .formLogin(formLogin -> formLogin.disable());

        return http.build();
    }
}
