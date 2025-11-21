package net.wcloud.helloworld.dynamicmenu.convert;

import net.wcloud.helloworld.dynamicmenu.entity.Menu;
import net.wcloud.helloworld.dynamicmenu.vo.MenuVO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MenuConvert
 *
 * MapStruct 转换器：将持久层实体（Menu）转换为前端展示层对象（MenuVO）。
 *
 * 设计说明：
 * ------------------------------------------------------------
 * - MapStruct 是编译期间生成代码，不允许在接口内添加日志逻辑
 * - 该接口仅负责字段映射，不包含任何业务逻辑（符合转换器职责单一原则）
 * - 默认会将同名字段自动复制（id → id, parentId → parentId 等）
 *
 * 使用场景：
 * ------------------------------------------------------------
 * 1. 构建菜单树前，将 Menu 转成 MenuVO（前端使用）
 * 2. RoleController / MenuService 中调用
 * 3. 在需要提供不同视图模型时，可扩展新的 toXxx 方法
 *
 * 注意点：
 * ------------------------------------------------------------
 * - componentModel = "spring"：MapStruct 会生成 Spring Bean，可直接 @Autowired 注入
 * - 若 MenuVO 中的字段比 Menu 多，需要在此接口中声明 @Mapping
 * - MapStruct 内不支持 logger，如果需要调试映射，请在调用处添加日志，而不是这里
 */
@Mapper(componentModel = "spring")
public interface MenuConvert {

    /**
     * 单个实体转换为 VO
     *
     * @param menu Menu 实体
     * @return MenuVO 展示对象
     */
    MenuVO toVO(Menu menu);

    /**
     * 批量转换（List<Menu> → List<MenuVO>）
     *
     * @param list Menu 实体列表
     * @return VO 列表
     */
    List<MenuVO> toVOList(List<Menu> list);
}
