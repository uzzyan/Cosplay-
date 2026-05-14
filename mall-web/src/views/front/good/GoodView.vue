<template>
    <div class="main-box">
        <div>
            <!--    左侧的图片-->
            <!-- <div class="image-box">
        <img :src="baseApi + good.imgs" class="image" />
      </div> -->

            <div class="image-container">
                <img :src="baseApi + good.imgs" alt="Your Image" />
            </div>
            <!--    右侧盒子-->
            <div class="detail-box">
                <!--      商品名与描述-->
                <div>
                    <span style="font-size: 22px"
                        ><strong>{{ good.name }}</strong></span
                    ><br />
                    <div style="margin-top: 8px">
                        <el-tag v-if="isOffShelf" type="info">已下架</el-tag>
                        <el-tag v-else-if="isSoldOutAll" type="danger">售罄</el-tag>
                    </div>
                </div>
                <div style="margin-top: 20px">
                    <span style="font-size: 17px">{{ good.description }}</span>
                </div>
                <!--      价格盒子-->

                <div class="price-box" v-if="good.discount < 1">
                    <dl>
                        <div>
                            <dt>原价</dt>
                            <dd style="text-decoration: line-through">
                                <b>{{ price }}</b
                                >元
                            </dd>
                        </div>
                        <div>
                            <dt>折扣</dt>
                            <dd>{{ discount }}</dd>
                        </div>
                        <div>
                            <dt>现价</dt>
                            <dd style="color: red; font-size: 25px">
                                <b>{{ realPrice }}</b
                                >元
                            </dd>
                        </div>
                    </dl>
                </div>
                <div class="price-box" v-if="good.discount === 1">
                    <dl>
                        <div>
                            <dt>价格</dt>
                            <dd style="color: red; font-size: 25px">
                                ￥ <b>{{ price }}</b>
                            </dd>
                        </div>
                    </dl>
                </div>
                <!--      月销量-->
                <div style="margin-top: 20px">
                    <span>月销量：</span>
                    <span>{{ good.sales }}</span
                    ><br />
                    <span style="height: 40px" v-if="showStore"
                        >库存：{{ storeCount > 0 ? storeCount : totalStore }}</span
                    >
					<br>
                </div>
                <!--      选择规格-->
                <div
                    style="margin-top: 15px; height: 50px"
                    v-if="standards.length !== 0"
                >
                    <el-radio-group
                        v-for="(standard, index) in standards"
                        v-model="checkedStandard"
                        @change="change(standard)"
                        :key="index"
                    >
                        <el-radio-button
                            class="standard"
                            :label="standard.value"
                        ></el-radio-button>
                    </el-radio-group>
                </div>
                <!--      选择数量-->
                <div style="margin-top: 20px">
                    <el-input-number
                        v-model="count"
                        controls-position="right"
                        :min="1"
                        :max="store"
                    ></el-input-number>
                </div>
                <!--      购买按钮组-->
                <div style="margin-top: 30px">
                    <el-button
                        type="success"
                        @click="goToOrder"
                        style="font-size: 22px"
                        :disabled="isOffShelf || isSoldOutAll || (checkedStandard && store === 0)"
                    >
                        <i
                            class=""
                            style="font-size: 26px"
                        ></i
                        > 购买</el-button
                    >
                    <el-button
                        type="primary"
                        @click="addToCart"
                        style="font-size: 22px"
                        :disabled="isOffShelf || isSoldOutAll || (checkedStandard && store === 0)"
                    >
                        <i
                            class=""
                            style="font-size: 26px"
                        ></i
                        > 加入购物车</el-button
                    >
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useStore } from 'vuex'
import API from '@/utils/request'

const store = useStore()
const route = useRoute()
const router = useRouter()

const baseApi = store.state.baseApi
const good = ref({})
const goodId = ref(Number)
const price = ref(-1)
const isDiscount = ref(false)
const discount = ref('')
const standards = ref([])
const checkedStandard = ref('')
const storeCount = ref(0)  // 当前选中规格的库存
const showStore = ref(false)
const count = ref(1)
const totalStore = ref(0)  // 总库存

const getPriceRange = (standardsArr) => {
  let arr = standardsArr.map((item) => {
    return item.price
  })
  //选择排序
  for (let i = 0; i < arr.length; i++) {
    let min = i
    for (let j = i + 1; j < arr.length; j++) {
      if (arr[j] < arr[min]) {
        min = j
      }
    }
    ;[arr[i], arr[min]] = [arr[min], arr[i]]
  }
  if (arr[0] === arr[arr.length - 1]) {
    return arr[0]
  } else {
    return arr[0] + '元 ~ ' + arr[arr.length - 1]
  }
}

const change = (standard) => {
  showStore.value = true
  price.value = standard.price
  storeCount.value = standard.store
}

const goToOrder = () => {
  if (isOffShelf.value) {
    ElMessage.error('商品已下架')
    return false
  }
  if (isSoldOutAll.value) {
    ElMessage.error('商品已售罄')
    return false
  }
  if (standards.value.length !== 0) {
    if (checkedStandard.value === '') {
      ElMessage.warning('请选择规格')
      return false
    }
    if (storeCount.value === 0) {
      ElMessage.error('该规格已售罄')
      return false
    }
  }
  router.push({
    name: 'preOrder',
    query: {
      good: JSON.stringify(good.value),
      realPrice: realPrice.value,
      num: count.value,
      standard: checkedStandard.value
    }
  })
}

