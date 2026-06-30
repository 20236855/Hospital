<template>
  <div class="skin-check-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-button link @click="goBack" class="back-btn">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <div class="header-info">
        <h2>皮肤病变检查</h2>
        <el-tag type="info" size="small">AI 智能痣识别</el-tag>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="openAiDialog" :disabled="!imageUrl" style="margin-right:8px">
          <el-icon><ChatDotRound /></el-icon> AI 辅助诊断
        </el-button>
        <el-button type="success" @click="saveDiagnosis" :disabled="!result">
          <el-icon><Check /></el-icon> 保存诊断
        </el-button>
      </div>
    </div>

    <!-- 患者信息栏 -->
    <div class="patient-bar" v-if="patientInfo">
      <div class="patient-item">
        <span class="label">患者姓名</span>
        <span class="value">{{ patientInfo.name || '--' }}</span>
      </div>
      <div class="patient-item">
        <span class="label">性别</span>
        <span class="value">{{ patientInfo.gender || '--' }}</span>
      </div>
      <div class="patient-item">
        <span class="label">年龄</span>
        <span class="value">{{ patientInfo.age ? patientInfo.age + '岁' : '--' }}</span>
      </div>
      <div class="patient-item">
        <span class="label">挂号ID</span>
        <span class="value">{{ patientInfo.registerNo || patientInfo.registerId || '--' }}</span>
      </div>
      <div class="patient-item">
        <span class="label">申请单号</span>
        <span class="value">{{ patientInfo.applyId || '--' }}</span>
      </div>
    </div>

    <!-- 主体：左右两栏 -->
    <el-row :gutter="16" class="main-row">
      <!-- 左侧：图片上传区 + AI标注图像 -->
      <el-col :span="14">
        <div class="panel upload-panel">
          <div class="panel-title">皮肤图像</div>
          <div class="panel-body">
            <!-- 未上传：拖拽区域 -->
            <div v-if="!imageUrl" class="drop-zone" @click="triggerUpload" @dragover.prevent @drop.prevent="onDrop">
              <input ref="fileInput" type="file" accept="image/*" @change="onFileChange" style="display:none" />
              <el-icon :size="48" color="#c0c4cc"><Upload /></el-icon>
              <p>点击或拖拽上传皮肤图片</p>
              <span class="drop-tip">支持 JPG/PNG，建议正面光线充足</span>
            </div>

            <!-- 已上传：预览 + 分析 -->
            <div v-else class="preview-zone">
              <div class="img-container">
                <img :src="imageUrl" class="skin-img" alt="皮肤图片" />
                <!-- 检测框覆盖层 -->
                <svg v-if="result && result.hasMole && result.boxes" class="detection-overlay">
                  <rect v-for="(box, i) in result.boxes" :key="i"
                    :x="box.x" :y="box.y" :width="box.w" :height="box.h"
                    fill="none" stroke="#00e676" stroke-width="2" />
                </svg>
              </div>
              <div class="preview-actions">
                <el-button type="primary" @click="analyzeImage" :loading="analyzing" size="large">
                  识别分析
                </el-button>
                <el-button @click="resetImage" size="large">重新上传</el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- AI 标注图像：始终显示 -->
        <div class="panel annotated-panel">
          <div class="panel-title">AI 标注图像</div>
          <div class="panel-body">
            <template v-if="result && result.annotatedImage">
              <img :src="'data:image/png;base64,' + result.annotatedImage" class="annotated-img" alt="标注结果" />
            </template>
            <template v-else-if="analyzing">
              <div class="annotated-placeholder">
                <el-icon :size="36" class="is-loading"><Loading /></el-icon>
                <p>AI 正在分析中...</p>
              </div>
            </template>
            <template v-else-if="imageUrl">
              <div class="annotated-placeholder">
                <el-icon :size="36"><Picture /></el-icon>
                <p>点击「AI 识别分析」后将在此显示标注结果</p>
              </div>
            </template>
            <template v-else>
              <div class="annotated-placeholder">
                <el-icon :size="36"><Picture /></el-icon>
                <p>请先上传皮肤图片，再点击 AI 识别分析</p>
              </div>
            </template>
          </div>
        </div>
      </el-col>

      <!-- 右侧：分析结果 + 诊断 -->
      <el-col :span="10" class="right-col">
        <!-- 检测结果 -->
        <div class="panel result-panel" v-if="result">
          <div class="panel-title">
            AI 检测结果
            <el-tag :type="result.hasMole ? 'warning' : 'success'" size="small" style="margin-left:8px">
              {{ result.hasMole ? '发现可疑' : '未见异常' }}
            </el-tag>
          </div>
          <div class="panel-body">
            <div class="stats-row-custom">
              <div class="stat-item" v-if="result.hasMole">
                <span class="stat-num">{{ result.moleCount || 1 }}</span>
                <span class="stat-txt">可疑病灶</span>
              </div>
              <div class="stat-item">
                <span class="stat-num">{{ result.confidence || '-' }}%</span>
                <span class="stat-txt">置信度</span>
              </div>
              <div class="stat-item">
                <span class="stat-num">{{ result.threshold || '-' }}</span>
                <span class="stat-txt">阈值</span>
              </div>
            </div>
            <div class="algo-stats" v-if="result.removedSmall !== undefined">
              <span>过滤: 小区域 {{ result.removedSmall }} 个</span>
              <span v-if="result.removedShape">形态不符 {{ result.removedShape }} 个</span>
            </div>
          </div>
        </div>

        <!-- AI 辅助诊断对话结果 -->
        <div class="panel ai-result-panel">
          <div class="panel-title">AI检测结果</div>
          <div class="panel-body">
            <template v-if="chatMessages.length">
              <div class="ai-chat-inline">
                <div v-for="(msg, i) in chatMessages" :key="i" :class="['chat-msg', msg.role]">
                  <strong>{{ msg.role === 'user' ? '医生' : 'AI' }}：</strong>
                  <p>{{ msg.content }}</p>
                </div>
              </div>
            </template>
            <template v-else>
              <div class="ai-result-placeholder">
                <el-icon :size="32" color="#c0c4cc"><ChatDotRound /></el-icon>
                <p>打开「AI辅助诊断」选择病变图片并提问</p>
                <p class="sub-tip">AI将结合当前切片生成返回结果</p>
              </div>
            </template>
          </div>
        </div>

        <!-- 诊断报告录入表单 -->
        <div class="panel diagnosis-panel">
          <div class="panel-title">诊断报告</div>
          <div class="panel-body">
            <el-form :model="diagnosisForm" label-width="80px" size="small">
              <el-form-item label="影像所见">
                <el-input v-model="diagnosisForm.imageFindings" type="textarea" :rows="2" placeholder="描述皮肤图像所见..." />
              </el-form-item>
              <el-form-item label="诊断意见">
                <el-input v-model="diagnosisForm.opinion" type="textarea" :rows="2" placeholder="初步诊断意见..." />
              </el-form-item>
              <el-form-item label="诊断结论">
                <el-radio-group v-model="diagnosisForm.conclusion">
                  <el-radio value="benign">良性</el-radio>
                  <el-radio value="suspicious">可疑</el-radio>
                  <el-radio value="malignant">恶性</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="建议">
                <el-checkbox-group v-model="diagnosisForm.suggestions">
                  <el-checkbox value="observe">定期观察</el-checkbox>
                  <el-checkbox value="dermoscopy">皮肤镜检查</el-checkbox>
                  <el-checkbox value="biopsy">病理活检</el-checkbox>
                  <el-checkbox value="excision">手术切除</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- AI 辅助诊断弹窗 -->
    <el-dialog v-model="aiDialogVisible" title="AI 辅助诊断" width="650px" @close="aiReply = ''">
      <div class="ai-dialog">
        <div class="ai-preview" v-if="imageUrl">
          <img :src="imageUrl" class="ai-preview-img" alt="当前图像" />
          <div class="ai-preview-info">
            <p>当前图像已分析，发现 {{ result?.moleCount || 0 }} 个可疑区域（置信度 {{ result?.confidence || '-' }}%）</p>
          </div>
        </div>
        <div class="ai-input-area">
          <el-input v-model="aiQuestion" type="textarea" :rows="2" placeholder="请输入您的问题，AI 将基于检测结果给出专业建议..." />
          <el-button type="primary" @click="sendAiQuestion" :loading="aiLoading" style="margin-top:10px">
            <el-icon><Promotion /></el-icon> 发送
          </el-button>
        </div>
        <div class="ai-chat" v-if="chatMessages.length">
          <div v-for="(msg, i) in chatMessages" :key="i" :class="['chat-msg', msg.role]">
            <strong>{{ msg.role === 'user' ? '医生' : 'AI' }}：</strong>
            <p>{{ msg.content }}</p>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="SkinCheck">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { analyzeSkin, skinAiDiagnosis } from '@/api/hisexam/skinAnalysis'
