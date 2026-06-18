import request from '@/utils/request'

export function consultAi(data) {
  return request({
    url: '/emr/chat/consult',
    method: 'post',
    data: {
      ...data,
      mode: 'patient'
    }
  })
}

export function getAiChatHistory(query) {
  return request({
    url: '/emr/chat/history',
    method: 'get',
    params: query
  })
}

export function getSessionList(query) {
  return request({
    url: '/emr/chat/sessionList',
    method: 'get',
    params: query
  })
}
