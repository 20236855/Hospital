<template>
  <div class="appointments-page">
    <div class="ambient-layer" aria-hidden="true">
      <span class="mesh mesh-a"></span>
      <span class="mesh mesh-b"></span>
      <span class="grid-plane"></span>
    </div>

    <header class="page-header slide-up-animation">
      <button class="icon-button" type="button" @click="goBack">
        <van-icon name="arrow-left" />
      </button>
      <div class="title-block">
        <span>APPOINTMENTS</span>
        <h1>我的预约</h1>
      </div>
      <button class="icon-button" type="button" @click="onRefresh">
        <van-icon name="replay" />
      </button>
    </header>

    <main class="content-section">
      <section class="overview-panel slide-up-animation" style="animation-delay: 0.06s">
        <div class="overview-copy">
          <p>智慧就诊队列</p>
          <h2>{{ nextAppointment ? '下一次就诊已同步' : '暂无待办就诊' }}</h2>
          <span>{{ nextAppointment ? formatFullDate(nextAppointment.registerTime) : '可以快速发起新的预约挂号' }}</span>
        </div>
        <div class="overview-orbit" aria-hidden="true">
          <span class="orbit-ring ring-a"></span>
          <span class="orbit-ring ring-b"></span>
          <span class="orbit-core">
            <van-icon name="todo-list-o" />
          </span>
        </div>
      </section>

      <section class="stats-grid slide-up-animation" style="animation-delay: 0.12s">
        <div class="stat-card">
          <strong>{{ appointmentList.length }}</strong>
          <span>全部预约</span>
        </div>
        <div class="stat-card">
          <strong>{{ unpaidCount }}</strong>
          <span>待支付</span>
        </div>
        <div class="stat-card">
          <strong>{{ todayCount }}</strong>
          <span>今日就诊</span>
        </div>
      </section>

      <section class="quick-bar slide-up-animation" style="animation-delay: 0.18s">
        <button type="button" @click="goToRegister">
          <van-icon name="add-o" />
          新预约
        </button>
        <button type="button" @click="onRefresh">
          <van-icon name="replay" />
          刷新
        </button>
      </section>

      <section class="filter-section slide-up-animation" style="animation-delay: 0.24s">
        <button
          v-for="tab in filterTabs"
          :key="tab.value"
          class="filter-chip"
          :class="{ active: activeFilter === tab.value }"
          type="button"
          @click="activeFilter = tab.value"
        >
          {{ tab.label }}
        </button>
      </section>

      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list v-model:loading="loading" :finished="finished" :immediate-check="false" @load="onLoad">
          <div v-if="filteredAppointments.length === 0 && !loading" class="empty-state slide-up-animation">
            <div class="empty-visual">
              <span class="empty-ring"></span>
              <van-icon name="calendar-o" />
            </div>
            <h3>{{ activeFilter === 'all' ? '暂无预约记录' : '暂无匹配预约' }}</h3>
            <p>预约、问诊和支付状态会在这里集中展示。</p>
            <button type="button" @click="goToRegister">立即预约</button>
          </div>

          <div v-else class="appointment-list">
            <article
              v-for="(item, index) in filteredAppointments"
              :key="item.registerId || index"
              class="appointment-card slide-up-animation"
              :style="{ animationDelay: `${index * 0.05}s` }"
              @click="viewDetail(item)"
            >
              <div class="card-top">
                <div class="date-tile">
                  <strong>{{ formatDate(item.registerTime).day }}</strong>
                  <span>{{ formatDate(item.registerTime).month }}</span>
                </div>
                <div class="doctor-block">
                  <div class="doctor-row">
                    <span class="doctor-avatar">{{ getAvatarText(item.doctorName) }}</span>
                    <div>
                      <h3>{{ item.doctorName || '接诊医生' }}</h3>
                      <p>{{ item.deptName || '门诊科室' }}</p>
                    </div>
                  </div>
                  <div class="status-row">
                    <span class="status-pill" :class="getStatusClass(item.registerStatus)">
                      {{ getStatusText(item.registerStatus) }}
                    </span>
                    <span class="pay-pill" :class="isPaid(item) ? 'paid' : 'unpaid'">
                      {{ isPaid(item) ? '已支付' : '待支付' }}
                    </span>
                  </div>
                </div>
              </div>

              <div class="info-grid">
                <div>
                  <span>挂号单号</span>
                  <strong>{{ item.registerNo || '-' }}</strong>
                </div>
                <div>
                  <span>挂号级别</span>
                  <strong>{{ item.levelName || '普通号' }}</strong>
                </div>
                <div>
                  <span>挂号类型</span>
                  <strong>{{ getTypeText(item.registerType) }}</strong>
                </div>
                <div>
                  <span>费用</span>
                  <strong class="fee-value">¥{{ item.registerFee || 0 }}</strong>
                </div>
              </div>

              <div class="card-footer">
                <span class="time-line">
                  <van-icon name="clock-o" />
                  {{ formatFullDate(item.registerTime) }}
                </span>
                <button type="button" @click.stop="viewDetail(item)">
                  详情
                  <van-icon name="arrow" />
                </button>
              </div>
            </article>
          </div>
        </van-list>
      </van-pull-refresh>
    </main>

    <van-tabbar v-model="active" route>
      <van-tabbar-item icon="home-o" to="/">首页</van-tabbar-item>
      <van-tabbar-item icon="add-o" to="/register">挂号</van-tabbar-item>
      <van-tabbar-item icon="notes-o" to="/record">病历</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/profile">我的</van-tabbar-item>
    </van-tabbar>

    <van-popup v-model:show="showDetailPopup" position="bottom" round closeable class="detail-shell">
      <div v-if="currentAppointment" class="detail-popup">
        <div class="detail-hero">
          <div>
            <span>预约详情</span>
            <h2>{{ currentAppointment.doctorName || '接诊医生' }}</h2>
            <p>{{ currentAppointment.deptName || '门诊科室' }} · {{ formatFullDate(currentAppointment.registerTime) }}</p>
          </div>
          <span class="detail-status" :class="getStatusClass(currentAppointment.registerStatus)">
            {{ getStatusText(currentAppointment.registerStatus) }}
          </span>
        </div>

        <div class="detail-content">
          <section class="detail-section">
            <div class="section-title">
              <van-icon name="todo-list-o" />
              挂号信息
            </div>
            <div class="info-row">
              <span>挂号单号</span>
              <strong>{{ currentAppointment.registerNo || '-' }}</strong>
            </div>
            <div class="info-row">
              <span>挂号时间</span>
              <strong>{{ formatFullDate(currentAppointment.registerTime) }}</strong>
            </div>
            <div class="info-row">
              <span>挂号类型</span>
              <strong>{{ getTypeText(currentAppointment.registerType) }}</strong>
            </div>
            <div class="info-row">
              <span>挂号级别</span>
              <strong>{{ currentAppointment.levelName || '普通号' }}</strong>
            </div>
          </section>

          <section class="detail-section">
            <div class="section-title">
              <van-icon name="balance-o" />
              支付信息
            </div>
            <div class="info-row">
              <span>支付状态</span>
              <strong :class="isPaid(currentAppointment) ? 'paid-text' : 'unpaid-text'">
                {{ isPaid(currentAppointment) ? '已支付' : '待支付' }}
              </strong>
            </div>
            <div class="info-row">
              <span>挂号费用</span>
              <strong class="fee-value">¥{{ currentAppointment.registerFee || 0 }}</strong>
            </div>
            <div class="info-row">
              <span>创建时间</span>
              <strong>{{ currentAppointment.createTime || '-' }}</strong>
            </div>
          </section>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getRegisterList } from '@/api/register'

