# Cosplay Mall (U次元) 项目问题检测报告

## Context

对 Cosplay 商城系统进行全面的代码审查和问题检测。项目为 Spring Boot 2.5.6 + Vue.js 2.6.14 的全栈电商系统，包含后端 (`mall-server`) 和前端 (`mall-web`) 两个模块。本报告仅输出检测结果，不做任何代码修改。

---

## 严重级别说明

| 级别 | 含义 |
|------|------|
| CRITICAL | 可被直接利用，导致数据泄露、系统入侵或认证绕过 |
| HIGH | 严重漏洞，可导致未授权访问或重大业务影响 |
| MEDIUM | 纵深防御弱点，需配合其他问题才能利用 |
| LOW | 代码质量或轻微安全卫生问题 |

---

## 一、安全漏洞 (11项)

### CRITICAL-SEC-01: JWT Token 使用用户名作为签名密钥

- **文件**: `mall-server/src/main/java/com/rabbiter/em/utils/TokenUtils.java` 第29行
- **文件**: `mall-server/src/main/java/com/rabbiter/em/interceptor/JwtInterceptor.java` 第80行
- **问题**: `Algorithm.HMAC256(username)` — 用户名是公开信息，任何人知道用户名就能伪造该用户的 JWT Token
- **影响**: 完全的认证绕过。攻击者只需构造 `JWT.create().withAudience(userId).sign(Algorithm.HMAC256(username))` 即可冒充任意用户

### CRITICAL-SEC-02: 密码以无盐 MD5 哈希存储

- **文件**: `mall-web/src/views/Login.vue` 第71行: `form.password = md5(this.user.password)`
- **文件**: `mall-server/src/main/java/com/rabbiter/em/service/UserService.java` 第39-40行
- **问题**: 前端用 MD5 哈希密码后发送，后端直接比对存储的 MD5 值，无盐值
- **影响**: MD5 已被密码学证明不安全，网上有数十亿条 MD5 彩虹表可直接反查。数据库泄露 = 所有密码泄露

### CRITICAL-SEC-03: 订单操作无资源归属权校验

- **文件**: `mall-server/src/main/java/com/rabbiter/em/controller/OrderController.java`
  - 第37行: `selectByUserId` — 任意登录用户可查看任意用户的订单
  - 第152行: `payOrder` — 任意用户可支付任意订单 (且用 GET 请求!)
  - 第177行: `receiveOrder` — 任意用户可确认任意订单收货
  - 第192行: `update` — 任意用户可修改任意订单
  - 第203行: `delete` — 任意用户可删除任意订单
- **问题**: 类级别 `@Authority(AuthorityType.requireLogin)` 只验证是否登录，不验证资源归属
- **影响**: 水平越权。恶意用户可操控所有其他用户的订单

### HIGH-SEC-04: 文件上传接口无需认证

- **文件**: `mall-server/src/main/java/com/rabbiter/em/config/InterceptorConfig.java` 第22行
- **问题**: `.excludePathPatterns("/login","/register","/file/**","/avatar/**")` 将 `/file/**` 和 `/avatar/**` 完全排除在 JWT 验证之外
- **文件**: `mall-server/src/main/java/com/rabbiter/em/controller/FileController.java` 第24-25行
- **影响**: 互联网上任何人无需登录即可上传文件到服务器

### HIGH-SEC-05: 文件上传无类型/内容校验

- **文件**: `mall-server/src/main/java/com/rabbiter/em/controller/FileController.java` 第25行
- **问题**: `upload(@RequestParam MultipartFile file)` 接受任意文件类型，无扩展名白名单、无 MIME 类型验证、无内容检查
- **影响**: 结合 SEC-04，攻击者可无认证上传 `.jsp`、`.html`、`.exe` 等恶意文件，可能导致远程代码执行 (RCE)

### HIGH-SEC-06: 密码重置接口无权限校验 (且用 GET 请求)

