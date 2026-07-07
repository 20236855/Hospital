<template>
  <div class="payment-page">
    <header class="page-header">
      <button class="back-btn" type="button" @click="goBack">
        <van-icon name="arrow-left" />
      </button>
      <div>
        <p>PAYMENT CENTER</p>
        <h1>我的缴费</h1>
      </div>
    </header>

    <section class="payment-summary">
      <div>
        <span>待缴费提醒</span>
        <strong :class="{ danger: unpaidCount > 0 }">{{ unpaidCount }}</strong>
        <em>笔待处理</em>
      </div>
      <div>
        <span>待支付金额</span>
        <strong class="danger">¥{{ unpaidAmount }}</strong>
        <em>请及时完成缴费</em>
      </div>
    </section>

    <section v-if="unpaidCount > 0" class="alert-card">
      <van-icon name="warning-o" />
      <div>
        <strong>您有 {{ unpaidCount }} 笔费用需要缴纳</strong>
        <span>未缴费可能影响后续就诊、签到或检查安排。</span>
      </div>
    </section>

    <van-tabs v-model:active="activeTab" sticky color="#347b70" title-active-color="#173e48">
      <van-tab title="全部">
        <PaymentList
          :items="allPayments"
          :paying-id="payingId"
          @pay="handlePay"
        />
      </van-tab>
      <van-tab :title="`待缴费 ${unpaidPayments.length}`">
        <PaymentList
          :items="unpaidPayments"
          :paying-id="payingId"
          empty-text="暂无待缴费记录"
          @pay="handlePay"
        />
      </van-tab>
      <van-tab :title="`已缴费 ${paidPayments.length}`">
        <PaymentList
          :items="paidPayments"
          :paying-id="payingId"
          empty-text="暂无已缴费记录"
          @pay="handlePay"
        />
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup>
import { computed, defineComponent, h, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showDialog, showToast } from 'vant'
import { getRegisterList } from '@/api/register'
import { payExamByRegister, payRegister } from '@/api/payment'
import { getMyExamPayments } from '@/api/exam'

const router = useRouter()
const loading = ref(false)
const activeTab = ref(0)
const paymentRows = ref([])
const payingId = ref(null)

const allPayments = computed(() => paymentRows.value)
const unpaidPayments = computed(() => paymentRows.value.filter((item) => !isPaid(item.payStatus)))
const paidPayments = computed(() => paymentRows.value.filter((item) => isPaid(item.payStatus)))
const unpaidCount = computed(() => unpaidPayments.value.length)
const unpaidAmount = computed(() => formatAmount(unpaidPayments.value.reduce((sum, item) => sum + toNumber(item.amount), 0)))

const PaymentList = defineComponent({
  name: 'PaymentList',
  props: {
    items: { type: Array, default: () => [] },
    payingId: { type: [Number, String], default: null },
    emptyText: { type: String, default: '暂无缴费记录' }
  },
  emits: ['pay'],
  setup(props, { emit }) {
    return () => h('div', { class: 'payment-list' }, [
      props.items.length === 0
        ? h('div', { class: 'empty-state' }, [
          h('i', { class: 'van-badge__wrapper van-icon van-icon-balance-list-o' }),
          h('strong', props.emptyText),
          h('span', '挂号或检查开单后会在这里展示缴费记录')
        ])
        : props.items.map((item) => {
          const paid = isPaid(item.payStatus)
          const examPayment = item.paymentType === 'exam'
          return h('article', { class: ['payment-card', paid ? 'paid' : 'unpaid'], key: item.paymentKey }, [
            h('div', { class: 'payment-card-head' }, [
              h('div', [
                h('h3', item.title || '门诊费用'),
                h('p', item.subtitle || `${item.doctorName || '接诊医生'} · ${item.levelName || '门诊号'}`)
              ]),
              h('span', { class: ['status-tag', paid ? 'paid' : 'unpaid'] }, paid ? '已缴费' : '待缴费')
            ]),
            h('div', { class: 'payment-meta' }, [
              h('span', `${examPayment ? '申请单号' : '挂号单号'}：${item.billNo || '-'}`),
              h('span', `${examPayment ? '申请时间' : '挂号时间'}：${formatDate(item.billTime)}`)
            ]),
            h('div', { class: 'payment-card-foot' }, [
              h('strong', { class: paid ? '' : 'danger' }, `¥${formatAmount(item.amount)}`),
              paid
                ? h('span', { class: 'paid-note' }, '费用已结清')
                : h('button', {
                  type: 'button',
                  disabled: props.payingId === item.paymentKey,
                  onClick: () => emit('pay', item)
                }, props.payingId === item.paymentKey ? '缴费中...' : '立即缴费')
            ])
          ])
        })
    ])
  }
})

function isPaid(status) {
  const value = String(status || '').trim().toLowerCase()
  return value === 'paid' || value === '1' || value === '已支付' || value === '已缴费'
}

function toNumber(value) {
  const numberValue = Number(value)
  return Number.isFinite(numberValue) ? numberValue : 0
}

function formatAmount(value) {
  return toNumber(value).toFixed(2)
}

