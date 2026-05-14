<template>
  <div>
    <div>
      <el-table :data="tableData" border stripe style="width: 80%;margin: 2px auto">
        <el-table-column label="商品">
          <template #default="scope">
            <a :href="'/goodView/'+scope.row.goodId">{{scope.row.goodName}}</a>
          </template>
        </el-table-column>
        <el-table-column  label="图片" >
          <template #default="scope">
            <img :src="baseApi + scope.row.img" width="300" height="185" />
          </template>
        </el-table-column>
        <el-table-column prop="showOrder" label="顺序"></el-table-column>

        <el-table-column
            fixed="right"
            label="操作"
            width="250">
          <template #default="scope">
            <el-button type="primary" style="font-size: 18px;"  @click="edit(scope.row)">
               编辑</el-button>
            <el-popconfirm
                @confirm="del(scope.row.id)"
                title="确定删除？"
            >
              <template #reference>
                <el-button type="danger" style="margin-left: 10px;font-size: 18px;">
                 删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>
<!--新增按钮-->
    <div style="text-align: center">
      <el-button @click="add" type="primary" style="margin: 30px;width: 150px; font-size: 20px;">

        新增
      </el-button>
    </div>
    <!-- 弹窗   -->

    <el-dialog title="轮播图信息" v-model="dialogFormVisible" width="500px"
               :close-on-click-modal="false">
      <el-form :model="entity" label-width="90px">
        <!-- 商品搜索选择 -->
        <el-form-item label="搜索商品">
          <el-input
            v-model="searchGoodText"
            placeholder="输入商品名称搜索"
            clearable
            @input="searchGoods"
            style="width: 100%"
          >
            <template #suffix>
              <span style="color: #909399; font-size: 12px">输入关键字搜索</span>
            </template>
          </el-input>
          <!-- 搜索结果列表 -->
          <div v-if="goodSearchResults.length > 0" class="search-result-list">
            <div
              v-for="g in goodSearchResults"
              :key="g.id"
              class="search-result-item"
              :class="{ 'selected': entity.goodId === g.id }"
              @click="selectGood(g)"
            >
              <img :src="baseApi + g.imgs" class="result-img" />
              <div class="result-info">
                <div class="result-name">{{ g.name }}</div>
                <div class="result-id">ID: {{ g.id }}</div>
              </div>
              <span v-if="entity.goodId === g.id" style="color: #67c23a; margin-left: auto">✓ 已选</span>
            </div>
          </div>
          <div v-else-if="searchGoodText && goodSearchResults.length === 0 && !searching" class="no-result">
            未找到匹配的商品
          </div>
        </el-form-item>

        <!-- 当前已选商品 -->
        <el-form-item label="已选商品">
          <div v-if="selectedGood" class="selected-good-box">
            <img :src="baseApi + selectedGood.imgs" class="selected-img" />
            <div>
              <div style="font-weight: 600">{{ selectedGood.name }}</div>
              <div style="color: #909399; font-size: 12px">ID: {{ selectedGood.id }}</div>
            </div>
          </div>
          <div v-else style="color: #c0c4cc; font-size: 13px">请先搜索并选择商品</div>
        </el-form-item>

        <el-form-item label="轮播顺序">
          <el-select v-model="entity.showOrder" placeholder="请选择顺序">
            <el-option v-for="index in maxOrderOptions" :key="index" :label="index" :value="index">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeDialog" style="font-size: 16px">取消</el-button>
          <el-button type="primary" @click="save" style="font-size: 16px">确定</el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useStore } from 'vuex'
import API from '@/utils/request'

const store = useStore()

const baseApi = store.state.baseApi
const tableData = ref([])
const entity = ref({})
const dialogFormVisible = ref(false)

// 商品搜索相关
const searchGoodText = ref('')
const goodSearchResults = ref([])
const selectedGood = ref(null)
const searching = ref(false)
let searchTimer = null

const url = '/api/carousel/'

// showOrder 最大选项数（新增时 = 现有数量+1，编辑时 = 现有数量）
const isAdding = ref(true)
const maxOrderOptions = computed(() =>
  isAdding.value ? tableData.value.length + 1 : tableData.value.length
)

