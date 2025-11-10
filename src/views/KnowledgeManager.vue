<template>
  <div class="knowledge-manager">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-actions">
            <div class="search-bar-inline">
              <el-form :inline="true" size="small">
                <el-form-item label="产品机型:">
                  <el-select v-model="searchForm.productModel" placeholder="请选择产品机型" clearable filterable>
                    <el-option
                      v-for="item in productModelOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="产品分类:">
                  <el-select v-model="searchForm.productCategory" placeholder="请选择产品分类" clearable filterable>
                    <el-option
                      v-for="item in productCategoryOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="失效模式:">
                  <el-select v-model="searchForm.failureMode" placeholder="请选择失效模式" clearable>
                    <el-option
                      v-for="item in failureModeOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="问题来源:">
                  <el-select v-model="searchForm.issueSource" placeholder="请选择问题来源" clearable>
                    <el-option
                      v-for="item in issueSourceOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="完成状态:">
                  <el-select v-model="searchForm.completionStatus" placeholder="请选择完成状态" clearable>
                    <el-option label="待完成" :value="0" />
                    <el-option label="已学习" :value="1" />
                    <el-option label="已掌握" :value="2" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleSearch">查询</el-button>
                  <el-button @click="handleReset">重置</el-button>
                </el-form-item>
              </el-form>
            </div>
            <div class="header-buttons">
              <el-button v-permission="'knowledge:add'" type="primary" size="small" @click="handleAdd">  
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
        >
          <!-- 经验库基本信息列 -->
          <el-table-column label="经验库信息" width="400" fixed="left">
            <template #default="{ row }">
              <div class="knowledge-info" :class="{
                'knowledge-info-pending': row.completionStatus === 0,
                'knowledge-info-learning': row.completionStatus === 1,
                'knowledge-info-mastered': row.completionStatus === 2
              }">
                <div class="info-item">
                  <span class="label">产品机型:</span>
                  <span class="value">{{ row.productModel }}</span>
                </div>
                <div class="info-item">
                  <span class="label">产品分类:</span>
                  <span class="value">{{ row.productCategory }}</span>
                </div>
                <div class="info-item">
                  <span class="label">失效模式:</span>
                  <el-tag :type="getFailureModeTagType(row.failureMode)" size="small">
                    {{ row.failureMode }}
                  </el-tag>
                </div>
                <div class="info-item">
                  <span class="label">问题来源:</span>
                  <el-tag :type="getIssueSourceTagType(row.issueSource)" size="small">
                    {{ row.issueSource }}
                  </el-tag>
                </div>
                <div class="info-item">
                  <span class="label">完成状态:</span>
                  <el-tag :type="getCompletionStatusTagType(row.completionStatus)" size="small">
                    {{ getCompletionStatusText(row.completionStatus) }}
                  </el-tag>
                </div>
              </div>
              <div class="knowledge-actions">
                <el-button v-permission="'knowledge:update'" type="primary" size="small" link @click="handleEdit(row)">
                  <el-icon><Edit /></el-icon>编辑
                </el-button>
                <el-button v-permission="'knowledge:delete'" type="danger" size="small" link @click="handleDelete(row)">
                  <el-icon><Delete /></el-icon>删除
                </el-button>
                <el-button type="success" size="small" link @click="handleViewDetail(row)">
                  <el-icon><View /></el-icon>详情
                </el-button>
              </div>
            </template>
          </el-table-column>

          <!-- 问题描述列 -->
          <el-table-column label="问题描述" min-width="300">
            <template #default="{ row }">
              <div class="description-container">
                <p class="description-text">{{ row.issueDescription }}</p>
                <div v-if="row.issueAttachmentsFileUrl" class="attachment-container">
                  <span class="attachment-label">问题附件:</span>
                  <el-link 
                    type="primary" 
                    @click="downloadFile(row.issueAttachmentsFileUrl, row.issueAttachmentsFileName)"
                    class="attachment-link"
                  >
                    <el-icon><Download /></el-icon>
                    {{ row.issueAttachmentsFileName }}
                  </el-link>
                </div>
              </div>
            </template>
          </el-table-column>

          <!-- 解决方案列 -->
          <el-table-column label="解决方案" min-width="300">
            <template #default="{ row }">
              <div class="solution-container">
                <div class="solution-item">
                  <span class="solution-label">根本原因:</span>
                  <p class="solution-text">{{ row.rootCause }}</p>
                </div>
                <div class="solution-item">
                  <span class="solution-label">永久措施:</span>
                  <p class="solution-text">{{ row.permanentAction }}</p>
                </div>
                <div v-if="row.actionAttachmentsFileUrl" class="attachment-container">
                  <span class="attachment-label">措施附件:</span>
                  <el-link 
                    type="primary" 
                    @click="downloadFile(row.actionAttachmentsFileUrl, row.actionAttachmentsFileName)"
                    class="attachment-link"
                  >
                    <el-icon><Download /></el-icon>
                    {{ row.actionAttachmentsFileName }}
                  </el-link>
                </div>
              </div>
            </template>
          </el-table-column>

          <!-- 应用场景列 -->
          <el-table-column label="应用场景" width="200">
            <template #default="{ row }">
              <div class="scene-container">
                <p class="scene-text">{{ row.applicationScene }}</p>
              </div>
            </template>
          </el-table-column>

          <!-- 时间信息列 -->
          <el-table-column label="时间信息" width="180">
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

    <!-- 经验库编辑对话框 -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.title"
      width="800px"
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
              <el-input v-model="dialog.form.productCategory" placeholder="请输入产品分类" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="失效模式" prop="failureMode">
              <el-select v-model="dialog.form.failureMode" placeholder="请选择失效模式" style="width: 100%">
                <el-option
                  v-for="item in failureModeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
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
            action="/api/minio/upload/knowledge"
            :limit="1"
            :on-success="handleIssueUploadSuccess"
            :on-remove="handleIssueUploadRemove"
            :file-list="dialog.issueFileList"
            :before-upload="beforeUpload"
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon>选择问题附件
            </el-button>
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
            action="/api/minio/upload/knowledge"
            :limit="1"
            :on-success="handleActionUploadSuccess"
            :on-remove="handleActionUploadRemove"
            :file-list="dialog.actionFileList"
            :before-upload="beforeUpload"
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon>选择措施附件
            </el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="应用场景" prop="applicationScene">
          <el-input 
            v-model="dialog.form.applicationScene" 
            type="textarea" 
            :rows="2"
            placeholder="请输入应用场景"
          />
        </el-form-item>
        <el-form-item label="所属角色" prop="role">
          <el-checkbox-group v-model="roleList">
            <el-checkbox label="engineer">工程师</el-checkbox>
            <el-checkbox label="quality">质量</el-checkbox>
            <el-checkbox label="support">支持</el-checkbox>
            <el-checkbox label="design">设计</el-checkbox>
            <el-checkbox label="iqc">IQC</el-checkbox>
            <el-checkbox label="production">生产</el-checkbox>
            <el-checkbox label="developer">开发</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialog.visible = false">取消</el-button>
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
          <el-button v-permission="'knowledge:update'" type="primary" @click="handleEditFromDetail">编辑</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Edit, Delete, Upload, Download, View } from '@element-plus/icons-vue'
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
  getProductCategoryOptions
} from '@/api/knowledge'

