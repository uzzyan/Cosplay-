<template>
  <div class="cart-item-card">
    <!-- 时间标签 -->
    <div class="time-badge">
      <i class="el-icon-time"></i>
      <span>{{ cart.createTime }}</span>
    </div>

    <!-- 卡片内容 -->
    <div class="card-content">
      <!-- 商品图片 -->
      <div class="product-image">
        <router-link :to="'goodview/'+cart.goodId">
          <img :src="baseApi + cart.img" alt="商品图片">
        </router-link>
      </div>

      <!-- 商品信息 -->
      <div class="product-info">
        <router-link :to="'goodview/'+cart.goodId" class="product-name">
          {{ cart.goodName }}
        </router-link>
        <div class="product-spec">
          <span class="spec-label">规格：</span>
          <span class="spec-value">{{ cart.standard }}</span>
        </div>
      </div>

      <!-- 单价 -->
      <div class="price-section">
        <div class="label">单价</div>
        <div class="price">¥{{ realPrice.toFixed(2) }}</div>
      </div>

      <!-- 数量 -->
      <div class="quantity-section">
        <div class="label">数量</div>
        <div class="quantity-control">
          <el-button 
            v-if="!countChangeFlag" 
            @click="countChangeFlag=true"
            class="quantity-btn"
            size="small"
          >
            {{ cart.count }}
          </el-button>
          <el-input-number 
            v-else
            v-model="cart.count" 
            :min="1" 
            :max="cart.store"
            size="small"
            class="quantity-input"
          ></el-input-number>
        </div>
      </div>

      <!-- 总价 -->
      <div class="total-section">
        <div class="label">总价</div>
        <div class="total-price">¥{{ totalPrice }}</div>
      </div>

      <!-- 操作按钮 -->
      <div class="actions">
        <el-button 
          type="success" 
          @click="pay"
          size="medium"
          round
        >
          <el-icon><Wallet /></el-icon>
          支付
        </el-button>
        <el-popconfirm
          @confirm="del"
          title="确定删除？"
          confirm-button-text="确定"
          cancel-button-text="取消"
        >
          <template #reference>
            <el-button 
              type="danger" 
              size="medium"
              plain
              round
            >
              <el-icon><Delete /></el-icon>
              移除
            </el-button>
          </template>
        </el-popconfirm>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Wallet, Delete } from '@element-plus/icons-vue'
import { useStore } from 'vuex'
import request from '@/utils/request'

const store = useStore()
const router = useRouter()

const props = defineProps({
  cart: Object,
  countChangeFlag: false
})

const emit = defineEmits(['delete'])

const baseApi = store.state.baseApi

const totalPrice = computed(() => {
  return (realPrice.value * props.cart.count).toFixed(2)
})

const realPrice = computed(() => {
  return (props.cart.price * props.cart.discount)
})

//从购物车移除
const del = (id) => {
  request.delete('/api/cart/' + props.cart.id).then(res => {
    if (res.code === '200') {
      ElMessage.success('删除成功')
      emit('delete', props.cart.id)
    }
  })
}

//跳转到支付页面
const pay = () => {
  let good = { id: props.cart.goodId, name: props.cart.goodName, imgs: props.cart.img, discount: props.cart.discount }
  router.push({ name: 'preOrder', query: { good: JSON.stringify(good), realPrice: realPrice.value, num: props.cart.count, standard: props.cart.standard, cartId: props.cart.id } })
}
</script>

<style scoped>
.cart-item-card {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: all 0.3s ease;
}

.cart-item-card:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

/* 时间标签 */
.time-badge {
  background: #e8f4ff;
  padding: 12px 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.time-badge i {
  color: #409EFF;
  font-size: 16px;
}

/* 卡片内容 */
.card-content {
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
  min-width: 200px;
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

/* 价格、数量、总价区域 */
.price-section,
.quantity-section,
.total-section {
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

.price {
  font-size: 18px;
  color: #F56C6C;
  font-weight: 600;
}

.quantity-control {
  display: flex;
  justify-content: center;
}

.quantity-btn {
  min-width: 60px;
  font-size: 15px;
  font-weight: 600;
}

.quantity-input {
  width: 120px;
}

.total-price {
  font-size: 22px;
  color: #E6A23C;
  font-weight: 700;
}

/* 操作按钮 */
.actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 100px;
}

.actions .el-button {
  width: 100%;
  font-weight: 500;
}

/* 响应式 */
@media (max-width: 1024px) {
  .card-content {
    flex-wrap: wrap;
    gap: 20px;
  }
  
  .product-info {
    flex-basis: 100%;
  }
}

@media (max-width: 768px) {
  .card-content {
    flex-direction: column;
    padding: 20px;
  }
  
  .product-image img {
    width: 100px;
    height: 100px;
  }
  
  .price-section,
  .quantity-section,
  .total-section {
    flex-direction: row;
    justify-content: space-between;
    width: 100%;
  }
  
  .actions {
    width: 100%;
  }
}
</style>