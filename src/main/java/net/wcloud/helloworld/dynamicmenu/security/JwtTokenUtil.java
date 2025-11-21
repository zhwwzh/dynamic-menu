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

/**
 * JWT 工具类（生成 / 校验 / 解析）
 *
 * 主要职责：
 * ------------------------------------------------------------
 * 1. 生成 JWT（包含 username、claims、自定义数据）
 * 2. 校验 JWT 的合法性（签名正确、未过期）
 * 3. 解析 username、userId 等自定义字段
 *
 * 技术说明：
 * ------------------------------------------------------------
 * - 使用 jjwt 0.11.x
 * - HS256 需要至少 32 字节长度的 secret，否则会报错
 * - 使用 Key 对象进行签名
 *
 * 日志策略：
 * ------------------------------------------------------------
 * - info：一般不打印 token（避免敏感信息泄露）
 * - debug：开发/测试环境可开启，打印解析的 claims
 * - warn：token 过期、伪造、解析错误等
 */
@Component
public class JwtTokenUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenUtil.class);

    private final JwtProperties jwtProperties;
    private final Key key;

    public JwtTokenUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;

        // secret 必须 >= 32 字节，否则 HMAC SHA256 无法正常工作
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));

        log.info("[JwtTokenUtil] JWT 工具初始化完成，secret 长度={} 字节",
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8).length);
    }

    /**
     * 生成 token
     *
     * @param username 登录用户名（sub）
     * @param claims   自定义 claims，例如 userId、roleIds 等
     * @return JWT 字符串
     *
     *         JWT 构成：
     *         ------------------------------------------------------------
     *         Header: {"alg":"HS256","typ":"JWT"}
     *         Payload: {"sub":"admin","userId":1,...}
     *         Signature: HMACSHA256(base64Url(header) + "." + base64Url(payload),
     *         key)
     */
    public String generateToken(String username, Map<String, Object> claims) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtProperties.getExpiration() * 1000L);

        log.debug("[generateToken] 生成 JWT, username={}, expiresAt={}", username, exp);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username) // 将 username 作为 JWT 的 subject
                .setIssuedAt(now) // 签发时间
                .setExpiration(exp) // 过期时间
                .signWith(key, SignatureAlgorithm.HS256) // 签名
                .compact();
    }

    /**
     * 校验 token 是否有效（签名 OK + 未过期）
     *
     * @param token JWT 字符串
     * @return true=有效, false=无效
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            log.debug("[validateToken] Token 校验通过");
            return true;

        } catch (ExpiredJwtException e) {
            log.warn("[validateToken] Token 已过期: {}", e.getMessage());

        } catch (MalformedJwtException e) {
            log.warn("[validateToken] Token 格式非法: {}", e.getMessage());

        } catch (UnsupportedJwtException e) {
            log.warn("[validateToken] 不支持该 Token: {}", e.getMessage());

        } catch (SignatureException e) {
            log.warn("[validateToken] Token 签名验证失败（可能被伪造）: {}", e.getMessage());

        } catch (IllegalArgumentException e) {
            log.warn("[validateToken] Token 为空或解析失败: {}", e.getMessage());

        } catch (Exception e) {
            log.error("[validateToken] Token 验证未知异常: {}", e.getMessage(), e);
        }

        return false;
    }

    /**
     * 获取 username（JWT 中的 "sub"）
     */
    public String getUsername(String token) {
        try {
            Claims claims = getClaims(token);
            String username = claims.getSubject();

            log.debug("[getUsername] 解析 username={} from Token", username);

            return username;

        } catch (Exception e) {
            log.warn("[getUsername] 解析 username 失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取用户 ID（从自定义 claims 中取 userId）
     */
    public Long getUserId(String token) {
        try {
            Object v = getClaims(token).get("userId");

            if (v == null) {
                log.debug("[getUserId] Token 中没有 userId 字段");
                return null;
            }

            Long userId = Long.valueOf(v.toString());
            log.debug("[getUserId] 解析 userId={} from Token", userId);
            return userId;

        } catch (Exception e) {
            log.warn("[getUserId] 解析 userId 失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取 Claims（含所有 payload）
     *
     * 会校验签名，不校验过期时间。
     *
     * @param token JWT 字符串
     * @return Claims 对象
     */
    private Claims getClaims(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token) // 若签名错误或过期，会抛异常
                    .getBody();

            log.debug("[getClaims] 解析 Token 成功, claims={}", claims);
            return claims;

        } catch (Exception e) {
            log.warn("[getClaims] 解析 Token 失败: {}", e.getMessage());
            throw e;
        }
    }
}
