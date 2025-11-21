package net.wcloud.helloworld.dynamicmenu.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.wcloud.helloworld.dynamicmenu.security.JwtAuthenticationFilter;
import net.wcloud.helloworld.dynamicmenu.security.LoginUserDetailsService;
import net.wcloud.helloworld.dynamicmenu.security.RestAccessDeniedHandler;
import net.wcloud.helloworld.dynamicmenu.security.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 核心配置类
 *
 * 职责说明：
 * ------------------------------------------------------------
 * 1. 配置全局安全过滤链（SecurityFilterChain）
 * - 禁用 CSRF（基于 JWT，无状态）
 * - 配置 Session 策略为 STATELESS
 * - 配置 URL 级别的权限规则
 * - 配置异常处理（401 / 403 JSON 返回）
 * - 注册自定义 JwtAuthenticationFilter
 *
 * 2. 配置 AuthenticationProvider
 * - 使用自定义 LoginUserDetailsService
 * - 使用 BCryptPasswordEncoder
 *
 * 3. 暴露 AuthenticationManager
 * - 提供给登录接口（AuthController）进行密码校验
 *
 * 开启注解：
 * ------------------------------------------------------------
 * 
 * @EnableMethodSecurity
 *                       - 开启方法级别权限控制（@PreAuthorize、@PostAuthorize 等）
 */
@Slf4j
@Configuration
@EnableMethodSecurity // 开启方法级别权限控制 @PreAuthorize 等
@RequiredArgsConstructor
public class SecurityConfig {

    /** 自定义的用户加载服务（包含角色 + 权限） */
    private final LoginUserDetailsService userDetailsService;

    /** JWT 认证过滤器：解析 Token，设置 SecurityContext */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /** 未认证（未登录）时的处理器（返回 401） */
    private final RestAuthenticationEntryPoint authenticationEntryPoint;

    /** 已认证但无权限访问时的处理器（返回 403） */
    private final RestAccessDeniedHandler accessDeniedHandler;

    /**
     * 主安全过滤链配置（Spring Security 核心）
     *
     * 说明：
     * ------------------------------------------------------------
     * - 所有 HTTP 请求都会经过该过滤链
     * - 我们在这里完成：
     * 1) 禁用 CSRF
     * 2) 配置 Session 策略（无状态）
     * 3) 配置哪些 URL 放行 / 需要认证
     * 4) 配置异常处理（401 / 403）
     * 5) 注册 JWT 过滤器
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        log.info("[SecurityConfig] 开始构建 SecurityFilterChain");

        http
                // ============================================================
                // 1. 基于 JWT，无需 CSRF（跨站请求伪造）防护
                // 如果未来有表单登录、同域 Cookie 场景，可再考虑打开
                // ============================================================
                .csrf(csrf -> {
                    csrf.disable();
                    log.debug("[SecurityConfig] CSRF 已禁用（JWT 无状态鉴权）");
                })

                // ============================================================
                // 2. Session 管理：无状态（STATELESS）
                // - 不创建 HttpSession
                // - 所有认证状态依赖 JWT
                // ============================================================
                .sessionManagement(sm -> {
                    sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                    log.debug("[SecurityConfig] SessionCreationPolicy 设置为 STATELESS");
                })

                // ============================================================
                // 3. URL 授权配置
                // - 明确哪些接口匿名可访问，哪些需要登录
                // ============================================================
                .authorizeHttpRequests(auth -> {
                    auth
                            // 登录接口放行（匿名访问）
                            .requestMatchers("/api/auth/login").permitAll()

                            // 你可以在这里继续添加白名单接口，例如 Swagger、静态资源等：
                            // .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                            // 其余所有请求，都需要认证后才能访问
                            .anyRequest().authenticated();

                    log.debug("[SecurityConfig] URL 权限规则已配置：/api/auth/login 允许匿名访问，其它请求需认证");
                })

                // ============================================================
                // 4. 异常处理：统一返回 JSON
                // - 未认证：RestAuthenticationEntryPoint（401）
                // - 无权限：RestAccessDeniedHandler（403）
                // ============================================================
                .exceptionHandling(ex -> {
                    ex.authenticationEntryPoint(authenticationEntryPoint)
                            .accessDeniedHandler(accessDeniedHandler);
                    log.debug("[SecurityConfig] 已配置 AuthenticationEntryPoint 和 AccessDeniedHandler");
                })

                // ============================================================
                // 5. 注册 JWT 过滤器：
                // - 在 UsernamePasswordAuthenticationFilter 之前执行
                // - 作用：从 Header 提取 Token，设置 SecurityContext
                // ============================================================
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // ============================================================
                // 6. 指定认证 Provider：
                // - 使用自定义 UserDetailsService + PasswordEncoder
                // ============================================================
                .authenticationProvider(daoAuthenticationProvider());

        SecurityFilterChain chain = http.build();
        log.info("[SecurityConfig] SecurityFilterChain 构建完成");

        return chain;
    }

    /**
     * PasswordEncoder（密码加密器）
     *
     * 说明：
     * ------------------------------------------------------------
     * - 使用 BCryptPasswordEncoder 作为默认密码哈希算法
     * - 所有用户密码入库前都应该使用 passwordEncoder.encode(rawPassword) 进行加密
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        log.info("[SecurityConfig] 已创建 BCryptPasswordEncoder 作为 PasswordEncoder 实现");
        return encoder;
    }

    /**
     * DaoAuthenticationProvider：
     *
     * 职责：
     * ------------------------------------------------------------
     * - 使用 UserDetailsService 加载用户信息（用户名、密码、角色、权限）
     * - 使用 PasswordEncoder 校验密码
     *
     * setHideUserNotFoundExceptions(false)：
     * ------------------------------------------------------------
     * - 默认情况下，用户名不存在与密码错误统一抛 BadCredentialsException
     * - 设置为 false 后，用户名不存在会抛 UsernameNotFoundException
     * - 方便我们进行更精细的错误区分（可在登录日志中使用）
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        provider.setHideUserNotFoundExceptions(false);

        log.info(
                "[SecurityConfig] DaoAuthenticationProvider 初始化完成, 使用自定义 LoginUserDetailsService + BCryptPasswordEncoder");
        return provider;
    }

    /**
     * AuthenticationManager：
     *
     * 说明：
     * ------------------------------------------------------------
     * - Spring Security 的核心认证管理器
     * - AuthController 在进行用户名密码登录时会直接使用该 Bean：
     * authenticationManager.authenticate(UsernamePasswordAuthenticationToken)
     *
     * AuthenticationConfiguration：
     * ------------------------------------------------------------
     * - 由 Spring Boot 自动配置
     * - 会自动整合所有 AuthenticationProvider
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        AuthenticationManager authenticationManager = configuration.getAuthenticationManager();
        log.info("[SecurityConfig] AuthenticationManager 从 AuthenticationConfiguration 中获取完成");
        return authenticationManager;
    }
}
