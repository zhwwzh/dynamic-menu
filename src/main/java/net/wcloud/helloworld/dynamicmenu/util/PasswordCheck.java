package net.wcloud.helloworld.dynamicmenu.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordCheck {
    public static void main(String[] args) {
        // 数据库里这条密码直接复制过来
        String dbPassword = "$2a$10$I4zDCNKUUcosYFCu9VSUBe3idBKL13UZztprLUaUGLSRZjVjkZABK";
        String raw = "123456";

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("matches = " + encoder.matches(raw, dbPassword));
    }
}
