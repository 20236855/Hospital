<template>
  <div class="his-home">
    <!-- 固定视频背景 -->
    <video class="home-video-bg" autoplay loop muted playsinline>
      <source src="/running.mp4" type="video/mp4" />
    </video>
    <div class="home-video-overlay"></div>

    <!-- Hero Section -->
    <section class="home-hero">
      <div class="hero-content">
        <h2 class="hero-heading">
          <span v-for="(word, i) in heroWords" :key="i"
            class="hero-word" :style="{ animationDelay: (0.15 + i * 0.08) + 's' }">{{ word }}</span>
        </h2>
        <p class="hero-sub">智慧医疗平台 — 用心守护每一次就医</p>
        <div class="hero-scroll-hint"><span></span></div>
      </div>
    </section>

    <!-- 医疗资源展示区 -->
    <section class="home-resources">
      <div class="resources-stats">
        <div class="stat-card" v-for="s in statsData" :key="s.label" :style="{ '--delay': s.delay }">
          <div class="stat-icon" :style="{ background: s.color }" v-html="s.svg"></div>
          <div class="stat-body">
            <strong>{{ s.value }}</strong>
            <span>{{ s.label }}</span>
          </div>
        </div>
      </div>

      <div class="resources-grid">
        <!-- 重点科室 -->
        <div class="res-card dept-card">
          <h3><i v-html="svgDept"></i>重点科室</h3>
          <div class="dept-list">
            <div class="dept-row" v-for="(d, idx) in departments" :key="d.name">
              <svg class="dept-dot" viewBox="0 0 12 12"><circle cx="6" cy="6" r="4" :fill="deptColors[idx]"/></svg>
              <span class="dept-name">{{ d.name }}</span>
              <span class="dept-info">{{ d.doctors }}位专家 · {{ d.beds }}张床位</span>
              <el-tag size="small" :type="d.load > 80 ? 'warning' : 'success'">{{ d.status }}</el-tag>
            </div>
          </div>
        </div>

        <!-- 大型设备 -->
        <div class="res-card equip-card">
          <h3><i v-html="svgEquip"></i>大型设备</h3>
          <div class="equip-grid">
            <div class="equip-item" v-for="e in equipments" :key="e.name">
              <strong>{{ e.count }}台</strong>
              <span>{{ e.name }}</span>
              <small>{{ e.model }}</small>
            </div>
          </div>
        </div>

        <!-- 服务能力 -->
        <div class="res-card service-card">
          <h3><i v-html="svgService"></i>服务能力</h3>
          <div class="service-list">
            <div class="service-item" v-for="sv in services" :key="sv.name">
              <div class="sv-left">
                <span class="sv-name">{{ sv.name }}</span>
                <span class="sv-desc">{{ sv.desc }}</span>
              </div>
              <strong class="sv-value">{{ sv.value }}</strong>
            </div>
          </div>
        </div>

        <!-- 质量趋势 — 折线图 -->
        <div class="res-card quality-card">
          <h3><i v-html="svgQuality"></i>质量趋势</h3>
          <div class="chart-wrap">
            <svg class="line-chart" viewBox="0 0 340 160" preserveAspectRatio="none">
              <g class="grid-lines">
                <line x1="40" y1="10" x2="330" y2="10" stroke="rgba(255,255,255,.18)" stroke-width=".5"/>
                <line x1="40" y1="42" x2="330" y2="42" stroke="rgba(255,255,255,.18)" stroke-width=".5"/>
                <line x1="40" y1="74" x2="330" y2="74" stroke="rgba(255,255,255,.18)" stroke-width=".5"/>
                <line x1="40" y1="106" x2="330" y2="106" stroke="rgba(255,255,255,.18)" stroke-width=".5"/>
                <line x1="40" y1="138" x2="330" y2="138" stroke="rgba(255,255,255,.3)" stroke-width="1"/>
              </g>
              <g class="y-labels" font-size="10" fill="rgba(255,255,255,.7)">
                <text x="35" y="13" text-anchor="end">100%</text>
                <text x="35" y="45" text-anchor="end">98%</text>
                <text x="35" y="77" text-anchor="end">96%</text>
                <text x="35" y="109" text-anchor="end">94%</text>
                <text x="35" y="141" text-anchor="end">92%</text>
              </g>
              <g class="x-labels" font-size="10" fill="rgba(255,255,255,.7)" text-anchor="middle">
                <text x="58" y="154">1月</text><text x="104" y="154">2月</text><text x="150" y="154">3月</text>
                <text x="196" y="154">4月</text><text x="242" y="154">5月</text><text x="288" y="154">6月</text><text x="334" y="154">7月</text>
              </g>
              <polyline class="line-path line-1" fill="none"
                points="58,42 104,30 150,50 196,36 242,28 288,44 334,12" />
              <polyline class="line-path line-2" fill="none"
                points="58,68 104,54 150,72 196,60 242,50 288,62 334,38" />
              <polyline class="line-path line-3" fill="none"
                points="58,30 104,50 150,22 196,44 242,34 288,20 334,20" />
              <g class="dots dots-1">
                <circle cx="58" cy="42" r="2.5"/><circle cx="104" cy="30" r="2.5"/><circle cx="150" cy="50" r="2.5"/>
                <circle cx="196" cy="36" r="2.5"/><circle cx="242" cy="28" r="2.5"/><circle cx="288" cy="44" r="2.5"/>
                <circle cx="334" cy="12" r="3.5"/>
                <text x="334" y="8" font-size="10" fill="#27ae80" font-weight="700" text-anchor="middle">99.1%</text>
              </g>
              <g class="dots dots-2">
                <circle cx="58" cy="68" r="2.5"/><circle cx="104" cy="54" r="2.5"/><circle cx="150" cy="72" r="2.5"/>
                <circle cx="196" cy="60" r="2.5"/><circle cx="242" cy="50" r="2.5"/><circle cx="288" cy="62" r="2.5"/>
                <circle cx="334" cy="38" r="3.5"/>
                <text x="334" y="33" font-size="10" fill="#5dade2" font-weight="700" text-anchor="middle">97.2%</text>
              </g>
              <g class="dots dots-3">
                <circle cx="58" cy="30" r="2.5"/><circle cx="104" cy="50" r="2.5"/><circle cx="150" cy="22" r="2.5"/>
                <circle cx="196" cy="44" r="2.5"/><circle cx="242" cy="34" r="2.5"/><circle cx="288" cy="20" r="2.5"/>
                <circle cx="334" cy="20" r="3.5"/>
                <text x="334" y="15" font-size="10" fill="#f39c12" font-weight="700" text-anchor="middle">98.4%</text>
              </g>
            </svg>
            <div class="chart-legend">
              <span><i style="background:#27ae80"></i>诊断符合率</span>
              <span><i style="background:#5dade2"></i>治愈好转率</span>
              <span><i style="background:#f39c12"></i>患者满意度</span>
            </div>
            <div class="quality-values">
              <div class="qv-item"><strong style="color:#27ae80">98.6<span>%</span></strong>诊断符合率</div>
              <div class="qv-item"><strong style="color:#5dade2">96.2<span>%</span></strong>治愈好转率</div>
              <div class="qv-item"><strong style="color:#f39c12">97.8<span>%</span></strong>患者满意度</div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 完善信息弹窗 -->
    <el-dialog v-model="showCompleteInfoDialog" title="完善个人信息" width="420px" :close-on-click-modal="false" center>
      <div class="complete-info-content">
        <div class="complete-info-icon">
          <svg viewBox="0 0 24 24" width="36" height="36" fill="none" stroke="#fff" stroke-width="2" stroke-linecap="round"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
        </div>
        <p>完善您的个人信息可以为您提供更好的就诊服务</p>
      </div>
      <template #footer>
        <div class="complete-info-footer">
          <el-button @click="showCompleteInfoDialog = false">稍后完善</el-button>
          <el-button type="primary" @click="goToCompleteInfo">立即完善</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Index">
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import useUserStore from '@/store/modules/user'
import { getPendingMedicalStaff } from '@/api/system/user'

