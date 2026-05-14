<template>
  <div>
    <div style="width: 60%; margin: 30px auto">
      <el-button type="success" style="font-size: 20px;"
        @click="addDialogFormVisible = true"> 
        <i class="iconfont icon-r-add"></i>
        新增上级分类</el-button>
      <el-table :data="iconsList" stripe>
        <!-- 下级分类表-->
        <el-table-column type="expand" label="下级分类" width="100px">
          <template #default="scope">
            <el-table :data="scope.row.categories || []" :header-cell-style="{ background: '#cbefea', color: 'black' }">
              <el-table-column label="分类id" prop="id"></el-table-column>
              <el-table-column label="分类名称" prop="name"></el-table-column>
              <el-table-column label="操作" width="240">
                <template #default="scope">
                  <el-button type="primary" @click="handleEditCategory(scope.row)"> 修改</el-button>

                  <el-popconfirm @confirm="deleteCategory(scope.row)" title="确定删除？">
                    <template #reference>
                      <el-button type="danger"> 删除</el-button>
                    </template>
                  </el-popconfirm>
                </template>
              </el-table-column>
            </el-table>
          </template>
          <!---->
        </el-table-column>
        <el-table-column label="id" prop="id" width="60px"></el-table-column>
        <el-table-column label="icon">
          <template #default="scope">
            <i class="iconfont" v-html="scope.row.value"></i>
          </template>
        </el-table-column>

        <el-table-column fixed="right" label="操作" width="200">
          <template #default="scope">
            <el-button type="primary" style="font-size: 18px;" circle
              @click="handleEditIcon(scope.row)"><i class="iconfont icon-r-edit"></i></el-button>
            <el-button type="success"  style="font-size: 18px;" circle
              @click="handleAddCategory(scope.row)">
              <i class="iconfont icon-r-add"></i>
            </el-button>

            <el-popconfirm @confirm="deleteIcon(scope.row.id)" title="确定删除？">
              <template #reference>
                <el-button type="danger" style="font-size: 18px;margin-left: 10px;" circle>
                  <i class="iconfont icon-r-delete"></i>
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!--icon修改弹窗-->
    <el-dialog title="修改上级分类" v-model="dialogFormVisible">
      <el-form :model="icon">
        <el-form-item label="图标" label-width="100px">
          <i class="iconfont" v-html="icon.value"></i>
        </el-form-item>
        <el-form-item label="更改图标" label-width="100px">
          <el-select placeholder="请选择图标" v-model="icon.value">
            <el-option v-for="item in iconStore" :value="item" :key="item">
              <i class="iconfont" v-html="item"></i>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogFormVisible = false" style="font-size: 20px;"> 取消</el-button>
          <el-button type="primary" @click="editIcon" style="font-size: 20px;"><i class="iconfont icon-r-yes"
            style="font-size: 22px;"></i> 确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="新增上级分类" v-model="addDialogFormVisible">
      <el-form :model="addIcon">
        <el-form-item label="图标" label-width="100px">
          <i class="iconfont" v-html="addIcon.value"></i>
        </el-form-item>
        <el-form-item label="更改图标" label-width="100px">
          <el-select placeholder="请选择图标" v-model="addIcon.value">
            <el-option v-for="item in iconStore" :value="item" :key="item">
              <i class="iconfont" v-html="item"></i>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="addDialogFormVisible = false" style="font-size: 20px;"> 取消</el-button>
          <el-button type="primary" @click="saveIcon" style="font-size: 20px;"><i class="iconfont icon-r-yes"
            style="font-size: 22px;"></i> 确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import API from '@/utils/request'
import icons from '@/utils/icons'

const iconStore = icons.iconStore
const iconsList = ref([])
const icon = ref({})
const addIcon = ref({})
const dialogFormVisible = ref(false)
const addDialogFormVisible = ref(false)

const load = () => {
  API.get('/api/icon').then((res) => {
    if (res.code === '200') {
      // 确保数据是数组,并且每个item的categories也是数组
      iconsList.value = Array.isArray(res.data) ? res.data.map(item => ({
        ...item,
        categories: Array.isArray(item.categories) ? item.categories : []
      })) : []
    } else if (res.code === '401') {
      ElMessage.error('登录状态已失效，请重新登录')
      iconsList.value = []
    } else {
      ElMessage.error(res.msg || '加载数据失败')
      iconsList.value = []
    }
  }).catch((err) => {
    console.error('加载分类数据失败:', err)
    ElMessage.error('加载失败，请检查网络连接')
    iconsList.value = []
  })
}

const handleEditCategory = (category) => {
  ElMessageBox.prompt('请输入修改后的名称', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(({ value }) => {
    if (value == null || value == undefined || value.trim() == '') {
      ElMessage.error('请输入名称')
      return
    }
    category.name = value
    API.post('/api/category', category).then((res) => {
      if (res.code === '200') {
        ElMessage.success('修改成功')
      } else {
        ElMessage.error('修改失败')
      }
    })
  }).catch(() => {})
}

const handleAddCategory = (iconItem) => {
  ElMessageBox.prompt('请输入新增的下级分类名称', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(({ value }) => {
    if (value == null || value == undefined || value.trim() == '') {
      ElMessage.error('请输入名称')
      return
    }
    API.post('/api/category/add', { name: value, iconId: iconItem.id }).then((res) => {
      if (res.code === '200') {
        ElMessage.success('新增成功')
        load()
      } else {
        ElMessage.error('新增失败')
      }
    })
  }).catch(() => {})
}

const handleEditIcon = (iconItem) => {
  icon.value = JSON.parse(JSON.stringify(iconItem))
  dialogFormVisible.value = true
}

const editIcon = () => {
  const iconData = { ...icon.value }
  delete iconData.categories
  API.post('/api/icon', iconData).then((res) => {
    if (res.code === '200') {
      ElMessage.success('修改成功')
      dialogFormVisible.value = false
    } else {
      ElMessage.error('修改失败')
    }
  })
}

const saveIcon = () => {
  if (addIcon.value.value == undefined) {
    ElMessage.error('请选择上级分类图标')
    return
  }
  API.post('/api/icon', addIcon.value).then((res) => {
    if (res.code === '200') {
      ElMessage.success('新增成功')
      addDialogFormVisible.value = false
      load()
    } else {
      ElMessage.error('新增失败')
    }
  })
}

const deleteIcon = (iconId) => {
  API.get('/api/icon/delete?id=' + iconId).then((res) => {
    if (res.code == '200') {
      ElMessage.success('删除成功')
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const deleteCategory = (category) => {
  API.get('/api/category/delete?id=' + category.id).then((res) => {
    if (res.code == '200') {
      ElMessage.success('删除成功')
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

onMounted(() => {
  load()
})
</script>

<style scoped>
@import "../../../resource/css/icon.css";
</style>
