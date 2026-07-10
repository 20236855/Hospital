<template>
  <div class="guide-assistant">
    <button
      class="guide-float"
      type="button"
      :class="{ active: open }"
      aria-label="打开指导助手"
      @click="togglePanel"
    >
      <el-icon><ChatDotRound /></el-icon>
      <span>指导</span>
    </button>

    <transition name="guide-panel">
      <section v-if="open" class="guide-panel">
        <div class="guide-head">
          <div>
            <span>OPERATION GUIDE</span>
            <h3>指导助手</h3>
          </div>
          <button type="button" class="guide-close" aria-label="关闭指导助手" @click="open = false">
            <el-icon><Close /></el-icon>
          </button>
        </div>

        <div ref="bodyRef" class="guide-body">
          <div class="guide-message assistant">
            <div class="guide-avatar">
              <el-icon><Service /></el-icon>
            </div>
            <div class="guide-bubble">
              想找哪个操作入口？可以问我：检查检验申请在哪里、排班界面在哪里、药品信息在哪里。
            </div>
          </div>

          <template v-for="message in messages" :key="message.id">
            <div v-if="message.type === 'user'" class="guide-message user">
              <div class="guide-bubble">{{ message.text }}</div>
            </div>
            <div v-else class="guide-message assistant">
              <div class="guide-avatar">
                <el-icon><Service /></el-icon>
              </div>
              <div class="guide-bubble">
                <p>{{ message.text }}</p>
                <div v-if="message.results?.length" class="guide-results">
                  <button
                    v-for="item in message.results"
                    :key="item.path"
                    type="button"
                    class="guide-result"
                    @click="goRoute(item)"
                  >
                    <span>
                      <strong>{{ item.title }}</strong>
                      <small>{{ item.desc }}</small>
                    </span>
                    <em>{{ item.path }}</em>
                  </button>
                </div>
              </div>
            </div>
          </template>
        </div>

        <div class="guide-quick">
          <button v-for="item in quickQuestions" :key="item" type="button" @click="askQuick(item)">
            {{ item }}
          </button>
        </div>

        <div class="guide-input">
          <el-input
            v-model="question"
            placeholder="输入要找的功能，例如：排班、检查申请"
            @keyup.enter="sendQuestion"
          />
          <el-button type="primary" :disabled="!question.trim()" @click="sendQuestion">发送</el-button>
        </div>
      </section>
    </transition>
  </div>
</template>

