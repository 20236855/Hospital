<template>
  <div class="resource-page">
    <header class="top-bar">
      <button class="back-btn" type="button" @click="goBack">
        <van-icon name="arrow-left" />
      </button>
      <div>
        <p>HOSPITAL RESOURCE</p>
        <h1>医院资源</h1>
      </div>
    </header>

    <section class="summary-panel">
      <div class="summary-copy">
        <span class="summary-label">优质医疗资源</span>
        <strong>覆盖门诊医生、重点科室与专长介绍</strong>
        <p>可按姓名、科室、职称快速查找医生，进入详情查看医生简介与擅长方向。</p>
      </div>
      <div class="summary-grid">
        <div>
          <strong>{{ total || doctorList.length }}</strong>
          <span>医生</span>
        </div>
        <div>
          <strong>{{ deptCount }}</strong>
          <span>科室</span>
        </div>
        <div>
          <strong>{{ seniorCount }}</strong>
          <span>高级职称</span>
        </div>
      </div>
    </section>

    <section class="search-section">
      <van-search
        v-model="searchName"
        placeholder="搜索医生姓名、科室、擅长方向"
        shape="round"
        background="transparent"
        @update:model-value="handleSearch"
      />
      <div class="dept-scroll">
        <button
          v-for="item in deptFilters"
          :key="item.value"
          type="button"
          class="dept-chip"
          :class="{ active: activeDept === item.value }"
          @click="selectDept(item.value)"
        >
          {{ item.label }}
        </button>
      </div>
    </section>

    <section class="resource-section">
      <div class="section-title">
        <div>
          <span>DOCTORS</span>
          <h2>医生团队</h2>
        </div>
        <em>{{ filteredDoctors.length }} 位</em>
      </div>

      <div v-if="loading" class="loading-state">
        <van-loading color="#2563eb" />
        <span>正在加载医生资源</span>
      </div>

      <template v-else>
        <article
          v-for="doctor in filteredDoctors"
          :key="doctor.doctorId"
          class="doctor-card"
          @click="goToDetail(doctor.doctorId)"
        >
          <div class="avatar-box">
            <img v-if="doctor.avatar" :src="doctor.avatar" alt="" @error="hideBrokenAvatar" />
            <span v-else>{{ getInitial(doctor.doctorName) }}</span>
          </div>
          <div class="doctor-main">
            <div class="doctor-head">
              <div>
                <h3>{{ doctor.doctorName || '未命名医生' }}</h3>
                <p>{{ getDeptName(doctor) }} · {{ getLevelName(doctor) }}</p>
              </div>
              <span class="status-pill" :class="{ off: doctor.status !== '0' }">
                {{ doctor.status === '0' ? '可预约' : '停诊' }}
              </span>
            </div>

            <div class="tag-row">
              <span>{{ getGenderText(doctor.gender) }}</span>
              <span>{{ getLevelName(doctor) }}</span>
              <span>{{ doctor.doctorNo || '院内医生' }}</span>
            </div>

            <p class="specialty">
              <b>擅长</b>
              {{ doctor.specialty || getDefaultSpecialty(doctor) }}
            </p>
            <p class="intro">{{ doctor.introduction || getDefaultIntro(doctor) }}</p>

            <div class="card-footer">
              <span>查看医生详情</span>
              <van-icon name="arrow" />
            </div>
          </div>
        </article>

        <div v-if="filteredDoctors.length === 0" class="empty-state">
          <van-icon name="friends-o" />
          <strong>暂无医生资源</strong>
          <span>请更换筛选条件后重试</span>
        </div>
      </template>
    </section>

    <div v-if="!loading && total > doctorList.length && !searchName && !activeDept" class="load-more">
      <button type="button" :disabled="loadingMore" @click="loadMore">
        {{ loadingMore ? '加载中...' : '加载更多医生' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listDoctor } from '@/api/doctor'
import { getDeptList } from '@/api/register'

const router = useRouter()
const loading = ref(false)
const loadingMore = ref(false)
const total = ref(0)
const doctorList = ref([])
const deptList = ref([])
const searchName = ref('')
const activeDept = ref('')
let searchTimer = null

const levelNameMap = {
  1: '普通号',
  2: '专家号',
  3: '主任号'
}

const queryParams = ref({
  pageNum: 1,
  pageSize: 100,
  doctorName: undefined
})

const deptFilters = computed(() => {
  const ids = Array.from(new Set(doctorList.value.map((item) => item.deptId).filter(Boolean)))
  return [
    { label: '全部', value: '' },
    ...ids.map((id) => ({ label: getDeptName({ deptId: id }), value: id }))
  ]
})

const filteredDoctors = computed(() => {
  const keyword = searchName.value.trim().toLowerCase()
  return doctorList.value.filter((doctor) => {
    const matchDept = !activeDept.value || String(doctor.deptId) === String(activeDept.value)
    if (!keyword) return matchDept
    const text = [
      doctor.doctorName,
      doctor.levelName,
      getDeptName(doctor),
      doctor.specialty,
      doctor.introduction
    ].filter(Boolean).join(' ').toLowerCase()
    return matchDept && text.includes(keyword)
  })
})

const deptCount = computed(() => new Set(doctorList.value.map((item) => item.deptId).filter(Boolean)).size)
const seniorCount = computed(() => doctorList.value.filter((item) => Number(item.levelId) >= 2 || /专家|主任/.test(item.levelName || '')).length)

function getDeptName(doctor) {
  const dept = deptList.value.find((item) => String(item.deptId) === String(doctor?.deptId))
  return doctor?.deptName || dept?.deptName || (doctor?.deptId ? `科室${doctor.deptId}` : '门诊科室')
}

function getLevelName(doctor) {
  return doctor?.levelName || levelNameMap[doctor?.levelId] || '门诊医生'
}

function getGenderText(value) {
  if (!value) return '专业医生'
  return value === '0' ? '男医生' : value === '1' ? '女医生' : value
}

function getInitial(name) {
  return (name || '医').slice(0, 1)
}

function getDefaultSpecialty(doctor) {
  return `${getDeptName(doctor)}常见病、多发病诊疗及会诊评估`
}

function getDefaultIntro(doctor) {
  return `${doctor?.doctorName || '该医生'}长期参与${getDeptName(doctor)}临床诊疗工作，注重规范化诊疗与患者沟通。`
}

function fetchDoctorList(append = false) {
  if (append) {
    loadingMore.value = true
  } else {
    loading.value = true
  }

  listDoctor(queryParams.value).then((response) => {
    const rows = response.rows || []
    doctorList.value = append ? [...doctorList.value, ...rows] : rows
    total.value = response.total || doctorList.value.length
  }).finally(() => {
    loading.value = false
    loadingMore.value = false
  })
}

function fetchDeptList() {
  return getDeptList({ pageNum: 1, pageSize: 500 }).then((response) => {
    deptList.value = response.rows || response.data || []
  }).catch(() => {
    deptList.value = []
  })
}

function handleSearch() {
  window.clearTimeout(searchTimer)
  searchTimer = window.setTimeout(() => {
    activeDept.value = ''
  }, 260)
}

function selectDept(deptId) {
  activeDept.value = deptId
}

function loadMore() {
  if (loadingMore.value) return
  queryParams.value.pageNum += 1
  fetchDoctorList(true)
}

function goToDetail(doctorId) {
  router.push(`/doctor/${doctorId}`)
}

function goBack() {
  router.back()
}

function hideBrokenAvatar(event) {
  event.target.style.display = 'none'
}

onMounted(() => {
  fetchDeptList()
  fetchDoctorList()
})
</script>

<style scoped lang="scss">
.resource-page {
  min-height: 100vh;
  padding: 75px 14px 34px;
  background:
    linear-gradient(180deg, rgba(231, 242, 255, .9) 0%, rgba(247, 251, 255, 1) 44%),
    #f4f9ff;
  color: #1e3a5f;
}

.top-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 14px;
  background: #1250af;
  color: #fff;

  p {
    margin: 0 0 1px;
    font-size: 15px;
    line-height: 1.2;
    color: #fff;
  }

  h1 {
    margin: 0;
    font-size: 17px;
    line-height: 1.2;
    color: #fff;
  }
}

