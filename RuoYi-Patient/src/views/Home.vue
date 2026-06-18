<template>
  <div class="home-page">
    <!-- ========== 模块1：全屏轮播 Banner ========== -->
    <div class="banner-area">
      <van-swipe
        class="banner-swipe"
        :autoplay="4000"
        :duration="600"
        indicator-color="rgba(255,255,255,.7)"
        indicator-active-color="#5f9e8c"
        :show-indicators="true"
        @change="onBannerChange"
      >
        <van-swipe-item v-for="(slide, idx) in bannerSlides" :key="idx">
          <div class="banner-slide" :style="{ background: slide.bgGradient }">
            <!-- 底纹：心电图 + 十字 -->
            <div class="banner-pattern" aria-hidden="true">
              <svg class="ecg-wave" viewBox="0 0 400 120" fill="none">
                <path d="M0 60H80L95 20L130 100L155 40H200L220 80L245 20L270 60H400" :stroke="slide.accentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" opacity="0.25"/>
                <path d="M0 72H80L95 32L130 112L155 52H200L220 92L245 32L270 72H400" :stroke="slide.accentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" opacity="0.12"/>
              </svg>
              <svg class="ecg-bg" viewBox="0 0 400 200" fill="none">
                <circle cx="60" cy="50" r="30" :stroke="slide.accentColor" stroke-width="1" opacity="0.08"/>
                <circle cx="340" cy="150" r="40" :stroke="slide.accentColor" stroke-width="1" opacity="0.06"/>
              </svg>
              <!-- 爱心底纹 -->
              <span class="banner-heart" v-for="h in 5" :key="'h'+h" :style="heartStyle(h, slide)">♥</span>
              <!-- 十字底纹 -->
              <span class="banner-cross" v-for="c in 4" :key="'c'+c" :style="crossStyle(c, slide)">+</span>
            </div>

            <!-- 左侧文案区 -->
            <div class="banner-text">
              <h1 class="banner-title">{{ slide.title }}</h1>
              <div class="banner-metrics">
                <div class="banner-metric">
                  <span class="bm-number">{{ slide.data1 }}</span>
                  <span class="bm-label">{{ slide.data1Label }}</span>
                </div>
                <div class="banner-divider"></div>
                <div class="banner-metric">
                  <span class="bm-number">{{ slide.data2 }}</span>
                  <span class="bm-label">{{ slide.data2Label }}</span>
                </div>
              </div>
              <button class="banner-cta" :style="{ background: slide.btnBg, color: slide.btnColor }" @click="slide.action">
                {{ slide.btnText }}
                <svg viewBox="0 0 16 16" fill="currentColor" width="14" height="14">
                  <path d="M6 4l4 4-4 4" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round"/>
                </svg>
              </button>
            </div>

            <!-- 右侧人物图 -->
            <div class="banner-figure">
              <img :src="slide.figure" :class="['banner-person', slide.figureClass]" alt="医生" />
              <!-- 人物光环 -->
              <span class="figure-glow" :style="{ background: slide.glowColor }"></span>
            </div>

          </div>
        </van-swipe-item>
      </van-swipe>
    </div>

    <!-- ========== 模块2：核心功能双卡片入口区 ========== -->
    <div class="dual-cards">
      <!-- 预约挂号卡片 -->
      <div class="feature-card card-register" @click="goToRegister">
        <div class="fc-body">
          <span class="fc-icon">
            <svg viewBox="0 0 40 40" fill="none">
              <rect x="6" y="6" width="28" height="28" rx="6" stroke="#e89860" stroke-width="2.5"/>
              <path d="M14 14h12M14 20h12M14 26h8" stroke="#e89860" stroke-width="2.5" stroke-linecap="round"/>
              <circle cx="28" cy="28" r="8" fill="#fef0e8" stroke="#e89860" stroke-width="2"/>
              <path d="M26 28h4M28 26v4" stroke="#e89860" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </span>
          <div class="fc-info">
            <div class="fc-title-row">
              <strong>预约挂号</strong>
              <span class="fc-tag tag-orange">互联网医院</span>
            </div>
            <span class="fc-desc">本院接诊范围</span>
          </div>
          <span class="fc-arrow">
            <svg viewBox="0 0 16 16" fill="none" width="16" height="16">
              <path d="M6 4l4 4-4 4" stroke="#e89860" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </span>
        </div>
      </div>

      <!-- 在线问诊卡片 -->
      <div class="feature-card card-consult" @click="goToChat">
        <div class="fc-body">
          <span class="fc-icon">
            <svg viewBox="0 0 40 40" fill="none">
              <circle cx="20" cy="20" r="14" stroke="#5db8d8" stroke-width="2.5"/>
              <path d="M14 16c0-3.3 2.7-6 6-6s6 2.7 6 6c0 4-6 10-6 10s-6-6-6-10z" stroke="#5db8d8" stroke-width="2.5" stroke-linejoin="round"/>
              <circle cx="20" cy="16" r="3" fill="#5db8d8"/>
            </svg>
          </span>
          <div class="fc-info">
            <div class="fc-title-row">
              <strong>在线问诊</strong>
              <span class="fc-tag tag-blue">急速</span>
            </div>
            <span class="fc-desc">AI 智能响应</span>
          </div>
          <span class="fc-arrow">
            <svg viewBox="0 0 16 16" fill="none" width="16" height="16">
              <path d="M6 4l4 4-4 4" stroke="#5db8d8" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </span>
        </div>
      </div>
    </div>

    <!-- ========== 模块3：今日就诊 + 更多服务（保留） ========== -->
    <div class="content-wrapper">
      <section class="info-section slide-up-animation">
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

      <section class="more-section slide-up-animation" style="animation-delay: 0.18s">
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
import doctor1 from '@/assets/doctor1.png'
import doctor2 from '@/assets/doctor2.png'
import nurse1 from '@/assets/nurse1.png'

