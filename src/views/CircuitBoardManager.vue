<template>
  <div class="circuit-board-manager">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-actions">
            <div class="search-bar-inline">
              <el-form :inline="true" size="small">
                <el-form-item label="线路板编码:">
                  <el-input v-model="searchForm.boardCode" placeholder="请输入线路板编码" clearable />
                </el-form-item>
                <el-form-item label="线路板名称:">
                  <el-input v-model="searchForm.boardName" placeholder="请输入线路板名称" clearable />
                </el-form-item>
                <el-form-item label="半成品编号:">
                  <el-input v-model="searchForm.semiProductCode" placeholder="请输入半成品编号" clearable />
                </el-form-item>
                <el-form-item label="半成品名称:">
                  <el-input v-model="searchForm.semiProductName" placeholder="请输入半成品名称" clearable />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleSearch">查询</el-button>
                  <el-button @click="handleReset">重置</el-button>
                </el-form-item>
              </el-form>
            </div>
            <div class="header-buttons">
              <el-button 
                :type="isAuthenticated ? 'success' : 'warning'" 
                size="small" 
                @click="showPasswordDialog = true"
              >
                <el-icon><Lock /></el-icon>
                {{ isAuthenticated ? '已验证' : '管理员验证' }}
              </el-button>
              <el-button type="primary" size="small" @click="handleAdd" v-if="isAuthenticated">
                <el-icon><Plus /></el-icon>
                新增线路板
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
        >
          <!-- 线路板信息列 -->
          <el-table-column label="线路板信息" width="350" fixed="left">
            <template #default="{ row }">
              <div class="board-info" :class="{
                'board-info-enabled': row.status === 1,
                'board-info-disabled': row.status === 0,
                'board-info-consumed': row.status === 2
              }">
                <div class="info-item">
                  <span class="label">线路板编号:</span>
                  <span class="value">{{ row.boardCode }}</span>
                </div>
                <div class="info-item">
                  <span class="label">线路板名称:</span>
                  <span class="value">{{ row.boardName }}</span>
                </div>
                <div class="info-item">
                  <span class="label">PCB文件:</span>
                  <span class="value">
                    <el-link 
                      v-if="row.fileUrl" 
                      type="primary" 
                      @click="downloadFile(row.fileUrl, row.fileName)"
                    >
                      {{ row.fileName }}
                    </el-link>
                    <span v-else>-</span>
                  </span>
                </div>


              </div>
              <div class="board-actions">
                <el-button type="primary" size="small" link @click="handleEdit(row)" v-if="isAuthenticated">
                  <el-icon><Edit /></el-icon>编辑
                </el-button>
                <el-button type="danger" size="small" link @click="handleDelete(row)" v-if="isAuthenticated">
                  <el-icon><Delete /></el-icon>删除
                </el-button>
                <el-button 
                  type="primary" 
                  size="small" 
                  link 
                  @click="handleConsumeCircuitBoard(row)"
                  :disabled="row.status === 2"
                  v-if="isAuthenticated"
                >
                  <el-icon><Minus /></el-icon>消耗
                </el-button>
                <el-button 
                  :type="row.status === 1 ? 'danger' : 'success'" 
                  size="small" 
                  link 
                  @click="handleToggleCircuitBoardStatus(row)"
                  v-if="isAuthenticated"
                >
                  <el-icon><CircleClose v-if="row.status === 1" /><CircleCheck v-else /></el-icon>{{ row.status === 1 ? '停用' : '启用' }}
                </el-button>
                <el-button type="success" size="small" link @click="handleAddSemiProduct(row)" v-if="isAuthenticated">
                  <el-icon><Plus /></el-icon>添加半成品
                </el-button>
              </div>
            </template>
          </el-table-column>

          <!-- 半成品信息列 -->
          <el-table-column label="半成品信息" min-width="400">
            <template #default="{ row }">
              <div class="semi-product-container">
                <div v-if="!row.semiProductDTOList || row.semiProductDTOList.length === 0" class="no-data">
                  <el-empty description="暂无半成品数据" :image-size="60" />
                </div>
                <div v-else class="semi-product-list">
                  <div 
                    v-for="semi in row.semiProductDTOList" 
                    :key="semi.id"
                    class="semi-product-item"
                    :class="{
                      'semi-product-enabled': semi.status === 1,
                      'semi-product-disabled': semi.status === 0,
                      'semi-product-consumed': semi.status === 2
                    }"
                  >
                    <div class="semi-product-info">
                      <div class="info-row">
                        <span class="label">半成品编号:</span>
                        <span class="value">{{ semi.semiProductCode }}</span>
                      </div>
                      <div class="info-row">
                        <span class="label">半成品名称:</span>
                        <span class="value">{{ semi.semiProductName }}</span>
                      </div>
                      <div class="info-row">
                        <span class="label">原理图:</span>
                        <el-link 
                          v-if="semi.schematicFileUrl" 
                          type="primary" 
                          @click="downloadFile(semi.schematicFileUrl, semi.schematicFileName)"
                        >
                          {{ semi.schematicFileName }}
                        </el-link>
                        <span v-else>-</span>
                      </div>
                      <div class="info-row">
                        <span class="label">贴片图:</span>
                        <el-link 
                          v-if="semi.smtFileUrl" 
                          type="primary" 
                          @click="downloadFile(semi.smtFileUrl, semi.smtFileName)"
                        >
                          {{ semi.smtFileName }}
                        </el-link>
                        <span v-else>-</span>
                      </div>


                    </div>
                    <div class="semi-product-actions">
                      <el-button type="warning" size="small" link @click="handleEditSemiProduct(semi)" v-if="isAuthenticated">
                        <el-icon><Edit /></el-icon>编辑
                      </el-button>
                      <el-button type="danger" size="small" link @click="handleDeleteSemiProduct(semi)" v-if="isAuthenticated">
                        <el-icon><Delete /></el-icon>删除
                      </el-button>
                      <el-button 
                        type="primary" 
                        size="small" 
                        link 
                        @click="handleConsumeSemiProduct(semi)"
                        :disabled="semi.status === 2"
                        v-if="isAuthenticated"
                      >
                        <el-icon><Remove /></el-icon>消耗
                      </el-button>
                      <el-button 
                        :type="semi.status === 1 ? 'danger' : 'success'" 
                        size="small" 
                        link 
                        @click="handleToggleSemiProductStatus(semi)"
                        v-if="isAuthenticated"
                      >
                        <el-icon><CircleClose v-if="semi.status === 1" /><CircleCheck v-else /></el-icon>{{ semi.status === 1 ? '停用' : (semi.status === 2 ? '启用' : '启用') }}
                      </el-button>
                      <!-- 添加灯板插件按钮已隐藏 -->
                      <!-- <el-button type="info" size="small" link @click="handleAddLedBoardPlugin(semi)" v-if="isAuthenticated">
                        <el-icon><Plus /></el-icon>添加灯板插件
                      </el-button> -->
                      <!-- 灯板插件数量按钮已隐藏 -->
                      <!-- <el-button 
                        v-if="semi.ledBoardPluginSemiProductDTOList && semi.ledBoardPluginSemiProductDTOList.length > 0"
                        type="primary" 
                        size="small" 
                        link 
                        @click="toggleLedBoardPlugin(semi)"
                      >
                        <el-icon><ArrowDown v-if="!semi.showLedBoardPlugin" /><ArrowUp v-else /></el-icon>
                        灯板插件({{ semi.ledBoardPluginSemiProductDTOList.length }})
                      </el-button> -->
                    </div>
                    
                    <!-- 灯板插件半成品信息 -->
                    <div v-if="semi.showLedBoardPlugin" class="led-board-plugin-container">
                      <div class="led-board-plugin-list">
                        <div 
                          v-for="led in semi.ledBoardPluginSemiProductDTOList" 
                          :key="led.id"
                          class="led-board-plugin-item"
                        >
                          <div class="led-info">
                            <div class="info-row">
                              <span class="label">插件编号:</span>
                              <span class="value">{{ led.ledBoardPluginCode }}</span>
                            </div>
                            <div class="info-row">
                              <span class="label">插件名称:</span>
                              <span class="value">{{ led.ledBoardPluginName }}</span>
                            </div>
                            <div class="info-row">
                              <span class="label">文件:</span>
                              <el-link 
                                v-if="led.fileUrl" 
                                type="primary" 
                                @click="downloadFile(led.fileUrl, led.fileName)"
                              >
                                {{ led.fileName }}
                              </el-link>
                              <span v-else>-</span>
                            </div>


                          </div>
                          <div class="led-actions">
                            <el-button type="warning" size="small" link @click="handleEditLedBoardPlugin(led)" v-if="isAuthenticated">
                              <el-icon><Edit /></el-icon>编辑
                            </el-button>
                            <el-button type="danger" size="small" link @click="handleDeleteLedBoardPlugin(led)" v-if="isAuthenticated">
                              <el-icon><Delete /></el-icon>删除
                            </el-button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
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

    <!-- 线路板编辑对话框 -->
    <el-dialog
      v-model="boardDialog.visible"
      :title="boardDialog.title"
      width="600px"
    >
      <el-form :model="boardDialog.form" :rules="boardDialog.rules" ref="boardFormRef" label-width="100px">
        <el-form-item label="线路板编码" prop="boardCode">
  <el-input 
    v-model="boardDialog.form.boardCode" 
    placeholder="请输入线路板编码"
    @input="handleBoardCodeInput"
  />
