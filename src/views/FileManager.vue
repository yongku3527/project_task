<template>
  <div class="file-manager-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>文件管理器（数据库）</span>
          <div class="header-actions">
            <!-- <el-button v-permission="'file:add'" type="primary" size="small" @click="showUploadDialog = true">
              <el-icon><Upload /></el-icon>
              上传到MinIO
            </el-button> -->
            <el-button type="success" size="small" @click="refreshFiles">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <!-- 文件统计信息11 -->
      <div class="file-stats">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-label">总文件数</div>
                <div class="stat-value">{{ totalFiles }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-label">总大小</div>
                <div class="stat-value">{{ totalSize }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-label">PDF文件</div>
                <div class="stat-value">{{ pdfCount }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-label">图片文件</div>
                <div class="stat-value">{{ imageCount }}</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <div class="file-manager-content">
        <!-- 搜索和筛选 -->
        <div class="file-filter">
          <el-form :inline="true" size="small">
            <el-form-item label="存储桶:">
              <el-select 
                v-model="selectedBucket" 
                placeholder="全部桶" 
                style="width: 120px"
                clearable
                @change="handleBucketChange"
              >
                <el-option label="全部桶" value="" />
                <el-option 
                  v-for="bucket in buckets" 
                  :key="bucket.name" 
                  :label="bucket.name" 
                  :value="bucket.name"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="文件名:">
              <el-input 
                v-model="searchKeyword" 
                placeholder="请输入文件名关键字" 
                style="width: 200px"
                clearable
                @clear="loadFiles"
                @keyup.enter="loadFiles"
              />
            </el-form-item>
            <el-form-item label="文件类型:">
              <el-select 
                v-model="searchSuffix" 
                placeholder="全部类型" 
                style="width: 120px"
                clearable
                @clear="loadFiles"
              >
                <el-option label="全部类型" value="" />
                <el-option label="PDF文件" value=".pdf" />
                <el-option label="图片文件" value=".jpg" />
                <el-option label="Excel文件" value=".xlsx" />
                <el-option label="Word文件" value=".docx" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="small" @click="loadFiles">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button size="small" @click="resetSearch">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 桶统计信息 -->
        <div v-if="buckets.length > 0" class="bucket-stats">
          <el-row :gutter="20" style="margin-bottom: 20px;">
            <el-col 
              v-for="bucket in buckets" 
              :key="bucket.name" 
              :span="6"
            >
              <el-card shadow="hover" :class="{ 'active-bucket': selectedBucket === bucket.name }">
                <div class="bucket-stat-item">
                  <div class="bucket-name">{{ bucket.name }}</div>
                  <div class="bucket-count">{{ bucketFilesMap[bucket.name]?.length || 0 }} 个文件</div>
                  <div class="bucket-size">{{ getBucketSize(bucket.name) }}</div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 按桶分表格显示文件列表 -->
        <div class="bucket-files-container">
          <div 
            v-for="bucket in buckets" 
            :key="bucket.name" 
            class="bucket-table-section"
          >
            <div class="bucket-table-header">
              <h3>
                <el-icon><Folder /></el-icon>
                {{ bucket.name }} 
                <span class="file-count">({{ getBucketFileCount(bucket.name) }} 个文件)</span>
              </h3>
            </div>
            
            <el-table 
              :data="getBucketFiles(bucket.name)" 
              v-loading="loading"
              style="width: 100%"
              @selection-change="(selection) => handleBucketSelectionChange(selection, bucket.name)"
              :row-key="getRowKey"
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
                  {{ row.size > 0 ? formatFileSize(row.size) : '-' }}
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="上传时间" width="180">
                <template #default="{ row }">
                  {{ formatDate(row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="{ row }">
                  <el-button 
                    v-permission="'file:menu'"
                    type="primary" 
                    size="small" 
                    link
                    @click="downloadFile(row)"
                  >
                    <el-icon><Download /></el-icon>
                    下载
                  </el-button>
                  <el-button 
                    v-permission="'file:delete'"
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
            
            <!-- 桶内分页 -->
            <div class="bucket-pagination" v-if="getBucketFileCount(bucket.name) > 10">
              <el-pagination
                :current-page="bucketCurrentPages[bucket.name] || 1"
                :page-size="10"
                :page-sizes="[10, 20, 50, 100]"
                :total="getBucketFileCount(bucket.name)"
                layout="total, prev, pager, next"
                @current-change="(val) => handleBucketCurrentChange(val, bucket.name)"
              />
            </div>
          </div>
          
          <!-- 空状态 -->
          <div v-if="buckets.length === 0 && !loading" class="empty-state">
            <el-empty description="暂无文件数据" />
          </div>
        </div>
      </div>
    </el-card>

    <!-- 上传文件对话框 -->
    <el-dialog
      v-model="showUploadDialog"
      title="上传文件到MinIO"
      width="80%"
      :close-on-click-modal="false"
    >
      <FileUpload />
    </el-dialog>


  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Folder, Download, Delete, Plus, Refresh, Upload, Search } from '@element-plus/icons-vue'
import FileUpload from '../components/FileUpload.vue'
import axios from 'axios'
import PermissionManager from '@/utils/permission'

const baseUrl = 'http://192.168.100.125:8083'

// 响应式数据
const files = ref([])
const loading = ref(false)
const selectedFiles = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const totalFiles = ref(0)
const showUploadDialog = ref(false)
const searchKeyword = ref('')
const searchSuffix = ref('')
const totalSize = ref('0 B') // 总文件大小
const pdfCount = ref(0) // PDF文件数量
const imageCount = ref(0) // 图片文件数量
const selectedBucket = ref('') // 当前选中的桶
const buckets = ref([]) // 所有桶列表
const bucketFilesMap = ref({}) // 桶到文件的映射
const bucketCurrentPages = ref({}) // 每个桶的当前页数
const bucketSelectionMaps = ref({}) // 每个桶的选中项映射

// 生命周期
onMounted(() => {
  loadFiles()
  loadStatistics()
  loadBuckets()
})

// 加载所有桶信息
const loadBuckets = async () => {
  try {
    // 从文件列表中提取所有桶名
    const bucketSet = new Set()
    files.value.forEach(file => {
      if (file.bucketName && file.bucketName !== 'unknown') {
        bucketSet.add(file.bucketName)
      }
    })
    
    // 转换为桶对象数组
    buckets.value = Array.from(bucketSet).map(name => ({
      name: name,
      displayName: name
    }))
    
    console.log('发现的桶:', buckets.value)
  } catch (error) {
    console.error('加载桶信息失败:', error)
  }
}

// 按桶组织文件
const organizeFilesByBucket = () => {
  const bucketMap = {}
  
  files.value.forEach(file => {
    const bucketName = file.bucketName || 'unknown'
    if (!bucketMap[bucketName]) {
      bucketMap[bucketName] = []
    }
    bucketMap[bucketName].push(file)
  })
  
  bucketFilesMap.value = bucketMap
  
  // 刷新桶列表
  loadBuckets()
}

// 处理桶选择变化
const handleBucketChange = (bucketName) => {
  selectedBucket.value = bucketName
  if (bucketName) {
    // 过滤显示指定桶的文件
    files.value = bucketFilesMap.value[bucketName] || []
  } else {
    // 显示所有文件
    loadFiles()
  }
}

// 获取桶的总大小
const getBucketSize = (bucketName) => {
  const bucketFiles = bucketFilesMap.value[bucketName] || []
  let totalBytes = 0
  
  bucketFiles.forEach(file => {
    if (file.fileSize && file.fileSize > 0) {
      totalBytes += file.fileSize
    }
  })
  
  return formatFileSize(totalBytes)
}

// 获取指定桶的文件数量
const getBucketFileCount = (bucketName) => {
  return bucketFilesMap.value[bucketName]?.length || 0
}

// 获取指定桶的文件（带分页）
const getBucketFiles = (bucketName) => {
  const bucketFiles = bucketFilesMap.value[bucketName] || []
  const currentPage = bucketCurrentPages.value[bucketName] || 1
  const start = (currentPage - 1) * pageSize.value
  const end = start + pageSize.value
  return bucketFiles.slice(start, end)
}

// 获取桶的当前页数
const getBucketCurrentPage = (bucketName) => {
  return bucketCurrentPages.value[bucketName] || 1
}

// 桶级别的选择变化处理
const handleBucketSelectionChange = (selection, bucketName) => {
  bucketSelectionMaps.value[bucketName] = selection
}

// 桶级别的分页处理
const handleBucketSizeChange = (val, bucketName) => {
  pageSize.value = val
  // 可以选择重新加载数据或重新计算分页
}

// 桶级别的页码变化处理
const handleBucketCurrentChange = (val, bucketName) => {
  bucketCurrentPages.value[bucketName] = val
}

// 获取表格行key
const getRowKey = (row) => {
  return row.id || row.objectName
}

// 加载文件统计信息
const loadStatistics = async () => {
  try {
    const response = await axios.get(`${baseUrl}/file-info/statistics`)
    if (response.data?.code === 200) {
      const data = response.data?.data
      
      // 计算总文件大小
      let totalBytes = 0
      files.value.forEach(file => {
        if (file.fileSize && file.fileSize > 0) {
          totalBytes += file.fileSize
        }
      })
      totalSize.value = formatFileSize(totalBytes)
      
      // 统计PDF和图片文件数量
      pdfCount.value = files.value.filter(file => 
        file.fileSuffix && file.fileSuffix.toLowerCase() === '.pdf'
      ).length
      
      imageCount.value = files.value.filter(file => {
        const suffix = file.fileSuffix ? file.fileSuffix.toLowerCase() : ''
        return ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.svg'].includes(suffix)
      }).length
      
      // 如果后端有统计信息，使用后端的总数
      if (data.totalFiles !== undefined) {
        totalFiles.value = data.totalFiles
      }
    }
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}



// 加载文件列表（从数据库）
const loadFiles = async () => {
  loading.value = true
  try {
    const response = await axios.get(`${baseUrl}/file-info/list`, {
      params: {
        page: currentPage.value,
        pageSize: pageSize.value,
        keyword: searchKeyword.value,
        suffix: searchSuffix.value
      }
    })
    
    if (response.data?.code === 200) {
      const data = response.data?.data
      // 将数据库的文件信息转换为文件列表格式
      // 将数据库的文件信息转换为文件列表格式
      const allFiles = data?.list?.map(item => {
        // 从文件URL中提取桶名
        const urlMatch = item.fileUrl?.match(/\/minio\/buckets\/([^\/]+)\/files\/(.+)/)
        const bucketName = urlMatch ? urlMatch[1] : 'unknown'
        
        return {
          ...item,
          bucketName: bucketName,
          objectName: item.fileName || item.originalName, // 使用文件名作为显示名称
          name: item.fileName || item.originalName,
          size: item.fileSize || 0, // 使用数据库中的文件大小信息
          lastModified: item.createTime, // 使用创建时间作为修改时间
          isFile: true, // 数据库中的记录都是文件
          id: item.id, // 添加数据库ID
          fileUrl: item.fileUrl // 添加文件URL
        }
      }) || []
      
      files.value = allFiles
      totalFiles.value = data.total
      
      // 加载完成后更新统计信息
       loadStatistics()
       
       // 按桶组织文件
       organizeFilesByBucket()
       
       ElMessage.success('文件列表加载成功')
    } else {
      ElMessage.error('获取文件列表失败')
    }
  } catch (error) {
    ElMessage.error('获取文件列表失败: ' + (error.response?.data?.msg || error.message))
  } finally {
    loading.value = false
  }
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

// 下载文件（使用文件ID）
const downloadFile = async (file) => {
  try {
    // 从文件URL中提取桶名和对象名
    const urlMatch = file.fileUrl.match(/\/minio\/buckets\/([^\/]+)\/files\/(.+)/)
    if (urlMatch) {
      const bucketName = urlMatch[1]
      const objectName = urlMatch[2]
      
      // 获取预签名下载URL
      const response = await axios.get(
        `${baseUrl}/minio/buckets/${bucketName}/files/${objectName}/presigned-url`
      )
      
      if (response.data?.code === 200) {
        const downloadUrl = response.data?.data
        
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
    } else {
      ElMessage.error('文件URL格式不正确')
    }
  } catch (error) {
    ElMessage.error('下载文件失败: ' + (error.response?.data?.msg || error.message))
  }
}

// 从MinIO中删除文件
const deleteFileFromMinIO = async (fileUrl, fileName) => {
  try {
    // 从文件URL中提取桶名和对象名
    const urlMatch = fileUrl.match(/\/minio\/buckets\/([^\/]+)\/files\/(.+)/)
    if (urlMatch) {
      const bucketName = urlMatch[1]
      const objectName = urlMatch[2]
      
      // 调用后端接口删除MinIO中的文件
      const response = await axios.delete(
        `${baseUrl}/minio/buckets/${bucketName}/files/${objectName}`
      )
      
      if (response.data?.code === 200) {
        console.log(`成功删除MinIO文件: ${fileName}`)
        return true
      } else {
        console.error(`删除MinIO文件失败: ${fileName}`, response.data?.msg)
        return false
      }
    } else {
      console.error('文件URL格式不正确:', fileUrl)
      return false
    }
  } catch (error) {
    console.error('删除MinIO文件出错:', error)
    return false
  }
}

// 删除文件（同时删除数据库和MinIO中的文件）
const deleteFile = async (file) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文件 "${file.objectName}" 吗？删除后无法恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 先从MinIO中删除文件
    const minioDeleteSuccess = await deleteFileFromMinIO(file.fileUrl, file.objectName)
    
    // 再删除数据库记录
    const response = await axios.delete(
      `${baseUrl}/file-info/${file.id}`
    )
    
    if (response.data?.code === 200) {
      if (minioDeleteSuccess) {
        ElMessage.success('文件删除成功，已同时删除MinIO中的文件')
      } else {
        ElMessage.success('文件删除成功，但MinIO中的文件删除失败')
      }
      loadFiles()
    } else {
      ElMessage.error(response.data?.msg || '删除文件失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除文件失败: ' + (error.response?.data?.msg || error.message))
    }
  }
}



// 重置搜索
const resetSearch = () => {
  searchKeyword.value = ''
  searchSuffix.value = ''
  currentPage.value = 1
  loadFiles()
}

// 刷新文件列表
const refreshFiles = () => {
  loadFiles()
  loadStatistics()
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

.file-filter {
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

.file-stats {
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
  padding: 10px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
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

.bucket-stats {
  margin-bottom: 20px;
}

.bucket-stat-item {
  text-align: center;
  padding: 10px;
}

.bucket-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.bucket-count {
  font-size: 14px;
  color: #409EFF;
  margin-bottom: 5px;
}

.bucket-size {
  font-size: 12px;
  color: #909399;
}

.active-bucket {
  border: 2px solid #409EFF;
  background-color: #ecf5ff;
}

/* 按桶分表格样式 */
.bucket-files-container {
  margin-bottom: 20px;
}

.bucket-table-section {
  margin-bottom: 30px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.bucket-table-header {
  background-color: #f8f9fa;
  color: #333;
  padding: 12px 16px;
  border-bottom: 1px solid #e9ecef;
}

.bucket-table-header h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
}

.bucket-table-header .file-count {
  font-size: 13px;
  font-weight: normal;
  color: #6c757d;
}

.bucket-pagination {
  padding: 15px 20px;
  background-color: #fafafa;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: center;
}

.empty-state {
  padding: 40px;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .bucket-table-header {
    padding: 12px 15px;
  }
  
  .bucket-table-header h3 {
    font-size: 14px;
  }
  
  .bucket-pagination {
    padding: 10px;
  }
  
  .bucket-table-section {
    margin-bottom: 20px;
  }
}
</style>