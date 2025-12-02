import { http } from '@/utils/request'

// 获取规格书列表
export function getSpecificationList(params) {
  return http.get('/specification/list', params)
}

// 获取规格书详情
export function getSpecificationById(id) {
  return http.get(`/specification/info/${id}`)
}

// 根据原材料ID获取规格书列表
export function getSpecificationsByMaterialId(materialId) {
  return http.get(`/specification/list-by-materialId/${materialId}`)
}

// 获取所有规格书及其文件信息
export function getAllSpecificationsWithFiles() {
  return http.get('/specification/list-with-files')
}

// 新增规格书
export function addSpecification(data) {
  return http.post('/specification/save', data)
}

// 更新规格书
export function updateSpecification(data) {
  return http.put('/specification/update', data)
}

// 删除规格书
export function deleteSpecification(id) {
  return http.delete(`/specification/delete/${id}`)
}

// 下载文件
export function downloadFile(url, fileName) {
  // 创建一个隐藏的a标签来下载文件
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}