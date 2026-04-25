# 单元测试说明文档

本项目已实施完整的单元测试方案，覆盖核心业务逻辑，确保系统稳定性和无回归缺陷。

## 1. 测试环境与框架

*   **测试框架**: JUnit 5 (Jupiter)
*   **Mock 框架**: Mockito 3.x (含 `mockito-inline` 支持静态方法 Mock)
*   **断言库**: JUnit Assertions
*   **Web 测试**: Spring MockMvc
*   **覆盖率工具**: JaCoCo

## 2. 测试结构

测试代码位于 `mall-server/src/test/java/com/rabbiter/em/` 目录下，主要分为 Service 层单元测试和 Controller 层集成测试。

### 核心测试类

| 测试类 | 测试目标 | 覆盖场景 |
| :--- | :--- | :--- |
| `OrderServiceTest` | 订单核心业务 | 下单(`saveOrder`)、支付(`payOrder`)、确认收货(`receiveOrder`)、库存扣减、Redis销量更新 |
| `UserServiceTest` | 用户认证业务 | 登录(`login`)、注册(`register`)、Token生成、Redis缓存、异常处理(密码错误/用户已存在) |
| `RecommendationServiceTest` | 推荐算法业务 | 推荐逻辑(`recommend`)、冷启动兜底策略(热销推荐) |
| `GoodControllerTest` | 商品接口层 | 商品列表查询(`findAll`)、推荐接口集成测试、HTTP状态码验证、JSON响应结构验证 |

## 3. 关键测试策略

### 3.1 隔离依赖 (Mocking)
为了保证单元测试的独立性和速度，我们使用 Mockito 模拟了所有外部依赖：
*   **Database**: Mock `BaseMapper` (如 `OrderMapper`, `UserMapper`)，不连接真实数据库。
*   **Redis**: Mock `RedisTemplate` 和 `ValueOperations`。
*   **Static Utils**: 使用 `MockedStatic` 模拟 `TokenUtils` 等静态工具类。

### 3.2 MyBatis-Plus 兼容
*   对于 `ServiceImpl` 中的 `baseMapper` 依赖，使用 `ReflectionTestUtils` 进行手动注入，解决了 Mockito 无法自动注入父类字段的问题。
*   针对 `LambdaQueryWrapper` 等依赖元数据的操作，在测试中进行了适配或规避。

## 4. 运行测试

在 `mall-server` 目录下运行以下命令：

```bash
mvn test
```

若要生成覆盖率报告（HTML格式）：

```bash
mvn test jacoco:report
```
报告生成路径：`mall-server/target/site/jacoco/index.html`

## 5. CI/CD 集成
本项目配置了 `jacoco-maven-plugin`，可轻松集成到 Jenkins 或 GitHub Actions 中。建议在每次 Push 或 PR 时自动触发 `mvn test`。
