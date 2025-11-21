package net.wcloud.helloworld.dynamicmenu.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.wcloud.helloworld.dynamicmenu.common.Result;
import net.wcloud.helloworld.dynamicmenu.dto.LoginRequestDTO;
import net.wcloud.helloworld.dynamicmenu.entity.User;
import net.wcloud.helloworld.dynamicmenu.security.JwtTokenUtil;
import net.wcloud.helloworld.dynamicmenu.security.LoginUserDetails;
import net.wcloud.helloworld.dynamicmenu.service.MenuService;
import net.wcloud.helloworld.dynamicmenu.service.UserService;
import net.wcloud.helloworld.dynamicmenu.dto.LoginResponseDTO;
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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final MenuService menuService;
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * 登录接口
     */
    @PostMapping("/login")
    public Result<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO req) {
        System.out.println(">>> Enter AuthController.login, username = " + req.getUsername());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            LoginUserDetails loginUser = (LoginUserDetails) authentication.getPrincipal();
            User dbUser = loginUser.getUser();

            // JWT
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", dbUser.getId());
            String token = jwtTokenUtil.generateToken(dbUser.getUsername(), claims);

            // 菜单树 & 权限
            List<MenuVO> menus = menuService.listMenuTreeByUserId(dbUser.getId());
            List<String> permissions = loginUser.getPermissions();

            LoginResponseDTO resp = new LoginResponseDTO();
            resp.setToken(token);
            resp.setUserId(dbUser.getId());
            resp.setUsername(dbUser.getUsername());
            resp.setNickname(dbUser.getNickname());
            resp.setMenus(menus);
            resp.setPermissions(permissions);

            return Result.success(resp);
        } catch (BadCredentialsException e) {
            e.printStackTrace(); // 暂时打印堆栈，确认是密码错误
            return Result.fail(401, "用户名或密码错误");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail(500, "登录异常：" + e.getClass().getSimpleName());
        }
    }

    /**
     * 获取当前登录用户信息（带菜单 & 权限）
     */
    @GetMapping("/me")
    public Result<UserVO> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUserDetails loginUser)) {
            return Result.fail(401, "未登录");
        }

        Long userId = loginUser.getUser().getId();
        UserVO userVO = userService.getUserDetail(userId);
        return Result.success(userVO);
    }
}