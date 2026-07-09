<template>
  <div class="chat-page">
    <div class="ambient-grid" aria-hidden="true"></div>

    <div class="chat-header">
      <button class="icon-btn header-left" type="button" aria-label="返回" @click="goBack">
        <svg viewBox="0 0 24 24" fill="none">
          <path d="M15 18l-6-6 6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>

      <div class="header-title">
        <div class="ai-avatar">
          <span class="avatar-ring"></span>
          <img :src="assistantAvatar" alt="智能助手" />
        </div>
        <div class="title-copy">
          <h2>智能问诊助手</h2>
          <span class="ai-status"><i></i>实时在线</span>
        </div>
      </div>

      <div class="header-actions">
        <button class="icon-btn" type="button" aria-label="历史记录" @click="toggleSidebar">
          <svg viewBox="0 0 24 24" fill="none">
            <path d="M4 5h16M4 12h12M4 19h9" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </button>
        <button class="icon-btn primary-action" type="button" aria-label="新建对话" @click="createNewSession">
          <svg viewBox="0 0 24 24" fill="none">
            <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>
    </div>

    <div class="chat-main">
      <div v-if="!sidebarCollapsed" class="history-mask" @click="toggleSidebar"></div>

      <aside :class="['session-sidebar', { collapsed: sidebarCollapsed }]">
        <div class="sidebar-header">
          <div>
            <span>CONSULT LOG</span>
            <h3>历史记录</h3>
          </div>
          <button class="icon-btn" type="button" aria-label="收起历史记录" @click="toggleSidebar">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </button>
        </div>

        <button class="new-session-card" type="button" @click="createNewSession">
          <span class="new-session-icon">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </span>
          <span>开启新的问诊</span>
        </button>

        <div class="session-list">
          <button
            v-for="(sessionId, index) in sessionList"
            :key="sessionId"
            :class="['session-item', { active: currentSessionId === sessionId }]"
            type="button"
            @click="switchSession(sessionId)"
          >
            <span class="session-index">{{ String(index + 1).padStart(2, '0') }}</span>
            <span class="session-info">
              <strong>问诊记录 {{ index + 1 }}</strong>
              <small>{{ sessionId === currentSessionId ? '当前对话' : '点击继续' }}</small>
            </span>
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M9 18l6-6-6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>

          <div v-if="sessionList.length === 0" class="empty-history">
            <span class="empty-history-icon">
              <svg viewBox="0 0 24 24" fill="none">
                <path d="M5 6h14v12H5zM8 10h8M8 14h5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </span>
            <strong>暂无历史记录</strong>
            <small>发送第一条消息后会自动保存</small>
          </div>
        </div>
      </aside>

      <section class="chat-content" ref="chatContentRef">
        <div v-if="messages.length === 0" class="welcome-section">
          <div class="welcome-icon">
            <span></span>
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M7 12a5 5 0 0110 0v2a3 3 0 01-3 3h-4a3 3 0 01-3-3v-2z" stroke="currentColor" stroke-width="1.8"/>
              <path d="M9.5 11h.01M14.5 11h.01M10.5 14c.78.58 2.22.58 3 0M12 5V3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
            </svg>
          </div>
          <span class="welcome-kicker">AI TRIAGE</span>
          <h3>您好，我是您的智能问诊助手</h3>
          <p>可以描述症状、持续时间和既往病史，我会帮您整理就医建议。</p>
          <div class="quick-questions">
            <button
              v-for="(question, index) in quickQuestions"
              :key="index"
              class="quick-question"
              type="button"
              @click="sendQuickQuestion(question)"
            >
              <span>{{ question }}</span>
              <svg viewBox="0 0 24 24" fill="none">
                <path d="M9 18l6-6-6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
          </div>
        </div>

        <div v-else class="message-list">
          <div v-for="(message, index) in messages" :key="index" :class="['message-item', message.role]">
            <div v-if="message.role === 'assistant'" class="avatar assistant-avatar">
              <img :src="assistantAvatar" alt="智能助手" />
            </div>
            <div class="message-content">
              <div class="message-bubble">
                <div v-if="message.role === 'assistant'" class="formatted-content" v-html="formatMessageContent(message.content)"></div>
                <div v-else>{{ message.content }}</div>
              </div>
              <div v-if="message.actions?.length" class="message-actions">
                <button
                  v-for="(action, actionIndex) in message.actions"
                  :key="`${index}-${actionIndex}`"
                  :class="['message-action-btn', `action-${action.type}`, { primary: action.primary }]"
                  type="button"
                  :disabled="loading"
                  @click="handleAssistantAction(action)"
                >
                  {{ action.label }}
                </button>
              </div>
              <div v-if="message.isThinking" class="thinking-indicator">
                <span class="thinking-dot"></span>
                <span class="thinking-dot"></span>
                <span class="thinking-dot"></span>
                <span class="thinking-step-text">{{ thinkingStepText }}</span>
              </div>
            </div>
            <div v-if="message.role === 'user'" class="avatar user-avatar">
              <svg viewBox="0 0 24 24" fill="none">
                <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2M12 11a4 4 0 100-8 4 4 0 000 8z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
          </div>
        </div>
      </section>
    </div>

    <div class="chat-input-area">
      <div v-if="isRecording" class="voice-status">
        <span class="voice-bars">
          <i></i><i></i><i></i><i></i>
        </span>
        正在听，请说出您的症状
      </div>
      <div class="input-wrapper">
        <button
          :class="['voice-btn', { recording: isRecording }]"
          type="button"
          aria-label="语音输入"
          @click="toggleVoiceInput"
        >
          <svg viewBox="0 0 24 24" fill="none">
            <path d="M12 14a3 3 0 003-3V6a3 3 0 10-6 0v5a3 3 0 003 3z" stroke="currentColor" stroke-width="2"/>
            <path d="M19 11a7 7 0 01-14 0M12 18v3M8 21h8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </button>
        <textarea
          ref="textareaRef"
          v-model="inputMessage"
          placeholder="描述您的症状或问题..."
          rows="1"
          @input="adjustTextareaHeight"
          @keydown="handleKeydown"
        ></textarea>
        <button class="send-btn" type="button" @click="sendMessage" :disabled="!inputMessage.trim() || loading">
          <svg v-if="!loading" viewBox="0 0 24 24" fill="none">
            <path d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <svg v-else class="loading-icon" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2" opacity="0.2"/>
            <path d="M21 12a9 9 0 00-9-9" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { consultAi, getAiChatHistory, getSessionList } from '@/api/chat'
import { createRegister, getDeptList, getOpenDoctorSlots, getOpenScheduleSlots } from '@/api/register'
import { getPatientByUserId } from '@/api/patient'
import { getInfo } from '@/api/user'
import assistantAvatar from '@/assets/picture.png'

const router = useRouter()
const chatContentRef = ref(null)
const textareaRef = ref(null)
const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const thinkingStepIndex = ref(0)
let thinkingTimer = null
const thinkingSteps = [
  '正在理解您的症状描述...',
  '正在查阅相关医学知识库...',
  '正在结合临床经验分析...',
  '正在整合回答...'
]
const thinkingStepText = ref(thinkingSteps[0])
const currentSessionId = ref('')
const sessionList = ref([])
const sidebarCollapsed = ref(true)
const isRecording = ref(false)
const recognition = ref(null)
const voiceSupported = ref(false)
let voiceBaseText = ''

