<template>
  <div class="his-home">
    <div class="home-ambient" aria-hidden="true">
      <span class="ambient-cross cross-one"></span>
      <span class="ambient-cross cross-two"></span>
      <span class="ambient-cross cross-three"></span>
    </div>

    <section class="home-header">
      <div class="header-copy">
        <div class="eyebrow">
          <span class="live-dot"></span>
          <el-icon><OfficeBuilding /></el-icon>
          门诊一体化运营工作台
        </div>
        <h1>今日门诊全业务运行概览</h1>
        <p>聚合挂号、诊疗、医技检查、收费与药房发药关键节点，辅助门诊大厅实时调度。</p>
        <div class="header-chips">
          <span>智慧分诊在线</span>
          <span>候诊队列同步</span>
          <span>药房库存正常</span>
        </div>
      </div>

      <div class="hero-monitor">
        <div class="monitor-core">
          <div class="monitor-title">
            <span>实时诊疗心跳</span>
            <strong>86%</strong>
          </div>
          <svg viewBox="0 0 360 120" class="ecg-line" aria-hidden="true">
            <path class="ecg-track" d="M8 70H76l20-34 28 78 26-52 20 18h54l19-28 25 54 24-36h54" />
            <path class="ecg-glow" d="M8 70H76l20-34 28 78 26-52 20 18h54l19-28 25 54 24-36h54" />
          </svg>
          <div class="monitor-grid">
            <div>
              <span>平均候诊</span>
              <strong>18min</strong>
            </div>
            <div>
              <span>接诊效率</span>
              <strong>+12%</strong>
            </div>
          </div>
        </div>
        <div class="floating-badge badge-a">
          <el-icon><FirstAidKit /></el-icon>
          药房联动
        </div>
        <div class="floating-badge badge-b">
          <el-icon><DataLine /></el-icon>
          队列平稳
        </div>
      </div>

      <div class="header-status">
        <div v-for="item in statusCards" :key="item.label" class="status-card">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <small>{{ item.note }}</small>
        </div>
      </div>
    </section>

    <section class="metric-grid">
      <article v-for="(item, index) in coreMetrics" :key="item.label" class="metric-card" :style="{ '--delay': `${index * 90}ms` }">
        <div class="metric-top">
          <div class="metric-icon" :class="item.tone">
            <el-icon><component :is="item.icon" /></el-icon>
          </div>
          <span class="metric-trend">{{ item.trend }}</span>
        </div>
        <div class="metric-content">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <small>{{ item.note }}</small>
        </div>
        <div class="metric-progress">
          <i :style="{ width: `${item.progress}%` }"></i>
        </div>
      </article>
    </section>

    <section class="insight-strip">
      <div v-for="item in insightCards" :key="item.label" class="insight-item">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.desc }}</small>
      </div>
    </section>

    <section class="dashboard-layout">
      <div class="panel business-flow">
        <div class="panel-head">
          <div>
            <span class="panel-kicker">业务闭环</span>
            <h2>门诊流程</h2>
          </div>
          <el-tag type="primary" effect="plain">实时队列</el-tag>
        </div>
        <div class="flow-list">
          <div v-for="step in outpatientFlow" :key="step.name" class="flow-item">
            <div class="flow-marker" :class="step.tone">
              <el-icon><component :is="step.icon" /></el-icon>
            </div>
            <div class="flow-info">
              <div class="flow-title">
                <strong>{{ step.name }}</strong>
                <span>{{ step.count }}</span>
              </div>
              <el-progress :percentage="step.progress" :stroke-width="7" :show-text="false" />
              <p>{{ step.note }}</p>
            </div>
          </div>
        </div>
      </div>

      <div class="panel queue-panel">
        <div class="panel-head">
          <div>
            <span class="panel-kicker">诊区动态</span>
            <h2>候诊与在诊</h2>
          </div>
          <el-icon class="panel-head-icon"><DataLine /></el-icon>
        </div>
        <div class="queue-summary">
          <div>
            <span>平均候诊</span>
            <strong>18 min</strong>
          </div>
          <div>
            <span>医生接诊率</span>
            <strong>86%</strong>
          </div>
        </div>
        <div class="dept-list">
          <div v-for="dept in departments" :key="dept.name" class="dept-row">
            <div class="dept-main">
              <strong>{{ dept.name }}</strong>
              <span>{{ dept.doctor }} 位医生出诊</span>
              <i :style="{ width: `${dept.load}%` }"></i>
            </div>
            <div class="dept-count">
              <strong>{{ dept.waiting }}</strong>
              <span>候诊</span>
            </div>
          </div>
        </div>
      </div>

      <div class="panel finance-panel">
        <div class="panel-head">
          <div>
            <span class="panel-kicker">收费与药房</span>
            <h2>结算发药状态</h2>
          </div>
          <el-icon class="panel-head-icon"><Coin /></el-icon>
        </div>
        <div class="finance-grid">
          <div v-for="item in financeStats" :key="item.label">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
            <small>{{ item.note }}</small>
          </div>
        </div>
        <div class="pharmacy-line">
          <div>
            <span>待发药处方</span>
            <strong>72</strong>
          </div>
          <el-progress :percentage="68" :stroke-width="10" :show-text="false" />
        </div>
      </div>

      <div class="panel task-panel">
        <div class="panel-head">
          <div>
            <span class="panel-kicker">待办提醒</span>
            <h2>需要处理的单据</h2>
          </div>
          <el-icon class="panel-head-icon"><BellFilled /></el-icon>
        </div>
        <div class="task-list">
          <div v-for="task in pendingTasks" :key="task.title" class="task-row">
            <div class="task-badge" :class="task.tone">
              <el-icon><component :is="task.icon" /></el-icon>
            </div>
            <div>
              <strong>{{ task.title }}</strong>
              <span>{{ task.desc }}</span>
            </div>
            <em>{{ task.count }}</em>
          </div>
        </div>
      </div>
    </section>

    <section ref="showcaseStageRef" class="showcase-stage">
      <div class="showcase-ambient" aria-hidden="true">
        <span class="showcase-cross cross-a"></span>
        <span class="showcase-cross cross-b"></span>
        <span class="showcase-cross cross-c"></span>
        <span class="dna-strand strand-a"></span>
        <span class="dna-strand strand-b"></span>
        <span class="pill-float pill-a"></span>
        <span class="pill-float pill-b"></span>
      </div>
      <div class="showcase-path" aria-hidden="true">
        <div class="path-canvas">
          <span
            class="path-line"
            :style="{
              top: `${showcaseViewportHeight / 2}px`,
              bottom: `${showcaseViewportHeight / 2}px`,
              left: 'var(--path-x)'
            }"
          ></span>
          <svg class="path-svg" viewBox="0 0 220 640" preserveAspectRatio="none">
            <path
              class="path-track"
              d="M116 20 C42 126 172 178 108 288 S52 432 128 526 S176 592 112 626"
            />
          </svg>
          <span
            v-for="(node, index) in pathNodes"
            :key="node.label"
            class="path-node"
            :class="{ active: activeShowcaseIndex >= index }"
            :style="{ top: node.top, left: node.left }"
          >
            <i></i>
            <em>{{ node.label }}</em>
          </span>
          <span
            class="path-progress-dot"
            :style="{ top: pathDotTop, left: 'var(--path-x)' }"
          ></span>
        </div>
      </div>

      <section
        v-for="(item, index) in showcaseSections"
        :key="item.key"
        :ref="el => setShowcaseRef(el, index)"
        class="showcase-section"
        :class="[item.motion, item.layout, { 'is-active': activeShowcaseIndex === index }]"
      >
        <div class="showcase-inner">
          <div class="showcase-art">
            <img :src="item.image" :alt="item.title" />
            <span class="art-ring"></span>
            <span class="art-chip chip-one">{{ item.chips[0] }}</span>
            <span class="art-chip chip-two">{{ item.chips[1] }}</span>
          </div>
          <div class="showcase-copy">
            <span class="showcase-step">0{{ index + 1 }} / 04</span>
            <h2>{{ item.title }}</h2>
            <p>{{ item.description }}</p>
            <button v-if="item.button" class="showcase-button" type="button">
              {{ item.button }}
            </button>
          </div>
        </div>
      </section>
    </section>

    <!-- 完善信息提醒弹窗 -->
    <el-dialog
      v-model="showCompleteInfoDialog"
      :title="`完善您的${userTypeText}信息`"
      width="500px"
      center
      close-on-click-modal
      :close-on-click-modal="false"
    >
      <div class="complete-info-content">
        <div class="complete-info-icon">
          <el-icon><UserFilled /></el-icon>
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
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  BellFilled,
  Coin,
  DataLine,
  DocumentChecked,
  FirstAidKit,
  Histogram,
  OfficeBuilding,
  Operation,
  Tickets,
  UserFilled,
  Van,
  WarningFilled
} from '@element-plus/icons-vue'
import useUserStore from '@/store/modules/user'

