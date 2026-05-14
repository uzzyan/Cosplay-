-- ============================================
-- Update old MD5 passwords to BCrypt format
-- Execution Date: 2026-04-27
-- ============================================
-- 
-- NOTE: This script requires manual BCrypt hash generation
-- Please use UpdateAdminPassword.java tool to generate BCrypt passwords
-- 
-- Current old passwords (MD5 format):
-- user: e10adc3949ba59abbe56e057f20f883e (123456)
-- user2: e10adc3949ba59abbe56e057f20f883e (123456)
--
-- Please run the Java tool to generate new BCrypt passwords first!
-- ============================================

USE `s003`;

-- Example update (replace with actual BCrypt hashes):
-- UPDATE sys_user SET password = '$2a$10$...' WHERE username = 'user';
-- UPDATE sys_user SET password = '$2a$10$...' WHERE username = 'user2';

-- ============================================
-- End of script
-- ============================================
