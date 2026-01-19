import { ElMessage } from 'element-plus'

// 创建axios实例（这里使用原生fetch实现）
class Request {
  // 基础配置
  private baseConfig = {
    baseURL: '/api',
    timeout: 10000,
    credentials: 'include', // 允许发送cookie
    headers: {
      'Content-Type': 'application/json'
    }
  }

  // 请求拦截器
  private async interceptRequest(config: any) {
    // 添加token到请求头
    const token = localStorage.getItem('token')
    if (token) {
      config.headers = {
        ...config.headers,
        'satoken': token
      }
    }
    return config
  }

  // 响应拦截器
  private async interceptResponse(response: any) {
    // 如果是登录接口，直接返回数据
    if (response.url && response.url.includes('/auth/login')) {
      return response.json()
    }
    
    // 检查响应状态
    if (response.ok) {
      const data = await response.json()
      // 检查响应体中的 code 字段（后端定义的业务状态码）
      if (data.code === 401) {
        // 未登录或 Token 过期
        this.handleTokenInvalid(data.msg || '登录已过期，请重新登录')
      }
      return data
    } else {
      // 处理错误响应
      if (response.status === 401) {
        // 未授权，Token 过期或无效
        this.handleTokenInvalid('登录已过期，请重新登录')
      } else if (response.status === 403) {
        // 无权访问，Token 无效或权限不足
        this.handleTokenInvalid('登录状态无效，请重新登录')
      } else if (response.status === 419) {
        // Token 过期
        this.handleTokenInvalid('登录已过期，请重新登录')
      } else if (response.status === 500) {
        // 服务端错误
        ElMessage.error('服务端错误，请稍后重试')
      } else {
        ElMessage.error('请求失败，请稍后重试')
      }
      throw new Error(`HTTP error! status: ${response.status}`)
    }
  }

  // 处理 Token 无效的情况
  private handleTokenInvalid(message: string) {
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('userInfo')
    
    // 清除 Cookie 中的 token
    document.cookie = 'satoken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;'
    
    // 提示用户
    ElMessage.error(message)
    
    // 跳转到登录页（如果当前不在登录页）
    if (!window.location.pathname.includes('/login')) {
      window.location.href = '/login'
    }
  }

  // GET请求
  async get(url: string, params?: any) {
    const config = {
      method: 'GET',
      headers: this.baseConfig.headers,
      credentials: this.baseConfig.credentials
    }

    if (params) {
      // 修复参数处理，确保正确处理对象参数
      const searchParams = new URLSearchParams()
      Object.keys(params).forEach(key => {
        if (params[key] !== null && params[key] !== undefined && params[key] !== '') {
          searchParams.append(key, params[key])
        }
      })
      const queryString = searchParams.toString()
      if (queryString) {
        url += `?${queryString}`
      }
    }

    const requestUrl = this.baseConfig.baseURL + url
    
    const finalConfig = await this.interceptRequest(config)
    const response = await fetch(requestUrl, finalConfig)
    return this.interceptResponse(response)
  }

  // POST请求
  async post(url: string, data?: any, params?: any) {
    const config = {
      method: 'POST',
      headers: this.baseConfig.headers,
      credentials: this.baseConfig.credentials,
      body: data ? JSON.stringify(data) : undefined
    }

    let requestUrl = this.baseConfig.baseURL + url
    
    // 处理查询参数
    if (params) {
      const queryString = new URLSearchParams(params).toString()
      requestUrl += `?${queryString}`
    }
    
    const finalConfig = await this.interceptRequest(config)
    const response = await fetch(requestUrl, finalConfig)
    return this.interceptResponse(response)
  }

  // PUT请求
  async put(url: string, data?: any, params?: any) {
    const config = {
      method: 'PUT',
      headers: this.baseConfig.headers,
      credentials: this.baseConfig.credentials,
      body: data ? JSON.stringify(data) : undefined
    }

    let requestUrl = this.baseConfig.baseURL + url
    
    // 处理查询参数
    if (params) {
      const queryString = new URLSearchParams(params).toString()
      requestUrl += `?${queryString}`
    }
    
    const finalConfig = await this.interceptRequest(config)
    const response = await fetch(requestUrl, finalConfig)
    return this.interceptResponse(response)
  }

  // DELETE请求
  async delete(url: string) {
    const config = {
      method: 'DELETE',
      headers: this.baseConfig.headers,
      credentials: this.baseConfig.credentials
    }

    const requestUrl = this.baseConfig.baseURL + url
    
    const finalConfig = await this.interceptRequest(config)
    const response = await fetch(requestUrl, finalConfig)
    return this.interceptResponse(response)
  }
}

// 创建请求实例
const request = new Request()

// 导出请求方法
export const http = {
  get: request.get.bind(request),
  post: request.post.bind(request),
  put: request.put.bind(request),
  delete: request.delete.bind(request)
}

export default request