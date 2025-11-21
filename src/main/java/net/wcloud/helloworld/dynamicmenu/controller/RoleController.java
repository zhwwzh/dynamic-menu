package net.wcloud.helloworld.dynamicmenu.controller;

import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.wcloud.helloworld.dynamicmenu.common.Result;
import net.wcloud.helloworld.dynamicmenu.entity.Role;
import net.wcloud.helloworld.dynamicmenu.mapper.RoleMapper;
import net.wcloud.helloworld.dynamicmenu.service.MenuService;
import net.wcloud.helloworld.dynamicmenu.vo.MenuVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        List<Role> roles = roleMapper.selectList(null);
        return Result.success(roles);
    }

    /**
     * 角色详情
     */
    @GetMapping("/{id}")
    public Result<Role> detail(@PathVariable Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            return Result.fail(404, "角色不存在");
        }
        return Result.success(role);
    }

    /**
     * 新增角色
     */
    @PostMapping
    public Result<Boolean> create(@Valid @RequestBody Role role) {
        int rows = roleMapper.insert(role);
        return rows > 0 ? Result.success(true) : Result.fail(500, "新增角色失败");
    }

    /**
     * 修改角色
     */
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id,
            @Valid @RequestBody Role role) {
        role.setId(id);
        int rows = roleMapper.updateById(role);
        return rows > 0 ? Result.success(true) : Result.fail(500, "修改角色失败");
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        int rows = roleMapper.deleteById(id);
        // 你可以顺便删除 user_role / role_menu 关联，这里暂时没处理
        return rows > 0 ? Result.success(true) : Result.fail(500, "删除角色失败");
    }

    /**
     * 查询角色已分配的菜单 ID 列表（授权页面回显用）
     */
    @GetMapping("/{id}/menus")
    public Result<List<Long>> getRoleMenuIds(@PathVariable Long id) {
        List<Long> menuIds = roleMapper.listMenuIdsByRoleId(id);
        return Result.success(menuIds);
    }

    /**
     * 给角色分配菜单（保存角色-菜单权限）
     */
    @PostMapping("/{id}/menus")
    public Result<Boolean> assignMenus(@PathVariable Long id,
            @Valid @RequestBody RoleAssignMenuDTO req) {
        // 1. 先删旧关系
        roleMapper.deleteRoleMenusByRoleId(id);
        // 2. 再插入新关系
        if (req.getMenuIds() != null && !req.getMenuIds().isEmpty()) {
            roleMapper.insertRoleMenus(id, req.getMenuIds());
        }
        return Result.success(true);
    }

    /**
     * 可选：查询角色可用的菜单树（一般就是 allMenuTree）
     */
    @GetMapping("/menu/tree")
    public Result<List<MenuVO>> allMenuTreeForRole() {
        List<MenuVO> tree = menuService.listAllMenuTree();
        return Result.success(tree);
    }

    /**
     * 角色分配菜单 DTO
     */
    @Data
    public static class RoleAssignMenuDTO {
        private List<Long> menuIds;
    }
}