import { getApply, updateApply } from '@/api/hisexam/apply'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ChatDotRound, Check, Search, Upload, Promotion, Loading, Picture } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const fileInput = ref(null)
const imageUrl = ref('')
const imageFile = ref(null)
const analyzing = ref(false)
const result = ref(null)
const patientInfo = ref(null)
const aiDialogVisible = ref(false)
const aiQuestion = ref('')
const aiReply = ref('')
const aiLoading = ref(false)
const chatMessages = ref([])
const currentApplyId = ref(null)

const diagnosisForm = ref({
  imageFindings: '',
  opinion: '',
  conclusion: '',
  suggestions: []
})

function triggerUpload() {
  fileInput.value?.click()
}

function onFileChange(e) {
  const file = e.target.files[0]
  if (!file) return
  imageFile.value = file
  imageUrl.value = URL.createObjectURL(file)
  result.value = null
}

function onDrop(e) {
  const file = e.dataTransfer.files[0]
  if (!file) return
  imageFile.value = file
  imageUrl.value = URL.createObjectURL(file)
  result.value = null
}

function resetImage() {
  imageUrl.value = ''
  imageFile.value = null
  result.value = null
  diagnosisForm.value = { imageFindings: '', opinion: '', conclusion: '', suggestions: [] }
}

