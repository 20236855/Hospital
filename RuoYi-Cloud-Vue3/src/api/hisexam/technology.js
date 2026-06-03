import request from '@/utils/request'

// 查询医技项目列表
export function listTechnology(query) {
  return request({
    url: '/hisexam/technology/list',
    method: 'get',
    params: query
  })
}

// 查询医技项目详细
export function getTechnology(id) {
  return request({
    url: '/hisexam/technology/' + id,
    method: 'get'
  })
}

// 新增医技项目
export function addTechnology(data) {
  return request({
    url: '/hisexam/technology',
    method: 'post',
    data: data
  })
}

// 修改医技项目
export function updateTechnology(data) {
  return request({
    url: '/hisexam/technology',
    method: 'put',
    data: data
  })
}

// 删除医技项目
export function delTechnology(id) {
  return request({
    url: '/hisexam/technology/' + id,
    method: 'delete'
  })
}
