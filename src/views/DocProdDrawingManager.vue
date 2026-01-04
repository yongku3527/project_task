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
              <el-button v-permission="'prodDrawing:add'" type="primary" size="small" @click="handleAdd">
                <el-icon><Plus /></el-icon>
                新增图纸
              </el-button>
              <el-button v-permission="'prodDrawing:add'" type="success" size="small" @click="handleBatchAdd">
                <el-icon><Plus /></el-icon>
                多条新建
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
          <el-table-column type="index" label="序号" width="70" align="center">
            <template #default="{ $index }">
              {{ $index + 1 }}
            </template>
          </el-table-column>
          
          <el-table-column prop="drawingType" label="产品类别" min-width="70" />
          <el-table-column prop="pid" label="成品编号" min-width="60" />
          
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
          
          <el-table-column   label="操作" width="250" fixed="right">
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
                  {{ row.status === 1 ? '停用' : '启用' }}
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

    <!-- 成品图纸编辑对话框 -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.title"
      width="600px"
      @close="handleCancel"
    >
      <el-form :model="dialog.form" :rules="dialog.rules" ref="formRef" label-width="100px">
        <el-form-item label="成品编号" prop="pid">
          <el-input
            v-model="dialog.form.pid"
            placeholder="请输入成品编号"
            clearable
            @input="handlePidInput"
            style="width: 100%;"
          />
          <!-- 搜索结果展示 -->
          <div v-if="pidSearchResults.length > 0" class="pid-search-results">
            <div class="search-results-header">
              <div class="search-results-title">
                <span>搜索结果 (共{{ pidSearchResults.length }} 条)</span>
                
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
              <el-button type="text" size="small" @click="clearPidSearchResults">
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
            <div class="search-results-list">
              <div 
                v-for="item in pidSearchResults" 
                :key="item.id"
                :class="`search-result-item status-${item.status}`"
              >
                <div class="result-main">
                  <div class="result-pid">{{ item.pid }}</div>
                  <div class="result-item-name">{{ item.itemName }}</div>
                  <div class="result-model">{{ item.model }}</div>
                </div>
                <div class="result-status">
                  <el-tag :type="getStatusType(item.status)" size="small">
                    {{ getStatusText(item.status) }}
                  </el-tag>
                  <div class="status-actions">
                    <el-button 
                      v-if="item.status === 0" 
                      type="primary" 
                      size="small" 
                      @click="updateItemStatus(item, 1)"
                    >
                      启用
                    </el-button>
                    <el-button 
                      v-if="item.status === 1" 
                      type="warning" 
                      size="small" 
                      @click="updateItemStatus(item, 2)"
                    >
                      消耗
                    </el-button>
                    <el-button 
                      v-if="item.status === 2" 
                      type="primary" 
                      size="small" 
                      @click="updateItemStatus(item, 0)"
                    >
                      停用
                    </el-button>
                  </div>
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
                文件大小不超过100MB
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
                文件大小不超过100MB
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

    <!-- 多条新建对话框 -->
    <el-dialog
      v-model="batchDialog.visible"
      :title="batchDialog.title"
      width="1200px"
      @close="handleBatchDialogClose"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <div class="batch-form-container">
        <div class="batch-header">
          <el-button 
            type="primary" 
            size="small" 
            @click="addBatchItem"
            style="margin-bottom: 20px;"
          >
            <el-icon><Plus /></el-icon>
            添加一行
          </el-button>
          <el-button 
            type="danger" 
            size="small" 
            @click="removeSelectedBatchItems"
            :disabled="!selectedBatchItems.length"
            style="margin-bottom: 20px; margin-left: 10px;"
          >
            <el-icon><Delete /></el-icon>
            删除选中行
          </el-button>
        </div>
        <el-scrollbar style="height: 500px;">
          <el-table 
            :data="batchDialog.formList" 
            border 
            size="small"
            style="width: 100%;"
            @selection-change="handleBatchSelectionChange"
            :row-key="(row, index) => index"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column label="序号" type="index" width="60" />
            
            <el-table-column prop="pid" label="成品编号" min-width="120">
              <template #default="scope">
                <el-input 
                  v-model="batchDialog.formList[scope.$index].pid" 
                  placeholder="请输入"
                  size="small"
                  @input="handleBatchPidInput(scope.$index)"
                />
              </template>
            </el-table-column>
            
            <el-table-column prop="drawingType" label="文件类别" min-width="120">
              <template #default="scope">
                <el-select 
                  v-model="batchDialog.formList[scope.$index].drawingType" 
                  placeholder="请选择"
                  style="width: 100%;"
                  size="small"
                >
                  <el-option 
                    v-for="type in drawingTypes" 
                    :key="type.value" 
                    :label="type.label" 
                    :value="type.value" 
                  />
                </el-select>
              </template>
            </el-table-column>
            
            <el-table-column prop="itemName" label="物料名称" min-width="120">
              <template #default="scope">
                <el-input 
                  v-model="batchDialog.formList[scope.$index].itemName" 
                  placeholder="请输入"
                  size="small"
                />
              </template>
            </el-table-column>
            
            <el-table-column prop="model" label="规格型号" min-width="120">
              <template #default="scope">
                <el-input 
                  v-model="batchDialog.formList[scope.$index].model" 
                  placeholder="请输入"
                  size="small"
                />
              </template>
            </el-table-column>
            
            <el-table-column prop="dwgFileName" label="DWG文件" min-width="200">
              <template #default="scope">
                <div class="file-upload-cell">
                  <el-upload
                    :action="`/minio/upload/${dwgBucket}`"
                    :limit="1"
                    :on-success="(response, file) => handleBatchDwgUploadSuccess(response, file, scope.$index)"
                    :on-remove="() => handleBatchDwgFileRemove(scope.$index)"
                    :file-list="batchDialog.formList[scope.$index].dwgFileList"
                    :before-upload="beforeDwgUpload"
                    :http-request="(options) => handleBatchDwgUpload(options, scope.$index)"
                    :auto-upload="true"
                    :show-file-list="false"
                  >
                    <el-button type="primary" size="small" v-if="!batchDialog.formList[scope.$index].dwgFileName">
                      <el-icon><Upload /></el-icon>
                      选择DWG
                    </el-button>
                    <el-link 
                      v-else 
                      type="primary" 
                      :underline="false"
                      size="small"
                    >
                      {{ batchDialog.formList[scope.$index].dwgFileName }}
                    </el-link>
                  </el-upload>
                  <el-button 
                    type="danger" 
                    size="small" 
                    icon="Delete" 
                    v-if="batchDialog.formList[scope.$index].dwgFileName"
                    @click.stop="handleBatchDwgFileRemove(scope.$index)"
                    style="margin-left: 5px;"
                  />
                </div>
              </template>
            </el-table-column>
            
            <el-table-column prop="pdfFileName" label="PDF文件" min-width="200">
              <template #default="scope">
                <div class="file-upload-cell">
                  <el-upload
                    :action="`/minio/upload/${pdfBucket}`"
                    :limit="1"
                    :on-success="(response, file) => handleBatchPdfUploadSuccess(response, file, scope.$index)"
                    :on-remove="() => handleBatchPdfFileRemove(scope.$index)"
                    :file-list="batchDialog.formList[scope.$index].pdfFileList"
                    :before-upload="beforePdfUpload"
                    :http-request="(options) => handleBatchPdfUpload(options, scope.$index)"
                    :auto-upload="true"
                    :show-file-list="false"
                  >
                    <el-button type="primary" size="small" v-if="!batchDialog.formList[scope.$index].pdfFileName">
                      <el-icon><Upload /></el-icon>
                      选择PDF
                    </el-button>
                    <el-link 
                      v-else 
                      type="primary" 
                      :underline="false"
                      size="small"
                    >
                      {{ batchDialog.formList[scope.$index].pdfFileName }}
                    </el-link>
                  </el-upload>
                  <el-button 
                    type="danger" 
                    size="small" 
                    icon="Delete" 
                    v-if="batchDialog.formList[scope.$index].pdfFileName"
                    @click.stop="handleBatchPdfFileRemove(scope.$index)"
                    style="margin-left: 5px;"
                  />
                </div>
              </template>
            </el-table-column>
            
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="scope">
                <el-button 
                  type="danger" 
                  size="small" 
                  icon="Delete"
                  @click="removeBatchItem(scope.$index)"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-scrollbar>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="batchDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="handleBatchSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Switch, Refresh, Upload, Close } from '@element-plus/icons-vue'
