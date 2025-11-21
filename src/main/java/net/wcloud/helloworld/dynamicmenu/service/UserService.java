package net.wcloud.helloworld.dynamicmenu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.wcloud.helloworld.dynamicmenu.entity.User;
import net.wcloud.helloworld.dynamicmenu.vo.UserVO;

import java.util.List;

public interface UserService extends IService<User> {

    /**
     * 根据用户名查询用户（给登录与安全模块使用）
     */
    User getByUsername(String username);

    /**
     * 根据用户ID获取角色编码列表，如：["ROLE_ADMIN", "ROLE_USER"]
     */
    List<String> listRoleCodesByUserId(Long userId);

    /**
     * 根据用户ID获取权限标识集合，如：["sys:user:list"]
     */
    List<String> listPermissionsByUserId(Long userId);

    /**
     * 获取用户详情（含角色 / 权限 / 菜单树），一般用在“个人中心 / 当前用户信息”接口
     */
    UserVO getUserDetail(Long userId);

    /**
     * 查询用户列表，并补充角色编码、角色名称、权限列表等信息
     */
    List<UserVO> listUsersWithDetail();
}
