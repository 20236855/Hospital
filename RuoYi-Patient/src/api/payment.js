import request from '@/utils/request'

export function payRegister(registerId, payType = '微信') {
  return request({
    url: '/payment/payment/register/' + registerId + '/pay',
    method: 'post',
    data: { payType }
  })
}

export function payExamByRegister(registerId, payType = '微信') {
  return request({
    url: '/payment/payment/exam/register/' + registerId + '/pay',
    method: 'post',
    data: { payType }
  })
}

export function payPrescription(prescriptionId, payType = '微信') {
  return request({
    url: '/payment/payment/prescription/' + prescriptionId + '/pay',
    method: 'post',
    data: { payType }
  })
}
