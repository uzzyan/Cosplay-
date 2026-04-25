<template>
  <el-menu :default-openeds="['file', 'report']" style="height: 100%;"
           background-color="transparent"
           text-color ="#333"
           :collapse-transition="false"
           :collapse="isCollapse"
           router
           class="gradient-menu"
  >
    <div style="height: 60px;margin-left: 30px; line-height: 60px">
      <!--<router-link to="/manage/home">
        <img src="../resource/logo.png" style="width: 32px;position: relative; top: 7px;right: 6px;">
      </router-link>-->
      <span style="color: #333;font-size: 20px;font-weight: 600;" v-show="!isCollapse">U次元 - Cosplay服装商城</span>
    </div>
    <!-- 用户管理 -->
    <el-menu-item index="/manage/user" v-if="menuFlags.userMenu" class="el-item-menu" style="font-size: 16px">
      <i class="el-icon-user" style="font-size: 20px;color: #606266;"></i>
      <span>用户管理</span>
    </el-menu-item>

    <!-- 分类管理 -->
    <el-menu-item index="/manage/category" v-if="menuFlags.categoryMenu" class="el-item-menu" style="font-size: 16px">
      <i class="el-icon-menu" style="font-size: 20px;color: #606266;"></i>
      <span>分类管理</span>
    </el-menu-item>

    <!-- 商品管理 -->
    <el-menu-item index="/manage/good" v-if="menuFlags.goodMenu" class="el-item-menu" style="font-size: 16px">
      <i class="el-icon-goods" style="font-size: 20px;color: #606266;"></i>
      <span>商品管理</span>
    </el-menu-item>

    <!-- 订单管理 -->
    <el-menu-item index="/manage/order" v-if="menuFlags.orderMenu" class="el-item-menu" style="font-size: 16px">
      <i class="el-icon-document" style="font-size: 20px;color: #606266;"></i>
      <span>订单管理</span>
    </el-menu-item>

    <!-- 轮播图管理 -->
    <el-menu-item index="/manage/carousel" v-if="menuFlags.carouselMenu" class="el-item-menu" style="font-size: 16px">
      <i class="el-icon-picture-outline" style="font-size: 20px;color: #606266;"></i>
      <span>轮播图管理</span>
    </el-menu-item>

    <!-- 留言管理 -->
    <el-menu-item index="/manage/message" v-if="menuFlags.messageMenu" class="el-item-menu" style="font-size: 16px">
      <i class="el-icon-chat-dot-round" style="font-size: 20px;color: #606266;"></i>
      <span>留言管理</span>
    </el-menu-item>

    <!-- 售后管理 -->
    <el-menu-item index="/manage/afterSale" v-if="menuFlags.afterSaleMenu" class="el-item-menu" style="font-size: 16px">
      <i class="el-icon-s-order" style="font-size: 20px;color: #606266;"></i>
      <span>售后管理</span>
    </el-menu-item>

    <!-- 文件管理 -->
    <el-submenu v-if="fileGroup" index="file" class="el-item-menu" style="font-size: 16px">
      <template #title>
        <i class="el-icon-folder-opened" style="font-size: 20px;color: #606266;"></i>
        <span>文件管理</span>
      </template>
      <el-menu-item index="/manage/file" v-if="menuFlags.fileMenu">文件管理</el-menu-item>
      <el-menu-item index="/manage/avatar" v-if="menuFlags.avatarMenu">头像管理</el-menu-item>
    </el-submenu>

    <!-- 数据报表 -->
    <el-submenu v-if="incomeGroup" index="report" class="el-item-menu" style="font-size: 16px">
      <template #title>
        <i class="el-icon-data-analysis" style="font-size: 20px;color: #606266;"></i>
        <span>数据报表</span>
      </template>
      <el-menu-item index="/manage/incomeChart" v-if="menuFlags.incomeChartMenu">图表数据</el-menu-item>
      <el-menu-item index="/manage/incomeRank" v-if="menuFlags.incomeRankMenu">收入数据</el-menu-item>
    </el-submenu>

    <!-- 前台 -->
    <el-menu-item index="/" class="el-item-menu" style="font-size: 16px">
      <i class="el-icon-s-home" style="font-size: 20px;color: #606266;"></i>
      <span>前台首页</span>
    </el-menu-item>
  </el-menu>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'

const props = defineProps({
  isCollapse: Boolean
})

const role = ref('user')
const menuFlags = ref({
  userMenu: false,
  fileMenu: false,
  avatarMenu: false,
  goodMenu: false,
  carouselMenu: false,
  orderMenu: false,
  categoryMenu: false,
  messageMenu: false,
  afterSaleMenu: false,
  incomeChartMenu: false,
  incomeRankMenu: false
})

const fileGroup = computed(() => {
  return menuFlags.value.fileMenu || menuFlags.value.avatarMenu
})

const incomeGroup = computed(() => {
  return menuFlags.value.incomeChartMenu || menuFlags.value.incomeRankMenu
})

const setAdminMenus = () => {
  menuFlags.value.userMenu = true
  menuFlags.value.fileMenu = true
  menuFlags.value.avatarMenu = true
  menuFlags.value.categoryMenu = true
  menuFlags.value.goodMenu = true
  menuFlags.value.carouselMenu = true
  menuFlags.value.orderMenu = true
  menuFlags.value.messageMenu = true
  menuFlags.value.afterSaleMenu = true
  menuFlags.value.incomeChartMenu = true
  menuFlags.value.incomeRankMenu = true
}

onMounted(() => {
  // 先从 localStorage 读取角色，立即显示菜单（避免闪烁）
  let stored = localStorage.getItem('user')
  if (stored) {
    let parsedUser = JSON.parse(stored)
    if (parsedUser.role === 'admin') {
      role.value = 'admin'
      setAdminMenus()
    }
  }
  // 再从后端确认角色
  request.post('/role').then(res => {
    if (res.code === '200') {
      role.value = res.data
      if (role.value === 'admin') {
        setAdminMenus()
      }
    }
  })
})
</script>

<style scoped>
.gradient-menu {
  background: linear-gradient(180deg, #f5f3ff 0%, #f0f9ff 100%) !important;
}

/* 菜单项悬停效果 */
.gradient-menu :deep(.el-menu-item:hover) {
  background-color: rgba(64, 158, 255, 0.1) !important;
}

/* 子菜单标题悬停效果 */
.gradient-menu :deep(.el-submenu__title:hover) {
  background-color: rgba(64, 158, 255, 0.1) !important;
}

/* 激活状态的菜单项 */
.gradient-menu :deep(.el-menu-item.is-active) {
  background-color: rgba(64, 158, 255, 0.15) !important;
  color: #409EFF !important;
}

/* 激活状态的菜单项图标 */
.gradient-menu :deep(.el-menu-item.is-active i) {
  color: #409EFF !important;
}

/* 子菜单项 */
.gradient-menu :deep(.el-menu) {
  background-color: transparent !important;
}

/* 子菜单项悬停 */
.gradient-menu :deep(.el-menu--inline .el-menu-item:hover) {
  background-color: rgba(64, 158, 255, 0.08) !important;
}

/* 展开的子菜单背景 */
.gradient-menu :deep(.el-menu--inline) {
  background-color: rgba(255, 255, 255, 0.3) !important;
}
</style>
