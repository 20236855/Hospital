import request from '@/utils/request'

// 查询医生排班列表
export function listSchedule(query) {
  return request({
    url: '/hisdoctor/schedule/list',
    method: 'get',
    params: query
  })
}

// 查询医生排班详细
export function getSchedule(scheduleId) {
  return request({
    url: '/hisdoctor/schedule/' + scheduleId,
    method: 'get'
  })
}

// 新增医生排班
export function addSchedule(data) {
  return request({
    url: '/hisdoctor/schedule',
    method: 'post',
    data: data
  })
}

// 修改医生排班
export function updateSchedule(data) {
  return request({
    url: '/hisdoctor/schedule',
    method: 'put',
    data: data
  })
}

// 删除医生排班
export function delSchedule(scheduleId) {
  return request({
    url: '/hisdoctor/schedule/' + scheduleId,
    method: 'delete'
  })
}
