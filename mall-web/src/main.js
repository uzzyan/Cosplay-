import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import request from './utils/request'
import '@/resource/global.css'
import './resource/icon_store/iconfont.css'
import './resource/font3/iconfont.css'

const app = createApp(App)
app.use(ElementPlus)
app.use(router)
app.use(store)
app.config.globalProperties.request = request
app.mount('#app')
