package net.wcloud.helloworld.dynamicmenu.convert;

import net.wcloud.helloworld.dynamicmenu.entity.Menu;
import net.wcloud.helloworld.dynamicmenu.vo.MenuVO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MenuConvert {

    MenuVO toVO(Menu menu);

    List<MenuVO> toVOList(List<Menu> list);
}