const registrationFlow = ref({
  active: false,
  step: 'idle',
  departments: [],
  recommendations: [],
  dept: null,
  schedules: [],
  schedule: null,
  slots: [],
  slot: null,
  createdRegister: null
})

const quickQuestions = [
  '最近有点头晕，该怎么办？',
  '我想让助手帮我挂号',
  '我之前的病历里有什么需要注意的？',
  '最近睡眠不好，有什么建议？',
  '日常饮食需要注意什么？'
]

const goBack = () => {
  stopVoiceInput()
  router.back()
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatContentRef.value) {
      chatContentRef.value.scrollTop = chatContentRef.value.scrollHeight
    }
  })
}

const adjustTextareaHeight = () => {
  nextTick(() => {
    if (!textareaRef.value) return
    textareaRef.value.style.height = 'auto'
    textareaRef.value.style.height = `${Math.min(textareaRef.value.scrollHeight, 118)}px`
  })
}

const loadSessionList = async () => {
  try {
    const patientId = getStoredPatientId()
    const res = await getSessionList({ patientId })
    if (res.data) {
      sessionList.value = res.data
      if (sessionList.value.length > 0 && !currentSessionId.value) {
        switchSession(sessionList.value[0])
      }
    }
  } catch (error) {
    console.log('加载会话列表失败', error)
  }
}

const loadChatHistory = async (sessionIdToLoad) => {
  try {
    if (!sessionIdToLoad) {
      return
    }
    const patientId = getStoredPatientId()
    const res = await getAiChatHistory({ sessionId: sessionIdToLoad, patientId })
    if (res.data) {
      messages.value = res.data
        .filter(item => item.role === 'user' || item.role === 'assistant')
        .map(item => ({
          role: item.role,
          content: item.content
        }))
      scrollToBottom()
    }
  } catch (error) {
    console.log('加载问诊记忆失败', error)
  }
}

