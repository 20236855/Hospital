<template>
  <div class="chat-page">
    <div class="chat-header">
      <div class="header-left" @click="goBack">
        <svg viewBox="0 0 24 24" fill="none">
          <path d="M15 18l-6-6 6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
      <div class="header-title">
        <div class="ai-avatar">
          <svg viewBox="0 0 24 24" fill="none">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z" fill="#68c7a9"/>
          </svg>
        </div>
        <h2>智能问诊助手</h2>
        <span class="ai-status">在线</span>
      </div>
      <div class="header-right" @click="createNewSession">
        <svg viewBox="0 0 24 24" fill="none">
          <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
    </div>

    <div class="chat-main">
      <div :class="['session-sidebar', { collapsed: sidebarCollapsed }]">
        <div class="sidebar-header">
          <h3>历史对话</h3>
        </div>
        <div class="session-list">
          <div 
            v-for="(sessionId, index) in sessionList" 
            :key="sessionId"
            :class="['session-item', { active: currentSessionId === sessionId }]"
            @click="switchSession(sessionId)"
          >
            <div class="session-icon">
              <svg viewBox="0 0 24 24" fill="none">
                <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" stroke="#68c7a9" stroke-width="2" fill="none"/>
              </svg>
            </div>
            <div class="session-info">
              <div class="session-title">对话 {{ index + 1 }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="toggle-btn" @click="toggleSidebar">
        <svg v-if="!sidebarCollapsed" viewBox="0 0 24 24" fill="none">
          <path d="M15 18l-6-6 6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <svg v-else viewBox="0 0 24 24" fill="none">
          <path d="M9 18l6-6-6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>

      <div class="chat-content" ref="chatContentRef">
        <div v-if="messages.length === 0" class="welcome-section">
          <div class="welcome-icon">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" stroke="#68c7a9" stroke-width="2" fill="none"/>
            </svg>
          </div>
          <h3>您好，我是您的智能问诊助手</h3>
          <p>我可以结合您的病历信息，为您提供健康咨询服务</p>
          <div class="quick-questions">
            <div v-for="(question, index) in quickQuestions" :key="index" class="quick-question" @click="sendQuickQuestion(question)">
              {{ question }}
            </div>
          </div>
        </div>
        
        <div v-else class="message-list">
          <div v-for="(message, index) in messages" :key="index" :class="['message-item', message.role]">
            <div v-if="message.role === 'assistant'" class="avatar">
              <svg viewBox="0 0 24 24" fill="none">
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z" fill="#68c7a9"/>
              </svg>
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
                <span>正在思考中...</span>
              </div>
            </div>
            <div v-if="message.role === 'user'" class="avatar">
              <svg viewBox="0 0 24 24" fill="none">
                <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2M12 11a4 4 0 100-8 4 4 0 000 8z" stroke="#8ed6f2" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="chat-input-area">
      <div class="input-wrapper">
        <textarea
          v-model="inputMessage"
          placeholder="描述您的症状或问题..."
          rows="1"
          @keydown="handleKeydown"
          ref="textareaRef"
        ></textarea>
        <button class="send-btn" @click="sendMessage" :disabled="!inputMessage.trim() || loading">
          <svg v-if="!loading" viewBox="0 0 24 24" fill="none">
            <path d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <svg v-else class="loading-icon" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" opacity="0.2"/>
            <path d="M21 12a9 9 0 00-9-9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { consultAi, getAiChatHistory, getSessionList } from '@/api/chat'

const router = useRouter()
const chatContentRef = ref(null)
const textareaRef = ref(null)
const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const currentSessionId = ref('')
const sessionList = ref([])
const sidebarCollapsed = ref(false)

const quickQuestions = [
  '最近有点头晕，该怎么办？',
  '我之前的病历里有什么需要注意的？',
  '最近睡眠不好，有什么建议？',
  '日常饮食需要注意什么？'
]

const goBack = () => {
  router.back()
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatContentRef.value) {
      chatContentRef.value.scrollTop = chatContentRef.value.scrollHeight
    }
  })
}

