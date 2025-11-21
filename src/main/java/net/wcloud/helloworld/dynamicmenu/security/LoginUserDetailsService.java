package net.wcloud.helloworld.dynamicmenu.security;

import lombok.RequiredArgsConstructor;
import net.wcloud.helloworld.dynamicmenu.entity.User;
import net.wcloud.helloworld.dynamicmenu.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring Security 的用户加载服务
 *
 * 认证流程中，Spring 会调用 loadUserByUsername 获取 LoginUserDetails：
 * - 登录时（用户名密码登录）
 * - JwtAuthenticationFilter 从 token 中解析出 username 时
 */
@Service
@RequiredArgsConstructor
public class LoginUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 查用户
        User user = userService.getByUsername(username);
        if (user == null) {
            // Spring 会把这个异常认为是“用户名不存在”
            throw new UsernameNotFoundException("用户不存在：" + username);
        }

        // 2. 查角色 & 权限
        List<String> roleCodes = userService.listRoleCodesByUserId(user.getId());
        List<String> permissions = userService.listPermissionsByUserId(user.getId());

        // 3. 返回我们自己的 LoginUserDetails（Security 后续会使用）
        return new LoginUserDetails(user, roleCodes, permissions);
    }
}
