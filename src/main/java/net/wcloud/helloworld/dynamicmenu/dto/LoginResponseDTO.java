package net.wcloud.helloworld.dynamicmenu.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.wcloud.helloworld.dynamicmenu.vo.MenuVO;

import java.util.List;

/**
 * 登录响应 DTO（返回给前端的数据结构）
 *
 * 说明：
 * ------------------------------------------------------------
 * 登录成功后，API 返回以下信息：
 * 1. token → 客户端存储，用于后续请求认证
 * 2. 基本用户信息 → userId / username / nickname
 * 3. 用户菜单树 → 前端动态路由 & 菜单渲染
 * 4. 权限标识列表 → 前端按钮级权限控制（v-if / disable）
 *
 * 安全说明：
 * ------------------------------------------------------------
 * - DTO 本身不应打印 token（敏感数据）
 * - logSelf() 方法提供安全日志功能，只输出必要调试信息
 *
 * 日志策略：
 * ------------------------------------------------------------
 * - debug：用于开发阶段调试，不曝光敏感信息（token）
 * - info/warn/error：交由 Controller 控制，不放在 DTO 内
 */
@Slf4j
@Data
public class LoginResponseDTO {

    /** 登录成功后颁发的 JWT Token */
    private String token;

    /** 当前登录用户基本信息 */
    private Long userId;
    private String username;
    private String nickname;

    /** 当前用户可访问的菜单树（用于左侧菜单 / 路由） */
    private List<MenuVO> menus;

    /** 当前用户拥有的权限列表（如：sys:user:list） */
    private List<String> permissions;
}
