import request from '@/utils/request'

// Result apply options.
export function listResultApplyOptions() {
  return request({
    url: '/hisexam/result/applyOptions',
    method: 'get'
  })
}

// Result list.
export function listResult(query) {
  return request({
    url: '/hisexam/result/list',
    method: 'get',
    params: query
  })
}

// Result detail.
export function getResult(resultId) {
  return request({
    url: '/hisexam/result/' + resultId,
    method: 'get'
  })
}

// Add result.
export function addResult(data) {
  return request({
    url: '/hisexam/result',
    method: 'post',
    data: data
  })
}

// Save report results by apply id.
export function saveResultReport(data) {
  return request({
    url: '/hisexam/result/report',
    method: 'post',
    data: data
  })
}

// Update result.
export function updateResult(data) {
  return request({
    url: '/hisexam/result',
    method: 'put',
    data: data
  })
}

// Delete result.
export function delResult(resultId) {
  return request({
    url: '/hisexam/result/' + resultId,
    method: 'delete'
  })
}
