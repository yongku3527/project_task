<template>
  <div class="doc-prod-drawing-manager">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-actions">
            <div class="search-bar-inline">
              <el-form :inline="true" size="small">
                <el-form-item label="成品编号:">
                  <el-input v-model="searchForm.pid" placeholder="请输入成品编号" clearable />
                </el-form-item>
                <el-form-item label="物料名称:">
                  <el-input v-model="searchForm.itemName" placeholder="请输入物料名称" clearable />
                </el-form-item>
                <el-form-item label="规格型号:">
                  <el-input v-model="searchForm.model" placeholder="请输入规格型号" clearable />
                </el-form-item>
                <el-form-item label="文件类别:">
                  <el-select v-model="searchForm.drawingType" placeholder="请选择文件类别" clearable>
                    <el-option label="原理图" value="原理图" />
                    <el-option label="装配图" value="装配图" />
                    <el-option label="零件图" value="零件图" />
                    <el-option label="接线图" value="接线图" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleSearch">查询</el-button>
                  <el-button @click="handleReset">重置</el-button>
                </el-form-item>
              </el-form>
            </div>
            <div class="header-buttons">
              <el-button v-permission="'prodDrawing:add'" type="primary" size="small" @click="handleAdd">
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
          <el-table-column prop="pid" label="成品编号" min-width="70" />
          <el-table-column prop="drawingType" label="文件类别" min-width="50" />
          <el-table-column prop="itemName" label="物料名称" min-width="50" />
          <el-table-column prop="model" label="规格型号" min-width="120" />
          
          <!-- DWG文件列 -->
          <el-table-column label="DWG文件" min-width="150">
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
          <el-table-column label="PDF文件" min-width="150">
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
          
          <el-table-column   label="操作" width="280" fixed="right">
            <template #default="{ row }" >
              <div class="action-buttons" >
                <el-button v-permission="'prodDrawing:update'" type="primary" size="small" link @click="handleEdit(row)">
                  <el-icon><Edit /></el-icon>编辑
                </el-button>
                <el-button v-permission="'prodDrawing:delete'" type="danger" size="small" link @click="handleDelete(row)">
                  <el-icon><Delete /></el-icon>删除
                </el-button>
                <el-button v-permission="'prodDrawing:update'" 
                  :type="row.status === 1 ? 'warning' : 'success'" 
                  size="small" 
                  link 
                  @click="handleToggleStatus(row)"
                >
                  <el-icon><Switch /></el-icon>
                  {{ row.status === 1 ? '禁用' : '启用' }}
                </el-button>
                <el-button v-permission="'prodDrawing:update'" 
                  type="info" 
                  size="small" 
                  link 
                  @click="handleConsumeStatus(row)"
                  v-if="row.status !== 2"
                >
                  <el-icon><Switch /></el-icon>消耗
                </el-button>
                <el-button v-permission="'prodDrawing:update'" 
                  type="danger" 
                  size="small" 
                  link 
                  @click="handleDisableStatus(row)"
                  v-if="row.status === 2"
                >
                  <el-icon><Switch /></el-icon>禁用
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

    <!-- 成品图纸编辑对话框 -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.title"
      width="600px"
    >
      <el-form :model="dialog.form" :rules="dialog.rules" ref="formRef" label-width="100px">
        <el-form-item label="成品编号" prop="pid">
          <el-input 
            v-model="dialog.form.pid" 
            placeholder="请输入成品编号"
            @input="handlePidInput"
          />
        </el-form-item>
        <el-form-item label="文件类别" prop="drawingType">
          <el-input v-model="dialog.form.drawingType" placeholder="请输入文件类别" />
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
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialog.visible = false">取消</el-button>
          <el-button type="primary" @click="save">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Switch, Refresh, Upload } from '@element-plus/icons-vue'
import * as docProdDrawingApi from '@/api/docProdDrawing'
import { http } from '@/utils/request'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 定义不同文件类型的存储桶
const dwgBucket = ref('dwg-files')
const pdfBucket = ref('pdf-files')

