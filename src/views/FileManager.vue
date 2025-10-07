<template>
  <div class="file-manager-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>文件管理器</span>
          <div class="header-actions">
            <el-button type="primary" size="small" @click="showUploadDialog = true">
              <el-icon><Upload /></el-icon>
              上传文件
            </el-button>
            <el-button type="success" size="small" @click="refreshFiles">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <div class="file-manager-content">
        <!-- 存储桶选择 -->
        <div class="bucket-selector">
          <el-form :inline="true" size="small">
            <el-form-item label="存储桶:">
              <el-select v-model="currentBucket" @change="onBucketChange" style="width: 200px">
                <el-option 
                  v-for="bucket in buckets" 
                  :key="bucket.name" 
                  :label="bucket.name" 
                  :value="bucket.name" 
                />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="small" @click="showCreateBucketDialog = true">
                <el-icon><Plus /></el-icon>
                新建存储桶
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 文件列表 -->
        <div class="file-list-container">
          <el-table 
            :data="files" 
            v-loading="loading"
            style="width: 100%"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="objectName" label="文件名" min-width="200">
              <template #default="{ row }">
                <div class="file-name-cell">
                  <el-icon class="file-icon">
                    <Document v-if="row.isFile" />
                    <Folder v-else />
                  </el-icon>
                  <span class="file-name" @click="handleFileClick(row)">{{ row.objectName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="size" label="大小" width="120">
              <template #default="{ row }">
                {{ formatFileSize(row.size) }}
              </template>
            </el-table-column>
            <el-table-column prop="lastModified" label="修改时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.lastModified) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button 
                  type="primary" 
                  size="small" 
                  link
                  @click="downloadFile(row)"
                >
                  <el-icon><Download /></el-icon>
                  下载
                </el-button>
                <el-button 
                  type="danger" 
                  size="small" 
                  link
                  @click="deleteFile(row)"
                >
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="totalFiles"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>

    <!-- 上传文件对话框 -->
    <el-dialog
      v-model="showUploadDialog"
      title="上传文件"
      width="80%"
      :close-on-click-modal="false"
    >
      <FileUpload />
    </el-dialog>

    <!-- 创建存储桶对话框 -->
    <el-dialog
      v-model="showCreateBucketDialog"
      title="创建存储桶"
      width="400px"
    >
      <el-form :model="bucketForm" :rules="bucketRules" ref="bucketFormRef">
        <el-form-item label="存储桶名称" prop="name">
          <el-input v-model="bucketForm.name" placeholder="请输入存储桶名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateBucketDialog = false">取消</el-button>
          <el-button type="primary" @click="createBucket">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Folder, Download, Delete, Plus, Refresh, Upload } from '@element-plus/icons-vue'
import FileUpload from '../components/FileUpload.vue'
import axios from 'axios'

const baseUrl = 'http://192.168.90.64:8083'

// 响应式数据
const buckets = ref([])
const files = ref([])
const currentBucket = ref('files')
const loading = ref(false)
const selectedFiles = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const totalFiles = ref(0)
const showUploadDialog = ref(false)
const showCreateBucketDialog = ref(false)
const bucketFormRef = ref()

const bucketForm = reactive({
  name: ''
})

const bucketRules = {
  name: [
    { required: true, message: '请输入存储桶名称', trigger: 'blur' },
    { pattern: /^[a-z0-9][a-z0-9-]*[a-z0-9]$/, message: '存储桶名称只能包含小写字母、数字和连字符', trigger: 'blur' }
  ]
}

// 生命周期
onMounted(() => {
  loadBuckets()
  loadFiles()
})

// 加载存储桶列表
const loadBuckets = async () => {
  try {
    const response = await axios.get(`${baseUrl}/minio/buckets`)
    if (response.data.code === 200) {
      // 后端现在返回的是字符串数组，直接作为桶名称
      buckets.value = response.data.data.map(bucketName => ({
        name: bucketName
      }))
      if (buckets.value.length > 0 && !currentBucket.value) {
        currentBucket.value = buckets.value[0].name
      }
    } else {
      ElMessage.error('获取存储桶列表失败')
    }
  } catch (error) {
    ElMessage.error('获取存储桶列表失败: ' + error.message)
  }
}

// 加载文件列表
const loadFiles = async () => {
  if (!currentBucket.value) return
  
  loading.value = true
  try {
    const response = await axios.get(`${baseUrl}/minio/buckets/${currentBucket.value}/files`, {
      params: {
        prefix: '',
        recursive: true
      }
    })
    
    if (response.data.code === 200) {
      // debugger
      files.value = response.data.data.map(item => ({
        ...item,
        objectName: item.objectName || item.name,
        size: item.size || 0,
        lastModified: item.lastModified || new Date(),
        isFile: !item.objectName?.endsWith('/')
      }))
      totalFiles.value = files.value.length
    } else {
      ElMessage.error('获取文件列表失败')
    }
  } catch (error) {
    ElMessage.error('获取文件列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 存储桶切换
const onBucketChange = () => {
  currentPage.value = 1
  loadFiles()
}

// 文件选择变化
const handleSelectionChange = (selection) => {
  selectedFiles.value = selection
}

// 文件点击
const handleFileClick = (file) => {
  if (!file.isFile) {
    // 如果是文件夹，可以进入文件夹
    ElMessage.info('文件夹功能开发中...')
  }
}

// 下载文件
const downloadFile = async (file) => {
  try {
    // 获取预签名下载URL
    const response = await axios.get(
      `${baseUrl}/minio/buckets/${currentBucket.value}/files/${file.objectName}/presigned-url`
    )
    
    if (response.data.code === 200) {
      const downloadUrl = response.data.data
      
      // 创建下载链接
      const link = document.createElement('a')
      link.href = downloadUrl
      link.download = file.objectName
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      
      ElMessage.success('文件下载开始')
    } else {
      ElMessage.error('获取下载链接失败')
    }
  } catch (error) {
    ElMessage.error('下载文件失败: ' + error.message)
  }
}

// 删除文件
const deleteFile = async (file) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文件 "${file.objectName}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await axios.delete(
      `${baseUrl}/minio/buckets/${currentBucket.value}/files/${file.objectName}`
    )
    
    if (response.data.code === 200) {
      ElMessage.success('文件删除成功')
      loadFiles()
    } else {
      ElMessage.error('删除文件失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除文件失败: ' + error.message)
    }
  }
}

// 创建存储桶
const createBucket = async () => {
  if (!bucketFormRef.value) return
  
  try {
    await bucketFormRef.value.validate()
    
    const response = await axios.post(`${baseUrl}/minio/buckets/${bucketForm.name}`)
    
    if (response.data.code === 200) {
      ElMessage.success('存储桶创建成功')
      showCreateBucketDialog.value = false
      bucketForm.name = ''
      loadBuckets()
    } else {
      ElMessage.error(response.data.msg || '创建存储桶失败')
    }
  } catch (error) {
    if (error !== false) {
      ElMessage.error('创建存储桶失败: ' + error.message)
    }
  }
}

// 刷新文件列表
const refreshFiles = () => {
  loadFiles()
  ElMessage.success('文件列表已刷新')
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  loadFiles()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadFiles()
}

// 工具函数
const formatFileSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatDate = (date) => {
  if (!date) return '-'
  const d = new Date(date)
  return d.toLocaleString('zh-CN')
}
</script>

<style scoped>
.file-manager-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.bucket-selector {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.file-list-container {
  margin-bottom: 20px;
}

.file-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-icon {
  color: #409EFF;
  font-size: 16px;
}

.file-name {
  cursor: pointer;
  color: #303133;
  text-decoration: none;
}

.file-name:hover {
  color: #409EFF;
  text-decoration: underline;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    gap: 10px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .bucket-selector .el-form-item {
    display: block;
    margin-bottom: 10px;
  }
}
</style>