import request from '@/utils/request'

// 完善医生信息
export function completeDoctor(data) {
  return request({
    url: '/doctor/complete',
    method: 'post',
    data: data
  })
}
