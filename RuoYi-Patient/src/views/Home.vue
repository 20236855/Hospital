<template>
  <div class="home-page">
    <div class="ambient-layer" aria-hidden="true">
      <span class="mesh mesh-a"></span>
      <span class="mesh mesh-b"></span>
      <span class="mesh mesh-c"></span>
      <span class="grid-plane"></span>
    </div>

    <div class="content-wrapper">
      <section class="hero-section slide-up-animation">
        <div class="hero-copy">
          <div class="top-row">
            <div>
              <p class="eyebrow">智慧医院 · 患者端</p>
              <h1>早上好，{{ displayName }}</h1>
            </div>
            <button class="status-badge" type="button">
              <span class="status-dot"></span>
              在线
            </button>
          </div>
          <p class="hero-desc">今天适合把就诊安排处理得更从容。</p>
          <div class="hero-metrics">
            <div class="metric-card">
              <span class="metric-value">{{ todayAppointments }}</span>
              <span class="metric-label">今日就诊</span>
            </div>
            <div class="metric-card">
              <span class="metric-value">{{ appointmentList.length }}</span>
              <span class="metric-label">预约记录</span>
            </div>
            <div class="metric-card">
              <span class="metric-value">AI</span>
              <span class="metric-label">在线问诊</span>
            </div>
          </div>
        </div>

        <div class="hero-visual" aria-hidden="true">
          <div class="radar-ring ring-one"></div>
          <div class="radar-ring ring-two"></div>
          <div class="core-card">
            <div class="cross-mark">
              <span></span>
              <span></span>
            </div>
            <div class="pulse-track">
              <svg viewBox="0 0 190 44" fill="none">
                <path class="pulse-shadow" d="M2 25H45L58 8L78 39L94 20H126L140 12L153 31H188" />
                <path class="pulse-line" d="M2 25H45L58 8L78 39L94 20H126L140 12L153 31H188" />
              </svg>
            </div>
          </div>
          <span class="float-chip chip-a">挂号</span>
          <span class="float-chip chip-b">病历</span>
          <span class="float-chip chip-c">报告</span>
        </div>
      </section>

      <section class="quick-actions slide-up-animation" style="animation-delay: 0.08s">
        <button
          v-for="item in quickActions"
          :key="item.title"
          class="action-card"
          type="button"
          @click="item.action"
        >
          <span class="action-icon" :class="item.className">
            <img v-if="item.image" :src="item.image" alt="" />
            <van-icon v-else :name="item.icon" />
          </span>
          <span>{{ item.title }}</span>
        </button>
      </section>

      <section class="info-section slide-up-animation" style="animation-delay: 0.18s">
        <div class="section-heading">
          <div>
            <span class="section-kicker">TODAY</span>
            <h2>今日就诊</h2>
          </div>
          <button type="button" @click="goToMyAppointments">查看预约</button>
        </div>

        <div class="info-card">
          <div class="timeline-glow"></div>
          <template v-if="appointmentList.length > 0">
            <div
              v-for="appt in appointmentList.slice(0, 2)"
              :key="appt.registerId"
              class="detail-item"
            >
              <span class="time-pill">{{ formatRegisterTime(appt.registerTime) }}</span>
              <div class="detail-copy">
                <strong>{{ appt.deptName || '门诊科室' }}</strong>
                <span>{{ appt.doctorName || '接诊医生' }} · 待就诊</span>
              </div>
            </div>
          </template>
          <div v-else class="empty-state">
            <span class="empty-icon">
              <van-icon name="calendar-o" />
            </span>
            <div>
              <strong>暂无今日预约</strong>
              <span>可以先进行预约挂号或在线问诊。</span>
            </div>
          </div>
        </div>
      </section>

      <section class="more-section slide-up-animation" style="animation-delay: 0.24s">
        <div class="section-heading">
          <div>
            <span class="section-kicker">SERVICES</span>
            <h2>更多服务</h2>
          </div>
          <button type="button" @click="toggleServices">
            {{ showAllServices ? '收起' : '展开全部' }}
          </button>
        </div>

        <div class="service-list">
          <button
            v-for="item in visibleServiceItems"
            :key="item.title"
            class="service-item"
            type="button"
            @click="item.action"
          >
            <span class="service-icon">
              <van-icon :name="item.icon" />
            </span>
            <span>{{ item.title }}</span>
          </button>
        </div>
      </section>
    </div>

    <van-tabbar v-model="active" route>
      <van-tabbar-item icon="home-o" to="/">首页</van-tabbar-item>
      <van-tabbar-item icon="add-o" to="/register">挂号</van-tabbar-item>
      <van-tabbar-item icon="notes-o" to="/record">病历</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/profile">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getPatientList } from '@/api/patient'
