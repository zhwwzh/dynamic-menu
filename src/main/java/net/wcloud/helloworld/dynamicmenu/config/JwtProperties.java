package net.wcloud.helloworld.dynamicmenu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /** Header 名称，比如 Authorization */
    private String header = "Authorization";

    /** Token 前缀，例如 "Bearer "（注意有空格） */
    private String tokenPrefix = "Bearer ";

    /** 签名秘钥（至少 32 字符） */
    private String secret = "helloworld-dynamicmenu-secret-1234567890";

    /** 过期时间，单位：秒 */
    private long expiration = 86400;
}