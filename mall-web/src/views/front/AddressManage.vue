<template>
  <div class="address-manage-container">
    <div class="content-wrapper">
      <!-- 页面标题栏 -->
      <div class="page-header">
        <div class="header-left">
          <i class="el-icon-location header-icon"></i>
          <h2 class="page-title">我的收货地址</h2>
          <span class="address-count">共 {{ addressData.length }} 个地址</span>
        </div>
        <el-button 
          type="primary" 
          size="medium"
          @click="addAddress"
          class="add-btn"
        >
          <el-icon><Plus /></el-icon>
          新增地址
        </el-button>
      </div>
      
      <!-- 地址列表 -->
      <div v-if="addressData.length > 0">
        <span v-for="(item, index) in addressData" :key="index">
          <address-box
            :address="item"
            :selected="false"
            style="margin-right: 20px; margin-bottom: 20px;"
            @edit="editAddress(item)"
            @delete="deleteAddress(item)"
          ></address-box>
        </span>
      </div>
      
      <!-- 无地址提示 -->
      <div v-else class="empty-state">
        <div class="empty-icon">
          <i class="el-icon-map-location"></i>
        </div>
        <p class="empty-title">暂无收货地址</p>
        <p class="empty-desc">添加收货地址，让购物更便捷</p>
        <el-button 
          type="primary" 
          size="medium"
          @click="addAddress"
          class="empty-add-btn"
        >
          <el-icon><Plus /></el-icon>
          添加收货地址
        </el-button>
      </div>

      <!-- 地址弹窗 -->
      <el-dialog 
        :title="dialogTitle" 
        v-model="dialogFormVisible" 
        width="540px"
        :close-on-click-modal="false"
        class="address-dialog"
      >
        <el-form 
          :model="address" 
          label-width="100px" 
          label-position="right"
          class="address-form"
        >
          <el-form-item label="收货人" required>
            <el-input 
              v-model="address.linkUser" 
              autocomplete="off"
              placeholder="请输入收货人姓名"
              prefix-icon="el-icon-user"
            ></el-input>
          </el-form-item>
          <el-form-item label="联系电话" required>
            <el-input 
              v-model="address.linkPhone" 
              autocomplete="off"
              placeholder="请输入11位联系电话"
              prefix-icon="el-icon-phone"
              maxlength="11"
              show-word-limit
              @input="address.linkPhone = address.linkPhone.replace(/[^\d]/g, '')"
            ></el-input>
          </el-form-item>
          <el-form-item label="收货地址" required>
            <el-input
              type="textarea"
              :rows="4"
              v-model="address.linkAddress"
              autocomplete="off"
              placeholder="请输入详细地址，例如：XX省XX市XX区XX街道XX小区XX号楼XX单元XX室"
            ></el-input>
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="dialogFormVisible = false" size="medium">
              <el-icon><Close /></el-icon> 取消
            </el-button>
            <el-button type="primary" @click="saveAddress" size="medium">
              <el-icon><Check /></el-icon> 确定
            </el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Close, Check } from '@element-plus/icons-vue'
import AddressBox from '@/components/AddressBox.vue'
import API from '@/utils/request'

const userId = ref(0)
const addressData = ref([])
const address = ref({})
const dialogFormVisible = ref(false)
const dialogTitle = ref('新增地址')

const addAddress = () => {
  address.value = {}
  dialogTitle.value = '新增地址'
  dialogFormVisible.value = true
}

const editAddress = (item) => {
  // 深拷贝
  address.value = JSON.parse(JSON.stringify(item))
  dialogTitle.value = '编辑地址'
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
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    })
  }).catch(() => {
    // 用户取消删除
  })
}

const saveAddress = () => {
  // 表单验证
  if (!address.value.linkUser || !address.value.linkUser.trim()) {
    ElMessage.warning('请输入收货人姓名')
    return
  }
  if (!address.value.linkPhone || !address.value.linkPhone.trim()) {
    ElMessage.warning('请输入联系电话')
    return
  }
  if (!/^\d{11}$/.test(address.value.linkPhone)) {
    ElMessage.warning('联系电话必须为11位数字')
    return
  }
  if (!address.value.linkAddress || !address.value.linkAddress.trim()) {
    ElMessage.warning('请输入收货地址')
    return
  }

  address.value.userId = userId.value
  API.post('/api/address', address.value).then((res) => {
    if (res.code === '200') {
      ElMessage.success('保存成功')
      loadAddress()
      dialogFormVisible.value = false
    } else {
      ElMessage.error(res.msg || '保存失败')
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

onMounted(() => {
  loadAddress()
})
</script>

<style scoped>
.address-manage-container {
  min-height: calc(100vh - 80px);
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

/* 页面标题栏 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  margin-bottom: 30px;
  border-bottom: 3px solid #f0f0f0;
  background: #f8f9fa;
  border-radius: 8px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.header-icon {
  font-size: 32px;
  color: #409EFF;
}

.page-title {
  margin: 0;
  font-size: 26px;
  font-weight: 600;
  color: #303133;
}

.address-count {
  background: #409EFF;
  color: white;
  padding: 5px 15px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
}

.add-btn {
  font-size: 16px;
  padding: 12px 28px;
  border-radius: 8px;
  font-weight: 500;
}


/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
}

.empty-icon {
  margin-bottom: 20px;
}

.empty-icon i {
  font-size: 120px;
  color: #dcdfe6;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.empty-title {
  font-size: 22px;
  color: #909399;
  margin: 20px 0 10px;
  font-weight: 500;
}

.empty-desc {
  font-size: 16px;
  color: #c0c4cc;
  margin-bottom: 30px;
}

.empty-add-btn {
  padding: 12px 32px;
  font-size: 16px;
  border-radius: 8px;
}

/* 对话框样式 */
.address-form {
  padding: 20px 30px 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 20px 30px;
}

.dialog-footer .el-button {
  padding: 10px 24px;
  font-size: 15px;
  border-radius: 6px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .address-grid {
    grid-template-columns: 1fr;
  }
  
  .page-header {
    flex-direction: column;
    gap: 15px;
  }
  
  .header-left {
    flex-direction: column;
    text-align: center;
  }
  
  .content-wrapper {
    width: 95%;
    padding: 20px;
  }
}
</style>

