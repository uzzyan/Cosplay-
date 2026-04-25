<template>
  <div>
    <el-tabs v-model="activeName" @tab-click="handleClick">
      <div
        id="chartSum"
        style="
          display: inline-block;
          margin-left: 50px;
          margin-top: 30px;
          font-weight: bold;
          font-size: 22px;
          color: #ffb02a;
          border: 1px lightgrey solid;
          border-radius: 10px;
          padding: 20px;
        "
        > 总计：￥{{ numFilter(total) }}</div
      >
      <!--      柱状图-->
      <el-tab-pane label="各类收入柱状图" name="bar">
        <div
          id="bar"
          style="width: 1200px; height: 500px; margin: auto auto"
        ></div>
      </el-tab-pane>
      <!--      饼图-->
      <el-tab-pane label="各类收入饼图" name="pie">
        <div
          id="pie"
          style="width: 600px; height: 600px; margin: 10px auto"
        ></div>
      </el-tab-pane>
      <!--  本周收入折线图-->
      <el-tab-pane label="本周收入" name="line1">
        <div
          id="weekLine"
          style="width: 900px; height: 500px; margin: 10px auto"
        ></div>
      </el-tab-pane>
      <!-- 本月收入折线图-->
      <el-tab-pane label="本月收入" name="line2">
        <div
          id="monthLine"
          style="width: 1500px; height: 500px; margin: 10px auto"
        ></div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

const activeName = ref('bar')
const total = ref(0)
const totalAll = ref(0)
const totalWeek = ref(0)
const totalMonth = ref(0)

const charts = ref([])

// 替代filters的函数
const numFilter = (value) => {
  return Number(Number(value).toFixed(2))
}

const handleClick = (tab) => {
  switch (tab.name) {
    case 'bar':
    case 'pie':
      total.value = totalAll.value
      break
    case 'line1':
      total.value = totalWeek.value
      break
    case 'line2':
      total.value = totalMonth.value
      break
  }
}

onMounted(() => {
  // 初始化图表
  const barChart = echarts.init(document.getElementById('bar'))
  const pieChart = echarts.init(document.getElementById('pie'))
  const lineChart1 = echarts.init(document.getElementById('weekLine'))
  const lineChart2 = echarts.init(document.getElementById('monthLine'))
  
  charts.value = [barChart, pieChart, lineChart1, lineChart2]
  
  const barOption = {
    tooltip: { trigger: 'item' },
    title: { text: '收入统计柱状图', x: 'center' },
    label: { show: true, position: 'top' },
    xAxis: { type: 'category', data: [] },
    yAxis: { type: 'value' },
    series: [{ data: [], type: 'bar' }]
  }
  
  const pieOption = {
    tooltip: { trigger: 'item' },
    title: { text: '收入统计饼图', x: 'center' },
    series: [{ type: 'pie', data: [] }]
  }
  
  const lineOption1 = {
    tooltip: { trigger: 'item' },
    label: { show: true },
    title: { text: '本周收入', x: 'center' },
    xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
    yAxis: { type: 'value' },
    series: [{ data: [], type: 'line' }]
  }
  
  const lineOption2 = {
    tooltip: { trigger: 'item' },
    label: { show: true },
    title: { text: '本月收入', x: 'center' },
    xAxis: { type: 'category', data: [] },
    yAxis: { type: 'value' },
    series: [{ data: [], type: 'line' }]
  }
  
  // 渲染柱状图和饼图
  request.get('/api/income/chart').then((res) => {
    if (res.code === '200') {
      const categoryIncomes = res.data.categoryIncomes
      const categoryNames = categoryIncomes.map(item => item.categoryName)
      const incomes = categoryIncomes.map(item => item.categoryIncome)
      
      barOption.xAxis.data = categoryNames
      barOption.series[0].data = incomes
      barChart.setOption(barOption)
      
      const pieData = categoryNames.map((name, i) => ({
        value: incomes[i],
        name: name
      }))
      pieOption.series[0].data = pieData
      pieChart.setOption(pieOption)
      
      const sum = incomes.reduce((acc, val) => acc + val, 0)
      total.value = sum
      totalAll.value = sum
    }
  }).catch(err => {
    console.error('加载收入图表失败:', err)
  })
  
  // 渲染本周折线图
  request.get('/api/income/week').then((res) => {
    if (res.code === '200') {
      const weekIncome = res.data.weekIncome
      lineOption1.series[0].data = weekIncome
      lineChart1.setOption(lineOption1)
      
      const sum = weekIncome.reduce((acc, val) => acc + val, 0)
      totalWeek.value = sum
    }
  }).catch(err => {
    console.error('加载本周收入失败:', err)
  })
  
  // 渲染本月折线图
  request.get('/api/income/month').then((res) => {
    if (res.code === '200') {
      lineOption2.xAxis.data = res.data.monthDays
      lineOption2.series[0].data = res.data.monthIncome
      lineChart2.setOption(lineOption2)
      
      const sum = res.data.monthIncome.reduce((acc, val) => acc + val, 0)
      totalMonth.value = sum
    }
  }).catch(err => {
    console.error('加载本月收入失败:', err)
  })
})

onBeforeUnmount(() => {
  // 清理所有ECharts实例
  charts.value.forEach(chart => chart.dispose())
})
</script>

<style scoped>
</style>