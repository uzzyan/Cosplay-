<template>
<div class="order-item-card">
  <!-- 订单头部 -->
  <div class="order-header">
    <div class="header-left">
      <i class="el-icon-time"></i>
      <span class="order-time">{{ order.create_time }}</span>
    </div>
    <div class="header-right">
      <i class="el-icon-document-copy"></i>
      <span class="order-no">订单号：{{ order.order_no }}</span>
    </div>
  </div>

  <!-- 订单内容 -->
  <div class="order-content">
    <!-- 商品图片 -->
    <div class="product-image">
      <router-link :to="'goodview/'+order.good_id">
        <img :src="baseApi + order.imgs" alt="商品图片">
      </router-link>
    </div>

    <!-- 商品信息 -->
    <div class="product-info">
      <router-link :to="'goodview/'+order.good_id" class="product-name">
        {{ order.good_name }}
      </router-link>
      <div class="product-spec">
        <span class="spec-label">规格：</span>
        <span class="spec-value">{{ order.standard }}</span>
      </div>
    </div>

    <!-- 数量 -->
    <div class="info-section">
      <div class="label">数量</div>
      <div class="value">× {{ order.count }}</div>
    </div>

    <!-- 总价 -->
    <div class="info-section">
      <div class="label">总价</div>
      <div class="total-price">¥{{ order.total_price }}</div>
    </div>

    <!-- 收货人 -->
    <div class="info-section">
      <div class="label">收货人</div>
      <el-popover
        placement="bottom-start"
        width="250"
        trigger="hover"
        :content="address"
      >
        <template #reference>
          <div class="receiver-name">
            <i class="el-icon-user"></i>
            {{ order.link_user }}
          </div>
        </template>
      </el-popover>
    </div>

    <!-- 订单状态 -->
    <div class="info-section">
      <div class="label">状态</div>
      <div class="status-value">
        <el-tag
          v-if="order.state==='已发货'"
          type="success"
          effect="plain"
          size="default"
        >
          <i class="el-icon-truck"></i> {{ order.state }}
        </el-tag>
        <el-tag
          v-else-if="order.state==='已收货'"
          type="success"
          size="default"
        >
          <i class="el-icon-check"></i> {{ order.state }}
        </el-tag>
        <el-tag
          v-else-if="order.state==='已支付'"
          type="primary"
          size="default"
        >
          <i class="el-icon-circle-check"></i> {{ order.state }}
        </el-tag>
        <el-tag
          v-else
          type="warning"
          size="default"
        >
          <i class="el-icon-warning"></i> {{ order.state }}
        </el-tag>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="actions">
      <el-button
        v-if="order.state==='已发货'"
        type="primary"
        @click="receive"
        size="default"
        round
      >
        <el-icon><Check /></el-icon>
        确认收货
      </el-button>
      <el-button
        v-else-if="order.state==='已支付'"
        type="info"
        plain
        disabled
        size="default"
        round
      >
        待发货
      </el-button>
      <el-button
        v-else-if="order.state!=='已收货'"
        type="success"
        @click="pay"
        size="default"
        round
      >
        <el-icon><Wallet /></el-icon>
        去支付
      </el-button>
    </div>
  </div>
</div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Wallet } from '@element-plus/icons-vue'
import { useStore } from 'vuex'
import request from '@/utils/request'

const store = useStore()
const router = useRouter()

const props = defineProps({
  order: Object
})

const baseApi = store.state.baseApi
const address = computed(() => {
  return '电话:' + props.order.link_phone + ' 地址:' + props.order.link_address
})

//跳转到支付页面
const pay = () => {
  router.push({ path: 'pay', query: { money: props.order.total_price, orderNo: props.order.order_no } })
}

//确认收货
const receive = () => {
  ElMessageBox.confirm('是否确认收货?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    request.post('/api/order/received/' + props.order.order_no).then(res => {
      if (res.code === '200') {
        ElMessage.success('收货成功')
        props.order.state = '已收货'
      }
    })
  })
}
</script>

<style scoped>
.order-item-card {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: all 0.3s ease;
}

.order-item-card:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

/* 订单头部 */
.order-header {
  background: #ffe0e0;
  padding: 12px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  font-weight: 500;
}

.header-left,
.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
}

.header-left i,
.header-right i {
  color: #F56C6C;
  font-size: 16px;
}

.order-time {
  font-weight: 600;
}

.order-no {
  font-family: monospace;
}

/* 订单内容 */
.order-content {
  display: flex;
  align-items: center;
  padding: 25px;
  gap: 25px;
}

/* 商品图片 */
.product-image {
  flex-shrink: 0;
}

.product-image img {
  width: 120px;
  height: 120px;
  border-radius: 12px;
  object-fit: cover;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.product-image img:hover {
  transform: scale(1.05);
}

/* 商品信息 */
.product-info {
  flex: 1;
  min-width: 180px;
}

.product-name {
  display: block;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  text-decoration: none;
  transition: color 0.3s;
  line-height: 1.4;
}

.product-name:hover {
  color: #409EFF;
}

.product-spec {
  font-size: 14px;
  color: #909399;
}

.spec-label {
  color: #606266;
  font-weight: 500;
}

.spec-value {
  color: #67C23A;
  font-weight: 500;
}

/* 信息区域 */
.info-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  min-width: 100px;
}

.label {
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}

.value {
  font-size: 16px;
  color: #606266;
  font-weight: 600;
}

.total-price {
  font-size: 22px;
  color: #E6A23C;
  font-weight: 700;
}

.receiver-name {
  color: #67C23A;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: color 0.3s;
}

.receiver-name:hover {
  color: #85ce61;
}

.status-value {
  display: flex;
  justify-content: center;
}

/* 操作按钮 */
.actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 110px;
}

.actions .el-button {
  width: 100%;
  font-weight: 500;
}

/* 响应式 */
@media (max-width: 1024px) {
  .order-content {
    flex-wrap: wrap;
    gap: 20px;
  }

  .product-info {
    flex-basis: 100%;
  }
}

@media (max-width: 768px) {
  .order-header {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }

  .order-content {
    flex-direction: column;
    padding: 20px;
  }

  .product-image img {
    width: 100px;
    height: 100px;
  }

  .info-section {
    flex-direction: row;
    justify-content: space-between;
    width: 100%;
  }

  .actions {
    width: 100%;
  }
}
</style>