async function analyzeImage() {
  if (!imageFile.value) {
    ElMessage.warning('请先上传皮肤图片')
    return
  }
  analyzing.value = true
  try {
    const formData = new FormData()
    formData.append('file', imageFile.value)
    if (patientInfo.value?.patientId) formData.append('patientId', patientInfo.value.patientId)
    const res = await analyzeSkin(formData)
    if (res.data) {
      result.value = res.data
      // 预填诊断表单
      if (res.data.hasMole) {
        diagnosisForm.value.imageFindings = `检测到 ${res.data.moleCount || 0} 个可疑皮肤病变区域，置信度 ${res.data.confidence || '-'}%`
      } else {
        diagnosisForm.value.imageFindings = '未见明显皮肤病变特征'
      }
      ElMessage.success('分析完成')
    }
  } catch (e) {
    ElMessage.error('分析失败：' + (e.message || '未知错误'))
  } finally {
    analyzing.value = false
  }
}

function openAiDialog() {
  aiDialogVisible.value = true
  aiQuestion.value = ''
  aiReply.value = ''
}

async function sendAiQuestion() {
  if (!aiQuestion.value.trim()) return
  chatMessages.value.push({ role: 'user', content: aiQuestion.value })
  aiLoading.value = true
  try {
    const imageBase64 = await imageToBase64(imageFile.value)
    const res = await skinAiDiagnosis({
      question: aiQuestion.value,
      patientId: route.query.patientId,
      result: result.value,
      imageBase64: imageBase64
    })
    const replyData = res.data || {}
    const reply = replyData.opinion || replyData.reply || replyData.rawText || 'AI 未返回有效回复'
    chatMessages.value.push({ role: 'assistant', content: reply })

    // 将AI返回的结构化数据填入诊断表单
    if (replyData.opinion) {
      diagnosisForm.value.opinion = replyData.opinion
    }
    if (replyData.findings) {
      diagnosisForm.value.imageFindings = replyData.findings
    }

    aiQuestion.value = ''
  } catch (e) {
    ElMessage.error('AI 请求失败')
  } finally {
    aiLoading.value = false
  }
}

function imageToBase64(file) {
  return new Promise((resolve) => {
    if (!file) {
      resolve('')
      return
    }
    const reader = new FileReader()
    reader.onload = (e) => {
      const result = e.target?.result || ''
      resolve(result.startsWith('data:image') ? result : '')
    }
    reader.onerror = () => {
      resolve('')
    }
    reader.readAsDataURL(file)
  })
}

