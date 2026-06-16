import request from '@/utils/request'

export function listDoctor(query) {
  return request({
    url: '/hisdoctor/doctor/list',
    method: 'get',
    params: query
  })
}

export function getDoctor(id) {
  return request({
    url: '/hisdoctor/doctor/' + id,
    method: 'get'
  })
}
