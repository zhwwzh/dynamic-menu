package net.wcloud.helloworld.dynamicmenu.convert;

import net.wcloud.helloworld.dynamicmenu.entity.User;
import net.wcloud.helloworld.dynamicmenu.vo.UserVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * User <-> UserVO 转换器（MapStruct）
 *
 * MapStruct 使用说明：
 * ------------------------------------------------------------
 * - @Mapper(componentModel = "spring")
 * 生成的实现类会被注册为 Spring Bean，可直接 @Autowired 注入。
 *
 * - MapStruct 会自动映射同名字段：
 * - id, username, nickname, status, createTime 等会自动复制。
 * - password 字段不会复制到 VO（因为 VO 中没有该字段）。
 *
 * - 接口中可以定义 default 方法，该方法逻辑完全由开发者实现，
 * MapStruct 不会生成相应代码。
 *
 * - 接口中可以定义 Logger（static final），用于 default 方法记录日志。
 */
@Mapper(componentModel = "spring")
public interface UserConvert {

    /** 日志对象（仅 default 方法会用到） */
    Logger log = LoggerFactory.getLogger(UserConvert.class);

    /**
     * 如果某些场景你不想使用 Spring 注入，也可以使用 INSTANCE：
     * UserConvert.INSTANCE.toVO(user)
     */
    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    /**
     * 基础字段映射：User -> UserVO
     *
     * MapStruct 默认行为：
     * ------------------------------------------------------------
     * - 同名字段自动复制
     * - 不同名字段需手动用 @Mapping 注解（本项目暂不需要）
     * - VO 中缺少的字段自动忽略（如 password）
     */
    UserVO toVO(User user);

    /**
     * 批量转换：List<User> -> List<UserVO>
     */
    List<UserVO> toVOList(List<User> users);

    /**
     * 扩展构造方法：用于构建包含角色 / 权限信息的 UserVO
     *
     * 使用场景：
     * ------------------------------------------------------------
     * UserServiceImpl.buildUserVO() 中，需要将
     * - 基础字段映射
     * - 角色编码 roleCodes
     * - 角色名称 roleNames
     * - 权限列表 permissions
     *
     * 封装为一个完整的 UserVO。
     *
     * 说明：
     * ------------------------------------------------------------
     * - 这里使用 default 方法，可以加入日志
     * - toVO(user) 调用 MapStruct 自动生成的基础映射代码
     */
    default UserVO toVO(User user,
            List<String> roleCodes,
            List<String> roleNames,
            List<String> permissions) {

        if (log.isDebugEnabled()) {
            log.debug("[UserConvert] 执行扩展 User -> UserVO 转换, userId={}, roleCount={}, permCount={}",
                    user == null ? null : user.getId(),
                    roleCodes == null ? 0 : roleCodes.size(),
                    permissions == null ? 0 : permissions.size());
        }

        UserVO vo = toVO(user); // 基础字段映射 (MapStruct 生成)
        vo.setRoleCodes(roleCodes);
        vo.setRoleNames(roleNames);
        vo.setPermissions(permissions);

        if (log.isDebugEnabled()) {
            log.debug("[UserConvert] UserVO 构建完成, userId={}, username={}, roles={}, perms={}",
                    vo.getId(), vo.getUsername(),
                    vo.getRoleCodes(), vo.getPermissions());
        }

        return vo;
    }
}