const createNewSession = () => {
  stopVoiceInput()
  const newSessionId = `session_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  currentSessionId.value = newSessionId
  messages.value = []
  localStorage.setItem('aiChatSessionId', newSessionId)
  sidebarCollapsed.value = true
  console.log('创建新会话:', newSessionId)
}

const switchSession = async (sessionIdToSwitch) => {
  if (!sessionIdToSwitch) return

  stopVoiceInput()
  console.log('切换到会话:', sessionIdToSwitch)
  currentSessionId.value = sessionIdToSwitch
  localStorage.setItem('aiChatSessionId', sessionIdToSwitch)
  sidebarCollapsed.value = true
  await loadChatHistory(sessionIdToSwitch)
}

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

const getStoredPatientId = () => {
  const value = localStorage.getItem('patientId')
  console.log('从 localStorage 获取 patientId:', value)
  return value ? Number(value) : null
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

const formatScheduleDate = (dateStr) => {
  const date = parseBusinessDate(dateStr)
  if (!date) return ''
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return `${date.getMonth() + 1}月${date.getDate()}日 ${weekDays[date.getDay()]}`
}

const formatMoney = (amount) => {
  const value = Number(amount || 0)
  return `￥${value.toFixed(2)}`
}

const getTimeSlotOrder = (timeSlot) => {
  if (!timeSlot) return 9
  const orderMap = { morning: 1, afternoon: 2, evening: 3 }
  return orderMap[timeSlot] || 9
}

const getScheduleReservedNumber = (schedule) => {
  const reserved = schedule?.reservedNumber ?? schedule?.reserved_number
  if (reserved != null) return Number(reserved)
  const max = getScheduleMaxNumber(schedule)
  const available = schedule?.availableNumber ?? schedule?.available_number
  if (max != null && available != null) {
    return Math.max(Number(max) - Number(available), 0)
  }
  return 0
}

const getScheduleMaxNumber = (schedule) => {
  const max = schedule?.maxNumber ?? schedule?.max_number
  if (max != null) return Number(max)
  const reserved = schedule?.reservedNumber ?? schedule?.reserved_number
  const available = schedule?.availableNumber ?? schedule?.available_number
  if (reserved != null && available != null) {
    return Math.max(Number(reserved) + Number(available), 0)
  }
  return 0
}

const getScheduleAvailableNumber = (schedule) => {
  const max = getScheduleMaxNumber(schedule)
  const reserved = getScheduleReservedNumber(schedule)
  if (max > 0) return Math.max(max - reserved, 0)
  return Number(schedule?.availableNumber ?? schedule?.available_number ?? 0)
}

const isSchedulePast = (schedule) => {
  if (!schedule?.scheduleDate) return true
  const target = parseBusinessDate(schedule.scheduleDate)
  if (!target) return true

  const today = new Date()
  const todayStart = new Date(today.getFullYear(), today.getMonth(), today.getDate())
  const targetStart = new Date(target.getFullYear(), target.getMonth(), target.getDate())

  if (targetStart < todayStart) return true
  if (targetStart > todayStart) return false

  const nowMinutes = today.getHours() * 60 + today.getMinutes()
  if (schedule?.startTime) {
    const [hour, minute] = String(schedule.startTime).split(':').map(Number)
    return nowMinutes >= hour * 60 + minute
  }

  return false
}

const isScheduleFull = (schedule) => {
  if (!schedule) return true
  const status = schedule.status
  if (status != null && status !== '0' && status !== 0) return true
  if (String(status).toLowerCase() === '已满' || String(status).toLowerCase() === 'full') return true
  return getScheduleMaxNumber(schedule) > 0 && getScheduleReservedNumber(schedule) >= getScheduleMaxNumber(schedule)
}

const isScheduleSelectable = (schedule) => !isSchedulePast(schedule) && !isScheduleFull(schedule)

const normalizeSchedule = (schedule) => ({
  ...schedule,
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
  maxNumber: getScheduleMaxNumber(schedule),
  reservedNumber: getScheduleReservedNumber(schedule),
  availableNumber: getScheduleAvailableNumber(schedule),
  status: schedule.status
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

const appendAssistantMessage = (content, actions = []) => {
  messages.value.push({
    role: 'assistant',
    content,
    actions
  })
  scrollToBottom()
}

const resetRegistrationFlow = () => {
  registrationFlow.value = {
    active: false,
    step: 'idle',
    departments: [],
    recommendations: [],
    dept: null,
    schedules: [],
    schedule: null,
    slots: [],
    slot: null,
    createdRegister: null
  }
}

const wantsRegistration = (message) => {
  return /(挂号|预约挂号|预约医生|约号|帮.*预约|帮.*挂|想.*看医生|我要看医生|安排.*门诊)/.test(message)
}

const normalizeText = (text) => String(text || '').replace(/\s+/g, '')

const symptomDeptRules = [
  {
    keywords: ['头痛', '头疼', '头晕', '眩晕', '偏头痛', '失眠', '手麻', '脚麻'],
    deptNames: ['神经内科', '脑病科', '内科门诊', '内科'],
    reason: '头痛、头晕、麻木等更适合先由神经内科或内科门诊评估。'
  },
  {
    keywords: ['发烧', '发热', '咳嗽', '咽痛', '流鼻涕', '感冒', '胸闷', '气短'],
    deptNames: ['呼吸内科', '发热门诊', '内科门诊', '内科'],
    reason: '发热、咳嗽、胸闷等常见于呼吸系统或内科问题，适合先挂呼吸内科或内科门诊。'
  },
  {
    keywords: ['胃痛', '肚子疼', '腹痛', '腹泻', '拉肚子', '恶心', '呕吐', '反酸', '胃胀'],
    deptNames: ['消化内科', '内科门诊', '内科'],
    reason: '腹痛、腹泻、反酸等更适合先由消化内科或内科门诊判断。'
  },
  {
    keywords: ['心慌', '心悸', '胸痛', '胸口痛', '血压高', '高血压'],
    deptNames: ['心血管内科', '心内科', '内科门诊', '内科'],
    reason: '心慌、胸痛、血压异常建议优先选择心血管内科或内科门诊。'
  },
  {
    keywords: ['牙疼', '牙痛', '牙龈', '口腔', '拔牙', '龋齿'],
    deptNames: ['口腔科'],
    reason: '牙痛、牙龈和口腔问题建议直接挂口腔科。'
  },
  {
    keywords: ['眼睛', '眼痛', '视力', '看不清', '红眼', '流泪'],
    deptNames: ['眼科'],
    reason: '眼痛、视力变化、红眼等建议挂眼科。'
  },
  {
    keywords: ['耳朵', '耳鸣', '鼻炎', '鼻塞', '喉咙', '咽喉', '扁桃体'],
    deptNames: ['耳鼻喉科', '五官科'],
    reason: '耳、鼻、咽喉相关不适建议挂耳鼻喉科。'
  },
  {
    keywords: ['皮疹', '过敏', '瘙痒', '痒', '湿疹', '荨麻疹', '长痘'],
    deptNames: ['皮肤科'],
    reason: '皮疹、瘙痒、过敏等皮肤表现建议挂皮肤科。'
  },
  {
    keywords: ['骨折', '扭伤', '腰痛', '腰疼', '关节', '膝盖', '颈椎', '肩膀', '腿疼'],
    deptNames: ['骨科'],
    reason: '骨折、扭伤、关节或颈肩腰腿痛建议挂骨科。'
  },
  {
    keywords: ['怀孕', '孕妇', '产检', '月经', '妇科', '白带', '痛经'],
    deptNames: ['妇产科', '妇科', '产科'],
    reason: '孕产、月经、妇科相关问题建议挂妇产科。'
  },
  {
    keywords: ['小孩', '孩子', '儿童', '宝宝', '婴儿'],
    deptNames: ['儿科'],
    reason: '儿童和婴幼儿不适建议优先挂儿科。'
  }
]

const matchDepartmentByName = (departments, names) => {
  const normalizedNames = names.map(normalizeText).filter(Boolean)
  return departments.find(dept => {
    const deptName = normalizeText(dept.deptName)
    return normalizedNames.some(name => deptName.includes(name) || name.includes(deptName))
  }) || null
}

const inferDepartmentsFromSymptoms = (message, departments) => {
  const cleanText = normalizeText(message)
  if (!cleanText) return []

  const matches = []
  symptomDeptRules.forEach(rule => {
    if (!rule.keywords.some(keyword => cleanText.includes(keyword))) return
    const dept = matchDepartmentByName(departments, rule.deptNames)
    if (!dept) return
    if (matches.some(item => item.dept.deptId === dept.deptId)) return
    matches.push({
      dept,
      reason: rule.reason
    })
  })

  return matches
}

const getDepartmentActions = (departments) => {
  const visibleDepartments = departments.slice(0, 10)
  return [
    ...visibleDepartments.map(dept => ({
      label: dept.deptName,
      type: 'select-dept',
      payload: dept
    })),
    { label: '取消', type: 'cancel-register' }
  ]
}

const getRecommendationActions = (recommendations, departments) => {
  const recommendedDeptIds = new Set(recommendations.map(item => item.dept.deptId))
  const otherDepartments = departments.filter(dept => !recommendedDeptIds.has(dept.deptId)).slice(0, 6)
  return [
    ...recommendations.map((item, index) => ({
      label: `挂${item.dept.deptName}`,
      type: 'select-dept',
      payload: item.dept,
      primary: index === 0
    })),
    ...otherDepartments.map(dept => ({
      label: dept.deptName,
      type: 'select-dept',
      payload: dept
    })),
    { label: '我再描述一下', type: 'describe-symptom' },
    { label: '取消', type: 'cancel-register' }
  ]
}

const getScheduleActionLabel = (schedule) => {
  const dateText = formatScheduleDate(schedule.scheduleDate)
  const timeText = schedule.startTime && schedule.endTime ? ` ${schedule.startTime}-${schedule.endTime}` : ''
  const levelText = schedule.levelName ? ` ${schedule.levelName}` : ''
  return `${schedule.doctorName || '医生'} ${dateText}${timeText}${levelText} 余号${getScheduleAvailableNumber(schedule)}`
}

const getScheduleActions = (schedules) => {
  return [
    ...schedules.slice(0, 8).map(schedule => ({
      label: getScheduleActionLabel(schedule),
      type: 'select-schedule',
      payload: schedule
    })),
    { label: '重选科室', type: 'restart-register' },
    { label: '取消挂号', type: 'cancel-register' }
  ]
}

const getSlotActionLabel = (slot) => {
  const timeText = slot.startTime && slot.endTime ? `${slot.startTime}-${slot.endTime}` : '可预约时间'
  return `${timeText} ${slot.levelName || '普通号'} ${formatMoney(slot.fee)} 余号${getScheduleAvailableNumber(slot)}`
}

const getSlotActions = (slots) => {
  return [
    ...slots.slice(0, 8).map(slot => ({
      label: getSlotActionLabel(slot),
      type: 'select-slot',
      payload: slot
    })),
    { label: '重选医生', type: 'back-schedule' },
    { label: '取消挂号', type: 'cancel-register' }
  ]
}

const getLeafDepartments = (departments) => {
  const parentIds = new Set(departments.map(dept => dept.parentId).filter(id => id != null))
  const leafDepartments = departments.filter(dept => !parentIds.has(dept.deptId))
  return leafDepartments.length ? leafDepartments : departments
}

const findDepartmentFromText = (text, departments) => {
  const cleanText = normalizeText(text)
  if (!cleanText) return null
  return departments.find(dept => {
    const deptName = normalizeText(dept.deptName)
    return deptName && (cleanText.includes(deptName) || deptName.includes(cleanText))
  }) || null
}

const findScheduleFromText = (text, schedules) => {
  const cleanText = normalizeText(text)
  if (!cleanText) return null
  return schedules.find(schedule => {
    const doctorName = normalizeText(schedule.doctorName)
    return doctorName && cleanText.includes(doctorName)
  }) || null
}

const findSlotFromText = (text, slots) => {
  const cleanText = normalizeText(text)
  if (!cleanText) return null
  return slots.find(slot => {
    const start = String(slot.startTime || '').slice(0, 5)
    const end = String(slot.endTime || '').slice(0, 5)
    return (start && cleanText.includes(start)) || (end && cleanText.includes(end))
  }) || null
}

const loadRegistrationDepartments = async () => {
  const fallbackDepartments = [
    { deptId: 201, parentId: 200, deptName: '内科' },
    { deptId: 202, parentId: 200, deptName: '外科' },
    { deptId: 203, parentId: 200, deptName: '儿科' },
    { deptId: 204, parentId: 200, deptName: '妇产科' },
    { deptId: 205, parentId: 200, deptName: '骨科' },
    { deptId: 206, parentId: 200, deptName: '眼科' }
  ]

  try {
    const res = await getDeptList()
    const apiDepartments = Array.isArray(res.data) ? res.data : []
    const departments = apiDepartments.length ? apiDepartments : fallbackDepartments
    return getLeafDepartments(departments.map(normalizeDept).filter(dept => !isHospitalRootDept(dept)))
  } catch (error) {
    console.error('助手加载科室失败', error)
    return fallbackDepartments
  }
}

const loadRegistrationSchedules = async (deptId) => {
  const dateRange = getDateRange()
  const res = await getOpenDoctorSlots({
    pageNum: 1,
    pageSize: 200,
    deptId,
    beginDate: dateRange.beginDate,
    endDate: dateRange.endDate
  })

  return (res.rows || [])
    .map(normalizeSchedule)
    .filter(isScheduleSelectable)
    .sort((a, b) => {
      const dateDiff = parseBusinessDate(a.scheduleDate) - parseBusinessDate(b.scheduleDate)
      if (dateDiff !== 0) return dateDiff
      const slotDiff = getTimeSlotOrder(a.timeSlot) - getTimeSlotOrder(b.timeSlot)
      if (slotDiff !== 0) return slotDiff
      return Number(b.levelId || 0) - Number(a.levelId || 0)
    })
}

const loadRegistrationSlots = async (schedule) => {
  const res = await getOpenScheduleSlots({
    pageNum: 1,
    pageSize: 100,
    doctorId: schedule.doctorId,
    scheduleDate: schedule.scheduleDate
  })

  return (res.rows || [])
    .map(normalizeSchedule)
    .filter(isScheduleSelectable)
    .sort((a, b) => String(a.startTime || '').localeCompare(String(b.startTime || '')))
}

const ensurePatientId = async () => {
  const savedPatientId = getStoredPatientId()
  if (savedPatientId) return savedPatientId

  const userRes = await getInfo()
  const userId = userRes?.user?.userId
  if (!userId) return null

  const patientRes = await getPatientByUserId(userId)
  const patient = patientRes?.data
  if (!patient?.patientId) return null

  localStorage.setItem('patientId', patient.patientId)
  if (patient.name) {
    localStorage.setItem('patientName', patient.name)
  }
  return Number(patient.patientId)
}

const askDepartment = (intro = '可以，我来帮您挂号。请先选择就诊科室：') => {
  registrationFlow.value.step = 'chooseDept'
  appendAssistantMessage(intro, getDepartmentActions(registrationFlow.value.departments))
}

const askRecommendedDepartment = (message) => {
  const recommendations = inferDepartmentsFromSymptoms(message, registrationFlow.value.departments)
  registrationFlow.value.recommendations = recommendations

  if (!recommendations.length) {
    askDepartment('我还不能根据这些描述准确推荐科室。请再补充主要症状，或直接选择一个科室：')
    return false
  }

  const primary = recommendations[0]
  const extra = recommendations.slice(1).map(item => item.dept.deptName).join('、')
  const content = [
    `根据您的描述，我建议优先挂 ${primary.dept.deptName}。`,
    `判断依据：${primary.reason}`,
    extra ? `也可以备选：${extra}。` : '',
    '如果症状突然加重、剧烈胸痛、意识不清或肢体无力，请优先急诊。'
  ].filter(Boolean).join('\n')

  registrationFlow.value.step = 'chooseDept'
  appendAssistantMessage(content, getRecommendationActions(recommendations, registrationFlow.value.departments))
  return true
}

const selectRegistrationDepartment = async (dept) => {
  registrationFlow.value.dept = dept
  registrationFlow.value.step = 'chooseSchedule'
  registrationFlow.value.schedules = await loadRegistrationSchedules(dept.deptId)

  if (!registrationFlow.value.schedules.length) {
    appendAssistantMessage(
      `${dept.deptName} 未来一周暂时没有可预约号源，您可以换一个科室看看。`,
      [
        { label: '重选科室', type: 'restart-register', primary: true },
        { label: '取消挂号', type: 'cancel-register' }
      ]
    )
    return
  }

  appendAssistantMessage(
    `已为您筛选 ${dept.deptName} 可预约号源。请选择医生和日期：`,
    getScheduleActions(registrationFlow.value.schedules)
  )
}

const selectRegistrationSchedule = async (schedule) => {
  registrationFlow.value.schedule = schedule
  registrationFlow.value.step = 'chooseSlot'
  registrationFlow.value.slots = await loadRegistrationSlots(schedule)

  if (!registrationFlow.value.slots.length) {
    appendAssistantMessage(
      `${schedule.doctorName || '该医生'} ${formatScheduleDate(schedule.scheduleDate)} 暂无可预约时间片，请换一个医生或日期。`,
      getScheduleActions(registrationFlow.value.schedules.slice(0, 6))
    )
    return
  }

  appendAssistantMessage(
    `已选择 ${schedule.doctorName || '医生'} ${formatScheduleDate(schedule.scheduleDate)}。请选择就诊时间：`,
    getSlotActions(registrationFlow.value.slots)
  )
}

const selectRegistrationSlot = (slot) => {
  registrationFlow.value.slot = slot
  registrationFlow.value.step = 'confirm'
  const dept = registrationFlow.value.dept
  const schedule = registrationFlow.value.schedule
  const content = [
    '请确认本次预约信息：',
    `科室：${dept?.deptName || '-'}`,
    `医生：${schedule?.doctorName || '-'}`,
    `时间：${formatScheduleDate(slot.scheduleDate || schedule?.scheduleDate)} ${slot.startTime || ''}-${slot.endTime || ''}`,
    `号别：${slot.levelName || schedule?.levelName || '普通号'}`,
    `挂号费：${formatMoney(slot.fee ?? schedule?.fee)}`
  ].join('\n')

  appendAssistantMessage(content, [
    { label: '确认挂号', type: 'confirm-register', primary: true },
    { label: '重选时间', type: 'back-slot' },
    { label: '取消挂号', type: 'cancel-register' }
  ])
}

const confirmRegistration = async () => {
  const patientId = await ensurePatientId()
  if (!patientId) {
    appendAssistantMessage('还没有获取到您的患者档案，请先完善患者信息后再挂号。', [
      { label: '去完善信息', type: 'route', path: '/patient-complete', primary: true },
      { label: '取消挂号', type: 'cancel-register' }
    ])
    return
  }

  const dept = registrationFlow.value.dept
  const schedule = registrationFlow.value.schedule
  const slot = registrationFlow.value.slot
  const registerData = {
    patientId,
    doctorId: schedule.doctorId,
    deptId: dept.deptId,
    scheduleId: slot.scheduleId,
    slotId: slot.slotId,
    registerTime: toDateKey(slot.scheduleDate || schedule.scheduleDate),
    registerType: 'online'
  }

  const result = await createRegister(registerData)
  registrationFlow.value.createdRegister = result?.data || null
  registrationFlow.value.active = false
  registrationFlow.value.step = 'success'

  const registerInfo = registrationFlow.value.createdRegister || {}
  const content = [
    '预约成功！',
    `科室：${dept.deptName}`,
    `医生：${schedule.doctorName || '-'}`,
    `时间：${formatScheduleDate(slot.scheduleDate || schedule.scheduleDate)} ${slot.startTime || ''}-${slot.endTime || ''}`,
    registerInfo.registerFee != null ? `挂号费：${formatMoney(registerInfo.registerFee)}` : '请在30分钟内完成挂号费支付。'
  ].join('\n')

  appendAssistantMessage(content, [
    { label: '查看我的预约', type: 'route', path: '/my-appointments', primary: true },
    { label: '继续问诊', type: 'finish-register' }
  ])
}

const startRegistrationFlow = async (message) => {
  resetRegistrationFlow()
  registrationFlow.value.active = true
  registrationFlow.value.step = 'loadingDept'
  registrationFlow.value.departments = await loadRegistrationDepartments()

  if (!registrationFlow.value.departments.length) {
    resetRegistrationFlow()
    appendAssistantMessage('暂时没有获取到可预约科室，请稍后再试，或前往挂号页手动选择。', [
      { label: '前往挂号页', type: 'route', path: '/register', primary: true }
    ])
    return
  }

  const matchedDept = findDepartmentFromText(message, registrationFlow.value.departments)
  if (matchedDept) {
    await selectRegistrationDepartment(matchedDept)
    return
  }

  if (askRecommendedDepartment(message)) {
    return
  }

  askDepartment()
}

const handleRegistrationText = async (message) => {
  if (/取消|算了|退出|停止/.test(message)) {
    resetRegistrationFlow()
    appendAssistantMessage('好的，已取消本次挂号引导。您还可以继续描述症状或咨询其他问题。')
    return
  }

  if (!registrationFlow.value.active) {
    await startRegistrationFlow(message)
    return
  }

  if (registrationFlow.value.step === 'chooseDept') {
    const dept = findDepartmentFromText(message, registrationFlow.value.departments)
    if (dept) {
      await selectRegistrationDepartment(dept)
      return
    }
    if (askRecommendedDepartment(message)) {
      return
    }
    askDepartment('我还没有匹配到具体科室。您可以补充主要症状，或从下面选择一个科室：')
    return
  }

  if (registrationFlow.value.step === 'chooseSchedule') {
    if (/重选|换科室/.test(message)) {
      askDepartment('好的，请重新选择就诊科室：')
      return
    }
    const schedule = findScheduleFromText(message, registrationFlow.value.schedules)
    if (schedule) {
      await selectRegistrationSchedule(schedule)
      return
    }
    appendAssistantMessage('请选择一个可预约的医生和日期：', getScheduleActions(registrationFlow.value.schedules))
    return
  }

  if (registrationFlow.value.step === 'chooseSlot') {
    if (/重选|换医生|换日期/.test(message)) {
      appendAssistantMessage('好的，请重新选择医生和日期：', getScheduleActions(registrationFlow.value.schedules))
      registrationFlow.value.step = 'chooseSchedule'
      return
    }
    const slot = findSlotFromText(message, registrationFlow.value.slots)
    if (slot) {
      selectRegistrationSlot(slot)
      return
    }
    appendAssistantMessage('请选择一个具体就诊时间：', getSlotActions(registrationFlow.value.slots))
    return
  }

  if (registrationFlow.value.step === 'confirm') {
    if (/确认|提交|挂号|预约|可以|好的|是/.test(message)) {
      await confirmRegistration()
      return
    }
    if (/重选|换时间/.test(message)) {
      appendAssistantMessage('好的，请重新选择就诊时间：', getSlotActions(registrationFlow.value.slots))
      registrationFlow.value.step = 'chooseSlot'
      return
    }
    appendAssistantMessage('请确认是否为您提交预约：', [
      { label: '确认挂号', type: 'confirm-register', primary: true },
      { label: '重选时间', type: 'back-slot' },
      { label: '取消挂号', type: 'cancel-register' }
    ])
  }
}

const runRegistrationAction = async (action) => {
  if (loading.value) return

  loading.value = true
  try {
    if (action.type === 'route') {
      router.push(action.path)
      return
    }

    if (action.type === 'finish-register') {
      appendAssistantMessage('好的，您可以继续描述症状或咨询其他问题。')
      return
    }

    if (action.type === 'cancel-register') {
      resetRegistrationFlow()
      appendAssistantMessage('已取消本次挂号引导。')
      return
    }

    if (action.type === 'describe-symptom') {
      registrationFlow.value.active = true
      registrationFlow.value.step = 'chooseDept'
      appendAssistantMessage('请告诉我最明显的症状，比如“头疼两天”“咳嗽发烧”“牙疼”，我会先帮您推荐科室。')
      return
    }

    if (action.type === 'restart-register') {
      registrationFlow.value.active = true
      askDepartment('请重新选择就诊科室：')
      return
    }

    if (action.type === 'back-schedule') {
      registrationFlow.value.step = 'chooseSchedule'
      appendAssistantMessage('请重新选择医生和日期：', getScheduleActions(registrationFlow.value.schedules))
      return
    }

    if (action.type === 'back-slot') {
      registrationFlow.value.step = 'chooseSlot'
      appendAssistantMessage('请重新选择就诊时间：', getSlotActions(registrationFlow.value.slots))
      return
    }

    if (action.type === 'select-dept') {
      await selectRegistrationDepartment(action.payload)
      return
    }

    if (action.type === 'select-schedule') {
      await selectRegistrationSchedule(action.payload)
      return
    }

    if (action.type === 'select-slot') {
      selectRegistrationSlot(action.payload)
      return
    }

    if (action.type === 'confirm-register') {
      await confirmRegistration()
    }
  } catch (error) {
    console.error('助手挂号流程失败:', error)
    appendAssistantMessage(error?.message || '挂号处理失败，请稍后再试。')
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

const handleAssistantAction = (action) => {
  if (!action || loading.value) return
  messages.value.push({
    role: 'user',
    content: action.label
  })
  scrollToBottom()
  runRegistrationAction(action)
}

const askBackendAi = async (message) => {
  const patientId = getStoredPatientId()
  console.log('AI patientId:', patientId)
  console.log('正在调用 AI 咨询接口...', { message, sessionId: currentSessionId.value, patientId })
  const res = await consultAi({
    message,
    sessionId: currentSessionId.value,
    patientId
  })
  console.log('AI 咨询接口返回:', res)
  if (res.data?.sessionId) {
    currentSessionId.value = res.data.sessionId
    localStorage.setItem('aiChatSessionId', res.data.sessionId)
    await loadSessionList()
  }
  return res.data?.reply || ''
}

const sendMessage = async () => {
  const message = inputMessage.value.trim()
  if (!message || loading.value) return

  stopVoiceInput()
  loading.value = true

  if (!currentSessionId.value) {
    createNewSession()
  }

  messages.value.push({
    role: 'user',
    content: message
  })

  inputMessage.value = ''
  adjustTextareaHeight()
  scrollToBottom()

  const thinkingMessage = {
    role: 'assistant',
    content: '',
    isThinking: true
  }
  messages.value.push(thinkingMessage)
  scrollToBottom()

  thinkingStepIndex.value = 0
  thinkingStepText.value = thinkingSteps[0]
  thinkingTimer = setInterval(() => {
    thinkingStepIndex.value = (thinkingStepIndex.value + 1) % thinkingSteps.length
    thinkingStepText.value = thinkingSteps[thinkingStepIndex.value]
  }, 2000)

  try {
    if (registrationFlow.value.active || wantsRegistration(message)) {
      await handleRegistrationText(message)
      return
    }

    const thinkingMessage = {
      role: 'assistant',
      content: '',
      isThinking: true
    }
    messages.value.push(thinkingMessage)
    scrollToBottom()

    const response = await askBackendAi(message)

    clearInterval(thinkingTimer)
    thinkingTimer = null
    messages.value = messages.value.filter(m => !m.isThinking)
    messages.value.push({
      role: 'assistant',
      content: response || '我已经收到您的问题，但暂时没有生成有效回复，请稍后再试。'
    })
  } catch (error) {
    console.error('AI 咨询失败:', error)
    clearInterval(thinkingTimer)
    thinkingTimer = null
    messages.value = messages.value.filter(m => !m.isThinking)

    let errorMsg = '抱歉，我暂时无法为您提供服务，请稍后再试。如果您有紧急情况，请及时线下就医。'

    if (error.message) {
      errorMsg += ` (${error.message})`
    }
    if (error.response) {
      console.error('错误响应:', error.response)
      if (error.response.data && error.response.data.msg) {
        errorMsg += ` [${error.response.data.msg}]`
      }
    }

    messages.value.push({
      role: 'assistant',
      content: errorMsg
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

const sendQuickQuestion = (question) => {
  inputMessage.value = question
  adjustTextareaHeight()
  sendMessage()
}

const handleKeydown = (e) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

const escapeHtml = (content) => {
  return content
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

const formatMessageContent = (content) => {
  if (!content) return ''
  
  let formatted = content
  // 清理 markdown 格式符号（###、---、**、*、`、代码块等）
  formatted = formatted.replace(/###\s*/g, '')
  formatted = formatted.replace(/##\s*/g, '')
  formatted = formatted.replace(/---+/g, '')
  formatted = formatted.replace(/\*\*(.+?)\*\*/g, '$1')
  formatted = formatted.replace(/\*(.+?)\*/g, '$1')
  formatted = formatted.replace(/`{3}[\s\S]*?`{3}/g, '')
  formatted = formatted.replace(/`(.+?)`/g, '$1')
  // 换行处理
  formatted = formatted.replace(/\n\n/g, '<br><br>')
  formatted = formatted.replace(/\n/g, '<br>')
  
  return formatted
}

const initSpeechRecognition = () => {
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
  voiceSupported.value = Boolean(SpeechRecognition)

  if (!SpeechRecognition) {
    return
  }

  const recorder = new SpeechRecognition()
  recorder.lang = 'zh-CN'
  recorder.continuous = false
  recorder.interimResults = true

  recorder.onresult = (event) => {
    let finalText = ''
    let interimText = ''

    for (let i = event.resultIndex; i < event.results.length; i += 1) {
      const transcript = event.results[i][0].transcript
      if (event.results[i].isFinal) {
        finalText += transcript
      } else {
        interimText += transcript
      }
    }

    const nextText = `${voiceBaseText}${finalText || interimText}`.trimStart()
    inputMessage.value = nextText
    adjustTextareaHeight()

    if (finalText) {
      voiceBaseText = `${voiceBaseText}${finalText}`
    }
  }

  recorder.onerror = (event) => {
    isRecording.value = false
    const message = event.error === 'not-allowed'
      ? '需要允许麦克风权限后才能语音输入'
      : '语音识别暂时不可用，请改用文字输入'
    showToast(message)
  }

  recorder.onend = () => {
    isRecording.value = false
    voiceBaseText = inputMessage.value ? `${inputMessage.value} ` : ''
  }

  recognition.value = recorder
}

const startVoiceInput = () => {
  if (!voiceSupported.value || !recognition.value) {
    showToast('当前浏览器不支持语音输入')
    return
  }

  if (loading.value) return

  try {
    voiceBaseText = inputMessage.value ? `${inputMessage.value} ` : ''
    recognition.value.start()
    isRecording.value = true
  } catch (error) {
    console.log('语音识别启动失败', error)
  }
}

const stopVoiceInput = () => {
  if (!isRecording.value || !recognition.value) return
  recognition.value.stop()
  isRecording.value = false
}

const toggleVoiceInput = () => {
  if (isRecording.value) {
    stopVoiceInput()
  } else {
    startVoiceInput()
  }
}

onMounted(() => {
  initSpeechRecognition()

  const savedSessionId = localStorage.getItem('aiChatSessionId')
  if (savedSessionId) {
    currentSessionId.value = savedSessionId
  }

  loadSessionList()

  if (currentSessionId.value) {
    loadChatHistory(currentSessionId.value)
  }
})

onBeforeUnmount(() => {
  stopVoiceInput()
  clearInterval(thinkingTimer)
})
</script>

<style scoped lang="scss">
$blue-dark: #2D62C8;
$blue-bright: #4A90E2;
$sky-blue: #A0C8F0;
$ink: #2C3E50;
$warm: #e88050;

.chat-page {
  position: relative;
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background:
    linear-gradient(155deg, rgba(238, 243, 249, .94) 0%, rgba(215, 226, 242, .74) 42%, rgba(225, 235, 246, .9) 100%),
    var(--bg-gradient);
  color: var(--text-primary);
}

.ambient-grid {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image:
    linear-gradient(rgba(45, 98, 200, .08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(74, 144, 226, .08) 1px, transparent 1px);
  background-size: 28px 28px;
  mask-image: linear-gradient(to bottom, rgba(0,0,0,.55), rgba(0,0,0,.08));
  animation: gridDrift 16s linear infinite;
}

button {
  border: 0;
  font-family: inherit;
  -webkit-tap-highlight-color: transparent;
}

.icon-btn {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, .62);
  border: 1px solid rgba(74, 144, 226, .16);
  color: $ink;
  box-shadow: 0 10px 26px rgba(74, 144, 226, .12);

  svg {
    width: 21px;
    height: 21px;
  }

  &:active {
    transform: scale(.96);
  }
}

.chat-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  display: grid;
  grid-template-columns: 32px 1fr auto;
  align-items: center;
  gap: 10px;
  padding: calc(8px + env(safe-area-inset-top)) 14px 8px;
  background: #fff;
  border-bottom: 1px solid rgba(0, 0, 0, .1);
  box-shadow: 0 6px 18px rgba(0, 0, 0, .06);
  backdrop-filter: blur(18px);
}

.chat-header .icon-btn {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: rgba(0, 0, 0, .04);
  color: #374151;
  border-color: rgba(0, 0, 0, .08);
}

.chat-header .primary-action {
  background: #1250af;
  color: #fff;
  border-color: transparent;
}

.header-title {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.ai-avatar {
  position: relative;
  width: 44px;
  height: 44px;
  flex: 0 0 auto;
  border-radius: 14px;
  display: grid;
  place-items: center;
  color: $blue-dark;
  background: linear-gradient(135deg, rgba(238, 243, 249, .96), rgba(225, 235, 246, .98));
  border: 1px solid rgba(74, 144, 226, .24);
  box-shadow: inset 0 0 18px rgba(104, 199, 169, .12), 0 14px 24px rgba(45, 98, 200, .12);

  svg {
    position: relative;
    z-index: 2;
    width: 25px;
    height: 25px;
  }

  img {
    position: relative;
    z-index: 2;
    width: 34px;
    height: 34px;
    border-radius: 11px;
    object-fit: cover;
    display: block;
  }
}

.avatar-ring {
  position: absolute;
  inset: 5px;
  border: 1px solid rgba(93, 184, 216, .42);
  border-radius: 12px;
  animation: avatarPulse 2.4s ease-in-out infinite;
}

.title-copy {
  min-width: 0;

  h2 {
    margin: 0;
    color: #1e3a5f;
    font-size: 17px;
    font-weight: 800;
    line-height: 1.2;
  }
}

.ai-status {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  margin-top: 4px;
  color: rgba(30, 64, 175, .62);
  font-size: 12px;
  font-weight: 700;

  i {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: $blue-bright;
    box-shadow: 0 0 0 5px rgba(74, 144, 226, .13);
  }
}

.header-actions {
  display: flex;
  gap: 8px;
}

.primary-action {
  background: linear-gradient(135deg, rgba(45, 98, 200, .95), rgba(74, 144, 226, .95));
  color: #fff;
  border-color: rgba(255,255,255,.38);
}

.chat-main {
  position: relative;
  z-index: 2;
  flex: 1;
  min-height: 0;
  display: flex;
  overflow: hidden;
  padding-top: calc(60px + env(safe-area-inset-top));
}

.history-mask {
  position: fixed;
  inset: 0;
  z-index: 25;
  background: rgba(11, 37, 96, .18);
  backdrop-filter: blur(2px);
}

.session-sidebar {
  position: fixed;
  z-index: 30;
  top: calc(68px + env(safe-area-inset-top));
  bottom: calc(84px + env(safe-area-inset-bottom));
  left: 12px;
  width: min(320px, calc(100vw - 24px));
  display: flex;
  flex-direction: column;
  padding: 14px;
  border-radius: 18px;
  background: rgba(255, 255, 255, .86);
  border: 1px solid rgba(74, 144, 226, .18);
  box-shadow: 0 28px 70px rgba(45, 90, 78, .22);
  backdrop-filter: blur(20px);
  transform: translateX(0);
  transition: transform .28s cubic-bezier(.2,.76,.28,1), opacity .24s ease;

  &.collapsed {
    opacity: 0;
    pointer-events: none;
    transform: translateX(calc(-100% - 18px));
  }
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(213, 237, 243, .58);

  span {
    color: var(--text-light);
    font-size: 10px;
    font-weight: 900;
  }

  h3 {
    margin: 3px 0 0;
    color: $ink;
    font-size: 18px;
    line-height: 1.2;
    font-weight: 800;
  }
}

.new-session-card {
  width: 100%;
  min-height: 48px;
  margin: 14px 0 10px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 12px;
  color: $ink;
  font-size: 14px;
  font-weight: 800;
  text-align: left;
  background: linear-gradient(135deg, rgba(238, 243, 249, .95), rgba(224, 238, 248, .86));
  border: 1px solid rgba(74, 144, 226, .18);
}

.new-session-icon {
  width: 30px;
  height: 30px;
  border-radius: 9px;
  display: grid;
  place-items: center;
  color: #fff;
  background: linear-gradient(135deg, $blue-dark, $sky-blue);

  svg {
    width: 18px;
    height: 18px;
  }
}

.session-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding-right: 2px;
}

.session-item {
  width: 100%;
  min-height: 66px;
  margin-bottom: 9px;
  border-radius: 14px;
  display: grid;
  grid-template-columns: 40px 1fr 22px;
  align-items: center;
  gap: 10px;
  padding: 11px;
  text-align: left;
  color: var(--text-primary);
  background: rgba(255, 255, 255, .72);
  border: 1px solid rgba(74, 144, 226, .12);
  transition: transform .2s ease, border-color .2s ease, background .2s ease;

  svg {
    width: 18px;
    height: 18px;
    color: rgba(45, 98, 200, .64);
  }

  &.active {
    background: rgba(238, 243, 249, .92);
    border-color: rgba(45, 98, 200, .36);
    box-shadow: inset 3px 0 0 $blue-dark;
  }

  &:active {
    transform: scale(.985);
  }
}

.session-index {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  color: $blue-dark;
  background: rgba(185, 225, 205, .22);
  font-size: 13px;
  font-weight: 900;
}

.session-info {
  min-width: 0;

  strong,
  small {
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  strong {
    color: $ink;
    font-size: 14px;
    font-weight: 800;
  }

  small {
    margin-top: 4px;
    color: var(--text-regular);
    font-size: 12px;
  }
}

.empty-history {
  min-height: 170px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--text-regular);
  text-align: center;

  strong {
    margin-top: 10px;
    color: $ink;
    font-size: 15px;
  }

  small {
    margin-top: 5px;
    font-size: 12px;
  }
}

.empty-history-icon {
  width: 50px;
  height: 50px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  color: $sky-blue;
  background: rgba(224, 238, 248, .68);

  svg {
    width: 28px;
    height: 28px;
  }
}

.chat-content {
  flex: 1;
  min-width: 0;
  overflow-y: auto;
  padding: 18px 14px 18px;
  scroll-behavior: smooth;
}

.welcome-section {
  min-height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 8px 2px 18px;
  text-align: center;
}

.welcome-icon {
  position: relative;
  width: 86px;
  height: 86px;
  margin: 0 auto 15px;
  border-radius: 22px;
  display: grid;
  place-items: center;
  color: $blue-dark;
  background: rgba(255, 255, 255, .72);
  border: 1px solid rgba(213, 237, 243, .78);
  box-shadow: 0 22px 50px rgba(102, 170, 189, .16);

  span {
    position: absolute;
    inset: 12px;
    border-radius: 18px;
    border: 1px solid rgba(93, 184, 216, .36);
    animation: avatarPulse 2.8s ease-in-out infinite;
  }

  svg {
    position: relative;
    z-index: 2;
    width: 46px;
    height: 46px;
  }
}

.welcome-kicker {
  color: var(--text-light);
  font-size: 11px;
  font-weight: 900;
}

.welcome-section h3 {
  margin: 5px 0 7px;
  color: $ink;
  font-size: 20px;
  line-height: 1.25;
  font-weight: 850;
}

.welcome-section p {
  width: min(290px, 100%);
  margin: 0 auto 20px;
  color: var(--text-regular);
  font-size: 14px;
  line-height: 1.6;
}

.quick-questions {
  display: grid;
  gap: 10px;
}

.quick-question {
  min-height: 52px;
  border-radius: 14px;
  display: grid;
  grid-template-columns: 1fr 22px;
  align-items: center;
  gap: 10px;
  padding: 13px 14px;
  color: $ink;
  text-align: left;
  background: rgba(255, 255, 255, .76);
  border: 1px solid rgba(74, 144, 226, .16);
  box-shadow: 0 14px 30px rgba(102, 170, 189, .1);
  transition: transform .18s ease, border-color .18s ease;

  span {
    min-width: 0;
    font-size: 14px;
    line-height: 1.35;
    font-weight: 700;
  }

  svg {
    width: 18px;
    height: 18px;
    color: rgba(95, 158, 140, .72);
  }

  &:active {
    transform: scale(.985);
    border-color: rgba(45, 98, 200, .36);
  }
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-bottom: 4px;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 9px;
  animation: messageIn .26s ease both;

  &.user {
    flex-direction: row-reverse;
  }
}

.avatar {
  width: 34px;
  height: 34px;
  flex: 0 0 auto;
  border-radius: 11px;
  display: grid;
  place-items: center;
  box-shadow: 0 10px 24px rgba(74, 144, 226, .12);

  svg {
    width: 20px;
    height: 20px;
  }
}

.assistant-avatar {
  color: $blue-dark;
  background: rgba(255, 255, 255, .78);
  border: 1px solid rgba(104, 199, 169, .22);

  img {
    width: 28px;
    height: 28px;
    border-radius: 9px;
    object-fit: cover;
    display: block;
  }
}

.user-avatar {
  color: #fff;
  background: linear-gradient(135deg, $sky-blue, $blue-bright);
}

.message-content {
  max-width: min(78%, 560px);
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.message-bubble {
  position: relative;
  padding: 12px 14px;
  border-radius: 16px;
  font-size: 15px;
  line-height: 1.65;
  word-break: break-word;
  overflow-wrap: anywhere;
}

.assistant .message-bubble {
  color: var(--text-primary);
  background: rgba(255, 255, 255, .86);
  border: 1px solid rgba(213, 237, 243, .78);
  border-bottom-left-radius: 5px;
  box-shadow: 0 12px 30px rgba(102, 170, 189, .11);
}

.user .message-bubble {
  color: #fff;
  background: #3880c8;
  border-bottom-right-radius: 5px;
  box-shadow: 0 14px 30px rgba(67, 184, 180, .22);
}

.message-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  max-width: 100%;
}

.message-action-btn {
  max-width: 100%;
  min-height: 36px;
  border-radius: 10px;
  padding: 8px 11px;
  color: $blue-dark;
  background: rgba(255, 255, 255, .86);
  border: 1px solid rgba(74, 144, 226, .22);
  box-shadow: 0 8px 20px rgba(74, 144, 226, .1);
  font-size: 13px;
  font-weight: 800;
  line-height: 1.28;
  text-align: left;
  word-break: break-word;
  overflow-wrap: anywhere;

  &.primary {
    color: #fff;
    background: linear-gradient(135deg, $blue-dark, $blue-bright);
    border-color: transparent;
  }

  &:disabled {
    opacity: .56;
  }

  &:active {
    transform: scale(.98);
  }
}

.thinking-indicator {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px 9px;
  color: rgba(45, 90, 78, .58);
  font-size: 12px;
  font-weight: 700;
}

.thinking-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: $sky-blue;
  animation: thinkBounce 1.4s ease-in-out infinite;

  &:nth-child(2) {
    animation-delay: .2s;
  }

  &:nth-child(3) {
    animation-delay: .4s;
  }
}

