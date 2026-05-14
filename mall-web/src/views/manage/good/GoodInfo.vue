<template>
  <div style="width: 1000px; margin: 50px auto">
    <el-form :model="good">
      <el-form-item label="商品名称" label-width="150px">
        <el-input
          v-model="good.name"
          autocomplete="off"
          style="width: 80%"
        ></el-input>
      </el-form-item>
      <el-form-item label="商品描述" label-width="150px">
        <el-input
          v-model="good.description"
          autocomplete="off"
          style="width: 80%"
        ></el-input>
      </el-form-item>
      <el-form-item label="规格" label-width="150px">
        <el-tag
          closable
          @close="handleClose(standard)"
          :disable-transitions="true"
          style="margin-right: 10px"
          v-for="(standard, index) in standards"
          :key="index"
          @click="editStandard(index)"
          >{{ standard.value }}</el-tag
        >
        <el-button icon="el-icon-plus" circle @click="addStandard"></el-button>
      </el-form-item>

      <el-form-item label="折扣" label-width="150px">
        <el-input
          v-model="good.discount"
          autocomplete="off"
          style="width: 80%"
          placeholder="请输入0-1之间的数字，如0.8表示8折"
        ></el-input>
        <div style="color: #909399; font-size: 12px; margin-top: 5px;">
          提示：请输入0-1之间的小数，1表示不打折，0.8表示8折
        </div>
      </el-form-item>

      <el-form-item label="分类" label-width="150px">
        <el-select v-model="good.categoryId" placeholder="请选择">
          <el-option
            v-for="item in categoryItems"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          >
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="商品图片" label-width="150px">
        <el-upload
          class="upload-demo"
          ref="upload"
          :action="baseApi + '/file/upload'"
          :file-list="fileList"
          :on-change="handleChange"
          :limit="1"
          :on-remove="handleRemove"
          :on-success="handleImgSuccess"
          :auto-upload="false"
        >
          <template #trigger>
            <el-button size="small" type="primary">选取文件</el-button>
          </template>
          <template #tip>
            <div class="el-upload__tip">
              只能上传一个jpg/png文件，且不超过500kb
            </div>
          </template>
        </el-upload>
      </el-form-item>
      <el-form-item label-width="150px">
        <el-button type="success" @click="submit" style="font-size: 20px;">
          提交</el-button>
      </el-form-item>
    </el-form>
    <!-- 弹窗   -->
    <el-dialog
      title="规格信息"
      v-model="dialogFormVisible"
      width="30%"
      :close-on-click-modal="false"
    >
      <el-form :model="standard">
        <el-form-item label="规格名称" label-width="150px">
          <el-input
            v-model="standard.value"
            autocomplete="off"
            style="width: 80%"
          ></el-input>
        </el-form-item>
        <el-form-item label="价格" label-width="150px">
          <el-input
            v-model="standard.price"
            autocomplete="off"
            style="width: 80%"
          ></el-input>
        </el-form-item>
        <el-form-item label="库存" label-width="150px">
          <el-input
            v-model="standard.store"
            autocomplete="off"
            style="width: 80%"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogFormVisible = false" style="font-size: 20px;"> 取消</el-button>
          <el-button type="primary" @click="saveStandard" style="font-size: 20px;"> 确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useStore } from 'vuex'
import API from '@/utils/request'

const store = useStore()
const route = useRoute()
const router = useRouter()

const baseApi = store.state.baseApi
const good = ref({ discount: '1' })
const standards = ref([])
const index = ref(0)
const standard = ref({})
const fileList = ref([])
const dialogFormVisible = ref(false)
const categoryItems = ref([])
const checkedCategory = ref({})

const url = '/api/good'

const editStandard = (idx) => {
  standard.value = standards.value[idx]
  index.value = idx
  dialogFormVisible.value = true
}

const addStandard = () => {
  standard.value = {}
  index.value = standards.value.length
  dialogFormVisible.value = true
}

const saveStandard = () => {
  if (standard.value.value == undefined || standard.value.value == '') {
    ElMessage({
      type: 'error',
      message: '请输入规格名称',
      showClose: true
    })
    return
  }
  if (standard.value.price == undefined || standard.value.price == '') {
    ElMessage({
      type: 'error',
      message: '请输入价格',
      showClose: true
    })
    return
  }
  if (standard.value.store == undefined || standard.value.store == '') {
    ElMessage({
      type: 'error',
      message: '请输入库存',
      showClose: true
    })
    return
  }
  standards.value[index.value] = standard.value
  dialogFormVisible.value = false
}

