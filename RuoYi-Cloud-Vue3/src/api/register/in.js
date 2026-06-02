import request from '@/utils/request'

// 查询签到列表
export function listIn(query) {
  return request({
    url: '/register/in/list',
    method: 'get',
    params: query
  })
}

// 查询签到详细
export function getIn(checkInId) {
  return request({
    url: '/register/in/' + checkInId,
    method: 'get'
  })
}

// 新增签到
export function addIn(data) {
  return request({
    url: '/register/in',
    method: 'post',
    data: data
  })
}

// 修改签到
export function updateIn(data) {
  return request({
    url: '/register/in',
    method: 'put',
    data: data
  })
}

// 删除签到
export function delIn(checkInId) {
  return request({
    url: '/register/in/' + checkInId,
    method: 'delete'
  })
}
