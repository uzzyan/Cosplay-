# Apifox自动化测试失败排查指南

> 问题：19个接口全部测试失败  
> 更新日期：2026-04-15

---

## 🔍 常见原因分析

### 原因1：后端服务未启动（最常见）

**症状**：所有接口都返回网络错误

**检查方法**：
```bash
# 检查后端服务是否运行
# Windows
netstat -ano | findstr 9191

# 如果没有任何输出，说明服务未启动
```

**解决方案**：
```bash
# 启动后端服务
cd mall-server
mvn spring-boot:run

# 等待启动完成，看到以下信息表示成功：
# Started MallApplication in X.XXX seconds

# 测试服务是否正常
curl http://localhost:9191
```

---

### 原因2：环境变量未正确设置

**症状**：接口URL显示为 `{{baseUrl}}/login` 而不是实际地址

**检查方法**：
```
1. 在Apifox左上角查看当前环境
2. 点击环境名称 → 查看变量
3. 检查 baseUrl 是否有值
```

**解决方案**：
```
1. 创建/编辑环境
2. 添加变量：
   变量名: baseUrl
   初始值: http://localhost:9191
   
3. 保存并选择该环境
4. 重新运行测试
```

---

### 原因3：未先执行登录接口

**症状**：需要Token的接口都返回401错误

**原因**：
```
token变量为空，因为还没有执行登录接口
```

**解决方案**：
```
方法1：按顺序执行
1. 先单独运行"用户登录"接口
2. 检查环境变量中token是否已保存
3. 再运行其他接口

方法2：运行测试场景
1. 使用"完整购物流程"测试场景
2. 自动按顺序执行，先登录再其他操作
```

---

### 原因4：数据库未初始化

**症状**：登录失败，提示"用户不存在"

**检查方法**：
```sql
-- 连接MySQL
mysql -u root -p

-- 检查数据库
SHOW DATABASES;
USE s003;

-- 检查用户表
SELECT * FROM user;

-- 如果没有数据，需要导入SQL
```

**解决方案**：
```bash
# 导入数据库
mysql -u root -p s003 < s003.sql

# 或者使用Navicat/MySQL Workbench导入
```

---

### 原因5：Redis服务未启动

**症状**：登录失败或Token验证失败

**检查方法**：
```bash
# Windows
# 进入redis目录
cd redis
RedisStarter.bat

# 检查Redis是否运行
redis-cli
PING
# 应该返回 PONG
```

**解决方案**：
```bash
# 启动Redis
cd redis
RedisStarter.bat

# 保持Redis窗口打开
```

---

## 🛠️ 逐步排查方案

### 第1步：检查服务状态

```bash
# 1. 检查MySQL
mysql -u root -p -e "SELECT 1"

# 2. 检查Redis
redis-cli PING

# 3. 检查后端服务
curl http://localhost:9191

# 4. 检查端口占用
netstat -ano | findstr 9191
```

**预期结果**：
```
✅ MySQL: 连接成功
✅ Redis: PONG
✅ 后端: 返回错误或HTML（说明服务在运行）
✅ 端口: 显示9191端口被占用
```

---

### 第2步：单独测试登录接口

**在Apifox中**：
```
1. 找到"01-用户认证" → "用户登录"
2. 检查请求配置：
   URL: http://localhost:9191/login
   Method: POST
   Body: {"username":"admin","password":"e10adc3949ba59abbe56e057f20f883e"}
   
3. 点击"发送"
4. 查看响应
```

**预期成功响应**：
```json
{
  "code": "200",
  "msg": null,
  "data": {
    "id": 1,
    "username": "admin",
    "nickname": "管理员",
    "role": "admin",
    "token": "eyJhbGci..."
  }
}
```

**常见错误及解决**：

| 错误信息 | 原因 | 解决方案 |
|---------|------|---------|
| Network Error | 后端未启动 | 启动后端服务 |
| 用户不存在 | 数据库无数据 | 导入s003.sql |
| 密码错误 | 密码不对 | 使用MD5加密的密码 |
| Connection refused | 端口不对 | 检查application.yml中的端口 |

---

### 第3步：检查Token是否保存

**在Apifox中**：
```
1. 登录接口执行成功后
2. 点击右上角"环境"图标
3. 查看当前环境的变量
4. 检查 token 是否有值
```

**如果没有值**：
```
手动添加：
变量名: token
初始值: （粘贴登录返回的token）

或者检查后置脚本是否执行：
查看"控制台"标签，应该显示：
✅ Token已保存: eyJhbGci...
```

---

### 第4步：测试其他接口

**测试商品列表**：
```
1. 找到"02-商品管理" → "商品列表（分页）"
2. 检查URL：
   应该是：http://localhost:9191/api/good/page?pageNum=1&pageSize=10
   不应该是：{{baseUrl}}/api/good/page...
   
3. 检查Headers：
   token: {{token}}
   实际值应该是真实的token
   
4. 点击"发送"
```

---

## 📊 错误类型诊断

### 错误类型1：全部接口 Network Error

**原因**：后端服务未启动

**解决**：
```bash
cd mall-server
mvn spring-boot:run
```

---

### 错误类型2：登录失败，其他全部401

**原因**：登录接口失败，导致token为空

**解决**：
```
1. 先修复登录接口
2. 检查数据库是否有用户
3. 检查密码是否正确
4. 检查Redis是否运行
```

---

### 错误类型3：登录成功，其他接口401

**原因**：token未正确传递

**解决**：
```
1. 检查环境变量中token是否有值
2. 检查接口Headers中是否使用了{{token}}
3. 手动在接口Headers中添加token值测试
```

