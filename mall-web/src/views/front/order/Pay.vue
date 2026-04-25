
<template>
<div class="pay-container">
  <div class="content-wrapper">
    <!-- 页面标题 -->
    <div class="page-header">
      <i class="el-icon-wallet header-icon"></i>
      <h2 class="page-title">订单支付</h2>
    </div>

    <!-- 订单信息 -->
    <div class="order-info">
      <div class="info-item">
        <span class="label">订单号：</span>
        <span class="value">{{ orderNo }}</span>
      </div>
      <div class="info-item amount">
        <span class="label">支付金额：</span>
        <span class="amount-value">
          <span class="symbol">¥</span>
          <span class="number">{{ money }}</span>
        </span>
      </div>
    </div>

    <!-- 支付方式 -->
    <div class="payment-section">
      <div class="section-title">
        <i class="el-icon-bank-card"></i>
        选择支付方式
      </div>
      <div class="payment-methods">
        <div class="payment-item" @click="confirmPay('微信支付')">
          <img src="../../../resource/img/微信支付.png" alt="微信支付" class="payment-icon">
          <span class="payment-name">微信支付</span>
        </div>
        <div class="payment-item" @click="confirmPay('支付宝')">
          <img src="../../../resource/img/支付宝.png" alt="支付宝" class="payment-icon">
          <span class="payment-name">支付宝</span>
        </div>
      </div>
    </div>

    <!-- 提示信息 -->
    <div class="tips">
      <i class="el-icon-info"></i>
      <span>请选择支付方式完成支付</span>
    </div>

    <!-- 支付确认对话框 -->
    <el-dialog
      title="确认支付"
      v-model="paymentDialogVisible"
      width="400px"
      center
    >
      <div class="dialog-content">
        <i class="el-icon-warning-outline dialog-icon"></i>
        <p class="dialog-text">确认使用<strong>{{ selectedPayment }}</strong>支付</p>
        <p class="dialog-amount">¥{{ money }}</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="paymentDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="pay">确认支付</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()

const userId = ref(0)
const money = ref(0)
const orderNo = ref('')
const paymentDialogVisible = ref(false)
const selectedPayment = ref('')

const confirmPay = (paymentMethod) => {
  selectedPayment.value = paymentMethod
  paymentDialogVisible.value = true
}

const pay = () => {
  paymentDialogVisible.value = false
  request.post('/api/order/paid/' + orderNo.value).then(res => {
    if (res.code === '200') {
      ElMessage.success('您成功支付 ¥' + money.value + ' 元')
      setTimeout(() => {
        router.replace('/orderlist')
      }, 1000)
    } else {
      ElMessage.error(res.msg)
    }
  })
}

onMounted(() => {
  money.value = parseFloat(route.query.money).toFixed(2)
  orderNo.value = route.query.orderNo
})
</script>

<style scoped>
.pay-container {
  padding: 30px 0;
}

.content-wrapper {
  width: 90%;
  max-width: 600px;
  margin: 0 auto;
  background-color: white;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

/* 页面标题 */
.page-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0f0f0;
}

.header-icon {
  font-size: 28px;
  color: #67C23A;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

/* 订单信息 */
.order-info {
  background: #f5f7fa;
  border-radius: 12px;
  padding: 25px;
  margin-bottom: 25px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
}

.info-item.amount {
  margin-top: 12px;
  padding-top: 15px;
  border-top: 2px dashed #e0e0e0;
}

.label {
  font-size: 15px;
  color: #606266;
  font-weight: 500;
}

.value {
  font-size: 15px;
  color: #303133;
  font-family: monospace;
  font-weight: 600;
}

.amount-value {
  display: flex;
  align-items: baseline;
  color: #ff4d4f;
}

.amount-value .symbol {
  font-size: 20px;
  font-weight: 600;
  margin-right: 4px;
}

.amount-value .number {
  font-size: 30px;
  font-weight: 700;
}

/* 支付方式 */
.payment-section {
  margin-bottom: 25px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title i {
  font-size: 18px;
  color: #409EFF;
}

.payment-methods {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.payment-item {
  background: white;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  padding: 25px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.payment-item:hover {
  border-color: #67C23A;
  box-shadow: 0 4px 16px rgba(103, 194, 58, 0.2);
  transform: translateY(-4px);
}

.payment-icon {
  width: 50px;
  height: 50px;
  object-fit: contain;
}

.payment-name {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
}

.payment-item:hover .payment-name {
  color: #67C23A;
}

/* 提示信息 */
.tips {
  text-align: center;
  color: #909399;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 15px;
  background: #f0f9ff;
  border-radius: 8px;
}

.tips i {
  font-size: 16px;
  color: #409EFF;
}

/* 确认对话框 */
.dialog-content {
  text-align: center;
  padding: 20px;
}

.dialog-icon {
  font-size: 60px;
  color: #E6A23C;
  margin-bottom: 20px;
}

.dialog-text {
  font-size: 16px;
  color: #606266;
  margin-bottom: 15px;
}

.dialog-text strong {
  color: #409EFF;
}

.dialog-amount {
  font-size: 32px;
  color: #ff4d4f;
  font-weight: 700;
  margin: 10px 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .content-wrapper {
    width: 95%;
    padding: 25px 20px;
  }

  .payment-methods {
    grid-template-columns: 1fr;
  }

  .amount-value .number {
    font-size: 24px;
  }
}
</style>
