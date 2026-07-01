import request from '@/utils/request'

export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/file/upload',
    method: 'post',
    data: formData,
    timeout: 60000
  })
}
