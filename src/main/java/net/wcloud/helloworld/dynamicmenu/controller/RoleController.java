package net.wcloud.helloworld.dynamicmenu.controller;

import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.wcloud.helloworld.dynamicmenu.common.Result;
import net.wcloud.helloworld.dynamicmenu.entity.Role;
import net.wcloud.helloworld.dynamicmenu.mapper.RoleMapper;
import net.wcloud.helloworld.dynamicmenu.service.MenuService;
import net.wcloud.helloworld.dynamicmenu.vo.MenuVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理接口（RoleController）
 *
 * 功能说明：
 * ------------------------------------------------------------
 * 1. 角色 CRUD（列表 / 详情 / 新增 / 修改 / 删除）
 * 2. 给角色分配菜单（role_menu 关联）
 * 3. 查询角色已绑定的菜单 ID（授权页面回显）
 * 4. 查询系统全部菜单树（给前端展示）
 *
 * 数据关系：
 * ------------------------------------------------------------
 * 角色与菜单：多对多（role_menu）
 * 角色与用户：多对多（user_role）
 *
 * 日志策略：
 * ------------------------------------------------------------
 * - info : 记录角色增删改 / 授权 等关键审计行为
 * - warn : 角色不存在 / 删除失败 / 空菜单等
 * - debug : 参数与返回数据数量
 */
@Slf4j
@RestController
@RequestMapping("/api/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleMapper roleMapper;
    private final MenuService menuService;

    /**
     * 角色列表
     */
    @GetMapping("/list")
    public Result<List<Role>> list() {

        log.info("[RoleController] 请求角色列表");

        List<Role> roles = roleMapper.selectList(null);

        log.debug("[RoleController] 角色数量={}", roles == null ? 0 : roles.size());

        return Result.success(roles);
    }

    /**
     * 查询角色详情
     */
    @GetMapping("/{id}")
    public Result<Role> detail(@PathVariable Long id) {

        log.info("[RoleController] 查询角色详情, roleId={}", id);

        Role role = roleMapper.selectById(id);

        if (role == null) {
            log.warn("[RoleController] 角色不存在, roleId={}", id);
            return Result.fail(404, "角色不存在");
        }

        log.debug("[RoleController] 角色详情加载成功, roleId={}, roleName={}", id, role.getRoleName());

        return Result.success(role);
    }

    /**
     * 新增角色
     */
    @PostMapping
    public Result<Boolean> create(@Valid @RequestBody Role role) {

        log.info("[RoleController] 新增角色, roleName={}", role.getRoleName());

        int rows = roleMapper.insert(role);

        if (rows > 0) {
            log.info("[RoleController] 新增角色成功, roleId={}, roleName={}", role.getId(), role.getRoleName());
            return Result.success(true);
        } else {
            log.error("[RoleController] 新增角色失败, roleName={}", role.getRoleName());
            return Result.fail(500, "新增角色失败");
        }
    }

    /**
     * 修改角色
     */
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id,
            @Valid @RequestBody Role role) {

        log.info("[RoleController] 修改角色, roleId={}, roleName={}", id, role.getRoleName());

        role.setId(id);
        int rows = roleMapper.updateById(role);

        if (rows > 0) {
            log.info("[RoleController] 修改角色成功, roleId={}", id);
            return Result.success(true);
        } else {
            log.warn("[RoleController] 修改角色失败, roleId={}", id);
            return Result.fail(500, "修改角色失败");
        }
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {

        log.info("[RoleController] 删除角色, roleId={}", id);

        int rows = roleMapper.deleteById(id);

        if (rows > 0) {
            log.info("[RoleController] 删除角色成功, roleId={}", id);
            return Result.success(true);
        } else {
            log.warn("[RoleController] 删除角色失败, roleId={}", id);
            return Result.fail(500, "删除角色失败");
        }
    }

    /**
     * 查询角色已分配的菜单 ID 列表（授权页面回显）
     */
    @GetMapping("/{id}/menus")
    public Result<List<Long>> getRoleMenuIds(@PathVariable Long id) {

        log.info("[RoleController] 查询角色拥有的菜单 ID, roleId={}", id);

        List<Long> menuIds = roleMapper.listMenuIdsByRoleId(id);

        log.debug("[RoleController] 已分配菜单数量={}, roleId={}",
                menuIds == null ? 0 : menuIds.size(), id);

        return Result.success(menuIds);
    }

    /**
     * 给角色分配菜单（保存角色-菜单权限）
     */
    @PostMapping("/{id}/menus")
    public Result<Boolean> assignMenus(@PathVariable Long id,
            @Valid @RequestBody RoleAssignMenuDTO req) {

        log.info("[RoleController] 角色授权菜单, roleId={}, menuCount={}",
                id, req.getMenuIds() == null ? 0 : req.getMenuIds().size());

        // 1. 删除旧权限
        roleMapper.deleteRoleMenusByRoleId(id);
        log.debug("[RoleController] 已删除旧关联关系, roleId={}", id);

        // 2. 插入新权限
        if (req.getMenuIds() != null && !req.getMenuIds().isEmpty()) {
            roleMapper.insertRoleMenus(id, req.getMenuIds());
            log.info("[RoleController] 新菜单授权成功, roleId={}, newMenuCount={}",
                    id, req.getMenuIds().size());
        } else {
            log.warn("[RoleController] 新授权菜单为空, roleId={}", id);
        }

        return Result.success(true);
    }

    /**
     * 查询系统全部菜单树（给角色授权时使用）
     */
    @GetMapping("/menu/tree")
    public Result<List<MenuVO>> allMenuTreeForRole() {

        log.info("[RoleController] 查询系统全量菜单树");

        List<MenuVO> tree = menuService.listAllMenuTree();

        log.debug("[RoleController] 全量菜单树根节点数量={}",
                tree == null ? 0 : tree.size());

        return Result.success(tree);
    }

    /**
     * 接收前端“角色分配菜单”请求的 DTO
     */
    @Data
    public static class RoleAssignMenuDTO {
        private List<Long> menuIds;
    }
}
