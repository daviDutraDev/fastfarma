package com.fastfarma.security.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Rate limiting simples por IP para os endpoints publicos de autenticacao.
 *
 * <p>Protecao contra forca bruta: cada IP pode tentar ate 10 logins e
 * 5 cadastros por minuto (contagem em janela deslizante). Excedeu ->
 * HTTP 429.</p>
 *
 * <p>Implementacao propria (sem bucket4j) usando ConcurrentHashMap +
 * Deque de timestamps — consome O(N) por IP mas N e' pequeno (numero
 * de requisicoes no ultimo minuto).</p>
 *
 * <p>Em producao multi-instancia, troque o mapa local por Redis.</p>
 */
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH = "/api/auth/login";
    private static final String CADASTRO_PATH = "/api/auth/cadastrar";

    private static final long JANELA_MS = 60_000L;   // 1 minuto
    private static final int LIMITE_LOGIN = 10;
    private static final int LIMITE_CADASTRO = 5;

    private final ConcurrentHashMap<String, Deque<Long>> loginHits =
            new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Deque<Long>> cadastroHits =
            new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        boolean isLogin     = LOGIN_PATH.equals(path);
        boolean isCadastro  = CADASTRO_PATH.equals(path);

        if (!isLogin && !isCadastro) {
            chain.doFilter(request, response);
            return;
        }

        String ip = clientIp(request);
        long agora = System.currentTimeMillis();
        int limite = isLogin ? LIMITE_LOGIN : LIMITE_CADASTRO;
        var mapa = isLogin ? loginHits : cadastroHits;

        Deque<Long> hits = mapa.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>());
        // Janela deslizante: descarta timestamps mais antigos que 60s
        while (!hits.isEmpty() && (agora - hits.peekFirst()) > JANELA_MS) {
            hits.pollFirst();
        }

        if (hits.size() >= limite) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"mensagem\":\"Muitas tentativas. Tente novamente em instantes.\","
                            + "\"status\":429}");
            return;
        }

        hits.addLast(agora);
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