</el-form-item>
        <el-form-item label="线路板名称" prop="boardName">
          <el-input v-model="boardDialog.form.boardName" placeholder="请输入线路板名称" />
        </el-form-item>
        <el-form-item label="上传文件">
          <el-upload
            ref="boardUploadRef"
            :action="`${baseUrl}/minio/upload/${circuitBoardBucket}`"
            :limit="1"
            :on-success="handleBoardUploadSuccess"
            :on-remove="handleBoardUploadRemove"
            :file-list="boardDialog.fileList"
            :before-upload="beforeBoardUpload"
            :http-request="handleBoardUpload"
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon>选择文件
            </el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="boardDialog.form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="boardDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="saveBoard">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 半成品编辑对话框 -->
    <el-dialog
      v-model="semiProductDialog.visible"
      :title="semiProductDialog.title"
      width="700px"
    >
      <el-form :model="semiProductDialog.form" :rules="semiProductDialog.rules" ref="semiProductFormRef" label-width="120px">
        <el-form-item label="所属线路板" prop="circuitBoardId">
          <el-select v-model="semiProductDialog.form.circuitBoardId" placeholder="请选择线路板" style="width: 100%">
            <el-option 
              v-for="board in circuitBoardOptions" 
              :key="board.id" 
              :label="`${board.boardCode} - ${board.boardName}`"
              :value="board.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="半成品编码" prop="semiProductCode">
  <el-input 
    v-model="semiProductDialog.form.semiProductCode" 
    placeholder="请输入半成品编码"
    @input="handleSemiProductCodeInput"
  />
</el-form-item>
        <el-form-item label="半成品名称" prop="semiProductName">
          <el-input v-model="semiProductDialog.form.semiProductName" placeholder="请输入半成品名称" />
        </el-form-item>
        <el-form-item label="原理图文件">
          <el-upload
            ref="schematicUploadRef"
            :action="`${baseUrl}/minio/upload/${schematicBucket}`"
            :limit="1"
            :on-success="handleSchematicUploadSuccess"
            :on-remove="handleSchematicUploadRemove"
            :file-list="semiProductDialog.schematicFileList"
            :before-upload="beforeBoardUpload"
            :http-request="handleSchematicUpload"
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon>选择原理图文件
            </el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="SMT文件">
          <el-upload
            ref="smtUploadRef"
            :action="`${baseUrl}/minio/upload/${smtBucket}`"
            :limit="1"
            :on-success="handleSmtUploadSuccess"
            :on-remove="handleSmtUploadRemove"
            :file-list="semiProductDialog.smtFileList"
            :before-upload="beforeBoardUpload"
            :http-request="handleSmtUpload"
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon>选择SMT文件
            </el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="semiProductDialog.form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="semiProductDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="saveSemiProduct">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 灯板插件编辑对话框 -->
    <el-dialog
      v-model="ledBoardPluginDialog.visible"
      :title="ledBoardPluginDialog.title"
      width="600px"
    >
      <el-form :model="ledBoardPluginDialog.form" :rules="ledBoardPluginDialog.rules" ref="ledBoardPluginFormRef" label-width="120px">
        <el-form-item label="所属半成品" prop="semiProductId">
          <el-select v-model="ledBoardPluginDialog.form.semiProductId" placeholder="请选择半成品" style="width: 100%">
            <el-option 
              v-for="semi in semiProductOptions" 
              :key="semi.id" 
              :label="`${semi.semiProductCode} - ${semi.semiProductName}`"
              :value="semi.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="灯板插件编码" prop="ledBoardPluginCode">
          <el-input 
            v-model="ledBoardPluginDialog.form.ledBoardPluginCode" 
            placeholder="请输入灯板插件编码"
            @input="handleLedBoardPluginCodeInput"
          />
        </el-form-item>
        <el-form-item label="灯板插件名称" prop="ledBoardPluginName">
          <el-input v-model="ledBoardPluginDialog.form.ledBoardPluginName" placeholder="请输入灯板插件名称" />
        </el-form-item>
        <el-form-item label="上传文件">
          <el-upload
            ref="ledBoardPluginUploadRef"
            :action="`${baseUrl}/minio/upload/${ledBoardPluginBucket}`"
            :limit="1"
            :on-success="handleLedBoardPluginUploadSuccess"
            :on-remove="handleLedBoardPluginUploadRemove"
            :file-list="ledBoardPluginDialog.fileList"
            :before-upload="beforeBoardUpload"
            :http-request="handleLedBoardPluginUpload"
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon>选择文件
            </el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="ledBoardPluginDialog.form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="ledBoardPluginDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="saveLedBoardPlugin">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 管理员密码验证对话框 -->
    <el-dialog
      v-model="showPasswordDialog"
      title="管理员验证"
      width="400px"
    >
      <el-form>
        <el-form-item label="密码">
          <el-input
            v-model="passwordInput"
            type="password"
            placeholder="请输入管理员密码"
            @keyup.enter="authenticateAdmin"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="authenticateAdmin">验证</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Edit, Delete, Upload, Download, ArrowDown, ArrowUp, Minus, Switch, Remove, CircleClose, CircleCheck, Lock } from '@element-plus/icons-vue'
import axios from 'axios'

const baseUrl = 'http://192.168.100.125:8083'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(5)
const total = ref(0)
const isAuthenticated = ref(false) // 管理员验证状态
const showPasswordDialog = ref(false) // 显示密码验证对话框
const passwordInput = ref('') // 管理员密码输入
const ADMIN_PASSWORD = 'admin123' // 管理员密码
// 定义不同文件类型的存储桶
const circuitBoardBucket = ref('circuit-boards')
const semiProductBucket = ref('semi-products')
const schematicBucket = ref('schematic-files')  // 原理图文件专用存储桶
const smtBucket = ref('smt-files')  // SMT文件专用存储桶
const ledBoardPluginBucket = ref('led-board-plugins')

// 向后兼容，保留currentBucket变量
const currentBucket = ref('files')

// 下拉选项数据
const circuitBoardOptions = ref([])
const semiProductOptions = ref([])

// 搜索表单
const searchForm = reactive({
  boardCode: '',
  boardName: '',
  semiProductCode: '',
  semiProductName: ''
})

// 线路板对话框
const boardDialog = reactive({
  visible: false,
  title: '',
  form: {
    id: null,
    boardCode: '',
    boardName: '',
    fileId: null,
    fileUrl: '',
    fileName: '',
    status: 1
  },
  rules: {
    boardCode: [{ required: true, message: '请输入线路板编码', trigger: 'blur' }],
    boardName: [{ required: true, message: '请输入线路板名称', trigger: 'blur' }],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }]
  },
  fileList: []
})

// 半成品对话框
const semiProductDialog = reactive({
  visible: false,
  title: '',
  form: {
    id: null,
    circuitBoardId: null,
    semiProductCode: '',
    semiProductName: '',
    schematicFileId: null,
    schematicFileUrl: '',
    schematicFileName: '',
    smtFileId: null,
    smtFileUrl: '',
    smtFileName: '',
    status: 1
  },
  rules: {
    circuitBoardId: [{ required: true, message: '请选择所属线路板', trigger: 'change' }],
    semiProductCode: [{ required: true, message: '请输入半成品编码', trigger: 'blur' }],
    semiProductName: [{ required: true, message: '请输入半成品名称', trigger: 'blur' }],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }]
  },
  schematicFileList: [],
  smtFileList: []
})

// 灯板插件对话框
const ledBoardPluginDialog = reactive({
  visible: false,
  title: '',
  form: {
    id: null,
    semiProductId: null,
    ledBoardPluginCode: '',
    ledBoardPluginName: '',
    fileId: null,
    fileUrl: '',
    fileName: '',
    status: 1
  },
  rules: {
    semiProductId: [{ required: true, message: '请选择所属半成品', trigger: 'change' }],
    ledBoardPluginCode: [{ required: true, message: '请输入灯板插件编码', trigger: 'blur' }],
    ledBoardPluginName: [{ required: true, message: '请输入灯板插件名称', trigger: 'blur' }],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }]
  },
  fileList: []
})

// 表单引用
const boardFormRef = ref()
const boardUploadRef = ref()
const semiProductFormRef = ref()
const schematicUploadRef = ref()
const smtUploadRef = ref()
const ledBoardPluginFormRef = ref()
const ledBoardPluginUploadRef = ref()

// 生命周期
onMounted(() => {
  loadData()
})

