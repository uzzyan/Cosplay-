<template>
<div class="address-card" :class="{ 'selected': selected }">
  <div class="card-header">
    <i class="el-icon-user-solid" style="margin-right: 8px; color: #409EFF;"></i>
    <span class="user-name">{{address.linkUser}}</span>
  </div>
  
  <div class="card-body">
    <div class="info-row">
      <i class="el-icon-phone" style="color: #67C23A;"></i>
      <span class="info-text">{{address.linkPhone}}</span>
    </div>
    <div class="info-row">
      <i class="el-icon-location-information" style="color: #E6A23C;"></i>
      <span class="info-text">{{address.linkAddress}}</span>
    </div>
  </div>
  
  <div class="card-footer">
    <el-button 
      type="primary" 
      link
      @click.stop="emit('edit')"
      class="action-btn edit-btn"
    >
      修改
    </el-button>
    <el-button 
      type="danger" 
      link
      @click.stop="emit('delete')"
      class="action-btn delete-btn"
    >
      删除
    </el-button>
  </div>
</div>
</template>

<script setup>
const props = defineProps({
  address: Object,
  selected: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['edit', 'delete'])
</script>

<style scoped>
.address-card {
  position: relative;
  width: 320px;
  min-height: 160px;
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  border: 2px solid #e8e8e8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  display: inline-block;
  vertical-align: top;
  cursor: pointer;
}

.address-card.selected {
  border: 3px solid #409EFF;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 12px;
  border-bottom: 2px solid #f0f0f0;
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.card-body {
  margin-bottom: 15px;
}

.info-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 10px;
  padding: 5px 0;
}

.info-row i {
  font-size: 16px;
  margin-right: 10px;
  margin-top: 2px;
  flex-shrink: 0;
}

.info-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  word-break: break-all;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.action-btn {
  font-size: 14px;
  font-weight: 500;
  padding: 5px 15px;
  transition: all 0.3s ease;
}

.edit-btn {
  color: #409EFF;
}

.edit-btn:hover {
  color: #66b1ff;
  background-color: #ecf5ff;
}

.delete-btn {
  color: #F56C6C;
}

.delete-btn:hover {
  color: #f78989;
  background-color: #fef0f0;
}
</style>