package com.lmkr.hesco.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * HS256 JWT issuing/parsing carrying HESCO's bound-scoped claims
 * (roleCode, boundType, circleId/divisionId/subDivisionId) so
 * downstream modules can enforce per-role query scoping without
 * re-querying AppUser — per the revamp plan's auth-service design.
 */
@Component
public class JwtService {

    private final Key signingKey;
    private final long expirationMinutes;
    private final String issuer;

    public JwtService(
            @Value("${auth.jwt.secret:5fJuBaNboJfaPsWq0tCaaXHa4bnWFXbrJJhThrtIgho=}") String secret,
            @Value("${auth.jwt.expiration-minutes:60}") long expirationMinutes,
            @Value("${auth.jwt.issuer:hesco}") String issuer) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = expirationMinutes;
        this.issuer = issuer;
    }

    public record IssuedToken(String token, Instant expiresAt) {
    }

    public IssuedToken issue(String username, Long userId, String roleCode, String boundType,
                              Long circleId, Long divisionId, Long subDivisionId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(expirationMinutes, ChronoUnit.MINUTES);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("roleCode", "ROLE_" + roleCode);
        claims.put("boundType", boundType);
        if (circleId != null) claims.put("circleId", circleId);
        if (divisionId != null) claims.put("divisionId", divisionId);
        if (subDivisionId != null) claims.put("subDivisionId", subDivisionId);

        String token = Jwts.builder()
                .setSubject(username)
                .setIssuer(issuer)
                .setIssuedAt(java.util.Date.from(now))
                .setExpiration(java.util.Date.from(expiresAt))
                .addClaims(claims)
                .signWith(signingKey)
                .compact();

        return new IssuedToken(token, expiresAt);
    }

    public Claims parse(String token) {
        JwtParser parser = Jwts.parserBuilder().setSigningKey(signingKey).build();
        return parser.parseClaimsJws(token).getBody();
    }

    public static void main(String[] args) {
        String key = Base64.getEncoder().encodeToString(Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256).getEncoded());
        System.out.println(key);
    }
}
