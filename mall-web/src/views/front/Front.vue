
<template>
  <el-container style="height: 100%;width:100%;">
    <el-header style="background-color: white">
      <Navagation :user="user"
                  :role="role"
                  :login-status="loginStatus"
      ></Navagation>
    </el-header>


    <el-main style="background: linear-gradient(135deg, #fef9f3 0%, #fef5f1 50%, #fef6f8 100%);width:100%;min-height: 100vh;">

      <router-view @refresh="getUser" />
    </el-main>

  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Navagation from '@/components/Navagation.vue'
import request from '@/utils/request'

const user = ref({})
const role = ref('user')
const loginStatus = ref(false)

const getUser = () => {
  let username = localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')).username : ''
  if (username) {
    // 从后台获取User数据
    request.get('/userinfo/' + username).then(res => {
      // 重新赋值后台的最新User数据
      user.value = res.data
    })
  }
}

onMounted(() => {
  let stored = localStorage.getItem('user')
  if (stored) {
    let parsedUser = JSON.parse(stored)
    user.value = parsedUser
    loginStatus.value = true
    // 立即从 localStorage 的 UserDTO 中读取 role（登录接口已返回 role）
    if (parsedUser.role) {
      role.value = parsedUser.role
      console.log('从 localStorage 读取到 role:', role.value)
    } else {
      console.warn('localStorage 中未找到 role 字段')
    }
    // 异步向后端确认角色（作为二次校验，不依赖此结果做首次渲染）
    request.post('/role').then(res => {
      if (res.code === '200') {
        role.value = res.data
        console.log('从后端 /role 接口获取到 role:', role.value)
        getUser()
      }
      // 非200时不清除登录态，保留 localStorage 中的角色
    }).catch((error) => {
      console.error('获取 role 失败:', error)
      // 网络异常时保留 localStorage 中的状态
    })
  } else {
    user.value = { nickname: '您未登录', avatarUrl: null }
    loginStatus.value = false
  }
})
</script>

<style scoped>
@import "../../resource/css/search.css";
.image {
  width: 100%;
  display: block;
}
</style>