const router = useRouter()
const userStore = useUserStore()

// 完善信息弹窗状态
const showCompleteInfoDialog = ref(false)

// 用户类型文本
const userTypeText = computed(() => {
  return userStore.userType === 'doctor' ? '医生' : '患者'
})

// 页面加载时检查是否需要完善信息
onMounted(() => {
  nextTick(() => {
    // 延迟一下，确保用户信息已经加载
    setTimeout(() => {
      if (userStore.needCompleteInfo && userStore.userType) {
        showCompleteInfoDialog.value = true
      }
    }, 500)
  })
})

// 跳转到完善信息页面
const goToCompleteInfo = () => {
  showCompleteInfoDialog.value = false
  if (userStore.userType === 'doctor') {
    router.push('/complete/doctor')
  } else if (userStore.userType === 'patient') {
    router.push('/complete/patient')
  }
}

const currentTime = computed(() => {
  const now = new Date()
  return now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
})

const statusCards = computed(() => [
  { label: '当前班次', value: '日间门诊', note: '08:00 - 17:30' },
  { label: '系统状态', value: '运行平稳', note: '核心服务在线' },
  { label: '刷新时间', value: currentTime.value, note: '自动同步' }
])

const coreMetrics = [
  { label: '今日挂号', value: '1,286', trend: '+8.6%', note: '较昨日增长', icon: Tickets, tone: 'blue', progress: 76 },
  { label: '在诊人数', value: '146', trend: '3 个高峰', note: '诊区运行中', icon: UserFilled, tone: 'cyan', progress: 68 },
  { label: '待检查单据', value: '93', trend: '11 张超时', note: '医技队列关注', icon: DocumentChecked, tone: 'teal', progress: 54 },
  { label: '待发药处方', value: '72', trend: '9 min', note: '平均等待', icon: FirstAidKit, tone: 'mint', progress: 68 }
]

const insightCards = [
  { label: '分诊协同', value: '24 个诊室', desc: '队列自动均衡' },
  { label: '患者满意度', value: '96.8%', desc: '今日回访样本' },
  { label: '异常预警', value: '11 条', desc: '已推送责任科室' }
]

const outpatientFlow = [
  { name: '挂号分诊', count: '312 人排队', progress: 76, note: '自助机与窗口分流正常', icon: Tickets, tone: 'blue' },
  { name: '医生诊疗', count: '146 人在诊', progress: 68, note: '内科诊区接诊压力偏高', icon: Operation, tone: 'cyan' },
  { name: '医技检查', count: '93 张待检', progress: 54, note: '检验、超声队列需关注', icon: Histogram, tone: 'teal' }
]

