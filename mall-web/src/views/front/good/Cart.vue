
<template>
<div class="cart-container">
  <div class="content-wrapper">
    <!-- 页面标题 -->
    <div class="page-title-section">
      <div class="title-content">
        <i class="el-icon-shopping-cart-2 title-icon"></i>
        <h1 class="page-title">我的购物车</h1>
        <span class="cart-count" v-if="carts.length > 0">{{ carts.length }} 件商品</span>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="carts.length === 0" class="empty-box">
      <i class="el-icon-shopping-cart-full empty-icon"></i>
      <p class="empty-title">购物车是空的哦</p>
      <p class="empty-desc">快去选购心仪的商品吧~</p>
      <el-button type="primary" size="medium" @click="router.push('/goodList')" round>
        去逛逛
      </el-button>
    </div>

    <!-- 购物车列表 -->
    <div v-else class="cart-list">
      <cart-item 
        v-for="cart in carts" 
        :key="cart.id"
        :cart="cart" 
        @delete="delItem"
      ></cart-item>
    </div>
  </div>
</div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import CartItem from '@/components/CartItem.vue'
import request from '@/utils/request'

const router = useRouter()

const userId = ref(0)
const carts = ref([])

const delItem = (id) => {
  carts.value = carts.value.filter(item => item.id != id)
}

onMounted(() => {
  request.get('/userid').then(res => {
    userId.value = res
    request.get('/api/cart/userid/' + userId.value).then(res => {
      if (res.code === '200') {
        carts.value = res.data
        // createTime 已经是格式化后的字符串，无需再次转换
      }
    })
  })
})
</script>

<style scoped>
.cart-container {
  padding: 30px 0;
}

.content-wrapper {
  width: 90%;
  max-width: 1400px;
  margin: 0 auto;
  background-color: white;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

/* 页面标题 */
.page-title-section {
  margin-bottom: 30px;
}

.title-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.title-icon {
  font-size: 36px;
  color: #409EFF;
}

.page-title {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.cart-count {
  background: #409EFF;
  color: white;
  padding: 6px 18px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

/* 购物车列表 */
.cart-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 空状态 */
.empty-box {
  text-align: center;
  padding: 50px 40px;
}

.empty-icon {
  font-size: 120px;
  color: #dcdfe6;
  margin-bottom: 20px;
  display: block;
}

.empty-title {
  font-size: 24px;
  color: #606266;
  margin: 15px 0 10px;
  font-weight: 500;
}

.empty-desc {
  font-size: 16px;
  color: #909399;
  margin-bottom: 30px;
}

/* 响应式 */
@media (max-width: 768px) {
  .cart-container {
    padding: 20px 10px;
  }
  
  .title-content {
    padding: 15px 20px;
  }
  
  .page-title {
    font-size: 22px;
  }
}
</style>
