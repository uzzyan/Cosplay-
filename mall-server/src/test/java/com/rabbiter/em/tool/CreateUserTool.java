package com.rabbiter.em.tool;

import cn.hutool.crypto.digest.DigestUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 创建用户工具类
 * 用途：生成用户密码哈希并输出可直接执行的 SQL INSERT 语句
 * 密码加密流程：明文 → MD5（前端）→ MD5（后端）→ BCrypt
 *
 * 使用方法（PowerShell）：
 *   cd mall-server
 *   mvn test -pl . -Dtest=CreateUserTool -Dsurefire.failIfNoSpecifiedTests=false
 *
 * @author uzzyan
 */
public class CreateUserTool {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    public static void main(String[] args) {
        // ======== 配置区：按需修改 ========
        String username = "admin";
        String plainPassword = "admin123@";
        String nickname = "超级管理员";
        String role = "admin";
        String phone = "13800000000";
        // ==================================

        String encodedPassword = encodePassword(plainPassword);

        System.out.println("========== 用户信息 ==========");
        System.out.println("用户名  : " + username);
        System.out.println("密码明文: " + plainPassword);
        System.out.println("角色    : " + role);
        System.out.println("加密密码: " + encodedPassword);
        System.out.println();
        System.out.println("========== SQL 语句 ==========");
        System.out.println("-- 若同名账号已存在则先删除（可选）");
        System.out.println("-- DELETE FROM sys_user WHERE username = '" + username + "';");
        System.out.println();
        System.out.println("INSERT INTO sys_user (username, password, nickname, role, phone)");
        System.out.printf("VALUES ('%s', '%s', '%s', '%s', '%s');%n",
                username, encodedPassword, nickname, role, phone);
        System.out.println();
        System.out.println("========== 验证（可选）==========");
        System.out.println("验证密码是否正确: " + verifyPassword(plainPassword, encodedPassword));
    }

    /**
     * 加密密码：明文 → MD5（模拟前端）→ MD5（模拟后端）→ BCrypt
     * 与 UserService.login() 保持一致：前端发 MD5，后端再做一次 MD5 后 BCrypt 匹配
     */
    public static String encodePassword(String plainPassword) {
        String md5Once = DigestUtil.md5Hex(plainPassword);   // 模拟前端 MD5
        String md5Twice = DigestUtil.md5Hex(md5Once);        // 模拟后端再次 MD5
        return PASSWORD_ENCODER.encode(md5Twice);            // BCrypt
    }

    /**
     * 验证密码（用于自测）
     */
    public static boolean verifyPassword(String plainPassword, String encodedPassword) {
        String md5Once = DigestUtil.md5Hex(plainPassword);
        String md5Twice = DigestUtil.md5Hex(md5Once);
        return PASSWORD_ENCODER.matches(md5Twice, encodedPassword);
    }
}
