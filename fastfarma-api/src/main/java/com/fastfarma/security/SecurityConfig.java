package com.fastfarma.security;

import com.fastfarma.security.ratelimit.AuthRateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AuthRateLimitFilter authRateLimitFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Pbkdf2PasswordEncoder();
    }

    /** CORS — origens do Vite + pre-flight. */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
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

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        var bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

    /** Headers OWASP — X-Frame-Options, X-Content-Type-Options, etc. */
    @Bean
    public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilter() {
        var bean = new FilterRegistrationBean<>(new SecurityHeadersFilter());
        bean.addUrlPatterns("/*");
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return bean;
    }

    /** Rate limit em /api/auth/*. */
    @Bean
    public FilterRegistrationBean<AuthRateLimitFilter> rateLimitFilter() {
        var bean = new FilterRegistrationBean<>(authRateLimitFilter);
        bean.addUrlPatterns("/api/auth/*");
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        return bean;
    }

    /** Lê o token JWT e popula o AuthContext. */
    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilterReg() {
        var bean = new FilterRegistrationBean<>(jwtAuthFilter);
        bean.addUrlPatterns("/api/*");
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 30);
        return bean;
    }

    /** Bloqueia com 401/403 se a rota requer autenticacao/role. */
    @Bean
    public FilterRegistrationBean<SecurityBlockerFilter> securityBlockerFilter() {
        var bean = new FilterRegistrationBean<>(new SecurityBlockerFilter());
        bean.addUrlPatterns("/api/*");
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 40);
        return bean;
    }
}
