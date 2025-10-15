<template>
  <div class="file-upload-container">
    <el-card class="upload-card">
      <template #header>
        <div class="card-header">
          <span>文件上传</span>
          <el-button 
            v-if="uploadTasks.length > 0"
            type="danger" 
            size="small" 
            @click="clearAllTasks"
          >
            清空任务
          </el-button>
        </div>
      </template>

      <div class="upload-controls">
        <el-upload
          ref="uploadRef"
          class="upload-area"
          drag
          multiple
          :auto-upload="false"
          :show-file-list="false"
          :on-change="handleFileChange"
          accept="*/*"
        >
          <el-icon class="upload-icon"><UploadFilled /></el-icon>
          <div class="el-upload__text">
            拖拽文件到此处或 <em>点击选择文件</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              支持大文件分片上传，单个文件最大支持5GB
            </div>
          </template>
        </el-upload>

        <div class="upload-options">
          <el-form :inline="true" size="small">
            <el-form-item label="分片大小:">
              <el-select v-model="partSize" style="width: 120px">
                <el-option label="5MB" :value="5 * 1024 * 1024" />
                <el-option label="10MB" :value="10 * 1024 * 1024" />
                <el-option label="20MB" :value="20 * 1024 * 1024" />
              </el-select>
            </el-form-item>
            <el-form-item label="存储桶:">
              <el-input v-model="bucketName" placeholder="输入存储桶名称" style="width: 150px" />
            </el-form-item>
          </el-form>
        </div>
      </div>

      <div v-if="uploadTasks.length > 0" class="upload-tasks">
        <h4>上传任务列表</h4>
        <div class="task-list">
          <div 
            v-for="task in uploadTasks" 
            :key="task.id"
            class="task-item"
            :class="{ 'task-completed': task.status === 'completed', 'task-failed': task.status === 'failed' }"
          >
            <div class="task-info">
              <div class="task-name">{{ task.file.name }}</div>
              <div class="task-size">{{ formatFileSize(task.file.size) }}</div>
              <div class="task-status">
                <el-tag :type="getStatusType(task.status)" size="small">
                  {{ getStatusText(task.status) }}
                </el-tag>
              </div>
            </div>
            
            <div class="task-progress" v-if="task.status === 'uploading'">
              <el-progress 
                :percentage="task.progress" 
                :status="task.progress === 100 ? 'success' : 'text'"
              />
            </div>

            <div class="task-actions">
              <el-button 
                v-if="task.status === 'pending'"
                type="primary" 
                size="small" 
                @click="startUpload(task)"
                :loading="task.uploading"
              >
                开始上传
              </el-button>
              <el-button 
                v-if="task.status === 'uploading'"
                type="warning" 
                size="small" 
                @click="pauseUpload(task)"
              >
                暂停
              </el-button>
              <el-button 
                v-if="task.status === 'paused'"
                type="primary" 
                size="small" 
                @click="resumeUpload(task)"
              >
                继续
              </el-button>
              <el-button 
                v-if="task.status === 'failed'"
                type="primary" 
                size="small" 
                @click="retryUpload(task)"
              >
                重试
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                @click="removeTask(task)"
                :icon="Delete"
                circle
              />
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled, Delete } from '@element-plus/icons-vue'
import axios from 'axios'

const baseUrl = 'http://192.168.100.125:8083'

// 响应式数据
const uploadRef = ref()
const bucketName = ref('files')
const partSize = ref(5 * 1024 * 1024) // 默认5MB
const uploadTasks = ref([])

// 获取全局属性
const internalInstance = getCurrentInstance()
const axiosInstance = internalInstance?.appContext.config.globalProperties.$axios || axios

// 文件选择处理
const handleFileChange = (file, fileList) => {
  // 添加新的上传任务
  const task = {
    id: Date.now() + Math.random(),
    file: file.raw,
    status: 'pending', // pending, uploading, paused, completed, failed
    progress: 0,
    uploadId: null,
    parts: [],
    uploadedParts: [],
    uploading: false,
    error: null
  }
  
  uploadTasks.value.push(task)
  
  // 自动开始上传
  setTimeout(() => startUpload(task), 100)
}

// 开始上传
const startUpload = async (task) => {
  if (task.status === 'completed') return
  
  task.status = 'uploading'
  task.uploading = true
  task.error = null
  
  try {
    // 创建预上传任务
    const response = await axiosInstance.post(
      `${baseUrl}/minio/buckets/${bucketName.value}/files/${task.file.name}/presigned-upload`,
      null,
      {
        params: {
          fileSize: task.file.size,
          partSize: partSize.value
        }
      }
    )
    
    if (response.data.code !== 200) {
      throw new Error(response.data.msg || '创建上传任务失败')
    }
    
    const uploadData = response.data.data
    
    // 统一使用简单上传
    await uploadSimpleFile(task, uploadData.presignedUrl)
    
    task.status = 'completed'
    task.progress = 100
    ElMessage.success(`文件 ${task.file.name} 上传成功`)
    
  } catch (error) {
    task.status = 'failed'
    task.error = error.response?.data?.msg || error.message
    ElMessage.error(`文件 ${task.file.name} 上传失败: ${error.response?.data?.msg || error.message}`)
  } finally {
    task.uploading = false
  }
}

