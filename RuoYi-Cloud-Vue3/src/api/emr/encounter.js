import request from '@/utils/request'

// 查询接诊列表
export function listEncounter(query) {
  return request({
    url: '/emr/encounter/list',
    method: 'get',
    params: query
  })
}

// 查询接诊详细
export function getEncounter(encounterId) {
  return request({
    url: '/emr/encounter/' + encounterId,
    method: 'get'
  })
}

// 新增接诊
export function addEncounter(data) {
  return request({
    url: '/emr/encounter',
    method: 'post',
    data: data
  })
}

// 修改接诊
export function updateEncounter(data) {
  return request({
    url: '/emr/encounter',
    method: 'put',
    data: data
  })
}

// 删除接诊
export function delEncounter(encounterId) {
  return request({
    url: '/emr/encounter/' + encounterId,
    method: 'delete'
  })
}
