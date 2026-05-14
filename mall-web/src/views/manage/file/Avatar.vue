<template>
  <div>
    <!--          表格-->
    <el-table :data="tableData" background-color="black" @selection-change="handleSelectionChange" >
      <el-table-column type="selection" ></el-table-column>
      <el-table-column  label="头像" width="150" >
        <template   #default="scope">
          <img :src="baseApi + scope.row.url"  min-width="100" height="100" />
        </template>
      </el-table-column>

      <el-table-column prop="type" label="文件类型" width="180" ></el-table-column>
      <el-table-column prop="size" label="文件大小" width="180" ></el-table-column>

      <el-table-column label="操作">
        <template #default="scope">

<!--          下载-->
          <a :href="baseApi + scope.row.url">
            <el-button
              type="success"
              style="font-size: 18px;"
              >
              下载
            </el-button>
          </a>
<!--          删除-->
          <el-button
              type="danger"
              style="margin-left: 10px;font-size: 18px;"
              @click="handleDelete(scope.row.id)">
              
              删除
            </el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="block" style="flex: 0 0 auto">
      <el-pagination
          :current-page="currentPage"
          :page-sizes="[3, 5, 8, 10]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentPage"
      >
      </el-pagination>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const store = useStore()
const baseApi = store.state.baseApi

const tableData = ref([])
const total = ref(0)
const pageSize = ref(5)
const currentPage = ref(1)
const multipleSelection = ref([])

const load = () => {
  request.get('/avatar/page', {
    params: {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
  }).then(res => {
    if (res.code === '200') {
      // 确保数据是数组
      tableData.value = Array.isArray(res.data.records) ? res.data.records : []
      for (let s of tableData.value) {
        let size = s.size
        if (size < 1024) {
          s.size = size + ' Kb'
        } else if (size > 1024 && size < 1024 * 1024) {
          s.size = (size / 1024).toFixed(2) + ' Mb'
        } else {
          s.size = (size / 1024 / 1024).toFixed(2) + ' Gb'
        }
      }
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
  }).catch(err => {
    console.error('加载头像列表失败:', err)
    ElMessage.error('加载失败，请检查网络连接')
    tableData.value = []
    total.value = 0
  })
}

const handleSizeChange = (newPageSize) => {
  pageSize.value = newPageSize
  load()
}

const handleCurrentPage = (newCurrentPage) => {
  currentPage.value = newCurrentPage
  load()
}

const handleSelectionChange = (val) => {
  multipleSelection.value = val
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确认删除该头像吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    request.delete('/avatar/' + id).then(res => {
      if (res.code === '200') {
        ElMessage.success('删除成功')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(() => {
    // 用户取消删除
  })
}

onMounted(() => {
  load()
})
</script>

<style scoped>

</style>