# Apifox JSON文件导入与自动化测试指南

> 文档版本：v1.0  
> 更新日期：2026-04-15  
> 导入文件：apifox_cosplay_mall_api.json

---

## 📦 导入文件说明

**文件名**：`apifox_cosplay_mall_api.json`

**包含内容**：
- ✅ 6个接口分组（31个API接口）
- ✅ 1个环境配置（开发环境）
- ✅ 7个环境变量
- ✅ 2个自动化测试场景
- ✅ 完整的后置脚本（自动保存Token、断言验证）

---

## 🚀 快速导入（7步完成）

### 第1步：打开Apifox

```
启动Apifox桌面客户端
或访问 https://apifox.com/
```

### 第2步：创建项目

```
1. 点击"新建项目"
2. 选择"手动创建"
3. 填写：
   - 项目名称：Cosplay商城API
   - 项目描述：Cosplay服装电商系统API接口
   - 基础URL：http://localhost:9191
4. 点击"创建"
```

### 第3步：导入JSON文件

```
1. 在项目页面，点击顶部"导入"按钮
2. 选择导入方式：
   ✅ 数据导入
3. 选择文件格式：
   ✅ Apifox JSON 格式
4. 点击"上传文件"
5. 选择：apifox_cosplay_mall_api.json
```

### 第4步：选择导入模式

```
导入模式选择：
✅ 普通导入（推荐）- 仅导入，不覆盖现有数据
❌ 同步导入 - 会覆盖现有数据（首次导入可用）

点击"下一步"
```

### 第5步：预览并确认

```
检查导入预览：
✅ 接口分组：6个
✅ API接口：31个
✅ 环境配置：1个
✅ 测试场景：2个

确认无误后，点击"确定导入"
```

### 第6步：配置环境

```
1. 切换到左上角环境选择器
2. 选择"开发环境"
3. 检查环境变量：
   ✅ baseUrl: http://localhost:9191
   ✅ token: （空，登录后自动填充）
   ✅ userId: （空，登录后自动填充）
   ✅ adminToken: （空，登录后自动填充）
```

### 第7步：开始测试

```
✅ 导入完成！
现在可以开始测试接口了
```

---

## 🧪 自动化测试使用

### 方式1：运行单个接口

```
1. 在左侧接口列表中找到接口
   例如：01-用户认证 → 用户登录

2. 检查请求参数：
   - URL: {{baseUrl}}/login
   - Method: POST
   - Body: {"username":"admin","password":"e10adc..."}

3. 点击"发送"按钮

4. 查看响应结果：
   ✅ 状态码：200
   ✅ 响应Body：包含token
   ✅ 控制台：显示保存成功信息

5. 检查环境变量：
   - token已自动保存
   - userId已自动保存
```

### 方式2：运行测试场景（推荐）

#### 场景1：完整购物流程

```
执行步骤：
1. 点击左侧"测试场景"
2. 找到"完整购物流程"
3. 点击"运行"
4. 选择环境：开发环境
5. 点击"开始运行"

自动执行9个步骤：
1️⃣ 用户登录 → 自动保存token
2️⃣ 浏览商品 → 使用token查询
3️⃣ 商品详情 → 查看具体商品
4️⃣ 添加购物车 → 使用token添加
5️⃣ 查看购物车 → 确认添加成功
6️⃣ 创建订单 → 自动生成订单号
7️⃣ 支付订单 → 使用订单号支付
8️⃣ 查询订单 → 查看订单列表
9️⃣ 确认收货 → 完成订单

查看测试报告：
✅ 通过/失败统计
✅ 每个步骤的响应时间
✅ 详细的请求和响应数据
```

#### 场景2：管理员日常操作

```
执行步骤：
1. 点击"测试场景"
2. 找到"管理员日常操作"
3. 点击"运行"

自动执行5个步骤：
1️⃣ 管理员登录 → 保存adminToken
2️⃣ 用户列表 → 查看所有用户
3️⃣ 新增商品 → 添加测试商品
4️⃣ 收入统计 → 查看收入图表
5️⃣ 商品排行 → 查看销量排行
```

### 方式3：批量运行分组

```
1. 在左侧接口分组上右键
   例如：右键"02-商品管理"

2. 选择"批量运行"

3. 设置：
   - 环境：开发环境
   - 迭代次数：1
   - 延迟：0ms

4. 点击"运行"

5. 查看结果：
   该分组下所有接口自动运行
   生成批量测试报告
```

---

## 📊 接口清单（31个）

