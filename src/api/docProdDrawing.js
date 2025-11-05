import { http } from '@/utils/request'

// 获取成品图纸列表
export function getDocProdDrawingList(params) {
  return http.get('/doc-prod-drawing/list', params)
}

// 获取成品图纸详情
export function getDocProdDrawingById(id) {
  return http.get(`/doc-prod-drawing/info/${id}`)
}

// 根据成品编号获取图纸列表
export function getDocProdDrawingsByPid(pid) {
  return http.get(`/doc-prod-drawing/list-by-pid/${pid}`)
}

// 获取所有成品图纸及其文件信息
export function getAllDocProdDrawingsWithFiles() {
  return http.get('/doc-prod-drawing/list-with-files')
}

// 新增成品图纸
export function addDocProdDrawing(data) {
  return http.post('/doc-prod-drawing/save', data)
}

// 更新成品图纸
export function updateDocProdDrawing(data) {
  return http.put('/doc-prod-drawing/update', data)
}

// 删除成品图纸
export function deleteDocProdDrawing(id) {
  return http.delete(`/doc-prod-drawing/delete/${id}`)
}

// 根据成品编号前三位查询物料类型
export function getItemTypeByPid(pid) {
  return http.get('/item-type/get-type-by-pid', { pid })
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