const router = useRouter()
const active = ref(3)
const refreshing = ref(false)
const loading = ref(false)
const finished = ref(false)
const appointmentList = ref([])
const pageNum = ref(1)
const activeFilter = ref('all')
const showDetailPopup = ref(false)
const currentAppointment = ref(null)

const filterTabs = [
  { label: '全部', value: 'all' },
  { label: '今日', value: 'today' },
  { label: '待支付', value: 'unpaid' },
  { label: '已支付', value: 'paid' }
]

const isPaid = (item) => item?.payStatus === 'paid'

const isToday = (dateStr) => {
  if (!dateStr) return false
  const date = parseDate(dateStr)
  if (!date) return false
  return date.toDateString() === new Date().toDateString()
}

const filteredAppointments = computed(() => {
  const list = appointmentList.value
  if (activeFilter.value === 'today') return list.filter((item) => isToday(item.registerTime))
  if (activeFilter.value === 'unpaid') return list.filter((item) => !isPaid(item))
  if (activeFilter.value === 'paid') return list.filter((item) => isPaid(item))
  return list
})

const unpaidCount = computed(() => appointmentList.value.filter((item) => !isPaid(item)).length)
const todayCount = computed(() => appointmentList.value.filter((item) => isToday(item.registerTime)).length)
const nextAppointment = computed(() => {
  const now = Date.now()
  return [...appointmentList.value]
    .filter((item) => {
      const date = parseDate(item.registerTime)
      return date && date.getTime() >= now
    })
    .sort((a, b) => parseDate(a.registerTime).getTime() - parseDate(b.registerTime).getTime())[0]
})

