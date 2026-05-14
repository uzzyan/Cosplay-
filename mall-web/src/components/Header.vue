
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
        <img :src="baseApi + user.avatarUrl" class="avatar">
          {{user.nickname }}
      <i class="el-icon-arrow-down el-icon--right" style="margin-right: 15px"></i>
      </div>
    </span>
      <template #dropdown>
        <el-dropdown-menu style="text-align: center">
          <el-dropdown-item>
            <div @click="router.push('/manage/person')">个人信息</div>
          </el-dropdown-item>
          <el-dropdown-item>
            <div @click="logout">退出</div>
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>

</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
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
</style>
