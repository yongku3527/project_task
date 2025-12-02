<template>
  <div class="specification-manager">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-actions">
            <div class="search-bar-inline">
              <el-form :inline="true" size="small">
                <el-form-item label="原材料ID:">
                  <el-input v-model="searchForm.materialId" placeholder="请输入原材料ID" clearable />
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
              <el-button type="primary" @click="handleAdd">
                <el-icon><Plus /></el-icon>
                新增
              </el-button>
            </div>
          </div>
        </div>
      </template>

      <!-- 数据表格 -->
      <el-table 
        v-loading="loading" 
        :data="tableData" 
        stripe 
        border 
        style="width: 100%; table-layout: auto;"
        @selection-change="handleSelectionChange"
        :row-class-name="tableRowClassName"
      >

        <el-table-column type="index" label="序号" width="80" />
        <el-table-column prop="materialId" label="原材料ID" min-width="120" />
        <el-table-column prop="drawingType" label="文件类别" min-width="100" />
        <el-table-column prop="itemName" label="物料名称" min-width="150" />
        <el-table-column prop="model" label="规格型号" min-width="180" />
        <el-table-column prop="fileName" label="文件资料" min-width="200">
          <template #default="scope">
            <el-link 
              v-if="scope.row.fileUrl" 
              type="primary" 
              @click="downloadFile(scope.row.fileUrl, scope.row.fileName)"
              style="word-break: break-all; display: inline-block; max-width: 100%;"
            >
              {{ scope.row.fileName }}
            </el-link>
            <span v-else class="no-file">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <!-- <el-table-column prop="createTime" label="创建时间" min-width="160" /> -->
        <el-table-column prop="updateTime" label="更新时间" min-width="160" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-button type="primary" size="small" link @click="handleEdit(scope.row)">
                <el-icon><Edit /></el-icon>编辑
              </el-button>
              <el-button type="danger" size="small" link @click="handleDelete(scope.row.id)">
                <el-icon><Delete /></el-icon>删除
              </el-button>
              <el-button 
                :type="scope.row.status === 1 ? 'warning' : 'success'" 
                size="small" 
                link 
                @click="handleToggleStatus(scope.row)"
                v-if="scope.row.status !== 2"
              >
                <el-icon><Switch /></el-icon>{{ scope.row.status === 1 ? '停用' : '启用' }}
              </el-button>
              <el-button 
                type="info" 
                size="small" 
                link 
                @click="handleConsumeStatus(scope.row)"
                v-if="scope.row.status !== 2"
              >
                <el-icon><Switch /></el-icon>消耗
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                link 
                @click="handleDisableStatus(scope.row)"
                v-if="scope.row.status === 2"
              >
                <el-icon><Switch /></el-icon>停用
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.title"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="dialog.form"
        :rules="dialog.rules"
        label-width="120px"
      >
        <el-form-item label="原材料ID" prop="materialId">
          <el-input 
            v-model="dialog.form.materialId" 
            placeholder="请输入原材料ID"
            @input="handleMaterialIdInput"
          />
        </el-form-item>
        <el-form-item label="文件类别" prop="drawingType">
          <el-select 
            v-model="dialog.form.drawingType" 
            placeholder="请选择文件类别"
            style="width: 100%"
          >
            <el-option 
              v-for="type in drawingTypes" 
              :key="type.value" 
              :label="type.label" 
              :value="type.value" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="物料名称" prop="itemName">
          <el-input 
            v-model="dialog.form.itemName" 
            placeholder="请输入物料名称"
          />
        </el-form-item>
        <el-form-item label="规格型号" prop="model">
          <el-input 
            v-model="dialog.form.model" 
            placeholder="请输入规格型号"
          />
        </el-form-item>
        <el-form-item label="文件上传">
          <el-upload
            ref="fileUploadRef"
            :action="`/minio/upload/${specificationBucket}`"
            :limit="1"
            :on-success="handleFileUploadSuccess"
            :on-remove="handleFileRemove"
            :file-list="dialog.fileList"
            :before-upload="beforeFileUpload"
            :http-request="handleFileUpload"
            :auto-upload="true"
            accept=".pdf,.doc,.docx,.dwg"
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon>
              选择文件
            </el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持上传PDF、Word、DWG文件，单个文件大小不超过100MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select 
            v-model="dialog.form.status" 
            placeholder="请选择状态"
            style="width: 100%"
          >
            <el-option label="启用" value="1" />
            <el-option label="停用" value="0" />
            <el-option label="消耗" value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialog.visible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Edit, Delete, Upload,
  Switch
} from '@element-plus/icons-vue'
import * as specificationApi from '@/api/specification'
import { http } from '@/utils/request'

