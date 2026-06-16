<template>
  <div class="home-page">
    <div class="medical-visual-background">
      <svg class="medical-art" viewBox="0 0 640 560" role="img" aria-label="医疗主题插画">
        <defs>
          <linearGradient id="cloudGradient" x1="0" x2="1" y1="0" y2="1">
            <stop offset="0" stop-color="#f8fdff" />
            <stop offset="1" stop-color="#dff4fb" />
          </linearGradient>
          <linearGradient id="mintGradient" x1="0" x2="1" y1="0" y2="1">
            <stop offset="0" stop-color="#b9e1cd" />
            <stop offset="1" stop-color="#73d2b7" />
          </linearGradient>
          <linearGradient id="blueGradient" x1="0" x2="1" y1="0" y2="1">
            <stop offset="0" stop-color="#cfe8d8" />
            <stop offset="1" stop-color="#8ed6f2" />
          </linearGradient>
          <filter id="softShadow" x="-20%" y="-20%" width="140%" height="140%">
            <feDropShadow dx="0" dy="18" stdDeviation="18" flood-color="#8cc9dd" flood-opacity=".22" />
          </filter>
        </defs>

        <g class="wave-lines" fill="none" stroke="#ccecf5" stroke-linecap="round" stroke-width="3">
          <path d="M34 380 C 148 332, 230 430, 330 374 S 522 328, 608 390" />
          <path d="M60 418 C 176 370, 238 448, 342 406 S 510 380, 586 432" />
          <path d="M80 456 C 188 422, 250 470, 360 440 S 502 420, 566 466" />
        </g>

        <g class="cloud cloud-a" fill="url(#cloudGradient)">
          <path d="M83 150c5-29 30-50 60-50 23 0 43 13 53 32 8-4 18-6 28-6 35 0 63 28 63 62H92c-30 0-54-24-54-54s24-54 54-54c2 0 4 0 6 1-7 8-12 18-15 29Z" />
        </g>
        <g class="cloud cloud-b" fill="url(#cloudGradient)">
          <path d="M410 96c6-22 26-38 50-38 19 0 36 10 45 26 7-3 14-4 22-4 30 0 54 24 54 54H396c-24 0-44-20-44-44s20-44 44-44c8 0 15 2 21 5-3 5-5 10-7 15Z" />
        </g>

        <g class="floating icon-card form-card" filter="url(#softShadow)">
          <rect x="82" y="225" width="112" height="130" rx="24" fill="#fffaf4" />
          <rect x="112" y="248" width="52" height="14" rx="7" fill="#acdbe9" />
          <rect x="108" y="282" width="68" height="10" rx="5" fill="#d9edf2" />
          <rect x="108" y="306" width="58" height="10" rx="5" fill="#d9edf2" />
          <path d="M126 334h28" stroke="#82d7bf" stroke-width="12" stroke-linecap="round" />
        </g>

        <g class="floating icon-card med-kit" filter="url(#softShadow)">
          <rect x="438" y="180" width="120" height="96" rx="22" fill="#fff7f1" />
          <path d="M472 180v-22c0-8 7-15 15-15h22c8 0 15 7 15 15v22" fill="none" stroke="#b9e1cd" stroke-width="10" stroke-linecap="round" />
          <path d="M498 206v46M475 229h46" stroke="#f0a5b8" stroke-width="12" stroke-linecap="round" />
        </g>

        <g class="floating stethoscope" fill="none" stroke="#6fbacb" stroke-linecap="round" stroke-width="10">
          <path d="M402 338v48c0 38-28 70-64 70s-64-32-64-70v-28" />
          <path d="M248 338v38c0 24-18 44-40 44s-40-20-40-44v-38" />
          <circle cx="402" cy="332" r="14" fill="#fffaf4" />
        </g>

        <g class="heart-pulse" fill="none" stroke="#ef8fa9" stroke-linecap="round" stroke-linejoin="round" stroke-width="8">
          <path d="M306 140c-22-25-62-9-62 26 0 35 62 70 62 70s62-35 62-70c0-35-40-51-62-26Z" fill="#fff5f7" />
          <path class="pulse-line" d="M252 182h35l12-24 20 48 14-28h39" />
        </g>

        <g class="pill pill-a">
          <rect x="210" y="130" width="86" height="34" rx="17" fill="#fffaf4" transform="rotate(-18 253 147)" />
          <path d="M253 130v34" stroke="#a9e5d2" stroke-width="4" transform="rotate(-18 253 147)" />
        </g>
        <g class="pill pill-b">
          <rect x="484" y="360" width="76" height="30" rx="15" fill="#e6f6fb" transform="rotate(23 522 375)" />
          <path d="M522 360v30" stroke="#efb2c1" stroke-width="4" transform="rotate(23 522 375)" />
        </g>

        <g class="hospital" filter="url(#softShadow)">
          <path d="M146 494h388c18 0 32-14 32-32v-24H114v24c0 18 14 32 32 32Z" fill="#d8eef4" />
          <rect x="174" y="284" width="292" height="210" rx="26" fill="#fffdf8" />
          <rect x="238" y="224" width="164" height="270" rx="28" fill="#f6fbfd" />
          <rect x="274" y="254" width="92" height="68" rx="18" fill="url(#blueGradient)" />
          <path d="M320 268v38M301 287h38" stroke="#fffdf8" stroke-width="12" stroke-linecap="round" />
          <g fill="#b9e8f4">
            <rect x="208" y="326" width="42" height="38" rx="10" />
            <rect x="208" y="386" width="42" height="38" rx="10" />
            <rect x="410" y="326" width="42" height="38" rx="10" />
            <rect x="410" y="386" width="42" height="38" rx="10" />
          </g>
          <rect x="290" y="382" width="80" height="112" rx="18" fill="url(#mintGradient)" />
          <path d="M330 382v112" stroke="#ecfff8" stroke-width="5" />
        </g>

        <g class="plant plant-left">
          <path d="M118 494c0-46 18-86 56-118" fill="none" stroke="#8bc9ae" stroke-width="8" stroke-linecap="round" />
          <path d="M138 444c-28-6-44-24-48-52 30 1 52 18 60 48Z" fill="#b9e1cd" />
          <path d="M157 405c5-27 24-45 56-54-2 33-21 56-52 66Z" fill="#9bd5bc" />
        </g>
        <g class="plant plant-right">
          <path d="M550 494c-4-42-22-74-54-98" fill="none" stroke="#8bc9ae" stroke-width="8" stroke-linecap="round" />
          <path d="M522 434c24-10 42-27 52-52-30-2-55 13-66 42Z" fill="#b9e1cd" />
        </g>

        <g class="tiny-crosses" fill="none" stroke="#b9e1cd" stroke-linecap="round" stroke-width="5">
          <path d="M132 92v24M120 104h24" />
          <path d="M516 150v22M505 161h22" />
          <path d="M76 405v18M67 414h18" />
          <path d="M564 448v18M555 457h18" />
        </g>
      </svg>
    </div>

    <div class="content-wrapper">
      <div class="header-section slide-up-animation">
          <div class="greeting">
            <h1>早上好，{{ username || '患者' }}</h1>
            <p>今天是个就诊的好日子</p>
          </div>
          <div class="status-badge">
            <span class="status-dot"></span>
            <span>在线</span>
          </div>
        </div>

      <div class="quick-actions slide-up-animation" style="animation-delay: 0.08s">
        <div class="action-card" @click="goToRegister">
          <div class="action-icon icon-register">
            <img src="@/assets/images/guahao.png" alt="挂号" />
          </div>
          <span>预约挂号</span>
        </div>
        <div class="action-card" @click="goToDoctors">
          <div class="action-icon icon-doctor">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="12" cy="8" r="5" stroke="#b9e1cd" stroke-width="2"/>
              <path d="M20 21a8 8 0 0 0-16 0" stroke="#b9e1cd" stroke-width="2" stroke-linecap="round"/>
              <path d="M18 4h-1v-1h-2v1h-1v2h1v1h2v-1h1v-2z" fill="#b9e1cd" opacity="0.6"/>
            </svg>
          </div>
          <span>科室医生</span>
        </div>
        <div class="action-card" @click="goToRecord">
          <div class="action-icon icon-record">
            <img src="@/assets/images/bingli.png" alt="病历" />
          </div>
          <span>病历查询</span>
        </div>
        <div class="action-card" @click="showToast('我的预约')">
          <div class="action-icon icon-appointment">
            <img src="@/assets/images/yuyue.png" alt="预约" />
          </div>
          <span>我的预约</span>
        </div>
        <div class="action-card" @click="showToast('健康报告')">
          <div class="action-icon icon-report">
            <img src="@/assets/images/baogao.png" alt="报告" />
          </div>
          <span>健康报告</span>
        </div>
      </div>

      <div class="info-section slide-up-animation" style="animation-delay: 0.16s">
          <div class="info-card">
            <div class="info-header">
              <span class="info-title">今日就诊</span>
              <span class="info-count">{{ todayAppointments }}</span>
            </div>
            <div class="info-detail">
              <div v-if="appointmentList.length > 0" v-for="(appt, index) in appointmentList.slice(0, 2)" :key="appt.registerId" class="detail-item">
                <span class="detail-label">{{ new Date(appt.registerTime).toLocaleTimeString('zh-CN', {hour: '2-digit', minute: '2-digit'}) }}</span>
                <span class="detail-text">{{ appt.deptId ? '科室' : '内科' }} · {{ appt.doctorId ? '医生' : '医生' }}</span>
              </div>
              <div v-else class="detail-item">
                <span class="detail-label">-</span>
                <span class="detail-text">暂无预约</span>
              </div>
            </div>
          </div>
        </div>

      <div class="more-section slide-up-animation" style="animation-delay: 0.24s">
        <div class="section-title">
          <span>更多服务</span>
          <span class="more-link" @click="showToast('查看全部')">查看全部</span>
        </div>
        <div class="service-list">
          <div class="service-item" @click="showToast('健康资讯')">
            <div class="service-icon">
              <svg viewBox="0 0 24 24" fill="none">
                <path d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10l4 4v10a2 2 0 01-2 2z" stroke="#b9e1cd" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M12 10v4M10 12h4" stroke="#b9e1cd" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <span>健康资讯</span>
          </div>
          <div class="service-item" @click="goToChat">
            <div class="service-icon">
              <svg viewBox="0 0 24 24" fill="none">
                <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" stroke="#b9e1cd" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M12 8v4M10 10h4" stroke="#b9e1cd" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <span>在线问诊</span>
          </div>
          <div class="service-item" @click="showToast('检查预约')">
            <div class="service-icon">
              <svg viewBox="0 0 24 24" fill="none">
                <path d="M9 12h6M12 9v6M19 3H5a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2V5a2 2 0 00-2-2z" stroke="#b9e1cd" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <span>检查预约</span>
          </div>
          <div class="service-item" @click="showToast('个人中心')">
            <div class="service-icon">
              <svg viewBox="0 0 24 24" fill="none">
                <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2M12 11a4 4 0 100-8 4 4 0 000 8z" stroke="#b9e1cd" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <span>个人中心</span>
          </div>
        </div>
      </div>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getPatientList } from '@/api/patient'
