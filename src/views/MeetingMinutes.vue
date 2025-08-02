<template>
  <div class="meeting-minutes-container">
    <div class="page-header">
      <h1>会议纪要管理</h1>
      <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索机型名称、配套厂家、业务员或会议记录"
        prefix-icon="Search"
        style="width: 350px"
        clearable
      />
    </div>
      <div class="header-actions">
        <el-checkbox v-model="showCompleted" style="margin-right: 16px;">
          显示已完成项目
        </el-checkbox>
        <el-button type="primary" @click="showAddDialog = true">
          <el-icon><Plus /></el-icon>
          新增项目
        </el-button>
      </div>
    </div>



    

    <!-- 会议纪要列表 -->
    <div class="minutes-list">
      <div
        v-for="(minute, index) in filteredMinutes"
        :key="minute.id"
        class="minute-item"
      >
        
        <div class="item-content">
          <div class="content-row horizontal-info">
            <div class="info-item">
              <span class="content-label">机型名称：</span>
              <span class="content-value">{{ minute.modelName }}</span>
            </div>
            <div class="info-item">
              <span class="content-label">配套厂家：</span>
              <span class="content-value">{{ minute.supplier }}</span>
            </div>
            <div class="info-item">
              <span class="content-label">业务人员：</span>
              <span class="content-value">{{ minute.salesPerson }}</span>
            </div>
            <div class="item-actions">
            <el-tag :type="minute.isFinish ? 'success' : 'info'" style="margin-top: 4px;">
              {{ minute.isFinish ? '已完成' : '未完成' }}
            </el-tag>
            <el-button type="primary" text @click="editMinutes(minute)">编辑</el-button>
            <el-button type="danger" text @click="deleteMinutes(minute)">删除</el-button>
          </div>
          </div>
          
          <!-- 会议记录 -->
          <div class="meeting-section">
            <!-- <div class="section-header">
              <div class="section-title">会议记录</div>
            </div> -->
            <div v-if="minute.rawData?.meetingInfoList?.length && minute.rawData.meetingInfoList.some(m => m.content !== null && m.content !== undefined)" class="meetings">
              <ul class="meeting-items">
                <li 
                  v-for="(meeting, meetingIndex) in minute.rawData.meetingInfoList" 
                  :key="meeting.id" 
                  class="meeting-item-with-actions" 
                  :class="{ 'marked': meeting.isMarked }"
                >
                  <div class="meeting-content-row">
                    <span class="meeting-date-inline">{{ meeting.date }}</span>
                    <span class="item-content" :class="{ 'marked-content': meeting.isMarked }">{{ meeting.content }}</span>
                    <div class="item-actions">
                      <el-button 
                        size="small" 
                        :type="meeting.isMarked ? 'warning' : 'info'"
                        text 
                        @click="toggleMarkItem(meeting)"
                      >
                        {{ meeting.isMarked ? '取消标记' : '标记' }}
                      </el-button>
                      <el-button 
                        size="small" 
                        type="primary" 
                        text 
                        @click="editMeetingItem(meeting)"
                      >
                        修改
                      </el-button>
                    </div>
                  </div>
                </li>
              </ul>
              <div class="add-meeting-button">
                <el-button 
                  size="small" 
                  type="primary" 
                  @click="addMeetingInfo(minute)"
                >
                  <el-icon><Plus /></el-icon>
                  新增会议记录
                </el-button>
              </div>
            </div>
            <div v-else class="no-meetings">
              <div class="no-meetings-text">暂无会议记录</div>
              <div class="add-meeting-button">
                <el-button 
                  size="small" 
                  type="primary" 
                  @click="addMeetingInfo(minute)"
                >
                  <el-icon><Plus /></el-icon>
                  新增会议记录
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="isEdit ? '编辑会议纪要' : '新增会议纪要'"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="机型名称" prop="modelName">
          <el-select
            v-model="formData.modelName"
            filterable
            allow-create
            placeholder="请选择或输入机型名称"
            style="width: 100%"
          >
            <el-option
              v-for="item in modelNameOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="配套厂家" prop="supplier">
          <el-select
            v-model="formData.supplier"
            filterable
            allow-create
            placeholder="请选择或输入配套厂家"
            style="width: 100%"
          >
            <el-option
              v-for="item in supplierOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="业务员" prop="salesPerson">
          <el-select
            v-model="formData.salesPerson"
            filterable
            allow-create
            placeholder="请选择或输入业务员姓名"
            style="width: 100%"
          >
            <el-option
              v-for="item in salesPersonOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="完成状态">
          <el-checkbox v-model="formData.isFinish">已完成</el-checkbox>
        </el-form-item>

      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false; resetForm()">取消</el-button>
        <el-button type="primary" @click="saveMinutes">保存</el-button>
      </template>
    </el-dialog>

    

    <!-- 添加会议记录对话框 -->
    <el-dialog
      v-model="showAddMeetingDialog"
      title="新增会议记录"
      width="500px"
    >
      <el-form
        ref="addMeetingFormRef"
        :model="addMeetingFormData"
        :rules="addMeetingFormRules"
        label-width="100px"
      >
        <el-form-item label="会议日期" prop="date">
          <el-date-picker
            v-model="addMeetingFormData.date"
            type="date"
            placeholder="请选择会议日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddMeetingDialog = false; resetAddMeetingForm()">取消</el-button>
        <el-button type="primary" @click="saveMeetingInfo">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'

