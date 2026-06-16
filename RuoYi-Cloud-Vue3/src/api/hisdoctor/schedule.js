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

// 查询当前周智能排班
export function currentWeekSchedule() {
  return request({
    url: '/hisdoctor/schedule/agent/current-week',
    method: 'get'
  })
}

// 预览下周智能排班
export function previewNextWeekSchedule() {
  return request({
    url: '/hisdoctor/schedule/agent/next-week-preview',
    method: 'get'
  })
}

// 生成下周智能排班并入库
export function generateNextWeekSchedule() {
  return request({
    url: '/hisdoctor/schedule/agent/generate-next-week',
    method: 'post'
  })
}

// 同步公示排班
export function publishWeekSchedule(weekStart) {
  return request({
    url: '/hisdoctor/schedule/agent/publish/' + weekStart,
    method: 'post'
  })
}

// 查询智能体最新运行状态
export function getAgentScheduleStatus() {
  return request({
    url: '/hisdoctor/schedule/agent/status',
    method: 'get'
  })
}

// 查询智能体运行历史
export function listAgentScheduleRuns() {
  return request({
    url: '/hisdoctor/schedule/agent/runs',
    method: 'get'
  })
}
