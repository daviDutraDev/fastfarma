package com.fastfarma.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Adiciona headers OWASP em todas as respostas:
 * <ul>
 *   <li>{@code X-Frame-Options: DENY} — anti-clickjacking</li>
 *   <li>{@code X-Content-Type-Options: nosniff} — anti MIME-sniffing</li>
 *   <li>{@code Referrer-Policy: no-referrer}</li>
 *   <li>{@code Permissions-Policy} — bloqueia APIs sensiveis no browser</li>
 *   <li>{@code Strict-Transport-Security} (HSTS) — quando HTTPS</li>
 *   <li>{@code Content-Security-Policy} basico</li>
 * </ul>
 */
public class SecurityHeadersFilter extends OncePerRequestFilter {

    private static final String CSP =
            "default-src 'self'; "
            + "script-src 'self'; "
            + "style-src 'self' 'unsafe-inline'; "
            + "img-src 'self' data:; "
            + "connect-src 'self'; "
            + "frame-ancestors 'none'";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("Permissions-Policy",
                "geolocation=(), microphone=(), camera=(), payment=()");
        response.setHeader("Content-Security-Policy", CSP);
        // HSTS so faz sentido com HTTPS; alguns navegadores ignoram em http.
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

        chain.doFilter(request, response);
    }
}