.thinking-step-text {
  transition: opacity .4s ease;
}

.chat-input-area {
  position: relative;
  z-index: 22;
  padding: 9px 12px calc(10px + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, .76);
  border-top: 1px solid rgba(213, 237, 243, .72);
  box-shadow: 0 -12px 34px rgba(74, 144, 226, .12);
  backdrop-filter: blur(18px);
}

.voice-status {
  width: fit-content;
  max-width: 100%;
  min-height: 28px;
  margin: 0 auto 7px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 11px;
  color: $ink;
  background: rgba(238, 243, 249, .92);
  border: 1px solid rgba(104, 199, 169, .2);
  font-size: 12px;
  font-weight: 800;
}

.voice-bars {
  display: inline-flex;
  align-items: center;
  gap: 2px;

  i {
    width: 3px;
    height: 10px;
    border-radius: 99px;
    background: $blue-bright;
    animation: voiceBar .82s ease-in-out infinite;

    &:nth-child(2) { animation-delay: .12s; }
    &:nth-child(3) { animation-delay: .24s; }
    &:nth-child(4) { animation-delay: .36s; }
  }
}

.input-wrapper {
  display: grid;
  grid-template-columns: 40px 1fr 40px;
  align-items: end;
  gap: 8px;
  min-height: 52px;
  padding: 6px;
  border-radius: 18px;
  background: rgba(240, 249, 253, .84);
  border: 1px solid rgba(213, 237, 243, .86);
  box-shadow: inset 0 0 0 1px rgba(255,255,255,.44);
}

