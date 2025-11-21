package net.wcloud.helloworld.dynamicmenu.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 简单密码加密工具类
 * 运行后会输出 BCrypt 加密后的密码
 */
public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String raw = "123456"; // 修改为你想加密的密码
        String encoded = encoder.encode(raw);
        System.out.println("Raw Password: " + raw);
        System.out.println("BCrypt Encoded Password: " + encoded);
    }
}