import { getRegisterList } from '@/api/register'
import guahaoIcon from '@/assets/images/guahao.png'
import yuyueIcon from '@/assets/images/yuyue.png'
import bingliIcon from '@/assets/images/bingli.png'
import baogaoIcon from '@/assets/images/baogao.png'

const router = useRouter()
const active = ref(0)
const username = ref('')
const todayAppointments = ref(0)
const appointmentList = ref([])
const showAllServices = ref(false)

const displayName = computed(() => username.value || '患者')
const visibleServiceItems = computed(() => (showAllServices.value ? serviceItems : serviceItems.slice(0, 4)))

const goToRegister = () => {
  router.push('/register')
}

const goToDoctors = () => {
  router.push('/doctors')
}

const goToRecord = () => {
  router.push('/record')
}

const goToChat = () => {
  localStorage.removeItem('aiChatSessionId')
  router.push('/chat')
}

const goToMyAppointments = () => {
  router.push('/my-appointments')
}

const toggleServices = () => {
  showAllServices.value = !showAllServices.value
}

const quickActions = [
  { title: '预约挂号', image: guahaoIcon, className: 'icon-register', action: goToRegister },
  { title: '病历查询', image: bingliIcon, className: 'icon-record', action: goToRecord },
  { title: '我的预约', image: yuyueIcon, className: 'icon-appointment', action: goToMyAppointments },
  { title: '健康报告', image: baogaoIcon, className: 'icon-report', action: () => showToast('健康报告') }
]

const serviceItems = [
  { title: '科室医生', icon: 'manager-o', action: goToDoctors },
  { title: '健康资讯', icon: 'newspaper-o', action: () => showToast('健康资讯') },
  { title: '在线问诊', icon: 'chat-o', action: goToChat },
  { title: '检查预约', icon: 'records-o', action: () => showToast('检查预约') },
  { title: '个人中心', icon: 'user-circle-o', action: () => router.push('/profile') }
]

const formatRegisterTime = (value) => {
  if (!value) return '--:--'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '--:--'
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const loadUserData = async () => {
  try {
    const savedUsername = localStorage.getItem('username')
    const savedPatientName = localStorage.getItem('patientName')
    const savedPatientId = localStorage.getItem('patientId')

    if (savedPatientName) {
      username.value = savedPatientName
    } else if (savedUsername) {
      username.value = savedUsername
    } else {
      username.value = '患者'
    }

    if (!savedPatientId) {
      const patientRes = await getPatientList({ pageNum: 1, pageSize: 10 })
      if (patientRes.rows && patientRes.rows.length > 0) {
        const patient = patientRes.rows.find((p) => p.name === savedUsername) || patientRes.rows[0]
        localStorage.setItem('patientId', patient.patientId)
        localStorage.setItem('patientName', patient.name)
        username.value = patient.name
      }
    }

    const registerRes = await getRegisterList({ pageNum: 1, pageSize: 10 })
    if (registerRes.rows) {
      appointmentList.value = registerRes.rows
      todayAppointments.value = registerRes.rows.filter((r) => {
        const today = new Date().toDateString()
        const regDate = new Date(r.registerTime).toDateString()
        return today === regDate
      }).length
    }
  } catch (error) {
    console.error('加载用户数据失败', error)
  }
}

onMounted(() => {
  loadUserData()
})
</script>

<style scoped lang="scss">
.home-page {
  min-height: 100vh;
  background: var(--bg-gradient);
  color: var(--text-primary);
  padding-bottom: calc(78px + env(safe-area-inset-bottom));
  position: relative;
  overflow: hidden;
}

.ambient-layer {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}

.mesh {
  position: absolute;
  border-radius: 999px;
  filter: blur(3px);
  opacity: .72;
  animation: drift 9s ease-in-out infinite;
}

.mesh-a {
  width: 280px;
  height: 280px;
  top: -88px;
  right: -92px;
  background: radial-gradient(circle, rgba(185, 225, 205, .72), rgba(185, 225, 205, 0) 68%);
}

.mesh-b {
  width: 230px;
  height: 230px;
  left: -110px;
  top: 280px;
  background: radial-gradient(circle, rgba(142, 214, 242, .52), rgba(142, 214, 242, 0) 70%);
  animation-delay: -3s;
}

.mesh-c {
  width: 190px;
  height: 190px;
  right: -74px;
  bottom: 120px;
  background: radial-gradient(circle, rgba(255, 253, 248, .9), rgba(255, 253, 248, 0) 72%);
  animation-delay: -5s;
}

.grid-plane {
  position: absolute;
  inset: 0;
  opacity: .28;
  background-image:
    linear-gradient(rgba(26, 77, 69, .08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(26, 77, 69, .08) 1px, transparent 1px);
  background-size: 28px 28px;
  -webkit-mask-image: linear-gradient(to bottom, rgba(0, 0, 0, .8), transparent 74%);
  mask-image: linear-gradient(to bottom, rgba(0, 0, 0, .8), transparent 74%);
}

.content-wrapper {
  position: relative;
  z-index: 1;
  padding: 18px 16px 0;
}

.hero-section {
  min-height: 210px;
  border: 1px solid rgba(213, 237, 243, .74);
  border-radius: 18px;
  padding: 16px;
  position: relative;
  overflow: hidden;
  background:
    linear-gradient(145deg, rgba(255, 253, 248, .86), rgba(255, 253, 248, .42)),
    linear-gradient(120deg, rgba(185, 225, 205, .24), rgba(142, 214, 242, .16));
  box-shadow: 0 28px 70px rgba(102, 170, 189, .2);
  -webkit-backdrop-filter: blur(18px);
  backdrop-filter: blur(18px);

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(110deg, transparent 0%, rgba(255, 253, 248, .58) 45%, transparent 72%);
    transform: translateX(-110%);
    animation: sheen 5.8s ease-in-out infinite;
  }

  &::after {
    content: '';
    position: absolute;
    left: 18px;
    right: 18px;
    bottom: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba(26, 77, 69, .24), transparent);
  }
}

