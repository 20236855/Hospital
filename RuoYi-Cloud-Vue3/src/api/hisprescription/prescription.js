import request from '@/utils/request'

// 查询处方主列表
export function listPrescription(query) {
  return request({
    url: '/hisprescription/prescription/list',
    method: 'get',
    params: query
  })
}

// 查询处方主详细
export function getPrescription(prescriptionId) {
  return request({
    url: '/hisprescription/prescription/' + prescriptionId,
    method: 'get'
  })
}

// 新增处方主
export function addPrescription(data) {
  return request({
    url: '/hisprescription/prescription',
    method: 'post',
    data: data
  })
}

// 修改处方主
export function updatePrescription(data) {
  return request({
    url: '/hisprescription/prescription',
    method: 'put',
    data: data
  })
}

// 删除处方主
export function delPrescription(prescriptionId) {
  return request({
    url: '/hisprescription/prescription/' + prescriptionId,
    method: 'delete'
  })
}