const addToCart = () => {
  //未登录,拦截
  if (!localStorage.getItem('user')) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (isOffShelf.value) {
    ElMessage.error('商品已下架')
    return
  }
  if (isSoldOutAll.value) {
    ElMessage.error('商品已售罄')
    return
  }
  if (!checkedStandard.value) {
    ElMessage.error('请选择规格')
    return
  }
  if (storeCount.value === 0) {
    ElMessage.error('该规格已售罄')
    return
  }
  // 从服务器获取当前用户的id,保证安全
  API.get('/userid').then((res) => {
    if (res.code !== '200') {
      ElMessage.error('获取用户信息失败')
      return
    }
    let userId = res.data
    let cart = {
      userId: userId,
      goodId: goodId.value,
      standard: checkedStandard.value,
      count: count.value
    }
    API.post('/api/cart', cart).then((res) => {
      if (res.code === '200') {
        ElMessage.success('商品加入购物车成功')
      } else if (res.code === '401') {
        ElMessage.error('登录状态已失效,请重新登录')
        localStorage.removeItem('user')
        router.push('/login')
      } else {
        ElMessage.error(res.msg || '加入购物车失败')
      }
    }).catch((err) => {
      console.error('加入购物车失败:', err)
      ElMessage.error('网络错误,请稍后重试')
    })
  }).catch((err) => {
    console.error('获取用户信息失败:', err)
    ElMessage.error('获取用户信息失败')
  })
}

const isOffShelf = computed(() => {
  return good.value && good.value.status === 0
})

const isSoldOutAll = computed(() => {
  return totalStore.value === 0 && standards.value.length > 0
})

//折后价，小数点后2位
const realPrice = computed(() => {
  if (good.value.discount < 1) {
    //价格为范围，即不是数字，则返回一个范围
    if (isNaN(price.value)) {
      let down = price.value.substring(0, price.value.indexOf('元')) * good.value.discount
      let up = price.value.substring(price.value.lastIndexOf(' ')) * good.value.discount
      return down.toFixed(2) + '元 ~ ' + up.toFixed(2)
    } else {
      return (price.value * good.value.discount).toFixed(2)
    }
  }
  return price.value
})

onMounted(() => {
  // 检查登录状态
  if (!localStorage.getItem('user')) {
    ElMessage.warning('请先登录再查看商品详情')
    router.push({ path: '/login', query: { to: route.fullPath } })
    return
  }
  
  //初始化商品信息
  goodId.value = route.params.goodId
  API.get('/api/good/detail/' + goodId.value)
    .then((res) => {
      if (res.code === '200') {
        good.value = res.data
        let discountVal = good.value.discount
        if (discountVal < 1) {
          isDiscount.value = true
          discount.value = discountVal * 10 + '折'
        }
      } else {
        ElMessage.error(res.msg || '获取商品信息失败')
        router.push('/')
      }
    })
    .catch((error) => {
      ElMessage.error('获取商品信息失败')
      console.error(error)
    })
  //从服务器获取商品规格信息
  API.get('/api/good/standard/' + goodId.value)
    .then((res) => {
      if (res.code === '200') {
        let standardsArr = JSON.parse(res.data)
        standards.value = standardsArr
        totalStore.value = standardsArr.reduce((sum, item) => sum + (item.store || 0), 0)
        //默认选择第一个标准
        price.value = getPriceRange(standardsArr)
      } else {
        //没有规格
        price.value = good.value.price
        storeCount.value = good.value.store
        showStore.value = true
      }
    })
    .catch((error) => {
      console.error('获取规格信息失败', error)
    })
})
</script>

<style scoped>
.main-box {
    width: 90%;
    max-width: 1400px;
    margin: 30px auto;
    padding: 40px;
    background-color: #ffffff;
    border-radius: 16px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    overflow: hidden;
}

.image {
    height: 100%;
    width: 350px;
}

.image-container {
    width: 480px;
    height: 480px;
    overflow: hidden;
    text-align: center;
    margin-left: 60px;
    margin-top: 20px;
    display: inline-block;
    border-radius: 12px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    background: #f8f9fa;
}

.image-container img {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.detail-box {
    width: 480px;
    display: inline-block;
    margin-left: 60px;
    vertical-align: top;
    overflow: hidden;
}

.price-box {
    background: linear-gradient(135deg, #fff5f5 0%, #fff9f9 100%);
    border: 2px solid #ffe0e0;
    border-radius: 12px;
    font: 14px/1.8 "Microsoft Yahei", tahoma, arial;
    padding: 20px;
    margin-right: 0;
    margin-top: 25px;
}

.price-box div {
    line-height: 28px;
    margin-left: 0;
    margin-bottom: 8px;
}

.price-box div:last-child {
    margin-bottom: 0;
}

.price-box dl dt {
    float: left;
    font-size: 15px;
    line-height: 28px;
    color: #606266;
    font-weight: 500;
    min-width: 70px;
}

.price-box dl dd {
    font-size: 20px;
    line-height: 28px;
    font-weight: 600;
}

.button {
    width: 130px;
    height: 45px;
    background-color: #96e2e0;
    color: #710a0a;
}

.standard {
    height: 36px;
    margin-right: 12px;
    margin-bottom: 8px;
}

/* 响应式 */
@media (max-width: 1200px) {
    .main-box {
        width: 95%;
        padding: 30px;
    }
    
    .image-container {
        width: 400px;
        height: 400px;
        margin-left: 30px;
    }
    
    .detail-box {
        width: 400px;
        margin-left: 40px;
    }
}

@media (max-width: 1024px) {
    .image-container {
        display: block;
        width: 100%;
        max-width: 500px;
        height: auto;
        aspect-ratio: 1;
        margin: 0 auto 30px;
    }
    
    .detail-box {
        display: block;
        width: 100%;
        margin-left: 0;
    }
}

@media (max-width: 768px) {
    .main-box {
        padding: 20px;
    }
    
    .image-container {
        max-width: 100%;
    }
}
</style>
