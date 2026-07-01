import request from '@/utils/request'

/**
 * 肺部CT分析API
 */

/**
 * 上传CT文件执行完整肺结节分析
 * Step 01: CT值结节检测与分类
 * Step 02: HU阈值 + 形态学精确分割
 * Step 03: CT特征良恶性风险评估
 * @param {File} file CT文件 (DICOM zip / MHD / NIfTI)
 * @param {Object} params 算法参数 { max_slices, lung_threshold, soft_min, soft_max, autodl_endpoint }
 */
export function analyzeLungCt(file, params = {}) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('params', JSON.stringify(params))
  return request({
    url: '/hisexam/lung-ct/analyze',
    method: 'post',
    data: formData,
    timeout: 300000
  })
}

/**
 * 单层切片分析
 */
export function analyzeLungCtSlice(files, sliceIndex = 0) {
  const formData = new FormData()
  files.forEach(f => formData.append('files', f))
  formData.append('sliceIndex', String(sliceIndex))
  return request({
    url: '/hisexam/lung-ct/analyze-slice',
    method: 'post',
    data: formData,
    timeout: 120000
  })
}

/**
 * 健康检查
 */
export function checkLungCtServiceHealth() {
  return request({
    url: '/hisexam/lung-ct/health',
    method: 'get',
    timeout: 5000
  }).catch(() => {
    return { data: { available: false } }
  })
}

/**
 * 保存CT切片到数据库
 * @param {Object} saveData { applyId, checkType, patientId, doctorId, fileName, diagnosisReport, slices, remark }
 */
export function saveCtSlices(saveData) {
  return request({
    url: '/hisexam/lung-ct/save',
    method: 'post',
    data: saveData,
    timeout: 60000
  })
}

/**
 * 根据申请单ID查询已保存的切片
 */
export function getSlicesByApplyId(applyId) {
  return request({
    url: `/hisexam/lung-ct/slices/${applyId}`,
    method: 'get'
  })
}

/**
 * 删除申请单下所有切片
 */
export function deleteSlicesByApplyId(applyId) {
  return request({
    url: `/hisexam/lung-ct/slices/${applyId}`,
    method: 'delete'
  })
}
