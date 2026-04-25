# 留言管理API

<cite>
**本文引用的文件**
- [MessageController.java](file://mall-server/src/main/java/com/rabbiter/em/controller/MessageController.java)
- [MessageService.java](file://mall-server/src/main/java/com/rabbiter/em/service/MessageService.java)
- [Message.java](file://mall-server/src/main/java/com/rabbiter/em/entity/Message.java)
</cite>

## 目录
1. [简介](#简介)
2. [API接口列表](#api接口列表)
3. [详细接口说明](#详细接口说明)
4. [数据模型](#数据模型)
5. [权限控制](#权限控制)
6. [错误码](#错误码)
7. [使用示例](#使用示例)

## 简介

留言管理模块提供用户在线留言和管理员回复留言的功能。支持普通留言和定制意图两种类型，包含创建、查询、回复、删除等完整功能。

## API接口列表

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 创建留言 | POST | /api/message | requireLogin | 用户创建留言 |
| 查询我的留言 | GET | /api/message/mine/page | requireLogin | 查询当前用户的留言列表 |
| 查询留言详情 | GET | /api/message/{id} | requireLogin | 查询留言详情 |
| 查询所有留言 | GET | /api/message/page | requireAuthority | 管理员查询所有留言 |
| 回复留言 | PUT | /api/message/reply | requireAuthority | 管理员回复留言 |
| 删除留言 | DELETE | /api/message/{id} | requireAuthority | 管理员删除留言 |

## 详细接口说明

### 1. 创建留言

用户提交在线留言，包括标题、内容、联系方式等信息。

**请求**
- 方法：`POST`
- 路径：`/api/message`
- 权限：requireLogin
- Content-Type：`application/json`

**请求体**
```json
{
  "title": "商品咨询",
  "content": "请问这款Cosplay服装有XL码吗？",
  "contact": "user@example.com",
  "customIntent": false
}
```

**参数说明**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | String | 是 | 留言标题 |
| content | String | 是 | 留言内容 |
| contact | String | 是 | 联系方式 |
| customIntent | Boolean | 否 | 是否为定制意图（默认false） |

**成功响应**
```json
{
  "code": "200",
  "msg": null,
  "data": null
}
```

**错误响应**
```json
{
  "code": "400",
  "msg": "标题不能为空",
  "data": null
}
```

或

```json
{
  "code": "400",
  "msg": "内容不能为空",
  "data": null
}
```

或

```json
{
  "code": "400",
  "msg": "联系方式不能为空",
  "data": null
}
```

**业务逻辑**
1. 验证用户登录状态
2. 校验必填字段（标题、内容、联系方式）
3. 设置用户ID和创建时间
4. 设置默认customIntent为false
5. 清空回复相关字段
6. 保存到数据库

### 2. 查询我的留言

查询当前登录用户的留言列表（分页）。

**请求**
- 方法：`GET`
- 路径：`/api/message/mine/page?pageNum=1&pageSize=10`
- 权限：requireLogin

**查询参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | int | 是 | 页码 |
| pageSize | int | 是 | 每页数量 |

**成功响应**
```json
{
  "code": "200",
  "msg": null,
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 1001,
        "title": "商品咨询",
        "content": "请问有XL码吗？",
        "contact": "user@example.com",
        "customIntent": false,
        "createTime": "2025-12-01 10:00:00",
        "reply": "有的，请选择尺码下单",
        "replyTime": "2025-12-01 14:00:00"
      }
    ],
    "total": 1
  }
}
```

### 3. 查询留言详情

查询指定留言的详细信息。

**请求**
- 方法：`GET`
- 路径：`/api/message/{id}`
- 权限：requireLogin

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 留言ID |

**成功响应**
```json
{
  "code": "200",
  "msg": null,
  "data": {
    "id": 1,
    "userId": 1001,
    "title": "商品咨询",
    "content": "请问有XL码吗？",
    "contact": "user@example.com",
    "customIntent": false,
    "createTime": "2025-12-01 10:00:00",
    "reply": "有的，请选择尺码下单",
    "replyTime": "2025-12-01 14:00:00"
  }
}
```

**错误响应**
```json
{
  "code": "404",
  "msg": "未找到留言",
  "data": null
}
```

或

```json
{
  "code": "403",
  "msg": "无权限",
  "data": null
}
```

### 4. 查询所有留言（管理员）

管理员查询所有用户的留言，支持条件筛选。

**请求**
- 方法：`GET`
- 路径：`/api/message/page?pageNum=1&pageSize=10&customIntent=&searchText=`
- 权限：requireAuthority

**查询参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | int | 是 | 页码 |
| pageSize | int | 是 | 每页数量 |
| customIntent | Boolean | 否 | 筛选定制意图留言 |
| searchText | String | 否 | 搜索关键词（标题/内容） |

**成功响应**
```json
{
  "code": "200",
  "msg": null,
  "data": {
    "records": [...],
    "total": 100
  }
}
```

### 5. 回复留言（管理员）

管理员回复用户的留言。

**请求**
- 方法：`PUT`
- 路径：`/api/message/reply`
- 权限：requireAuthority
- Content-Type：`application/json`

**请求体**
```json
{
  "id": 1,
  "reply": "您好，XL码有货的，请直接下单。"
}
```

**参数说明**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 留言ID |
| reply | String | 是 | 回复内容 |

**成功响应**
```json
{
  "code": "200",
  "msg": null,
  "data": null
}
```

**错误响应**
```json
{
  "code": "400",
  "msg": "回复内容不能为空",
  "data": null
}
```

或

```json
{
  "code": "404",
  "msg": "未找到留言",
  "data": null
}
```

**业务逻辑**
1. 验证管理员权限
2. 校验回复内容非空
3. 查找留言
4. 更新回复内容和回复时间
5. 保存到数据库

### 6. 删除留言（管理员）

管理员删除留言。

**请求**
- 方法：`DELETE`
- 路径：`/api/message/{id}`
- 权限：requireAuthority

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 留言ID |

**成功响应**
```json
{
  "code": "200",
  "msg": null,
  "data": null
}
```

**错误响应**
```json
{
  "code": "500",
  "msg": "删除失败",
  "data": null
}
```

## 数据模型

### Message实体

```java
public class Message {
    private Long id;
    private Long userId;              // 用户ID
    private String title;             // 标题
    private String content;           // 内容
    private String contact;           // 联系方式
    private Boolean customIntent;     // 是否定制意图
    private String createTime;        // 创建时间
    private String reply;             // 回复内容
    private String replyTime;         // 回复时间
}
```

**字段说明**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 留言ID |
| userId | Long | 是 | 用户ID |
| title | String | 是 | 留言标题 |
| content | String | 是 | 留言内容 |
| contact | String | 是 | 联系方式 |
| customIntent | Boolean | 否 | 是否为定制意图 |
| createTime | String | 是 | 创建时间 |
| reply | String | 否 | 管理员回复内容 |
| replyTime | String | 否 | 回复时间 |

## 权限控制

| 接口 | 权限级别 | 说明 |
|------|---------|------|
| 创建留言 | requireLogin | 登录用户可创建 |
| 查询我的留言 | requireLogin | 只能查看自己的留言 |
| 查询留言详情 | requireLogin | 只能查看自己的，管理员可查看所有 |
| 查询所有留言 | requireAuthority | 仅管理员可访问 |
| 回复留言 | requireAuthority | 仅管理员可回复 |
| 删除留言 | requireAuthority | 仅管理员可删除 |

## 错误码

| 错误码 | 说明 | 触发场景 |
|--------|------|---------|
| 200 | 成功 | 操作成功 |
| 400 | 参数错误 | 必填字段为空 |
| 401 | 未授权 | 未登录或token无效 |
| 403 | 无权限 | 查看他人留言或普通用户调用管理接口 |
| 404 | 未找到 | 留言不存在 |
| 500 | 系统错误 | 服务器内部错误 |

## 使用示例

### 前端Vue 3调用示例

```vue
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 创建留言
const createMessage = async (formData) => {
  try {
    const res = await request.post('/api/message', formData)
    if (res.code === '200') {
      ElMessage.success('留言提交成功')
      load()
    } else {
      ElMessage.error(res.msg || '留言失败')
    }
  } catch (err) {
    console.error('创建留言失败:', err)
    ElMessage.error('留言失败，请重试')
  }
}

// 查询我的留言
const loadMine = async () => {
  try {
    const res = await request.get('/api/message/mine/page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value
      }
    })
    if (res.code === '200') {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (err) {
    console.error('加载留言列表失败:', err)
    ElMessage.error('加载失败，请重试')
  }
}

// 回复留言（管理员）
const replyMessage = async (id, reply) => {
  try {
    const res = await request.put('/api/message/reply', {
      id,
      reply
    })
    if (res.code === '200') {
      ElMessage.success('回复成功')
      load()
    } else {
      ElMessage.error(res.msg || '回复失败')
    }
  } catch (err) {
    console.error('回复留言失败:', err)
    ElMessage.error('回复失败，请重试')
  }
}

// 删除留言（管理员）
const deleteMessage = async (id) => {
  try {
    const res = await request.delete(`/api/message/${id}`)
    if (res.code === '200') {
      ElMessage.success('删除成功')
      load()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (err) {
    console.error('删除留言失败:', err)
    ElMessage.error('删除失败，请重试')
  }
}

onMounted(() => {
  loadMine()
})
</script>
```

## 附录

### 相关文件

- 控制器：[MessageController.java](file://mall-server/src/main/java/com/rabbiter/em/controller/MessageController.java)
- 服务层：[MessageService.java](file://mall-server/src/main/java/com/rabbiter/em/service/MessageService.java)
- 实体类：[Message.java](file://mall-server/src/main/java/com/rabbiter/em/entity/Message.java)
- 前端页面：[Message.vue](file://mall-web/src/views/front/Message.vue)
- 管理页面：[Message.vue](file://mall-web/src/views/manage/Message.vue)

### 留言类型说明

- **普通留言**：`customIntent = false`，一般咨询或反馈
- **定制意图**：`customIntent = true`，用户有定制需求
