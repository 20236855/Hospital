import request from '@/utils/request'

// 查询挂号列表
export function listRegister(query) {
  return request({
    url: '/register/register/list',
    method: 'get',
    params: query
  })
}

// 查询挂号详细
export function getRegister(registerId) {
  return request({
    url: '/register/register/' + registerId,
    method: 'get'
  })
}

// 查询全部挂号级别
export function listRegisterLevel() {
  return request({
    url: '/register/register/level/list',
    method: 'get'
  })
}

// 查询全部科室
export function listRegisterDept() {
  return request({
    url: '/register/register/dept/list',
    method: 'get'
  })
}

// 根据科室ID查询医生
export function listRegisterDoctor(deptId) {
  return request({
    url: '/register/register/doctor/list/' + deptId,
    method: 'get'
  })
}

// 根据医生ID获取挂号费
export function getRegisterFee(doctorId) {
  return request({
    url: '/register/register/getFee',
    method: 'get',
    params: { doctorId }
  })
}

// 新增挂号
export function addRegister(data) {
  return request({
    url: '/register/register',
    method: 'post',
    data: data
  })
}

// 修改挂号
export function updateRegister(data) {
  return request({
    url: '/register/register',
    method: 'put',
    data: data
  })
}

// 删除挂号
export function delRegister(registerId) {
  return request({
    url: '/register/register/' + registerId,
    method: 'delete'
  })
}

// 退号
export function cancelRegister(registerId) {
  return request({
    url: '/register/register/cancel/' + registerId,
    method: 'put'
  })
}
