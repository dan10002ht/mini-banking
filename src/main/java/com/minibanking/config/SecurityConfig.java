package com.minibanking.config;

import com.minibanking.security.jwt.JwtAuthenticationFilter;
import com.minibanking.security.jwt.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security Configuration for Mini Banking System
 * 
 * Development mode: Allows access to all API endpoints without authentication
 * Production mode: Should be configured with proper authentication
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtService jwtService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for API development (enable in production)
            .csrf(csrf -> csrf.disable())
            
            // Configure session management
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Configure authorization
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()           // Authentication endpoints
                .requestMatchers("/api/security/initialize").permitAll() // Security initialization
                .requestMatchers("/swagger-ui/**").permitAll()         // Swagger UI
                .requestMatchers("/api-docs/**").permitAll()           // API docs
                .requestMatchers("/actuator/**").permitAll()           // Health checks
                .requestMatchers("/h2-console/**").permitAll()         // H2 Console (if used)
                
                // Protected endpoints
                .requestMatchers("/api/transfers/**").authenticated()  // Transfer operations
                .requestMatchers("/api/accounts/**").authenticated()   // Account management
                .requestMatchers("/api/customers/**").authenticated()  // Customer management
                .requestMatchers("/api/transactions/**").authenticated() // Transaction history
                .requestMatchers("/api/blockchain/**").authenticated() // Blockchain operations
                .requestMatchers("/api/security/**").authenticated()   // Security operations
                
                .anyRequest().authenticated()                          // Everything else requires auth
            )
            
            // Disable HTTP Basic authentication
            .httpBasic(httpBasic -> httpBasic.disable())
            
            // Disable form login
            .formLogin(formLogin -> formLogin.disable())
            
            // Add JWT filter
            .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
