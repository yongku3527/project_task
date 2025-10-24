<template>
  <div class="role-permission-manager">
    <el-row :gutter="20">
      <!-- 角色管理 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>角色管理</span>
              <el-button type="primary" size="small" @click="handleAddRole">
                <el-icon><Plus /></el-icon>
                新增角色
              </el-button>
            </div>
          </template>

          <el-table 
            :data="roleData" 
            v-loading="roleLoading"
            style="width: 100%"
            row-key="id"
            border
            stripe
            @row-click="handleRoleRowClick"
            highlight-current-row
          >
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="roleName" label="角色名称" width="120" />
            <el-table-column prop="roleKey" label="角色标识" width="120" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" link @click.stop="handleEditRole(row)">
                  <el-icon><Edit /></el-icon>编辑
                </el-button>
                <el-button type="warning" size="small" link @click.stop="handleRolePermission(row)">
                  <el-icon><Key /></el-icon>权限
                </el-button>
                <el-button type="danger" size="small" link @click.stop="handleDeleteRole(row)">
                  <el-icon><Delete /></el-icon>删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-container">
            <el-pagination
              v-model:current-page="rolePagination.current"
              v-model:page-size="rolePagination.size"
              :page-sizes="[10, 20, 50]"
              :total="rolePagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleRoleSizeChange"
              @current-change="handleRoleCurrentChange"
            />
          </div>
        </el-card>
      </el-col>

      <!-- 权限管理 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>权限管理</span>
              <el-button type="primary" size="small" @click="handleAddPermission">
                <el-icon><Plus /></el-icon>
                新增权限
              </el-button>
            </div>
          </template>

          <el-table 
            :data="permissionData" 
            v-loading="permissionLoading"
            style="width: 100%"
            row-key="id"
            border
            stripe
          >
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="permName" label="权限名称" width="120" />
            <el-table-column prop="permCode" label="权限编码" width="120" />
            <el-table-column prop="permType" label="类型" width="80">
              <template #default="{ row }">
                <el-tag :type="row.permType === 1 ? 'primary' : 'info'">
                  {{ row.permType === 1 ? '菜单' : '按钮' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" link @click="handleEditPermission(row)">
                  <el-icon><Edit /></el-icon>编辑
                </el-button>
                <el-button type="danger" size="small" link @click="handleDeletePermission(row)">
                  <el-icon><Delete /></el-icon>删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-container">
            <el-pagination
              v-model:current-page="permissionPagination.current"
              v-model:page-size="permissionPagination.size"
              :page-sizes="[10, 20, 50]"
              :total="permissionPagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handlePermissionSizeChange"
              @current-change="handlePermissionCurrentChange"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 角色编辑对话框 -->
    <el-dialog
      v-model="roleDialogVisible"
      :title="roleDialogTitle"
      width="500px"
      @close="handleRoleDialogClose"
    >
      <el-form
        ref="roleFormRef"
        :model="roleForm"
        :rules="roleRules"
        label-width="80px"
      >
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="roleForm.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色标识" prop="roleKey">
          <el-input v-model="roleForm.roleKey" placeholder="请输入角色标识" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="roleForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="roleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitRole" :loading="roleSubmitLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 权限编辑对话框 -->
    <el-dialog
      v-model="permissionDialogVisible"
      :title="permissionDialogTitle"
      width="500px"
      @close="handlePermissionDialogClose"
    >
      <el-form
        ref="permissionFormRef"
        :model="permissionForm"
        :rules="permissionRules"
        label-width="80px"
      >
        <el-form-item label="权限名称" prop="permName">
          <el-input v-model="permissionForm.permName" placeholder="请输入权限名称" />
        </el-form-item>
        <el-form-item label="权限编码" prop="permCode">
          <el-input v-model="permissionForm.permCode" placeholder="请输入权限编码" />
        </el-form-item>
        <el-form-item label="权限类型" prop="permType">
          <el-radio-group v-model="permissionForm.permType">
            <el-radio :value="1">菜单</el-radio>
            <el-radio :value="2">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="父级权限" prop="parentId">
          <el-select v-model="permissionForm.parentId" placeholder="请选择父级权限" clearable>
            <el-option
              v-for="perm in parentPermissions"
              :key="perm.id"
              :label="perm.permName"
              :value="perm.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="permissionForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="permissionDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitPermission" :loading="permissionSubmitLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 角色权限分配对话框 -->
    <el-dialog
      v-model="rolePermissionDialogVisible"
      title="角色权限分配"
      width="700px"
    >
      <div class="permission-tree-container">
        <el-tree
          ref="permissionTreeRef"
          :data="permissionTree"
          show-checkbox
          node-key="id"
          :default-checked-keys="checkedPermissionIds"
          :props="treeProps"
          default-expand-all
          style="max-height: 400px; overflow-y: auto;"
        />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rolePermissionDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAssignRolePermissions" :loading="rolePermissionSubmitLoading">
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

// 数据定义
interface Role {
  id: number
  roleName: string
  roleKey: string
  status: number
  createTime: string
  updateTime: string
}

interface Permission {
  id: number
  permName: string
  permCode: string
  parentId: number
  permType: number
  status: number
  createTime: string
  updateTime: string
}

interface PermissionTreeNode {
  id: number
  label: string
  permCode: string
  permType: number
  children?: PermissionTreeNode[]
}

// 角色相关数据
const roleData = ref<Role[]>([])
const roleLoading = ref(false)
const rolePagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 权限相关数据
const permissionData = ref<Permission[]>([])
const permissionLoading = ref(false)
const permissionPagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 角色编辑对话框
const roleDialogVisible = ref(false)
const roleDialogTitle = ref('')
const roleDialogType = ref<'add' | 'edit'>('add')
const roleSubmitLoading = ref(false)
const roleFormRef = ref<FormInstance>()

const roleForm = reactive({
  id: 0,
  roleName: '',
  roleKey: '',
  status: 1
})

const roleRules: FormRules = {
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 20, message: '角色名称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  roleKey: [
    { required: true, message: '请输入角色标识', trigger: 'blur' },
    { min: 3, max: 30, message: '角色标识长度在 3 到 30 个字符', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 权限编辑对话框
const permissionDialogVisible = ref(false)
const permissionDialogTitle = ref('')
const permissionDialogType = ref<'add' | 'edit'>('add')
const permissionSubmitLoading = ref(false)
const permissionFormRef = ref<FormInstance>()

const permissionForm = reactive({
  id: 0,
  permName: '',
  permCode: '',
  parentId: null as number | null,
  permType: 1,
  status: 1
})

const permissionRules: FormRules = {
  permName: [
    { required: true, message: '请输入权限名称', trigger: 'blur' },
    { min: 1, max: 20, message: '权限名称长度在 1 到 20 个字符', trigger: 'blur' }
  ],
  permCode: [
    { required: true, message: '请输入权限编码', trigger: 'blur' },
    // { min: 3, max: 50, message: '权限编码长度在 3 到 50 个字符', trigger: 'blur' }
  ],
  permType: [
    { required: true, message: '请选择权限类型', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 角色权限分配对话框
const rolePermissionDialogVisible = ref(false)
const rolePermissionSubmitLoading = ref(false)
const currentRoleId = ref(0)
const permissionTreeRef = ref()

const permissionTree = ref<PermissionTreeNode[]>([])
const checkedPermissionIds = ref<number[]>([])
const parentPermissions = ref<Permission[]>([])

const treeProps = {
  children: 'children',
  label: 'label'
}

// 方法定义
const loadRoleData = async () => {
  roleLoading.value = true
  try {
    const response = await request.get('/sys/role/list', {
      current: rolePagination.current,
      size: rolePagination.size
    })
    
    if (response.code === 200) {
      roleData.value = response.data.records
      rolePagination.total = response.data.total
    } else {
      ElMessage.error(response.msg || '获取角色列表失败')
    }
  } catch (error) {
    ElMessage.error('获取角色列表失败')
    console.error('Load role data error:', error)
  } finally {
    roleLoading.value = false
  }
}

const loadPermissionData = async () => {
  permissionLoading.value = true
  try {
    const response = await request.get('/sys/permission/list', {
      current: permissionPagination.current,
      size: permissionPagination.size
    })
    
    if (response.code === 200) {
      permissionData.value = response.data.records
      permissionPagination.total = response.data.total
    } else {
      ElMessage.error(response.msg || '获取权限列表失败')
    }
  } catch (error) {
    ElMessage.error('获取权限列表失败')
    console.error('Load permission data error:', error)
  } finally {
    permissionLoading.value = false
  }
}

const handleRoleRowClick = (row: Role) => {
  // 可以在这里添加点击角色行的逻辑
  console.log('Clicked role:', row)
}

const handleAddRole = () => {
  roleDialogType.value = 'add'
  roleDialogTitle.value = '新增角色'
  resetRoleForm()
  roleDialogVisible.value = true
}

const handleEditRole = (row: Role) => {
  roleDialogType.value = 'edit'
  roleDialogTitle.value = '编辑角色'
  roleForm.id = row.id
  roleForm.roleName = row.roleName
  roleForm.roleKey = row.roleKey
  roleForm.status = row.status
  roleDialogVisible.value = true
}

const handleDeleteRole = async (row: Role) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除角色 "${row.roleName}" 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await request.delete(`/sys/role/${row.id}`)

    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadRoleData()
    } else {
      ElMessage.error(response.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error('Delete role error:', error)
    }
  }
}

const handleAddPermission = async () => {
  permissionDialogType.value = 'add'
  permissionDialogTitle.value = '新增权限'
  resetPermissionForm()
  await loadParentPermissions()
  permissionDialogVisible.value = true
}

const handleEditPermission = async (row: Permission) => {
  permissionDialogType.value = 'edit'
  permissionDialogTitle.value = '编辑权限'
  permissionForm.id = row.id
  permissionForm.permName = row.permName
  permissionForm.permCode = row.permCode
  permissionForm.parentId = row.parentId
  permissionForm.permType = row.permType
  permissionForm.status = row.status
  await loadParentPermissions()
  permissionDialogVisible.value = true
}

const handleDeletePermission = async (row: Permission) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除权限 "${row.permName}" 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await request.delete(`/sys/permission/${row.id}`)

    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadPermissionData()
    } else {
      ElMessage.error(response.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error('Delete permission error:', error)
    }
  }
}

const handleRolePermission = async (row: Role) => {
  currentRoleId.value = row.id
  // 先清空之前的权限选择，避免显示上一个角色的权限
  checkedPermissionIds.value = []
  rolePermissionDialogVisible.value = true
  await loadRolePermissions()
}

const loadParentPermissions = async () => {
  try {
    const response = await request.get('/sys/permission/tree')
    if (response.code === 200) {
      parentPermissions.value = response.data.filter((perm: Permission) => perm.parentId === 0 || !perm.parentId)
    }
  } catch (error) {
    console.error('Load parent permissions error:', error)
  }
}

const loadRolePermissions = async () => {
  try {
    // 获取所有权限树
    const treeResponse = await request.get('/sys/permission/tree')
    if (treeResponse.code === 200) {
      permissionTree.value = buildPermissionTree(treeResponse.data)
    }

    // 获取当前角色的权限
    const rolePermResponse = await request.get(`/sys/rolePermission/role/${currentRoleId.value}`)
    if (rolePermResponse.code === 200) {
      const permissionIds = rolePermResponse.data.map((rp: any) => rp.permissionId)
      checkedPermissionIds.value = permissionIds
    } else {
      // 如果获取角色权限失败，清空权限选择
      checkedPermissionIds.value = []
    }
  } catch (error) {
    ElMessage.error('加载权限数据失败')
    console.error('Load role permissions error:', error)
    // 发生错误时也清空权限选择
    checkedPermissionIds.value = []
  }
}

const buildPermissionTree = (permissions: Permission[]): PermissionTreeNode[] => {
  const map = new Map<number, PermissionTreeNode>()
  const result: PermissionTreeNode[] = []

  // 首先创建所有节点
  permissions.forEach(perm => {
    map.set(perm.id, {
      id: perm.id,
      label: perm.permName,
      permCode: perm.permCode,
      permType: perm.permType,
      children: []
    })
  })

  // 然后构建树形结构
  permissions.forEach(perm => {
    const node = map.get(perm.id)!
    if (perm.parentId === 0 || !perm.parentId) {
      result.push(node)
    } else {
      const parent = map.get(perm.parentId)
      if (parent) {
        if (!parent.children) {
          parent.children = []
        }
        parent.children.push(node)
      }
    }
  })

  return result
}

const handleSubmitRole = async () => {
  if (!roleFormRef.value) return
  
  await roleFormRef.value.validate(async (valid) => {
    if (valid) {
      roleSubmitLoading.value = true
      try {
        const url = roleDialogType.value === 'add' ? '/sys/role' : '/sys/role'
        const method = roleDialogType.value === 'add' ? 'post' : 'put'
        
        const response = await request[method](url, roleForm)
        
        if (response.code === 200) {
          ElMessage.success(roleDialogType.value === 'add' ? '新增成功' : '编辑成功')
          roleDialogVisible.value = false
          loadRoleData()
        } else {
          ElMessage.error(response.msg || '操作失败')
        }
      } catch (error) {
        ElMessage.error('操作失败')
        console.error('Submit role error:', error)
      } finally {
        roleSubmitLoading.value = false
      }
    }
  })
}

const handleSubmitPermission = async () => {
  if (!permissionFormRef.value) return
  
  await permissionFormRef.value.validate(async (valid) => {
    if (valid) {
      permissionSubmitLoading.value = true
      try {
        const url = permissionDialogType.value === 'add' ? '/sys/permission' : '/sys/permission'
        const method = permissionDialogType.value === 'add' ? 'post' : 'put'
        
        const response = await request[method](url, permissionForm)
        
        if (response.code === 200) {
          ElMessage.success(permissionDialogType.value === 'add' ? '新增成功' : '编辑成功')
          permissionDialogVisible.value = false
          loadPermissionData()
        } else {
          ElMessage.error(response.msg || '操作失败')
        }
      } catch (error) {
        ElMessage.error('操作失败')
        console.error('Submit permission error:', error)
      } finally {
        permissionSubmitLoading.value = false
      }
    }
  })
}

const handleAssignRolePermissions = async () => {
  rolePermissionSubmitLoading.value = true
  try {
    // 获取选中的权限ID
    const checkedKeys = permissionTreeRef.value.getCheckedKeys()
    const halfCheckedKeys = permissionTreeRef.value.getHalfCheckedKeys()
    const allCheckedKeys = [...checkedKeys, ...halfCheckedKeys]

    // 构建角色权限关联数据
    const rolePermissions = allCheckedKeys.map(permissionId => ({
      roleId: currentRoleId.value,
      permissionId: permissionId
    }))

    const response = await request.post('/sys/rolePermission/batch', rolePermissions)
    
    if (response.code === 200) {
      ElMessage.success('分配权限成功')
      rolePermissionDialogVisible.value = false
      // 重新加载权限数据，确保权限分配表及时更新
      await loadRolePermissions()
    } else {
      ElMessage.error(response.msg || '分配权限失败')
    }
  } catch (error) {
    ElMessage.error('分配权限失败')
    console.error('Assign role permissions error:', error)
  } finally {
    rolePermissionSubmitLoading.value = false
  }
}

const handleRoleSizeChange = (size: number) => {
  rolePagination.size = size
  loadRoleData()
}

const handleRoleCurrentChange = (current: number) => {
  rolePagination.current = current
  loadRoleData()
}

const handlePermissionSizeChange = (size: number) => {
  permissionPagination.size = size
  loadPermissionData()
}

const handlePermissionCurrentChange = (current: number) => {
  permissionPagination.current = current
  loadPermissionData()
}

const handleRoleDialogClose = () => {
  roleFormRef.value?.resetFields()
  resetRoleForm()
}

const handlePermissionDialogClose = () => {
  permissionFormRef.value?.resetFields()
  resetPermissionForm()
}

const resetRoleForm = () => {
  roleForm.id = 0
  roleForm.roleName = ''
  roleForm.roleKey = ''
  roleForm.status = 1
}

const resetPermissionForm = () => {
  permissionForm.id = 0
  permissionForm.permName = ''
  permissionForm.permCode = ''
  permissionForm.parentId = null
  permissionForm.permType = 1
  permissionForm.status = 1
}

// 生命周期
onMounted(() => {
  loadRoleData()
  loadPermissionData()
})
</script>

<style scoped>
.role-permission-manager {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.permission-tree-container {
  padding: 20px;
  background: #f5f7fa;
  border-radius: 5px;
  max-height: 400px;
  overflow-y: auto;
}

@media (max-width: 1200px) {
  .role-permission-manager .el-col {
    margin-bottom: 20px;
  }
}
</style>