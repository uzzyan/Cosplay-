# Cosplay商城系统 - 关键代码汇总

> 文档版本：v1.0  
> 更新日期：2026-04-15  
> 说明：每个模块只保留最核心的几行关键代码

---

## 👤 用户端模块关键代码

### 1. 用户登录模块

**后端核心代码** - [UserService.java](file:///c:/q_code4.12/cosplay_mall(qoder)/mall-server/src/main/java/com/rabbiter/em/service/UserService.java#L44-L75)

```java
// 用户登录核心逻辑
public UserDTO login(LoginForm loginForm) {
    // 1. 查询用户
    User user = getOne(new QueryWrapper<User>().eq("username", loginForm.getUsername()));
    
    // 2. BCrypt密码验证（支持MD5自动升级）
    boolean matched = PASSWORD_ENCODER.matches(rawPassword, storedPassword);
    
    // 3. 生成JWT Token
    String token = TokenUtils.genToken(user.getId().toString(), user.getUsername());
    
    // 4. 存储到Redis（30分钟TTL）
    redisTemplate.opsForValue().set("user_token:" + token, user, 30, TimeUnit.MINUTES);
    
    // 5. 返回用户信息+Token
    return new UserDTO(user, token);
}
```

**前端核心代码** - [Login.vue](file:///c:/q_code4.12/cosplay_mall(qoder)/mall-web/src/views/Login.vue#L67-L97)

```javascript
// 登录处理
const onSubmit = () => {
  // MD5加密密码
  form.password = md5(user.value.password)
  
  // 发送登录请求
  request.post('/login', form).then(res => {
    // 存储到localStorage
    localStorage.setItem('user', JSON.stringify(res.data))
    
    // 根据角色跳转
    router.push(res.data.role === 'admin' ? '/manage' : '/')
  })
}
```

---

### 2. 用户注册模块

**后端核心代码** - [UserService.java](file:///c:/q_code4.12/cosplay_mall(qoder)/mall-server/src/main/java/com/rabbiter/em/service/UserService.java#L77-L108)

```java
// 用户注册核心逻辑
public User register(LoginForm loginForm) {
    // 1. 检查用户名是否存在
    if (getOne(new QueryWrapper<User>().eq("username", username)) != null) {
        throw new ServiceException("用户名已被使用");
    }
    
    // 2. BCrypt加密密码
    user.setPassword(PASSWORD_ENCODER.encode(password));
    
    // 3. 设置默认值
    user.setNickname("新用户");
    user.setRole("user");
    
    // 4. 保存到数据库
    save(user);
    return user;
}
```

---

### 3. 商品搜索模块

**后端核心代码** - [GoodController.java](file:///c:/q_code4.12/cosplay_mall(qoder)/mall-server/src/main/java/com/rabbiter/em/controller/GoodController.java#L100-L140)

```java
// 商品搜索（分页+条件查询）
@GetMapping("/page")
public Result findPage(@RequestParam int pageNum,
                      @RequestParam int pageSize,
                      @RequestParam(required = false) String name,
                      @RequestParam(required = false) Long categoryId) {
    // MyBatis-Plus分页查询
    IPage<Good> page = new Page<>(pageNum, pageSize);
    QueryWrapper<Good> wrapper = new QueryWrapper<>();
    
    // 关键词模糊搜索
    if (StrUtil.isNotEmpty(name)) {
        wrapper.like("name", name);
    }
    
    // 分类筛选
    if (categoryId != null) {
        wrapper.eq("category_id", categoryId);
    }
    
    return Result.success(goodService.page(page, wrapper));
}
```

---

### 4. 购物车模块

**后端核心代码** - [CartController.java](file:///c:/q_code4.12/cosplay_mall(qoder)/mall-server/src/main/java/com/rabbiter/em/controller/CartController.java#L68-L95)

```java
// 添加商品到购物车
@PostMapping
public Result save(@RequestBody Cart cart) {
    // 设置当前用户ID
    cart.setUserId(TokenUtils.getCurrentUser().getId().longValue());
    
    // 验证商品存在
    Good good = goodService.getById(cart.getGoodId());
    
    // 添加到购物车
    cartService.save(cart);
    return Result.success();
}

// 查询用户购物车
@GetMapping("/userid/{userId}")
public Result selectByUserId(@PathVariable Long userId) {
    return Result.success(cartService.selectByUserId(userId));
}
```

---

### 5. 商品购买模块

**后端核心代码** - [OrderController.java](file:///c:/q_code4.12/cosplay_mall(qoder)/mall-server/src/main/java/com/rabbiter/em/controller/OrderController.java#L134-L165)

```java
// 创建订单
@PostMapping
public Result create(@RequestBody Order order) {
    // 生成订单编号
    String orderNo = "ORD" + System.currentTimeMillis();
    order.setOrderNo(orderNo);
    
    // 设置用户ID
    order.setUserId(TokenUtils.getCurrentUser().getId());
    
    // 设置订单状态
    order.setState("待付款");
    order.setCreateTime(LocalDateTime.now());
    
    // 保存订单
    orderService.save(order);
    return Result.success(orderNo);
}

// 支付订单
@PostMapping("/paid/{orderNo}")
public Result paid(@PathVariable String orderNo) {
    // 更新订单状态
    Order order = orderService.selectByOrderNo(orderNo);
    order.setState("已支付");
    orderService.updateById(order);
    
    // 更新商品销量
    goodService.updateSales(order);
    return Result.success();
}
```

---

### 6. 个人订单模块

**后端核心代码** - [OrderController.java](file:///c:/q_code4.12/cosplay_mall(qoder)/mall-server/src/main/java/com/rabbiter/em/controller/OrderController.java#L50-L63)

```java
// 查询用户订单列表
@GetMapping("/userid/{userid}")
public Result selectByUserId(@PathVariable int userid) {
    // 权限校验：只能查看自己的订单
    User current = TokenUtils.getCurrentUser();
    if (current.getId() != userid && !isAdmin()) {
        throw new ServiceException("无权访问他人订单");
    }
    return Result.success(orderService.selectByUserId(userid));
}

// 确认收货
@PostMapping("/receive/{orderNo}")
public Result receive(@PathVariable String orderNo) {
    Order order = orderService.selectByOrderNo(orderNo);
    order.setState("已完成");
    orderService.updateById(order);
    return Result.success();
}
```

---

### 7. 个人信息模块

**后端核心代码** - [UserController.java](file:///c:/q_code4.12/cosplay_mall(qoder)/mall-server/src/main/java/com/rabbiter/em/controller/UserController.java#L64-L68)

```java
// 获取用户信息
@GetMapping("/userinfo/{username}")
public Result getUserInfoByName(@PathVariable String username) {
    return Result.success(userService.getOne(username));
}

// 更新用户信息
@PostMapping("/user")
public Result save(@RequestBody User user) {
    // 部分更新（昵称、头像、手机等）
    return userService.saveUpdate(user);
}
```

---

## 👨‍💼 管理员模块关键代码

### 1. 用户管理模块

**后端核心代码** - [UserController.java](file:///c:/q_code4.12/cosplay_mall(qoder)/mall-server/src/main/java/com/rabbiter/em/controller/UserController.java#L83-L163)

```java
// 分页查询用户（需要管理员权限）
@Authority(AuthorityType.requireAuthority)
@GetMapping("/user/page")
public Result findPage(@RequestParam int pageNum,
                      @RequestParam int pageSize,
                      String username, String nickname) {
    IPage<User> page = new Page<>(pageNum, pageSize);
    QueryWrapper<User> wrapper = new QueryWrapper<>();
    
    // 条件搜索
    if (StrUtil.isNotEmpty(username)) {
        wrapper.like("username", username);
    }
    
    return Result.success(userService.page(page, wrapper));
}

// 删除用户
@Authority(AuthorityType.requireAuthority)
@DeleteMapping("/user/{id}")
public Result deleteById(@PathVariable int id) {
    return userService.removeById(id) ? Result.success() : Result.error("删除失败");
}

// 重置密码
@Authority(AuthorityType.requireAuthority)
@PostMapping("/user/resetPassword")
public Result resetPassword(@RequestParam String id, @RequestParam String newPassword) {
    user.setPassword(PASSWORD_ENCODER.encode(newPassword));
    userService.updateById(user);
    return Result.success();
}
```

---

### 2. 文件管理模块

**后端核心代码** - [FileController.java](file:///c:/q_code4.12/cosplay_mall(qoder)/mall-server/src/main/java/com/rabbiter/em/controller/FileController.java)

```java
// 文件上传
@PostMapping("/upload")
public Result upload(@RequestParam("file") MultipartFile file) {
    // 生成唯一文件名
    String fileName = UUID.randomUUID().toString() + "." + fileExtension;
    
    // 保存到本地
    File dest = new File(uploadPath + fileName);
    file.transferTo(dest);
    
    // 返回文件URL
    return Result.success("/file/" + fileName);
}

// 删除文件
@Authority(AuthorityType.requireAuthority)
@DeleteMapping("/delete/{fileName}")
public Result delete(@PathVariable String fileName) {
    File file = new File(uploadPath + fileName);
    return file.delete() ? Result.success() : Result.error("删除失败");
}
```

---

### 3. 分类管理模块

**后端核心代码** - [CategoryController.java](file:///c:/q_code4.12/cosplay_mall(qoder)/mall-server/src/main/java/com/rabbiter/em/controller/CategoryController.java)

```java
// 查询所有分类
@GetMapping
public Result findAll() {
    return Result.success(categoryService.list());
}

// 新增分类
@Authority(AuthorityType.requireAuthority)
@PostMapping
public Result save(@RequestBody Category category) {
    categoryService.save(category);
    return Result.success();
}

// 删除分类（需检查是否有关联商品）
@Authority(AuthorityType.requireAuthority)
@DeleteMapping("/{id}")
public Result delete(@PathVariable Long id) {
    // 检查是否有关联商品
    long count = goodService.count(new QueryWrapper<Good>().eq("category_id", id));
    if (count > 0) {
        return Result.error("该分类下有商品，无法删除");
    }
    categoryService.removeById(id);
    return Result.success();
}
```

---

### 4. 商品管理模块

**后端核心代码** - [GoodController.java](file:///c:/q_code4.12/cosplay_mall(qoder)/mall-server/src/main/java/com/rabbiter/em/controller/GoodController.java#L45-L75)

```java
// 新增商品
@Authority(AuthorityType.requireAuthority)
@PostMapping
public Result save(@RequestBody Good good) {
    good.setCreateTime(new Date());
    good.setSales(0L);
    return Result.success(goodService.saveOrUpdateGood(good));
}

// 更新商品
@Authority(AuthorityType.requireAuthority)
@PutMapping
public Result update(@RequestBody Good good) {
    goodService.update(good);
    return Result.success();
}

// 删除商品（逻辑删除）
@Authority(AuthorityType.requireAuthority)
@DeleteMapping("/delete/{id}")
public Result delete(@PathVariable Long id) {
    goodService.deleteGood(id);  // 设置is_delete=1
    return Result.success();
}

// 设置推荐
@Authority(AuthorityType.requireAuthority)
@PutMapping("/recommend/{id}")
public Result setRecommend(@PathVariable Long id, @RequestParam boolean recommend) {
    Good good = goodService.getById(id);
    good.setRecommend(recommend ? 1 : 0);
    goodService.updateById(good);
    return Result.success();
}
```

---

### 5. 订单管理模块

**后端核心代码** - [OrderController.java](file:///c:/q_code4.12/cosplay_mall(qoder)/mall-server/src/main/java/com/rabbiter/em/controller/OrderController.java#L72-L130)

```java
// 分页查询订单
@Authority(AuthorityType.requireAuthority)
@GetMapping("/page")
public Result findPage(@RequestParam int pageNum,
                      @RequestParam int pageSize,
                      String orderNo, String state) {
    IPage<Order> page = new Page<>(pageNum, pageSize);
    QueryWrapper<Order> wrapper = new QueryWrapper<>();
    
    // 条件筛选
    if (StrUtil.isNotEmpty(state)) {
        wrapper.eq("state", state);
    }
    if (StrUtil.isNotEmpty(orderNo)) {
        wrapper.like("order_no", orderNo);
    }
    
    return Result.success(orderService.page(page, wrapper));
}

// 订单发货
@Authority(AuthorityType.requireAuthority)
@PostMapping("/deliver/{orderNo}")
public Result deliver(@PathVariable String orderNo) {
    Order order = orderService.selectByOrderNo(orderNo);
    order.setState("已发货");
    orderService.updateById(order);
    return Result.success();
}

// 导出订单（CSV）
@Authority(AuthorityType.requireAuthority)
@GetMapping("/export")
public ResponseEntity<byte[]> export(String orderNo, String state) {
    List<Map<String, Object>> rows = orderService.export(orderNo, state);
    
    // 构建CSV内容
    StringBuilder csv = new StringBuilder();
    csv.append("订单编号,用户名,商品名称,金额,状态,时间\n");
    rows.forEach(row -> csv.append(row.values()).append("\n"));
    
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=orders.csv")
        .body(csv.toString().getBytes());
}
```

---

### 6. 营销数据管理模块

**后端核心代码** - [IncomeController.java](file:///c:/q_code4.12/cosplay_mall(qoder)/mall-server/src/main/java/com/rabbiter/em/controller/IncomeController.java)

```java
// 获取收入图表数据（按分类统计）
@Authority(AuthorityType.requireAuthority)
@GetMapping("/chart")
public Result getChart() {
    return Result.success(incomeService.getChart());
    // 返回: [{categoryName: "Cosplay服装", categoryIncome: 15000.00}, ...]
}

// 获取本周收入
@Authority(AuthorityType.requireAuthority)
@GetMapping("/week")
public Result getWeekIncome() {
    return Result.success(incomeService.getWeekIncome());
    // 返回: {weekIncome: [1200.50, 1500.00, ...]} (7天数据)
}

// 获取本月收入
@Authority(AuthorityType.requireAuthority)
@GetMapping("/month")
public Result getMonthIncome() {
    return Result.success(incomeService.getMonthIncome());
    // 返回: {monthDays: ["12-01", ...], monthIncome: [1200.50, ...]}
}

// 商品销量排行
@GetMapping("/good/rank")
public Result getRank(@RequestParam(defaultValue = "10") int num) {
    return Result.success(goodService.getRank(num));
    // 返回: [{id: 1, name: "初音未来", sales: 150, saleMoney: 45000.00}, ...]
}
```

---

## 📊 代码统计

| 模块类型 | 模块数 | 关键代码行数 | 核心技术 |
|---------|--------|------------|---------|
| 用户端 | 7 | ~70行 | JWT、BCrypt、MyBatis-Plus |
| 管理端 | 6 | ~120行 | 权限注解、分页查询、CSV导出 |
| **总计** | **13** | **~190行** | **Spring Boot + Vue 3** |

---

## 🔑 核心技术点

### 1. 权限控制
```java
@Authority(AuthorityType.requireAuthority)  // 需要管理员
@Authority(AuthorityType.requireLogin)      // 需要登录
@Authority(AuthorityType.noRequire)         // 公开接口
```

### 2. 密码加密
```java
// BCrypt加密（注册）
user.setPassword(PASSWORD_ENCODER.encode(password));

// BCrypt验证（登录）
boolean matched = PASSWORD_ENCODER.matches(rawPassword, storedPassword);
```

### 3. JWT Token
```java
// 生成Token
String token = JWT.create()
    .withAudience(userId)
    .sign(Algorithm.HMAC256(jwtSecret));

// 存储到Redis
redisTemplate.opsForValue().set("user_token:" + token, user, 30, TimeUnit.MINUTES);
```

### 4. 分页查询
```java
IPage<Good> page = new Page<>(pageNum, pageSize);
QueryWrapper<Good> wrapper = new QueryWrapper<>();
wrapper.like("name", keyword);
return goodService.page(page, wrapper);
```

### 5. 统一响应
```java
// 成功响应
return Result.success(data);

// 错误响应
return Result.error("400", "错误信息");
```

---

## 📝 代码说明

### 技术栈
- **后端框架**: Spring Boot 2.5.6
- **ORM框架**: MyBatis-Plus 3.5.5
- **认证方式**: JWT + Redis
- **密码加密**: BCrypt
- **统一响应**: Result封装

### 代码特点
1. ✅ **简洁明了** - 每个模块只保留核心逻辑
2. ✅ **注释清晰** - 每行代码都有说明
3. ✅ **易于理解** - 适合快速掌握系统核心
4. ✅ **可直接使用** - 代码来自实际项目

---

**文档生成时间**: 2026-04-15  
**系统版本**: v3.0（Vue 3 + Spring Boot）  
**适用场景**: 毕业设计、项目答辩、技术文档
