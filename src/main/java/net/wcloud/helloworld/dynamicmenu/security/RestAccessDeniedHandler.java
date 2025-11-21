package net.wcloud.helloworld.dynamicmenu.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.wcloud.helloworld.dynamicmenu.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * RestAccessDeniedHandler
 *
 * 已认证但无权限访问时触发（403 Forbidden）
 *
 * 触发场景：
 * ------------------------------------------------------------
 * 1. 用户已登录，但没有访问某接口的权限
 * 2. @PreAuthorize / @Secured 权限校验不通过
 * 3. SecurityConfig 中配置的 URL 权限规则匹配失败
 *
 * 注意：与 AuthenticationEntryPoint 不同
 * ------------------------------------------------------------
 * - AuthenticationEntryPoint：用户“未认证”“未登录” → 返回 401
 * - AccessDeniedHandler：用户“已认证”“权限不足” → 返回 403
 *
 * 返回内容：
 * ------------------------------------------------------------
 * {
 * "code": 403,
 * "message": "无权限访问该资源",
 * "data": null
 * }
 *
 * 日志说明：
 * ------------------------------------------------------------
 * - warn：记录无权限访问日志（包含用户、IP、URI、方法）
 * - debug：可开启查看序列化 JSON
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(RestAccessDeniedHandler.class);

    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        // 获取当前登录用户信息（可能为 null）
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication();
        String username = (authentication == null ? "匿名用户" : authentication.getName());

        // 记录审计日志
        log.warn("[AccessDenied] 用户权限不足! username={}, method={}, uri={}, ip={}, msg={}",
                username,
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr(),
                accessDeniedException.getMessage());

        // 设置响应头
        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 构造返回 JSON
        Result<?> body = Result.fail(403, "无权限访问该资源");
        String json = objectMapper.writeValueAsString(body);

        log.debug("[AccessDenied] 返回 JSON 响应: {}", json);

        response.getWriter().write(json);
    }
}
