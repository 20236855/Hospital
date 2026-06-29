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
              @click="selectDepartment(dept)"
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
            <div class="header-icon">📅</div>
            <div class="header-text">
              <h3>选择就诊号源</h3>
              <p>{{ form.department }}未来一周可预约医生</p>
            </div>
          </div>
          <div class="schedule-week">
            <section
              v-for="day in scheduleDateGroups"
              :key="day.date"
              class="schedule-day"
            >
              <div class="schedule-day-header">
                <div>
                  <strong>{{ day.monthDay }}</strong>
                  <span>{{ day.weekDay }}</span>
                </div>
                <em>{{ day.schedules.length ? `${day.schedules.length}位医生` : '暂无医生' }}</em>
              </div>
              <div v-if="day.schedules.length" class="schedule-list">
                <div
                  v-for="schedule in day.schedules"
                  :key="`${day.date}-${schedule.doctorId}`"
                  class="schedule-card"
                  :class="{
                    selected: form.doctorId === schedule.doctorId && form.selectedDate === day.date,
                    disabled: !isScheduleSelectable(schedule),
                    full: isScheduleFull(schedule),
                    past: isSchedulePast(schedule)
                  }"
                  @click="selectDoctorSchedule(day.date, schedule)"
                >
                  <div class="schedule-main">
                    <div class="schedule-doctor">
                      <span class="doctor-avatar-mini">{{ getDoctorInitial(schedule) }}</span>
                      <div>
                        <strong>{{ schedule.doctorName || '医生' }}</strong>
                        <span>{{ schedule.levelName || '普通号' }}</span>
                      </div>
                    </div>
                    <div class="schedule-time">{{ schedule.startTime }}-{{ schedule.endTime }} 可约</div>
                  </div>
                  <div class="schedule-meta">
                    <strong>{{ formatMoney(schedule.fee) }}</strong>
                    <span>{{ getScheduleAvailableText(schedule) }}</span>
                  </div>
                  <div v-if="form.doctorId === schedule.doctorId && form.selectedDate === day.date" class="select-mark">
                    <div class="mark-circle">✓</div>
                  </div>
                </div>
              </div>
              <div v-else class="empty-day">当天暂无可预约医生</div>
            </section>
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
          <div class="card-header">
            <div class="header-icon">⏱</div>
            <div class="header-text">
              <h3>选择就诊时间</h3>
              <p>{{ form.doctorName }} · {{ formatScheduleDate(form.selectedDate) }}</p>
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
              <div>
                <strong>{{ slot.startTime }}-{{ slot.endTime }}</strong>
                <span>{{ slot.levelName || '普通号' }} · {{ getScheduleAvailableText(slot) }}</span>
              </div>
              <em>{{ formatMoney(slot.fee) }}</em>
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
              <span class="info-value">{{ formatScheduleDate(selectedSlot?.scheduleDate || form.selectedDate) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">就诊时段</span>
              <span class="info-value">{{ selectedSlot ? `${selectedSlot.startTime}-${selectedSlot.endTime}` : '-' }}</span>
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
const doctorSlotList = ref([])
const slotList = ref([])

const selectedSlot = computed(() => {
    return slotList.value.find(s => s.slotId === form.value.slotId)
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
    try {
        const res = await getDeptList()
        if (res.data && res.data.length > 0) {
            departmentList.value = res.data.filter(dept => {
                const isRootByParent = dept.parentId !== undefined && String(dept.parentId) === '0'
                const isRootByName = dept.deptName === '智慧医院' || dept.deptName === '智慧医院总部门'
                const isRootById = dept.deptId === 200
                return !isRootByParent && !isRootByName && !isRootById
            })
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
    } catch (error) {
        console.error('加载可预约医生失败', error)
        doctorSlotList.value = []
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

const selectDepartment = async (dept) => {
    form.value.department = dept.deptName
    form.value.deptId = dept.deptId
    form.value.doctorId = null
    form.value.doctorName = ''
    form.value.selectedDate = ''
    form.value.scheduleId = null
    form.value.slotId = null
    slotList.value = []
    await loadDoctorSlots()
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

.schedule-week {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.schedule-day {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.schedule-day-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 0 2px;

  div {
    display: flex;
    align-items: baseline;
    gap: 8px;
  }

  strong {
    font-size: 16px;
    font-weight: 700;
    color: #4f7380;
  }

  span,
  em {
    font-size: 13px;
    color: #8e9fa8;
    font-style: normal;
  }
}

.schedule-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.schedule-card {
  display: flex;
  align-items: center;
  gap: 12px;
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

  &.disabled {
    opacity: 0.55;
    cursor: not-allowed;
    background: rgba(220, 230, 236, 0.9);
  }

  &.full {
    opacity: 0.5;
    cursor: not-allowed;
    background: rgba(240, 83, 145, 0.08);
  }

  &.past {
    opacity: 0.45;
    cursor: not-allowed;
    background: rgba(150, 160, 170, 0.08);
  }
}

.schedule-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.schedule-doctor {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;

  div {
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  strong {
    font-size: 16px;
    color: #4f7380;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  span:last-child {
    font-size: 12px;
    color: #68c7a9;
  }
}

.doctor-avatar-mini {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: linear-gradient(135deg, #68c7a9, #89dbc1);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  flex-shrink: 0;
}

.schedule-meta {
  min-width: 78px;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  text-align: right;

  strong {
    font-size: 16px;
    color: #f05391;
  }

  span {
    font-size: 12px;
    line-height: 1.35;
    color: #68c7a9;
  }
}

.empty-day {
  padding: 14px 16px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.45);
  color: #8e9fa8;
  font-size: 13px;
  text-align: center;
}

.slot-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.slot-card {
  min-height: 86px;
  padding: 14px;
  border-radius: 16px;
  border: 2px solid transparent;
  background: rgba(255, 255, 255, 0.6);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 10px;
  cursor: pointer;
  transition: all 0.3s ease;

  strong {
    display: block;
    font-size: 15px;
    color: #4f7380;
    margin-bottom: 4px;
  }

  span {
    display: block;
    font-size: 12px;
    line-height: 1.35;
    color: #8e9fa8;
  }

  em {
    font-size: 14px;
    font-style: normal;
    font-weight: 700;
    color: #f05391;
  }

  &.selected {
    border-color: #68c7a9;
    background: linear-gradient(135deg, rgba(104, 199, 169, 0.1), rgba(142, 214, 242, 0.1));
  }

  &.disabled,
  &.full,
  &.past {
    opacity: 0.5;
    cursor: not-allowed;
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
</style>