// 简单文件上传
const uploadSimpleFile = async (task, presignedUrl) => {
  const xhr = new XMLHttpRequest()
  
  // 监听上传进度
  xhr.upload.onprogress = (event) => {
    if (event.lengthComputable) {
      task.progress = Math.round((event.loaded / event.total) * 100)
    }
  }
  
  return new Promise((resolve, reject) => {
    xhr.onload = () => {
      if (xhr.status === 200) {
        resolve()
      } else {
        reject(new Error(`上传失败: ${xhr.status} ${xhr.statusText}`))
      }
    }
    
    xhr.onerror = () => reject(new Error('网络错误'))
    
    xhr.open('PUT', presignedUrl, true)
    xhr.send(task.file)
  })
}

// 分片文件上传
const uploadMultipartFile = async (task) => {
  const totalParts = task.parts.length
  let uploadedCount = 0
  
  for (const part of task.parts) {
    if (task.status === 'paused') {
      break
    }
    
    try {
      // 读取分片数据
      const chunk = await readFileChunk(task.file, part.start, part.end)
      
      // 上传分片
      const response = await axiosInstance.put(part.presignedUrl, chunk, {
        headers: {
          'Content-Type': task.file.type || 'application/octet-stream'
        }
      })
      
      // 保存分片信息
      task.uploadedParts.push({
        partNumber: part.partNumber,
        etag: response.headers.etag || response.headers.etag
      })
      
      uploadedCount++
      task.progress = Math.round((uploadedCount * 100) / totalParts)
      
    } catch (error) {
      throw new Error(`分片 ${part.partNumber} 上传失败: ${error.response?.data?.msg || error.message}`)
    }
  }
  
  if (task.status === 'uploading' && task.uploadedParts.length === totalParts) {
    // 完成分片上传
    await completeMultipartUpload(task)
  }
}

// 读取文件分片
const readFileChunk = (file, start, end) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    const blob = file.slice(start, end)
    
    reader.onload = (e) => resolve(e.target.result)
    reader.onerror = (error) => reject(error)
    reader.readAsArrayBuffer(blob)
  })
}

// 完成分片上传
const completeMultipartUpload = async (task) => {
  const response = await axiosInstance.post(
    `${baseUrl}/minio/buckets/${bucketName.value}/files/${task.file.name}/complete-upload`,
    task.uploadedParts,
    {
      params: {
        uploadId: task.uploadId
      }
    }
  )
  
  if (response.data.code !== 200) {
    throw new Error(response.data.msg || '完成分片上传失败')
  }
}

// 暂停上传
const pauseUpload = (task) => {
  if (task.status === 'uploading') {
    task.status = 'paused'
    ElMessage.info('上传已暂停')
  }
}

// 继续上传
const resumeUpload = (task) => {
  if (task.status === 'paused') {
    startUpload(task)
  }
}

// 重试上传
const retryUpload = (task) => {
  task.status = 'pending'
  task.progress = 0
  task.uploadedParts = []
  setTimeout(() => startUpload(task), 100)
}

// 移除任务
const removeTask = async (task) => {
  if (task.status === 'uploading') {
    await ElMessageBox.confirm('文件正在上传中，确定要移除吗？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  }
  
  // 如果是分片上传且已初始化，取消上传
  if (task.uploadId && task.status !== 'completed') {
    try {
      await axiosInstance.delete(
        `${baseUrl}/minio/buckets/${bucketName.value}/files/${task.file.name}/abort-upload`,
        {
          params: {
            uploadId: task.uploadId
          }
        }
      )
    } catch (error) {
      console.warn('取消分片上传失败:', error)
    }
  }
  
  const index = uploadTasks.value.findIndex(t => t.id === task.id)
  if (index > -1) {
    uploadTasks.value.splice(index, 1)
  }
}

// 清空所有任务
const clearAllTasks = () => {
  uploadTasks.value = []
  ElMessage.success('已清空所有上传任务')
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    pending: 'info',
    uploading: 'warning',
    paused: 'warning',
    completed: 'success',
    failed: 'danger'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    pending: '等待上传',
    uploading: '上传中',
    paused: '已暂停',
    completed: '上传完成',
    failed: '上传失败'
  }
  return texts[status] || '未知状态'
}
</script>

<style scoped>
.file-upload-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.upload-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.upload-controls {
  margin-bottom: 20px;
}

.upload-area {
  width: 100%;
}

.upload-icon {
  font-size: 48px;
  color: #409EFF;
  margin-bottom: 10px;
}

.upload-options {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.upload-tasks {
  margin-top: 30px;
}

.task-list {
  margin-top: 15px;
}

.task-item {
  display: flex;
  flex-direction: column;
  padding: 15px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin-bottom: 10px;
  background-color: #fff;
  transition: all 0.3s;
}

.task-item:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.task-completed {
  background-color: #f0f9ff;
  border-color: #b3d8ff;
}

.task-failed {
  background-color: #fef0f0;
  border-color: #fbc4c4;
}

.task-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.task-name {
  font-weight: 500;
  color: #303133;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-size {
  color: #909399;
  margin: 0 15px;
}

.task-status {
  margin: 0 15px;
}

.task-progress {
  margin-bottom: 10px;
}

.task-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .task-info {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .task-size,
  .task-status {
    margin: 5px 0;
  }
  
  .task-actions {
    justify-content: flex-start;
  }
}
</style>