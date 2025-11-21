package net.wcloud.helloworld.dynamicmenu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * Role（角色实体）
 *
 * 所属表：dynamicmenu_sys_role
 *
 * 用途说明：
 * ------------------------------------------------------------
 * - RBAC 权限模型中的“角色”维度
 * - 与用户（user_role）、菜单（role_menu）均为多对多关系
 * - 一个角色可以绑定多个菜单（页面权限 + 按钮权限）
 * - 一个用户可以绑定多个角色
 *
 * 字段说明：
 * ------------------------------------------------------------
 * id 主键 ID（自增）
 * roleCode 角色编码（如：ROLE_ADMIN，必须以 ROLE_ 开头）
 * roleName 角色名称（如：管理员）
 * status 状态（1启用，0禁用）
 * createTime 创建时间（自动填充）
 * updateTime 更新时间（自动填充）
 *
 * 特别注意：
 * ------------------------------------------------------------
 * - Spring Security 默认仅识别以 ROLE_ 开头的角色编码
 * - 若不满足规范，@PreAuthorize("hasRole('ADMIN')") 将无法识别
 *
 * 日志策略：
 * ------------------------------------------------------------
 * - 实体类避免自动打印日志（避免性能和污染）
 * - 提供 logSelf() 方法用于调试角色字段
 */
@Slf4j
@Data
@TableName("dynamicmenu_sys_role")
public class Role {

    /** 角色 ID（主键，自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色编码（必须以 ROLE_ 开头）
     * 示例：ROLE_ADMIN、ROLE_USER
     */
    private String roleCode;

    /** 角色名称（用于展示，如 管理员） */
    private String roleName;

    /** 状态：1-启用；0-禁用 */
    private Integer status;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 修改时间（自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
