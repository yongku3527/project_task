import { http } from '@/utils/request'

/**
 * 获取物料类型列表
 * @param {Object} params 查询参数
 * @returns {Promise} 返回物料类型列表
 */
export function getItemTypeList(params) {
  return http.get('/item-type/list', params)
}

/**
 * 根据ID获取物料类型详情
 * @param {Number} id 物料类型ID
 * @returns {Promise} 返回物料类型详情
 */
export function getItemTypeById(id) {
  return http.get(`/item-type/${id}`)
}

/**
 * 新增物料类型
 * @param {Object} data 物料类型数据
 * @returns {Promise} 返回新增结果
 */
export function createItemType(data) {
  return http.post('/item-type', data)
}

/**
 * 更新物料类型
 * @param {Object} data 物料类型数据
 * @returns {Promise} 返回更新结果
 */
export function updateItemType(data) {
  return http.put('/item-type', data)
}

/**
 * 删除物料类型
 * @param {Number} id 物料类型ID
 * @returns {Promise} 返回删除结果
 */
export function deleteItemType(id) {
  return http.delete(`/item-type/${id}`)
}

/**
 * 根据成品编号前缀获取物料类型
 * @param {String} pid 成品编号
 * @returns {Promise} 返回物料类型
 */
export function getItemTypeByPid(pid) {
  return http.get('/item-type/get-type-by-pid', { pid })
}