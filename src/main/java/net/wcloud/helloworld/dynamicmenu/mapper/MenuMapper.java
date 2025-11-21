package net.wcloud.helloworld.dynamicmenu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.wcloud.helloworld.dynamicmenu.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单 Mapper
 *
 * 说明：
 * - 所有菜单查询均基于 MyBatis 注解 SQL
 * - 支持用户菜单树、多角色合并、系统菜单树等业务场景
 */
@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

    /**
     * 查询某用户拥有的菜单（多角色合并 + 去重）
     *
     * 逻辑：
     * 1. 用户 → 用户角色 user_role
     * 2. 角色 → 角色菜单 role_menu
     * 3. 菜单 → sys_menu
     * 4. DISTINCT 去重（用户可能有多个角色）
     *
     * @param userId 用户ID
     * @return 用户拥有的菜单列表（未构建树，需由 service 组装）
     */
    @Select("""
            SELECT DISTINCT m.*
            FROM dynamicmenu_sys_menu m
            INNER JOIN dynamicmenu_sys_role_menu rm ON m.id = rm.menu_id
            INNER JOIN dynamicmenu_sys_user_role ur ON ur.role_id = rm.role_id
            WHERE ur.user_id = #{userId}
            ORDER BY m.sort_order ASC, m.id ASC
            """)
    List<Menu> listMenusByUserId(@Param("userId") Long userId);

    /**
     * 查询所有菜单（用于后台菜单管理）
     *
     * @return 全量菜单（未构建树）
     */
    @Select("""
            SELECT *
            FROM dynamicmenu_sys_menu
            ORDER BY sort_order ASC, id ASC
            """)
    List<Menu> selectAllMenus();

    /**
     * 查询状态启用的所有菜单（可用于角色管理 → 菜单授权）
     */
    @Select("""
            SELECT *
            FROM dynamicmenu_sys_menu
            WHERE status = 1
            ORDER BY sort_order ASC, id ASC
            """)
    List<Menu> selectEnableMenus();

    /**
     * 按主键查询菜单（虽然 BaseMapper 已有，但这里作为显式声明，用于复杂场景）
     */
    @Select("""
            SELECT *
            FROM dynamicmenu_sys_menu
            WHERE id = #{id}
            LIMIT 1
            """)
    Menu selectMenuById(@Param("id") Long id);

    /**
     * 查询某角色下的菜单（角色菜单绑定页面用）
     */
    @Select("""
            SELECT m.*
            FROM dynamicmenu_sys_menu m
            INNER JOIN dynamicmenu_sys_role_menu rm ON m.id = rm.menu_id
            WHERE rm.role_id = #{roleId}
            ORDER BY m.sort_order ASC, m.id ASC
            """)
    List<Menu> listMenusByRoleId(@Param("roleId") Long roleId);
}
