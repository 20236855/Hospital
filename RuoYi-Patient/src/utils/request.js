import axios from 'axios'
import { showToast } from 'vant'

const request = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API,
  timeout: 60000
})

request.interceptors.request.use(
  config => {
    const isToken = (config.headers || {}).isToken === false
    const token = localStorage.getItem('token')
    if (token && !isToken) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    const res = response.data
    const hideErrorToast = response.config?.hideErrorToast
    if (res.code !== 200) {
      if (!hideErrorToast) {
        showToast(res.msg || '请求失败')
      }
      if (res.code === 401) {
        localStorage.removeItem('token')
        window.location.href = '#/login'
      }
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res
  },
  error => {
    console.error('请求错误:', error)
    const hideErrorToast = error.config?.hideErrorToast
    let message = '网络错误'
    if (error.response) {
      // 服务器返回错误
      const res = error.response.data
      if (res && res.msg) {
        message = res.msg
      } else if (error.response.status === 401) {
        message = '未授权，请重新登录'
        localStorage.removeItem('token')
        window.location.href = '#/login'
      } else if (error.response.status === 404) {
        message = '请求的资源不存在'
      } else if (error.response.status === 500) {
        message = '服务器内部错误'
      }
    } else if (error.message) {
      message = error.message
    }
    if (!hideErrorToast) {
      showToast(message || '请求失败')
    }
    return Promise.reject(new Error(message))
  }
)

export default request
