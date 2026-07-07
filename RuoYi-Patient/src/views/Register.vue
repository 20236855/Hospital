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
          <span class="step-label">选择时间</span>
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
            <div class="header-text">
              <h3>选择就诊科室</h3>
            </div>
          </div>
          <div class="department-selector">
            <div class="department-column parent-column">
              <div
                v-for="dept in departmentList"
                :key="dept.deptId"
                class="dept-item parent-dept"
                :class="{ selected: activeParentDeptId === dept.deptId }"
                @click="selectParentDepartment(dept)"
              >
                <span class="dept-name">{{ dept.deptName }}</span>
              </div>
            </div>
            <div class="department-column child-column">
              <div
                v-for="dept in subDepartmentList"
                :key="dept.deptId"
                class="dept-item child-dept"
                :class="{ selected: form.deptId === dept.deptId }"
                @click="selectDepartment(dept)"
              >
                <span class="dept-name">{{ dept.deptName }}</span>
              </div>
              <div v-if="!subDepartmentList.length" class="empty-sub-dept">
                暂无可预约门诊
              </div>
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
            <div class="header-text">
              <h3>选择就诊号源</h3>
            </div>
          </div>
          <div class="schedule-calendar">
            <div v-if="availableScheduleDates.length" class="date-strip">
              <button
                v-for="day in availableScheduleDates"
                :key="day.date"
                type="button"
                class="date-card"
                :class="{ active: selectedScheduleDate === day.date }"
                @click="selectScheduleDate(day.date)"
              >
                <span class="date-week">{{ day.weekDay }}</span>
                <strong>{{ day.monthDay }}</strong>
                <small>余号 {{ getDayAvailableCount(day) }}</small>
              </button>
            </div>
            <div v-else class="empty-day">未来一周暂无可预约日期</div>

            <div v-if="selectedDateSchedules.length" class="slot-list">
              <div
                v-for="schedule in selectedDateSchedules"
                :key="`${selectedScheduleDate}-${schedule.slotId || schedule.scheduleId || schedule.doctorId}`"
                :data-schedule-key="`${selectedScheduleDate}-${schedule.slotId || schedule.scheduleId || schedule.doctorId}`"
                class="slot-card"
                :class="{
                  selected: form.doctorId === schedule.doctorId && form.selectedDate === selectedScheduleDate,
                  disabled: !isScheduleSelectable(schedule),
                  full: isScheduleFull(schedule),
                  past: isSchedulePast(schedule)
                }"
                @click="selectDoctorSchedule(selectedScheduleDate, schedule)"
              >
                <div class="slot-field slot-time-field doctor-field">
                  <span class="doctor-avatar">
                    <img
                      :src="getDoctorAvatar(schedule)"
                      :alt="schedule.doctorName || '医生'"
                      @error="onDoctorAvatarError"
                    />
                    <span v-if="!schedule.avatarOk" class="doctor-avatar-fallback">{{ getDoctorInitial(schedule) }}</span>
                  </span>
                  <div class="doctor-meta">
                    <strong>{{ schedule.doctorName || '医生' }}</strong>
                    <span class="doctor-sub">
                      <em>{{ schedule.levelName || '普通号' }}</em>
                      <span class="doctor-time">{{ schedule.startTime }}-{{ schedule.endTime }} 可约</span>
                    </span>
                  </div>
                </div>
                <div class="slot-field">
                  <span class="slot-label">余号数量</span>
                  <strong>{{ schedule.availableNumber || Math.max(getScheduleMaxNumber(schedule) - getScheduleReservedNumber(schedule), 0) }}</strong>
                </div>
                <div class="slot-field slot-fee-field">
                  <span class="slot-label">挂号费用</span>
                  <strong>{{ formatMoney(schedule.fee) }}</strong>
                </div>
                <van-icon class="slot-arrow" name="arrow" />
              </div>
            </div>
            <div v-else-if="availableScheduleDates.length" class="empty-day">请选择上方日期查看医生号源</div>
          </div>
        </div>
        <div class="btn-group">
          <van-button class="back-btn" @click="prevStep">
            <van-icon name="arrow-left" />
            上一步
          </van-button>
          <van-button class="next-btn" @click="nextStep" :disabled="!form.doctorId || !form.selectedDate">
            下一步
            <van-icon name="arrow" />
          </van-button>
        </div>
      </div>

      <div v-if="step === 3" class="step-content slide-up-animation" style="animation-delay: 0.1s">
        <div class="form-card">
          <div class="card-header time-card-header">
            <div class="header-text">
              <h3>选择就诊时间</h3>
            </div>
            <div class="appt-info-inline">
              <div class="appt-info-row"><span class="appt-label">科室</span><span class="appt-value">{{ form.department }}</span></div>
              <div class="appt-info-row"><span class="appt-label">医生</span><span class="appt-value">{{ form.doctorName }}<em v-if="selectedSlot?.levelName" class="appt-level">{{ selectedSlot.levelName }}</em></span></div>
              <div class="appt-info-row"><span class="appt-label">日期</span><span class="appt-value">{{ formatScheduleDate(form.selectedDate) }}</span></div>
            </div>
          </div>
          <div class="slot-list">
            <div
              v-for="slot in slotList"
              :key="slot.slotId"
              class="slot-card"
              :class="{
                selected: form.slotId === slot.slotId,
                disabled: !isScheduleSelectable(slot),
                full: isScheduleFull(slot),
                past: isSchedulePast(slot)
              }"
              @click="selectTimeSlot(slot)"
            >
              <div class="slot-field slot-time-field">
                <span class="slot-label">就诊时间段</span>
                <strong>{{ slot.startTime }}-{{ slot.endTime }}</strong>
              </div>
              <div class="slot-field">
                <span class="slot-label">余号数量</span>
                <strong>余号 1</strong>
              </div>
              <div class="slot-field slot-fee-field">
                <span class="slot-label">挂号费用</span>
                <strong>{{ formatMoney(slot.fee) }}</strong>
              </div>
              <van-icon class="slot-arrow" name="arrow" />
            </div>
          </div>
          <div v-if="!slotList.length" class="empty-day">该医生当天暂无可预约时间片</div>
        </div>
        <div class="btn-group">
          <van-button class="back-btn" @click="prevStep">
            <van-icon name="arrow-left" />
            上一步
          </van-button>
          <van-button class="next-btn" @click="nextStep" :disabled="!form.slotId">
            下一步
            <van-icon name="arrow" />
          </van-button>
        </div>
      </div>

      <div v-if="step === 4" class="step-content slide-up-animation" style="animation-delay: 0.1s">
        <div class="form-card confirm-card">
          <div class="card-header">
            <div class="header-text">
              <h3>确认预约信息</h3>
            </div>
          </div>
          <div class="confirm-summary">
            <div>
              <span>预约时间</span>
              <strong>{{ formatScheduleDate(selectedSlot?.scheduleDate || form.selectedDate) }}</strong>
              <em>{{ selectedSlot ? `${selectedSlot.startTime}-${selectedSlot.endTime}` : '-' }}</em>
            </div>
            <div class="confirm-status">待确认</div>
          </div>
          <div class="confirm-info">
            <div class="confirm-section">
              <div class="section-title">就诊信息</div>
              <div class="info-row">
                <span class="info-label">就诊科室</span>
                <span class="info-value">{{ form.department }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">就诊医生</span>
                <span class="info-value">{{ form.doctorName }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">号别类型</span>
                <span class="info-value">{{ selectedSlot?.levelName || '普通号' }}</span>
              </div>
            </div>
            <div class="confirm-section">
              <div class="section-title">费用信息</div>
              <div class="info-row">
                <span class="info-label">余号数量</span>
                <span class="info-value">余号 1</span>
              </div>
              <div class="info-row fee-row">
                <span class="info-label">挂号费用</span>
                <span class="info-value fee-amount">{{ formatMoney(selectedSlot?.fee) }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="btn-group">
          <van-button class="back-btn" @click="prevStep">
            <van-icon name="arrow-left" />
            上一步
          </van-button>
          <van-button 
            class="submit-btn" 
            :class="{ 'success-btn': success }"
            @click="submitRegister" 
            :loading="loading && !success"
            :disabled="success"
          >
            <template v-if="success">
              <van-icon name="success" /> 预约成功！
            </template>
            <template v-else>
              确认预约
            </template>
          </van-button>
        </div>
      </div>
    </div>

    <!-- 预约成功弹窗 -->
    <van-dialog
      v-model:show="showSuccessDialog"
      title="预约成功"
      confirm-button-text="立即支付"
      cancel-button-text="稍后支付"
      :show-cancel-button="true"
      @confirm="payCreatedRegister"
      @cancel="goToHome"
    >
      <div class="dialog-content">
        <div class="pay-alert">
          请在30分钟内支付挂号费，超时未支付将自动取消预约并释放医生号源。
        </div>
        <div class="dialog-title">就诊信息</div>
        <div class="dialog-item">
          <span class="dialog-label">科室：</span>
          <span class="dialog-value">{{ form.department || '未选择' }}</span>
        </div>
        <div class="dialog-item">
          <span class="dialog-label">医生：</span>
          <span class="dialog-value">{{ form.doctorName || '未选择' }}</span>
        </div>
        <div class="dialog-item">
          <span class="dialog-label">就诊时间：</span>
          <span class="dialog-value">{{ formatScheduleDate(selectedSlot?.scheduleDate || form.selectedDate) }} {{ selectedSlot ? `${selectedSlot.startTime}-${selectedSlot.endTime}` : '' }}</span>
        </div>
        <div class="dialog-item">
          <span class="dialog-label">挂号类型：</span>
          <span class="dialog-value">网上预约挂号</span>
        </div>
        <div class="dialog-item">
          <span class="dialog-label">挂号费：</span>
          <span class="dialog-value fee-value">{{ formatMoney(createdRegister?.registerFee) }}</span>
        </div>
      </div>
    </van-dialog>

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
import { createRegister, getDeptList, getOpenDoctorSlots, getOpenScheduleSlots } from '@/api/register'
import { getPatientByUserId } from '@/api/patient'
import { getInfo } from '@/api/user'
import { payRegister } from '@/api/payment'
import doctorAvatar1 from '@/assets/doctor1.png'
import doctorAvatar2 from '@/assets/doctor2.png'

console.log('[Register] module loaded')
const router = useRouter()
const active = ref(1)
const step = ref(1)
const loading = ref(false)
const success = ref(false)
const showSuccessDialog = ref(false)
const createdRegister = ref(null)

const form = ref({
    department: '',
    deptId: null,
    doctorId: null,
    doctorName: '',
    selectedDate: '',
    scheduleId: null,
    slotId: null
})

const departmentList = ref([])
const allDepartmentList = ref([])
const activeParentDeptId = ref(null)
const doctorSlotList = ref([])
const slotList = ref([])
const selectedScheduleDate = ref('')

const selectedSlot = computed(() => {
    return slotList.value.find(s => s.slotId === form.value.slotId)
})

const normalizeDept = (dept) => ({
  ...dept,
  deptId: Number(dept.deptId),
  parentId: dept.parentId == null ? null : Number(dept.parentId)
})

const isHospitalRootDept = (dept) => {
  const deptName = dept.deptName || ''
  return dept.parentId === 0
    || dept.deptId === 200
    || deptName === '医院'
    || deptName === '智慧医院'
    || deptName === '智慧医院总部门'
}

const resetDepartmentSelection = () => {
  form.value.department = ''
  form.value.deptId = null
  form.value.doctorId = null
  form.value.doctorName = ''
  form.value.selectedDate = ''
  form.value.scheduleId = null
  form.value.slotId = null
  doctorSlotList.value = []
  slotList.value = []
  selectedScheduleDate.value = ''
}

const getChildDepartments = (parentId, visited = new Set()) => {
  if (visited.has(parentId)) return []
  visited.add(parentId)
  return allDepartmentList.value
    .filter(dept => dept.parentId === parentId)
    .flatMap(dept => [dept, ...getChildDepartments(dept.deptId, visited)])
}

const subDepartmentList = computed(() => {
  if (!activeParentDeptId.value) return []
  const children = getChildDepartments(activeParentDeptId.value)
  if (children.length) return children
  const current = allDepartmentList.value.find(dept => dept.deptId === activeParentDeptId.value)
  return current ? [current] : []
})

const formatScheduleDate = (dateStr) => {
    if (!dateStr) return ''
    const date = parseBusinessDate(dateStr)
    if (!date) return ''
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

const formatMoney = (amount) => {
  const value = Number(amount || 0)
  return `￥${value.toFixed(2)}`
}

const padDatePart = (value) => String(value).padStart(2, '0')
const dateOnlyPattern = /^(\d{4})[-/](\d{1,2})[-/](\d{1,2})$/
const datePartPattern = /(\d{4})[-/](\d{1,2})[-/](\d{1,2})/

const formatDateParts = (year, month, day) => `${year}-${padDatePart(month)}-${padDatePart(day)}`

const toDateKey = (value) => {
  if (!value) return ''
  if (value instanceof Date) {
    return formatDateParts(value.getFullYear(), value.getMonth() + 1, value.getDate())
  }
  const raw = String(value).trim()
  const dateOnlyMatch = raw.match(dateOnlyPattern)
  if (dateOnlyMatch) {
    return formatDateParts(dateOnlyMatch[1], dateOnlyMatch[2], dateOnlyMatch[3])
  }
  if (raw.includes('T') || /\d{1,2}:\d{2}/.test(raw)) {
    const parsed = new Date(raw.includes('T') ? raw : raw.replace(' ', 'T'))
    if (!Number.isNaN(parsed.getTime())) {
      return formatDateParts(parsed.getFullYear(), parsed.getMonth() + 1, parsed.getDate())
    }
  }
  const datePartMatch = raw.match(datePartPattern)
  if (datePartMatch) {
    return formatDateParts(datePartMatch[1], datePartMatch[2], datePartMatch[3])
  }
  return ''
}

const getLocalDate = (dateKey) => {
  return parseBusinessDate(dateKey)
}

const parseBusinessDate = (value) => {
  const dateKey = toDateKey(value)
  if (!dateKey) return null
  const [year, month, day] = dateKey.split('-').map(Number)
  const date = new Date(year, month - 1, day)
  return Number.isNaN(date.getTime()) ? null : date
}

const getCurrentWeekDates = () => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return Array.from({ length: 7 }, (_, index) => {
    const date = new Date(today)
    date.setDate(today.getDate() + index)
    return date
  })
}

const getDateRange = () => {
  const dates = getCurrentWeekDates()
  return {
    beginDate: toDateKey(dates[0]),
    endDate: toDateKey(dates[dates.length - 1])
  }
}

const getTimeSlotOrder = (timeSlot) => {
  if (!timeSlot) return 9
  const orderMap = { morning: 1, afternoon: 2, evening: 3 }
  return orderMap[timeSlot] || 9
}

const getDoctorInitial = (schedule) => {
  return (schedule?.doctorName || '医').charAt(0)
}

const doctorAvatarPool = [doctorAvatar1, doctorAvatar2]
const doctorAvatarMap = new Map()
const joinApiPath = (path) => {
  const base = import.meta.env.VITE_APP_BASE_API || ''
  if (!base) return path
  return `${base.replace(/\/$/, '')}/${path.replace(/^\//, '')}`
}

const resolveAvatarUrl = (avatar) => {
  const raw = String(avatar || '').trim()
  if (!raw) return ''
  if (/^(data:|blob:|https?:\/\/)/i.test(raw)) return raw
  if (raw.startsWith(import.meta.env.VITE_APP_BASE_API || '/__empty_base__')) return raw
  if (raw.startsWith('/profile/') || raw.startsWith('profile/')) return joinApiPath(raw)
  if (raw.startsWith('/')) return joinApiPath(raw)
  return joinApiPath(`/profile/${raw}`)
}

const getDefaultDoctorAvatar = (schedule) => {
  const id = schedule?.doctorId
  if (id == null) return doctorAvatarPool[0]
  if (doctorAvatarMap.has(id)) return doctorAvatarMap.get(id)
  const seed = String(id).split('').reduce((acc, ch) => acc + ch.charCodeAt(0), 0)
  const nameSeed = (schedule?.doctorName || '').charCodeAt(0) || 0
  const avatar = doctorAvatarPool[(seed + nameSeed) % doctorAvatarPool.length]
  doctorAvatarMap.set(id, avatar)
  return avatar
}

const getDoctorAvatar = (schedule) => {
  schedule.avatarOk = true
  if (!schedule?.avatarBroken) {
    const dbAvatar = resolveAvatarUrl(schedule?.avatar)
    if (dbAvatar) return dbAvatar
  }
  return getDefaultDoctorAvatar(schedule)
}

const onDoctorAvatarError = (event) => {
  const target = event?.target
  const card = target?.closest('.slot-card')
  let item = null
  if (card) {
    const scheduleKey = card.dataset?.scheduleKey
    if (scheduleKey) {
      item = (selectedDateSchedules.value || []).find(
        (s) => `${selectedScheduleDate.value}-${s.slotId || s.scheduleId || s.doctorId}` === scheduleKey
      )
    }
  }
  if (item && !item.avatarBroken && resolveAvatarUrl(item.avatar)) {
    item.avatarBroken = true
    if (target) {
      target.style.display = 'block'
      target.src = getDefaultDoctorAvatar(item)
    }
    return
  }
  if (target && target.style) target.style.display = 'none'
  if (item) item.avatarOk = false
}

const scheduleDateGroups = computed(() => {
  const grouped = new Map()
  doctorSlotList.value.forEach((schedule) => {
    const dateKey = toDateKey(schedule.scheduleDate)
    if (!grouped.has(dateKey)) grouped.set(dateKey, [])
    grouped.get(dateKey).push(schedule)
  })

  return getCurrentWeekDates().map((date) => {
    const dateKey = toDateKey(date)
    const schedules = (grouped.get(dateKey) || []).sort((a, b) => {
      const slotDiff = getTimeSlotOrder(a.timeSlot) - getTimeSlotOrder(b.timeSlot)
      if (slotDiff !== 0) return slotDiff
      const levelDiff = Number(b.levelId || 0) - Number(a.levelId || 0)
      if (levelDiff !== 0) return levelDiff
      return Number(a.doctorId || 0) - Number(b.doctorId || 0)
    })

    return {
      date: dateKey,
      monthDay: `${date.getMonth() + 1}月${date.getDate()}日`,
      weekDay: ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][date.getDay()],
      schedules
    }
  })
})

const availableScheduleDates = computed(() => {
  return scheduleDateGroups.value.filter(day => day.schedules.some(isScheduleSelectable))
})

const selectedDateSchedules = computed(() => {
  return (availableScheduleDates.value.find(day => day.date === selectedScheduleDate.value)?.schedules || [])
    .filter(isScheduleSelectable)
})

const getDayAvailableCount = (day) => {
  return day.schedules.reduce((total, schedule) => {
    return total + Number(schedule?.availableNumber ?? schedule?.available_number ?? 0)
  }, 0)
}

const getScheduleReservedNumber = (schedule) => {
  const reserved = schedule?.reservedNumber ?? schedule?.reserved_number
  if (reserved != null) {
    return Number(reserved)
  }
  const max = getScheduleMaxNumber(schedule)
  const available = schedule?.availableNumber ?? schedule?.available_number
  if (max != null && available != null) {
    return Math.max(Number(max) - Number(available), 0)
  }
  return 0
}

const getScheduleMaxNumber = (schedule) => {
  const max = schedule?.maxNumber ?? schedule?.max_number
  if (max != null) {
    return Number(max)
  }
  const reserved = schedule?.reservedNumber ?? schedule?.reserved_number
  const available = schedule?.availableNumber ?? schedule?.available_number
  if (reserved != null && available != null) {
    return Math.max(Number(reserved) + Number(available), 0)
  }
  return 0
}

const isScheduleFull = (schedule) => {
  if (!schedule) return true
  const reserved = getScheduleReservedNumber(schedule)
  const max = getScheduleMaxNumber(schedule)
  const status = schedule.status
  if (status != null && status !== '0' && status !== 0) return true
  if (String(status).toLowerCase() === '已满' || String(status).toLowerCase() === 'full') return true
  return max > 0 && reserved >= max
}

const isSchedulePast = (schedule) => {
  if (!schedule?.scheduleDate) return true
  const target = parseBusinessDate(schedule.scheduleDate)
  if (!target) return true
  
  const today = new Date()
  const todayStart = new Date(today.getFullYear(), today.getMonth(), today.getDate())
  const targetStart = new Date(target.getFullYear(), target.getMonth(), target.getDate())
  
  // 如果目标日期在今天之前，返回true（已过去）
  if (targetStart < todayStart) {
    return true
  }
  
  // 如果目标日期在今天之后，返回false（未过去）
  if (targetStart > todayStart) {
    return false
  }
  
  // 如果是今天，需要检查时间段
  const nowMinutes = today.getHours() * 60 + today.getMinutes()
  if (schedule?.startTime) {
    const [hour, minute] = String(schedule.startTime).split(':').map(Number)
    return nowMinutes >= hour * 60 + minute
  }
  
  return false
}

const isScheduleSelectable = (schedule) => !isSchedulePast(schedule) && !isScheduleFull(schedule)

const getScheduleAvailableText = (schedule) => {
  const max = getScheduleMaxNumber(schedule)
  const reserved = getScheduleReservedNumber(schedule)
  const available = Number(schedule?.availableNumber ?? schedule?.available_number ?? Math.max(max - reserved, 0))
  if (max <= 0) {
    return '余号 0'
  }
  return `余号 ${available} · 已约 ${reserved}/${max}`
}

const normalizeSchedule = (schedule) => {
  const max = getScheduleMaxNumber(schedule)
  const reserved = getScheduleReservedNumber(schedule)
  const available = Number(schedule?.availableNumber ?? schedule?.available_number ?? Math.max(max - reserved, 0))
  return {
    ...schedule,
    maxNumber: max,
    reservedNumber: reserved,
    availableNumber: available,
    slotId: schedule.slotId ?? schedule.slot_id,
    scheduleId: schedule.scheduleId ?? schedule.schedule_id,
    doctorId: schedule.doctorId ?? schedule.doctor_id,
    deptId: schedule.deptId ?? schedule.dept_id,
    doctorName: schedule.doctorName ?? schedule.doctor_name,
    avatar: schedule.avatar,
    avatarOk: true,
    avatarBroken: false,
    levelId: schedule.levelId ?? schedule.level_id,
    levelName: schedule.levelName ?? schedule.level_name,
    fee: schedule.fee ?? schedule.registerFee ?? schedule.register_fee,
    scheduleDate: toDateKey(schedule.scheduleDate ?? schedule.schedule_date),
    startTime: schedule.startTime ?? schedule.start_time,
    endTime: schedule.endTime ?? schedule.end_time,
    timeSlot: schedule.timeSlot ?? schedule.time_slot,
    status: schedule.status
  }
}

const loadDepartments = async () => {
    const fallbackDepartments = [
        { deptId: 201, parentId: 200, deptName: '内科' },
        { deptId: 202, parentId: 200, deptName: '外科' },
        { deptId: 203, parentId: 200, deptName: '儿科' },
        { deptId: 204, parentId: 200, deptName: '妇产科' },
        { deptId: 205, parentId: 200, deptName: '骨科' },
        { deptId: 206, parentId: 200, deptName: '眼科' }
    ]
    const applyDepartments = (list) => {
        const normalized = list.map(normalizeDept).filter(dept => !isHospitalRootDept(dept))
        const allIds = new Set(normalized.map(dept => dept.deptId))
        const parentCandidates = normalized.filter(dept => !allIds.has(dept.parentId))
        allDepartmentList.value = normalized
        departmentList.value = parentCandidates.length ? parentCandidates : normalized
        activeParentDeptId.value = departmentList.value[0]?.deptId || null
        resetDepartmentSelection()
    }

    try {
        const res = await getDeptList()
        if (res.data && res.data.length > 0) {
            applyDepartments(res.data)
        }
        if (departmentList.value.length === 0) {
            applyDepartments(fallbackDepartments)
        }
    } catch (error) {
        console.error('加载科室失败', error)
        applyDepartments(fallbackDepartments)
    }
}

const loadDoctorSlots = async () => {
    if (!form.value.deptId) {
        doctorSlotList.value = []
        return
    }
    try {
        const dateRange = getDateRange()
        const res = await getOpenDoctorSlots({
            pageNum: 1,
            pageSize: 200,
            deptId: form.value.deptId,
            beginDate: dateRange.beginDate,
            endDate: dateRange.endDate
        })
        const rows = res.rows || []
        doctorSlotList.value = rows
            .map(normalizeSchedule)
            .filter(item => !isSchedulePast(item))
            .sort((a, b) => {
                const dateDiff = getLocalDate(a.scheduleDate) - getLocalDate(b.scheduleDate)
                if (dateDiff !== 0) return dateDiff
                return Number(b.levelId || 0) - Number(a.levelId || 0)
            })
        selectedScheduleDate.value = doctorSlotList.value.find(isScheduleSelectable)?.scheduleDate || ''
    } catch (error) {
        console.error('加载可预约医生失败', error)
        doctorSlotList.value = []
        selectedScheduleDate.value = ''
    }
}

const loadTimeSlots = async () => {
    if (!form.value.doctorId || !form.value.selectedDate) {
        slotList.value = []
        return
    }
    try {
        const res = await getOpenScheduleSlots({
            pageNum: 1,
            pageSize: 100,
            doctorId: form.value.doctorId,
            scheduleDate: form.value.selectedDate
        })
        slotList.value = (res.rows || [])
            .map(normalizeSchedule)
            .filter(item => !isSchedulePast(item))
            .sort((a, b) => String(a.startTime || '').localeCompare(String(b.startTime || '')))
    } catch (error) {
        console.error('加载时间片失败', error)
        slotList.value = []
    }
}

const selectParentDepartment = (dept) => {
    activeParentDeptId.value = dept.deptId
    resetDepartmentSelection()
}

const selectDepartment = async (dept) => {
    form.value.department = dept.deptName
    form.value.deptId = dept.deptId
    form.value.doctorId = null
    form.value.doctorName = ''
    form.value.selectedDate = ''
    form.value.scheduleId = null
    form.value.slotId = null
    slotList.value = []
    selectedScheduleDate.value = ''
    await loadDoctorSlots()
}

const selectScheduleDate = (date) => {
    if (selectedScheduleDate.value === date) return
    selectedScheduleDate.value = date
    form.value.doctorId = null
    form.value.doctorName = ''
    form.value.selectedDate = ''
    form.value.scheduleId = null
    form.value.slotId = null
    slotList.value = []
}

const selectDoctorSchedule = (date, schedule) => {
    if (!isScheduleSelectable(schedule)) {
      if (isSchedulePast(schedule)) {
        showToast('只能选择今天及以后的排班时间')
        return
      }
      showToast('该排班已约满或已关闭，请选择其他时间')
      return
    }
    form.value.doctorId = schedule.doctorId
    form.value.doctorName = schedule.doctorName
    form.value.selectedDate = date
    form.value.scheduleId = null
    form.value.slotId = null
    slotList.value = []
}

const selectTimeSlot = (slot) => {
    if (!isScheduleSelectable(slot)) {
      showToast('该时间片已约满或已关闭，请选择其他时间')
      return
    }
    form.value.scheduleId = slot.scheduleId
    form.value.slotId = slot.slotId
}

const nextStep = async () => {
    if (step.value === 1) {
        if (!form.value.deptId) {
            showToast('请选择就诊科室')
            return
        }
        await loadDoctorSlots()
        step.value = 2
        return
    }
    if (step.value === 2) {
        if (!form.value.doctorId || !form.value.selectedDate) {
            showToast('请选择就诊医生')
            return
        }
        await loadTimeSlots()
        step.value = 3
        return
    }
    if (step.value === 3 && !form.value.slotId) {
        showToast('请选择排班时间')
        return
    }
    step.value++
}

const prevStep = () => {
    step.value--
}

const submitRegister = async () => {
    loading.value = true
    success.value = false
    try {
        const patientId = localStorage.getItem('patientId')
        const registerData = {
            patientId: patientId ? Number(patientId) : null,
            doctorId: form.value.doctorId,
            deptId: form.value.deptId,
            scheduleId: form.value.scheduleId,
            slotId: form.value.slotId,
            registerTime: toDateKey(form.value.selectedDate || selectedSlot.value?.scheduleDate),
            registerType: 'online'
        }
        console.log('提交的预约数据:', registerData)
        const result = await createRegister(registerData)
        createdRegister.value = result?.data || null
        
        // 预约成功，设置状态
        success.value = true
        loading.value = false
        
        // 显示预约成功弹窗
        showSuccessDialog.value = true
    } catch (error) {
        console.error(error)
        showToast(error?.message || '预约失败，请稍后重试')
    } finally {
        if (!success.value) {
            loading.value = false
        }
    }
}

const goBack = () => {
    if (step.value > 1) {
        prevStep()
    } else {
        router.back()
    }
}

const goToHome = () => {
    showSuccessDialog.value = false
    router.push('/')
}

const payCreatedRegister = async () => {
    const registerId = createdRegister.value?.registerId
    if (!registerId) {
        showToast('未获取到挂号单号，请在我的预约中支付')
        goToHome()
        return
    }
    try {
        await payRegister(registerId, '微信')
        await showDialog({
            title: '支付成功',
            message: `已支付挂号费 ${formatMoney(createdRegister.value?.registerFee)}`,
            confirmButtonText: '确定'
        })
        goToHome()
    } catch (error) {
        showToast(error?.message || '支付失败，请稍后重试')
        showSuccessDialog.value = true
    }
}

const ensurePatientId = async () => {
    try {
        const savedPatientId = localStorage.getItem('patientId')
        
        if (!savedPatientId) {
            const userRes = await getInfo()
            const userId = userRes?.user?.userId
            if (userId) {
                const patientRes = await getPatientByUserId(userId)
                const patient = patientRes?.data
                if (patient) {
                    localStorage.setItem('patientId', patient.patientId)
                    localStorage.setItem('patientName', patient.name)
                }
            }
        }
    } catch (error) {
        console.error('获取患者信息失败', error)
    }
}

onMounted(() => {
    ensurePatientId()
    loadDepartments()
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
    background: #4A90E2;
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
    font-size: 20px;
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
    background: linear-gradient(135deg, #4A90E2, #A8C8EC);
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
  padding: 0 10px;
}

.step-indicator {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 640px;
  margin: 0 auto;
  padding: 4px 2px 16px;
}

.step-item {
  display: flex;
  flex: 0 0 48px;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  opacity: 0.5;
  transition: all 0.3s ease;

  &.active,
  &.completed {
    opacity: 1;
  }
}

.step-circle {
  width: 34px;
  height: 34px;
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
    background: linear-gradient(135deg, #4A90E2, #A8C8EC);
    color: white;
    border-color: #4A90E2;
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
  font-size: 11px;
  line-height: 1;
  color: #7e98a4;
  font-weight: 500;
  white-space: nowrap;

  .step-item.active & {
    color: #5f7580;
  }
}

.step-line {
  flex: 1;
  min-width: 12px;
  max-width: 84px;
  height: 3px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 2px;
  margin: 0 4px 24px;
  transition: all 0.3s ease;
}

.step-content {
  width: 100%;
  max-width: 720px;
  min-height: 390px;
  margin: 0 auto 24px;
}

.form-card {
  background: rgba(255, 255, 255, 0.74);
  min-height: 360px;
  border-radius: 0;
  padding: 28px;
  border: 1px solid rgba(194, 228, 236, 0.72);
  box-shadow: var(--card-shadow);
  backdrop-filter: blur(8px);
}

.card-header {
  display: flex;
  align-items: flex-start;
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(213, 237, 243, 0.6);
}

.header-text {
  flex: 1;

  h3 {
    font-size: 17px;
    font-weight: 700;
    color: #4f7380;
    margin: 0 0 4px;
  }

  p {
    font-size: 12px;
    color: #8e9fa8;
    margin: 0;
  }
}

.department-selector {
  display: grid;
  grid-template-columns: minmax(110px, 0.32fr) minmax(0, 0.68fr);
  gap: 14px;
  min-height: 260px;
}

.department-column {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 0;
}

.parent-column {
  gap: 4px;
}

.child-column {
  padding-left: 14px;
  border-left: 1px solid rgba(213, 237, 243, 0.85);
}

.dept-item {
  display: flex;
  align-items: center;
  min-height: 54px;
  padding: 14px 18px;
  background: rgba(255, 255, 255, 0.66);
  border: 1px solid rgba(213, 237, 243, 0.9);
  border-left: 4px solid transparent;
  border-radius: 0;
  cursor: pointer;
  transition: all 0.25s ease;

  &:hover {
    transform: translateX(2px);
    background: rgba(255, 255, 255, 0.9);
    border-color: rgba(104, 199, 169, 0.42);
    border-left-color: rgba(104, 199, 169, 0.42);
  }

  &:active {
    transform: translateX(0);
  }

  &.selected {
    border-color: rgba(104, 199, 169, 0.75);
    border-left-color: #4A90E2;
    background: linear-gradient(135deg, rgba(104, 199, 169, 0.12), rgba(255, 255, 255, 0.92));
    box-shadow: 0 8px 18px rgba(104, 199, 169, 0.12);
  }
}

.parent-dept {
  background: transparent;
  border: none;
  border-left: 4px solid transparent;
  min-height: auto;
  padding: 8px 12px;

  &:hover {
    background: rgba(255, 255, 255, 0.4);
    border-color: transparent;
    border-left-color: rgba(104, 199, 169, 0.42);
  }

  &.selected {
    border-color: transparent;
    border-left-color: #4A90E2;
    background: rgba(104, 199, 169, 0.08);
    box-shadow: none;
  }
}

.dept-name {
  flex: 1;
  min-width: 0;
  font-size: 15px;
  line-height: 1.4;
  font-weight: 600;
  color: #4f7380;
}

.parent-column .dept-name {
  font-size: 13px;
}

.child-dept {
  background: rgba(255, 255, 255, 0.78);
}

.empty-sub-dept {
  min-height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 18px;
  border: 1px dashed rgba(126, 152, 164, 0.36);
  color: #8e9fa8;
  font-size: 14px;
  background: rgba(255, 255, 255, 0.38);
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
    border-color: #4A90E2;
    background: linear-gradient(135deg, rgba(104, 199, 169, 0.08), rgba(255, 255, 255, 0.8));
  }
}

.doctor-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4A90E2, #A8C8EC);
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
  color: #4A90E2;
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
  background: linear-gradient(135deg, #4A90E2, #A8C8EC);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
  font-weight: 700;
}

.schedule-calendar {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.date-strip {
  display: flex;
  gap: 6px;
  overflow-x: auto;
  padding: 2px 2px 6px;
}

.date-card {
  min-width: 64px;
  min-height: 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1px;
  padding: 4px 6px;
  border: 1px solid rgba(213, 237, 243, 0.9);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.62);
  color: #5f7580;
  cursor: pointer;
  transition: all 0.25s ease;

  strong {
    font-size: 12px;
    font-weight: 700;
    color: #4f7380;
    line-height: 1.2;
  }

  em {
    display: none;
  }

  small {
    font-size: 10px;
    color: #4A90E2;
    line-height: 1.2;
  }

  &:hover {
    transform: translateY(-1px);
    border-color: rgba(104, 199, 169, 0.45);
    background: rgba(255, 255, 255, 0.9);
  }

  &.active {
    border-color: rgba(104, 199, 169, 0.82);
    background: linear-gradient(135deg, rgba(104, 199, 169, 0.14), rgba(255, 255, 255, 0.94));
    box-shadow: 0 8px 18px rgba(104, 199, 169, 0.14);
  }
}

.date-week {
  font-size: 11px;
  color: #7e98a4;
  line-height: 1.2;
}

.schedule-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.empty-day {
  padding: 14px 16px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.45);
  color: #8e9fa8;
  font-size: 13px;
  text-align: center;
}

// 医生卡片（步骤 2 复用 slot-card 样式时的扩展）
.doctor-field {
  display: flex;
  align-items: center;
  gap: 10px;
}

.doctor-avatar {
  position: relative;
  width: 40px;
  height: 40px;
  flex: 0 0 auto;
  border-radius: 50%;
  overflow: hidden;
  background: linear-gradient(135deg, #4A90E2, #A8C8EC);
  color: #fff;
  display: grid;
  place-items: center;
  box-shadow: 0 6px 14px rgba(104, 199, 169, 0.22);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }
}

.doctor-avatar-fallback {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  font-size: 16px;
  font-weight: 800;
  color: #fff;
}

.doctor-meta {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;

  > strong {
    display: block;
    font-size: 14px;
    color: #4f7380;
    line-height: 1.3;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    margin: 0;
  }
}

.doctor-sub {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  font-size: 12px;
  line-height: 1.3;
  color: #8e9fa8;

  em {
    flex: 0 0 auto;
    font-style: normal;
    color: #4A90E2;
    background: rgba(104, 199, 169, 0.12);
    padding: 1px 6px;
    border-radius: 6px;
    font-weight: 700;
  }
}

.doctor-time {
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.slot-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.slot-card {
  min-height: 64px;
  padding: 12px 14px;
  border-radius: 0;
  border: 1px solid rgba(213, 237, 243, 0.9);
  border-left: 4px solid transparent;
  background: rgba(255, 255, 255, 0.68);
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.25s ease;

  strong {
    display: block;
    font-size: 14px;
    color: #4f7380;
    margin: 0;
  }

  .slot-label {
    display: block;
    font-size: 12px;
    line-height: 1.35;
    color: #8e9fa8;
    margin-bottom: 3px;
  }

  &.selected {
    border-color: rgba(104, 199, 169, 0.78);
    border-left-color: #4A90E2;
    background: linear-gradient(135deg, rgba(104, 199, 169, 0.12), rgba(255, 255, 255, 0.92));
    box-shadow: 0 8px 18px rgba(104, 199, 169, 0.12);
  }

  &.disabled,
  &.full,
  &.past {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.slot-field {
  flex: 1;
  min-width: 0;
}

.slot-time-field {
  flex: 1.25;
}

.slot-fee-field {
  flex: 0.9;

  strong {
    color: #f05391;
  }
}

.slot-arrow {
  flex-shrink: 0;
  color: #4A90E2;
  font-size: 18px;
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
    color: #4A90E2;
  }

  .status-full {
    color: #f05391;
  }

    .status-past {
      color: #999;
    }
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

.confirm-card {
  background: rgba(255, 255, 255, 0.82);
}

.confirm-summary {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  margin-bottom: 18px;
  border: 1px solid rgba(194, 228, 236, 0.88);
  border-left: 4px solid #4A90E2;
  background: linear-gradient(135deg, rgba(230, 246, 251, 0.82), rgba(255, 255, 255, 0.92));

  span {
    display: block;
    font-size: 13px;
    color: #7e98a4;
    margin-bottom: 6px;
  }

  strong {
    display: block;
    font-size: 20px;
    line-height: 1.25;
    color: #4f7380;
    margin-bottom: 4px;
  }

  em {
    font-size: 14px;
    font-style: normal;
    color: #4A90E2;
    font-weight: 700;
  }
}

.confirm-status {
  flex-shrink: 0;
  padding: 5px 10px;
  border: 1px solid rgba(104, 199, 169, 0.34);
  background: rgba(104, 199, 169, 0.12);
  color: #357ABD;
  font-size: 12px;
  font-weight: 700;
}

.confirm-info {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.confirm-section {
  border: 1px solid rgba(213, 237, 243, 0.88);
  background: rgba(255, 255, 255, 0.62);
}

.section-title {
  padding: 12px 16px;
  border-bottom: 1px solid rgba(213, 237, 243, 0.76);
  color: #4f7380;
  font-size: 15px;
  font-weight: 700;
}

.info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 13px 16px;
  border-bottom: 1px solid rgba(213, 237, 243, 0.52);

  &:last-child {
    border-bottom: none;
  }
}

.fee-row {
  background: rgba(240, 83, 145, 0.05);
}

.info-label {
  font-size: 14px;
  color: #8e9fa8;
}

.info-value {
  font-size: 14px;
  font-weight: 600;
  color: #4f7380;
  text-align: right;
}

.fee-amount {
  color: #f05391;
  font-size: 18px;
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
  background: linear-gradient(135deg, #4A90E2, #A8C8EC);
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
  
  &.success-btn {
    background: linear-gradient(135deg, #52c41a, #73d13d);
    box-shadow: 0 8px 24px rgba(82, 196, 26, 0.3);
  }
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

/* 弹窗内容样式 */
.dialog-content {
  padding: 10px;
}

.dialog-title {
  font-weight: bold;
  font-size: 16px;
  color: #333;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.pay-alert {
  margin-bottom: 14px;
  padding: 10px 12px;
  border-radius: 12px;
  background: rgba(255, 152, 0, 0.1);
  color: #b36b00;
  font-size: 13px;
  line-height: 1.5;
}

.dialog-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  font-size: 14px;
}

.dialog-label {
  color: #666;
  min-width: 60px;
}

.dialog-value {
  color: #333;
  flex: 1;
}

.fee-value {
  color: #f05391;
  font-weight: 700;
}

.time-card-header {
  flex-direction: column;
  align-items: stretch;

  .header-text {
    margin-bottom: 10px;
  }
}

.appt-info-inline {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 16px;
  align-items: center;
  width: 100%;
}

.appt-info-row {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.appt-label {
  font-size: 12px;
  color: #8e9fa8;
  font-weight: 500;
}

.appt-value {
  font-size: 13px;
  font-weight: 600;
  color: #4f7380;
}

.appt-level {
  display: inline-block;
  margin-left: 6px;
  padding: 1px 8px;
  font-size: 10px;
  font-weight: 700;
  font-style: normal;
  color: #4A90E2;
  background: rgba(104, 199, 169, 0.12);
  border-radius: 6px;
}

.fee-text {
  color: #f05391;
}

@media (max-width: 480px) {
  .content-section {
    padding: 0 6px;
  }

  .form-card {
    min-height: 320px;
    padding: 18px 12px;
  }

  .card-header {
    margin-bottom: 12px;
    padding-bottom: 10px;
  }

  .department-selector {
    grid-template-columns: minmax(90px, 0.3fr) minmax(0, 0.7fr);
    gap: 10px;
  }

  .child-column {
    padding-left: 10px;
  }

  .dept-item {
    min-height: 50px;
    padding: 12px;
  }

  .dept-name {
    font-size: 14px;
  }

  .date-card {
    min-width: 72px;
    min-height: 58px;
    padding: 6px 7px;
    gap: 2px;

    strong {
      font-size: 13px;
    }

    small {
      font-size: 10px;
    }
  }

  .date-week {
    font-size: 10px;
  }

  .slot-list {
    gap: 9px;
    margin: 0 -4px;
  }

  .slot-card {
    min-height: 68px;
    gap: 7px;
    padding: 10px 8px;
  }

  .slot-time-field {
    flex: 1.22;
  }

  .doctor-field {
    gap: 8px;
  }

  .doctor-avatar {
    width: 38px;
    height: 38px;
  }

  .doctor-meta > strong {
    font-size: 13px;
  }

  .doctor-sub {
    gap: 4px;
    font-size: 11px;
  }

  .doctor-time {
    display: none;
  }

  .slot-card strong {
    font-size: 13px;
  }

  .slot-card .slot-label {
    font-size: 11px;
  }

  .slot-fee-field {
    flex: 0.82;
  }

  .slot-arrow {
    font-size: 17px;
  }

  .confirm-summary {
    padding: 15px 14px;
    gap: 10px;

    strong {
      font-size: 18px;
    }
  }

  .info-row {
    padding: 12px;
  }

  .section-title {
    padding: 11px 12px;
  }
}
</style>
