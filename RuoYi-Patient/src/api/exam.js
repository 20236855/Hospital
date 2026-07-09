import request from '@/utils/request'

export function getMyExamReports() {
  return request({
    url: '/hisexam/result/patient/list',
    method: 'get'
  })
}

export function getMyExamPayments(config = {}) {
  return request({
    url: '/hisexam/apply/patient/payments',
    method: 'get',
    ...config
  })
}
