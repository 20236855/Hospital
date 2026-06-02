import request from '@/utils/request'

// 查询疾病字典列表
export function listDisease(query) {
  return request({
    url: '/emr/disease/list',
    method: 'get',
    params: query
  })
}

// 查询疾病字典详细
export function getDisease(diseaseId) {
  return request({
    url: '/emr/disease/' + diseaseId,
    method: 'get'
  })
}

// 新增疾病字典
export function addDisease(data) {
  return request({
    url: '/emr/disease',
    method: 'post',
    data: data
  })
}

// 修改疾病字典
export function updateDisease(data) {
  return request({
    url: '/emr/disease',
    method: 'put',
    data: data
  })
}

// 删除疾病字典
export function delDisease(diseaseId) {
  return request({
    url: '/emr/disease/' + diseaseId,
    method: 'delete'
  })
}
