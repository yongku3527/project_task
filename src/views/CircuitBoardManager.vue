<template>
  <div class="circuit-board-manager">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>线路板管理系统</span>
          <div class="header-actions">
            <el-button type="primary" size="small" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增线路板
            </el-button>
            <el-button type="success" size="small" @click="loadData">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <div class="search-bar">
        <el-form :inline="true" size="small">
          <el-form-item label="线路板编码:">
            <el-input v-model="searchForm.boardCode" placeholder="请输入线路板编码" clearable />
          </el-form-item>
          <el-form-item label="线路板名称:">
            <el-input v-model="searchForm.boardName" placeholder="请输入线路板名称" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

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
          <el-table-column label="线路板信息" width="300" fixed="left">
            <template #default="{ row }">
              <div class="board-info">
                <div class="info-item">
                  <span class="label">编码:</span>
                  <span class="value">{{ row.boardCode }}</span>
                </div>
                <div class="info-item">
                  <span class="label">名称:</span>
                  <span class="value">{{ row.boardName }}</span>
                </div>
                <div class="info-item">
                  <span class="label">文件:</span>
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
                <div class="info-item">
                  <span class="label">状态:</span>
                  <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                    {{ row.status === 1 ? '启用' : '禁用' }}
                  </el-tag>
                </div>
                <div class="info-item">
                  <span class="label">创建时间:</span>
                  <span class="value">{{ formatDate(row.createTime) }}</span>
                </div>
              </div>
              <div class="board-actions">
                <el-button type="primary" size="small" link @click="handleEdit(row)">
                  <el-icon><Edit /></el-icon>编辑
                </el-button>
                <el-button type="danger" size="small" link @click="handleDelete(row)">
                  <el-icon><Delete /></el-icon>删除
                </el-button>
                <el-button type="success" size="small" link @click="handleAddSemiProduct(row)">
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
                  >
                    <div class="semi-product-info">
                      <div class="info-row">
                        <span class="label">编码:</span>
                        <span class="value">{{ semi.semiProductCode }}</span>
                      </div>
                      <div class="info-row">
                        <span class="label">名称:</span>
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
                        <span class="label">SMT文件:</span>
                        <el-link 
                          v-if="semi.smtFileUrl" 
                          type="primary" 
                          @click="downloadFile(semi.smtFileUrl, semi.smtFileName)"
                        >
                          {{ semi.smtFileName }}
                        </el-link>
                        <span v-else>-</span>
                      </div>
                      <div class="info-row">
                        <span class="label">状态:</span>
                        <el-tag :type="semi.status === 1 ? 'success' : 'danger'" size="small">
                          {{ semi.status === 1 ? '启用' : '禁用' }}
                        </el-tag>
                      </div>
                    </div>
                    <div class="semi-product-actions">
                      <el-button type="warning" size="small" link @click="handleEditSemiProduct(semi)">
                        <el-icon><Edit /></el-icon>编辑
                      </el-button>
                      <el-button type="danger" size="small" link @click="handleDeleteSemiProduct(semi)">
                        <el-icon><Delete /></el-icon>删除
                      </el-button>
                      <el-button type="info" size="small" link @click="handleAddLedBoardPlugin(semi)">
                        <el-icon><Plus /></el-icon>添加灯板插件
                      </el-button>
                      <el-button 
                        v-if="semi.ledBoardPluginSemiProductDTOList && semi.ledBoardPluginSemiProductDTOList.length > 0"
                        type="primary" 
                        size="small" 
                        link 
                        @click="toggleLedBoardPlugin(semi)"
                      >
                        <el-icon><ArrowDown v-if="!semi.showLedBoardPlugin" /><ArrowUp v-else /></el-icon>
                        灯板插件({{ semi.ledBoardPluginSemiProductDTOList.length }})
                      </el-button>
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
                              <span class="label">插件编码:</span>
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
                            <div class="info-row">
                              <span class="label">状态:</span>
                              <el-tag :type="led.status === 1 ? 'success' : 'danger'" size="small">
                                {{ led.status === 1 ? '启用' : '禁用' }}
                              </el-tag>
                            </div>
                          </div>
                          <div class="led-actions">
                            <el-button type="warning" size="small" link @click="handleEditLedBoardPlugin(led)">
                              <el-icon><Edit /></el-icon>编辑
                            </el-button>
                            <el-button type="danger" size="small" link @click="handleDeleteLedBoardPlugin(led)">
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Edit, Delete, Upload, Download, ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import axios from 'axios'

