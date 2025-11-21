package net.wcloud.helloworld.dynamicmenu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * User（系统用户实体）
 *
 * 所属表：dynamicmenu_sys_user
 *
 * 该实体是 RBAC 权限体系的基础：
 * ------------------------------------------------------------
 * - 一个用户可绑定多个角色（user_role）
 * - 用户权限 = 角色所拥有的权限（菜单 + 按钮权限）
 *
 * 字段说明：
 * ------------------------------------------------------------
 * id 用户主键 ID（自增）
 * username 登录用户名（唯一）
 * password BCrypt 加密密码（永不明文保存）
 * nickname 用户昵称（展示用）
 * avatar 用户头像 URL
 * status 用户状态（1启用 / 0禁用）
 * createTime 创建时间（MyBatis-Plus 自动填充）
 *
 * 安全说明：
 * ------------------------------------------------------------
 * - password 字段绝对不能写入日志
 * - logSelf() 方法不会输出 password
 * - 仅用于调试用户基本信息
 *
 * 日志策略：
 * ------------------------------------------------------------
 * - 不自动打印日志，避免实体层污染
 * - 仅提供 logSelf() 方法，由调用处按需触发
 */
@Slf4j
@Data
@TableName("dynamicmenu_sys_user")
public class User {

    /** 用户主键 ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名（登录名，唯一） */
    private String username;

    /**
     * 用户密码（BCrypt 加密存储）
     * 永远不能以明文日志输出！！！
     */
    private String password;

    /** 用户昵称（前端展示用） */
    private String nickname;

    /** 用户头像 URL */
    private String avatar;

    /** 状态：1-启用；0-禁用 */
    private Integer status;

    /** 创建时间（插入时自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
