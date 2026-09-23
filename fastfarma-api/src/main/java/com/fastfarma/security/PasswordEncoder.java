package com.fastfarma.security;

/**
 * Hashing de senhas sem dependencia externa.
 *
 * <p>Usa PBKDF2-HMAC-SHA256 com salt aleatorio e 65536 iteracoes
 * (ajustavel). Formato de armazenamento:</p>
 *
 * <pre>
 *   pbkdf2_sha256$ITERACOES$saltBase64$hashBase64
 * </pre>
 *
 * <p>Verificacao em tempo constante para evitar timing attacks.
 * Substitui o BCryptPasswordEncoder do Spring Security para ambientes
 * onde spring-security-crypto nao esta disponivel.</p>
 */
public interface PasswordEncoder {

    /** Gera um hash da senha em texto puro (com salt aleatorio). */
    String encode(String rawPassword);

    /**
     * Verifica se a senha em texto puro bate com o hash armazenado.
     * Comparacao em tempo constante.
     */
    boolean matches(String rawPassword, String storedHash);
}