.hero-copy,
.hero-visual {
  position: relative;
  z-index: 1;
}

.top-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.eyebrow {
  margin: 0 0 6px;
  color: rgba(26, 77, 69, .74);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0;
}

.hero-copy h1 {
  margin: 0;
  color: var(--primary-color);
  font-size: 22px;
  line-height: 1.18;
  font-weight: 800;
}

.hero-desc {
  margin: 8px 0 0;
  color: var(--text-regular);
  font-size: 14px;
  line-height: 1.6;
}

.status-badge {
  flex: 0 0 auto;
  height: 34px;
  border: 1px solid rgba(185, 225, 205, .56);
  border-radius: 10px;
  padding: 0 12px;
  display: inline-flex;
  align-items: center;
  gap: 7px;
  background: rgba(255, 253, 248, .72);
  color: #5f9e8c;
  font-size: 12px;
  font-weight: 700;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, .6);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #82d7bf;
  box-shadow: 0 0 0 6px rgba(130, 215, 191, .16);
  animation: pulse 2s ease-in-out infinite;
}

.hero-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  margin-top: 14px;
}

.metric-card {
  min-height: 54px;
  border-radius: 12px;
  padding: 9px 10px;
  background: rgba(255, 253, 248, .54);
  border: 1px solid rgba(213, 237, 243, .64);
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 5px;
}

.metric-value {
  color: var(--primary-color);
  font-size: 19px;
  font-weight: 800;
  line-height: 1;
}

.metric-label {
  color: var(--text-light);
  font-size: 10px;
  font-weight: 700;
}

.hero-visual {
  height: 82px;
  margin-top: 4px;
}

.radar-ring {
  position: absolute;
  left: 50%;
  top: 54%;
  border: 1px solid rgba(26, 77, 69, .13);
  border-radius: 50%;
  transform: translate(-50%, -50%);
}

.ring-one {
  width: 188px;
  height: 58px;
  animation: radarBreath 4.4s ease-in-out infinite;
}

.ring-two {
  width: 246px;
  height: 76px;
  animation: radarBreath 4.4s ease-in-out infinite reverse;
}

.core-card {
  position: absolute;
  left: 50%;
  top: 50%;
  width: min(220px, 72vw);
  height: 58px;
  transform: translate(-50%, -50%);
  border-radius: 14px;
  background: rgba(255, 253, 248, .78);
  border: 1px solid rgba(213, 237, 243, .78);
  box-shadow: 0 20px 50px rgba(102, 170, 189, .18);
  display: grid;
  grid-template-columns: 42px 1fr;
  align-items: center;
  padding: 10px;
}