// 搜索表单
const searchForm = reactive({
  pid: '',
  itemName: '',
  model: '',
  drawingType: ''
})

// 对话框
const dialog = reactive({
  visible: false,
  title: '',
  form: {
    id: null,
    pid: '',
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
    pid: [{ required: true, message: '请输入成品编号', trigger: 'blur' }],
    drawingType: [{ required: true, message: '请输入文件类别', trigger: 'blur' }],
    itemName: [{ required: true, message: '请输入物料名称', trigger: 'blur' }],
    model: [{ required: true, message: '请输入规格型号', trigger: 'blur' }],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }]
  },
  dwgFileList: [],
  pdfFileList: []
})

// 表单引用
const formRef = ref(null)
const dwgUploadRef = ref(null)
const pdfUploadRef = ref(null)

// 输入防抖定时器
const inputTimers = {}

// 使用预签名URL上传文件
const uploadFileWithPresignedUrl = async (file, presignedUrl, onProgress) => {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest()
    
    // 监听上传进度
    xhr.upload.onprogress = (event) => {
      if (event.lengthComputable) {
        const percentCompleted = Math.round((event.loaded * 100) / event.total)
        onProgress({ percent: percentCompleted })
      }
    }
    
    xhr.onload = () => {
      if (xhr.status === 200) {
        resolve()
      } else {
        reject(new Error(`上传失败: ${xhr.status} ${xhr.statusText}`))
      }
    }
    
    xhr.onerror = () => reject(new Error('网络错误'))
    
    xhr.open('PUT', presignedUrl, true)
    xhr.send(file)
  })
}

