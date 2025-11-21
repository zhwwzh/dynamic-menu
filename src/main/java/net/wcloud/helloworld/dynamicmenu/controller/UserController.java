package net.wcloud.helloworld.dynamicmenu.controller;

import lombok.RequiredArgsConstructor;
import net.wcloud.helloworld.dynamicmenu.common.Result;
import net.wcloud.helloworld.dynamicmenu.vo.UserVO;
import net.wcloud.helloworld.dynamicmenu.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理接口
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户列表（包含角色编码、角色名称、权限列表）
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('sys:user:list')")
    public Result<List<UserVO>> listUsers() {
        List<UserVO> list = userService.listUsersWithDetail();
        return Result.success(list);
    }

    /**
     * 简单测试：只要登录即可访问
     */
    @GetMapping("/testAuth")
    public Result<String> testAuth() {
        return Result.success("You are authenticated.");
    }
}
