<template>
  <div class="record-page">
    <div class="auth-bg-cross cross-one"></div>
    <div class="auth-bg-cross cross-two"></div>
    <div class="auth-bg-cross cross-three"></div>
    <div class="auth-bg-cross cross-four"></div>

    <div class="header-section">
      <div class="header-back" @click="goBack">
        <van-icon name="arrow-left" />
      </div>
      <div class="header-title">
        <h1>电子病历</h1>
      </div>
    </div>

    <div class="content-section">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list v-model:loading="loading" :finished="finished" :immediate-check="false" @load="onLoad">
          <div v-if="recordList.length === 0 && !loading" class="empty-state">
            <div class="empty-icon">
              <svg viewBox="0 0 120 120" class="empty-svg">
                <defs>
                  <linearGradient id="emptyGrad" x1="0" x2="1" y1="0" y2="1">
                    <stop offset="0" stop-color="#4A90E2" />
                    <stop offset="1" stop-color="#8ed6f2" />
                  </linearGradient>
                </defs>
                <circle cx="60" cy="60" r="45" fill="none" stroke="url(#emptyGrad)" stroke-width="3" opacity="0.3" />
                <rect x="38" y="42" width="44" height="52" rx="8" fill="none" stroke="url(#emptyGrad)" stroke-width="3" />
                <line x1="50" y1="58" x2="70" y2="58" stroke="url(#emptyGrad)" stroke-width="3" stroke-linecap="round" />
                <line x1="50" y1="70" x2="65" y2="70" stroke="url(#emptyGrad)" stroke-width="3" stroke-linecap="round" opacity="0.6" />
                <line x1="50" y1="82" x2="60" y2="82" stroke="url(#emptyGrad)" stroke-width="3" stroke-linecap="round" opacity="0.4" />
              </svg>
            </div>
            <div class="empty-text">
              <h3>暂无病例</h3>
              <p>当前患者暂无电子病历记录</p>
            </div>
          </div>

          <div v-else class="record-list">
            <div 
              v-for="(item, index) in recordList" 
              :key="item.recordId"
              class="record-card"
              @click="viewDetail(item)"
            >
              <div class="card-left">
                <div class="date-badge">
                  <span class="date-day">{{ formatDate(item.date).day }}</span>
                  <span class="date-month">{{ formatDate(item.date).month }}</span>
                </div>
              </div>
              <div class="card-content">
                <div class="card-header">
                    <div class="doctor-info">
                        <span class="doctor-avatar">{{ (item.doctorName || '医生').charAt(0) }}</span>
                        <div class="doctor-details">
                            <span class="doctor-name">{{ item.doctorName || '医生' }}</span>
                            <span class="dept-name">{{ item.deptName || '内科' }}</span>
                        </div>
                    </div>
                    <div class="visit-type">
                        <span class="type-tag">{{ item.visitType || '门诊' }}</span>
                    </div>
                </div>
                <div class="card-body">
                    <div class="diagnosis-section">
                        <span class="diagnosis-label">诊断</span>
                        <span class="diagnosis-text">{{ item.diagnosisOpinion || '常见病症，建议多休息，多喝水' }}</span>
                    </div>
                </div>
                <div class="card-footer">
                    <span class="visit-time">
                        <van-icon name="clock-o" />
                        {{ formatTime(item.date) }}
                    </span>
                    <span class="view-more">
                        查看详情
                        <van-icon name="arrow" />
                    </span>
                </div>
            </div>
            </div>
          </div>
        </van-list>
      </van-pull-refresh>
    </div>

    <van-tabbar v-model="active" class="custom-tabbar">
      <van-tabbar-item icon="home-o" to="/">首页</van-tabbar-item>
      <van-tabbar-item icon="todo-list-o" to="/register">挂号</van-tabbar-item>
      <van-tabbar-item icon="notes-o" to="/record">病历</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/profile">我的</van-tabbar-item>
    </van-tabbar>

    <van-popup
      v-model:show="showDetailPopup"
      position="bottom"
      round
      :style="{ height: '80%' }"
      closeable
    >
      <div v-if="currentRecord" class="detail-popup">
        <div class="detail-header">
          <div class="detail-title">病历详情</div>
          <div class="detail-date">{{ formatFullDate(currentRecord.date) }}</div>
        </div>

        <div class="detail-content">
            <div class="detail-section">
                <div class="section-title">
                    <van-icon name="medal" />
                    就诊信息
                </div>
                <div class="section-content">
                    <div class="info-row">
                        <span class="info-label">就诊医生</span>
                        <span class="info-value">{{ currentRecord.doctorName || '未知' }}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">就诊科室</span>
                        <span class="info-value">{{ currentRecord.deptName || '未知' }}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">就诊类型</span>
                        <span class="info-value">{{ currentRecord.visitType || '门诊' }}</span>
                    </div>
                </div>
            </div>

            <div class="detail-section">
                <div class="section-title">
                    <van-icon name="chat-o" />
                    主诉
                </div>
                <div class="section-content">
                    <div class="text-content">{{ currentRecord.chiefComplaint || '暂无记录' }}</div>
                </div>
            </div>

            <div class="detail-section">
                <div class="section-title">
                    <van-icon name="description" />
                    现病史
                </div>
                <div class="section-content">
                    <div class="text-content">{{ currentRecord.presentIllness || '暂无记录' }}</div>
                </div>
            </div>

            <div class="detail-section">
                <div class="section-title">
                    <van-icon name="balance-o" />
                    诊断意见
                </div>
                <div class="section-content">
                    <div class="diagnosis-box">
                        {{ currentRecord.diagnosisOpinion || '暂无记录' }}
                    </div>
                </div>
            </div>

            <div class="detail-section">
                <div class="section-title">
                    <van-icon name="notes-o" />
                    治疗方案
                </div>
                <div class="section-content">
                    <div class="text-content">{{ currentRecord.treatmentPlan || '暂无记录' }}</div>
                </div>
            </div>

            <div class="detail-section">
                <div class="section-title">
                    <van-icon name="balance-o" />
                    医生建议
                </div>
                <div class="section-content">
                    <div class="text-content">{{ currentRecord.doctorAdvice || '暂无记录' }}</div>
                </div>
            </div>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showLoadingToast, closeToast } from 'vant'
