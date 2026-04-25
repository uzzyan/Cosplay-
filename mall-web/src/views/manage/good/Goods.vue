<template>
  <div>
    <div style="padding: 5px 0">
      <el-input v-model="searchText" @keyup.enter="load" style="width: 200px">
        <template #prefix>
          <i class="el-input__icon iconfont icon-r-find"></i>
        </template>
      </el-input>
      
      <br>
      <el-button @click="load" type="primary" style="margin: 5px">
        搜索
      </el-button>
      <el-button @click="reset" type="warning" style="margin: 5px">
        重置
      </el-button>
      <el-button @click="add" type="success" style="margin: 5px;">
        新增
      </el-button>
    </div>

    <el-table :data="tableData" border stripe style="width: 100%">
      <el-table-column prop="id" label="商品id" width="80px"></el-table-column>
      <el-table-column prop="name" label="商品名称"></el-table-column>
      <el-table-column label="商品图片" width="120px">
        <template #default="scope">
          <img :src="baseApi + scope.row.imgs" style="width: 90px;height: 80px">
        </template>
      </el-table-column>
      <el-table-column prop="description" label="商品描述" show-overflow-tooltip></el-table-column>
      <el-table-column prop="discount" label="折扣"></el-table-column>
      <el-table-column prop="sales" label="销量"></el-table-column>
      <el-table-column prop="saleMoney" label="销售额（元)"></el-table-column>
      <el-table-column label="状态" width="240">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 0" type="info">下架</el-tag>
          <el-tag v-else-if="scope.row.totalStore === 0" type="danger">售罄</el-tag>
          <el-tag v-else type="success">上架</el-tag>
          <el-select
            v-model="scope.row.status"
            size="small"
            style="width: 90px; margin-left: 10px"
            @change="handleStatus(scope.row)"
          >
            <el-option label="上架" :value="1"></el-option>
            <el-option label="下架" :value="0"></el-option>
          </el-select>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间">
        <template #default="scope">
          <span v-html="scope.row.createTime.replace(' ','&nbsp;&nbsp;')"></span>
        </template>
      </el-table-column>
      <el-table-column label="推荐" width="150" >
        <template #default="scope">
          <el-switch
              v-model="scope.row.recommend"
              @change="handleRecommend(scope.row)"
              active-color="#13ce66"
              inactive-color="#ff4949">
          </el-switch>
        </template>
      </el-table-column>

      <el-table-column
          fixed="right"
          label="操作"
          width="250">
        <template #default="scope">
          <el-button type="primary" style="font-size: 18px;"  @click="edit(scope.row)">
             修改
          </el-button>
          <el-popconfirm
              @confirm="del(scope.row.id)"
              title="确定删除？"
          >
            <template #reference>
              <el-button type="danger" style="font-size: 18px;margin-left: 10px">
                 删除
              </el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <div style="margin-top: 10px">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pageNum"
        :page-size="pageSize"
        :page-sizes="[3, 5, 8, 10]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
      >
      </el-pagination>
    </div>

    <!-- 弹窗   -->
    <el-dialog title="信息" v-model="dialogFormVisible" width="30%"
               :close-on-click-modal="false">
      <el-form :model="entity">
        <el-form-item label="商品名称" label-width="150px">
          <el-input v-model="entity.name" autocomplete="off" style="width: 80%"></el-input>
        </el-form-item>
        <el-form-item label="商品描述" label-width="150px">
          <el-input v-model="entity.description" autocomplete="off" style="width: 80%"></el-input>
        </el-form-item>
        <el-form-item label="折扣" label-width="150px">
          <el-input v-model="entity.discount" autocomplete="off" style="width: 80%" placeholder="请输入0-1之间的数字，如0.8表示8折"></el-input>
          <div style="color: #909399; font-size: 12px; margin-top: 5px;">
            提示：请输入0-1之间的小数，1表示不打折，0.8表示8折
          </div>
        </el-form-item>
        <el-form-item label="分类id" label-width="150px">
          <el-input v-model="entity.categoryId" autocomplete="off" style="width: 80%"></el-input>
        </el-form-item>
        <el-form-item label="商品图片" label-width="150px">
          <el-upload
              class="upload-demo"
              ref="uploadRef"
              :action="baseApi + '/file/upload'"
              :file-list="fileList"
              :on-change="handleChange"
              :limit="2"
              :on-success="handleImgSuccess"
              :auto-upload="false">
            <template #trigger>
              <el-button size="small" type="primary">选取文件</el-button>
            </template>
            <template #tip>
              <div class="el-upload__tip">只能上传jpg/png文件，且不超过500kb</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogFormVisible = false" style="font-size: 20px;"> 取消</el-button>
          <el-button type="primary" @click="save" style="font-size: 20px;"> 确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useStore } from 'vuex'
