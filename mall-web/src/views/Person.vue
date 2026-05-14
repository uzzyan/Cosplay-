<template>
  <el-card class="card">
    <div style="text-align: center; margin-bottom: 30px">
      <b>修改个人信息</b>
    </div>

    <el-form label-width="60px">
      <el-form-item label="头像">
        <el-upload
          class="avatar-uploader"
          :action="baseApi + '/avatar'"
          :headers="token"
          :show-file-list="false"
          :on-success="handleAvatarSuccess"
        >
          <img
            v-if="form.avatarUrl"
            :src="baseApi + form.avatarUrl"
            class="avatar"
          />
          <i v-else class="el-icon-plus avatar-uploader-icon"></i>
        </el-upload>
      </el-form-item>

      <el-form-item label="昵称">
        <el-input v-model="form.nickname" autocomplete="off"></el-input>
      </el-form-item>
      <el-form-item label="电话">
        <el-input v-model="form.phone" autocomplete="off"></el-input>
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="form.email" autocomplete="off"></el-input>
      </el-form-item>
      <el-form-item label="地址">
        <el-input v-model="form.address" autocomplete="off"></el-input>
      </el-form-item>
      <el-button
        type="primary"
        style="font-size: 20px;margin-left: 190px; margin-top: 20px"
        @click="save"><i class="iconfont icon-r-yes" style="font-size: 22px;"></i> 确定</el-button
      >
    </el-form>
    <el-popover placement="right" width="300" trigger="click">
      <el-form label-width="80px">
        <el-form-item label="新密码">
          <el-input
            type="password"
            v-model="resetPsw.newPassword"
            autocomplete="off"
            placeholder="6-12位，包含字母、数字和特殊符号"
            show-password
          ></el-input>
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input
            type="password"
            v-model="resetPsw.confirmPassword"
            autocomplete="off"
            placeholder="请再次输入新密码"
            show-password
          ></el-input>
        </el-form-item>
        <div style="text-align: center; margin-top: 10px;">
          <el-button type="primary" @click="toResetPassword" style="font-size: 16px;">
            <i class="iconfont icon-r-yes" style="font-size: 18px;"></i> 确定修改
          </el-button>
        </div>
      </el-form>
      <template #reference>
        <el-button
          type="warning"
          style="margin-left: 190px; margin-top: 20px; font-size: 20px;"
          @click="resetPsw = { newPassword: '', confirmPassword: '' }"
        >
          <i class="iconfont icon-r-lock" style="font-size: 22px;"></i> 重置密码
        </el-button>
      </template>
    </el-popover>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useStore } from 'vuex'
import request from '@/utils/request'

const store = useStore()
const router = useRouter()

const baseApi = store.state.baseApi
const user = ref(
  localStorage.getItem('user')
    ? JSON.parse(localStorage.getItem('user'))
    : {}
)

const form = ref({})
const resetPsw = ref({
  newPassword: '',
  confirmPassword: ''
})

const passwordRule = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{}|;:,.<>?])[A-Za-z\d!@#$%^&*()_+\-=\[\]{}|;:,.<>?]{6,12}$/

const token = computed(() => {
  return { token: user.value.token }
})

const toResetPassword = () => {
  // 验证新密码
  if (!resetPsw.value.newPassword || resetPsw.value.newPassword.trim() === '') {
    ElMessage.error('新密码不能为空')
    return
  }
  
  // 密码强度验证
  if (!passwordRule.test(resetPsw.value.newPassword)) {
    ElMessage.error('密码必须为6-12位，且同时包含字母、数字和特殊符号')
    return
  }
  
  // 验证确认密码
  if (!resetPsw.value.confirmPassword || resetPsw.value.confirmPassword.trim() === '') {
    ElMessage.error('确认密码不能为空')
    return
  }
  
  // 两次密码一致性验证
  if (resetPsw.value.confirmPassword !== resetPsw.value.newPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  
  // 发送修改密码请求
  request
    .post(
      '/user/resetPassword',
      null,
      { params: { id: user.value.id, newPassword: resetPsw.value.newPassword } }
    )
    .then((res) => {
      if (res.code === '200') {
        ElMessage.success('密码修改成功，请重新登录')
        resetPsw.value = {
          newPassword: '',
          confirmPassword: ''
        }
        // 清除登录状态，跳转到登录页
        localStorage.removeItem('user')
        setTimeout(() => {
          router.push('/login')
        }, 1500)
      } else if (res.code === '401') {
        ElMessage.error('登录状态已失效，请重新登录')
        localStorage.removeItem('user')
        router.push('/login')
      } else {
        ElMessage.error(res.msg || '密码修改失败')
      }
    })
    .catch((err) => {
      console.error('密码修改失败:', err)
      ElMessage.error('网络错误，请稍后重试')
    })
}

//图片上传成功钩子
const handleAvatarSuccess = (res) => {
  form.value.avatarUrl = res.data
}

//提交事件
const save = () => {
  //把表格传给后台，保存到数据库
  request.post('/user', form.value).then((res) => {
    if (res.code === '200') {
      ElMessage.success('保存成功')
      //把表格的数据更新到user中
      if (form.value && typeof form.value === 'object') {
        Object.keys(form.value).forEach(key => {
          if (form.value[key] !== undefined) {
            user.value[key] = form.value[key]
          }
        })
      }
      //更新localstorage的user
      localStorage.setItem('user', JSON.stringify(user.value))
      router.go(0)
    } else {
      ElMessage.error(res.msg)
    }
  })
}

onMounted(() => {
  request.get('/userinfo/' + user.value.username).then((res) => {
    if (res.code === '200') {
      form.value = res.data
    } else {
      alert(res.msg)
    }
  })
})
</script>

<style scoped>
.card {
  width: 500px;
  margin: 80px auto;
  padding: 30px;
}
.avatar-uploader {
  padding-bottom: 10px;
}
.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}
.avatar-uploader .el-upload:hover {
  border-color: #409eff;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 138px;
  height: 138px;
  line-height: 138px;
  text-align: center;
  border: 3px dashed #409eff;  /* 蓝色虚线边框 */
  border-radius: 6px;  /* 圆角 */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);  /* 轻微阴影 */
}
.avatar {
  width: 138px;
  height: 138px;
  display: block;
  border: 3px solid #409eff;  /* 蓝色边框 */
  border-radius: 6px;  /* 圆角 */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);  /* 阴影效果 */
}
</style>