<template>
  <div class="user-manager">
    <el-card>
      <template #header>
        <div class="card-header">
          <span></span>
          <div class="header-actions">
            <el-form :inline="true" size="small">
              <el-form-item label="用户名:">
                <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSearch">查询</el-button>
                <el-button @click="handleReset">重置</el-button>
              </el-form-item>
            </el-form>
            <div class="header-buttons">
              <el-button v-permission="'user:add'" type="primary" size="small" @click="handleAdd">
                <el-icon><Plus /></el-icon>
                新增用户
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
          <el-table-column prop="id" label="ID" width="80" fixed="left" />
          <el-table-column prop="username" label="用户名" width="150" />
          <el-table-column prop="nickname" label="昵称" width="150" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="updateTime" label="更新时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.updateTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="320" fixed="right">
            <template #default="{ row }">
              <el-button v-permission="'user:update'" type="primary" size="small" link @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>编辑
              </el-button>
              <el-button v-permission="'user:role'" type="warning" size="small" link @click="handleRoleAssignment(row)">
                <el-icon><User /></el-icon>分配角色
              </el-button>
              <el-button 
                v-permission="'user:update'"
                :type="row.status === 1 ? 'danger' : 'success'" 
                size="small" 
                link 
                @click="handleToggleStatus(row)"
              >
                <el-icon><CircleClose v-if="row.status === 1" /><CircleCheck v-else /></el-icon>
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button v-permission="'user:delete'" type="danger" size="small" link @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- <div class="pagination-container" >
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div> -->
      </div>
    </el-card>

    <!-- 用户编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="userForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="userForm.password" 
            type="password" 
            :placeholder="dialogType === 'add' ? '请输入密码' : '留空则不修改密码'"
            :autocomplete="dialogType === 'add' ? 'new-password' : 'new-password'"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="userForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 角色分配对话框 -->
    <el-dialog
      v-model="roleDialogVisible"
      title="分配角色"
      width="600px"
    >
      <div class="role-assignment">
        <div class="current-roles">
          <h4>当前角色</h4>
          <el-tag
            v-for="role in currentUserRoles"
            :key="role.id"
            closable
            @close="handleRemoveRole(role)"
            style="margin-right: 10px; margin-bottom: 10px;"
          >
            {{ role.roleName }}
          </el-tag>
          <el-empty v-if="currentUserRoles.length === 0" description="暂无角色" :image-size="40" />
        </div>
        
        <div class="available-roles">
          <h4>可用角色</h4>
          <el-checkbox-group v-model="selectedRoleIds">
            <el-checkbox
              v-for="role in availableRoles"
              :key="role.id"
              :label="role.id"
              style="margin-right: 15px; margin-bottom: 10px;"
            >
              {{ role.roleName }}
            </el-checkbox>
          </el-checkbox-group>
          <el-empty v-if="availableRoles.length === 0" description="暂无可用角色" :image-size="40" />
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="roleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAssignRoles" :loading="roleSubmitLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import request from '../utils/request'
import PermissionManager from '@/utils/permission'

// 数据定义
interface User {
  id: number
  username: string
  nickname: string
  status: number
  createTime: string
  updateTime: string
}

interface Role {
  id: number
  roleName: string
  roleKey: string
  status: number
}

