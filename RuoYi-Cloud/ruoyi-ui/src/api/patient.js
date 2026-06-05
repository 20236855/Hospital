import request from '@/utils/request'

// 完善患者信息
export function completePatient(data) {
  return request({
    url: '/patient/complete',
    method: 'post',
    data: data
  })
}
