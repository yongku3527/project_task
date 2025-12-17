<template>
  <div class="countersign-drawing-manager">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-actions">
            <div class="search-bar-inline">
              <el-form :inline="true" size="small">
                <el-form-item label="图纸来源:">
                  <el-input v-model="searchForm.drawingSource" placeholder="请输入图纸来源" clearable />
                </el-form-item>
                <el-form-item label="产品类别:">
                  <el-input v-model="searchForm.productCategory" placeholder="请输入产品类别" clearable />
                </el-form-item>
                <el-form-item label="客户名称:">
                  <el-input v-model="searchForm.customerName" placeholder="请输入客户名称" clearable />
                </el-form-item>
                <el-form-item label="产品名称:">
                  <el-input v-model="searchForm.prodName" placeholder="请输入产品名称" clearable />
                </el-form-item>
                <el-form-item label="零部件号:">
                  <el-input v-model="searchForm.partNo" placeholder="请输入零部件号" clearable style="width: 150px;" />
                </el-form-item>
                
                <el-form-item>
                  <el-button type="primary" @click="handleSearch">查询</el-button>
                  <el-button @click="handleReset">重置</el-button>
                </el-form-item>
              </el-form>
            </div>
            <div class="header-buttons">
              <el-button type="primary" @click="handleAdd" v-permission="'countersignDrawing:add'">
                <el-icon><Plus /></el-icon>
                新增
              </el-button>
              <el-button type="success" @click="handleBatchAdd" v-permission="'countersignDrawing:add'">
                <el-icon><Plus /></el-icon>
                多条新建
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
        <el-table-column prop="productCategory" label="产品类别" min-width="100" />
        <el-table-column prop="drawingSource" label="图纸来源" min-width="120" />
        
        <el-table-column prop="prodName" label="产品名称" min-width="150" />
        <el-table-column prop="partNo" label="零部件号" min-width="180" />
        <el-table-column prop="customerName" label="客户名称" min-width="150" />
        <el-table-column prop="dwgFileName" label="图纸文件" min-width="200">
          <template #default="scope">
            <el-link 
              v-if="scope.row.dwgFileUrl" 
              type="primary" 
              @click="downloadFile(scope.row.dwgFileUrl, scope.row.dwgFileName)"
              style="word-break: break-all; display: inline-block; max-width: 100%;"
            >
              {{ scope.row.dwgFileName }}
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
        <el-table-column prop="updateTime" label="更新时间" min-width="160" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-button type="primary" size="small" link @click="handleEdit(scope.row)" v-permission="'countersignDrawing:update'">
                <el-icon><Edit /></el-icon>编辑
              </el-button>
              <el-button type="danger" size="small" link @click="handleDelete(scope.row.id)" v-permission="'countersignDrawing:delete'">
                <el-icon><Delete /></el-icon>删除
              </el-button>
              <el-button 
                v-permission="'countersignDrawing:update'"
                :type="scope.row.status === 1 ? 'warning' : 'success'" 
                size="small" 
                link 
                @click="handleToggleStatus(scope.row)"
                v-if="scope.row.status !== 2"
              >
                <el-icon><Switch /></el-icon>{{ scope.row.status === 1 ? '停用' : '启用' }}
              </el-button>
              <el-button 
                v-permission="'countersignDrawing:update'"
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
                v-permission="'countersignDrawing:update'"
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
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <el-form
        ref="formRef"
        :model="dialog.form"
        :rules="dialog.rules"
        label-width="120px"
      >
        <el-form-item label="零部件号" prop="partNo">
          <el-input 
            v-model="dialog.form.partNo" 
            placeholder="请输入零部件号"
            style="width: 100%"
            @input="handlePartNoInput"
            clearable
          />
          <!-- 搜索结果展示 -->
          <div v-if="partNoSearchResults.length > 0" class="part-no-search-results">
            <div class="search-results-header">
              <div class="search-results-title">
                <span>搜索结果 (共{{ partNoSearchResults.length }} 条)</span>
                
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
              <el-button type="text" size="small" @click="clearPartNoSearchResults">
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
            <div class="search-results-list">
              <div 
                v-for="item in partNoSearchResults" 
                :key="item.id"
                :class="`search-result-item status-${item.status}`"
              >
                <div class="result-main">
                  <div class="result-part-no">{{ item.partNo }}</div>
                  <div class="result-prod-name">{{ item.prodName }}</div>
                  <div class="result-customer-name">{{ item.customerName }}</div>
              
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
                      type="danger" 
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
        <el-form-item label="图纸来源" prop="drawingSource">
          <el-select 
            v-model="dialog.form.drawingSource" 
            placeholder="请选择图纸来源"
            style="width: 100%;"
          >

            <el-option label="客户工程师" value="客户工程师" />
            <el-option label="业务经理" value="业务经理" />
            <el-option label="平台下载" value="平台下载" />
          </el-select>
        </el-form-item>
        <el-form-item label="产品类别" prop="productCategory">
          <el-input 
            v-model="dialog.form.productCategory" 
            placeholder="请输入产品类别"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="产品名称" prop="prodName">
          <el-input 
            v-model="dialog.form.prodName" 
            placeholder="请输入产品名称"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="客户名称" prop="customerName">
          <el-input 
            v-model="dialog.form.customerName" 
            placeholder="请输入客户名称"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="文件">
          <el-upload
            ref="fileUploadRef"
            :action="`/minio/upload/${countersignBucket}`"
            :limit="1"
            :on-success="handleFileUploadSuccess"
            :on-remove="handleFileUploadRemove"
            :file-list="dialog.fileList"
            :before-upload="beforeFileUpload"
            :http-request="handleFileUpload"
            :auto-upload="true"
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon>
              选择文件
            </el-button>
            <template #tip>
              <div class="el-upload__tip">
                文件大小不超过100MB
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
            
            <el-table-column prop="drawingSource" label="图纸来源" min-width="120">
              <template #default="scope">
                <el-select 
                  v-model="batchDialog.formList[scope.$index].drawingSource" 
                  placeholder="请选择"
                  style="width: 100%;"
                >
                  <el-option label="客户工程师" value="客户工程师" />
                  <el-option label="业务经理" value="业务经理" />
                  <el-option label="平台下载" value="平台下载" />
                </el-select>
              </template>
            </el-table-column>
            
            <el-table-column prop="productCategory" label="产品类别" min-width="120">
              <template #default="scope">
                <el-input 
                  v-model="batchDialog.formList[scope.$index].productCategory" 
                  placeholder="请输入"
                  size="small"
                />
              </template>
            </el-table-column>
            
            <el-table-column prop="prodName" label="产品名称" min-width="150">
              <template #default="scope">
                <el-input 
                  v-model="batchDialog.formList[scope.$index].prodName" 
                  placeholder="请输入"
                  size="small"
                />
              </template>
            </el-table-column>
            
            <el-table-column prop="partNo" label="零部件号" min-width="180">
              <template #default="scope">
                <el-input 
                  v-model="batchDialog.formList[scope.$index].partNo" 
                  placeholder="请输入"
                  size="small"
                  @input="handleBatchPartNoInput(scope.$index)"
                />
              </template>
            </el-table-column>
            
            <el-table-column prop="customerName" label="客户名称" min-width="150">
              <template #default="scope">
                <el-input 
                  v-model="batchDialog.formList[scope.$index].customerName" 
                  placeholder="请输入"
                  size="small"
                />
              </template>
            </el-table-column>
            
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-select 
                  v-model="batchDialog.formList[scope.$index].status" 
                  placeholder="请选择"
                  style="width: 100%;"
                  size="small"
                >
                  <el-option label="启用" value="1" />
                  <el-option label="停用" value="0" />
                  <el-option label="消耗" value="2" />
                </el-select>
              </template>
            </el-table-column>
            
            <el-table-column prop="dwgFileName" label="图纸文件" min-width="200">
              <template #default="scope">
                <div class="file-upload-cell">
                  <el-upload
                    :action="`/minio/upload/${countersignBucket}`"
                    :limit="1"
                    :on-success="(response, file) => handleBatchFileUploadSuccess(response, file, scope.$index)"
                    :on-remove="() => handleBatchFileRemove(scope.$index)"
                    :file-list="batchDialog.formList[scope.$index].fileList"
                    :before-upload="beforeFileUpload"
                    :http-request="(options) => handleBatchFileUpload(options, scope.$index)"
                    :auto-upload="true"
                    :show-file-list="false"
                  >
                    <el-button type="primary" size="small" v-if="!batchDialog.formList[scope.$index].dwgFileName">
                      <el-icon><Upload /></el-icon>
                      选择文件
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
                    @click.stop="handleBatchFileRemove(scope.$index)"
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

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Edit, Delete, Upload,
  Switch, Close
} from '@element-plus/icons-vue'
import * as countersignDrawingApi from '@/api/countersignDrawing'
import { http } from '@/utils/request'