// 响应式数据
const loading = ref(false)
const tableData = ref<User[]>([])
const searchForm = reactive({
  username: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 用户编辑对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogType = ref<'add' | 'edit'>('add')
const submitLoading = ref(false)
const userFormRef = ref<FormInstance>()

const userForm = reactive({
  id: 0,
  username: '',
  nickname: '',
  password: '',
  status: 1
})

const userRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    {
      validator: (rule: any, value: any, callback: any) => {
        // 如果是编辑模式且密码为空，则不验证密码
        if (dialogType.value === 'edit' && !value) {
          callback()
        } else if (dialogType.value === 'add') {
          // 新增模式下必须输入密码
          if (!value) {
            callback(new Error('请输入密码'))
          } else if (value.length < 1 || value.length > 20) {
            callback(new Error('密码长度在 1 到 20 个字符'))
          } else {
            callback()
          }
        } else {
          // 编辑模式下如果输入了密码，需要验证长度
          if (value && (value.length < 1 || value.length > 20)) {
            callback(new Error('密码长度在 1 到 20 个字符'))
          } else {
            callback()
          }
        }
      },
      trigger: 'blur'
    }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 角色分配对话框
const roleDialogVisible = ref(false)
const roleSubmitLoading = ref(false)
const currentUserId = ref(0)
const currentUserRoles = ref<Role[]>([])
const availableRoles = ref<Role[]>([])
const selectedRoleIds = ref<number[]>([])

// 方法定义
const loadData = async () => {
  loading.value = true
  try {
    const response = await request.get('/sys/user/list', {
  
      current: pagination.current,
      size: pagination.size,
      username: searchForm.username
    })
    // debugger
    
    if (response.code === 200) {
      tableData.value = response.data.records
      pagination.total = response.data.total
    } else {
      ElMessage.error(response.msg || '获取用户列表失败')
    }
  } catch (error) {
    ElMessage.error('获取用户列表失败')
    console.error('Load data error:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  searchForm.username = ''
  pagination.current = 1
  loadData()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  loadData()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  loadData()
}

const handleAdd = () => {
  dialogType.value = 'add'
  dialogTitle.value = '新增用户'
  resetUserForm()
  dialogVisible.value = true
}

const handleEdit = (row: User) => {
  dialogType.value = 'edit'
  dialogTitle.value = '编辑用户'
  userForm.id = row.id
  userForm.username = row.username
  userForm.nickname = row.nickname
  userForm.status = row.status
  userForm.password = '' // 编辑时密码字段保持空白，不加载原密码
  dialogVisible.value = true
}

const handleToggleStatus = async (row: User) => {
  try {
    await ElMessageBox.confirm(
      `确定要${row.status === 1 ? '禁用' : '启用'}用户 "${row.username}" 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await request.put('/sys/user', {
      ...row,
      status: row.status === 1 ? 0 : 1
    })

    if (response.code === 200) {
      ElMessage.success(`${row.status === 1 ? '禁用' : '启用'}成功`)
      loadData()
    } else {
      ElMessage.error(response.msg || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
      console.error('Toggle status error:', error)
    }
  }
}

const handleDelete = async (row: User) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${row.username}" 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await request.delete(`/sys/user/${row.id}`)

    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(response.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error('Delete error:', error)
    }
  }
}

const handleSubmit = async () => {
  if (!userFormRef.value) return
  
  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const url = dialogType.value === 'add' ? '/sys/user' : '/sys/user'
        const method = dialogType.value === 'add' ? 'post' : 'put'
        
        // 如果是编辑模式且密码为空，则不传递密码字段
        let submitData = { ...userForm }
        if (dialogType.value === 'edit' && !submitData.password) {
          delete submitData.password
        }
        
        const response = await request[method](url, submitData)
        
        if (response.code === 200) {
          ElMessage.success(dialogType.value === 'add' ? '新增成功' : '编辑成功')
          dialogVisible.value = false
          loadData()
        } else {
          ElMessage.error(response.msg || '操作失败')
        }
      } catch (error) {
        ElMessage.error('操作失败')
        console.error('Submit error:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDialogClose = () => {
  userFormRef.value?.resetFields()
  resetUserForm()
}

const resetUserForm = () => {
  userForm.id = 0
  userForm.username = ''
  userForm.nickname = ''
  userForm.password = ''
  userForm.status = 1 
}

// 角色分配相关方法
const handleRoleAssignment = async (row: User) => {
  currentUserId.value = row.id
  roleDialogVisible.value = true
  await loadRoles()
}

const loadRoles = async () => {
  try {
    // 获取当前用户的角色
    const userRolesResponse = await request.get(`/sys/userRole/user/${currentUserId.value}`)
    if (userRolesResponse.code === 200) {
      // 获取角色详情
      const roleIds = userRolesResponse.data.map((ur: any) => ur.roleId)
      if (roleIds.length > 0) {
        const rolesResponse = await Promise.all(
          roleIds.map(id => request.get(`/sys/role/${id}`))
        )
        currentUserRoles.value = rolesResponse.map(r => r.data)
      } else {
        currentUserRoles.value = []
      }
    }

    // 获取所有可用角色
    const allRolesResponse = await request.get('/sys/role/list', {
      current: 1,
      size: 100
    })
    if (allRolesResponse.code === 200) {
      availableRoles.value = allRolesResponse.data.records.filter((role: Role) => role.status === 1)
    }

    // 设置已选中的角色ID
    selectedRoleIds.value = currentUserRoles.value.map(role => role.id)
  } catch (error) {
    ElMessage.error('加载角色数据失败')
    console.error('Load roles error:', error)
  }
}

const handleRemoveRole = async (role: Role) => {
  try {
    const response = await request.delete(`/sys/userRole/user/${currentUserId.value}/role/${role.id}`)
    if (response.code === 200) {
      ElMessage.success('移除角色成功')
      await loadRoles()
    } else {
      ElMessage.error(response.msg || '移除角色失败')
    }
  } catch (error) {
    ElMessage.error('移除角色失败')
    console.error('Remove role error:', error)
  }
}

const handleAssignRoles = async () => {
  roleSubmitLoading.value = true
  try {
    // 构建用户角色关联数据
    const userRoles = selectedRoleIds.value.map(roleId => ({
      userId: currentUserId.value,
      roleId: roleId
    }))

    const response = await request.post('/sys/userRole/batch', userRoles)
    
    if (response.code === 200) {
      ElMessage.success('分配角色成功')
      roleDialogVisible.value = false
    } else {
      ElMessage.error(response.msg || '分配角色失败')
    }
  } catch (error) {
    ElMessage.error('分配角色失败')
    console.error('Assign roles error:', error)
  } finally {
    roleSubmitLoading.value = false
  }
}

// 工具方法
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.user-manager {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.header-buttons {
  display: flex;
  gap: 10px;
}

.table-container {
  margin-top: 20px;
  margin-left: 12%;
  width: 70%;
}

/* 修复表格在最大化时表头与内容错位的问题 */
.table-container :deep(.el-table) {
  table-layout: fixed;
  width: 100%;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.role-assignment {
  padding: 20px 0;
}

.current-roles {
  margin-bottom: 30px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 5px;
}

.available-roles {
  padding: 20px;
  background: #f0f9ff;
  border-radius: 5px;
}

.current-roles h4,
.available-roles h4 {
  margin-bottom: 15px;
  color: #333;
}

@media (max-width: 768px) {
  .header-actions {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }
}
</style>