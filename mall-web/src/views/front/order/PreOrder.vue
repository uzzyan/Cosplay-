<template>
  <div style="margin-top: 10px; width: 90%; margin: 10px auto">
    <div style="background-color: white; padding: 10px; border-radius: 12px">
      <!--收货地址-->
      <div
        style="
          padding: 10px;
          margin-bottom: 20px;
          border-bottom: 1px solid #eee;
        "
      >
        <div
          style="
            font-size: 20px;
            border-bottom: 2px solid dodgerblue;
            padding-bottom: 10px;
            margin-bottom: 20px;
          "
        >
          收货地址
          <el-button style="height: 25px; padding: 5px" @click="addAddress"
            >+</el-button
          >
        </div>
        <span v-for="(item, index) in addressData" :key="index">
          <address-box
            :address="item"
            :selected="index === checkedIndex"
            style="margin-right: 20px; margin-bottom: 20px;"
            @edit="editAddress(item)"
            @delete="deleteAddress(item)"
            @click="select(index)"
          ></address-box>
        </span>
      </div>
      <!--      地址弹窗-->
      <el-dialog title="地址信息" v-model="dialogFormVisible">
        <el-form label-width="90px" style="padding: 0 60px">
          <el-form-item label="联系人">
            <el-input v-model="address.linkUser" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="联系电话" required>
            <el-input 
              v-model="address.linkPhone" 
              autocomplete="off" 
              placeholder="请输入11位联系电话"
              maxlength="11"
              show-word-limit
              @input="address.linkPhone = address.linkPhone.replace(/[^\d]/g, '')"
            ></el-input>
          </el-form-item>
          <el-form-item label="地址">
            <el-input
              v-model="address.linkAddress"
              autocomplete="off"
            ></el-input>
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="dialogFormVisible = false" style="font-size: 20px;"> 取消</el-button>
            <el-button type="primary" @click="saveAddress" style="font-size: 20px;"><i class="iconfont icon-r-yes" style="font-size: 22px;"></i> 确定</el-button>
          </div>
        </template>
      </el-dialog>

      <!--        商品确认-->
      <el-table :data="goods" stripe style="width: 100%">
        <el-table-column label="商品图片" width="150">
          <template #default="scope">
            <el-image
              :src="baseApi + scope.row.imgs"
              style="width: 100px; height: 100px"
              fit="contain"
            ></el-image>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称"></el-table-column>
        <el-table-column prop="standard" label="规格"></el-table-column>
        <el-table-column prop="realPrice" label="单价"></el-table-column>
        <el-table-column prop="num" label="数量"></el-table-column>
        <el-table-column label="价格">
          <template #default="scope">
            {{ (scope.row.realPrice * scope.row.num).toFixed(2) }}
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 10px">
        <div style="background-color: white; padding: 10px">
          <div style="color: red; text-align: right">
            <div>
              <span>商品总价：</span>
              <span>￥ {{ sumPrice }}</span>
            </div>
            <div style="text-align: right; color: #999; font-size: 12px">
              优惠： ￥{{ sumDiscount }}
            </div>
            <div style="padding: 10px 0">
              <el-button
                style="background-color: red; color: white;"
                @click="submitOrder"
                > 提交订单</el-button
              >
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useStore } from 'vuex'
import AddressBox from '@/components/AddressBox.vue'
import API from '@/utils/request'

const store = useStore()
const route = useRoute()
const router = useRouter()

const baseApi = store.state.baseApi
const userId = ref(0)
const addressData = ref([])
const address = ref({})
const checkedIndex = ref(0)
const dialogFormVisible = ref(false)
const good = ref({})
const realPrice = ref(-1)
const goods = ref([])
const cartId = ref('')

const select = (index) => {
  checkedIndex.value = index
}

const addAddress = () => {
  address.value = {}
  dialogFormVisible.value = true
}

const editAddress = (item) => {
  //深拷贝
  address.value = JSON.parse(JSON.stringify(item))
  dialogFormVisible.value = true
}

const deleteAddress = (item) => {
  ElMessageBox.confirm('您确认删除该地址吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    API.delete('api/address/' + item.id).then((res) => {
      if (res.code === '200') {
        ElMessage.success('删除地址成功')
        loadAddress()
      }
    })
  })
}

const saveAddress = () => {
  address.value.userId = userId.value
  API.post('/api/address', address.value).then((res) => {
    if (res.code === '200') {
      ElMessage.success('保存成功')
      loadAddress()
      dialogFormVisible.value = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const loadAddress = () => {
  API.get('/userid').then((res) => {
    userId.value = res
    API.get('/api/address/' + res).then((res) => {
      if (res.code === '200') {
        addressData.value = res.data
      }
    })
  })
}

const sumPrice = computed(() => {
  let sum = 0
  goods.value.forEach(function (good) {
    sum += good.realPrice * good.num
  })
  return sum.toFixed(2)
})

const sumDiscount = computed(() => {
  let sum = 0
  goods.value.forEach(function (good) {
    sum += (good.realPrice / good.discount - good.realPrice) * good.num
  })
  return sum.toFixed(2)
})

const submitOrder = () => {
  let address = addressData.value[checkedIndex.value]
  if (!address) {
    ElMessage({
      type: 'warning',
      message: '请选择收货地址！'
    })
    return
  }
  // 提交订单
  let totalPrice = sumPrice.value
  API.post('/api/order', {
    totalPrice: totalPrice,
    linkUser: address.linkUser,
    linkPhone: address.linkPhone,
    linkAddress: address.linkAddress,
    state: '待付款',
    goods: JSON.stringify(goods.value),
    cartId: cartId.value
  }).then((res) => {
    if (res.code === '200') {
      let orderNo = res.data
      //跳转到支付页面
      router.replace({
        path: 'pay',
        query: { money: totalPrice, orderNo: orderNo }
      })
    } else {
      ElMessage({
        type: 'error',
        message: res.msg
      })
    }
  })
}

onMounted(() => {
  loadAddress()

  good.value = JSON.parse(route.query.good)
  good.value.realPrice = route.query.realPrice
  good.value.num = route.query.num
  good.value.standard = route.query.standard
  cartId.value = route.query.cartId
  goods.value.push(good.value)
})
</script>

<style scoped>
</style>
