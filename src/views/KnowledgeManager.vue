<template>
  <div class="knowledge-manager">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-actions">
            <div class="search-bar-container">
              <el-form :inline="true" size="small" class="search-form">
                <el-form-item label="产品机型:" class="search-form-item">
                  <el-select v-model="searchForm.productModel" placeholder="请选择产品机型" clearable filterable style="width: 120px;">
                    <el-option
                      v-for="item in productModelOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="产品分类:" class="search-form-item">
                  <el-select v-model="searchForm.productCategory" placeholder="请选择产品分类" clearable filterable style="width: 120px;">
                    <el-option
                      v-for="item in productCategoryOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="失效模式:" class="search-form-item">
                  <el-input v-model="searchForm.failureMode" placeholder="请输入失效模式" clearable style="width: 120px;" />
                </el-form-item>
                <el-form-item label="问题来源:" class="search-form-item">
                  <el-select v-model="searchForm.issueSource" placeholder="请选择问题来源" clearable style="width: 120px;">
                    <el-option
                      v-for="item in issueSourceOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="问题描述:" class="search-form-item">
                  <el-input v-model="searchForm.issueDescription" placeholder="请输入问题描述" clearable style="width: 150px;" />
                </el-form-item>
                <el-form-item label="根本原因:" class="search-form-item">
                  <el-input v-model="searchForm.rootCause" placeholder="请输入根本原因" clearable style="width: 150px;" />
                </el-form-item>
                <el-form-item label="处理措施:" class="search-form-item">
                  <el-input v-model="searchForm.permanentAction" placeholder="请输入处理措施" clearable style="width: 150px;" />
                </el-form-item>
                <!-- 暂时隐藏状态查询列 TODO -->
                <!-- <el-form-item label="完成状态:" class="search-form-item">
                  <el-select v-model="searchForm.completionStatus" placeholder="请选择完成状态" clearable style="width: 120px;">
                    <el-option label="待学习" :value="0" />
                    <el-option label="已学习" :value="1" />
                    <el-option label="已掌握" :value="2" />
                  </el-select>
                </el-form-item> -->
                <el-form-item class="search-form-item">
                  <el-button type="primary" @click="handleSearch">查询</el-button>
                  <el-button @click="handleReset">重置</el-button>
                </el-form-item>
              </el-form>
            </div>
            <div class="header-buttons">
              <el-button v-permission="'knowledge_info:add'" type="primary" size="small" @click="handleAdd">  
                <el-icon><Plus /></el-icon>
                新增经验库
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
          :cell-style="{ 'white-space': 'pre-wrap', 'word-break': 'break-word' }"
        >
          <!-- 序号列 -->
          <el-table-column type="index" label="序号" width="60" align="center" fixed="left"></el-table-column>

          <!-- 产品机型列 -->
          <el-table-column label="产品机型" width="120" fixed="left">
            <template #default="{ row }">
              <div class="product-info">
                <div class="main-field">
                  <el-tooltip :content="row.productModel" placement="top">
                    <span class="field-text">{{ row.productModel }}</span>
                  </el-tooltip>
                </div>
                <!-- 状态信息暂时隐藏TODO -->
                <!-- <div class="status-tags">
                  <el-tag :type="getCompletionStatusTagType(row.completionStatus)" size="small">
                    {{ getCompletionStatusText(row.completionStatus) }}
                  </el-tag>
                </div> -->
              </div>
            </template>
          </el-table-column>

          <!-- 产品分类列 -->
          <el-table-column label="产品分类" width="90">
            <template #default="{ row }">
              <el-tooltip :content="row.productCategory" placement="top">
                <span class="field-text">{{ row.productCategory }}</span>
              </el-tooltip>
            </template>
          </el-table-column>

          <!-- 失效模式列 -->
          <el-table-column label="失效模式" width="120">
            <template #default="{ row }">
              <el-tag :type="getFailureModeTagType(row.failureMode)" size="small">
                {{ row.failureMode }}
              </el-tag>
            </template>
          </el-table-column>

          <!-- 问题来源列 -->
          <el-table-column label="问题来源" width="100">
            <template #default="{ row }">
              <el-tag :type="getIssueSourceTagType(row.issueSource)" size="small">
                {{ row.issueSource }}
              </el-tag>
            </template>
          </el-table-column>

          <!-- 问题描述列 -->
          <el-table-column label="问题描述" min-width="100">
            <template #default="{ row }">
              <div class="description-container">
                <p class="description-text">{{ row.issueDescription }}</p>
                <div v-if="row.issueAttachmentsFileUrl" class="attachment-container">
                  <el-icon><Paperclip /></el-icon>
                  <el-link 
                    type="primary" 
                    @click="downloadFile(row.issueAttachmentsFileUrl, row.issueAttachmentsFileName)"
                    class="attachment-link"
                  >
                    {{ row.issueAttachmentsFileName }}
                  </el-link>
                </div>
              </div>
            </template>
          </el-table-column>

          <!-- 根本原因列 -->
          <el-table-column label="根本原因" min-width="200">
            <template #default="{ row }">
              <div class="root-cause-text" style="white-space: pre-wrap; word-break: break-word;">{{ row.rootCause }}</div>
            </template>
          </el-table-column>

          <!-- 解决方案列 -->
          <el-table-column label="处理措施" min-width="200">
            <template #default="{ row }">
              <div class="solution-container">
                <div class="solution-text" style="white-space: pre-wrap; word-break: break-word;">
                  {{ row.permanentAction }}
                </div>
                <div v-if="row.actionAttachmentsFileUrl" class="attachment-container">
                  <el-icon><Paperclip /></el-icon>
                  <el-link 
                    type="primary" 
                    @click="downloadFile(row.actionAttachmentsFileUrl, row.actionAttachmentsFileName)"
                    class="attachment-link"
                  >
                    {{ row.actionAttachmentsFileName }}
                  </el-link>
                </div>
              </div>
            </template>
          </el-table-column>

          <!-- 应用场景列 -->
          <el-table-column label="应用场景" width="150">
            <template #default="{ row }">
              <p class="scene-text">{{ row.applicationScene }}</p>
            </template>
          </el-table-column>

          <!-- 操作列 -->
          <el-table-column label="操作" width="190" fixed="right">
            <template #default="{ row }">
              <div class="knowledge-actions">
                <el-button v-permission="'knowledge_info:update'" type="primary" size="small" link @click="handleEdit(row)">
                  <el-icon><Edit /></el-icon>编辑
                </el-button>
                <el-button v-permission="'knowledge_info:delete'" type="danger" size="small" link @click="handleDelete(row)">
                  <el-icon><Delete /></el-icon>删除
                </el-button>
                <el-button type="success" size="small" link @click="handleViewDetail(row)">
                  <el-icon><View /></el-icon>详情
                </el-button>
                <!-- 完成状态修改按钮 -->
                 <!-- 状态信息暂时隐藏TODO -->
                <!-- <el-dropdown @command="(command) => handleCompletionStatusChange(row, command)" trigger="click">
                  <el-button type="warning" size="small" link>
                    <el-icon><Setting /></el-icon>完成状态
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item :command="0">待学习</el-dropdown-item>
                      <el-dropdown-item :command="1">已学习</el-dropdown-item>
                      <el-dropdown-item :command="2">已掌握</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown> -->
              </div>
            </template>
          </el-table-column>
          
          <!-- 时间信息列 -->
          <el-table-column label="时间信息" width="170">
            <template #default="{ row }">
              <div class="time-container">
                <div class="time-item">
                  <span class="time-label">创建时间:</span>
                  <span class="time-value">{{ formatDateTime(row.createTime) }}</span>
                </div>
                <div class="time-item">
                  <span class="time-label">更新时间:</span>
                  <span class="time-value">{{ formatDateTime(row.updateTime) }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页11 -->
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

    <!-- 经验库编辑对话框 -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.title"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form :model="dialog.form" :rules="dialog.rules" ref="formRef" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="产品机型" prop="productModel">
              <el-input v-model="dialog.form.productModel" placeholder="请输入产品机型" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品分类" prop="productCategory">
              <el-select v-model="dialog.form.productCategory" placeholder="请选择产品分类" style="width: 100%">
                <el-option
                  v-for="item in productCategoryOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="失效模式" prop="failureMode">
              <el-input v-model="dialog.form.failureMode" placeholder="请输入失效模式" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="问题来源" prop="issueSource">
              <el-select v-model="dialog.form.issueSource" placeholder="请选择问题来源" style="width: 100%">
                <el-option
                  v-for="item in issueSourceOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="问题描述" prop="issueDescription">
          <el-input 
            v-model="dialog.form.issueDescription" 
            type="textarea" 
            :rows="3"
            placeholder="请输入问题描述"
          />
        </el-form-item>
        <el-form-item label="问题附件">
          <el-upload
            ref="issueUploadRef"
            :limit="1"
            :on-success="handleIssueUploadSuccess"
            :on-remove="handleIssueUploadRemove"
            :file-list="dialog.issueFileList"
            :before-upload="beforeUpload"
            :http-request="handleIssueUpload"
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon>选择问题附件
            </el-button>
            <template #tip>
              <div class="upload-tip">支持PDF、图片等格式，单个文件不超过200MB</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="根本原因" prop="rootCause">
          <el-input 
            v-model="dialog.form.rootCause" 
            type="textarea" 
            :rows="3"
            placeholder="请输入根本原因"
          />
        </el-form-item>
        <el-form-item label="永久处理措施" prop="permanentAction">
          <el-input 
            v-model="dialog.form.permanentAction" 
            type="textarea" 
            :rows="3"
            placeholder="请输入永久处理措施"
          />
        </el-form-item>
        <el-form-item label="措施附件">
          <el-upload
            ref="actionUploadRef"
            :limit="1"
            :on-success="handleActionUploadSuccess"
            :on-remove="handleActionUploadRemove"
            :file-list="dialog.actionFileList"
            :before-upload="beforeUpload"
            :http-request="handleActionUpload"
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon>选择措施附件
            </el-button>
            <template #tip>
              <div class="upload-tip">支持PDF、图片等格式，单个文件不超过200MB</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="应用场景" prop="applicationScene">
          <el-input 
            v-model="dialog.form.applicationScene" 
            placeholder="请输入应用场景"
            
          />
        </el-form-item>

      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCancel">取消</el-button>
          <el-button type="info" @click="saveDraft">存草稿</el-button>
          <el-button type="primary" @click="saveKnowledge">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 经验库详情对话框 -->
    <el-dialog
      v-model="detailDialog.visible"
      title="经验库详情"
      width="900px"
    >
      <div v-if="detailDialog.data" class="detail-container">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="产品机型">{{ detailDialog.data.productModel }}</el-descriptions-item>
          <el-descriptions-item label="产品分类">{{ detailDialog.data.productCategory }}</el-descriptions-item>
          <el-descriptions-item label="失效模式">
            <el-tag :type="getFailureModeTagType(detailDialog.data.failureMode)" size="small">
              {{ detailDialog.data.failureMode }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="问题来源">
            <el-tag :type="getIssueSourceTagType(detailDialog.data.issueSource)" size="small">
              {{ detailDialog.data.issueSource }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="完成状态">
            <el-tag :type="getCompletionStatusTagType(detailDialog.data.completionStatus)" size="small">
              {{ getCompletionStatusText(detailDialog.data.completionStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="所属角色">{{ detailDialog.data.role }}</el-descriptions-item>
          <el-descriptions-item label="问题描述" :span="2">
            <div class="description-detail">{{ detailDialog.data.issueDescription }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="问题附件" :span="2" v-if="detailDialog.data.issueAttachmentsFileUrl">
            <el-link 
              type="primary" 
              @click="downloadFile(detailDialog.data.issueAttachmentsFileUrl, detailDialog.data.issueAttachmentsFileName)"
            >
              <el-icon><Download /></el-icon>
              {{ detailDialog.data.issueAttachmentsFileName }}
            </el-link>
          </el-descriptions-item>
          <el-descriptions-item label="根本原因" :span="2">
            <div class="description-detail">{{ detailDialog.data.rootCause }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="永久处理措施" :span="2">
            <div class="description-detail">{{ detailDialog.data.permanentAction }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="措施附件" :span="2" v-if="detailDialog.data.actionAttachmentsFileUrl">
            <el-link 
              type="primary" 
              @click="downloadFile(detailDialog.data.actionAttachmentsFileUrl, detailDialog.data.actionAttachmentsFileName)"
            >
              <el-icon><Download /></el-icon>
              {{ detailDialog.data.actionAttachmentsFileName }}
            </el-link>
          </el-descriptions-item>
          <el-descriptions-item label="应用场景" :span="2">
            <div class="description-detail">{{ detailDialog.data.applicationScene }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(detailDialog.data.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatDateTime(detailDialog.data.updateTime) }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialog.visible = false">关闭</el-button>
          <el-button v-permission="'knowledge_info:update'" type="primary" @click="handleEditFromDetail">编辑</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Edit, Delete, Upload, Download, View, Paperclip, Setting } from '@element-plus/icons-vue'
import { 
  getKnowledgeList, 
  getCompleteKnowledgeList, 
  getCompleteKnowledgeById,
  createKnowledge, 
  updateKnowledge, 
  deleteKnowledge,
  getFailureModeOptions,
  getIssueSourceOptions,
  getProductModelOptions,
  getProductCategoryOptions,
  updateCompletionStatus
} from '@/api/knowledge'
import request from '@/utils/request'

// 响应式数据
const loading = ref(false)
const allTableData = ref([]) // 存储所有数据
const tableData = ref([]) // 当前页显示的数据
const currentPage = ref(1)
const pageSize = ref(50)
const total = ref(0)

// MinIO存储桶配置
const knowledgeBucket = ref('knowledge')

// 选项数据
const failureModeOptions = ref([])
const issueSourceOptions = ref([])
const productModelOptions = ref([])
const productCategoryOptions = ref([])

// 搜索表单
const searchForm = reactive({
  productModel: '',
  productCategory: '',
  failureMode: '',
  issueSource: '',
  completionStatus: null,
  issueDescription: '',
  rootCause: '',
  permanentAction: ''
})

// 经验库对话框
const dialog = reactive({
  visible: false,
  title: '',
  form: {
    id: null,
    productModel: '',
    productCategory: '',
    failureMode: '',
    issueSource: '',
    issueDescription: '',
    issueAttachmentsId: null,
    rootCause: '',
    permanentAction: '',
    actionAttachmentsId: null,
    applicationScene: '',
    role: ''
  },
  rules: {
    productModel: [{ required: true, message: '请输入产品机型', trigger: 'blur' }],
    productCategory: [{ required: true, message: '请输入产品分类', trigger: 'blur' }],
    failureMode: [{ required: true, message: '请选择失效模式', trigger: 'change' }],
    issueSource: [{ required: true, message: '请选择问题来源', trigger: 'change' }],
    issueDescription: [{ required: true, message: '请输入问题描述', trigger: 'blur' }],
    rootCause: [{ required: true, message: '请输入根本原因', trigger: 'blur' }],
    permanentAction: [{ required: true, message: '请输入永久处理措施', trigger: 'blur' }],
    applicationScene: [{ required: true, message: '请输入应用场景', trigger: 'blur' }]
  },
  issueFileList: [],
  actionFileList: []
})

// 详情对话框
const detailDialog = reactive({
  visible: false,
  data: null
})

// 表单引用
const formRef = ref()
const issueUploadRef = ref()
const actionUploadRef = ref()



// 生命周期
onMounted(() => {
  loadData()
  loadOptionsData()
})

// 加载选项数据
const loadOptionsData = async () => {
  try {
    // 并行加载所有选项数据
    const [
      failureModeRes,
      issueSourceRes,
      productModelRes,
      productCategoryRes
    ] = await Promise.all([
      getFailureModeOptions(),
      getIssueSourceOptions(),
      getProductModelOptions(),
      getProductCategoryOptions()
    ])
    
    // 处理失效模式选项
    if (failureModeRes.code === 200) {
      // 将字符串数组转换为选项对象数组
      failureModeOptions.value = (failureModeRes.data || []).map(item => ({
        label: item,
        value: item
      }))
    } else {
      console.error('获取失效模式选项失败:', failureModeRes.msg)
      // 如果接口失败，使用默认选项
      failureModeOptions.value = [
        { label: '不开机', value: '不开机' },
        { label: '花屏', value: '花屏' },
        { label: '烧屏', value: '烧屏' },
        { label: '触摸失灵', value: '触摸失灵' },
        { label: '系统崩溃', value: '系统崩溃' },
        { label: '充电异常', value: '充电异常' }
      ]
    }
    
    // 处理问题来源选项 - 使用固定数据源
    issueSourceOptions.value = [
      { label: '售后三包', value: '售后三包' },
      { label: '零公里', value: '零公里' },
      { label: 'DV试验', value: 'DV试验' },
      { label: '产品量产', value: '产品量产' }
    ]
    
    // 处理产品机型选项
    if (productModelRes.code === 200) {
      // 将字符串数组转换为选项对象数组
      productModelOptions.value = (productModelRes.data || []).map(item => ({
        label: item,
        value: item
      }))
    } else {
      console.error('获取产品机型选项失败:', productModelRes.msg)
      // 如果接口失败，使用空数组
      productModelOptions.value = []
    }
    
    // 处理产品分类选项 - 使用固定数据源
    productCategoryOptions.value = [
      { label: '多媒体', value: '多媒体' },
      { label: '收放机', value: '收放机' },
      { label: '仪表', value: '仪表' },
      { label: '电器件', value: '电器件' }
    ]
  } catch (error) {
    console.error('加载选项数据失败:', error)
    ElMessage.error('加载选项数据失败')
    
    // 如果请求失败，使用默认选项
    failureModeOptions.value = [
      { label: '不开机', value: '不开机' },
      { label: '花屏', value: '花屏' },
      { label: '烧屏', value: '烧屏' },
      { label: '触摸失灵', value: '触摸失灵' },
      { label: '系统崩溃', value: '系统崩溃' },
      { label: '充电异常', value: '充电异常' }
    ]
    
    issueSourceOptions.value = [
      { label: 'IQC', value: 'IQC' },
      { label: '市场', value: '市场' },
      { label: '生产线', value: '生产线' },
      { label: '客退', value: '客退' },
      { label: '可靠性实验', value: '可靠性实验' },
      { label: '现场故障', value: '现场故障' },
      { label: '内部测试', value: '内部测试' }
    ]
    
    productModelOptions.value = []
    // 处理产品分类选项 - 使用固定数据源
    productCategoryOptions.value = [
      { label: '多媒体', value: '多媒体' },
      { label: '收放机', value: '收放机' },
      { label: '仪表', value: '仪表' },
      { label: '电器件', value: '电器件' }
    ]
  }
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    // 使用完整经验库列表接口，不传递任何参数
    const res = await getCompleteKnowledgeList()
    if (res.code === 200) {
      // 存储所有数据
      allTableData.value = res.data || []
      
      // 应用筛选条件
      applyFilters()
    } else {
      ElMessage.error(res.msg || '获取数据失败')
    }
  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 应用筛选条件
const applyFilters = () => {
  // 应用搜索条件
  let filteredData = allTableData.value.filter(item => {
    // 产品机型筛选
    if (searchForm.productModel && !item.productModel.includes(searchForm.productModel)) {
      return false
    }
    
    // 产品分类筛选
    if (searchForm.productCategory && !item.productCategory.includes(searchForm.productCategory)) {
      return false
    }
    
    // 失效模式筛选
    if (searchForm.failureMode && !item.failureMode.includes(searchForm.failureMode)) {
      return false
    }
    
    // 问题来源筛选
    if (searchForm.issueSource && !item.issueSource.includes(searchForm.issueSource)) {
      return false
    }
    
    // 问题描述筛选
    if (searchForm.issueDescription && !item.issueDescription.toLowerCase().includes(searchForm.issueDescription.toLowerCase())) {
      return false
    }
    
    // 根本原因筛选
    if (searchForm.rootCause && !item.rootCause.toLowerCase().includes(searchForm.rootCause.toLowerCase())) {
      return false
    }
    
    // 处理措施筛选
    if (searchForm.permanentAction && !item.permanentAction.toLowerCase().includes(searchForm.permanentAction.toLowerCase())) {
      return false
    }
    
    // 完成状态筛选
    if (searchForm.completionStatus !== null && searchForm.completionStatus !== undefined && 
        item.completionStatus !== searchForm.completionStatus) {
      return false
    }
    
    return true
  })
  
  // 更新分页数据
  updatePagedData(filteredData)
}

// 更新分页数据
const updatePagedData = (data = null) => {
  const sourceData = data || allTableData.value
  total.value = sourceData.length
  
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  tableData.value = sourceData.slice(start, end)
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  applyFilters()
}

// 重置
const handleReset = () => {
  Object.keys(searchForm).forEach(key => {
    if (key === 'completionStatus') {
      searchForm[key] = null
    } else {
      searchForm[key] = ''
    }
  })
  currentPage.value = 1
  applyFilters()
}

// 分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1 // 重置到第一页
  applyFilters()
}

// 当前页变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  applyFilters()
}

// 新增
const handleAdd = () => {
  dialog.visible = true
  dialog.title = '新增经验库'
  
  // 检查localStorage中是否有草稿数据
  const draft = localStorage.getItem('knowledgeDraft')
  if (draft) {
    // 加载草稿数据
    dialog.form = JSON.parse(draft)
    // 恢复文件列表
    dialog.issueFileList = dialog.form.issueAttachmentsFileUrl ? [{ 
      name: dialog.form.issueAttachmentsFileName, 
      url: dialog.form.issueAttachmentsFileUrl 
    }] : []
    dialog.actionFileList = dialog.form.actionAttachmentsFileUrl ? [{ 
      name: dialog.form.actionAttachmentsFileName, 
      url: dialog.form.actionAttachmentsFileUrl 
    }] : []
    ElMessage.info('已加载草稿数据')
  } else {
    // 初始化空表单
    dialog.form = {
      id: null,
      productModel: '',
      productCategory: '',
      failureMode: '',
      issueSource: '',
      issueDescription: '',
      issueAttachmentsId: null,
      issueAttachmentsFileName: '',
      issueAttachmentsFileUrl: '',
      rootCause: '',
      permanentAction: '',
      actionAttachmentsId: null,
      actionAttachmentsFileName: '',
      actionAttachmentsFileUrl: '',
      applicationScene: '',
      role: ''
    }
    dialog.issueFileList = []
    dialog.actionFileList = []
  }
}

// 编辑
const handleEdit = (row) => {
  dialog.visible = true
  dialog.title = '编辑经验库'
  dialog.form = { ...row }
  
  // 设置文件列表
  dialog.issueFileList = row.issueAttachmentsFileUrl ? [{
    name: row.issueAttachmentsFileName,
    url: row.issueAttachmentsFileUrl
  }] : []
  
  dialog.actionFileList = row.actionAttachmentsFileUrl ? [{
    name: row.actionAttachmentsFileName,
    url: row.actionAttachmentsFileUrl
  }] : []
  

}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除经验库"${row.productModel} - ${row.failureMode}"吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await deleteKnowledge(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadData()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 用户取消删除
  })
}

// 查看详情
const handleViewDetail = async (row) => {
  try {
    const res = await getCompleteKnowledgeById(row.id)
    if (res.code === 200) {
      detailDialog.data = res.data
      detailDialog.visible = true
    } else {
      ElMessage.error(res.msg || '获取详情失败')
    }
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

// 从详情页编辑
const handleEditFromDetail = () => {
  detailDialog.visible = false
  handleEdit(detailDialog.data)
}

// 保存
const saveKnowledge = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = dialog.form.id 
          ? await updateKnowledge(dialog.form)
          : await createKnowledge(dialog.form)
          
        if (res.code === 200) {
          ElMessage.success(dialog.form.id ? '更新成功' : '创建成功')
          dialog.visible = false
          // 保存成功后清除草稿数据
          localStorage.removeItem('knowledgeDraft')
          // 重置表单校验信息
          formRef.value?.resetFields()
          loadData()
        } else {
          ElMessage.error(res.msg || '保存失败')
        }
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error('保存失败')
      }
    }
  })
}

// 取消操作
const handleCancel = () => {
  dialog.visible = false
  // 重置表单校验信息
  formRef.value?.resetFields()
}

// 存草稿
const saveDraft = () => {
  try {
    // 将当前表单数据保存到localStorage
    localStorage.setItem('knowledgeDraft', JSON.stringify(dialog.form))
    ElMessage.success('草稿保存成功')
  } catch (error) {
    console.error('保存草稿失败:', error)
    ElMessage.error('保存草稿失败')
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

// 问题附件预签名URL上传
const handleIssueUpload = async (options) => {
  const { file, onSuccess, onError, onProgress } = options
  
  try {
    // 格式化文件名：原名~当前时间戳.扩展名
    const timestamp = Date.now()
    const fileNameWithoutExt = file.name.substring(0, file.name.lastIndexOf('.'))
    const fileExt = file.name.substring(file.name.lastIndexOf('.'))
    const formattedFileName = `${fileNameWithoutExt}~${timestamp}${fileExt}`
    const objectName = formattedFileName
    
    // 第一步：创建普通预上传任务
    const presignResponse = await request.post(
      `/minio/buckets/${knowledgeBucket.value}/files/${encodeURIComponent(objectName)}/presigned-upload?fileSize=${file.size}`,
      null
    )
    
    if (presignResponse.code !== 200) {
      throw new Error(presignResponse.msg || '创建预上传任务失败')
    }
    
    const { presignedUrl } = presignResponse.data
    
    // 第二步：使用预签名URL直接上传文件到MinIO
    await uploadFileWithPresignedUrl(file, presignedUrl, onProgress)
    
    // 保存文件信息到数据库
    const saveFileResponse = await request.post(`/minio/buckets/${knowledgeBucket.value}/files/save-info`, {
      bucketName: knowledgeBucket.value,
      objectName: objectName,
      originalName: file.name,
      fileSize: file.size,
      contentType: file.type || 'application/octet-stream'
    })
    
    if (saveFileResponse.code !== 200) {
      throw new Error('保存文件信息失败: ' + saveFileResponse.msg)
    }
    
    // 模拟原上传成功回调格式
    const fileUrl = `/minio/buckets/${knowledgeBucket.value}/files/${encodeURIComponent(objectName)}`
    const mockResponse = {
      code: 200,
      message: '上传成功',
      data: {
        fileUrl: fileUrl,
        fileName: file.name,
        fileId: saveFileResponse.data?.fileId || null
      }
    }
    
    // 调用原成功处理函数
    handleIssueUploadSuccess(mockResponse)
    onSuccess(mockResponse)
    
    ElMessage.success(`问题附件上传成功，文件名: ${file.name}`)
    
  } catch (error) {
    ElMessage.error('问题附件上传失败: ' + error.message)
    onError(error)
  }
}

// 措施附件预签名URL上传
const handleActionUpload = async (options) => {
  const { file, onSuccess, onError, onProgress } = options
  
  try {
    // 格式化文件名：原名~当前时间戳.扩展名
    const timestamp = Date.now()
    const fileNameWithoutExt = file.name.substring(0, file.name.lastIndexOf('.'))
    const fileExt = file.name.substring(file.name.lastIndexOf('.'))
    const formattedFileName = `${fileNameWithoutExt}~${timestamp}${fileExt}`
    const objectName = formattedFileName
    
    // 第一步：创建普通预上传任务
    const presignResponse = await request.post(
      `/minio/buckets/${knowledgeBucket.value}/files/${encodeURIComponent(objectName)}/presigned-upload?fileSize=${file.size}`,
      null
    )
    
    if (presignResponse.code !== 200) {
      throw new Error(presignResponse.msg || '创建预上传任务失败')
    }
    
    const { presignedUrl } = presignResponse.data
    
    // 第二步：使用预签名URL直接上传文件到MinIO
    await uploadFileWithPresignedUrl(file, presignedUrl, onProgress)
    
    // 保存文件信息到数据库
    const saveFileResponse = await request.post(`/minio/buckets/${knowledgeBucket.value}/files/save-info`, {
      bucketName: knowledgeBucket.value,
      objectName: objectName,
      originalName: file.name,
      fileSize: file.size,
      contentType: file.type || 'application/octet-stream'
    })
    
    if (saveFileResponse.code !== 200) {
      throw new Error('保存文件信息失败: ' + saveFileResponse.msg)
    }
    
    // 模拟原上传成功回调格式
    const fileUrl = `/minio/buckets/${knowledgeBucket.value}/files/${encodeURIComponent(objectName)}`
    const mockResponse = {
      code: 200,
      message: '上传成功',
      data: {
        fileUrl: fileUrl,
        fileName: file.name,
        fileId: saveFileResponse.data?.fileId || null
      }
    }
    
    // 调用原成功处理函数
    handleActionUploadSuccess(mockResponse)
    onSuccess(mockResponse)
    
    ElMessage.success(`措施附件上传成功，文件名: ${file.name}`)
    
  } catch (error) {
    ElMessage.error('措施附件上传失败: ' + error.message)
    onError(error)
  }
}

// 问题附件上传成功
const handleIssueUploadSuccess = (response) => {
  if (response.code === 200 && response.data) {
    // 使用fileId字段而不是id字段
    dialog.form.issueAttachmentsId = response.data.fileId
    // 设置文件信息
    dialog.issueFileList = [{
      name: response.data.fileName,
      url: response.data.fileUrl
    }]
    console.log('问题附件上传成功，fileId:', response.data.fileId)
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}

// 问题附件移除
const handleIssueUploadRemove = async (file, fileList) => {
  const originalFileId = dialog.form.issueAttachmentsId
  
  // 清除相关字段
  dialog.form.issueAttachmentsId = null
  dialog.form.issueAttachmentsFileName = ''
  dialog.form.issueAttachmentsFileUrl = ''
  dialog.issueFileList = []
  
  // 如果有文件URL，需要删除MinIO文件和文件信息数据
  if (file && file.url) {
    try {
      const urlMatch = file.url.match(/\/minio\/buckets\/([^\/]+)\/files\/(.+)/)
      if (urlMatch) {
        const bucketName = urlMatch[1]
        const objectName = urlMatch[2]
        
        // 删除MinIO文件
        await request.delete(`/minio/buckets/${bucketName}/files/${objectName}`)
        console.log('问题附件从MinIO删除成功')
        
        // 如果有文件ID，同时使用FileInfoController删除文件信息数据
        if (originalFileId) {
          try {
            await request.delete(`/file-info/${originalFileId}`)
            console.log('问题附件文件信息数据删除成功')
          } catch (error) {
            console.warn('删除问题附件文件信息数据失败:', error)
          }
        }
      }
    } catch (error) {
      console.warn('删除问题附件MinIO文件失败:', error)
      // 不阻止删除流程，只记录警告
    }
  } else if (originalFileId) {
    // 如果没有URL但有文件ID，也尝试删除文件信息数据
    try {
      await request.delete(`/file-info/${originalFileId}`)
      console.log('问题附件文件信息数据删除成功')
    } catch (error) {
      console.warn('删除问题附件文件信息数据失败:', error)
    }
  }
}

// 措施附件上传成功
const handleActionUploadSuccess = (response) => {
  if (response.code === 200 && response.data) {
    // 使用fileId字段而不是id字段
    dialog.form.actionAttachmentsId = response.data.fileId
    // 设置文件信息
    dialog.actionFileList = [{
      name: response.data.fileName,
      url: response.data.fileUrl
    }]
    console.log('措施附件上传成功，fileId:', response.data.fileId)
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}

// 措施附件移除
const handleActionUploadRemove = async (file, fileList) => {
  const originalFileId = dialog.form.actionAttachmentsId
  
  // 清除相关字段
  dialog.form.actionAttachmentsId = null
  dialog.form.actionAttachmentsFileName = ''
  dialog.form.actionAttachmentsFileUrl = ''
  dialog.actionFileList = []
  
  // 如果有文件URL，需要删除MinIO文件和文件信息数据
  if (file && file.url) {
    try {
      const urlMatch = file.url.match(/\/minio\/buckets\/([^\/]+)\/files\/(.+)/)
      if (urlMatch) {
        const bucketName = urlMatch[1]
        const objectName = urlMatch[2]
        
        // 删除MinIO文件
        await request.delete(`/minio/buckets/${bucketName}/files/${objectName}`)
        console.log('措施附件从MinIO删除成功')
        
        // 如果有文件ID，同时使用FileInfoController删除文件信息数据
        if (originalFileId) {
          try {
            await request.delete(`/file-info/${originalFileId}`)
            console.log('措施附件文件信息数据删除成功')
          } catch (error) {
            console.warn('删除措施附件文件信息数据失败:', error)
          }
        }
      }
    } catch (error) {
      console.warn('删除措施附件MinIO文件失败:', error)
      // 不阻止删除流程，只记录警告
    }
  } else if (originalFileId) {
    // 如果没有URL但有文件ID，也尝试删除文件信息数据
    try {
      await request.delete(`/file-info/${originalFileId}`)
      console.log('措施附件文件信息数据删除成功')
    } catch (error) {
      console.warn('删除措施附件文件信息数据失败:', error)
    }
  }
}

// 上传前校验
const beforeUpload = (file) => {
  // 文件大小限制 (200MB)
  const maxSize = 200 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过200MB')
    return false
  }
  return true
}

// 下载文件 - 使用预签名URL方式
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
    const response = await request.get(
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

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN')
}

// 获取失效模式标签类型
const getFailureModeTagType = (failureMode) => {
  const typeMap = {
    '不开机': 'danger',
    '花屏': 'warning',
    '烧屏': 'warning',
    '触摸失灵': 'warning',
    '系统崩溃': 'danger',
    '充电异常': 'warning'
  }
  // return typeMap[failureMode] || 'info'
   return 'danger'
}

// 获取问题来源标签类型
const getIssueSourceTagType = (issueSource) => {
  const typeMap = {
    'IQC': 'success',
    '市场': 'danger',
    '生产线': 'warning',
    '客退': 'danger',
    '可靠性实验': 'primary'
  }

  // return typeMap[issueSource] || 'info'
  return 'danger'
}

// 获取完成状态标签类型
const getCompletionStatusTagType = (status) => {
  const typeMap = {
    0: 'warning',    // 待学习
    1: 'primary', // 已学习
    2: 'success'  // 已掌握
  }
  return typeMap[status] || 'info'
}

// 修改完成状态
const handleCompletionStatusChange = async (row, newStatus) => {
  try {
    // 检查状态是否发生变化
    if (row.completionStatus === newStatus) {
      ElMessage.warning('状态未发生变化')
      return
    }
    
    // 调用后端接口更新完成状态
    const response = await updateCompletionStatus(row.id, newStatus)
    
    if (response.code === 200) {
      // 更新前端数据
      row.completionStatus = newStatus
      ElMessage.success('完成状态更新成功')
    } else {
      ElMessage.error('更新完成状态失败：' + (response.msg || '未知错误'))
    }
  } catch (error) {
    console.error('更新完成状态失败:', error)
    ElMessage.error('更新完成状态失败')
  }
}

// 获取完成状态文本
const getCompletionStatusText = (status) => {
  const textMap = {
    0: '待学习',
    1: '已学习',
    2: '已掌握'
  }
  return textMap[status] || '未知'
}
</script>

<style scoped>
.knowledge-manager {
  .card-header {
    .header-actions {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      flex-wrap: nowrap;
      gap: 10px;

      .search-bar-container {
        flex: 1;
        min-width: 0;
        overflow-x: auto;
        
        .search-form {
          display: flex;
          align-items: center;
          flex-wrap: nowrap;
          gap: 8px;
          
          .search-form-item {
            margin-bottom: 0;
            margin-right: 0;
            
            .el-select {
              .el-input__wrapper {
                min-width: 120px;
              }
            }
          }
        }
      }

      .header-buttons {
        display: flex;
        gap: 10px;
        white-space: nowrap;
        flex-shrink: 0;
      }
    }
  }

  .table-container {
    margin-top: 10px;
  }

  .product-info {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .main-field {
    display: flex;
    align-items: center;
  }

  .field-text {
    font-weight: 500;
    color: #303133;
    font-size: 14px;
    display: block;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .status-tags {
    display: flex;
    gap: 4px;
  }

  .attachment-indicator {
    display: inline-flex;
    align-items: center;
    color: #409eff;
    margin-top: 4px;
  }

  .knowledge-actions {
    display: flex;
    gap: 4px;
    flex-wrap: wrap;
    align-items: center;
  }

  .upload-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }

  .description-container {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .description-text {
    margin: 0;
    line-height: 1.4;
    color: #303133;
    font-size: 13px;
    white-space: pre-wrap;
    word-break: break-word;
  }

  .solution-container {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .solution-text {
    margin: 0;
    line-height: 1.4;
    color: #303133;
    font-size: 13px;
    white-space: pre-wrap;
    word-break: break-word;
  }

  .root-cause-text {
    margin: 0;
    line-height: 1.4;
    color: #303133;
    font-size: 13px;
    white-space: pre-wrap;
    word-break: break-word;
    display: block;
  }

  .scene-text {
    margin: 0;
    line-height: 1.4;
    color: #303133;
    font-size: 13px;
    white-space: pre-wrap;
    word-break: break-word;
  }

  .attachment-container {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-top: 2px;
  }

  .attachment-label {
    font-weight: 600;
    color: #606266;
    margin-right: 8px;
  }

  .attachment-link {
    display: flex;
    align-items: center;
    gap: 2px;
    font-size: 12px;
    max-width: 200px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: normal;
    word-break: break-word;
  }

  .time-container {
    padding: 10px;
  }

  .time-item {
    margin-bottom: 8px;
  }

  .time-label {
    font-weight: 600;
    color: #606266;
    display: block;
    margin-bottom: 2px;
    font-size: 12px;
  }

  .time-value {
    color: #303133;
    font-size: 12px;
  }

  .detail-container {
    .description-detail {
      line-height: 1.6;
      white-space: pre-wrap;
    }
  }

  .pagination-container {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>