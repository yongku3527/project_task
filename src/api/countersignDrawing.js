import { http } from '@/utils/request'

// 获取会签图纸列表
export function getCountersignDrawingList(params) {
  return http.get('/countersign-drawing/list', params)
}

// 获取会签图纸详情
export function getCountersignDrawingById(id) {
  return http.get(`/countersign-drawing/info/${id}`)
}

// 根据零部件号获取会签图纸列表
export function getCountersignDrawingsByPartNo(partNo) {
  return http.get(`/countersign-drawing/list-by-partNo/${partNo}`)
}

// 获取所有会签图纸及其文件信息
export function getAllCountersignDrawingsWithFiles() {
  return http.get('/countersign-drawing/list-with-files')
}

// 新增会签图纸
export function addCountersignDrawing(data) {
  return http.post('/countersign-drawing/save', data)
}

// 更新会签图纸
export function updateCountersignDrawing(data) {
  return http.put('/countersign-drawing/update', data)
}

// 删除会签图纸
export function deleteCountersignDrawing(id) {
  return http.delete(`/countersign-drawing/delete/${id}`)
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