package net.wcloud.helloworld.dynamicmenu.security;

import lombok.Getter;
import net.wcloud.helloworld.dynamicmenu.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * LoginUserDetails
 *
 * 这是 Spring Security 在认证过程中使用的“用户安全对象”。
 *
 * 主要作用：
 * 1. 在登录时，AuthenticationManager 会将 username/password 提交给 UserDetailsService，
 * UserDetailsService 根据用户名加载 UserDetails（也就是此对象）。
 *
 * 2. Spring Security 会从此对象中获取：
 * - 密码（getPassword）
 * - 用户名（getUsername）
 * - 用户是否启用（isEnabled）
 * - 角色/权限（getAuthorities）
 *
 * 3. 登录成功后，此对象会被存入 SecurityContext（线程上下文），
 * 后续每次请求会通过 JwtAuthenticationFilter 将该对象重新放入 SecurityContext。
 */
@Getter
public class LoginUserDetails implements UserDetails {

    /**
     * 绑定你系统的真实 User 实体（来自 dynamicmenu_sys_user）
     * 这里不直接继承 User，而是使用组合方式，避免污染实体类。
     */
    private final User user;

    /**
     * 用户角色编码列表（如：["ROLE_ADMIN", "ROLE_USER"]）
     * 注意：角色必须以 "ROLE_" 开头，Spring Security 才能正确识别。
     */
    private final List<String> roleCodes;

    /**
     * 用户权限标识（按钮权限集合，如：["sys:user:list", "sys:user:add"]）
     * 用于实现按钮级（Action-Level）权限控制。
     */
    private final List<String> permissions;

    /**
     * 构造方法
     */
    public LoginUserDetails(User user, List<String> roleCodes, List<String> permissions) {
        this.user = user;
        this.roleCodes = roleCodes;
        this.permissions = permissions;
    }

    /**
     * 获取当前用户的权限集合（角色 + 权限点）
     *
     * Spring Security 授权流程会调用：
     * getAuthorities() -> List<GrantedAuthority>
     *
     * 注意：
     * 1. 角色会以 SimpleGrantedAuthority("ROLE_ADMIN") 的形式加入；
     * 2. 权限点也会以 SimpleGrantedAuthority("sys:user:list") 的形式加入；
     * 3. 最终 Security 会将两类权限统一处理，框架会根据 hasRole / hasAuthority 做校验。
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getAllAuthorityStrings().stream()
                .map(SimpleGrantedAuthority::new) // 转为 GrantedAuthority
                .collect(Collectors.toList());
    }

    /**
     * 组合角色 + 权限点字符串
     */
    private List<String> getAllAuthorityStrings() {
        return List.of(
                // 两个 List<String> 合并为一个 stream
                roleCodes.stream(),
                permissions.stream()).stream()
                .flatMap(s -> s) // 拍平
                .distinct() // 去重
                .collect(Collectors.toList());
    }

    /**
     * 返回加密后的密码
     *
     * 用于登录认证时，AuthenticationProvider 会自动调用此方法，
     * 并使用 PasswordEncoder.matches(原始密码，加密后的密码) 进行校验。
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * 返回用户名（登录名）
     */
    @Override
    public String getUsername() {
        return user.getUsername();
    }

    /**
     * 账户是否未过期
     * 一般不使用此功能，全项目统一返回 true 即可
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 账户是否未锁定
     * 你可以在 User 表中增加锁定字段并在此处理
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 凭据是否未过期
     * 一般不做密码有效期限制，这里默认 true 即可
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 是否启用账号（此方法最常使用）
     *
     * 绑定你自己的 User 表字段：
     * status = 1 → Enabled（启用）
     * status = 0 → Disabled（禁用）
     *
     * 如果返回 false，则 Security 会抛异常“User is disabled”
     */
    @Override
    public boolean isEnabled() {
        return user.getStatus() != null && user.getStatus() == 1;
    }
}