const baseUrl = 'http://192.168.90.64:8083'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(5)
const total = ref(0)
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
  boardName: ''
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
    const response = await axios.get(`${baseUrl}/circuit-board/all-with-details`)
    if (response.data.code === 200) {
      tableData.value = response.data.data
      total.value = response.data.data.length
    } else {
      ElMessage.error('加载数据失败: ' + response.data.msg)
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

// 文件下载 - 使用预签名链接
const downloadFile = async (url, fileName) => {
  try {
    // 提取存储桶名称和对象名称
    const urlMatch = url.match(/\/minio\/buckets\/([^\/]+)\/files\/(.+)/)
    if (!urlMatch) {
      // 如果不是MinIO URL，直接下载
      const link = document.createElement('a')
      link.href = url
      link.download = fileName || 'download'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
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
      
      // 创建下载链接
      const link = document.createElement('a')
      link.href = downloadUrl
      link.download = fileName || 'download'
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
    await ElMessageBox.confirm('确认删除该线路板吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await axios.delete(`${baseUrl}/circuit-board/delete/${row.id}`)
    
    if (response.data.code === 200) {
      ElMessage.success('删除成功')
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
    await ElMessageBox.confirm('确认删除该半成品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await axios.delete(`${baseUrl}/semi-product/delete/${semi.id}`)
    
    if (response.data.code === 200) {
      ElMessage.success('删除成功')
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
    await ElMessageBox.confirm('确认删除该灯板插件吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await axios.delete(`${baseUrl}/led-board-plugin-semi-product/delete/${led.id}`)
    
    if (response.data.code === 200) {
      ElMessage.success('删除成功')
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

const handleBoardUploadRemove = (file, fileList) => {
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

const handleSchematicUploadRemove = (file, fileList) => {
  semiProductDialog.form.schematicFileId = null
}

const handleSmtUploadSuccess = (response, file, fileList) => {
  if (response.code === 200) {
    semiProductDialog.form.smtFileId = response.data.fileId
  } else {
    ElMessage.error('SMT文件上传失败: ' + response.message)
  }
}

const handleSmtUploadRemove = (file, fileList) => {
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

const handleLedBoardPluginUploadRemove = (file, fileList) => {
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
  const maxSize = 50 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过50MB')
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
</script>

<style scoped>
.circuit-board-manager {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .header-actions {
    display: flex;
    gap: 10px;
  }

  .search-bar {
    margin-bottom: 20px;
    padding: 15px;
    background-color: #f5f7fa;
    border-radius: 4px;
  }

  .table-container {
    margin-bottom: 20px;
  }

  .board-info {
    padding: 10px;
    background-color: #f0f9ff;
    border-radius: 4px;
    margin-bottom: 10px;

    .info-item {
      display: flex;
      margin-bottom: 8px;
      align-items: center;

      &:last-child {
        margin-bottom: 0;
      }

      .label {
        font-weight: bold;
        color: #606266;
        width: 70px;
        flex-shrink: 0;
      }

      .value {
        flex: 1;
        color: #303133;
      }
    }
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
    border: 1px solid #e4e7ed;
    border-radius: 4px;
    padding: 15px;
    background-color: #fafafa;
  }

  .semi-product-info {
    margin-bottom: 10px;

    .info-row {
      display: flex;
      margin-bottom: 6px;
      align-items: center;

      &:last-child {
        margin-bottom: 0;
      }

      .label {
        font-weight: bold;
        color: #606266;
        width: 80px;
        flex-shrink: 0;
      }

      .value {
        flex: 1;
        color: #303133;
      }
    }
  }

  .semi-product-actions {
    display: flex;
    gap: 5px;
    flex-wrap: wrap;
    padding-top: 10px;
    border-top: 1px dashed #dcdfe6;
  }

  .led-board-plugin-container {
    margin-top: 15px;
    padding: 15px;
    background-color: #f5f7fa;
    border-radius: 4px;
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

    .info-row {
      display: flex;
      margin-bottom: 4px;
      align-items: center;

      &:last-child {
        margin-bottom: 0;
      }

      .label {
        font-weight: bold;
        color: #606266;
        width: 90px;
        flex-shrink: 0;
      }

      .value {
        flex: 1;
        color: #303133;
      }
    }
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