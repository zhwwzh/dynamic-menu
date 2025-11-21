package net.wcloud.helloworld.dynamicmenu.dto;

import lombok.Data;
import net.wcloud.helloworld.dynamicmenu.vo.MenuVO;
import java.util.List;

@Data
public class LoginResponseDTO {

    private String token;

    /** 当前用户基本信息 */
    private Long userId;
    private String username;
    private String nickname;

    /** 当前用户的菜单树 */
    private List<MenuVO> menus;

    /** 当前用户拥有的权限标识集合（如：sys:user:list） */
    private List<String> permissions;
}