import * as docProdDrawingApi from '@/api/docProdDrawing'
import * as itemTypeApi from '@/api/itemType'
import { http } from '@/utils/request'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(50)
const total = ref(0)
const drawingTypes = ref([]) // 文件类别选项

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

// 多条新建对话框
const batchDialog = reactive({
  visible: false,
  title: '多条新建成品图纸',
  formList: [{
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
    status: 1,
    dwgFileList: [],
    pdfFileList: []
  }]
})

// 选中的批量项
const selectedBatchItems = ref([])

// 批量选择变化
const handleBatchSelectionChange = (selection) => {
  selectedBatchItems.value = selection
}

// 响应式数据
const pidSearchResults = ref([]) // 成品编号搜索结果

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
  loadDrawingTypes()
})

// 加载文件类别选项
const loadDrawingTypes = async () => {
  try {
    const response = await itemTypeApi.getItemTypeList({ page: 1, size: 1000 })
    if (response.code === 200) {
      // 提取所有物料类型的typeName作为文件类别选项
      drawingTypes.value = response.data.records.map(item => ({
        label: item.typeName,
        value: item.typeName
      }))
    }
  } catch (error) {
    console.error('加载文件类别失败:', error)
  }
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

// 多条新增
const handleBatchAdd = () => {
  batchDialog.title = '多条新建成品图纸'
  batchDialog.formList = [{
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
    status: 1,
    dwgFileList: [],
    pdfFileList: []
  }]
  batchDialog.visible = true
}

// 添加批量项
const addBatchItem = () => {
  batchDialog.formList.push({
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
    status: 1,
    dwgFileList: [],
    pdfFileList: []
  })
}

// 删除批量项
const removeBatchItem = async (index) => {
  // 如果有DWG文件，先删除文件
  const item = batchDialog.formList[index]
  if (item.dwgFileId && item.dwgFileUrl) {
    await deleteFileFromMinIO(item.dwgFileUrl, 'DWG', item.dwgFileId)
  }
  // 如果有PDF文件，先删除文件
  if (item.pdfFileId && item.pdfFileUrl) {
    await deleteFileFromMinIO(item.pdfFileUrl, 'PDF', item.pdfFileId)
  }
  batchDialog.formList.splice(index, 1)
}

// 删除选中行
const removeSelectedBatchItems = async () => {
  // 按索引从大到小删除，避免索引错乱
  const indexes = selectedBatchItems.value.map(item => batchDialog.formList.indexOf(item)).sort((a, b) => b - a)
  
  for (const index of indexes) {
    // 如果有DWG文件，先删除文件
    const item = batchDialog.formList[index]
    if (item.dwgFileId && item.dwgFileUrl) {
      await deleteFileFromMinIO(item.dwgFileUrl, 'DWG', item.dwgFileId)
    }
    // 如果有PDF文件，先删除文件
    if (item.pdfFileId && item.pdfFileUrl) {
      await deleteFileFromMinIO(item.pdfFileUrl, 'PDF', item.pdfFileId)
    }
    batchDialog.formList.splice(index, 1)
  }
  
  // 清空选择
  selectedBatchItems.value = []
}

// 批量新增表单的成品编号输入处理（防抖）
const handleBatchPidInput = (index) => {
  // 清除之前的定时器
  if (inputTimers[`batchPid_${index}`]) {
    clearTimeout(inputTimers[`batchPid_${index}`])
  }
  
  // 设置新的定时器
  inputTimers[`batchPid_${index}`] = setTimeout(async () => {
    const pidValue = batchDialog.formList[index].pid?.trim()
    const currentItem = batchDialog.formList[index]
    
    if (pidValue) {
      // 检查当前批次中是否有重复的成品编号（仅检查启用状态）
      const duplicateInBatch = batchDialog.formList.some((item, idx) => {
        return idx !== index && item.pid?.trim() === pidValue && item.status === 1
      })
      
      if (duplicateInBatch) {
        ElMessage.warning(`第 ${index + 1} 行：成品编号 "${pidValue}" 在当前批次中已存在（启用状态）`)
      }
      
      // 实时搜索数据库中是否有重复的成品编号
      await searchPidItems(pidValue)
      
      // 检查数据库中是否有启用状态的重复记录
      const hasEnabledDuplicate = pidSearchResults.value.some(item => item.status === 1)
      if (hasEnabledDuplicate) {
        ElMessage.warning(`第 ${index + 1} 行：成品编号 "${pidValue}" 在数据库中已存在（启用状态）`)
      }
      
      // 调用MES接口根据成品编号获取物料信息
      const itemInfo = await getItemInfoFromMES(pidValue)

      if (itemInfo) {
        currentItem.itemName = itemInfo.itemName || ''
        currentItem.model = itemInfo.itemSpec || ''
        ElMessage.success(`第 ${index + 1} 行：已自动填充物料名称和规格型号`)
      }
      
      // 调用API根据成品编号前N位查询物料类型
      try {
        const itemTypeResponse = await docProdDrawingApi.getItemTypeByPid(pidValue, 3)
        if (itemTypeResponse.code === 200) {
          currentItem.drawingType = itemTypeResponse.data.drawingType || ''
          ElMessage.success(`第 ${index + 1} 行：已自动填充文件类别`)
        }
      } catch (error) {
        console.warn(`第 ${index + 1} 行：获取文件类别失败: ` + error.message)
      }
    }
    inputTimers[`batchPid_${index}`] = null
  }, 500) // 500ms防抖
}

// 批量提交表单
const handleBatchSubmit = async () => {
  try {
    let successCount = 0
    let failCount = 0
    const validItems = []
    
    // 验证所有字段
    for (let i = 0; i < batchDialog.formList.length; i++) {
      const item = batchDialog.formList[i]
      let isValid = true
      
      // 验证必填字段
      if (!item.pid) {
        ElMessage.error(`第 ${i + 1} 行：成品编号不能为空`)
        isValid = false
      }
      if (!item.drawingType) {
        ElMessage.error(`第 ${i + 1} 行：文件类别不能为空`)
        isValid = false
      }
      if (!item.itemName) {
        ElMessage.error(`第 ${i + 1} 行：物料名称不能为空`)
        isValid = false
      }
      if (!item.model) {
        ElMessage.error(`第 ${i + 1} 行：规格型号不能为空`)
        isValid = false
      }
      if (!item.status) {
        ElMessage.error(`第 ${i + 1} 行：状态不能为空`)
        isValid = false
      }
      
      if (isValid) {
        validItems.push(item)
      } else {
        failCount++
      }
    }
    
    // 如果没有有效记录，直接返回
    if (validItems.length === 0) {
      return
    }
    
    // 使用批量接口提交数据
    // 这里假设后端有批量保存接口，如果没有，需要遍历调用单个保存接口
    for (const item of validItems) {
      try {
        const data = { ...item }
        const response = await docProdDrawingApi.addDocProdDrawing(data)
        if (response.code === 200) {
          successCount++
        } else {
          failCount++
          ElMessage.error(`提交失败: ${response.msg || '未知错误'}`)
        }
      } catch (error) {
        failCount++
        ElMessage.error(`提交失败: ${error.message}`)
      }
    }
    
    // 显示提交结果
    if (successCount > 0) {
      ElMessage.success(`成功提交 ${successCount} 条记录`)
      batchDialog.visible = false
      loadData()
    }
    if (failCount > 0) {
      ElMessage.error(`失败 ${failCount} 条记录`)
    }
    
    // 如果有成功记录，关闭对话框并重新加载数据
    if (successCount > 0) {
      batchDialog.visible = false
      loadData()
    }
    
  } catch (error) {
    console.error('批量提交失败:', error)
    ElMessage.error('批量提交失败: ' + error.message)
  }
}

// 批量对话框关闭
const handleBatchDialogClose = () => {
  batchDialog.formList = [{
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
    status: 1,
    dwgFileList: [],
    pdfFileList: []
  }]
  selectedBatchItems.value = []
}

// 批量DWG文件上传成功
const handleBatchDwgUploadSuccess = (response, file, index) => {
  if (response.code === 200) {
    batchDialog.formList[index].dwgFileId = response.data.id
    batchDialog.formList[index].dwgFileUrl = response.data.fileUrl
    batchDialog.formList[index].dwgFileName = response.data.fileName
    // 更新文件列表
    batchDialog.formList[index].dwgFileList = [{
      name: response.data.fileName,
      url: response.data.fileUrl
    }]
  } else {
    ElMessage.error('DWG文件上传失败: ' + response.msg)
  }
}

// 批量PDF文件上传成功
const handleBatchPdfUploadSuccess = (response, file, index) => {
  if (response.code === 200) {
    batchDialog.formList[index].pdfFileId = response.data.id
    batchDialog.formList[index].pdfFileUrl = response.data.fileUrl
    batchDialog.formList[index].pdfFileName = response.data.fileName
    // 更新文件列表
    batchDialog.formList[index].pdfFileList = [{
      name: response.data.fileName,
      url: response.data.fileUrl
    }]
  } else {
    ElMessage.error('PDF文件上传失败: ' + response.msg)
  }
}

// 批量DWG文件删除
const handleBatchDwgFileRemove = async (index) => {
  // 如果有文件ID和URL，先删除数据库和MinIO文件
  if (batchDialog.formList[index].dwgFileId && batchDialog.formList[index].dwgFileUrl) {
    await deleteFileFromMinIO(batchDialog.formList[index].dwgFileUrl, 'DWG', batchDialog.formList[index].dwgFileId)
  }
  
  // 重置文件信息
  batchDialog.formList[index].dwgFileId = null
  batchDialog.formList[index].dwgFileUrl = ''
  batchDialog.formList[index].dwgFileName = ''
  batchDialog.formList[index].dwgFileList = []
}

// 批量PDF文件删除
const handleBatchPdfFileRemove = async (index) => {
  // 如果有文件ID和URL，先删除数据库和MinIO文件
  if (batchDialog.formList[index].pdfFileId && batchDialog.formList[index].pdfFileUrl) {
    await deleteFileFromMinIO(batchDialog.formList[index].pdfFileUrl, 'PDF', batchDialog.formList[index].pdfFileId)
  }
  
  // 重置文件信息
  batchDialog.formList[index].pdfFileId = null
  batchDialog.formList[index].pdfFileUrl = ''
  batchDialog.formList[index].pdfFileName = ''
  batchDialog.formList[index].pdfFileList = []
}

// 批量DWG自定义文件上传
const handleBatchDwgUpload = async (options, index) => {
  const { file, onSuccess, onError, onProgress } = options
  
  try {
    // 获取当前成品图纸信息用于生成格式化文件名
    const pid = batchDialog.formList[index].pid || 'UNKNOWN'
    let itemName = batchDialog.formList[index].itemName || '成品文件'
    
    // 处理产品名称字段，当文本中包含括号时截取括号前的文本
    const leftParenIndex = itemName.search(/[（(]/)
    if (leftParenIndex > -1) {
      itemName = itemName.substring(0, leftParenIndex).trim()
    }
    
    // 第一步：创建格式化文件名预上传任务
    const presignResponse = await http.post(
      `/minio/buckets/${dwgBucket.value}/files/formatted-presigned-upload-for-doc-prod-drawing`,
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
        fileUrl: fileUrl,
        fileName: file.name
      }
    }
    
    // 调用批量上传成功处理函数
    handleBatchDwgUploadSuccess(mockResponse, file, index)
    onSuccess(mockResponse)
    
    ElMessage.success(`第 ${index + 1} 行DWG文件上传成功，文件名: ${formattedFileName}`)
    
  } catch (error) {
    onError(error)
    ElMessage.error(`第 ${index + 1} 行DWG文件上传失败: ` + error.message)
  }
}

// 批量PDF自定义文件上传
const handleBatchPdfUpload = async (options, index) => {
  const { file, onSuccess, onError, onProgress } = options
  
  try {
    // 获取当前成品图纸信息用于生成格式化文件名
    const pid = batchDialog.formList[index].pid || 'UNKNOWN'
    let itemName = batchDialog.formList[index].itemName || '成品文件'
    
    // 处理产品名称字段，当文本中包含括号时截取括号前的文本
    const leftParenIndex = itemName.search(/[（(]/)
    if (leftParenIndex > -1) {
      itemName = itemName.substring(0, leftParenIndex).trim()
    }
    
    // 第一步：创建格式化文件名预上传任务
    const presignResponse = await http.post(
      `/minio/buckets/${pdfBucket.value}/files/formatted-presigned-upload-for-doc-prod-drawing`,
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
      contentType: file.type || 'application/octet-stream'
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
        fileUrl: fileUrl,
        fileName: file.name
      }
    }
    
    // 调用批量上传成功处理函数
    handleBatchPdfUploadSuccess(mockResponse, file, index)
    onSuccess(mockResponse)
    
    ElMessage.success(`第 ${index + 1} 行PDF文件上传成功，文件名: ${formattedFileName}`)
    
  } catch (error) {
    onError(error)
    ElMessage.error(`第 ${index + 1} 行PDF文件上传失败: ` + error.message)
  }
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
    
    // 如果有DWG文件，先删除DWG文件
    if (row.dwgFileId && row.dwgFileUrl) {
      await deleteFileFromMinIO(row.dwgFileUrl, 'DWG', row.dwgFileId)
    }
    
    // 如果有PDF文件，先删除PDF文件
    if (row.pdfFileId && row.pdfFileUrl) {
      await deleteFileFromMinIO(row.pdfFileUrl, 'PDF', row.pdfFileId)
    }
    
    // 使用API服务删除数据
    const response = await docProdDrawingApi.deleteDocProdDrawing(row.id)
    
    if (response.code === 200) {
      ElMessage.success('删除成功，已同时删除关联的文件')
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
    const statusText = newStatus === 1 ? '启用' : '停用'
    
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
      return 'danger' // 停用
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
      return '停用'
    case 1:
      return '启用'
    case 2:
      return '消耗'
    default:
      return '未知'
  }
}

// 获取状态统计数量
const getStatusCount = (status) => {
  return pidSearchResults.value.filter(item => item.status === status).length
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

// 停用状态处理（从消耗状态变更为停用）
const handleDisableStatus = async (row) => {
  try {
    await ElMessageBox.confirm('确认将该成品图纸状态从消耗变更为停用吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用实际的API
    const response = await docProdDrawingApi.updateDocProdDrawing({
      id: row.id,
      status: 0 // 0表示停用状态
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
      ? await docProdDrawingApi.updateDocProdDrawing(dialog.form)
      : await docProdDrawingApi.addDocProdDrawing(dialog.form)
    
    if (response.code === 200) {
      ElMessage.success(dialog.form.id ? '更新成功' : '新增成功')
      // 清除搜索结果
      pidSearchResults.value = []
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
    pidSearchResults.value = []
  }
}

// 取消
const handleCancel = () => {
  // 清除搜索结果
  pidSearchResults.value = []
  dialog.visible = false
}

// DWG文件上传前的验证
const beforeDwgUpload = (file) => {
  const isLt50M = file.size / 1024 / 1024 < 100
  
  if (!isLt50M) {
    ElMessage.error('上传文件大小不能超过100MB!')
    return false
  }
  
  return true
}

// PDF文件上传前的验证
const beforePdfUpload = (file) => {
  const isLt50M = file.size / 1024 / 1024 < 100
  
  if (!isLt50M) {
    ElMessage.error('上传文件大小不能超过100MB!') 
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
      `/minio/buckets/${dwgBucket.value}/files/formatted-presigned-upload-for-doc-prod-drawing`,
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
      `/minio/buckets/${pdfBucket.value}/files/formatted-presigned-upload-for-doc-prod-drawing`,
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
      
      ElMessage.success('文件已在新窗口中打开')
    } else {
      ElMessage.error('获取下载链接失败')
    }
  } catch (error) {
    ElMessage.error('打开文件失败: ' + error.message)
  }
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

// 实时搜索成品编号匹配的条目
const searchPidItems = async (pidValue) => {
  try {
    // 调用API搜索匹配的条目
    const response = await docProdDrawingApi.getDocProdDrawingList({
      pid: pidValue,
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
      
      pidSearchResults.value = results
      console.log(`找到 ${results.length} 个匹配条目`)
    } else {
      pidSearchResults.value = []
      console.warn('搜索失败:', response.msg)
    }
  } catch (error) {
    pidSearchResults.value = []
    console.error('搜索失败:', error.message)
  }
}

// 清除搜索结果
const clearPidSearchResults = () => {
  pidSearchResults.value = []
  ElMessage.info('已清除搜索结果')
}

// 更新条目状态
const updateItemStatus = async (item, newStatus) => {
  try {
    const statusText = getStatusText(newStatus)
    
    await ElMessageBox.confirm(`确认将条目"${item.pid} - ${item.itemName}"状态修改为"${statusText}"吗？`, '状态修改确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用API更新状态
    const response = await docProdDrawingApi.updateDocProdDrawing({
      id: item.id,
      status: newStatus
    })
    
    if (response.code === 200) {
      ElMessage.success(`状态已更新为"${statusText}"`)
      
      // 更新本地搜索结果中的状态
      const index = pidSearchResults.value.findIndex(result => result.id === item.id)
      if (index !== -1) {
        pidSearchResults.value[index].status = newStatus
      }
      
      // 更新表格数据中的状态
      const tableIndex = tableData.value.findIndex(tableItem => tableItem.id === item.id)
      if (tableIndex !== -1) {
        tableData.value[tableIndex].status = newStatus
      }
    } else {
      ElMessage.error(response.msg || '状态更新失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('状态更新失败: ' + error.message)
    }
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
    const pidValue = dialog.form.pid?.trim()
    
    if (pidValue) {
      // 实时搜索匹配的条目
      await searchPidItems(pidValue)
      
      // 调用MES接口根据成品编号获取物料信息
      const itemInfo = await getItemInfoFromMES(pidValue)

      if (itemInfo) {
        dialog.form.itemName = itemInfo.itemName || ''
        dialog.form.model = itemInfo.itemSpec ||  ''
        ElMessage.success('已自动填充物料名称和规格型号')
      }
      
      // 调用API根据成品编号前N位查询物料类型
      try {
        const itemTypeResponse = await docProdDrawingApi.getItemTypeByPid(pidValue, 3)
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
      pidSearchResults.value = []
    }
    inputTimers.pid = null
  }, 500) // 减少防抖时间，提高响应速度
}

// 根据状态获取表格行类名
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

/* 搜索结果区域 */
.pid-search-results {
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  max-height: 250px;
  overflow: hidden;
  margin-top: 8px;
  z-index: 1000;
  position: relative;
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
  border-bottom: 1px solid #f0f2f5;
  cursor: pointer;
  transition: background-color 0.2s;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-result-item:hover {
  background-color: #f5f7fa;
}

.search-result-item:last-child {
  border-bottom: none;
}

.result-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.result-pid {
  font-weight: 500;
  color: #303133;
  font-size: 14px;
}

.result-item-name {
  color: #606266;
  font-size: 13px;
}

.result-model {
  color: #909399;
  font-size: 12px;
}

.result-status {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  min-width: 120px;
}

.status-actions {
  display: flex;
  gap: 4px;
}

/* 不同状态的背景色 */
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

/* 多条新建表格样式 */
.batch-form-container {
  padding: 10px 0;
}

.batch-header {
  display: flex;
  align-items: center;
}

.el-scrollbar {
  border-radius: 4px;
  border: 1px solid #e8e8e8;
}

/* 文件上传单元格样式 */
.file-upload-cell {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

:deep(.el-table .el-table__cell) {
  padding: 8px 12px;
}

/* 表格行操作按钮样式 */
:deep(.el-table .el-button--small) {
  padding: 4px 8px;
}

/* 确保表格在滚动时表头固定 */
:deep(.el-table__header-wrapper) {
  position: sticky;
  top: 0;
  z-index: 10;
  background-color: #fff;
}

/* 调整表格中输入框和选择框的高度 */
:deep(.el-table .el-input__wrapper) {
  box-sizing: border-box;
}

:deep(.el-table .el-select .el-input__wrapper) {
  box-sizing: border-box;
}

/* 调整表格中按钮的对齐方式 */
:deep(.el-table .el-button) {
  vertical-align: middle;
}
</style>