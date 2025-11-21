package net.wcloud.helloworld.dynamicmenu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.wcloud.helloworld.dynamicmenu.entity.Menu;
import net.wcloud.helloworld.dynamicmenu.vo.MenuVO;

import java.util.List;

/**
 * 菜单服务接口（MenuService）
 *
 * 负责功能：
 * ------------------------------------------------------------
 * 1. 构建【用户可见的菜单树】（根据用户 → 角色 → 菜单，多角色合并去重）
 * 2. 构建【后台管理用的系统全量菜单树】（不考虑权限）
 * 3. 提供内部查询封装方法（getMenuTreeByUserId）
 *
 * 数据背景（核心逻辑）：
 * ------------------------------------------------------------
 * 动态菜单系统使用 RBAC 模型：
 *
 * 用户(User) ←→ 用户角色(UserRole)
 * 角色(Role) ←→ 角色菜单(RoleMenu)
 * 菜单(Menu)（menu_type=1目录、2菜单、3按钮）
 *
 * 动态菜单展示过程：
 * 用户登录 → 查询角色 → 查询角色所拥有的菜单 → 去重 → 构建树
 *
 * 菜单存储表结构：
 * - dynamicmenu_sys_menu : 存储目录、菜单、按钮三类资源
 * - 字段菜单说明：
 * id : 主键
 * parent_id : 上级目录 ID（根节点 parent_id = 0）
 * menu_type : 菜单类型（1=目录、2=菜单、3=按钮）
 * status : 菜单状态（1=启用，0=禁用）
 * sort_order : 排序号（树结构展示顺序）
 *
 * 返回对象：
 * ------------------------------------------------------------
 * 所有菜单树结果均返回 MenuVO（前端友好结构，可直接渲染）
 */
public interface MenuService extends IService<Menu> {

    /**
     * 查询当前登录用户的【可见菜单树】（多角色合并 + 菜单去重）
     *
     * 使用场景：
     * ------------------------------------------------------------
     * - 登录成功后，构建前端左侧菜单（侧边栏导航）
     * - “我的权限菜单”
     * - 刷新页面后重新加载当前用户的菜单
     *
     * 行为说明（逻辑步骤）：
     * ------------------------------------------------------------
     * 1. 根据 userId 查询用户所拥有的角色列表
     * 2. 根据角色查询角色绑定的菜单（目录 / 菜单 / 按钮）
     * 3. 多角色菜单合并并去重
     * 4. 过滤掉按钮 menu_type = 3（按钮权限不在菜单树中展示）
     * 5. 过滤掉禁用菜单 status != 1
     * 6. 按 sortOrder 排序
     * 7. 按 parentId 构建树形结构
     *
     * 注意事项：
     * ------------------------------------------------------------
     * - 返回值只包含“目录 + 菜单”，用于构建前端导航菜单
     * - 按钮权限返回值属于 Role → Permission，不在菜单树中返回
     *
     * @param userId 用户 ID
     * @return 当前登录用户可见的树形菜单
     */
    List<MenuVO> listMenuTreeByUserId(Long userId);

    /**
     * 查询系统所有菜单的树（后台菜单管理使用）
     *
     * 使用场景：
     * ------------------------------------------------------------
     * - 后台“菜单管理”页面
     * - 后台“角色分配菜单权限”页面（树 + checkbox）
     * - 系统管理员查看全量菜单结构
     *
     * 行为说明（逻辑步骤）：
     * ------------------------------------------------------------
     * 1. 查询 dynamicmenu_sys_menu 全表所有记录
     * 2. 可选择是否过滤按钮（通常后台管理需要展示按钮）
     * 3. 按 parentId 构建完整树形结构
     * 4. 按 sortOrder 排序
     *
     * 与 listMenuTreeByUserId 的区别：
     * ------------------------------------------------------------
     * - listMenuTreeByUserId 会按用户权限过滤
     * - listAllMenuTree 不进行权限过滤，展示全量资源
     *
     * 注意：
     * ------------------------------------------------------------
     * - 在大多数后台管理系统中，菜单管理页面需要展示按钮（menu_type=3）
     * - 前端也可以在渲染时决定是否隐藏按钮
     *
     * @return 系统完整的菜单树（包含目录、菜单、按钮）
     */
    List<MenuVO> listAllMenuTree();

    /**
     * 根据任意 userId 获取菜单树（与 listMenuTreeByUserId 一致）
     *
     * 说明：
     * ------------------------------------------------------------
     * - 用于后端内部服务调用，如 UserServiceImpl.getUserDetail() 组装用户信息
     * - 与 listMenuTreeByUserId 逻辑一样，只是语义不同
     * - 保留下该方法有助于代码可读性：一个用于“当前登录用户”，一个用于“任意用户”
     *
     * 区别（语义级别）：
     * ------------------------------------------------------------
     * - listMenuTreeByUserId：强调用于“登录用户”，常配合 SecurityContext 使用
     * - getMenuTreeByUserId：强调用于“后台业务根据 userId 查询”，不要求用户必须是当前登录者
     *
     * @param userId 用户ID
     * @return 用户可见菜单树（目录 + 菜单）
     */
    List<MenuVO> getMenuTreeByUserId(Long userId);
}
