import request from '@/utils/request'

export function payRegister(registerId, payType = '微信') {
  return request({
    url: '/payment/payment/register/' + registerId + '/pay',
    method: 'post',
    data: { payType }
  })
}
