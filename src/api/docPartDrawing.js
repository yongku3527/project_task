import { http } from '@/utils/request'

// 获取零件图纸列表
export function getDocPartDrawingList(params) {
  return http.get('/doc-part-drawing/list', params)
}

// 获取零件图纸详情
export function getDocPartDrawingById(id) {
  return http.get(`/doc-part-drawing/info/${id}`)
}

// 根据零件编号获取图纸列表
export function getDocPartDrawingsByPartId(partId) {
  return http.get(`/doc-part-drawing/list-by-partId/${partId}`)
}

// 获取所有零件图纸及其文件信息
export function getAllDocPartDrawingsWithFiles() {
  return http.get('/doc-part-drawing/list-with-files')
}

// 新增零件图纸
export function addDocPartDrawing(data) {
  return http.post('/doc-part-drawing/save', data)
}

// 更新零件图纸
export function updateDocPartDrawing(data) {
  return http.put('/doc-part-drawing/update', data)
}

// 删除零件图纸
export function deleteDocPartDrawing(id) {
  return http.delete(`/doc-part-drawing/delete/${id}`)
}

// 根据零件编号前N位查询物料类型
export function getItemTypeByPartId(partId, prefixLength = 3) {
  return http.get('/item-type/get-type-by-pid', { pid: partId, prefixLength })
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