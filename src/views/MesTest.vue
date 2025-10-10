<template>
  <div class="mes-test-container">
    <h2>MES接口集成测试</h2>
    
    <div class="test-section">
      <h3>单个物料查询测试</h3>
      <el-form :inline="true" class="demo-form-inline">
        <el-form-item label="物料编码">
          <el-input v-model="testItemCode" placeholder="请输入物料编码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="testSingleQuery">查询</el-button>
        </el-form-item>
      </el-form>
      
      <div v-if="singleResult" class="result-section">
        <h4>查询结果：</h4>
        <pre>{{ JSON.stringify(singleResult, null, 2) }}</pre>
      </div>
    </div>

    <div class="test-section">
      <h3>批量物料查询测试</h3>
      <el-form :inline="true" class="demo-form-inline">
        <el-form-item label="物料编码列表">
          <el-input 
            v-model="testItemCodes" 
            placeholder="请输入物料编码，用逗号分隔"
            style="width: 300px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="testBatchQuery">批量查询</el-button>
        </el-form-item>
      </el-form>
      
      <div v-if="batchResult" class="result-section">
        <h4>批量查询结果：</h4>
        <pre>{{ JSON.stringify(batchResult, null, 2) }}</pre>
      </div>
    </div>

    <div class="test-section">
      <h3>前端自动填充测试</h3>
      <el-button type="success" @click="goToCircuitBoardManager">前往线路板管理页面测试</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const baseUrl = 'http://192.168.90.64:8083'

const testItemCode = ref('TEST001')
const testItemCodes = ref('TEST001,TEST002,TEST003')
const singleResult = ref(null)
const batchResult = ref(null)

// 单个查询测试
const testSingleQuery = async () => {
  if (!testItemCode.value.trim()) {
    ElMessage.warning('请输入物料编码')
    return
  }
  
  try {
    const response = await axios.get(`${baseUrl}/mes/item-info`, {
      params: { itemCode: testItemCode.value.trim() }
    })
    
    if (response.data.code === 200) {
      singleResult.value = response.data.data
      ElMessage.success('查询成功')
    } else {
      ElMessage.error('查询失败: ' + response.data.msg)
      singleResult.value = null
    }
  } catch (error) {
    ElMessage.error('接口调用失败: ' + error.message)
    singleResult.value = null
  }
}

// 批量查询测试
const testBatchQuery = async () => {
  if (!testItemCodes.value.trim()) {
    ElMessage.warning('请输入物料编码列表')
    return
  }
  
  const codes = testItemCodes.value.split(',').map(code => code.trim()).filter(code => code)
  if (codes.length === 0) {
    ElMessage.warning('请输入有效的物料编码')
    return
  }
  
  // 构建请求体
  const requestBody = {}
  codes.forEach(code => {
    requestBody[code] = code
  })
  
  try {
    const response = await axios.post(`${baseUrl}/mes/item-info/batch`, requestBody)
    
    if (response.data.code === 200) {
      batchResult.value = response.data.data
      ElMessage.success('批量查询成功')
    } else {
      ElMessage.error('批量查询失败: ' + response.data.msg)
      batchResult.value = null
    }
  } catch (error) {
    ElMessage.error('接口调用失败: ' + error.message)
    batchResult.value = null
  }
}

// 跳转到线路板管理页面
const goToCircuitBoardManager = () => {
  router.push('/circuit-board-manager')
}
</script>

<style scoped>
.mes-test-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.test-section {
  margin-bottom: 40px;
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background-color: #f5f7fa;
}

.test-section h3 {
  margin-top: 0;
  color: #303133;
}

.result-section {
  margin-top: 20px;
  padding: 15px;
  background-color: white;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.result-section h4 {
  margin-top: 0;
  color: #409eff;
}

.result-section pre {
  margin: 0;
  padding: 10px;
  background-color: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  font-size: 12px;
  overflow-x: auto;
}

.demo-form-inline {
  display: flex;
  align-items: center;
}
</style>