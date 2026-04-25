package com.rabbiter.em.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 密码安全链路专项测试
 * 验证：前端MD5 -> 后端BCrypt的匹配一致性
 */
public class PasswordSecurityAuditTest {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    public void testPasswordSecurityChain() {
        // 1. 模拟前端操作
        // 明文密码
        String rawPassword = "admin123@";
        // 模拟前端 MD5 (32位小写)
        // 使用外部工具或手动确认：admin123@ 的 MD5 是 ecd00aa1acd325ba7575cb0f638b04a5
        String frontEndMd5 = "ecd00aa1acd325ba7575cb0f638b04a5";

        // 2. 模拟后端存储 (注册/修改密码阶段)
        // 后端接收到 MD5 串，进行 BCrypt 哈希
        String dbBCryptHash = encoder.encode(frontEndMd5);
        
        System.out.println("Audit - Front-end MD5: " + frontEndMd5);
        System.out.println("Audit - DB BCrypt Hash: " + dbBCryptHash);

        // 3. 模拟登录校验
        // 登录时，后端再次接收到相同明文生成的 MD5 串
        boolean isMatch = encoder.matches(frontEndMd5, dbBCryptHash);
        
        // 断言：匹配必须成功
        assertTrue(isMatch, "密码安全链路校验失败：前端MD5与库内BCrypt不匹配！");
        
        // 4. 反向验证
        // 模拟错误的 MD5 串
        String wrongMd5 = "00000000000000000000000000000000";
        assertFalse(encoder.matches(wrongMd5, dbBCryptHash), "安全漏洞：错误的MD5串竟然匹配成功！");
        
        // 模拟被篡改的密文
        String tamperedHash = dbBCryptHash.substring(0, dbBCryptHash.length() - 1) + (dbBCryptHash.endsWith("a") ? "b" : "a");
        assertFalse(encoder.matches(frontEndMd5, tamperedHash), "密文被篡改后应无法通过校验");
    }
}
