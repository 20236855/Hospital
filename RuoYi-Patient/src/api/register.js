import request from '@/utils/request'

export function getRegisterList(query, config = {}) {
  return request({
    url: '/register/register/list',
    method: 'get',
    params: query,
    ...config
  })
}

export function addRegister(data) {
  return request({
    url: '/register/register',
    method: 'post',
    data,
    timeout: 60000
  })
}

export function createRegister(data) {
  return addRegister(data)
}

export function cancelPatientRegister(registerId) {
  return request({
    url: '/register/register/patient/cancel/' + registerId,
    method: 'put'
  })
}

export function getDoctorList(query) {
  return request({
    url: '/hisdoctor/doctor/open/list',
    method: 'get',
    params: query
  })
}

export function getScheduleList(query) {
  return request({
    url: '/hisdoctor/schedule/open/list',
    method: 'get',
    params: query
  })
}

export function getOpenDoctorSlots(query) {
  return request({
    url: '/hisdoctor/schedule/slot/open/doctor-list',
    method: 'get',
    params: query
  })
}

export function getOpenScheduleSlots(query) {
  return request({
    url: '/hisdoctor/schedule/slot/open/list',
    method: 'get',
    params: query
  })
}

export function getDeptList(query, config = {}) {
  return request({
    url: '/system/dept/register/list',
    method: 'get',
    params: query,
    ...config
  })
}
