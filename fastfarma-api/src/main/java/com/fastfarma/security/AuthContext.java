package com.fastfarma.security;

/**
 * Substituto minimalista do {@code SecurityContextHolder} do Spring Security.
 *
 * <p>Guarda o usuario autenticado em um {@link ThreadLocal} durante o
 * request. O {@link JwtAuthFilter} chama {@link #set(AuthUser)} no
 * inicio do filtro e {@link #clear()} no finally. Os controllers usam
 * {@link AuthPrincipal} para ler.</p>
 *
 * <p>Usar ThreadLocal e seguro aqui porque o Tomcat sempre serve um
 * request inteiro na mesma thread. Limpar no finally evita vazamento
 * caso o request seja reusado.</p>
 */
public final class AuthContext {

    private static final ThreadLocal<AuthUser> CURRENT = new ThreadLocal<>();

    private AuthContext() {}

    public static void set(AuthUser user) { CURRENT.set(user); }
    public static AuthUser get() { return CURRENT.get(); }
    public static void clear() { CURRENT.remove(); }

    /**
     * Representa o usuario autenticado extraido do JWT.
     *
     * @param name nome do usuario (subject do token)
     * @param role CLIENTE ou FUNCIONARIO
     */
    public record AuthUser(String name, String role) {
        public boolean isFuncionario() { return "FUNCIONARIO".equals(role); }
    }
}