### 01-用户认证（2个）

| 序号 | 接口名称 | 方法 | 路径 | 说明 |
|------|---------|------|------|------|
| 1 | 用户登录 | POST | /login | 获取Token |
| 2 | 用户注册 | POST | /register | 新用户注册 |

### 02-商品管理（4个）

| 序号 | 接口名称 | 方法 | 路径 | 说明 |
|------|---------|------|------|------|
| 3 | 商品列表（分页） | GET | /api/good/page | 分页查询 |
| 4 | 商品搜索 | GET | /api/good/page | 关键词搜索 |
| 5 | 商品详情 | GET | /api/good/detail/1 | 查看详情 |
| 6 | 推荐商品 | GET | /api/good/recommend/{userId} | 个性化推荐 |

### 03-购物车（2个）

| 序号 | 接口名称 | 方法 | 路径 | 说明 |
|------|---------|------|------|------|
| 7 | 添加商品到购物车 | POST | /api/cart | 添加商品 |
| 8 | 查看购物车 | GET | /api/cart/userid/{userId} | 查询购物车 |

### 04-订单管理（4个）

| 序号 | 接口名称 | 方法 | 路径 | 说明 |
|------|---------|------|------|------|
| 9 | 创建订单 | POST | /api/order | 创建订单 |
| 10 | 查询我的订单 | GET | /api/order/userid/{userId} | 订单列表 |
| 11 | 支付订单 | POST | /api/order/paid/{orderNo} | 模拟支付 |
| 12 | 确认收货 | POST | /api/order/receive/{orderNo} | 确认收货 |

### 05-个人中心（2个）

| 序号 | 接口名称 | 方法 | 路径 | 说明 |
|------|---------|------|------|------|
| 13 | 获取用户信息 | GET | /userinfo/{username} | 查看信息 |
| 14 | 更新用户信息 | POST | /user | 修改信息 |

### 06-管理员功能（5个）

| 序号 | 接口名称 | 方法 | 路径 | 说明 |
|------|---------|------|------|------|
| 15 | 用户管理（分页） | GET | /user/page | 用户列表 |
| 16 | 新增商品 | POST | /api/good | 添加商品 |
| 17 | 订单发货 | POST | /api/order/deliver/{orderNo} | 发货 |
| 18 | 收入统计 | GET | /api/income/chart | 收入图表 |
| 19 | 商品销量排行 | GET | /api/good/rank | 销量排行 |

---

## 🔑 环境变量说明

| 变量名 | 初始值 | 自动填充 | 说明 |
|--------|--------|---------|------|
| baseUrl | http://localhost:9191 | 否 | 后端服务地址 |
| token | （空） | ✅ 登录后 | 普通用户Token |
| userId | （空） | ✅ 登录后 | 当前用户ID |
| adminToken | （空） | ✅ 管理员登录后 | 管理员Token |
| orderNo | （空） | ✅ 创建订单后 | 订单编号 |
| username | admin | 否 | 测试用户名 |
| password | e10adc... | 否 | 测试密码（MD5） |

---

## 📝 后置脚本说明

每个接口都配置了后置脚本，自动执行以下操作：

### 1. 自动保存数据

```javascript
// 用户登录接口
if (response.code === "200") {
    pm.environment.set("token", response.data.token);
    pm.environment.set("userId", response.data.id);
    console.log("✅ Token已保存");
}

// 创建订单接口
if (response.code === "200") {
    pm.environment.set("orderNo", response.data);
    console.log("✅ 订单号:", response.data);
}
```

### 2. 断言验证

```javascript
// 验证响应状态码
pm.test("登录成功", function () {
    pm.expect(response.code).to.eql("200");
});

// 验证响应数据结构
pm.test("获取商品列表成功", function () {
    pm.expect(response.data).to.have.property("records");
    pm.expect(response.data).to.have.property("total");
});
```

### 3. 日志输出

```javascript
// 控制台输出关键信息
console.log("✅ 商品总数:", response.data.total);
console.log("✅ 当前页商品数:", response.data.records.length);
```

---

## 🎯 测试流程示例

### 完整测试流程（手动）

