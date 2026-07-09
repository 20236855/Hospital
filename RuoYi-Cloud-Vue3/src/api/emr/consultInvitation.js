import request from '@/utils/request'

// 发起联合会诊邀请
export function createConsultInvitation(data) {
  return request({
    url: '/hisdoctor/consult/invite',
    method: 'post',
    data
  })
}

// 查询当前医生所在科室收到的联合会诊邀请
export function listConsultInvitations() {
  return request({
    url: '/hisdoctor/consult/invitations',
    method: 'get'
  })
}

// 接受联合会诊邀请
export function acceptConsultInvitation(invitationId) {
  return request({
    url: '/hisdoctor/consult/accept',
    method: 'post',
    params: { invitationId }
  })
}
