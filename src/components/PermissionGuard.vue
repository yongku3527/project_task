<template>
  <slot v-if="hasAccess"></slot>
</template>

<script setup>
import { computed } from 'vue'
import PermissionManager from '../utils/permission'

const props = defineProps({
  // 权限标识，可以是单个权限字符串或权限数组
  permission: {
    type: [String, Array],
    default: null
  },
  // 角色标识，可以是单个角色字符串或角色数组
  role: {
    type: [String, Array],
    default: null
  },
  // 权限检查模式：'any' 表示满足任一权限即可，'all' 表示需要满足所有权限
  mode: {
    type: String,
    default: 'any',
    validator: (value) => ['any', 'all'].includes(value)
  },
  // 角色检查模式：'any' 表示满足任一角色即可，'all' 表示需要满足所有角色
  roleMode: {
    type: String,
    default: 'any',
    validator: (value) => ['any', 'all'].includes(value)
  }
})

// 计算是否有访问权限
const hasAccess = computed(() => {
  // 如果同时指定了权限和角色，需要同时满足
  const hasPermissionAccess = checkPermissionAccess()
  const hasRoleAccess = checkRoleAccess()
  
  // 如果同时指定了权限和角色，需要同时满足
  if (props.permission !== null && props.role !== null) {
    return hasPermissionAccess && hasRoleAccess
  }
  
  // 如果只指定了权限或角色，只需满足其中一个
  return hasPermissionAccess || hasRoleAccess
})

/**
 * 检查权限访问
 * @returns {boolean} 是否有权限访问
 */
function checkPermissionAccess() {
  if (props.permission === null) {
    return true // 没有指定权限，默认允许访问
  }
  
  if (Array.isArray(props.permission)) {
    return props.mode === 'any'
      ? PermissionManager.hasAnyPermission(props.permission)
      : PermissionManager.hasAllPermissions(props.permission)
  } else {
    return PermissionManager.hasPermission(props.permission)
  }
}

/**
 * 检查角色访问
 * @returns {boolean} 是否有角色访问
 */
function checkRoleAccess() {
  if (props.role === null) {
    return true // 没有指定角色，默认允许访问
  }
  
  if (Array.isArray(props.role)) {
    return props.roleMode === 'any'
      ? PermissionManager.hasAnyRole(props.role)
      : PermissionManager.hasAllRoles(props.role)
  } else {
    return PermissionManager.hasRole(props.role)
  }
}
</script>