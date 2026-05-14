<template>
  <div>
    <div class="demo-input-size">
      <el-select
        v-model="searchMode"
        placeholder="请选择"
        style="width: 150px; margin-right: 10px"
      >
        <el-option value="id" label="用户id"></el-option>
        <el-option value="username" label="账号"></el-option>
        <el-option value="nickname" label="昵称"></el-option>
      </el-select>
      <el-input
        v-if="searchMode === 'id'"
        placeholder="请输入用户id"
        prefix-icon="el-icon-search"
        style="width: 250px; padding-right: 5px"
        v-model="searchParams.id"
      ></el-input>
      <el-input
        v-if="searchMode === 'username'"
        placeholder="请输入账号"
        prefix-icon="el-icon-search"
        style="width: 250px; padding-right: 5px"
        v-model="searchParams.username"
      ></el-input>
      <el-input
        v-if="searchMode === 'nickname'"
        placeholder="请输入昵称"
        prefix-icon="el-icon-search"
        style="width: 250px; padding-right: 5px"
        v-model="searchParams.nickname"
      ></el-input>
      <el-button type="primary" @click="search">
        
        搜索
      </el-button>
      <el-button type="warning" @click="reload">
         重置
      </el-button>
    </div>
    <!--          按钮栏-->
    <div style="padding-top: 10px">
      <el-button type="primary" @click="handleAdd" style="font-size: 18px;"
        > 新增</el-button
      >
      <el-button type="danger" @click="delBatch" style="font-size: 18px;"
        > 批量删除</el-button
      >
    </div>
    <!--          弹窗-->
    <el-dialog :title="dialogTitle" v-model="dialogFormVisible">
      <el-form label-width="50px" style="padding: 0 60px">
        <el-form-item label="账号" v-if="dialogTitle == '新增用户'">
          <el-input v-model="user.username" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="密码" v-if="dialogTitle == '新增用户'">
          <el-input v-model="user.newPasswordRaw" autocomplete="off" show-password placeholder="6-12位，含字母、数字和特殊符号"></el-input>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="user.nickname" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="身份">
          <el-select v-model="user.role" placeholder="请选择">
            <el-option
              v-for="item in roleOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            >
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="电话">
          <el-input v-model="user.phone" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="user.email" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="user.address" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogFormVisible = false" style="font-size: 20px;"> 取消</el-button>
          <el-button type="primary" @click="save" style="font-size: 20px;"> 确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!--          表格-->
    <el-table
      :data="tableData"
      background-color="black"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection"></el-table-column>
      <el-table-column prop="id" label="id" width="100"></el-table-column>
      <el-table-column
        prop="username"
        label="账号"
        width="150"
      ></el-table-column>
      <el-table-column label="身份" width="150">
        <template #default="scope">
          <span v-if="scope.row.role === 'user'">用户</span>
          <span v-if="scope.row.role === 'admin'">管理员</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="nickname"
        label="昵称"
        width="180"
      ></el-table-column>
      <el-table-column prop="phone" label="电话" width="180"></el-table-column>
      <el-table-column prop="email" label="邮箱" width="180"></el-table-column>
      <el-table-column
        prop="address"
        label="地址"
        width="350"
      ></el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="scope">
          <el-button style="font-size: 18px;" type="success" @click="handleEdit(scope.row)">
            编辑
          </el-button>
          <el-button
            style="font-size: 18px;"
            type="danger"
            @click="handleDelete(scope.row.id)"
          >
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
import { ElMessage, ElMessageBox } from 'element-plus'
import md5 from 'js-md5'
import request from '@/utils/request'

const tableData = ref([])
const roleOptions = [
  {
    value: 'admin',
    label: '管理员'
  },
  {
    value: 'user',
    label: '用户'
  }
]
const roleValue = ref('')
const total = ref(0)
const pageSize = ref(5)
const currentPage = ref(1)
const searchMode = ref('id')
const searchParams = ref({
  id: '',
  username: '',
  nickname: ''
})
const dialogFormVisible = ref(false)
const dialogTitle = ref('')
const user = ref({
  username: '',
  nickname: '',
  newPassword: '',
  newPasswordRaw: '',
  role: '',
  phone: '',
  email: '',
  address: ''
})
const passwordRule = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{}|;:,.<>?])[A-Za-z\d!@#$%^&*()_+\-=\[\]{}|;:,.<>?]{6,12}$/
const multipleSelection = ref([])

const handleSizeChange = (pageSizeVal) => {
  pageSize.value = pageSizeVal
  load()
}

const handleCurrentPage = (currentPageVal) => {
  currentPage.value = currentPageVal
  load()
}

const handleSelectionChange = (val) => {
  multipleSelection.value = val
}

const load = () => {
  request
    .get('/user/page', {
      params: {
        pageNum: currentPage.value,
        pageSize: pageSize.value,
        id: searchParams.value.id,
        username: searchParams.value.username,
        nickname: searchParams.value.nickname
      }
    })
    .then((res) => {
      if (res.code === '200') {
        // 确保数据是数组
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
    .catch((error) => {
      console.error('加载用户数据失败:', error)
      ElMessage.error('加载数据失败，请检查网络连接')
      tableData.value = []
      total.value = 0
    })
}

const search = () => {
  currentPage.value = 1
  load()
}

const reload = () => {
  searchParams.value.id = ''
  searchParams.value.username = ''
  searchParams.value.nickname = ''
  load()
}

//插入或修改
const save = () => {
  if (dialogTitle.value == '新增用户') {
    if (user.value.username.trim() == '') {
      ElMessage.error('账号不能为空')
      return
    }
    var rawPsw = (user.value.newPasswordRaw || '').trim()
    if (!rawPsw) {
      ElMessage.error('密码不能为空')
      return
    }
    if (!passwordRule.test(rawPsw)) {
      ElMessage.error('密码必须为6-12位，且同时包含字母、数字和特殊符号')
      return
    }
    user.value.newPassword = rawPsw
  }
  if (user.value.nickname.trim() == '') {
    ElMessage.error('昵称不能为空')
    return
  }
  if (user.value.role.trim() == '') {
    ElMessage.error('身份不能为空')
    return
  }
  if (user.value.phone.trim() == '') {
    ElMessage.error('电话不能为空')
    return
  }
  if (user.value.email.trim() == '') {
    ElMessage.error('邮箱不能为空')
    return
  }
  dialogTitle.value = '新增用户'
  request.post('/user', user.value).then((res) => {
    if (res.code === '200') {
      ElMessage.success('保存成功')
      dialogFormVisible.value = false
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const handleAdd = () => {
  dialogTitle.value = '新增用户'
  dialogFormVisible.value = true
  user.value = {
    username: '',
    nickname: '',
    newPassword: '',
    newPasswordRaw: '',
    role: '',
    phone: '',
    email: '',
    address: ''
  }
}

//编辑
const handleEdit = (row) => {
  user.value = JSON.parse(JSON.stringify(row))
  dialogTitle.value = '编辑用户'
  dialogFormVisible.value = true
}

//删除
const handleDelete = (id) => {
  ElMessageBox.confirm('确认删除该用户吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    request.delete('/user/' + id).then((res) => {
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
  })
}

//批量删除
const delBatch = () => {
  let ids = multipleSelection.value.map((v) => v.id)
  ElMessageBox.confirm('确认删除这些用户吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    request.post('/user/del/batch', ids).then((res) => {
      if (res.code === '200') {
        ElMessage({
          type: 'success',
          message: '删除成功',
          duration: 3000
        })
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  })
}

onMounted(() => {
  load()
})
</script>

<style scoped>
</style>