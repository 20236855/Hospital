<template>
  <div class="exam-page">
    <header class="page-header">
      <button class="back-btn" type="button" @click="goBack">
        <van-icon name="arrow-left" />
      </button>
      <div>
        <p>EXAM REPORTS</p>
        <h1>我的检查</h1>
      </div>
    </header>

    <section class="exam-summary">
      <div class="summary-main">
        <span>检查检验报告</span>
        <strong>{{ reports.length }}</strong>
        <em>按申请单归档展示</em>
      </div>
      <div>
        <span>已出报告</span>
        <strong>{{ reportedCount }}</strong>
        <em>可查看结果详情</em>
      </div>
    </section>

    <section v-if="abnormalCount > 0" class="notice-card">
      <van-icon name="warning-o" />
      <div>
        <strong>{{ abnormalCount }} 项指标需要关注</strong>
        <span>结果仅供就诊参考，请结合医生诊断意见查看。</span>
      </div>
    </section>

    <van-tabs v-model:active="activeTab" sticky color="#347b70" title-active-color="#173e48">
      <van-tab title="全部">
        <ExamList :items="reports" :loading="loading" @open="openDetail" />
      </van-tab>
      <van-tab :title="`异常 ${abnormalReports.length}`">
        <ExamList
          :items="abnormalReports"
          :loading="loading"
          empty-text="暂无异常检查结果"
          @open="openDetail"
        />
      </van-tab>
    </van-tabs>

    <van-popup
      v-model:show="showDetail"
      position="bottom"
      round
      closeable
      :style="{ height: '82%' }"
    >
      <div v-if="currentReport" class="exam-detail">
        <div class="detail-header">
          <span>{{ currentReport.applyTypeName || '检查检验' }}报告</span>
          <h2>{{ currentReport.techName || '检查项目' }}</h2>
          <p>{{ formatDateTime(currentReport.reportTime || currentReport.examTime || currentReport.applyTime) }}</p>
        </div>

        <div class="detail-status">
          <div>
            <span>申请状态</span>
            <strong>{{ currentReport.applyStatus || '已申请' }}</strong>
          </div>
          <div>
            <span>报告状态</span>
            <strong>{{ hasResults(currentReport) ? '已出结果' : '等待结果' }}</strong>
          </div>
        </div>

        <section v-if="currentReport.applyInfo || currentReport.applyPosition" class="detail-section">
          <h3>申请信息</h3>
          <p v-if="currentReport.applyInfo">{{ currentReport.applyInfo }}</p>
          <p v-if="currentReport.applyPosition">检查部位：{{ currentReport.applyPosition }}</p>
        </section>

        <section class="detail-section">
          <h3>结果明细</h3>
          <div v-if="!hasResults(currentReport)" class="pending-result">
            <van-icon name="clock-o" />
            <span>该申请暂未发布检查检验结果，请稍后查看。</span>
          </div>
          <div class="result-detail-list">
            <article
              v-for="item in currentReport.results"
              :key="item.resultId"
              class="result-detail-item"
              :class="{ abnormal: isAbnormal(item.abnormalFlag) }"
            >
              <div class="detail-item-head">
                <strong>{{ item.itemName || item.itemCode || '结果项目' }}</strong>
                <span>{{ abnormalText(item.abnormalFlag) }}</span>
              </div>
              <div v-if="item.resultValue" class="item-value">
                {{ item.resultValue }}<small v-if="item.resultUnit">{{ item.resultUnit }}</small>
              </div>
              <p v-if="item.referenceRange">参考范围：{{ item.referenceRange }}</p>
              <p v-if="item.imageFind">检查所见：{{ item.imageFind }}</p>
              <p v-if="item.diagnosisOpinion">诊断意见：{{ item.diagnosisOpinion }}</p>
              <p v-if="item.suggestion">建议：{{ item.suggestion }}</p>
              <img v-if="item.imageUrl" :src="item.imageUrl" alt="检查图像" />
            </article>
          </div>
        </section>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { computed, defineComponent, h, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getMyExamReports } from '@/api/exam'

const router = useRouter()
const activeTab = ref(0)
const loading = ref(false)
const reports = ref([])
const showDetail = ref(false)
const currentReport = ref(null)

const abnormalReports = computed(() => reports.value.filter((item) => Number(item.abnormalCount || 0) > 0))
const abnormalCount = computed(() => reports.value.reduce((sum, item) => sum + Number(item.abnormalCount || 0), 0))
const reportedCount = computed(() => reports.value.filter((item) => hasResults(item)).length)

