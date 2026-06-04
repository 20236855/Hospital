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
