<template>
  <div class="goodlist-container">
    <div class="content-wrapper">
      <!-- 分类筛选区域 -->
      <div class="category-section">
        <div class="section-header">
          <div class="header-left">
            <i class="el-icon-menu header-icon"></i>
            <h2 class="section-title">商品分类</h2>
          </div>
          <el-button 
            type="primary" 
            link
            @click="loadAll()"
            class="all-btn"
            :class="{ active: !categoryId && !searchText }"
          >
            <i class="el-icon-s-grid"></i> 查看全部
          </el-button>
        </div>

        <!-- 分类标签 -->
        <div class="category-tags">
          <div 
            v-for="(item, index) in icons" 
            :key="index"
            class="category-group"
          >
            <i class="iconfont category-icon" v-html="item.value"></i>
            <div class="category-items">
              <el-tag
                v-for="(category, index2) in item.categories"
                :key="index2"
                :type="categoryId == category.id ? 'primary' : 'info'"
                :effect="categoryId == category.id ? 'dark' : 'plain'"
                @click="load(category.id)"
                class="category-tag"
              >
                {{ category.name }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>

      <!-- 商品列表区域 -->
      <div class="goods-section">
        <div class="goods-header" v-if="searchText">
          <i class="el-icon-search"></i>
          搜索结果：<span class="search-keyword">{{ searchText }}</span>
          <span class="total-count">（共 {{ total }} 件商品）</span>
        </div>

        <div class="goods-grid" v-if="good.length > 0">
          <div
            v-for="good in good"
            :key="good.id"
            class="good-item"
          >
            <div class="good-card">
              <router-link :to="'/goodview/' + good.id" class="good-link">
                <div class="image-wrapper">
                  <div class="soldout-badge" v-if="good.soldOut">售罄</div>
                  <img
                    :src="baseApi + good.imgs"
                    class="good-image"
                    alt="商品图片"
                  />
                </div>
                <div class="good-info">
                  <h3 class="good-name">{{ good.name }}</h3>
                  <div class="price-box">
                    <span class="price-symbol">¥</span>
                    <span class="price-value">{{ good.price }}</span>
                  </div>
                </div>
              </router-link>
              <div class="cart-action">
                <el-button
                  type="primary"
                  size="small"
                  @click.stop="addToCart(good)"
                  :disabled="good.soldOut"
                >
                  <i class="el-icon-shopping-cart-2"></i> 加入购物车
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-state">
          <i class="el-icon-shopping-bag-2 empty-icon"></i>
          <p class="empty-text">暂无商品</p>
        </div>

        <!-- 分页 -->
        <div class="pagination-wrapper" v-if="total > 0">
          <el-pagination
            background
            :hide-on-single-page="false"
            :current-page="currentPage"
            :page-size="pageSize"
            layout="total, prev, pager, next, jumper"
            :total="total"
            @current-change="handleCurrentPage"
          >
          </el-pagination>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const store = useStore()
const route = useRoute()
const router = useRouter()

const icons = ref([])
const total = ref(0)
const pageSize = ref(8)
const currentPage = ref(1)
const categoryId = ref(null) // 修复：使用null而不是Number
const searchText = ref('')
const good = ref([])
const baseApi = store.state.baseApi

const loadCategories = () => {
  request.get('/api/icon')
    .then((res) => {
      if (res.code === '200') {
        icons.value = res.data
      } else {
        ElMessage.error(res.msg || '加载分类失败')
      }
    })
    .catch((err) => {
      console.error('加载分类失败:', err)
      ElMessage.error('加载分类失败，请重试')
    })
}

const loadAll = () => {
  // 清除所有筛选条件，查看全部商品
  searchText.value = ''
  categoryId.value = null
  currentPage.value = 1
  router.push({
    path: '/goodList'
  })
  load()
}

const handleCurrentPage = (currentPageVal) => {
  currentPage.value = currentPageVal
  load()
}

const load = (categoryIdVal) => {
  if (categoryIdVal != undefined) {
    categoryId.value = categoryIdVal
    // 选择分类时清空搜索，使搜索和分类独立
    searchText.value = ''
    currentPage.value = 1 // 重置到第一页
    router.push({
      path: '/goodlist',
      query: { categoryId: categoryId.value }
    })
  }
  request
    .get('/api/good/page', {
      params: {
        pageNum: currentPage.value,
        pageSize: pageSize.value,
        searchText: searchText.value,
        categoryId: categoryId.value
      }
    })
    .then((res) => {
      if (res.code === '200') {
        total.value = res.data.total
        good.value = res.data.records
      } else {
        ElMessage.error(res.msg || '加载商品失败')
      }
    })
    .catch((err) => {
      console.error('加载商品失败:', err)
      ElMessage.error('加载商品失败，请重试')
    })
}

// 加入购物车
const addToCart = (goodItem) => {
  // 未登录，拦截
  if (!localStorage.getItem('user')) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { to: '/goodList' } })
    return
  }
  
  // 检查商品是否售罄
  if (goodItem.soldOut) {
    ElMessage.error('商品已售罄')
    return
  }
  
  // 跳转到商品详情页选择规格
  ElMessage.info('请选择商品规格')
  router.push({
    path: '/goodview/' + goodItem.id,
    query: { from: 'goodList' }  // 添加来源标记
  })
}