const handleClose = (standardItem) => {
  standards.value.splice(standards.value.indexOf(standardItem), 1)
}

const handleImgSuccess = (res) => {
  good.value.imgs = res.data
  save()
}

const handleChange = (file, fileListVal) => {
  fileList.value = fileListVal.slice(-3)
}

const handleRemove = () => {
  fileList.value.pop()
}

const submit = () => {
  if (good.value.name == undefined || good.value.name.trim() == '') {
    ElMessage({
      type: 'error',
      message: '请输入商品名称',
      showClose: true
    })
    return false
  }
  if (good.value.description == undefined || good.value.description.trim() == '') {
    ElMessage({
      type: 'error',
      message: '请输入商品描述',
      showClose: true
    })
    return false
  }
  if (standards.value.length === 0) {
    ElMessage({
      type: 'error',
      message: '请至少添加一个规格',
      showClose: true
    })
    return false
  }
  if (good.value.discount == undefined || good.value.discount === '') {
    ElMessage({
      type: 'error',
      message: '请输入商品折扣',
      showClose: true
    })
    return false
  }
  if (good.value.categoryId == undefined) {
    ElMessage({
      type: 'error',
      message: '请选择商品分类',
      showClose: true
    })
    return false
  }
  if (fileList.value.length === 0) {
    ElMessage({
      type: 'error',
      message: '请上传图片',
      showClose: true
    })
    return false
  }
  // 如果文件是新选择的(状态为ready),先上传文件
  if (fileList.value[0].status === 'ready') {
    // 手动触发上传
    const uploadRef = document.querySelector('.upload-demo input[type=file]')
    if (uploadRef) {
      // 创建上传请求
      const file = fileList.value[0]
      const formData = new FormData()
      formData.append('file', file.raw)
      
      API.post(baseApi + '/file/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      }).then((res) => {
        if (res.code === '200') {
          good.value.imgs = res.data
          save()
        } else {
          ElMessage.error(res.msg || '上传图片失败')
        }
      }).catch((err) => {
        console.error('上传图片失败:', err)
        ElMessage.error('上传图片失败，请重试')
      })
    }
  } else if (fileList.value[0].status === 'success') {
    // 文件已经上传成功,直接保存
    save()
  }
}

const save = () => {
  API.post(url, good.value).then((res2) => {
    if (res2.code === '200') {
      good.value.id = res2.data
      API.post(url + '/standard?goodId=' + good.value.id, standards.value).then((res) => {
        if (res.code === '200') {
          ElMessage.success('操作成功')
          router.go(-1)
        } else {
          ElMessage.error(res.msg || '操作失败')
        }
      }).catch((err) => {
        console.error('保存规格数据失败:', err)
        ElMessage.error('保存失败，请重试')
      })
    } else if (res2.code === '401') {
      ElMessage.error('登录状态已失效，请重新登录')
    } else {
      ElMessage.error(res2.msg || '保存失败')
    }
  }).catch((err) => {
    console.error('保存商品数据失败:', err)
    ElMessage.error('保存失败，请检查网络连接')
  })
}

onMounted(() => {
  API.get('/api/category').then((res) => {
    if (res.code === '200') {
      // 确保categoryItems是数组
      categoryItems.value = Array.isArray(res.data) ? res.data : []
      
      if (route.query.good) {
        good.value = JSON.parse(route.query.good)
        fileList.value = [{ name: '原始图片', url: good.value.imgs }]
        
        // 正确获取选中的分类
        checkedCategory.value = categoryItems.value.find(item => item.id === good.value.categoryId)
        
        API.get(url + '/standard/' + good.value.id).then((res2) => {
          if (res2.code === '200') {
            let standardsData = JSON.parse(res2.data)
            standards.value = Array.isArray(standardsData) ? standardsData : []
          }
        }).catch((err) => {
          console.error('加载规格数据失败:', err)
        })
      }
    } else if (res.code === '401') {
      ElMessage.error('登录状态已失效，请重新登录')
      categoryItems.value = []
    } else {
      ElMessage.error(res.msg || '加载分类数据失败')
      categoryItems.value = []
    }
  }).catch((err) => {
    console.error('加载分类数据失败:', err)
    ElMessage.error('加载失败，请检查网络连接')
    categoryItems.value = []
  })
})
</script>

<style scoped>
</style>