---

### 错误类型4：部分接口403

**原因**：权限不足

**解决**：
```
用户接口使用 {{token}}
管理员接口使用 {{adminToken}}

检查：
- 普通用户接口 → token
- 管理员接口 → adminToken
```

---

### 错误类型5：商品/订单相关500错误

**原因**：数据库表缺失或数据问题

**解决**：
```sql
-- 检查表是否存在
SHOW TABLES;

-- 应该包含以下表：
-- user, good, cart, orders, order_goods
-- category, comment, message, after_sale
-- carousel, notice, icon, etc.

-- 如果缺失，重新导入SQL
```

---

## 🎯 快速修复步骤

### 完整的启动顺序

```
第1步：启动MySQL
✅ 确认MySQL运行在3309端口

第2步：启动Redis
cd redis
RedisStarter.bat
✅ 确认Redis运行在6379端口

第3步：导入数据库（如果未导入）
mysql -u root -p s003 < s003.sql
✅ 确认user表有数据

第4步：启动后端
cd mall-server
mvn spring-boot:run
✅ 看到"Started MallApplication"

第5步：验证后端
curl http://localhost:9191
✅ 有响应（即使是错误响应）

第6步：启动Apifox
✅ 创建项目
✅ 导入JSON文件
✅ 配置环境变量

第7步：测试登录
✅ 发送登录请求
✅ 检查token已保存

第8步：测试其他接口
✅ 逐个测试
✅ 或运行测试场景
```

---

## 💡 调试技巧

### 技巧1：查看完整请求

```
在Apifox中：
1. 点击"预览"标签
2. 查看完整的请求URL
3. 查看Headers
4. 查看Body

确认所有变量都已替换为实际值
```

### 技巧2：查看控制台输出

```
在Apifox中：
1. 点击"控制台"标签
2. 查看console.log输出
3. 查看脚本执行结果

应该看到：
✅ Token已保存: eyJhbGci...
✅ 用户ID: 1
```

### 技巧3：使用curl测试

```bash
# 测试登录
curl -X POST http://localhost:9191/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"e10adc3949ba59abbe56e057f20f883e\"}"

# 测试商品列表（使用返回的token）
curl http://localhost:9191/api/good/page?pageNum=1&pageSize=10 \
  -H "token: YOUR_TOKEN_HERE"
```

### 技巧4：查看后端日志

```
在IDEA或命令行中：
1. 查看后端控制台输出
2. 查找错误信息
3. 常见错误：
   - SQL异常 → 数据库问题
   - NullPointerException → 代码问题
   - 404 Not Found → 路径问题
```

---

## 📝 测试报告示例

### 成功的测试报告

```
测试环境：开发环境
测试时间：2026-04-15 15:30:00

测试结果：
总接口数：19
通过：19
失败：0
通过率：100%

详细结果：
✅ 01-用户认证/用户登录 - 120ms
✅ 01-用户认证/用户注册 - 95ms
✅ 02-商品管理/商品列表 - 85ms
✅ 02-商品管理/商品搜索 - 90ms
✅ 02-商品管理/商品详情 - 75ms
✅ 02-商品管理/推荐商品 - 110ms
✅ 03-购物车/添加商品 - 150ms
✅ 03-购物车/查看购物车 - 80ms
✅ 04-订单管理/创建订单 - 200ms
✅ 04-订单管理/查询订单 - 95ms
✅ 04-订单管理/支付订单 - 180ms
✅ 04-订单管理/确认收货 - 160ms
✅ 05-个人中心/获取用户信息 - 70ms
✅ 05-个人中心/更新用户信息 - 130ms
✅ 06-管理员功能/用户管理 - 100ms
✅ 06-管理员功能/新增商品 - 180ms
✅ 06-管理员功能/订单发货 - 170ms
✅ 06-管理员功能/收入统计 - 250ms
✅ 06-管理员功能/商品排行 - 190ms
```

### 失败的测试报告（需要修复）

```
测试环境：开发环境
测试时间：2026-04-15 15:30:00

测试结果：
总接口数：19
通过：0
失败：19
通过率：0%

失败原因分析：
❌ 全部接口：Network Error
   原因：后端服务未启动
   
解决步骤：
1. 启动后端服务
2. 重新测试
```

---

## 🔧 一键检查脚本

创建 `check_services.bat`（Windows）：

```batch
@echo off
echo === Cosplay商城服务检查 ===
echo.

echo [1/4] 检查MySQL...
mysql -u root -p123456 -e "SELECT 1" >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ MySQL运行正常
) else (
    echo ❌ MySQL未运行或密码错误
)

echo.
echo [2/4] 检查Redis...
redis-cli PING >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Redis运行正常
) else (
    echo ❌ Redis未运行
)

echo.
echo [3/4] 检查后端服务...
curl -s http://localhost:9191 >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ 后端服务运行正常
) else (
    echo ❌ 后端服务未运行
)

echo.
echo [4/4] 检查数据库数据...
mysql -u root -p123456 s003 -e "SELECT COUNT(*) FROM user" >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ 数据库有数据
) else (
    echo ❌ 数据库无数据或未导入
)

echo.
echo === 检查完成 ===
pause
```

---

## 📞 需要帮助？

如果按照以上步骤仍然无法解决，请提供：

1. **错误截图** - Apifox中的错误信息
2. **后端日志** - 启动和控制台输出
3. **接口详情** - 某个失败接口的完整请求和响应
4. **环境变量** - 当前环境的变量值

我会帮您进一步诊断问题！

---

**文档更新时间**：2026-04-15  
**适用场景**：Apifox自动化测试全部失败排查