interface MeetingMinute {
  id: string
  modelName: string
  supplier: string
  salesPerson: string
  meetingNotes: string
  createdAt: string
  isFinish: boolean
  rawData?: any
}

// 响应式数据
const searchKeyword = ref('')
const minutes = ref<MeetingMinute[]>([])
const showAddDialog = ref(false)
const showAddMeetingDialog = ref(false)
const showCompleted = ref(true)
const isEdit = ref(false)
const editingId = ref<string | null>(null)
const formRef = ref()
const addMeetingFormRef = ref()
const currentMinuteId = ref<string | null>(null)

// 历史数据选项
const modelNameOptions = ref<string[]>([])
const supplierOptions = ref<string[]>([])
const salesPersonOptions = ref<string[]>([])

// 表单数据
const formData = ref({
  modelName: '',
  supplier: '',
  salesPerson: '',
  isFinish: false
})



// 会议日期表单数据
const addMeetingFormData = ref({
  date: new Date().toISOString().split('T')[0]
})

// 会议日期表单验证规则
const addMeetingFormRules = {
  date: [
    { required: true, message: '请选择会议日期', trigger: 'change' }
  ]
}



// 表单验证规则
const formRules = {
  modelName: [
    { required: true, message: '请输入机型名称', trigger: 'blur' }
  ],
  supplier: [
    { required: true, message: '请输入配套厂家', trigger: 'blur' }
  ],
  salesPerson: [
    { required: true, message: '请输入业务员姓名', trigger: 'blur' }
  ]
}

// 计算属性：过滤后的数据
const filteredMinutes = computed(() => {
  let filtered = minutes.value
  
  // 根据完成状态过滤
  if (!showCompleted.value) {
    filtered = filtered.filter(item => !item.isFinish)
  }
  
  // 根据搜索关键词过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(item =>
      item.modelName.toLowerCase().includes(keyword) ||
      item.supplier.toLowerCase().includes(keyword) ||
      item.salesPerson.toLowerCase().includes(keyword) ||
      item.meetingNotes.toLowerCase().includes(keyword)
    )
  }
  
  return filtered
})



// 方法：保存会议纪要
const saveMinutes = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const meetingData = {
          machineType: formData.value.modelName,
          factory: formData.value.supplier,
          salesPerson: formData.value.salesPerson,
          isFinish: formData.value.isFinish,
          meetingNotes: ''
        }

        let response
        let result

        if (isEdit.value && editingId.value) {
          // 更新会议纪要
          response = await fetch(`${API_BASE_URL}/meeting/update`, {
            method: 'PUT',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({
              ...meetingData,
              id: parseInt(editingId.value)
            })
          })
          result = await response.json()
          
          if (result.code === 200) {
            await loadMeetingData()
            ElMessage.success('更新成功')
          } else {
            ElMessage.error('更新失败: ' + (result.msg || '未知错误'))
            return
          }
        } else {
          // 新增会议纪要
          response = await fetch(`${API_BASE_URL}/meeting/add`, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify(meetingData)
          })
          result = await response.json()
          
          if (result.code === 200) {
            await loadMeetingData()
            ElMessage.success('添加成功')
          } else {
            ElMessage.error('添加失败: ' + (result.msg || '未知错误'))
            return
          }
        }
        
        showAddDialog.value = false
        resetForm()
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error('网络错误，请检查接口连接')
      }
    }
  })
}

// 方法：编辑会议纪要
const editMinutes = (row: MeetingMinute) => {
  isEdit.value = true
  editingId.value = row.id
  formData.value = {
          modelName: row.modelName,
          supplier: row.supplier,
          salesPerson: row.salesPerson,
          isFinish: row.isFinish
        }
  showAddDialog.value = true
}