import { getRegisterList } from '@/api/register'

const router = useRouter()
const active = ref(0)
const username = ref('')
const todayAppointments = ref(0)
const appointmentList = ref([])

const loadUserData = async () => {
  try {
    // 优先从localStorage获取
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
    
    // 尝试获取患者列表（如果没有patientId）
    if (!savedPatientId) {
      const patientRes = await getPatientList({ pageNum: 1, pageSize: 10 })
      if (patientRes.rows && patientRes.rows.length > 0) {
        const patient = patientRes.rows.find(p => p.name === savedUsername) || patientRes.rows[0]
        localStorage.setItem('patientId', patient.patientId)
        localStorage.setItem('patientName', patient.name)
        username.value = patient.name
      }
    }
    
    // 加载预约列表
    const registerRes = await getRegisterList({ pageNum: 1, pageSize: 10 })
    if (registerRes.rows) {
      appointmentList.value = registerRes.rows
      todayAppointments.value = registerRes.rows.filter(r => {
        const today = new Date().toDateString()
        const regDate = new Date(r.registerTime).toDateString()
        return today === regDate
      }).length
    }
  } catch (error) {
    console.error('加载用户数据失败', error)
  }
}

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
  // 清除旧的 AI 会话数据
  localStorage.removeItem('aiChatSessionId')
  // 清除旧的 patientId（如果需要）
  // localStorage.removeItem('patientId')
  router.push('/chat')
}

