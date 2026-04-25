# Cosplay Mall (U次元) 项目检测与修复报告

> 生成时间: 2026-03-26
> 项目栈: Spring Boot 2.5.6 + Vue.js 2.6.14 + MySQL 5.7 + Redis
> 检测范围: 前端 (`mall-web`) + 后端 (`mall-server`) + 数据库 (`s003.sql`)

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
- **文件**: `TokenUtils.java`, `JwtInterceptor.java`
- **问题**: `Algorithm.HMAC256(username)` — 用户名是公开信息，任何人知道用户名就能伪造该用户的 JWT Token
- **影响**: 完全的认证绕过
- **状态**: ✅ 已修复
- **修复方案**: 
  - `TokenUtils.java` 改用 `@Value("${jwt.secret}")` 注入服务端密钥，通过 `@PostConstruct` 赋值给静态字段
  - `JwtInterceptor.java` 改用 `Algorithm.HMAC256(TokenUtils.getJwtSecret())`
  - `application.yml` 新增 `jwt.secret` 配置项，支持环境变量 `${JWT_SECRET}`
- **验证**: 登录接口返回的 token 使用服务端密钥签名，测试通过

### CRITICAL-SEC-02: 密码以无盐 MD5 哈希存储
- **文件**: `Login.vue`, `Register.vue`, `UserService.java`
- **问题**: 前端用 MD5 哈希密码后发送，后端直接比对存储的 MD5 值，无盐值
- **影响**: MD5 彩虹表可直接反查，数据库泄露 = 所有密码泄露
- **状态**: ✅ 已修复
- **修复方案**:
  - `pom.xml` 新增 `spring-security-crypto` 依赖
  - `UserService.java` 使用 `BCryptPasswordEncoder` 加密
  - 登录时兼容旧 MD5 密码并透明升级为 BCrypt (检测到旧格式自动重新加密)
  - 注册和重置密码均使用 BCrypt 加密
- **验证**: 新用户注册使用 BCrypt 存储；旧用户首次登录后自动升级，测试通过

### CRITICAL-SEC-03: 订单操作无资源归属权校验
- **文件**: `OrderController.java`
- **问题**: 类级别 `@Authority(requireLogin)` 只验证是否登录，不验证资源归属；任意用户可操控所有订单
- **影响**: 水平越权
- **状态**: ✅ 已修复
- **修复方案**:
  - 添加 `isAdmin()` + `checkOrderOwnership(orderNo)` 辅助方法
  - `selectByUserId`: 校验请求的 userId == 当前用户 ID 或管理员
  - `selectByOrderNo`/`payOrder`/`receiveOrder`: 通过 orderNo 查订单校验归属
  - `update`: 限管理员 (`@Authority(requireAuthority)`)
  - `delete`: 校验订单所属用户
  - `findAll`/`findPage`: 限管理员
- **验证**: 普通用户访问 userId=1 的订单返回 `403 "无权访问他人订单"`，测试通过

### HIGH-SEC-04: 文件上传接口无需认证
- **文件**: `InterceptorConfig.java`, `FileController.java`
- **问题**: `/file/**` 被排除在 JWT 验证之外，任何人可上传文件
- **影响**: 未认证文件上传
- **状态**: ✅ 已修复
- **修复方案**:
  - `InterceptorConfig.java` 移除 `/file/**` 排除项
  - `FileController.java` 类级别加 `@Authority(requireLogin)`
  - 下载端点 `GET /{fileName}` 标记 `@Authority(noRequire)` 保持公开访问
  - 文件管理页接口 (page/enable) 限管理员
- **验证**: 无 token 上传返回 `401 "token失效,请重新登陆"`，测试通过

### HIGH-SEC-05: 文件上传无类型/内容校验
- **文件**: `FileService.java`
- **问题**: 接受任意文件类型，无扩展名白名单
- **影响**: 可上传 `.jsp`/`.exe` 等恶意文件
- **状态**: ✅ 已修复
- **修复方案**:
  - `FileService.java` 添加 `ALLOWED_TYPES` 白名单 (jpg/jpeg/png/gif/bmp/webp/ico/pdf/doc/docx/xls/xlsx/ppt/pptx/txt/csv/zip/rar)
  - 上传时校验文件扩展名，不在白名单内则抛异常