// 防抖搜索商品
const searchGoods = () => {
  clearTimeout(searchTimer)
  if (!searchGoodText.value.trim()) {
    goodSearchResults.value = []
    return
  }
  searching.value = true
  searchTimer = setTimeout(() => {
    API.get('/api/good/fullPage', {
      params: { pageNum: 1, pageSize: 10, searchText: searchGoodText.value }
    }).then(res => {
      if (res.code === '200') {
        goodSearchResults.value = res.data.records || []
      }
    }).catch(() => {
      goodSearchResults.value = []
    }).finally(() => {
      searching.value = false
    })
  }, 300)
}

// 选择商品
const selectGood = (good) => {
  entity.value.goodId = good.id
  selectedGood.value = good
  searchGoodText.value = good.name
  goodSearchResults.value = []
}

const load = () => {
  API.get(url).then(res => {
    if (res.code === '200') {
      tableData.value = Array.isArray(res.data) ? res.data : []
    } else if (res.code === '401') {
      ElMessage.error('登录状态已失效，请重新登录')
      tableData.value = []
    } else {
      ElMessage.error(res.msg || '加载数据失败')
      tableData.value = []
    }
  }).catch(err => {
    console.error('加载轮播图数据失败:', err)
    ElMessage.error('加载失败，请检查网络连接')
    tableData.value = []
  })
}

const add = () => {
  entity.value = {}
  selectedGood.value = null
  searchGoodText.value = ''
  goodSearchResults.value = []
  isAdding.value = true
  dialogFormVisible.value = true
}

const edit = (row) => {
  entity.value = JSON.parse(JSON.stringify(row))
  selectedGood.value = { id: row.goodId, name: row.goodName, imgs: row.img }
  searchGoodText.value = row.goodName || ''
  goodSearchResults.value = []
  isAdding.value = false
  dialogFormVisible.value = true
}

const closeDialog = () => {
  dialogFormVisible.value = false
  searchGoodText.value = ''
  goodSearchResults.value = []
  selectedGood.value = null
  // 取消时刷新表格，防止 phantom 行残留
  load()
}

const save = () => {
  if (entity.value.goodId == undefined || entity.value.goodId === '') {
    ElMessage.error('商品id不能为空')
    return
  }
  if (entity.value.showOrder == undefined) {
    ElMessage.error('轮播顺序不能为空')
    return
  }

  API.post(url, entity.value).then(res => {
    if (res.code === '200') {
      ElMessage.success('保存成功')
      load()
      dialogFormVisible.value = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const del = (id) => {
  API.delete('/api/carousel/' + id).then(res => {
    if (res.code === '200') {
      ElMessage.success('删除成功')
      load()
    } else if (res.code === '401') {
      ElMessage.error('登录状态已失效，请重新登录')
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  }).catch(err => {
    console.error('删除失败:', err)
    ElMessage.error('删除失败，请重试')
  })
}

onMounted(() => {
  load()
})
</script>

<style scoped>
.search-result-list {
  width: 100%;
  max-height: 220px;
  overflow-y: auto;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  margin-top: 4px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.search-result-item {
  display: flex;
  align-items: center;
  padding: 8px 10px;
  cursor: pointer;
  transition: background 0.15s;
  border-bottom: 1px solid #f5f5f5;
}

.search-result-item:last-child {
  border-bottom: none;
}

.search-result-item:hover,
.search-result-item.selected {
  background: #ecf5ff;
}

.result-img {
  width: 48px;
  height: 48px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 10px;
  flex-shrink: 0;
}

.result-info {
  flex: 1;
  overflow: hidden;
}

.result-name {
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.result-id {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.no-result {
  text-align: center;
  color: #909399;
  font-size: 13px;
  padding: 12px;
}

.selected-good-box {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  background: #f0f9eb;
  border-radius: 6px;
  border: 1px solid #b3e19d;
}

.selected-img {
  width: 48px;
  height: 48px;
  object-fit: cover;
  border-radius: 4px;
}
</style>
