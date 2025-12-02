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
          <div class="part-search-results">
            <div class="search-results-header">
              <div class="search-results-title">
                <span>搜索结果 (共{{ partIdSearchResults.length }} 条)</span>

                  <span class="stats-item">
                    <el-tag type="success" size="small">
                      启用 {{ getStatusCount(1) }} 条
                    </el-tag>
                  </span>
                  <span class="stats-item">
                    <el-tag type="warning" size="small">
                      消耗 {{ getStatusCount(2) }} 条
                    </el-tag>
                  </span>
                  <span class="stats-item">
                    <el-tag type="danger" size="small">
                      停用 {{ getStatusCount(0) }} 条
                    </el-tag>
                  </span>

              </div>
              <el-button type="text" size="small" @click="clearPartIdSearchResults">
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
            <div class="search-results-list">
              <div 
                v-for="item in partIdSearchResults" 
                :key="item.id"
                class="search-result-item"
                :class="`status-${item.status}`"
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
                <div class="status-actions">
                  <el-button 
                    type="primary" 
                    size="small" 
                    @click.stop="updateItemStatus(item, 1)"
                  >
                    启用
                  </el-button>
                  <el-button 
                    type="warning" 
                    size="small" 
                    @click.stop="updateItemStatus(item, 2)"
                  >
                    消耗
                  </el-button>
                  <el-button 
                    type="danger" 
                    size="small" 
                    @click.stop="updateItemStatus(item, 0)"
                  >
                    停用
                  </el-button>
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
import { http } from '@/utils/request'

// API函数导入
import { 
  getDocPartDrawingList,
  getDocPartDrawingById,
  addDocPartDrawing,
  updateDocPartDrawing,
  deleteDocPartDrawing,
  getDocPartDrawingsByPartId,
  getItemTypeByPartId
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
  
  { label: '其他', value: '其他' }
])

// 定义不同文件类型的存储桶
const dwgBucket = ref('part-dwg')
const pdfBucket = ref('part-pdf')

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

// MES接口调用 - 根据零件编号查询物料信息
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

// 清除零件编号搜索结果
const clearPartIdSearchResults = () => {
  partIdSearchResults.value = []
  ElMessage.info('已清除搜索结果')
}