.voice-btn,
.send-btn {
  width: 40px;
  height: 40px;
  border-radius: 13px;
  display: grid;
  place-items: center;
  color: #fff;
  transition: transform .18s ease, opacity .18s ease, filter .18s ease;

  svg {
    width: 20px;
    height: 20px;
  }

  &:active {
    transform: scale(.94);
  }
}

.voice-btn {
  color: $blue-dark;
  background: rgba(255, 255, 255, .9);
  border: 1px solid rgba(74, 144, 226, .18);

  &.recording {
    color: #fff;
    background: linear-gradient(135deg, $warm, $blue-bright);
    filter: drop-shadow(0 8px 18px rgba(232, 152, 96, .22));
    animation: micPulse 1.2s ease-in-out infinite;
  }
}

textarea {
  width: 100%;
  min-height: 40px;
  max-height: 118px;
  padding: 9px 0;
  border: none;
  outline: none;
  resize: none;
  overflow-y: auto;
  background: transparent;
  color: $ink;
  font-family: inherit;
  font-size: 15px;
  line-height: 1.45;

  &::placeholder {
    color: rgba(88, 113, 128, .52);
  }
}

.send-btn {
  background: linear-gradient(135deg, $sky-blue, $blue-bright);

  &:disabled {
    opacity: .42;
    filter: grayscale(.25);
  }
}

.loading-icon {
  animation: spin 1s linear infinite;
}

@keyframes gridDrift {
  from { background-position: 0 0, 0 0; }
  to { background-position: 28px 28px, 28px 28px; }
}

@keyframes avatarPulse {
  0%, 100% { opacity: .42; transform: scale(.94); }
  50% { opacity: .9; transform: scale(1.08); }
}

@keyframes messageIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes thinkBounce {
  0%, 80%, 100% { transform: translateY(0); opacity: .48; }
  40% { transform: translateY(-7px); opacity: 1; }
}

@keyframes voiceBar {
  0%, 100% { height: 8px; opacity: .55; }
  50% { height: 16px; opacity: 1; }
}

@keyframes micPulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(232, 152, 96, .28); }
  50% { box-shadow: 0 0 0 8px rgba(232, 152, 96, 0); }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@media (min-width: 768px) {
  .chat-page {
    max-width: 520px;
    margin: 0 auto;
    border-left: 1px solid rgba(74, 144, 226, .12);
    border-right: 1px solid rgba(74, 144, 226, .12);
  }

  .session-sidebar {
    left: calc(50% - 248px);
  }
}
</style>
