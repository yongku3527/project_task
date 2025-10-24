<template>
  <div class="test-login">
    <el-card class="test-card">
      <template #header>
        <div class="card-header">
          <span>登录测试页面</span>
        </div>
      </template>
      
      <div class="test-content">
        <el-form :model="loginForm" label-width="80px">
          <el-form-item label="用户名">
            <el-input v-model="loginForm.username" placeholder="请输入用户名"></el-input>
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleTestLogin" :loading="loading">测试登录</el-button>
            <el-button @click="handleTestLogout">测试退出</el-button>
          </el-form-item>
        </el-form>
        
        <div class="test-result" v-if="result">
          <el-divider></el-divider>
          <h4>测试结果：</h4>
          <pre>{{ result }}</pre>
        </div>
        
        <div class="current-status">
          <el-divider></el-divider>
          <h4>当前状态：</h4>
          <p><strong>登录状态：</strong>{{ isLoggedIn ? '已登录' : '未登录' }}</p>
          <p><strong>用户名：</strong>{{ username || '无' }}</p>
          <p><strong>Token：</strong>{{ token ? token.substring(0, 20) + '...' : '无' }}</p>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { http } from '../utils/request'
import { ElMessage } from 'element-plus'

const loginForm = ref({
  username: 'admin',
  password: 'admin123'
})

const loading = ref(false)
const result = ref('')
const isLoggedIn = ref(false)
const username = ref('')
const token = ref('')

onMounted(() => {
  updateStatus()
})

const updateStatus = () => {
  isLoggedIn.value = !!localStorage.getItem('token')
  username.value = localStorage.getItem('username') || ''
  token.value = localStorage.getItem('token') || ''
}

const handleTestLogin = async () => {
  loading.value = true
  try {
    const response = await http.post('/auth/login', loginForm.value)
    result.value = JSON.stringify(response, null, 2)
    
    if (response.code === 200) {
      localStorage.setItem('token', response.data)
      localStorage.setItem('username', loginForm.value.username)
      ElMessage.success('登录成功！')
      updateStatus()
    }
  } catch (error) {
    result.value = '登录失败：' + error.message
    ElMessage.error('登录失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const handleTestLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  result.value = '已退出登录'
  ElMessage.success('已退出登录')
  updateStatus()
}
</script>

<style scoped>
.test-login {
  max-width: 800px;
  margin: 20px auto;
  padding: 20px;
}

.test-card {
  margin-bottom: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.test-content {
  padding: 20px 0;
}

.test-result pre {
  background-color: #f5f5f5;
  padding: 15px;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 14px;
}

.current-status p {
  margin: 10px 0;
  line-height: 1.6;
}

.current-status strong {
  display: inline-block;
  width: 80px;
  text-align: right;
  margin-right: 10px;
}
</style>