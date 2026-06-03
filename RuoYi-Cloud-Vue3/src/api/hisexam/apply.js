import request from '@/utils/request'

// 查询检查/检验/处置申请单列表
export function listApply(query) {
  return request({
    url: '/hisexam/apply/list',
    method: 'get',
    params: query
  })
}

// 查询检查/检验/处置申请单详细
export function getApply(applyId) {
  return request({
    url: '/hisexam/apply/' + applyId,
    method: 'get'
  })
}

// 新增检查/检验/处置申请单
export function addApply(data) {
  return request({
    url: '/hisexam/apply',
    method: 'post',
    data: data
  })
}

// 修改检查/检验/处置申请单
export function updateApply(data) {
  return request({
    url: '/hisexam/apply',
    method: 'put',
    data: data
  })
}

// 删除检查/检验/处置申请单
export function delApply(applyId) {
  return request({
    url: '/hisexam/apply/' + applyId,
    method: 'delete'
  })
}