const departments = [
  { name: '普通内科', doctor: 8, waiting: 42, load: 82 },
  { name: '儿科门诊', doctor: 5, waiting: 31, load: 68 },
  { name: '骨科门诊', doctor: 4, waiting: 18, load: 45 },
  { name: '妇产科', doctor: 6, waiting: 24, load: 58 }
]

const pendingTasks = [
  { title: '待检查申请', desc: '检验、影像、超声未执行', count: '93', icon: DocumentChecked, tone: 'blue' },
  { title: '异常候诊提醒', desc: '超过目标候诊时间', count: '11', icon: WarningFilled, tone: 'amber' },
  { title: '待结算费用', desc: '诊间支付与窗口缴费', count: '218', icon: Coin, tone: 'cyan' },
  { title: '转运协同', desc: '需导诊协助患者', count: '6', icon: Van, tone: 'teal' }
]

const financeStats = [
  { label: '今日收费', value: '¥486,920', note: '门诊收入' },
  { label: '退费审核', value: '12', note: '待财务确认' },
  { label: '医保结算', value: '74%', note: '占总交易' },
  { label: '处方完成', value: '91%', note: '发药完成率' }
]

const activeShowcaseIndex = ref(-1)
const showcaseProgress = ref(0)
const showcaseStageRef = ref(null)
const showcaseViewportHeight = ref(640)
const pathDotTop = ref('320px')
const showcaseRefs = ref([])
let showcaseObserver
let showcaseScrollRoot

const showcaseSections = [
  {
    key: 'easy-register',
    title: '轻松线上挂号',
    description: '线上预约、减少排队、便捷就诊，患者可快速完成科室选择与时段确认。',
    image: 'https://pic1.imgdb.cn/item/6a203edb9ecef740178671e4.png',
    motion: 'showcase-from-right',
    layout: 'copy-left',
    chips: ['智能排队', '预约提醒'],
    button: '立即预约'
  },
  {
    key: 'expert-consult',
    title: '资深专家一对一问诊',
    description: '在线沟通病情、同步诊疗建议、沉淀随访记录，让复诊与慢病管理更安心。',
    image: 'https://pic1.imgdb.cn/item/6a203e599ecef74017867172.png',
    motion: 'showcase-from-left',
    layout: 'copy-right',
    chips: ['远程问诊', '病历同步']
  },
  {
    key: 'precision-check',
    title: '高精度专业检查',
    description: 'CT 影像、心电、全项生化筛查，精准体检诊断，辅助医生快速判断风险。',
    image: 'https://pic1.imgdb.cn/item/6a203e949ecef740178671a2.png',
    motion: 'showcase-from-bottom',
    layout: 'copy-left',
    chips: ['CT 影像', '数据判读']
  },
  {
    key: 'medicine-guide',
    title: '科学用药指导',
    description: '处方核验、用药科普与药品管理联动，减少漏服误服，守护患者长期健康。',
    image: 'https://pic1.imgdb.cn/item/6a203eba9ecef740178671c6.png',
    motion: 'showcase-from-corner',
    layout: 'copy-right',
    chips: ['处方核验', '药师咨询'],
    button: '咨询药师'
  }
]

const pathNodeLabels = ['挂号', '问诊', '检查', '用药']
const pathNodes = computed(() => pathNodeLabels.map((label, index) => ({
  label,
  top: `${(index + 0.5) * showcaseViewportHeight.value}px`,
  left: 'var(--path-x)'
})))

function setShowcaseRef(el, index) {
  if (el) {
    showcaseRefs.value[index] = el
  }
}

function updateShowcaseProgress() {
  const stage = showcaseStageRef.value
  if (!stage) {
    return
  }

  const isWindowRoot = !showcaseScrollRoot || showcaseScrollRoot === window
  const viewportHeight = isWindowRoot ? window.innerHeight : showcaseScrollRoot.clientHeight
  const rootTop = isWindowRoot ? 0 : showcaseScrollRoot.getBoundingClientRect().top
  const stageRect = stage.getBoundingClientRect()
  showcaseViewportHeight.value = viewportHeight
  const totalDistance = Math.max(stage.offsetHeight - viewportHeight, 1)
  const traveled = rootTop - stageRect.top
  showcaseProgress.value = Math.min(1, Math.max(0, traveled / totalDistance))
  const start = viewportHeight / 2
  const end = stage.offsetHeight - viewportHeight / 2
  const dotTop = start + showcaseProgress.value * (end - start)
  const centerInStage = traveled + viewportHeight / 2
  const nextIndex = Math.floor(centerInStage / viewportHeight)

  pathDotTop.value = `${dotTop}px`

  if (nextIndex >= 0 && nextIndex < showcaseSections.length) {
    activeShowcaseIndex.value = nextIndex
  } else if (nextIndex < 0) {
    activeShowcaseIndex.value = -1
  }
}

onMounted(() => {
  nextTick(() => {
    const scrollRoot = document.querySelector('.app-main')
    showcaseScrollRoot = scrollRoot || window
    showcaseObserver = new IntersectionObserver((entries) => {
      const visibleEntry = entries
        .filter(entry => entry.isIntersecting)
        .sort((a, b) => b.intersectionRatio - a.intersectionRatio)[0]

      if (!visibleEntry) {
        return
      }

      updateShowcaseProgress()
    }, {
      root: scrollRoot || null,
      threshold: [0.3, 0.48, 0.62]
    })

    showcaseRefs.value.forEach((section, index) => {
      section.dataset.showcaseIndex = String(index)
      showcaseObserver.observe(section)
    })

    updateShowcaseProgress()
    showcaseScrollRoot.addEventListener('scroll', updateShowcaseProgress, { passive: true })
    window.addEventListener('resize', updateShowcaseProgress)
  })
})

