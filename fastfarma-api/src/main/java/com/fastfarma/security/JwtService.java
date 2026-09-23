package com.fastfarma.security;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Gera e valida tokens JWT (HMAC-SHA256) usando apenas a standard library
 * do Java ({@link Base64} + {@link Mac}).
 *
 * <p>Implementação zero-dependência para ambientes onde o Maven não tem
 * acesso a repositórios externos. O segredo precisa ter pelo menos
 * 256 bits (32 caracteres ASCII) — caso contrário a aplicação falha
 * no startup.</p>
 *
 * <p>Claims emitidos no payload JSON:
 * <ul>
 *   <li>{@code sub}: nome do usuário</li>
 *   <li>{@code uid}: id do usuário</li>
 *   <li>{@code role}: CLIENTE ou FUNCIONARIO</li>
 *   <li>{@code iat}: emitido em (segundos desde epoch)</li>
 *   <li>{@code exp}: expira em (segundos desde epoch)</li>
 * </ul>
 */
@Service
public class JwtService {

    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder B64D = Base64.getUrlDecoder();

    private final String secret;
    private final long expirationMinutes;
    private SecretKeySpec signingKey;

    public JwtService(
            @Value("${fastfarma.jwt.secret}") String secret,
            @Value("${fastfarma.jwt.expiration-minutes:1440}") long expirationMinutes) {
        this.secret = secret;
        this.expirationMinutes = expirationMinutes;
    }

    @PostConstruct
    void init() {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException(
                    "FASTFARMA_JWT_SECRET precisa ter pelo menos 32 caracteres (256 bits). "
                            + "Atual: " + bytes.length + " bytes.");
        }
        this.signingKey = new SecretKeySpec(bytes, "HmacSHA256");
    }

    /** Gera um token assinado para o usuario. */
    public String generateToken(String nomeUsuario, Integer id, String role) {
        long nowSec = System.currentTimeMillis() / 1000;
        long expSec = nowSec + expirationMinutes * 60;

        String payloadJson = "{"
                + "\"sub\":\"" + jsonEscape(nomeUsuario) + "\","
                + "\"uid\":" + (id == null ? -1 : id) + ","
                + "\"role\":\"" + jsonEscape(role) + "\","
                + "\"iat\":" + nowSec + ","
                + "\"exp\":" + expSec
                + "}";

        String headerB64 = B64.encodeToString(HEADER_JSON.getBytes(StandardCharsets.UTF_8));
        String payloadB64 = B64.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signingInput = headerB64 + "." + payloadB64;
        String signatureB64 = hmacSha256B64(signingInput);

        return signingInput + "." + signatureB64;
    }

    public boolean isTokenValid(String token) {
        try {
            Map<String, String> claims = parseAndVerify(token);
            String exp = claims.get("exp");
            return exp != null && Long.parseLong(exp) > System.currentTimeMillis() / 1000;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return parseAndVerify(token).get("sub");
    }

    public Integer extractUserId(String token) {
        String uid = parseAndVerify(token).get("uid");
        if (uid == null) return null;
        try { return Integer.valueOf(uid); } catch (NumberFormatException e) { return null; }
    }

    public String extractRole(String token) {
        return parseAndVerify(token).get("role");
    }

    // -----------------------------------------------------------------
    // Internals
    // -----------------------------------------------------------------

    /**
     * Valida o token e devolve as claims. Lanca RuntimeException em caso
     * de token malformado, assinatura invalida ou qualquer outra falha.
     */
    private Map<String, String> parseAndVerify(String token) {
        if (token == null) throw new IllegalArgumentException("Token nulo");
        String[] parts = token.split("\\.");
        if (parts.length != 3) throw new IllegalArgumentException("Token JWT malformado");

        String signingInput = parts[0] + "." + parts[1];
        String expectedSig = hmacSha256B64(signingInput);
        if (!constantTimeEquals(expectedSig, parts[2])) {
            throw new IllegalArgumentException("Assinatura JWT invalida");
        }

        byte[] payloadBytes = B64D.decode(parts[1]);
        String payloadJson = new String(payloadBytes, StandardCharsets.UTF_8);
        return parseFlatJson(payloadJson);
    }

    /** HMAC-SHA256 da entrada, retornado em Base64 URL sem padding. */
    private String hmacSha256B64(String input) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] sig = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));
            return B64.encodeToString(sig);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao calcular HMAC-SHA256", e);
        }
    }

    /** Comparacao constant-time para evitar timing attacks na assinatura. */
    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);
        }
        return diff == 0;
    }

    /** Escape minimo para JSON string. */
    private static String jsonEscape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * Parser minimo para JSON plano (sem objetos/arrays aninhados).
     * Suficiente para os claims que esta classe emite.
     */
    private static Map<String, String> parseFlatJson(String json) {
        Map<String, String> out = new HashMap<>();
        String trimmed = json.trim();
        if (trimmed.startsWith("{")) trimmed = trimmed.substring(1);
        if (trimmed.endsWith("}")) trimmed = trimmed.substring(0, trimmed.length() - 1);

        // Itera caractere a caractere respeitando strings com aspas e escapes.
        int i = 0;
        int n = trimmed.length();
        while (i < n) {
            // Pula espacos e virgulas
            while (i < n && (trimmed.charAt(i) == ' ' || trimmed.charAt(i) == ',')) i++;
            if (i >= n) break;

            // Lê a chave (entre aspas)
            if (trimmed.charAt(i) != '"') break;
            int keyStart = ++i;
            StringBuilder key = new StringBuilder();
            while (i < n && trimmed.charAt(i) != '"') {
                if (trimmed.charAt(i) == '\\' && i + 1 < n) i++;
                key.append(trimmed.charAt(i++));
            }
            i++; // fecha aspas

            // Pula ate o :
            while (i < n && trimmed.charAt(i) != ':') i++;
            i++; // consome o :

            // Pula espacos
            while (i < n && trimmed.charAt(i) == ' ') i++;

            // Lê o valor
            String value;
            if (i < n && trimmed.charAt(i) == '"') {
                int valStart = ++i;
                StringBuilder val = new StringBuilder();
                while (i < n && trimmed.charAt(i) != '"') {
                    if (trimmed.charAt(i) == '\\' && i + 1 < n) i++;
                    val.append(trimmed.charAt(i++));
                }
                i++; // fecha aspas
                value = val.toString();
            } else {
                int valStart = i;
                while (i < n && trimmed.charAt(i) != ',') i++;
                value = trimmed.substring(valStart, i).trim();
            }
            out.put(key.toString(), value);
        }
        return out;
    }
}
