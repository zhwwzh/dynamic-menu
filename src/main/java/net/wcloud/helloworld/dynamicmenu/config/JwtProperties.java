package net.wcloud.helloworld.dynamicmenu.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性（从 application.yml 中加载）
 *
 * 配置前缀：app.jwt
 *
 * 示例配置：
 * ------------------------------------------------------------
 * app:
 * jwt:
 * header: Authorization
 * tokenPrefix: "Bearer "
 * secret: helloworld-dynamicmenu-secret-1234567890
 * expiration: 86400
 *
 * 作用：
 * ------------------------------------------------------------
 * 1. 向 JwtTokenUtil 提供 token 生成和校验所需的配置
 * 2. 向 JwtAuthenticationFilter 提供 Header / TokenPrefix 信息
 * 3. 统一 JWT 配置入口，便于后续动态调整（例如改成 Redis + JWT 混合方案）
 *
 * 日志策略：
 * ------------------------------------------------------------
 * - info : 打印配置加载完成日志（不打印敏感 secret）
 * - debug : 打印 secret 长度、token prefix 等信息
 */
@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /**
     * 前端在请求头中携带 Token 的字段名
     * 常用：Authorization
     */
    private String header = "Authorization";

    /**
     * Token 前缀（通常带空格，如 "Bearer "）
     * 作用：
     * - 解析 token 时用于去除前缀
     * - 与前端保持一致
     */
    private String tokenPrefix = "Bearer ";

    /**
     * JWT 签名秘钥（必须 >= 32 字节）
     * 作用：
     * - HS256/HS512 签名算法依赖该密钥
     * - JwtTokenUtil 使用此值创建 HMAC Key
     *
     * 建议：
     * - 生产环境中务必放入环境变量或 KMS，而不是写在配置文件
     */
    private String secret = "helloworld-dynamicmenu-secret-1234567890";

    /**
     * Token 过期时间（单位：秒）
     * 示例：86400 = 24 小时
     */
    private long expiration = 86400;

    /**
     * Bean 初始化完成后打印配置信息
     * （@PostConstruct 仅运行一次，极适合作为配置日志）
     */
    @PostConstruct
    public void init() {
        log.info("[JwtProperties] JWT 配置加载完成: header={}, tokenPrefix={}, expiration={} 秒",
                header, tokenPrefix, expiration);

        if (secret == null) {
            log.error("[JwtProperties] JWT secret 为空，系统将无法生成/校验 JWT！");
            return;
        }

        int length = secret.getBytes().length;
        log.debug("[JwtProperties] JWT secret 字节长度为: {}（>=32 字节才安全）", length);

        if (length < 32) {
            log.warn("[JwtProperties] 警告：JWT secret 长度不足！当前={}，建议 >= 32 字节提高安全性", length);
        }
    }
}