onBeforeUnmount(() => {
  showcaseObserver?.disconnect()
  showcaseScrollRoot?.removeEventListener('scroll', updateShowcaseProgress)
  window.removeEventListener('resize', updateShowcaseProgress)
})
</script>

<style scoped lang="scss">
.his-home {
  position: relative;
  min-height: 100%;
  padding: 22px;
  overflow: hidden;
  color: #20445a;
  background:
    linear-gradient(180deg, rgba(224, 247, 241, .78) 0%, rgba(246, 251, 249, .82) 260px),
    linear-gradient(135deg, #f4fbfa 0%, #edf8fb 46%, #fffaf4 100%);
}

.his-home::before {
  position: absolute;
  inset: 0;
  content: "";
  pointer-events: none;
  background-image:
    linear-gradient(rgba(116, 198, 190, .12) 1px, transparent 1px),
    linear-gradient(90deg, rgba(116, 198, 190, .12) 1px, transparent 1px);
  background-size: 36px 36px;
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, .55), transparent 72%);
}

.home-ambient {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.ambient-cross {
  position: absolute;
  width: 18px;
  height: 18px;
  opacity: .24;
  animation: softBreath 4.6s ease-in-out infinite;

  &::before,
  &::after {
    position: absolute;
    content: "";
    border-radius: 6px;
    background: #72cdbb;
  }

  &::before {
    top: 7px;
    left: 0;
    width: 18px;
    height: 4px;
  }

  &::after {
    top: 0;
    left: 7px;
    width: 4px;
    height: 18px;
  }
}

.cross-one {
  top: 12%;
  right: 10%;
}

.cross-two {
  top: 54%;
  left: 7%;
  animation-delay: -1.8s;
}

.cross-three {
  right: 28%;
  bottom: 12%;
  animation-delay: -3s;
}

.home-header,
.metric-card,
.panel,
.insight-strip {
  position: relative;
  z-index: 1;
  border: 1px solid rgba(165, 216, 221, .56);
  border-radius: 8px;
  background: rgba(255, 255, 255, .78);
  box-shadow: 0 14px 36px rgba(54, 126, 140, .1);
  backdrop-filter: blur(16px);
}

.home-header {
  display: grid;
  grid-template-columns: minmax(380px, 1.1fr) minmax(320px, .9fr) minmax(260px, 330px);
  gap: 18px;
  align-items: stretch;
  margin-bottom: 16px;
  padding: 22px;
  overflow: hidden;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, .9), rgba(233, 250, 247, .84)),
    rgba(255, 255, 255, .76);
}

.home-header::after {
  position: absolute;
  right: -60px;
  bottom: -90px;
  width: 360px;
  height: 220px;
  content: "";
  border: 1px solid rgba(116, 198, 190, .24);
  border-radius: 50%;
  animation: waveFloat 7s ease-in-out infinite;
}

.header-copy {
  position: relative;
  z-index: 1;

  .eyebrow {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    color: #22917f;
    font-size: 13px;
    font-weight: 800;
  }

  h1 {
    margin: 14px 0 10px;
    color: #19445f;
    font-size: 30px;
    line-height: 1.2;
    font-weight: 800;
    letter-spacing: 0;
  }

  p {
    margin: 0;
    max-width: 680px;
    color: #607f8c;
    font-size: 14px;
    line-height: 1.8;
  }
}

.live-dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: #65c9a8;
  box-shadow: 0 0 0 6px rgba(101, 201, 168, .18);
  animation: pulseDot 1.8s ease-in-out infinite;
}

.header-chips {
  display: flex;
  gap: 10px;
  margin-top: 18px;
  flex-wrap: wrap;

  span {
    height: 30px;
    padding: 0 12px;
    border: 1px solid rgba(112, 199, 184, .35);
    border-radius: 8px;
    color: #37776f;
    font-size: 12px;
    font-weight: 700;
    line-height: 28px;
    background: rgba(239, 251, 247, .86);
  }
}

.hero-monitor {
  position: relative;
  z-index: 1;
  min-height: 210px;
}

.monitor-core {
  height: 100%;
  min-height: 210px;
  padding: 18px;
  border: 1px solid rgba(134, 207, 210, .46);
  border-radius: 8px;
  background:
    linear-gradient(180deg, rgba(249, 255, 252, .9), rgba(228, 248, 247, .72)),
    #f8fffc;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, .78);
}

