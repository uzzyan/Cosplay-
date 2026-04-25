<!--
 * @Description: 
 * @Author: gzc
 * @Date: 2024-03-21 15:27:05
-->
<template>
  <div style="height: 100%">
    <el-container style="height: 100%">
      <!--      侧边栏-->
      <el-aside
        :width="sideWidth + 'px'"
        style="background: #f8f9fa; height: 100%"
      >
        <Aside :is-collapse="isCollapse"></Aside>
      </el-aside>

      <el-container>
        <!--        导航栏-->
        <el-header
          style="border-bottom: 1px solid #e5e7eb; background: linear-gradient(90deg, #f0fdfa 0%, #fef2f2 100%);height: 80px;"
        >
          <Header
            :collapse-icon="collapseIcon"
            :collapse-title="collapseTitle"
            @collapse="handleCollapse"
            :user="user"
          ></Header>
        </el-header>

        <el-main :class="{bk: $route.path=='/manage/home'}">
          <router-view @refresh="getUser" />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<style>
.el-header {
  background-color: #b3c0d1;
  color: #333;
  line-height: 80px;
}

.el-aside {
  color: #333;
}

.bk {
  width: 100%;
  background: url("@/resource/img/01.png") center center no-repeat;
  background-size: 100% 100%;
}
</style>

<script setup>
import { ref, onMounted } from 'vue'
import Aside from '@/components/Aside.vue'
import Header from '@/components/Header.vue'
import request from '@/utils/request'

const user = ref({})
const isCollapse = ref(false)
const sideWidth = ref(250)
const collapseIcon = ref('el-icon-s-fold')
const collapseTitle = ref('收缩')

const handleCollapse = () => {
  isCollapse.value = !isCollapse.value
  if (isCollapse.value) {
    //点击收缩按钮
    sideWidth.value = 64
    collapseIcon.value = 'el-icon-s-unfold'
    collapseTitle.value = '展开'
  } else {
    //点击展开按钮
    sideWidth.value = 250
    collapseIcon.value = 'el-icon-s-fold'
    collapseTitle.value = '收缩'
  }
}

const getUser = () => {
  let username = localStorage.getItem('user')
    ? JSON.parse(localStorage.getItem('user')).username
    : ''
  if (username) {
    // 从后台获待User数据
    request.get('/userinfo/' + username)
      .then((res) => {
        // 重新赋值后台的最新User数据
        user.value = res.data
      })
      .catch((err) => {
        console.error('获取用户信息失败:', err)
      })
  }
}

onMounted(() => {
  getUser()
})
</script>
