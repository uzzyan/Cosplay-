<template>
    <div>
        <div>
            <el-select
                v-model="searchMode"
                placeholder="请选择订单类型"
                style="width: 150px; margin-right: 10px"
            >
                <el-option value="已支付" label="已支付"></el-option>
                <el-option value="已发货" label="已发货"></el-option>
                <el-option value="已收货" label="已收货"></el-option>
            </el-select>
            <el-input
                v-model="searchText"
                @keyup.enter="load"
                style="width: 200px"
            >
                <template #prefix>
                    <i class="el-input__icon iconfont icon-r-find"></i>
                </template>
            </el-input>
            <el-button @click="reset" type="warning" style="margin: 10px">
                
                重置
            </el-button>
            <el-button @click="load" type="primary" style="margin: 10px">
                
                搜索
            </el-button>
            <el-button @click="exportOrders" type="success" style="margin: 10px">
                导出订单
            </el-button>
        </div>
        <el-table :data="tableData" border stripe style="width: 100%">
            <el-table-column prop="id" label="ID" width="50" sortable>
            </el-table-column>
            <el-table-column
                prop="orderNo"
                label="订单编号"
                width="200"
            ></el-table-column>
            <el-table-column
                prop="totalPrice"
                label="总价"
                width="100"
            ></el-table-column>
            <el-table-column
                prop="userId"
                label="下单人id"
                width="100"
            ></el-table-column>
            <el-table-column
                prop="linkUser"
                label="联系人"
                width="150"
            ></el-table-column>
            <el-table-column
                prop="linkPhone"
                label="联系电话"
            ></el-table-column>
            <el-table-column
                prop="linkAddress"
                label="送货地址"
                width="300"
            ></el-table-column>
            <el-table-column prop="state" label="状态" width="100">
                <template #default="scope">
                    <el-tag
                        type="success"
                        v-if="scope.row.state === '已支付'"
                        >{{ scope.row.state }}</el-tag
                    >
                    <el-tag
                        type="primary"
                        v-if="scope.row.state === '已发货'"
                        >{{ scope.row.state }}</el-tag
                    >
                    <el-tag type="info" v-if="scope.row.state === '已收货'">{{
                        scope.row.state
                    }}</el-tag>
                </template>
            </el-table-column>
            <el-table-column
                prop="createTime"
                label="下单时间"
            ></el-table-column>
            <el-table-column fixed="right" label="操作" width="250">
                <template #default="scope">
                    <el-button
                        type="primary"
                        @click="showDetail(scope.row)"
                    >
                        详情
                    </el-button>
                    <el-popconfirm
                        @confirm="delivery(scope.row)"
                        title="确定发货吗？"
                        v-if="scope.row.state === '已支付'"
                    >
                        <template #reference>
                            <el-button
                                type="primary"
                                style="margin-left: 10px"
                            >
                                发货
                            </el-button>
                        </template>
                    </el-popconfirm>
                </template>
            </el-table-column>
        </el-table>
        <!--    分页-->
        <div style="margin-top: 10px">
            <el-pagination
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                :current-page="pageNum"
                :page-size="pageSize"
                :page-sizes="[3, 5, 8, 10]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="total"
            >
            </el-pagination>
        </div>
        <!--    详情弹窗-->
        <el-dialog v-model="dialogFormVisible">
            <el-table :data="detail" background-color="black">
                <el-table-column label="图片" width="150">
                    <template #default="scope">
                        <!-- Fix2: imgs 可能是 JSON 数组字符串，取第一张展示 -->
                        <img
                            v-if="scope.row"
                            :src="baseApi + getFirstImg(scope.row.imgs)"
                            min-width="100"
                            height="100"
                        />
                    </template>
                </el-table-column>

                <el-table-column prop="goodId" label="商品id"></el-table-column>
                <el-table-column
                    prop="goodName"
                    label="商品名称"
                ></el-table-column>
                <el-table-column
                    prop="standard"
                    label="商品规格"
                ></el-table-column>
                <el-table-column prop="price" label="单价"></el-table-column>
                <el-table-column prop="discount" label="折扣"></el-table-column>
                <el-table-column label="实价">
                    <template #default="scope">
                        {{ scope.row ? scope.row.price * scope.row.discount : 0 }}
                    </template>
                </el-table-column>
                <el-table-column prop="count" label="数量"></el-table-column>
                <el-table-column label="总价">
                    <template #default="scope">
                        {{
                            scope.row ? scope.row.price * scope.row.discount * scope.row.count : 0
                        }}
                    </template>
                </el-table-column>
            </el-table>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useStore } from 'vuex'
