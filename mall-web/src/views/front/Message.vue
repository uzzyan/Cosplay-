<template>
  <div style="width: 90%; max-width: 1200px; margin: 20px auto">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>在线留言</span>
        </div>
      </template>

      <el-form label-width="90px">
        <el-form-item label="标题">
          <el-input v-model.trim="form.title" maxlength="200" show-word-limit></el-input>
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model.trim="form.contact" maxlength="50"></el-input>
        </el-form-item>
        <el-form-item label="定制意愿">
          <el-switch v-model="form.customIntent" active-text="有" inactive-text="无"></el-switch>
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            type="textarea"
            :rows="5"
            v-model.trim="form.content"
            maxlength="2000"
            show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="success" @click="submit">提交留言</el-button>
          <el-button @click="resetForm">清空</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 18px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>我的留言</span>
        </div>
      </template>

      <el-table :data="tableData" stripe style="width: 100%">
        <el-table-column prop="createTime" label="时间" width="170"></el-table-column>
        <el-table-column prop="title" label="标题"></el-table-column>
        <el-table-column label="定制意愿" width="90">
          <template #default="scope">
            <span>{{ scope.row.customIntent ? "有" : "无" }}</span>
          </template>
        </el-table-column>
        <el-table-column label="回复" width="90">
          <template #default="scope">
            <span>{{ scope.row.reply ? "已回复" : "未回复" }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button type="primary" link @click="openDetail(scope.row.id)">查看</el-button>
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

    <el-dialog title="留言详情" v-model="detailVisible" width="700px">
      <div v-if="detail">
        <div style="margin-bottom: 10px"><b>标题：</b>{{ detail.title }}</div>
        <div style="margin-bottom: 10px"><b>联系方式：</b>{{ detail.contact }}</div>
        <div style="margin-bottom: 10px"><b>定制意愿：</b>{{ detail.customIntent ? "有" : "无" }}</div>
        <div style="margin-bottom: 10px"><b>内容：</b></div>
        <div style="white-space: pre-wrap">{{ detail.content }}</div>
        <div style="margin: 14px 0 10px"><b>管理员回复：</b></div>
        <div v-if="detail.reply" style="white-space: pre-wrap">{{ detail.reply }}</div>
        <div v-else>暂无回复</div>
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

const form = ref({
  title: '',
  content: '',
  contact: '',
  customIntent: false
})

const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const detailVisible = ref(false)
const detail = ref(null)

const resetForm = () => {
  form.value = { title: '', content: '', contact: '', customIntent: false }
}

const submit = () => {
  const title = (form.value.title || '').trim()
  const content = (form.value.content || '').trim()
  const contact = (form.value.contact || '').trim()
  if (!title) {
    ElMessage.error('标题不能为空')
    return
  }
  if (!content) {
    ElMessage.error('内容不能为空')
    return
  }
  if (!contact) {
    ElMessage.error('联系方式不能为空')
    return
  }
  if (!/^[0-9\-+() ]{7,20}$/.test(contact)) {
    ElMessage.error('联系方式格式不正确')
    return
  }
  request.post('/api/message', form.value).then((res) => {
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
    .get('/api/message/mine/page', {
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
  request.get('/api/message/' + id).then((res) => {
    if (res.code === '200') {
      detail.value = res.data
      detailVisible.value = true
    } else {
      ElMessage.error(res.msg || '加载失败')
    }
  })
}

onMounted(() => {
  load()
})
</script>

<style scoped></style>
