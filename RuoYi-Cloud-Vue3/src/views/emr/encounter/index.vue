<template>
  <div class="app-container encounter-page">
    <DoctorInsightDashboard
      eyebrow="Outpatient Command"
      title="接诊医生业务驾驶舱"
      subtitle="基于当前接诊列表实时计算待接诊、接诊中、完成率和AI辅助使用情况"
      tone="clinical"
      :focus-value="encounterDashboard.focusValue"
      focus-label="完成率"
      :metrics="encounterDashboard.metrics"
      :lanes="encounterDashboard.lanes"
      :chips="encounterDashboard.chips"
    />

    <!-- ===== 搜索区域 ===== -->
    <div class="search-panel">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
        <el-form-item label="挂号ID" prop="registerId">
          <el-input v-model="queryParams.registerId" placeholder="请输入" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="患者ID" prop="patientId">
          <el-input v-model="queryParams.patientId" placeholder="请输入" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="医生ID" prop="doctorId">
          <el-input v-model="queryParams.doctorId" placeholder="请输入" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="科室ID" prop="deptId">
          <el-input v-model="queryParams.deptId" placeholder="请输入" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="接诊日期" prop="visitTime">
          <el-date-picker clearable v-model="queryParams.visitTime" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- ===== 工具栏 ===== -->
    <el-row :gutter="10" class="toolbar-row">
      <el-col :span="1.5">
        <el-button type="success" :color="'#1A6B54'" icon="MagicStick" @click="openAIAssistantFirst">🤖 AI助手</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['emr:encounter:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['emr:encounter:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['emr:encounter:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['emr:encounter:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- ===== 数据表格 ===== -->
    <div class="table-wrap">
      <el-table v-loading="loading" :data="encounterList" @selection-change="handleSelectionChange" stripe>
        <el-table-column type="selection" width="45" fixed="left" />
        <el-table-column label="接诊ID" prop="encounterId" width="80" />
        <el-table-column label="患者" min-width="100" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.patientName || scope.row.patientId }}</template>
        </el-table-column>
        <el-table-column label="挂号编号" prop="registerNo" width="130" />
        <el-table-column label="类型" prop="encounterType" width="90" />
        <el-table-column label="状态" width="110">
          <template #default="scope">
            <el-tag :type="statusTag(scope.row.encounterStatus)" size="small">{{ scope.row.encounterStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="接诊时间" width="110">
          <template #default="scope">{{ parseTime(scope.row.visitTime, '{y}-{m}-{d}') }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="openAIAssistant(scope.row)">
              <el-icon><MagicStick /></el-icon>AI助手
            </el-button>
            <el-button link type="primary" size="small" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['emr:encounter:edit']">编辑</el-button>
            <el-button link type="danger" size="small" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['emr:encounter:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- ===== 新增/修改对话框 ===== -->
    <el-dialog :title="title" v-model="open" width="700px" append-to-body>
      <el-form ref="encounterRef" :model="form" :rules="rules" label-width="80px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="挂号ID" prop="registerId">
              <el-select v-model="form.registerId" placeholder="选择挂号" filterable clearable style="width:100%" @change="handleRegisterChange">
                <el-option v-for="item in registerOptions" :key="item.registerId" :label="formatRegisterLabel(item)" :value="item.registerId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="患者ID" prop="patientId">
              <el-select v-model="form.patientId" placeholder="选择患者" filterable clearable style="width:100%">
                <el-option v-for="item in patientOptions" :key="item.patientId" :label="formatPatientLabel(item)" :value="item.patientId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="医生ID" prop="doctorId">
              <el-input v-model="form.doctorId" :disabled="isDoctorUser" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="科室ID" prop="deptId">
              <el-input v-model="form.deptId" :disabled="isDoctorUser" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类型" prop="encounterType">
              <el-select v-model="form.encounterType" clearable style="width:100%">
                <el-option label="普通门诊" value="普通门诊" />
                <el-option label="复诊" value="复诊" />
                <el-option label="急诊" value="急诊" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="encounterStatus">
              <el-select v-model="form.encounterStatus" clearable style="width:100%">
                <el-option label="待接诊" value="待接诊" />
                <el-option label="接诊中" value="接诊中" />
                <el-option label="就诊已完成" value="就诊已完成" />
                <el-option label="已取消就诊" value="已取消就诊" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="接诊时间" prop="visitTime">
              <el-date-picker v-model="form.visitTime" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="finishTime">
              <el-date-picker v-model="form.finishTime" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ========== AI医疗助手抽屉 ========== -->
    <el-drawer
      v-model="aiDrawer"
      title=""
      direction="rtl"
      size="560px"
      :close-on-click-modal="false"
      class="ai-drawer"
    >
      <template #header>
        <div class="ai-drawer-header">
          <div class="ai-header-icon">
            <el-icon :size="24"><MagicStick /></el-icon>
          </div>
          <div class="ai-header-text">
            <span class="ai-title">AI 诊疗助手</span>
            <span class="ai-subtitle">辅助诊断 · 临床决策支持</span>
          </div>
          <el-tag size="small" :type="aiConfigured ? 'success' : 'danger'">
            {{ aiConfigured ? '已连接' : '未配置API' }}
          </el-tag>
        </div>
      </template>

      <div class="ai-drawer-body" v-if="currentPatient">
        <!-- 患者卡片 -->
        <div class="ai-patient-card">
          <div class="ai-patient-avatar">
            <el-icon :size="28"><UserFilled /></el-icon>
          </div>
          <div class="ai-patient-info">
            <div class="ai-patient-name">{{ currentPatient.patientName || '患者' }} <el-tag size="small">{{ currentPatient.encounterType }}</el-tag></div>
            <div class="ai-patient-meta">
              <span>挂号：{{ currentPatient.registerNo || currentPatient.registerId }}</span>
              <span v-if="currentPatient.patientPhone">电话：{{ currentPatient.patientPhone }}</span>
              <span>状态：{{ currentPatient.encounterStatus }}</span>
            </div>
          </div>
        </div>

        <!-- 快捷操作 -->
        <div class="ai-quick-actions">
          <el-button size="small" @click="aiQuickAsk('请根据该患者的既往病历和当前主诉，给出鉴别诊断建议（按可能性排序）及推荐的辅助检查项目')">
            <el-icon><List /></el-icon>鉴别诊断
          </el-button>
          <el-button size="small" @click="aiQuickAsk('请根据患者病情，推荐循证治疗方案，包括用药建议和注意事项')">
            <el-icon><Document /></el-icon>治疗方案
          </el-button>
          <el-button size="small" @click="aiQuickAsk('请列出该患者需要重点关注的体格检查项目及可能的阳性体征')">
            <el-icon><Connection /></el-icon>体格检查
          </el-button>
          <el-button size="small" type="success" :loading="generatingRecord" @click="aiGenerateFullRecord">
            <el-icon><MagicStick /></el-icon>一键生成病历
          </el-button>
        </div>

        <!-- 对话/结果区 -->
        <div class="ai-chat-area" ref="chatArea">
          <!-- 欢迎状态 -->
          <div class="ai-welcome" v-if="aiMessages.length === 0 && !aiThinking">
            <el-icon :size="40"><MagicStick /></el-icon>
            <p>AI 诊疗助手已就绪</p>
            <p class="hint">输入患者症状、体征等关键信息，AI 将辅助您进行临床决策</p>
          </div>

          <!-- 消息列表 -->
          <div v-for="(msg, idx) in aiMessages" :key="idx" class="ai-message" :class="'msg-' + msg.role">
            <div class="msg-avatar">
              <el-icon :size="18"><component :is="msg.role === 'user' ? 'UserFilled' : 'MagicStick'" /></el-icon>
            </div>
            <div class="msg-body">
              <div class="msg-content" v-html="formatMsgContent(msg.content)"></div>
              <div v-if="msg.hasMedicalRecord" class="msg-record-tip">📋 已结合既往病历分析</div>
              <!-- AI结果卡片 -->
              <div v-if="msg.aiResult" class="ai-result-card">
                <div v-if="msg.aiResult.urgentFlags?.length" class="result-urgent">
                  <el-alert :title="msg.aiResult.urgentFlags.join('；')" type="danger" :closable="false" show-icon />
                </div>
                <div v-if="msg.aiResult.chiefComplaint" class="result-item">
                  <span class="r-label">主诉</span>
                  <span class="r-val">{{ msg.aiResult.chiefComplaint }}</span>
                </div>
                <div v-if="msg.aiResult.diagnosisOpinion" class="result-item">
                  <span class="r-label">诊断建议</span>
                  <span class="r-val">{{ msg.aiResult.diagnosisOpinion }}</span>
                </div>
                <div v-if="msg.aiResult.needExam?.length" class="result-item">
                  <span class="r-label">建议检查</span>
                  <span class="r-val">{{ msg.aiResult.needExam.join('、') }}</span>
                </div>
                <div v-if="msg.aiResult.treatmentPlan" class="result-item">
                  <span class="r-label">治疗方案</span>
                  <span class="r-val">{{ msg.aiResult.treatmentPlan }}</span>
                </div>
              </div>
              <div class="msg-time">{{ msg.time }}</div>
            </div>
          </div>

          <!-- AI思考中 -->
          <div v-if="aiThinking" class="ai-message msg-assistant">
            <div class="msg-avatar"><el-icon :size="18"><MagicStick /></el-icon></div>
            <div class="msg-body"><div class="msg-thinking"><span></span><span></span><span></span></div></div>
          </div>
        </div>

        <!-- 输入区 -->
        <div class="ai-input-area">
          <el-input
            v-model="aiInput"
            placeholder="输入患者病情信息...（如：患者男45岁，头痛3天，伴恶心呕吐，BP 160/95mmHg，既往高血压史，目前服用氨氯地平）"
            type="textarea"
            :rows="3"
            @keydown.enter.exact="aiSend"
            resize="none"
          />
          <el-button type="primary" :color="'#1A6B54'" :loading="aiThinking" @click="aiSend" class="ai-send-btn">
            <el-icon><Position /></el-icon>发送
          </el-button>
        </div>
      </div>

      <!-- 未选择患者 -->
      <div class="ai-drawer-body ai-empty" v-else>
        <el-icon :size="48"><UserFilled /></el-icon>
        <p>请先选择一个接诊记录</p>
      </div>
    </el-drawer>

    <!-- ===== 病历生成预览弹窗 ===== -->
    <el-dialog v-model="previewOpen" title="AI 生成病历预览" width="700px" append-to-body>
      <el-form :model="previewRecord" label-width="80px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="主诉"><el-input v-model="previewRecord.chiefComplaint" type="textarea" :rows="2" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="现病史"><el-input v-model="previewRecord.presentIllness" type="textarea" :rows="2" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="既往史"><el-input v-model="previewRecord.pastHistory" type="textarea" :rows="2" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="过敏史"><el-input v-model="previewRecord.allergyHistory" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="体格检查"><el-input v-model="previewRecord.physicalExam" type="textarea" :rows="2" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="诊断意见"><el-input v-model="previewRecord.diagnosisOpinion" type="textarea" :rows="2" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="治疗方案"><el-input v-model="previewRecord.treatmentPlan" type="textarea" :rows="2" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="医生建议"><el-input v-model="previewRecord.doctorAdvice" type="textarea" :rows="2" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="saveRecord">确认保存病历</el-button>
        <el-button @click="previewOpen = false">继续编辑</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Encounter">
import { ref, reactive, toRefs, computed, getCurrentInstance, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick, UserFilled, List, Connection, Document, Position, TrendCharts, Clock, CircleCheckFilled } from '@element-plus/icons-vue'
import DoctorInsightDashboard from '@/components/DoctorInsightDashboard/index.vue'
import { listEncounter, getEncounter, delEncounter, addEncounter, updateEncounter } from "@/api/emr/encounter"
import { saveRecord as saveRecordApi } from "@/api/emr/record"
import { listPatient } from "@/api/patient/patient"
import { listRegister } from "@/api/register/register"
import { getDoctorByUserId, getMyDoctorInfo } from "@/api/hisdoctor/doctor"
import { aiChat } from "@/api/ai/assistant"
import useUserStore from "@/store/modules/user"

const { proxy } = getCurrentInstance()
const userStore = useUserStore()

// ===== 接诊列表 =====
const encounterList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const patientOptions = ref([])
const registerOptions = ref([])

const isDoctorUser = computed(() => {
  return userStore.roles?.some(r => r === 'doctor' || r.roleKey === 'doctor' || r.roleId === 5) || false
})

const percent = (part, whole) => whole ? Math.round((part / whole) * 100) : 0
const countStatus = (status) => encounterList.value.filter(item => item.encounterStatus === status).length
const encounterDashboard = computed(() => {
  const rows = encounterList.value || []
  const listTotal = rows.length
  const waiting = countStatus('待接诊')
  const active = countStatus('接诊中')
  const done = countStatus('就诊已完成')
  const canceled = countStatus('已取消就诊')
  const urgent = rows.filter(item => item.encounterType === '急诊').length
  const aiTurns = aiMessages.value.filter(item => item.role === 'assistant').length
  const doneRate = percent(done, listTotal)
  return {
    focusValue: doneRate + '%',
    metrics: [
      { label: '当前列表接诊', value: listTotal, note: `接口总数 ${total.value || listTotal}`, icon: UserFilled, progress: Math.min(listTotal * 8, 100) },
      { label: '待接诊队列', value: waiting, note: waiting ? '需要尽快处理' : '暂无积压', icon: Clock, state: waiting ? 'warn' : 'good', progress: percent(waiting, listTotal) },
      { label: '接诊中', value: active, note: active ? '正在诊疗' : '空闲可接诊', icon: TrendCharts, progress: percent(active, listTotal) },
      { label: '已完成', value: done, note: `完成率 ${doneRate}%`, icon: CircleCheckFilled, state: 'good', progress: doneRate }
    ],
    lanes: [
      { label: '待接诊', value: waiting, progress: percent(waiting, listTotal) },
      { label: '接诊中', value: active, progress: percent(active, listTotal) },
      { label: '已完成', value: done, progress: doneRate },
      { label: '已取消', value: canceled, progress: percent(canceled, listTotal) }
    ],
    chips: [
      { label: '急诊关注', value: urgent, type: urgent ? 'danger' : 'success' },
      { label: 'AI回复', value: aiTurns, type: aiTurns ? 'success' : 'info' },
      { label: '当前医生', value: isDoctorUser.value ? '本人视角' : '全院视角', type: 'info' }
    ]
  }
})

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, registerId: undefined, patientId: undefined, doctorId: undefined, deptId: undefined, visitTime: undefined, finishTime: undefined },
  rules: { registerId: [{ required: true, message: "挂号ID不能为空" }], patientId: [{ required: true, message: "患者ID不能为空" }], doctorId: [{ required: true, message: "医生ID不能为空" }], deptId: [{ required: true, message: "科室ID不能为空" }] }
})
const { queryParams, form, rules } = toRefs(data)

