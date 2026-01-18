package com.stitcho.beta.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/login/**").permitAll()
                        .requestMatchers("/api/owners/create").permitAll()
                        
                        // Allow all secure endpoints (JWT validation done in controllers)
                        .requestMatchers("/api/owners/**").permitAll()
                        .requestMatchers("/api/profile/**").permitAll()
                        .requestMatchers("/api/tasks/**").permitAll()
                        .requestMatchers("/api/orders/**").permitAll()
                        .requestMatchers("/api/workers/**").permitAll()
                        .requestMatchers("/api/customers/**").permitAll()
                        .requestMatchers("/api/shops/**").permitAll()
                        .requestMatchers("/api/measurements/**").permitAll()
                        .requestMatchers("/api/ratings/**").permitAll()
                        
                        // All other requests require authentication
                        .anyRequest().authenticated()
                );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow specific origins for production and development
        configuration.setAllowedOrigins(Arrays.asList(
            "https://smart-stitch-frontend.onrender.com",  // Production frontend
            "http://localhost:3000",                        // Local development
            "http://localhost:5173"                         // Vite development server
        ));
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);  // Enable credentials for cookies/auth headers
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
