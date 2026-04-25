# Database Column Cleanup Plan

## Context

Cosplay Mall 系统在前期重构中已移除了租赁功能的所有前后端代码，但数据库中仍残留了与租赁相关的字段和一张无用的重复表。`Order.java` 实体类中已经没有这些字段的定义，因此这些数据库列完全是死数据。本次清理旨在让数据库结构与实际业务代码保持一致。

## 分析结果

### 1. `t_order` 表 - 4个可删除字段

| 字段名 | 类型 | 当前值 | 原因 |
|---|---|---|---|
| `type` | int(11), default 0 | 全部为默认值 | 租赁订单类型标识，已废弃 |
| `rental_days` | int(11), default 0 | 全部为默认值 | 租赁天数，已废弃 |
| `deposit` | decimal(10,2), default 0.00 | 全部为默认值 | 租赁押金，已废弃 |
| `return_state` | varchar(50), nullable | 全部为NULL | 租赁归还状态，已废弃 |

**验证**: `Order.java` 实体类中不包含这4个字段。全项目搜索 `rental_days`/`deposit`/`return_state` 仅在历史文档/SQL脚本中出现，无业务代码引用。

### 2. `standard` 表 - 可整表删除

- 表结构：`goodId(bigint)`, `value(varchar)`, `price(decimal)`, `store(bigint)` - 列名为 camelCase
- `good_standard` 表：`good_id(bigint)`, `value(varchar)`, `price(decimal)`, `store(bigint)` - 列名为 snake_case
- `standard` 表数据量: **0行**（空表）
- 所有代码（`Standard.java` 通过 `@TableName("good_standard")` 注解）均映射到 `good_standard` 表
- **无任何代码引用 `standard` 表**

### 3. 其他表字段 - 全部在使用中

经逐一核查，`address`, `after_sale`, `avatar`, `carousel`, `cart`, `category`, `comment`, `good`, `good_standard`, `icon`, `icon_category`, `message`, `notice`, `order_goods`, `sys_file`, `sys_user` 表的所有字段均有对应的实体类定义和业务代码引用，不可删除。

## 实施步骤

### Step 1: 创建并执行 SQL 脚本

创建 `db_column_cleanup.sql`：

```sql
-- 1. 删除 t_order 表中4个废弃的租赁字段
ALTER TABLE `t_order` DROP COLUMN `type`;
ALTER TABLE `t_order` DROP COLUMN `rental_days`;
ALTER TABLE `t_order` DROP COLUMN `deposit`;
ALTER TABLE `t_order` DROP COLUMN `return_state`;

-- 2. 删除废弃的空表 standard
DROP TABLE IF EXISTS `standard`;
```

执行脚本：`mysql -uroot -p123456 s003 < db_column_cleanup.sql`

### Step 2: 删除重复的 GoodStandard.java（可选）

`GoodStandard.java` 是 `Standard.java` 的功能重复，两者都映射 `good_standard` 表。可以将 `GoodMapper` 中返回类型从 `GoodStandard` 改为 `Standard`，然后删除 `GoodStandard.java`。

涉及文件：
- `mall-server/src/main/java/com/rabbiter/em/entity/GoodStandard.java` (删除)
- `mall-server/src/main/java/com/rabbiter/em/mapper/GoodMapper.java` (改 `List<GoodStandard>` -> `List<Standard>`)
- `mall-server/src/main/java/com/rabbiter/em/service/GoodService.java` (改 import 和类型引用)

## 验证

1. 执行 SQL 后用 `SHOW COLUMNS FROM t_order` 确认4个字段已不存在
2. 用 `SHOW TABLES` 确认 `standard` 表已不存在
3. 后端编译：`cd mall-server && mvn compile`
4. 启动后端，测试订单相关接口（创建订单、查询订单）确认无报错