- **文件**: `mall-server/src/main/java/com/rabbiter/em/controller/UserController.java` (resetPassword 端点)
- **文件**: `mall-server/src/main/java/com/rabbiter/em/service/UserService.java` 第151-158行
- **问题**: resetPassword 使用 GET 请求 (新密码出现在 URL 中!)，无 `@Authority` 注解，不验证调用者是账户本人还是管理员
- **影响**: 任意登录用户可通过提供 userId 重置任意用户的密码。密码会记录在服务器日志、浏览器历史和代理日志中

### HIGH-SEC-07: 数据库凭据硬编码

- **文件**: `mall-server/src/main/resources/application.yml` 第7-8行
- **问题**: `username: root`, `password: 123456`，未使用环境变量或密钥管理
- **影响**: 源码泄露即数据库沦陷，且使用 root 用户拥有完整数据库管理权限

### MEDIUM-SEC-08: CORS 完全开放且携带凭据

- **文件**: `mall-server/src/main/java/com/rabbiter/em/config/CorsConfig.java` 第19-24行
- **问题**: `allowedOriginPatterns("*")` + `allowCredentials(true)` + `exposedHeaders(SET_COOKIE)`
- **影响**: 任何网站都可以发起带凭据的跨域请求到此 API

### MEDIUM-SEC-09: 用户数据(含 Token)存储在 localStorage

- **文件**: `mall-web/src/views/Login.vue` 第81行
- **文件**: `mall-web/src/utils/request.js` 第15-17行
- **问题**: 完整的 UserDTO (含 JWT Token) 存入 localStorage，任何 XSS 漏洞即可窃取 Token
- **影响**: localStorage 无法设置 HttpOnly 标志，对 XSS 完全不设防

### MEDIUM-SEC-10: GET 请求用于状态变更操作

- **文件**: `mall-server/src/main/java/com/rabbiter/em/controller/OrderController.java`
  - 第152行: `@GetMapping("/paid/{orderNo}")` — 支付订单
  - 第165行: `@GetMapping("/delivery/{orderNo}")` — 发货
  - 第176行: `@GetMapping("/received/{orderNo}")` — 确认收货
- **问题**: GET 请求可被浏览器预加载、`<img>` 标签、搜索引擎爬虫等无意触发
- **影响**: 订单可能被意外支付或状态变更

### LOW-SEC-11: 登录页面存在开放重定向风险

- **文件**: `mall-web/src/views/Login.vue` 第58行、第80行
- **问题**: `this.to = this.$route.query.to` 未验证重定向目标 URL
- **影响**: 攻击者可构造钓鱼链接 `login?to=https://evil.com` 在登录后重定向用户

---

## 二、架构问题 (4项)

### HIGH-ARCH-01: 重复的 GlobalExceptionHandler 类

- **文件**: `mall-server/src/main/java/com/rabbiter/em/config/GlobalExceptionHandler.java` — 当前生效
- **文件**: `mall-server/src/main/java/com/rabbiter/em/exception/GlobalExceptionHandler.java` — 同名类
- **问题**: 两个同名类在不同包中，维护混乱。若 exception 包中的也启用 `@ControllerAdvice`，行为不可预测

### HIGH-ARCH-02: 异常信息混淆导致无法调试

- **文件**: `mall-server/src/main/java/com/rabbiter/em/config/GlobalExceptionHandler.java` 第28-44行
- **问题**: 将数据库/基础设施错误替换为无意义的字母代码:
  - MySQL 认证失败 -> "PU Request failed"
  - 缺少表 -> "T Request failed"
  - Redis 错误 -> "R Request failed" (通过检测子串 "edis" 判断，第38行)
  - SQL 语法错误 -> "S Request failed"
- **影响**: 生产环境出问题时几乎无法调试。"edis" 子串匹配极其脆弱

### MEDIUM-ARCH-03: GoodService 存在严重的 N+1 查询问题

- **文件**: `mall-server/src/main/java/com/rabbiter/em/service/GoodService.java`
  - `findPage()` 第205-211行: 每个商品额外查 2 次 DB (getMinPrice + getTotalStore)
  - `findFullPage()` 第241-247行: 同样问题
  - `findFrontGoods()` 第101-103行: 同样问题
- **问题**: pageSize=10 时，1 + 10x2 = 21 次查询/请求；pageSize=20 时 41 次查询
- **影响**: 数据库负载和响应时间随流量线性增长

