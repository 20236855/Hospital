import request from '@/utils/request'

// 查询结算类别列表
export function listCategory(query) {
  return request({
    url: '/payment/category/list',
    method: 'get',
    params: query
  })
}

// 查询结算类别详细
export function getCategory(categoryId) {
  return request({
    url: '/payment/category/' + categoryId,
    method: 'get'
  })
}

// 新增结算类别
export function addCategory(data) {
  return request({
    url: '/payment/category',
    method: 'post',
    data: data
  })
}

// 修改结算类别
export function updateCategory(data) {
  return request({
    url: '/payment/category',
    method: 'put',
    data: data
  })
}

// 删除结算类别
export function delCategory(categoryId) {
  return request({
    url: '/payment/category/' + categoryId,
    method: 'delete'
  })
}