function formatDateTime(date = new Date()) {
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

async function saveDiagnosis() {
  if (!currentApplyId.value) {
    ElMessage.warning('缺少申请单ID，不能保存检查结果')
    return
  }
  const report = {
    ...diagnosisForm.value,
    patientId: patientInfo.value?.patientId,
    applyId: currentApplyId.value,
    resultSummary: result.value
  }
  try {
    await updateApply({
      applyId: currentApplyId.value,
      examResult: JSON.stringify(report),
      imageUrl: imageUrl.value || null,
      examTime: formatDateTime(),
      applyStatus: '已完成'
    })
    ElMessage.success('诊断报告已保存到申请单')
  } catch (e) {
    ElMessage.error('保存失败：' + (e.response?.data?.msg || e.message || '未知错误'))
  }
}

function goBack() {
  router.back()
}

onMounted(async () => {
  const applyId = route.query.applyId
  if (!applyId) {
    ElMessage.warning('请先从申请单列表选择对应申请单再执行检查')
    router.back()
    return
  }
  currentApplyId.value = applyId
  try {
    const res = await getApply(applyId)
    const data = res.data || {}
    patientInfo.value = {
      ...data,
      name: data.patientName,
      gender: data.gender || '-',
      age: data.age || '-'
    }
    if (data.examResult) {
      try {
        const parsed = JSON.parse(data.examResult)
        diagnosisForm.value = {
          imageFindings: parsed.imageFindings || '',
          opinion: parsed.opinion || '',
          conclusion: parsed.conclusion || '',
          suggestions: parsed.suggestions || []
        }
        result.value = parsed.resultSummary || null
      } catch (e) {
        diagnosisForm.value.imageFindings = data.examResult
      }
    }
  } catch (e) {
    ElMessage.error('申请单加载失败，无法执行检查')
    router.back()
  }
})
</script>

<style scoped lang="scss">
.skin-check-page {
  padding: 16px;
  background: #f0f2f5;
  min-height: 100vh;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  .header-info {
    display: flex; align-items: center; gap: 10px;
    h2 { margin: 0; font-size: 20px; }
  }
}

.patient-bar {
  display: grid;
  grid-template-columns: repeat(5, minmax(108px, 1fr));
  gap: 10px;
  margin-bottom: 14px;
  padding: 12px;
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  box-shadow: 0 6px 18px rgba(15, 23, 42, .04);

  .patient-item {
    display: flex;
    min-width: 0;
    flex-direction: column;
    gap: 4px;
    padding: 8px 10px;
    background: #F8FAFC;
    border: 1px solid #EEF2F7;
    border-radius: 6px;

    .label {
      color: #64748B;
      font-size: 12px;
      line-height: 1.2;
    }

    .value {
      overflow: hidden;
      color: #0F172A;
      font-size: 14px;
      font-weight: 600;
      line-height: 1.35;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

.main-row { margin: 0; align-items: stretch; }
.right-col {
  display: flex; flex-direction: column;
  > .panel { flex: 1 1 0; display: flex; flex-direction: column; min-height: 0;
    .panel-body { flex: 1; overflow-y: auto; display: flex; flex-direction: column; align-items: center; justify-content: center; }
  }
  > .result-panel { flex: 0 0 auto; }
}

.panel {
  background: #fff; border-radius: 8px; margin-bottom: 14px; overflow: hidden;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
  .panel-title {
    padding: 12px 16px; font-weight: 700; font-size: 14px; border-bottom: 1px solid #ebeef5;
    background: #fafbfc;
  }
  .panel-body { padding: 16px; }
}

.drop-zone {
  border: 2px dashed #dcdfe6; border-radius: 8px; padding: 80px 20px;
  text-align: center; cursor: pointer; transition: border-color .3s; color: #909399;
  &:hover { border-color: #409eff; color: #409eff; }
  .drop-tip { font-size: 12px; color: #c0c4cc; display: block; margin-top: 6px; }
}

.preview-zone {
  text-align: center;
  .img-container {
    position: relative; display: inline-block; max-width: 100%;
  }
  .skin-img { max-width: 100%; max-height: 420px; border-radius: 4px; }
  .detection-overlay {
    position: absolute; inset: 0; width: 100%; height: 100%; pointer-events: none;
  }
  .preview-actions { margin-top: 12px; display: flex; gap: 8px; justify-content: center; }
}

.result-panel {
  .stats-row-custom { display: flex; gap: 20px; }
  .stat-item { text-align: center; }
  .stat-num { font-size: 26px; font-weight: 800; color: #e6a23c; display: block; }
  .stat-txt { font-size: 12px; color: #909399; }
  .algo-stats { margin-top: 12px; font-size: 12px; color: #909399; display: flex; gap: 12px; }
}

.annotated-img { width: 100%; border-radius: 4px; }
.annotated-placeholder {
  text-align: center; padding: 40px 16px; color: #909399;
  p { margin-top: 10px; font-size: 13px; }
}

.diagnosis-panel {
  .el-form { margin-top: 0; }
}

.ai-result-panel {
  .ai-chat-inline { max-height: 260px; overflow-y: auto; }
  .chat-msg {
    margin-bottom: 10px; padding: 10px; border-radius: 8px; font-size: 13px;
    &.user { background: #ecf5ff; }
    &.assistant { background: #f0f9eb; }
    p { margin: 4px 0 0; }
  }
}
.ai-result-placeholder {
  text-align: center; padding: 24px 16px; color: #909399;
  p { margin-top: 8px; font-size: 13px; }
  .sub-tip { font-size: 12px; color: #c0c4cc; margin-top: 4px; }
}

.ai-dialog {
  .ai-preview { text-align: center; margin-bottom: 16px;
    .ai-preview-img { max-width: 200px; max-height: 200px; border-radius: 6px; }
    .ai-preview-info { margin-top: 8px; font-size: 13px; color: #606266; }
  }
  .ai-chat { margin-top: 16px; max-height: 300px; overflow-y: auto; }
  .chat-msg {
    margin-bottom: 12px; padding: 10px; border-radius: 8px;
    &.user { background: #ecf5ff; }
    &.assistant { background: #f0f9eb; }
    p { margin: 4px 0 0; font-size: 13px; }
  }
}
</style>