// 响应式数据
const loading = ref(false)
const allTableData = ref([]) // 存储所有数据
const tableData = ref([]) // 当前页显示的数据
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

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
  completionStatus: null
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
    applicationScene: [{ required: true, message: '请输入应用场景', trigger: 'blur' }],
    role: [{ required: true, message: '请选择所属角色', trigger: 'change' }]
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

// 角色列表
const roleList = ref([])

// 计算属性，将角色列表转换为字符串
const roleString = computed(() => {
  return roleList.value.join(',')
})

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
    
    // 处理问题来源选项
    if (issueSourceRes.code === 200) {
      // 将字符串数组转换为选项对象数组
      issueSourceOptions.value = (issueSourceRes.data || []).map(item => ({
        label: item,
        value: item
      }))
    } else {
      console.error('获取问题来源选项失败:', issueSourceRes.msg)
      // 如果接口失败，使用默认选项
      issueSourceOptions.value = [
        { label: 'IQC', value: 'IQC' },
        { label: '市场', value: '市场' },
        { label: '生产线', value: '生产线' },
        { label: '客退', value: '客退' },
        { label: '可靠性实验', value: '可靠性实验' }
      ]
    }
    
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
    
    // 处理产品分类选项
    if (productCategoryRes.code === 200) {
      // 将字符串数组转换为选项对象数组
      productCategoryOptions.value = (productCategoryRes.data || []).map(item => ({
        label: item,
        value: item
      }))
    } else {
      console.error('获取产品分类选项失败:', productCategoryRes.msg)
      // 如果接口失败，使用空数组
      productCategoryOptions.value = []
    }
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
      { label: '可靠性实验', value: '可靠性实验' }
    ]
    
    productModelOptions.value = []
    productCategoryOptions.value = []
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
  dialog.form = {
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
  }
  dialog.issueFileList = []
  dialog.actionFileList = []
  roleList.value = []
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
  
  // 设置角色列表
  roleList.value = row.role ? row.role.split(',') : []
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
        // 设置角色字符串
        dialog.form.role = roleString.value
        
        const res = dialog.form.id 
          ? await updateKnowledge(dialog.form)
          : await createKnowledge(dialog.form)
          
        if (res.code === 200) {
          ElMessage.success(dialog.form.id ? '更新成功' : '创建成功')
          dialog.visible = false
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

// 问题附件上传成功
const handleIssueUploadSuccess = (response) => {
  if (response.code === 200) {
    dialog.form.issueAttachmentsId = response.data.id
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}

// 问题附件移除
const handleIssueUploadRemove = () => {
  dialog.form.issueAttachmentsId = null
}

// 措施附件上传成功
const handleActionUploadSuccess = (response) => {
  if (response.code === 200) {
    dialog.form.actionAttachmentsId = response.data.id
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}

// 措施附件移除
const handleActionUploadRemove = () => {
  dialog.form.actionAttachmentsId = null
}

// 上传前校验
const beforeUpload = (file) => {
  const isLt10M = file.size / 1024 / 1024 < 50
  if (!isLt10M) {
    ElMessage.error('上传文件大小不能超过 10MB!')
  }
  return isLt10M
}

// 下载文件
const downloadFile = (url, fileName) => {
  const link = document.createElement('a')
  link.href = url
  link.download = fileName || 'download'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
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
  return typeMap[failureMode] || 'info'
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
  return typeMap[issueSource] || 'info'
}

// 获取完成状态标签类型
const getCompletionStatusTagType = (status) => {
  const typeMap = {
    0: 'info',    // 待完成
    1: 'warning', // 已学习
    2: 'success'  // 已掌握
  }
  return typeMap[status] || 'info'
}

// 获取完成状态文本
const getCompletionStatusText = (status) => {
  const textMap = {
    0: '待完成',
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
      align-items: center;
      flex-wrap: wrap;
      gap: 10px;

      .search-bar-inline {
        flex: 1;
        min-width: 800px;
      }

      .header-buttons {
        display: flex;
        gap: 10px;
        white-space: nowrap;
      }
    }
  }

  .table-container {
    margin-top: 10px;
  }

  .knowledge-info {
    padding: 10px;
    border-radius: 6px;
    border: 1px solid #e2e0df;
    margin-bottom: 8px;
  }

  .knowledge-info-pending {
    background-color: #f0f9ff;
    border-color: #bae6fd;
  }

  .knowledge-info-learning {
    background-color: #fefce8;
    border-color: #fde047;
  }

  .knowledge-info-mastered {
    background-color: #f0fdf4;
    border-color: #bbf7d0;
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
    min-width: 70px;
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

  .knowledge-actions {
    display: flex;
    gap: 5px;
    flex-wrap: wrap;
  }

  .description-container,
  .solution-container,
  .scene-container {
    padding: 10px;
  }

  .description-text,
  .scene-text {
    margin: 0 0 10px 0;
    line-height: 1.5;
    color: #303133;
  }

  .solution-item {
    margin-bottom: 15px;
  }

  .solution-label {
    font-weight: 600;
    color: #606266;
    display: block;
    margin-bottom: 5px;
  }

  .solution-text {
    margin: 0;
    line-height: 1.5;
    color: #303133;
  }

  .attachment-container {
    margin-top: 10px;
    display: flex;
    align-items: center;
    flex-wrap: wrap;
  }

  .attachment-label {
    font-weight: 600;
    color: #606266;
    margin-right: 8px;
  }

  .attachment-link {
    display: flex;
    align-items: center;
    gap: 4px;
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