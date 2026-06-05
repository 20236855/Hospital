<template>
  <div class="register-page">
    <div class="auth-bg-cross cross-one"></div>
    <div class="auth-bg-cross cross-two"></div>
    <div class="auth-bg-cross cross-three"></div>
    <div class="auth-bg-cross cross-four"></div>

    <div class="header-section">
      <div class="header-back" @click="goBack">
        <van-icon name="arrow-left" />
      </div>
      <div class="header-title">
        <span class="title-icon float-animation">
          <span class="title-cross"></span>
        </span>
        <h1>预约挂号</h1>
      </div>
    </div>

    <div class="content-section">
      <div class="step-indicator slide-up-animation">
        <div class="step-item" :class="{ active: step >= 1, completed: step > 1 }">
          <div class="step-circle">
            <span v-if="step > 1" class="step-check">✓</span>
            <span v-else class="step-num">1</span>
          </div>
          <span class="step-label">选择科室</span>
        </div>
        <div class="step-line"></div>
        <div class="step-item" :class="{ active: step >= 2, completed: step > 2 }">
          <div class="step-circle">
            <span v-if="step > 2" class="step-check">✓</span>
            <span v-else class="step-num">2</span>
          </div>
          <span class="step-label">选择医生</span>
        </div>
        <div class="step-line"></div>
        <div class="step-item" :class="{ active: step >= 3, completed: step > 3 }">
          <div class="step-circle">
            <span v-if="step > 3" class="step-check">✓</span>
            <span v-else class="step-num">3</span>
          </div>
          <span class="step-label">选择排班</span>
        </div>
        <div class="step-line"></div>
        <div class="step-item" :class="{ active: step >= 4 }">
          <div class="step-circle">
            <span class="step-num">4</span>
          </div>
          <span class="step-label">确认预约</span>
        </div>
      </div>

      <div v-if="step === 1" class="step-content slide-up-animation" style="animation-delay: 0.1s">
        <div class="form-card">
          <div class="card-header">
            <div class="header-icon">🏥</div>
            <div class="header-text">
              <h3>选择就诊科室</h3>
              <p>请选择您需要就诊的科室</p>
            </div>
          </div>
          <div class="department-grid">
            <div 
              v-for="(dept, index) in departmentList" 
              :key="dept.deptId"
              class="dept-item"
              :class="{ selected: form.department === dept.deptName }"
              @click="selectDepartment(dept.deptName)"
            >
              <span class="dept-emoji">{{ getDeptEmoji(dept.deptName) }}</span>
              <span class="dept-name">{{ dept.deptName }}</span>
            </div>
          </div>
        </div>
        <div class="btn-group">
          <van-button class="next-btn" @click="nextStep" :disabled="!form.department">
            下一步
            <van-icon name="arrow" />
          </van-button>
        </div>
      </div>

      <div v-if="step === 2" class="step-content slide-up-animation" style="animation-delay: 0.1s">
        <div class="form-card">
          <div class="card-header">
            <div class="header-icon">👨‍⚕️</div>
            <div class="header-text">
              <h3>选择就诊医生</h3>
              <p>{{ form.department }}的医生列表</p>
            </div>
          </div>
          <div class="doctor-list">
            <div 
              v-for="(doctor, index) in doctorList" 
              :key="doctor.doctorId || doctor.id"
              class="doctor-card"
              :class="{ selected: form.doctorId === (doctor.doctorId || doctor.id) }"
              @click="selectDoctor(doctor)"
            >
              <div class="doctor-avatar float-animation" :style="{ animationDelay: `${index * 0.1}s` }">
                <span class="avatar-text">{{ (doctor.doctorName || doctor.name)?.charAt(0) || '医' }}</span>
              </div>
              <div class="doctor-info">
                <div class="doctor-name">
                  {{ doctor.doctorName || doctor.name || '医生' }}
                  <span class="doctor-title">{{ doctor.title || '主治医师' }}</span>
                </div>
                <div class="doctor-desc">
                  <span class="specialty">{{ doctor.specialty || '常见病诊疗' }}</span>
                </div>
              </div>
              <div v-if="form.doctorId === (doctor.doctorId || doctor.id)" class="select-mark">
                <div class="mark-circle">✓</div>
              </div>
            </div>
          </div>
        </div>
        <div class="btn-group">
          <van-button class="back-btn" @click="prevStep">
            <van-icon name="arrow-left" />
            上一步
          </van-button>
          <van-button class="next-btn" @click="nextStep" :disabled="!form.doctorId">
            下一步
            <van-icon name="arrow" />
          </van-button>
        </div>
      </div>

      <div v-if="step === 3" class="step-content slide-up-animation" style="animation-delay: 0.1s">
        <div class="form-card">
          <div class="card-header">
            <div class="header-icon">📅</div>
            <div class="header-text">
              <h3>选择排班时间</h3>
              <p>{{ form.doctorName }}医生的可预约排班</p>
            </div>
          </div>
          <div class="schedule-list">
            <div 
              v-for="schedule in scheduleList" 
              :key="schedule.scheduleId"
              class="schedule-card"
              :class="{ 
                selected: form.scheduleId === schedule.scheduleId, 
                full: schedule.reservedNumber >= schedule.maxNumber || schedule.status === 1 
              }"
              @click="selectSchedule(schedule)"
            >
              <div class="schedule-date">
                {{ formatScheduleDate(schedule.scheduleDate) }}
              </div>
              <div class="schedule-time">
                {{ getTimeSlotText(schedule.timeSlot) }}
              </div>
              <div class="schedule-status">
                <span v-if="schedule.reservedNumber >= schedule.maxNumber || schedule.status === 1" class="status-full">已满</span>
                <span v-else class="status-available">可预约 {{ schedule.maxNumber - schedule.reservedNumber }}/{{ schedule.maxNumber }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="btn-group">
          <van-button class="back-btn" @click="prevStep">
            <van-icon name="arrow-left" />
            上一步
          </van-button>
          <van-button class="next-btn" @click="nextStep" :disabled="!form.scheduleId">
            下一步
            <van-icon name="arrow" />
          </van-button>
        </div>
      </div>

      <div v-if="step === 4" class="step-content slide-up-animation" style="animation-delay: 0.1s">
        <div class="form-card">
          <div class="card-header">
            <div class="header-icon">✅</div>
            <div class="header-text">
              <h3>确认预约信息</h3>
              <p>请核对以下预约信息</p>
            </div>
          </div>
          <div class="confirm-info">
            <div class="info-item">
              <span class="info-label">就诊科室</span>
              <span class="info-value">{{ form.department }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">就诊医生</span>
              <span class="info-value">{{ form.doctorName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">就诊日期</span>
              <span class="info-value">{{ formatScheduleDate(selectedSchedule?.scheduleDate) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">就诊时段</span>
              <span class="info-value">{{ getTimeSlotText(selectedSchedule?.timeSlot) }}</span>
            </div>
          </div>
        </div>
        <div class="btn-group">
          <van-button class="back-btn" @click="prevStep">
            <van-icon name="arrow-left" />
            上一步
          </van-button>
          <van-button class="submit-btn" @click="submitRegister" :loading="loading">
            确认预约
          </van-button>
        </div>
      </div>
    </div>

    <van-tabbar v-model="active" class="custom-tabbar">
      <van-tabbar-item icon="home-o" to="/">首页</van-tabbar-item>
      <van-tabbar-item icon="todo-list-o" to="/register">挂号</van-tabbar-item>
      <van-tabbar-item icon="notes-o" to="/record">病历</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/profile">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showDialog } from 'vant'
import { createRegister, getDoctorList, getDeptList, getScheduleList } from '@/api/register'
import { getPatientList } from '@/api/patient'

const router = useRouter()
const active = ref(1)
const step = ref(1)
const loading = ref(false)

const form = ref({
    department: '',
    deptId: null,
    doctorId: null,
    doctorName: '',
    scheduleId: null
})

const departmentList = ref([])
const doctorList = ref([])
const allDoctorList = ref([])
const scheduleList = ref([])

const selectedSchedule = computed(() => {
    return scheduleList.value.find(s => s.scheduleId === form.value.scheduleId)
})

const getDeptEmoji = (dept) => {
    const emojiMap = {
        '内科': '❤️',
        '外科': '🔧',
        '儿科': '👶',
        '妇产科': '👩',
        '眼科': '👁️',
        '耳鼻喉科': '👂',
        '口腔科': '😬',
        '皮肤科': '🧴',
        '骨科': '🦴'
    }
    return emojiMap[dept] || '🏥'
}

const findDeptIdByName = (deptName) => {
    const dept = departmentList.value.find(d => d.deptName === deptName)
    return dept ? dept.deptId : null
}

const filteredDoctorList = computed(() => {
    if (!form.value.department) return allDoctorList.value
    const deptId = findDeptIdByName(form.value.department)
    if (!deptId) return allDoctorList.value
    return allDoctorList.value.filter(doc => doc.deptId === deptId)
})

const formatScheduleDate = (dateStr) => {
    if (!dateStr) return ''
    const date = new Date(dateStr)
    const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    const month = date.getMonth() + 1
    const day = date.getDate()
    const weekDay = weekDays[date.getDay()]
    return `${month}月${day}日 ${weekDay}`
}

const getTimeSlotText = (timeSlot) => {
    if (timeSlot === 'morning') return '上午 08:00-12:00'
    if (timeSlot === 'afternoon') return '下午 14:00-17:30'
    return timeSlot
}

const loadDepartments = async () => {
    try {
        const res = await getDeptList()
        if (res.data && res.data.length > 0) {
            departmentList.value = res.data.filter(dept => dept.deptId > 100)
        }
        if (departmentList.value.length === 0) {
            departmentList.value = [
                { deptId: 201, deptName: '内科' },
                { deptId: 202, deptName: '外科' },
                { deptId: 203, deptName: '儿科' },
                { deptId: 204, deptName: '妇产科' },
                { deptId: 205, deptName: '骨科' },
                { deptId: 206, deptName: '眼科' }
            ]
        }
    } catch (error) {
        console.error('加载科室失败', error)
        departmentList.value = [
            { deptId: 201, deptName: '内科' },
            { deptId: 202, deptName: '外科' },
            { deptId: 203, deptName: '儿科' },
            { deptId: 204, deptName: '妇产科' },
            { deptId: 205, deptName: '骨科' },
            { deptId: 206, deptName: '眼科' }
        ]
    }
}

const loadDoctors = async () => {
    try {
        const res = await getDoctorList({ pageNum: 1, pageSize: 50 })
        allDoctorList.value = res.rows || []
        doctorList.value = allDoctorList.value
    } catch (error) {
        console.error('加载医生失败', error)
        allDoctorList.value = []
        doctorList.value = []
    }
}

const loadSchedules = async () => {
    if (!form.value.doctorId) return
    try {
        const res = await getScheduleList({ doctorId: form.value.doctorId })
        scheduleList.value = res.rows || []
    } catch (error) {
        console.error('加载排班失败', error)
        scheduleList.value = []
    }
}

const selectDepartment = (dept) => {
    form.value.department = dept
    form.value.deptId = findDeptIdByName(dept)
    form.value.doctorId = null
    form.value.doctorName = ''
    form.value.scheduleId = null
}

const selectDoctor = (doctor) => {
    form.value.doctorId = doctor.doctorId || doctor.id
    form.value.doctorName = doctor.doctorName || doctor.name
    form.value.scheduleId = null
}

const selectSchedule = (schedule) => {
    if (schedule.reservedNumber >= schedule.maxNumber || schedule.status === 1) {
        showToast('该排班已满，请选择其他时间')
        return
    }
    form.value.scheduleId = schedule.scheduleId
}

const nextStep = async () => {
    if (step.value === 3 && !form.value.scheduleId) {
        showToast('请选择排班时间')
        return
    }
    step.value++
    if (step.value === 2) {
        doctorList.value = filteredDoctorList.value
    }
    if (step.value === 3) {
        await loadSchedules()
    }
}

const prevStep = () => {
    step.value--
}

const submitRegister = async () => {
    loading.value = true
    try {
        const patientId = localStorage.getItem('patientId')
        const registerData = {
            patientId: patientId ? Number(patientId) : null,
            doctorId: form.value.doctorId,
            deptId: form.value.deptId,
            scheduleId: form.value.scheduleId,
            registerTime: selectedSchedule.value?.scheduleDate,
            registerType: 'online'
        }
        console.log('提交的预约数据:', registerData)
        await createRegister(registerData)
        await showDialog({
            title: '预约成功',
            message: `您已成功预约${form.value.doctorName}医生，就诊时间：${formatScheduleDate(selectedSchedule.value?.scheduleDate)} ${getTimeSlotText(selectedSchedule.value?.timeSlot)}`,
            confirmButtonText: '确定',
            theme: 'success'
        })
        router.push('/')
    } catch (error) {
        console.error(error)
        showToast(error.msg || '预约失败，请稍后重试')
    } finally {
        loading.value = false
    }
}

const goBack = () => {
    if (step.value > 1) {
        prevStep()
    } else {
        router.back()
    }
}

const ensurePatientId = async () => {
    try {
        const savedPatientId = localStorage.getItem('patientId')
        const savedUsername = localStorage.getItem('username')
        
        if (!savedPatientId) {
            const patientRes = await getPatientList({ pageNum: 1, pageSize: 10 })
            if (patientRes.rows && patientRes.rows.length > 0) {
                const patient = patientRes.rows.find(p => p.name === savedUsername) || patientRes.rows[0]
                localStorage.setItem('patientId', patient.patientId)
                localStorage.setItem('patientName', patient.name)
            }
        }
    } catch (error) {
        console.error('获取患者信息失败', error)
    }
}

onMounted(() => {
    ensurePatientId()
    loadDepartments()
    loadDoctors()
})
</script>

<style scoped lang="scss">
.register-page {
  min-height: 100vh;
  background: var(--bg-gradient);
  padding-bottom: 60px;
  position: relative;
  overflow-x: hidden;
}

.auth-bg-cross {
  position: absolute;
  width: 40px;
  height: 40px;
  opacity: 0.12;
  animation: authCrossBreath 8s ease-in-out infinite;

  &::before,
  &::after {
    content: "";
    position: absolute;
    background: #68c7a9;
    border-radius: 8px;
  }

  &::before {
    top: 50%;
    left: 0;
    width: 100%;
    height: 6px;
    transform: translateY(-50%);
  }

  &::after {
    top: 0;
    left: 50%;
    width: 6px;
    height: 100%;
    transform: translateX(-50%);
  }
}

.cross-one {
  top: 12%;
  left: 8%;
  animation-delay: 0s;
}

.cross-two {
  top: 25%;
  right: 10%;
  animation-delay: -2s;
}

.cross-three {
  bottom: 30%;
  left: 12%;
  animation-delay: -4s;
}

.cross-four {
  bottom: 15%;
  right: 6%;
  animation-delay: -6s;
}

.header-section {
  padding: 16px 20px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-back {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.96);
    background: rgba(255, 255, 255, 0.8);
  }

  .van-icon {
    color: #5f7580;
    font-size: 20px;
  }
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;

  h1 {
    font-size: 24px;
    font-weight: 700;
    color: #4f7380;
    margin: 0;
  }
}

.title-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(104, 199, 169, 0.2);
}

.title-cross {
  position: relative;
  width: 24px;
  height: 24px;

  &::before,
  &::after {
    content: "";
    position: absolute;
    background: linear-gradient(135deg, #68c7a9, #89dbc1);
    border-radius: 6px;
  }

  &::before {
    top: 50%;
    left: 0;
    width: 100%;
    height: 5px;
    transform: translateY(-50%);
  }

  &::after {
    top: 0;
    left: 50%;
    width: 5px;
    height: 100%;
    transform: translateX(-50%);
  }
}

.content-section {
  padding: 0 16px;
}

.step-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px 0 24px;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  opacity: 0.5;
  transition: all 0.3s ease;

  &.active,
  &.completed {
    opacity: 1;
  }
}

.step-circle {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: #8e9fa8;
  transition: all 0.3s ease;
  border: 2px solid transparent;

  .step-item.active & {
    background: linear-gradient(135deg, #68c7a9, #89dbc1);
    color: white;
    border-color: #68c7a9;
    box-shadow: 0 4px 12px rgba(104, 199, 169, 0.3);
  }

  .step-item.completed & {
    background: linear-gradient(135deg, #8ed6f2, #bfefff);
    color: white;
  }
}

.step-num {
  font-size: 16px;
}

.step-check {
  font-size: 18px;
}

.step-label {
  font-size: 13px;
  color: #7e98a4;
  font-weight: 500;

  .step-item.active & {
    color: #5f7580;
  }
}

.step-line {
  width: 40px;
  height: 3px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 2px;
  margin: 0 8px 24px;
  transition: all 0.3s ease;
}

.step-content {
  margin-bottom: 20px;
}

.form-card {
  background: rgba(255, 255, 255, 0.74);
  border-radius: 24px;
  padding: 24px;
  border: 1px solid rgba(194, 228, 236, 0.72);
  box-shadow: var(--card-shadow);
  backdrop-filter: blur(8px);
}

.card-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(213, 237, 243, 0.6);
}

.header-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: linear-gradient(135deg, #e6f6fb, #fffaf4);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.header-text {
  flex: 1;

  h3 {
    font-size: 18px;
    font-weight: 700;
    color: #4f7380;
    margin: 0 0 4px;
  }

  p {
    font-size: 14px;
    color: #8e9fa8;
    margin: 0;
  }
}

.department-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.dept-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 8px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    background: rgba(255, 255, 255, 0.8);
  }

  &:active {
    transform: translateY(0);
  }

  &.selected {
    border-color: #68c7a9;
    background: linear-gradient(135deg, rgba(104, 199, 169, 0.1), rgba(142, 214, 242, 0.1));
  }
}

.dept-emoji {
  font-size: 28px;
}

.dept-name {
  font-size: 13px;
  font-weight: 600;
  color: #5f7580;
}

.doctor-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.doctor-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 18px;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    background: rgba(255, 255, 255, 0.8);
  }

  &:active {
    transform: translateY(0);
  }

  &.selected {
    border-color: #68c7a9;
    background: linear-gradient(135deg, rgba(104, 199, 169, 0.08), rgba(255, 255, 255, 0.8));
  }
}

.doctor-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #68c7a9, #89dbc1);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar-text {
  font-size: 22px;
  font-weight: 700;
  color: white;
}