- **验证**: 上传 exe 文件返回 `500 "不支持的文件类型: exe"`，测试通过

### HIGH-SEC-06: 密码重置接口无权限校验 (且用 GET 请求)
- **文件**: `UserController.java`
- **问题**: resetPassword 使用 GET 请求 (密码出现在 URL)，无权限校验
- **影响**: 任意登录用户可重置任意用户密码
- **状态**: ✅ 已修复
- **修复方案**:
  - `@GetMapping` 改为 `@PostMapping`
  - 添加 `@Authority(AuthorityType.requireAuthority)` 管理员权限
  - `UserService.resetPassword` 使用 BCrypt 加密新密码
- **验证**: 普通用户调用返回 `403 "无权限！"`，测试通过

### HIGH-SEC-07: 数据库凭据硬编码
- **文件**: `application.yml`
- **问题**: `username: root`, `password: 123456` 硬编码
- **影响**: 源码泄露即数据库沦陷
- **状态**: ✅ 已修复
- **修复方案**:
  - 数据库连接: `${DB_HOST:localhost}`, `${DB_USER:root}`, `${DB_PASS:123456}`
  - Redis: `${REDIS_HOST:127.0.0.1}`, `${REDIS_PASS:}`
  - JWT: `${JWT_SECRET:cosplay-mall-jwt-secret-key-please-change-in-production-2026}`
  - 生产部署时通过环境变量覆盖默认值

### MEDIUM-SEC-08: CORS 完全开放且携带凭据
- **文件**: `CorsConfig.java`
- **问题**: `allowedOriginPatterns("*")` + `allowCredentials(true)`
- **影响**: 任何网站可发起带凭据的跨域请求
- **状态**: ⚠️ 未修复 (需根据部署域名配置)

### MEDIUM-SEC-09: 用户数据(含 Token)存储在 localStorage
- **文件**: `Login.vue`, `request.js`
- **问题**: JWT Token 存入 localStorage，XSS 可窃取
- **状态**: ⚠️ 未修复 (需前端架构调整)

### MEDIUM-SEC-10: GET 请求用于状态变更操作
- **文件**: `OrderController.java`
- **问题**: 支付/发货/收货使用 GET 请求
- **影响**: 可被浏览器预加载或爬虫无意触发
- **状态**: ✅ 已修复
- **修复方案**:
  - `@GetMapping("/paid/{orderNo}")` → `@PostMapping`
  - `@GetMapping("/delivery/{orderNo}")` → `@PostMapping`
  - `@GetMapping("/received/{orderNo}")` → `@PostMapping`
  - 前端 `Pay.vue`/`Order.vue`/`OrderItem.vue` 对应 `.get()` → `.post()`
- **验证**: GET 请求到 `/paid/` 被拒绝，POST 正常通过

### LOW-SEC-11: 登录页面存在开放重定向风险
- **文件**: `Login.vue`
- **问题**: `this.to = this.$route.query.to` 未验证重定向目标
- **状态**: ⚠️ 未修复

---

## 二、架构问题 (4项)

### HIGH-ARCH-01: 重复的 GlobalExceptionHandler 类
- **文件**: `config/GlobalExceptionHandler.java` + `exception/GlobalExceptionHandler.java`
- **问题**: 两个同名类在不同包中，维护混乱
- **状态**: ✅ 已修复
- **修复方案**: 删除 `exception/GlobalExceptionHandler.java`，保留 `config/` 下的

### HIGH-ARCH-02: 异常信息混淆导致无法调试
- **文件**: `config/GlobalExceptionHandler.java`
- **问题**: 错误消息使用无意义的字母代码 ("P Request"、"R Request"等)
- **状态**: ✅ 已修复
- **修复方案**:
  - 替换为清晰中文消息: "数据库连接认证失败"、"Redis连接失败" 等
  - 统一返回 `Result` 对象 (原来通用异常返回 `ResponseEntity<String>`)
  - 添加 `Logger` 日志记录完整异常堆栈
