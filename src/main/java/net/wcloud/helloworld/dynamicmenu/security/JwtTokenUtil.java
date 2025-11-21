package net.wcloud.helloworld.dynamicmenu.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import net.wcloud.helloworld.dynamicmenu.config.JwtProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenUtil.class);

    private final JwtProperties jwtProperties;
    private final Key key;

    public JwtTokenUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        // secret 长度至少 32 字节，否则 jjwt 会报错
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 token：
     * - subject: username（比如 "admin"）
     * - claims: 额外自定义信息（比如 userId）
     */
    public String generateToken(String username, Map<String, Object> claims) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtProperties.getExpiration() * 1000L);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 校验 token 是否有效（签名 & 过期）
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT 已过期: {}", e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT 非法: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 解析用户名（subject）
     */
    public String getUsername(String token) {
        try {
            return getClaims(token).getSubject(); // 就是 payload 里的 "sub"
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("解析 JWT username 失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 解析 userId（如果需要）
     */
    public Long getUserId(String token) {
        try {
            Object v = getClaims(token).get("userId");
            return v == null ? null : Long.valueOf(v.toString());
        } catch (Exception e) {
            log.warn("解析 JWT userId 失败: {}", e.getMessage());
            return null;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}