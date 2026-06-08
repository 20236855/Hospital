import request from '@/utils/request'

export function login(username, password, code, uuid) {
  return request({
    url: '/auth/login',
    headers: {
      isToken: false,
      repeatSubmit: false
    },
    method: 'post',
    data: { username, password, code, uuid }
  })
}

export function patientLogin(username, password, code, uuid) {
  return request({
    url: '/auth/patient/login',
    headers: {
      isToken: false,
      repeatSubmit: false
    },
    method: 'post',
    data: { username, password, code, uuid }
  })
}

export function register(data) {
  return request({
    url: '/auth/register',
    headers: {
      isToken: false,
      repeatSubmit: false
    },
    method: 'post',
    data
  })
}

export function getCodeImg() {
  return request({
    url: '/code',
    headers: {
      isToken: false
    },
    method: 'get',
    timeout: 20000
  })
}

export function logout() {
  return request({
    url: '/auth/logout',
    method: 'delete'
  })
}
