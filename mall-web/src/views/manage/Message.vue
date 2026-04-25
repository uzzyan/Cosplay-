<template>
  <div>
    <div style="padding: 10px 0">
      <el-input
        style="width: 240px"
        placeholder="搜索标题或联系方式"
        v-model="searchText"
        clearable
      ></el-input>
      <el-select style="width: 180px; margin-left: 10px" v-model="customIntentFilter" clearable placeholder="定制意愿">
        <el-option label="有定制意愿" :value="true"></el-option>
        <el-option label="无定制意愿" :value="false"></el-option>
      </el-select>
      <el-button style="margin-left: 10px" type="primary" @click="load">查询</el-button>
    </div>

    <el-table :data="tableData" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="userId" label="用户ID" width="90"></el-table-column>
      <el-table-column prop="title" label="标题"></el-table-column>
      <el-table-column prop="contact" label="联系方式" width="160"></el-table-column>
      <el-table-column label="定制意愿" width="90">
        <template #default="scope">
          <span>{{ scope.row.customIntent ? "有" : "无" }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="时间" width="170"></el-table-column>
      <el-table-column label="回复" width="90">
        <template #default="scope">
          <span>{{ scope.row.reply ? "已回复" : "未回复" }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button type="primary" link @click="openDetail(scope.row.id)">详情</el-button>
          <el-button type="primary" link @click="openReply(scope.row)">回复</el-button>
          <el-popconfirm title="确定删除？" @confirm="del(scope.row.id)">
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
        layout="prev, pager, next"
        :current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        @current-change="handlePageChange"
      ></el-pagination>
    </div>

    <el-dialog title="留言详情" v-model="detailVisible" width="750px">
      <div v-if="detail">
        <div style="margin-bottom: 10px"><b>ID：</b>{{ detail.id }}</div>
        <div style="margin-bottom: 10px"><b>用户ID：</b>{{ detail.userId }}</div>
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

    <el-dialog title="回复留言" v-model="replyVisible" width="650px">
      <el-input type="textarea" :rows="6" v-model.trim="replyForm.reply" maxlength="2000" show-word-limit></el-input>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="replyVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReply">提交</el-button>
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
const customIntentFilter = ref(null)
const detailVisible = ref(false)
const detail = ref(null)
const replyVisible = ref(false)
const replyForm = ref({
  id: null,
  reply: ''
})

const load = () => {
  request
    .get('/api/message/page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        customIntent: customIntentFilter.value,
        searchText: searchText.value
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

const openReply = (row) => {
  replyForm.value = { id: row.id, reply: row.reply || '' }
  replyVisible.value = true
}

const submitReply = () => {
  if (!replyForm.value.reply) {
    ElMessage.error('回复内容不能为空')
    return
  }
  request.put('/api/message/reply', replyForm.value).then((res) => {
    if (res.code === '200') {
      ElMessage.success('回复成功')
      replyVisible.value = false
      load()
    } else {
      ElMessage.error(res.msg || '回复失败')
    }
  })
}

const del = (id) => {
  request.delete('/api/message/' + id).then((res) => {
    if (res.code === '200') {
      ElMessage.success('删除成功')
      load()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  })
}

onMounted(() => {
  load()
})
</script>

<style scoped></style>
