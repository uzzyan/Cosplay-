import hashlib
from flask import Flask
from flask_bcrypt import Bcrypt

# 管理员账号
admin_username = "manager"
admin_password = "Admin@123"  # 8位，包含字母、数字、特殊符号
admin_md5 = hashlib.md5(hashlib.md5(admin_password.encode()).hexdigest().encode()).hexdigest()

# 普通用户账号
user_username = "customer"
user_password = "User@123"  # 8位，包含字母、数字、特殊符号
user_md5 = hashlib.md5(hashlib.md5(user_password.encode()).hexdigest().encode()).hexdigest()

print("=== 管理员账号 ===")
print(f"用户名: {admin_username}")
print(f"密码: {admin_password}")
print(f"MD5: {hashlib.md5(admin_password.encode()).hexdigest()}")
print(f"MD5(MD5): {admin_md5}")
print()
print("=== 普通用户账号 ===")
print(f"用户名: {user_username}")
print(f"密码: {user_password}")
print(f"MD5: {hashlib.md5(user_password.encode()).hexdigest()}")
print(f"MD5(MD5): {user_md5}")
print()
print("=== 密码加密验证 ===")
print(f"管理员密码验证: {hashlib.md5(admin_md5.encode()).hexdigest()}")
print(f"用户密码验证: {hashlib.md5(user_md5.encode()).hexdigest()}")