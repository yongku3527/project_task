import { http } from '@/utils/request'

/**
 * 获取经验库列表
 * @param {Object} params 查询参数
 * @returns {Promise} 返回经验库列表
 */
export function getKnowledgeList(params) {
  return http.get('/knowledge-info/list', params)
}

/**
 * 根据ID获取经验库详情
 * @param {Number} id 经验库ID
 * @returns {Promise} 返回经验库详情
 */
export function getKnowledgeById(id) {
  return http.get(`/knowledge-info/${id}`)
}

/**
 * 新增经验库
 * @param {Object} data 经验库数据
 * @returns {Promise} 返回新增结果
 */
export function createKnowledge(data) {
  return http.post('/knowledge-info', data)
}

/**
 * 更新经验库
 * @param {Object} data 经验库数据
 * @returns {Promise} 返回更新结果
 */
export function updateKnowledge(data) {
  return http.put('/knowledge-info', data)
}

/**
 * 删除经验库
 * @param {Number} id 经验库ID
 * @returns {Promise} 返回删除结果
 */
export function deleteKnowledge(id) {
  return http.delete(`/knowledge-info/${id}`)
}

/**
 * 获取完整经验库列表（包含文件信息和完成状态）
 * @returns {Promise} 返回完整经验库列表
 */
export function getCompleteKnowledgeList() {
  return http.get('/knowledge-info/complete-list')
}

/**
 * 根据ID获取完整经验库详情（包含文件信息和完成状态）
 * @param {Number} id 经验库ID
 * @returns {Promise} 返回完整经验库详情
 */
export function getCompleteKnowledgeById(id) {
  return http.get(`/knowledge-info/complete-info/${id}`)
}

/**
 * 根据完成状态获取经验库列表
 * @param {Number} completionStatus 完成状态（0待完成、1已学习、2已掌握）
 * @param {Object} params 查询参数
 * @returns {Promise} 返回经验库列表
 */
export function getKnowledgeListByStatus(completionStatus, params) {
  return http.get(`/knowledge-info/complete-list-by-status/${completionStatus}`, params)
}