const ExamList = defineComponent({
  name: 'ExamList',
  props: {
    items: { type: Array, default: () => [] },
    loading: { type: Boolean, default: false },
    emptyText: { type: String, default: '暂无检查检验报告' }
  },
  emits: ['open'],
  setup(props, { emit }) {
    return () => h('div', { class: 'exam-list' }, [
      props.items.length === 0 && !props.loading
        ? h('div', { class: 'empty-state' }, [
          h('i', { class: 'van-badge__wrapper van-icon van-icon-description-o' }),
          h('strong', props.emptyText),
          h('span', '完成检查检验并发布报告后会显示在这里')
        ])
        : props.items.map((item) => h('article', {
          class: ['exam-card', Number(item.abnormalCount || 0) > 0 ? 'has-abnormal' : 'normal'],
          key: item.applyId,
          onClick: () => emit('open', item)
        }, [
          h('div', { class: 'exam-card-head' }, [
            h('div', [
              h('h3', item.techName || '检查检验项目'),
              h('p', `${item.deptName || '执行科室'} · ${item.doctorName || '开单医生'}`)
            ]),
            h('span', { class: ['report-tag', Number(item.abnormalCount || 0) > 0 ? 'warn' : 'ok'] },
              Number(item.abnormalCount || 0) > 0 ? `${item.abnormalCount}项异常` : '结果正常')
          ]),
          h('div', { class: 'exam-meta' }, [
            h('span', `申请单号：${item.applyId || '-'}`),
            h('span', `报告时间：${formatDateTime(item.reportTime || item.examTime || item.applyTime)}`)
          ]),
          h('div', { class: 'result-preview' }, previewResults(item.results).map((result) =>
            h('span', { class: isAbnormal(result.abnormalFlag) ? 'abnormal' : '' },
              `${result.itemName || result.itemCode || '项目'}${result.resultValue ? ` ${result.resultValue}${result.resultUnit || ''}` : ''}`)
          )),
          h('div', { class: 'exam-card-foot' }, [
            h('strong', hasResults(item) ? '已出结果' : '等待结果'),
            h('span', `${item.resultCount || 0} 项结果 · ${item.applyStatus || '已申请'}`),
            h('i', { class: 'van-badge__wrapper van-icon van-icon-arrow' })
          ])
        ]))
    ])
  }
})

async function loadReports() {
  loading.value = true
  try {
    const response = await getMyExamReports()
    reports.value = response.data || []
  } finally {
    loading.value = false
  }
}

function previewResults(results = []) {
  return results.slice(0, 3)
}

function openDetail(item) {
  currentReport.value = item
  showDetail.value = true
}

function goBack() {
  router.back()
}

function isAbnormal(flag) {
  const value = String(flag || '').trim()
  return value && value !== '0' && value !== '4'
}

function hasResults(report) {
  return Array.isArray(report?.results) && report.results.length > 0
}

function abnormalText(flag) {
  const map = {
    0: '正常',
    1: '偏高',
    2: '偏低',
    3: '阳性',
    4: '阴性'
  }
  return map[String(flag || '0')] || '需关注'
}