.doctor-info {
  flex: 1;
  min-width: 0;
}

.doctor-name {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  font-size: 16px;
  font-weight: 700;
  color: #4f7380;
}

.doctor-title {
  font-size: 12px;
  font-weight: 600;
  color: #68c7a9;
  background: rgba(104, 199, 169, 0.12);
  padding: 2px 8px;
  border-radius: 8px;
}

.doctor-desc {
  display: flex;
  align-items: center;
  gap: 8px;
}

.specialty {
  font-size: 13px;
  color: #8e9fa8;
}

.select-mark {
  width: 28px;
  height: 28px;
  flex-shrink: 0;
}

.mark-circle {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: linear-gradient(135deg, #68c7a9, #89dbc1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
  font-weight: 700;
}

.schedule-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.schedule-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    background: rgba(255, 255, 255, 0.8);
  }

  &:active {
    transform: translateY(0);
  }

  &.selected {
    border-color: #68c7a9;
    background: linear-gradient(135deg, rgba(104, 199, 169, 0.1), rgba(142, 214, 242, 0.1));
  }

  &.full {
    opacity: 0.5;
    cursor: not-allowed;
    background: rgba(240, 83, 145, 0.08);
  }
}

.schedule-date {
  font-size: 15px;
  font-weight: 600;
  color: #4f7380;
}

