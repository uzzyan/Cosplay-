
<template>
    <div class="top-view-container">
        <div class="content-wrapper">
            <!-- 轮播图区域 - 占满一行 -->
            <div class="carousel-section">
                <el-carousel
                    height="450px"
                    :interval="4000"
                    arrow="always"
                >
                    <el-carousel-item
                        v-for="carousel in carousels"
                        :key="carousel.id"
                    >
                        <router-link :to="'/goodview/' + carousel.goodId">
                            <img
                                class="carousel-image"
                                :src="baseApi + carousel.img"
                                alt="轮播图"
                            />
                        </router-link>
                    </el-carousel-item>
                </el-carousel>
            </div>

            <!-- 推荐商品区域 -->
            <div class="recommend-section">
                <div class="section-header">
                    <div class="header-left">
                        <i class="el-icon-star-on header-icon"></i>
                        <h2 class="section-title">精选推荐</h2>
                    </div>
                    <el-button 
                        type="primary" 
                        link
                        @click="router.push('/goodList')"
                        class="more-btn"
                    >
                        查看更多 <i class="el-icon-arrow-right"></i>
                    </el-button>
                </div>

                <div class="goods-grid">
                    <div
                        v-for="good in good"
                        :key="good.id"
                        class="good-item"
                    >
                        <router-link :to="'goodview/' + good.id">
                            <div class="good-card">
                                <div class="image-wrapper">
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
                            </div>
                        </router-link>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useStore } from 'vuex'
import request from '@/utils/request'

const store = useStore()
const router = useRouter()

const carousels = ref([])
const good = ref([])
const icons = ref([])
const baseApi = store.state.baseApi

const handleError = (e) => {
  if (e.response == undefined || e.response.data == undefined) {
    ElMessage({
      message: e,
      type: 'error',
      duration: 5000,
    })
  } else {
    ElMessage({
      message: e.response.data,
      type: 'error',
      duration: 5000,
    })
  }
}

onMounted(() => {
  request
    .get('/api/good')
    .then((res) => {
      if (res.code === '200') {
        good.value = res.data
      } else {
        ElMessage.error(res.msg)
      }
    })
    .catch(handleError)
    
  request
    .get('/api/icon')
    .then((res) => {
      if (res.code === '200') {
        icons.value = res.data
        if (icons.value.length > 6) {
          icons.value = icons.value.slice(0, 6)
        }
      }
    })
    .catch(handleError)
    
  request
    .get('/api/carousel')
    .then((res) => {
      if (res.code === '200') {
        carousels.value = res.data
      }
    })
    .catch(handleError)
})
</script>

<style scoped>
.top-view-container {
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

/* 轮播图区域 */
.carousel-section {
    margin-bottom: 40px;
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.carousel-image {
    width: 100%;
    height: 450px;
    object-fit: cover;
    display: block;
}

/* 推荐商品区域 */
.recommend-section {
    margin-top: 40px;
}

.section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30px;
    padding-bottom: 20px;
    border-bottom: 2px solid #f0f0f0;
}

.header-left {
    display: flex;
    align-items: center;
    gap: 12px;
}

.header-icon {
    font-size: 32px;
    color: #ff6b6b;
}

.section-title {
    margin: 0;
    font-size: 28px;
    font-weight: 600;
    color: #303133;
}

.more-btn {
    font-size: 16px;
    color: #409EFF;
    font-weight: 500;
}

.more-btn:hover {
    color: #66b1ff;
}

/* 商品网格 */
.goods-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 25px;
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

/* 响应式 */
@media (max-width: 1200px) {
    .goods-grid {
        grid-template-columns: repeat(3, 1fr);
    }
}

@media (max-width: 768px) {
    .goods-grid {
        grid-template-columns: repeat(2, 1fr);
        gap: 15px;
    }
    
    .carousel-image {
        height: 300px;
    }
    
    .content-wrapper {
        width: 95%;
        padding: 20px;
    }
}

@media (max-width: 480px) {
    .goods-grid {
        grid-template-columns: 1fr;
    }
}
</style>