// 搜索表单
const searchForm = reactive({
  materialId: '',
  drawingType: '',
  itemName: '',
  model: ''
})

// 表格数据
const tableData = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(50)
const multipleSelection = ref([])

// 文件类别选项
const drawingTypes = ref([
  { label: '规格书', value: '规格书' },
  { label: '原理图', value: '原理图' },
  { label: '装配图', value: '装配图' },
  { label: '其他', value: '其他' }
])

// 对话框
const dialog = reactive({
  visible: false,
  title: '新增规格书',
  form: {
    id: null,
    materialId: '',
    drawingType: '',
    itemName: '',
    model: '',
    fileId: null,
    fileUrl: '',
    fileName: '',
    status: 1
  },
  rules: {
    materialId: [{ required: true, message: '请输入原材料ID', trigger: 'blur' }],
    drawingType: [{ required: true, message: '请选择文件类别', trigger: 'change' }],
    itemName: [{ required: true, message: '请输入物料名称', trigger: 'blur' }],
    model: [{ required: true, message: '请输入规格型号', trigger: 'blur' }],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }]
  },
  fileList: []
})

// 表单引用
const formRef = ref(null)
const fileUploadRef = ref(null)

// 定义存储桶名称
const specificationBucket = ref('specification')

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

// 文件上传前的验证
const beforeFileUpload = (file) => {
  const isLt100M = file.size / 1024 / 1024 < 100
  
  if (!isLt100M) {
    ElMessage.error('上传文件大小不能超过100MB!')
    return false
  }
  
  return true
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
    
    const response = await specificationApi.getSpecificationList(params)
    
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

// 选择变化
const handleSelectionChange = (val) => {
  multipleSelection.value = val
}

// 新增
const handleAdd = () => {
  dialog.title = '新增规格书'
  dialog.form = {
    id: null,
    materialId: '',
    drawingType: '',
    itemName: '',
    model: '',
    fileId: null,
    fileUrl: '',
    fileName: '',
    status: 1
  }
  dialog.fileList = []
  dialog.visible = true
}

// 编辑
const handleEdit = (row) => {
  dialog.title = '编辑规格书'
  dialog.form = {
    id: row.id,
    materialId: row.materialId,
    drawingType: row.drawingType,
    itemName: row.itemName,
    model: row.model,
    fileId: row.fileId,
    fileUrl: row.fileUrl,
    fileName: row.fileName,
    status: row.status
  }
  
  // 设置文件列表
  dialog.fileList = []
  if (row.fileUrl && row.fileName) {
    dialog.fileList.push({
      name: row.fileName,
      url: row.fileUrl
    })
  }
  
  dialog.visible = true
}

// 关闭对话框
const handleDialogClose = () => {
  dialog.form = {
    id: null,
    materialId: '',
    drawingType: '',
    itemName: '',
    model: '',
    fileId: null,
    fileUrl: '',
    fileName: '',
    status: 1
  }
  dialog.fileList = []
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    const data = { ...dialog.form }
    
    let response
    if (data.id) {
      // 更新
      response = await specificationApi.updateSpecification(data)
    } else {
      // 新增
      response = await specificationApi.addSpecification(data)
    }
    
    if (response.code === 200) {
      ElMessage.success(data.id ? '更新成功' : '新增成功')
      dialog.visible = false
      loadData()
    } else {
      ElMessage.error(response.msg || (data.id ? '更新失败' : '新增失败'))
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败: ' + error.message)
  }
}

// 删除
const handleDelete = async (id) => {
  try {
    // 先获取要删除的规格书信息，包括文件URL和ID
    const row = tableData.value.find(item => item.id === id)
    
    await ElMessageBox.confirm('确定要删除这条记录吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 如果有文件，先删除MinIO文件
    if (row && row.fileUrl) {
      await deleteFileFromMinIO(row.fileUrl, '规格书', row.fileId)
    }
    
    const response = await specificationApi.deleteSpecification(id)
    
    if (response.code === 200) {
      ElMessage.success('删除成功，已同时删除关联的文件')
      loadData()
    } else {
      ElMessage.error(response.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败: ' + error.message)
    }
  }
}

// 设置行背景颜色
const tableRowClassName = ({ row }) => {
  switch (row.status) {
    case 0:
      return 'row-disabled' // 停用状态 - 红色背景
    case 1:
      return 'row-enabled' // 启用状态 - 默认背景
    case 2:
      return 'row-consumed' // 消耗状态 - 黄色背景
    default:
      return ''
  }
}

// 切换状态
const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '启用' : '停用'
  
  try {
    await ElMessageBox.confirm(`确定要${statusText}该规格书吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await specificationApi.updateSpecification({
      id: row.id,
      status: newStatus
    })
    
    if (response.code === 200) {
      // 更新本地状态
      row.status = newStatus
      ElMessage.success(`已${statusText}该规格书`)
      
      // 重新加载数据
      await loadData()
    } else {
      ElMessage.error(response.message || `${statusText}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新状态失败:', error)
      ElMessage.error('更新状态失败')
    }
  }
}

// 消耗状态处理
const handleConsumeStatus = async (row) => {
  try {
    await ElMessageBox.confirm('确认将该规格书状态变更为消耗吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await specificationApi.updateSpecification({
      id: row.id,
      status: 2 // 2表示消耗状态
    })
    
    if (response.code === 200) {
      // 更新本地状态
      row.status = 2
      ElMessage.success('状态已变更为消耗')
      
      // 重新加载数据
      await loadData()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新状态失败:', error)
      ElMessage.error('更新状态失败')
    }
  }
}

// 停用状态处理（用于消耗状态切换为停用）
const handleDisableStatus = async (row) => {
  try {
    await ElMessageBox.confirm('确认将该规格书状态变更为停用吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await specificationApi.updateSpecification({
      id: row.id,
      status: 0 // 0表示停用状态
    })
    
    if (response.code === 200) {
      // 更新本地状态
      row.status = 0
      ElMessage.success('状态已变更为停用')
      
      // 重新加载数据
      await loadData()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新状态失败:', error)
      ElMessage.error('更新状态失败')
    }
  }
}