const loadSessionList = async () => {
  try {
    const patientId = getStoredPatientId()
    const res = await getSessionList({ patientId })
    if (res.data) {
      sessionList.value = res.data
      // 如果有会话但没有当前会话，默认选择第一个
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
  // 生成新的会话ID
  const newSessionId = `session_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  currentSessionId.value = newSessionId
  // 清空消息
  messages.value = []
  // 保存到localStorage
  localStorage.setItem('aiChatSessionId', newSessionId)
  console.log('创建新会话:', newSessionId)
}

const switchSession = async (sessionIdToSwitch) => {
  if (!sessionIdToSwitch) return
  
  console.log('切换到会话:', sessionIdToSwitch)
  currentSessionId.value = sessionIdToSwitch
  localStorage.setItem('aiChatSessionId', sessionIdToSwitch)
  // 加载该会话的历史消息
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
    // 发送后重新加载会话列表
    await loadSessionList()
  }
  return res.data?.reply || ''
}

const sendMessage = async () => {
  const message = inputMessage.value.trim()
  if (!message || loading.value) return
  
  loading.value = true
  
  // 如果没有会话ID，先创建一个
  if (!currentSessionId.value) {
    createNewSession()
  }
  
  messages.value.push({
    role: 'user',
    content: message
  })
  
  inputMessage.value = ''
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
  sendMessage()
}

const handleKeydown = (e) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
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

onMounted(() => {
  // 初始化时检查localStorage中是否有会话ID
  const savedSessionId = localStorage.getItem('aiChatSessionId')
  if (savedSessionId) {
    currentSessionId.value = savedSessionId
  }
  // 加载会话列表
  loadSessionList()
  // 如果有会话ID，加载该会话的历史
  if (currentSessionId.value) {
    loadChatHistory(currentSessionId.value)
  }
})
</script>

<style scoped lang="scss">
.chat-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: var(--bg-gradient);
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: rgba(255, 253, 248, 0.9);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(213, 237, 243, 0.5);
  position: sticky;
  top: 0;
  z-index: 100;
  
  .header-left,
  .header-right {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    
    svg {
      width: 24px;
      height: 24px;
      color: var(--text-primary);
    }
  }
  
  .header-title {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .ai-avatar {
      width: 44px;
      height: 44px;
      background: linear-gradient(135deg, #e6f6fb, #d0eff6);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      
      svg {
        width: 24px;
        height: 24px;
      }
    }
    
    .ai-info {
      h2 {
        margin: 0;
        font-size: 16px;
        font-weight: 700;
        color: var(--text-primary);
      }
      
      .ai-status {
        font-size: 12px;
        color: #68c7a9;
        font-weight: 500;
      }
    }
  }
}

.chat-main {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.session-sidebar {
  width: 240px;
  background: rgba(255, 253, 248, 0.8);
  border-right: 1px solid rgba(213, 237, 243, 0.5);
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  
  &.collapsed {
    width: 0;
    padding: 0;
    overflow: hidden;
    border-right: none;
  }
  
  .sidebar-header {
    padding: 16px;
    border-bottom: 1px solid rgba(213, 237, 243, 0.5);
    
    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: var(--text-primary);
    }
  }
  
  .session-list {
    flex: 1;
    overflow-y: auto;
    padding: 8px;
  }
  
  .session-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.2s ease;
    
    &:hover {
      background: rgba(142, 214, 242, 0.1);
    }
    
    &.active {
      background: rgba(104, 199, 169, 0.15);
    }
    
    .session-icon {
      width: 36px;
      height: 36px;
      background: rgba(104, 199, 169, 0.1);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      
      svg {
        width: 20px;
        height: 20px;
      }
    }
    
    .session-info {
      flex: 1;
      min-width: 0;
      
      .session-title {
        font-size: 14px;
        font-weight: 500;
        color: var(--text-primary);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
}

.toggle-btn {
  width: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 253, 248, 0.9);
  cursor: pointer;
  transition: all 0.3s ease;
  border-right: 1px solid rgba(213, 237, 243, 0.5);
  
  &:hover {
    background: rgba(142, 214, 242, 0.1);
  }
  
  svg {
    width: 16px;
    height: 16px;
    color: var(--text-primary);
  }
}

.chat-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  
  .welcome-section {
    text-align: center;
    padding: 40px 20px;
    
    .welcome-icon {
      width: 80px;
      height: 80px;
      margin: 0 auto 20px;
      background: rgba(104, 199, 169, 0.1);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      
      svg {
        width: 44px;
        height: 44px;
      }
    }
    
    h3 {
      margin: 0 0 8px;
      font-size: 18px;
      font-weight: 700;
      color: var(--text-primary);
    }
    
    p {
      margin: 0 0 24px;
      font-size: 14px;
      color: var(--text-regular);
    }
    
    .quick-questions {
      display: flex;
      flex-direction: column;
      gap: 12px;
      
      .quick-question {
        padding: 14px 18px;
        background: rgba(255, 253, 248, 0.85);
        border: 1px solid rgba(213, 237, 243, 0.5);
        border-radius: 16px;
        font-size: 14px;
        color: var(--text-primary);
        text-align: left;
        cursor: pointer;
        transition: all 0.2s ease;
        
        &:hover {
          background: rgba(255, 253, 248, 1);
          border-color: #8ed6f2;
        }
        
        &:active {
          transform: scale(0.98);
        }
      }
    }
  }
  
  .message-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }
  
  .message-item {
    display: flex;
    gap: 12px;
    align-items: flex-start;
    
    &.user {
      flex-direction: row-reverse;
    }
    
    .avatar {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      background: rgba(142, 214, 242, 0.15);
    }
    
    .message-content {
      max-width: 75%;
      display: flex;
      flex-direction: column;
      gap: 8px;
    }
    
    .message-bubble {
      padding: 12px 16px;
      border-radius: 16px;
      font-size: 15px;
      line-height: 1.6;
      word-wrap: break-word;
    }
    
    &.assistant .message-bubble {
      background: rgba(255, 253, 248, 0.9);
      border: 1px solid rgba(213, 237, 243, 0.5);
      color: var(--text-primary);
      border-bottom-left-radius: 4px;
    }
    
    &.user .message-bubble {
      background: linear-gradient(135deg, #8ed6f2, #68c7a9);
      color: #fff;
      border-bottom-right-radius: 4px;
    }
    
    .thinking-indicator {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 8px 12px;
      font-size: 13px;
      color: #888;
      
      .thinking-dot {
        width: 6px;
        height: 6px;
        background: #8ed6f2;
        border-radius: 50%;
        animation: thinkBounce 1.4s ease-in-out infinite;
        
        &:nth-child(2) {
          animation-delay: 0.2s;
        }
        
        &:nth-child(3) {
          animation-delay: 0.4s;
        }
      }
    }
  }
}

.chat-input-area {
  padding: 12px 16px;
  background: rgba(255, 253, 248, 0.9);
  backdrop-filter: blur(12px);
  border-top: 1px solid rgba(213, 237, 243, 0.5);
  
  .input-wrapper {
    display: flex;
    gap: 12px;
    align-items: flex-end;
    background: rgba(240, 249, 253, 0.8);
    border: 1px solid rgba(213, 237, 243, 0.7);
    border-radius: 24px;
    padding: 10px 16px;
    
    textarea {
      flex: 1;
      border: none;
      background: transparent;
      font-size: 15px;
      resize: none;
      max-height: 120px;
      line-height: 1.5;
      color: var(--text-primary);
      font-family: inherit;
      
      &:focus {
        outline: none;
      }
      
      &::placeholder {
        color: #999;
      }
    }
    
    .send-btn {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      border: none;
      background: linear-gradient(135deg, #8ed6f2, #68c7a9);
      color: #fff;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      transition: all 0.2s ease;
      
      &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }
      
      &:not(:disabled):active {
        transform: scale(0.9);
      }
      
      svg {
        width: 18px;
        height: 18px;
      }
      
      .loading-icon {
        animation: spin 1s linear infinite;
      }
    }
  }
}

@keyframes thinkBounce {
  0%, 80%, 100% {
    transform: translateY(0);
    opacity: 0.5;
  }
  40% {
    transform: translateY(-8px);
    opacity: 1;
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
