import PermissionManager from '../utils/permission'

/**
 * 权限控制指令
 * 用法：
 * v-permission="'user:add'" - 检查单个权限
 * v-permission="['user:add', 'user:edit']" - 检查多个权限（需要全部满足）
 * v-permission.any="['user:add', 'user:edit']" - 检查多个权限（满足任一即可）
 * v-role="'admin'" - 检查单个角色
 * v-role="['admin', 'manager']" - 检查多个角色（需要全部满足）
 * v-role.any="['admin', 'manager']" - 检查多个角色（满足任一即可）
 */
const permission = {
  mounted(el, binding) {
    checkPermission(el, binding)
  },
  updated(el, binding) {
    checkPermission(el, binding)
  }
}

/**
 * 检查权限并控制元素显示
 * @param {Element} el DOM元素
 * @param {Object} binding 指令绑定对象
 */
function checkPermission(el, binding) {
  const { value, modifiers } = binding
  const hasPermission = checkUserPermission(value, modifiers)
  
  if (!hasPermission) {
    // 没有权限，隐藏元素
    el.style.display = 'none'
    // 或者完全移除元素
    // el.parentNode && el.parentNode.removeChild(el)
  } else {
    // 有权限，显示元素
    el.style.display = ''
  }
}

/**
 * 检查用户权限
 * @param {string|Array} value 权限值或权限数组
 * @param {Object} modifiers 修饰符对象
 * @returns {boolean} 是否有权限
 */
function checkUserPermission(value, modifiers) {
  if (!value) {
    return false
  }

  // 检查角色权限
  if (modifiers.role) {
    if (Array.isArray(value)) {
      return modifiers.any 
        ? PermissionManager.hasAnyRole(value)
        : PermissionManager.hasAllRoles(value)
    } else {
      return PermissionManager.hasRole(value)
    }
  }

  // 检查普通权限
  if (Array.isArray(value)) {
    return modifiers.any 
      ? PermissionManager.hasAnyPermission(value)
      : PermissionManager.hasAllPermissions(value)
  } else {
    return PermissionManager.hasPermission(value)
  }
}

export default permission