// 更新条目状态
const updateItemStatus = async (item, newStatus) => {
  try {
    const statusText = newStatus === 1 ? '启用' : '停用'
    
    const response = await specificationApi.updateSpecification({
      id: item.id,
      status: newStatus
    })
    
    if (response.code === 200) {
      // 更新本地状态
      item.status = newStatus
      ElMessage.success(`已${statusText}该条目`)
      
      // 重新加载数据
      await loadData()
    } else {
      ElMessage.error(response.message || `${statusText}失败`)
    }
  } catch (error) {
    console.error('更新状态失败:', error)
    ElMessage.error('更新状态失败')
  }
}

// 自定义文件上传
const handleFileUpload = async (options) => {
  const { file, onSuccess, onError, onProgress } = options
  
  try {
    // 获取当前规格书信息用于生成格式化文件名
    const materialId = dialog.form.materialId || 'UNKNOWN'
    let itemName = dialog.form.itemName || '规格书文件'
    
    // 处理物料名称字段，当文本中包含括号时截取括号前的文本
    const leftParenIndex = itemName.search(/[（(]/)
    if (leftParenIndex > -1) {
      itemName = itemName.substring(0, leftParenIndex).trim()
    }
    
    // 第一步：创建格式化文件名预上传任务
    const presignResponse = await http.post(
      `/minio/buckets/${specificationBucket.value}/files/formatted-presigned-upload-for-doc-prod-drawing`,
      null,
      {
        code: materialId,
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
    const saveFileResponse = await http.post(`/minio/buckets/${specificationBucket.value}/files/save-info`, {
      bucketName: specificationBucket.value,
      objectName: formattedFileName,
      originalName: file.name,
      fileSize: file.size,
      contentType: file.type || 'application/octet-stream'
    })
    
    if (saveFileResponse.code !== 200) {
      throw new Error('保存文件信息失败: ' + saveFileResponse.msg)
    }
    
    // 模拟原上传成功回调格式
    const fileUrl = `/minio/buckets/${specificationBucket.value}/files/${encodeURIComponent(formattedFileName)}`
    const mockResponse = {
      code: 200,
      message: '上传成功',
      data: {
        id: saveFileResponse.data?.fileId || null,
        fileUrl: fileUrl,
        fileName: file.name
      }
    }
    
    // 调用原成功处理函数
    handleFileUploadSuccess(mockResponse, file)
    onSuccess(mockResponse)
    
    ElMessage.success(`文件上传成功，文件名: ${formattedFileName}`)
    
  } catch (error) {
    onError(error)
    ElMessage.error('文件上传失败: ' + error.message)
  }
}

// 文件上传成功
const handleFileUploadSuccess = (response, uploadFile) => {
  if (response.code === 200) {
    dialog.form.fileId = response.data.id
    dialog.form.fileUrl = response.data.fileUrl
    dialog.form.fileName = response.data.fileName
    // 更新文件列表
    dialog.fileList = [{
      name: response.data.fileName,
      url: response.data.fileUrl
    }]
  } else {
    ElMessage.error('文件上传失败: ' + response.msg)
  }
}

// 通用文件删除方法（从MinIO和数据库删除文件）
const deleteFileFromMinIO = async (fileUrl, fileType = '规格书', fileId = null) => {
  if (!fileUrl) {
    console.warn(`文件URL为空，跳过${fileType}文件删除`)
    return
  }
  
  try {
    // 从fileUrl中提取桶名称和对象名称
    const urlMatch = fileUrl.match(/\/minio\/buckets\/([^\/]+)\/files\/(.+)/)
    if (!urlMatch) {
      console.error(`无法从${fileType}文件URL中提取桶名称和文件名称:`, fileUrl)
      return
    }
    
    const bucketName = urlMatch[1]
    const objectName = urlMatch[2]
    
    console.log(`提取的${fileType}桶名称:`, bucketName)
    console.log(`提取的${fileType}文件名称:`, objectName)
    
    // 先删除MinIO文件
    try {
      await http.delete(`/minio/buckets/${bucketName}/files/${objectName}`)
      console.log(`${fileType}文件从MinIO删除成功`)
    } catch (minioError) {
      console.warn(`删除${fileType}MinIO文件失败:`, minioError)
      // MinIO删除失败也继续，不抛出错误
    }
    
    // 如果提供了fileId，也删除数据库记录
    if (fileId) {
      try {
        await http.delete(`/file-info/${fileId}`)
        console.log(`${fileType}文件数据库记录删除成功`)
      } catch (dbError) {
        console.warn(`删除${fileType}文件数据库记录失败:`, dbError)
        // 数据库删除失败也继续，不抛出错误
      }
    }
  } catch (error) {
    console.error(`删除${fileType}文件时出错:`, error)
  }
}

// 文件移除
const handleFileRemove = async () => {
  // 如果有文件ID和URL，先删除数据库和MinIO文件
  if (dialog.form.fileId && dialog.form.fileUrl) {
    await deleteFileFromMinIO(dialog.form.fileUrl, '规格书', dialog.form.fileId)
  }
  
  // 重置文件信息
  dialog.form.fileId = null
  dialog.form.fileUrl = ''
  dialog.form.fileName = ''
  dialog.fileList = []
}

// 输入防抖定时器
const inputTimers = {}

// MES接口调用 - 根据原材料ID查询物料信息
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

// 根据原材料ID前N位查询物料类型
const getItemTypeByMaterialId = async (materialId, prefixLength = 6) => {
  try {
    // 调用现有的根据pid查询物料类型的接口，因为逻辑是通用的
    const response = await http.get('/item-type/get-type-by-pid', {
      pid: materialId,
      prefixLength
    })
    return response
  } catch (error) {
    console.warn('获取物料类型失败: ' + error.message)
    throw error
  }
}

// 原材料ID输入处理
const handleMaterialIdInput = (value) => {
  const materialIdValue = value?.trim()
  
  // 清除之前的定时器
  if (inputTimers.materialId) {
    clearTimeout(inputTimers.materialId)
  }
  
  // 设置新的定时器
  inputTimers.materialId = setTimeout(async () => {
    if (materialIdValue) {
      // 调用MES接口根据原材料ID获取物料信息
      const itemInfo = await getItemInfoFromMES(materialIdValue)

      if (itemInfo) {
        dialog.form.itemName = itemInfo.itemName || ''
        dialog.form.model = itemInfo.itemSpec || ''
        ElMessage.success('已自动填充物料名称和规格型号')
      }
      
      // 调用API根据原材料ID前N位查询物料类型
      try {
        const itemTypeResponse = await getItemTypeByMaterialId(materialIdValue, 6)
        if (itemTypeResponse.code === 200) {
          dialog.form.drawingType = itemTypeResponse.data.drawingType || ''
          ElMessage.success('已自动填充文件类别')
        } else {
          // 显示后端返回的错误信息
          ElMessage.warning(itemTypeResponse.msg || '获取文件类别失败')
        }
      } catch (error) {
        console.warn('获取文件类别失败: ' + error.message)
        ElMessage.error('获取文件类别失败: ' + error.message)
      }
    }
    inputTimers.materialId = null
  }, 500) // 500毫秒防抖
}

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    0: 'danger',
    1: 'success',
    2: 'warning'
  }
  return statusMap[status] || 'danger'
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

// 文件下载
const downloadFile = async (url, fileName) => {
  try {
    // 提取存储桶名称和对象名称
    const urlMatch = url.match(/\/minio\/buckets\/([^\/]+)\/files\/(.+)/)
    if (!urlMatch) {
      // 如果不是MinIO URL，直接在新窗口中打开
      window.open(url, '_blank')
      return
    }
    
    const bucketName = urlMatch[1]
    const objectName = urlMatch[2]
    
    // 获取预签名下载URL
    const response = await http.get(
      `/minio/buckets/${bucketName}/files/${objectName}/presigned-url`
    )
    
    if (response.code === 200) {
      const presignedUrl = response.data.presignedUrl
      // 使用预签名URL下载文件
      const link = document.createElement('a')
      link.href = presignedUrl
      link.download = fileName
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    } else {
      ElMessage.error('获取下载链接失败: ' + response.msg)
    }
  } catch (error) {
    console.error('下载文件失败:', error)
    ElMessage.error('下载文件失败: ' + error.message)
  }
}

// 页面加载时初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.specification-manager {
  padding: 20px;

  margin: 0 auto;
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
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-buttons {
  display: flex;
  gap: 10px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.action-buttons {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

.no-file {
  color: #999;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
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

/* 禁用悬停效果变化 */
:deep(.el-table__body tr:hover > td) {
  background-color: inherit !important;
}

:deep(.el-table__body tr.row-disabled:hover > td) {
  background-color: #fff1f0 !important;
}

:deep(.el-table__body tr.row-consumed:hover > td) {
  background-color: #fffbe6 !important;
}

:deep(.el-table__body tr.row-enabled:hover > td) {
  background-color: #ffffff !important;
}
</style>