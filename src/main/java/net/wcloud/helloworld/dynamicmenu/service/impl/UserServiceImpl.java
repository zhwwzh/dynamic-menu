package net.wcloud.helloworld.dynamicmenu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.wcloud.helloworld.dynamicmenu.convert.UserConvert;
import net.wcloud.helloworld.dynamicmenu.entity.User;
import net.wcloud.helloworld.dynamicmenu.mapper.UserMapper;
import net.wcloud.helloworld.dynamicmenu.service.MenuService;
import net.wcloud.helloworld.dynamicmenu.service.UserService;
import net.wcloud.helloworld.dynamicmenu.vo.MenuVO;
import net.wcloud.helloworld.dynamicmenu.vo.UserVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户业务实现类（UserServiceImpl）
 *
 * 功能职责：
 * ----------------------------------------------------------
 * 1. 根据用户名查询用户（登录使用）
 * 2. 查询角色编码 / 角色名称 / 权限标识
 * 3. 查询单个用户详情（基础 + 角色 + 权限 + 菜单树）
 * 4. 查询用户列表（附带角色/权限信息）
 *
 * 技术说明：
 * ----------------------------------------------------------
 * - 继承 MyBatis-Plus ServiceImpl：自动提供基础 CRUD
 * - 配合 UserMapper 执行自定义 SQL
 * - 配合 MenuService 构建用户菜单树
 *
 * 日志记录说明：
 * ----------------------------------------------------------
 * - info : 业务入口／关键输出
 * - debug : 细节过程数据（可在 yml 中调高 debug 查看）
 * - warn : 异常参数／空数据等非正常流程
 * - error : 真实异常
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final MenuService menuService;

    /**
     * 根据用户名查询用户（用于登录）
     */
    @Override
    public User getByUsername(String username) {
        log.info("[getByUsername] 开始查询用户, username={}", username);

        if (!StringUtils.hasText(username)) {
            log.warn("[getByUsername] username 为空，直接返回 null");
            return null;
        }

        User user = userMapper.selectByUsername(username);
        if (user == null) {
            log.warn("[getByUsername] 未查询到用户, username={}", username);
        } else {
            log.debug("[getByUsername] 查询到用户 id={}, username={}", user.getId(), user.getUsername());
        }

        return user;
    }

    /**
     * 查询用户的角色编码列表
     */
    @Override
    public List<String> listRoleCodesByUserId(Long userId) {
        log.info("[listRoleCodesByUserId] 查询角色编码列表, userId={}", userId);

        if (userId == null) {
            log.warn("[listRoleCodesByUserId] userId 为空，返回空列表");
            return Collections.emptyList();
        }

        List<String> roleCodes = userMapper.listRoleCodesByUserId(userId);
        log.debug("[listRoleCodesByUserId] 查询到角色编码数量={}, userId={}",
                (roleCodes == null ? 0 : roleCodes.size()), userId);

        return roleCodes;
    }

    /**
     * 查询用户的权限标识列表
     */
    @Override
    public List<String> listPermissionsByUserId(Long userId) {
        log.info("[listPermissionsByUserId] 查询权限列表, userId={}", userId);

        if (userId == null) {
            log.warn("[listPermissionsByUserId] userId 为空，返回空列表");
            return Collections.emptyList();
        }

        List<String> permissions = userMapper.listPermissionsByUserId(userId);
        log.debug("[listPermissionsByUserId] 查询到权限标识数量={}, userId={}",
                (permissions == null ? 0 : permissions.size()), userId);

        return permissions;
    }

    /**
     * 查询单个用户详情：基础信息 + 角色 + 权限 + 菜单树
     */
    @Override
    public UserVO getUserDetail(Long userId) {
        log.info("[getUserDetail] 查询用户详情开始, userId={}", userId);

        if (userId == null) {
            log.warn("[getUserDetail] userId 为空，返回 null");
            return null;
        }

        User user = this.getById(userId);
        if (user == null) {
            log.warn("[getUserDetail] 未找到用户记录, userId={}", userId);
            return null;
        }

        UserVO vo = buildUserVO(user);
        log.info("[getUserDetail] 用户详情构建完成, userId={}", userId);

        return vo;
    }

    /**
     * 查询用户列表（包含角色信息 + 权限信息）
     *
     * 注意：
     * - 不建议在列表中加载“菜单树”，性能非常差（N 次大查询）
     */
    @Override
    public List<UserVO> listUsersWithDetail() {
        log.info("[listUsersWithDetail] 查询全部用户列表及详情");

        List<User> users = this.list();
        if (users == null || users.isEmpty()) {
            log.warn("[listUsersWithDetail] 用户表为空，返回空列表");
            return Collections.emptyList();
        }

        log.debug("[listUsersWithDetail] 用户数量={}", users.size());

        List<UserVO> voList = users.stream()
                .map(this::buildUserVO)
                .collect(Collectors.toList());

        log.info("[listUsersWithDetail] 构建用户详情完成, size={}", voList.size());
        return voList;
    }

    /**
     * 将 User 实体封装为 UserVO（附带 角色编码 + 角色名称 + 权限 + 菜单树）
     *
     * 注意：
     * - 菜单树查询非常重，可能包含几百个节点，尽量避免在列表接口调用
     */
    private UserVO buildUserVO(User user) {
        Long userId = user.getId();
        log.debug("[buildUserVO] 开始构建 UserVO, userId={}", userId);

        // 1. 基础字段映射
        UserVO vo = UserConvert.INSTANCE.toVO(user);

        // 2. 角色编码
        List<String> roleCodes = userMapper.listRoleCodesByUserId(userId);
        vo.setRoleCodes(roleCodes);
        log.debug("[buildUserVO] 已设置角色编码数量={}, userId={}",
                (roleCodes == null ? 0 : roleCodes.size()), userId);

        // 3. 角色名称
        List<String> roleNames = userMapper.listRoleNamesByUserId(userId);
        vo.setRoleNames(roleNames);
        log.debug("[buildUserVO] 已设置角色名称数量={}, userId={}",
                (roleNames == null ? 0 : roleNames.size()), userId);

        // 4. 权限标识
        List<String> perms = userMapper.listPermissionsByUserId(userId);
        vo.setPermissions(perms);
        log.debug("[buildUserVO] 已设置权限标识数量={}, userId={}",
                (perms == null ? 0 : perms.size()), userId);

        // 5. 菜单树（如果在列表调用，则会非常耗时）
        List<MenuVO> menus = menuService.getMenuTreeByUserId(userId);
        vo.setMenus(menus);
        log.debug("[buildUserVO] 已设置菜单树，根节点数量={}, userId={}",
                (menus == null ? 0 : menus.size()), userId);

        log.info("[buildUserVO] UserVO 构建完成, userId={}", userId);
        return vo;
    }
}
