import request from '@/utils/request'

// 查询处方明细列表
export function listItem(query) {
  return request({
    url: '/hisprescription/item/list',
    method: 'get',
    params: query
  })
}

// 查询处方明细详细
export function getItem(itemId) {
  return request({
    url: '/hisprescription/item/' + itemId,
    method: 'get'
  })
}

// 新增处方明细
export function addItem(data) {
  return request({
    url: '/hisprescription/item',
    method: 'post',
    data: data
  })
}

// 修改处方明细
export function updateItem(data) {
  return request({
    url: '/hisprescription/item',
    method: 'put',
    data: data
  })
}

// 删除处方明细
export function delItem(itemId) {
  return request({
    url: '/hisprescription/item/' + itemId,
    method: 'delete'
  })
}
