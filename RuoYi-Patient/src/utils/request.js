import axios from 'axios'
import { showToast } from 'vant'

const request = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API,
  timeout: 10000
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
    if (res.code !== 200) {
      showToast(res.msg || '请求失败')
      if (res.code === 401) {
        localStorage.removeItem('token')
        window.location.href = '#/login'
      }
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res
  },
  error => {
    showToast(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
