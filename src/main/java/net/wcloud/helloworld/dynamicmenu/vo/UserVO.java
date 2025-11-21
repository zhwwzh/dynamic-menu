package net.wcloud.helloworld.dynamicmenu.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserVO {

    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 头像 */
    private String avatar;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 用户角色编码列表，如：["ROLE_ADMIN", "ROLE_USER"] */
    private List<String> roleCodes;

    /** 用户角色名称列表，如：["管理员", "普通用户"] — 可选 */
    private List<String> roleNames;

    /** 按钮权限集合（Permission List），如：["sys:user:list"] */
    private List<String> permissions;

    private List<MenuVO> menus;
}
