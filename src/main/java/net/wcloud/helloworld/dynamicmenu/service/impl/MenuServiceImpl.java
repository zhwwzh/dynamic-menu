package net.wcloud.helloworld.dynamicmenu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.wcloud.helloworld.dynamicmenu.convert.MenuConvert;
import net.wcloud.helloworld.dynamicmenu.entity.Menu;
import net.wcloud.helloworld.dynamicmenu.mapper.MenuMapper;
import net.wcloud.helloworld.dynamicmenu.service.MenuService;
import net.wcloud.helloworld.dynamicmenu.vo.MenuVO;
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
 *
 * 日志说明：
 * - info：关键业务行为，如查询入口、结果统计
 * - debug：中间过程，如过滤、树构建细节（默认可在 yml 里将该包的日志级别调为 debug 查看）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    /**
     * 菜单 Mapper，用于从数据库查询菜单数据
     */
    private final MenuMapper menuMapper;

    /**
     * 菜单实体与 VO 之间的转换组件
     * 一般使用 MapStruct 或手写 Convert 类实现
     */
    private final MenuConvert menuConvert;

    /**
     * 查询当前登录用户的菜单树（多角色合并 + 去重）
     *
     * 说明：
     * 1) 根据 用户 → 角色 → 角色菜单 → 菜单，查询该用户所有有权访问的菜单
     * 2) 只保留状态启用(status = 1)的菜单
     * 3) 过滤掉按钮(menuType = 3)，只在树中展示【目录 + 菜单】
     * 4) 按 sortOrder 排序，并构建 parentId → children 的树
     *
     * @param userId 用户 ID
     * @return 该用户可访问的菜单树（目录 + 菜单）
     */
    @Override
    public List<MenuVO> listMenuTreeByUserId(Long userId) {
        if (userId == null) {
            log.warn("[listMenuTreeByUserId] userId is null, return empty list");
            return Collections.emptyList();
        }

        // 这里依赖 MenuMapper.listMenusByUserId，请确保在 MenuMapper 中有对应 SQL
        log.info("[listMenuTreeByUserId] 查询用户菜单开始, userId={}", userId);
        List<Menu> menuList = menuMapper.listMenusByUserId(userId);

        if (CollectionUtils.isEmpty(menuList)) {
            log.info("[listMenuTreeByUserId] 用户无任何菜单记录, userId={}", userId);
            return Collections.emptyList();
        }
        log.debug("[listMenuTreeByUserId] 原始菜单数量={}, userId={}", menuList.size(), userId);

        // 1. 只保留启用的目录/菜单（status = 1, menuType != 3）
        List<Menu> filtered = menuList.stream()
                .filter(m -> {
                    boolean enabled = Objects.equals(m.getStatus(), 1);
                    if (!enabled) {
                        log.debug("[listMenuTreeByUserId] 过滤掉未启用菜单, menuId={}, status={}", m.getId(), m.getStatus());
                    }
                    return enabled;
                })
                .filter(m -> {
                    // menuType: 1=目录, 2=菜单, 3=按钮（示例约定，如有差异请按你的枚举调整）
                    boolean notButton = m.getMenuType() == null || m.getMenuType() != 3;
                    if (!notButton) {
                        log.debug("[listMenuTreeByUserId] 过滤掉按钮菜单, menuId={}, menuType={}", m.getId(), m.getMenuType());
                    }
                    return notButton;
                })
                .sorted(Comparator.comparing(Menu::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());

        log.info("[listMenuTreeByUserId] 过滤后菜单数量={}, userId={}", filtered.size(), userId);

        // 2. 实体 -> VO
        List<MenuVO> voList = menuConvert.toVOList(filtered);
        log.debug("[listMenuTreeByUserId] 转换为 VO 后数量={}, userId={}", voList.size(), userId);

        // 3. 构建树
        List<MenuVO> tree = buildMenuTree(voList);
        log.info("[listMenuTreeByUserId] 构建菜单树完成, 根节点数量={}, userId={}", tree.size(), userId);

        return tree;
    }

    /**
     * 查询系统所有菜单树（后台菜单管理使用）
     *
     * 说明：
     * 1) 一般用于“系统管理 → 菜单管理”页面，展示系统所有菜单
     * 2) 可按需选择是否包含按钮（这里示例默认不过滤按钮）
     * 3) 与用户无关，不做权限过滤
     *
     * @return 系统全量菜单树
     */
    @Override
    public List<MenuVO> listAllMenuTree() {
        log.info("[listAllMenuTree] 查询系统全量菜单树开始");

        // 直接查全表，也可以使用自定义 SQL 按 sort_order 排序
        List<Menu> menuList = this.lambdaQuery()
                .orderByAsc(Menu::getSortOrder)
                .list();

        if (CollectionUtils.isEmpty(menuList)) {
            log.warn("[listAllMenuTree] 系统当前无任何菜单数据");
            return Collections.emptyList();
        }

        log.info("[listAllMenuTree] 查询到菜单总数={}", menuList.size());

        // 1. 可根据需要是否过滤按钮，这里示例：菜单管理页面通常也会展示按钮，因此不做过滤
        List<Menu> filtered = menuList.stream()
                .sorted(Comparator.comparing(Menu::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());

        log.debug("[listAllMenuTree] 排序后菜单数量={}", filtered.size());

        // 2. 转 VO
        List<MenuVO> voList = menuConvert.toVOList(filtered);
        log.debug("[listAllMenuTree] 转换为 VO 后数量={}", voList.size());

        // 3. 构建树
        List<MenuVO> tree = buildMenuTree(voList);
        log.info("[listAllMenuTree] 构建系统菜单树完成, 根节点数量={}", tree.size());

        return tree;
    }

    /**
     * 对外暴露的别名方法：
     * - 语义上强调“根据 userId 查询用户菜单树”
     * - 实现上直接复用 listMenuTreeByUserId
     *
     * @param userId 用户 ID
     * @return 菜单树
     */
    @Override
    public List<MenuVO> getMenuTreeByUserId(Long userId) {
        log.debug("[getMenuTreeByUserId] 调用别名方法, userId={}", userId);
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
     *
     * @param flatList 平铺的菜单 VO 列表
     * @return 树形菜单列表（根节点集合）
     */
    private List<MenuVO> buildMenuTree(List<MenuVO> flatList) {
        if (CollectionUtils.isEmpty(flatList)) {
            log.warn("[buildMenuTree] flatList 为空, 直接返回空树");
            return Collections.emptyList();
        }

        log.debug("[buildMenuTree] 开始构建菜单树, 平铺节点数量={}", flatList.size());

        // 1. 先建一个 id -> MenuVO 的 Map，后面拼树用
        Map<Long, MenuVO> idMap = new LinkedHashMap<>();
        for (MenuVO vo : flatList) {
            if (vo.getId() == null) {
                log.warn("[buildMenuTree] 检测到 id 为空的菜单节点, 将忽略该节点: {}", vo);
                continue;
            }
            vo.setChildren(new ArrayList<>()); // 确保 children 不为 null
            idMap.put(vo.getId(), vo);
        }
        log.debug("[buildMenuTree] 构建 idMap 完成, 有效节点数量={}", idMap.size());

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
                    // 找不到父节点的情况，按根节点处理，避免数据完全丢失
                    log.warn("[buildMenuTree] 找不到父节点, 将该节点视为根节点处理, menuId={}, parentId={}",
                            vo.getId(), parentId);
                    roots.add(vo);
                }
            }
        }

        log.debug("[buildMenuTree] 初步构建树完成, 根节点数量={}", roots.size());

        // 3. 对每一层 children 再按 sortOrder 排一下（可选，保证前端显示顺序）
        roots.forEach(this::sortChildrenRecursively);

        log.debug("[buildMenuTree] 递归排序 children 完成");
        return roots;
    }

    /**
     * 递归对子节点按照 sortOrder 排序
     *
     * @param parent 当前节点
     */
    private void sortChildrenRecursively(MenuVO parent) {
        List<MenuVO> children = parent.getChildren();
        if (CollectionUtils.isEmpty(children)) {
            return;
        }

        children.sort(Comparator.comparing(MenuVO::getSortOrder, Comparator.nullsLast(Integer::compareTo)));
        log.debug("[sortChildrenRecursively] 已为节点 menuId={} 的 children 排序, 子节点数量={}",
                parent.getId(), children.size());

        for (MenuVO child : children) {
            sortChildrenRecursively(child);
        }
    }
}