onMounted(() => {
  loadUserData()
})
</script>

<style scoped lang="scss">
.home-page {
  min-height: 100vh;
  background: var(--bg-gradient);
  padding-bottom: 60px;
  position: relative;
  overflow: hidden;
}

.medical-visual-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
  pointer-events: none;
}

.medical-art {
  position: absolute;
  top: -100px;
  left: -50px;
  width: 150%;
  height: 150%;
  opacity: 0.18;
}

.content-wrapper {
  position: relative;
  z-index: 1;
  padding: 20px 16px 0;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px 0 24px;
}

.greeting {
  h1 {
    margin: 0;
    font-size: 24px;
    font-weight: 700;
    color: var(--text-primary);
  }

  p {
    margin: 6px 0 0;
    font-size: 14px;
    color: var(--text-regular);
  }
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: rgba(255, 253, 248, 0.85);
  border-radius: 20px;
  border: 1px solid rgba(185, 225, 205, 0.3);
  backdrop-filter: blur(10px);

  .status-dot {
    width: 8px;
    height: 8px;
    background: #82d7bf;
    border-radius: 50%;
    animation: pulse 2s ease-in-out infinite;
  }

  span:last-child {
    font-size: 13px;
    font-weight: 600;
    color: #5f9e8c;
  }
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.action-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 16px;
  border-radius: 20px;
  background: rgba(255, 253, 248, 0.85);
  border: 1px solid rgba(213, 237, 243, 0.5);
  backdrop-filter: blur(12px);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 16px 40px rgba(102, 170, 189, 0.15);
    background: rgba(255, 253, 248, 0.95);
  }

  &:active {
    transform: translateY(-2px);
  }

  span {
    margin-top: 14px;
    font-size: 15px;
    font-weight: 600;
    color: var(--text-primary);
  }
}

