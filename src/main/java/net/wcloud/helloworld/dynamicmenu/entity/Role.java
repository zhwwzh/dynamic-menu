package net.wcloud.helloworld.dynamicmenu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dynamicmenu_sys_role")
public class Role {

    /** 角色ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色编码，如 ROLE_ADMIN */
    private String roleCode;

    /** 角色名称，如 管理员 */
    private String roleName;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
