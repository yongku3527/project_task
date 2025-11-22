<template>
  <div class="doc-part-drawing-manager">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-actions">
            <div class="search-bar-inline">
              <el-form :inline="true" size="small">
                <el-form-item label="零件编号:">
                  <el-input v-model="searchForm.partId" placeholder="请输入零件编号" clearable />
                </el-form-item>
                <el-form-item label="物料名称:">
                  <el-input v-model="searchForm.itemName" placeholder="请输入物料名称" clearable />
                </el-form-item>
                <el-form-item label="规格型号:">
                  <el-input v-model="searchForm.model" placeholder="请输入规格型号" clearable />
                </el-form-item>
                <el-form-item label="文件类别:">
                  <el-select v-model="searchForm.drawingType" placeholder="请选择文件类别" clearable style="width: 150px;">
                    <el-option 
                      v-for="type in drawingTypes" 
                      :key="type.value" 
                      :label="type.label" 
                      :value="type.value" 
                    />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleSearch">查询</el-button>
                  <el-button @click="handleReset">重置</el-button>
                </el-form-item>
              </el-form>
            </div>
            <div class="header-buttons">
              <el-button v-permission="'partDrawing:add'" type="primary" size="small" @click="handleAdd">
                <el-icon><Plus /></el-icon>
                新增图纸
              </el-button>
              <el-button type="success" size="small" @click="loadData">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </div>
        </div>
      </template>

      <div class="table-container">
        <el-table 
          :data="tableData" 
          v-loading="loading"
          style="width: 100%"
          row-key="id"
          border
          stripe
          :flexible="true"
          :table-layout="'fixed'"
          :row-class-name="getRowClassName"
        >
          <!-- 序号列 -->
          <el-table-column type="index" label="序号" width="40" align="center">
            <template #default="{ $index }">
              {{ $index + 1 }}
            </template>
          </el-table-column>
          
          <el-table-column prop="drawingType" label="文件类别" min-width="70" />
          <el-table-column prop="partId" label="零件编号" min-width="90" />
          
          <el-table-column prop="itemName" label="物料名称" min-width="90" />
          <el-table-column prop="model" label="规格型号" min-width="110" />
          
          <!-- DWG文件列 -->
          <el-table-column label="DWG文件" min-width="120">
            <template #default="{ row }">
              <div v-if="row.dwgFileUrl" class="file-info">
                <el-link 
                  type="primary" 
                  @click="downloadFile(row.dwgFileUrl, row.dwgFileName)"
                  class="file-link"
                >
                  {{ row.dwgFileName }}
                </el-link>
              </div>
              <span v-else class="no-file">无DWG文件</span>
            </template>
          </el-table-column>
          
          <!-- PDF文件列 -->
          <el-table-column label="PDF文件" min-width="120">
            <template #default="{ row }">
              <div v-if="row.pdfFileUrl" class="file-info">
                <el-link 
                  type="primary" 
                  @click="downloadFile(row.pdfFileUrl, row.pdfFileName)"
                  class="file-link"
                >
                  {{ row.pdfFileName }}
                </el-link>
              </div>
              <span v-else class="no-file">无PDF文件</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="createTime" label="创建时间" min-width="100" />
          
          <el-table-column label="操作" width="250" fixed="right">
            <template #default="{ row }" >
              <div class="action-buttons" >
                <el-button v-permission="'partDrawing:update'" type="primary" size="small" link @click="handleEdit(row)">
                  <el-icon><Edit /></el-icon>编辑
                </el-button>
                <el-button v-permission="'partDrawing:delete'" type="danger" size="small" link @click="handleDelete(row)">
                  <el-icon><Delete /></el-icon>删除
                </el-button>
                <el-button v-permission="'partDrawing:update'" 
                  :type="row.status === 1 ? 'warning' : 'success'" 
                  size="small" 
                  link 
                  @click="handleToggleStatus(row)"
                >
                  <el-icon><Switch /></el-icon>
                  {{ row.status === 1 ? '停用' : '启用' }}
                </el-button>
                <el-button v-permission="'partDrawing:update'" 
                  type="info" 
                  size="small" 
                  link 
                  @click="handleConsumeStatus(row)"
                  v-if="row.status !== 2"
                >
                  <el-icon><Switch /></el-icon>消耗
                </el-button>
                <el-button v-permission="'partDrawing:update'" 
                  type="danger" 
                  size="small" 
                  link 
                  @click="handleDisableStatus(row)"
                  v-if="row.status === 2"
                >
                  <el-icon><Switch /></el-icon>停用
                </el-button>
              </div> 
            
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 零件图纸编辑对话框 -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.title"
      width="600px"
      @close="handleCancel"
    >
      <el-form :model="dialog.form" :rules="dialog.rules" ref="formRef" label-width="100px">
        <el-form-item label="零件编号" prop="partId">
          <el-input
            v-model="dialog.form.partId"
            placeholder="请输入零件编号"
            clearable
            @input="handlePartIdInput"
          />
        </el-form-item>
        
        <!-- 零件编号搜索结果 -->
        <el-form-item v-if="partIdSearchResults.length > 0" label-width="0">
          <div class="search-results">
            <div 
              v-for="item in partIdSearchResults" 
              :key="item.id"
              class="search-result-item"
              :class="`status-${item.status}`"
              @click="selectPartIdItem(item)"
            >
              <div class="result-content">
                <div class="result-row">
                  <span class="result-label">零件编号：</span>
                  <span class="result-value">{{ item.partId }}</span>
                </div>
                <div class="result-row">
                  <span class="result-label">物料名称：</span>
                  <span class="result-value">{{ item.itemName }}</span>
                </div>
                <div class="result-row">
                  <span class="result-label">规格型号：</span>
                  <span class="result-value">{{ item.model }}</span>
                </div>
                <div class="result-row">
                  <span class="result-label">状态：</span>
                  <span class="result-value">
                    <el-tag :type="getStatusType(item.status)" size="small">
                      {{ getStatusText(item.status) }}
                    </el-tag>
                  </span>
                </div>
              </div>
            </div>
          </div>
        </el-form-item>
        
        <el-form-item label="文件类别" prop="drawingType">
          <el-select v-model="dialog.form.drawingType" placeholder="请选择文件类别" style="width: 100%;">
            <el-option 
              v-for="type in drawingTypes" 
              :key="type.value" 
              :label="type.label" 
              :value="type.value" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="物料名称" prop="itemName">
          <el-input v-model="dialog.form.itemName" placeholder="请输入物料名称" />
        </el-form-item>
        <el-form-item label="规格型号" prop="model">
          <el-input v-model="dialog.form.model" placeholder="请输入规格型号" />
        </el-form-item>
        <el-form-item label="DWG文件">
          <el-upload
            ref="dwgUploadRef"
            :action="`/minio/upload/${dwgBucket}`"
            :limit="1"
            :on-success="handleDwgUploadSuccess"
            :on-remove="handleDwgUploadRemove"
            :file-list="dialog.dwgFileList"
            :before-upload="beforeDwgUpload"
            :http-request="handleDwgUpload"
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon>选择文件
            </el-button>
            <template #tip>
              <div class="el-upload__tip">
                文件大小不超过50MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="PDF文件">
          <el-upload
            ref="pdfUploadRef"
            :action="`/minio/upload/${pdfBucket}`"
            :limit="1"
            :on-success="handlePdfUploadSuccess"
            :on-remove="handlePdfUploadRemove"
            :file-list="dialog.pdfFileList"
            :before-upload="beforePdfUpload"
            :http-request="handlePdfUpload"
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon>选择PDF文件
            </el-button>
            <template #tip>
              <div class="el-upload__tip">
                文件大小不超过50MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dialog.form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCancel">取消</el-button>
          <el-button type="primary" @click="save">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, 
  Refresh, 
  Edit, 
  Delete, 
  Switch,
  Close,
  Upload
} from '@element-plus/icons-vue'

