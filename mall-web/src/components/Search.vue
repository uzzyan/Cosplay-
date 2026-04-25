
<template>
  <!--      搜索栏-->
  <el-row>
    <el-col :span="24" style="text-align: center;">
      <div class="search" style="display: inline-block">
        <div style="height: 60px;margin-left: 30px">
          <input @keydown.enter="emit('search', searchText)" type="text" placeholder="请输入商品" v-model="searchText">
          <button @click="emit('search', searchText)"></button>
        </div>
      </div>
    </el-col>
  </el-row>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  value: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['search'])

const searchText = ref(props.value)

watch(
  () => props.value,
  (newVal) => {
    searchText.value = newVal
  }
)
</script>

<style scoped>

</style>
