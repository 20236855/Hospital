<template>
  <div class="schedule-page">
    <div class="schedule-header">
      <div>
        <div class="page-title">智能会诊排班</div>
        <div class="page-subtitle">
          {{ weekRangeText }} · 接诊医生 role_id=5 · 每周日自动生成下周排班
        </div>
      </div>
      <div class="header-actions">
        <el-button icon="Refresh" @click="loadCurrentWeek">本周排班</el-button>
        <el-button v-if="isAdminUser" type="primary" plain icon="View" @click="handlePreview">预览下周</el-button>
        <el-button v-if="isAdminUser" type="success" icon="MagicStick" :loading="generating" @click="handleGenerate">一键生成下周</el-button>
        <el-button v-if="isAdminUser" type="warning" plain icon="Promotion" :disabled="!agentResult.weekStart" @click="handlePublish">同步公示</el-button>
      </div>
    </div>

    <el-row :gutter="14" class="metric-row">
      <el-col :xs="12" :sm="6">
        <div class="metric-card">
          <span class="metric-label">接诊医生</span>
          <strong>{{ agentResult.doctorCount || 0 }}</strong>
          <span>role_id=5</span>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="metric-card">
          <span class="metric-label">覆盖科室</span>
          <strong>{{ agentResult.deptCount || 0 }}</strong>
          <span>按科室兜底</span>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="metric-card">
          <span class="metric-label">{{ agentResult.preview ? '预生成班次' : '新增班次' }}</span>
          <strong>{{ agentResult.insertedCount || 0 }}</strong>
          <span>上午/下午</span>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="metric-card danger">
          <span class="metric-label">兜底提醒</span>
          <strong>{{ warningCount }}</strong>
          <span>单医生科室</span>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="14" class="main-grid">
      <el-col :xs="24" :lg="isAdminUser ? 17 : 24">
        <div class="panel schedule-board">
          <div class="panel-title">
            <span>周排班视图</span>
            <el-tag :type="agentResult.preview ? 'warning' : 'success'" effect="plain">
              {{ agentResult.preview ? '预览未入库' : statusText(agentResult.publishStatus) }}
            </el-tag>
          </div>
          <div v-loading="loading" class="week-grid">
            <div v-for="day in weekDays" :key="day.date" class="day-column">
              <div class="day-head">
                <strong>{{ day.week }}</strong>
                <span>{{ day.date }}</span>
              </div>
              <div v-for="slot in slotOptions" :key="slot.value" class="slot-section">
                <div class="slot-title">{{ slot.label }}</div>
                <div v-if="daySlotItems(day.date, slot.value).length" class="doctor-list">
                  <div
                    v-for="item in daySlotItems(day.date, slot.value)"
                    :key="`${item.scheduleId || 'new'}-${item.doctorId}-${item.timeSlot}-${item.scheduleDate}`"
                    class="doctor-card"
                    :class="{ generated: item.generated }"
                  >
                    <div class="doctor-main">
                      <span class="doctor-name">{{ item.doctorName || ('医生' + item.doctorId) }}</span>
                      <el-tag size="small" effect="plain">{{ item.levelName || levelName(item.levelId) }}</el-tag>
                    </div>
                    <div class="doctor-meta">
                      <span>{{ item.deptName || ('科室' + item.deptId) }}</span>
                      <span>{{ item.reservedNumber || 0 }}/{{ item.maxNumber || 0 }}</span>
                    </div>
                    <div v-if="item.reason" class="doctor-reason">{{ item.reason }}</div>
                  </div>
                </div>
                <el-empty v-else description="未排班" :image-size="36" />
              </div>
            </div>
          </div>
        </div>
      </el-col>

      <el-col v-if="isAdminUser" :xs="24" :lg="7">
        <div class="panel agent-console">
          <div class="panel-title">
            <span>智能体工作台</span>
            <el-button link type="primary" icon="Refresh" @click="refreshAgentPanel">刷新</el-button>
          </div>

          <div v-if="agentRun && agentRun.runNo" class="agent-brief">
            <div>
              <strong>{{ agentRun.runNo }}</strong>
              <span>{{ triggerText(agentRun.triggerType) }} · {{ agentRun.startTime || '-' }}</span>
            </div>
            <el-tag :type="statusType(agentRun.status)" effect="plain">{{ runStatusText(agentRun.status) }}</el-tag>
          </div>
          <el-empty v-else description="暂无运行记录" :image-size="58" />

          <el-tabs class="agent-tabs">
            <el-tab-pane label="运行状态">
              <div v-if="agentRun && agentRun.runNo" class="agent-tab-body">
                <div class="model-line compact">
                  <span>模型</span>
                  <strong>{{ agentRun.modelProvider || 'rules' }}/{{ agentRun.modelName || 'deterministic-rule-engine' }}</strong>
                </div>
                <div class="stage-flow compact">
                  <div
                    v-for="step in lifecycleSteps"
                    :key="step.value"
                    class="stage-item"
                    :class="{ done: step.done, active: step.active, failed: step.failed }"
                  >
                    <span class="stage-dot"></span>
                    <span>{{ step.label }}</span>
                  </div>
                </div>
                <div class="summary-card">
                  <label>感知</label>
                  <p>{{ agentRun.perceptionSummary || '等待智能体感知业务数据' }}</p>
                </div>
                <div class="summary-card">
                  <label>推理</label>
                  <p>{{ agentRun.reasoningSummary || '等待AI结合规则和历史数据推理' }}</p>
                </div>
                <div class="summary-card">
                  <label>执行与监控</label>
                  <p>{{ agentRun.actionSummary || '等待生成排班并入库' }}</p>
                  <p v-if="agentRun.warningSummary" class="warning-text">{{ agentRun.warningSummary }}</p>
                  <p v-if="agentRun.errorMessage" class="error-text">{{ agentRun.errorMessage }}</p>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="推理报告">
              <div class="agent-tab-body">
                <el-alert
                  v-for="(warning, index) in agentResult.warnings"
                  :key="index"
                  :title="warning"
                  type="warning"
                  show-icon
                  :closable="false"
                  class="warning-alert"
                />
                <div v-if="agentResult.reports && agentResult.reports.length" class="report-list compact">
                  <div v-for="(item, index) in agentResult.reports" :key="index" class="report-item compact">
                    <span class="report-index">{{ index + 1 }}</span>
                    <p>{{ item }}</p>
                  </div>
                </div>
                <el-empty v-else description="暂无推理报告" :image-size="58" />
              </div>
            </el-tab-pane>

            <el-tab-pane label="运行历史">
              <div class="agent-tab-body run-history compact">
                <div v-for="run in runHistory" :key="run.runId || run.runNo" class="history-item">
                  <div>
                    <strong>{{ run.weekStart }} 至 {{ run.weekEnd }}</strong>
                    <span>{{ triggerText(run.triggerType) }} · {{ run.insertedCount || 0 }}条入库</span>
                  </div>
                  <el-tag size="small" :type="statusType(run.status)" effect="plain">{{ runStatusText(run.status) }}</el-tag>
                </div>
                <el-empty v-if="!runHistory.length" description="暂无历史运行" :image-size="58" />
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-col>
    </el-row>

    <div class="panel detail-panel">
      <div class="panel-title">
        <span>排班明细与人工微调</span>
        <div>
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hisdoctor:schedule:add']">新增</el-button>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['hisdoctor:schedule:export']">导出</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="tableList" @selection-change="handleSelectionChange" height="360">
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column label="日期" align="center" prop="scheduleDate" width="110" />
        <el-table-column label="午别" align="center" prop="timeSlot" width="90">
          <template #default="scope">{{ slotName(scope.row.timeSlot) }}</template>
        </el-table-column>
        <el-table-column label="科室" align="center" prop="deptName" min-width="100" />
        <el-table-column label="医生" align="center" prop="doctorName" min-width="100" />
        <el-table-column label="级别" align="center" prop="levelName" min-width="90" />
        <el-table-column label="容量" align="center" width="110">
          <template #default="scope">{{ scope.row.reservedNumber || 0 }}/{{ scope.row.maxNumber || 0 }}</template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === '1' ? 'danger' : 'success'" effect="plain">
              {{ scope.row.status === '1' ? '已满' : '可预约' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="来源" align="center" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.generated ? 'warning' : 'info'" effect="plain">
              {{ scope.row.generated ? '预生成' : '已入库' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="180" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.scheduleId"
              link
              type="primary"
              icon="Edit"
              @click="handleUpdate(scope.row)"
              v-hasPermi="['hisdoctor:schedule:edit']"
            >微调</el-button>
            <el-button
              v-if="scope.row.scheduleId"
              link
              type="danger"
              icon="Delete"
              @click="handleDelete(scope.row)"
              v-hasPermi="['hisdoctor:schedule:remove']"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog :title="title" v-model="open" width="520px" append-to-body>
      <el-form ref="scheduleRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="医生ID" prop="doctorId">
          <el-input v-model="form.doctorId" placeholder="请输入医生ID" :disabled="isDoctorUser" />
        </el-form-item>
        <el-form-item label="排班日期" prop="scheduleDate">
          <el-date-picker
            v-model="form.scheduleDate"
            clearable
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择排班日期"
          />
        </el-form-item>
        <el-form-item label="午别" prop="timeSlot">
          <el-select v-model="form.timeSlot" placeholder="请选择午别" style="width: 100%">
            <el-option v-for="slot in slotOptions" :key="slot.value" :label="slot.label" :value="slot.value" />
            <el-option label="夜间" value="evening" />
          </el-select>
        </el-form-item>
        <el-form-item label="最大挂号数" prop="maxNumber">
          <el-input-number v-model="form.maxNumber" :min="1" :max="99" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="已预约人数" prop="reservedNumber">
          <el-input-number v-model="form.reservedNumber" :min="0" :max="99" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">可预约</el-radio>
            <el-radio label="1">已满</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Schedule">
import useUserStore from '@/store/modules/user'
import { getDoctorByUserId } from "@/api/hisdoctor/doctor"
import {
  getSchedule,
  delSchedule,
  addSchedule,
  updateSchedule,
  currentWeekSchedule,
  previewNextWeekSchedule,
  generateNextWeekSchedule,
  publishWeekSchedule,
  getAgentScheduleStatus,
  listAgentScheduleRuns
} from "@/api/hisdoctor/schedule"

const { proxy } = getCurrentInstance()
const userStore = useUserStore()

const loading = ref(false)
const generating = ref(false)
const open = ref(false)
const title = ref("")
const ids = ref([])
const currentDoctorId = ref(null)
const agentRun = ref(null)
const runHistory = ref([])
const agentResult = ref({
  schedules: [],
  reports: [],
  warnings: []
})

const isAdminUser = computed(() => {
  return String(userStore.id) === '1'
    || userStore.roles.includes('admin')
    || userStore.permissions.includes('*:*:*')
})
const isDoctorUser = computed(() => !isAdminUser.value && (userStore.userType === 'doctor' || userStore.roles.includes('doctor')))
const warningCount = computed(() => (agentResult.value.warnings || []).length + (agentResult.value.fallbackCount || 0))
const tableList = computed(() => agentResult.value.schedules || [])
const weekRangeText = computed(() => {
  if (!agentResult.value.weekStart || !agentResult.value.weekEnd) {
    return '排班周未加载'
  }
  return `${agentResult.value.weekStart} 至 ${agentResult.value.weekEnd}`
})
const weekDays = computed(() => buildWeekDays(agentResult.value.weekStart))
const lifecycleSteps = computed(() => buildLifecycleSteps(agentRun.value))

const slotOptions = [
  { label: '上午', value: 'morning' },
  { label: '下午', value: 'afternoon' }
]

const data = reactive({
  form: {},
  rules: {
    doctorId: [{ required: true, message: "医生ID不能为空", trigger: "blur" }],
    scheduleDate: [{ required: true, message: "排班日期不能为空", trigger: "change" }],
    timeSlot: [{ required: true, message: "午别不能为空", trigger: "change" }],
    maxNumber: [{ required: true, message: "最大挂号数不能为空", trigger: "blur" }]
  }
})

const { form, rules } = toRefs(data)

function loadCurrentWeek() {
  loading.value = true
  currentWeekSchedule().then(response => {
    agentResult.value = normalizeResult(response.data)
    loadAgentMonitor()
  }).finally(() => {
    loading.value = false
  })
}

function loadAgentMonitor() {
  if (!isAdminUser.value) {
    agentRun.value = null
    runHistory.value = []
    return Promise.resolve()
  }
  Promise.all([getAgentScheduleStatus(), listAgentScheduleRuns()]).then(([statusResponse, runsResponse]) => {
    agentRun.value = statusResponse.data || null
    runHistory.value = runsResponse.data || []
  })
}

function refreshAgentPanel() {
  if (!isAdminUser.value) {
    return
  }
  loadAgentMonitor()
  currentWeekSchedule().then(response => {
    agentResult.value = normalizeResult(response.data)
  })
}

function handlePreview() {
  loading.value = true
  previewNextWeekSchedule().then(response => {
    agentResult.value = normalizeResult(response.data)
    proxy.$modal.msgSuccess("下周智能排班预览已生成")
    loadAgentMonitor()
  }).finally(() => {
    loading.value = false
  })
}

function handleGenerate() {
  proxy.$modal.confirm("确认生成下周一至周日完整会诊排班并写入数据库？").then(() => {
    generating.value = true
    return generateNextWeekSchedule()
  }).then(response => {
    agentResult.value = normalizeResult(response.data)
    proxy.$modal.msgSuccess("下周智能排班已生成")
    loadAgentMonitor()
  }).finally(() => {
    generating.value = false
  }).catch(() => {})
}

function handlePublish() {
  publishWeekSchedule(agentResult.value.weekStart).then(response => {
    agentResult.value = normalizeResult(response.data)
    proxy.$modal.msgSuccess("排班已同步公示")
    loadAgentMonitor()
  })
}

function daySlotItems(date, slot) {
  return tableList.value.filter(item => item.scheduleDate === date && item.timeSlot === slot)
}

function buildWeekDays(weekStart) {
  if (!weekStart) {
    return []
  }
  const start = new Date(`${weekStart}T00:00:00`)
  const names = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  return names.map((week, index) => {
    const date = new Date(start)
    date.setDate(start.getDate() + index)
    return {
      week,
      date: formatDate(date)
    }
  })
}

function formatDate(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function normalizeResult(result) {
  const data = result || {}
  data.schedules = (data.schedules || []).map(item => ({
    ...item,
    scheduleDate: typeof item.scheduleDate === 'string' ? item.scheduleDate.slice(0, 10) : item.scheduleDate
  }))
  data.reports = data.reports || []
  data.warnings = data.warnings || []
  return data
}

function slotName(slot) {
  const match = slotOptions.find(item => item.value === slot)
  if (match) return match.label
  if (slot === 'evening') return '夜间'
  return slot
}

function levelName(levelId) {
  if (levelId === 3) return '主任号'
  if (levelId === 2) return '专家号'
  if (levelId === 1) return '普通号'
  return '未分级'
}

function statusText(status) {
  if (status === 'generated') return '已生成'
  if (status === 'published') return '已公示'
  return '草稿'
}

function buildLifecycleSteps(run) {
  const steps = [
    { label: '定时唤醒', value: 'awakened' },
    { label: '感知数据', value: 'perceiving' },
    { label: 'AI推理', value: 'reasoning' },
    { label: '执行入库', value: 'executing' },
    { label: '监控告警', value: 'monitoring' },
    { label: '完成沉淀', value: 'completed' }
  ]
  if (!run || !run.stage) {
    return steps
  }
  const currentIndex = steps.findIndex(item => item.value === run.stage)
  const completedIndex = run.stage === 'alerting' ? steps.length - 2 : (currentIndex >= 0 ? currentIndex : 0)
  return steps.map((item, index) => ({
    ...item,
    done: run.stage === 'completed' ? true : index < completedIndex,
    active: item.value === run.stage || (run.stage === 'alerting' && item.value === 'monitoring'),
    failed: run.status === 'failed' && (item.value === run.stage || (run.stage === 'alerting' && item.value === 'monitoring'))
  }))
}

function triggerText(triggerType) {
  if (triggerType === 'scheduled') return '每周日自动唤醒'
  if (triggerType === 'manual') return '人工触发'
  return triggerType || '未知触发'
}

function runStatusText(status) {
  if (status === 'success') return '成功'
  if (status === 'warning') return '有告警'
  if (status === 'failed') return '失败'
  if (status === 'running') return '运行中'
  return status || '未运行'
}

function statusType(status) {
  if (status === 'success') return 'success'
  if (status === 'warning') return 'warning'
  if (status === 'failed') return 'danger'
  if (status === 'running') return 'primary'
  return 'info'
}

function reset() {
  form.value = {
    scheduleId: null,
    doctorId: isDoctorUser.value ? currentDoctorId.value : null,
    scheduleDate: null,
    timeSlot: 'morning',
    maxNumber: 15,
    reservedNumber: 0,
    status: '0'
  }
  proxy.resetForm("scheduleRef")
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.scheduleId).filter(Boolean)
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新增会诊排班"
}

function handleUpdate(row) {
  reset()
  getSchedule(row.scheduleId).then(response => {
    form.value = response.data
    if (typeof form.value.scheduleDate === 'string') {
      form.value.scheduleDate = form.value.scheduleDate.slice(0, 10)
    }
    if (isDoctorUser.value) {
      form.value.doctorId = currentDoctorId.value
    }
    open.value = true
    title.value = "人工微调排班"
  })
}

function submitForm() {
  proxy.$refs["scheduleRef"].validate(valid => {
    if (!valid) return
    if (isDoctorUser.value) {
      form.value.doctorId = currentDoctorId.value
    }
    const request = form.value.scheduleId != null ? updateSchedule(form.value) : addSchedule(form.value)
    request.then(() => {
      proxy.$modal.msgSuccess(form.value.scheduleId != null ? "微调成功" : "新增成功")
      open.value = false
      loadCurrentWeek()
    })
  })
}

function cancel() {
  open.value = false
  reset()
}

function handleDelete(row) {
  const scheduleIds = row.scheduleId || ids.value
  proxy.$modal.confirm(`是否确认删除排班编号为"${scheduleIds}"的数据项？`).then(() => {
    return delSchedule(scheduleIds)
  }).then(() => {
    loadCurrentWeek()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hisdoctor/schedule/export', {}, `schedule_${new Date().getTime()}.xlsx`)
}

function loadCurrentDoctor() {
  if (!isDoctorUser.value) {
    loadCurrentWeek()
    return
  }
  getDoctorByUserId(userStore.id).then(response => {
    const doctor = response.data
    currentDoctorId.value = doctor ? doctor.doctorId : null
    loadCurrentWeek()
  })
}

loadCurrentDoctor()
loadAgentMonitor()
</script>

<style scoped>
.schedule-page {
  padding: 18px;
  background: #f5f7fb;
  min-height: calc(100vh - 84px);
  overflow-x: hidden;
}

.schedule-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #172033;
}

.page-subtitle {
  margin-top: 6px;
  color: #667085;
  font-size: 13px;
}

.header-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.metric-row {
  margin-bottom: 14px;
}

.metric-row .el-col,
.main-grid .el-col {
  margin-bottom: 14px;
}

.metric-card,
.panel {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 8px 22px rgba(15, 23, 42, 0.05);
  box-sizing: border-box;
  min-width: 0;
}

.metric-card {
  min-height: 96px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  color: #667085;
}

.metric-card strong {
  font-size: 28px;
  line-height: 1;
  color: #172033;
}

.metric-card.danger strong {
  color: #d92d20;
}

.metric-label {
  font-size: 13px;
  color: #475467;
}

.main-grid {
  margin-bottom: 14px;
}

.panel {
  padding: 16px;
  overflow: hidden;
}

.panel-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
  font-size: 16px;
  font-weight: 700;
  color: #172033;
}

.week-grid {
  display: grid;
  grid-template-columns: repeat(7, minmax(156px, 1fr));
  gap: 10px;
  overflow-x: auto;
  width: 100%;
  max-width: 100%;
  padding-bottom: 4px;
}

.day-column {
  min-width: 156px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fbfcfe;
  overflow: hidden;
}

.day-head {
  padding: 10px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
  color: #172033;
}

.day-head span {
  color: #667085;
  font-size: 12px;
}

.slot-section {
  padding: 10px;
  min-height: 180px;
}

.slot-section + .slot-section {
  border-top: 1px dashed #d0d5dd;
}

.slot-title {
  font-size: 13px;
  font-weight: 700;
  color: #344054;
  margin-bottom: 8px;
}

.doctor-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.doctor-card {
  border: 1px solid #d0d5dd;
  border-left: 4px solid #2e90fa;
  border-radius: 8px;
  padding: 9px;
  background: #fff;
  min-width: 0;
  overflow: hidden;
}

.doctor-card.generated {
  border-left-color: #f79009;
  background: #fffaf0;
}

.doctor-main,
.doctor-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
  min-width: 0;
}

.doctor-name {
  font-weight: 700;
  color: #172033;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.doctor-meta {
  margin-top: 6px;
  font-size: 12px;
  color: #667085;
}

.doctor-reason {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.45;
  color: #8a4b00;
  word-break: break-word;
}

.report-panel {
  min-height: 100%;
}

.agent-panel {
  margin-bottom: 14px;
}

.agent-status {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.run-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.run-head strong {
  display: block;
  color: #172033;
}

.run-head span,
.model-line span,
.history-item span {
  display: block;
  margin-top: 4px;
  color: #667085;
  font-size: 12px;
}

.model-line {
  padding: 10px;
  border-radius: 8px;
  background: #f8fafc;
}

.model-line strong {
  display: block;
  margin-top: 4px;
  color: #344054;
  word-break: break-all;
}

.stage-flow {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.stage-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px;
  border-radius: 8px;
  background: #f8fafc;
  color: #667085;
  font-size: 12px;
}

.stage-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #d0d5dd;
  flex: 0 0 8px;
}

.stage-item.done {
  color: #067647;
  background: #ecfdf3;
}

.stage-item.done .stage-dot {
  background: #12b76a;
}

.stage-item.active {
  color: #155eef;
  background: #eff6ff;
  font-weight: 700;
}

.stage-item.active .stage-dot {
  background: #2e90fa;
}

.stage-item.failed {
  color: #b42318;
  background: #fef3f2;
}

.stage-item.failed .stage-dot {
  background: #f04438;
}

.summary-block {
  border-top: 1px solid #eef2f6;
  padding-top: 10px;
}

.summary-block label,
.history-title {
  font-size: 13px;
  font-weight: 700;
  color: #344054;
}

.summary-block p {
  margin: 6px 0 0;
  color: #475467;
  line-height: 1.5;
  word-break: break-word;
}

.warning-text {
  color: #b54708 !important;
}

.error-text {
  color: #b42318 !important;
}

.run-history {
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid #eef2f6;
}

.history-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 9px 0;
  border-bottom: 1px solid #f2f4f7;
}

.history-item > div {
  min-width: 0;
}

.history-item strong {
  display: block;
  color: #344054;
  font-size: 13px;
  word-break: break-word;
}

.report-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.report-item {
  display: flex;
  gap: 10px;
  padding: 10px;
  border-radius: 8px;
  background: #f8fafc;
}

.report-index {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #155eef;
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  flex: 0 0 24px;
}

.report-item p {
  margin: 0;
  color: #344054;
  line-height: 1.5;
  word-break: break-word;
}

.warning-alert {
  margin-top: 10px;
}

.detail-panel {
  margin-top: 14px;
}

.agent-console {
  position: sticky;
  top: 12px;
  max-height: calc(100vh - 126px);
  display: flex;
  flex-direction: column;
}

.agent-console .panel-title {
  margin-bottom: 10px;
}

.agent-brief {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #f8fafc;
}

.agent-brief > div {
  min-width: 0;
}

.agent-brief strong {
  display: block;
  color: #172033;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.agent-brief span {
  display: block;
  margin-top: 4px;
  color: #667085;
  font-size: 12px;
  line-height: 1.35;
}

.agent-tabs {
  margin-top: 10px;
  min-height: 0;
}

.agent-tabs :deep(.el-tabs__header) {
  margin-bottom: 10px;
}

.agent-tabs :deep(.el-tabs__nav) {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.agent-tabs :deep(.el-tabs__item) {
  justify-content: center;
  padding: 0 8px;
  font-size: 13px;
}

.agent-tab-body {
  max-height: calc(100vh - 290px);
  min-height: 220px;
  overflow-y: auto;
  padding-right: 2px;
}

.model-line.compact {
  padding: 9px 10px;
  margin-bottom: 10px;
}

.stage-flow.compact {
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 6px;
  margin-bottom: 10px;
}

.stage-flow.compact .stage-item {
  padding: 7px 6px;
  font-size: 12px;
}

.summary-card {
  padding: 10px;
  border: 1px solid #eef2f6;
  border-radius: 8px;
  background: #fff;
}

.summary-card + .summary-card {
  margin-top: 8px;
}

.summary-card label {
  display: block;
  margin-bottom: 5px;
  color: #344054;
  font-size: 13px;
  font-weight: 700;
}

.summary-card p {
  margin: 0;
  color: #475467;
  font-size: 13px;
  line-height: 1.5;
  word-break: break-word;
}

.report-list.compact {
  gap: 8px;
}

.report-item.compact {
  padding: 9px;
}

.report-item.compact p {
  font-size: 13px;
  line-height: 1.45;
}

.run-history.compact {
  margin-top: 0;
  padding-top: 0;
  border-top: 0;
}

@media (max-width: 992px) {
  .schedule-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .header-actions {
    justify-content: flex-start;
  }
}

@media (max-width: 768px) {
  .schedule-page {
    padding: 12px;
  }

  .header-actions {
    width: 100%;
  }

  .header-actions .el-button {
    flex: 1 1 calc(50% - 8px);
    margin-left: 0;
  }

  .stage-flow {
    grid-template-columns: 1fr;
  }

  .agent-console {
    position: static;
    max-height: none;
  }

  .agent-tab-body {
    max-height: none;
  }

  .stage-flow.compact {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