// API函数导入
import { 
  getDocPartDrawingList,
  getDocPartDrawingById,
  addDocPartDrawing,
  updateDocPartDrawing,
  deleteDocPartDrawing,
  getDocPartDrawingsByPartId
} from '@/api/docPartDrawing'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  partId: '',
  itemName: '',
  model: '',
  drawingType: ''
})

// 文件类别选项
const drawingTypes = ref([
  { label: '工艺图纸', value: '工艺图纸' },
  { label: '装配图纸', value: '装配图纸' },
  { label: '机械图纸', value: '机械图纸' },
  { label: '电气图纸', value: '电气图纸' },
  { label: '其他', value: '其他' }
])

// 定义不同文件类型的存储桶
const dwgBucket = ref('dwg-files')
const pdfBucket = ref('pdf-files')

// 对话框
const dialog = reactive({
  visible: false,
  title: '',
  form: {
    id: null,
    partId: '',
    drawingType: '',
    itemName: '',
    model: '',
    dwgFileId: null,
    dwgFileUrl: '',
    dwgFileName: '',
    pdfFileId: null,
    pdfFileUrl: '',
    pdfFileName: '',
    status: 1
  },
  rules: {
    partId: [{ required: true, message: '请输入零件编号', trigger: 'blur' }],
    drawingType: [{ required: true, message: '请选择文件类别', trigger: 'change' }],
    itemName: [{ required: true, message: '请输入物料名称', trigger: 'blur' }],
    model: [{ required: true, message: '请输入规格型号', trigger: 'blur' }],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }]
  },
  dwgFileList: [],
  pdfFileList: []
})