.back-btn {
  width: 32px;
  height: 32px;
  border: 0;
  border-radius: 8px;
  background: rgba(255, 255, 255, .18);
  color: #fff;
  backdrop-filter: blur(4px);
  box-shadow: 0 4px 12px rgba(15, 40, 80, .2);
}

.summary-panel {
  border-radius: 18px;
  padding: 16px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, .95), rgba(255, 255, 255, .76)),
    linear-gradient(120deg, rgba(96, 165, 250, .18), rgba(59, 130, 246, .2));
  border: 1px solid rgba(191, 219, 254, .9);
  box-shadow: 0 18px 44px rgba(37, 99, 235, .13);
}

.summary-copy {
  strong,
  p,
  span {
    display: block;
  }

  .summary-label {
    width: fit-content;
    padding: 4px 8px;
    border-radius: 8px;
    background: rgba(52, 123, 112, .1);
    color: #2563eb;
    font-size: 11px;
    font-weight: 800;
  }

  strong {
    margin-top: 10px;
    font-size: 18px;
    line-height: 1.35;
    color: #1e3a5f;
  }

  p {
    margin: 7px 0 0;
    color: #64748b;
    font-size: 13px;
    line-height: 1.55;
  }
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  margin-top: 14px;

  div {
    min-height: 58px;
    border-radius: 12px;
    padding: 10px;
    background: rgba(219, 234, 254, .72);
    border: 1px solid rgba(191, 219, 254, .72);
  }

  strong {
    display: block;
    color: #1d4ed8;
    font-size: 19px;
    line-height: 1;
  }

  span {
    display: block;
    margin-top: 7px;
    color: #64748b;
    font-size: 11px;
    font-weight: 700;
  }
}

