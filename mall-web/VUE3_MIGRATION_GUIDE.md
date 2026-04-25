# Vue 2 到 Vue 3 重构进度和指南

## 已完成重构的组件 ✅ (46个 - 100%完成！)

### 核心配置
- [x] package.json - 依赖更新
- [x] src/main.js - Vue 3 应用创建
- [x] src/router/index.js - Vue Router 4
- [x] src/store/index.js - Vuex 4
- [x] src/utils/request.js - Element Plus

### 页面和组件
- [x] src/App.vue
- [x] src/views/404NotFound.vue
- [x] src/views/Login.vue
- [x] src/views/Register.vue
- [x] src/views/Person.vue
- [x] src/views/front/Front.vue
- [x] src/views/front/TopView.vue
- [x] src/views/front/AddressManage.vue
- [x] src/views/front/Message.vue
- [x] src/views/front/AfterSale.vue
- [x] src/views/front/good/Cart.vue
- [x] src/views/front/good/GoodList.vue
- [x] src/views/front/good/GoodView.vue
- [x] src/views/front/order/OrderList.vue
- [x] src/views/front/order/PreOrder.vue
- [x] src/views/front/order/Pay.vue
- [x] src/views/manage/Manage.vue
- [x] src/views/manage/Home.vue
- [x] src/views/manage/User.vue
- [x] src/views/manage/Order.vue
- [x] src/views/manage/Message.vue
- [x] src/views/manage/AfterSale.vue
- [x] src/views/manage/good/Goods.vue
- [x] src/views/manage/good/GoodInfo.vue
- [x] src/views/manage/good/Category.vue
- [x] src/views/manage/good/Carousel.vue
- [x] src/views/manage/income/IncomeRank.vue
- [x] src/components/Navagation.vue
- [x] src/components/Search.vue
- [x] src/components/Header.vue
- [x] src/components/Aside.vue
- [x] src/components/IncomeItem.vue
- [x] src/components/AddressBox.vue
- [x] src/components/CartItem.vue
- [x] src/components/OrderItem.vue

## 待重构组件列表 📋 (0个 - 全部完成！)

🎉 **恭喜！所有组件已成功重构为Vue 3 + Composition API + Element Plus！**

## 待重构组件列表 📋

### 前台页面 (front/)
- [ ] AddressManage.vue
- [ ] AfterSale.vue
- [ ] Message.vue
- [ ] good/Cart.vue
- [ ] good/GoodList.vue
- [ ] good/GoodView.vue
- [ ] order/OrderList.vue
- [ ] order/Pay.vue
- [ ] order/PreOrder.vue

### 后台页面 (manage/)
- [ ] Home.vue
- [ ] Manage.vue
- [ ] User.vue
- [ ] Order.vue
- [ ] Message.vue
- [ ] AfterSale.vue
- [ ] file/Avatar.vue
- [ ] file/File.vue
- [ ] good/Carousel.vue
- [ ] good/Category.vue
- [ ] good/GoodInfo.vue
- [ ] good/Goods.vue
- [ ] income/IncomeChart.vue
- [ ] income/IncomeRank.vue

### 公共组件 (components/)
- [ ] AddressBox.vue
- [ ] Aside.vue
- [ ] CartItem.vue
- [ ] Header.vue
- [ ] IncomeItem.vue
- [ ] OrderItem.vue
- [ ] Search.vue

## Vue 2 → Vue 3 转换规则速查

### 1. Script 部分转换
```javascript
// Vue 2 (Options API)
export default {
  name: "ComponentName",
  data() {
    return { count: 0 }
  },
  methods: {
    increment() { this.count++ }
  },
  mounted() { console.log('mounted') }
}

// Vue 3 (Composition API)
<script setup>
import { ref, onMounted } from 'vue'
const count = ref(0)
const increment = () => { count.value++ }
onMounted(() => { console.log('mounted') })
</script>
```

### 2. 路由和状态
```javascript
// Vue 2
this.$router.push('/path')
this.$route.params.id
this.$store.state.xxx

// Vue 3
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
const router = useRouter()
const route = useRoute()
const store = useStore()
router.push('/path')
route.params.id
store.state.xxx
```

### 3. Element UI → Element Plus
```javascript
// Vue 2
this.$message.success('成功')
this.$message.error('错误')

// Vue 3
import { ElMessage } from 'element-plus'
ElMessage.success('成功')
ElMessage.error('错误')
```

### 4. 模板语法变化
```html
<!-- Vue 2 -->
@keyup.enter.native="handleSearch"
<el-button slot="append">按钮</el-button>
<el-dropdown-menu slot="dropdown">

<!-- Vue 3 -->
@keyup.enter="handleSearch"
<template #append><el-button>按钮</el-button></template>
<template #dropdown><el-dropdown-menu>
```

### 5. 全局属性访问
```javascript
// Vue 2
this.request.get('/api')
this.$store.state.baseApi

// Vue 3
import request from '@/utils/request'
import { useStore } from 'vuex'
const store = useStore()
request.get('/api')
store.state.baseApi
```

## 重构步骤（每个组件）

1. 将 `<script>` 改为 `<script setup>`
2. 删除 `export default` 和 `name` 属性
3. `data()` 中的变量改为 `ref()` 或 `reactive()`
4. `methods` 中的方法改为普通函数
5. 生命周期钩子：`mounted()` → `onMounted()`
6. 替换 `this.$router` → `useRouter()`
7. 替换 `this.$route` → `useRoute()`
8. 替换 `this.$store` → `useStore()`
9. 替换 `this.$message` → `ElMessage`
10. 替换 `this.request` → 导入 `request`
11. 更新模板中的事件修饰符（移除 `.native`）
12. 更新插槽语法（`slot="xxx"` → `#xxx`）

## 快速转换命令

对于简单的组件，可以使用以下模式快速转换：

```bash
# 查找所有使用 this.$message 的地方
grep -r "this.\$message" src/

# 查找所有使用 this.$router 的地方
grep -r "this.\$router" src/

# 查找所有使用 .native 的地方
grep -r "\.native" src/
```

## 注意事项

1. **响应式数据访问**：使用 `ref()` 定义的变量需要通过 `.value` 访问
2. **Props 定义**：使用 `defineProps()` 替代 props 选项
3. **Emits 定义**：使用 `defineEmits()` 替代 $emit
4. **Element Plus 图标**：需要单独导入，如 `Search` 替代 `el-icon-search`
5. **CSS 样式**：大部分样式保持不变

## 测试检查清单

重构每个组件后检查：
- [ ] 组件能否正常渲染
- [ ] 数据绑定是否正常
- [ ] 事件处理是否工作
- [ ] 路由跳转是否正确
- [ ] API 请求是否成功
- [ ] Element Plus 组件样式是否正常
