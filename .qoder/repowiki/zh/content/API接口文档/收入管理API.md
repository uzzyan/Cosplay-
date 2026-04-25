# 收入管理API

<cite>
**本文引用的文件**
- [IncomeController.java](file://mall-server/src/main/java/com/rabbiter/em/controller/IncomeController.java)
- [IncomeService.java](file://mall-server/src/main/java/com/rabbiter/em/service/IncomeService.java)
- [GoodController.java](file://mall-server/src/main/java/com/rabbiter/em/controller/GoodController.java)
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

收入管理模块提供商城营收数据统计和分析功能，包括收入图表、周收入统计、月收入统计以及商品销量排行等数据接口，帮助管理员了解经营状况。

## API接口列表

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 获取收入图表数据 | GET | /api/income/chart | requireAuthority | 获取各类别收入统计 |
| 获取本周收入 | GET | /api/income/week | requireAuthority | 获取本周每日收入 |
| 获取本月收入 | GET | /api/income/month | requireAuthority | 获取本月每日收入 |
| 获取商品销量排行 | GET | /api/good/rank?num=10 | noRequire | 获取销量最高的商品 |

## 详细接口说明

### 1. 获取收入图表数据

获取按商品分类统计的收入数据，用于绘制柱状图和饼图。

**请求**
- 方法：`GET`
- 路径：`/api/income/chart`
- 权限：requireAuthority

**成功响应**
```json
{
  "code": "200",
  "msg": null,
  "data": {
    "categoryIncomes": [
      {
        "categoryName": "Cosplay服装",
        "categoryIncome": 15000.00
      },
      {
        "categoryName": "动漫配件",
        "categoryIncome": 8000.00
      },
      {
        "categoryName": "道具武器",
        "categoryIncome": 5000.00
      }
    ]
  }
}
```

**响应字段说明**
| 字段 | 类型 | 说明 |
|------|------|------|
| categoryIncomes | Array | 分类收入列表 |
| categoryName | String | 分类名称 |
| categoryIncome | Double | 该分类总收入 |

**业务逻辑**
1. 查询所有已支付订单
2. 按商品分类分组
3. 计算每个分类的总收入
4. 返回分类收入列表

### 2. 获取本周收入

获取本周（周一至周日）每天的收入数据，用于绘制折线图。

**请求**
- 方法：`GET`
- 路径：`/api/income/week`
- 权限：requireAuthority

**成功响应**
```json
{
  "code": "200",
  "msg": null,
  "data": {
    "weekIncome": [1200.50, 1500.00, 1800.75, 1600.00, 2000.00, 2500.50, 2200.00]
  }
}
```

**响应字段说明**
| 字段 | 类型 | 说明 |
|------|------|------|
| weekIncome | Array[7] | 周一到周日的收入数组 |

**业务逻辑**
1. 计算本周一和周日的日期
2. 查询本周内已支付订单
3. 按日期分组统计
4. 返回7天的收入数组

### 3. 获取本月收入

获取本月每天的收入数据，用于绘制折线图。

**请求**
- 方法：`GET`
- 路径：`/api/income/month`
- 权限：requireAuthority

**成功响应**
```json
{
  "code": "200",
  "msg": null,
  "data": {
    "monthDays": ["12-01", "12-02", "12-03", ...],
    "monthIncome": [1200.50, 1500.00, 1800.75, ...]
  }
}
```

**响应字段说明**
| 字段 | 类型 | 说明 |
|------|------|------|
| monthDays | Array | 日期数组（MM-DD格式） |
| monthIncome | Array | 对应日期的收入数组 |

**业务逻辑**
1. 计算本月第一天和最后一天
2. 查询本月内已支付订单
3. 按日期分组统计
4. 返回日期和收入数组

### 4. 获取商品销量排行

获取销量最高的商品列表，可选数量。

**请求**
- 方法：`GET`
- 路径：`/api/good/rank?num=10`
- 权限：noRequire（公开接口）

**查询参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| num | int | 否 | 返回数量（默认10） |

**成功响应**
```json
{
  "code": "200",
  "msg": null,
  "data": [
    {
      "id": 123,
      "name": "初音未来Cosplay服装",
      "description": "高品质初音未来服装",
      "discount": 0.8,
      "sales": 150,
      "saleMoney": 45000.00,
      "categoryId": 1,
      "imgs": "/file/good123.jpg",
      "createTime": "2025-10-01 10:00:00",
      "recommend": true,
      "status": 1
    }
  ]
}
```

**响应字段说明**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 商品ID |
| name | String | 商品名称 |
| sales | Integer | 销量 |
| saleMoney | Double | 销售额 |
| price | Double | 单价 |
| discount | Double | 折扣 |

**业务逻辑**
1. 查询所有商品
2. 按销量降序排序
3. 返回前N个商品

## 数据模型

### 收入图表数据

```json
{
  "categoryIncomes": [
    {
      "categoryName": "分类名称",
      "categoryIncome": 收入金额
    }
  ]
}
```

### 周收入数据

```json
{
  "weekIncome": [周一收入, 周二收入, ..., 周日收入]
}
```

### 月收入数据

```json
{
  "monthDays": ["日期1", "日期2", ...],
  "monthIncome": [收入1, 收入2, ...]
}
```

### 商品排行数据

```json
{
  "id": 123,
  "name": "商品名称",
  "sales": 销量,
  "saleMoney": 销售额,
  "price": 单价,
  "discount": 折扣
}
```

## 权限控制

| 接口 | 权限级别 | 说明 |
|------|---------|------|
| 获取收入图表数据 | requireAuthority | 仅管理员可访问 |
| 获取本周收入 | requireAuthority | 仅管理员可访问 |
| 获取本月收入 | requireAuthority | 仅管理员可访问 |
| 获取商品销量排行 | noRequire | 公开接口，无需登录 |

## 错误码

| 错误码 | 说明 | 触发场景 |
|--------|------|---------|
| 200 | 成功 | 操作成功 |
| 401 | 未授权 | 未登录或token无效 |
| 403 | 无权限 | 普通用户调用收入接口 |
| 500 | 系统错误 | 服务器内部错误 |

## 使用示例

### 前端Vue 3调用示例

```vue
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import request from '@/utils/request'

const totalIncome = ref(0)
const chartRef = ref(null)
let chartInstance = null

// 获取收入图表数据
const loadChart = async () => {
  try {
    const res = await request.get('/api/income/chart')
    if (res.code === '200') {
      const categoryIncomes = res.data.categoryIncomes
      const categoryNames = categoryIncomes.map(item => item.categoryName)
      const incomes = categoryIncomes.map(item => item.categoryIncome)
      
      // 计算总收入
      totalIncome.value = incomes.reduce((sum, val) => sum + val, 0)
      
      // 绘制柱状图
      renderBarChart(categoryNames, incomes)
    }
  } catch (err) {
    console.error('加载收入图表失败:', err)
    ElMessage.error('加载失败，请重试')
  }
}

// 获取本周收入
const loadWeekIncome = async () => {
  try {
    const res = await request.get('/api/income/week')
    if (res.code === '200') {
      const weekIncome = res.data.weekIncome
      // 绘制本周收入折线图
      renderWeekLineChart(weekIncome)
    }
  } catch (err) {
    console.error('加载本周收入失败:', err)
    ElMessage.error('加载失败，请重试')
  }
}

// 获取本月收入
const loadMonthIncome = async () => {
  try {
    const res = await request.get('/api/income/month')
    if (res.code === '200') {
      const { monthDays, monthIncome } = res.data
      // 绘制本月收入折线图
      renderMonthLineChart(monthDays, monthIncome)
    }
  } catch (err) {
    console.error('加载本月收入失败:', err)
    ElMessage.error('加载失败，请重试')
  }
}

// 获取商品销量排行
const loadRank = async (num = 10) => {
  try {
    const res = await request.get('/api/good/rank', {
      params: { num }
    })
    if (res.code === '200') {
      return res.data
    }
  } catch (err) {
    console.error('加载商品排行失败:', err)
    ElMessage.error('加载失败，请重试')
  }
}

// 绘制柱状图
const renderBarChart = (names, incomes) => {
  if (!chartRef.value) return
  
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  
  const option = {
    title: { text: '收入统计柱状图', left: 'center' },
    tooltip: { trigger: 'item' },
    xAxis: {
      type: 'category',
      data: names
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      data: incomes,
      type: 'bar',
      label: { show: true, position: 'top' }
    }]
  }
  
  chartInstance.setOption(option)
}

onMounted(() => {
  loadChart()
  loadWeekIncome()
  loadMonthIncome()
})

// 组件销毁时清理图表
import { onBeforeUnmount } from 'vue'
onBeforeUnmount(() => {
  if (chartInstance) {
    chartInstance.dispose()
  }
})
</script>

<template>
  <div>
    <div class="total-income">
      总计：¥{{ totalIncome.toFixed(2) }}
    </div>
    <div ref="chartRef" style="width: 100%; height: 500px;"></div>
  </div>
</template>
```

## 附录

### 相关文件

- 收入控制器：[IncomeController.java](file://mall-server/src/main/java/com/rabbiter/em/controller/IncomeController.java)
- 收入服务层：[IncomeService.java](file://mall-server/src/main/java/com/rabbiter/em/service/IncomeService.java)
- 商品控制器：[GoodController.java](file://mall-server/src/main/java/com/rabbiter/em/controller/GoodController.java)
- 前端页面：[IncomeChart.vue](file://mall-web/src/views/manage/income/IncomeChart.vue)
- 前端页面：[IncomeRank.vue](file://mall-web/src/views/manage/income/IncomeRank.vue)

### 收入统计说明

- **统计范围**：仅统计已支付订单（state='已支付'）
- **计算方式**：按订单实际支付金额累加
- **时间维度**：
  - 图表数据：按商品分类
  - 周收入：按本周每天
  - 月收入：按本月每天

### ECharts集成建议

1. **柱状图**：用于展示分类收入对比
2. **饼图**：用于展示分类收入占比
3. **折线图**：用于展示收入趋势

```javascript
// 饼图配置
const pieOption = {
  title: { text: '收入统计饼图', left: 'center' },
  tooltip: { trigger: 'item' },
  series: [{
    type: 'pie',
    data: categoryIncomes.map(item => ({
      value: item.categoryIncome,
      name: item.categoryName
    }))
  }]
}
```
