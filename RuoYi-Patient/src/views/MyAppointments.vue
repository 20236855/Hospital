<template>
  <div class="appointments-page">
    <div class="auth-bg-cross cross-one"></div>
    <div class="auth-bg-cross cross-two"></div>
    <div class="auth-bg-cross cross-three"></div>
    <div class="auth-bg-cross cross-four"></div>

    <div class="header-section">
      <div class="header-back" @click="goBack">
        <van-icon name="arrow-left" />
      </div>
      <div class="header-title">
        <span class="title-icon float-animation">
          <span class="title-cross"></span>
        </span>
        <h1>我的预约</h1>
      </div>
    </div>

    <div class="content-section">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list v-model:loading="loading" :finished="finished" @load="onLoad">
          <div v-if="appointmentList.length === 0 && !loading" class="empty-state slide-up-animation">
            <div class="empty-icon">
              <svg viewBox="0 0 120 120" class="empty-svg">
                <defs>
                  <linearGradient id="emptyGrad" x1="0" x2="1" y1="0" y2="1">
                    <stop offset="0" stop-color="#68c7a9" />
                    <stop offset="1" stop-color="#8ed6f2" />
                  </linearGradient>
                </defs>
                <circle cx="60" cy="60" r="45" fill="none" stroke="url(#emptyGrad)" stroke-width="3" opacity="0.3" />
                <rect x="38" y="38" width="44" height="44" rx="8" fill="none" stroke="url(#emptyGrad)" stroke-width="3" />
                <line x1="48" y1="52" x2="72" y2="52" stroke="url(#emptyGrad)" stroke-width="3" stroke-linecap="round" />
                <line x1="48" y1="60" x2="72" y2="60" stroke="url(#emptyGrad)" stroke-width="3" stroke-linecap="round" />
                <line x1="48" y1="68" x2="60" y2="68" stroke="url(#emptyGrad)" stroke-width="3" stroke-linecap="round" opacity="0.6" />
              </svg>
            </div>
            <div class="empty-text">
              <h3>暂无预约记录</h3>
              <p>您还没有挂号预约</p>
            </div>
          </div>

          <div v-else class="appointment-list">
            <div
              v-for="(item, index) in appointmentList"
              :key="item.registerId"
              class="appointment-card slide-up-animation"
              :style="{ animationDelay: `${index * 0.08}s` }"
              @click="viewDetail(item)"
            >
              <div class="card-left">
                <div class="date-badge float-animation" :style="{ animationDelay: `${index * 0.05}s` }">
                  <span class="date-day">{{ formatDate(item.registerTime).day }}</span>
                  <span class="date-month">{{ formatDate(item.registerTime).month }}</span>
                </div>
              </div>
              <div class="card-content">
                <div class="card-header">
                  <div class="doctor-info">
                    <span class="doctor-avatar">{{ (item.doctorName || '医生').charAt(0) }}</span>
                    <div class="doctor-details">
                      <span class="doctor-name">{{ item.doctorName || '医生' }}</span>
                      <span class="dept-name">{{ item.deptName || '科室' }}</span>
                    </div>
                  </div>
                  <div class="status-tags">
                    <span class="status-tag pay-tag" :class="item.payStatus === 'paid' ? 'paid' : 'unpaid'">
                      {{ item.payStatus === 'paid' ? '已支付' : '待支付' }}
                    </span>
                  </div>
                </div>
                <div class="card-body">
                  <div class="appointment-info">
                    <span class="info-label">挂号单号</span>
                    <span class="info-value">{{ item.registerNo || '-' }}</span>
                  </div>
                  <div class="appointment-info">
                    <span class="info-label">挂号级别</span>
                    <span class="info-value level-value">{{ item.levelName || '普通号' }}</span>
                  </div>
                </div>
                <div class="card-footer">
                  <div class="footer-left">
                    <span class="register-type" :class="item.registerType === 'online' ? 'online' : 'offline'">
                      {{ item.registerType === 'online' ? '线上预约' : '线下挂号' }}
                    </span>
                    <span class="register-fee">¥{{ item.registerFee || 0 }}</span>
                  </div>
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
      :style="{ height: '75%' }"
      closeable
    >
      <div v-if="currentAppointment" class="detail-popup">
        <div class="detail-header">
          <div class="detail-title">预约详情</div>
          <div class="detail-date">{{ formatFullDate(currentAppointment.registerTime) }}</div>
        </div>

        <div class="detail-content">
          <div class="detail-section">
            <div class="section-title">
              <van-icon name="todo-list-o" />
              挂号信息
            </div>
            <div class="section-content">
              <div class="info-row">
                <span class="info-label">挂号单号</span>
                <span class="info-value">{{ currentAppointment.registerNo || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">挂号时间</span>
                <span class="info-value">{{ formatFullDate(currentAppointment.registerTime) }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">挂号类型</span>
                <span class="info-value">
                  <span class="detail-type-tag" :class="currentAppointment.registerType === 'online' ? 'online' : 'offline'">
                    {{ currentAppointment.registerType === 'online' ? '线上预约' : '线下挂号' }}
                  </span>
                </span>
              </div>
              <div class="info-row">
                <span class="info-label">挂号级别</span>
                <span class="info-value">{{ currentAppointment.levelName || '普通号' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">挂号费用</span>
                <span class="info-value fee-value">¥{{ currentAppointment.registerFee || 0 }}</span>
              </div>
            </div>
          </div>

          <div class="detail-section">
            <div class="section-title">
              <van-icon name="medal" />
              就诊信息
            </div>
            <div class="section-content">
              <div class="info-row">
                <span class="info-label">就诊医生</span>
                <span class="info-value">{{ currentAppointment.doctorName || '未知' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">就诊科室</span>
                <span class="info-value">{{ currentAppointment.deptName || '未知' }}</span>
              </div>
            </div>
          </div>

          <div class="detail-section">
            <div class="section-title">
              <van-icon name="balance-o" />
              状态信息
            </div>
            <div class="section-content">
              <div class="info-row">
                <span class="info-label">挂号状态</span>
                <span class="info-value">
                  <span class="status-badge-detail" :class="currentAppointment.registerStatus">
                    {{ getStatusText(currentAppointment.registerStatus) }}
                  </span>
                </span>
              </div>
              <div class="info-row">
                <span class="info-label">支付状态</span>
                <span class="info-value">
                  <span class="pay-badge-detail" :class="currentAppointment.payStatus === 'paid' ? 'paid' : 'unpaid'">
                    {{ currentAppointment.payStatus === 'paid' ? '已支付' : '待支付' }}
                  </span>
                </span>
              </div>
              <div class="info-row">
                <span class="info-label">创建时间</span>
                <span class="info-value">{{ currentAppointment.createTime || '-' }}</span>
              </div>
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
import { getRegisterList } from '@/api/register'

const router = useRouter()
const active = ref(3)
const refreshing = ref(false)
const loading = ref(false)
const finished = ref(false)
const appointmentList = ref([])
const pageNum = ref(1)
const showDetailPopup = ref(false)
const currentAppointment = ref(null)

const formatDate = (dateStr) => {
  if (!dateStr) return { day: '--', month: '--' }
  const date = new Date(dateStr.replace(/-/g, '/'))
  if (isNaN(date.getTime())) return { day: '--', month: '--' }
  const months = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
  return {
    day: date.getDate().toString().padStart(2, '0'),
    month: months[date.getMonth()]
  }
}

const formatFullDate = (dateStr) => {
  if (!dateStr) return '--'
  const date = new Date(dateStr.replace(/-/g, '/'))
  if (isNaN(date.getTime())) return '--'
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}

const getStatusText = (status) => {
  const map = {
    'registered': '已挂号',
    'cancel': '已取消',
    'completed': '已完成'
  }
  return map[status] || status || '-'
}

const onLoad = async () => {
  try {
    const res = await getRegisterList({
      pageNum: pageNum.value,
      pageSize: 10
    })

    if (pageNum.value === 1) {
      appointmentList.value = res.rows || []
    } else {
      appointmentList.value = [...appointmentList.value, ...(res.rows || [])]
    }
    loading.value = false
    if (appointmentList.value.length >= (res.total || 10)) {
      finished.value = true
    } else {
      pageNum.value++
    }
  } catch (error) {
    loading.value = false
    console.error('加载预约列表失败', error)
  }
}

const onRefresh = async () => {
  pageNum.value = 1
  finished.value = false
  appointmentList.value = []
  await onLoad()
  refreshing.value = false
}

const viewDetail = (item) => {
  currentAppointment.value = item
  showDetailPopup.value = true
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  onLoad()
})
</script>

<style scoped lang="scss">
.appointments-page {
  min-height: 100vh;
  background: var(--bg-gradient);
  padding-bottom: 60px;
  position: relative;
  overflow-x: hidden;
}

.auth-bg-cross {
  position: absolute;
  width: 40px;
  height: 40px;
  opacity: 0.12;
  animation: authCrossBreath 8s ease-in-out infinite;

  &::before,
  &::after {
    content: "";
    position: absolute;
    background: #68c7a9;
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
    background: linear-gradient(135deg, #68c7a9, #89dbc1);
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
  padding: 0 16px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
}

.empty-icon {
  margin-bottom: 24px;
}

.empty-svg {
  width: 140px;
  height: 140px;
  animation: floatSoft 4s ease-in-out infinite;
}

.empty-text {
  text-align: center;

  h3 {
    font-size: 18px;
    font-weight: 700;
    color: #4f7380;
    margin: 0 0 8px;
  }

  p {
    font-size: 14px;
    color: #8e9fa8;
    margin: 0;
  }
}

.appointment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.appointment-card {
  background: rgba(255, 255, 255, 0.74);
  border-radius: 20px;
  padding: 16px;
  border: 1px solid rgba(194, 228, 236, 0.72);
  box-shadow: var(--card-shadow);
  backdrop-filter: blur(8px);
  display: flex;
  gap: 14px;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 16px 32px rgba(102, 170, 189, 0.18);
  }

  &:active {
    transform: translateY(0);
  }
}

.card-left {
  flex-shrink: 0;
}

.date-badge {
  width: 64px;
  height: 72px;
  border-radius: 16px;
  background: linear-gradient(135deg, #ffd6a5, #ffc477);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 16px rgba(255, 196, 119, 0.3);

  .date-day {
    font-size: 24px;
    font-weight: 700;
    color: white;
    line-height: 1;
  }

  .date-month {
    font-size: 12px;
    font-weight: 600;
    color: rgba(255, 255, 255, 0.85);
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
  border-radius: 12px;
  background: linear-gradient(135deg, #8ed6f2, #bfefff);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  color: white;
}

.doctor-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.doctor-name {
  font-size: 16px;
  font-weight: 700;
  color: #4f7380;
}

.dept-name {
  font-size: 13px;
  color: #8e9fa8;
}

.status-tags {
  flex-shrink: 0;
}

.status-tag {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 8px;

  &.pay-tag {
    &.paid {
      color: #68c7a9;
      background: rgba(104, 199, 169, 0.12);
    }

    &.unpaid {
      color: #ef8fa9;
      background: rgba(239, 143, 169, 0.12);
    }
  }
}

.card-body {
  margin-bottom: 12px;
}

.appointment-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 2px 0;

  .info-label {
    font-size: 13px;
    color: #8e9fa8;
    flex-shrink: 0;
    width: 60px;
  }

  .info-value {
    font-size: 14px;
    font-weight: 500;
    color: #5f7580;

    &.level-value {
      color: #68c7a9;
      font-weight: 600;
    }
  }
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px solid rgba(213, 237, 243, 0.6);
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.register-type {
  font-size: 12px;
  font-weight: 600;
  padding: 3px 8px;
  border-radius: 6px;

  &.online {
    color: #8ed6f2;
    background: rgba(142, 214, 242, 0.12);
  }

  &.offline {
    color: #a5b8c0;
    background: rgba(165, 184, 192, 0.12);
  }
}

.register-fee {
  font-size: 14px;
  font-weight: 600;
  color: #ef8fa9;
}

.view-more {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 600;
  color: #68c7a9;

  .van-icon {
    font-size: 14px;
  }
}

.custom-tabbar {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-top: 1px solid rgba(194, 228, 236, 0.72);
}

.detail-popup {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f8fcfd;
}

.detail-header {
  padding: 20px 24px;
  background: linear-gradient(135deg, #ffc477, #ffd6a5);

  .detail-title {
    font-size: 22px;
    font-weight: 700;
    color: white;
    margin-bottom: 6px;
  }

  .detail-date {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.9);
  }
}

.detail-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-section {
  background: white;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(104, 199, 169, 0.08);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #4f7380;
  margin-bottom: 12px;

  .van-icon {
    color: #68c7a9;
    font-size: 18px;
  }
}

.section-content {
  padding-left: 4px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid rgba(213, 237, 243, 0.5);

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
  color: #4f7380;
  font-weight: 500;

  &.fee-value {
    color: #ef8fa9;
    font-weight: 700;
    font-size: 16px;
  }
}

.detail-type-tag {
  font-size: 12px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 6px;

  &.online {
    color: #8ed6f2;
    background: rgba(142, 214, 242, 0.12);
  }

  &.offline {
    color: #a5b8c0;
    background: rgba(165, 184, 192, 0.12);
  }
}

.status-badge-detail {
  font-size: 13px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 8px;

  &.registered {
    color: #68c7a9;
    background: rgba(104, 199, 169, 0.12);
  }

  &.cancel {
    color: #a5b8c0;
    background: rgba(165, 184, 192, 0.12);
  }

  &.completed {
    color: #8bd6e2;
    background: rgba(139, 214, 226, 0.12);
  }
}

.pay-badge-detail {
  font-size: 13px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 8px;

  &.paid {
    color: #68c7a9;
    background: rgba(104, 199, 169, 0.12);
  }

  &.unpaid {
    color: #ef8fa9;
    background: rgba(239, 143, 169, 0.12);
  }
}

@keyframes authCrossBreath {
  0%, 100% { opacity: 0.08; transform: scale(1); }
  50% { opacity: 0.18; transform: scale(1.08); }
}

@keyframes floatSoft {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}
</style>
