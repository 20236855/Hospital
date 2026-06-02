import request from '@/utils/request'

// 查询电子病历列表
export function listRecord(query) {
  return request({
    url: '/emr/record/list',
    method: 'get',
    params: query
  })
}

// 查询电子病历详细
export function getRecord(recordId) {
  return request({
    url: '/emr/record/' + recordId,
    method: 'get'
  })
}

// 新增电子病历
export function addRecord(data) {
  return request({
    url: '/emr/record',
    method: 'post',
    data: data
  })
}

// 修改电子病历
export function updateRecord(data) {
  return request({
    url: '/emr/record',
    method: 'put',
    data: data
  })
}

// 删除电子病历
export function delRecord(recordId) {
  return request({
    url: '/emr/record/' + recordId,
    method: 'delete'
  })
}
