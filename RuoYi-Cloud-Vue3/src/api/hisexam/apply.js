import request from '@/utils/request'

// 查询检查/检验/处置申请单列表
export function listApply(query) {
  return request({
    url: '/hisexam/apply/list',
    method: 'get',
    params: query
  })
}

// 查询可开检查/检验/处置申请单的挂号记录
export function listApplyRegisterOptions(query) {
  return listApplyRegisterOptionsByLegacyApi(query)
}

function listApplyRegisterOptionsByLegacyApi(query = {}) {
  const keyword = query.keyword
  const registerQuery = {
    pageNum: 1,
    pageSize: 30
  }
  if (keyword) {
    registerQuery.patientName = keyword
  }
  return request({
    url: '/register/register/list',
    method: 'get',
    params: registerQuery
  }).then(async res => {
    let registers = res.rows || res.data?.rows || []
    if (keyword && registers.length === 0) {
      const byNo = await request({
        url: '/register/register/list',
        method: 'get',
        params: { pageNum: 1, pageSize: 30, registerNo: keyword }
      })
      registers = byNo.rows || byNo.data?.rows || []
    }
    const options = await Promise.all(registers.map(async item => {
      try {
        const encRes = await request({
          url: '/emr/encounter/list',
          method: 'get',
          params: { registerId: item.registerId, pageNum: 1, pageSize: 1 }
        })
        const encounters = encRes.rows || encRes.data?.rows || []
        return {
          registerId: item.registerId,
          registerNo: item.registerNo,
          encounterId: encounters[0]?.encounterId,
          patientId: item.patientId,
          patientName: item.patientName,
          gender: item.gender,
          age: item.age,
          doctorId: item.doctorId,
          doctorName: item.doctorName,
          deptId: item.deptId,
          deptName: item.deptName,
          registerTime: item.registerTime
        }
      } catch (e) {
        return null
      }
    }))
    return { data: options.filter(item => item && item.encounterId) }
  })
}

// 查询检查/检验/处置申请单详细
export function getApply(applyId) {
  return request({
    url: '/hisexam/apply/' + applyId,
    method: 'get'
  })
}

// 新增检查/检验/处置申请单
export function addApply(data) {
  return request({
    url: '/hisexam/apply',
    method: 'post',
    data: data
  })
}

// 修改检查/检验/处置申请单
export function updateApply(data) {
  return request({
    url: '/hisexam/apply',
    method: 'put',
    data: data
  })
}

// 删除检查/检验/处置申请单
export function delApply(applyId) {
  return request({
    url: '/hisexam/apply/' + applyId,
    method: 'delete'
  })
}