// 搜索表单
const searchForm = reactive({
  drawingSource: '',
  productCategory: '',
  prodName: '',
  partNo: '',
  customerName: ''
})

// 表格数据
const tableData = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(50)
const multipleSelection = ref([])

// 定义存储桶名称
const countersignBucket = ref('countersign')

// 表单引用
const formRef = ref(null)
const fileUploadRef = ref(null)

// 对话框
const dialog = reactive({
  visible: false,
  title: '新增会签图纸',
  form: {
    id: null,
    drawingSource: '',
    productCategory: '',
    prodName: '',
    partNo: '',
    customerName: '',
    dwgFileId: null,
    dwgFileUrl: '',
    dwgFileName: '',
    status: 1
  },
  rules: {
    drawingSource: [{ required: true, message: '请输入图纸来源', trigger: 'blur' }],
    productCategory: [{ required: true, message: '请输入产品类别', trigger: 'blur' }],
    prodName: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
    partNo: [{ required: true, message: '请输入零部件号', trigger: 'blur' }],
    customerName: [{ required: true, message: '请输入客户名称', trigger: 'blur' }],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }]
  },
  fileList: []
})

// 多条新建对话框
const batchDialog = reactive({
  visible: false,
  title: '多条新建会签图纸',
  formList: [{
    id: null,
    drawingSource: '',
    productCategory: '',
    prodName: '',
    partNo: '',
    customerName: '',
    dwgFileId: null,
    dwgFileUrl: '',
    dwgFileName: '',
    status: 1,
    fileList: []
  }]
})

