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
import request from '@/utils/request'

const num = ref(10)
const good = ref([])
const categories = ref([])

onMounted(() => {
  //先查询分类id和名称
  request.get('/api/category').then(res => {
    if (res.code === '200') {
      categories.value = res.data
    }
    //获取排行数据
    request.get('/api/good/rank/', { params: { num: num.value } }).then(res => {
      if (res.code === '200') {
        good.value = res.data
      }
    })
  })
})
</script>

<style scoped>

</style>