// 页面加载时获取数据
onMounted(() => {
  loadData()
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      ...searchForm
    }
    
    // 使用API服务获取数据
    const response = await docProdDrawingApi.getDocProdDrawingList(params)
    
    if (response.code === 200) {
      tableData.value = response.data.records || response.data
      total.value = response.data.total || response.data.length
    } else {
      ElMessage.error(response.msg || '加载数据失败')
    }
  } catch (error) {
    ElMessage.error('加载数据失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 模拟数据
const getMockData = async (params) => {
  // 模拟API延迟
  await new Promise(resolve => setTimeout(resolve, 500))
  
  // 模拟数据
  const mockData = [
    {
      id: 1,
      pid: 'P001',
      drawingType: '原理图',
      itemName: '主控制板',
      model: 'V2.1',
      dwgFileUrl: 'http://example.com/file1.dwg',
      dwgFileName: '主控制板原理图.dwg',
      pdfFileUrl: 'http://example.com/file1.pdf',
      pdfFileName: '主控制板原理图.pdf',
      status: 1,
      createTime: '2024-01-16 14:20:00',
      updateTime: '2024-01-16 14:20:00'
    },
    {
      id: 2,
      pid: 'P002',
      drawingType: '装配图',
      itemName: '电源板',
      model: 'V1.5',
      dwgFileUrl: 'http://example.com/file2.dwg',
      dwgFileName: '电源板装配图.dwg',
      pdfFileUrl: 'http://example.com/file2.pdf',
      pdfFileName: '电源板装配图.pdf',
      status: 1,
      createTime: '2024-01-17 10:30:00',
      updateTime: '2024-01-17 10:30:00'
    },
    {
      id: 3,
      pid: 'P003',
      drawingType: '零件图',
      itemName: '显示模块',
      model: 'V3.0',
      dwgFileUrl: 'http://example.com/file3.dwg',
      dwgFileName: '显示模块零件图.dwg',
      pdfFileUrl: null,
      pdfFileName: null,
      status: 0,
      createTime: '2024-01-18 16:45:00',
      updateTime: '2024-01-18 16:45:00'
    }
  ]
  
  // 简单的过滤逻辑
  let filteredData = mockData.filter(item => {
    return (!params.pid || item.pid.includes(params.pid)) &&
           (!params.itemName || item.itemName.includes(params.itemName)) &&
           (!params.model || item.model.includes(params.model)) &&
           (!params.drawingType || item.drawingType === params.drawingType)
  })
  
  // 分页
  const start = (params.page - 1) * params.size
  const end = start + params.size
  const paginatedData = filteredData.slice(start, end)
  
  return {
    data: paginatedData,
    total: filteredData.length
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

// 重置搜索
const handleReset = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  currentPage.value = 1
  loadData()
}

// 分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  loadData()
}

// 当前页变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  loadData()
}

// 新增
const handleAdd = () => {
  dialog.title = '新增成品图纸'
  dialog.form = {
    id: null,
    pid: '',
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
  dialog.title = '编辑成品图纸'
  dialog.form = {
    id: row.id,
    pid: row.pid,
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
    await ElMessageBox.confirm('确认删除该成品图纸吗？此操作不可恢复。', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 使用API服务删除数据
    const response = await docProdDrawingApi.deleteDocProdDrawing(row.id)
    
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(response.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + error.message)
    }
  }
}

// 切换状态
const handleToggleStatus = async (row) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    const statusText = newStatus === 1 ? '启用' : '禁用'
    
    await ElMessageBox.confirm(`确认${statusText}该成品图纸吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用实际的API
    const response = await docProdDrawingApi.updateDocProdDrawing({
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

// 状态类型处理函数
const getStatusType = (status) => {
  switch (status) {
    case 0:
      return 'danger' // 禁用
    case 1:
      return 'success' // 启用
    case 2:
      return 'warning' // 消耗
    default:
      return 'info'
  }
}

// 状态文本处理函数
const getStatusText = (status) => {
  switch (status) {
    case 0:
      return '禁用'
    case 1:
      return '启用'
    case 2:
      return '消耗'
    default:
      return '未知'
  }
}

// 消耗状态处理
const handleConsumeStatus = async (row) => {
  try {
    await ElMessageBox.confirm('确认将该成品图纸状态变更为消耗吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用实际的API
    const response = await docProdDrawingApi.updateDocProdDrawing({
      id: row.id,
      status: 2 // 2表示消耗状态
    })
    
    if (response.code === 200) {
      row.status = 2
      ElMessage.success('状态已变更为消耗')
    } else {
      ElMessage.error(response.msg || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败: ' + error.message)
    }
  }
}

// 禁用状态处理（从消耗状态变更为禁用）
const handleDisableStatus = async (row) => {
  try {
    await ElMessageBox.confirm('确认将该成品图纸状态从消耗变更为禁用吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用实际的API
    const response = await docProdDrawingApi.updateDocProdDrawing({
      id: row.id,
      status: 0 // 0表示禁用状态
    })
    
    if (response.code === 200) {
      row.status = 0
      ElMessage.success('状态已从消耗变更为禁用')
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
      ? await docProdDrawingApi.updateDocProdDrawing(dialog.form)
      : await docProdDrawingApi.addDocProdDrawing(dialog.form)
    
    if (response.code === 200) {
      ElMessage.success(dialog.form.id ? '更新成功' : '新增成功')
      dialog.visible = false
      loadData()
    } else {
      ElMessage.error(response.msg || '保存失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('保存失败: ' + error.message)
    }
  }
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
    // 获取当前成品图纸信息用于生成格式化文件名
    const pid = dialog.form.pid || 'UNKNOWN'
    let itemName = dialog.form.model || 'DWG文件'
    
    // 处理规格型号字段，当文本中包含括号时截取括号前的文本
    const leftParenIndex = itemName.search(/[（(]/)
    if (leftParenIndex > -1) {
      itemName = itemName.substring(0, leftParenIndex).trim()
    }
    
    // 第一步：创建格式化文件名预上传任务
    const presignResponse = await http.post(
      `/minio/buckets/${dwgBucket.value}/files/formatted-presigned-upload`,
      null,
      {
        code: pid,
        name: itemName,
        originalFileName: file.name,
        fileSize: file.size
      }
    )
    
    if (presignResponse.code !== 200) {
      throw new Error(presignResponse.msg || '创建预上传任务失败')
    }
    
    const { presignedUrl, objectName: formattedFileName } = presignResponse.data
    
    // 第二步：使用预签名URL直接上传文件到MinIO
    await uploadFileWithPresignedUrl(file, presignedUrl, onProgress)
    
    // 第三步：保存文件信息到数据库
    const saveFileResponse = await http.post(`/minio/buckets/${dwgBucket.value}/files/save-info`, {
      bucketName: dwgBucket.value,
      objectName: formattedFileName,
      originalName: file.name,
      fileSize: file.size,
      contentType: file.type || 'application/octet-stream'
    })
    
    if (saveFileResponse.code !== 200) {
      throw new Error('保存文件信息失败: ' + saveFileResponse.msg)
    }
    
    // 模拟原上传成功回调格式
    const fileUrl = `/minio/buckets/${dwgBucket.value}/files/${encodeURIComponent(formattedFileName)}`
    const mockResponse = {
      code: 200,
      message: '上传成功',
      data: {
        id: saveFileResponse.data?.fileId || null,
        url: fileUrl,
        fileName: file.name
      }
    }
    
    // 调用原成功处理函数
    handleDwgUploadSuccess(mockResponse, file)
    onSuccess(mockResponse)
    
    ElMessage.success(`DWG文件上传成功，文件名: ${formattedFileName}`)
    
  } catch (error) {
    onError(error)
    ElMessage.error('DWG文件上传失败: ' + error.message)
  }
}

// 自定义PDF文件上传
const handlePdfUpload = async (options) => {
  const { file, onSuccess, onError, onProgress } = options
  
  try {
    // 获取当前成品图纸信息用于生成格式化文件名
    const pid = dialog.form.pid || 'UNKNOWN'
    let itemName = dialog.form.itemName || 'PDF文件'
    
    // 处理物料名称字段，当文本中包含括号时截取括号前的文本
    const leftParenIndex = itemName.search(/[（(]/)
    if (leftParenIndex > -1) {
      itemName = itemName.substring(0, leftParenIndex).trim()
    }
    
    // 第一步：创建格式化文件名预上传任务
    const presignResponse = await http.post(
      `/minio/buckets/${pdfBucket.value}/files/formatted-presigned-upload`,
      null,
      {
        code: pid,
        name: itemName,
        originalFileName: file.name,
        fileSize: file.size
      }
    )
    
    if (presignResponse.code !== 200) {
      throw new Error(presignResponse.msg || '创建预上传任务失败')
    }
    
    const { presignedUrl, objectName: formattedFileName } = presignResponse.data
    
    // 第二步：使用预签名URL直接上传文件到MinIO
    await uploadFileWithPresignedUrl(file, presignedUrl, onProgress)
    
    // 第三步：保存文件信息到数据库
    const saveFileResponse = await http.post(`/minio/buckets/${pdfBucket.value}/files/save-info`, {
      bucketName: pdfBucket.value,
      objectName: formattedFileName,
      originalName: file.name,
      fileSize: file.size,
      contentType: file.type || 'application/pdf'
    })
    
    if (saveFileResponse.code !== 200) {
      throw new Error('保存文件信息失败: ' + saveFileResponse.msg)
    }
    
    // 模拟原上传成功回调格式
    const fileUrl = `/minio/buckets/${pdfBucket.value}/files/${encodeURIComponent(formattedFileName)}`
    const mockResponse = {
      code: 200,
      message: '上传成功',
      data: {
        id: saveFileResponse.data?.fileId || null,
        url: fileUrl,
        fileName: file.name
      }
    }
    
    // 调用原成功处理函数
    handlePdfUploadSuccess(mockResponse, file)
    onSuccess(mockResponse)
    
    ElMessage.success(`PDF文件上传成功，文件名: ${formattedFileName}`)
    
  } catch (error) {
    onError(error)
    ElMessage.error('PDF文件上传失败: ' + error.message)
  }
}

// 下载文件
const downloadFile = (url, fileName) => {
  // 使用API服务下载文件
  docProdDrawingApi.downloadFile(url, fileName)
}

// MES接口调用 - 根据物料编号查询物料信息
const getItemInfoFromMES = async (itemCode) => {
  try {
    const response = await http.get(`/mes/item-info/${itemCode}`)
    
    if (response.code === 200) {
      return response.data
    } else {
      console.warn('MES物料信息查询失败: ' + response.msg)
      return null
    }
  } catch (error) {
    console.warn('MES接口调用失败: ' + error.message)
    return null
  }
}

// 成品编号输入处理（防抖）
const handlePidInput = () => {
  // 清除之前的定时器
  if (inputTimers.pid) {
    clearTimeout(inputTimers.pid)
  }
  
  // 设置新的定时器
  inputTimers.pid = setTimeout(async () => {
    if (dialog.form.pid?.trim()) {
      // 调用MES接口根据成品编号获取物料信息
      const itemInfo = await getItemInfoFromMES(dialog.form.pid.trim())

      if (itemInfo) {
        dialog.form.itemName = itemInfo.itemName || ''
        dialog.form.model = itemInfo.itemSpec ||  ''
        ElMessage.success('已自动填充物料名称和规格型号')
      }
      
      // 调用API根据成品编号前三位查询物料类型
      try {
        const itemTypeResponse = await docProdDrawingApi.getItemTypeByPid(dialog.form.pid.trim())
        if (itemTypeResponse.code === 200) {
          dialog.form.drawingType = itemTypeResponse.data.drawingType || ''
          ElMessage.success('已自动填充图纸类型')
        } else {
          // 显示后端返回的错误信息
          ElMessage.warning(itemTypeResponse.msg || '获取图纸类型失败')
        }
      } catch (error) {
        console.warn('获取图纸类型失败: ' + error.message)
        ElMessage.error('获取图纸类型失败: ' + error.message)
      }
    }
    inputTimers.pid = null
  }, 1000)
}

// 根据状态获取表格行类名
const getRowClassName = ({ row }) => {
  switch (row.status) {
    case 0:
      return 'row-disabled' // 禁用状态 - 红色背景
    case 1:
      return 'row-enabled' // 启用状态 - 默认背景
    case 2:
      return 'row-consumed' // 消耗状态 - 黄色背景
    default:
      return ''
  }
}
</script>

<style scoped>
.doc-prod-drawing-manager {
  padding: 20px;
  height: calc(100vh - 40px);
  display: flex;
  flex-direction: column;
}

.el-card {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.el-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  flex: 1;
  padding: 20px;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

.header-actions {
  display: flex;
  width: 100%;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.search-bar-inline {
  flex: 1;
  display: flex;
  align-items: center;
}

.header-buttons {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.table-container {
  margin-top: 15px;
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.table-container :deep(.el-table) {
  flex: 1;
  height: 100%;
}

.table-container :deep(.el-table__body-wrapper) {
  flex: 1;
  overflow-y: auto;
}

.file-info {
  display: flex;
  align-items: center;
}

.file-link {
  max-width: 500px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: inline-block;
}

.no-file {
  color: #909399;
  font-style: italic;
}

.action-buttons {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  flex-shrink: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-bar-inline {
    width: 100%;
  }
  
  .header-buttons {
    justify-content: center;
  }
}

/* 状态行背景色 */
:deep(.el-table .row-enabled) {
  background-color: #ffffff !important;
}

:deep(.el-table .row-enabled td) {
  background-color: #ffffff !important;
  border-color: #e2e0df !important;
}

:deep(.el-table .row-disabled) {
  background-color: #fff1f0 !important;
}

:deep(.el-table .row-disabled td) {
  background-color: #fff1f0 !important;
  border-color: #ffa39e !important;
}

:deep(.el-table .row-consumed) {
  background-color: #fffbe6 !important;
}

:deep(.el-table .row-consumed td) {
  background-color: #fffbe6 !important;
  border-color: #ffe58f !important;
}
</style>