import request from '@/utils/request'

/**
 * AI医疗助手 API（通过后端代理调用 DeepSeek）
 * 后端在 his-emr 模块，路由: /emr/chat/**
 *
 * mode 参数：
 *   "patient" — 患者端在线问诊，通俗口语，禁止开药/确诊
 *   "doctor"  — 医生端辅助诊疗，专业术语，循证决策，支持病历生成
 */

/**
 * AI问诊咨询（发送消息，获取AI回复）
 * @param {Object} params
 * @param {string} params.message - 用户消息
 * @param {string} params.sessionId - 会话ID（首次为空，后续使用返回的sessionId）
 * @param {number} params.patientId - 患者ID（可选，用于病历上下文）
 * @param {string} params.mode - 模式：patient（患者端）或 doctor（医生端），默认patient
 * @returns {Promise<Object>} { sessionId, reply, patientId, hasMedicalRecord }
 */
export function aiChat(params) {
  const { message, sessionId, patientId, mode } = params
  return request({
    url: '/emr/chat/consult',
    method: 'post',
    data: {
      message,
      sessionId: sessionId || undefined,
      patientId: patientId || undefined,
      mode: mode || 'patient'
    }
  }).then(res => res.data)
}

/**
 * 获取聊天历史
 * @param {string} sessionId - 会话ID
 * @param {number} patientId - 患者ID
 */
export function getChatHistory(sessionId, patientId) {
  return request({
    url: '/emr/chat/history',
    method: 'get',
    params: { sessionId, patientId }
  }).then(res => res.data)
}

/**
 * 获取会话列表
 * @param {number} patientId - 患者ID
 */
export function getSessionList(patientId) {
  return request({
    url: '/emr/chat/sessionList',
    method: 'get',
    params: { patientId }
  }).then(res => res.data)
}