function formatDate(value) {
  if (!value) return '-'
  const date = new Date(String(value).replace(/-/g, '/'))
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

async function loadPayments() {
  loading.value = true
  try {
    const [registerResult, examResult] = await Promise.allSettled([
      getRegisterList({ pageNum: 1, pageSize: 100 }),
      getMyExamPayments()
    ])
    const registerRes = registerResult.status === 'fulfilled' ? registerResult.value : { rows: [] }
    const examRes = examResult.status === 'fulfilled' ? examResult.value : { data: [] }
    const registerPayments = (registerRes.rows || []).map((item) => ({
      ...item,
      paymentType: 'register',
      paymentKey: `register-${item.registerId}`,
      title: item.deptName || '门诊挂号',
      subtitle: `${item.doctorName || '接诊医生'} · ${item.levelName || '门诊号'}`,
      billNo: item.registerNo || item.registerId || '-',
      billTime: item.registerTime,
      amount: item.registerFee
    }))
    const examPayments = (examRes.data || []).map((item) => ({
      ...item,
      paymentType: 'exam',
      paymentKey: `exam-${item.registerId}-${item.payStatus || item.applyId}`,
      title: item.techName || '检查检验项目',
      subtitle: `${item.deptName || '执行科室'} · ${item.itemCount || 1} 个项目`,
      billNo: item.registerNo || item.registerId || '-',
      billTime: item.applyTime,
      amount: item.techPrice
    }))
    paymentRows.value = [...registerPayments, ...examPayments]
  } finally {
    loading.value = false
  }
}

async function handlePay(item) {
  if (!item?.registerId) return
  try {
    await showConfirmDialog({
      title: '确认缴费',
      message: `本次需缴纳 ¥${formatAmount(item.amount)}，是否继续？`,
      confirmButtonText: '立即缴费',
      confirmButtonColor: '#d35f45'
    })
    payingId.value = item.paymentKey
    if (item.paymentType === 'exam') {
      await payExamByRegister(item.registerId)
    } else {
      await payRegister(item.registerId)
    }
    showDialog({
      title: '缴费成功',
      message: `已成功缴纳 ¥${formatAmount(item.amount)}`,
      confirmButtonText: '确定',
      className: 'unpaid-notice'
    })
    await loadPayments()
  } catch (error) {
    if (error !== 'cancel') {
      showToast('缴费失败，请稍后重试')
    }
  } finally {
    payingId.value = null
  }
}

function goBack() {
  router.back()
}

onMounted(async () => {
  await loadPayments()
  if (unpaidCount.value > 0) {
    showDialog({
      title: '缴费提醒',
      message: `您有 ${unpaidCount.value} 笔费用尚未缴纳，共计 ¥${unpaidAmount.value}，请及时缴费以免影响就诊。`,
      confirmButtonText: '去缴费',
      cancelButtonText: '稍后再说',
      showCancelButton: true,
      className: 'unpaid-notice'
    }).then(() => {
      activeTab.value = 1
    }).catch(() => {})
  }
})
</script>

<style scoped lang="scss">
.payment-page {
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

.payment-summary {
  display: grid;
  grid-template-columns: .85fr 1.15fr;
  gap: 10px;

  div {
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

.danger {
  color: #d64545 !important;
}

.alert-card {
  display: grid;
  grid-template-columns: 34px 1fr;
  gap: 10px;
  align-items: center;
  margin-top: 12px;
  padding: 12px;
  border-radius: 14px;
  background: #fff4f2;
  border: 1px solid rgba(214, 69, 69, .16);
  color: #b63b35;

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

.payment-list {
  padding-top: 12px;
}

:deep(.payment-card) {
  margin-bottom: 12px;
  padding: 14px;
  border-radius: 16px;
  background: rgba(255, 255, 255, .96);
  border: 1px solid rgba(211, 232, 237, .86);
  box-shadow: 0 12px 30px rgba(75, 132, 145, .1);

  &.unpaid {
    border-color: rgba(214, 69, 69, .24);
  }
}

:deep(.payment-card-head) {
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

:deep(.status-tag) {
  flex: 0 0 auto;
  height: 24px;
  border-radius: 8px;
  padding: 0 8px;
  display: inline-flex;
  align-items: center;
  font-size: 11px;
  font-weight: 900;

  &.paid {
    background: rgba(52, 123, 112, .1);
    color: #347b70;
  }

  &.unpaid {
    background: #fff0ee;
    color: #d64545;
  }
}

:deep(.payment-meta) {
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

:deep(.payment-card-foot) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;

  strong {
    color: #173e48;
    font-size: 22px;
  }

  button {
    height: 34px;
    border: 0;
    border-radius: 10px;
    padding: 0 14px;
    background: #d64545;
    color: #fff;
    font-size: 13px;
    font-weight: 900;

    &:disabled {
      opacity: .68;
    }
  }
}

:deep(.paid-note) {
  color: #347b70;
  font-size: 12px;
  font-weight: 800;
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

:global(.payment-notice-dialog) {
  width: min(86vw, 350px);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 22px 56px rgba(44, 73, 86, 0.18);
}

:global(.payment-notice-dialog .van-dialog__header) {
  padding-top: 22px;
  color: #1f3f4a;
  font-size: 18px;
  font-weight: 800;
}

:global(.payment-notice-dialog .van-dialog__message) {
  padding: 14px 24px 18px;
  color: #526b76;
  font-size: 14px;
  line-height: 1.7;
}

:global(.payment-notice-dialog .van-dialog__footer) {
  padding: 0 16px 16px;
  gap: 10px;
  border-top: 0;
}

:global(.payment-notice-dialog .van-dialog__cancel),
:global(.payment-notice-dialog .van-dialog__confirm) {
  height: 42px;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 700;
}

:global(.payment-notice-dialog .van-dialog__cancel) {
  background: #f2f7f8;
  color: #607783;
}

:global(.unpaid-notice .van-dialog__confirm) {
  background: linear-gradient(135deg, #3f8f88, #4aa6a0);
  color: #fff;
  box-shadow: 0 10px 22px rgba(63, 143, 136, 0.22);
}
</style>
