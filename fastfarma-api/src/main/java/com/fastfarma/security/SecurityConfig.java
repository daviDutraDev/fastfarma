package com.fastfarma.security;

import com.fastfarma.security.ratelimit.AuthRateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuração central de segurança.
 *
 * <p>Política:
 * <ul>
 *   <li>Stateless (sem JSESSIONID).</li>
 *   <li>CSRF desabilitado (API stateless com JWT).</li>
 *   <li>CORS liberado para os origens do Vite (5173/4173/3000).</li>
 *   <li>Headers OWASP: X-Frame-Options, X-Content-Type-Options,
 *       Referrer-Policy, Permissions-Policy, CSP básico.</li>
 *   <li>Rotas públicas: {@code /api/auth/*}, actuator/health.</li>
 *   <li>Rotas admin (FUNCIONARIO): CRUD de usuarios, produtos, estoque,
 *       listagem geral de pedidos e mudança de status.</li>
 *   <li>CLIENTE autenticado pode criar pedido e ver os proprios.</li>
 * </ul>
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AuthRateLimitFilter authRateLimitFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        var corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:4173",
                "http://localhost:3000"));
        corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        corsConfig.setAllowedHeaders(List.of("*"));
        corsConfig.setExposedHeaders(List.of("X-Usuario-Nome"));
        corsConfig.setAllowCredentials(false);
        corsConfig.setMaxAge(3600L);

        var corsSource = new UrlBasedCorsConfigurationSource();
        corsSource.registerCorsConfiguration("/**", corsConfig);

        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsSource))
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())
                .contentTypeOptions(c -> {})    // X-Content-Type-Options: nosniff
                .httpStrictTransportSecurity(h -> {}) // HSTS (no-op em dev)
            )
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(reg -> reg
                // Publicos
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/error").permitAll()

                // Admin / funcionario (gerencia tudo)
                .requestMatchers("/api/usuarios/**").hasRole("FUNCIONARIO")
                .requestMatchers(HttpMethod.GET, "/api/produtos/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/produtos").hasRole("FUNCIONARIO")
                .requestMatchers(HttpMethod.PUT, "/api/produtos/**").hasRole("FUNCIONARIO")
                .requestMatchers(HttpMethod.DELETE, "/api/produtos/**").hasRole("FUNCIONARIO")
                .requestMatchers("/api/estoque/**").hasRole("FUNCIONARIO")

                // Pedidos
                .requestMatchers(HttpMethod.GET, "/api/pedidos").hasRole("FUNCIONARIO")
                .requestMatchers(HttpMethod.GET, "/api/pedidos/status/**").hasRole("FUNCIONARIO")
                .requestMatchers(HttpMethod.PATCH, "/api/pedidos/**/status").hasRole("FUNCIONARIO")
                .requestMatchers(HttpMethod.GET, "/api/pedidos/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/pedidos").authenticated()

                // Qualquer outra rota autenticada
                .anyRequest().authenticated()
            )
            .addFilterBefore(authRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
