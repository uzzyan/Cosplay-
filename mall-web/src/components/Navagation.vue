
<template>
  <div class="navagation">
    <el-row>
      <el-col :span="3">
        <div style="font-size: 20px; font-weight: bold; text-align: center">
          <a href="/"><i class="iconfont icon-r-home" style="font-size: 24px;"></i> U次元 - Cosplay服装商城</a>
        </div>
      </el-col>
      <el-col :span="13">
        <el-menu
          :default-active="activeIndex"
          class="el-menu-demo"
          mode="horizontal"
          router
        >
          <el-menu-item index="/" class="menu-item">商城首页</el-menu-item>

          <el-menu-item index="/goodList" class="menu-item"
            >所有商品</el-menu-item
          >
          <el-menu-item index="/cart" class="menu-item" v-show="loginStatus"
            >购物车</el-menu-item
          >
          <el-menu-item index="/orderlist" class="menu-item" v-show="loginStatus"
            >我的订单</el-menu-item
          >
          <el-menu-item index="/addressManage" class="menu-item" v-show="loginStatus"
            >地址管理</el-menu-item
          >
          <el-menu-item index="/message" class="menu-item" v-show="loginStatus"
            >在线留言</el-menu-item
          >
          <el-menu-item index="/afterSale" class="menu-item" v-show="loginStatus"
            >售后申请</el-menu-item
          >
          <el-menu-item
            index="/manage"
            class="menu-item"
            v-show="role === 'admin'"
            >后台管理</el-menu-item
          >
        </el-menu>
      </el-col>
      <el-col :span="5">
        <!-- 搜索框 -->
        <div class="nav-search">
          <el-input
            v-model="searchText"
            placeholder="搜索商品"
            @keyup.enter="handleSearch"
            size="default"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #append>
              <el-button
                @click="handleSearch"
              >
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </div>
      </el-col>
      <el-col :span="3">
        <!--         右上角个人信息-->
        <el-dropdown style="cursor: pointer; float: right; margin-right: 20px">
          <span class="el-dropdown-link">
            <div style="display: inline-block">
              <img
                v-if="loginStatus && user.avatarUrl"
                :src="baseApi + user.avatarUrl"
                class="avatar"
              />
              <el-icon v-else-if="!loginStatus" style="vertical-align: middle; margin-right: 5px;"><User /></el-icon>
              {{ loginStatus ? user.nickname : '请登录' }}
              <i
                class="el-icon-arrow-down el-icon--right"
                style="margin-right: 5px"
              ></i>
            </div>
          </span>
          <!--          下拉菜单-->
          <template #dropdown>
            <el-dropdown-menu style="text-align: center">
            <el-dropdown-item v-if="!loginStatus">
              <!--              传给前端，登录后跳转页面的path为 "/"-->
              <div
                @click="$router.push({ path: '/login', query: { to: '/' } })"
              >
                登录
              </div>
            </el-dropdown-item>
            <el-dropdown-item v-if="loginStatus">
              <div @click="$router.push('/person')">个人信息</div>
            </el-dropdown-item>
            <el-dropdown-item v-if="loginStatus">
              <div @click="logout">退出</div>
            </el-dropdown-item>
          </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-col>
    </el-row>
  </div>
</template>


<script setup>
import { ref, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, User } from '@element-plus/icons-vue'
import { useStore } from 'vuex'

const store = useStore()
const router = useRouter()
const route = useRoute()

const props = defineProps({
  user: Object,
  loginStatus: Boolean,
  role: String
})

// 调试：监听 loginStatus 变化
watch(() => props.loginStatus, (newVal) => {
  console.log('Navagation.vue - loginStatus:', newVal)
  console.log('Navagation.vue - user:', props.user)
  console.log('Navagation.vue - role:', props.role)
}, { immediate: true })

const baseApi = store.state.baseApi
const searchText = ref('')

// 根据当前路由动态计算 activeIndex
const activeIndex = computed(() => {
  const path = route.path
  
  // 后台管理
  if (path.startsWith('/manage')) {
    return '/manage'
  }
  
  // 购物车
  if (path === '/cart') {
    return '/cart'
  }
  
  // 我的订单
  if (path === '/orderlist') {
    return '/orderlist'
  }
  
  // 地址管理
  if (path === '/addressManage') {
    return '/addressManage'
  }
  
  // 在线留言
  if (path === '/message') {
    return '/message'
  }
  
  // 售后申请
  if (path === '/afterSale') {
    return '/afterSale'
  }
  
  // 商品详情页也算在商品列表下
  if (path.startsWith('/goodview') || path === '/goodList') {
    return '/goodList'
  }
  
  // 默认首页
  return '/'
})

const logout = () => {
  localStorage.removeItem('user')
  ElMessage.success('退出成功')
  // 退出后刷新页面，确保所有状态重置
  window.location.href = '/'
}

const handleSearch = () => {
  if (searchText.value && searchText.value.trim()) {
    router.push({
      path: '/goodList',
      query: { searchText: searchText.value.trim() }
    })
  }
}
</script>
<style>
a {
  text-decoration: none;
}
.navagation {
  width: 100%;
  height: 60px;
  line-height: 60px;
  background-color: white;
  overflow: hidden;
}
.avatar {
  width: 45px;
  border-radius: 5px;
  position: relative;
  top: 10px;
  right: 5px;
}
.menu-item {
  padding-left: 30px;
  padding-right: 30px;
  font-size: 16px !important;
  font-weight: 500;
}
.nav-search {
  padding: 10px 10px;
  line-height: normal;
}
</style>
