package com.fastfarma.security;

import com.fastfarma.model.TipoUsuario;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que extrai o header {@code Authorization: Bearer <token>},
 * valida via {@link JwtService} e popula o {@link AuthContext}.
 *
 * <p>O filtro NAO bloqueia requisicoes sem token — quem decide isso
 * e o SecurityConfig (ou a checagem manual nos controllers, no nosso
 * caso). Apenas deixa o request passar sem autenticar.</p>
 *
 * <p>O {@link AuthContext#clear()} no finally garante que o ThreadLocal
 * nao vaze entre requests.</p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        try {
            String header = request.getHeader(HEADER);
            if (header != null && header.startsWith(PREFIX)) {
                String token = header.substring(PREFIX.length()).trim();
                if (!token.isEmpty() && jwtService.isTokenValid(token)) {
                    String username = jwtService.extractUsername(token);
                    String role = jwtService.extractRole(token);

                    // Validacao defensiva: a role no token precisa bater com TipoUsuario
                    if (username != null && role != null) {
                        try {
                            TipoUsuario tipo = TipoUsuario.valueOf(role);
                            AuthContext.set(
                                new AuthContext.AuthUser(username, tipo.name()));
                        } catch (IllegalArgumentException ignored) {
                            // Role lixo — segue sem autenticar
                        }
                    }
                }
            }

            chain.doFilter(request, response);
        } finally {
            AuthContext.clear();
        }
    }
}
