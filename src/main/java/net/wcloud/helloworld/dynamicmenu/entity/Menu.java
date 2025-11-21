package net.wcloud.helloworld.dynamicmenu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * Menu（系统菜单/权限资源实体）
 *
 * 表结构 dynamicmenu_sys_menu 用途：
 * ------------------------------------------------------------
 * 支持三种菜单类型：
 * 1. 目录（folder）
 * 2. 菜单（page）
 * 3. 按钮权限（action）
 *
 * 与角色的关系：
 * ------------------------------------------------------------
 * - 多对多关系（role_menu 表）
 * - RBAC 权限控制基础
 *
 * 字段说明：
 * ------------------------------------------------------------
 * id 主键
 * parentId 父节点 ID（0 or null 表示根节点）
 * menuName 菜单名称
 * menuIcon 前端图标（可选）
 * menuType 类型（1目录、2菜单、3按钮）
 * routePath 前端路由路径
 * component 前端组件路径（仅菜单时使用）
 * perms 按钮权限标识（如 sys:user:list）
 * visible 是否可见（1显示 / 0隐藏）
 * status 是否启用（1启用 / 0禁用）
 * sortOrder 排序字段
 * createTime 创建时间（自动填充）
 * updateTime 修改时间（自动填充）
 *
 * 日志策略：
 * ------------------------------------------------------------
 * - 默认不主动输出日志，避免实体层污染和性能开销
 * - 提供 logSelf() 方法用于调试，输出核心非敏感信息
 */
@Slf4j
@Data
@TableName("dynamicmenu_sys_menu")
public class Menu {

    /** 主键 ID （自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 父级菜单 ID（0 或 null 代表根节点） */
    private Long parentId;

    /** 菜单名称 */
    private String menuName;

    /** 菜单图标（前端展示用） */
    private String menuIcon;

    /** 菜单类型：1-目录 2-菜单 3-按钮 */
    private Integer menuType;

    /** 前端路由 path（仅菜单时使用） */
    private String routePath;

    /** 前端组件路径，如：views/system/user/index.vue */
    private String component;

    /** 按钮权限标识，如：sys:user:list */
    private String perms;

    /** 是否可见：1-显示 0-隐藏 */
    private Integer visible;

    /** 菜单状态：1-启用 0-禁用 */
    private Integer status;

    /** 菜单排序字段 */
    private Integer sortOrder;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间（自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