// 组件卸载时清理定时器
onUnmounted(() => {
  Object.keys(inputTimers).forEach(key => {
    if (inputTimers[key]) {
      clearTimeout(inputTimers[key])
      inputTimers[key] = null
    }
  })
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    // 如果有搜索条件，使用分页查询接口，否则使用获取所有数据的接口
    if (searchForm.boardCode || searchForm.boardName || searchForm.semiProductCode || searchForm.semiProductName) {
      // 使用分页查询接口，支持搜索
      const params = {
        page: currentPage.value,
        size: pageSize.value,
        boardCode: searchForm.boardCode || undefined,
        boardName: searchForm.boardName || undefined,
        semiProductCode: searchForm.semiProductCode || undefined,
        semiProductName: searchForm.semiProductName || undefined
      }
      const response = await axios.get(`${baseUrl}/circuit-board/list`, { params })
      if (response.data.code === 200) {
        // 分页查询接口返回的是分页数据对象
        const pageData = response.data.data
        // 按创建时间降序排序，最新的数据排在前面
        const sortedList = pageData.list.sort((a, b) => {
          return new Date(b.createTime) - new Date(a.createTime)
        })
        tableData.value = sortedList
        total.value = pageData.total
      } else {
        ElMessage.error('加载数据失败: ' + response.data.msg)
      }
    } else {
      // 使用获取所有数据的接口（包含嵌套数据）
      const response = await axios.get(`${baseUrl}/circuit-board/all-with-details`)
      if (response.data.code === 200) {
        // 按创建时间降序排序，最新的数据排在前面
        const sortedData = response.data.data.sort((a, b) => {
          return new Date(b.createTime) - new Date(a.createTime)
        })
        tableData.value = sortedData
        total.value = sortedData.length
      } else {
        ElMessage.error('加载数据失败: ' + response.data.msg)
      }
    }
  } catch (error) {
    ElMessage.error('加载数据失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 生成模拟数据
const generateMockData = async () => {
  // 实际使用时，这里应该调用后端接口获取数据
  // 例如：const response = await axios.get(`${baseUrl}/circuit-board/list`, { params: searchForm })
  
  const mockData = [
    {
      id: 1,
      boardCode: 'PCB001',
      boardName: '主控制板',
      fileUrl: 'http://example.com/file1.pdf',
      fileName: '主控制板规格书.pdf',
      status: 1,
      createTime: '2024-01-15 10:30:00',
      updateTime: '2024-01-15 10:30:00',
      semiProductDTOList: [
        {
          id: 11,
          circuitBoardId: 1,
          circuitBoardCode: 'PCB001',
          circuitBoardName: '主控制板',
          semiProductCode: 'SP001',
          semiProductName: '主控制板半成品A',
          schematicFileUrl: 'http://example.com/schematic1.pdf',
          schematicFileName: '原理图A.pdf',
          smtFileUrl: 'http://example.com/smt1.pdf',
          smtFileName: 'SMT文件A.pdf',
          status: 1,
          createTime: '2024-01-16 14:20:00',
          updateTime: '2024-01-16 14:20:00',
          showLedBoardPlugin: false,
          ledBoardPluginSemiProductDTOList: [
            {
              id: 111,
              semiProductId: 11,
              semiProductCode: 'SP001',
              semiProductName: '主控制板半成品A',
              ledBoardPluginCode: 'LED001',
              ledBoardPluginName: '灯板插件A1',
              fileUrl: 'http://example.com/led1.pdf',
              fileName: '灯板插件规格书A1.pdf',
              status: 1,
              createTime: '2024-01-17 09:15:00',
              updateTime: '2024-01-17 09:15:00'
            },
            {
              id: 112,
              semiProductId: 11,
              semiProductCode: 'SP001',
              semiProductName: '主控制板半成品A',
              ledBoardPluginCode: 'LED002',
              ledBoardPluginName: '灯板插件A2',
              fileUrl: 'http://example.com/led2.pdf',
              fileName: '灯板插件规格书A2.pdf',
              status: 1,
              createTime: '2024-01-17 10:30:00',
              updateTime: '2024-01-17 10:30:00'
            }
          ]
        },
        {
          id: 12,
          circuitBoardId: 1,
          circuitBoardCode: 'PCB001',
          circuitBoardName: '主控制板',
          semiProductCode: 'SP002',
          semiProductName: '主控制板半成品B',
          schematicFileUrl: 'http://example.com/schematic2.pdf',
          schematicFileName: '原理图B.pdf',
          smtFileUrl: 'http://example.com/smt2.pdf',
          smtFileName: 'SMT文件B.pdf',
          status: 1,
          createTime: '2024-01-18 16:45:00',
          updateTime: '2024-01-18 16:45:00',
          showLedBoardPlugin: false,
          ledBoardPluginSemiProductDTOList: []
        }
      ]
    },
    {
      id: 2,
      boardCode: 'PCB002',
      boardName: '电源板',
      fileUrl: 'http://example.com/file2.pdf',
      fileName: '电源板规格书.pdf',
      status: 1,
      createTime: '2024-01-20 11:20:00',
      updateTime: '2024-01-20 11:20:00',
      semiProductDTOList: [
        {
          id: 21,
          circuitBoardId: 2,
          circuitBoardCode: 'PCB002',
          circuitBoardName: '电源板',
          semiProductCode: 'SP003',
          semiProductName: '电源板半成品C',
          schematicFileUrl: 'http://example.com/schematic3.pdf',
          schematicFileName: '原理图C.pdf',
          smtFileUrl: 'http://example.com/smt3.pdf',
          smtFileName: 'SMT文件C.pdf',
          status: 1,
          createTime: '2024-01-21 13:30:00',
          updateTime: '2024-01-21 13:30:00',
          showLedBoardPlugin: false,
          ledBoardPluginSemiProductDTOList: [
            {
              id: 211,
              semiProductId: 21,
              semiProductCode: 'SP003',
              semiProductName: '电源板半成品C',
              ledBoardPluginCode: 'LED003',
              ledBoardPluginName: '灯板插件C1',
              fileUrl: 'http://example.com/led3.pdf',
              fileName: '灯板插件规格书C1.pdf',
              status: 1,
              createTime: '2024-01-22 08:45:00',
              updateTime: '2024-01-22 08:45:00'
            }
          ]
        }
      ]
    }
  ]
  
  return mockData
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.boardCode = ''
  searchForm.boardName = ''
  searchForm.semiProductCode = ''
  searchForm.semiProductName = ''
  handleSearch()
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  loadData()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadData()
}

// 展开/收起灯板插件
const toggleLedBoardPlugin = (semi) => {
  semi.showLedBoardPlugin = !semi.showLedBoardPlugin
}

// 文件下载/预览 - 使用预签名链接
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
    const response = await axios.get(
      `${baseUrl}/minio/buckets/${bucketName}/files/${objectName}/presigned-url`
    )
    
    if (response.data.code === 200) {
      const downloadUrl = response.data.data
      
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

// 日期格式化
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 19)
}

// 新增线路板
const handleAdd = () => {
  boardDialog.title = '新增线路板'
  boardDialog.form = {
    id: null,
    boardCode: '',
    boardName: '',
    fileId: null,
    fileUrl: '',
    fileName: '',
    status: 1
  }
  boardDialog.fileList = []
  boardDialog.visible = true
}

// 管理员验证
const authenticateAdmin = () => {
  if (passwordInput.value === ADMIN_PASSWORD) {
    isAuthenticated.value = true
    showPasswordDialog.value = false
    passwordInput.value = ''
    ElMessage.success('管理员验证成功')
  } else {
    ElMessage.error('密码错误，请重新输入')
    passwordInput.value = ''
  }
}



// 编辑线路板
const handleEdit = (row) => {
  boardDialog.title = '编辑线路板'
  // 使用深拷贝避免引用共享问题
  boardDialog.form = {
    id: row.id,
    boardCode: row.boardCode,
    boardName: row.boardName,
    fileId: row.fileId,
    fileUrl: row.fileUrl,
    fileName: row.fileName,
    status: row.status
  }
  boardDialog.fileList = []
  if (row.fileUrl) {
    boardDialog.fileList.push({
      name: row.fileName,
      url: row.fileUrl
    })
  }
  boardDialog.visible = true
}

// 删除线路板
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该线路板吗？此操作将同时删除关联的半成品信息及文件。', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 首先删除关联的半成品及其文件
    if (row.semiProductDTOList && row.semiProductDTOList.length > 0) {
      for (const semi of row.semiProductDTOList) {
        // 删除半成品的原理图文件
        if (semi.schematicFileId && semi.schematicFileUrl) {
          await deleteFileFromMinIO(semi.schematicFileUrl, 'schematic', semi.schematicFileId)
        }
        // 删除半成品的SMT文件
        if (semi.smtFileId && semi.smtFileUrl) {
          await deleteFileFromMinIO(semi.smtFileUrl, 'smt', semi.smtFileId)
        }
        
        // 删除灯板插件及其文件
        if (semi.ledBoardPluginSemiProductDTOList && semi.ledBoardPluginSemiProductDTOList.length > 0) {
          for (const led of semi.ledBoardPluginSemiProductDTOList) {
            if (led.fileId && led.fileUrl) {
              await deleteFileFromMinIO(led.fileUrl, 'led-board-plugin', led.fileId)
            }
            // 删除灯板插件记录
            await axios.delete(`${baseUrl}/led-board-plugin-semi-product/delete/${led.id}`)
          }
        }
        
        // 删除半成品记录
        await axios.delete(`${baseUrl}/semi-product/delete/${semi.id}`)
      }
    }
    
    // 删除线路板的PCB文件
    if (row.fileId && row.fileUrl) {
      await deleteFileFromMinIO(row.fileUrl, 'circuit-board', row.fileId)
    }
    
    // 最后删除线路板记录
    const response = await axios.delete(`${baseUrl}/circuit-board/delete/${row.id}`)
    
    if (response.data.code === 200) {
      ElMessage.success('删除成功，已同时删除关联的半成品信息及文件')
      loadData()
    } else {
      ElMessage.error('删除失败: ' + response.data.msg)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + error.message)
    }
  }
}

