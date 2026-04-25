<!--
 * @Description:
 * @Author: gzc
 * @Date: 2024-03-21 15:27:05
-->
<template>
    <div id="bk" class="wrapper">
        <div class="login-box">
            <div class="title">
                <b style="font-size: 28px"> 登录U次元 </b>
            </div>
            <div style="margin-top: 30px">
                <el-form label-width="70px">
                    <el-form-item label="用户名">
                        <el-input
                            v-model.trim="user.username"
                            aria-required="true"
                        ></el-input>
                    </el-form-item>
                    <el-form-item label="密码" style="margin-top: 25px">
                        <el-input
                            v-model.trim="user.password"
                            show-password
                            aria-required="true"
                            placeholder="6-12位，含字母、数字和特殊符号"
                        ></el-input>
                    </el-form-item>
                    <el-form-item style="text-align: center">
                        <el-button
                            type="success"
                            @click="onSubmit"
                            style="font-size: 22px"
                        > 登录</el-button
                        >
                        <el-button
                            @click="$router.push('/register')"
                            style="font-size: 22px"
                        > 注册</el-button
                        >
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import md5 from 'js-md5'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()

const to = ref('/') // 登陆成功跳转的页面
const user = ref({
  username: '',
  password: ''
})

onMounted(() => {
  to.value = route.query.to ? route.query.to : '/'
})

const onSubmit = () => {
  if (user.value.username === '' || user.value.password === '') {
    ElMessage.error('账号或密码不能为空')
    return
  }
  const passwordRule = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{}|;:,.<>?])[A-Za-z\d!@#$%^&*()_+\-=\[\]{}|;:,.<>?]{6,12}$/;
  if (!passwordRule.test(user.value.password)) {
    ElMessage.error('密码必须为6-12位，且同时包含字母、数字和特殊符号')
    return
  }
  let form = {}
  Object.assign(form, user.value)
  form.password = md5(user.value.password)
  
  request
    .post('/login', form)
    .then((res) => {
      if (res.code === '200') {
        localStorage.setItem('user', JSON.stringify(res.data))
        ElMessage.success('登陆成功')
        // 根据用户角色决定跳转页面
        let redirectPath = to.value
        if (!to.value || to.value === '/') {
          // 如果没有指定跳转页面，根据角色决定
          if (res.data.role === 'admin') {
            redirectPath = '/manage'
          } else {
            redirectPath = '/'
          }
        }
        router.push(redirectPath) // 使用路由跳转，避免页面刷新
      } else {
        ElMessage.error(res.msg)
      }
    })
    .catch((e) => {
      if (
        e.response == undefined ||
        e.response.data == undefined
      ) {
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
    })
}
</script>

<style scoped>
.wrapper {
    height: 100vh;
    background-color: rgb(161, 202, 220);
    overflow: auto;
}
.login-box {
    margin: 220px auto;
    padding: 40px;
    width: 450px;
    height: 280px;
    background-color: #ffffff;
    border-radius: 10px;
}
.title {
    text-align: center;
    margin: 30px auto;
    font-size: 25px;
}

#bk {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    overflow-y: auto;
    height: 100%;
    background: url("../resource/01.png") center top / cover no-repeat;
}
</style>
