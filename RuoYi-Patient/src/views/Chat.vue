<template>
  <div class="chat-page">
    <div class="ambient-grid" aria-hidden="true"></div>
    <div class="signal-wave signal-wave-a" aria-hidden="true"></div>
    <div class="signal-wave signal-wave-b" aria-hidden="true"></div>

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
              <div v-if="message.isThinking" class="thinking-indicator">
                <span class="thinking-dot"></span>
                <span class="thinking-dot"></span>
                <span class="thinking-dot"></span>
                <span>正在分析症状...</span>
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
import assistantAvatar from '@/assets/picture.png'

const router = useRouter()
const chatContentRef = ref(null)
const textareaRef = ref(null)
const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const currentSessionId = ref('')
const sessionList = ref([])
const sidebarCollapsed = ref(true)
const isRecording = ref(false)
const recognition = ref(null)
const voiceSupported = ref(false)
let voiceBaseText = ''

const quickQuestions = [
  '最近有点头晕，该怎么办？',
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

  try {
    const response = await askBackendAi(message)

    messages.value = messages.value.filter(m => !m.isThinking)
    messages.value.push({
      role: 'assistant',
      content: response || '我已经收到您的问题，但暂时没有生成有效回复，请稍后再试。'
    })
  } catch (error) {
    console.error('AI 咨询失败:', error)
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

.signal-wave {
  position: absolute;
  left: -12%;
  right: -12%;
  height: 2px;
  pointer-events: none;
  background: linear-gradient(90deg, transparent, rgba(74, 144, 226, .52), rgba(74, 144, 226, .34), transparent);
  opacity: .55;
  filter: blur(.2px);
}

.signal-wave-a {
  top: 118px;
  animation: signalSweep 4.8s ease-in-out infinite;
}

.signal-wave-b {
  bottom: 116px;
  animation: signalSweep 5.6s ease-in-out infinite reverse;
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

@keyframes signalSweep {
  0%, 100% { transform: translateX(-12%) scaleX(.84); opacity: .18; }
  50% { transform: translateX(12%) scaleX(1); opacity: .58; }
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
