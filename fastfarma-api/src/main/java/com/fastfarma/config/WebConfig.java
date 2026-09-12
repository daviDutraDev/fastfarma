package com.fastfarma.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração web central da API.
 *
 * <p>Centraliza o CORS para evitar a anotação {@code @CrossOrigin}
 * espalhada por cada controller. Permite que o frontend em
 * {@code http://localhost:5173} (porta padrão do Vite) consuma a API
 * durante o desenvolvimento, e mantém {@code *} como fallback para
 * ferramentas e clientes curl/Postman.</p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:5173", // Vite dev
                        "http://localhost:4173", // Vite preview
                        "http://localhost:3000"  // React dev alternativo
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("X-Usuario-Nome")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
