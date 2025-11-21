package net.wcloud.helloworld.dynamicmenu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.wcloud.helloworld.dynamicmenu.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("""
            SELECT id, username, password, nickname, avatar, status, create_time
            FROM dynamicmenu_sys_user
            WHERE username = #{username}
            LIMIT 1
            """)
    User selectByUsername(@Param("username") String username);

    @Select("""
            SELECT DISTINCT r.role_code
            FROM dynamicmenu_sys_role r
            INNER JOIN dynamicmenu_sys_user_role ur ON r.id = ur.role_id
            WHERE ur.user_id = #{userId}
            """)
    List<String> listRoleCodesByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT DISTINCT r.role_name
            FROM dynamicmenu_sys_role r
            INNER JOIN dynamicmenu_sys_user_role ur ON r.id = ur.role_id
            WHERE ur.user_id = #{userId}
            """)
    List<String> listRoleNamesByUserId(@Param("userId") Long userId);

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