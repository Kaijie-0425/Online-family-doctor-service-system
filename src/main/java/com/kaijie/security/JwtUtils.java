package com.kaijie.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;

import java.util.Date;

/**
 * 简单的 JWT 工具类（使用 jjwt 0.9.1）
 */
public class JwtUtils {
    // NOTE: 在生产环境中请把 SECRET_KEY 放在安全的配置或环境变量中，并使用更强的随机密钥
    private static final String SECRET_KEY = "ChangeThisSecretKeyToASecureRandomValue-ReplaceMe";
    private static final long EXPIRATION_MS = 24 * 60 * 60 * 1000L; // 24 小时

    /**
     * 生成 token，claims 中存放 username 和 roleType
     */
    public static String generateToken(String username, Integer roleType) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_MS);

        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roleType", roleType);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY.getBytes())
                .compact();
    }

    /**
     * 解析 token，返回 Claims；若解析失败会抛出运行时异常
     */
    public static Claims parseToken(String token) {
        if (token == null) return null;
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (SignatureException | MalformedJwtException e) {
            throw e;
        }
    }

    public static boolean isTokenValid(String token) {
        try {
            Claims claims = parseToken(token);
            return claims != null && claims.getSubject() != null;
        } catch (Exception ex) {
            return false;
        }
    }

    public static String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims == null ? null : claims.getSubject();
    }

    public static Integer getRoleTypeFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) return null;
        Object v = claims.get("roleType");
        if (v == null) return null;
        if (v instanceof Integer) return (Integer) v;
        try {
            return Integer.valueOf(String.valueOf(v));
        } catch (Exception ex) {
            return null;
        }
    }
}

