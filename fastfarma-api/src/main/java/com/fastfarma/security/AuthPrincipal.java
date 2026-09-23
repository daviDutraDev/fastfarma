package com.fastfarma.security;

/**
 * Helper para extrair o usuario autenticado a partir do {@link AuthContext}
 * (preenchido pelo {@link JwtAuthFilter}).
 */
public final class AuthPrincipal {

    private AuthPrincipal() {}

    /** @return nome do usuario logado, ou null se nao autenticado. */
    public static String currentName() {
        AuthContext.AuthUser user = AuthContext.get();
        return user == null ? null : user.name();
    }

    /** @return role do usuario logado (CLIENTE / FUNCIONARIO), ou null. */
    public static String currentRole() {
        AuthContext.AuthUser user = AuthContext.get();
        return user == null ? null : user.role();
    }

    /** @return true se o usuario logado for FUNCIONARIO. */
    public static boolean isFuncionario() {
        AuthContext.AuthUser user = AuthContext.get();
        return user != null && user.isFuncionario();
    }
}