.monitor-title,
.monitor-grid {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.monitor-title {
  span {
    color: #6a8d96;
    font-size: 13px;
    font-weight: 700;
  }

  strong {
    color: #1c8175;
    font-size: 28px;
  }
}

.ecg-line {
  width: 100%;
  height: 92px;
  margin: 8px 0;
  overflow: visible;
  fill: none;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.ecg-track {
  stroke: rgba(147, 210, 216, .38);
  stroke-width: 8;
}

.ecg-glow {
  stroke: #58bfae;
  stroke-width: 4;
  stroke-dasharray: 520;
  stroke-dashoffset: 520;
  animation: ecgDraw 3.2s ease-in-out infinite;
}

.monitor-grid {
  div {
    flex: 1;
    padding: 12px;
    border-radius: 8px;
    background: rgba(255, 255, 255, .72);
  }

  span,
  strong {
    display: block;
  }

  span {
    color: #76919b;
    font-size: 12px;
  }

  strong {
    margin-top: 5px;
    color: #24506a;
    font-size: 18px;
  }
}

.floating-badge {
  position: absolute;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 34px;
  padding: 0 12px;
  border: 1px solid rgba(143, 204, 211, .45);
  border-radius: 8px;
  color: #35766f;
  font-size: 12px;
  font-weight: 800;
  background: rgba(255, 255, 255, .86);
  box-shadow: 0 10px 22px rgba(75, 145, 154, .12);
  animation: floatBadge 4s ease-in-out infinite;
}

.badge-a {
  top: 22px;
  right: -10px;
}

.badge-b {
  left: -14px;
  bottom: 22px;
  animation-delay: -1.8s;
}

.header-status {
  position: relative;
  z-index: 1;
  display: grid;
  gap: 10px;
}

.status-card {
  padding: 14px;
  border: 1px solid rgba(144, 207, 212, .38);
  border-radius: 8px;
  background: rgba(255, 255, 255, .68);

  span,
  small {
    display: block;
    color: #748b95;
    font-size: 12px;
  }

  strong {
    display: block;
    margin: 7px 0 4px;
    color: #214a63;
    font-size: 18px;
  }
}

.metric-grid {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 14px;
}

.metric-card {
  min-height: 146px;
  padding: 16px;
  overflow: hidden;
  animation: cardIn .45s ease both;
  animation-delay: var(--delay);

  &::after {
    position: absolute;
    inset: auto -30px -40px auto;
    width: 130px;
    height: 90px;
    content: "";
    border-radius: 50%;
    background: rgba(112, 210, 190, .16);
  }
}

.metric-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.metric-icon,
.flow-marker,
.task-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #ffffff;
  border-radius: 8px;

  &.blue {
    background: linear-gradient(135deg, #75b8df, #4a93bb);
  }

  &.cyan {
    background: linear-gradient(135deg, #67c9d6, #38a5b5);
  }

  &.teal {
    background: linear-gradient(135deg, #70cdbc, #3fae9c);
  }

  &.mint {
    background: linear-gradient(135deg, #8bd9b9, #55b98f);
  }

  &.amber {
    background: linear-gradient(135deg, #e6bd74, #c8923f);
  }
}

.metric-icon {
  width: 46px;
  height: 46px;
  font-size: 23px;
  box-shadow: 0 12px 22px rgba(68, 153, 156, .18);
}

.metric-trend {
  padding: 5px 9px;
  border-radius: 8px;
  color: #2f887a;
  font-size: 12px;
  font-weight: 800;
  background: rgba(226, 248, 241, .9);
}

.metric-content {
  position: relative;
  z-index: 1;
  min-width: 0;

  span,
  small {
    display: block;
    color: #6a8490;
    font-size: 13px;
  }

  strong {
    display: block;
    margin: 7px 0 5px;
    color: #163f5a;
    font-size: 30px;
    line-height: 1;
  }
}

.metric-progress {
  position: absolute;
  right: 16px;
  bottom: 14px;
  left: 16px;
  height: 6px;
  overflow: hidden;
  border-radius: 8px;
  background: rgba(214, 235, 238, .88);

  i {
    display: block;
    height: 100%;
    border-radius: inherit;
    background: linear-gradient(90deg, #7fd7bf, #67b9dc);
    animation: progressGlow 2.4s ease-in-out infinite;
  }
}

.insight-strip {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 14px;
  padding: 12px;
}

.insight-item {
  padding: 12px 14px;
  border-radius: 8px;
  background: rgba(244, 252, 250, .82);

  span,
  small {
    display: block;
    color: #6d8790;
    font-size: 12px;
  }

  strong {
    display: block;
    margin: 6px 0 4px;
    color: #1d5e57;
    font-size: 20px;
  }
}

.dashboard-layout {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.panel {
  padding: 18px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;

  h2 {
    margin: 5px 0 0;
    color: #19445f;
    font-size: 18px;
    font-weight: 800;
  }
}

.panel-kicker {
  color: #5f9c93;
  font-size: 12px;
  font-weight: 800;
}

.panel-head-icon {
  color: #3aa897;
  font-size: 24px;
}

.flow-list {
  display: grid;
  gap: 13px;
}

.flow-item {
  position: relative;
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  gap: 12px;
  padding: 12px;
  border: 1px solid rgba(219, 235, 238, .8);
  border-radius: 8px;
  background: rgba(250, 253, 253, .76);

  &::before {
    position: absolute;
    top: 54px;
    bottom: -14px;
    left: 33px;
    width: 2px;
    content: "";
    background: linear-gradient(180deg, rgba(114, 205, 187, .55), transparent);
  }

  &:last-child::before {
    display: none;
  }
}

.flow-marker {
  width: 42px;
  height: 42px;
  font-size: 21px;
}

.flow-title {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 8px;
  color: #21475f;

  span {
    color: #2d8b7d;
    font-weight: 800;
    font-size: 13px;
  }
}

.flow-info {
  strong {
    font-size: 14px;
  }

  p {
    margin: 8px 0 0;
    color: #6f8792;
    font-size: 12px;
  }
}

.queue-summary {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 12px;

  div {
    padding: 14px;
    border-radius: 8px;
    background: linear-gradient(135deg, #eefaf7, #edf8fb);
  }

  span,
  strong {
    display: block;
  }

  span {
    color: #6c8790;
    font-size: 12px;
  }

  strong {
    margin-top: 7px;
    color: #1e526b;
    font-size: 23px;
  }
}

.dept-list,
.task-list {
  display: grid;
  gap: 8px;
}

.dept-row,
.task-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 12px 0;
  border-top: 1px solid #e3eff0;
}

.dept-main {
  flex: 1;
  min-width: 0;

  strong,
  span {
    display: block;
  }

  span {
    margin-top: 4px;
    color: #708994;
    font-size: 12px;
  }

  i {
    display: block;
    height: 5px;
    margin-top: 9px;
    border-radius: 8px;
    background: linear-gradient(90deg, #78d5bd, #73bede);
  }
}

.dept-count {
  text-align: right;

  strong {
    color: #148173;
    font-size: 23px;
  }

  span {
    font-size: 11px;
    color: #94a6ad;
  }
}

.finance-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 12px;

  div {
    padding: 13px;
    border: 1px solid #dfedef;
    border-radius: 8px;
    background: rgba(251, 254, 253, .86);
  }

  span,
  strong,
  small {
    display: block;
  }

  span,
  small {
    color: #6f8790;
    font-size: 12px;
  }

  strong {
    margin: 8px 0 5px;
    color: #1d4d68;
    font-size: 20px;
  }
}

.pharmacy-line {
  padding: 15px;
  border-radius: 8px;
  background: linear-gradient(135deg, #eefaf7, #fffaf2);

  div {
    display: flex;
    justify-content: space-between;
    margin-bottom: 10px;
    color: #204860;
  }

  strong {
    color: #178374;
    font-size: 23px;
  }
}

.task-row {
  justify-content: flex-start;
  padding: 11px 0;

  strong,
  span {
    display: block;
  }

  span {
    margin-top: 4px;
    color: #758b94;
    font-size: 12px;
  }

  em {
    margin-left: auto;
    color: #1f526b;
    font-size: 22px;
    font-style: normal;
    font-weight: 800;
  }
}

.task-badge {
  width: 36px;
  height: 36px;
  font-size: 18px;
}

.showcase-stage {
  position: relative;
  z-index: 1;
  --path-x: 50%;
  margin: 18px -22px -22px;
  overflow: hidden;
  background: #dff6ef;
}

.showcase-stage::before {
  position: absolute;
  inset: 0;
  content: "";
  pointer-events: none;
  background-image:
    linear-gradient(rgba(83, 172, 154, .1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(83, 172, 154, .1) 1px, transparent 1px);
  background-size: 42px 42px;
  opacity: .7;
}

.showcase-ambient {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.showcase-cross {
  position: absolute;
  width: 20px;
  height: 20px;
  opacity: .22;
  animation: showcaseBreath 4.8s ease-in-out infinite;

  &::before,
  &::after {
    position: absolute;
    content: "";
    border-radius: 8px;
    background: #59bca5;
  }

  &::before {
    top: 8px;
    left: 0;
    width: 20px;
    height: 4px;
  }

  &::after {
    top: 0;
    left: 8px;
    width: 4px;
    height: 20px;
  }
}

.cross-a {
  top: 8%;
  left: 8%;
}

.cross-b {
  top: 43%;
  right: 9%;
  animation-delay: -1.6s;
}

.cross-c {
  right: 24%;
  bottom: 11%;
  animation-delay: -3s;
}

.dna-strand {
  position: absolute;
  width: 126px;
  height: 220px;
  opacity: .22;
  background:
    radial-gradient(circle at 18px 16px, rgba(64, 154, 136, .55) 0 3px, transparent 4px),
    radial-gradient(circle at 108px 42px, rgba(64, 154, 136, .55) 0 3px, transparent 4px),
    radial-gradient(circle at 18px 68px, rgba(64, 154, 136, .55) 0 3px, transparent 4px),
    radial-gradient(circle at 108px 94px, rgba(64, 154, 136, .55) 0 3px, transparent 4px);
  background-size: 126px 108px;
  animation: dnaFade 7s ease-in-out infinite;

  &::before,
  &::after {
    position: absolute;
    inset: 0;
    content: "";
    border-left: 2px dashed rgba(64, 154, 136, .36);
    border-radius: 50%;
  }

  &::after {
    right: 0;
    left: auto;
    transform: scaleX(-1);
  }
}

.strand-a {
  top: 14%;
  right: 5%;
  transform: rotate(16deg);
}

.strand-b {
  bottom: 9%;
  left: 5%;
  transform: rotate(-18deg);
  animation-delay: -2.3s;
}

.pill-float {
  position: absolute;
  width: 72px;
  height: 28px;
  border: 1px solid rgba(78, 176, 154, .25);
  border-radius: 999px;
  background: linear-gradient(90deg, rgba(255, 255, 255, .78) 0 49%, rgba(125, 215, 191, .58) 50% 100%);
  box-shadow: 0 10px 24px rgba(65, 145, 133, .12);
  animation: pillFloat 6.2s ease-in-out infinite;
}

.pill-a {
  top: 22%;
  left: 20%;
  transform: rotate(-18deg);
}

.pill-b {
  right: 18%;
  bottom: 18%;
  transform: rotate(22deg);
  animation-delay: -2s;
}

.showcase-path {
  position: absolute;
  inset: 0;
  z-index: 4;
  pointer-events: none;
}

.path-canvas {
  position: absolute;
  inset: 0;
}

.path-line {
  position: absolute;
  width: 0;
  border-left: 3px dashed rgba(76, 166, 149, .5);
  filter: drop-shadow(0 0 10px rgba(105, 202, 181, .18));
  animation: pathColumn 10s linear infinite;
}

.path-svg {
  display: none;
}

.path-track {
  stroke: rgba(76, 166, 149, .5);
  stroke-width: 3;
  stroke-linecap: round;
  stroke-dasharray: 8 10;
  filter: drop-shadow(0 0 10px rgba(105, 202, 181, .18));
  animation: pathDash 10s linear infinite;
}

.path-node {
  position: absolute;
  width: 18px;
  height: 18px;
  color: rgba(45, 117, 105, .68);
  font-size: 12px;
  font-weight: 900;
  transform: translate(-50%, -50%);
  transition: color .25s ease, transform .25s ease;

  i {
    position: relative;
    display: block;
    width: 16px;
    height: 16px;
    border: 2px solid rgba(80, 174, 153, .54);
    border-radius: 50%;
    background: rgba(250, 255, 252, .86);
    box-shadow: 0 8px 18px rgba(75, 146, 132, .12);
  }

  em {
    position: absolute;
    top: 23px;
    left: 50%;
    padding: 4px 8px;
    border: 1px solid rgba(255, 255, 255, .7);
    border-radius: 8px;
    font-style: normal;
    white-space: nowrap;
    background: rgba(255, 255, 255, .58);
    backdrop-filter: blur(10px);
    transform: translateX(-50%);
  }

  &.active {
    color: #23796c;
    transform: translate(-50%, -50%) scale(1.04);

    i {
      border-color: #62c5a7;
      background: #78d9b8;
      box-shadow:
        0 8px 18px rgba(75, 146, 132, .18),
        0 0 0 8px rgba(120, 217, 184, .18);
      animation: nodePulse 1.8s ease-in-out infinite;
    }
  }
}

.path-progress-dot {
  position: absolute;
  width: 18px;
  height: 18px;
  border: 3px solid rgba(255, 255, 255, .92);
  border-radius: 50%;
  background: #77d9b7;
  box-shadow:
    0 10px 24px rgba(73, 160, 139, .24),
    0 0 0 9px rgba(119, 217, 183, .16);
  transform: translate(-50%, -50%);
  transition: top 120ms linear;
}

.showcase-section {
  position: relative;
  z-index: 1;
  display: grid;
  min-height: calc(100vh - 92px);
  padding: 52px 54px;
  place-items: center;
}

.showcase-inner {
  display: grid;
  width: min(1120px, 100%);
  grid-template-columns: minmax(0, 1fr) 96px minmax(0, 1fr);
  gap: 22px;
  align-items: center;
}

.copy-left {
  .showcase-copy {
    grid-column: 1;
  }

  .showcase-art {
    grid-column: 3;
  }
}

.copy-right {
  .showcase-art {
    grid-column: 1;
  }

  .showcase-copy {
    grid-column: 3;
  }
}

.showcase-art {
  position: relative;
  display: grid;
  min-height: 420px;
  place-items: center;
  opacity: 0;
  transform: translate3d(var(--showcase-x, 0), var(--showcase-y, 0), 0) scale(.98);
  transition:
    opacity 650ms ease,
    transform 650ms cubic-bezier(.2, .86, .24, 1.08);
  will-change: transform, opacity;

  img {
    position: relative;
    z-index: 2;
    display: block;
    width: min(100%, 540px);
    max-height: min(55vh, 500px);
    object-fit: contain;
    filter: drop-shadow(0 26px 36px rgba(70, 148, 135, .16));
  }
}

.art-ring {
  position: absolute;
  z-index: 1;
  width: min(76%, 430px);
  aspect-ratio: 1;
  border: 1px dashed rgba(73, 164, 146, .34);
  border-radius: 50%;
  animation: ringRotate 18s linear infinite;
}

.art-chip {
  position: absolute;
  z-index: 3;
  height: 34px;
  padding: 0 12px;
  border: 1px solid rgba(255, 255, 255, .78);
  border-radius: 8px;
  color: #33786e;
  font-size: 12px;
  font-weight: 800;
  line-height: 32px;
  background: rgba(255, 255, 255, .82);
  box-shadow: 0 12px 26px rgba(77, 145, 134, .14);
  opacity: 0;
  transform: translateY(12px);
  transition:
    opacity 350ms ease 1050ms,
    transform 350ms ease 1050ms;
}

.chip-one {
  top: 16%;
  right: 9%;
}

.chip-two {
  left: 8%;
  bottom: 18%;
  transition-delay: 1180ms;
}

.showcase-copy {
  max-width: 430px;
  color: #265b56;
}

.showcase-step {
  display: inline-flex;
  height: 32px;
  padding: 0 12px;
  margin-bottom: 14px;
  border: 1px solid rgba(73, 164, 146, .32);
  border-radius: 8px;
  color: #459184;
  font-size: 12px;
  font-weight: 900;
  line-height: 30px;
  background: rgba(250, 255, 252, .54);
}

.showcase-copy h2 {
  margin: 0;
  color: #215d55;
  font-size: clamp(30px, 4vw, 48px);
  font-weight: 900;
  line-height: 1.15;
  letter-spacing: 0;
  opacity: 0;
  transform: translateY(-32px);
  transition:
    opacity 300ms ease 650ms,
    transform 300ms ease 650ms;
}

.showcase-copy p {
  margin: 16px 0 0;
  color: #5d8279;
  font-size: 17px;
  line-height: 1.8;
  opacity: 0;
  transform: translateY(12px);
  transition:
    opacity 350ms ease 950ms,
    transform 350ms ease 950ms;
}

.showcase-button {
  min-width: 124px;
  height: 44px;
  margin-top: 22px;
  border: 0;
  border-radius: 999px;
  color: #fff;
  font-size: 15px;
  font-weight: 900;
  background: linear-gradient(135deg, #73d8b9, #49b99d);
  box-shadow: 0 14px 28px rgba(65, 170, 145, .22);
  cursor: pointer;
  opacity: 0;
  transform: translateY(24px);
  transition:
    opacity 350ms ease 1300ms,
    transform 350ms ease 1300ms,
    background .25s ease,
    box-shadow .25s ease;

  &:hover {
    background: linear-gradient(135deg, #63cfad, #3da98e);
    box-shadow: 0 18px 34px rgba(65, 170, 145, .3);
  }
}

.showcase-from-right {
  --showcase-x: 110vw;
  --showcase-y: 0;
}

.showcase-from-left {
  --showcase-x: -110vw;
  --showcase-y: 0;
}

.showcase-from-bottom {
  --showcase-x: 0;
  --showcase-y: 105vh;
}

.showcase-from-corner {
  --showcase-x: 90vw;
  --showcase-y: -68vh;
}

.showcase-section.is-active {
  .showcase-art {
    opacity: 1;
    transform: translate3d(0, 0, 0) scale(1);
  }

  .showcase-copy h2,
  .showcase-copy p,
  .showcase-button,
  .art-chip {
    opacity: 1;
    transform: translateY(0);
  }
}

:deep(.el-progress-bar__outer) {
  background-color: #dcecef;
}

:deep(.el-progress-bar__inner) {
  background: linear-gradient(90deg, #75d5bd, #67b9dc);
}

:deep(.el-tag.el-tag--primary) {
  --el-tag-text-color: #1e8074;
  --el-tag-border-color: #bde5df;
  --el-tag-bg-color: #effbf7;
}

@keyframes pulseDot {
  0%, 100% {
    box-shadow: 0 0 0 5px rgba(101, 201, 168, .18);
  }
  50% {
    box-shadow: 0 0 0 10px rgba(101, 201, 168, .06);
  }
}

@keyframes ecgDraw {
  0% {
    stroke-dashoffset: 520;
    opacity: .2;
  }
  55%, 75% {
    stroke-dashoffset: 0;
    opacity: 1;
  }
  100% {
    stroke-dashoffset: -520;
    opacity: .25;
  }
}

@keyframes floatBadge {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-8px);
  }
}

@keyframes softBreath {
  0%, 100% {
    opacity: .16;
    transform: translateY(0);
  }
  50% {
    opacity: .42;
    transform: translateY(-8px);
  }
}

@keyframes waveFloat {
  0%, 100% {
    transform: translateY(0) rotate(-8deg);
  }
  50% {
    transform: translateY(-12px) rotate(-4deg);
  }
}

@keyframes cardIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes progressGlow {
  0%, 100% {
    filter: brightness(1);
  }
  50% {
    filter: brightness(1.08);
  }
}

@keyframes showcaseBreath {
  0%, 100% {
    opacity: .14;
    transform: translateY(0) scale(.92);
  }
  50% {
    opacity: .36;
    transform: translateY(-8px) scale(1.05);
  }
}

@keyframes dnaFade {
  0%, 100% {
    opacity: .12;
  }
  50% {
    opacity: .28;
  }
}

@keyframes pillFloat {
  0%, 100% {
    margin-top: 0;
  }
  50% {
    margin-top: -12px;
  }
}

@keyframes ringRotate {
  to {
    transform: rotate(360deg);
  }
}

@keyframes pathDash {
  to {
    stroke-dashoffset: -72;
  }
}

@keyframes pathColumn {
  0%, 100% {
    opacity: .68;
  }
  50% {
    opacity: .95;
  }
}

@keyframes nodePulse {
  0%, 100% {
    box-shadow:
      0 8px 18px rgba(75, 146, 132, .18),
      0 0 0 7px rgba(120, 217, 184, .16);
  }
  50% {
    box-shadow:
      0 8px 18px rgba(75, 146, 132, .18),
      0 0 0 12px rgba(120, 217, 184, .06);
  }
}

@media (max-width: 1280px) {
  .home-header {
    grid-template-columns: 1fr;
  }

  .header-status {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1100px) {
  .metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .dashboard-layout {
    grid-template-columns: 1fr;
  }

  .showcase-inner {
    grid-template-columns: minmax(0, 1fr) 72px minmax(0, 1fr);
    gap: 16px;
  }

  .showcase-art {
    min-height: 330px;
  }
}

@media (max-width: 760px) {
  .his-home {
    padding: 14px;
  }

  .header-status,
  .insight-strip,
  .finance-grid {
    grid-template-columns: 1fr;
  }

  .metric-grid {
    grid-template-columns: 1fr;
  }

  .home-header {
    padding: 18px;
  }

  .header-copy h1 {
    font-size: 24px;
  }

  .monitor-grid,
  .queue-summary {
    grid-template-columns: 1fr;
    display: grid;
  }

  .showcase-stage {
    --path-x: 32px;
    margin: 16px -14px -14px;
  }

  .showcase-section {
    min-height: calc(100vh - 58px);
    padding: 34px 18px 34px 62px;
  }

  .showcase-inner {
    grid-template-columns: 1fr;
    gap: 18px;
  }

  .copy-left,
  .copy-right {
    .showcase-art,
    .showcase-copy {
      grid-column: 1;
    }
  }

  .showcase-copy {
    max-width: 620px;
    text-align: center;
  }

  .path-canvas {
    inset: 0;
    width: auto;
    height: auto;
    min-height: 0;
    opacity: .58;
  }

  .path-node em {
    display: none;
  }

  .path-progress-dot {
    width: 15px;
    height: 15px;
  }

  .showcase-art {
    min-height: 250px;

    img {
      max-height: 36vh;
    }
  }

  .art-chip {
    height: 30px;
    font-size: 11px;
    line-height: 28px;
  }

  .chip-one {
    right: 2%;
  }

  .chip-two {
    left: 2%;
  }

  .showcase-copy h2 {
    font-size: 28px;
  }

  .showcase-copy p {
    font-size: 15px;
    line-height: 1.7;
  }
}

/* 完善信息弹窗样式 */
.complete-info-content {
  text-align: center;
  padding: 20px 0;
}

.complete-info-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  border-radius: 50%;
  background: linear-gradient(135deg, #73d8b9, #49b99d);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 40px;
}

.complete-info-content p {
  font-size: 16px;
  color: #607f8c;
  margin: 0;
}

.complete-info-footer {
  display: flex;
  justify-content: center;
  gap: 12px;
}
</style>
