<template>
  <div>
    <div style="padding: 10px 0">
      <el-input
        style="width: 240px"
        placeholder="搜索公告标题"
        v-model="searchText"
        clearable
        @keyup.enter="load"
      ></el-input>
      <el-button style="margin-left: 10px" type="primary" @click="load">查询</el-button>
      <el-button style="margin-left: 10px" type="success" @click="openAdd">新增公告</el-button>
    </div>

    <el-table :data="tableData" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="title" label="公告标题" min-width="200"></el-table-column>
      <el-table-column prop="content" label="公告内容" min-width="300" show-overflow-tooltip></el-table-column>
      <el-table-column prop="createTime" label="发布时间" width="180"></el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button type="primary" link @click="openEdit(scope.row)">编辑</el-button>
          <el-button type="primary" link @click="openDetail(scope.row)">详情</el-button>
          <el-popconfirm title="确定删除此公告？" @confirm="del(scope.row.id)">
            <template #reference>
              <el-button type="primary" link style="color: #f56c6c">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div style="padding: 15px 0; text-align: right">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        :current-page="pageNum"
        :page-size="pageSize"
        :page-sizes="[5, 10, 20, 50]"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      ></el-pagination>
    </div>

    <!-- 新增/编辑公告弹窗 -->
    <el-dialog 
      :title="dialogTitle" 
      v-model="dialogVisible" 
      width="700px"
      @close="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" maxlength="200" show-word-limit></el-input>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input 
            type="textarea" 
            v-model="form.content" 
            placeholder="请输入公告内容" 
            :rows="8"
            maxlength="5000"
            show-word-limit
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog title="公告详情" v-model="detailVisible" width="800px">
      <div v-if="detailData">
        <div style="margin-bottom: 15px">
          <b>标题：</b>{{ detailData.title }}
        </div>
        <div style="margin-bottom: 15px">
          <b>发布时间：</b>{{ detailData.createTime }}
        </div>
        <div style="margin-bottom: 15px">
          <b>内容：</b>
        </div>
        <div style="white-space: pre-wrap; line-height: 1.8; padding: 15px; background: #f8f9fa; border-radius: 4px;">
          {{ detailData.content }}
        </div>
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
const searchText = ref('')
const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('新增公告')
const formRef = ref(null)
const detailData = ref(null)

const form = ref({
  id: null,
  title: '',
  content: ''
})

const rules = {
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { min: 2, max: 200, message: '长度在 2 到 200 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' },
    { min: 5, max: 5000, message: '长度在 5 到 5000 个字符', trigger: 'blur' }
  ]
}

const load = () => {
  request
    .get('/api/notice/page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        title: searchText.value
      }
    })
    .then((res) => {
      if (res.code === '200') {
        tableData.value = Array.isArray(res.data.records) ? res.data.records : []
        total.value = res.data.total || 0
      } else if (res.code === '401') {
        ElMessage.error('登录状态已失效，请重新登录')
        tableData.value = []
        total.value = 0
      } else {
        ElMessage.error(res.msg || '加载数据失败')
        tableData.value = []
        total.value = 0
      }
    })
    .catch((err) => {
      console.error('加载公告数据失败:', err)
      ElMessage.error('加载失败，请检查网络连接')
      tableData.value = []
      total.value = 0
    })
}

const handlePageChange = (page) => {
  pageNum.value = page
  load()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  pageNum.value = 1
  load()
}

const openAdd = () => {
  dialogTitle.value = '新增公告'
  form.value = {
    id: null,
    title: '',
    content: ''
  }
  dialogVisible.value = true
}

const openEdit = (row) => {
  dialogTitle.value = '编辑公告'
  form.value = {
    id: row.id,
    title: row.title,
    content: row.content
  }
  dialogVisible.value = true
}

const openDetail = (row) => {
  detailData.value = row
  detailVisible.value = true
}

const submit = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      request
        .post('/api/notice', form.value)
        .then((res) => {
          if (res.code === '200') {
            ElMessage.success(dialogTitle.value === '新增公告' ? '新增成功' : '更新成功')
            dialogVisible.value = false
            load()
          } else {
            ElMessage.error(res.msg || '操作失败')
          }
        })
        .catch((err) => {
          console.error('提交失败:', err)
          ElMessage.error('操作失败，请重试')
        })
    }
  })
}

const del = (id) => {
  request
    .delete(`/api/notice/${id}`)
    .then((res) => {
      if (res.code === '200') {
        ElMessage.success('删除成功')
        load()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    })
    .catch((err) => {
      console.error('删除失败:', err)
      ElMessage.error('删除失败，请重试')
    })
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

onMounted(() => {
  load()
})
</script>

<style scoped>
</style>
