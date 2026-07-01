<template>
  <div class="lab-page">
    <div class="lab-header">
      <div class="header-left">
        <el-button link class="back-btn" @click="router.back()">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回申请单</span>
        </el-button>
        <div>
          <div class="eyebrow">医学检验科 / {{ config.category }}</div>
          <h1>{{ config.title }}</h1>
        </div>
      </div>
      <div class="header-actions">
        <el-button @click="loadInstrumentResult">
          <el-icon><DataLine /></el-icon>
          载入仪器结果
        </el-button>
        <el-button type="primary" :loading="aiLoading" @click="runAiAssist">
          <el-icon><Cpu /></el-icon>
          AI 辅助审核
        </el-button>
        <el-button type="success" :loading="saveLoading" @click="saveReport">
          <el-icon><DocumentChecked /></el-icon>
          保存报告
        </el-button>
      </div>
    </div>

    <div class="patient-strip">
      <div class="strip-item">
        <span>申请单</span>
        <strong>{{ applyInfo.applyId || '-' }}</strong>
      </div>
      <div class="strip-item">
        <span>挂号编号</span>
        <strong>{{ applyInfo.registerNo || applyInfo.registerId || '-' }}</strong>
      </div>
      <div class="strip-item">
        <span>患者</span>
        <strong>{{ applyInfo.patientName || '-' }}</strong>
      </div>
      <div class="strip-item">
        <span>开单医生</span>
        <strong>{{ applyInfo.doctorName || '-' }}</strong>
      </div>
      <div class="strip-item wide">
        <span>申请目的</span>
        <strong>{{ applyInfo.applyInfo || applyInfo.applyPosition || '-' }}</strong>
      </div>
    </div>

    <div class="workflow">
      <div v-for="step in workflow" :key="step.name" class="workflow-step" :class="{ active: step.active }">
        <span class="step-dot"></span>
        <div>
          <strong>{{ step.name }}</strong>
          <small>{{ step.desc }}</small>
        </div>
      </div>
    </div>

    <el-row :gutter="14" class="main-grid">
      <el-col :xs="24" :lg="16">
        <div class="panel">
          <div class="panel-head">
            <div>
              <h2>检验结果</h2>
              <p>{{ config.method }}</p>
            </div>
            <el-tag :type="abnormalCount > 0 ? 'warning' : 'success'" effect="plain">
              {{ abnormalCount > 0 ? abnormalCount + ' 项异常' : '未见异常' }}
            </el-tag>
          </div>
          <el-table :data="testRows" border stripe class="result-table">
            <el-table-column label="项目" min-width="150">
              <template #default="{ row }">
                <div class="test-name">
                  <strong>{{ row.name }}</strong>
                  <span>{{ row.code }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="结果" width="150" align="center">
              <template #default="{ row }">
                <el-input v-model="row.value" size="small" @input="row.manual = true" />
              </template>
            </el-table-column>
            <el-table-column label="单位" prop="unit" width="100" align="center" />
            <el-table-column label="参考范围" prop="range" min-width="140" align="center" />
            <el-table-column label="提示" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="flagType(row)" size="small">{{ flagText(row) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="临床意义" prop="meaning" min-width="220" show-overflow-tooltip />
          </el-table>
        </div>
      </el-col>

      <el-col :xs="24" :lg="8">
        <div class="panel side-panel">
          <div class="panel-head compact">
            <h2>标本信息</h2>
            <el-tag type="info" effect="plain">{{ sample.status }}</el-tag>
          </div>
          <el-form label-position="top" :model="sample">
            <el-form-item label="标本编号">
              <el-input v-model="sample.sampleNo" />
            </el-form-item>
            <el-form-item label="标本类型">
              <el-select v-model="sample.specimen" style="width:100%">
                <el-option v-for="item in config.specimens" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
            <el-form-item label="采集时间">
              <el-date-picker v-model="sample.collectTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
            </el-form-item>
            <el-form-item label="仪器 / 方法">
              <el-input v-model="sample.instrument" />
            </el-form-item>
            <el-form-item label="质控状态">
              <el-segmented v-model="sample.qcStatus" :options="['通过', '复查', '失控']" />
            </el-form-item>
            <el-form-item label="检验备注">
              <el-input v-model="report.remark" type="textarea" :rows="3" />
            </el-form-item>
          </el-form>
        </div>

        <div class="panel ai-panel">
          <div class="panel-head compact">
            <h2>AI 审核意见</h2>
            <el-tag :type="aiRiskType">{{ aiRiskText }}</el-tag>
          </div>
          <div class="ai-content" v-if="aiReply">{{ aiReply }}</div>
          <div class="ai-empty" v-else>
            <el-icon><Cpu /></el-icon>
            <span>点击 AI 辅助审核，结合患者申请和检验结果生成专业解释。</span>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Cpu, DataLine, DocumentChecked } from '@element-plus/icons-vue'
import { getApply, updateApply } from '@/api/hisexam/apply'
import { labAiAssist } from '@/api/hisexam/labAnalysis'
import { saveResultReport } from '@/api/hisexam/result'

const props = defineProps({
  config: {
    type: Object,
    required: true
  }
})

const route = useRoute()
const router = useRouter()
const applyInfo = reactive({})
const testRows = ref([])
const aiLoading = ref(false)
const saveLoading = ref(false)
const aiReply = ref('')
const aiRisk = ref('normal')

const sample = reactive({
  sampleNo: '',
  specimen: props.config.specimens[0],
  collectTime: '',
  instrument: props.config.instrument,
  qcStatus: '通过',
  status: '待审核'
})

const report = reactive({
  remark: '',
  reviewer: '',
  conclusion: ''
})

const workflow = computed(() => [
  { name: '申请核对', desc: applyInfo.applyId ? '已绑定申请单' : '等待申请单', active: true },
  { name: '标本接收', desc: sample.sampleNo || '未生成标本号', active: !!sample.sampleNo },
  { name: '仪器检测', desc: sample.instrument, active: testRows.value.some(row => row.value !== '') },
  { name: '结果审核', desc: abnormalCount.value > 0 ? '需关注异常项' : '可签发', active: true }
])

const abnormalCount = computed(() => testRows.value.filter(row => flagText(row) !== '正常').length)
const aiRiskText = computed(() => ({ normal: '低风险', warning: '需复核', critical: '危急值' }[aiRisk.value] || '待审核'))
const aiRiskType = computed(() => ({ normal: 'success', warning: 'warning', critical: 'danger' }[aiRisk.value] || 'info'))

function pad(n) {
  return String(n).padStart(2, '0')
}

function nowDateTime() {
  const d = new Date()
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

function newSampleNo() {
  const d = new Date()
  return `${props.config.samplePrefix}${d.getFullYear()}${pad(d.getMonth() + 1)}${pad(d.getDate())}${pad(d.getHours())}${pad(d.getMinutes())}${pad(d.getSeconds())}`
}

function initRows() {
  testRows.value = props.config.tests.map(item => ({ ...item, value: '', manual: false }))
}

function flagText(row) {
  const value = Number(row.value)
  if (!Number.isFinite(value)) return '待测'
  if (row.criticalLow != null && value < row.criticalLow) return '危急低'
  if (row.criticalHigh != null && value > row.criticalHigh) return '危急高'
  if (row.low != null && value < row.low) return '偏低'
  if (row.high != null && value > row.high) return '偏高'
  return '正常'
}

function flagType(row) {
  const flag = flagText(row)
  if (flag.includes('危急')) return 'danger'
  if (flag === '偏高' || flag === '偏低') return 'warning'
  if (flag === '待测') return 'info'
  return 'success'
}

function abnormalFlag(row) {
  const value = Number(row.value)
  if (!Number.isFinite(value)) return null
  if (row.criticalLow != null && value < row.criticalLow) return '2'
  if (row.criticalHigh != null && value > row.criticalHigh) return '1'
  if (row.low != null && value < row.low) return '2'
  if (row.high != null && value > row.high) return '1'
  return '0'
}

function buildExamResultRows(reportTime) {
  return testRows.value
    .filter(row => row.value !== '' && row.value != null)
    .map((row, index) => ({
      applyId: applyInfo.applyId,
      itemCode: row.code,
      itemName: row.name,
      resultValue: String(row.value),
      resultUnit: row.unit,
      referenceRange: row.range,
      abnormalFlag: abnormalFlag(row),
      diagnosisOpinion: aiReply.value || null,
      sort: index + 1,
      status: '1',
      reportTime,
      remark: report.remark || null
    }))
}

function loadInstrumentResult() {
  if (!sample.sampleNo) sample.sampleNo = newSampleNo()
  if (!sample.collectTime) sample.collectTime = nowDateTime()
  testRows.value.forEach(row => {
    if (!row.manual) row.value = String(row.defaultValue)
  })
  sample.status = '待审核'
  ElMessage.success('已载入模拟仪器结果')
}

async function loadApplyDetail() {
  const applyId = route.query.applyId
  if (!applyId) {
    ElMessage.warning('请先从申请单列表选择对应申请单再执行检验')
    router.back()
    return
  }
  try {
    const res = await getApply(applyId)
    Object.assign(applyInfo, res.data || {})
    if (applyInfo.examResult) {
      try {
        const saved = JSON.parse(applyInfo.examResult)
        if (saved.sample) Object.assign(sample, saved.sample)
        if (Array.isArray(saved.results)) testRows.value = saved.results
        if (saved.aiReply) aiReply.value = saved.aiReply
        if (saved.aiRisk) aiRisk.value = saved.aiRisk
        report.remark = saved.remark || ''
      } catch (e) {}
    }
  } catch (e) {
    ElMessage.error('申请单加载失败，无法执行检验')
    router.back()
  }
}

async function runAiAssist() {
  if (!testRows.value.some(row => row.value !== '')) {
    ElMessage.warning('请先录入或载入检验结果')
    return
  }
  aiLoading.value = true
  try {
    const res = await labAiAssist({
      labType: props.config.title,
      patient: applyInfo,
      sample,
      results: testRows.value.map(row => ({
        code: row.code,
        name: row.name,
        value: row.value,
        unit: row.unit,
        range: row.range,
        flag: flagText(row),
        meaning: row.meaning
      })),
      question: `请以检验科审核医生视角，对${props.config.title}结果进行专业审核，指出异常组合、可能临床意义、是否建议复查或危急值上报。`
    })
    const data = res.data || {}
    aiReply.value = data.reply || data.answer || 'AI 未返回有效内容'
    aiRisk.value = data.riskLevel || (abnormalCount.value > 0 ? 'warning' : 'normal')
    sample.status = '已AI审核'
    ElMessage.success('AI 审核完成')
  } catch (e) {
    ElMessage.error('AI 审核失败：' + (e.response?.data?.msg || e.message || '未知错误'))
  } finally {
    aiLoading.value = false
  }
}

async function saveReport() {
  if (!applyInfo.applyId) {
    ElMessage.warning('缺少申请单ID')
    return
  }
  const reportTime = nowDateTime()
  const examResults = buildExamResultRows(reportTime)
  if (!examResults.length) {
    ElMessage.warning('请先录入或载入检验结果')
    return
  }
  const payload = {
    labType: props.config.title,
    sample: { ...sample },
    results: testRows.value.map(row => ({ ...row, flag: flagText(row) })),
    abnormalCount: abnormalCount.value,
    aiReply: aiReply.value,
    aiRisk: aiRisk.value,
    remark: report.remark,
    savedAt: reportTime
  }
  saveLoading.value = true
  try {
    await saveResultReport(examResults)
    await updateApply({
      applyId: applyInfo.applyId,
      examResult: JSON.stringify(payload),
      examTime: reportTime,
      applyStatus: '已完成'
    })
    sample.status = '已签发'
    ElMessage.success('检验报告已保存到检查检验结果表')
  } catch (e) {
    ElMessage.error('保存失败：' + (e.response?.data?.msg || e.message || '未知错误'))
  } finally {
    saveLoading.value = false
  }
}

onMounted(() => {
  initRows()
  loadApplyDetail()
})
</script>

<style scoped lang="scss">
.lab-page {
  min-height: 100vh;
  padding: 18px;
  background: #f4f7f8;
  color: #10201c;
}

.lab-header,
.patient-strip,
.workflow,
.panel {
  border: 1px solid #dfe8e5;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(16, 32, 28, .05);
}

.lab-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;

  .header-left,
  .header-actions {
    display: flex;
    align-items: center;
    gap: 14px;
  }

  .header-actions {
    flex-wrap: wrap;
    justify-content: flex-end;
  }

  .back-btn {
    color: #5f746e;
  }

  .eyebrow {
    color: #5f746e;
    font-size: 12px;
    font-weight: 700;
  }

  h1 {
    margin: 4px 0 0;
    font-size: 22px;
    letter-spacing: 0;
  }
}

.patient-strip {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 10px;
  margin-top: 14px;
  padding: 12px;

  .strip-item {
    min-width: 0;
    padding: 10px;
    background: #f8fbfa;
    border: 1px solid #edf2f1;
    border-radius: 6px;

    span {
      display: block;
      color: #6b7f79;
      font-size: 12px;
    }

    strong {
      display: block;
      overflow: hidden;
      margin-top: 4px;
      font-size: 14px;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

.workflow {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  margin-top: 14px;
  padding: 12px;

  .workflow-step {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px;
    color: #75857f;

    &.active {
      color: #125f4a;
    }

    .step-dot {
      width: 10px;
      height: 10px;
      border-radius: 50%;
      background: currentColor;
    }

    strong,
    small {
      display: block;
    }

    small {
      margin-top: 2px;
      font-size: 12px;
    }
  }
}

.main-grid {
  margin-top: 14px;
}

.panel {
  margin-bottom: 14px;
  overflow: hidden;

  .panel-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 10px;
    padding: 14px 16px;
    border-bottom: 1px solid #e7efec;

    &.compact h2 {
      margin-bottom: 0;
    }

    h2 {
      margin: 0 0 4px;
      font-size: 16px;
    }

    p {
      margin: 0;
      color: #6b7f79;
      font-size: 12px;
    }
  }

  :deep(.el-form) {
    padding: 14px 16px 2px;
  }
}

.result-table {
  :deep(.el-table__cell) {
    padding: 8px 0;
  }
}

.test-name {
  display: grid;
  gap: 2px;

  span {
    color: #7b8c86;
    font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
    font-size: 11px;
  }
}

.ai-panel {
  .ai-content {
    min-height: 170px;
    padding: 16px;
    white-space: pre-wrap;
    line-height: 1.7;
  }

  .ai-empty {
    display: grid;
    place-items: center;
    gap: 10px;
    min-height: 170px;
    padding: 24px;
    color: #78918a;
    text-align: center;
  }
}

@media (max-width: 980px) {
  .lab-header {
    align-items: stretch;
    flex-direction: column;
  }

  .patient-strip,
  .workflow {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 640px) {
  .patient-strip,
  .workflow {
    grid-template-columns: 1fr;
  }
}
</style>
