// package com.community.api.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.config.Customizer;
// import
// org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.web.cors.CorsConfiguration;
// import org.springframework.web.cors.CorsConfigurationSource;
// import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// import java.util.List;

// @Configuration
// public class SecurityConfig {
// // Starter configuration only. JWT authentication/authorization must be
// ported
// // from the Node backend before exposing protected endpoints.
// @Bean
// SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
// return http
// .csrf(csrf -> csrf.disable())
// .cors(Customizer.withDefaults())
// .sessionManagement(s ->
// s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
// .authorizeHttpRequests(auth -> auth
// .requestMatchers("/api/v1/health", "/swagger-ui/**",
// "/v3/api-docs/**").permitAll()
// .anyRequest().permitAll())
// .build();
// }

// @Bean
// CorsConfigurationSource corsConfigurationSource() {
// CorsConfiguration config = new CorsConfiguration();
// config.setAllowedOriginPatterns(List.of("http://localhost:*",
// "https://localhost:*"));
// config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE",
// "OPTIONS"));
// config.setAllowedHeaders(List.of("*"));
// config.setAllowCredentials(true);
// UrlBasedCorsConfigurationSource source = new
// UrlBasedCorsConfigurationSource();
// source.registerCorsConfiguration("/**", config);
// return source;
// }
// }