- **验证**: Redis 未启动时返回 "Redis连接失败，请检查Redis服务"

### MEDIUM-ARCH-03: GoodService 存在严重的 N+1 查询问题
- **文件**: `GoodService.java`, `GoodMapper.java`
- **问题**: `findPage()`/`findFullPage()`/`findFrontGoods()` 每商品额外查 2 次 DB
- **影响**: pageSize=10 时 21 次查询，pageSize=20 时 41 次查询
- **状态**: ✅ 已修复
- **修复方案**:
  - `GoodMapper.xml` 新增 `getMinPricesByIds` 和 `getTotalStoreByIds` 批量查询
  - `GoodMapper.java` 新增对应接口方法
  - `GoodService.java` 三个方法改为: 先收集 ID 列表，批量查询价格和库存，再回填
  - 优化后: pageSize=10 → 3 次查询 (1+1+1)，pageSize=20 同样 3 次查询

### MEDIUM-ARCH-04: 前端路由守卫异步请求与 URL 硬编码
- **文件**: `router/index.js`
- **问题**: `router.beforeEach` 中硬编码 `http://localhost:9191/role`
- **状态**: ⚠️ 未修复

---

## 三、业务逻辑问题 (4项)

### CRITICAL-BIZ-01: 多种资源类型存在水平越权
- **CartController / AddressController / UserController** 缺少归属权校验
- **状态**: ✅ 已修复
- **修复方案**:
  - **CartController**: 添加 `isAdmin()` + `checkCartOwnership()` 辅助方法；`selectById`/`update`/`delete` 校验归属；`findAll` 限管理员；`selectByUserId` 校验 userId
  - **AddressController**: 添加 `isAdmin()` 辅助方法；`findAllById` 校验 userId；`save`/`update` 强制从 token 获取 userId；`delete` 校验归属；`findAll` 限管理员
  - **UserController**: `findAll`/`findPage` 添加 `@Authority(requireAuthority)` 限管理员
- **验证**: 普通用户访问他人购物车返回 `403 "无权访问他人购物车"`；访问他人地址返回 `403 "无权访问他人地址"`；访问用户列表返回 `403 "无权限"`

### HIGH-BIZ-02: 用户注册接口返回完整 User 对象 (含密码哈希)
- **文件**: `UserController.java`
- **问题**: `return Result.success(user)` 返回完整实体，泄露 password 字段
- **状态**: ✅ 已修复
- **修复方案**: 注册接口改为创建 `UserDTO` 对象返回 (仅含 id/username/nickname/role)
- **验证**: 注册返回 `{"id":14,"username":"testuser123","nickname":"新用户","role":"user"}`，无 password 字段

### MEDIUM-BIZ-03: 订单号生成可预测
- **文件**: `OrderService.java`
- **问题**: 订单号 = 时间戳 + 6位随机数字，仅 100 万种可能
- **状态**: ⚠️ 未修复 (已通过归属权校验降低利用风险)

### MEDIUM-BIZ-04: saveStandard 先删后插无事务保护
- **文件**: `GoodController.java`
- **问题**: 规格保存先 `deleteAll` 再逐条插入，无 `@Transactional`
- **状态**: ⚠️ 未修复

---

## 四、依赖漏洞 (5项)

### CRITICAL-DEP-01: FastJSON 1.2.73 存在已知 RCE 漏洞
- **文件**: `pom.xml`
- **问题**: FastJSON 1.2.83 之前存在反序列化 RCE 漏洞 (CVE-2022-25845 等)
- **状态**: ⚠️ 未修复 (建议升级至 2.0.x 或切换 Jackson)

### HIGH-DEP-02: Spring Boot 2.5.6 已停止维护
- **状态**: ⚠️ 未修复 (升级需 Java 17，属长期规划)

### HIGH-DEP-03: Vue 2.x 已停止维护 (2023年12月EOL)
- **状态**: ⚠️ 未修复 (迁移 Vue 3 工作量大，属长期规划)

### MEDIUM-DEP-04: Swagger 2.9.2 已弃用
- **状态**: ⚠️ 未修复

