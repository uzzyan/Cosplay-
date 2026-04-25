
<template>
<div class="order-container">
  <div class="content-wrapper">
    <!-- 页面标题 -->
    <div class="page-title-section">
      <div class="title-content">
        <i class="el-icon-document title-icon"></i>
        <h1 class="page-title">我的订单</h1>
        <span class="order-count" v-if="orders.length > 0">{{ orders.length }} 条订单</span>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="orders.length === 0" class="empty-box">
      <i class="el-icon-folder-opened empty-icon"></i>
      <p class="empty-title">没有订单记录</p>
      <p class="empty-desc">您还没有购买过商品哦~</p>
      <el-button type="primary" size="medium" @click="router.push('/goodList')" round>
        去购物
      </el-button>
    </div>

    <!-- 订单列表 -->
    <div v-else class="order-list">
      <div style="display: flex; justify-content: flex-end">
        <el-button type="success" size="medium" @click="router.push('/afterSale')" round>退款 / 售后申请</el-button>
      </div>
      <order-item
        v-for="order in orders"
        :key="order.id"
        :order="order"
      ></order-item>
    </div>
  </div>
</div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import OrderItem from '@/components/OrderItem.vue'
import API from '@/utils/request'

const router = useRouter()

const orders = ref({})

onMounted(() => {
  API.get('/userid').then(res => {
    API.get('/api/order/userid/' + res).then(res => {
      if (res.code === '200') {
        orders.value = res.data
        for (var i = 0; i < orders.value.length; ++i) {
          orders.value[i].create_time = orders.value[i].create_time.toLocaleString().replace(/T/g, ' ').replace(/\.[\d]{3}Z/, '')
        }
      }
    })
  })
})
</script>

<style scoped>
.order-container {
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
  color: #F56C6C;
}

.page-title {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.order-count {
  background: #F56C6C;
  color: white;
  padding: 6px 18px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(245, 108, 108, 0.3);
}

/* 订单列表 */
.order-list {
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
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-15px); }
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
  .order-container {
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