// 添加半成品
const handleAddSemiProduct = async (row) => {
  try {
    // 加载线路板选项
    await loadCircuitBoardOptions()
    
    semiProductDialog.title = '新增半成品'
    semiProductDialog.form = {
      id: null,
      circuitBoardId: row.id,
      semiProductCode: '',
      semiProductName: '',
      schematicFileId: null,
      smtFileId: null,
      status: 1
    }
    semiProductDialog.schematicFileList = []
    semiProductDialog.smtFileList = []
    semiProductDialog.visible = true
  } catch (error) {
    ElMessage.error('加载线路板选项失败: ' + error.message)
  }
}

// 编辑半成品
const handleEditSemiProduct = async (semi) => {
  try {
    // 加载线路板选项
    await loadCircuitBoardOptions()
    
    semiProductDialog.title = '编辑半成品'
    semiProductDialog.form = {
      id: semi.id,
      circuitBoardId: semi.circuitBoardId,
      semiProductCode: semi.semiProductCode,
      semiProductName: semi.semiProductName,
      schematicFileId: semi.schematicFileId,
      smtFileId: semi.smtFileId,
      status: semi.status
    }
    
    // 设置文件列表
    semiProductDialog.schematicFileList = []
    semiProductDialog.smtFileList = []
    
    if (semi.schematicFileUrl) {
      semiProductDialog.schematicFileList.push({
        name: semi.schematicFileName,
        url: semi.schematicFileUrl
      })
    }
    
    if (semi.smtFileUrl) {
      semiProductDialog.smtFileList.push({
        name: semi.smtFileName,
        url: semi.smtFileUrl
      })
    }
    
    semiProductDialog.visible = true
  } catch (error) {
    ElMessage.error('加载线路板选项失败: ' + error.message)
  }
}

// 删除半成品
const handleDeleteSemiProduct = async (semi) => {
  try {
    await ElMessageBox.confirm('确认删除该半成品吗？此操作将同时删除关联的文件。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 首先删除半成品的原理图文件
    if (semi.schematicFileId && semi.schematicFileUrl) {
      await deleteFileFromMinIO(semi.schematicFileUrl, 'schematic', semi.schematicFileId)
    }
    
    // 删除半成品的SMT文件
    if (semi.smtFileId && semi.smtFileUrl) {
      await deleteFileFromMinIO(semi.smtFileUrl, 'smt', semi.smtFileId)
    }
    
    // 删除半成品记录
    const response = await axios.delete(`${baseUrl}/semi-product/delete/${semi.id}`)
    
    if (response.data.code === 200) {
      ElMessage.success('删除成功，已同时删除关联的文件')
      loadData()
    } else {
      ElMessage.error('删除失败: ' + response.data.msg)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + error.message)
    }
  }
}

// 添加灯板插件
const handleAddLedBoardPlugin = async (semi) => {
  try {
    // 加载半成品选项
    await loadSemiProductOptions()
    
    ledBoardPluginDialog.title = '新增灯板插件'
    ledBoardPluginDialog.form = {
      id: null,
      semiProductId: semi.id,
      ledBoardPluginCode: '',
      ledBoardPluginName: '',
      fileId: null,
      status: 1
    }
    ledBoardPluginDialog.fileList = []
    ledBoardPluginDialog.visible = true
  } catch (error) {
    ElMessage.error('加载半成品选项失败: ' + error.message)
  }
}

// 编辑灯板插件
const handleEditLedBoardPlugin = async (led) => {
  try {
    // 加载半成品选项
    await loadSemiProductOptions()
    
    ledBoardPluginDialog.title = '编辑灯板插件'
    ledBoardPluginDialog.form = {
      id: led.id,
      semiProductId: led.semiProductId,
      ledBoardPluginCode: led.ledBoardPluginCode,
      ledBoardPluginName: led.ledBoardPluginName,
      fileId: led.fileId,
      status: led.status
    }
    
    // 设置文件列表
    ledBoardPluginDialog.fileList = []
    if (led.fileUrl) {
      ledBoardPluginDialog.fileList.push({
        name: led.fileName,
        url: led.fileUrl
      })
    }
    
    ledBoardPluginDialog.visible = true
  } catch (error) {
    ElMessage.error('加载半成品选项失败: ' + error.message)
  }
}

// 删除灯板插件
const handleDeleteLedBoardPlugin = async (led) => {
  try {
    await ElMessageBox.confirm('确认删除该灯板插件吗？此操作将同时删除关联的文件。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 首先删除灯板插件的文件
    if (led.fileId && led.fileUrl) {
      await deleteFileFromMinIO(led.fileUrl, 'led-board-plugin', led.fileId)
    }
    
    // 删除灯板插件记录
    const response = await axios.delete(`${baseUrl}/led-board-plugin-semi-product/delete/${led.id}`)
    
    if (response.data.code === 200) {
      ElMessage.success('删除成功，已同时删除关联的文件')
      loadData()
    } else {
      ElMessage.error('删除失败: ' + response.data.msg)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + error.message)
    }
  }
}