### MEDIUM-ARCH-04: 前端路由守卫中发起异步请求存在竞态条件

- **文件**: `mall-web/src/router/index.js` 第89-143行
- **问题**: `router.beforeEach` 中对管理员页面发起 `request.post("http://localhost:9191/role")` 异步请求，URL 硬编码为 localhost
- **影响**: 网络慢时页面可能短暂闪现未授权内容

---

## 三、业务逻辑问题 (4项)

### CRITICAL-BIZ-01: 多种资源类型存在水平越权

除订单外 (SEC-03)，以下控制器同样缺少归属权校验:
- **CartController**: `selectByUserId` — 任意用户可查看任意用户的购物车; `findAll` — 返回所有用户的购物车
- **AddressController**: `findAllById` — 任意用户可查看任意用户的地址 (含姓名、电话、住址等隐私); `findAll` — 返回所有用户地址; `delete` — 可删除任意地址
- **UserController**: `getUserInfoByName` — 可查询任意用户完整资料; `findAll` — 返回所有用户记录 (含密码哈希)
- **影响**: 任意认证用户可访问、修改、删除其他用户的所有资源

### HIGH-BIZ-02: 用户注册接口返回完整 User 对象 (含密码哈希)

- **文件**: `mall-server/src/main/java/com/rabbiter/em/service/UserService.java` 第90行
- **问题**: `return user;` 返回完整实体，包含 password 字段
- **影响**: 每次注册响应中泄露密码哈希

### MEDIUM-BIZ-03: 订单号生成可预测

- **文件**: `mall-server/src/main/java/com/rabbiter/em/service/OrderService.java`
- **问题**: 订单号 = `yyyyMMddHHmmss` + 6位随机数字，时间戳部分已知，6位数字仅 100 万种可能
- **影响**: 结合 SEC-03 的越权漏洞，攻击者可暴力枚举有效订单号

### MEDIUM-BIZ-04: saveStandard 先删后插无事务保护

- **文件**: `mall-server/src/main/java/com/rabbiter/em/controller/GoodController.java` 第128-139行
- **问题**: 先 `deleteAll(goodId)` 删除所有规格，再逐条插入。无 `@Transactional` 注解
- **影响**: 插入中途失败会导致规格数据部分丢失

---

## 四、依赖漏洞 (5项)

### CRITICAL-DEP-01: FastJSON 1.2.73 存在已知 RCE 漏洞

- **文件**: `mall-server/pom.xml` 第88行
- **问题**: FastJSON 1.2.83 之前的版本存在多个反序列化 RCE 漏洞 (CVE-2022-25845 等)
- **使用位置**: `OrderService` 中 `JSON.parseArray(goods, OrderItem.class)` 反序列化用户提交的数据
- **影响**: 潜在的远程代码执行

### HIGH-DEP-02: Spring Boot 2.5.6 已停止维护

- **文件**: `mall-server/pom.xml` 第8行
- **问题**: Spring Boot 2.5.x 已结束商业支持，不再发布安全补丁

### HIGH-DEP-03: Vue 2.x 已停止维护 (2023年12月31日)

- **文件**: `mall-web/package.json`
- **问题**: Vue 2 已于 2023 年底 EOL，不再接收安全更新

### MEDIUM-DEP-04: Swagger 2.9.2 已弃用

- **文件**: `mall-server/pom.xml` 第61-69行
- **问题**: springfox-swagger 与 Spring Boot 2.5+ 存在兼容性问题，应迁移至 springdoc-openapi

### MEDIUM-DEP-05: java-jwt 3.10.3 版本过旧

- **文件**: `mall-server/pom.xml` 第74行
- **问题**: 4.x 版本包含安全改进和 Bug 修复

---

## 五、代码质量问题 (4项)

### MEDIUM-CQ-01: 生产代码中残留 System.out.println

- `mall-server/src/main/java/com/rabbiter/em/service/GoodService.java` 第127行: `System.out.println(good)`
- `mall-server/src/main/java/com/rabbiter/em/config/GlobalExceptionHandler.java` 第26行: `e.printStackTrace()`
- **影响**: 无结构化日志，可能泄露敏感信息

