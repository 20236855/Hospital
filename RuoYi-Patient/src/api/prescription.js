import request from '@/utils/request'

export async function getMyPrescriptionPayments(config = {}) {
  try {
    return await request({
      url: '/hisprescription/prescription/patient/payments',
      method: 'get',
      hideErrorToast: true,
      ...config
    })
  } catch (error) {
    const message = String(error?.message || '')
    if (message.includes('No static resource') || message.includes('请求的资源不存在')) {
      return { code: 200, data: [] }
    }
    throw error
  }
}