<script setup>
import { ChatDotRound, Close, Service } from '@element-plus/icons-vue'
import { nextTick, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const open = ref(false)
const question = ref('')
const messages = ref([])
const bodyRef = ref(null)
let messageId = 0

const quickQuestions = ['检查检验申请', '排班界面', '药品信息', '电子病历']

const guideEntries = [
  {
    title: '检查检验申请',
    path: '/hisexam/apply',
    desc: '新建、查询和维护检查/检验申请单',
    keywords: ['检查检验申请', '检查申请', '检验申请', '申请单', '开检查', '开检验', 'hisexam apply']
  },
  {
    title: '检查项目维护',
    path: '/hisexam/technology',
    desc: '维护检查检验项目、技术项目与基础信息',
    keywords: ['检查项目', '检验项目', '项目维护', '医技项目', 'technology']
  },
  {
    title: '检查检验结果',
    path: '/hisexam/result',
    desc: '查看检查检验结果与报告信息',
    keywords: ['检查结果', '检验结果', '报告结果', '检查报告', '检验报告', 'result']
  },
  {
    title: '智能会诊排班',
    path: '/hisdoctor/schedule',
    desc: '查看、生成和微调医生排班',
    keywords: ['排班', '排班界面', '医生排班', '会诊排班', '智能排班', 'schedule']
  },
  {
    title: '医生信息',
    path: '/hisdoctor/doctor',
    desc: '维护医生档案、职称、科室和简介',
    keywords: ['医生', '医生信息', '医生管理', '医生档案', 'doctor']
  },
  {
    title: '挂号级别',
    path: '/hisdoctor/doctorlevel',
    desc: '维护挂号级别、号别和诊查费用',
    keywords: ['挂号级别', '号别', '诊查费', '级别', 'doctorlevel']
  },
  {
    title: '患者档案',
    path: '/patient/patient',
    desc: '查询和维护患者基础档案',
    keywords: ['患者', '患者档案', '患者信息', '病人', 'patient']
  },
  {
    title: '接诊管理',
    path: '/emr/encounter',
    desc: '管理接诊记录、患者就诊过程',
    keywords: ['接诊', '就诊', '门诊接诊', 'encounter']
  },
  {
    title: '电子病历',
    path: '/emr/record',
    desc: '查看和维护患者电子病历',
    keywords: ['电子病历', '病历', '病历管理', 'emr', 'record']
  },
  {
    title: '疾病管理',
    path: '/emr/disease',
    desc: '维护疾病字典与诊断基础数据',
    keywords: ['疾病', '疾病管理', '诊断字典', 'disease']
  },
  {
    title: '处方管理',
    path: '/hisprescription/prescription',
    desc: '开立、查询和维护处方主信息',
    keywords: ['处方', '处方管理', '开方', 'prescription']
  },
  {
    title: '处方明细',
    path: '/hisprescription/item',
    desc: '查看处方药品明细、用法用量',
    keywords: ['处方明细', '药品明细', '用药明细', 'item']
  },
  {
    title: '药品信息',
    path: '/hisprescription/drug',
    desc: '药房管理、药品维护和采购预测控制台',
    keywords: ['药品', '药房', '药品信息', '药房管理', '采购预测', 'drug']
  },
  {
    title: '收费管理',
    path: '/payment/payment',
    desc: '处理患者收费、缴费和费用记录',
    keywords: ['收费', '缴费', '费用', '收费管理', 'payment']
  },
  {
    title: '收费类别',
    path: '/payment/category',
    desc: '维护收费项目分类',
    keywords: ['收费类别', '费用类别', '收费分类', 'category']
  },
  {
    title: '挂号管理',
    path: '/register/register',
    desc: '查看和维护挂号记录',
    keywords: ['挂号', '挂号管理', '预约挂号', 'register']
  },
  {
    title: '签到管理',
    path: '/register/in',
    desc: '处理患者到院签到/就诊确认',
    keywords: ['签到', '到院', '就诊确认', 'check in']
  },
  {
    title: '用户管理',
    path: '/system/user',
    desc: '维护系统用户、角色和账号状态',
    keywords: ['用户', '账号', '用户管理', 'system user']
  },
  {
    title: '角色管理',
    path: '/system/role',
    desc: '配置角色权限与菜单权限',
    keywords: ['角色', '权限', '角色管理', 'role']
  },
  {
    title: '菜单管理',
    path: '/system/menu',
    desc: '维护系统菜单和路由配置',
    keywords: ['菜单', '路由', '菜单管理', 'menu']
  }
]

function togglePanel() {
  open.value = !open.value
  if (open.value) {
    nextTick(scrollToBottom)
  }
}

function askQuick(text) {
  question.value = text
  sendQuestion()
}

function sendQuestion() {
  const text = question.value.trim()
  if (!text) return
  messages.value.push({ id: nextId(), type: 'user', text })
  const results = findGuideEntries(text)
  messages.value.push({
    id: nextId(),
    type: 'assistant',
    text: results.length
      ? '我找到了这些入口，点击卡片即可跳转：'
      : '暂时没有匹配到明确入口。可以换个说法，比如“检查申请”“排班”“药品信息”。',
    results
  })
  question.value = ''
  nextTick(scrollToBottom)
}

function findGuideEntries(text) {
  const query = normalize(text)
  return guideEntries
    .map(entry => ({ ...entry, score: scoreEntry(entry, query) }))
    .filter(entry => entry.score > 0)
    .sort((a, b) => b.score - a.score)
    .slice(0, 4)
}

function scoreEntry(entry, query) {
  const title = normalize(entry.title)
  const desc = normalize(entry.desc)
  let score = 0
  if (title === query) score += 100
  if (title.includes(query) || query.includes(title)) score += 60
  if (desc.includes(query)) score += 12
  entry.keywords.forEach(keyword => {
    const normalizedKeyword = normalize(keyword)
    if (query.includes(normalizedKeyword) || normalizedKeyword.includes(query)) {
      score += normalizedKeyword.length >= 4 ? 36 : 20
    }
  })
  return score
}

function normalize(value) {
  return String(value || '')
    .toLowerCase()
    .replace(/[，。！？、,.!?？\s]/g, '')
}

function goRoute(item) {
  router.push(item.path)
  messages.value.push({
    id: nextId(),
    type: 'assistant',
    text: `已打开「${item.title}」。如果没有权限，系统会停留在当前页面或提示无权访问。`,
    results: []
  })
  nextTick(scrollToBottom)
}

function scrollToBottom() {
  if (bodyRef.value) {
    bodyRef.value.scrollTop = bodyRef.value.scrollHeight
  }
}

function nextId() {
  messageId += 1
  return messageId
}
</script>

<style scoped lang="scss">
.guide-assistant {
  position: fixed;
  right: 22px;
  bottom: 24px;
  z-index: 3000;
}

.guide-float {
  width: 64px;
  height: 64px;
  border: 0;
  border-radius: 18px;
  display: grid;
  place-items: center;
  gap: 2px;
  color: #fff;
  background: linear-gradient(135deg, #2563eb, #06b6d4);
  box-shadow: 0 18px 38px rgba(37, 99, 235, .28);
  cursor: pointer;
  transition: transform .18s ease, box-shadow .18s ease;

  .el-icon {
    font-size: 23px;
  }

  span {
    font-size: 12px;
    font-weight: 800;
  }

  &:hover,
  &.active {
    transform: translateY(-2px);
    box-shadow: 0 22px 48px rgba(37, 99, 235, .34);
  }
}

.guide-panel {
  position: absolute;
  right: 0;
  bottom: 78px;
  width: 380px;
  max-width: calc(100vw - 32px);
  overflow: hidden;
  border: 1px solid rgba(148, 163, 184, .28);
  border-radius: 12px;
  background: rgba(255, 255, 255, .96);
  box-shadow: 0 24px 70px rgba(15, 23, 42, .2);
}

.guide-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px 16px;
  color: #fff;
  background: linear-gradient(135deg, #1d4ed8, #0ea5e9);

  span {
    font-size: 11px;
    font-weight: 800;
    opacity: .78;
  }

  h3 {
    margin: 3px 0 0;
    font-size: 18px;
    line-height: 1.2;
  }
}

.guide-close {
  width: 32px;
  height: 32px;
  border: 0;
  border-radius: 9px;
  display: grid;
  place-items: center;
  color: #fff;
  background: rgba(255, 255, 255, .16);
  cursor: pointer;
}

.guide-body {
  height: 330px;
  overflow-y: auto;
  padding: 14px;
  background: linear-gradient(180deg, #f8fbff, #fff);
}

.guide-message {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;

  &.user {
    justify-content: flex-end;

    .guide-bubble {
      color: #fff;
      background: #2563eb;
      border-color: #2563eb;
    }
  }
}

.guide-avatar {
  width: 30px;
  height: 30px;
  flex: 0 0 auto;
  border-radius: 10px;
  display: grid;
  place-items: center;
  color: #1d4ed8;
  background: #e0edff;
}

.guide-bubble {
  max-width: 292px;
  padding: 10px 12px;
  border: 1px solid #e5edf7;
  border-radius: 10px;
  color: #263449;
  background: #fff;
  font-size: 13px;
  line-height: 1.55;

  p {
    margin: 0;
  }
}

.guide-results {
  display: grid;
  gap: 8px;
  margin-top: 9px;
}

.guide-result {
  width: 100%;
  min-height: 58px;
  border: 1px solid #dbeafe;
  border-radius: 9px;
  display: grid;
  grid-template-columns: 1fr auto;
  align-items: center;
  gap: 10px;
  padding: 10px;
  text-align: left;
  background: #f8fbff;
  cursor: pointer;

  strong,
  small {
    display: block;
  }

  strong {
    color: #1e293b;
    font-size: 13px;
  }

  small {
    margin-top: 3px;
    color: #64748b;
    font-size: 12px;
  }

  em {
    color: #2563eb;
    font-size: 11px;
    font-style: normal;
    white-space: nowrap;
  }

  &:hover {
    border-color: #93c5fd;
    background: #eef6ff;
  }
}

.guide-quick {
  display: flex;
  gap: 8px;
  padding: 10px 12px 0;
  overflow-x: auto;

  button {
    border: 1px solid #dbeafe;
    border-radius: 999px;
    padding: 6px 10px;
    color: #2563eb;
    background: #eff6ff;
    font-size: 12px;
    white-space: nowrap;
    cursor: pointer;
  }
}

.guide-input {
  display: grid;
  grid-template-columns: 1fr 64px;
  gap: 8px;
  padding: 12px;
  border-top: 1px solid #eef2f7;
}

.guide-panel-enter-active,
.guide-panel-leave-active {
  transition: opacity .18s ease, transform .18s ease;
}

.guide-panel-enter-from,
.guide-panel-leave-to {
  opacity: 0;
  transform: translateY(8px) scale(.98);
}

@media (max-width: 992px) {
  .guide-assistant {
    display: none;
  }
}
</style>
