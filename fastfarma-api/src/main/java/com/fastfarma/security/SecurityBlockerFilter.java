package com.fastfarma.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Filtro que bloqueia com 401/403 as rotas protegidas quando o
 * {@link AuthContext} nao foi preenchido (ou foi preenchido com role
 * insuficiente). Roda DEPOIS do {@link JwtAuthFilter}.
 *
 * <p>Rotas publicas (OPTIONS e /api/auth/*) passam direto.</p>
 */
public class SecurityBlockerFilter extends OncePerRequestFilter {

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/auth/login",
            "/api/auth/cadastrar"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // 1) CORS pre-flight sempre passa
        if (HttpMethod.OPTIONS.matches(method)) {
            chain.doFilter(request, response);
            return;
        }

        // 2) Endpoints publicos passam
        if (PUBLIC_PATHS.contains(path)) {
            chain.doFilter(request, response);
            return;
        }

        // 3) A partir daqui, exige autenticacao
        AuthContext.AuthUser user = AuthContext.get();
        if (user == null) {
            unauthorized(response, "Autenticação obrigatória.");
            return;
        }

        // 4) Checagem de role
        String requiredRole = requiredRoleFor(path, method);
        if (requiredRole != null && !"FUNCIONARIO".equals(user.role())) {
            forbidden(response, "Acesso restrito a funcionários.");
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * @return "FUNCIONARIO" se a rota exige essa role, ou null se
     *         qualquer autenticado pode acessar.
     */
    private String requiredRoleFor(String path, String method) {
        if (path.startsWith("/api/usuarios/")) return "FUNCIONARIO";

        if (path.startsWith("/api/estoque/")) return "FUNCIONARIO";

        if (path.startsWith("/api/relatorios/")) return "FUNCIONARIO";

        if (path.startsWith("/api/produtos")) {
            // GET em qualquer listagem é publico para autenticado;
            // escrita exige FUNCIONARIO.
            if (HttpMethod.GET.matches(method)) return null;
            return "FUNCIONARIO";
        }

        if (path.startsWith("/api/pedidos")) {
            // GET geral e por status sao admin; GET por id e POST sao any auth.
            if (path.equals("/api/pedidos")) return "FUNCIONARIO";
            if (path.startsWith("/api/pedidos/status/")) return "FUNCIONARIO";
            // PATCH .../status e admin
            if (HttpMethod.PATCH.matches(method)
                    && path.matches(".*/status$")) return "FUNCIONARIO";
            // /api/pedidos/{id} e /api/pedidos/cliente/{nome} sao any auth
            // (o controller faz a checagem fina de "e o proprio pedido").
            return null;
        }

        return null;
    }

    private void unauthorized(HttpServletResponse resp, String mensagem) throws IOException {
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resp.getWriter().write(
                "{\"mensagem\":\"" + mensagem + "\",\"status\":401}");
    }

    private void forbidden(HttpServletResponse resp, String mensagem) throws IOException {
        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        resp.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resp.getWriter().write(
                "{\"mensagem\":\"" + mensagem + "\",\"status\":403}");
    }
}
