<template>
  <div class="user-info">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户信息</span>
        </div>
      </template>
      <div class="user-details">
        <p><strong>用户名：</strong>{{ username }}</p>
        <p><strong>登录状态：</strong><el-tag type="success">已登录</el-tag></p>
        <p><strong>Token：</strong>{{ token ? token.substring(0, 20) + '...' : '无' }}</p>
        <el-button type="primary" @click="refreshUserInfo">刷新信息</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const username = ref('')
const token = ref('')

onMounted(() => {
  refreshUserInfo()
})

const refreshUserInfo = () => {
  username.value = localStorage.getItem('username') || ''
  token.value = localStorage.getItem('token') || ''
  
  if (!username.value || !token.value) {
    ElMessage.warning('用户信息不完整，请重新登录')
  }
}
</script>

<style scoped>
.user-info {
  max-width: 600px;
  margin: 20px auto;
}

.user-details p {
  margin: 10px 0;
  line-height: 1.6;
}

.user-details strong {
  display: inline-block;
  width: 80px;
  text-align: right;
  margin-right: 10px;
}
</style>