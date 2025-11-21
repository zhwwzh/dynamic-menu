package net.wcloud.helloworld.dynamicmenu.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.wcloud.helloworld.dynamicmenu.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * RestAuthenticationEntryPoint
 *
 * 处理用户“未认证（未登录）或 token 无效/过期”的情况。
 *
 * 触发场景：
 * ------------------------------------------------------------
 * 1. 用户未登录访问受保护资源（无 token）
 * 2. Token 过期（ExpiredJwtException）
 * 3. Token 签名非法（SignatureException）
 * 4. JwtAuthenticationFilter 捕获到异常并未认证
 *
 * AuthenticationEntryPoint vs AccessDeniedHandler：
 * ------------------------------------------------------------
 * - AuthenticationEntryPoint：未登录 → 返回 401（Unauthorized）
 * - AccessDeniedHandler：已登录但无权限 → 返回 403（Forbidden）
 *
 * 返回内容（JSON）：
 * ------------------------------------------------------------
 * {
 * "code": 401,
 * "message": "未登录或登录状态已过期",
 * "data": null
 * }
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(RestAuthenticationEntryPoint.class);

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException, ServletException {

        // 记录安全审计日志
        log.warn("[AuthenticationEntryPoint] 未认证访问被拦截! method={}, uri={}, ip={}, error={}",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr(),
                (authException == null ? "NONE" : authException.getMessage()));

        // 设置 HTTP 响应状态码和 JSON 类型
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 统一响应体
        Result<?> body = Result.fail(401, "未登录或登录状态已过期");
        String json = objectMapper.writeValueAsString(body);

        log.debug("[AuthenticationEntryPoint] 响应 JSON: {}", json);

        // 输出 JSON
        response.getWriter().write(json);
    }
}
