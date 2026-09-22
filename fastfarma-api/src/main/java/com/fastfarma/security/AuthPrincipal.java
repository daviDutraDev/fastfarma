package com.fastfarma.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Helper para extrair o nome do usuário autenticado a partir do
 * SecurityContext (preenchido pelo {@link JwtAuthFilter}).
 */
public final class AuthPrincipal {

    private AuthPrincipal() {}

    /** @return nome do usuário logado, ou null se não autenticado. */
    public static String currentName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        return auth.getName();
    }
}
