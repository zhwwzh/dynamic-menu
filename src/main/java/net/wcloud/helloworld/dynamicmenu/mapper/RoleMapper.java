package net.wcloud.helloworld.dynamicmenu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.wcloud.helloworld.dynamicmenu.entity.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * RoleMapper（角色表 Mapper）
 *
 * 对应表： dynamicmenu_sys_role
 *
 * RBAC 权限模型中：
 * ------------------------------------------------------------
 * - 角色（role）是权限的中间层
 * - 用户 → 用户角色（user_role）→ 角色
 * - 角色 → 角色菜单（role_menu）→ 菜单（Menu）
 *
 * 该 Mapper 主要提供：
 * ------------------------------------------------------------
 * 1. 根据角色编码查角色（用于登录后的权限加载）
 * 2. 根据 userId 查询用户的角色列表（多角色）
 * 3. 查询角色已绑定的菜单 ID（菜单授权页面回显）
 * 4. 删除/插入角色-菜单关联（角色授权时使用）
 *
 * 注意：
 * ------------------------------------------------------------
 * - 所有返回的 Role/ID 均为“平铺结构”，不包含菜单，需要另行查询
 * - Mapper 层不负责业务逻辑，如合并、构建树、去重等
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

        // ============================================================
        // 1. 根据 RoleCode 查询角色
        // ============================================================

        /**
         * 根据角色编码查询角色。
         *
         * 作用场景：
         * ------------------------------------------------------------
         * - 登录时，当用户的角色编码保存在 token 时，用于数据库校验
         * - 权限变更后，根据编码查角色是否存在
         *
         * @param roleCode 角色编码（如 ROLE_ADMIN）
         * @return 匹配的角色对象（role），不存在返回 null
         */
        @Select("""
                        SELECT
                            id,
                            role_code,
                            role_name,
                            status,
                            create_time,
                            update_time
                        FROM dynamicmenu_sys_role
                        WHERE role_code = #{roleCode}
                        LIMIT 1
                        """)
        Role selectByRoleCode(@Param("roleCode") String roleCode);

        // ============================================================
        // 2. 根据用户 ID 查询角色列表（多角色合并）
        // ============================================================

        /**
         * 根据用户 ID 查询该用户绑定的所有角色（多角色情况）
         *
         * 表关系：
         * ------------------------------------------------------------
         * user.id → user_role.role_id → role.id → role
         *
         * 使用场景：
         * ------------------------------------------------------------
         * - 登录时加载权限
         * - 用户管理页面“查看用户角色”
         *
         * @param userId 用户ID
         * @return 用户的角色列表（可能多个）
         */
        @Select("""
                        SELECT DISTINCT
                            r.id,
                            r.role_code,
                            r.role_name,
                            r.status,
                            r.create_time,
                            r.update_time
                        FROM dynamicmenu_sys_role r
                        INNER JOIN dynamicmenu_sys_user_role ur
                            ON r.id = ur.role_id
                        WHERE ur.user_id = #{userId}
                        """)
        List<Role> listByUserId(@Param("userId") Long userId);

        // ============================================================
        // 3. 查询角色已绑定的菜单 ID 列表（角色授权页面回显）
        // ============================================================

        /**
         * 查询某角色已绑定的菜单 ID 列表。
         *
         * 使用场景：
         * ------------------------------------------------------------
         * - 角色授权菜单页面（勾选树基本数据）
         * - 编辑角色时需要回显菜单范围
         *
         * @param roleId 角色 ID
         * @return 菜单 ID 列表
         */
        @Select("""
                        SELECT menu_id
                        FROM dynamicmenu_sys_role_menu
                        WHERE role_id = #{roleId}
                        """)
        List<Long> listMenuIdsByRoleId(@Param("roleId") Long roleId);

        // ============================================================
        // 4. 删除角色所有菜单关联（授权前清除旧记录）
        // ============================================================

        /**
         * 删除角色的所有菜单绑定记录。
         *
         * 作用场景：
         * ------------------------------------------------------------
         * - 角色重新授权菜单（先删旧，再插新）
         *
         * @param roleId 角色 ID
         * @return 删除记录数
         */
        @Delete("""
                        DELETE FROM dynamicmenu_sys_role_menu
                        WHERE role_id = #{roleId}
                        """)
        int deleteRoleMenusByRoleId(@Param("roleId") Long roleId);

        // ============================================================
        // 5. 批量插入角色 - 菜单关联（授权新菜单）
        // ============================================================

        /**
         * 批量插入角色与菜单的关联关系。
         *
         * 使用 MyBatis Script + foreach 做批量 insert。
         *
         * 使用场景：
         * ------------------------------------------------------------
         * - 角色授权保存菜单时
         *
         * 注意：
         * ------------------------------------------------------------
         * - 如果 menuIds 为空，SQL 不会执行
         * - 必须先执行 deleteRoleMenusByRoleId 清除旧数据
         *
         * @param roleId  角色 ID
         * @param menuIds 菜单 ID 列表
         * @return 插入行数
         */
        @Insert("""
                        <script>
                        INSERT INTO dynamicmenu_sys_role_menu(role_id, menu_id)
                        VALUES
                        <foreach collection="menuIds" item="menuId" separator=",">
                            (#{roleId}, #{menuId})
                        </foreach>
                        </script>
                        """)
        int insertRoleMenus(@Param("roleId") Long roleId,
                        @Param("menuIds") List<Long> menuIds);
}
