import request from '@/utils/request'

export function getMyPrescriptionPayments(config = {}) {
  return request({
    url: '/hisprescription/prescription/patient/payments',
    method: 'get',
    ...config
  })
}