const router = useRouter()
const active = ref(0)
const currentBanner = ref(0)
const username = ref('')
const todayAppointments = ref(0)
const appointmentList = ref([])
const showAllServices = ref(false)

const displayName = computed(() => username.value || '患者')
const visibleServiceItems = computed(() => (showAllServices.value ? serviceItems : serviceItems.slice(0, 4)))

// Banner 轮播数据
const bannerSlides = [
  {
    title: '预约挂号不用等',
    data1: '2000+',
    data1Label: '三甲医院',
    data2: '100万+',
    data2Label: '名医',
    btnText: '立即预约',
    figure: doctor1,
    figureClass: '',
    bgGradient: 'linear-gradient(155deg, #e8f5f0 0%, #d4ece2 35%, #c9e8da 68%, #dfefe6 100%)',
    accentColor: '#5f9e8c',
    btnBg: '#e8f5f0',
    btnColor: '#4a8a7a',
    glowColor: 'radial-gradient(circle, rgba(120,200,170,.35) 0%, transparent 70%)',
    action: () => router.push('/register')
  },
  {
    title: '在线问诊更便捷',
    data1: 'AI',
    data1Label: '智能导诊',
    data2: '24h',
    data2Label: '在线医生',
    btnText: '在线问诊',
    figure: doctor2,
    figureClass: '',
    bgGradient: 'linear-gradient(155deg, #e0eef8 0%, #cde4f2 35%, #d8ebf6 68%, #e8f2f8 100%)',
    accentColor: '#5db8d8',
    btnBg: '#e0eef8',
    btnColor: '#3a809b',
    glowColor: 'radial-gradient(circle, rgba(93,184,216,.3) 0%, transparent 70%)',
    action: () => router.push('/chat')
  },
  {
    title: '健康档案随时看',
    data1: '电子',
    data1Label: '病历报告',
    data2: '一键',
    data2Label: '查阅下载',
    btnText: '查看报告',
    figure: nurse1,
    figureClass: '',
    bgGradient: 'linear-gradient(155deg, #f0f4e8 0%, #e4ecd5 35%, #d8e5c9 68%, #eaf0e0 100%)',
    accentColor: '#8a9e6b',
    btnBg: '#f0f4e8',
    btnColor: '#6b7d4e',
    glowColor: 'radial-gradient(circle, rgba(138,158,107,.3) 0%, transparent 70%)',
    action: () => router.push('/record')
  }
]

function onBannerChange(index) {
  currentBanner.value = index
}

function goToRegister() {
  router.push('/register')
}

function goToDoctors() {
  router.push('/doctors')
}

