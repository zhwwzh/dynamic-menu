package net.wcloud.helloworld.dynamicmenu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.wcloud.helloworld.dynamicmenu.entity.User;
import net.wcloud.helloworld.dynamicmenu.vo.UserVO;

import java.util.List;

/**
 * 用户业务接口（核心：用户、角色、权限、菜单）
 *
 * - 此 Service 是动态菜单权限系统的基础服务。
 * - 负责提供用户登录验证、角色权限查询、个人信息查询、后台用户列表等功能。
 * - 供 Security 模块、UserController、后台管理模块使用。
 */
public interface UserService extends IService<User> {

    /**
     * 根据用户名查询用户
     *
     * 使用场景：
     * - 登录接口：Spring Security 的 UserDetailsService 根据用户名加载用户
     * - 获取密码、状态（启用/禁用）、用户ID等
     *
     * @param username 用户名
     * @return 用户实体（不包含角色、权限、菜单）
     */
    User getByUsername(String username);

    /**
     * 根据用户ID查询角色编码列表
     *
     * 说明：
     * - 用户可能拥有多个角色，如 admin 拥有 ["ROLE_ADMIN", "ROLE_USER"]
     * - 角色编码一般用于 Security 鉴权（例如 hasRole）
     * - 在动态菜单中也用于判断可访问的菜单节点
     *
     * 使用场景：
     * - 鉴权：SecurityContext 权限注入
     * - 前端：展示用户角色
     *
     * @param userId 用户ID
     * @return 角色编码集合
     */
    List<String> listRoleCodesByUserId(Long userId);

    /**
     * 根据用户ID查询权限标识列表
     *
     * 说明：
     * - 权限标识如 "sys:user:list"、"sys:menu:update"
     * - 来自角色绑定的菜单按钮级权限（多角色合并去重）
     * - 用于 Spring Security 的 hasAuthority("sys:xxx")
     *
     * 使用场景：
     * - Security 动态权限
     * - 网关鉴权（如果集成微服务）
     * - 前端按钮权限控制
     *
     * @param userId 用户ID
     * @return 权限标识集合
     */
    List<String> listPermissionsByUserId(Long userId);

    /**
     * 获取用户详情（角色 / 权限 / 菜单树）
     *
     * 说明：
     * - 这是最重要的接口之一，用于“当前用户信息”功能
     * - 返回给前端一个完整的用户上下文对象 UserVO
     *
     * 返回内容包括：
     * - 用户基础信息（id、username、nickname、avatar 等）
     * - 角色编码列表
     * - 权限标识列表（按钮级）
     * - 菜单树（前端根据该树渲染侧边栏）
     *
     * 使用场景：
     * - 前端刷新页面时，获取登录用户可访问菜单
     * - "个人中心" 显示用户详情
     *
     * @param userId 用户ID
     * @return 包含角色/权限/菜单树的 UserVO
     */
    UserVO getUserDetail(Long userId);

    /**
     * 查询所有用户列表（附带角色、权限、菜单信息）
     *
     * 说明：
     * - 后台“用户管理”列表使用
     * - 会查询每个用户的：
     * • 角色信息（编码、名称）
     * • 权限列表（权限标识）
     * • 菜单（一般不包含菜单树，避免性能开销）
     *
     * 性能说明：
     * - 会进行多表 JOIN（user-role、role-menu、menu-permission）
     * - 建议分页查询
     *
     * 使用场景：
     * - 后台 admin 角色查看系统所有用户
     * - 用户列表管理界面需要显示用户角色
     *
     * @return 用户 VO 列表（带角色/权限信息）
     */
    List<UserVO> listUsersWithDetail();
}
