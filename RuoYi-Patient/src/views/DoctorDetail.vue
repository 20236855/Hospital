<template>
  <div class="doctor-detail-page">
    <button class="back-btn" type="button" @click="goBack">
      <van-icon name="arrow-left" />
    </button>

    <div v-if="loading" class="loading-state">
      <van-loading color="#347b70" />
      <span>正在加载医生详情</span>
    </div>

    <template v-else-if="doctor">
      <section class="profile-panel">
        <div class="avatar-wrap">
          <img v-if="doctor.avatar" :src="doctor.avatar" alt="" @error="hideBrokenAvatar" />
          <span v-else>{{ getInitial(doctor.doctorName) }}</span>
        </div>
        <div class="profile-info">
          <div class="name-row">
            <h1>{{ doctor.doctorName || '未命名医生' }}</h1>
            <span :class="{ off: doctor.status !== '0' }">
              {{ doctor.status === '0' ? '可预约' : '停诊' }}
            </span>
          </div>
          <p>{{ getDeptName(doctor) }} · {{ getLevelName(doctor) }}</p>
          <div class="profile-tags">
            <em>{{ getGenderText(doctor.gender) }}</em>
            <em>{{ doctor.doctorNo || '院内医生' }}</em>
            <em>{{ doctor.phone ? '可电话咨询' : '门诊接诊' }}</em>
          </div>
        </div>
      </section>

      <section class="resource-metrics">
        <div>
          <strong>{{ getLevelName(doctor) }}</strong>
          <span>挂号级别</span>
        </div>
        <div>
          <strong>{{ getDeptName(doctor) }}</strong>
          <span>所在科室</span>
        </div>
        <div>
          <strong>{{ doctor.status === '0' ? '开放' : '暂停' }}</strong>
          <span>接诊状态</span>
        </div>
      </section>

      <section class="detail-section">
        <div class="section-heading">
          <van-icon name="medal-o" />
          <h2>擅长方向</h2>
        </div>
        <p class="rich-text">{{ doctor.specialty || getDefaultSpecialty(doctor) }}</p>
      </section>

      <section class="detail-section">
        <div class="section-heading">
          <van-icon name="description-o" />
          <h2>医生简介</h2>
        </div>
        <p class="rich-text">{{ doctor.introduction || getDefaultIntro(doctor) }}</p>
      </section>

      <section class="detail-section">
        <div class="section-heading">
          <van-icon name="hotel-o" />
          <h2>医院优质资源</h2>
        </div>
        <div class="resource-list">
          <div>
            <strong>规范诊疗</strong>
            <span>按科室、职称与接诊状态统一展示，便于患者选择。</span>
          </div>
          <div>
            <strong>便捷预约</strong>
            <span>医生信息与挂号流程联动，减少反复查找。</span>
          </div>
          <div>
            <strong>持续服务</strong>
            <span>结合电子病历与预约记录，支持后续复诊管理。</span>
          </div>
        </div>
      </section>

      <div class="bottom-action">
        <button type="button" @click="goRegister">
          <van-icon name="add-o" />
          预约挂号
        </button>
      </div>
    </template>

    <div v-else class="empty-state">
      <van-icon name="warning-o" />
      <strong>未找到医生信息</strong>
      <span>该医生可能已停用或暂未公开展示</span>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getDoctor } from '@/api/doctor'
import { getDeptList } from '@/api/register'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const doctor = ref(null)
const deptList = ref([])

const levelNameMap = {
  1: '普通号',
  2: '专家号',
  3: '主任号'
}

function getDeptName(item) {
  const dept = deptList.value.find((deptItem) => String(deptItem.deptId) === String(item?.deptId))
  return item?.deptName || dept?.deptName || (item?.deptId ? `科室${item.deptId}` : '门诊科室')
}

function getLevelName(item) {
  return item?.levelName || levelNameMap[item?.levelId] || '门诊医生'
}

function getGenderText(value) {
  if (!value) return '专业医生'
  return value === '0' ? '男医生' : value === '1' ? '女医生' : value
}

function getInitial(name) {
  return (name || '医').slice(0, 1)
}

function getDefaultSpecialty(item) {
  return `${getDeptName(item)}常见病、多发病诊疗及会诊评估，提供规范化门诊诊疗服务。`
}

function getDefaultIntro(item) {
  return `${item?.doctorName || '该医生'}长期参与${getDeptName(item)}临床诊疗工作，重视病情评估、治疗沟通和复诊管理。`
}

function fetchDoctorDetail() {
  const doctorId = route.params.id
  if (!doctorId) {
    goBack()
    return
  }

  loading.value = true
  getDoctor(doctorId).then((response) => {
    doctor.value = response.data || null
  }).finally(() => {
    loading.value = false
  })
}

function fetchDeptList() {
  return getDeptList({ pageNum: 1, pageSize: 500 }).then((response) => {
    deptList.value = response.rows || response.data || []
  }).catch(() => {
    deptList.value = []
  })
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/doctors')
  }
}

