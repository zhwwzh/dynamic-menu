package net.wcloud.helloworld.dynamicmenu.security;

import lombok.Getter;
import net.wcloud.helloworld.dynamicmenu.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * LoginUserDetails
 *
 * 这是 Spring Security 在用户认证 / 鉴权过程中使用的核心对象。
 *
 * 使用场景：
 * ------------------------------------------------------------
 * 1. 登录时：UserDetailsService.loadUserByUsername() 返回此对象
 * 2. 认证成功后：放入 SecurityContextHolder
 * 3. 每次请求中：JwtAuthenticationFilter 重新构建该对象并放入 SecurityContext
 *
 * 作用：
 * ------------------------------------------------------------
 * - 提供用户密码
 * - 提供用户名
 * - 提供角色和权限点（组合成 GrantedAuthority）
 * - 决定用户是否可用（isEnabled）
 */
@Getter
public class LoginUserDetails implements UserDetails {

    private static final Logger log = LoggerFactory.getLogger(LoginUserDetails.class);

    /**
     * 真实 User 实体
     * 使用组合而不是继承，避免污染数据库实体
     */
    private final User user;

    /** 角色编码列表（必须以 ROLE_ 开头，Security 才能识别） */
    private final List<String> roleCodes;

    /** 权限标识列表（按钮级权限，例如："sys:user:list"） */
    private final List<String> permissions;

    public LoginUserDetails(User user, List<String> roleCodes, List<String> permissions) {
        this.user = user;
        this.roleCodes = roleCodes;
        this.permissions = permissions;

        log.debug("[LoginUserDetails] 创建 LoginUserDetails 对象, userId={}, username={}, roleCount={}, permCount={}",
                (user == null ? null : user.getId()),
                (user == null ? null : user.getUsername()),
                (roleCodes == null ? 0 : roleCodes.size()),
                (permissions == null ? 0 : permissions.size()));
    }

    /**
     * 返回角色 + 权限点，供 Spring Security 授权判断使用。
     *
     * Security 中的权限判断规则：
     * ------------------------------------------------------------
     * - hasRole("ADMIN") → 实际匹配 ROLE_ADMIN
     * - hasAuthority("sys:user:list")
     *
     * 因此此处包含两类数据：
     * - SimpleGrantedAuthority("ROLE_ADMIN")
     * - SimpleGrantedAuthority("sys:user:list")
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<String> authorities = getAllAuthorityStrings();

        log.debug("[getAuthorities] 构建 GrantedAuthority 列表, username={}, count={}",
                user.getUsername(), authorities.size());

        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    /**
     * 合并角色编码与权限标识
     *
     * 注意事项：
     * ------------------------------------------------------------
     * - 必须 DISTINCT 去重，否则重复权限可能导致性能问题
     * - 若角色列表或权限列表为 null，需要防御式处理
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
     * 返回数据库中保存的加密密码
     */
    @Override
    public String getPassword() {
        String pwd = user.getPassword();
        log.debug("[getPassword] 获取密码（已加密），username={}, passwordLength={}",
                user.getUsername(), pwd == null ? 0 : pwd.length());
        return pwd;
    }

    /**
     * 返回用户名
     */
    @Override
    public String getUsername() {
        String username = user.getUsername();
        log.debug("[getUsername] 获取 username={}", username);
        return username;
    }

    /** 账户是否未过期，项目未做控制统一返回 true */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /** 账户是否未锁定，如需要可扩展 User.locked 字段 */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /** 密码凭证是否有效（一般不用） */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 是否启用账号（安全控制最常用字段）
     * status = 1 → 启用
     * status !=1 → 禁用
     */
    @Override
    public boolean isEnabled() {
        boolean enabled = user.getStatus() != null && user.getStatus() == 1;

        if (!enabled) {
            log.warn("[isEnabled] 用户已被禁用, userId={}, username={}", user.getId(), user.getUsername());
        } else {
            log.debug("[isEnabled] 用户可用, username={}", user.getUsername());
        }

        return enabled;
    }
}