// 实时搜索零件编号匹配的条目
const searchPartIdItems = async (partIdValue) => {
  try {
    // 调用API搜索匹配的条目
    const response = await getDocPartDrawingList({
      partId: partIdValue,
      page: 1,
      size: 50 // 限制搜索结果数量
    })
    
    if (response.code === 200) {
      let results = response.data.records || response.data || []
      
      // 按照状态排序：启用(1) > 消耗(2) > 停用(0)
      results = results.sort((a, b) => {
        // 启用状态优先级最高
        if (a.status === 1 && b.status !== 1) return -1
        if (b.status === 1 && a.status !== 1) return 1
        
        // 消耗状态优先级其次
        if (a.status === 2 && b.status === 0) return -1
        if (b.status === 2 && a.status === 0) return 1
        
        // 停用状态优先级最低
        return 0
      })
      
      partIdSearchResults.value = results
      console.log(`找到 ${results.length} 个匹配条目`)
    } else {
      partIdSearchResults.value = []
      console.warn('搜索失败:', response.msg)
    }
  } catch (error) {
    partIdSearchResults.value = []
    console.error('搜索失败:', error.message)
  }
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

// 获取状态统计数量
const getStatusCount = (status) => {
  return partIdSearchResults.value.filter(item => item.status === status).length
}

// 获取行样式类名
const getRowClassName = ({ row }) => {
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
        try {
          // 检查响应内容是否为空或无效JSON
          if (!xhr.responseText || xhr.responseText.trim() === '') {
            resolve({ success: true, message: '上传成功' })
            return
          }
          resolve(JSON.parse(xhr.responseText))
        } catch (parseError) {
          console.error('JSON解析错误:', parseError, '响应内容:', xhr.responseText)
          resolve({ success: true, message: '上传成功，但响应格式异常' })
        }
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

// 零件编号输入处理（防抖）
const handlePartIdInput = () => {
  // 清除之前的定时器
  if (inputTimers.partId) {
    clearTimeout(inputTimers.partId)
  }
  
  // 设置新的定时器
  inputTimers.partId = setTimeout(async () => {
    const partIdValue = dialog.form.partId?.trim()
    
    if (partIdValue) {
      // 实时搜索匹配的条目
      await searchPartIdItems(partIdValue)
      
      // 调用MES接口根据零件编号获取物料信息
      const itemInfo = await getItemInfoFromMES(partIdValue)

      if (itemInfo) {
        dialog.form.itemName = itemInfo.itemName || ''
        dialog.form.model = itemInfo.itemSpec ||  ''
        ElMessage.success('已自动填充物料名称和规格型号')
      }
      
      // 调用API根据零件编号前N位查询物料类型
      try {
        const itemTypeResponse = await getItemTypeByPartId(partIdValue, 6)
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
    } else {
      // 清除搜索结果
      partIdSearchResults.value = []
    }
    inputTimers.partId = null
  }, 500) // 减少防抖时间，提高响应速度
}

// 更新条目状态
const updateItemStatus = async (item, newStatus) => {
  try {
    const statusText = newStatus === 1 ? '启用' : newStatus === 2 ? '消耗' : '停用'
    
    const response = await updateDocPartDrawing({
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

// 选择搜索结果
const selectPartIdItem = (item) => {
  // 填充表单数据
  dialog.form.partId = item.partId
  dialog.form.itemName = item.itemName || ''
  dialog.form.model = item.model || ''
  dialog.form.drawingType = item.drawingType || ''
  dialog.form.status = item.status
  
  // 清除搜索结果
  partIdSearchResults.value = []
  
  ElMessage.success('已填充选中条目的数据')
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
      `确定要删除零件编号为"${row.partId}"的图纸吗？\n此操作将一并删除相关的DWG和PDF附件文件。`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 先删除DWG文件
    if (row.dwgFileId && row.dwgFileUrl) {
      await deleteFileFromMinIO(row.dwgFileUrl, 'DWG', row.dwgFileId)
    }
    
    // 再删除PDF文件  
    if (row.pdfFileId && row.pdfFileUrl) {
      await deleteFileFromMinIO(row.pdfFileUrl, 'PDF', row.pdfFileId)
    }
    
    // 最后删除数据库记录
    const response = await deleteDocPartDrawing(row.id)
    if (response.code === 200) {
      ElMessage.success('删除成功，附件已一并删除')
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

// 通用文件删除方法（从MinIO和数据库删除文件）
const deleteFileFromMinIO = async (fileUrl, fileType, fileId = null) => {
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

// DWG文件移除 - 添加删除数据库和MinIO文件的逻辑
const handleDwgUploadRemove = async () => {
  // 如果有文件ID和URL，先删除数据库和MinIO文件
  if (dialog.form.dwgFileId && dialog.form.dwgFileUrl) {
    await deleteFileFromMinIO(dialog.form.dwgFileUrl, 'DWG', dialog.form.dwgFileId)
  }
  
  // 重置文件信息
  dialog.form.dwgFileId = null
  dialog.form.dwgFileUrl = ''
  dialog.form.dwgFileName = ''
}

// PDF文件移除 - 添加删除数据库和MinIO文件的逻辑
const handlePdfUploadRemove = async () => {
  // 如果有文件ID和URL，先删除数据库和MinIO文件
  if (dialog.form.pdfFileId && dialog.form.pdfFileUrl) {
    await deleteFileFromMinIO(dialog.form.pdfFileUrl, 'PDF', dialog.form.pdfFileId)
  }
  
  // 重置文件信息
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
    
    // 第一步：创建格式化文件名预上传任务
    const presignResponse = await http.post(
      `/minio/buckets/${dwgBucket.value}/files/formatted-presigned-upload-for-doc-prod-drawing`,
      null,
      {
        code: partId,
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
    // 获取当前零件图纸信息用于生成格式化文件名
    const partId = dialog.form.partId || 'UNKNOWN'
    let itemName = dialog.form.itemName || 'PDF文件'
    
    // 处理物料名称字段，当文本中包含括号时截取括号前的文本
    const leftParenIndex = itemName.search(/[（(]/)
    if (leftParenIndex > -1) {
      itemName = itemName.substring(0, leftParenIndex).trim()
    }
    
    // 第一步：创建格式化文件名预上传任务
    const presignResponse = await http.post(
      `/minio/buckets/${pdfBucket.value}/files/formatted-presigned-upload-for-doc-prod-drawing`,
      null,
      {
        code: partId,
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

// 下载文件 - 使用预签名链接在新窗口中打开文件
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
      const downloadUrl = response.data
      
      // 在新窗口中打开文件内容，而不是下载
      window.open(downloadUrl, '_blank')
      ElMessage.success('文件下载开始')
    } else {
      ElMessage.error('获取下载链接失败')
    }
  } catch (error) {
    ElMessage.error('下载文件失败: ' + (error.response?.data?.msg || error.message))
  }
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
.part-search-results {
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  max-height: 250px;
  overflow: hidden;
  margin: 8px auto 0;
  z-index: 1000;
  position: relative;
  width: 80%;
  max-width: 600px;
}

.search-results-header {
  padding: 8px 12px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #606266;
}

.search-results-list {
  max-height: 200px;
  overflow-y: auto;
}

.search-result-item {
  padding: 12px;
  cursor: pointer;
  transition: background-color 0.2s;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #f0f2f5;
}

.search-result-item:last-child {
  border-bottom: none;
}

.search-result-item:hover {
  background-color: #f5f7fa;
}

.result-content {
  flex: 1;
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

.status-actions {
  display: flex;
  flex-direction: column;
  gap: 4px;
  /* min-width: 120px; */
  max-width: 120px;
}

/* 搜索结果状态样式 */
.search-result-item.status-0 {
  background-color: #fff1f0;
}

.search-result-item.status-0:hover {
  background-color: #ffebe6;
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