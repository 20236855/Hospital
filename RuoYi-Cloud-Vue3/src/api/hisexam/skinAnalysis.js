import request from '@/utils/request'

// 皮肤病变分析（上传图片）
export function analyzeSkin(formData) {
  return request({
    url: '/hisexam/skin-analysis/analyze',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 120000
  })
}

// AI 辅助诊断（基于分析结果提问）
export function skinAiDiagnosis(data) {
  return request({
    url: '/hisexam/skin-analysis/ai-diagnosis',
    method: 'post',
    data: data,
    timeout: 180000
  })
}

// 健康检查
export function checkSkinServiceHealth() {
  return request({
    url: '/hisexam/skin-analysis/health',
    method: 'get',
    timeout: 5000
  })
}