import API from '@/utils/request'

const store = useStore()

const options = ref([])
const searchMode = ref('')
const searchText = ref('')
const user = ref({})
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(8)
const entity = ref({})
const total = ref(0)
const dialogFormVisible = ref(false)
const detail = ref([])
const baseApi = store.state.baseApi

const url = '/api/order/'

const handleSizeChange = (pageSizeVal) => {
  pageSize.value = pageSizeVal
  load()
}

const handleCurrentChange = (pageNumVal) => {
  pageNum.value = pageNumVal
  load()
}

const reset = () => {
  searchMode.value = ''
  searchText.value = ''
  load()
}

const load = () => {
  API.get(url + '/page', {
    params: {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      orderNo: searchText.value,
      state: searchMode.value
    }
  })
    .then((res) => {
      if (res.code === '200') {
        tableData.value = Array.isArray(res.data.records) ? res.data.records : []
        total.value = res.data.total || 0
      } else if (res.code === '401') {
        ElMessage.error('登录状态已失效，请重新登录')
        tableData.value = []
        total.value = 0
      } else {
        ElMessage.error(res.msg || '加载数据失败')
        tableData.value = []
        total.value = 0
      }
    })
    .catch((e) => {
      console.error('加载订单数据失败:', e)
      ElMessage.error('加载失败，请检查网络连接')
      tableData.value = []
      total.value = 0
    })
}

const exportOrders = () => {
  API({
    url: url + 'export',
    method: 'get',
    params: {
      orderNo: searchText.value,
      state: searchMode.value
    },
    responseType: 'blob'
  }).then((data) => {
    const blob = new Blob([data], { type: 'text/csv;charset=utf-8;' })
    const urlObject = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    const now = new Date()
    const pad = (n) => (n < 10 ? '0' + n : '' + n)
    const fileName =
      'orders_' +
      now.getFullYear() +
      pad(now.getMonth() + 1) +
      pad(now.getDate()) +
      pad(now.getHours()) +
      pad(now.getMinutes()) +
      pad(now.getSeconds()) +
      '.csv'
    a.href = urlObject
    a.download = fileName
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlObject)
  })
}

const showDetail = (row) => {
  API.get('/api/order/orderNo/' + row.orderNo).then((res) => {
    if (res.code === '200') {
      // 确保详情数据是数组
      detail.value = Array.isArray(res.data) ? res.data : [res.data]
      dialogFormVisible.value = true
    } else if (res.code === '401') {
      ElMessage.error('登录状态已失效，请重新登录')
      detail.value = []
    } else {
      ElMessage.error(res.msg || '加载订单详情失败')
      detail.value = []
    }
  }).catch((err) => {
    console.error('加载订单详情失败:', err)
    ElMessage.error('加载失败，请检查网络连接')
    detail.value = []
  })
}

// Fix2: 解析 imgs 字段，imgs 可能是 JSON 数组字符串，取第一张
const getFirstImg = (imgs) => {
  if (!imgs) return ''
  try {
    const arr = JSON.parse(imgs)
    return Array.isArray(arr) ? arr[0] : imgs
  } catch {
    return imgs
  }
}

//发货
const delivery = (order) => {
  API.post('/api/order/delivery/' + order.orderNo).then((res) => {
    if (res.code === '200') {
      ElMessage.success('成功发货')
      order.state = '已发货'
    } else if (res.code === '401') {
      ElMessage.error('登录状态已失效，请重新登录')
    } else {
      ElMessage.error(res.msg || '发货失败')
    }
  }).catch((err) => {
    console.error('发货失败:', err)
    ElMessage.error('发货失败，请重试')
  })
}

onMounted(() => {
  load()
})
</script>

<style scoped>
</style>
