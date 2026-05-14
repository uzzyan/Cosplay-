<?php
// 管理员账号
$admin_username = "manager";
$admin_password = "Admin@123";  // 8位，包含字母、数字、特殊符号
$admin_md5 = md5(md5($admin_password));

// 普通用户账号
$user_username = "customer";
$user_password = "User@123";  // 8位，包含字母、数字、特殊符号
$user_md5 = md5(md5($user_password));

echo "=== 管理员账号 ===\n";
echo "用户名: $admin_username\n";
echo "密码: $admin_password\n";
echo "MD5: " . md5($admin_password) . "\n";
echo "MD5(MD5): $admin_md5\n";
echo "\n";

echo "=== 普通用户账号 ===\n";
echo "用户名: $user_username\n";
echo "密码: $user_password\n";
echo "MD5: " . md5($user_password) . "\n";
echo "MD5(MD5): $user_md5\n";
echo "\n";

// 查找现有的BCrypt密码作为模板
$sample_bcrypt = "\$2a\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

// 生成用于演示的SQL语句（BCrypt部分需要手动添加到数据库）
echo "=== SQL插入语句（需要替换BCrypt密码） ===\n";
echo "INSERT INTO sys_user (username, nickname, password, role, email, phone) VALUES\n";
echo "('$admin_username', '系统管理员', '\$BCRYPT_PLACEHOLDER', 'admin', 'admin@shop.com', '13900000001'),\n";
echo "('$user_username', '普通用户', '\$BCRYPT_PLACEHOLDER', 'user', 'user@shop.com', '13900000002');\n";
?>