.search-section {
  margin-top: 12px;

  :deep(.van-search) {
    padding: 0;
  }

  :deep(.van-search__content) {
    background: #fff;
    border: 1px solid rgba(206, 232, 238, .8);
    box-shadow: 0 10px 28px rgba(75, 132, 145, .1);
  }
}

.dept-scroll {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 10px 2px 2px;
  scrollbar-width: none;

  &::-webkit-scrollbar {
    display: none;
  }
}

.dept-chip {
  flex: 0 0 auto;
  height: 32px;
  border: 1px solid rgba(206, 232, 238, .95);
  border-radius: 999px;
  padding: 0 13px;
  background: #fff;
  color: #475569;
  font-size: 12px;
  font-weight: 800;

  &.active {
    border-color: #2563eb;
    background: #2563eb;
    color: #fff;
  }
}

.resource-section {
  margin-top: 16px;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 10px;

  span {
    color: #64748b;
    font-size: 10px;
    font-weight: 800;
    letter-spacing: 0;
  }

  h2 {
    margin: 2px 0 0;
    color: #1e3a5f;
    font-size: 18px;
  }

  em {
    font-style: normal;
    color: #2563eb;
    font-size: 12px;
    font-weight: 800;
  }
}

.doctor-card {
  display: grid;
  grid-template-columns: 66px 1fr;
  gap: 12px;
  padding: 14px;
  margin-bottom: 12px;
  border-radius: 16px;
  background: rgba(255, 255, 255, .94);
  border: 1px solid rgba(211, 232, 237, .86);
  box-shadow: 0 12px 30px rgba(75, 132, 145, .1);
  transition: transform .18s ease, box-shadow .18s ease;

  &:active {
    transform: scale(.985);
    box-shadow: 0 8px 20px rgba(75, 132, 145, .12);
  }
}

.avatar-box {
  width: 66px;
  height: 76px;
  border-radius: 14px;
  overflow: hidden;
  background: linear-gradient(145deg, #dbeafe, #bfdbfe);
  display: grid;
  place-items: center;
  color: #1d4ed8;
  font-size: 25px;
  font-weight: 900;
  border: 1px solid rgba(255, 255, 255, .78);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.doctor-main {
  min-width: 0;
}

.doctor-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8px;

  h3 {
    margin: 0;
    color: #1e3a5f;
    font-size: 17px;
    line-height: 1.25;
  }

  p {
    margin: 4px 0 0;
    color: #64748b;
    font-size: 12px;
    line-height: 1.35;
  }
}

.status-pill {
  flex: 0 0 auto;
  height: 24px;
  border-radius: 8px;
  padding: 0 8px;
  display: inline-flex;
  align-items: center;
  background: rgba(52, 123, 112, .1);
  color: #2563eb;
  font-size: 11px;
  font-weight: 900;

  &.off {
    background: rgba(151, 164, 170, .14);
    color: #64748b;
  }
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 9px;

  span {
    max-width: 100%;
    min-height: 22px;
    border-radius: 7px;
    padding: 4px 7px;
    background: #f0f5ff;
    color: #64748b;
    font-size: 11px;
    line-height: 1.2;
  }
}

.specialty,
.intro {
  margin: 9px 0 0;
  color: #334155;
  font-size: 13px;
  line-height: 1.55;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.specialty {
  -webkit-line-clamp: 2;

  b {
    margin-right: 5px;
    color: #d35f45;
  }
}

.intro {
  -webkit-line-clamp: 2;
  color: #64748b;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 3px;
  margin-top: 10px;
  color: #2563eb;
  font-size: 12px;
  font-weight: 900;
}

.loading-state,
.empty-state {
  min-height: 190px;
  border-radius: 16px;
  background: rgba(255, 255, 255, .76);
  border: 1px solid rgba(211, 232, 237, .86);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #64748b;
  gap: 10px;
}

.empty-state {
  .van-icon {
    color: #60a5fa;
    font-size: 42px;
  }

  strong {
    color: #1e3a5f;
    font-size: 16px;
  }

  span {
    font-size: 13px;
  }
}

.load-more {
  padding: 4px 0 18px;
  text-align: center;

  button {
    height: 38px;
    border: 0;
    border-radius: 999px;
    padding: 0 22px;
    background: #2563eb;
    color: #fff;
    font-size: 13px;
    font-weight: 900;

    &:disabled {
      opacity: .68;
    }
  }
}

@media (max-width: 360px) {
  .resource-page {
    padding-left: 10px;
    padding-right: 10px;
  }

  .doctor-card {
    grid-template-columns: 58px 1fr;
    gap: 10px;
    padding: 12px;
  }

  .avatar-box {
    width: 58px;
    height: 70px;
  }
}
</style>