function goRegister() {
  router.push('/register')
}

function hideBrokenAvatar(event) {
  event.target.style.display = 'none'
}

onMounted(() => {
  fetchDeptList()
  fetchDoctorDetail()
})
</script>

<style scoped lang="scss">
.doctor-detail-page {
  min-height: 100vh;
  padding: 14px 14px 92px;
  background:
    linear-gradient(180deg, rgba(232, 248, 246, .96) 0%, rgba(247, 250, 252, 1) 42%),
    #f7fafc;
  color: #183f4a;
}

.back-btn {
  width: 38px;
  height: 38px;
  border: 0;
  border-radius: 10px;
  background: #1250af;
  color: #fff;
  box-shadow: 0 8px 22px rgba(30, 64, 175, .25);
}

.profile-panel {
  margin-top: 12px;
  padding: 16px;
  border-radius: 18px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, .96), rgba(255, 255, 255, .78)),
    linear-gradient(120deg, rgba(108, 181, 194, .18), rgba(123, 207, 170, .2));
  border: 1px solid rgba(206, 232, 238, .9);
  box-shadow: 0 18px 44px rgba(75, 132, 145, .13);
}

.avatar-wrap {
  width: 104px;
  height: 118px;
  margin: 0 auto 14px;
  border-radius: 20px;
  overflow: hidden;
  background: linear-gradient(145deg, #dff4ef, #d5edf3);
  display: grid;
  place-items: center;
  color: #276b63;
  font-size: 38px;
  font-weight: 900;
  border: 2px solid rgba(255, 255, 255, .9);
  box-shadow: 0 12px 30px rgba(75, 132, 145, .15);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.name-row {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;

  h1 {
    margin: 0;
    color: #173e48;
    font-size: 24px;
    line-height: 1.2;
  }

  span {
    height: 24px;
    border-radius: 8px;
    padding: 0 8px;
    display: inline-flex;
    align-items: center;
    background: rgba(52, 123, 112, .1);
    color: #347b70;
    font-size: 11px;
    font-weight: 900;

    &.off {
      background: rgba(151, 164, 170, .14);
      color: #7a8a90;
    }
  }
}

.profile-info {
  text-align: center;

  p {
    margin: 8px 0 0;
    color: #607982;
    font-size: 14px;
  }
}

.profile-tags {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 7px;
  margin-top: 12px;

  em {
    min-height: 24px;
    border-radius: 8px;
    padding: 5px 8px;
    background: rgba(255, 255, 255, .78);
    color: #5d7881;
    font-size: 11px;
    font-style: normal;
    font-weight: 800;
  }
}

.resource-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  margin-top: 12px;

  div {
    min-height: 62px;
    border-radius: 14px;
    padding: 10px 8px;
    background: rgba(255, 255, 255, .94);
    border: 1px solid rgba(211, 232, 237, .86);
    box-shadow: 0 10px 24px rgba(75, 132, 145, .09);
  }

  strong,
  span {
    display: block;
    text-align: center;
  }

  strong {
    color: #276b63;
    font-size: 14px;
    line-height: 1.2;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  span {
    margin-top: 8px;
    color: #718b94;
    font-size: 11px;
    font-weight: 800;
  }
}

.detail-section {
  margin-top: 12px;
  padding: 16px;
  border-radius: 16px;
  background: rgba(255, 255, 255, .94);
  border: 1px solid rgba(211, 232, 237, .86);
  box-shadow: 0 12px 30px rgba(75, 132, 145, .1);
}

.section-heading {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;

  .van-icon {
    color: #347b70;
    font-size: 19px;
  }

  h2 {
    margin: 0;
    color: #173e48;
    font-size: 17px;
  }
}

.rich-text {
  margin: 0;
  color: #4d6971;
  font-size: 14px;
  line-height: 1.75;
  white-space: pre-wrap;
  word-break: break-word;
}

.resource-list {
  display: flex;
  flex-direction: column;
  gap: 10px;

  div {
    padding: 12px;
    border-radius: 12px;
    background: #f5fafb;
    border: 1px solid rgba(206, 232, 238, .72);
  }

  strong {
    display: block;
    color: #276b63;
    font-size: 14px;
  }

  span {
    display: block;
    margin-top: 5px;
    color: #6f8790;
    font-size: 12px;
    line-height: 1.55;
  }
}

.bottom-action {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 10px 14px calc(10px + env(safe-area-inset-bottom));
  background: rgba(247, 250, 252, .9);
  border-top: 1px solid rgba(211, 232, 237, .86);
  backdrop-filter: blur(12px);

  button {
    width: 100%;
    height: 46px;
    border: 0;
    border-radius: 14px;
    background: #347b70;
    color: #fff;
    font-size: 15px;
    font-weight: 900;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
  }
}

.loading-state,
.empty-state {
  min-height: 70vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #6f8790;
}

.empty-state {
  .van-icon {
    color: #d35f45;
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
</style>
