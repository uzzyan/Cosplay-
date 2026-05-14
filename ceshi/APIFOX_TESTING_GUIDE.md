# Apifox 测试 Cosplay商城 API 完整指南

> 文档版本：v1.0  
> 更新日期：2026-04-15  
> 测试工具：Apifox（国产API协作平台）

---

## 📋 目录

1. [Apifox简介](#apifox简介)
2. [安装与配置](#安装与配置)
3. [创建项目](#创建项目)
4. [导入API接口](#导入api接口)
5. [环境变量配置](#环境变量配置)
6. [接口测试详解](#接口测试详解)
7. [自动化测试](#自动化测试)
8. [Mock数据](#mock数据)
9. [API文档生成](#api文档生成)
10. [团队协作](#团队协作)

---

## 🎯 Apifox简介

### 什么是Apifox？

Apifox = Postman + Swagger + Mock + JMeter

**核心功能**：
- ✅ API文档管理
- ✅ API调试测试
- ✅ API Mock数据
- ✅ API自动化测试
- ✅ 团队协作

**优势**：
- 🇨🇳 国产软件，中文界面
- 🚀 功能强大，一站式平台
- 💰 免费版功能充足
- 📚 学习成本低

### 下载方式

- **桌面客户端**：https://www.apifox.cn/
- **网页版**：https://apifox.com/
- **推荐**：桌面客户端（功能更完整）

---

## 🔧 安装与配置

### 1. 下载并安装

```bash
# Windows
下载 .exe 安装包，双击安装

# Mac
下载 .dmg 文件，拖入Applications

# Linux
下载 .AppImage 文件，添加执行权限后运行
```

### 2. 注册/登录

- 支持手机号注册
- 支持微信登录
- 支持GitHub登录

### 3. 初始设置

```
设置 → 通用
├── 主题：浅色/深色
├── 语言：中文
└── 字体大小：默认
```

---

## 📁 创建项目

### 1. 新建项目

```
步骤：
1. 点击"新建项目"
2. 选择"手动创建"
3. 填写项目信息：
   - 项目名称：Cosplay商城API
   - 项目描述：Cosplay服装电商系统API接口
   - 基础URL：http://localhost:9191
4. 点击"创建"
```

### 2. 项目结构

```
Cosplay商城API
├── 环境管理
│   ├── 开发环境（localhost:9191）
│   └── 生产环境（yourdomain.com）
├── 接口管理
│   ├── 用户认证
│   ├── 商品管理
│   ├── 购物车
│   ├── 订单管理
│   ├── 个人中心
│   └── 管理员功能
├── 数据模型
│   ├── User
│   ├── Good
│   ├── Order
│   └── Cart
└── 测试场景
    ├── 用户登录流程
    └── 完整购物流程
```

---

## 🌐 环境变量配置

### 1. 创建环境

```
项目设置 → 环境管理 → 新建环境

环境名称：开发环境
环境变量：
├── baseUrl: http://localhost:9191
├── token: （留空，登录后自动填充）
├── userId: （留空，登录后自动填充）
└── adminToken: （留空，管理员登录后填充）
```

### 2. 全局变量（可选）

```
全局环境 → 添加变量
├── appName: Cosplay商城
├── version: v3.0
└── author: 开发团队
```

---

## 📥 导入API接口

### 方式1：手动创建（推荐学习）

#### 创建接口分组

```
1. 点击"新建分组"
2. 创建以下分组：
   - 01-用户认证
   - 02-商品管理
   - 03-购物车
   - 04-订单管理
   - 05-个人中心
   - 06-管理员功能
```

#### 添加接口示例：用户登录

```
1. 选择"01-用户认证"分组
2. 点击"新建接口"
3. 填写接口信息：

基本信息：
├── 接口名称：用户登录
├── 接口路径：/login
├── 请求方法：POST
└── 接口描述：用户登录获取Token

请求配置：
├── Headers：
│   └── Content-Type: application/json
├── Body（raw JSON）：
│   {
│     "username": "{{username}}",
│     "password": "{{password}}"
│   }
└── 前置操作：无

后置操作（脚本）：
└── 见下方"自动保存Token脚本"

预期响应：
└── 添加成功响应示例
```

### 方式2：导入OpenAPI/Swagger

如果有Swagger文档：

```
1. 点击"导入"
2. 选择"URL导入"
3. 输入：http://localhost:9191/v2/api-docs
4. 选择导入模式：
   - 普通导入：仅查看
   - 同步导入：自动更新
5. 点击"确定"
```

### 方式3：导入Postman集合

```
1. 从Postman导出Collection（JSON）
2. Apifox点击"导入"
3. 选择"Postman Collection"
4. 上传JSON文件
5. 确认导入
```

---

## 🧪 接口测试详解

### 1. 用户登录测试

#### 接口配置

```
接口：POST /login
URL: {{baseUrl}}/login

Headers:
  Content-Type: application/json

Body (JSON):
{
  "username": "admin",
  "password": "e10adc3949ba59abbe56e057f20f883e"
}
```

#### 后置脚本（自动保存Token）

```javascript
// 解析响应JSON
const response = pm.response.json();

// 测试响应状态
pm.test("登录成功", function () {
    pm.expect(response.code).to.eql("200");
});

// 保存Token到环境变量
if (response.code === "200") {
    // 保存普通用户Token
    pm.environment.set("token", response.data.token);
    pm.environment.set("userId", response.data.id);
    
    // 如果是管理员，同时保存adminToken
    if (response.data.role === "admin") {
        pm.environment.set("adminToken", response.data.token);
    }
    
    console.log("✅ Token已保存:", response.data.token);
    console.log("✅ 用户ID:", response.data.id);
    console.log("✅ 用户角色:", response.data.role);
}
```

#### 测试步骤

```
1. 选择"开发环境"
2. 填写username和password
3. 点击"发送"
4. 查看响应结果
5. 检查环境变量是否已保存Token
```

#### 预期响应

```json
{
  "code": "200",
  "msg": null,
  "data": {
    "id": 1,
    "username": "admin",
    "nickname": "管理员",
    "role": "admin",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

---

### 2. 商品列表测试

#### 接口配置

```
接口：GET /api/good/page
URL: {{baseUrl}}/api/good/page?pageNum=1&pageSize=10&name=&categoryId=

Headers:
  Content-Type: application/json
  token: {{token}}

查询参数：
├── pageNum: 1
├── pageSize: 10
├── name: （可选，搜索关键词）
└── categoryId: （可选，分类ID）
```

#### 后置脚本

```javascript
const response = pm.response.json();

pm.test("获取商品列表成功", function () {
    pm.expect(response.code).to.eql("200");
    pm.expect(response.data).to.have.property("records");
    pm.expect(response.data).to.have.property("total");
});

console.log("✅ 商品总数:", response.data.total);
console.log("✅ 当前页商品数:", response.data.records.length);
```

#### 测试场景

**场景1：查询所有商品**
```
pageNum=1&pageSize=10
```

**场景2：搜索商品**
```
pageNum=1&pageSize=10&name=初音
```

**场景3：分类筛选**
```
pageNum=1&pageSize=10&categoryId=1
```

---

### 3. 添加购物车测试

#### 接口配置

```
接口：POST /api/cart
URL: {{baseUrl}}/api/cart

Headers:
  Content-Type: application/json
  token: {{token}}

Body (JSON):
{
  "goodId": 1,
  "count": 2,
  "standard": "M码,蓝色"
}
```

#### 后置脚本

```javascript
const response = pm.response.json();

pm.test("添加到购物车成功", function () {
    pm.expect(response.code).to.eql("200");
});

console.log("✅ 商品已添加到购物车");
```

---

### 4. 创建订单测试

#### 接口配置

```
接口：POST /api/order
URL: {{baseUrl}}/api/order

Headers:
  Content-Type: application/json
  token: {{token}}

Body (JSON):
{
  "totalPrice": 199.98,
  "linkUser": "张三",
  "linkPhone": "13800138000",
  "linkAddress": "北京市朝阳区XXX街道"
}
```

#### 后置脚本

```javascript
const response = pm.response.json();

pm.test("创建订单成功", function () {
    pm.expect(response.code).to.eql("200");
});

// 保存订单号供后续测试使用
if (response.code === "200") {
    pm.environment.set("orderNo", response.data);
    console.log("✅ 订单号:", response.data);
}
```

---

### 5. 管理员功能测试

#### 用户管理（分页查询）

```
接口：GET /api/user/page
URL: {{baseUrl}}/user/page?pageNum=1&pageSize=10&username=

Headers:
  token: {{adminToken}}  // 注意使用管理员Token

查询参数：
├── pageNum: 1
├── pageSize: 10
└── username: （可选，搜索关键词）
```

#### 商品管理（新增商品）

```
接口：POST /api/good
URL: {{baseUrl}}/api/good

Headers:
  Content-Type: application/json
  token: {{adminToken}}

Body (JSON):
{
  "name": "测试商品",
  "description": "商品描述",
  "discount": 1.0,
  "categoryId": 1,
  "recommend": false,
  "status": 1
}
```

#### 订单发货

```
接口：POST /api/order/deliver/{orderNo}
URL: {{baseUrl}}/api/order/deliver/{{orderNo}}

Headers:
  token: {{adminToken}}
```

#### 收入统计

```
接口：GET /api/income/chart
URL: {{baseUrl}}/api/income/chart

Headers:
  token: {{adminToken}}
```

---

## 🤖 自动化测试

### 1. 创建测试场景

#### 场景1：完整购物流程

```
测试场景名称：用户完整购物流程

步骤：
1️⃣ 用户登录
2️⃣ 浏览商品列表
3️⃣ 查看商品详情
4️⃣ 添加到购物车
5️⃣ 查看购物车
6️⃣ 创建订单
7️⃣ 支付订单
8️⃣ 查看订单列表
9️⃣ 确认收货

执行方式：
- 点击"测试场景"
- 添加上述9个接口
- 设置执行顺序
- 点击"运行"
```

#### 场景2：管理员操作流程

```
测试场景名称：管理员日常操作

步骤：
1️⃣ 管理员登录
2️⃣ 查看用户列表
3️⃣ 查看订单列表
4️⃣ 订单发货
5️⃣ 查看收入统计
6️⃣ 新增商品
```

### 2. 批量测试

```
1. 选择接口分组
2. 右键 → "批量运行"
3. 选择环境
4. 设置迭代次数
5. 点击"运行"
6. 查看测试报告
```

### 3. 定时任务（高级版）

```
1. 点击"自动化测试"
2. 创建定时任务
3. 选择测试场景
4. 设置执行时间（如每天9:00）
5. 设置通知方式（邮件/钉钉/企业微信）
```

---

## 🎭 Mock数据

### 1. 启用Mock服务

```
1. 项目设置 → Mock设置
2. 开启Mock服务
3. 获取Mock地址：
   https://mock.apifox.cn/m1/xxxxx
```

### 2. 配置Mock规则

#### 商品列表Mock

```
接口：GET /api/good/page

Mock响应：
{
  "code": "200",
  "msg": null,
  "data": {
    "records": [
      {
        "id": "@integer(1, 100)",
        "name": "@cword(4, 8)服装",
        "description": "@csentence(20, 50)",
        "price": "@float(50, 500, 2, 2)",
        "sales": "@integer(0, 1000)",
        "discount": "@float(0.5, 1, 2, 2)",
        "categoryId": "@integer(1, 5)",
        "imgs": "@image('200x200')",
        "recommend": "@boolean"
      }
    ],
    "total": "@integer(10, 100)"
  }
}
```

### 3. 使用Mock数据

```
前端开发时，将API地址改为Mock地址：

// request.js
const request = axios.create({
    baseURL: 'https://mock.apifox.cn/m1/xxxxx',  // Mock地址
    timeout: 50000
})
```

---

## 📖 API文档生成

### 1. 自动生成文档

```
优势：
✅ 接口测试完成后，文档自动生成
✅ 支持在线分享
✅ 支持导出为多种格式
```

### 2. 导出文档

```
1. 点击"导出"
2. 选择格式：
   - Markdown
   - HTML
   - PDF
   - Word
3. 选择范围：
   - 全部接口
   - 选定分组
4. 点击"导出"
```

### 3. 在线分享

```
1. 点击"分享"
2. 设置权限：
   - 公开/私有
   - 只读/可编辑
3. 生成分享链接
4. 发送给团队成员
```

---

## 👥 团队协作

### 1. 邀请成员

```
1. 项目设置 → 成员管理
2. 点击"邀请成员"
3. 输入邮箱或手机号
4. 设置角色：
   - 管理员
   - 编辑者
   - 只读者
```

### 2. 版本管理

```
1. 所有修改自动保存
2. 支持查看历史记录
3. 支持版本对比
4. 支持回滚到历史版本
```

### 3. 评论功能

```
1. 在接口页面点击"评论"
2. 添加备注或问题
3. @相关人员
4. 跟踪问题解决
```

---

## 📊 完整测试用例库

### 用户认证模块（5个用例）

| 序号 | 用例名称 | 请求方法 | 预期结果 |
|------|---------|---------|---------|
| 1 | 正常登录 | POST /login | 返回200，包含token |
| 2 | 错误密码 | POST /login | 返回403，密码错误 |
| 3 | 用户不存在 | POST /login | 返回403，用户不存在 |
| 4 | 用户注册 | POST /register | 返回200，创建成功 |
| 5 | 重复注册 | POST /register | 返回403，用户名已存在 |

### 商品管理模块（6个用例）

| 序号 | 用例名称 | 请求方法 | 预期结果 |
|------|---------|---------|---------|
| 1 | 商品列表 | GET /api/good/page | 返回200，包含records |
| 2 | 商品搜索 | GET /api/good/page?name=xx | 返回匹配商品 |
| 3 | 商品详情 | GET /api/good/detail/1 | 返回200，包含商品信息 |
| 4 | 推荐商品 | GET /api/good/recommend/1 | 返回推荐列表 |
| 5 | 分类筛选 | GET /api/good/page?categoryId=1 | 返回分类商品 |
| 6 | 热销排行 | GET /api/good/rank?num=10 | 返回Top10商品 |

### 购物车模块（5个用例）

| 序号 | 用例名称 | 请求方法 | 预期结果 |
|------|---------|---------|---------|
| 1 | 添加商品 | POST /api/cart | 返回200，添加成功 |
| 2 | 查看购物车 | GET /api/cart/userid/1 | 返回购物车列表 |
| 3 | 修改数量 | PUT /api/cart | 返回200，更新成功 |
| 4 | 删除商品 | DELETE /api/cart/1 | 返回200，删除成功 |
| 5 | 清空购物车 | DELETE /api/cart/clear | 返回200，清空成功 |

### 订单管理模块（7个用例）

| 序号 | 用例名称 | 请求方法 | 预期结果 |
|------|---------|---------|---------|
| 1 | 创建订单 | POST /api/order | 返回200，返回订单号 |
| 2 | 查询订单 | GET /api/order/userid/1 | 返回订单列表 |
| 3 | 订单详情 | GET /api/order/orderNo/xxx | 返回订单详情 |
| 4 | 支付订单 | POST /api/order/paid/xxx | 返回200，支付成功 |
| 5 | 确认收货 | POST /api/order/receive/xxx | 返回200，状态变更 |
| 6 | 取消订单 | POST /api/order/cancel/xxx | 返回200，取消成功 |
| 7 | 删除订单 | DELETE /api/order/xxx | 返回200，删除成功 |

### 管理员模块（8个用例）

| 序号 | 用例名称 | 请求方法 | 预期结果 |
|------|---------|---------|---------|
| 1 | 用户列表 | GET /user/page | 返回用户分页数据 |
| 2 | 删除用户 | DELETE /user/1 | 返回200，删除成功 |
| 3 | 重置密码 | POST /user/resetPassword | 返回200，重置成功 |
| 4 | 新增商品 | POST /api/good | 返回200，创建成功 |
| 5 | 订单发货 | POST /api/order/deliver/xxx | 返回200，发货成功 |
| 6 | 订单导出 | GET /api/order/export | 返回CSV文件 |
| 7 | 收入统计 | GET /api/income/chart | 返回分类收入 |
| 8 | 商品排行 | GET /api/good/rank | 返回销量排行 |

---

## 🐛 常见问题

### Q1: Token无效怎么办？

**解决**：
```
1. 检查环境变量中是否有token
2. 重新登录获取新token
3. 检查token是否正确传递
```

### Q2: 如何批量修改接口的baseUrl？

**解决**：
```
1. 使用环境变量
2. 修改环境配置中的baseUrl
3. 所有使用{{baseUrl}}的接口自动更新
```

### Q3: 如何导出测试报告？

**解决**：
```
1. 运行测试场景
2. 点击"导出报告"
3. 选择格式（HTML/PDF）
4. 下载报告
```

### Q4: 如何模拟慢网络？

**解决**：
```
1. 项目设置 → Mock设置
2. 设置延迟时间（如2000ms）
3. 所有Mock请求将延迟响应
```

---

## 💡 Apifox使用技巧

### 技巧1：使用变量传递数据

```javascript
// 在接口A的后置脚本中保存数据
pm.environment.set("orderId", response.data.id);

// 在接口B中直接使用
URL: {{baseUrl}}/api/order/{{orderId}}
```

### 技巧2：使用前置脚本生成数据

```javascript
// 生成随机用户名
const randomName = 'user_' + Math.random().toString(36).slice(2, 8);
pm.environment.set("randomUsername", randomName);

// 生成当前时间
const now = new Date().toISOString();
pm.environment.set("currentTime", now);
```

### 技巧3：使用断言验证响应

```javascript
// 验证状态码
pm.test("状态码200", () => {
    pm.response.to.have.status(200);
});

// 验证响应时间 < 500ms
pm.test("响应时间 < 500ms", () => {
    pm.expect(pm.response.responseTime).to.be.below(500);
});

// 验证返回数据
pm.test("返回用户信息", () => {
    const json = pm.response.json();
    pm.expect(json.data).to.have.property("username");
    pm.expect(json.data).to.have.property("token");
});
```

### 技巧4：数据驱动测试

```
1. 准备CSV文件：
username,password,expectedCode
admin,e10adc...,200
user1,e10adc...,200
wrong,123456,403

2. 导入数据文件
3. 设置迭代次数
4. 自动遍历所有测试数据
```

---

## 📈 测试报告示例

```
测试报告：Cosplay商城API测试
测试时间：2026-04-15 14:30:00
测试环境：开发环境

测试结果汇总：
├── 总用例数：31
├── 通过：29
├── 失败：2
└── 通过率：93.5%

失败用例：
❌ 创建订单 - 库存不足
   接口：POST /api/order
   错误：{"code":"500","msg":"库存不足"}

❌ 文件上传 - 文件过大
   接口：POST /file/upload
   错误：请求超时

性能统计：
├── 平均响应时间：245ms
├── 最快接口：85ms（商品列表）
└── 最慢接口：1200ms（收入统计）
```

---

## 🎯 快速上手（5分钟教程）

### 第1步：创建项目（1分钟）
```
新建项目 → 填写名称 → 设置baseUrl
```

### 第2步：配置环境（1分钟）
```
环境管理 → 添加baseUrl、token变量
```

### 第3步：创建登录接口（1分钟）
```
新建接口 → 填写POST /login → 添加Body → 发送
```

### 第4步：保存Token（1分钟）
```
后置脚本 → 添加保存token代码 → 再次发送
```

### 第5步：测试其他接口（1分钟）
```
创建商品列表接口 → 使用{{token}} → 发送 → 查看结果
```

---

## 📚 相关资源

- **Apifox官网**：https://www.apifox.cn/
- **官方文档**：https://www.apifox.cn/help/
- **视频教程**：https://www.apifox.cn/help/video/
- **示例项目**：https://www.apifox.cn/api-specification-sample/

---

**文档生成时间**：2026-04-15  
**系统版本**：v3.0（Vue 3 + Spring Boot）  
**测试工具**：Apifox v2.0+  
**适用场景**：毕业设计、项目测试、API文档
