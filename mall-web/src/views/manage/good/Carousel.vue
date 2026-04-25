<template>
  <div>
    <div>
      <el-table :data="tableData" border stripe style="width: 80%;margin: 2px auto">
        <el-table-column label="商品">
          <template #default="scope">
            <a :href="'/goodView/'+scope.row.goodId">{{scope.row.goodName}}</a>
          </template>
        </el-table-column>
        <el-table-column  label="图片" >
          <template #default="scope">
            <img :src="baseApi + scope.row.img" width="300" height="185" />
          </template>
        </el-table-column>
        <el-table-column prop="showOrder" label="顺序"></el-table-column>

        <el-table-column
            fixed="right"
            label="操作"
            width="250">
          <template #default="scope">
            <el-button type="primary" style="font-size: 18px;"  @click="edit(scope.row)">
               编辑</el-button>
            <el-popconfirm
                @confirm="del(scope.row.id)"
                title="确定删除？"
            >
              <template #reference>
                <el-button type="danger" style="margin-left: 10px;font-size: 18px;">
                 删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>
<!--新增按钮-->
    <div style="text-align: center">
      <el-button @click="add" type="primary" style="margin: 30px;width: 150px; font-size: 20px;">

        新增
      </el-button>
    </div>
    <!-- 弹窗   -->

    <el-dialog title="信息" v-model="dialogFormVisible" width="30%"
               :close-on-click-modal="false">
      <el-form :model="entity">
        <el-form-item label="商品id" label-width="150px">
          <el-input v-model="entity.goodId" autocomplete="off" style="width: 80%"></el-input>
        </el-form-item>
        <el-form-item label="轮播顺序" label-width="150px">
          <el-select v-model="entity.showOrder">
            <el-option v-for="index in tableData.length" :key="index" :label="index" :value="index">
            </el-option>
          </el-select>
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
import { ElMessage } from 'element-plus'
import { useStore } from 'vuex'
import API from '@/utils/request'

const store = useStore()

const baseApi = store.state.baseApi
const tableData = ref([])
const entity = ref({})
const dialogFormVisible = ref(false)

const url = '/api/carousel/'

const load = () => {
  API.get(url).then(res => {
    tableData.value = res.data || []
  })
}

const add = () => {
  entity.value = {}
  tableData.value.length++
  dialogFormVisible.value = true
}

const edit = (row) => {
  entity.value = JSON.parse(JSON.stringify(row))
  dialogFormVisible.value = true
}

const save = () => {
  if (entity.value.goodId == undefined || entity.value.goodId === '') {
    ElMessage.error('商品id不能为空')
    return
  }
  if (entity.value.showOrder == undefined) {
    ElMessage.error('轮播顺序不能为空')
    return
  }

  API.post(url, entity.value).then(res => {
    if (res.code === '200') {
      ElMessage.success('保存成功')
      load()
      dialogFormVisible.value = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const del = (id) => {
  API.delete(url + id).then(res => {
    if (res.code === '200') {
      ElMessage({
        type: 'success',
        message: '删除成功'
      })
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
</style>
