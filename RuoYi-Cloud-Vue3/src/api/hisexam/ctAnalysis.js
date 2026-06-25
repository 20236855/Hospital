import request from '@/utils/request'

/**
 * CT金属伪影检测 - 上传CT文件进行分析
 * @param {File} file CT文件 (DICOM zip/MHD/NIfTI)
 * @param {Object} params 算法参数 { metal_hu, art_high, art_low, dilate_r, enable_classify, enable_3d }
 */
export function detectArtifact(file, params = {}) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('params', JSON.stringify(params))
  // 注意：不要手动设置 Content-Type！
  // axios 会自动为 FormData 设置正确的 multipart/form-data; boundary=xxxx
  return request({
    url: '/hisexam/ct-analysis/artifact-detect',
    method: 'post',
    data: formData,
    timeout: 300000  // 5分钟超时，CT处理较慢
  })
}

/**
 * 健康检查 - 检测Python CT分析服务是否运行
 */
export function checkCtServiceHealth() {
  return request({
    url: '/hisexam/ct-analysis/health',
    method: 'get',
    timeout: 5000
  }).catch(() => {
    // 服务未启动时静默失败
    return { data: { available: false } }
  })
}
