import request from '@/utils/request'

export function listDoctor(query) {
  return request({
    url: '/hisdoctor/doctor/open/list',
    method: 'get',
    params: query
  })
}

export async function getDoctor(id) {
  const response = await request({
    url: '/hisdoctor/doctor/open/list',
    method: 'get',
    params: {
      pageNum: 1,
      pageSize: 1,
      doctorId: id
    }
  })

  return {
    ...response,
    data: response?.data || response?.rows?.[0] || null
  }
}