// 响应式数据
const partIdSearchResults = ref([]) // 零件编号搜索结果

// 表单引用
const formRef = ref(null)
const dwgUploadRef = ref(null)
const pdfUploadRef = ref(null)

// 输入防抖定时器
const inputTimers = {}

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    0: 'info',
    1: 'success', 
    2: 'warning'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusTextMap = {
    0: '停用',
    1: '启用',
    2: '消耗'
  }
  return statusTextMap[status] || '未知'
}

// 获取行样式类名
const getRowClassName = ({ row }) => {
  if (row.status === 0) {
    return 'disabled-row'
  } else if (row.status === 2) {
    return 'consumed-row'
  }
  return ''
}

// 使用预签名URL上传文件
const uploadFileWithPresignedUrl = async (file, presignedUrl, onProgress) => {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest()
    
    // 监听上传进度
    xhr.upload.onprogress = (event) => {
      if (event.lengthComputable) {
        const percentCompleted = Math.round((event.loaded * 100) / event.total)
        onProgress && onProgress(percentCompleted)
      }
    }
    
    xhr.onload = () => {
      if (xhr.status >= 200 && xhr.status < 300) {
        resolve(JSON.parse(xhr.responseText))
      } else {
        reject(new Error(`上传失败: ${xhr.status}`))
      }
    }
    
    xhr.onerror = () => {
      reject(new Error('网络错误'))
    }
    
    xhr.open('PUT', presignedUrl)
    xhr.setRequestHeader('Content-Type', file.type || 'application/octet-stream')
    xhr.send(file)
  })
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      ...searchForm
    }
    
    const response = await getDocPartDrawingList(params)

    if (response.code === 200) {
      const data = response.data
      tableData.value = data.records || []
      total.value = data.total || 0
    } else {
      ElMessage.error(response.message || '加载数据失败')
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.partId = ''
  searchForm.itemName = ''
  searchForm.model = ''
  searchForm.drawingType = ''
  currentPage.value = 1
  loadData()
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  loadData()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadData()
}

// 零件编号输入处理
const handlePartIdInput = (value) => {
  // 清除之前的定时器
  if (inputTimers.partId) {
    clearTimeout(inputTimers.partId)
  }
  
  // 如果为空，清除搜索结果
  if (!value || value.trim() === '') {
    partIdSearchResults.value = []
    return
  }
  
  // 设置新的定时器，延迟500ms后搜索
  inputTimers.partId = setTimeout(async () => {
    try {
      const response = await getDocPartDrawingsByPartId(value.trim())
      if (response.code === 200) {
        partIdSearchResults.value = response.data || []
      }
    } catch (error) {
      console.error('搜索零件编号失败:', error)
    }
  }, 500)
}

