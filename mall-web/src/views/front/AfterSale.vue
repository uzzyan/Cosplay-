<template>
  <div style="width: 90%; max-width: 1200px; margin: 20px auto">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>退款 / 售后申请</span>
        </div>
      </template>

      <el-form label-width="90px">
        <el-form-item label="订单编号">
          <el-input v-model.trim="form.orderNo" maxlength="64"></el-input>
        </el-form-item>
        <el-form-item label="申请类型">
          <el-select v-model="form.type" placeholder="请选择" style="width: 200px">
            <el-option label="退款" value="退款"></el-option>
            <el-option label="售后" value="售后"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="申请原因">
          <el-input type="textarea" :rows="4" v-model.trim="form.reason" maxlength="2000" show-word-limit></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="success" @click="submit">提交申请</el-button>
          <el-button @click="resetForm">清空</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 18px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>我的申请</span>
        </div>
      </template>

      <el-table :data="tableData" stripe style="width: 100%">
        <el-table-column prop="createTime" label="申请时间" width="170"></el-table-column>
        <el-table-column prop="orderNo" label="订单编号" width="200"></el-table-column>
        <el-table-column prop="type" label="类型" width="80"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.status === '待处理'" type="warning">待处理</el-tag>
            <el-tag v-else-if="scope.row.status === '已同意'" type="success">已同意</el-tag>
            <el-tag v-else type="info">已拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button type="primary" link @click="openDetail(scope.row.id)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="padding: 15px 0; text-align: right">
        <el-pagination
          background
          layout="prev, pager, next"
          :current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          @current-change="handlePageChange"
        ></el-pagination>
      </div>
    </el-card>

    <el-dialog title="申请详情" v-model="detailVisible" width="700px">
      <div v-if="detail">
        <div style="margin-bottom: 10px"><b>订单编号：</b>{{ detail.orderNo }}</div>
        <div style="margin-bottom: 10px"><b>类型：</b>{{ detail.type }}</div>
        <div style="margin-bottom: 10px"><b>状态：</b>{{ detail.status }}</div>
        <div style="margin-bottom: 10px"><b>申请时间：</b>{{ detail.createTime }}</div>
        <div style="margin-bottom: 10px"><b>处理时间：</b>{{ detail.handleTime || '-' }}</div>
        <div style="margin-bottom: 10px"><b>原因：</b></div>
        <div style="white-space: pre-wrap">{{ detail.reason }}</div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const route = useRoute()

const form = ref({
  orderNo: '',
  type: '',
  reason: ''
})

const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const detailVisible = ref(false)
const detail = ref(null)

const resetForm = () => {
  form.value = { orderNo: route.query.orderNo || '', type: '', reason: '' }
}

const submit = () => {
  const orderNo = (form.value.orderNo || '').trim()
  const type = (form.value.type || '').trim()
  const reason = (form.value.reason || '').trim()
  if (!orderNo) {
    ElMessage.error('订单编号不能为空')
    return
  }
  if (!type) {
    ElMessage.error('申请类型不能为空')
    return
  }
  if (!reason) {
    ElMessage.error('申请原因不能为空')
    return
  }
  request.post('/api/afterSale', { orderNo, type, reason }).then((res) => {
    if (res.code === '200') {
      ElMessage.success('提交成功')
      resetForm()
      pageNum.value = 1
      load()
    } else {
      ElMessage.error(res.msg || '提交失败')
    }
  })
}

const load = () => {
  request
    .get('/api/afterSale/mine/page', {
      params: { pageNum: pageNum.value, pageSize: pageSize.value }
    })
    .then((res) => {
      if (res.code === '200') {
        tableData.value = res.data.records || []
        total.value = res.data.total || 0
      } else {
        ElMessage.error(res.msg || '加载失败')
      }
    })
}

const handlePageChange = (page) => {
  pageNum.value = page
  load()
}

const openDetail = (id) => {
  request.get('/api/afterSale/' + id).then((res) => {
    if (res.code === '200') {
      detail.value = res.data
      detailVisible.value = true
    } else {
      ElMessage.error(res.msg || '加载失败')
    }
  })
}

onMounted(() => {
  form.value.orderNo = route.query.orderNo || ''
  load()
})
</script>

<style scoped></style>
