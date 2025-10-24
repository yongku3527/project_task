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
  private interceptResponse(response: any) {
    // 如果是登录接口，直接返回数据
    if (response.url && response.url.includes('/auth/login')) {
      return response.json()
    }
    
    // 检查响应状态
    if (response.ok) {
      return response.json()
    } else {
      // 处理错误响应
      if (response.status === 401) {
        // 未授权，跳转到登录页
        localStorage.removeItem('token')
        localStorage.removeItem('username')
        window.location.href = '/login'
        ElMessage.error('登录已过期，请重新登录')
      } else if (response.status === 403) {
        ElMessage.error('没有权限访问')
      } else {
        ElMessage.error('请求失败，请稍后重试')
      }
      throw new Error(`HTTP error! status: ${response.status}`)
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
      const queryString = new URLSearchParams(params).toString()
      url += `?${queryString}`
    }

    const requestUrl = this.baseConfig.baseURL + url
    
    const finalConfig = await this.interceptRequest(config)
    const response = await fetch(requestUrl, finalConfig)
    return this.interceptResponse(response)
  }

  // POST请求
  async post(url: string, data?: any) {
    const config = {
      method: 'POST',
      headers: this.baseConfig.headers,
      credentials: this.baseConfig.credentials,
      body: data ? JSON.stringify(data) : undefined
    }

    const requestUrl = this.baseConfig.baseURL + url
    
    const finalConfig = await this.interceptRequest(config)
    const response = await fetch(requestUrl, finalConfig)
    return this.interceptResponse(response)
  }

  // PUT请求
  async put(url: string, data?: any) {
    const config = {
      method: 'PUT',
      headers: this.baseConfig.headers,
      credentials: this.baseConfig.credentials,
      body: data ? JSON.stringify(data) : undefined
    }

    const requestUrl = this.baseConfig.baseURL + url
    
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