.cross-mark {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, rgba(185, 225, 205, .92), rgba(142, 214, 242, .52));
  position: relative;
  box-shadow: 0 12px 28px rgba(102, 170, 189, .18);

  span {
    position: absolute;
    left: 50%;
    top: 50%;
    width: 22px;
    height: 4px;
    border-radius: 999px;
    background: rgba(255, 253, 248, .96);
    transform: translate(-50%, -50%);
  }

  span:last-child {
    transform: translate(-50%, -50%) rotate(90deg);
  }
}

.pulse-track {
  svg {
    width: 100%;
    height: 32px;
    overflow: visible;
  }
}

.pulse-shadow,
.pulse-line {
  stroke-linecap: round;
  stroke-linejoin: round;
  stroke-width: 3;
}

.pulse-shadow {
  stroke: rgba(142, 214, 242, .22);
}

.pulse-line {
  stroke: #6fbacb;
  stroke-dasharray: 260;
  stroke-dashoffset: 260;
  animation: pulseDraw 3.2s ease-in-out infinite;
}

.float-chip {
  position: absolute;
  height: 24px;
  border-radius: 9px;
  padding: 0 10px;
  display: inline-flex;
  align-items: center;
  background: rgba(255, 253, 248, .78);
  border: 1px solid rgba(185, 225, 205, .42);
  color: rgba(26, 77, 69, .72);
  font-size: 11px;
  font-weight: 700;
  box-shadow: 0 12px 30px rgba(102, 170, 189, .12);
  animation: floatSoft 5s ease-in-out infinite;
}

.chip-a {
  left: 10px;
  top: 6px;
}

.chip-b {
  right: 18px;
  top: 0;
  animation-delay: -1.6s;
}

.chip-c {
  right: 54px;
  bottom: 2px;
  animation-delay: -2.7s;
}

.quick-actions,
.info-section,
.more-section {
  margin-top: 12px;
}

