package net.wcloud.helloworld.dynamicmenu.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.wcloud.helloworld.dynamicmenu.common.Result;
import net.wcloud.helloworld.dynamicmenu.dto.LoginRequestDTO;
import net.wcloud.helloworld.dynamicmenu.dto.LoginResponseDTO;
import net.wcloud.helloworld.dynamicmenu.entity.User;
import net.wcloud.helloworld.dynamicmenu.security.JwtTokenUtil;
import net.wcloud.helloworld.dynamicmenu.security.LoginUserDetails;
import net.wcloud.helloworld.dynamicmenu.service.MenuService;
import net.wcloud.helloworld.dynamicmenu.service.UserService;
import net.wcloud.helloworld.dynamicmenu.vo.MenuVO;
import net.wcloud.helloworld.dynamicmenu.vo.UserVO;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 登录 / JWT / 用户信息接口
 *
 * 功能说明：
 * ------------------------------------------------------------
 * 1. /login 负责用户名密码登录，校验成功后生成 JWT
 * 2. /me 返回当前登录用户的详细信息（带角色/权限/菜单树）
 *
 * 安全说明：
 * ------------------------------------------------------------
 * - 登录成功后不会创建 Session，而是将用户信息写入 JWT
 * - 后续请求由 JwtAuthenticationFilter 解析 token 设置 SecurityContext
 *
 * 日志策略：
 * ------------------------------------------------------------
 * - info : 登录成功日志、关键审计行为
 * - warn : 登录失败（用户名不存在 / 密码错误）
 * - error : 系统异常
 * - debug : 开发阶段查看细节（菜单数量、权限数量等）
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final MenuService menuService;
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * 登录接口（用户名 + 密码）
     */
    @PostMapping("/login")
    public Result<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO req) {

        String username = req.getUsername();
        log.info("[AuthController] 用户请求登录, username={}, ip={}",
                username, req != null ? "unknown" : "");

        try {
            // 1) 进行密码校验
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, req.getPassword()));

            // 认证成功，写入 SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            LoginUserDetails loginUser = (LoginUserDetails) authentication.getPrincipal();
            User dbUser = loginUser.getUser();

            log.info("[AuthController] 登录成功, username={}, userId={}",
                    dbUser.getUsername(), dbUser.getId());

            // 2) 创建 JWT Token
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", dbUser.getId());

            String token = jwtTokenUtil.generateToken(dbUser.getUsername(), claims);
            log.debug("[AuthController] JWT 生成成功, username={}", dbUser.getUsername());

            // 3) 加载菜单树 & 权限
            List<MenuVO> menus = menuService.listMenuTreeByUserId(dbUser.getId());
            List<String> permissions = loginUser.getPermissions();

            log.debug("[AuthController] 用户权限加载成功, username={}, permCount={}, menuRootCount={}",
                    dbUser.getUsername(),
                    permissions == null ? 0 : permissions.size(),
                    menus == null ? 0 : menus.size());

            // 4) 封装登录返回体
            LoginResponseDTO resp = new LoginResponseDTO();
            resp.setToken(token);
            resp.setUserId(dbUser.getId());
            resp.setUsername(dbUser.getUsername());
            resp.setNickname(dbUser.getNickname());
            resp.setMenus(menus);
            resp.setPermissions(permissions);

            return Result.success(resp);

        } catch (BadCredentialsException e) {
            log.warn("[AuthController] 登录失败：用户名或密码错误, username={}", username);
            return Result.fail(401, "用户名或密码错误");

        } catch (Exception e) {
            log.error("[AuthController] 登录异常, username={}, error={}",
                    username, e.getMessage(), e);
            return Result.fail(500, "登录异常：" + e.getClass().getSimpleName());
        }
    }

    /**
     * 获取当前登录用户信息（包含角色、权限、菜单树）
     */
    @GetMapping("/me")
    public Result<UserVO> me() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUserDetails loginUser)) {
            log.warn("[AuthController] 未登录访问 /me");
            return Result.fail(401, "未登录");
        }

        Long userId = loginUser.getUser().getId();
        log.info("[AuthController] 查询当前用户信息 /me, userId={}", userId);

        UserVO userVO = userService.getUserDetail(userId);

        if (log.isDebugEnabled()) {
            log.debug("[AuthController] /me 返回成功, username={}, menuRootCount={}",
                    loginUser.getUsername(),
                    userVO.getMenus() == null ? 0 : userVO.getMenus().size());
        }

        return Result.success(userVO);
    }
}