const parseDate = (dateStr) => {
  if (!dateStr) return null
  const date = new Date(String(dateStr).replace(/-/g, '/'))
  return Number.isNaN(date.getTime()) ? null : date
}

const formatDate = (dateStr) => {
  const date = parseDate(dateStr)
  if (!date) return { day: '--', month: '--' }
  return {
    day: date.getDate().toString().padStart(2, '0'),
    month: `${date.getMonth() + 1}月`
  }
}

const formatFullDate = (dateStr) => {
  const date = parseDate(dateStr)
  if (!date) return '--'
  return date.toLocaleString('zh-CN', {
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getAvatarText = (name) => (name || '医').slice(0, 1)

const getTypeText = (type) => (type === 'online' ? '线上预约' : '线下挂号')

const getStatusText = (status) => {
  const map = {
    registered: '已挂号',
    cancel: '已取消',
    completed: '已完成'
  }
  return map[status] || '已挂号'
}

const getStatusClass = (status) => {
  if (status === 'completed') return 'completed'
  if (status === 'cancel') return 'cancel'
  return 'registered'
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
    finished.value = true
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

const goToRegister = () => {
  router.push('/register')
}

onMounted(() => {
  onLoad()
})
</script>

<style scoped lang="scss">
.appointments-page {
  min-height: 100vh;
  background: var(--bg-gradient);
  padding-bottom: calc(78px + env(safe-area-inset-bottom));
  position: relative;
  overflow-x: hidden;
  color: var(--text-primary);
}

.ambient-layer {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}

.mesh,
.grid-plane {
  position: absolute;
}

.mesh {
  border-radius: 999px;
  filter: blur(3px);
  animation: drift 9s ease-in-out infinite;
}

.mesh-a {
  width: 260px;
  height: 260px;
  top: -96px;
  right: -88px;
  background: radial-gradient(circle, rgba(185, 225, 205, .76), rgba(185, 225, 205, 0) 68%);
}

.mesh-b {
  width: 230px;
  height: 230px;
  left: -116px;
  top: 290px;
  background: radial-gradient(circle, rgba(142, 214, 242, .52), rgba(142, 214, 242, 0) 70%);
  animation-delay: -3s;
}

.grid-plane {
  inset: 0;
  opacity: .24;
  background-image:
    linear-gradient(rgba(26, 77, 69, .08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(26, 77, 69, .08) 1px, transparent 1px);
  background-size: 28px 28px;
  -webkit-mask-image: linear-gradient(to bottom, rgba(0, 0, 0, .85), transparent 76%);
  mask-image: linear-gradient(to bottom, rgba(0, 0, 0, .85), transparent 76%);
}

.page-header,
.content-section {
  position: relative;
  z-index: 1;
}

.page-header {
  display: grid;
  grid-template-columns: 44px 1fr 44px;
  align-items: center;
  gap: 12px;
  padding: 18px 16px 12px;
}

.icon-button {
  width: 44px;
  height: 44px;
  border: 1px solid rgba(213, 237, 243, .72);
  border-radius: 16px;
  background: rgba(255, 253, 248, .72);
  color: var(--primary-color);
  display: grid;
  place-items: center;
  box-shadow: 0 12px 28px rgba(102, 170, 189, .12);
  -webkit-backdrop-filter: blur(14px);
  backdrop-filter: blur(14px);

  .van-icon {
    font-size: 20px;
  }

  &:active {
    transform: scale(.96);
  }
}

.title-block {
  min-width: 0;

  span {
    color: rgba(26, 77, 69, .58);
    font-size: 10px;
    font-weight: 800;
    letter-spacing: 0;
  }

  h1 {
    margin: 3px 0 0;
    color: var(--primary-color);
    font-size: 24px;
    line-height: 1.15;
  }
}

.content-section {
  padding: 0 16px;
}

.overview-panel {
  min-height: 158px;
  border-radius: 18px;
  padding: 20px;
  overflow: hidden;
  position: relative;
  background:
    linear-gradient(135deg, rgba(255, 253, 248, .88), rgba(255, 253, 248, .52)),
    linear-gradient(120deg, rgba(185, 225, 205, .46), rgba(142, 214, 242, .26));
  border: 1px solid rgba(213, 237, 243, .78);
  box-shadow: 0 24px 56px rgba(102, 170, 189, .18);
  -webkit-backdrop-filter: blur(18px);
  backdrop-filter: blur(18px);

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(110deg, transparent 10%, rgba(255, 253, 248, .58) 46%, transparent 70%);
    transform: translateX(-105%);
    animation: sheen 5s ease-in-out infinite;
  }

  &::before {
    content: '';
    position: absolute;
    right: -40px;
    top: -54px;
    width: 160px;
    height: 160px;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(142, 214, 242, .34), rgba(142, 214, 242, 0) 68%);
  }
}

.overview-copy {
  position: relative;
  z-index: 1;
  max-width: 68%;

  p,
  h2,
  span {
    margin: 0;
  }

  p {
    color: rgba(26, 77, 69, .62);
    font-size: 12px;
    font-weight: 800;
  }

  h2 {
    color: var(--primary-color);
    font-size: 22px;
    line-height: 1.25;
    margin-top: 10px;
  }

  span {
    display: block;
    color: var(--text-regular);
    font-size: 12px;
    line-height: 1.5;
    margin-top: 10px;
  }
}

.overview-orbit {
  position: absolute;
  right: 12px;
  top: 20px;
  width: 122px;
  height: 122px;
  z-index: 1;
}

.orbit-ring,
.orbit-core {
  position: absolute;
  border-radius: 50%;
}

.orbit-ring {
  inset: 0;
  border: 1px solid rgba(26, 77, 69, .12);
  animation: radarBreath 4.2s ease-in-out infinite;
}

.ring-b {
  inset: 18px;
  animation-direction: reverse;
}

.orbit-core {
  inset: 36px;
  display: grid;
  place-items: center;
  background: rgba(255, 253, 248, .74);
  border: 1px solid rgba(213, 237, 243, .72);
  color: #6fbacb;
  font-size: 28px;
  box-shadow: 0 16px 32px rgba(102, 170, 189, .14);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-top: 14px;
}

.stat-card {
  min-height: 70px;
  border-radius: 14px;
  padding: 14px 10px;
  background: rgba(255, 253, 248, .74);
  border: 1px solid rgba(213, 237, 243, .72);
  box-shadow: 0 16px 34px rgba(102, 170, 189, .12);
  -webkit-backdrop-filter: blur(14px);
  backdrop-filter: blur(14px);
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 6px;

  strong {
    color: var(--primary-color);
    font-size: 23px;
    line-height: 1;
  }

  span {
    color: var(--text-light);
    font-size: 11px;
    font-weight: 800;
  }
}

.quick-bar {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-top: 14px;

  button {
    height: 44px;
    border: 0;
    border-radius: 12px;
    background: rgba(255, 253, 248, .76);
    color: var(--primary-color);
    font-size: 14px;
    font-weight: 800;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    box-shadow: 0 14px 30px rgba(102, 170, 189, .1);
  }
}

.filter-section {
  display: flex;
  gap: 9px;
  overflow-x: auto;
  padding: 18px 0 12px;
  scrollbar-width: none;

  &::-webkit-scrollbar {
    display: none;
  }
}

.filter-chip {
  height: 34px;
  border: 1px solid rgba(213, 237, 243, .78);
  border-radius: 10px;
  padding: 0 14px;
  flex: 0 0 auto;
  background: rgba(255, 253, 248, .68);
  color: rgba(26, 77, 69, .62);
  font-size: 13px;
  font-weight: 800;

  &.active {
    color: #4f9fb4;
    background: linear-gradient(135deg, rgba(185, 225, 205, .34), rgba(142, 214, 242, .22));
    border-color: rgba(142, 214, 242, .34);
    box-shadow: 0 12px 28px rgba(102, 170, 189, .14);
  }
}

.empty-state {
  min-height: 310px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 34px 20px;
  border-radius: 16px;
  background: rgba(255, 253, 248, .68);
  border: 1px solid rgba(213, 237, 243, .72);

  h3 {
    margin: 18px 0 8px;
    color: var(--primary-color);
    font-size: 18px;
  }

  p {
    margin: 0;
    color: var(--text-regular);
    font-size: 13px;
    line-height: 1.55;
  }

  button {
    margin-top: 20px;
    height: 42px;
    border: 0;
    border-radius: 10px;
    padding: 0 22px;
    color: var(--primary-color);
    background: linear-gradient(135deg, rgba(185, 225, 205, .62), rgba(142, 214, 242, .32));
    font-weight: 800;
    box-shadow: 0 12px 26px rgba(102, 170, 189, .14);
  }
}

.empty-visual {
  width: 86px;
  height: 86px;
  border-radius: 18px;
  display: grid;
  place-items: center;
  position: relative;
  color: #6fbacb;
  font-size: 36px;
  background: rgba(185, 225, 205, .18);
}

.empty-ring {
  position: absolute;
  inset: -8px;
  border-radius: 22px;
  border: 1px solid rgba(142, 214, 242, .34);
  animation: iconHalo 3.4s ease-in-out infinite;
}

.appointment-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.appointment-card {
  border-radius: 14px;
  padding: 12px;
  background: rgba(255, 253, 248, .76);
  border: 1px solid rgba(213, 237, 243, .72);
  box-shadow: 0 18px 44px rgba(102, 170, 189, .13);
  -webkit-backdrop-filter: blur(14px);
  backdrop-filter: blur(14px);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 16px;
    bottom: 16px;
    width: 2px;
    border-radius: 4px;
    background: linear-gradient(to bottom, rgba(185, 225, 205, .2), rgba(142, 214, 242, .78), rgba(185, 225, 205, .2));
  }

  &:active {
    transform: scale(.99);
  }
}

.card-top {
  display: grid;
  grid-template-columns: 54px 1fr;
  gap: 10px;
}

.date-tile {
  height: 58px;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(145deg, rgba(185, 225, 205, .72), rgba(142, 214, 242, .38));
  border: 1px solid rgba(255, 253, 248, .5);
  box-shadow: 0 16px 28px rgba(102, 170, 189, .13);

  strong {
    color: var(--primary-color);
    font-size: 21px;
    line-height: 1;
  }

  span {
    color: rgba(26, 77, 69, .62);
    font-size: 11px;
    font-weight: 800;
    margin-top: 5px;
  }
}

.doctor-block {
  min-width: 0;
}

.doctor-row {
  display: flex;
  align-items: center;
  gap: 8px;

  h3,
  p {
    margin: 0;
  }

  h3 {
    color: var(--primary-color);
    font-size: 15px;
    line-height: 1.2;
  }

  p {
    color: var(--text-regular);
    font-size: 12px;
    margin-top: 3px;
  }
}

.doctor-avatar {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, rgba(185, 225, 205, .86), rgba(142, 214, 242, .54));
  color: #fffdf8;
  font-size: 15px;
  font-weight: 800;
  flex: 0 0 auto;
}

.status-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

.status-pill,
.pay-pill,
.detail-status {
  min-height: 26px;
  border-radius: 8px;
  padding: 4px 8px;
  display: inline-flex;
  align-items: center;
  font-size: 12px;
  font-weight: 800;
}

.registered {
  color: #5f9e8c;
  background: rgba(104, 199, 169, .14);
}

.completed,
.paid {
  color: #4f9fb4;
  background: rgba(142, 214, 242, .16);
}

.cancel {
  color: #8fa5ad;
  background: rgba(165, 184, 192, .14);
}

.unpaid {
  color: #ef8fa9;
  background: rgba(239, 143, 169, .13);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  margin-top: 12px;

  div {
    min-width: 0;
    border-radius: 10px;
    padding: 8px;
    background: rgba(185, 225, 205, .1);
    border: 1px solid rgba(213, 237, 243, .5);
  }

  span,
  strong {
    display: block;
  }

  span {
    color: var(--text-light);
    font-size: 10px;
    font-weight: 800;
  }

  strong {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: var(--text-primary);
    font-size: 12px;
    margin-top: 5px;
  }
}

.fee-value {
  color: #ef8fa9 !important;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid rgba(213, 237, 243, .52);

  button {
    border: 0;
    border-radius: 8px;
    height: 30px;
    padding: 0 10px;
    flex: 0 0 auto;
    display: inline-flex;
    align-items: center;
    gap: 3px;
    background: linear-gradient(135deg, rgba(185, 225, 205, .48), rgba(142, 214, 242, .26));
    color: var(--primary-color);
    font-size: 12px;
    font-weight: 800;
    box-shadow: 0 10px 22px rgba(102, 170, 189, .12);
  }
}

.time-line {
  min-width: 0;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: var(--text-regular);
  font-size: 12px;

  .van-icon {
    color: #6fbacb;
    flex: 0 0 auto;
  }
}

.detail-shell {
  height: 78%;
}

.detail-popup {
  min-height: 100%;
  background: var(--bg-gradient);
}

.detail-hero {
  min-height: 128px;
  padding: 24px 22px 20px;
  display: flex;
  justify-content: space-between;
  gap: 14px;
  background:
    linear-gradient(135deg, rgba(255, 253, 248, .9), rgba(255, 253, 248, .58)),
    linear-gradient(120deg, rgba(185, 225, 205, .48), rgba(142, 214, 242, .28));
  border-bottom: 1px solid rgba(213, 237, 243, .72);

  span,
  h2,
  p {
    margin: 0;
  }

  > div > span {
    color: rgba(26, 77, 69, .58);
    font-size: 12px;
    font-weight: 800;
  }

  h2 {
    color: var(--primary-color);
    font-size: 23px;
    margin-top: 8px;
  }

  p {
    color: var(--text-regular);
    font-size: 13px;
    margin-top: 8px;
    line-height: 1.45;
  }
}

.detail-status {
  flex: 0 0 auto;
  height: 28px;
}

.detail-content {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.detail-section {
  border-radius: 14px;
  padding: 16px;
  background: rgba(255, 253, 248, .78);
  border: 1px solid rgba(213, 237, 243, .72);
  box-shadow: 0 14px 34px rgba(102, 170, 189, .1);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--primary-color);
  font-size: 15px;
  font-weight: 800;
  margin-bottom: 10px;

  .van-icon {
    color: #6fbacb;
    font-size: 18px;
  }
}

.info-row {
  min-height: 42px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 14px;
  border-top: 1px solid rgba(213, 237, 243, .44);

  span {
    color: var(--text-light);
    font-size: 13px;
    flex: 0 0 auto;
  }

  strong {
    min-width: 0;
    color: var(--text-primary);
    font-size: 13px;
    text-align: right;
    overflow-wrap: anywhere;
  }
}

.paid-text {
  color: #4f9fb4 !important;
}

.unpaid-text {
  color: #ef8fa9 !important;
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
    transform: translateX(-105%);
  }
  78%, 100% {
    transform: translateX(105%);
  }
}

@keyframes radarBreath {
  0%, 100% {
    opacity: .42;
    transform: scale(.96);
  }
  50% {
    opacity: .82;
    transform: scale(1.04);
  }
}

@keyframes iconHalo {
  0%, 100% {
    opacity: 0;
    transform: scale(.92);
  }
  45% {
    opacity: 1;
    transform: scale(1.06);
  }
}

@media (max-width: 360px) {
  .content-section,
  .page-header {
    padding-left: 12px;
    padding-right: 12px;
  }

  .overview-copy {
    max-width: 72%;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
