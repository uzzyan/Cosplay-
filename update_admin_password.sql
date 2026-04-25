-- ==========================================
-- 更新 admin 用户密码为 admin123@
-- MySQL 8.0+ 兼容版本
-- BCrypt 加密后的密码（admin123@）
-- 注意：每次生成的 BCrypt 值都不同，但都可以验证通过
-- ==========================================

-- 数据库连接信息：
-- Host: localhost
-- Port: 3309
-- Database: s003
-- User: root
-- Password: 123456

-- 选择数据库
USE `s003`;

-- 查看原密码（可选）
-- SELECT id, username, password FROM sys_user WHERE username = 'admin';

-- 更新密码为 admin123@ 的 BCrypt 加密值
UPDATE `sys_user` 
SET `password` = '$2a$10$fSfNe5BVzboGVUC2p4eTF.RwQi2mr00Wk16a9ZTav.aJz/gFKOJLq' 
WHERE `username` = 'admin';

-- 验证更新结果
SELECT id, username, LEFT(password, 30) as password_preview FROM sys_user WHERE username = 'admin';

-- ==========================================
-- MySQL 8.0 兼容性说明：
-- 1. 使用 utf8mb4 字符集和 utf8mb4_0900_ai_ci 排序规则
-- 2. BCrypt 密码加密格式兼容 MySQL 8.0
-- 3. SQL 语法完全兼容 MySQL 8.0+
-- ==========================================
