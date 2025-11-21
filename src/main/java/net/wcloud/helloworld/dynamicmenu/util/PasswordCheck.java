package net.wcloud.helloworld.dynamicmenu.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * PasswordCheck 工具类
 *
 * 作用：
 * ------------------------------------------------------------
 * 用于在开发/调试环境下验证：
 * - 数据库中保存的 BCrypt 加密密码
 * - 是否能与“明文密码”成功匹配
 *
 * 使用场景：
 * ------------------------------------------------------------
 * - 登录失败时，用于检查数据库中的密码是否正确加密
 * - 确认 PasswordEncoder.encode() 与 matches() 是否一致
 * - 测试不同版本的 BCrypt 加密兼容性
 *
 * 注意：
 * ------------------------------------------------------------
 * - 明文密码 raw 仅在本地调试时使用，不要在线上打印！
 * - BCrypt 每次 encode() 都会产生不同盐值，因此存储密码永不重复
 * - matches(raw, encoded) 是 BCrypt 的唯一校验方式
 *
 * 运行方式（IDE 或命令行）：
 * ------------------------------------------------------------
 * 执行 main() 方法即可输出验证结果：
 * matches = true / false
 */
@Slf4j
public class PasswordCheck {

    public static void main(String[] args) {

        // 数据库中存储的 BCrypt 密文（示例）
        String dbPassword = "$2a$10$I4zDCNKUUcosYFCu9VSUBe3idBKL13UZztprLUaUGLSRZjVjkZABK";

        // 明文密码（本地测试用）
        String raw = "123456";

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        boolean match = encoder.matches(raw, dbPassword);

        // 安全日志（不打印原始密码）
        log.info("BCrypt 验证结果 matches = {}", match);

        // 控制台打印（为了 main 方法使用方便）
        System.out.println("matches = " + match);
    }
}
