import request from '@/utils/request'

// 查询患者列表
export function listPatient(query) {
  return request({
    url: '/patient/patient/list',
    method: 'get',
    params: query
  })
}

// 查询患者详细
export function getPatient(patientId) {
  return request({
    url: '/patient/patient/' + patientId,
    method: 'get'
  })
}

// 根据身份证号查询患者
export function getPatientByIdCard(idCard) {
  return request({
    url: '/patient/patient/idCard/' + idCard,
    method: 'get'
  })
}

// 根据用户ID查询患者
export function getPatientByUserId(userId) {
  return request({
    url: '/patient/patient/userId/' + userId,
    method: 'get'
  })
}

// 新增患者
export function addPatient(data) {
  return request({
    url: '/patient/patient',
    method: 'post',
    data: data
  })
}

// 修改患者
export function updatePatient(data) {
  return request({
    url: '/patient/patient',
    method: 'put',
    data: data
  })
}

// 删除患者
export function delPatient(patientId) {
  return request({
    url: '/patient/patient/' + patientId,
    method: 'delete'
  })
}

// 患者自助完善信息
export function completePatient(data) {
  return request({
    url: '/patient/patient/complete',
    method: 'post',
    data: data
  })
}
