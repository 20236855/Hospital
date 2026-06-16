import request from '@/utils/request'

// 查询医生信息列表
export function listDoctor(query) {
  return request({
    url: '/hisdoctor/doctor/list',
    method: 'get',
    params: query
  })
}

// 查询可创建医生档案的系统用户
export function listAvailableDoctorUsers() {
  return request({
    url: '/hisdoctor/doctor/availableUsers',
    method: 'get'
  })
}

// 查询医生信息详细
export function getDoctor(doctorId) {
  return request({
    url: '/hisdoctor/doctor/' + doctorId,
    method: 'get'
  })
}

// 根据用户ID查询医生信息
export function getDoctorByUserId(userId) {
  return request({
    url: '/hisdoctor/doctor/userId/' + userId,
    method: 'get'
  })
}

// 新增医生信息
export function addDoctor(data) {
  return request({
    url: '/hisdoctor/doctor',
    method: 'post',
    data: data
  })
}

// 修改医生信息
export function updateDoctor(data) {
  return request({
    url: '/hisdoctor/doctor',
    method: 'put',
    data: data
  })
}

// 删除医生信息
export function delDoctor(doctorId) {
  return request({
    url: '/hisdoctor/doctor/' + doctorId,
    method: 'delete'
  })
}

// 医生自助完善信息
export function completeDoctor(data) {
  return request({
    url: '/hisdoctor/doctor/complete',
    method: 'post',
    data: data
  })
}

// 上传医生头像
export function upload(data) {
  return request({
    url: '/file/upload',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