function formatPatientLabel(item) { return item?.phone ? item.name + " / " + item.phone : (item?.name || item?.patientId || "") }
function formatRegisterLabel(item) { return item?.registerNo ? item.registerNo : item?.registerId || "" }
function getPatientOptions() { listPatient({ pageNum: 1, pageSize: 1000 }).then(r => patientOptions.value = r.rows || []) }
function getRegisterOptions() { listRegister({ pageNum: 1, pageSize: 1000, registerStatus: "registered" }).then(r => registerOptions.value = r.rows || []) }

function handleRegisterChange(id) {
  if (!id) return
  const reg = registerOptions.value.find(i => i.registerId === id)
  if (reg?.patientId) form.value.patientId = reg.patientId
}
function getList() { loading.value = true; listEncounter(queryParams.value).then(r => { encounterList.value = r.rows; total.value = r.total; loading.value = false }) }
function cancel() { open.value = false; reset() }
function reset() {
  form.value = { encounterId: null, registerId: null, patientId: null, doctorId: null, deptId: null, encounterType: null, encounterStatus: null, visitTime: null, finishTime: null }
  proxy.resetForm("encounterRef")
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function handleSelectionChange(sel) { ids.value = sel.map(i => i.encounterId); single.value = sel.length != 1; multiple.value = !sel.length }
async function handleAdd() {
  reset()
  getPatientOptions()
  getRegisterOptions()
  if (isDoctorUser.value) {
    form.value.deptId = userStore.deptId || form.value.deptId
    try {
      const res = await getMyDoctorInfo()
      const doctor = res.data || {}
      if (doctor.doctorId) {
        form.value.doctorId = doctor.doctorId
        form.value.deptId = doctor.deptId || userStore.deptId || form.value.deptId
      }
    } catch {
      const userId = userStore.id
      if (userId) {
        try {
          const res = await getDoctorByUserId(userId)
          const doctor = res.data || {}
          if (doctor.doctorId) {
            form.value.doctorId = doctor.doctorId
            form.value.deptId = doctor.deptId || userStore.deptId || form.value.deptId
          }
        } catch {}
      }
    }
  }
  open.value = true
  title.value = "添加接诊"
}
function handleUpdate(row) {
  reset(); getPatientOptions(); getRegisterOptions()
  getEncounter(row.encounterId || ids.value).then(r => { form.value = r.data; open.value = true; title.value = "修改接诊" })
}
function submitForm() {
  proxy.$refs["encounterRef"].validate(v => {
    if (!v) return
    const act = form.value.encounterId != null ? updateEncounter : addEncounter
    act(form.value).then(() => { proxy.$modal.msgSuccess("操作成功"); open.value = false; getList() })
  })
}
function handleDelete(row) {
  proxy.$modal.confirm('确认删除该接诊记录？').then(() => delEncounter(row.encounterId || ids.value)).then(() => { getList(); proxy.$modal.msgSuccess("删除成功") }).catch(() => {})
}
function handleExport() { proxy.download('emr/encounter/export', { ...queryParams.value }, 'encounter_' + new Date().getTime() + '.xlsx') }
function statusTag(s) { const m = { '待接诊': 'warning', '接诊中': 'success', '就诊已完成': 'info', '已取消就诊': 'danger' }; return m[s] || 'info' }

// ==================== AI 医疗助手 ====================
const aiDrawer = ref(false)
const aiInput = ref('')
const aiThinking = ref(false)
const aiMessages = ref([])
const currentPatient = ref(null)
const chatArea = ref(null)
const generatingRecord = ref(false)
const aiSessionId = ref('')  // 会话ID，用于多轮对话

const aiConfigured = computed(() => {
  return true // 后端已配置 DeepSeek API Key
})

function openAIAssistant(row) {
  currentPatient.value = row
  aiMessages.value = []
  aiInput.value = ''
  aiSessionId.value = ''  // 新会话
  aiDrawer.value = true
  nextTick(() => scrollChat())
}

function openAIAssistantFirst() {
  if (encounterList.value.length > 0) {
    openAIAssistant(encounterList.value[0])
  } else {
    ElMessage.info('暂无可接诊记录')
  }
}

function aiQuickAsk(question) {
  aiInput.value = question
  aiSend()
}

async function aiSend() {
  const msg = aiInput.value.trim()
  if (!msg || aiThinking.value) return
  aiInput.value = ''

  const now = new Date().toLocaleTimeString()
  aiMessages.value.push({ role: 'user', content: msg, time: now })

  aiThinking.value = true
  await nextTick()
  scrollChat()

  try {
    const patientId = currentPatient.value?.patientId
    const result = await aiChat({
      message: msg,
      sessionId: aiSessionId.value || undefined,
      patientId: patientId || undefined,
      mode: 'doctor'
    })

    // 保存后端返回的 sessionId 以支持多轮对话
    if (result.sessionId) {
      aiSessionId.value = result.sessionId
    }

    // 尝试解析AI回复中的JSON结构化数据
    let aiResult = null
    let displayContent = result.reply || ''
    try {
      const parsed = JSON.parse(displayContent)
      if (parsed && typeof parsed === 'object' && !Array.isArray(parsed)) {
        aiResult = parsed
        displayContent = parsed.chiefComplaint || parsed.reasoning || displayContent
      }
    } catch {
      // 不是JSON，直接使用文本回复
    }

    aiMessages.value.push({
      role: 'assistant',
      content: displayContent,
      time: new Date().toLocaleTimeString(),
      aiResult: aiResult,
      hasMedicalRecord: result.hasMedicalRecord
    })

    // 提示是否有病历上下文
    if (result.hasMedicalRecord) {
      ElMessage.success('AI 已结合患者既往病历进行分析')
    }
  } catch (e) {
    console.error('[AI助手] 调用失败:', e)
    aiMessages.value.push({
      role: 'assistant',
      content: '⚠️ ' + (e.message || 'AI服务调用失败，请稍后重试'),
      time: new Date().toLocaleTimeString()
    })
  } finally {
    aiThinking.value = false
    await nextTick()
    scrollChat()
  }
}

function formatMsgContent(text) {
  if (!text) return ''
  return text
    // 清理 markdown 格式符号
    .replace(/###\s*/g, '')
    .replace(/##\s*/g, '')
    .replace(/---+/g, '')
    .replace(/\*\*(.+?)\*\*/g, '$1')
    .replace(/\*(.+?)\*/g, '$1')
    .replace(/`{3}[\s\S]*?`{3}/g, '')
    .replace(/`(.+?)`/g, '$1')
    // 换行处理
    .replace(/\n\n/g, '<br><br>')
    .replace(/\n/g, '<br>')
}

function scrollChat() {
  nextTick(() => {
    if (chatArea.value) chatArea.value.scrollTop = chatArea.value.scrollHeight
  })
}

// ==================== AI生成病历 ====================
const previewOpen = ref(false)
const previewRecord = reactive({
  encounterId: null, chiefComplaint: '', presentIllness: '', pastHistory: '',
  allergyHistory: '', physicalExam: '', diagnosisOpinion: '', treatmentPlan: '', doctorAdvice: ''
})

async function aiGenerateFullRecord() {
  if (!currentPatient.value) return

  // 收集对话中的问诊信息
  const messages = aiMessages.value
  const userMessages = messages.filter(m => m.role === 'user').map(m => m.content)
  const aiReplies = messages.filter(m => m.role === 'assistant').map(m => m.content)
  const conversationText = userMessages.join('；') + '\nAI回复摘要：' + aiReplies.join('；')

  if (userMessages.length === 0) {
    ElMessage.warning('请先输入患者症状描述，再生成病历')
    return
  }

  generatingRecord.value = true

  const recordPrompt = `请根据以下问诊对话，生成一份完整的电子病历JSON。要求直接输出纯JSON（不要markdown包裹），字段名使用英文camelCase：

患者信息：${currentPatient.value.patientName || ''}，${currentPatient.value.encounterType || '普通门诊'}
问诊记录：${conversationText}

JSON格式：
{
  "chiefComplaint": "...",
  "presentIllness": "...",
  "pastHistory": "...",
  "allergyHistory": "...",
  "physicalExam": "...",
  "diagnosisOpinion": "...",
  "treatmentPlan": "...",
  "doctorAdvice": "..."
}`

  try {
    const patientId = currentPatient.value?.patientId
    const result = await aiChat({
      message: recordPrompt,
      sessionId: aiSessionId.value || undefined,
      patientId: patientId || undefined,
      mode: 'doctor'
    })

    if (result.sessionId) {
      aiSessionId.value = result.sessionId
    }

    // 解析AI返回的JSON
    let data = null
    try {
      data = JSON.parse(result.reply)
    } catch {
      // 尝试从文本中提取JSON
      const jsonMatch = result.reply.match(/\{[\s\S]*\}/)
      if (jsonMatch) {
        try { data = JSON.parse(jsonMatch[0]) } catch {}
      }
    }

    if (data && data.chiefComplaint) {
      previewRecord.encounterId = currentPatient.value.encounterId
      previewRecord.chiefComplaint = data.chiefComplaint || ''
      previewRecord.presentIllness = data.presentIllness || ''
      previewRecord.pastHistory = data.pastHistory || ''
      previewRecord.allergyHistory = data.allergyHistory || ''
      previewRecord.physicalExam = data.physicalExam || ''
      previewRecord.diagnosisOpinion = data.diagnosisOpinion || ''
      previewRecord.treatmentPlan = data.treatmentPlan || ''
      previewRecord.doctorAdvice = data.doctorAdvice || ''

      aiMessages.value.push({
        role: 'assistant',
        content: '✅ 已生成电子病历草稿，请查看预览并确认保存。',
        time: new Date().toLocaleTimeString()
      })
      ElMessage.success('病历草稿已生成，请确认')
      previewOpen.value = true
    } else {
      // AI返回的不是JSON，显示原始回复
      aiMessages.value.push({
        role: 'assistant',
        content: '📋 病历草稿：\n\n' + result.reply,
        time: new Date().toLocaleTimeString()
      })
      ElMessage.info('AI已生成病历草稿，请手动整理后保存')
      // 也打开预览窗让医生手动填写
      previewRecord.encounterId = currentPatient.value.encounterId
      previewOpen.value = true
    }
  } catch (e) {
    ElMessage.error('病历生成失败：' + (e.message || '请检查后端AI服务'))
    aiMessages.value.push({
      role: 'assistant',
      content: '❌ 病历生成失败：' + (e.message || '请稍后重试'),
      time: new Date().toLocaleTimeString()
    })
  } finally {
    generatingRecord.value = false
    await nextTick(); scrollChat()
  }
}

async function saveRecord() {
  if (!previewRecord.encounterId) { ElMessage.warning('缺接诊ID'); return }
  try {
    await saveRecordApi({ ...previewRecord })
    ElMessage.success('病历保存成功')
    previewOpen.value = false
    // 更新接诊状态
    await updateEncounter({ encounterId: previewRecord.encounterId, encounterStatus: '就诊已完成' })
    getList()
  } catch { ElMessage.error('保存失败') }
}

getList(); getPatientOptions()
</script>

<style scoped lang="scss">
.encounter-page {
  .search-panel { background: #F8FAFC; border-radius: 12px; padding: 20px 16px 4px; margin-bottom: 14px; border: 1px solid #E2E8F0; }
  .toolbar-row { margin-bottom: 14px; }
  .table-wrap {
    border-radius: 12px; overflow: hidden; border: 1px solid #E2E8F0;
    :deep(.el-table) {
      font-size: 13px;
      th.el-table__cell { background: #F1F5F9; color: #475569; font-weight: 600; font-size: 12px; padding: 10px 0; }
      td.el-table__cell { padding: 8px 0; }
    }
  }
}

/* ===== AI 抽屉 ===== */
.ai-drawer {
  :deep(.el-drawer__header) { margin-bottom: 0; padding: 16px 20px; border-bottom: 1px solid #F1F5F9; }
  :deep(.el-drawer__body) { padding: 0; display: flex; flex-direction: column; height: calc(100% - 65px); }
}

.ai-drawer-header {
  display: flex; align-items: center; gap: 12px;
  .ai-header-icon { width: 40px; height: 40px; border-radius: 10px; background: linear-gradient(135deg, #1A6B54, #0F4C3A); display: flex; align-items: center; justify-content: center; color: #fff; }
  .ai-header-text { flex: 1; .ai-title { font-size: 17px; font-weight: 700; color: #1E293B; display: block; } .ai-subtitle { font-size: 12px; color: #94A3B8; } }
}

.ai-drawer-body {
  flex: 1; display: flex; flex-direction: column; overflow: hidden;
  &.ai-empty { justify-content: center; align-items: center; color: #94A3B8; }
}

.ai-patient-card {
  display: flex; align-items: center; gap: 12px; padding: 14px 20px; background: linear-gradient(135deg, #E8F3EF, #F8FAFC); border-bottom: 1px solid #F1F5F9;
  .ai-patient-avatar { width: 44px; height: 44px; border-radius: 50%; background: #1A6B54; display: flex; align-items: center; justify-content: center; color: #fff; }
  .ai-patient-name { font-size: 15px; font-weight: 600; color: #1E293B; display: flex; align-items: center; gap: 8px; }
  .ai-patient-meta { font-size: 12px; color: #64748B; margin-top: 2px; display: flex; gap: 16px; }
}

.ai-quick-actions {
  display: flex; flex-wrap: wrap; gap: 8px; padding: 12px 20px; border-bottom: 1px solid #F1F5F9;
}

.ai-chat-area {
  flex: 1; overflow-y: auto; padding: 16px 20px; background: #F8FAFC;
  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-thumb { background: #CBD5E1; border-radius: 2px; }
}

.ai-welcome { text-align: center; padding: 60px 20px; color: #94A3B8; .el-icon { margin-bottom: 12px; } p { margin: 0; &.hint { font-size: 12px; margin-top: 4px; } } }

.ai-message {
  display: flex; gap: 10px; margin-bottom: 16px;
  .msg-avatar { width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
  &.msg-user { flex-direction: row-reverse; .msg-avatar { background: #1A6B54; color: #fff; } .msg-body { align-items: flex-end; } }
  &.msg-assistant { .msg-avatar { background: #E2E8F0; color: #475569; } }
}

.msg-body {
  display: flex; flex-direction: column; max-width: 85%;
  .msg-content { background: #FFFFFF; padding: 10px 14px; border-radius: 12px; font-size: 13px; line-height: 1.6; color: #334155; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
  .msg-time { font-size: 11px; color: #94A3B8; margin-top: 4px; }
}

.msg-user .msg-body .msg-content { background: #1A6B54; color: #FFFFFF; }

.msg-record-tip {
  font-size: 11px; color: #1A6B54; background: #DCFCE7; padding: 4px 10px; border-radius: 6px; margin-top: 4px; display: inline-block;
}

.msg-thinking {
  display: flex; gap: 6px; padding: 14px 20px; background: #FFF; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.06);
  span { width: 8px; height: 8px; border-radius: 50%; background: #CBD5E1; animation: bounce 1.4s ease-in-out infinite both; }
  span:nth-child(1) { animation-delay: -0.32s; } span:nth-child(2) { animation-delay: -0.16s; }
}
@keyframes bounce { 0%, 80%, 100% { transform: scale(0); } 40% { transform: scale(1); } }

/* AI结果卡片 */
.ai-result-card {
  margin-top: 8px; padding: 12px; background: #F0FDF4; border-radius: 10px; border-left: 3px solid #1A6B54;
  .result-urgent { margin-bottom: 8px; }
  .result-item { display: flex; gap: 8px; margin-bottom: 6px; &:last-child { margin-bottom: 0; } }
  .r-label { font-size: 11px; font-weight: 700; color: #1A6B54; white-space: nowrap; padding-top: 2px; min-width: 56px; }
  .r-val { font-size: 12px; color: #334155; line-height: 1.5; }
}

/* 输入区 */
.ai-input-area {
  padding: 12px 20px; border-top: 1px solid #F1F5F9; background: #FFF; display: flex; gap: 10px; align-items: flex-end;
  :deep(.el-textarea__inner) { font-size: 13px; }
  .ai-send-btn { flex-shrink: 0; }
}
</style>
