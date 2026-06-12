import request from '@/utils/request'

export function getPatientList(query) {
  return request({
    url: '/patient/patient/list',
    method: 'get',
    params: query
  })
}

export function getPatient(id) {
  return request({
    url: '/patient/patient/' + id,
    method: 'get'
  })
}

export function getPatientInfo() {
  return request({
    url: '/patient/patient/info',
    method: 'get'
  })
}

export function getPatientByUserId(userId) {
  return request({
    url: '/patient/patient/userId/' + userId,
    method: 'get'
  })
}

export function completePatient(data) {
  return request({
    url: '/patient/patient/complete',
    method: 'post',
    data
  })
}
