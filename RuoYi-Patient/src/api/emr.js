import request from '@/utils/request'

export function getEncounterList(query) {
  return request({
    url: '/emr/encounter/list',
    method: 'get',
    params: query
  })
}

export function getEncounter(id) {
  return request({
    url: '/emr/encounter/' + id,
    method: 'get'
  })
}

export function getMedicalRecordList(query) {
  return request({
    url: '/emr/record/list',
    method: 'get',
    params: query
  })
}

export function getMedicalRecord(id) {
  return request({
    url: '/emr/record/' + id,
    method: 'get'
  })
}