// 监听路由变化，当从导航栏搜索时更新数据
watch(
  () => route.query,
  (to, from) => {
    if (to.searchText !== from.searchText || to.categoryId !== from.categoryId) {
      searchText.value = to.searchText
      categoryId.value = to.categoryId
      currentPage.value = 1 // 重置到第一页
      load()
    }
  }
)

onMounted(() => {
  //二者一般不同时存在
  searchText.value = route.query.searchText
  categoryId.value = route.query.categoryId

  loadCategories()
  load()
})
</script>

<style scoped>
.goodlist-container {
  padding: 30px 0;
}

.content-wrapper {
  width: 90%;
  max-width: 1400px;
  margin: 0 auto;
  background-color: white;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.soldout-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 2;
  background: rgba(245, 108, 108, 0.9);
  color: #fff;
  padding: 4px 8px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
}

/* 分类筛选区域 */
.category-section {
  margin-bottom: 30px;
  padding-bottom: 30px;
  border-bottom: 2px solid #f0f0f0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  font-size: 28px;
  color: #409EFF;
}

.section-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.all-btn {
  font-size: 15px;
  color: #909399;
  font-weight: 500;
}

.all-btn.active {
  color: #409EFF;
}

.all-btn:hover {
  color: #409EFF;
}

/* 分类标签 */
.category-tags {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.category-group {
  display: flex;
  align-items: center;
  gap: 15px;
}

.category-icon {
  font-size: 24px;
  color: #606266;
  flex-shrink: 0;
}

.category-items {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.category-tag {
  cursor: pointer;
  font-size: 14px;
  padding: 8px 20px;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  line-height: 1.5;
}

.category-tag:hover {
  transform: translateY(-2px);
}

/* 商品列表区域 */
.goods-section {
  margin-top: 30px;
}

.goods-header {
  font-size: 16px;
  color: #606266;
  margin-bottom: 25px;
  padding: 15px 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.search-keyword {
  color: #409EFF;
  font-weight: 600;
  font-size: 18px;
}

.total-count {
  color: #909399;
  margin-left: 10px;
}

/* 商品网格 */
.goods-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 25px;
  margin-bottom: 30px;
}

.good-item {
  position: relative;
}

.good-item a {
  text-decoration: none;
  color: inherit;
}

.good-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
  position: relative;
}

.good-link {
  text-decoration: none;
  color: inherit;
  display: block;
}

.good-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.image-wrapper {
  position: relative;
  width: 100%;
  height: 280px;
  overflow: hidden;
  background: #f8f9fa;
}

.good-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.good-card:hover .good-image {
  transform: scale(1.05);
}

.good-info {
  padding: 16px;
}

.good-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
  line-height: 1.5;
  height: 48px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  text-overflow: ellipsis;
}

.price-box {
  display: flex;
  align-items: baseline;
  color: #ff4d4f;
}

.price-symbol {
  font-size: 16px;
  font-weight: 600;
  margin-right: 2px;
}

.price-value {
  font-size: 24px;
  font-weight: 700;
}

/* 购物车按钮 */
.cart-action {
  padding: 0 16px 16px;
  display: flex;
  justify-content: center;
}

.cart-action .el-button {
  width: 100%;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.cart-action .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
}

.empty-icon {
  font-size: 100px;
  color: #dcdfe6;
  margin-bottom: 20px;
}

.empty-text {
  font-size: 18px;
  color: #909399;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  padding-top: 30px;
  border-top: 1px solid #f0f0f0;
}

/* 响应式 */
@media (max-width: 1400px) {
  .goods-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 1024px) {
  .goods-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .goods-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
  }
  
  .content-wrapper {
    width: 95%;
    padding: 20px;
  }
  
  .category-group {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 480px) {
  .goods-grid {
    grid-template-columns: 1fr;
  }
}
</style>
