package net.wcloud.helloworld.dynamicmenu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.wcloud.helloworld.dynamicmenu.entity.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 角色 Mapper
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据角色编码查询角色
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

    /**
     * 根据用户ID查询角色列表
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

    /**
     * 查询角色已绑定的菜单ID
     */
    @Select("""
            SELECT menu_id
            FROM dynamicmenu_sys_role_menu
            WHERE role_id = #{roleId}
            """)
    List<Long> listMenuIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 删除角色所有菜单关联
     */
    @Delete("""
            DELETE FROM dynamicmenu_sys_role_menu
            WHERE role_id = #{roleId}
            """)
    int deleteRoleMenusByRoleId(@Param("roleId") Long roleId);

    /**
     * 批量插入角色-菜单关联
     *
     * MyBatis 注解支持 foreach 批量插入。
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
