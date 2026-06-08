import request from '@/utils/request'

export function getRegisterList(query) {
  return request({
    url: '/register/register/list',
    method: 'get',
    params: query
  })
}

export function addRegister(data) {
  return request({
    url: '/register/register',
    method: 'post',
    data
  })
}

export function createRegister(data) {
  return addRegister(data)
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

export function getDeptList(query) {
  return request({
    url: '/system/dept/register/list',
    method: 'get',
    params: query
  })
}