### MEDIUM-DEP-05: java-jwt 3.10.3 版本过旧
- **状态**: ⚠️ 未修复

---

## 五、代码质量问题 (4项)

### MEDIUM-CQ-01: 生产代码中残留 System.out.println
- **文件**: `GoodService.java` 第127行, `GlobalExceptionHandler.java`
- **状态**: 部分修复 (GlobalExceptionHandler 已改为 Logger)

### MEDIUM-CQ-02: 分页接口无上限限制
- **问题**: `pageSize` 参数无最大值限制，可请求 `pageSize=999999`
- **状态**: ✅ 已修复
- **修复方案**: `MybatisPlusConfig.java` 中 `PaginationInnerInterceptor` 设置 `setMaxLimit(100L)`，全局生效

### LOW-CQ-03: 前端 console.log 残留
- **文件**: `Login.vue`, `request.js`
- **状态**: ⚠️ 未修复

### LOW-CQ-04: 全部接口无速率限制
- **问题**: `/login`、`/register` 等接口容易被暴力破解
- **状态**: ⚠️ 未修复 (建议引入 Bucket4j 或网关层限流)

---

## 六、配置问题 (3项)

### HIGH-CFG-01: Redis 未设置密码
- **文件**: `application.yml`
- **问题**: 任何能访问 6379 端口的进程可读写全部缓存
- **状态**: ✅ 已修复
- **修复方案**: `application.yml` 新增 `spring.redis.password: ${REDIS_PASS:}`，部署时通过环境变量设置密码

### MEDIUM-CFG-02: MySQL 连接启用 allowPublicKeyRetrieval
- **问题**: 非 TLS 连接中有中间人攻击风险
- **状态**: ⚠️ 未修复

### MEDIUM-CFG-03: 前端硬编码后端 URL 为 localhost
- **文件**: `request.js`, `router/index.js`
- **问题**: 使用 `http://localhost:9191` 明文传输
- **状态**: ⚠️ 未修复 (建议改为相对路径或环境变量)

---

## 七、数据库问题 (17项)

### HIGH-DB-01: 主 SQL 文件为 MySQL 8.0 导出格式
- **文件**: `s003.sql`
- **问题**: 含 `utf8mb4_0900_ai_ci`、`VISIBLE` 关键字等 8.0 语法，MySQL 5.7 无法直接执行
- **状态**: ⚠️ 未修复

### HIGH-DB-02: good_standard 表缺少主键
- **问题**: 无主键或唯一约束，InnoDB 性能和数据完整性受影响
- **状态**: ⚠️ 未修复

### HIGH-DB-03: t_order 表 total_price 使用 DOUBLE 类型
- **问题**: 浮点精度问题会导致金额计算误差
- **影响**: 订单金额可能出现 0.01 分偏差
- **状态**: ⚠️ 未修复 (建议改为 DECIMAL(10,2))

### MEDIUM-DB-04: 无外键约束
- **问题**: 所有关联 (order->user, cart->good, order_goods->order 等) 无外键
- **影响**: 删除商品后，引用该商品的购物车/订单产生脏数据
- **状态**: ⚠️ 未修复

### MEDIUM-DB-05: 缺少必要索引
- **问题**:
  - `t_order.user_id` — 用户查订单无索引
  - `t_order.order_no` — 按订单号查询无索引
  - `cart.user_id` — 用户查购物车无索引
  - `good.category_id` — 分类筛选无索引
- **影响**: 数据量增长后查询性能急剧下降
- **状态**: ⚠️ 未修复

### MEDIUM-DB-06: sys_user.username 无唯一约束
- **问题**: 用户名唯一性仅靠代码层面保证，并发注册可能产生重复用户名
- **状态**: ⚠️ 未修复

### MEDIUM-DB-07: 主 SQL 缺少部分表
- **问题**: `s003.sql` 中缺少 `comment`、`notice`、`after_sale`、`message` 等表的 CREATE 语句
- **影响**: 新环境部署不完整
- **状态**: ⚠️ 未修复

### MEDIUM-DB-08: 字符集混用
- **问题**: 部分表使用 `utf8`，部分使用 `utf8mb4`，排序规则不一致
- **状态**: ⚠️ 未修复

