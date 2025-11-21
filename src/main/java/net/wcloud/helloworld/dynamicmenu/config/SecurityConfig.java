package net.wcloud.helloworld.dynamicmenu.config;

import lombok.RequiredArgsConstructor;
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
 * Spring Security 核心配置
 */
@Configuration
@EnableMethodSecurity // 开启方法级别权限控制 @PreAuthorize 等
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    /**
     * 主安全过滤链配置
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // 基于 token，无需 csrf
                .csrf(csrf -> csrf.disable())

                // 不使用 session，改用 JWT（无状态会话）
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 配置请求权限
                .authorizeHttpRequests(auth -> auth

                        // 登录接口放行
                        .requestMatchers("/api/auth/login")
                        .permitAll()

                        // 其余请求都需要认证
                        .anyRequest().authenticated())

                // 异常处理：未登录 / 无权限 的 JSON 返回
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))

                // 将我们自定义的 JWT 过滤器加到 UsernamePasswordAuthenticationFilter 之前
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 指定使用自定义的认证 Provider（基于 Dao + UserDetailsService）
                .authenticationProvider(daoAuthenticationProvider());

        return http.build();
    }

    /**
     * PasswordEncoder（密码加密器）
     * 建议所有用户密码都使用 BCrypt 存储
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * DaoAuthenticationProvider：
     * 告诉 Spring Security 使用我们自己的 UserDetailsService + PasswordEncoder
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        provider.setHideUserNotFoundExceptions(false);
        return provider;
    }

    /**
     * AuthenticationManager：
     * 提供给 AuthController 在登录时使用
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}