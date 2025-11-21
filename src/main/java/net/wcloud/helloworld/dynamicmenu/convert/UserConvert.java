package net.wcloud.helloworld.dynamicmenu.convert;

import net.wcloud.helloworld.dynamicmenu.entity.User;
import net.wcloud.helloworld.dynamicmenu.vo.UserVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * User <-> UserVO 转换
 */
@Mapper(componentModel = "spring")
public interface UserConvert {

    // 如果你有地方不想用 Spring 注入，也可以通过 INSTANCE 使用（可选）
    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    /**
     * 基础字段 mapping：User -> UserVO
     * - createTime 字段名一致，不需要额外 @Mapping
     * - password 不在 UserVO 中，MapStruct 自动忽略
     */
    UserVO toVO(User user);

    List<UserVO> toVOList(List<User> users);

    /**
     * 带角色 / 权限 / 菜单的扩展构造方法：
     * 这里使用 default 方法，基于基础映射再补充其他字段，
     * MapStruct 不会为 default 方法生成代码，逻辑全由你掌控。
     */
    default UserVO toVO(User user,
            List<String> roleCodes,
            List<String> roleNames,
            List<String> permissions) {
        UserVO vo = toVO(user);
        vo.setRoleCodes(roleCodes);
        vo.setRoleNames(roleNames);
        vo.setPermissions(permissions);
        return vo;
    }
}
