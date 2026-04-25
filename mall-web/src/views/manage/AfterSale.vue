<template>
  <div>
    <div style="padding: 10px 0">
      <el-input style="width: 220px" placeholder="订单编号" v-model="orderNo" clearable></el-input>
      <el-select style="width: 140px; margin-left: 10px" v-model="type" clearable placeholder="类型">
        <el-option label="退款" value="退款"></el-option>
        <el-option label="售后" value="售后"></el-option>
      </el-select>
      <el-select style="width: 160px; margin-left: 10px" v-model="status" clearable placeholder="状态">
        <el-option label="待处理" value="待处理"></el-option>
        <el-option label="已同意" value="已同意"></el-option>
        <el-option label="已拒绝" value="已拒绝"></el-option>
      </el-select>
      <el-button style="margin-left: 10px" type="primary" @click="load">查询</el-button>
    </div>

    <el-table :data="tableData" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="orderNo" label="订单编号" width="200"></el-table-column>
      <el-table-column prop="userId" label="用户ID" width="90"></el-table-column>
      <el-table-column prop="type" label="类型" width="80"></el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.status === '待处理'" type="warning">待处理</el-tag>
          <el-tag v-else-if="scope.row.status === '已同意'" type="success">已同意</el-tag>
          <el-tag v-else type="info">已拒绝</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="申请时间" width="170"></el-table-column>
      <el-table-column label="操作" width="260">
        <template #default="scope">
          <el-button type="primary" link @click="openDetail(scope.row.id)">详情</el-button>
          <el-select
            v-model="scope.row.status"
            size="small"
            style="width: 110px; margin-left: 10px"
            @change="handle(scope.row)"
          >
            <el-option label="待处理" value="待处理"></el-option>
            <el-option label="已同意" value="已同意"></el-option>
            <el-option label="已拒绝" value="已拒绝"></el-option>
          </el-select>
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

    <el-dialog title="申请详情" v-model="detailVisible" width="750px">
      <div v-if="detail">
        <div style="margin-bottom: 10px"><b>订单编号：</b>{{ detail.orderNo }}</div>
        <div style="margin-bottom: 10px"><b>用户ID：</b>{{ detail.userId }}</div>
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
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const orderNo = ref('')
const type = ref('')
const status = ref('')
const detailVisible = ref(false)
const detail = ref(null)

const load = () => {
  request
    .get('/api/afterSale/page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        orderNo: orderNo.value,
        type: type.value,
        status: status.value
      }
    })
    .then((res) => {
      if (res.code === '200') {
        tableData.value = res.data.records || []
        total.value = res.data.total || 0
      } else {
        ElMessage.error(res.msg || '加载失败')
      }
    })
    .catch((err) => {
      console.error('加载售后列表失败:', err)
      ElMessage.error('加载失败，请重试')
    })
}

const handlePageChange = (page) => {
  pageNum.value = page
  load()
}

const openDetail = (id) => {
  request.get('/api/afterSale/' + id)
    .then((res) => {
      if (res.code === '200') {
        detail.value = res.data
        detailVisible.value = true
      } else {
        ElMessage.error(res.msg || '加载失败')
      }
    })
    .catch((err) => {
      console.error('加载详情失败:', err)
      ElMessage.error('加载失败，请重试')
    })
}

const handle = (row) => {
  request.put('/api/afterSale/handle', { id: row.id, status: row.status })
    .then((res) => {
      if (res.code === '200') {
        ElMessage.success('处理成功')
        load()
      } else {
        ElMessage.error(res.msg || '处理失败')
      }
    })
    .catch((err) => {
      console.error('处理失败:', err)
      ElMessage.error('处理失败，请重试')
    })
}

onMounted(() => {
  load()
})
</script>

<style scoped></style>