// 选中的批量项
const selectedBatchItems = ref([])

// 批量选择变化
const handleBatchSelectionChange = (selection) => {
  selectedBatchItems.value = selection
}

// 删除选中行
const removeSelectedBatchItems = async () => {
  // 按索引从大到小删除，避免索引错乱
  const indexes = selectedBatchItems.value.map(item => batchDialog.formList.indexOf(item)).sort((a, b) => b - a)
  
  for (const index of indexes) {
    // 如果有文件，先删除文件
    const item = batchDialog.formList[index]
    if (item.dwgFileId && item.dwgFileUrl) {
      await deleteFileFromMinIO(item.dwgFileUrl, '会签文件', item.dwgFileId)
    }
    batchDialog.formList.splice(index, 1)
  }
  
  // 清空选择
  selectedBatchItems.value = []
}

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
    
    const response = await countersignDrawingApi.getCountersignDrawingList(params)
    
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
  dialog.title = '新增会签图纸'
  dialog.form = {
    id: null,
    drawingSource: '',
    productCategory: '',
    prodName: '',
    partNo: '',
    customerName: '',
    dwgFileId: null,
    dwgFileUrl: '',
    dwgFileName: '',
    status: 1
  }
  dialog.fileList = []
  dialog.visible = true
}

// 多条新建
const handleBatchAdd = () => {
  batchDialog.title = '多条新建会签图纸'
  batchDialog.formList = [{
    id: null,
    drawingSource: '',
    productCategory: '',
    prodName: '',
    partNo: '',
    customerName: '',
    dwgFileId: null,
    dwgFileUrl: '',
    dwgFileName: '',
    status: 1,
    fileList: []
  }]
  batchDialog.visible = true
}

// 添加批量项
const addBatchItem = () => {
  batchDialog.formList.push({
    id: null,
    drawingSource: '',
    productCategory: '',
    prodName: '',
    partNo: '',
    customerName: '',
    dwgFileId: null,
    dwgFileUrl: '',
    dwgFileName: '',
    status: 1,
    fileList: []
  })
}