const router = useRouter()
const userStore = useUserStore()

const heroWords = ['智慧医疗', 'AI 驱动', '温暖守护', '每一次就医']
const showCompleteInfoDialog = ref(false)
const pendingMedicalCount = ref(0)

const isAdmin = computed(() => userStore.roles?.some(role => role.roleKey?.includes('admin')) || false)

// 医疗 SVG 图标
const svgHeart = '<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78L12 21.23l8.84-8.84a5.5 5.5 0 000-7.78z"/></svg>'
const svgBuilding = '<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M3 21h18M9 8h1M9 12h1M9 16h1M14 8h1M14 12h1M14 16h1M5 21V5a2 2 0 012-2h10a2 2 0 012 2v16"/></svg>'
const svgUsers = '<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75"/></svg>'
const svgScalpel = '<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="6" cy="6" r="2"/><circle cx="18" cy="18" r="2"/><line x1="7" y1="7" x2="17" y2="17"/><circle cx="12" cy="12" r="8"/></svg>'
const svgDept = '<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" style="vertical-align:middle;margin-right:6px"><path d="M3 21h18M5 21V5a2 2 0 012-2h10a2 2 0 012 2v16"/><line x1="9" y1="21" x2="9" y2="17"/><line x1="15" y1="21" x2="15" y2="17"/></svg>'
const svgEquip = '<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" style="vertical-align:middle;margin-right:6px"><rect x="4" y="4" width="16" height="16" rx="3"/><path d="M9 4v16M15 4v16M4 9h16M4 15h16"/></svg>'
const svgService = '<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" style="vertical-align:middle;margin-right:6px"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>'
const svgQuality = '<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" style="vertical-align:middle;margin-right:6px"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>'

