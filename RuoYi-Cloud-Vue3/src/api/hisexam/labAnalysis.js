import request from '@/utils/request'

export function labAiAssist(data) {
  return request({
    url: '/hisexam/lab-analysis/ai-assist',
    method: 'post',
    data,
    timeout: 180000
  })
}