function goToRecord() {
  router.push('/record')
}

function goToChat() {
  localStorage.removeItem('aiChatSessionId')
  router.push('/chat')
}

function goToMyAppointments() {
  router.push('/my-appointments')
}

const goToPayment = () => {
  router.push('/my-payment')
}

const toggleServices = () => {
  showAllServices.value = !showAllServices.value
}

// 爱心位置样式
function heartStyle(index, slide) {
  const positions = [
    { top: '12%', left: '42%' },
    { top: '25%', left: '55%' },
    { top: '58%', left: '38%' },
    { top: '72%', left: '52%' },
    { top: '35%', left: '62%' }
  ]
  const p = positions[index - 1]
  return {
    top: p.top,
    left: p.left,
    color: slide.accentColor,
    opacity: (0.06 + index * 0.015)
  }
}

// 十字位置样式
function crossStyle(index, slide) {
  const positions = [
    { top: '15%', left: '30%' },
    { top: '45%', left: '25%' },
    { top: '68%', left: '44%' },
    { top: '82%', left: '35%' }
  ]
  const p = positions[index - 1]
  return {
    top: p.top,
    left: p.left,
    color: slide.accentColor,
    opacity: (0.04 + index * 0.01)
  }
}

const serviceItems = [
  { title: '医院资源', icon: 'manager-o', action: goToDoctors },
  { title: '我的缴费', icon: 'balance-list-o', action: goToPayment },
  { title: '我的预约', icon: 'calendar-o', action: goToMyAppointments },
  { title: '检查预约', icon: 'records-o', action: () => showToast('检查预约功能建设中') },
  { title: '在线问诊', icon: 'chat-o', action: goToChat },
  { title: '健康资讯', icon: 'newspaper-o', action: () => showToast('健康资讯') },
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
$mint-dark: #5f9e8c;
$mint-pale: #e8f5f0;
$sky-blue: #5db8d8;
$sky-pale: #e0eef8;
$orange-warm: #e89860;

.home-page {
  min-height: 100vh;
  background: var(--bg-gradient);
  color: var(--text-primary);
  padding-bottom: calc(78px + env(safe-area-inset-bottom));
  position: relative;
  overflow-x: hidden;
}

// ========== 模块1：Banner 轮播区 ==========
.banner-area {
  width: 100%;
  border-radius: 0;
  overflow: hidden;
}

.banner-swipe {
  width: 100%;
  height: 175px;

  :deep(.van-swipe__indicators) {
    bottom: 10px;
  }

  :deep(.van-swipe__indicator) {
    width: 6px;
    height: 6px;
    border-radius: 0;
    transition: all .3s ease;
  }

  :deep(.van-swipe__indicator--active) {
    width: 18px;
    border-radius: 0;
  }
}

.banner-slide {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
  padding: 20px 16px 16px;
  display: flex;
  align-items: center;
}

.banner-pattern {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.ecg-wave {
  position: absolute;
  top: 28%;
  left: -2%;
  width: 70%;
  height: auto;
  opacity: .5;
}

.ecg-bg {
  position: absolute;
  bottom: 5%;
  right: 50%;
  width: 60%;
  height: auto;
  opacity: .4;
}

.banner-heart {
  position: absolute;
  font-size: 22px;
  transform: rotate(-8deg);
}

.banner-cross {
  position: absolute;
  font-size: 16px;
  font-weight: 300;
}

// 左侧文案
.banner-text {
  position: relative;
  z-index: 2;
  flex: 1;
  padding-right: 10px;
}

.banner-title {
  margin: 0 0 10px;
  font-size: 24px;
  font-weight: 800;
  color: #2d5a4e;
  line-height: 1.2;
}

.banner-metrics {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.banner-metric {
  display: flex;
  flex-direction: column;
}

.bm-number {
  font-size: 15px;
  font-weight: 800;
  color: $mint-dark;
  line-height: 1;
}

.bm-label {
  margin-top: 3px;
  font-size: 11px;
  color: rgba(45, 90, 78, .6);
  font-weight: 600;
}

.banner-divider {
  width: 1px;
  height: 24px;
  background: rgba(45, 90, 78, .15);
  border-radius: 1px;
}

.banner-cta {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 9px 20px;
  border: none;
  border-radius: 0;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 4px 18px rgba(95, 158, 140, .14);
  transition: transform .2s ease, box-shadow .2s ease;

  &:active {
    transform: scale(.96);
    box-shadow: 0 2px 8px rgba(95, 158, 140, .1);
  }
}

// 右侧人物
.banner-figure {
  position: relative;
  z-index: 2;
  flex: 0 0 40%;
  height: 100%;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.banner-person {
  width: 100%;
  height: auto;
  max-height: 160px;
  object-fit: contain;
  object-position: bottom center;
  filter: drop-shadow(0 8px 24px rgba(80,120,100,.18));
}

.figure-glow {
  position: absolute;
  bottom: 5%;
  left: 50%;
  transform: translateX(-50%);
  width: 140px;
  height: 45px;
  border-radius: 50%;
  opacity: .5;
}

// ========== 模块2：核心功能双卡片 ==========
.dual-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  padding: 16px 16px 0;
}

.feature-card {
  border-radius: 16px;
  padding: 20px 16px;
  min-height: 80px;
  background: #fff;
  box-shadow: 0 4px 24px rgba(102, 170, 189, .1);
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: transform .22s ease, box-shadow .22s ease;

  &:active {
    transform: scale(.97);
    box-shadow: 0 2px 12px rgba(102, 170, 189, .08);
  }
}

.card-register { border: 1px solid rgba(232, 152, 96, .18); }
.card-consult  { border: 1px solid rgba(93, 184, 216, .18); }

.fc-body {
  display: flex;
  align-items: center;
  gap: 12px;
}

.fc-icon {
  flex-shrink: 0;
  width: 46px;
  height: 46px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  background: rgba(248, 245, 240, .8);

  svg {
    width: 38px;
    height: 38px;
  }
}

.fc-info {
  flex: 1;
  min-width: 0;
}

.fc-title-row {
  display: flex;
  align-items: center;
  gap: 8px;

  strong {
    font-size: 16px;
    font-weight: 800;
    color: #2d5a4e;
    line-height: 1.2;
    white-space: nowrap;
  }
}

.fc-desc {
  display: block;
  margin-top: 8px;
  font-size: 12px;
  color: rgba(45, 90, 78, .5);
  font-weight: 500;
}

.fc-tag {
  flex-shrink: 0;
  padding: 2px 8px;
  border-radius: 8px;
  font-size: 9px;
  font-weight: 700;
  line-height: 1.5;
}

.tag-orange { background: rgba(232, 152, 96, .12); color: $orange-warm; }
.tag-blue   { background: rgba(93, 184, 216, .12); color: $sky-blue; }

.fc-arrow {
  flex-shrink: 0;
  padding-top: 10px;
}

// ========== 保留模块 ==========
.content-wrapper {
  padding: 0 16px;
}

.info-section,
.more-section {
  margin-top: 12px;
}

button {
  font-family: inherit;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
}

.section-heading {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 12px;

  h2 {
    margin: 2px 0 0;
    color: #2d5a4e;
    font-size: 17px;
    line-height: 1.2;
    font-weight: 800;
  }

  button {
    border: 0;
    background: rgba(255, 253, 248, .54);
    border: 1px solid rgba(213, 237, 243, .64);
    border-radius: 8px;
    color: rgba(45, 90, 78, .55);
    font-size: 13px;
    font-weight: 700;
    padding: 5px 9px;
  }
}

.section-kicker {
  color: var(--text-light);
  font-size: 10px;
  font-weight: 800;
}

.info-card {
  position: relative;
  overflow: hidden;
  border-radius: 14px;
  padding: 16px;
  background: rgba(255, 253, 248, .76);
  border: 1px solid rgba(213, 237, 243, .72);
  box-shadow: 0 18px 44px rgba(102, 170, 189, .13);
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
    color: #2d5a4e;
    font-size: 15px;
    line-height: 1.2;
    font-weight: 700;
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

  strong, span { display: block; }

  strong {
    color: #2d5a4e;
    font-size: 15px;
    margin-bottom: 5px;
    font-weight: 700;
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
  gap: 12px;
}

.service-item {
  min-width: 0;
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

  > span:last-child {
    flex: 1;
    min-width: 0;
    line-height: 1.25;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
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
  from { opacity: 0; transform: translateY(18px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