.action-icon {
  width: 40px;
  height: 40px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s ease;
  background: transparent;

  img {
    width: 40px;
    height: 40px;
    object-fit: contain;
    transition: transform 0.3s ease;
  }
}

.action-card:hover .action-icon {
  transform: rotate(-6deg) scale(1.05);
}

.action-card:hover .action-icon img {
  transform: scale(1.1);
}

.action-card:hover .action-icon svg {
  transform: scale(1.1);
}

.icon-doctor svg {
  width: 36px;
  height: 36px;
  transition: transform 0.3s ease;
}

.info-section {
  margin-bottom: 20px;

  .info-card {
    background: rgba(255, 253, 248, 0.85);
    border: 1px solid rgba(213, 237, 243, 0.5);
    backdrop-filter: blur(12px);
    border-radius: 20px;
    padding: 20px;
  }

  .info-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    .info-title {
      font-size: 16px;
      font-weight: 700;
      color: var(--text-primary);
    }

    .info-count {
      font-size: 24px;
      font-weight: 700;
      color: #b9e1cd;
    }
  }

  .info-detail {
    .detail-item {
      display: flex;
      gap: 12px;
      padding: 12px 0;

      &:not(:last-child) {
        border-bottom: 1px solid rgba(213, 237, 243, 0.4);
      }

      .detail-label {
        font-size: 14px;
        font-weight: 600;
        color: #8ed6f2;
        min-width: 60px;
      }

      .detail-text {
        font-size: 14px;
        color: var(--text-regular);
      }
    }
  }
}

.more-section {
  .section-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    span:first-child {
      font-size: 16px;
      font-weight: 700;
      color: var(--text-primary);
    }

    .more-link {
      font-size: 14px;
      color: #b9e1cd;
      font-weight: 500;
      cursor: pointer;
    }
  }

  .service-list {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
    background: rgba(255, 253, 248, 0.85);
    border: 1px solid rgba(213, 237, 243, 0.5);
    backdrop-filter: blur(12px);
    border-radius: 20px;
    padding: 20px;

    .service-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 10px;
      cursor: pointer;
      transition: transform 0.2s ease;

      &:active {
        transform: scale(0.95);
      }

      .service-icon {
        width: 48px;
        height: 48px;
        border-radius: 14px;
        background: rgba(185, 225, 205, 0.08);
        display: flex;
        align-items: center;
        justify-content: center;

        svg {
          width: 24px;
          height: 24px;
        }
      }

      span {
        font-size: 13px;
        color: var(--text-primary);
        font-weight: 500;
        text-align: center;
      }
    }
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.6;
    transform: scale(1.2);
  }
}

.wave-lines {
  animation: waveMove 9s ease-in-out infinite;
}

.cloud {
  opacity: .88;
  animation: cloudDrift 9s ease-in-out infinite;
}

.cloud-b {
  animation-delay: -3s;
}

.floating,
.pill,
.plant {
  animation: floatSoft 5.8s ease-in-out infinite;
}

.form-card { animation-delay: -.8s; }
.med-kit { animation-delay: -1.4s; }
.stethoscope { animation-delay: -2s; }
.pill-a { animation-delay: -2.8s; }
.pill-b { animation-delay: -1.8s; }
.plant-left { animation-delay: -1.2s; }
.plant-right { animation-delay: -2.4s; }

.heart-pulse {
  animation: heartbeat 3.4s ease-in-out infinite;
  transform-origin: 306px 214px;
}

.pulse-line {
  stroke-dasharray: 190;
  stroke-dashoffset: 190;
  animation: pulseDraw 3.4s ease-in-out infinite;
}

.tiny-crosses {
  animation: twinkle 4.8s ease-in-out infinite;
}

@keyframes waveMove {
  0%, 100% { transform: translateX(-8px) translateY(0); opacity: .48; }
  50% { transform: translateX(12px) translateY(-8px); opacity: .76; }
}

@keyframes cloudDrift {
  0%, 100% { transform: translateX(0); }
  50% { transform: translateX(18px); }
}

@keyframes floatSoft {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-12px); }
}

@keyframes heartbeat {
  0%, 100% { transform: scale(1); }
  45% { transform: scale(1.035); }
  60% { transform: scale(.99); }
}

@keyframes pulseDraw {
  0% { stroke-dashoffset: 190; opacity: .2; }
  45%, 75% { stroke-dashoffset: 0; opacity: 1; }
  100% { stroke-dashoffset: -190; opacity: .25; }
}

@keyframes twinkle {
  0%, 100% { opacity: .38; }
  50% { opacity: .88; }
}

.slide-up-animation {
  animation: slideUp 0.5s ease-out forwards;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
