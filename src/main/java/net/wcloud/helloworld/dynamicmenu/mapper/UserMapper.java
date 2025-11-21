package net.wcloud.helloworld.dynamicmenu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.wcloud.helloworld.dynamicmenu.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * UserMapper（用户表 Mapper）
 *
 * 负责 dynamicmenu_sys_user 表的查询功能，并支持 RBAC 权限体系下的：
 * - 用户基本信息查询
 * - 用户 → 角色编码列表查询
 * - 用户 → 角色名称查询
 * - 用户 → 权限标识（按钮权限 perms）查询
 *
 * RBAC 权限模型结构：
 * ------------------------------------------------------------
 * 用户表 dynamicmenu_sys_user
 * 用户角色关联表 dynamicmenu_sys_user_role
 * 角色表 dynamicmenu_sys_role
 * 角色菜单关联表 dynamicmenu_sys_role_menu
 * 菜单/权限表 dynamicmenu_sys_menu
 *
 * 用户权限计算路径：
 * ------------------------------------------------------------
 * user.id
 * → user_role.role_id
 * → role_menu.menu_id
 * → menu.perms（按钮权限）
 *
 * 说明：
 * - 非按钮类菜单（目录、菜单）不参与权限点（perms）产出
 * - listPermissionsByUserId 只返回非空 perms
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

        // ============================================================
        // 1. 用户基本信息查询（用于登录、加载 UserDetails）
        // ============================================================

        /**
         * 根据用户名查询用户（用于登录表单提交验证）
         *
         * 使用场景：
         * ------------------------------------------------------------
         * - AuthController.login()
         * - LoginUserDetailsService.loadUserByUsername()
         *
         * @param username 登录用户名
         * @return User 实体对象（不包含角色、权限）
         */
        @Select("""
                        SELECT id, username, password, nickname, avatar, status, create_time
                        FROM dynamicmenu_sys_user
                        WHERE username = #{username}
                        LIMIT 1
                        """)
        User selectByUsername(@Param("username") String username);

        // ============================================================
        // 2. 查询用户拥有的角色编码列表
        // ============================================================

        /**
         * 查询某用户拥有的角色编码列表
         *
         * 作用场景：
         * ------------------------------------------------------------
         * - Spring Security / JWT 认证授权
         * - @PreAuthorize("hasRole('ADMIN')") 中需要 ROLE_XXX
         *
         * 注意：
         * - role_code 必须以 "ROLE_" 开头才能被 Spring Security 识别
         *
         * @param userId 用户 ID
         * @return 角色编码列表，如 ["ROLE_ADMIN", "ROLE_USER"]
         */
        @Select("""
                        SELECT DISTINCT r.role_code
                        FROM dynamicmenu_sys_role r
                        INNER JOIN dynamicmenu_sys_user_role ur ON r.id = ur.role_id
                        WHERE ur.user_id = #{userId}
                        """)
        List<String> listRoleCodesByUserId(@Param("userId") Long userId);

        // ============================================================
        // 3. 查询用户拥有的角色名称列表（如 管理员、运营、客服）
        // ============================================================

        /**
         * 查询某用户拥有的角色名称列表
         *
         * 使用场景：
         * ------------------------------------------------------------
         * - 后台用户管理列表展示
         * - 前端“个人中心”展示角色名称
         *
         * @param userId 用户ID
         * @return 角色名称列表，如 ["管理员", "客服"]
         */
        @Select("""
                        SELECT DISTINCT r.role_name
                        FROM dynamicmenu_sys_role r
                        INNER JOIN dynamicmenu_sys_user_role ur ON r.id = ur.role_id
                        WHERE ur.user_id = #{userId}
                        """)
        List<String> listRoleNamesByUserId(@Param("userId") Long userId);

        // ============================================================
        // 4. 查询用户拥有的权限标识 perms（按钮权限）
        // ============================================================

        /**
         * 查询用户拥有的权限标识集合（按钮级权限）
         *
         * 使用场景：
         * ------------------------------------------------------------
         * - 前端：控制按钮是否显示（v-if="hasPerm('sys:user:add')"）
         * - 后端：@PreAuthorize("hasAuthority('sys:user:list')")
         *
         * 权限来源：
         * ------------------------------------------------------------
         * user.id
         * → user_role.role_id
         * → role_menu.menu_id
         * → menu.perms（按钮权限）
         *
         * 特点：
         * ------------------------------------------------------------
         * - perms 字段只在 menu_type=3（按钮）时有值
         * - 自动 DISTINCT 去重
         * - 过滤空字符串 perms
         *
         * @param userId 用户ID
         * @return 权限编码列表，如 ["sys:user:list", "sys:role:assign"]
         */
        @Select("""
                        SELECT DISTINCT m.perms
                        FROM dynamicmenu_sys_menu m
                        INNER JOIN dynamicmenu_sys_role_menu rm ON m.id = rm.menu_id
                        INNER JOIN dynamicmenu_sys_role r      ON rm.role_id = r.id
                        INNER JOIN dynamicmenu_sys_user_role ur ON ur.role_id = r.id
                        WHERE ur.user_id = #{userId}
                          AND m.perms IS NOT NULL
                          AND m.perms <> ''
                        """)
        List<String> listPermissionsByUserId(@Param("userId") Long userId);
}