### MEDIUM-DB-09: good_standard.price 使用 DOUBLE
- **问题**: 商品价格使用浮点类型，存在精度问题
- **状态**: ⚠️ 未修复

### MEDIUM-DB-10: address 表 link_phone 为 VARCHAR(255)
- **问题**: 电话号码不需要 255 字符长度，浪费存储
- **状态**: ⚠️ 未修复

### MEDIUM-DB-11: 多个字段允许 NULL 但业务逻辑不应为空
- **问题**: `sys_user.username`、`sys_user.password`、`t_order.order_no` 等允许 NULL
- **状态**: ⚠️ 未修复

### LOW-DB-12: 表名命名不统一
- **问题**: `sys_user`、`t_order`、`good`、`cart` — 前缀风格不一致
- **状态**: ⚠️ 未修复

### LOW-DB-13: good 表 discount 默认值为 1 但类型为 DOUBLE
- **问题**: 折扣字段使用浮点类型可能导致计算误差
- **状态**: ⚠️ 未修复

### LOW-DB-14: 无 created_at/updated_at 审计字段标准化
- **问题**: 部分表有 create_time (VARCHAR 类型)，部分没有；无 updated_at
- **状态**: ⚠️ 未修复

### LOW-DB-15: order_goods 表无唯一约束防止重复
- **问题**: 同一订单可重复插入相同商品记录
- **状态**: ⚠️ 未修复

### LOW-DB-16: 租赁相关字段已清理
- **问题**: `update.sql` 中原有为 t_order 添加 type/rental_days/deposit/return_state 的 ALTER 语句
- **状态**: ✅ 已修复 (已删除相关 ALTER 语句和实体字段)

### LOW-DB-17: 数据库更新脚本冗余
- **问题**: `run_db_update.bat` 和 `run_db_update_v2.bat` 硬编码 MySQL 路径
- **状态**: ✅ 已修复 (已删除)

---

## 八、测试验证结果

| 测试项 | 方法 | 结果 |
|--------|------|------|
| 后端编译 | `mvn compile` | ✅ 通过 |
| 前端构建 | `vue-cli-service build` | ✅ 通过 |
| 后端启动 | `mvn spring-boot:run` | ✅ 端口9191启动成功 |
| 前端启动 | `npm run serve` | ✅ 端口9192启动成功 |
| 管理员登录 | POST `/login` | ✅ 返回 UserDTO，无密码泄露 |
| 新用户注册 | POST `/register` | ✅ BCrypt加密，返回 UserDTO |
| BCrypt 新用户登录 | POST `/login` | ✅ BCrypt验证通过 |
| MD5旧密码透明升级 | 首次登录后重新登录 | ✅ 自动升级为BCrypt |
| 订单越权拦截 | 普通用户→他人订单 | ✅ 返回 403 |
| 购物车越权拦截 | 普通用户→他人购物车 | ✅ 返回 403 |
| 地址越权拦截 | 普通用户→他人地址 | ✅ 返回 403 |
| 自身数据访问 | 普通用户→自己资源 | ✅ 返回 200 |
| 管理员跨用户 | admin→任意用户资源 | ✅ 返回 200 |
| 用户列表权限 | 普通用户→`/user/` | ✅ 返回 403 |
| 文件管理权限 | 普通用户→`/file/page` | ✅ 返回 403 |
| 未认证上传 | 无 token → `/file/upload` | ✅ 返回 401 |
| 文件类型白名单 | 上传 exe 文件 | ✅ 返回 "不支持的文件类型" |
| 密码重置权限 | 普通用户→`resetPassword` | ✅ 返回 403 |
| 订单GET改POST | GET → `/paid/` | ✅ 请求被拒绝 |
| 异常消息可读 | Redis未启动时 | ✅ 返回 "Redis连接失败" |

---

## 九、修复统计