import { getMedicalRecordList, getMedicalRecord } from '@/api/emr'

const router = useRouter()
const active = ref(2)
const refreshing = ref(false)
const loading = ref(false)
const finished = ref(false)
const recordList = ref([])
const pageNum = ref(1)
const showDetailPopup = ref(false)
const currentRecord = ref(null)

const formatDate = (dateStr) => {
  if (!dateStr) return { day: '--', month: '--' }
  const date = new Date(dateStr.replace(/-/g, '/'))
  const months = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
  return {
    day: date.getDate().toString().padStart(2, '0'),
    month: months[date.getMonth()]
  }
}

const formatTime = (dateStr) => {
  if (!dateStr) return '--:--'
  const date = new Date(dateStr.replace(/-/g, '/'))
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const formatFullDate = (dateStr) => {
  if (!dateStr) return '--'
  const date = new Date(dateStr.replace(/-/g, '/'))
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}

const getStoredPatientId = () => {
  const value = localStorage.getItem('patientId')
  return value ? Number(value) : null
}

const onLoad = async () => {
  try {
    const patientId = getStoredPatientId()
    if (!patientId) {
      recordList.value = []
      finished.value = true
      return
    }
    
    const res = await getMedicalRecordList({
      pageNum: pageNum.value,
      pageSize: 10,
      patientId: patientId
    })
    
    const rows = res.rows || []
    const total = Number(res.total || 0)

    if (pageNum.value === 1) {
      recordList.value = rows
    } else {
      recordList.value = [...recordList.value, ...rows]
    }

    if (rows.length === 0 || total === 0 || recordList.value.length >= total) {
      finished.value = true
    } else {
      pageNum.value++
    }
  } catch (error) {
    finished.value = true
    console.error(error)
  } finally {
    loading.value = false
  }
}

const onRefresh = async () => {
  pageNum.value = 1
  finished.value = false
  recordList.value = []
  await onLoad()
  refreshing.value = false
}

const viewDetail = async (item) => {
  try {
    showLoadingToast({
      message: '加载中...',
      forbidClick: true,
      duration: 0
    })
    
    const res = await getMedicalRecord(item.recordId)
    currentRecord.value = res.data || item
    showDetailPopup.value = true
  } catch (error) {
    console.error(error)
    currentRecord.value = item
    showDetailPopup.value = true
  } finally {
    closeToast()
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  onLoad()
})
</script>

<style scoped lang="scss">
.record-page {
  min-height: 100vh;
  background: var(--bg-gradient);
  padding-bottom: 60px;
  position: relative;
  overflow-x: hidden;
}

.auth-bg-cross {
  display: none;

  &::before,
  &::after {
    content: "";
    position: absolute;
    background: #4A90E2;
    border-radius: 8px;
  }

  &::before {
    top: 50%;
    left: 0;
    width: 100%;
    height: 6px;
    transform: translateY(-50%);
  }

  &::after {
    top: 0;
    left: 50%;
    width: 6px;
    height: 100%;
    transform: translateX(-50%);
  }
}

.cross-one {
  top: 12%;
  left: 8%;
  animation-delay: 0s;
}

.cross-two {
  top: 25%;
  right: 10%;
  animation-delay: -2s;
}

.cross-three {
  bottom: 30%;
  left: 12%;
  animation-delay: -4s;
}

.cross-four {
  bottom: 15%;
  right: 6%;
  animation-delay: -6s;
}

.header-section {
  padding: 16px 20px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-back {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.96);
    background: rgba(255, 255, 255, 0.8);
  }

  .van-icon {
    color: #5f7580;
    font-size: 20px;
  }
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;

  h1 {
    font-size: 24px;
    font-weight: 700;
    color: #4f7380;
    margin: 0;
  }
}

