package net.wcloud.helloworld.dynamicmenu.vo;

import lombok.Data;
import java.util.List;

@Data
public class MenuVO {

    private Long id;
    private Long parentId;
    private String menuName;
    private String menuIcon;
    private Integer menuType;
    private String routePath;
    private String component;
    private String perms;
    private Integer visible;
    private Integer sortOrder;

    private List<MenuVO> children;
}
