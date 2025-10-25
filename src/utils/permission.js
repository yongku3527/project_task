import { ref } from 'vue'
import { http } from './request'

// 权限和角色存储键名
const PERMISSION_KEY = 'user_permissions'
const ROLE_KEY = 'user_roles'

// 权限和角色状态
const permissions = ref([])
const roles = ref([])

/**
 * 权限管理工具类
 */
class PermissionManager {
  /**
   * 获取用户权限列表
   * @returns {Promise<Array>} 权限列表
   */
  static async fetchPermissions() {
    try {
      const response = await http.get('/auth/permission')

      if (response.code === 200) {
        const permissionList = response.data || []
        this.setPermissions(permissionList)
        return permissionList
      }
      return []
    } catch (error) {
      console.error('获取权限列表失败:', error)
      return []
    }
  }

  /**
   * 获取用户角色列表
   * @returns {Promise<Array>} 角色列表
   */
  static async fetchRoles() {
    try {
      const response = await http.get('/auth/role')
      if (response.code === 200) {
        const roleList = response.data || []
        this.setRoles(roleList)
        return roleList
      }
      return []
    } catch (error) {
      console.error('获取角色列表失败:', error)
      return []
    }
  }

  /**
   * 同时获取权限和角色
   * @returns {Promise<{permissions: Array, roles: Array}>}
   */
  static async fetchUserAuth() {
    try {
      const [permissionList, roleList] = await Promise.all([
        this.fetchPermissions(),
        this.fetchRoles()
      ])
      return { permissions: permissionList, roles: roleList }
    } catch (error) {
      console.error('获取用户权限和角色失败:', error)
      return { permissions: [], roles: [] }
    }
  }

  /**
   * 设置权限列表
   * @param {Array} permissionList 权限列表
   */
  static setPermissions(permissionList) {
    permissions.value = permissionList
    localStorage.setItem(PERMISSION_KEY, JSON.stringify(permissionList))
  }

  /**
   * 设置角色列表
   * @param {Array} roleList 角色列表
   */
  static setRoles(roleList) {
    roles.value = roleList
    localStorage.setItem(ROLE_KEY, JSON.stringify(roleList))
  }

  /**
   * 从本地存储加载权限
   */
  static loadPermissionsFromStorage() {
    try {
      const storedPermissions = localStorage.getItem(PERMISSION_KEY)
      if (storedPermissions) {
        permissions.value = JSON.parse(storedPermissions)
      }
    } catch (error) {
      console.error('加载权限失败:', error)
      permissions.value = []
    }
  }

  /**
   * 从本地存储加载角色
   */
  static loadRolesFromStorage() {
    try {
      const storedRoles = localStorage.getItem(ROLE_KEY)
      if (storedRoles) {
        roles.value = JSON.parse(storedRoles)
      }
    } catch (error) {
      console.error('加载角色失败:', error)
      roles.value = []
    }
  }

  /**
   * 检查是否有指定权限
   * @param {string} permission 权限标识
   * @returns {boolean} 是否有权限
   */
  static hasPermission(permission) {
    // 如果有全能权限，直接返回true
    if (permissions.value.includes('*')) {
      return true
    }
    return permissions.value.includes(permission)
  }

  /**
   * 检查是否有任一权限
   * @param {Array} permissionList 权限标识数组
   * @returns {boolean} 是否有任一权限
   */
  static hasAnyPermission(permissionList) {
    if (!permissionList || permissionList.length === 0) {
      return false
    }
    
    // 如果有全能权限，直接返回true
    if (permissions.value.includes('*')) {
      return true
    }
    
    return permissionList.some(permission => permissions.value.includes(permission))
  }

  /**
   * 检查是否有所有权限
   * @param {Array} permissionList 权限标识数组
   * @returns {boolean} 是否有所有权限
   */
  static hasAllPermissions(permissionList) {
    if (!permissionList || permissionList.length === 0) {
      return false
    }
    
    // 如果有全能权限，直接返回true
    if (permissions.value.includes('*')) {
      return true
    }
    
    return permissionList.every(permission => permissions.value.includes(permission))
  }

  /**
   * 检查是否有指定角色
   * @param {string} role 角色标识
   * @returns {boolean} 是否有角色
   */
  static hasRole(role) {
    return roles.value.includes(role)
  }

  /**
   * 检查是否有任一角色
   * @param {Array} roleList 角色标识数组
   * @returns {boolean} 是否有任一角色
   */
  static hasAnyRole(roleList) {
    if (!roleList || roleList.length === 0) {
      return false
    }
    
    return roleList.some(role => roles.value.includes(role))
  }

  /**
   * 检查是否有所有角色
   * @param {Array} roleList 角色标识数组
   * @returns {boolean} 是否有所有角色
   */
  static hasAllRoles(roleList) {
    if (!roleList || roleList.length === 0) {
      return false
    }
    
    return roleList.every(role => roles.value.includes(role))
  }

  /**
   * 获取所有权限
   * @returns {Array} 权限列表
   */
  static getPermissions() {
    return [...permissions.value]
  }

  /**
   * 获取所有角色
   * @returns {Array} 角色列表
   */
  static getRoles() {
    return [...roles.value]
  }

  /**
   * 清除权限和角色
   */
  static clearAuth() {
    permissions.value = []
    roles.value = []
    localStorage.removeItem(PERMISSION_KEY)
    localStorage.removeItem(ROLE_KEY)
  }
}

// 初始化时从本地存储加载权限和角色
PermissionManager.loadPermissionsFromStorage()
PermissionManager.loadRolesFromStorage()

export default PermissionManager
export { permissions, roles }