const deptColors = ['#27ae80', '#2980b9', '#27ae80', '#e67e22', '#e74c3c', '#f39c12']

// 核心统计数据
const statsData = [
  { label: '年门急诊量', value: '186 万', color: 'linear-gradient(135deg, #27ae80, #1abc6c)', delay: '0s', svg: svgHeart },
  { label: '开放床位', value: '2,200', color: 'linear-gradient(135deg, #2980b9, #5dade2)', delay: '0.1s', svg: svgBuilding },
  { label: '医护团队', value: '1,860 人', color: 'linear-gradient(135deg, #d35400, #e67e22)', delay: '0.2s', svg: svgUsers },
  { label: '手术量/年', value: '4.8 万', color: 'linear-gradient(135deg, #c0392b, #e74c3c)', delay: '0.3s', svg: svgScalpel }
]

const departments = [
  { name: '心血管内科', doctors: 32, beds: 186, load: 88, status: '高负荷' },
  { name: '神经外科', doctors: 24, beds: 142, load: 76, status: '正常' },
  { name: '骨科中心', doctors: 28, beds: 168, load: 82, status: '高负荷' },
  { name: '妇产科', doctors: 36, beds: 210, load: 65, status: '正常' },
  { name: '肿瘤科', doctors: 30, beds: 196, load: 91, status: '满床' },
  { name: '儿科中心', doctors: 22, beds: 130, load: 72, status: '正常' }
]

const equipments = [
  { name: '3.0T MRI', count: 3, model: 'Siemens Vida' },
  { name: '256排CT', count: 4, model: 'GE Revolution' },
  { name: 'DSA', count: 3, model: 'Philips Azurion' },
  { name: 'PET-CT', count: 1, model: 'uMI 780' },
  { name: '达芬奇机器人', count: 2, model: 'Da Vinci Xi' },
  { name: '直线加速器', count: 2, model: 'Varian TrueBeam' }
]

const services = [
  { name: '日均门诊人次', desc: '含急诊与专科门诊', value: '5,200' },
  { name: '日均手术台次', desc: '含择期与急诊手术', value: '132' },
  { name: '日均检查量', desc: 'CT/MRI/超声/检验', value: '2,860' },
  { name: '远程会诊/年', desc: '覆盖医联体单位', value: '3,400' }
]

onMounted(() => {
  nextTick(() => {
    if (isAdmin.value) {
      getPendingMedicalStaff().then(res => {
        if (res.data !== undefined && res.data !== null) pendingMedicalCount.value = res.data
      }).catch(() => {})
    }
    setTimeout(() => {
      if (userStore.needCompleteInfo && userStore.userType) {
        showCompleteInfoDialog.value = true
      }
    }, 500)
  })
})

