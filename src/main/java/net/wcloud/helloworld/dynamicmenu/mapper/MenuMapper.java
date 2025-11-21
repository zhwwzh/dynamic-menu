package net.wcloud.helloworld.dynamicmenu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.wcloud.helloworld.dynamicmenu.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * MenuMapper（菜单资源数据访问层）
 *
 * 该 Mapper 负责查询 dynamicmenu_sys_menu 角色/用户可访问的菜单记录。
 * 主要用于 RBAC 权限模型中的：
 * - 角色 → 菜单
 * - 用户 → 角色 → 菜单
 *
 * 底层表关系（用于理解 SQL）：
 * ------------------------------------------------------------
 * 用户表： dynamicmenu_sys_user (user)
 * 用户角色关联表： dynamicmenu_sys_user_role (user_role)
 * 角色表： dynamicmenu_sys_role (role)
 * 角色菜单关联表： dynamicmenu_sys_role_menu (role_menu)
 * 菜单表： dynamicmenu_sys_menu (menu)
 *
 * 核心查询目标：
 * ------------------------------------------------------------
 * - 一个用户可以有多个角色
 * - 一个角色可以拥有多个菜单
 * - 需要对重复菜单做 DISTINCT 去重
 *
 * 注意：
 * - 返回结果为“平铺列表”，不会构建树，需要在 Service 层组装成树形结构
 */
@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

    // ============================================================
    // 1. 根据角色编码查询菜单
    // ============================================================

    /**
     * 根据角色编码查询该角色已授权的菜单列表。
     *
     * 使用场景：
     * - “角色授权菜单”页面回显角色已有菜单
     *
     * 逻辑说明：
     * ------------------------------------------------------------
     * role_code → role.id → role_menu → menu
     *
     * 限制：
     * - 仅查询 status = 1（启用状态）的菜单
     *
     * @param roleCode 角色编码（如 ROLE_ADMIN）
     * @return 菜单列表（未构建树）
     */
    @Select("""
            SELECT m.*
            FROM dynamicmenu_sys_menu m
            INNER JOIN dynamicmenu_sys_role_menu rm
                ON m.id = rm.menu_id
            INNER JOIN dynamicmenu_sys_role r
                ON rm.role_id = r.id
            WHERE r.role_code = #{roleCode}
              AND m.status = 1
            ORDER BY m.sort_order ASC
            """)
    List<Menu> selectMenusByRoleCode(@Param("roleCode") String roleCode);

    // ============================================================
    // 2. 根据用户ID查询菜单（多角色合并 + 去重）
    // ============================================================

    /**
     * 查询用户拥有的菜单列表（根据 userId）。
     *
     * 使用场景：
     * - 用户登录后，需要加载动态菜单（前端侧边栏）
     *
     * 逻辑说明：
     * ------------------------------------------------------------
     * user.id
     * → user_role.role_id
     * → role_menu.menu_id
     * → menu
     *
     * 特点：
     * - DISTINCT 去重（因为用户可能有多个角色）
     * - 仅查询 status = 1 的菜单
     *
     * @param userId 用户ID
     * @return 用户所有菜单（未构建树）
     */
    @Select("""
            SELECT DISTINCT m.*
            FROM dynamicmenu_sys_menu m
            INNER JOIN dynamicmenu_sys_role_menu rm
                ON m.id = rm.menu_id
            INNER JOIN dynamicmenu_sys_role r
                ON rm.role_id = r.id
            INNER JOIN dynamicmenu_sys_user_role ur
                ON ur.role_id = r.id
            WHERE ur.user_id = #{userId}
              AND m.status = 1
            ORDER BY m.sort_order ASC
            """)
    List<Menu> selectMenusByUserId(@Param("userId") Long userId);

    // ============================================================
    // 3. 另一版用户菜单查询（SQL 修复版）
    // ============================================================

    /**
     * 与上一个 selectMenusByUserId 逻辑相同，但 SQL 更简化。
     *
     * ⚠️ 你原代码中 "INNERJOIN" 是拼写错误，我已修复为 "INNER JOIN"
     *
     * @param userId 用户ID
     * @return 用户菜单（未构建树）
     */
    @Select("""
            SELECT DISTINCT m.*
            FROM dynamicmenu_sys_menu m
            INNER JOIN dynamicmenu_sys_role_menu rm
                ON m.id = rm.menu_id
            INNER JOIN dynamicmenu_sys_user_role ur
                ON ur.role_id = rm.role_id
            WHERE ur.user_id = #{userId}
            ORDER BY m.sort_order ASC
            """)
    List<Menu> listMenusByUserId(@Param("userId") Long userId);
}
