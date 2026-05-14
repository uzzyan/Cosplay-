<!--
 * @Description: 
 * @Author: gzc
 * @Date: 2024-03-21 15:27:05
-->
<template >
  <div style="width: 58%;height:100%;margin: 20px auto;">
    <span v-for="(good,index) in good" :key="index">
      <income-item :index="index+1" :good="good" :categories="categories" style="margin-bottom: 2px"></income-item>
    </span>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import IncomeItem from '@/components/IncomeItem.vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const num = ref(10)
const good = ref([])
const categories = ref([])

onMounted(() => {
  //先查询分类id和名称
  request.get('/api/category').then(res => {
    if (res.code === '200') {
      categories.value = Array.isArray(res.data) ? res.data : []
    } else if (res.code === '401') {
      ElMessage.error('登录状态已失效，请重新登录')
      categories.value = []
    } else {
      ElMessage.error(res.msg || '加载数据失败')
      categories.value = []
    }
    //获取排行数据
    request.get('/api/good/rank/', { params: { num: num.value } }).then(res => {
      if (res.code === '200') {
        good.value = Array.isArray(res.data) ? res.data : []
      } else if (res.code === '401') {
        ElMessage.error('登录状态已失效，请重新登录')
        good.value = []
      } else {
        ElMessage.error(res.msg || '加载数据失败')
        good.value = []
      }
    }).catch(err => {
      console.error('加载排行数据失败:', err)
      ElMessage.error('加载失败，请检查网络连接')
      good.value = []
    })
  }).catch(err => {
    console.error('加载分类数据失败:', err)
    ElMessage.error('加载失败，请检查网络连接')
    categories.value = []
  })
})
</script>

<style scoped>

</style>