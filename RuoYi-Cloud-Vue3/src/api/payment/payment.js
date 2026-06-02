import request from '@/utils/request'

// 查询收费列表
export function listPayment(query) {
  return request({
    url: '/payment/payment/list',
    method: 'get',
    params: query
  })
}

// 查询收费详细
export function getPayment(paymentId) {
  return request({
    url: '/payment/payment/' + paymentId,
    method: 'get'
  })
}

// 新增收费
export function addPayment(data) {
  return request({
    url: '/payment/payment',
    method: 'post',
    data: data
  })
}

// 修改收费
export function updatePayment(data) {
  return request({
    url: '/payment/payment',
    method: 'put',
    data: data
  })
}

// 删除收费
export function delPayment(paymentId) {
  return request({
    url: '/payment/payment/' + paymentId,
    method: 'delete'
  })
}
