<template>
  <div class="item-type-manager">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>物料类型管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增物料类型
          </el-button>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-area">
        <el-form :inline="true" :model="searchForm" size="small">
          <el-form-item label="物料前缀:">
            <el-input v-model="searchForm.itemPrefix" placeholder="请输入物料前缀" clearable />
          </el-form-item>
          <el-form-item label="物料类型:">
            <el-input v-model="searchForm.typeName" placeholder="请输入物料类型" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 表格区域 -->
      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="itemPrefix" label="物料前缀" width="120" />
        <el-table-column prop="typeName" label="物料类型" min-width="150" />
        <el-table-column v-if="false" prop="description" label="描述" />
        <el-table-column v-if="false" prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>


    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.title"
      width="500px"
    >
      <el-form :model="dialog.form" :rules="dialog.rules" ref="formRef" label-width="100px">
        <el-form-item label="物料前缀" prop="itemPrefix">
          <el-input v-model="dialog.form.itemPrefix" placeholder="请输入物料前缀" />
        </el-form-item>
        <el-form-item label="物料类型" prop="typeName">
          <el-input v-model="dialog.form.typeName" placeholder="请输入物料类型" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="dialog.form.description" type="textarea" placeholder="请输入描述" />
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
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { 
  getItemTypeList, 
  getItemTypeById, 
  createItemType, 
  updateItemType, 
  deleteItemType 
} from '@/api/itemType'
import { http } from '@/utils/request'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  itemPrefix: '',
  typeName: ''
})

// 对话框
const dialog = reactive({
  visible: false,
  title: '',
  form: {
    id: null,
    itemPrefix: '',
    typeName: '',
    description: ''
  },
  rules: {
    itemPrefix: [
      { required: true, message: '请输入物料前缀', trigger: 'blur' },
      { min: 3, max: 10, message: '长度在 3 到 10 个字符', trigger: 'blur' }
    ],
    typeName: [
      { required: true, message: '请输入物料类型', trigger: 'blur' },
      { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
    ]
  }
})

// 表单引用
const formRef = ref(null)

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
    
    const response = await getItemTypeList(params)
    
    if (response.code === 200) {
      tableData.value = response.data.records || []
      total.value = response.data.total || 0
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

// 新增
const handleAdd = () => {
  dialog.title = '新增物料类型'
  dialog.form = {
    id: null,
    itemPrefix: '',
    typeName: '',
    description: ''
  }
  dialog.visible = true
}

// 编辑
const handleEdit = (row) => {
  dialog.title = '编辑物料类型'
  dialog.form = {
    id: row.id,
    itemPrefix: row.itemPrefix,
    typeName: row.typeName,
    description: row.description || ''
  }
  dialog.visible = true
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该物料类型吗？此操作不可恢复。', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deleteItemType(row.id)
    
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

// 保存
const save = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    const response = dialog.form.id 
      ? await updateItemType(dialog.form)
      : await createItemType(dialog.form)
    
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
</script>

<style scoped>
.item-type-manager {
  padding: 20px;
  height: 100%;
  box-sizing: border-box;
}

.el-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.el-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-area {
  margin-bottom: 20px;
  flex-shrink: 0;
}

.el-table {
  flex: 1;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  flex-shrink: 0;
}
</style>