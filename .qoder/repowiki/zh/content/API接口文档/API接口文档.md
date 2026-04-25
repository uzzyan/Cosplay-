# API接口文档

<cite>
**本文引用的文件**
- [UserController.java](file://mall-server/src/main/java/com/rabbiter/em/controller/UserController.java)
- [GoodController.java](file://mall-server/src/main/java/com/rabbiter/em/controller/GoodController.java)
- [CartController.java](file://mall-server/src/main/java/com/rabbiter/em/controller/CartController.java)
- [OrderController.java](file://mall-server/src/main/java/com/rabbiter/em/controller/OrderController.java)
- [FileController.java](file://mall-server/src/main/java/com/rabbiter/em/controller/FileController.java)
- [AddressController.java](file://mall-server/src/main/java/com/rabbiter/em/controller/AddressController.java)
- [CategoryController.java](file://mall-server/src/main/java/com/rabbiter/em/controller/CategoryController.java)
- [CarouselController.java](file://mall-server/src/main/java/com/rabbiter/em/controller/CarouselController.java)
- [CommentController.java](file://mall-server/src/main/java/com/rabbiter/em/controller/CommentController.java)
- [IconController.java](file://mall-server/src/main/java/com/rabbiter/em/controller/IconController.java)
- [LoginForm.java](file://mall-server/src/main/java/com/rabbiter/em/entity/LoginForm.java)
- [Result.java](file://mall-server/src/main/java/com/rabbiter/em/common/Result.java)
- [Authority.java](file://mall-server/src/main/java/com/rabbiter/em/annotation/Authority.java)
- [Constants.java](file://mall-server/src/main/java/com/rabbiter/em/constants/Constants.java)
- [application.yml](file://mall-server/src/main/resources/application.yml)
</cite>

## 目录
1. [简介](#简介)
2. [项目结构](#项目结构)
3. [核心组件](#核心组件)
4. [架构总览](#架构总览)
5. [详细组件分析](#详细组件分析)
6. [依赖分析](#依赖分析)
7. [性能与并发](#性能与并发)
8. [故障排除指南](#故障排除指南)
9. [结论](#结论)
10. [附录](#附录)

## 简介
本API接口文档面向cosplay商城系统的后端RESTful接口，覆盖用户认证、商品管理、购物车、订单处理、文件上传、地址管理、分类管理、轮播图、评论、图标等模块。文档提供每个接口的请求方法、URL路径、请求头、请求体与响应体格式、业务逻辑、参数校验规则、错误码与处理策略，并给出认证机制、权限控制、版本与兼容性建议、限流与安全防护、测试与调试方法以及常见问题排查。

## 项目结构
后端采用Spring Boot + MyBatis-Plus架构，控制器位于controller包，服务层service包，实体与DTO位于entity包，通用响应封装在common包，权限注解与常量在annotation与constants包，配置在resources目录。

```mermaid
graph TB
subgraph "控制器层"
UC["UserController<br/>用户认证/管理"]
GC["GoodController<br/>商品管理"]
CC["CartController<br/>购物车"]
OC["OrderController<br/>订单处理"]
FC["FileController<br/>文件上传/下载"]
AC["AddressController<br/>地址管理"]
CatC["CategoryController<br/>分类管理"]
CarC["CarouselController<br/>轮播图"]
ComC["CommentController<br/>评论"]
IC["IconController<br/>图标"]
end
subgraph "服务层"
US["UserService"]
GS["GoodService"]
CS["CartService"]
OS["OrderService"]
FS["FileService"]
AS["AddressService"]
Cats["CategoryService"]
Cars["CarouselService"]
ComS["CommentService"]
IS["IconService"]
end
subgraph "通用与配置"
R["Result<br/>统一响应"]
A["Authority<br/>权限注解"]
Cst["Constants<br/>错误码/路径"]
Yml["application.yml<br/>配置"]
end
UC --> US
GC --> GS
CC --> CS
OC --> OS
FC --> FS
AC --> AS
CatC --> Cats
CarC --> Cars
ComC --> ComS
IC --> IS
UC --- R
GC --- R
CC --- R
OC --- R
FC --- R
AC --- R
CatC --- R
CarC --- R
ComC --- R
IC --- R
UC --- A
GC --- A
CC --- A
OC --- A
FC --- A
AC --- A
CatC --- A
CarC --- A
ComC --- A
IC --- A
UC --- Cst
GC --- Cst
CC --- Cst
OC --- Cst
FC --- Cst
AC --- Cst
CatC --- Cst
CarC --- Cst
ComC --- Cst
IC --- Cst
Yml --- UC
Yml --- GC
Yml --- CC
Yml --- OC
Yml --- FC
Yml --- AC
Yml --- CatC
Yml --- CarC
Yml --- ComC
Yml --- IC
```

**图表来源**
- [UserController.java:28-179](file://mall-server/src/main/java/com/rabbiter/em/controller/UserController.java#L28-L179)
- [GoodController.java:18-209](file://mall-server/src/main/java/com/rabbiter/em/controller/GoodController.java#L18-L209)
- [CartController.java:23-110](file://mall-server/src/main/java/com/rabbiter/em/controller/CartController.java#L23-L110)
- [OrderController.java:30-196](file://mall-server/src/main/java/com/rabbiter/em/controller/OrderController.java#L30-L196)
- [FileController.java:20-80](file://mall-server/src/main/java/com/rabbiter/em/controller/FileController.java#L20-L80)
- [AddressController.java:21-87](file://mall-server/src/main/java/com/rabbiter/em/controller/AddressController.java#L21-L87)
- [CategoryController.java:18-82](file://mall-server/src/main/java/com/rabbiter/em/controller/CategoryController.java#L18-L82)
- [CarouselController.java:22-93](file://mall-server/src/main/java/com/rabbiter/em/controller/CarouselController.java#L22-L93)
- [CommentController.java:18-44](file://mall-server/src/main/java/com/rabbiter/em/controller/CommentController.java#L18-L44)
- [IconController.java:22-79](file://mall-server/src/main/java/com/rabbiter/em/controller/IconController.java#L22-L79)
- [Result.java:5-66](file://mall-server/src/main/java/com/rabbiter/em/common/Result.java#L5-L66)
- [Authority.java:10-12](file://mall-server/src/main/java/com/rabbiter/em/annotation/Authority.java#L10-L12)
- [Constants.java:5-16](file://mall-server/src/main/java/com/rabbiter/em/constants/Constants.java#L5-L16)
- [application.yml:1-42](file://mall-server/src/main/resources/application.yml#L1-L42)

**章节来源**
- [application.yml:1-42](file://mall-server/src/main/resources/application.yml#L1-L42)

## 核心组件
- 统一响应封装：Result类提供success/error静态方法，统一返回code/msg/data结构。
- 权限注解：@Authority定义接口所需权限级别，支持requireLogin/requireAuthority/noRequire等。
- 常量与错误码：Constants集中定义HTTP语义错误码及文件存储路径常量。
- 配置：application.yml定义端口、数据库连接、Redis、文件大小限制、JWT密钥等。

**章节来源**
- [Result.java:11-27](file://mall-server/src/main/java/com/rabbiter/em/common/Result.java#L11-L27)
- [Authority.java:10-12](file://mall-server/src/main/java/com/rabbiter/em/annotation/Authority.java#L10-L12)
- [Constants.java:5-16](file://mall-server/src/main/java/com/rabbiter/em/constants/Constants.java#L5-L16)
- [application.yml:1-42](file://mall-server/src/main/resources/application.yml#L1-L42)

## 架构总览
以下序列图展示典型登录流程，体现JWT生成与权限拦截链路。

```mermaid
sequenceDiagram
participant Client as "客户端"
participant Ctrl as "UserController"
participant Svc as "UserService"
participant JWT as "TokenUtils/JWT"
participant Resp as "Result"
Client->>Ctrl : POST /login {username,password}
Ctrl->>Svc : login(LoginForm)
Svc->>JWT : 生成JWT令牌
JWT-->>Svc : 返回token
Svc-->>Ctrl : 返回UserDTO
Ctrl->>Resp : Result.success(UserDTO)
Resp-->>Client : {code : "200",data : {...}}
```

**图表来源**
- [UserController.java:37-41](file://mall-server/src/main/java/com/rabbiter/em/controller/UserController.java#L37-L41)
- [LoginForm.java:3-49](file://mall-server/src/main/java/com/rabbiter/em/entity/LoginForm.java#L3-L49)
- [Result.java:11-17](file://mall-server/src/main/java/com/rabbiter/em/common/Result.java#L11-L17)

## 详细组件分析

### 用户认证与管理
- 登录
  - 方法与路径：POST /login
  - 请求头：Content-Type: application/json
  - 请求体：LoginForm {username,password}
  - 成功响应：Result.success(UserDTO)
  - 失败响应：Result.error(code,msg)
  - 业务逻辑：校验凭据，生成JWT并返回用户信息
  - 参数校验：用户名/密码非空
  - 权限：无需登录
- 注册
  - 方法与路径：POST /register
  - 请求体：LoginForm {username,password,phone}
  - 成功响应：Result.success(UserDTO)
  - 失败响应：Result.error(code,msg)
  - 权限：无需登录
- 获取当前用户ID
  - 方法与路径：GET /userid
  - 成功响应：Result.success(long)
  - 权限：requireLogin
- 获取用户信息
  - 方法与路径：GET /userinfo/{username}
  - 成功响应：Result.success(User)
  - 权限：无需登录
- 管理员接口（需管理员）
  - GET /user/：获取所有用户
  - POST /user：保存或更新用户
  - DELETE /user/{id}：按ID删除用户
  - POST /user/del/batch：批量删除用户
  - GET /user/page：分页查询（模糊条件：id/username/nickname）
  - POST /user/resetPassword：重置密码（id,newPassword）

请求示例（登录）
- 请求
  - POST /login
  - Content-Type: application/json
  - Body: {"username":"alice","password":"pass123"}
- 响应
  - 200 OK
  - Body: {"code":"200","msg":null,"data":{"id":1001,"username":"alice","nickname":"Alice","role":"user","token":"..."}}

请求示例（分页查询用户）
- 请求
  - GET /user/page?pageNum=1&pageSize=10&id=&username=&nickname=
- 响应
  - 200 OK
  - Body: {"code":"200","msg":null,"data":{"records":[],"total":0,...}}

**章节来源**
- [UserController.java:37-178](file://mall-server/src/main/java/com/rabbiter/em/controller/UserController.java#L37-L178)
- [LoginForm.java:3-49](file://mall-server/src/main/java/com/rabbiter/em/entity/LoginForm.java#L3-L49)
- [Result.java:11-27](file://mall-server/src/main/java/com/rabbiter/em/common/Result.java#L11-L27)
- [Constants.java:5-16](file://mall-server/src/main/java/com/rabbiter/em/constants/Constants.java#L5-L16)

### 商品管理
- 推荐商品
  - 方法与路径：GET /api/good/recommend/{userId}
  - 权限：noRequire
- 新增商品
  - 方法与路径：POST /api/good
  - 权限：requireAuthority
- 更新商品
  - 方法与路径：PUT /api/good
  - 权限：requireAuthority
- 删除商品
  - 方法与路径：DELETE /api/good/delete/{id}
  - 权限：requireAuthority
- 商品详情
  - 方法与路径：GET /api/good/detail/{id}
  - 权限：noRequire
- 商品规格
  - 方法与路径：GET /api/good/standard/{id}
  - 权限：noRequire
- 前台商品列表
  - 方法与路径：GET /api/good
  - 权限：noRequire
- 销量排行
  - 方法与路径：GET /api/good/rank?num=10
  - 权限：noRequire
- 保存规格
  - 方法与路径：POST /api/good/standard?goodId=123
  - 权限：requireAuthority
- 删除规格
  - 方法与路径：DELETE /api/good/standard
  - 权限：requireAuthority
- 设置推荐状态
  - 方法与路径：GET /api/good/recommend?id=123&isRecommend=true
  - 权限：requireAuthority
- 分页查询（前台）
  - 方法与路径：GET /api/good/page?pageNum=1&pageSize=10&searchText=&categoryId=
  - 权限：noRequire
- 分页查询（完整信息）
  - 方法与路径：GET /api/good/fullPage?pageNum=1&pageSize=10&searchText=&categoryId=
  - 权限：noRequire

请求示例（新增商品）
- 请求
  - POST /api/good
  - Authorization: Bearer {token}
  - Content-Type: application/json
  - Body: {"name":"商品A","price":299,"status":1,...}
- 响应
  - 200 OK
  - Body: {"code":"200","msg":null,"data":1001}

**章节来源**
- [GoodController.java:28-208](file://mall-server/src/main/java/com/rabbiter/em/controller/GoodController.java#L28-L208)

### 购物车
- 获取购物车项
  - 方法与路径：GET /api/cart/{id}
  - 权限：requireLogin
  - 校验：仅本人或管理员可访问
- 查看所有购物车
  - 方法与路径：GET /api/cart
  - 权限：requireAuthority
- 按用户查询
  - 方法与路径：GET /api/cart/userid/{userId}
  - 权限：requireLogin
  - 校验：仅本人或管理员可访问
- 新增/更新
  - 方法与路径：POST /api/cart
  - 权限：requireLogin
  - 校验：goodId/count/standard必填；商品存在且未下架；库存充足
- 更新
  - 方法与路径：PUT /api/cart
  - 权限：requireLogin
- 删除
  - 方法与路径：DELETE /api/cart/{id}
  - 权限：requireLogin
  - 校验：仅本人或管理员可删除

请求示例（新增购物车项）
- 请求
  - POST /api/cart
  - Authorization: Bearer {token}
  - Content-Type: application/json
  - Body: {"goodId":123,"count":2,"standard":"L"}
- 响应
  - 200 OK
  - Body: {"code":"200","msg":null,"data":null}

**章节来源**
- [CartController.java:46-108](file://mall-server/src/main/java/com/rabbiter/em/controller/CartController.java#L46-L108)

### 订单处理
- 按用户查询
  - 方法与路径：GET /api/order/userid/{userid}
  - 权限：requireLogin
  - 校验：仅本人或管理员可访问
- 按订单号查询
  - 方法与路径：GET /api/order/orderNo/{orderNo}
  - 权限：requireLogin
  - 校验：仅本人或管理员可访问
- 查看所有订单
  - 方法与路径：GET /api/order
  - 权限：requireAuthority
- 分页查询
  - 方法与路径：GET /api/order/page?pageNum=1&pageSize=10&orderNo=&state=
  - 权限：requireAuthority
- 导出CSV
  - 方法与路径：GET /api/order/export?orderNo=&state=
  - 权限：requireAuthority
- 创建订单
  - 方法与路径：POST /api/order
  - 权限：requireLogin
- 支付订单
  - 方法与路径：POST /api/order/paid/{orderNo}
  - 权限：requireLogin
  - 校验：仅本人或管理员可操作
- 发货
  - 方法与路径：POST /api/order/delivery/{orderNo}
  - 权限：requireAuthority
- 确认收货
  - 方法与路径：POST /api/order/received/{orderNo}
  - 权限：requireLogin
- 更新订单
  - 方法与路径：PUT /api/order
  - 权限：requireAuthority
- 删除订单
  - 方法与路径：DELETE /api/order/{id}
  - 权限：requireLogin/requireAuthority
  - 校验：仅本人或管理员可删除

请求示例（创建订单）
- 请求
  - POST /api/order
  - Authorization: Bearer {token}
  - Content-Type: application/json
  - Body: {"userId":1001,"addressId":2001,...}
- 响应
  - 200 OK
  - Body: {"code":"200","msg":null,"data":"202512010001"}

**章节来源**
- [OrderController.java:50-194](file://mall-server/src/main/java/com/rabbiter/em/controller/OrderController.java#L50-L194)

### 文件上传与下载
- 上传
  - 方法与路径：POST /file/upload
  - 权限：requireLogin
  - 请求体：multipart/form-data，字段file
  - 成功响应：Result.success(url)
- 下载
  - 方法与路径：GET /file/{fileName}
  - 权限：noRequire
  - 响应：文件流
- 删除文件（软删）
  - 方法与路径：DELETE /file/{id}
  - 权限：requireAuthority
- 批量删除
  - 方法与路径：POST /file/del/batch
  - 权限：requireAuthority
- 启用/禁用
  - 方法与路径：GET /file/enable?id=1&enable=true
  - 权限：requireAuthority
- 分页查询
  - 方法与路径：GET /file/page?pageNum=1&pageSize=10&fileName=
  - 权限：requireAuthority

请求示例（上传）
- 请求
  - POST /file/upload
  - Authorization: Bearer {token}
  - Content-Type: multipart/form-data
  - Form-Data: file=@image.jpg
- 响应
  - 200 OK
  - Body: {"code":"200","msg":null,"data":"/file/image.jpg"}

**章节来源**
- [FileController.java:25-78](file://mall-server/src/main/java/com/rabbiter/em/controller/FileController.java#L25-L78)

### 地址管理
- 查询地址
  - 方法与路径：GET /api/address/{userId}
  - 权限：requireLogin
  - 校验：仅本人或管理员可访问
- 查看所有地址
  - 方法与路径：GET /api/address
  - 权限：requireAuthority
- 新增地址
  - 方法与路径：POST /api/address
  - 权限：requireLogin
  - 校验：手机号11位数字
- 更新地址
  - 方法与路径：PUT /api/address
  - 权限：requireLogin
  - 校验：手机号11位数字；仅本人或管理员可更新
- 删除地址
  - 方法与路径：DELETE /api/address/{id}
  - 权限：requireLogin
  - 校验：仅本人或管理员可删除

请求示例（新增地址）
- 请求
  - POST /api/address
  - Authorization: Bearer {token}
  - Content-Type: application/json
  - Body: {"consignee":"Alice","linkPhone":"13800001111","province":"上海"}
- 响应
  - 200 OK
  - Body: {"code":"200","msg":null,"data":null}

**章节来源**
- [AddressController.java:29-85](file://mall-server/src/main/java/com/rabbiter/em/controller/AddressController.java#L29-L85)

### 分类管理
- 查询分类
  - 方法与路径：GET /api/category/{id}
  - 权限：无需登录
- 查询所有分类
  - 方法与路径：GET /api/category
  - 权限：无需登录
- 新增分类
  - 方法与路径：POST /api/category
  - 权限：requireAuthority
- 新增下级分类
  - 方法与路径：POST /api/category/add
  - 权限：无需登录
- 更新分类
  - 方法与路径：PUT /api/category
  - 权限：requireAuthority
- 删除分类
  - 方法与路径：GET /api/category/delete?id=1
  - 权限：requireAuthority

请求示例（新增分类）
- 请求
  - POST /api/category
  - Authorization: Bearer {token}
  - Content-Type: application/json
  - Body: {"name":"Cosplay服装配件"}
- 响应
  - 200 OK
  - Body: {"code":"200","msg":null,"data":null}

**章节来源**
- [CategoryController.java:24-75](file://mall-server/src/main/java/com/rabbiter/em/controller/CategoryController.java#L24-L75)

### 轮播图
- 查询轮播图
  - 方法与路径：GET /api/carousel
  - 权限：noRequire
- 查询轮播图详情
  - 方法与路径：GET /api/carousel/{id}
  - 权限：noRequire
- 新增轮播图
  - 方法与路径：POST /api/carousel
  - 权限：requireAuthority
  - 校验：商品ID有效
- 更新轮播图
  - 方法与路径：PUT /api/carousel
  - 权限：requireAuthority
  - 校验：商品ID有效
- 删除轮播图
  - 方法与路径：DELETE /api/carousel/{id}
  - 权限：requireAuthority

请求示例（新增轮播图）
- 请求
  - POST /api/carousel
  - Authorization: Bearer {token}
  - Content-Type: application/json
  - Body: {"goodId":123,"imageUrl":"/file/banner1.jpg"}
- 响应
  - 200 OK
  - Body: {"code":"200","msg":null,"data":null}

**章节来源**
- [CarouselController.java:42-86](file://mall-server/src/main/java/com/rabbiter/em/controller/CarouselController.java#L42-L86)

### 评论
- 查询商品评论
  - 方法与路径：GET /api/comment/good/{goodId}
  - 权限：noRequire
- 提交/更新评论
  - 方法与路径：POST /api/comment
  - 权限：requireLogin
  - 校验：首次提交自动填充用户ID与创建时间
- 删除评论
  - 方法与路径：DELETE /api/comment/{id}
  - 权限：无需登录（但实际业务中通常需要登录）

请求示例（提交评论）
- 请求
  - POST /api/comment
  - Authorization: Bearer {token}
  - Content-Type: application/json
  - Body: {"goodId":123,"content":"很好！"}
- 响应
  - 200 OK
  - Body: {"code":"200","msg":null,"data":null}

**章节来源**
- [CommentController.java:23-42](file://mall-server/src/main/java/com/rabbiter/em/controller/CommentController.java#L23-L42)

### 图标
- 查询图标
  - 方法与路径：GET /api/icon/{id}
  - 权限：noRequire
- 查询图标列表
  - 方法与路径：GET /api/icon
  - 权限：noRequire
- 新增图标
  - 方法与路径：POST /api/icon
  - 权限：requireAuthority
- 更新图标
  - 方法与路径：PUT /api/icon
  - 权限：requireAuthority
- 删除图标
  - 方法与路径：GET /api/icon/delete?id=1
  - 权限：requireAuthority

请求示例（新增图标）
- 请求
  - POST /api/icon
  - Authorization: Bearer {token}
  - Content-Type: application/json
  - Body: {"name":"角色A","categoryId":1}
- 响应
  - 200 OK
  - Body: {"code":"200","msg":null,"data":null}

**章节来源**
- [IconController.java:38-76](file://mall-server/src/main/java/com/rabbiter/em/controller/IconController.java#L38-L76)

### 售后管理
- 创建售后申请
  - 方法与路径：POST /api/afterSale
  - 权限：requireLogin
- 查询我的售后
  - 方法与路径：GET /api/afterSale/mine/page
  - 权限：requireLogin
- 查询所有售后
  - 方法与路径：GET /api/afterSale/page
  - 权限：requireAuthority
- 查询售后详情
  - 方法与路径：GET /api/afterSale/{id}
  - 权限：requireLogin
- 处理售后申请
  - 方法与路径：PUT /api/afterSale/handle
  - 权限：requireAuthority

详细文档：[售后管理API.md](售后管理API.md)

### 留言管理
- 创建留言
  - 方法与路径：POST /api/message
  - 权限：requireLogin
- 查询我的留言
  - 方法与路径：GET /api/message/mine/page
  - 权限：requireLogin
- 查询留言详情
  - 方法与路径：GET /api/message/{id}
  - 权限：requireLogin
- 查询所有留言
  - 方法与路径：GET /api/message/page
  - 权限：requireAuthority
- 回复留言
  - 方法与路径：PUT /api/message/reply
  - 权限：requireAuthority
- 删除留言
  - 方法与路径：DELETE /api/message/{id}
  - 权限：requireAuthority

详细文档：[留言管理API.md](留言管理API.md)

### 收入管理
- 获取收入图表数据
  - 方法与路径：GET /api/income/chart
  - 权限：requireAuthority
- 获取本周收入
  - 方法与路径：GET /api/income/week
  - 权限：requireAuthority
- 获取本月收入
  - 方法与路径：GET /api/income/month
  - 权限：requireAuthority
- 获取商品销量排行
  - 方法与路径：GET /api/good/rank?num=10
  - 权限：noRequire

详细文档：[收入管理API.md](收入管理API.md)

## 依赖分析
- 控制器与服务层：各控制器通过@Resource注入对应Service，遵循单一职责与松耦合。
- 权限控制：@Authority注解配合拦截器实现权限判定，requireLogin/requireAuthority/noRequire三档权限。
- 统一响应：Result封装所有接口返回，便于前端统一处理。
- 错误码：Constants集中定义错误码，避免魔法数。

```mermaid
classDiagram
class UserController
class GoodController
class CartController
class OrderController
class FileController
class AddressController
class CategoryController
class CarouselController
class CommentController
class IconController
class Result
class Authority
class Constants
UserController --> Result : "返回"
GoodController --> Result : "返回"
CartController --> Result : "返回"
OrderController --> Result : "返回"
FileController --> Result : "返回"
AddressController --> Result : "返回"
CategoryController --> Result : "返回"
CarouselController --> Result : "返回"
CommentController --> Result : "返回"
IconController --> Result : "返回"
UserController --> Authority : "使用"
GoodController --> Authority : "使用"
CartController --> Authority : "使用"
OrderController --> Authority : "使用"
FileController --> Authority : "使用"
AddressController --> Authority : "使用"
CategoryController --> Authority : "使用"
CarouselController --> Authority : "使用"
CommentController --> Authority : "使用"
IconController --> Authority : "使用"
UserController --> Constants : "使用"
GoodController --> Constants : "使用"
CartController --> Constants : "使用"
OrderController --> Constants : "使用"
FileController --> Constants : "使用"
AddressController --> Constants : "使用"
CategoryController --> Constants : "使用"
CarouselController --> Constants : "使用"
CommentController --> Constants : "使用"
IconController --> Constants : "使用"
```

**图表来源**
- [Result.java:5-66](file://mall-server/src/main/java/com/rabbiter/em/common/Result.java#L5-L66)
- [Authority.java:10-12](file://mall-server/src/main/java/com/rabbiter/em/annotation/Authority.java#L10-L12)
- [Constants.java:5-16](file://mall-server/src/main/java/com/rabbiter/em/constants/Constants.java#L5-L16)
- [UserController.java:28-179](file://mall-server/src/main/java/com/rabbiter/em/controller/UserController.java#L28-L179)
- [GoodController.java:18-209](file://mall-server/src/main/java/com/rabbiter/em/controller/GoodController.java#L18-L209)
- [CartController.java:23-110](file://mall-server/src/main/java/com/rabbiter/em/controller/CartController.java#L23-L110)
- [OrderController.java:30-196](file://mall-server/src/main/java/com/rabbiter/em/controller/OrderController.java#L30-L196)
- [FileController.java:20-80](file://mall-server/src/main/java/com/rabbiter/em/controller/FileController.java#L20-L80)
- [AddressController.java:21-87](file://mall-server/src/main/java/com/rabbiter/em/controller/AddressController.java#L21-L87)
- [CategoryController.java:18-82](file://mall-server/src/main/java/com/rabbiter/em/controller/CategoryController.java#L18-L82)
- [CarouselController.java:22-93](file://mall-server/src/main/java/com/rabbiter/em/controller/CarouselController.java#L22-L93)
- [CommentController.java:18-44](file://mall-server/src/main/java/com/rabbiter/em/controller/CommentController.java#L18-L44)
- [IconController.java:22-79](file://mall-server/src/main/java/com/rabbiter/em/controller/IconController.java#L22-L79)

**章节来源**
- [Result.java:11-27](file://mall-server/src/main/java/com/rabbiter/em/common/Result.java#L11-L27)
- [Authority.java:10-12](file://mall-server/src/main/java/com/rabbiter/em/annotation/Authority.java#L10-L12)
- [Constants.java:5-16](file://mall-server/src/main/java/com/rabbiter/em/constants/Constants.java#L5-L16)

## 性能与并发
- 数据库连接池与MyBatis：application.yml启用驼峰映射，mapper路径classpath:mapper/*.xml，有利于SQL与实体映射一致性。
- 文件上传：multipart.max-file-size设置为30MB，适合图片资源上传。
- Redis：配置了连接参数，可用于缓存与会话存储（具体使用见服务层）。
- 并发建议：
  - 对高并发接口（如商品详情、下单）增加Redis缓存热点数据。
  - 使用数据库事务保证订单与库存一致性。
  - 对分页查询添加索引优化（如商品表的分类、推荐状态、销量）。

**章节来源**
- [application.yml:14-42](file://mall-server/src/main/resources/application.yml#L14-L42)

## 故障排除指南
- 401 未授权/无效token
  - 可能原因：缺少Authorization头、token过期或签名错误
  - 处理：重新登录获取新token
- 403 拒绝执行
  - 可能原因：越权访问他人数据、非管理员调用管理接口
  - 处理：检查当前用户身份与目标资源归属
- 500 系统错误
  - 可能原因：数据库异常、库存不足、商品已下架、文件上传失败
  - 处理：查看服务端日志，核对输入参数与业务前置条件
- 510 未找到结果
  - 可能原因：查询条件无匹配记录
  - 处理：调整查询参数或检查数据是否存在

**章节来源**
- [Constants.java:5-16](file://mall-server/src/main/java/com/rabbiter/em/constants/Constants.java#L5-L16)
- [CartController.java:71-87](file://mall-server/src/main/java/com/rabbiter/em/controller/CartController.java#L71-L87)
- [OrderController.java:39-48](file://mall-server/src/main/java/com/rabbiter/em/controller/OrderController.java#L39-L48)
- [AddressController.java:32-34](file://mall-server/src/main/java/com/rabbiter/em/controller/AddressController.java#L32-L34)

## 结论
本API文档基于现有控制器与通用封装，提供了从用户认证到订单处理、文件上传、地址与分类管理等模块的完整接口规范。建议后续补充：
- 明确API版本号与兼容策略（如/v1），并在请求头中携带版本信息
- 引入统一的全局异常处理与参数校验框架
- 增加Rate Limiting与WAF防护配置
- 补充OpenAPI/Swagger文档与自动化测试报告

## 附录

### 认证与权限
- 认证机制：基于JWT，登录成功后返回token，后续请求在Header中携带Authorization: Bearer {token}
- 权限注解：
  - requireLogin：登录后可访问
  - requireAuthority：管理员权限
  - noRequire：无需登录
- 示例请求头
  - Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

**章节来源**
- [application.yml:35-36](file://mall-server/src/main/resources/application.yml#L35-L36)
- [Authority.java:10-12](file://mall-server/src/main/java/com/rabbiter/em/annotation/Authority.java#L10-L12)
- [UserController.java:37-41](file://mall-server/src/main/java/com/rabbiter/em/controller/UserController.java#L37-L41)

### 错误码规范
- 200：成功
- 500：系统错误
- 510：未找到结果
- 401：无权限/无效token
- 403：拒绝执行

**章节来源**
- [Constants.java:5-16](file://mall-server/src/main/java/com/rabbiter/em/constants/Constants.java#L5-L16)

### API测试与调试
- 测试工具建议：Postman/Insomnia
- 调试步骤：
  - 先登录获取token
  - 在请求头添加Authorization: Bearer {token}
  - 对需要登录的接口逐个验证参数与权限
  - 对文件上传接口验证文件大小与类型限制
- 建议：为每个模块编写最小化集成测试，覆盖成功与失败分支

### 安全与合规
- 传输安全：生产环境启用HTTPS
- 参数校验：对所有输入参数进行长度、格式与范围校验
- 权限控制：严格区分用户与管理员权限，避免越权
- 速率限制：建议在网关层或应用层实现限流（如每分钟请求数）
- 日志与审计：记录关键操作（登录、下单、支付、删除）以便追踪