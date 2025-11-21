package net.wcloud.helloworld.dynamicmenu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import net.wcloud.helloworld.dynamicmenu.convert.MenuConvert;
import net.wcloud.helloworld.dynamicmenu.entity.Menu;
import net.wcloud.helloworld.dynamicmenu.mapper.MenuMapper;
import net.wcloud.helloworld.dynamicmenu.vo.MenuVO;
import net.wcloud.helloworld.dynamicmenu.service.MenuService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单业务实现类
 *
 * 主要职责：
 * 1. 为【当前用户】构建可访问菜单树（多角色合并 + 去重）
 * 2. 为【后台菜单管理】提供系统全量菜单树
 * 3. 内部调用 getMenuTreeByUserId 作为 listMenuTreeByUserId 的别名
 */
@Service
@RequiredArgsConstructor
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    private final MenuMapper menuMapper;
    private final MenuConvert menuConvert;

    /**
     * 查询当前登录用户的菜单树（多角色合并 + 去重）
     *
     * 说明：
     * 1) 根据用户 → 角色 → 角色菜单 → 菜单，查询该用户所有有权访问的菜单
     * 2) 只保留状态启用(status = 1)的菜单
     * 3) 过滤掉按钮(menuType = 3)，只在树中展示【目录 + 菜单】
     * 4) 按 sortOrder 排序，并构建 parentId → children 的树
     */
    @Override
    public List<MenuVO> listMenuTreeByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        // ★ 这里依赖 MenuMapper.listMenusByUserId，请确保在 MenuMapper 中有对应 SQL
        List<Menu> menuList = menuMapper.listMenusByUserId(userId);
        if (CollectionUtils.isEmpty(menuList)) {
            return Collections.emptyList();
        }

        // 1. 只保留启用的目录/菜单（status = 1, menuType != 3）
        List<Menu> filtered = menuList.stream()
                .filter(m -> Objects.equals(m.getStatus(), 1))
                .filter(m -> m.getMenuType() == null || m.getMenuType() != 3)
                .sorted(Comparator.comparing(Menu::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());

        // 2. 实体 -> VO
        List<MenuVO> voList = menuConvert.toVOList(filtered);

        // 3. 构建树
        return buildMenuTree(voList);
    }

    /**
     * 查询系统所有菜单树（后台菜单管理使用）
     *
     * 说明：
     * 1) 一般用于“系统管理 → 菜单管理”页面，展示系统所有菜单
     * 2) 可按需选择是否包含按钮（这里示例默认也过滤掉按钮，可根据需要保留）
     * 3) 与用户无关，不做权限过滤
     */
    @Override
    public List<MenuVO> listAllMenuTree() {
        // 直接查全表，也可以使用自定义 SQL 按 sort_order 排序
        List<Menu> menuList = this.lambdaQuery()
                .orderByAsc(Menu::getSortOrder)
                .list();

        if (CollectionUtils.isEmpty(menuList)) {
            return Collections.emptyList();
        }

        // 1. 可根据需要是否过滤按钮，这里示例：管理页面一般也会看到按钮，所以不过滤
        // 如果你想在树中也隐藏按钮，可以和上面一样 filter menuType != 3
        List<Menu> filtered = menuList.stream()
                .sorted(Comparator.comparing(Menu::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());

        // 2. 转 VO
        List<MenuVO> voList = menuConvert.toVOList(filtered);

        // 3. 构建树
        return buildMenuTree(voList);
    }

    /**
     * 对外暴露的别名方法：
     * - 语义上强调“根据 userId 查询用户菜单树”
     * - 实现上直接复用 listMenuTreeByUserId
     */
    @Override
    public List<MenuVO> getMenuTreeByUserId(Long userId) {
        return listMenuTreeByUserId(userId);
    }

    // ===========================
    // 内部工具方法：构建菜单树
    // ===========================

    /**
     * 将平铺的 VO 列表构建为树形结构
     *
     * 要求：
     * - VO 中的 id / parentId 字段不能为空
     * - parentId = 0 或 null 时，视为根节点
     */
    private List<MenuVO> buildMenuTree(List<MenuVO> flatList) {
        if (CollectionUtils.isEmpty(flatList)) {
            return Collections.emptyList();
        }

        // 1. 先建一个 id -> MenuVO 的 Map，后面拼树用
        Map<Long, MenuVO> idMap = new LinkedHashMap<>();
        for (MenuVO vo : flatList) {
            vo.setChildren(new ArrayList<>()); // 确保 children 不为 null
            idMap.put(vo.getId(), vo);
        }

        // 2. 遍历所有节点，按 parentId 挂到对应的父节点下
        List<MenuVO> roots = new ArrayList<>();

        for (MenuVO vo : idMap.values()) {
            Long parentId = vo.getParentId();
            if (parentId == null || parentId == 0) {
                // 根节点
                roots.add(vo);
            } else {
                MenuVO parent = idMap.get(parentId);
                if (parent != null) {
                    parent.getChildren().add(vo);
                } else {
                    // 找不到父节点的情况，按根节点处理，避免丢数据
                    roots.add(vo);
                }
            }
        }

        // 3. 对每一层 children 再按 sortOrder 排一下（可选，保证前端显示顺序）
        roots.forEach(this::sortChildrenRecursively);

        return roots;
    }

    /**
     * 递归对子节点按照 sortOrder 排序
     */
    private void sortChildrenRecursively(MenuVO parent) {
        List<MenuVO> children = parent.getChildren();
        if (CollectionUtils.isEmpty(children)) {
            return;
        }

        children.sort(Comparator.comparing(MenuVO::getSortOrder, Comparator.nullsLast(Integer::compareTo)));

        for (MenuVO child : children) {
            sortChildrenRecursively(child);
        }
    }
}