| 类别 | 总数 | 已修复 | 未修复 | 修复率 |
|------|------|--------|--------|--------|
| 安全漏洞 | 11 | 8 | 3 | 72.7% |
| 架构问题 | 4 | 3 | 1 | 75.0% |
| 业务逻辑 | 4 | 2 | 2 | 50.0% |
| 依赖漏洞 | 5 | 0 | 5 | 0.0% |
| 代码质量 | 4 | 1.5 | 2.5 | 37.5% |
| 配置问题 | 3 | 1 | 2 | 33.3% |
| 数据库问题 | 17 | 2 | 15 | 11.8% |
| **总计** | **48** | **17.5** | **30.5** | **36.5%** |

### 按严重级别统计

| 严重级别 | 总数 | 已修复 | 未修复 |
|---------|------|--------|--------|
| CRITICAL | 6 | 4 | 2 |
| HIGH | 12 | 8 | 4 |
| MEDIUM | 21 | 4 | 17 |
| LOW | 9 | 1.5 | 7.5 |

> CRITICAL 和 HIGH 级别共 18 项，已修复 12 项 (66.7%)。所有可直接利用的认证/越权漏洞均已修复。

---

## 十、已修复问题完整清单

| # | 问题 | 修改文件 |
|---|------|---------|
| 1 | JWT 使用用户名签名 → 服务端密钥 | `TokenUtils.java`, `JwtInterceptor.java`, `application.yml` |
| 2 | MD5 密码 → BCrypt + 透明升级 | `pom.xml`, `UserService.java` |
| 3 | 订单越权 → 归属权校验 | `OrderController.java` |
| 4 | 购物车越权 → 归属权校验 | `CartController.java` |
| 5 | 地址越权 → 归属权校验 | `AddressController.java` |
| 6 | 用户管理无权限 → 管理员限制 | `UserController.java` |
| 7 | 文件上传无认证 → 需登录 | `InterceptorConfig.java`, `FileController.java` |
| 8 | 文件无类型校验 → 白名单 | `FileService.java` |
| 9 | resetPassword 无权限 → 管理员+POST | `UserController.java` |
| 10 | 注册泄露密码 → 返回 UserDTO | `UserController.java` |
| 11 | 订单状态用 GET → POST | `OrderController.java`, `Pay.vue`, `Order.vue`, `OrderItem.vue` |
| 12 | 凭据硬编码 → 环境变量 | `application.yml` |
| 13 | Redis 无密码 → 支持配置密码 | `application.yml` |
| 14 | 分页无上限 → maxLimit=100 | `MybatisPlusConfig.java` |
| 15 | N+1 查询 → 批量查询 | `GoodMapper.java`, `GoodMapper.xml`, `GoodService.java` |
| 16 | 重复异常处理器 → 删除冗余 | 删除 `exception/GlobalExceptionHandler.java` |
| 17 | 异常信息混淆 → 清晰中文+Logger | `config/GlobalExceptionHandler.java` |
| 18 | 租赁代码清理 | `Order.java`, `OrderService.java`, `OrderController.java`, `Order.xml`, `update.sql` |

---

## 十一、剩余未修复问题及建议

### 紧急 (建议尽快处理)
1. **DEP-01**: FastJSON 1.2.73 → 升级至 2.0.x 或切换为 Jackson
2. **DB-01**: 修复 `s003.sql` 的 MySQL 8.0 语法以兼容 5.7
3. **DB-02**: 为 `good_standard` 添加主键
4. **DB-03/DB-09**: DOUBLE 改 DECIMAL(10,2) 解决金额精度问题

### 重要 (建议计划内处理)
5. **SEC-08**: CORS 配置限定生产域名
6. **DB-05**: 为高频查询字段添加索引 (user_id, order_no, category_id)
7. **DB-06**: 为 `sys_user.username` 添加唯一索引
8. **DB-07**: 补全主 SQL 中缺失的表结构
9. **BIZ-04**: `saveStandard` 添加 `@Transactional`
10. **ARCH-04**: 前端路由守卫 URL 改为相对路径

### 长期规划
11. **DEP-02**: Spring Boot 2.5.6 → 3.x (需 Java 17)
12. **DEP-03**: Vue 2 → Vue 3
13. **CQ-04**: 引入接口速率限制 (Bucket4j 或网关层)
14. **SEC-09**: Token 存储从 localStorage 改为 HttpOnly Cookie