### MEDIUM-CQ-02: 分页接口无上限限制

- `OrderController.java` 第71-73行、`UserController.java`、`GoodController.java`
- **问题**: `pageSize` 参数无最大值限制，用户可请求 `pageSize=999999` 一次性拉取全部数据
- **影响**: DoS 风险和数据泄露

### LOW-CQ-03: 前端 console.log 残留

- `mall-web/src/views/Login.vue` 第87行: `console.log(e)`
- `mall-web/src/utils/request.js` 第50行: `console.log('err' + error)`

### LOW-CQ-04: 全部接口无速率限制

- **问题**: 后端未发现任何速率限制中间件或注解
- **影响**: `/login`、`/register`、`/user/resetPassword` 等接口容易被暴力破解

---

## 六、配置问题 (3项)

### HIGH-CFG-01: Redis 未设置密码

- **文件**: `mall-server/src/main/resources/application.yml` 第12-22行
- **问题**: Redis 配置无 `spring.redis.password`，任何能访问 6379 端口的进程可读写全部缓存数据 (含用户 Token 和完整 User 对象)

### MEDIUM-CFG-02: MySQL 连接启用 allowPublicKeyRetrieval

- **文件**: `mall-server/src/main/resources/application.yml` 第6行
- **问题**: `allowPublicKeyRetrieval=true` 在非 TLS 连接中有中间人攻击风险

### MEDIUM-CFG-03: 前端硬编码后端 URL 为 localhost

- `mall-web/src/utils/request.js` 第6行: `baseURL: 'http://localhost:9191'`
- `mall-web/src/router/index.js` 第100行: `request.post("http://localhost:9191/role")`
- **问题**: 使用 `http://` 明文传输 (含 Token)，且无法直接部署到其他环境

---

## 问题统计

| 严重级别 | 数量 | 问题编号 |
|---------|------|---------|
| CRITICAL | 5 | SEC-01, SEC-02, SEC-03, BIZ-01, DEP-01 |
| HIGH | 9 | SEC-04, SEC-05, SEC-06, SEC-07, ARCH-01, ARCH-02, BIZ-02, DEP-02, DEP-03, CFG-01 |
| MEDIUM | 10 | SEC-08, SEC-09, SEC-10, ARCH-03, ARCH-04, BIZ-03, BIZ-04, DEP-04, DEP-05, CQ-01, CQ-02, CFG-02, CFG-03 |
| LOW | 3 | SEC-11, CQ-03, CQ-04 |
| **总计** | **27** | |

---

## 建议修复优先级

### 紧急修复 (应立即处理)
1. **SEC-01**: 将 JWT 签名密钥从用户名改为强随机服务端密钥
2. **SEC-03 / BIZ-01**: 所有 Controller 端点添加资源归属权校验
3. **SEC-06**: resetPassword 改为 POST 并添加权限校验
4. **SEC-04 + SEC-05**: 文件上传添加认证 + 文件类型白名单
5. **DEP-01**: 升级 FastJSON 至 2.0.x 或切换为 Jackson

### 重要修复 (应尽快处理)
6. **SEC-02**: 密码哈希从 MD5 迁移至 bcrypt (需数据迁移)
7. **SEC-07**: 数据库凭据外部化为环境变量
8. **CFG-01**: 为 Redis 设置密码
9. **SEC-10**: 状态变更接口从 GET 改为 POST/PUT/DELETE
10. **BIZ-02**: 注册接口返回 UserDTO 而非完整 User 实体

### 常规修复 (计划内处理)
11. **ARCH-01**: 删除重复的 GlobalExceptionHandler
12. **ARCH-02**: 重写异常处理逻辑，使用结构化日志
13. **ARCH-03**: 修复 N+1 查询 (批量查询或 JOIN)
14. **CQ-01/CQ-02**: 替换 println、添加分页上限

### 长期规划
15. **DEP-02**: 升级 Spring Boot 至 3.x (需 Java 17)
16. **DEP-03**: 迁移 Vue 2 至 Vue 3
17. **DEP-04**: Swagger 迁移至 springdoc-openapi
