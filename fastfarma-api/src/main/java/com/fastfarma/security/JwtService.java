package com.fastfarma.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * Gera e valida tokens JWT (HMAC-SHA256).
 *
 * <p>O segredo precisa ter pelo menos 256 bits (32 caracteres ASCII) —
 * a aplicação falha no startup se isso não for satisfeito, evitando
 * rodar em prod com chave fraca.</p>
 *
 * <p>Claims emitidos:
 * <ul>
 *   <li>{@code sub}: nome do usuário (Subject)</li>
 *   <li>{@code uid}: id do usuário</li>
 *   <li>{@code role}: tipo (CLIENTE / FUNCIONARIO)</li>
 * </ul>
 */
@Service
public class JwtService {

    private final String secret;
    private final long expirationMinutes;
    private SecretKey signingKey;

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
        this.signingKey = Keys.hmacShaKeyFor(bytes);
    }

    /** Gera um token assinado para o usuário. */
    public String generateToken(String nomeUsuario, Integer id, String role) {
        long nowMs = System.currentTimeMillis();
        long expMs = nowMs + expirationMinutes * 60_000L;

        return Jwts.builder()
                .subject(nomeUsuario)
                .claims(Map.of("uid", id, "role", role))
                .issuedAt(new Date(nowMs))
                .expiration(new Date(expMs))
                .signWith(signingKey)
                .compact();
    }

    /** Lê o subject (nome do usuário) do token. */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /** Lê o id (uid) do token. */
    public Integer extractUserId(String token) {
        Object uid = extractClaim(token, c -> c.get("uid"));
        return uid == null ? null : Integer.valueOf(uid.toString());
    }

    /** Lê a role (CLIENTE / FUNCIONARIO) do token. */
    public String extractRole(String token) {
        Object role = extractClaim(token, c -> c.get("role"));
        return role == null ? null : role.toString();
    }

    public boolean isTokenValid(String token) {
        try {
            return extractClaim(token, Claims::getExpiration).after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }
}