.title-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(104, 199, 169, 0.2);
}

.title-cross {
  position: relative;
  width: 24px;
  height: 24px;

  &::before,
  &::after {
    content: "";
    position: absolute;
    background: linear-gradient(135deg, #4A90E2, #A8C8EC);
    border-radius: 6px;
  }

  &::before {
    top: 50%;
    left: 0;
    width: 100%;
    height: 5px;
    transform: translateY(-50%);
  }

  &::after {
    top: 0;
    left: 50%;
    width: 5px;
    height: 100%;
    transform: translateX(-50%);
  }
}

.content-section {
  padding: 14px 14px 18px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 220px);
  padding: 56px 20px;
  background: #fff;
  border: 1px solid #e6ebf1;
  border-radius: 12px;
}

.empty-icon {
  margin-bottom: 24px;
}

.empty-svg {
  width: 112px;
  height: 112px;
}

.empty-text {
  text-align: center;

  h3 {
    font-size: 18px;
    font-weight: 700;
    color: #1f3440;
    margin: 0 0 8px;
  }

  p {
    font-size: 14px;
    color: #8e9fa8;
    margin: 0;
  }
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.record-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid #e4eaf0;
  box-shadow: 0 6px 18px rgba(31, 52, 64, 0.06);
  display: flex;
  gap: 14px;
  cursor: pointer;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;

  &:hover {
    border-color: #b8d5dd;
    box-shadow: 0 8px 22px rgba(31, 52, 64, 0.08);
  }

  &:active {
    border-color: #8bb9c5;
  }
}

.card-left {
  flex-shrink: 0;
}

.date-badge {
  width: 60px;
  height: 68px;
  border-radius: 10px;
  background: #edf7f5;
  border: 1px solid #cae7e1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  .date-day {
    font-size: 23px;
    font-weight: 700;
    color: #207767;
    line-height: 1;
  }

  .date-month {
    font-size: 12px;
    font-weight: 600;
    color: #5b8f87;
    margin-top: 4px;
  }
}

.card-content {
  flex: 1;
  min-width: 0;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.doctor-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.doctor-avatar {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: #e8f1f8;
  border: 1px solid #d7e5ee;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  color: #2d647b;
}

.doctor-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.doctor-name {
  font-size: 16px;
  font-weight: 700;
  color: #1f3440;
}

.dept-name {
  font-size: 13px;
  color: #8e9fa8;
}

.visit-type {
  flex-shrink: 0;
}

.type-tag {
  font-size: 12px;
  font-weight: 600;
  color: #207767;
  background: #edf7f5;
  border: 1px solid #cae7e1;
  padding: 4px 10px;
  border-radius: 999px;
}

.card-body {
  margin-bottom: 12px;
}

.diagnosis-section {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.diagnosis-label {
  font-size: 13px;
  color: #8e9fa8;
  flex-shrink: 0;
  padding-top: 2px;
}

.diagnosis-text {
  font-size: 14px;
  font-weight: 500;
  color: #314a56;
  line-height: 1.6;
  flex: 1;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px solid #edf1f5;
}

.visit-time {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #a5b8c0;

  .van-icon {
    font-size: 14px;
  }
}

.view-more {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 600;
  color: #207767;

  .van-icon {
    font-size: 14px;
  }
}

.custom-tabbar {
  background: #fff;
  border-top: 1px solid #e6ebf1;
}

.detail-popup {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.detail-header {
  padding: 20px 24px;
  background: #fff;
  color: #1f3440;
  border-bottom: 1px solid #e6ebf1;
  
  .detail-title {
    font-size: 21px;
    font-weight: 700;
    margin-bottom: 6px;
  }
  
  .detail-date {
    font-size: 14px;
    color: #71838d;
  }
}

.detail-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-section {
  background: white;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid #e4eaf0;
  box-shadow: 0 4px 14px rgba(31, 52, 64, 0.04);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #1f3440;
  margin-bottom: 12px;
  
  .van-icon {
    color: #207767;
    font-size: 18px;
  }
}

.section-content {
  padding-left: 4px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #edf1f5;
  
  &:last-child {
    border-bottom: none;
  }
}

.info-label {
  font-size: 14px;
  color: #8e9fa8;
}

.info-value {
  font-size: 14px;
  color: #1f3440;
  font-weight: 500;
}

.text-content {
  font-size: 14px;
  color: #314a56;
  line-height: 1.8;
}

.diagnosis-box {
  background: #f7fbfa;
  border-left: 4px solid #207767;
  padding: 16px;
  border-radius: 8px;
  font-size: 15px;
  color: #1f3440;
  font-weight: 500;
  line-height: 1.8;
}
</style>
