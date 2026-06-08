import request from '@/utils/request'

export function getInfo() {
  return request({
    url: '/system/user/getInfo',
    method: 'get'
  })
}

