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

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // 打印 Header & Token，确认解析无误
        String rawHeader = request.getHeader(jwtProperties.getHeader());
        log.debug("Authorization Header Raw = {}", rawHeader);

        String token = resolveToken(rawHeader);
        log.debug("Resolved Token = {}", token);

        // 如果已经有认证，就不用再处理 JWT 了
        if (!StringUtils.hasText(token) ||
                SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 1. 校验 token 是否有效（签名 & 过期）
            boolean valid = jwtTokenUtil.validateToken(token);
            log.debug("JWT validateToken(token) = {}", valid);

            if (!valid) {
                // 不抛异常，交给后续的未认证处理（401）
                filterChain.doFilter(request, response);
                return;
            }

            // 2. 解析用户名（subject）
            String username = jwtTokenUtil.getUsername(token);
            log.debug("JWT getUsername(token) = {}", username);

            if (!StringUtils.hasText(username)) {
                log.warn("JWT 中未解析出 username，token={}", token);
                filterChain.doFilter(request, response);
                return;
            }

            // 3. 加载用户信息
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            log.debug("Loaded UserDetails username={}, authorities={}",
                    userDetails.getUsername(), userDetails.getAuthorities());

            // 4. 构造 Authentication
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities());
            authenticationToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            log.debug("SecurityContext 已设置认证用户 username={}", username);

        } catch (Exception ex) {
            // 任何异常都打印出来，方便定位
            log.warn("JWT 认证过程中发生异常, uri={}, msg={}",
                    request.getRequestURI(), ex.getMessage(), ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从 Header 中提取真正 token 字符串
     */
    private String resolveToken(String header) {
        if (!StringUtils.hasText(header)) {
            return null;
        }

        String prefix = jwtProperties.getTokenPrefix(); // 比如 "Bearer "
        if (StringUtils.hasText(prefix) && header.startsWith(prefix)) {
            return header.substring(prefix.length()).trim();
        }
        return header.trim();
    }
}
