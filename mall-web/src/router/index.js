import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request';
//requireAuth: 是否需要检查登录
const routes = [
    //前台
  {
    path: '/',
    name: 'front',
    redirect: "/topview",
    component: () => import('../views/front/Front.vue'),
    meta: {title:'U次元 - Cosplay服装商城', path: 'U次元 - Cosplay服装商城', requireAuth: false},
    children: [
      {path: 'person', name: 'person', meta: {title:'个人信息',requireLogin: true}, component: () => import('../views/Person.vue'),},
      {path: 'topview', name: 'topview', meta: {title:'U次元 - Cosplay服装商城'}, component: () => import('../views/front/TopView.vue'),},
      {path: 'cart', name: 'cart', meta: {title:'我的购物车',requireLogin: true}, component: () => import('../views/front/good/Cart.vue'),},
      {path: 'goodList', name: 'goodList', meta: {title:'商品界面'}, component: () => import('../views/front/good/GoodList.vue'),},
      {path: 'goodView/:goodId', name: 'goodview', meta: {title:'商品详情',requireLogin: true}, component: () => import('../views/front/good/GoodView.vue'),},
      {path: 'preOrder', name: 'preOrder', meta: {title:'确认订单',requireLogin: true}, component: () => import('../views/front/order/PreOrder.vue'),},
      {path: 'pay', name: 'pay', meta: {title:'支付',requireLogin: true}, component: () => import('../views/front/order/Pay.vue'),},
      {path: 'orderList', name: 'orderList', meta: {title:'我的订单',requireLogin: true}, component: () => import('../views/front/order/OrderList.vue'),},
      {path: 'addressManage', name: 'addressManage', meta: {title:'地址管理',requireLogin: true}, component: () => import('../views/front/AddressManage.vue'),},
      {path: 'message', name: 'message', meta: {title:'在线留言',requireLogin: true}, component: () => import('../views/front/Message.vue'),},
      {path: 'afterSale', name: 'afterSale', meta: {title:'售后申请',requireLogin: true}, component: () => import('../views/front/AfterSale.vue'),},

    ]
  },
    //后台
  {
    path: '/manage',
    name: 'manage',
    component: () => import('../views/manage/Manage.vue'),
    redirect: "/manage/home",
    meta: {title:'后台', path: '后台',requireAuth: true},
    children: [
      {path: 'home', name: 'home', meta: {title:'主页', path: '主页',requireAuth: true}, component: () => import('../views/manage/Home.vue'),},
      {path: 'user', name: 'user', meta: {title:'用户管理',path: '系统管理/用户管理',requireAuth: true}, component: () => import('../views/manage/User.vue'),},
      {path: 'person', name: 'managePerson', meta: {title:'个人信息',path: '个人信息',requireAuth: true}, component: () => import('../views/Person.vue'),},
      {path: 'file', name: 'file', meta: {title:'文件管理',path: '文件/文件管理',requireAuth: true}, component: () => import('../views/manage/file/File.vue'),},
      {path: 'avatar', name: 'avatar', meta: {title:'头像管理',path: '文件/头像管理',requireAuth: true}, component: () => import('../views/manage/file/Avatar.vue'),},
      {path: 'carousel', name: 'carousel', meta: {title:'轮播图管理',path: '商品/轮播图管理',requireAuth: true}, component: () => import('../views/manage/good/Carousel.vue'),},
      {path: 'category', name: 'category', meta: {title:'商品分类管理',path: '商品/商品分类管理',requireAuth: true}, component: () => import('../views/manage/good/Category.vue'),},
      {path: 'good', name: 'good', meta: {title:'商品管理',path: '商品/商品管理',requireAuth: true}, component: () => import('../views/manage/good/Goods.vue'),},
      {path: 'goodInfo', name: 'goodInfo', meta: {title:'商品管理',path: '商品/商品管理/商品信息',requireAuth: true}, component: () => import('../views/manage/good/GoodInfo.vue'),},
      {path: 'order', name: 'order', meta: {title:'订单管理',path: '商品/订单管理',requireAuth: true}, component: () => import('../views/manage/Order.vue'),},
      {path: 'message', name: 'manageMessage', meta: {title:'留言管理',path: '互动/留言管理',requireAuth: true}, component: () => import('../views/manage/Message.vue'),},
      {path: 'notice', name: 'manageNotice', meta: {title:'公告管理',path: '系统/公告管理',requireAuth: true}, component: () => import('../views/manage/Notice.vue'),},
      {path: 'afterSale', name: 'manageAfterSale', meta: {title:'售后管理',path: '订单/售后管理',requireAuth: true}, component: () => import('../views/manage/AfterSale.vue'),},
      {path: 'incomeChart', name: 'incomeChart', meta: {title:'收入图表',path: '营收/收入图表',requireAuth: true}, component: () => import('../views/manage/income/IncomeChart.vue'),},
      {path: 'incomeRank', name: 'incomeRank', meta: {title:'收入排行',path: '营收/收入排行',requireAuth: true}, component: () => import('../views/manage/income/IncomeRank.vue'),},

    ]
  },
  {
    path: '/login',
    name: 'login',
    meta: {
      title: '登录',
      requireAuth: false,
    },
    component: () => import(/* webpackChunkName: "about" */ '../views/Login.vue')
  },
  {
    path: '/register',
    name: 'register',
    meta: {
      title: '注册',requireAuth: false,
    },
    component: () => import(/* webpackChunkName: "about" */ '../views/Register.vue')
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'notFound',
    meta: {
      title: '找不到页面'
    },
    component: () => import(/* webpackChunkName: "about" */ '../views/404NotFound.vue')
  },
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

//beforeEach是router的钩子函数，在进入路由前执行
let roleCache = null // 角色缓存
let roleCacheTime = 0 // 角色缓存时间
const ROLE_CACHE_DURATION = 5 * 60 * 1000 // 5分钟缓存

router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title
  } else {
    document.title = '未知页面'
  }
  
  if (to.meta.requireAuth === true) {
    // 先检查本地存储是否有token
    const userStr = localStorage.getItem("user")
    if (!userStr) {
      // 没有登录，直接跳转
      ElMessage.warning('请先登录')
      next('/login')
      return
    }
    
    try {
      const userData = JSON.parse(userStr)
      
      // 检查缓存是否有效（页面刷新时也使用缓存，避免频繁请求）
      const now = Date.now()
      let role
      if (roleCache && (now - roleCacheTime) < ROLE_CACHE_DURATION) {
        // 使用内存缓存的角色
        role = roleCache
      } else if (userData.role) {
        // 缓存失效，优先使用localStorage中的role（页面刷新时）
        role = userData.role
        // 异步更新后端验证（不阻塞页面加载）
        request.post("/role", {}, { skipAuthError: true })
          .then(res => {
            if (res.code === '200') {
              roleCache = res.data
              roleCacheTime = Date.now()
              // 更新localStorage中的role
              userData.role = res.data
              localStorage.setItem('user', JSON.stringify(userData))
            }
          })
          .catch(err => {
            console.warn('角色验证请求失败:', err)
          })
      } else {
        // localStorage中也没有role，必须从后端获取
        const res = await request.post("/role", {}, { skipAuthError: true })
        if (res.code === '200') {
          role = res.data
          // 更新缓存
          roleCache = role
          roleCacheTime = now
          // 保存到localStorage
          userData.role = role
          localStorage.setItem('user', JSON.stringify(userData))
        } else if (res.code === '401') {
          // token失效，静默清除并跳转登录
          localStorage.removeItem("user")
          roleCache = null
          roleCacheTime = 0
          next('/login')
          return
        } else {
          // 其他错误
          localStorage.removeItem("user")
          roleCache = null
          roleCacheTime = 0
          ElMessage.error(res.msg || '登录状态已失效，请重新登录')
          next('/login')
          return
        }
      }
      
      if (role === 'admin') {
        // 管理员放行
        next()
      } else if (role === 'user') {
        ElMessage.error('您没有权限访问该页面')
        next("/")
      } else {
        ElMessage.error('未知角色')
        next("/")
      }
    } catch (err) {
      // 请求失败（网络错误等），尝试使用本地数据
      console.warn('角色验证异常，使用本地数据:', err)
      const userData = JSON.parse(localStorage.getItem("user") || '{}')
      const role = userData.role || 'user'
      
      if (role === 'admin') {
        next()
      } else {
        // 普通用户不能访问后台，但也不能无限跳转，放行后由页面处理
        next()
      }
    }
  } else {
    //不需要判断权限
    if (to.meta.requireLogin === true) {
      const userStr = localStorage.getItem("user")
      if (!userStr) {
        ElMessage.warning('请先登录')
        next('/login')
        return
      }
      // 进一步检查 token 字段是否存在（防止token已被清除但对象殳中）
      try {
        const user = JSON.parse(userStr)
        if (!user || !user.token) {
          localStorage.removeItem('user')
          ElMessage.warning('登录已过期，请重新登录')
          next('/login')
          return
        }
      } catch (e) {
        localStorage.removeItem('user')
        next('/login')
        return
      }
    }
    next()
  }
})

function isLogin() {
  let user = localStorage.getItem("user");
  if(user){
    return true;
  }else{
    return false;
  }
}
export default router