// 方法：删除会议纪要
const deleteMinutes = async (row: MeetingMinute) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除这条会议纪要吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await fetch(`${API_BASE_URL}/meeting/delete/${row.id}`, {
      method: 'DELETE'
    })
    const result = await response.json()
    
    if (result.code === 200) {
      await loadMeetingData()
      ElMessage.success('删除成功')
    } else {
      ElMessage.error('删除失败: ' + (result.msg || '未知错误'))
    }
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('网络错误，请检查接口连接')
  }
}

// 方法：重置表单
const resetForm = () => {
  formData.value = {
    modelName: '',
    supplier: '',
    salesPerson: '',
    isFinish: false
  }
  editingId.value = null
}



// 方法：重置会议日期表单
const resetAddMeetingForm = () => {
  addMeetingFormData.value = {
    date: new Date().toISOString().split('T')[0]
  }
  currentMinuteId.value = null
}

// API基础URL
const API_BASE_URL = 'http://192.168.90.64:8083'

// 从接口获取数据
const loadMeetingData = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/meeting/getAll`)
    const result = await response.json()
    
    if (result.code === 200 && result.data) {
      // 转换接口数据为页面需要的格式
          minutes.value = result.data.map(item => ({
            id: item.id.toString(),
            modelName: item.machineType || item.modelName || '',
            supplier: item.factory || item.supplier || '',
            salesPerson: item.salesPerson || '',
            meetingNotes: item.meetingNotes || formatMeetingNotes(item.meetingInfoList),
            createdAt: item.createdAt || item.meetingInfoList?.[0]?.date || new Date().toLocaleDateString(),
            isFinish: item.isFinish || false,
            rawData: item // 保留原始数据用于展示详情
          }))

      // 提取历史数据选项
      const modelNames = new Set<string>()
      const suppliers = new Set<string>()
      const salesPersons = new Set<string>()

      result.data.forEach(item => {
        if (item.machineType || item.modelName) {
          modelNames.add(item.machineType || item.modelName)
        }
        if (item.factory || item.supplier) {
          suppliers.add(item.factory || item.supplier)
        }
        if (item.salesPerson) {
          salesPersons.add(item.salesPerson)
        }
      })

      modelNameOptions.value = Array.from(modelNames).sort()
      supplierOptions.value = Array.from(suppliers).sort()
      salesPersonOptions.value = Array.from(salesPersons).sort()
    } else {
      ElMessage.error('获取数据失败: ' + (result.msg || '未知错误'))
    }
  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error('网络错误，请检查接口连接')
  }
}

// 方法：编辑会议内容
const editMeetingItem = async (meeting) => {
  try {
    const { value } = await ElMessageBox.prompt(
      '请输入新的会议内容',
      '编辑会议内容',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputValue: meeting.content,
        inputPattern: /^.{1,500}$/,
        inputErrorMessage: '内容不能为空且不超过500字符'
      }
    )
    
    if (meeting.id) {
      const response = await fetch(`${API_BASE_URL}/meeting/info/update`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          id: meeting.id,
          content: value,
          isMarked: meeting.isMarked,
          date: meeting.date
        })
      })
      const result = await response.json()
      
      if (result.code === 200) {
        await loadMeetingData()
        ElMessage.success('修改成功')
      } else {
        ElMessage.error('修改失败: ' + (result.msg || '未知错误'))
      }
    }
  } catch {
    // 用户取消编辑
  }
}

// 方法：切换标记状态
const toggleMarkItem = async (meeting) => {
  try {
    const newMarkedStatus = !meeting.isMarked
    
    if (meeting.id) {
      const response = await fetch(`${API_BASE_URL}/meeting/info/update`, {
         method: 'PUT',
         headers: {
           'Content-Type': 'application/json',
         },
         body: JSON.stringify({
           id: meeting.id,
           content: meeting.content,
           isMarked: newMarkedStatus,
           date: meeting.date
         })
       })
      const result = await response.json()
      
      if (result.code === 200) {
        await loadMeetingData()
        const message = newMarkedStatus ? '已标记' : '已取消标记'
        ElMessage.success(message)
      } else {
        ElMessage.error('操作失败: ' + (result.msg || '未知错误'))
      }
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('网络错误，请检查接口连接')
  }
}





// 方法：新增会议记录 - 显示日期选择对话框
const addMeetingInfo = (minute) => {
  currentMinuteId.value = minute.id
  addMeetingFormData.value.date = new Date().toLocaleDateString('zh-CN')
  showAddMeetingDialog.value = true
}

// 方法：保存会议记录
const saveMeetingInfo = () => {
  addMeetingFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const newMeeting = {
          meetingId: currentMinuteId.value,
          date: addMeetingFormData.value.date,
          meetingItemList: []
        }

        const response = await fetch(`${API_BASE_URL}/meeting/info/add`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(newMeeting)
        })
        const result = await response.json()

        if (result.code === 200) {
          await loadMeetingData()
          showAddMeetingDialog.value = false
          resetAddMeetingForm()
          ElMessage.success('新增会议记录成功')
        } else {
          ElMessage.error('新增会议记录失败: ' + (result.msg || '未知错误'))
        }
      } catch (error) {
        console.error('新增会议记录失败:', error)
        ElMessage.error('网络错误，请检查接口连接')
      }
    }
  })
}





// 格式化会议记录
const formatMeetingNotes = (meetingInfoList) => {
  if (!meetingInfoList || !Array.isArray(meetingInfoList)) return '暂无会议记录'
  
  return meetingInfoList
    .map(info => `${info.date}: ${info.content || ''}`)
    .join('\n')
}

// 生命周期
onMounted(() => {
  loadMeetingData()
})
</script>

<style scoped>
.meeting-minutes-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20px;
}

/* 页面标题 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 0 20px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.add-btn {
  background-color: #409eff;
  border-color: #409eff;
}



/* 搜索区域 */
.search-section {
  margin-bottom: 24px;
  padding: 0 20px;
}

.search-input {
  width: 300px;
}

/* 会议纪要列表 */
.minutes-list {
  padding: 0 20px;
}

.minute-item {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e4e7ed;
}

.item-number {
  font-size: 18px;
  font-weight: bold;
  color: #409eff;
}

.item-actions {

  display: flex;
  gap: 8px;
}

.item-content {
  color: #606266;
}

.content-row {
  display: flex;
  margin-bottom: 8px;
  align-items: flex-start;
}

.content-row.horizontal-info {
  display: flex;
  gap: 32px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.content-label {
  font-weight: 600;
  color: #2c3e50;
  min-width: 80px;
  flex-shrink: 0;
}

.content-value {
  color: #606266;
}

/* 会议记录 */
.meeting-section {
  margin-top: 16px;
}

.section-header {
  margin-bottom: 12px;
}

.section-title {
  font-weight: 600;
  color: #2c3e50;
  font-size: 16px;
}

.meetings {
  margin-top: 8px;
}



.meeting-content-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: space-between;
}

.meeting-date-inline {
  font-weight: 600;
  color: #409eff;
  font-size: 14px;
  flex-shrink: 0;
  white-space: nowrap;
}

.meeting-header .el-button {
  position: absolute;
  right: 0;
}

.meeting-items {
  margin: 0;
  padding-left: 0;
  list-style: none;
}

.add-item-section {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #e4e7ed;
}

.meeting-item-with-actions {
  display: flex;
  flex-direction: column;
  padding: 12px;
  margin-bottom: 8px;
  background-color: #f5f7fa;
  border-radius: 4px;
  border-left: 3px solid #409eff;
  width: 100%;
  box-sizing: border-box;
}

.meeting-item-with-actions.marked {
  border-left-color: #e6a23c;
  background-color: #fdf6ec;
}

.item-content {
  color: #606266;
  flex: 1 1 auto;
  word-break: break-word;
  min-width: 0;
  text-align: left;
  margin-right: 8px;
}

.marked-content {
  color: #f56c6c;
  font-weight: bold;
  transform: none;
  text-align: left;
}

.item-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
  margin-top: 0;
}

.meeting-items li {
  margin-bottom: 4px;
  color: #606266;
  line-height: 1.5;
}

.no-meetings {
  color: #909399;
  font-style: italic;
  padding: 8px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
}

.no-meetings-text {
  margin-bottom: 8px;
}

.add-meeting-button {
  margin-top: 12px;
  text-align: left;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #909399;
  font-size: 16px;
}

/* 表单样式 */
.meeting-form-container {
  max-height: 400px;
  overflow-y: auto;
}

.meeting-form-item {
  background-color: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 16px;
  margin-bottom: 12px;
}

.meeting-item-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  gap: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .meeting-minutes-container {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }
  
  .search-section {
    padding: 0 10px;
  }
  
  .minutes-list {
    padding: 0 10px;
  }
  
  .content-row.horizontal-info {
    flex-direction: column;
    gap: 12px;
  }
  
  .info-item {
    flex-direction: row;
  }
  
  .meeting-item-with-actions {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }
  
  .item-content {
    margin-right: 0;
    margin-bottom: 8px;
  }
  
  .item-actions {
    justify-content: flex-end;
  }
  
  .dialog-footer {
    flex-direction: column;
    gap: 8px;
  }
  
  .dialog-footer .el-button {
    width: 100%;
  }
}
</style>