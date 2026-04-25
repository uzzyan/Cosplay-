<!--
 * @Description: 
 * @Author: gzc
 * @Date: 2024-03-21 15:27:05
-->
<template>
    <div id="bk" class="wrapper">
        <div class="login-box">
            <div class="title">
                <b>注 册</b>
            </div>
            <div style="margin-top: 30px">
                <el-form label-width="70px">
                    <el-form-item label="用户名">
                        <el-input
                            v-model.trim="user.username"
                            aria-required="true"
                        ></el-input>
                    </el-form-item>
                    <el-form-item label="联系方式" style="margin-top: 25px">
                        <el-input
                            v-model.trim="user.phone"
                            aria-required="true"
                        ></el-input>
                    </el-form-item>
                    <el-form-item label="密码" style="margin-top: 25px">
                        <el-input
                            v-model.trim="user.password"
                            show-password
                            aria-required="true"
                        ></el-input>
                    </el-form-item>
                    <el-form-item label="确认密码" style="margin-top: 25px">
                        <el-input
                            v-model.trim="user.confirmPassword"
                            show-password
                            aria-required="true"
                        ></el-input>
                    </el-form-item>
                    <el-form-item style="text-align: center;">
                        <el-button
                            type="success"
                            @click="onSubmit"
                            style="font-size: 22px"
                        >
                             注册</el-button
                        >
                        <el-button
                            @click="router.push('/login')"
                            style="font-size: 22px"
                        >
                             返回登录</el-button
                        >
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import md5 from 'js-md5'
import request from '@/utils/request'

const router = useRouter()

const user = ref({
  username: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

const passwordRule = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{}|;:,.<>?])[A-Za-z\d!@#$%^&*()_+\-=\[\]{}|;:,.<>?]{6,12}$/

const onSubmit = () => {
  const username = (user.value.username || '').trim()
  const password = (user.value.password || '').trim()
  const confirmPassword = (user.value.confirmPassword || '').trim()
  const phone = (user.value.phone || '').trim()

  if (!username || !password || !confirmPassword) {
    ElMessage.error('用户名或密码不能为空')
    return
  }
  if (!passwordRule.test(password)) {
    ElMessage.error('密码必须为6-12位，且同时包含字母、数字和特殊符号')
    return
  }
  if (!phone) {
    ElMessage.error('联系方式不能为空')
    return
  }
  if (!/^[0-9\-+() ]{7,20}$/.test(phone)) {
    ElMessage.error('联系方式格式不正确')
    return
  }
  if (user.value.password !== user.value.confirmPassword) {
    ElMessage.error('两次密码不一致')
    return
  }
  
  let form = {}
  Object.assign(form, user.value)
  form.password = md5(user.value.password)
  
  request.post('/register', form).then((res) => {
    if (res.code === '200') {
      ElMessage.success('注册成功')
      router.push('/login')
    } else {
      ElMessage.error(res.msg)
    }
  })
}
</script>

<style scoped>
.wrapper {
    height: 100vh;
    /* background-image: linear-gradient(to top right,#42b983,#B3C0D1); */
    background-color: #006400;
    overflow: auto;
}
.login-box {
    margin: 220px auto;
    padding: 40px;
    width: 450px;
    height: 310px;
    background-color: #ffffff;
    border-radius: 10px;
}
.title {
    text-align: center;
    margin-top: 10px;
    font-size: 25px;
    color: #006400;
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
