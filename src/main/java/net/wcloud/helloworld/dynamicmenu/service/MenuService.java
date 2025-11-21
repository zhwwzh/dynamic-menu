package net.wcloud.helloworld.dynamicmenu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.wcloud.helloworld.dynamicmenu.entity.Menu;
import net.wcloud.helloworld.dynamicmenu.vo.MenuVO;

import java.util.List;

/**
 * 菜单服务接口
 *
 * 说明：
 * 1. 菜单表 dynamicmenu_sys_menu 存储了系统的【目录、菜单、按钮】三级资源
 * 2. 用户与角色(role)，角色与菜单(menu) 多对多关联
 * 3. 所有查询菜单树的逻辑，都需要按用户 → 角色 → 菜单进行合并去重，再构建树形结构
 *
 * 使用场景：
 * - listMenuTreeByUserId：登录用户获取自己的左侧菜单树（多角色合并）
 * - listAllMenuTree：后台“菜单管理”页面，展示全量菜单树
 * - getMenuTreeByUserId：为后端内部服务提供“用户菜单树”，与 listMenuTreeByUserId 基本一致
 */
public interface MenuService extends IService<Menu> {

    /**
     * 查询当前登录用户的菜单树（多角色合并 + 去重）
     *
     * 使用场景：
     * - 登录成功后返回给前端的菜单（侧边栏菜单）
     * - 当前登录用户“我的菜单”
     *
     * 行为说明：
     * 1. 根据 userId 查询其所有角色
     * 2. 根据角色查询其所拥有的所有菜单/目录/按钮（去重）
     * 3. 过滤掉按钮(menu_type = 3)，只构建目录/菜单树
     * 4. 按 parentId 构建树形结构
     * 5. 返回 MenuVO 的树（供前端渲染）
     *
     * @param userId 用户 ID
     * @return 用户可见的树形菜单（目录 + 菜单，不包含按钮）
     */
    List<MenuVO> listMenuTreeByUserId(Long userId);

    /**
     * 查询系统所有菜单的树（后台菜单管理使用）
     *
     * 使用场景：
     * - 后台 “菜单管理” 页面显示全量菜单树
     * - 后台给角色分配菜单权限（树形结构 + checkbox）
     *
     * 行为说明：
     * 1. 查询 dynamicmenu_sys_menu 全表数据
     * 2. 过滤掉按钮(menu_type = 3) 或按需保留
     * 3. 按 parentId 构建完整的树形结构
     * 4. 结果不会受用户权限影响
     *
     * 注意：
     * 后台菜单管理一般会展示全部类型（包括按钮），但是前端列表可能会隐藏按钮类型。
     *
     * @return 系统完整的菜单树
     */
    List<MenuVO> listAllMenuTree();

    /**
     * 获取用户的菜单树（与 listMenuTreeByUserId 作用相同）
     *
     * 说明：
     * - 为内部服务调用准备（例如 UserServiceImpl 中构建用户详情）
     * - 逻辑上等价于 listMenuTreeByUserId，可视作别名方法
     * - 保留该方法是为了代码语义更清晰
     *
     * 与 listMenuTreeByUserId 的区别（语义区分）：
     * - listMenuTreeByUserId 侧重“当前登录用户”
     * - getMenuTreeByUserId 侧重“后端根据 userId 查询任意用户”
     *
     * @param userId 用户 ID
     * @return 用户可见的菜单树
     */
    List<MenuVO> getMenuTreeByUserId(Long userId);
}