const goToCompleteInfo = () => {
  showCompleteInfoDialog.value = false
  if (userStore.userType === 'doctor') router.push('/complete/doctor')
  else if (userStore.userType === 'patient') router.push('/complete/patient')
}
</script>

<style scoped lang="scss">
.his-home {
  position: relative;
  min-height: 100vh;
  overflow-x: hidden;
  color: #fff;
}

/* 固定视频背景 */
.home-video-bg {
  position: fixed;
  top: 0; left: 0;
  width: 100%; height: 100vh;
  object-fit: cover;
  z-index: 0;
}

/* 暖绿柔和遮罩 */
.home-video-overlay {
  position: fixed;
  inset: 0;
  z-index: 0;
  background: linear-gradient(135deg, rgba(120,180,165,.22), rgba(100,160,140,.18), rgba(60,120,100,.30));
  pointer-events: none;
}

/* === Hero Section === */
.home-hero {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  height: calc(100vh - 50px);
  padding: 40px 32px 32px 32px;
}

.hero-content { display: flex; flex-direction: column; align-items: flex-start; max-width: 720px; margin-top: 20px; }

.hero-heading {
  display: flex; flex-wrap: wrap; gap: 0.25em;
  margin: 0; padding: 0; list-style: none;
}

.hero-word {
  display: inline-block;
  font-size: clamp(26px, 3vw, 42px);
  font-weight: 700;
  line-height: 1.08;
  letter-spacing: -0.01em;
  text-transform: uppercase;
  color: #fff;
  text-shadow: 0 2px 12px rgba(0,0,0,.3);
  opacity: 0;
  transform: translateY(32px);
  animation: heroFadeUp 0.7s cubic-bezier(0.22, 1, 0.36, 1) forwards;
}

.hero-sub {
  margin-top: 24px;
  font-size: 14px; line-height: 1.65;
  color: rgba(255,255,255,0.88);
  max-width: 260px;
  opacity: 0; transform: translateY(24px);
  animation: heroFadeUp 0.7s cubic-bezier(0.22, 1, 0.36, 1) forwards;
  animation-delay: 0.9s;
}

.hero-scroll-hint {
  position: absolute; bottom: 40px; left: 50%; transform: translateX(-50%);
  span {
    display: block; width: 24px; height: 24px;
    border-right: 2px solid rgba(255,255,255,.6);
    border-bottom: 2px solid rgba(255,255,255,.6);
    transform: rotate(45deg);
    animation: scrollBounce 2s ease-in-out infinite;
  }
}

@keyframes scrollBounce {
  0%, 100% { opacity: .5; transform: rotate(45deg) translate(0, 0); }
  50% { opacity: 1; transform: rotate(45deg) translate(4px, 4px); }
}

@keyframes heroFadeUp {
  to { opacity: 1; transform: translateY(0); }
}

/* === 医疗资源展示区 === */
.home-resources {
  position: relative;
  z-index: 1;
  padding: 50px 32px 60px;
  background: linear-gradient(180deg, rgba(80,150,130,.30) 0%, rgba(60,120,100,.48) 30%, rgba(40,90,75,.65) 100%);
  backdrop-filter: blur(12px);
}

/* 核心数据卡片 */
.resources-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  max-width: 1180px;
  margin: 0 auto 40px;
}

.stat-card {
  display: flex; align-items: center; gap: 14px;
  padding: 20px;
  border-radius: 14px;
  border: 1px solid rgba(255,255,255,.22);
  background: rgba(255,255,255,.14);
  backdrop-filter: blur(12px);
  opacity: 0; transform: translateY(24px);
  animation: cardSlideUp .6s ease forwards;
  animation-delay: var(--delay, 0s);
  transition: transform .3s, background .3s;
  &:hover { transform: translateY(-4px); background: rgba(255,255,255,.22); }
}

.stat-icon {
  width: 48px; height: 48px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  color: #fff; box-shadow: 0 8px 20px rgba(0,0,0,.2);
}

.stat-body {
  strong { display: block; font-size: 22px; font-weight: 800; color: #fff; margin-bottom: 2px; }
  span { font-size: 12px; color: rgba(255,255,255,.65); }
}

@keyframes cardSlideUp {
  to { opacity: 1; transform: translateY(0); }
}

/* 资源卡片网格 */
.resources-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18px;
  max-width: 1180px;
  margin: 0 auto;
}

