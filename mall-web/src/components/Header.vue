
<template>
  <div style="display: flex;">
    <div style="flex: 1;">
    <!--          收缩按钮-->
    <span style="font-size: 25px;cursor: pointer" :class="collapseIcon" @click="emit('collapse')" :title="collapseTitle"></span>
    <!--          收缩按钮-->
    <span style="font-size: 25px;cursor: pointer;margin-left: 10px" class='iconfont icon-r-left' v-on:click="back" title="返回"></span>
<!--    页签-->
    <el-breadcrumb style="display: inline-block; margin-left: 30px;font-size: 22px">
      <el-breadcrumb-item @click="router.push('/manage/home')">首页</el-breadcrumb-item>
      <el-breadcrumb-item>{{routePath}}</el-breadcrumb-item>
    </el-breadcrumb>
    </div>
    <!--          设置按钮-->
    <el-dropdown style="margin-right: 40px;cursor: pointer">
    <span class="el-dropdown-link">
      <div style="display: inline-block;font-size: 22px;font-weight: 600;">
        <img v-if="user && user.avatarUrl" :src="baseApi + user.avatarUrl" class="avatar" @error="onImgError">
        <el-icon v-else class="avatar-icon"><UserFilled /></el-icon>
          {{user.nickname }}
      <i class="el-icon-arrow-down el-icon--right" style="margin-right: 15px"></i>
      </div>
    </span>
      <template #dropdown>
        <el-dropdown-menu style="text-align: center">
          <el-dropdown-item @click="router.push('/manage/person')">个人信息</el-dropdown-item>
          <el-dropdown-item @click="logout">退出</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>

</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import { useStore } from 'vuex'

const store = useStore()
const router = useRouter()
const route = useRoute()

const props = defineProps({
  collapseIcon: String,
  collapseTitle: String,
  user: Object
})

const emit = defineEmits(['collapse'])

const routePath = ref('')
const baseApi = store.state.baseApi

const logout = () => {
  localStorage.removeItem('user')
  ElMessage.success('退出成功')
  window.location.href = '/'
}

const back = () => {
  router.go(-1)
}

// 头像加载失败时隐藏破图，表现为默认图标
 const onImgError = (e) => {
  e.target.style.display = 'none'
}

watch(
  () => route,
  () => {
    routePath.value = route.meta.path
  },
  { immediate: true }
)

onMounted(() => {
  routePath.value = route.meta.path
})
</script>

<style scoped>
.avatar{
  width: 60px;
  border-radius: 10px;
  position: relative;
  top: 10px;
  right: 5px;
}
.avatar-icon {
  font-size: 36px;
  margin-right: 8px;
  vertical-align: middle;
  color: #909399;
}
</style>
