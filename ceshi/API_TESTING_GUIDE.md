# 前端API接口测试指南

> 文档版本：v1.0  
> 更新日期：2026-04-15  
> 适用项目：Cosplay商城系统

---

## 📋 目录

1. [测试环境准备](#测试环境准备)
2. [测试工具推荐](#测试工具推荐)
3. [Postman测试（推荐）](#postman测试推荐)
4. [浏览器测试](#浏览器测试)
5. [自动化测试](#自动化测试)
6. [常见接口测试示例](#常见接口测试示例)
7. [测试 checklist](#测试-checklist)

---

## 🔧 测试环境准备

### 1. 启动后端服务

```bash
# 方式1：使用Maven启动
cd mall-server
mvn spring-boot:run

# 方式2：使用IDEA直接运行
# 右键 MallApplication.java -> Run

# 后端服务地址：http://localhost:9191
```

### 2. 启动前端服务

```bash
# 进入前端目录
cd mall-web

# 安装依赖（首次运行）
npm install

# 启动开发服务器
npm run serve

# 前端服务地址：http://localhost:9194
```

### 3. 确认服务状态

- ✅ 后端：访问 http://localhost:9191
- ✅ 前端：访问 http://localhost:9194
- ✅ MySQL：端口 3309
- ✅ Redis：端口 6379

---

## 🛠️ 测试工具推荐

### 工具对比

| 工具 | 优点 | 适用场景 | 推荐指数 |
|------|------|---------|---------|
| **Postman** | 功能强大、支持集合、自动化 | API测试首选 | ⭐⭐⭐⭐⭐ |
| **Apifox** | 国产、中文、集成文档 | 团队协作 | ⭐⭐⭐⭐⭐ |
| **浏览器DevTools** | 无需安装、实时调试 | 快速测试 | ⭐⭐⭐⭐ |
| **cURL** | 命令行、脚本化 | 自动化测试 | ⭐⭐⭐ |
| **Insomnia** | 轻量、开源 | 简单测试 | ⭐⭐⭐ |

---

## 📮 Postman测试（推荐）

### 1. 安装Postman

- 官网下载：https://www.postman.com/downloads/
- 或使用网页版：https://web.postman.co/

### 2. 创建测试集合

```
Cosplay Mall API
├── 用户认证
│   ├── 用户登录
│   └── 用户注册
├── 商品管理
│   ├── 商品列表
│   ├── 商品详情
│   └── 商品搜索
├── 购物车
│   ├── 添加商品
│   └── 查询购物车
├── 订单管理
│   ├── 创建订单
│   └── 查询订单
└── 个人中心
    ├── 获取用户信息
    └── 更新用户信息
```

### 3. 配置环境变量

在Postman中创建环境变量：

```
Environment: Cosplay Mall
Variables:
  - baseUrl: http://localhost:9191
  - token: {{留空，登录后自动填充}}
  - userId: {{留空，登录后自动填充}}
```

### 4. 测试用户登录

**请求配置**：
```
Method: POST
URL: {{baseUrl}}/login
Headers:
  Content-Type: application/json

Body (raw JSON):
{
  "username": "admin",
  "password": "e10adc3949ba59abbe56e057f20f883e"
}
```

**Tests脚本**（自动保存Token）：
```javascript
// 解析响应
var jsonData = pm.response.json();

// 检查响应码
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

// 检查返回code
pm.test("Response code is 200", function () {
    pm.expect(jsonData.code).to.eql("200");
});

// 保存Token到环境变量
if (jsonData.code === "200") {
    pm.environment.set("token", jsonData.data.token);
    pm.environment.set("userId", jsonData.data.id);
    console.log("Token已保存: " + jsonData.data.token);
}
```

### 5. 测试需要登录的接口

**示例：查询商品列表**

```
Method: GET
URL: {{baseUrl}}/api/good/page?pageNum=1&pageSize=10

Headers:
  Content-Type: application/json
  token: {{token}}  // 自动使用环境变量
```

### 6. 批量测试（Collection Runner）

1. 点击集合名称
2. 点击 "Run collection"
3. 选择环境和迭代次数
4. 点击 "Run" 执行所有测试

---

## 🌐 浏览器测试

### 1. 使用浏览器DevTools

**步骤**：

1. 打开浏览器，访问 http://localhost:9194
2. 按 `F12` 打开开发者工具
3. 切换到 **Network（网络）** 标签
4. 执行前端操作（如登录、搜索商品）
5. 查看API请求和响应

**示例测试流程**：

```javascript
// 在Console中直接测试API

// 1. 测试登录
fetch('http://localhost:9191/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    username: 'admin',
    password: 'e10adc3949ba59abbe56e057f20f883e'
  })
})
.then(res => res.json())
.then(data => {
  console.log('登录响应:', data);
  // 保存token
  localStorage.setItem('user', JSON.stringify(data.data));
});

// 2. 测试商品列表（使用保存的token）
const user = JSON.parse(localStorage.getItem('user'));
fetch('http://localhost:9191/api/good/page?pageNum=1&pageSize=10', {
  headers: {
    'token': user.token
  }
})
.then(res => res.json())
.then(data => console.log('商品列表:', data));
```

### 2. 使用Axios直接测试

在前端项目的Console中：

```javascript
// 导入request（需要先在前端代码中暴露）
import request from '@/utils/request'

// 测试登录
request.post('/login', {
  username: 'admin',
  password: 'e10adc3949ba59abbe56e057f20f883e'
}).then(res => {
  console.log('登录成功:', res);
});

// 测试商品列表
request.get('/api/good/page', {
  params: { pageNum: 1, pageSize: 10 }
}).then(res => {
  console.log('商品列表:', res);
});
```

---

## 🤖 自动化测试

### 1. 使用Postman Newman（命令行）

**安装Newman**：
```bash
npm install -g newman
```

**导出集合**：
- 在Postman中导出Collection为JSON文件
- 导出Environment为JSON文件

**运行测试**：
```bash
newman run "Cosplay_Mall_API.postman_collection.json" \
  -e "Cosplay_Mall_Environment.postman_environment.json" \
  --reporters cli,html \
  --reporter-html-export test-report.html
```

### 2. 使用JavaScript测试脚本

创建测试文件 `test_api.js`：

```javascript
const axios = require('axios');

const BASE_URL = 'http://localhost:9191';
let token = '';

// 测试登录
async function testLogin() {
  try {
    const res = await axios.post(`${BASE_URL}/login`, {
      username: 'admin',
      password: 'e10adc3949ba59abbe56e057f20f883e'
    });
    
    console.log('✅ 登录成功');
    token = res.data.data.token;
    return true;
  } catch (err) {
    console.error('❌ 登录失败:', err.response?.data || err.message);
    return false;
  }
}

// 测试商品列表
async function testGetGoods() {
  try {
    const res = await axios.get(`${BASE_URL}/api/good/page`, {
      params: { pageNum: 1, pageSize: 10 },
      headers: { token }
    });
    
    console.log('✅ 商品列表获取成功, 总数:', res.data.data.total);
    return true;
  } catch (err) {
    console.error('❌ 商品列表获取失败:', err.response?.data || err.message);
    return false;
  }
}

// 测试创建订单
async function testCreateOrder() {
  try {
    const res = await axios.post(`${BASE_URL}/api/order`, {
      totalPrice: 99.99,
      linkUser: '测试用户',
      linkPhone: '13800138000',
      linkAddress: '测试地址'
    }, {
      headers: { token }
    });
    
    console.log('✅ 订单创建成功, 订单号:', res.data.data);
    return true;
  } catch (err) {
    console.error('❌ 订单创建失败:', err.response?.data || err.message);
    return false;
  }
}

// 运行所有测试
async function runTests() {
  console.log('🚀 开始API测试...\n');
  
  const results = {
    login: await testLogin(),
    goods: await testGetGoods(),
    order: await testCreateOrder()
  };
  
  console.log('\n📊 测试结果:');
  console.log('登录:', results.login ? '✅ 通过' : '❌ 失败');
  console.log('商品:', results.goods ? '✅ 通过' : '❌ 失败');
  console.log('订单:', results.order ? '✅ 通过' : '❌ 失败');
  
  const passed = Object.values(results).filter(Boolean).length;
  console.log(`\n总计: ${passed}/${Object.keys(results).length} 通过`);
}

runTests();
```

**运行测试**：
```bash
node test_api.js
```

---

## 📝 常见接口测试示例

### 1. 用户认证模块

#### 1.1 用户登录

```http
POST http://localhost:9191/login
Content-Type: application/json

{
  "username": "admin",
  "password": "e10adc3949ba59abbe56e057f20f883e"
}
```

**预期响应**：
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

#### 1.2 用户注册

```http
POST http://localhost:9191/register
Content-Type: application/json

{
  "username": "testuser",
  "password": "e10adc3949ba59abbe56e057f20f883e",
  "phone": "13800138000"
}
```

---

### 2. 商品管理模块

#### 2.1 商品列表（分页）

```http
GET http://localhost:9191/api/good/page?pageNum=1&pageSize=10
token: {{token}}
```

#### 2.2 商品搜索

```http
GET http://localhost:9191/api/good/page?pageNum=1&pageSize=10&name=初音
token: {{token}}
```

#### 2.3 商品详情

```http
GET http://localhost:9191/api/good/detail/1
token: {{token}}
```

---

### 3. 购物车模块

#### 3.1 添加商品到购物车

```http
POST http://localhost:9191/api/cart
Content-Type: application/json
token: {{token}}

{
  "goodId": 1,
  "count": 2,
  "standard": "M码,蓝色"
}
```

#### 3.2 查询购物车

```http
GET http://localhost:9191/api/cart/userid/{{userId}}
token: {{token}}
```

---

### 4. 订单管理模块

#### 4.1 创建订单

```http
POST http://localhost:9191/api/order
Content-Type: application/json
token: {{token}}

{
  "totalPrice": 199.98,
  "linkUser": "张三",
  "linkPhone": "13800138000",
  "linkAddress": "北京市朝阳区XXX街道"
}
```

#### 4.2 查询我的订单

```http
GET http://localhost:9191/api/order/userid/{{userId}}
token: {{token}}
```

#### 4.3 支付订单

```http
POST http://localhost:9191/api/order/paid/ORD1234567890
token: {{token}}
```

---

### 5. 个人中心模块

#### 5.1 获取用户信息

```http
GET http://localhost:9191/userinfo/admin
token: {{token}}
```

#### 5.2 更新用户信息

```http
POST http://localhost:9191/user
Content-Type: application/json
token: {{token}}

{
  "id": 1,
  "nickname": "新昵称",
  "phone": "13900139000",
  "email": "test@example.com"
}
```

---

### 6. 管理员模块

#### 6.1 用户管理（分页）

```http
GET http://localhost:9191/user/page?pageNum=1&pageSize=10&username=admin
token: {{admin_token}}
```

#### 6.2 商品管理（新增）

```http
POST http://localhost:9191/api/good
Content-Type: application/json
token: {{admin_token}}

{
  "name": "测试商品",
  "description": "商品描述",
  "price": 99.99,
  "categoryId": 1,
  "recommend": true
}
```

#### 6.3 订单管理（发货）

```http
POST http://localhost:9191/api/order/deliver/ORD1234567890
token: {{admin_token}}
```

#### 6.4 收入统计

```http
GET http://localhost:9191/api/income/chart
token: {{admin_token}}
```

---

## ✅ 测试 Checklist

### 功能测试

- [ ] **用户认证**
  - [ ] 正常登录
  - [ ] 错误密码登录
  - [ ] 不存在的用户登录
  - [ ] 用户注册
  - [ ] 重复用户名注册

- [ ] **商品管理**
  - [ ] 商品列表分页
  - [ ] 关键词搜索
  - [ ] 分类筛选
  - [ ] 商品详情
  - [ ] 推荐商品

- [ ] **购物车**
  - [ ] 添加商品
  - [ ] 修改数量
  - [ ] 删除商品
  - [ ] 清空购物车
  - [ ] 查看购物车

- [ ] **订单管理**
  - [ ] 创建订单
  - [ ] 查询订单
  - [ ] 支付订单
  - [ ] 确认收货
  - [ ] 取消订单

- [ ] **个人中心**
  - [ ] 查看个人信息
  - [ ] 修改个人信息
  - [ ] 修改密码
  - [ ] 地址管理

- [ ] **管理员功能**
  - [ ] 用户管理
  - [ ] 商品管理
  - [ ] 订单管理
  - [ ] 数据统计
  - [ ] 文件管理

### 权限测试

- [ ] 未登录访问需要登录的接口 → 返回401
- [ ] 普通用户访问管理员接口 → 返回403
- [ ] Token过期访问 → 返回401
- [ ] 错误Token访问 → 返回401

### 性能测试

- [ ] 接口响应时间 < 1秒
- [ ] 并发请求测试
- [ ] 大数据量分页测试
- [ ] 文件上传大小限制

### 安全测试

- [ ] SQL注入防护
- [ ] XSS防护
- [ ] 密码加密存储
- [ ] Token安全传输
- [ ] 文件上传类型限制

---

## 🐛 常见问题排查

### 1. 401 未授权

**原因**：
- Token未传递
- Token过期
- Token格式错误

**解决**：
```javascript
// 检查localStorage中的token
console.log(localStorage.getItem('user'));

// 重新登录获取新token
```

### 2. 403 禁止访问

**原因**：
- 权限不足
- 访问他人资源

**解决**：
```javascript
// 检查用户角色
const user = JSON.parse(localStorage.getItem('user'));
console.log(user.role); // 应该是 'admin'
```

### 3. 跨域错误

**原因**：
- 后端未配置CORS
- 端口不一致

**解决**：
```java
// 后端已配置跨域（CorsConfig.java）
@CrossOrigin
@RestController
public class UserController { }
```

### 4. 网络错误

**原因**：
- 后端服务未启动
- 端口占用
- 防火墙拦截

**解决**：
```bash
# 检查后端服务
curl http://localhost:9191

# 检查端口占用
netstat -ano | findstr 9191

# 重启后端服务
mvn spring-boot:run
```

---

## 📊 测试报告模板

```markdown
# API测试报告

## 测试信息
- 测试日期：2026-04-15
- 测试人员：XXX
- 测试环境：开发环境

## 测试结果

### 通过接口（15个）
✅ POST /login - 用户登录
✅ POST /register - 用户注册
✅ GET /api/good/page - 商品列表
...

### 失败接口（2个）
❌ POST /api/order - 创建订单
   错误信息：库存不足
   严重程度：高

❌ GET /api/income/chart - 收入统计
   错误信息：数据为空
   严重程度：中

## 问题汇总
1. 库存检查逻辑需要完善
2. 收入统计需要初始化数据

## 测试结论
- 总接口数：17
- 通过数：15
- 失败数：2
- 通过率：88.2%
```

---

## 💡 最佳实践

### 1. 使用环境变量
- 不同环境使用不同baseUrl
- Token自动管理

### 2. 编写Tests脚本
- 自动化验证响应
- 自动提取数据

### 3. 组织测试集合
- 按模块分组
- 添加描述和文档

### 4. 持续集成
- 使用Newman集成到CI/CD
- 自动生成测试报告

### 5. 版本控制
- 导出Collection到Git
- 记录接口变更

---

## 📚 相关资源

- **Postman官方文档**：https://learning.postman.com/
- **Axios文档**：https://axios-http.com/
- **Spring Boot测试**：https://spring.io/guides/gs/testing-web/
- **项目API文档**：查看 `.qoder/repowiki/zh/content/API接口文档/`

---

**文档生成时间**：2026-04-15  
**系统版本**：v3.0（Vue 3 + Spring Boot）  
**维护人员**：开发团队
