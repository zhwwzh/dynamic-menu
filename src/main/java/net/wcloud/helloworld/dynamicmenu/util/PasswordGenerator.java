package net.wcloud.helloworld.dynamicmenu.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt 密码加密生成工具类
 *
 * 作用：
 * ------------------------------------------------------------
 * - 在开发或测试阶段快速生成 BCrypt 加密密码
 * - 适用于初始化用户密码或本地调试登录
 *
 * 注意事项（非常重要）：
 * ------------------------------------------------------------
 * 1. 明文密码 raw 仅在开发环境使用，不要在生产日志中输出！
 * 2. BCrypt 每次 encode() 都会生成不同的密文（因为随机盐），但 matches() 始终保持兼容。
 * 3. 生成的加密密码可直接写入数据库 dynamicmenu_sys_user.password 字段。
 *
 * 运行方式：
 * ------------------------------------------------------------
 * - 在 IDE 中直接运行 main()
 * - 控制台将输出原文密码（便于本地调试用）
 * - 日志中不会输出原文密码（安全设计）
 */
@Slf4j
public class PasswordGenerator {

    public static void main(String[] args) {

        // 修改为你需要加密的密码（仅用于开发调试）
        String raw = "123456";

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // BCrypt 加密，结果每次不同，但 matches() 可验证
        String encoded = encoder.encode(raw);

        // 控制台输出（仅用于本地调试）
        System.out.println("Raw Password: " + raw);
        System.out.println("BCrypt Encoded Password: " + encoded);

        // 日志输出（不包含明文密码，避免泄露）
        log.info("BCrypt 加密完成。encoded.length={} encoded={}", encoded.length(), encoded);
    }
}
