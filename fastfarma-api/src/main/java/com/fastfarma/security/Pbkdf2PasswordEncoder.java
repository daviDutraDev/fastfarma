package com.fastfarma.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Implementacao de {@link PasswordEncoder} usando PBKDF2-HMAC-SHA256.
 *
 * <p>Parametros:
 * <ul>
 *   <li>Algoritmo: PBKDF2WithHmacSHA256</li>
 *   <li>Iteracoes: 65536 (ajustavel)</li>
 *   <li>Salt: 16 bytes aleatorios</li>
 *   <li>Hash derivado: 32 bytes (256 bits)</li>
 * </ul>
 *
 * <p>Formato armazenado: {@code pbkdf2_sha256$ITERACOES$saltB64$hashB64}
 * — autodocumentado e retrocompativel se as iteracoes mudarem.</p>
 */
public class Pbkdf2PasswordEncoder implements PasswordEncoder {

    private static final String ALGO = "PBKDF2WithHmacSHA256";
    private static final String PREFIX = "pbkdf2_sha256";
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;
    private static final int DEFAULT_ITERATIONS = 65536;

    private final int iterations;
    private final SecureRandom random = new SecureRandom();
    private final Base64.Encoder b64 = Base64.getEncoder();
    private final Base64.Decoder b64d = Base64.getDecoder();

    public Pbkdf2PasswordEncoder() {
        this(DEFAULT_ITERATIONS);
    }

    public Pbkdf2PasswordEncoder(int iterations) {
        if (iterations < 1000) {
            throw new IllegalArgumentException(
                    "Iteracoes PBKDF2 muito baixas (>= 1000 recomendado).");
        }
        this.iterations = iterations;
    }

    @Override
    public String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Senha vazia");
        }
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        byte[] hash = pbkdf2(rawPassword.toCharArray(), salt, iterations);
        return PREFIX + "$" + iterations + "$"
                + b64.encodeToString(salt) + "$"
                + b64.encodeToString(hash);
    }

    @Override
    public boolean matches(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) return false;
        if (!storedHash.startsWith(PREFIX + "$")) return false;

        String[] parts = storedHash.split("\\$");
        if (parts.length != 4) return false;

        int iters;
        byte[] salt, expectedHash;
        try {
            iters = Integer.parseInt(parts[1]);
            salt = b64d.decode(parts[2]);
            expectedHash = b64d.decode(parts[3]);
        } catch (RuntimeException e) {
            return false;
        }

        byte[] actualHash = pbkdf2(rawPassword.toCharArray(), salt, iters);
        return constantTimeEquals(expectedHash, actualHash);
    }

    private byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, HASH_BITS);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGO);
            return skf.generateSecret(spec).getEncoded();
        } catch (InvalidKeySpecException | java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("Falha ao calcular PBKDF2", e);
        } finally {
            // Limpa a senha da memoria
            java.util.Arrays.fill(password, '\0');
        }
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a == null || b == null || a.length != b.length) return false;
        int diff = 0;
        for (int i = 0; i < a.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }
}