// 选择零件编号搜索结果
const selectPartIdItem = (item) => {
  dialog.form.partId = item.partId
  dialog.form.itemName = item.itemName
  dialog.form.model = item.model
  // 清除搜索结果
  partIdSearchResults.value = []
}

// 新增
const handleAdd = () => {
  dialog.title = '新增零件图纸'
  dialog.form = {
    id: null,
    partId: '',
    drawingType: '',
    itemName: '',
    model: '',
    dwgFileId: null,
    dwgFileUrl: '',
    dwgFileName: '',
    pdfFileId: null,
    pdfFileUrl: '',
    pdfFileName: '',
    status: 1
  }
  dialog.dwgFileList = []
  dialog.pdfFileList = []
  dialog.visible = true
}

// 编辑
const handleEdit = (row) => {
  dialog.title = '编辑零件图纸'
  dialog.form = {
    id: row.id,
    partId: row.partId,
    drawingType: row.drawingType,
    itemName: row.itemName,
    model: row.model,
    dwgFileId: row.dwgFileId,
    dwgFileUrl: row.dwgFileUrl,
    dwgFileName: row.dwgFileName,
    pdfFileId: row.pdfFileId,
    pdfFileUrl: row.pdfFileUrl,
    pdfFileName: row.pdfFileName,
    status: row.status
  }
  
  // 设置文件列表
  dialog.dwgFileList = []
  dialog.pdfFileList = []
  
  if (row.dwgFileUrl) {
    dialog.dwgFileList.push({
      name: row.dwgFileName,
      url: row.dwgFileUrl
    })
  }
  
  if (row.pdfFileUrl) {
    dialog.pdfFileList.push({
      name: row.pdfFileName,
      url: row.pdfFileUrl
    })
  }
  
  dialog.visible = true
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除零件编号为"${row.partId}"的图纸吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await deleteDocPartDrawing(row.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 切换状态
const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '启用' : '停用'
  
  try {
    await ElMessageBox.confirm(`确定要${statusText}该零件图纸吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用实际的API
    const response = await updateDocPartDrawing({
      id: row.id,
      status: newStatus
    })
    
    if (response.code === 200) {
      row.status = newStatus
      ElMessage.success(`${statusText}成功`)
    } else {
      ElMessage.error(response.msg || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败: ' + error.message)
    }
  }
}

// 消耗状态
const handleConsumeStatus = async (row) => {
  try {
    await ElMessageBox.confirm('确定要将该零件图纸标记为消耗吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用实际的API
    const response = await updateDocPartDrawing({
      id: row.id,
      status: 2
    })
    
    if (response.code === 200) {
      row.status = 2
      ElMessage.success('状态已从启用变更为消耗')
    } else {
      ElMessage.error(response.msg || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败: ' + error.message)
    }
  }
}

// 停用状态
const handleDisableStatus = async (row) => {
  try {
    await ElMessageBox.confirm('确定要将该零件图纸从消耗状态变更为停用吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用实际的API
    const response = await updateDocPartDrawing({
      id: row.id,
      status: 0
    })
    
    if (response.code === 200) {
      row.status = 0
      ElMessage.success('状态已从消耗变更为停用')
    } else {
      ElMessage.error(response.msg || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败: ' + error.message)
    }
  }
}

// 保存
const save = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    // 使用API服务保存数据
    const response = dialog.form.id 
      ? await updateDocPartDrawing(dialog.form)
      : await addDocPartDrawing(dialog.form)
    
    if (response.code === 200) {
      ElMessage.success(dialog.form.id ? '更新成功' : '新增成功')
      // 清除搜索结果
      partIdSearchResults.value = []
      dialog.visible = false
      loadData()
    } else {
      ElMessage.error(response.msg || '保存失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('保存失败: ' + error.message)
    }
  } finally {
    // 不管成功失败都清除搜索结果
    partIdSearchResults.value = []
  }
}

// 取消
const handleCancel = () => {
  // 清除搜索结果
  partIdSearchResults.value = []
  dialog.visible = false
}

// DWG文件上传前的验证
const beforeDwgUpload = (file) => {
  const isLt50M = file.size / 1024 / 1024 < 50
  
  if (!isLt50M) {
    ElMessage.error('上传文件大小不能超过50MB!')
    return false
  }
  
  return true
}

// PDF文件上传前的验证
const beforePdfUpload = (file) => {
  const isLt50M = file.size / 1024 / 1024 < 50
  
  if (!isLt50M) {
    ElMessage.error('上传文件大小不能超过50MB!')
    return false
  }
  
  return true
}

// DWG文件上传成功
const handleDwgUploadSuccess = (response, file) => {
  dialog.form.dwgFileId = response.data.id
  dialog.form.dwgFileUrl = response.data.url
  dialog.form.dwgFileName = file.name
}

// PDF文件上传成功
const handlePdfUploadSuccess = (response, file) => {
  dialog.form.pdfFileId = response.data.id
  dialog.form.pdfFileUrl = response.data.url
  dialog.form.pdfFileName = file.name
}

// DWG文件移除
const handleDwgUploadRemove = () => {
  dialog.form.dwgFileId = null
  dialog.form.dwgFileUrl = ''
  dialog.form.dwgFileName = ''
}

// PDF文件移除
const handlePdfUploadRemove = () => {
  dialog.form.pdfFileId = null
  dialog.form.pdfFileUrl = ''
  dialog.form.pdfFileName = ''
}

// 自定义DWG文件上传
const handleDwgUpload = async (options) => {
  const { file, onSuccess, onError, onProgress } = options
  
  try {
    // 获取当前零件图纸信息用于生成格式化文件名
    const partId = dialog.form.partId || 'UNKNOWN'
    let itemName = dialog.form.model || 'DWG文件'
    
    // 处理规格型号字段，当文本中包含括号时截取括号前的文本
    const leftParenIndex = itemName.search(/[（(]/)
    if (leftParenIndex > -1) {
      itemName = itemName.substring(0, leftParenIndex).trim()
    }
    
    // 模拟成功响应（实际项目中需要调用真实的MinIO API）
    const mockResponse = {
      code: 200,
      message: '上传成功',
      data: {
        id: Math.floor(Math.random() * 1000),
        url: `/minio/buckets/${dwgBucket.value}/files/${encodeURIComponent(file.name)}`,
        fileName: file.name
      }
    }
    
    // 调用成功处理函数
    handleDwgUploadSuccess(mockResponse, file)
    onSuccess(mockResponse)
    
    ElMessage.success(`DWG文件上传成功，文件名: ${file.name}`)
    
  } catch (error) {
    onError(error)
    ElMessage.error('DWG文件上传失败: ' + error.message)
  }
}

// 自定义PDF文件上传
const handlePdfUpload = async (options) => {
  const { file, onSuccess, onError, onProgress } = options
  
  try {
    // 获取当前零件图纸信息用于生成格式化文件名
    const partId = dialog.form.partId || 'UNKNOWN'
    let itemName = dialog.form.itemName || 'PDF文件'
    
    // 处理物料名称字段，当文本中包含括号时截取括号前的文本
    const leftParenIndex = itemName.search(/[（(]/)
    if (leftParenIndex > -1) {
      itemName = itemName.substring(0, leftParenIndex).trim()
    }
    
    // 模拟成功响应（实际项目中需要调用真实的MinIO API）
    const mockResponse = {
      code: 200,
      message: '上传成功',
      data: {
        id: Math.floor(Math.random() * 1000),
        url: `/minio/buckets/${pdfBucket.value}/files/${encodeURIComponent(file.name)}`,
        fileName: file.name
      }
    }
    
    // 调用成功处理函数
    handlePdfUploadSuccess(mockResponse, file)
    onSuccess(mockResponse)
    
    ElMessage.success(`PDF文件上传成功，文件名: ${file.name}`)
    
  } catch (error) {
    onError(error)
    ElMessage.error('PDF文件上传失败: ' + error.message)
  }
}

// 下载文件
const downloadFile = (url, fileName) => {
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.doc-part-drawing-manager {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.search-bar-inline {
  flex: 1;
}

.header-buttons {
  margin-left: 20px;
}

.table-container {
  margin-top: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.file-info {
  max-width: 200px;
}

.file-link {
  word-break: break-all;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.no-file {
  color: #999;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

/* 状态行样式 */
:deep(.disabled-row) {
  background-color: #f5f7fa;
  opacity: 0.6;
}

:deep(.consumed-row) {
  background-color: #fff7e6;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-bar-inline {
    margin-bottom: 10px;
  }
  
  .header-buttons {
    margin-left: 0;
    text-align: center;
  }
}

/* 搜索结果样式 */
.search-results {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: #fff;
  margin-top: 5px;
}

.search-result-item {
  padding: 10px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

.search-result-item:last-child {
  border-bottom: none;
}

.search-result-item:hover {
  background-color: #f5f7fa;
}

.result-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.result-row {
  display: flex;
  align-items: center;
}

.result-label {
  font-weight: 500;
  color: #606266;
  min-width: 80px;
}

.result-value {
  color: #303133;
}

/* 搜索结果状态样式 */
.search-result-item.status-0 {
  background-color: #f0f9ff;
}

.search-result-item.status-0:hover {
  background-color: #e6f7ff;
}

.search-result-item.status-1 {
  background-color: #f0f9ff;
}

.search-result-item.status-1:hover {
  background-color: #e6f7ff;
}

.search-result-item.status-2 {
  background-color: #fffbe6;
}

.search-result-item.status-2:hover {
  background-color: #fff1b8;
}

/* 对话框样式 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 文件上传样式 */
:deep(.el-upload__tip) {
  color: #909399;
  font-size: 12px;
  line-height: 1.4;
  margin-top: 5px;
}

:deep(.el-upload-list) {
  margin-top: 10px;
}

:deep(.el-upload-list__item) {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 8px;
  margin-bottom: 5px;
}

:deep(.el-upload-list__item-name) {
  color: #606266;
}

:deep(.el-upload-list__item-status-label) {
  color: #67c23a;
}

/* 表格内文件信息样式 */
.table-file-info {
  max-width: 200px;
  overflow: hidden;
}

.table-file-link {
  color: #409eff;
  text-decoration: none;
  white-space: nowrap;
  overflow: hidden;
  text-overment: ellipsis;
  display: inline-block;
}

.table-file-link:hover {
  text-decoration: underline;
  color: #66b1ff;
}

.table-file-download {
  color: #909399;
  font-size: 12px;
  margin-left: 5px;
}

/* 表单验证样式 */
:deep(.el-form-item__error) {
  color: #f56c6c;
  font-size: 12px;
  line-height: 1;
  padding-top: 4px;
  position: absolute;
  top: 100%;
  left: 0;
}

/* 对话框宽度调整 */
:deep(.el-dialog) {
  max-width: 90%;
}

:deep(.el-dialog__body) {
  padding: 20px;
}

/* 表单布局优化 */
:deep(.el-form-item) {
  margin-bottom: 18px;
}

:deep(.el-form-item__label) {
  color: #606266;
  font-weight: 500;
}

/* 输入框组样式 */
.input-with-select {
  display: flex;
  gap: 10px;
}

.input-with-select .el-input {
  flex: 1;
}

/* 状态标签样式 */
.status-tag {
  margin-right: 5px;
}

/* 加载状态样式 */
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

/* 按钮组样式优化 */
.button-group {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.button-group .el-button {
  margin: 0;
}

/* 搜索栏样式优化 */
.search-bar {
  display: flex;
  gap: 15px;
  align-items: flex-end;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  gap: 15px;
  align-items: flex-end;
  flex-wrap: wrap;
}

.search-form-item {
  min-width: 200px;
}

.search-form-item .el-form-item__content {
  width: 100%;
}

/* 卡片头部样式 */
.card-header {
  margin-bottom: 20px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

/* 表格容器样式 */
.table-wrapper {
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.table-wrapper :deep(.el-table) {
  border: none;
}

.table-wrapper :deep(.el-table th) {
  background-color: #fafafa;
  color: #606266;
  font-weight: 600;
}
</style>