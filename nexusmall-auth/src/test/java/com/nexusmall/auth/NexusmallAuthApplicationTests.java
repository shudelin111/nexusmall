package com.nexusmall.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class NexusmallAuthApplicationTests {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Test
    public void test() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String dbHash = "从数据库查到的 password 值";
        System.out.println(passwordEncoder.encode("123456"));
    }
}
