import request from '@/utils/request'

// 查询挂号级别列表
export function listDoctorlevel(query) {
  return request({
    url: '/hisdoctor/doctorlevel/list',
    method: 'get',
    params: query
  })
}

// 查询挂号级别详细
export function getDoctorlevel(levelId) {
  return request({
    url: '/hisdoctor/doctorlevel/' + levelId,
    method: 'get'
  })
}

// 新增挂号级别
export function addDoctorlevel(data) {
  return request({
    url: '/hisdoctor/doctorlevel',
    method: 'post',
    data: data
  })
}

// 修改挂号级别
export function updateDoctorlevel(data) {
  return request({
    url: '/hisdoctor/doctorlevel',
    method: 'put',
    data: data
  })
}

// 删除挂号级别
export function delDoctorlevel(levelId) {
  return request({
    url: '/hisdoctor/doctorlevel/' + levelId,
    method: 'delete'
  })
}