button {
  font-family: inherit;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.action-card {
  min-width: 0;
  min-height: 94px;
  border: 1px solid rgba(213, 237, 243, .76);
  border-radius: 14px;
  padding: 12px 10px;
  background: rgba(255, 253, 248, .72);
  box-shadow: 0 14px 32px rgba(102, 170, 189, .12);
  -webkit-backdrop-filter: blur(14px);
  backdrop-filter: blur(14px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--text-primary);
  transition: transform .22s ease, box-shadow .22s ease;

  &:active {
    transform: translateY(2px) scale(.98);
  }

  span:last-child {
    width: 100%;
    text-align: center;
    font-size: 14px;
    font-weight: 750;
    line-height: 1.25;
  }
}

.action-icon {
  width: 46px;
  height: 46px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  background: linear-gradient(145deg, rgba(185, 225, 205, .24), rgba(142, 214, 242, .12));
  border: 1px solid rgba(185, 225, 205, .22);
  position: relative;

  &::after {
    content: '';
    position: absolute;
    inset: -4px;
    border-radius: 13px;
    border: 1px solid rgba(185, 225, 205, .16);
    opacity: 0;
    animation: iconHalo 3.6s ease-in-out infinite;
  }

  img {
    width: 34px;
    height: 34px;
    object-fit: contain;
  }

  .van-icon {
    color: #6fbacb;
    font-size: 24px;
  }
}

.icon-register::after { animation-delay: -.3s; }
.icon-record::after { animation-delay: -.9s; }
.icon-appointment::after { animation-delay: -1.8s; }
.icon-report::after { animation-delay: -2.7s; }

.section-heading {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 12px;

  h2 {
    margin: 2px 0 0;
    color: var(--primary-color);
    font-size: 17px;
    line-height: 1.2;
  }

  button {
    border: 0;
    background: rgba(255, 253, 248, .54);
    border: 1px solid rgba(213, 237, 243, .64);
    border-radius: 8px;
    color: rgba(26, 77, 69, .62);
    font-size: 13px;
    font-weight: 700;
    padding: 5px 9px;
  }
}

.section-kicker {
  color: var(--text-light);
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0;
}

.info-card {
  position: relative;
  overflow: hidden;
  border-radius: 14px;
  padding: 16px;
  background: rgba(255, 253, 248, .76);
  border: 1px solid rgba(213, 237, 243, .72);
  box-shadow: 0 18px 44px rgba(102, 170, 189, .13);
  -webkit-backdrop-filter: blur(14px);
  backdrop-filter: blur(14px);
}

.timeline-glow {
  position: absolute;
  left: 30px;
  top: 20px;
  bottom: 20px;
  width: 2px;
  border-radius: 4px;
  background: linear-gradient(to bottom, rgba(185, 225, 205, .2), rgba(142, 214, 242, .6), rgba(185, 225, 205, .2));
}

.detail-item {
  position: relative;
  display: grid;
  grid-template-columns: 70px 1fr;
  gap: 12px;
  align-items: center;
  min-height: 64px;

  + .detail-item {
    border-top: 1px solid rgba(213, 237, 243, .45);
  }
}

.time-pill {
  justify-self: start;
  min-width: 58px;
  height: 30px;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: rgba(185, 225, 205, .18);
  color: #5f9e8c;
  font-size: 12px;
  font-weight: 800;
}

.detail-copy {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 5px;

  strong {
    color: var(--primary-color);
    font-size: 15px;
    line-height: 1.2;
  }

  span {
    color: var(--text-regular);
    font-size: 12px;
    line-height: 1.35;
  }
}

.empty-state {
  min-height: 76px;
  display: grid;
  grid-template-columns: 48px 1fr;
  gap: 12px;
  align-items: center;

  strong,
  span {
    display: block;
  }

  strong {
    color: var(--primary-color);
    font-size: 15px;
    margin-bottom: 5px;
  }

  span {
    color: var(--text-regular);
    font-size: 12px;
    line-height: 1.45;
  }
}

.empty-icon {
  width: 46px;
  height: 46px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  background: rgba(185, 225, 205, .16);
  color: #6fbacb;
  font-size: 24px;
}

.service-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.service-item {
  min-height: 72px;
  border: 1px solid rgba(213, 237, 243, .72);
  border-radius: 12px;
  padding: 13px;
  background: rgba(255, 253, 248, .7);
  box-shadow: 0 14px 30px rgba(102, 170, 189, .11);
  display: flex;
  align-items: center;
  gap: 11px;
  color: var(--text-primary);
  font-size: 13px;
  font-weight: 800;
  transition: transform .2s ease;

  &:active {
    transform: scale(.98);
  }
}

.service-icon {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  background: rgba(185, 225, 205, .16);
  color: #6fbacb;
  font-size: 22px;
  flex: 0 0 auto;
}

.slide-up-animation {
  animation: slideUp .58s cubic-bezier(.2, .78, .24, 1) both;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(18px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes drift {
  0%, 100% {
    transform: translate3d(0, 0, 0) scale(1);
  }
  50% {
    transform: translate3d(12px, -16px, 0) scale(1.04);
  }
}

@keyframes sheen {
  0%, 52% {
    transform: translateX(-115%);
  }
  76%, 100% {
    transform: translateX(115%);
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: .62;
    transform: scale(1.22);
  }
}

@keyframes radarBreath {
  0%, 100% {
    opacity: .42;
    transform: translate(-50%, -50%) scale(.97);
  }
  50% {
    opacity: .8;
    transform: translate(-50%, -50%) scale(1.04);
  }
}

@keyframes pulseDraw {
  0% {
    stroke-dashoffset: 260;
    opacity: .25;
  }
  45%, 75% {
    stroke-dashoffset: 0;
    opacity: 1;
  }
  100% {
    stroke-dashoffset: -260;
    opacity: .35;
  }
}

@keyframes floatSoft {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-8px);
  }
}

@keyframes iconHalo {
  0%, 100% {
    opacity: 0;
    transform: scale(.92);
  }
  45% {
    opacity: 1;
    transform: scale(1.08);
  }
}

@media (max-width: 360px) {
  .content-wrapper {
    padding-left: 12px;
    padding-right: 12px;
  }

  .hero-section {
    border-radius: 16px;
    padding: 14px;
  }

  .hero-copy h1 {
    font-size: 21px;
  }

  .quick-actions {
    gap: 8px;
  }

  .action-card {
    min-height: 88px;
  }

  .action-icon {
    width: 42px;
    height: 42px;

    img {
      width: 31px;
      height: 31px;
    }
  }
}
</style>
