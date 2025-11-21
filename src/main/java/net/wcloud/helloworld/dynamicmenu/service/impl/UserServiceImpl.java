package net.wcloud.helloworld.dynamicmenu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import net.wcloud.helloworld.dynamicmenu.convert.UserConvert;
import net.wcloud.helloworld.dynamicmenu.entity.User;
import net.wcloud.helloworld.dynamicmenu.mapper.UserMapper;
import net.wcloud.helloworld.dynamicmenu.vo.UserVO;
import net.wcloud.helloworld.dynamicmenu.vo.MenuVO;
import net.wcloud.helloworld.dynamicmenu.service.MenuService;
import net.wcloud.helloworld.dynamicmenu.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户业务实现类
 *
 * 说明：
 * - 继承 MyBatis-Plus ServiceImpl，带基础 CRUD（list / getById / save 等）
 * - 实现 UserService 中声明的所有方法
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final MenuService menuService; // 如果暂时不用 menus，可以先删掉这个注入

    /**
     * 根据用户名查询用户（用于登录）
     */
    @Override
    public User getByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        // 对应 UserMapper.selectByUsername
        return userMapper.selectByUsername(username);
    }

    /**
     * 查询用户的角色编码列表
     */
    @Override
    public List<String> listRoleCodesByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return userMapper.listRoleCodesByUserId(userId);
    }

    /**
     * 查询用户的权限标识列表
     */
    @Override
    public List<String> listPermissionsByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return userMapper.listPermissionsByUserId(userId);
    }

    /**
     * 查询单个用户详情：基础信息 + 角色 + 权限
     */
    @Override
    public UserVO getUserDetail(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = this.getById(userId);
        if (user == null) {
            return null;
        }
        return buildUserVO(user);
    }

    /**
     * 查询用户列表，并补充角色编码、角色名称、权限列表
     */
    @Override
    public List<UserVO> listUsersWithDetail() {
        List<User> users = this.list();
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        return users.stream()
                .map(this::buildUserVO)
                .collect(Collectors.toList());
    }

    /**
     * 将 User 实体封装为带角色/权限等信息的 UserVO
     */
    private UserVO buildUserVO(User user) {
        // 1. 基础字段：id / username / nickname / avatar / status / createTime
        UserVO vo = UserConvert.INSTANCE.toVO(user);

        Long userId = user.getId();

        // 2. 角色编码列表
        List<String> roleCodes = userMapper.listRoleCodesByUserId(userId);
        vo.setRoleCodes(roleCodes);

        // 3. 角色名称列表（需要在 UserMapper 中提供 listRoleNamesByUserId）
        List<String> roleNames = userMapper.listRoleNamesByUserId(userId);
        vo.setRoleNames(roleNames);

        // 4. 权限标识列表
        List<String> perms = userMapper.listPermissionsByUserId(userId);
        vo.setPermissions(perms);

        // 5. 菜单树（非常重，一般只在当前登录用户信息接口中返回，不在列表中查）
        // 如果你确实想在这里也查，可以取消下面注释（注意性能）：
        List<MenuVO> menus = menuService.getMenuTreeByUserId(userId);
        vo.setMenus(menus);

        return vo;
    }
}