import API from '@/utils/request'

const store = useStore()
const router = useRouter()

const baseApi = store.state.baseApi
const uploadRef = ref(null) // 定义upload组件的ref
const fileList = ref([])
const options = ref([])
const searchText = ref('')
const user = ref({})
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(5)
const entity = ref({})
const total = ref(0)
const dialogFormVisible = ref(false)

const url = '/api/good/'

const handleSizeChange = (pageSizeVal) => {
  pageSize.value = pageSizeVal
  load()
}

const handleCurrentChange = (pageNumVal) => {
  pageNum.value = pageNumVal
  load()
}

const handleRecommend = (good) => {
  API.get(url + 'recommend', {
    params: {
      id: good.id,
      isRecommend: good.recommend
    }
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('修改成功')
    } else {
      ElMessage.error(res.msg)
    }
  }).catch(err => {
    console.error('修改推荐状态失败:', err)
    ElMessage.error('修改失败，请重试')
  })
}

const handleStatus = (good) => {
  API.put(url, { id: good.id, status: good.status }).then(res => {
    if (res.code === '200') {
      ElMessage.success('修改成功')
      load()
    } else {
      ElMessage.error(res.msg)
    }
  }).catch(err => {
    console.error('修改状态失败:', err)
    ElMessage.error('修改失败，请重试')
  })
}

const load = () => {
  API.get(url + 'fullPage', {
    params: {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      searchText: searchText.value
    }
  }).then(res => {
    tableData.value = res.data.records
    total.value = res.data.total
  }).catch(err => {
    console.error('加载商品列表失败:', err)
    ElMessage.error('加载失败，请重试')
  })
}

const reset = () => {
  searchText.value = ''
  load()
}

const add = () => {
  router.push('goodInfo')
}

const edit = (obj) => {
  entity.value = JSON.parse(JSON.stringify(obj))
  router.push({ name: 'goodInfo', query: { good: JSON.stringify(entity.value) } })
}

const handleImgSuccess = (res) => {
  entity.value.imgs = res.data
  API.post(url, entity.value).then(res2 => {
    if (res2.code === '200') {
      ElMessage({
        type: 'success',
        message: '操作成功'
      })
    } else {
      ElMessage({
        type: 'error',
        message: res2.msg
      })
    }
    load()
    dialogFormVisible.value = false
  }).catch(err => {
    console.error('保存失败:', err)
    ElMessage.error('保存失败，请重试')
  })
}

const save = () => {
  if (fileList.value.length !== 0) {
    // 使用uploadRef提交文件
    if (uploadRef.value) {
      uploadRef.value.submit()
    } else {
      ElMessage.error('上传组件未初始化')
    }
  } else {
    API.post(url, entity.value).then(res2 => {
      if (res2.code === '200') {
        ElMessage({
          type: 'success',
          message: '操作成功'
        })
      } else {
        ElMessage({
          type: 'error',
          message: res2.msg
        })
      }
      load()
      dialogFormVisible.value = false
    }).catch(err => {
      console.error('保存失败:', err)
      ElMessage.error('保存失败，请重试')
    })
  }
}

const del = (id) => {
  API.delete(url + id).then(res => {
    ElMessage({
      type: 'success',
      message: '操作成功'
    })
    load()
  }).catch(err => {
    console.error('删除失败:', err)
    ElMessage.error('删除失败，请重试')
  })
}

const handleChange = (file, fileListVal) => {
  fileList.value = fileListVal.slice(-3)
}

onMounted(() => {
  user.value = localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : {}
  load()
})
</script>

<style scoped>
</style>