.res-card {
  padding: 22px;
  border-radius: 16px;
  border: 1px solid rgba(255,255,255,.2);
  background: rgba(255,255,255,.12);
  backdrop-filter: blur(14px);
  h3 {
    margin: 0 0 18px;
    font-size: 15px; font-weight: 700; color: #9be5cf;
    i { display: inline-flex; align-items: center; margin-right: 6px; }
    :deep(svg) { display: inline-block; }
  }
}

/* 科室列表 */
.dept-list { display: flex; flex-direction: column; gap: 6px; }
.dept-dot { width: 8px; height: 8px; flex-shrink: 0; }
.dept-row {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 10px; border-radius: 10px;
  background: rgba(255,255,255,.10);
  .dept-name { width: 90px; font-size: 12px; font-weight: 600; color: #e0f2e9; flex-shrink: 0; }
  .dept-info { flex: 1; font-size: 11px; color: rgba(255,255,255,.55); min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
}

/* 设备网格 */
.equip-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px;
}
.equip-item {
  padding: 12px; border-radius: 10px; text-align: center;
  background: rgba(255,255,255,.10); border: 1px solid rgba(255,255,255,.12);
  strong { display: block; font-size: 20px; font-weight: 800; color: #8cdbc4; margin-bottom: 4px; }
  span { display: block; font-size: 12px; font-weight: 600; color: rgba(255,255,255,.8); }
  small { display: block; font-size: 10px; color: rgba(255,255,255,.55); margin-top: 2px; }
}

/* 服务列表 */
.service-list { display: flex; flex-direction: column; gap: 6px; }
.service-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 10px 12px; border-radius: 10px; background: rgba(255,255,255,.04);
  .sv-left { .sv-name { display: block; font-size: 13px; font-weight: 600; color: #e0f2e9; }
    .sv-desc { font-size: 11px; color: rgba(255,255,255,.45); }
  }
  .sv-value { font-size: 18px; font-weight: 800; color: #8cdbc4; }
}

/* 质量折线图 */
.chart-wrap { position: relative; }
.line-chart { width: 100%; height: auto; display: block; }
.line-path {
  stroke-width: 1.4;
  fill: none;
  stroke-linejoin: round; stroke-linecap: round;
  &.line-1 { stroke: #27ae80; }
  &.line-2 { stroke: #5dade2; }
  &.line-3 { stroke: #f39c12; }
}
.dots {
  fill: #fff;
  &.dots-1 circle { fill: #27ae80; }
  &.dots-2 circle { fill: #5dade2; }
  &.dots-3 circle { fill: #f39c12; }
}
.chart-legend {
  display: flex; justify-content: center; gap: 16px; padding: 8px 0 12px;
  font-size: 11px; color: rgba(255,255,255,.75);
  i { display: inline-block; width: 8px; height: 8px; border-radius: 50%; margin-right: 4px; vertical-align: middle; }
}
.quality-values {
  display: flex; justify-content: space-around; padding-top: 4px;
}
.qv-item {
  text-align: center;
  strong { font-size: 22px; font-weight: 800; display: block; span { font-size: 14px; } }
  font-size: 11px; color: rgba(255,255,255,.7);
}

/* 完善信息 */
.complete-info-content { text-align: center; padding: 20px 0; }
.complete-info-icon {
  width: 80px; height: 80px; margin: 0 auto 20px; border-radius: 50%;
  background: linear-gradient(135deg, #73d8b9, #49b99d);
  display: flex; align-items: center; justify-content: center; color: #fff; font-size: 40px;
}
.complete-info-content p { font-size: 16px; color: #607f8c; margin: 0; }
.complete-info-footer { display: flex; justify-content: center; gap: 12px; }

@media (max-width: 900px) {
  .home-hero { padding: 90px 18px 32px 18px; }
  .hero-word { font-size: clamp(20px, 5vw, 32px); }
  .home-resources { padding: 40px 18px 50px; }
  .resources-stats { grid-template-columns: repeat(2, 1fr); }
  .resources-grid { grid-template-columns: 1fr; }
  .equip-grid { grid-template-columns: repeat(2, 1fr); }
  .quality-values { flex-direction: column; gap: 8px; }
}
</style>
