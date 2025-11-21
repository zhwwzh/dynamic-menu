package net.wcloud.helloworld.dynamicmenu.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.wcloud.helloworld.dynamicmenu.entity.User;
import net.wcloud.helloworld.dynamicmenu.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * LoginUserDetailsService
 *
 * Spring Security 在认证流程中用于加载“用户 + 角色/权限”信息的核心服务。
 *
 * 调用时机：
 * ------------------------------------------------------------
 * 1. 密码登录：UsernamePasswordAuthenticationFilter -> AuthenticationManager
 * 2. JWT 登录：JwtAuthenticationFilter 从 token 获取 username 后调用
 *
 * 返回对象：
 * ------------------------------------------------------------
 * 返回 LoginUserDetails，包括：
 * - User 实体（id, username, password, status）
 * - roleCodes（ROLE_ADMIN 等）
 * - permissions（sys:user:list 等按钮权限）
 *
 * 日志策略：
 * ------------------------------------------------------------
 * - info : 用户加载成功
 * - warn : 用户不存在 / 禁用 / 加载异常
 * - debug : 加载角色、权限等细节
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginUserDetailsService implements UserDetailsService {

    private final UserService userService;

    /**
     * 根据 username 加载用户完整权限信息
     *
     * @param username 登录账号
     * @return LoginUserDetails
     * @throws UsernameNotFoundException 用户不存在时必须抛出
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.info("[LoginUserDetailsService] 开始加载用户, username={}", username);

        // 1. 查询用户
        User user = userService.getByUsername(username);

        if (user == null) {
            log.warn("[LoginUserDetailsService] 用户不存在, username={}", username);
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        log.debug("[LoginUserDetailsService] 查询到用户 userId={}, username={}, status={}",
                user.getId(), user.getUsername(), user.getStatus());

        // 2. 加载角色编码
        List<String> roleCodes = userService.listRoleCodesByUserId(user.getId());
        log.debug("[LoginUserDetailsService] 用户角色加载完成, username={}, roleCount={}, roles={}",
                username, roleCodes == null ? 0 : roleCodes.size(), roleCodes);

        // 3. 加载按钮权限
        List<String> permissions = userService.listPermissionsByUserId(user.getId());
        log.debug("[LoginUserDetailsService] 用户权限加载完成, username={}, permCount={}, perms={}",
                username, permissions == null ? 0 : permissions.size(), permissions);

        // 4. 组装 UserDetails
        LoginUserDetails loginUserDetails = new LoginUserDetails(user, roleCodes, permissions);

        log.info("[LoginUserDetailsService] 用户权限对象创建成功, username={}, roleCount={}, permCount={}",
                username,
                roleCodes == null ? 0 : roleCodes.size(),
                permissions == null ? 0 : permissions.size());

        return loginUserDetails;
    }
}
