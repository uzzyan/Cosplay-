import axios from 'axios'
import { ElMessageBox, ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
    baseURL: '',  // 通过 vue.config.js 的 devServer.proxy 代理到后端
    timeout: 50000
})

// request 拦截器
// 可以自请求发送前对请求做一些处理
// 比如统一加token，对请求参数统一加密
request.interceptors.request.use(config => {
    config.headers['Content-Type'] = 'application/json;charset=utf-8';
    let user = JSON.parse(localStorage.getItem("user"))
    if(user){
         config.headers['token'] = user.token;  // 设置token
    }
    // 传递自定义配置到response拦截器
    return config
}, error => {
    return Promise.reject(error)
});

// response 拦截器
// 可以在接口响应后统一处理结果
request.interceptors.response.use(
    response => {
        let res = response.data;
        // 如果是返回的文件
        if (response.config.responseType === 'blob') {
            return res
        }
        // 兼容服务端返回的字符串数据
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res
        }
        // 发生错误，如token失效，则返回登录
        if(res.code==='401'){
            localStorage.removeItem("user");
            if (!response.config.skipAuthError) {
                ElMessage.warning(res.msg || '登录状态已失效，请重新登录');
                // 立即跳转登录页（不在登录页时才跳）
                if (router.currentRoute.value.path !== '/login') {
                    router.push('/login')
                }
            }
        }
        return res;
    },
    error => {
        // 统一错误处理
        if (error.response) {
            // 服务器返回错误状态码
            const { status } = error.response
            switch (status) {
                case 401:
                    ElMessage.error('未授权，请登录')
                    localStorage.removeItem("user")
                    router.push('/login')
                    break
                case 403:
                    ElMessage.error('拒绝访问')
                    break
                case 404:
                    ElMessage.error('请求错误，未找到该资源')
                    break
                case 500:
                    ElMessage.error('服务器错误')
                    break
                default:
                    ElMessage.error(`连接错误 ${status}`)
            }
        } else if (error.code === 'ECONNABORTED') {
            ElMessage.error('请求超时')
        } else if (error.message === 'Network Error') {
            ElMessage.error('网络错误，请检查网络连接')
        } else {
            ElMessage.error('请求失败，请重试')
        }
        return Promise.reject(error)
    }
)


export default request

