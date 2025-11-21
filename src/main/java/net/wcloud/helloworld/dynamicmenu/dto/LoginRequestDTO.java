package net.wcloud.helloworld.dynamicmenu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 登录请求 DTO（Data Transfer Object）
 *
 * 作用：
 * ------------------------------------------------------------
 * - 承接前端提交的登录数据（username + password）
 * - 使用 @Valid 机制自动校验字段有效性
 *
 * 字段验证：
 * ------------------------------------------------------------
 * - @NotBlank username 必填
 * - @NotBlank password 必填
 *
 * 安全说明：
 * ------------------------------------------------------------
 * - 密码字段绝对不能写入日志（避免泄露）
 * - 因此 logger 仅输出 username
 *
 * 日志策略（通过 logSelf() 方法触发）：
 * ------------------------------------------------------------
 * - debug 级别：记录用户名，用于调试登录请求
 * - 不在 DTO 构造时记录，避免框架反序列化时多次触发
 */
@Slf4j
@Data
public class LoginRequestDTO {

    /** 登录用户名（必填） */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 登录密码（必填）
     * 绝不能写入日志！！！
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
