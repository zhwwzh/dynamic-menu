package net.wcloud.helloworld.dynamicmenu.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.wcloud.helloworld.dynamicmenu.config.JwtProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 *
 * 功能职责：
 * ------------------------------------------------------------
 * 1. 从请求头中解析 Token（支持 Bearer 模式）
 * 2. 校验 Token 是否有效（签名、过期时间）
 * 3. 解析 Token 中的 username
 * 4. 查询数据库（或缓存）获取用户信息 UserDetails
 * 5. 将用户信息写入 SecurityContext，表示登录成功
 *
 * 技术说明：
 * ------------------------------------------------------------
 * - OncePerRequestFilter：保证一个请求只执行一次过滤逻辑
 * - 不会抛出异常交给 Spring Security 处理，而是自行吞掉并写入 WARN，继续链式过滤
 *
 * 日志策略（安全审计）：
 * ------------------------------------------------------------
 * - debug：用于开发阶段查看 token / 解析过程等敏感信息
 * - info ：正常登录流程触发一次
 * - warn ：Token 异常、解析失败、用户不存在等
 * - error：不可恢复的系统错误
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtProperties jwtProperties;
    private final JwtTokenUtil jwtTokenUtil;
    private final LoginUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtProperties jwtProperties,
            JwtTokenUtil jwtTokenUtil,
            LoginUserDetailsService userDetailsService) {
        this.jwtProperties = jwtProperties;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
    }

    /**
     * 过滤器主逻辑：解析 + 校验 + 认证 Token
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        log.debug("[JWT FILTER] 进入 JWT 认证过滤器, uri={}", requestURI);

        // 1. 获取 Authorization Header
        String rawHeader = request.getHeader(jwtProperties.getHeader());
        log.debug("[JWT FILTER] Raw Authorization Header = {}", rawHeader);

        // 2. 提取 Token 字符串
        String token = resolveToken(rawHeader);
        log.debug("[JWT FILTER] Resolved Token = {}", token);

        // 3. 若 token 为空，或 Context 已有认证，则不处理，直接放行
        if (!StringUtils.hasText(token)) {
            log.debug("[JWT FILTER] 请求未提供 Token，直接放行, uri={}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            log.debug("[JWT FILTER] SecurityContext 已存在 Authentication，跳过 JWT 校验, uri={}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // -------------------------------
            // Step 4: 校验 Token 签名、有效期
            // -------------------------------
            boolean valid = jwtTokenUtil.validateToken(token);
            log.debug("[JWT FILTER] Token validate result = {}", valid);

            if (!valid) {
                log.warn("[JWT FILTER] Token 无效或已过期, uri={}", requestURI);
                filterChain.doFilter(request, response);
                return;
            }

            // -------------------------------
            // Step 5: 从 Token 中解析用户名
            // -------------------------------
            String username = jwtTokenUtil.getUsername(token);
            log.debug("[JWT FILTER] Token username = {}", username);

            if (!StringUtils.hasText(username)) {
                log.warn("[JWT FILTER] Token 中无法解析 username, token={}", token);
                filterChain.doFilter(request, response);
                return;
            }

            // -------------------------------
            // Step 6: 查询用户信息
            // -------------------------------
            log.debug("[JWT FILTER] 通过 UserDetailsService 加载用户信息, username={}", username);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (userDetails == null) {
                log.warn("[JWT FILTER] 用户不存在或已被禁用, username={}", username);
                filterChain.doFilter(request, response);
                return;
            }

            log.debug("[JWT FILTER] Loaded UserDetails: username={}, authorities={}",
                    userDetails.getUsername(), userDetails.getAuthorities());

            // -------------------------------
            // Step 7: 构造 Authentication
            // -------------------------------
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities());

            authenticationToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));

            // 写入 SecurityContext，表示认证成功
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            log.info("[JWT FILTER] 用户认证成功, username={}, uri={}", username, requestURI);

        } catch (Exception ex) {
            // 禁止放过异常，否则可能造成 500，而不是 401
            log.warn("[JWT FILTER] Token 认证过程出现异常, uri={}, error={}",
                    requestURI, ex.getMessage(), ex);
        }

        // 放行后续过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 提取 Bearer Token 的真正 token 字符串
     *
     * HEADER 示例：
     * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9....
     *
     * @param header 请求头值
     * @return 提取出的 token 或 null
     */
    private String resolveToken(String header) {
        if (!StringUtils.hasText(header)) {
            log.debug("[JWT FILTER] Authorization Header 为空");
            return null;
        }

        String prefix = jwtProperties.getTokenPrefix(); // 默认 “Bearer ”
        if (StringUtils.hasText(prefix) && header.startsWith(prefix)) {
            String token = header.substring(prefix.length()).trim();
            log.debug("[JWT FILTER] Token 去除前缀后 = {}", token);
            return token;
        }

        // 不以 Bearer 开头，也允许直接传 token
        log.debug("[JWT FILTER] Header 未包含前缀，直接使用原始 header 内容作为 token");
        return header.trim();
    }
}