// 消耗半成品
const handleConsumeSemiProduct = async (semi) => {
  try {
    await ElMessageBox.confirm('确认消耗该半成品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 使用现有的更新接口，将状态设置为已消耗（假设状态值为2表示已消耗）
    const consumeData = {
      id: semi.id,
      circuitBoardId: semi.circuitBoardId,
      semiProductCode: semi.semiProductCode,
      semiProductName: semi.semiProductName,
      schematicFileId: semi.schematicFileId || 999999999999,
      smtFileId: semi.smtFileId || 999999999999,
      status: 2 // 2表示已消耗状态
    }
    
    const response = await axios.put(`${baseUrl}/semi-product/update`, consumeData)
    
    if (response.data.code === 200) {
      ElMessage.success('消耗成功')
      loadData()
    } else {
      ElMessage.error('消耗失败: ' + response.data.msg)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('消耗失败: ' + error.message)
    }
  }
}

// 切换半成品状态
const handleToggleSemiProductStatus = async (semi) => {
  try {
    // 如果当前是消耗状态（2），则切换到启用状态（1）
    // 如果当前是启用状态（1），则切换到停用状态（0）
    // 如果当前是停用状态（0），则切换到启用状态（1）
    const newStatus = semi.status === 2 ? 1 : (semi.status === 1 ? 0 : 1)
    const statusText = newStatus === 1 ? '启用' : '停用'
    
    await ElMessageBox.confirm(`确认${statusText}该半成品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 使用现有的更新接口来切换状态
    const statusData = {
      id: semi.id,
      circuitBoardId: semi.circuitBoardId,
      semiProductCode: semi.semiProductCode,
      semiProductName: semi.semiProductName,
      schematicFileId: semi.schematicFileId || 999999999999,
      smtFileId: semi.smtFileId || 999999999999,
      status: newStatus
    }
    
    const response = await axios.put(`${baseUrl}/semi-product/update`, statusData)
    
    if (response.data.code === 200) {
      ElMessage.success(`${statusText}成功`)
      loadData()
    } else {
      ElMessage.error(`${statusText}失败: ` + response.data.msg)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('状态切换失败: ' + error.message)
    }
  }
}

// 文件上传处理
const handleBoardUploadSuccess = (response, file, fileList) => {
  if (response.code === 200) {
    boardDialog.form.fileUrl = response.data.fileUrl
    boardDialog.form.fileName = response.data.fileName
    boardDialog.form.fileId = response.data.fileId
  } else {
    ElMessage.error('文件上传失败: ' + response.message)
  }
}

const handleBoardUploadRemove = async (file, fileList) => {
  // 如果存在文件ID，需要物理删除数据库记录和MinIO文件
  if (boardDialog.form.fileId) {
    try {
      await ElMessageBox.confirm(
        '确定要删除该文件吗？此操作将同时删除数据库记录和MinIO文件。',
        '确认删除',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      
      // 获取文件信息以构建删除URL
      const fileInfoResponse = await axios.get(`${baseUrl}/file-info/${boardDialog.form.fileId}`)
      console.log('线路板文件信息接口返回数据:', fileInfoResponse.data)
      if (fileInfoResponse.data.code === 200 && fileInfoResponse.data.data) {
        const fileInfo = fileInfoResponse.data.data
        console.log('线路板文件信息详情:', fileInfo)
        
        // 从fileUrl中提取桶名称和对象名称
        let bucketName, objectName
        if (fileInfo.fileUrl) {
          const urlMatch = fileInfo.fileUrl.match(/\/minio\/buckets\/([^\/]+)\/files\/(.+)/)
          if (urlMatch) {
            bucketName = urlMatch[1]
            objectName = urlMatch[2]
          }
        }
        
        console.log('提取的线路板桶名称:', bucketName)
        console.log('提取的线路板文件名称:', objectName)
        
        if (!bucketName || !objectName) {
          console.error('无法从线路板文件信息中提取桶名称或文件名称，可用字段:', Object.keys(fileInfo))
          ElMessage.error('线路板文件信息格式错误，无法删除MinIO文件')
        } else {
        
          try {
            // 先删除MinIO文件
            await axios.delete(`${baseUrl}/minio/buckets/${bucketName}/files/${objectName}`)
          } catch (minioError) {
            console.warn('删除MinIO文件失败:', minioError)
            // MinIO删除失败也继续删除数据库记录
          }
          
          // 再删除数据库记录
          await axios.delete(`${baseUrl}/file-info/${boardDialog.form.fileId}`)
          
          ElMessage.success('线路板文件删除成功')
        }
      } else {
        console.error('获取线路板文件信息失败:', fileInfoResponse.data)
        ElMessage.error('获取线路板文件信息失败，无法删除文件')
      }
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除文件失败: ' + (error.response?.data?.msg || error.message))
        return // 如果用户取消或删除失败，不清空表单
      }
    }
  }
  
  // 清空表单字段
  boardDialog.form.fileUrl = ''
  boardDialog.form.fileName = ''
  boardDialog.form.fileId = null
}

// 半成品文件上传处理
const handleSchematicUploadSuccess = (response, file, fileList) => {
  if (response.code === 200) {
    semiProductDialog.form.schematicFileId = response.data.fileId
  } else {
    ElMessage.error('原理图文件上传失败: ' + response.message)
  }
}

const handleSchematicUploadRemove = async (file, fileList) => {
  // 如果存在原理图文件ID，需要物理删除数据库记录和MinIO文件
  if (semiProductDialog.form.schematicFileId) {
    try {
      await ElMessageBox.confirm(
        '确定要删除原理图文件吗？此操作将同时删除数据库记录和MinIO文件。',
        '确认删除',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      
      // 获取文件信息以构建删除URL
      const fileInfoResponse = await axios.get(`${baseUrl}/file-info/${semiProductDialog.form.schematicFileId}`)
      console.log('原理图文件信息接口返回数据:', fileInfoResponse.data)
      if (fileInfoResponse.data.code === 200 && fileInfoResponse.data.data) {
        const fileInfo = fileInfoResponse.data.data
        console.log('原理图文件信息详情:', fileInfo)
        
        // 从fileUrl中提取桶名称和对象名称
        let bucketName, objectName
        if (fileInfo.fileUrl) {
          const urlMatch = fileInfo.fileUrl.match(/\/minio\/buckets\/([^\/]+)\/files\/(.+)/)
          if (urlMatch) {
            bucketName = urlMatch[1]
            objectName = urlMatch[2]
          }
        }
        
        console.log('提取的原理图桶名称:', bucketName)
        console.log('提取的原理图文件名称:', objectName)
        
        if (!bucketName || !objectName) {
          console.error('无法从原理图文件信息中提取桶名称或文件名称，可用字段:', Object.keys(fileInfo))
          ElMessage.error('原理图文件信息格式错误，无法删除MinIO文件')
        } else {
        
        try {
          // 先删除MinIO文件
          await axios.delete(`${baseUrl}/minio/buckets/${bucketName}/files/${objectName}`)
        } catch (minioError) {
          console.warn('删除MinIO文件失败:', minioError)
          // MinIO删除失败也继续删除数据库记录
        }
        
          // 再删除数据库记录
          await axios.delete(`${baseUrl}/file-info/${semiProductDialog.form.schematicFileId}`)
          
          ElMessage.success('原理图文件删除成功')
        }
        }
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除原理图文件失败: ' + (error.response?.data?.msg || error.message))
        return // 如果用户取消或删除失败，不清空表单
      }
    }
  }
  
  // 清空表单字段
  semiProductDialog.form.schematicFileId = null
}

const handleSmtUploadSuccess = (response, file, fileList) => {
  if (response.code === 200) {
    semiProductDialog.form.smtFileId = response.data.fileId
  } else {
    ElMessage.error('SMT文件上传失败: ' + response.message)
  }
}

const handleSmtUploadRemove = async (file, fileList) => {
  // 如果存在SMT文件ID，需要物理删除数据库记录和MinIO文件
  if (semiProductDialog.form.smtFileId) {
    try {
      await ElMessageBox.confirm(
        '确定要删除SMT文件吗？此操作将同时删除数据库记录和MinIO文件。',
        '确认删除',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      
      // 获取文件信息以构建删除URL
      const fileInfoResponse = await axios.get(`${baseUrl}/file-info/${semiProductDialog.form.smtFileId}`)
      console.log('SMT文件信息接口返回数据:', fileInfoResponse.data)
      if (fileInfoResponse.data.code === 200 && fileInfoResponse.data.data) {
        const fileInfo = fileInfoResponse.data.data
        console.log('SMT文件信息详情:', fileInfo)
        
        // 从fileUrl中提取桶名称和对象名称
        let bucketName, objectName
        if (fileInfo.fileUrl) {
          const urlMatch = fileInfo.fileUrl.match(/\/minio\/buckets\/([^\/]+)\/files\/(.+)/)
          if (urlMatch) {
            bucketName = urlMatch[1]
            objectName = urlMatch[2]
          }
        }
        console.log('提取的SMT桶名称:', bucketName)
        console.log('提取的SMT文件名称:', objectName)
        
        if (!bucketName || !objectName) {
          console.error('无法从SMT文件信息中提取桶名称或文件名称，可用字段:', Object.keys(fileInfo))
          ElMessage.error('SMT文件信息格式错误，无法删除MinIO文件')
        } else {
        
        try {
          // 先删除MinIO文件
          await axios.delete(`${baseUrl}/minio/buckets/${bucketName}/files/${objectName}`)
        } catch (minioError) {
          console.warn('删除MinIO文件失败:', minioError)
          // MinIO删除失败也继续删除数据库记录
        }
        
          // 再删除数据库记录
          await axios.delete(`${baseUrl}/file-info/${semiProductDialog.form.smtFileId}`)
          
          ElMessage.success('SMT文件删除成功')
        }
        }
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除SMT文件失败: ' + (error.response?.data?.msg || error.message))
        return // 如果用户取消或删除失败，不清空表单
      }
    }
  }
  
  // 清空表单字段
  semiProductDialog.form.smtFileId = null
}

// 灯板插件文件上传处理
const handleLedBoardPluginUploadSuccess = (response, file, fileList) => {
  if (response.code === 200) {
    ledBoardPluginDialog.form.fileId = response.data.fileId
  } else {
    ElMessage.error('文件上传失败: ' + response.message)
  }
}

const handleLedBoardPluginUploadRemove = async (file, fileList) => {
  // 如果存在灯板插件文件ID，需要物理删除数据库记录和MinIO文件
  if (ledBoardPluginDialog.form.fileId) {
    try {
      await ElMessageBox.confirm(
        '确定要删除灯板插件文件吗？此操作将同时删除数据库记录和MinIO文件。',
        '确认删除',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      
      // 获取文件信息以构建删除URL
      const fileInfoResponse = await axios.get(`${baseUrl}/file-info/${ledBoardPluginDialog.form.fileId}`)
      console.log('灯板插件文件信息接口返回数据:', fileInfoResponse.data)
      if (fileInfoResponse.data.code === 200 && fileInfoResponse.data.data) {
        const fileInfo = fileInfoResponse.data.data
        console.log('灯板插件文件信息详情:', fileInfo)
        
        // 从fileUrl中提取桶名称和对象名称
        let bucketName, objectName
        if (fileInfo.fileUrl) {
          const urlMatch = fileInfo.fileUrl.match(/\/minio\/buckets\/([^\/]+)\/files\/(.+)/)
          if (urlMatch) {
            bucketName = urlMatch[1]
            objectName = urlMatch[2]
          }
        }
        console.log('提取的灯板插件桶名称:', bucketName)
        console.log('提取的灯板插件文件名称:', objectName)
        
        if (!bucketName || !objectName) {
          console.error('无法从灯板插件文件信息中提取桶名称或文件名称，可用字段:', Object.keys(fileInfo))
          ElMessage.error('灯板插件文件信息格式错误，无法删除MinIO文件')
        } else {
        
        try {
          // 先删除MinIO文件
          await axios.delete(`${baseUrl}/minio/buckets/${bucketName}/files/${objectName}`)
        } catch (minioError) {
          console.warn('删除MinIO文件失败:', minioError)
          // MinIO删除失败也继续删除数据库记录
        }
        
          // 再删除数据库记录
          await axios.delete(`${baseUrl}/file-info/${ledBoardPluginDialog.form.fileId}`)
          
          ElMessage.success('灯板插件文件删除成功')
        }
        }
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除灯板插件文件失败: ' + (error.response?.data?.msg || error.message))
        return // 如果用户取消或删除失败，不清空表单
      }
    }
  }
  
  // 清空表单字段
  ledBoardPluginDialog.form.fileId = null
}

// 预上传文件处理 - 原理图
const handleSchematicUpload = async (options) => {
  const { file, onSuccess, onError, onProgress } = options
  
  try {
    // 获取当前半成品信息用于生成格式化文件名
    const semiProductCode = semiProductDialog.form.semiProductCode || 'UNKNOWN'
    const semiProductName = semiProductDialog.form.semiProductName || '原理图'
    
    // 第一步：创建格式化文件名预上传任务
    const presignResponse = await axios.post(
      `${baseUrl}/minio/buckets/${schematicBucket.value}/files/formatted-presigned-upload`,
      null,
      {
        params: {
          code: semiProductCode,
          name: semiProductName,
          originalFileName: file.name,
          fileSize: file.size
        }
      }
    )
    
    if (presignResponse.data.code !== 200) {
      throw new Error(presignResponse.data.msg || '创建预上传任务失败')
    }
    
    const { presignedUrl, objectName: formattedFileName } = presignResponse.data.data
    
    // 第二步：使用预签名URL直接上传文件到MinIO
    await uploadFileWithPresignedUrl(file, presignedUrl, onProgress)
    
    // 第三步：保存文件信息到数据库
    const saveFileResponse = await axios.post(`${baseUrl}/minio/buckets/${schematicBucket.value}/files/save-info`, {
      bucketName: schematicBucket.value,
      objectName: formattedFileName,
      originalName: file.name,
      fileSize: file.size,
      contentType: file.type || 'application/octet-stream'
    })
    
    if (saveFileResponse.data.code !== 200) {
      throw new Error('保存文件信息失败: ' + saveFileResponse.data.msg)
    }
    
    // 模拟原上传成功回调格式
    const fileUrl = `${baseUrl}/minio/buckets/${schematicBucket.value}/files/${encodeURIComponent(formattedFileName)}`
    const mockResponse = {
      code: 200,
      message: '上传成功',
      data: {
        fileUrl: fileUrl,
        fileName: file.name,
        fileId: saveFileResponse.data.data?.fileId || null
      }
    }
    
    // 调用原成功处理函数
    handleSchematicUploadSuccess(mockResponse, file, [file])
    onSuccess(mockResponse)
    
    ElMessage.success(`原理图文件上传成功，文件名: ${formattedFileName}`)
    
  } catch (error) {
    onError(error)
    ElMessage.error('原理图文件上传失败: ' + error.message)
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

// 预上传文件处理 - SMT
const handleSmtUpload = async (options) => {
  const { file, onSuccess, onError, onProgress } = options
  
  try {
    // 获取当前半成品信息用于生成格式化文件名
    const semiProductCode = semiProductDialog.form.semiProductCode || 'UNKNOWN'
    const semiProductName = semiProductDialog.form.semiProductName || 'SMT文件'
    
    // 第一步：创建格式化文件名预上传任务
    const presignResponse = await axios.post(
      `${baseUrl}/minio/buckets/${smtBucket.value}/files/formatted-presigned-upload`,
      null,
      {
        params: {
          code: semiProductCode,
          name: semiProductName,
          originalFileName: file.name,
          fileSize: file.size
        }
      }
    )
    
    if (presignResponse.data.code !== 200) {
      throw new Error(presignResponse.data.msg || '创建预上传任务失败')
    }
    
    const { presignedUrl, objectName: formattedFileName } = presignResponse.data.data
    
    // 第二步：使用预签名URL直接上传文件到MinIO
    await uploadFileWithPresignedUrl(file, presignedUrl, onProgress)
    
    // 保存文件信息到数据库
    const saveFileResponse = await axios.post(`${baseUrl}/minio/buckets/${smtBucket.value}/files/save-info`, {
      bucketName: smtBucket.value,
      objectName: formattedFileName,
      originalName: file.name,
      fileSize: file.size,
      contentType: file.type || 'application/octet-stream'
    })
    
    if (saveFileResponse.data.code !== 200) {
      throw new Error('保存文件信息失败: ' + saveFileResponse.data.msg)
    }
    
    // 模拟原上传成功回调格式
    const fileUrl = `${baseUrl}/minio/buckets/${smtBucket.value}/files/${encodeURIComponent(formattedFileName)}`
    const mockResponse = {
      code: 200,
      message: '上传成功',
      data: {
        fileUrl: fileUrl,
        fileName: file.name,
        fileId: saveFileResponse.data.data?.fileId || null
      }
    }
    
    // 调用原成功处理函数
    handleSmtUploadSuccess(mockResponse, file, [file])
    onSuccess(mockResponse)
    
    ElMessage.success(`SMT文件上传成功，文件名: ${formattedFileName}`)
    
  } catch (error) {
    onError(error)
    ElMessage.error('SMT文件上传失败: ' + error.message)
  }
}

// 预上传文件处理 - 灯板插件
const handleLedBoardPluginUpload = async (options) => {
  const { file, onSuccess, onError, onProgress } = options
  
  try {
    // 获取当前灯板插件信息用于生成格式化文件名
    const ledBoardPluginCode = ledBoardPluginDialog.form.ledBoardPluginCode || 'UNKNOWN'
    const ledBoardPluginName = ledBoardPluginDialog.form.ledBoardPluginName || '灯板插件'
    
    // 第一步：创建格式化文件名预上传任务
    const presignResponse = await axios.post(
      `${baseUrl}/minio/buckets/${ledBoardPluginBucket.value}/files/formatted-presigned-upload`,
      null,
      {
        params: {
          code: ledBoardPluginCode,
          name: ledBoardPluginName,
          originalFileName: file.name,
          fileSize: file.size
        }
      }
    )
    
    if (presignResponse.data.code !== 200) {
      throw new Error(presignResponse.data.msg || '创建预上传任务失败')
    }
    
    const { presignedUrl, objectName: formattedFileName } = presignResponse.data.data
    
    // 第二步：使用预签名URL直接上传文件到MinIO
    await uploadFileWithPresignedUrl(file, presignedUrl, onProgress)
    
    // 保存文件信息到数据库
    const saveFileResponse = await axios.post(`${baseUrl}/minio/buckets/${ledBoardPluginBucket.value}/files/save-info`, {
      bucketName: ledBoardPluginBucket.value,
      objectName: formattedFileName,
      originalName: file.name,
      fileSize: file.size,
      contentType: file.type || 'application/octet-stream'
    })
    
    if (saveFileResponse.data.code !== 200) {
      throw new Error('保存文件信息失败: ' + saveFileResponse.data.msg)
    }
    
    // 模拟原上传成功回调格式
    const fileUrl = `${baseUrl}/minio/buckets/${ledBoardPluginBucket.value}/files/${encodeURIComponent(formattedFileName)}`
    const mockResponse = {
      code: 200,
      message: '上传成功',
      data: {
        fileUrl: fileUrl,
        fileName: file.name,
        fileId: saveFileResponse.data.data?.fileId || null
      }
    }
    
    // 调用原成功处理函数
    handleLedBoardPluginUploadSuccess(mockResponse, file, [file])
    onSuccess(mockResponse)
    
    ElMessage.success(`灯板插件文件上传成功，文件名: ${formattedFileName}`)
    
  } catch (error) {
    onError(error)
    ElMessage.error('灯板插件文件上传失败: ' + error.message)
  }
}

// 预上传文件处理
const beforeBoardUpload = (file) => {
  // 文件大小限制 (50MB)
  const maxSize = 200 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过200MB')
    return false
  }
  return true
}

const handleBoardUpload = async (options) => {
  const { file, onSuccess, onError, onProgress } = options
  
  try {
    // 获取当前线路板信息用于生成格式化文件名
    const boardCode = boardDialog.form.boardCode || 'UNKNOWN'
    const boardName = boardDialog.form.boardName || '线路板文件'
    
    // 第一步：创建格式化文件名预上传任务
    const presignResponse = await axios.post(
      `${baseUrl}/minio/buckets/${circuitBoardBucket.value}/files/formatted-presigned-upload`,
      null,
      {
        params: {
          code: boardCode,
          name: boardName,
          originalFileName: file.name,
          fileSize: file.size
        }
      }
    )
    
    if (presignResponse.data.code !== 200) {
      throw new Error(presignResponse.data.msg || '创建预上传任务失败')
    }
    
    const { presignedUrl, objectName: formattedFileName } = presignResponse.data.data
    
    // 第二步：使用预签名URL直接上传文件到MinIO
    await uploadFileWithPresignedUrl(file, presignedUrl, onProgress)
    
    // 保存文件信息到数据库
    const saveFileResponse = await axios.post(`${baseUrl}/minio/buckets/${circuitBoardBucket.value}/files/save-info`, {
      bucketName: circuitBoardBucket.value,
      objectName: formattedFileName,
      originalName: file.name,
      fileSize: file.size,
      contentType: file.type || 'application/octet-stream'
    })
    
    if (saveFileResponse.data.code !== 200) {
      throw new Error('保存文件信息失败: ' + saveFileResponse.data.msg)
    }
    
    // 模拟原上传成功回调格式
    const fileUrl = `${baseUrl}/minio/buckets/${circuitBoardBucket.value}/files/${encodeURIComponent(formattedFileName)}`
    const mockResponse = {
      code: 200,
      message: '上传成功',
      data: {
        fileUrl: fileUrl,
        fileName: file.name,
        fileId: saveFileResponse.data.data?.fileId || null
      }
    }
    
    // 调用原成功处理函数
    handleBoardUploadSuccess(mockResponse, file, [file])
    onSuccess(mockResponse)
    
    ElMessage.success(`文件上传成功，文件名: ${formattedFileName}`)
    
  } catch (error) {
    ElMessage.error('文件上传失败: ' + error.message)
    onError(error)
  }
}

// 保存线路板
const saveBoard = async () => {
  try {
    await boardFormRef.value.validate()
    
    const url = boardDialog.form.id 
      ? `${baseUrl}/circuit-board/update`
      : `${baseUrl}/circuit-board/add`
    const method = boardDialog.form.id ? 'put' : 'post'
    
    const response = await axios[method](url, boardDialog.form)
    
    if (response.data.code === 200) {
      ElMessage.success('保存成功')
      boardDialog.visible = false
      loadData()
    } else {
      ElMessage.error('保存失败: ' + response.data.msg)
    }
  } catch (error) {
    ElMessage.error('保存失败: ' + error.message)
  }
}

// 保存半成品
const saveSemiProduct = async () => {
  try {
    await semiProductFormRef.value.validate()
    
    const formData = {
      ...semiProductDialog.form
    }
    
    let response
    if (formData.id) {
      // 编辑
      response = await axios.put(`${baseUrl}/semi-product/update`, formData)
    } else {
      // 新增
      response = await axios.post(`${baseUrl}/semi-product/add`, formData)
    }
    
    if (response.data.code === 200) {
      ElMessage.success('保存成功')
      semiProductDialog.visible = false
      loadData()
    } else {
      ElMessage.error('保存失败: ' + response.data.msg)
    }
  } catch (error) {
    if (error !== false) {
      ElMessage.error('保存失败: ' + error.message)
    }
  }
}

// 保存灯板插件
const saveLedBoardPlugin = async () => {
  try {
    await ledBoardPluginFormRef.value.validate()
    
    const formData = {
      ...ledBoardPluginDialog.form
    }
    
    let response
    if (formData.id) {
      // 编辑
      response = await axios.put(`${baseUrl}/led-board-plugin-semi-product/update`, formData)
    } else {
      // 新增
      response = await axios.post(`${baseUrl}/led-board-plugin-semi-product/add`, formData)
    }
    
    if (response.data.code === 200) {
      ElMessage.success('保存成功')
      ledBoardPluginDialog.visible = false
      loadData()
    } else {
      ElMessage.error('保存失败: ' + response.data.msg)
    }
  } catch (error) {
    if (error !== false) {
      ElMessage.error('保存失败: ' + error.message)
    }
  }
}

// 加载线路板选项
const loadCircuitBoardOptions = async () => {
  try {
    const response = await axios.get(`${baseUrl}/circuit-board/all`)
    if (response.data.code === 200) {
      circuitBoardOptions.value = response.data.data
    } else {
      ElMessage.error('加载线路板选项失败: ' + response.data.msg)
    }
  } catch (error) {
    ElMessage.error('加载线路板选项失败: ' + error.message)
  }
}

// 加载半成品选项
const loadSemiProductOptions = async () => {
  try {
    const response = await axios.get(`${baseUrl}/semi-product/all`)
    if (response.data.code === 200) {
      semiProductOptions.value = response.data.data
    } else {
      ElMessage.error('加载半成品选项失败: ' + response.data.msg)
    }
  } catch (error) {
    ElMessage.error('加载半成品选项失败: ' + error.message)
  }
}

// MES接口调用 - 根据物料编号查询物料信息
const getItemInfoFromMES = async (itemCode) => {
  try {
    const response = await axios.get(`${baseUrl}/mes/item-info/${itemCode}`)
    
    if (response.data.code === 200) {
      return response.data.data
    } else {
      console.warn('MES物料信息查询失败: ' + response.data.msg)
      return null
    }
  } catch (error) {
    console.warn('MES接口调用失败: ' + error.message)
    return null
  }
}

// 存储定时器ID的对象
const inputTimers = reactive({
  boardCode: null,
  semiProductCode: null,
  ledBoardPluginCode: null
})

// 线路板编码输入监听 - 自动查询MES接口
const handleBoardCodeInput = () => {
  const boardCode = boardDialog.form.boardCode?.trim()
  
  // 清除之前的定时器
  if (inputTimers.boardCode) {
    clearTimeout(inputTimers.boardCode)
  }
  
  if (boardCode) {
    // 延迟查询，避免用户输入过程中频繁调用
    inputTimers.boardCode = setTimeout(async () => {
      // 如果当前输入值与之前相同，则进行查询
      if (boardDialog.form.boardCode?.trim() === boardCode) {
        const itemInfo = await getItemInfoFromMES(boardCode)
        if (itemInfo && itemInfo.itemSpec) {
          boardDialog.form.boardName = itemInfo.itemSpec
          ElMessage.success(`已从MES系统获取物料规格: ${itemInfo.itemSpec}`)
        }
      }
      inputTimers.boardCode = null
    }, 1000) // 1秒延迟
  }
}

// 半成品编码输入监听 - 自动查询MES接口
const handleSemiProductCodeInput = () => {
  const semiProductCode = semiProductDialog.form.semiProductCode?.trim()
  
  // 清除之前的定时器
  if (inputTimers.semiProductCode) {
    clearTimeout(inputTimers.semiProductCode)
  }
  
  if (semiProductCode) {
    inputTimers.semiProductCode = setTimeout(async () => {
      if (semiProductDialog.form.semiProductCode?.trim() === semiProductCode) {
        const itemInfo = await getItemInfoFromMES(semiProductCode)
        if (itemInfo && itemInfo.itemSpec) {
          semiProductDialog.form.semiProductName = itemInfo.itemSpec
          ElMessage.success(`已从MES系统获取物料规格: ${itemInfo.itemSpec}`)
        }
      }
      inputTimers.semiProductCode = null
    }, 1000)
  }
}

// 灯板插件编码输入监听 - 自动查询MES接口
const handleLedBoardPluginCodeInput = () => {
  const ledBoardPluginCode = ledBoardPluginDialog.form.ledBoardPluginCode?.trim()
  
  // 清除之前的定时器
  if (inputTimers.ledBoardPluginCode) {
    clearTimeout(inputTimers.ledBoardPluginCode)
  }
  
  if (ledBoardPluginCode) {
    inputTimers.ledBoardPluginCode = setTimeout(async () => {
      if (ledBoardPluginDialog.form.ledBoardPluginCode?.trim() === ledBoardPluginCode) {
        const itemInfo = await getItemInfoFromMES(ledBoardPluginCode)
        if (itemInfo && itemInfo.itemSpec) {
          ledBoardPluginDialog.form.ledBoardPluginName = itemInfo.itemSpec
          ElMessage.success(`已从MES系统获取物料规格: ${itemInfo.itemSpec}`)
        }
      }
      inputTimers.ledBoardPluginCode = null
    }, 1000)
  }
}

// 消耗线路板
const handleConsumeCircuitBoard = async (row) => {
  try {
    await ElMessageBox.confirm('确认消耗该线路板吗？此操作将同时消耗该线路板下的所有半成品。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 使用现有的更新接口来消耗线路板，将状态改为2（已消耗）
    const consumeData = {
      id: row.id,
      boardCode: row.boardCode,
      boardName: row.boardName,
      fileId: row.fileId || 999999999999,
      status: 2
    }
    
    const response = await axios.put(`${baseUrl}/circuit-board/update`, consumeData)
    
    if (response.data.code === 200) {
      // 如果线路板消耗成功，同时消耗该线路板下的所有半成品
      if (row.semiProductDTOList && row.semiProductDTOList.length > 0) {
        let successCount = 0
        let failCount = 0
        
        for (const semi of row.semiProductDTOList) {
          // 消耗所有非消耗状态的半成品（状态不为2的）
          if (semi.status !== 2) {
            try {
              const semiConsumeData = {
                id: semi.id,
                semiProductCode: semi.semiProductCode,
                semiProductName: semi.semiProductName,
                boardId: semi.boardId,
                schematicFileId: semi.schematicFileId || 999999999999,
                smtFileId: semi.smtFileId || 999999999999,
                status: 2
              }
              
              const semiResponse = await axios.put(`${baseUrl}/semi-product/update`, semiConsumeData)
              if (semiResponse.data.code === 200) {
                successCount++
              } else {
                failCount++
              }
            } catch (error) {
              failCount++
            }
          }
        }
        
        let message = '线路板消耗成功'
        if (successCount > 0) {
          message += `，同时成功消耗了 ${successCount} 个半成品`
        }
        if (failCount > 0) {
          message += `，${failCount} 个半成品消耗失败`
        }
        ElMessage.success(message)
      } else {
        ElMessage.success('线路板消耗成功')
      }
      loadData()
    } else {
      ElMessage.error('消耗失败: ' + response.data.msg)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('消耗失败: ' + error.message)
    }
  }
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
      await axios.delete(`${baseUrl}/minio/buckets/${bucketName}/files/${objectName}`)
      console.log(`${fileType}文件从MinIO删除成功`)
    } catch (minioError) {
      console.warn(`删除${fileType}MinIO文件失败:`, minioError)
      // MinIO删除失败也继续，不抛出错误
    }
    
    // 如果提供了fileId，也删除数据库记录
    if (fileId) {
      try {
        await axios.delete(`${baseUrl}/file-info/${fileId}`)
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

// 切换线路板状态
const handleToggleCircuitBoardStatus = async (row) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    const statusText = newStatus === 1 ? '启用' : '停用'
    
    await ElMessageBox.confirm(`确认${statusText}该线路板吗？此操作将同时${statusText}该线路板下的所有半成品。`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 使用现有的更新接口来切换状态
    const statusData = {
      id: row.id,
      boardCode: row.boardCode,
      boardName: row.boardName,
      fileId: row.fileId || 999999999999,
      status: newStatus
    }
    
    const response = await axios.put(`${baseUrl}/circuit-board/update`, statusData)
    
    if (response.data.code === 200) {
      // 如果线路板状态切换成功，同时切换该线路板下的所有半成品状态
      if (row.semiProductDTOList && row.semiProductDTOList.length > 0) {
        let successCount = 0
        let failCount = 0
        
        for (const semi of row.semiProductDTOList) {
          // 只对状态不同的半成品进行切换，包括消耗状态的半成品
          if (semi.status !== newStatus) {
            try {
              const semiStatusData = {
                id: semi.id,
                semiProductCode: semi.semiProductCode,
                semiProductName: semi.semiProductName,
                boardId: semi.boardId,
                schematicFileId: semi.schematicFileId || 999999999999,
                smtFileId: semi.smtFileId || 999999999999,
                status: newStatus
              }
              
              const semiResponse = await axios.put(`${baseUrl}/semi-product/update`, semiStatusData)
              if (semiResponse.data.code === 200) {
                successCount++
              } else {
                failCount++
              }
            } catch (error) {
              failCount++
            }
          }
        }
        
        let message = `${statusText}成功`
        if (successCount > 0) {
          message += `，同时成功${statusText}了 ${successCount} 个半成品`
        }
        if (failCount > 0) {
          message += `，${failCount} 个半成品${statusText}失败`
        }
        ElMessage.success(message)
      } else {
        ElMessage.success(`${statusText}成功`)
      }
      loadData()
    } else {
      ElMessage.error(`${statusText}失败: ` + response.data.msg)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('状态切换失败: ' + error.message)
    }
  }
}
</script>

<style scoped>
.circuit-board-manager {
  padding: 20px;
  background-color: #f8f9fa;
  min-height: 100vh;

  .card-header {
    padding: 0;
  }

  .header-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
    gap: 20px;
    padding: 10px 0;
  }

  .search-bar-inline {
    flex: 1;
    display: flex;
    align-items: center;
  }

  .search-bar-inline .el-form {
    margin-bottom: 0;
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .search-bar-inline .el-form-item {
    margin-bottom: 0;
    margin-right: 8px;
    display: flex;
    align-items: center;
  }

  .search-bar-inline .el-form-item__label {
    height: 32px;
    line-height: 32px;
    padding-right: 8px;
    margin-bottom: 0;
  }

  .search-bar-inline .el-input {
    height: 32px;
    line-height: 32px;
  }

  .search-bar-inline .el-input__inner {
    height: 32px;
    line-height: 32px;
  }

  .search-bar-inline .el-button {
    height: 32px;
    padding: 8px 15px;
    line-height: 1;
  }

  .header-buttons {
    display: flex;
    gap: 10px;
    flex-shrink: 0;
    align-items: center;
  }

  .header-buttons .el-button {
    height: 32px;
    padding: 8px 15px;
    line-height: 1;
    display: inline-flex;
    align-items: center;
    justify-content: center;
  }

  .search-bar {
    margin-bottom: 20px;
    padding: 15px;
    background-color: #ffffff;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  .table-container {
    margin-bottom: 20px;
  }

  .board-info {
    padding: 12px;
    background-color: #ffffff;
    border-radius: 8px;
    margin-bottom: 12px;
    border: 1px solid #efe9e9;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;
  }

  .board-info-enabled {
    background-color: #ffffff;
    border-color: #e2e0df;
  }

  .board-info-disabled {
    background-color: #fff1f0;
    border-color: #ffa39e;
  }

  .board-info-consumed {
    background-color: #fffbe6;
    border-color: #ffe58f;
  }

  .info-item {
    display: inline-flex;
    margin-right: 15px;
    margin-bottom: 6px;
    align-items: center;
    flex-wrap: nowrap;
    padding: 2px 0;
  }

  .info-item:last-child {
    margin-right: 0;
  }

  .info-item .label {
    font-weight: 600;
    color: #606266;
    width: auto;
    min-width: 30px;
    flex-shrink: 0;
    margin-right: 6px;
    font-size: 13px;
  }

  .info-item .value {
    color: #303133;
    flex-shrink: 0;
    font-size: 13px;
    font-weight: 500;
  }

  .board-actions {
    display: flex;
    gap: 5px;
    flex-wrap: wrap;
  }

  .semi-product-container {
    padding: 10px;
  }

  .no-data {
    text-align: center;
    padding: 20px;
  }

  .semi-product-list {
    display: flex;
    flex-direction: column;
    gap: 15px;
  }

  .semi-product-item {
    border: 1px solid #d0d7e5;
    border-radius: 8px;
    padding: 15px;
    background-color: #fafbfc;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;
  }

  .semi-product-enabled {
    background-color: #fafbfc;
    border-color: #d0d7e5;
  }

  .semi-product-disabled {
    background-color: #fef0f0;
    border-color: #fab8b8;
  }

  .semi-product-consumed {
    background-color: #fef9e7;
    border-color: #fadb14;
  }

  .semi-product-info {
    margin-bottom: 6px;
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    padding: 3px 0;
  }

  .semi-product-info .info-row {
    display: inline-flex;
    align-items: center;
    flex-shrink: 0;
    padding: 2px 0;
  }

  .semi-product-info .info-row .label {
    font-weight: 600;
    color: #606266;
    width: auto;
    min-width: 35px;
    flex-shrink: 0;
    margin-left: 12px;
    margin-right: 6px;
    font-size: 13px;
  }

  .semi-product-info .info-row .value {
    color: #303133;
    flex-shrink: 0;
    font-size: 13px;
    font-weight: 500;
  }

  .semi-product-actions {
    display: flex;
    gap: 5px;
    flex-wrap: wrap;
    padding-top: 5px;
    border-top: 1px dashed #dcdfe6;
  }

  .led-board-plugin-container {
    margin-top: 15px;
    padding: 15px;
    background-color: #f8f9fa;
    border-radius: 8px;
    border-left: 4px solid #409eff;
  }

  .led-board-plugin-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .led-board-plugin-item {
    padding: 10px;
    background-color: white;
    border-radius: 4px;
    border: 1px solid #ebeef5;
  }

  .led-info {
    margin-bottom: 8px;
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    padding: 5px 0;
  }

  .led-info .info-row {
    display: inline-flex;
    align-items: center;
    flex-shrink: 0;
    padding: 1px 0;
  }

  .led-info .info-row .label {
    font-weight: 600;
    color: #606266;
    width: auto;
    min-width: 75px;
    flex-shrink: 0;
    margin-right: 6px;
    font-size: 13px;
  }

  .led-info .info-row .value {
    color: #303133;
    flex-shrink: 0;
    font-size: 13px;
    font-weight: 500;
  }

  .led-actions {
    display: flex;
    gap: 5px;
    flex-wrap: wrap;
    padding-top: 8px;
    border-top: 1px dashed #dcdfe6;
  }

  .pagination-container {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>