
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
          <el-menu-item index="/cart" class="menu-item"
            >购物车</el-menu-item
          >
          <el-menu-item index="/orderlist" class="menu-item"
            >我的订单</el-menu-item
          >
          <el-menu-item index="/addressManage" class="menu-item"
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
            prefix-icon="el-icon-search"
            size="medium"
            clearable
          >
            <template #append>
              <el-button
                icon="Search"
                @click="handleSearch"
              ></el-button>
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
                v-if="user.avatarUrl != null"
                :src="baseApi + user.avatarUrl"
                class="avatar"
              />
              {{ user.nickname }}
              <i
                class="el-icon-arrow-down el-icon--right"
                style="margin-right: 5px"
              ></i>
            </div>
          </span>
          <!--          下拉菜单-->
          <template #dropdown>
            <el-dropdown-menu style="text-align: center">
            <el-dropdown-item>
              <!--              传给前端，登录后跳转页面的path为 "/"-->
              <div
                @click="$router.push({ path: '/login', query: { to: '/' } })"
                v-show="!loginStatus"
              >
                登录
              </div>
            </el-dropdown-item>
            <el-dropdown-item v-show="loginStatus">
              <div @click="$router.push('/person')">个人信息</div>
            </el-dropdown-item>
            <el-dropdown-item v-show="loginStatus">
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
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useStore } from 'vuex'

const store = useStore()
const router = useRouter()

const props = defineProps({
  user: Object,
  loginStatus: Boolean,
  role: String
})

const baseApi = store.state.baseApi
const searchText = ref('')

const logout = () => {
  localStorage.removeItem('user')
  ElMessage.success('退出成功')
  router.push('/') // 使用路由跳转
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