// 删除批量项
const removeBatchItem = async (index) => {
  // 如果有文件，先删除文件
  const item = batchDialog.formList[index]
  if (item.dwgFileId && item.dwgFileUrl) {
    await deleteFileFromMinIO(item.dwgFileUrl, '会签文件', item.dwgFileId)
  }
  batchDialog.formList.splice(index, 1)
}

// 编辑
const handleEdit = (row) => {
  dialog.title = '编辑会签图纸'
  dialog.form = {
    id: row.id,
    drawingSource: row.drawingSource,
    productCategory: row.productCategory,
    prodName: row.prodName,
    partNo: row.partNo,
    customerName: row.customerName,
    dwgFileId: row.dwgFileId,
    dwgFileUrl: row.dwgFileUrl,
    dwgFileName: row.dwgFileName,
    status: row.status
  }
  
  // 设置文件列表
  dialog.fileList = []
  if (row.dwgFileUrl && row.dwgFileName) {
    dialog.fileList.push({
      name: row.dwgFileName,
      url: row.dwgFileUrl
    })
  }
  
  dialog.visible = true
}

// 关闭对话框
const handleDialogClose = () => {
  dialog.form = {
    id: null,
    drawingSource: '',
    productCategory: '',
    prodName: '',
    partNo: '',
    customerName: '',
    dwgFileId: null,
    dwgFileUrl: '',
    dwgFileName: '',
    status: 1
  }
  dialog.fileList = []
  // 清除搜索结果
  clearPartNoSearchResults()
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
      response = await countersignDrawingApi.updateCountersignDrawing(data)
    } else {
      // 新增
      response = await countersignDrawingApi.addCountersignDrawing(data)
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
    // 先获取要删除的会签图纸信息，包括文件URL和ID
    const row = tableData.value.find(item => item.id === id)
    
    await ElMessageBox.confirm('确定要删除这条记录吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 如果有DWG文件，先删除MinIO文件
    if (row && row.dwgFileUrl) {
      await deleteFileFromMinIO(row.dwgFileUrl, 'DWG', row.dwgFileId)
    }
    
    const response = await countersignDrawingApi.deleteCountersignDrawing(id)
    
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
    await ElMessageBox.confirm(`确定要${statusText}该会签图纸吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await countersignDrawingApi.updateCountersignDrawing({
      id: row.id,
      status: newStatus
    })
    
    if (response.code === 200) {
      // 更新本地状态
      row.status = newStatus
      ElMessage.success(`已${statusText}该会签图纸`)
      
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
    await ElMessageBox.confirm('确认将该会签图纸状态变更为消耗吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await countersignDrawingApi.updateCountersignDrawing({
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
    await ElMessageBox.confirm('确认将该会签图纸状态变更为停用吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await countersignDrawingApi.updateCountersignDrawing({
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

// 输入防抖定时器
const inputTimers = {}

// 零部件号搜索结果
const partNoSearchResults = ref([])

// 图纸来源输入处理
const handleDrawingSourceInput = (value) => {
  const drawingSourceValue = value?.trim()
  
  // 清除之前的定时器
  if (inputTimers.drawingSource) {
    clearTimeout(inputTimers.drawingSource)
  }
  
  // 设置新的定时器
  inputTimers.drawingSource = setTimeout(async () => {
    // 可以在这里添加搜索逻辑
    inputTimers.drawingSource = null
  }, 500) // 500毫秒防抖
}

// 实时搜索零部件号匹配的条目
const searchPartNoItems = async (partNoValue) => {
  try {
    // 调用API搜索匹配的条目
    const response = await countersignDrawingApi.getCountersignDrawingList({
      partNo: partNoValue,
      page: 1,
      size: 50 // 限制搜索结果数量
    })
    
    if (response.code === 200) {
      let results = response.data.records || response.data || []
      
      // 按照状态排序：启用(1) > 消耗(2) > 停用(0)
      results = results.sort((a, b) => {
        const statusOrder = { 1: 3, 2: 2, 0: 1 }
        return statusOrder[b.status] - statusOrder[a.status]
      })
      
      partNoSearchResults.value = results
    } else {
      console.warn('搜索零部件号失败: ' + response.msg)
    }
  } catch (error) {
    console.warn('搜索零部件号失败: ' + error.message)
  }
}

// 获取特定状态的搜索结果数量
const getStatusCount = (status) => {
  return partNoSearchResults.value.filter(item => item.status === status).length
}

// 清除搜索结果
const clearPartNoSearchResults = () => {
  partNoSearchResults.value = []
}

// 更新搜索结果中条目的状态
const updateItemStatus = async (item, newStatus) => {
  try {
    const statusText = getStatusText(newStatus)
    
    await ElMessageBox.confirm(`确定要将状态更新为"${statusText}"吗？`, '状态更新确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用API更新状态
    const response = await countersignDrawingApi.updateCountersignDrawing({
      id: item.id,
      status: newStatus
    })
    
    if (response.code === 200) {
      ElMessage.success(`状态已更新为"${statusText}"`)
      
      // 更新本地搜索结果中的状态
      const index = partNoSearchResults.value.findIndex(result => result.id === item.id)
      if (index !== -1) {
        partNoSearchResults.value[index].status = newStatus
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

// 零部件号输入处理（防抖）
const handlePartNoInput = () => {
  // 清除之前的定时器
  if (inputTimers.partNo) {
    clearTimeout(inputTimers.partNo)
  }
  
  // 设置新的定时器
  inputTimers.partNo = setTimeout(async () => {
    const partNoValue = dialog.form.partNo?.trim()
    
    if (partNoValue) {
      // 实时搜索匹配的条目
      await searchPartNoItems(partNoValue)
      
      // 检查是否有启用状态的重复记录
      const hasEnabledDuplicate = partNoSearchResults.value.some(item => item.status === 1)
      if (hasEnabledDuplicate) {
        ElMessage.warning(`零部件号 "${partNoValue}" 在数据库中已存在（启用状态）`)
      }
    } else {
      // 清除搜索结果
      partNoSearchResults.value = []
    }
    inputTimers.partNo = null
  }, 500) // 500ms防抖
}

// 批量新增表单的零部件号输入处理（防抖）
const handleBatchPartNoInput = (index) => {
  // 清除之前的定时器
  if (inputTimers[`batchPartNo_${index}`]) {
    clearTimeout(inputTimers[`batchPartNo_${index}`])
  }
  
  // 设置新的定时器
  inputTimers[`batchPartNo_${index}`] = setTimeout(async () => {
    const partNoValue = batchDialog.formList[index].partNo?.trim()
    const currentItem = batchDialog.formList[index]
    
    if (partNoValue) {
      // 检查当前批次中是否有重复的零部件号（仅检查启用状态）
      const duplicateInBatch = batchDialog.formList.some((item, idx) => {
        return idx !== index && item.partNo?.trim() === partNoValue && item.status === 1
      })
      
      if (duplicateInBatch) {
        ElMessage.warning(`第 ${index + 1} 行：零部件号 "${partNoValue}" 在当前批次中已存在（启用状态）`)
      }
      
      // 实时搜索数据库中是否有重复的零部件号
      await searchPartNoItems(partNoValue)
      
      // 检查数据库中是否有启用状态的重复记录
      const hasEnabledDuplicate = partNoSearchResults.value.some(item => item.status === 1)
      if (hasEnabledDuplicate) {
        ElMessage.warning(`第 ${index + 1} 行：零部件号 "${partNoValue}" 在数据库中已存在（启用状态）`)
      }
    }
    inputTimers[`batchPartNo_${index}`] = null
  }, 500) // 500ms防抖
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

// 文件打开
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
    
    // 获取预签名打开URL
    const response = await http.get(
      `/minio/buckets/${bucketName}/files/${objectName}/presigned-url`
    )
    
    if (response.code === 200) {
      const presignedUrl = response.data
      // 使用预签名URL直接在新窗口中打开文件
      window.open(presignedUrl, '_blank')
      ElMessage.success('文件已在新窗口中打开')
    } else {
      ElMessage.error('获取打开链接失败: ' + response.msg)
    }
  } catch (error) {
    console.error('打开文件失败:', error)
    ElMessage.error('打开文件失败: ' + error.message)
  }
}

// 自定义文件上传
const handleFileUpload = async (options) => {
  const { file, onSuccess, onError, onProgress } = options
  
  try {
    // 获取当前会签图纸信息用于生成格式化文件名
    const partNo = dialog.form.partNo || 'UNKNOWN'
    let prodName = dialog.form.prodName || '会签文件'
    
    // 处理产品名称字段，当文本中包含括号时截取括号前的文本
    const leftParenIndex = prodName.search(/[（(]/)
    if (leftParenIndex > -1) {
      prodName = prodName.substring(0, leftParenIndex).trim()
    }
    
    // 第一步：创建格式化文件名预上传任务
    const presignResponse = await http.post(
      `/minio/buckets/${countersignBucket.value}/files/formatted-presigned-upload-for-doc-prod-drawing`,
      null,
      {
        code: partNo,
        name: prodName,
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
    const saveFileResponse = await http.post(`/minio/buckets/${countersignBucket.value}/files/save-info`, {
      bucketName: countersignBucket.value,
      objectName: formattedFileName,
      originalName: file.name,
      fileSize: file.size,
      contentType: file.type || 'application/octet-stream'
    })
    
    if (saveFileResponse.code !== 200) {
      throw new Error('保存文件信息失败: ' + saveFileResponse.msg)
    }
    
    // 模拟原上传成功回调格式
    const fileUrl = `/minio/buckets/${countersignBucket.value}/files/${encodeURIComponent(formattedFileName)}`
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

// 批量自定义文件上传
const handleBatchFileUpload = async (options, index) => {
  const { file, onSuccess, onError, onProgress } = options
  
  try {
    // 获取当前批量项的会签图纸信息用于生成格式化文件名
    const partNo = batchDialog.formList[index].partNo || 'UNKNOWN'
    let prodName = batchDialog.formList[index].prodName || '会签文件'
    
    // 处理产品名称字段，当文本中包含括号时截取括号前的文本
    const leftParenIndex = prodName.search(/[（(]/)
    if (leftParenIndex > -1) {
      prodName = prodName.substring(0, leftParenIndex).trim()
    }
    
    // 第一步：创建格式化文件名预上传任务
    const presignResponse = await http.post(
      `/minio/buckets/${countersignBucket.value}/files/formatted-presigned-upload-for-doc-prod-drawing`,
      null,
      {
        code: partNo,
        name: prodName,
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
    const saveFileResponse = await http.post(`/minio/buckets/${countersignBucket.value}/files/save-info`, {
      bucketName: countersignBucket.value,
      objectName: formattedFileName,
      originalName: file.name,
      fileSize: file.size,
      contentType: file.type || 'application/octet-stream'
    })
    
    if (saveFileResponse.code !== 200) {
      throw new Error('保存文件信息失败: ' + saveFileResponse.msg)
    }
    
    // 模拟原上传成功回调格式
    const fileUrl = `/minio/buckets/${countersignBucket.value}/files/${encodeURIComponent(formattedFileName)}`
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
    handleBatchFileUploadSuccess(mockResponse, file, index)
    onSuccess(mockResponse)
    
    ElMessage.success(`第 ${index + 1} 行文件上传成功，文件名: ${formattedFileName}`)
    
  } catch (error) {
    onError(error)
    ElMessage.error(`第 ${index + 1} 行文件上传失败: ` + error.message)
  }
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
      if (!item.drawingSource) {
        ElMessage.error(`第 ${i + 1} 行：图纸来源不能为空`)
        isValid = false
      }
      if (!item.productCategory) {
        ElMessage.error(`第 ${i + 1} 行：产品类别不能为空`)
        isValid = false
      }
      if (!item.prodName) {
        ElMessage.error(`第 ${i + 1} 行：产品名称不能为空`)
        isValid = false
      }
      if (!item.partNo) {
        ElMessage.error(`第 ${i + 1} 行：零部件号不能为空`)
        isValid = false
      }
      if (!item.customerName) {
        ElMessage.error(`第 ${i + 1} 行：客户名称不能为空`)
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
    const response = await countersignDrawingApi.batchAddCountersignDrawing(validItems)
    
    if (response.code === 200) {
      successCount = validItems.length
      ElMessage.success(`成功提交 ${successCount} 条记录`)
      batchDialog.visible = false
      loadData()
    } else {
      failCount += validItems.length
      ElMessage.error('批量提交失败: ' + (response.msg || '未知错误'))
    }
    
    // 显示最终结果
    if (successCount > 0 && failCount > 0) {
      ElMessage.warning(`批量提交完成：成功 ${successCount} 条，失败 ${failCount} 条`)
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
    drawingSource: '',
    productCategory: '',
    prodName: '',
    partNo: '',
    customerName: '',
    dwgFileId: null,
    dwgFileUrl: '',
    dwgFileName: '',
    status: 1,
    fileList: []
  }]
}

// 文件上传成功
const handleFileUploadSuccess = (response, uploadFile) => {
  if (response.code === 200) {
    dialog.form.dwgFileId = response.data.id
    dialog.form.dwgFileUrl = response.data.fileUrl
    dialog.form.dwgFileName = response.data.fileName
    // 更新文件列表
    dialog.fileList = [{
      name: response.data.fileName,
      url: response.data.fileUrl
    }]
  } else {
    ElMessage.error('文件上传失败: ' + response.msg)
  }
}

// 批量文件上传成功
const handleBatchFileUploadSuccess = (response, file, index) => {
  if (response.code === 200) {
    batchDialog.formList[index].dwgFileId = response.data.id
    batchDialog.formList[index].dwgFileUrl = response.data.fileUrl
    batchDialog.formList[index].dwgFileName = response.data.fileName
    // 更新文件列表
    batchDialog.formList[index].fileList = [{
      name: response.data.fileName,
      url: response.data.fileUrl
    }]
  } else {
    ElMessage.error('文件上传失败: ' + response.msg)
  }
}

// 批量文件删除
const handleBatchFileRemove = async (index) => {
  // 如果有文件ID和URL，先删除数据库和MinIO文件
  if (batchDialog.formList[index].dwgFileId && batchDialog.formList[index].dwgFileUrl) {
    await deleteFileFromMinIO(batchDialog.formList[index].dwgFileUrl, '会签文件', batchDialog.formList[index].dwgFileId)
  }
  
  // 重置文件信息
  batchDialog.formList[index].dwgFileId = null
  batchDialog.formList[index].dwgFileUrl = ''
  batchDialog.formList[index].dwgFileName = ''
  batchDialog.formList[index].fileList = []
}

// 通用文件删除方法（从MinIO和数据库删除文件）
const deleteFileFromMinIO = async (fileUrl, fileType = '会签文件', fileId = null) => {
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
const handleFileUploadRemove = async () => {
  // 如果有文件ID和URL，先删除数据库和MinIO文件
  if (dialog.form.dwgFileId && dialog.form.dwgFileUrl) {
    await deleteFileFromMinIO(dialog.form.dwgFileUrl, '会签文件', dialog.form.dwgFileId)
  }
  
  // 重置文件信息
  dialog.form.dwgFileId = null
  dialog.form.dwgFileUrl = ''
  dialog.form.dwgFileName = ''
  dialog.fileList = []
}

// 页面加载时初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.countersign-drawing-manager {
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

/* 搜索结果样式 */
.part-no-search-results {
  margin-top: 10px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background-color: #ffffff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.search-results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.search-results-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  font-weight: 500;
}

.stats-item {
  margin-left: 10px;
}

.search-results-list {
  max-height: 300px;
  overflow-y: auto;
}

.search-result-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

.search-result-item:last-child {
  border-bottom: none;
}

.search-result-item:hover {
  background-color: #f5f7fa;
}

.result-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.result-part-no {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.result-prod-name {
  font-size: 13px;
  color: #606266;
}

.result-customer-name {
  font-size: 13px;
  color: #909399;
}

.result-status {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-actions {
  display: flex;
  gap: 5px;
}

/* 状态样式 */
.status-0 {
  border-left: 3px solid #f56c6c;
}

.status-1 {
  border-left: 3px solid #67c23a;
}

.status-2 {
  border-left: 3px solid #e6a23c;
}

/* 调整表格中按钮的对齐方式 */
:deep(.el-table .el-button) {
  vertical-align: middle;
}
</style>