.schedule-time {
  font-size: 14px;
  color: #8e9fa8;
}

.schedule-status {
  font-size: 13px;
  font-weight: 600;

  .status-available {
    color: #68c7a9;
  }

  .status-full {
    color: #f05391;
  }
}

.confirm-info {
  background: rgba(104, 199, 169, 0.08);
  border-radius: 16px;
  padding: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid rgba(213, 237, 243, 0.6);

  &:first-child {
    padding-top: 0;
  }

  &:last-child {
    padding-bottom: 0;
    border-bottom: none;
  }
}

.info-label {
  font-size: 14px;
  color: #8e9fa8;
}

.info-value {
  font-size: 14px;
  font-weight: 600;
  color: #4f7380;
}

.btn-group {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.back-btn,
.next-btn,
.submit-btn {
  flex: 1;
  height: 52px;
  border-radius: 16px;
  font-size: 16px;
  font-weight: 700;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.3s ease;
}

.back-btn {
  background: rgba(255, 255, 255, 0.7);
  color: #5f7580;

  &:active {
    transform: scale(0.98);
    background: rgba(255, 255, 255, 0.9);
  }
}

.next-btn,
.submit-btn {
  background: linear-gradient(135deg, #68c7a9, #89dbc1);
  color: white;
  box-shadow: 0 8px 24px rgba(104, 199, 169, 0.3);

  &:active {
    transform: scale(0.98);
    box-shadow: 0 4px 12px rgba(104, 199, 169, 0.3);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
    transform: none;
  }
}

.submit-btn {
  background: linear-gradient(135deg, #8ed6f2, #bfefff);
  box-shadow: 0 8px 24px rgba(142, 214, 242, 0.3);
}

.custom-tabbar {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-top: 1px solid rgba(194, 228, 236, 0.72);
}

@keyframes authCrossBreath {
  0%, 100% { opacity: 0.08; transform: scale(1); }
  50% { opacity: 0.18; transform: scale(1.08); }
}
</style>
