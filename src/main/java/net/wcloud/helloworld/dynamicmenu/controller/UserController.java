package net.wcloud.helloworld.dynamicmenu.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.wcloud.helloworld.dynamicmenu.common.Result;
import net.wcloud.helloworld.dynamicmenu.vo.UserVO;
import net.wcloud.helloworld.dynamicmenu.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理接口（UserController）
 *
 * 功能说明：
 * ------------------------------------------------------------
 * 1. 查询用户列表（含角色/权限等）
 * 2. 测试接口：验证 JWT 是否能正常解析
 *
 * 权限说明：
 * ------------------------------------------------------------
 * - @PreAuthorize("hasAuthority('sys:user:list')")
 * 表示必须拥有 sys:user:list 按钮权限才能访问
 *
 * 日志策略：
 * ------------------------------------------------------------
 * - info : 用户列表查询等关键行为
 * - warn : 未授权访问（交由 AccessDeniedHandler 处理）
 * - debug : 返回用户数量
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户列表（包含角色编码、角色名称、权限列表）
     *
     * 权限控制：
     * ------------------------------------------------------------
     * sys:user:list → 按钮级权限
     * 必须在角色菜单授权页勾选该按钮，才能访问此接口
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('sys:user:list')")
    public Result<List<UserVO>> listUsers() {

        log.info("[UserController] 查询用户列表开始");

        List<UserVO> list = userService.listUsersWithDetail();

        log.debug("[UserController] 查询用户列表成功, 用户数={}", list == null ? 0 : list.size());

        return Result.success(list);
    }

    /**
     * 测试接口：只要用户已登录即可访问
     *
     * 用途：
     * ------------------------------------------------------------
     * - 测试 JWT 是否生效
     * - 测试 SecurityConfig 是否正确识别认证用户
     * - 用于调试前端登录流程
     */
    @GetMapping("/testAuth")
    public Result<String> testAuth() {

        log.info("[UserController] /testAuth 已访问（用户已认证）");

        return Result.success("You are authenticated.");
    }
}
