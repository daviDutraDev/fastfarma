package com.fastfarma.security.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting simples por IP para os endpoints publicos de autenticação.
 *
 * <p>Proteção contra força bruta: cada IP pode tentar até 10 logins e
 * 5 cadastros por minuto. Excedeu -> HTTP 429.</p>
 *
 * <p>Em produção multi-instância, troque o mapa local por Redis.
 * Aqui fica local por simplicidade.</p>
 */
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH = "/api/auth/login";
    private static final String CADASTRO_PATH = "/api/auth/cadastrar";

    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> cadastroBuckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        Bucket bucket = null;

        if (LOGIN_PATH.equals(path) && "POST".equalsIgnoreCase(request.getMethod())) {
            bucket = loginBuckets.computeIfAbsent(clientIp(request),
                    k -> Bucket.builder()
                            .addLimit(Bandwidth.builder()
                                    .capacity(10)
                                    .refillGreedy(10, Duration.ofMinutes(1))
                                    .build())
                            .build());
        } else if (CADASTRO_PATH.equals(path) && "POST".equalsIgnoreCase(request.getMethod())) {
            bucket = cadastroBuckets.computeIfAbsent(clientIp(request),
                    k -> Bucket.builder()
                            .addLimit(Bandwidth.builder()
                                    .capacity(5)
                                    .refillGreedy(5, Duration.ofMinutes(1))
                                    .build())
                            .build());
        }

        if (bucket != null && !bucket.tryConsume(1)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"mensagem\":\"Muitas tentativas. Tente novamente em instantes.\","
                            + "\"status\":429}");
            return;
        }

        chain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest req) {
        String fwd = req.getHeader("X-Forwarded-For");
        if (fwd != null && !fwd.isBlank()) {
            return fwd.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }
}