```
第1步：用户登录
├── 接口：POST /login
├── 参数：username=admin, password=e10adc...
├── 结果：获取token
└── 自动保存：token, userId

第2步：浏览商品
├── 接口：GET /api/good/page
├── 参数：pageNum=1, pageSize=10
├── Headers：token: {{token}}
└── 结果：返回商品列表

第3步：添加购物车
├── 接口：POST /api/cart
├── Body：{goodId: 1, count: 2, standard: "M码"}
├── Headers：token: {{token}}
└── 结果：添加成功

第4步：创建订单
├── 接口：POST /api/order
├── Body：{totalPrice: 199.98, ...}
├── Headers：token: {{token}}
├── 结果：返回订单号
└── 自动保存：orderNo

第5步：支付订单
├── 接口：POST /api/order/paid/{{orderNo}}
├── Headers：token: {{token}}
└── 结果：支付成功
```

### 一键测试流程（自动化）

```
1. 点击"测试场景"
2. 选择"完整购物流程"
3. 点击"运行"
4. 等待自动执行9个步骤
5. 查看测试报告

结果：
✅ 总步骤：9
✅ 通过：9
✅ 失败：0
✅ 总耗时：2.5秒
```

---

## 🐛 常见问题

### Q1: 导入后接口显示404？

**原因**：后端服务未启动

**解决**：
```bash
# 启动后端服务
cd mall-server
mvn spring-boot:run

# 验证服务
curl http://localhost:9191
```

### Q2: 提示Token无效？

**原因**：未先执行登录接口

**解决**：
```
1. 先运行"用户登录"接口
2. 检查环境变量中token是否已保存
3. 再运行其他接口
```

### Q3: 如何修改测试数据？

**解决**：
```
1. 打开接口配置
2. 修改Body或Params
3. 保存后重新发送
```

### Q4: 测试场景运行失败？

**解决**：
```
1. 检查环境变量是否正确
2. 检查后端服务是否运行
3. 查看失败步骤的详细错误信息
4. 单独运行失败的接口调试
```

### Q5: 如何导出测试报告？

**解决**：
```
1. 运行测试场景
2. 点击"导出报告"
3. 选择格式：HTML / PDF
4. 下载报告文件
```

---

## 💡 使用技巧

### 技巧1：快速切换环境

```
左上角环境选择器：
- 开发环境：http://localhost:9191
- 生产环境：https://yourdomain.com
- Mock环境：https://mock.apifox.cn/m1/xxx
```

### 技巧2：使用全局变量

```
项目设置 → 全局变量
添加常用数据：
- appName: Cosplay商城
- version: v3.0
- testPhone: 13800138000
```

### 技巧3：批量修改接口

```
1. 选中多个接口（Ctrl+点击）
2. 右键 → 批量编辑
3. 修改Headers或Params
4. 应用到所有选中接口
```

### 技巧4：数据驱动测试

```
1. 准备CSV文件：
username,password
admin,e10adc...
user1,e10adc...

2. 测试场景 → 数据驱动
3. 导入CSV文件
4. 自动遍历所有数据
```

### 技巧5：设置请求延迟

```
测试场景设置：
- 请求间隔：1000ms（1秒）
- 避免请求过快导致服务器压力
```

---

## 📈 测试报告解读

### 报告内容

```
测试报告：完整购物流程
测试时间：2026-04-15 14:30:00
测试环境：开发环境

执行结果：
├── 总步骤：9
├── 成功：9
├── 失败：0
└── 成功率：100%

性能统计：
├── 总耗时：2500ms
├── 平均耗时：278ms
├── 最快接口：85ms（商品列表）
└── 最慢接口：850ms（创建订单）

详细步骤：
✅ 1. 用户登录 - 245ms
✅ 2. 商品列表 - 85ms
✅ 3. 商品详情 - 120ms
✅ 4. 添加购物车 - 180ms
✅ 5. 查看购物车 - 95ms
✅ 6. 创建订单 - 850ms
✅ 7. 支付订单 - 320ms
✅ 8. 查询订单 - 150ms
✅ 9. 确认收货 - 455ms
```

---

## 🎓 学习资源

- **Apifox官方文档**：https://www.apifox.cn/help/
- **视频教程**：https://www.apifox.cn/help/video/
- **API测试最佳实践**：https://www.apifox.cn/help/api-testing/
- **自动化测试指南**：https://www.apifox.cn/help/automated-testing/

---

## 📞 技术支持

如遇到问题：

1. 检查本文档的"常见问题"部分
2. 查看Apifox官方文档
3. 检查后端服务日志
4. 查看浏览器控制台错误

---

**文档生成时间**：2026-04-15  
**导入文件**：apifox_cosplay_mall_api.json  
**系统版本**：v3.0（Vue 3 + Spring Boot）  
**适用工具**：Apifox v2.0+