function formatDateTime(value) {
  if (!value) return '-'
  const date = new Date(String(value).replace(/-/g, '/'))
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(loadReports)
</script>

<style scoped lang="scss">
.exam-page {
  min-height: 100vh;
  padding: 14px 14px 36px;
  background:
    linear-gradient(180deg, rgba(232, 248, 246, .96) 0%, rgba(247, 250, 252, 1) 42%),
    #f7fafc;
  color: #183f4a;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 2px 12px;

  p {
    margin: 0 0 2px;
    color: #6f93a0;
    font-size: 10px;
    font-weight: 800;
    letter-spacing: 0;
  }

  h1 {
    margin: 0;
    color: #173e48;
    font-size: 24px;
    line-height: 1.2;
  }
}

.back-btn {
  width: 38px;
  height: 38px;
  border: 0;
  border-radius: 10px;
  background: rgba(255, 255, 255, .9);
  color: #315f68;
  box-shadow: 0 8px 22px rgba(81, 137, 151, .13);
}

.exam-summary {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;

  > div {
    min-height: 104px;
    border-radius: 16px;
    padding: 14px;
    background:
      linear-gradient(135deg, rgba(255, 255, 255, .96), rgba(255, 255, 255, .78)),
      linear-gradient(120deg, rgba(108, 181, 194, .18), rgba(123, 207, 170, .2));
    border: 1px solid rgba(206, 232, 238, .9);
    box-shadow: 0 14px 34px rgba(75, 132, 145, .12);
  }

  span,
  strong,
  em {
    display: block;
  }

  span {
    color: #6f8790;
    font-size: 12px;
    font-weight: 800;
  }

  strong {
    margin-top: 10px;
    color: #276b63;
    font-size: 26px;
    line-height: 1;
  }

  em {
    margin-top: 9px;
    color: #7d9299;
    font-size: 11px;
    font-style: normal;
  }
}

.notice-card {
  display: grid;
  grid-template-columns: 34px 1fr;
  gap: 10px;
  align-items: center;
  margin-top: 12px;
  padding: 12px;
  border-radius: 14px;
  background: #fff8ec;
  border: 1px solid rgba(217, 128, 34, .18);
  color: #b66b16;

  .van-icon {
    font-size: 24px;
  }

  strong,
  span {
    display: block;
  }

  strong {
    font-size: 14px;
  }

  span {
    margin-top: 4px;
    font-size: 12px;
    line-height: 1.45;
  }
}

:deep(.van-tabs) {
  margin-top: 12px;
  background: transparent;
}

:deep(.van-tabs__nav) {
  background: rgba(255, 255, 255, .88);
  border-radius: 12px;
}

:deep(.van-tabs__wrap) {
  border-radius: 12px;
  overflow: hidden;
}

.exam-list {
  padding-top: 12px;
}

:deep(.exam-card) {
  margin-bottom: 12px;
  padding: 14px;
  border-radius: 16px;
  background: rgba(255, 255, 255, .96);
  border: 1px solid rgba(211, 232, 237, .86);
  box-shadow: 0 12px 30px rgba(75, 132, 145, .1);

  &.has-abnormal {
    border-color: rgba(217, 128, 34, .28);
  }
}

:deep(.exam-card-head) {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;

  h3 {
    margin: 0;
    color: #173e48;
    font-size: 16px;
    line-height: 1.3;
  }

  p {
    margin: 5px 0 0;
    color: #687f87;
    font-size: 12px;
  }
}

:deep(.report-tag) {
  flex: 0 0 auto;
  height: 24px;
  border-radius: 8px;
  padding: 0 8px;
  display: inline-flex;
  align-items: center;
  font-size: 11px;
  font-weight: 900;

  &.ok {
    background: rgba(52, 123, 112, .1);
    color: #347b70;
  }

  &.warn {
    background: #fff4e3;
    color: #c46e14;
  }
}

:deep(.exam-meta) {
  display: flex;
  flex-direction: column;
  gap: 5px;
  margin-top: 12px;
  padding: 10px;
  border-radius: 12px;
  background: #f5fafb;
  color: #708890;
  font-size: 12px;
}

:deep(.result-preview) {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 12px;

  span {
    max-width: 100%;
    padding: 6px 8px;
    border-radius: 999px;
    background: rgba(52, 123, 112, .08);
    color: #347b70;
    font-size: 11px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;

    &.abnormal {
      background: #fff4e3;
      color: #c46e14;
    }
  }
}

:deep(.exam-card-foot) {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 10px;
  align-items: center;
  margin-top: 12px;

  strong {
    color: #173e48;
    font-size: 22px;
  }

  span {
    color: #708890;
    font-size: 12px;
  }

  .van-icon {
    color: #9ab1b8;
  }
}

:deep(.empty-state) {
  min-height: 220px;
  border-radius: 16px;
  background: rgba(255, 255, 255, .78);
  border: 1px solid rgba(211, 232, 237, .86);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 9px;
  color: #6f8790;

  .van-icon {
    color: #6fbacb;
    font-size: 42px;
  }

  strong {
    color: #173e48;
    font-size: 16px;
  }

  span {
    font-size: 13px;
  }
}

.exam-detail {
  min-height: 100%;
  padding: 22px 16px 32px;
  background: #f7fafc;
}

.detail-header {
  padding-right: 28px;

  span {
    color: #6f93a0;
    font-size: 11px;
    font-weight: 900;
  }

  h2 {
    margin: 6px 0 6px;
    color: #173e48;
    font-size: 22px;
    line-height: 1.25;
  }

  p {
    margin: 0;
    color: #78919a;
    font-size: 12px;
  }
}

.detail-status {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-top: 16px;

  div {
    padding: 12px;
    border-radius: 14px;
    background: #fff;
    border: 1px solid rgba(211, 232, 237, .9);
  }

  span {
    display: block;
    color: #78919a;
    font-size: 12px;
  }

  strong {
    display: block;
    margin-top: 8px;
    color: #276b63;
    font-size: 20px;

  }
}

.detail-section {
  margin-top: 14px;
  padding: 14px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid rgba(211, 232, 237, .9);

  h3 {
    margin: 0 0 10px;
    color: #173e48;
    font-size: 15px;
  }

  p {
    margin: 6px 0 0;
    color: #607b84;
    font-size: 13px;
    line-height: 1.6;
  }
}

.result-detail-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.pending-result {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px;
  border-radius: 14px;
  background: #f5fafb;
  color: #708890;
  font-size: 13px;

  .van-icon {
    color: #6fbacb;
    font-size: 18px;
  }
}

.result-detail-item {
  padding: 12px;
  border-radius: 14px;
  background: #f5fafb;
  border: 1px solid rgba(218, 235, 239, .9);

  &.abnormal {
    background: #fff9ef;
    border-color: rgba(217, 128, 34, .2);
  }

  img {
    display: block;
    width: 100%;
    margin-top: 10px;
    border-radius: 12px;
  }
}

.detail-item-head {
  display: flex;
  justify-content: space-between;
  gap: 10px;

  strong {
    color: #173e48;
    font-size: 14px;
  }

  span {
    flex: 0 0 auto;
    color: #347b70;
    font-size: 12px;
    font-weight: 900;
  }
}

.item-value {
  margin-top: 8px;
  color: #173e48;
  font-size: 22px;
  font-weight: 900;

  small {
    margin-left: 4px;
    color: #78919a;
    font-size: 12px;
  }
}
</style>
