<template>
  <div class="app-container brain-ct-page">
    <!-- ==================== 页面头部 ==================== -->
    <div class="page-header">
      <div class="header-left">
        <el-button link @click="goBack" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回检查列表</span>
        </el-button>
        <div class="header-title">
          <span class="title-badge">检查</span>
          <h1>颅内病变CT检查</h1>
        </div>
      </div>
      <div class="header-actions">
        <el-button type="primary" :loading="analyzing" @click="openAiDialog">
          <el-icon><Cpu /></el-icon>
          {{ analyzing ? 'AI分析中...' : 'AI辅助诊断' }}
        </el-button>
        <el-button type="success" @click="saveDiagnosis">
          <el-icon><DocumentChecked /></el-icon>
          保存诊断结果
        </el-button>
      </div>
    </div>

    <!-- ==================== 患者信息栏 ==================== -->
    <div class="patient-bar" v-if="patientInfo">
      <div class="patient-item">
        <span class="label">患者姓名</span>
        <span class="value">{{ patientInfo.patientName }}</span>
      </div>
      <div class="patient-item">
        <span class="label">性别</span>
        <span class="value">{{ patientInfo.gender }}</span>
      </div>
      <div class="patient-item">
        <span class="label">年龄</span>
        <span class="value">{{ patientInfo.age }}岁</span>
      </div>
      <div class="patient-item">
        <span class="label">挂号ID</span>
        <span class="value">{{ patientInfo.registerId }}</span>
      </div>
      <div class="patient-item">
        <span class="label">申请单号</span>
        <span class="value">{{ patientInfo.applyId }}</span>
      </div>
      <div class="patient-item">
        <span class="label">开单医生</span>
        <span class="value">{{ patientInfo.doctorName }}</span>
      </div>
      <div class="patient-item wide">
        <span class="label">检查目的</span>
        <span class="value">{{ patientInfo.purpose }}</span>
      </div>
    </div>

    <!-- ==================== 主体内容区 ==================== -->
    <el-row :gutter="16" class="main-content">
      <!-- 左侧：CT影像区 + 文件上传 -->
      <el-col :xs="24" :sm="24" :md="24" :lg="15" :xl="15" class="viewer-col">
        <div class="panel ct-panel">
          <div class="panel-header">
            <span class="panel-title">
              <el-icon><Picture /></el-icon>
              CT影像浏览
            </span>
            <div class="panel-tools">
              <el-tooltip content="放大">
                <el-button link @click="zoomIn"><el-icon><ZoomIn /></el-icon></el-button>
              </el-tooltip>
              <el-tooltip content="缩小">
                <el-button link @click="zoomOut"><el-icon><ZoomOut /></el-icon></el-button>
              </el-tooltip>
              <el-tooltip content="重置">
                <el-button link @click="resetZoom"><el-icon><RefreshRight /></el-icon></el-button>
              </el-tooltip>
              <el-divider direction="vertical" />
              <el-tooltip content="窗宽窗位">
                <el-button link><el-icon><Sunny /></el-icon></el-button>
              </el-tooltip>
            </div>
          </div>
          <div class="panel-body ct-viewer">
            <!-- CT影像显示区 -->
            <div class="ct-image-container" ref="ctContainer">
              <div class="ct-placeholder" v-if="!ctImageUrl && !artifactLoading">
                <el-upload
                  class="ct-upload"
                  :auto-upload="false"
                  :show-file-list="false"
                  :on-change="handleFileSelect"
                  accept=".zip,.mhd,.nii,.nii.gz,.dcm"
                  drag
                >
                  <div class="upload-area">
                    <el-icon :size="40"><Upload /></el-icon>
                    <p>拖拽CT文件到此处 或 点击选择</p>
                    <p class="sub">支持 DICOM(.zip)、MHD/RAW、NIfTI(.nii.gz)</p>
                  </div>
                </el-upload>
              </div>
              <!-- 加载中 -->
              <div class="ct-loading" v-if="artifactLoading">
                <el-icon :size="48" class="is-loading"><Loading /></el-icon>
                <p>正在分析CT影像...</p>
                <el-progress :percentage="loadingProgress" :stroke-width="8" />
                <p class="sub">{{ loadingText }}</p>
              </div>
              <!-- CT影像展示 -->
              <img v-if="ctImageUrl && !artifactLoading" :src="ctImageUrl" class="ct-image" :style="imageStyle" />
              
              <!-- AI检测框覆盖层 -->
              <div class="ai-overlay" v-if="aiDetections.length > 0">
                <div 
                  v-for="(det, idx) in aiDetections" 
                  :key="idx"
                  class="detection-box"
                  :style="det.style"
                  @click="selectDetection(det)"
                >
                  <span class="det-label">{{ det.label }}</span>
                  <span class="det-conf">{{ det.confidence }}%</span>
                </div>
              </div>
            </div>
            
            <!-- 序列导航 -->
            <div class="series-nav" v-if="ctImageUrl || uploadedFileName">
              <el-button text circle @click="prevSlice" :disabled="currentSlice <= 1">
                <el-icon><ArrowLeft /></el-icon>
              </el-button>
              <div class="nav-center">
                <span class="nav-slice-label">
                  原始切片 <strong>{{ currentSlice }}</strong> / {{ totalSlices }}
                </span>
                <span class="nav-z-info" v-if="artifactResult">
                  Z层 {{ currentSliceZIndex }} / {{ artifactResult.totalSlices }}
                </span>
                <el-slider v-model="currentSlice" :min="1" :max="totalSlices" :show-tooltip="false" class="nav-slider" />
              </div>
              <el-button text circle @click="nextSlice" :disabled="currentSlice >= totalSlices">
                <el-icon><ArrowRight /></el-icon>
              </el-button>
              <span class="nav-filename" v-if="uploadedFileName">{{ uploadedFileName }}</span>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 右侧：AI分析与诊断 -->
      <el-col :xs="24" :sm="24" :md="24" :lg="9" :xl="9" class="side-col">
        <!-- AI分析结果 -->
        <div class="panel ai-panel" v-loading="analyzing">
          <div class="panel-header">
            <span class="panel-title">
              <el-icon><Cpu /></el-icon>
              检测结果
            </span>
            <el-tag :type="aiStatus.type" size="small">{{ aiStatus.text }}</el-tag>
          </div>
          <div class="panel-body">
            <div class="ai-empty" v-if="!aiResult && !analyzing">
              <el-icon :size="48"><MagicStick /></el-icon>
              <p>打开"AI辅助诊断"选择切片并提问</p>
              <p class="sub">AI将结合当前切片和本地算法统计生成返回结果</p>
            </div>
            
            <div class="ai-result" v-if="aiResult">
              <div class="result-section">
                <h4>异常检测</h4>
                <div class="findings-list">
                  <div 
                    v-for="(finding, idx) in aiResult.findings" 
                    :key="idx"
                    class="finding-item"
                    :class="{ active: selectedFinding === idx }"
                    @click="selectedFinding = idx"
                  >
                    <div class="finding-icon" :class="finding.severity">
                      <el-icon><Warning v-if="finding.severity === 'high'" /><InfoFilled v-else /></el-icon>
                    </div>
                    <div class="finding-content">
                      <div class="finding-title">{{ finding.name }}</div>
                      <div class="finding-desc">{{ finding.description }}</div>
                      <div class="finding-meta">
                        <el-tag size="small" round :type="severityTagType(finding.severity)">
                          {{ severityLabel(finding.severity) }}
                        </el-tag>
                      </div>
                    </div>
                    <div class="finding-conf">{{ formatConfidence(finding.confidence) }}</div>
                  </div>
                </div>
              </div>
              <div class="result-section">
                <h4>AI诊断建议</h4>
                <div class="suggestion-box">
                  <p>{{ aiResult.suggestion }}</p>
                </div>
              </div>
              <div class="result-section">
                <h4>影像特征</h4>
                <el-descriptions :column="2" size="small" border>
                  <el-descriptions-item label="病灶数量">{{ aiResult.features.lesionCount }}</el-descriptions-item>
                  <el-descriptions-item label="最大病灶">{{ aiResult.features.maxSize }}mm</el-descriptions-item>
                  <el-descriptions-item label="位置">{{ aiResult.features.location }}</el-descriptions-item>
                  <el-descriptions-item label="密度">{{ aiResult.features.density }}</el-descriptions-item>
                </el-descriptions>
              </div>
            </div>
          </div>
        </div>

        <!-- 诊断录入 -->
        <div class="panel diagnosis-panel">
          <div class="panel-header">
            <span class="panel-title">
              <el-icon><EditPen /></el-icon>
              诊断报告
            </span>
          </div>
          <div class="panel-body">
            <el-form :model="diagnosisForm" label-position="top">
              <el-form-item label="影像所见">
                <el-input v-model="diagnosisForm.findings" type="textarea" :rows="3" placeholder="描述CT影像所见..." />
              </el-form-item>
              <el-form-item label="诊断意见">
                <el-input v-model="diagnosisForm.impression" type="textarea" :rows="3" placeholder="给出诊断意见..." />
              </el-form-item>
              <el-form-item label="诊断结论">
                <el-radio-group v-model="diagnosisForm.conclusion">
                  <el-radio label="normal">未见明显异常</el-radio>
                  <el-radio label="benign">良性病变</el-radio>
                  <el-radio label="malignant">恶性病变</el-radio>
                  <el-radio label="uncertain">性质待定，建议进一步检查</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="建议">
                <el-checkbox-group v-model="diagnosisForm.recommendations">
                  <el-checkbox value="mri">建议MRI进一步检查</el-checkbox>
                  <el-checkbox value="followup">建议随访复查</el-checkbox>
                  <el-checkbox value="biopsy">建议活检</el-checkbox>
                  <el-checkbox value="surgery">建议手术治疗</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
              <el-form-item label="备注">
                <el-input v-model="diagnosisForm.remark" type="textarea" :rows="2" />
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- ==================== CT标注结果区（全宽，始终显示） ==================== -->
    <div class="annotation-section">
      <div class="annotation-header">
        <div class="annotation-title">
          <el-icon><DataAnalysis /></el-icon>
          <span>CT 影像智能标注结果</span>
          <el-tag v-if="artifactResult" type="success" size="small" effect="dark" round>分析完成</el-tag>
          <el-tag v-else type="info" size="small" effect="dark" round>等待上传</el-tag>
        </div>
        <div class="annotation-stats" v-if="artifactResult">
          <div class="stat-badge hemorrhage">
            <span class="badge-num">{{ artifactResult.stats.hemorrhageCount || 0 }}</span>
            <span class="badge-txt">出血灶</span>
          </div>
          <div class="stat-badge artifact">
            <span class="badge-num">{{ artifactResult.stats.slicesWithArtifact || 0 }}</span>
            <span class="badge-txt">伪影层</span>
          </div>
          <div class="stat-badge metal">
            <span class="badge-num">{{ artifactResult.stats.slicesWithMetal || 0 }}</span>
            <span class="badge-txt">金属层</span>
          </div>
          <div class="stat-badge info">
            <span class="badge-num">{{ artifactResult.totalSlices }}</span>
            <span class="badge-txt">总切片</span>
          </div>
        </div>
        <div class="annotation-stats" v-else>
          <span class="stat-hint">上传CT文件后自动执行金属伪影 + 脑出血智能检测</span>
        </div>
      </div>

      <el-row :gutter="12" class="annotation-grid">
        <!-- 金属伪影标注 -->
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
          <div class="annotation-panel metal-panel">
            <div class="annotation-panel-header">
              <div class="panel-label-group">
                <span class="color-dot" style="background:#EF4444; box-shadow:0 0 6px #EF4444;"></span>
                <span>金属伪影标注</span>
                <el-tag v-if="artifactResult" size="small" round :type="artifactResult.stats.slicesWithMetal > 0 ? 'warning' : 'info'">
                  {{ artifactResult.stats.slicesWithMetal > 0 ? artifactResult.stats.slicesWithMetal + '层' : '未检出' }}
                </el-tag>
              </div>
              <template v-if="artifactResult">
                <div class="annotation-panel-tools">
                  <el-button size="small" type="primary" plain :disabled="!currentArtifactImage" @click="openSliceAiDialog('metal')">
                    <el-icon><Cpu /></el-icon>
                    选择当前切片
                  </el-button>
                <div class="annotation-nav">
                  <el-button text circle @click="artifactSliceIdx = Math.max(0, artifactSliceIdx - 1)">
                    <el-icon><ArrowLeft /></el-icon>
                  </el-button>
                  <span class="nav-label">{{ artifactSliceIdx + 1 }} / {{ artifactResult.slices.length }}</span>
                  <el-slider 
                    v-model="artifactSliceIdx" 
                    :min="0" :max="artifactResult.slices.length - 1" 
                    :show-tooltip="false" class="nav-slider" 
                  />
                  <el-button text circle @click="artifactSliceIdx = Math.min(artifactResult.slices.length - 1, artifactSliceIdx + 1)">
                    <el-icon><ArrowRight /></el-icon>
                  </el-button>
                </div>
                </div>
              </template>
            </div>
            <div class="annotation-image-area">
              <template v-if="artifactResult && currentArtifactImage">
                <div class="annotation-image-wrapper">
                  <img :src="'data:image/png;base64,' + currentArtifactImage" class="annotation-image" />
                  <div class="image-overlay-info">Z={{ artifactResult.sliceIndices[artifactSliceIdx] }}</div>
                </div>
              </template>
              <div class="annotation-empty" v-else>
                <el-icon :size="36"><Picture /></el-icon>
                <p>{{ artifactResult ? '未检测到金属伪影' : '上传CT文件后自动分析' }}</p>
              </div>
            </div>
            <div class="annotation-legend">
              <span class="legend-dot" style="background:#EF4444;"></span> 伪影区域
              <span class="legend-dot" style="background:#06B6D4;"></span> 金属植入物
            </div>
          </div>
        </el-col>

        <!-- 颅内出血标注 -->
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
          <div class="annotation-panel heme-panel">
            <div class="annotation-panel-header">
              <div class="panel-label-group">
                <span class="color-dot" style="background:#22C55E; box-shadow:0 0 6px #22C55E;"></span>
                <span>颅内出血标注</span>
                <el-tag 
                  v-if="artifactResult?.stats?.hemorrhageCount > 0" 
                  type="danger" size="small" effect="dark" round
                >{{ artifactResult.stats.hemorrhageCount }} 处</el-tag>
                <el-tag v-else-if="artifactResult" type="info" size="small" round>未检出</el-tag>
              </div>
              <template v-if="artifactResult">
                <div class="annotation-panel-tools">
                  <el-button size="small" type="primary" plain :disabled="!currentHemorrhageImage" @click="openSliceAiDialog('hemorrhage')">
                    <el-icon><Cpu /></el-icon>
                    选择当前切片
                  </el-button>
                <div class="annotation-nav">
                  <el-button text circle @click="hemeSliceIdx = Math.max(0, hemeSliceIdx - 1)">
                    <el-icon><ArrowLeft /></el-icon>
                  </el-button>
                  <span class="nav-label">{{ hemeSliceIdx + 1 }} / {{ totalSlices }}</span>
                  <el-slider v-model="hemeSliceIdx" :min="0" :max="Math.max(0, totalSlices - 1)" 
                             :show-tooltip="false" class="nav-slider" />
                  <el-button text circle @click="hemeSliceIdx = Math.min(totalSlices - 1, hemeSliceIdx + 1)">
                    <el-icon><ArrowRight /></el-icon>
                  </el-button>
                </div>
                </div>
              </template>
            </div>
            <div class="annotation-image-area">
              <template v-if="artifactResult && currentHemorrhageImage">
                <div class="annotation-image-wrapper">
                  <img :src="'data:image/png;base64,' + currentHemorrhageImage" class="annotation-image" />
                  <div class="image-overlay-info">Z={{ artifactResult.sliceIndices?.[hemeSliceIdx] ?? hemeSliceIdx }}</div>
                </div>
              </template>
              <div class="annotation-empty" v-else>
                <el-icon :size="36"><FirstAidKit /></el-icon>
                <p>{{ artifactResult ? '未检测到脑出血灶' : '上传CT文件后自动分析' }}</p>
              </div>
            </div>
            <div class="annotation-legend">
              <span class="legend-dot" style="background:#22C55E;"></span> 脑出血区域
              <span class="legend-status" v-if="artifactResult?.stats?.hemorrhageCount > 0" style="color:#10B981">
                ✓ {{ artifactResult.stats.hemorrhageTotalArea }}px · {{ artifactResult.stats.slicesWithHemorrhage }}层
              </span>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <el-dialog
      v-model="aiDialogVisible"
      title="AI辅助诊断"
      width="720px"
      class="ai-dialog"
      append-to-body
      destroy-on-close
    >
      <div class="ai-dialog-layout">
        <div class="ai-slice-preview">
          <div class="ai-slice-image">
            <img v-if="selectedAiSlice.imageUrl" :src="selectedAiSlice.imageUrl" alt="selected CT slice" />
            <div v-else class="annotation-empty">
              <el-icon :size="34"><Picture /></el-icon>
              <p>请选择切片</p>
            </div>
          </div>
          <div class="ai-slice-meta">
            <el-tag size="small" type="info" round>{{ selectedAiSlice.title || '未选择切片' }}</el-tag>
            <el-tag v-if="selectedAiSlice.sliceNo" size="small" round>切片 {{ selectedAiSlice.sliceNo }}</el-tag>
            <el-tag v-if="selectedAiSlice.zIndex !== null && selectedAiSlice.zIndex !== undefined" size="small" round>
              Z={{ selectedAiSlice.zIndex }}
            </el-tag>
          </div>
        </div>

        <el-form label-position="top" class="ai-question-form">
          <el-form-item label="想询问AI的问题">
            <el-input
              v-model="aiQuestion"
              type="textarea"
              :rows="4"
              maxlength="500"
              show-word-limit
              placeholder="例如：请判断当前切片是否存在金属伪影或颅内出血，并说明依据。"
            />
          </el-form-item>
        </el-form>

        <div class="ai-dialog-messages" v-if="aiDialogMessages.length">
          <div
            v-for="(message, index) in aiDialogMessages"
            :key="index"
            class="ai-dialog-message"
            :class="message.role"
          >
            <div class="message-head">{{ message.role === 'user' ? '医生提问' : 'AI返回结果' }}</div>
            <template v-if="message.role === 'assistant' && message.result">
              <div class="ai-answer-card" v-if="message.result.answer || message.result.suggestion">
                <div class="answer-label">直接回答</div>
                <div class="answer-text">{{ message.result.answer || message.result.suggestion }}</div>
              </div>
              <div class="dialog-findings" v-if="message.result.findings?.length">
                <div
                  v-for="(finding, findingIndex) in message.result.findings"
                  :key="findingIndex"
                  class="dialog-finding-card"
                >
                  <div class="dialog-finding-head">
                    <div class="dialog-finding-title">{{ finding.name || '异常发现' }}</div>
                    <div class="dialog-finding-badges">
                      <el-tag size="small" round :type="severityTagType(finding.severity)">
                        {{ severityLabel(finding.severity) }}
                      </el-tag>
                      <el-tag size="small" round type="success">{{ formatConfidence(finding.confidence) }}</el-tag>
                    </div>
                  </div>
                  <div class="dialog-finding-desc">{{ finding.description }}</div>
                </div>
              </div>
              <div class="ai-report-brief" v-if="message.result.report?.impression || message.result.report?.findings">
                <div v-if="message.result.report.findings">
                  <span>影像所见</span>{{ message.result.report.findings }}
                </div>
                <div v-if="message.result.report.impression">
                  <span>诊断意见</span>{{ message.result.report.impression }}
                </div>
              </div>
            </template>
            <div v-else class="message-body">{{ message.content }}</div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="aiDialogVisible = false">关闭</el-button>
        <el-button type="primary" :loading="analyzing" @click="sendAiQuestion">
          {{ analyzing ? 'AI分析中...' : '发送给AI' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BrainCTCheck">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  ArrowLeft, Cpu, DocumentChecked, Picture, ZoomIn, ZoomOut, 
  RefreshRight, Sunny, Upload, ArrowRight, Warning, InfoFilled,
  MagicStick, EditPen, Loading, DataAnalysis, FirstAidKit
} from '@element-plus/icons-vue'
import { detectArtifact, aiDiagnosis } from '@/api/hisexam/ctAnalysis'
import { getApply, updateApply } from '@/api/hisexam/apply'

const route = useRoute()
const router = useRouter()

// ============ 患者信息 ============
const patientInfo = reactive({
  patientName: '--',
  gender: '--',
  age: '',
  registerId: '',
  registerNo: '',
  applyId: '',
  doctorName: '--',
  purpose: ''
})

// ============ CT文件上传与影像 ============
const uploadedFileName = ref('')
const ctContainer = ref(null)
const currentSlice = ref(1)
const totalSlices = ref(128)
const zoom = ref(1)

const imageStyle = computed(() => ({
  transform: `scale(${zoom.value})`
}))

// CT主视图：优先使用原始图像（rawSlices），无标注版本
const ctImageUrl = computed(() => {
  if (!artifactResult.value) return ''
  // 优先使用原始无标注切片
  const src = artifactResult.value.rawSlices || artifactResult.value.slices
  if (!src || !src.length) return ''
  const idx = currentSlice.value - 1
  if (idx < 0 || idx >= src.length) return ''
  return 'data:image/png;base64,' + src[idx]
})

// 当前显示的原始Z层索引（用于提示）
const currentSliceZIndex = computed(() => {
  if (!artifactResult.value || !artifactResult.value.sliceIndices) return currentSlice.value
  const idx = currentSlice.value - 1
  return idx >= 0 && idx < artifactResult.value.sliceIndices.length
    ? artifactResult.value.sliceIndices[idx]
    : currentSlice.value
})

const zoomIn = () => { zoom.value = Math.min(zoom.value + 0.2, 3) }
const zoomOut = () => { zoom.value = Math.max(zoom.value - 0.2, 0.5) }
const resetZoom = () => { zoom.value = 1 }
const prevSlice = () => { if (currentSlice.value > 1) currentSlice.value-- }
const nextSlice = () => { if (currentSlice.value < totalSlices.value) currentSlice.value++ }

// ============ CT金属伪影分析 ============
const artifactLoading = ref(false)
const loadingProgress = ref(0)
const loadingText = ref('正在上传CT文件...')
const artifactResult = ref(null)
const artifactSliceIdx = ref(0)

// 当前选中的伪影标注图像
const currentArtifactImage = computed(() => {
  if (!artifactResult.value || !artifactResult.value.slices) return null
  const idx = artifactSliceIdx.value
  return artifactResult.value.slices[idx] || null
})

// ============ 脑出血标注 ============
const hemeSliceIdx = ref(0)

const currentHemorrhageImage = computed(() => {
  if (!artifactResult.value || !artifactResult.value.hemorrhageSlices) return null
  const idx = hemeSliceIdx.value
  return artifactResult.value.hemorrhageSlices[idx] || null
})

/**
 * 处理文件选择 - 触发CT上传与分析
 */
const handleFileSelect = async (uploadFile) => {
  const file = uploadFile.raw
  if (!file) return

  // 校验文件类型
  const allowedExts = ['.zip', '.mhd', '.nii', '.nii.gz', '.dcm']
  const fileName = file.name.toLowerCase()
  const isValid = allowedExts.some(ext => fileName.endsWith(ext))
  if (!isValid) {
    ElMessage.warning('请上传CT影像文件（DICOM zip / MHD / NIfTI / DCM）')
    return
  }

  const maxFileSize = 500 * 1024 * 1024
  if (file.size > maxFileSize) {
    ElMessage.warning('CT文件不能超过500MB')
    return
  }

  // 开始分析
  uploadedFileName.value = file.name
  artifactLoading.value = true
  loadingProgress.value = 0
  loadingText.value = '正在上传CT文件...'
  artifactResult.value = null

  // 模拟进度（因为真实上传进度需要onUploadProgress回调）
  const progressTimer = setInterval(() => {
    if (loadingProgress.value < 90) {
      loadingProgress.value += Math.random() * 10
      if (loadingProgress.value > 30) loadingText.value = '正在分析CT体数据...'
      if (loadingProgress.value > 60) loadingText.value = '正在执行金属伪影检测算法...'
      if (loadingProgress.value > 80) loadingText.value = '正在生成标注图像...'
    }
  }, 500)

  try {
    const res = await detectArtifact(file, {
      metal_hu: 2000,
      art_high: 500,
      art_low: -500,
      dilate_r: 20,
      enable_classify: true,
      enable_3d: false,
      return_all_slices: false,
      max_slices: 80  // 80层采样，步长约4层，覆盖连续出血区域
    })

    clearInterval(progressTimer)
    loadingProgress.value = 100
    loadingText.value = '分析完成！'

    // 解析结果
    const data = res.data || res
    if (data.success) {
      artifactResult.value = data.data
      // CT视图的 totalSlices 使用实际返回的切片数（采样后的）
      totalSlices.value = data.data.rawSlices ? data.data.rawSlices.length : data.data.sliceCount
      artifactSliceIdx.value = 0
      currentSlice.value = 1
      ElMessage.success(`CT分析完成！共返回 ${totalSlices.value} 层标注切片`)
    } else {
      ElMessage.error(data.error || 'CT分析失败')
    }
  } catch (error) {
    clearInterval(progressTimer)
    console.error('CT分析请求失败:', error)
    // 注意：request.js 拦截器已对 500/NetworkError 等弹过消息，
    // 这里仅处理未被拦截器覆盖的异常
    const msg = (typeof error === 'string')
      ? '请求处理异常，请重试'
      : (error.response?.data?.msg || error.message || '')
    if (msg && msg !== 'error' && !msg.includes('CT分析')) {
      // 只对拦截器未弹出的错误补充提示
      if (msg.includes('ResourceAccess') || msg.includes('Network Error') || msg.includes('timeout')) {
        ElMessage.error('CT分析服务未启动，请先启动Python服务：python ct_artifact_service.py --port 5001')
      } else if (msg.includes('MaxUploadSize')) {
        ElMessage.error('CT文件超过上传限制，请减小文件大小')
      } else {
        ElMessage.error('CT分析失败: ' + msg)
      }
    }
  } finally {
    artifactLoading.value = false
    loadingProgress.value = 0
  }
}

// ============ AI诊断 ============
const analyzing = ref(false)
const aiResult = ref(null)
const selectedFinding = ref(0)
const aiDetections = ref([])
const aiDialogVisible = ref(false)
const aiQuestion = ref('')
const aiDialogMessages = ref([])
const selectedAiSlice = ref({
  source: '',
  title: '',
  sliceNo: null,
  zIndex: null,
  imageUrl: ''
})

const aiStatus = computed(() => {
  if (analyzing.value) return { type: 'warning', text: '分析中' }
  if (aiResult.value) return { type: 'success', text: '已完成' }
  return { type: 'info', text: '未开始' }
})

const normalizeSliceImage = (image) => {
  if (!image) return ''
  return image.startsWith('data:image') ? image : 'data:image/png;base64,' + image
}

const getDefaultQuestion = (source) => {
  if (source === 'metal') return '请分析当前金属伪影标注切片，判断伪影和金属植入物对诊断的影响，并给出阅片建议。'
  if (source === 'hemorrhage') return '请分析当前颅内出血标注切片，判断是否存在出血征象、疑似部位和紧急程度。'
  return '请结合当前CT切片、患者信息和本地算法统计，给出颅内病变辅助诊断建议。'
}

const selectAiSlice = (source = 'main') => {
  if (!artifactResult.value) {
    ElMessage.warning('请先上传CT文件并完成影像分析')
    return false
  }

  let slice = null
  if (source === 'metal') {
    slice = {
      source,
      title: '金属伪影标注切片',
      sliceNo: artifactSliceIdx.value + 1,
      zIndex: artifactResult.value?.sliceIndices?.[artifactSliceIdx.value] ?? artifactSliceIdx.value,
      imageUrl: normalizeSliceImage(currentArtifactImage.value)
    }
  } else if (source === 'hemorrhage') {
    slice = {
      source,
      title: '颅内出血标注切片',
      sliceNo: hemeSliceIdx.value + 1,
      zIndex: artifactResult.value?.sliceIndices?.[hemeSliceIdx.value] ?? hemeSliceIdx.value,
      imageUrl: normalizeSliceImage(currentHemorrhageImage.value)
    }
  } else {
    slice = {
      source,
      title: '主视图原始切片',
      sliceNo: currentSlice.value,
      zIndex: currentSliceZIndex.value,
      imageUrl: ctImageUrl.value
    }
  }

  if (!slice.imageUrl) {
    ElMessage.warning('当前切片暂无可发送的图像')
    return false
  }

  selectedAiSlice.value = slice
  if (!aiQuestion.value) aiQuestion.value = getDefaultQuestion(source)
  return true
}

const openAiDialog = () => {
  if (selectAiSlice('main')) aiDialogVisible.value = true
}

const openSliceAiDialog = (source) => {
  if (selectAiSlice(source)) aiDialogVisible.value = true
}

const buildAiAnswerText = (result) => {
  if (result.answer) return result.answer
  if (result.suggestion) return result.suggestion
  if (result.rawText) return result.rawText
  return JSON.stringify(result)
}

const formatConfidence = (confidence) => {
  const value = Number(confidence)
  if (!Number.isFinite(value)) return '-'
  return `${Math.round((value <= 1 ? value * 100 : value))}%`
}

const severityLabel = (severity) => {
  const map = {
    high: '高风险',
    medium: '中风险',
    low: '低风险'
  }
  return map[severity] || '待判断'
}

const severityTagType = (severity) => {
  const map = {
    high: 'danger',
    medium: 'warning',
    low: 'info'
  }
  return map[severity] || 'info'
}

const applyAiResult = (result) => {
  aiResult.value = {
    findings: Array.isArray(result.findings) ? result.findings : [],
    suggestion: result.answer || result.suggestion || result.rawText || '',
    features: {
      lesionCount: result.features?.lesionCount || '-',
      maxSize: result.features?.maxSize || '-',
      location: result.features?.location || '-',
      density: result.features?.density || '-'
    },
    rawText: result.rawText,
    model: result.model
  }

  const report = result.report || {}
  diagnosisForm.findings = report.findings || diagnosisForm.findings
  diagnosisForm.impression = report.impression || aiResult.value.suggestion || diagnosisForm.impression
  diagnosisForm.conclusion = report.conclusion || diagnosisForm.conclusion
  diagnosisForm.recommendations = Array.isArray(report.recommendations) ? report.recommendations : diagnosisForm.recommendations
  diagnosisForm.remark = report.remark || diagnosisForm.remark
}

const sendAiQuestion = async () => {
  if (!artifactResult.value) {
    ElMessage.warning('请先上传CT文件并完成影像分析')
    return
  }
  if (!selectedAiSlice.value.imageUrl && !selectAiSlice('main')) return
  if (!aiQuestion.value.trim()) {
    ElMessage.warning('请输入想要询问AI的问题')
    return
  }

  analyzing.value = true
  aiResult.value = null
  aiDetections.value = []

  try {
    const question = aiQuestion.value.trim()
    aiDialogMessages.value.push({
      role: 'user',
      content: `${selectedAiSlice.value.title}（切片 ${selectedAiSlice.value.sliceNo}，Z=${selectedAiSlice.value.zIndex}）：${question}`
    })

    const res = await aiDiagnosis({
      patientInfo,
      uploadedFileName: uploadedFileName.value,
      currentSlice: selectedAiSlice.value.sliceNo,
      currentSliceZIndex: selectedAiSlice.value.zIndex,
      sliceSource: selectedAiSlice.value.title,
      analysisScene: selectedAiSlice.value.source,
      question,
      sliceImageBase64: selectedAiSlice.value.imageUrl,
      artifactStats: artifactResult.value?.stats || {}
    })

    const response = res.data || res
    if (response.code && response.code !== 200) {
      ElMessage.error(response.msg || '豆包AI辅助诊断失败')
      return
    }

    const result = response.data || response
    applyAiResult(result)
    aiDialogMessages.value.push({
      role: 'assistant',
      content: buildAiAnswerText(result),
      result
    })

    ElMessage.success('豆包AI辅助诊断已完成')
  } catch (error) {
    console.error('豆包AI辅助诊断失败:', error)
    const msg = error.response?.data?.msg || error.message || '豆包AI辅助诊断失败'
    ElMessage.error(msg)
  } finally {
    analyzing.value = false
  }
}

const startAIAnalysisMock = () => {
  analyzing.value = true
  aiResult.value = null
  aiDetections.value = []
  
  setTimeout(() => {
    analyzing.value = false
    aiResult.value = {
      findings: [
        { name: '右侧额叶低密度影', description: '约2.3×1.8cm类圆形低密度影，边界尚清，周围可见轻度水肿带', confidence: 94, severity: 'high' },
        { name: '中线结构偏移', description: '脑室系统受压，中线结构向左偏移约3mm', confidence: 88, severity: 'medium' }
      ],
      suggestion: '考虑右侧额叶占位性病变，建议进一步完善增强MRI检查以明确病变性质。',
      features: { lesionCount: 1, maxSize: '23×18', location: '右侧额叶', density: '低密度' }
    }
    aiDetections.value = [
      { label: '病灶1', confidence: 94, style: { left: '45%', top: '35%', width: '80px', height: '60px' } }
    ]
    diagnosisForm.findings = '右侧额叶见类圆形低密度影，约2.3×1.8cm，边界尚清，周围可见水肿带。脑室系统受压，中线结构向左偏移约3mm。'
  }, 3000)
}

// ============ 诊断表单 ============
const diagnosisForm = reactive({
  findings: '',
  impression: '',
  conclusion: '',
  recommendations: [],
  remark: ''
})

// ============ 操作 ============
const goBack = () => router.back()

function formatDateTime(date = new Date()) {
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

async function loadApplyDetail() {
  const applyId = route.query.applyId
  if (!applyId) {
    ElMessage.warning('请先从申请单列表选择对应申请单再执行检查')
    router.back()
    return
  }
  try {
    const res = await getApply(applyId)
    const data = res.data || {}
    patientInfo.patientName = data.patientName || '--'
    patientInfo.gender = data.gender || '--'
    patientInfo.age = data.age || ''
    patientInfo.registerId = data.registerId || ''
    patientInfo.registerNo = data.registerNo || ''
    patientInfo.applyId = data.applyId || applyId
    patientInfo.doctorName = data.doctorName || '--'
    patientInfo.purpose = data.applyInfo || data.applyPosition || ''
    if (data.examResult) {
      try {
        const parsed = JSON.parse(data.examResult)
        Object.assign(diagnosisForm, parsed.diagnosis || parsed)
      } catch (e) {
        diagnosisForm.findings = data.examResult
      }
    }
  } catch (e) {
    ElMessage.error('申请单加载失败，无法执行检查')
    router.back()
  }
}

const saveDiagnosis = async () => {
  if (!patientInfo.applyId) {
    ElMessage.warning('缺少申请单ID，不能保存检查结果')
    return
  }
  const diagnosisData = {
    checkType: 'BRAIN_CT',
    diagnosis: diagnosisForm,
    aiResult: aiResult.value,
    artifactStats: artifactResult.value?.stats || {},
    fileName: uploadedFileName.value
  }
  try {
    await updateApply({
      applyId: patientInfo.applyId,
      examResult: JSON.stringify(diagnosisData),
      imageUrl: uploadedFileName.value || null,
      examTime: formatDateTime(),
      applyStatus: '已完成'
    })
    ElMessage.success('诊断结果已保存到申请单')
  } catch (e) {
    ElMessage.error('保存失败：' + (e.response?.data?.msg || e.message || '未知错误'))
  }
}

const selectDetection = (det) => {
  console.log('选中检测:', det)
}

// ============ 生命周期 ============
onMounted(loadApplyDetail)
</script>

<style scoped lang="scss">
.brain-ct-page {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 380px;
  align-items: start;
  gap: 14px;
  min-height: 100vh;
  padding: 18px;
  background:
    linear-gradient(180deg, #EAF0F4 0, #F6F8FA 260px, #F5F7FA 100%);
  color: #1E293B;

  .page-header {
    grid-column: 1 / -1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    margin-bottom: 0;
    padding: 16px 18px;
    background: #FFFFFF;
    border: 1px solid #E2E8F0;
    border-radius: 8px;
    box-shadow: 0 8px 24px rgba(15, 23, 42, .06);

    .header-left {
      display: flex;
      align-items: center;
      gap: 18px;
      min-width: 0;
    }

    .back-btn {
      flex: 0 0 auto;
      color: #64748B;
      font-size: 14px;

      &:hover {
        color: #16614D;
      }
    }

    .header-title {
      display: flex;
      align-items: center;
      gap: 12px;
      min-width: 0;

      .title-badge {
        flex: 0 0 auto;
        padding: 4px 10px;
        background: #16614D;
        color: #FFFFFF;
        border-radius: 4px;
        font-size: 12px;
        font-weight: 700;
      }

      h1 {
        margin: 0;
        overflow: hidden;
        color: #0F172A;
        font-size: 20px;
        font-weight: 700;
        line-height: 1.3;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }

    .header-actions {
      display: flex;
      flex: 0 0 auto;
      flex-wrap: wrap;
      justify-content: flex-end;
      gap: 10px;
    }
  }

  .patient-bar {
    grid-column: 1 / -1;
    display: grid;
    grid-template-columns: repeat(6, minmax(108px, 1fr));
    gap: 10px;
    margin-bottom: 0;
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

      &.wide {
        grid-column: span 2;
      }

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

  .main-content {
    display: contents;
    align-items: stretch;

    .viewer-col,
    .side-col {
      display: flex;
      flex-direction: column;
      width: auto !important;
      max-width: none !important;
      padding-right: 0 !important;
      padding-left: 0 !important;
      flex: none !important;
      flex-basis: auto !important;
    }

    .viewer-col {
      grid-row: 3;
      grid-column: 1;
    }

    .side-col {
      grid-row: 3 / span 2;
      grid-column: 2;
      gap: 14px;
      min-height: 0;
    }

    .panel {
      display: flex;
      overflow: hidden;
      flex-direction: column;
      min-width: 0;
      margin-bottom: 0;
      background: #FFFFFF;
      border: 1px solid #E2E8F0;
      border-radius: 8px;
      box-shadow: 0 8px 24px rgba(15, 23, 42, .06);

      .panel-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 12px;
        min-height: 50px;
        padding: 12px 14px;
        background: #FFFFFF;
        border-bottom: 1px solid #E2E8F0;

        .panel-title {
          display: flex;
          align-items: center;
          gap: 8px;
          color: #0F172A;
          font-size: 15px;
          font-weight: 700;

          .el-icon {
            color: #16614D;
          }
        }

        .panel-tools {
          display: flex;
          align-items: center;
          gap: 2px;
        }
      }

      .panel-body {
        padding: 14px;
      }
    }

    .ct-panel {
      flex: 1;
      height: 100%;

      .ct-viewer {
        display: flex;
        flex: 1;
        flex-direction: column;
        padding: 0;
      }

      .ct-image-container {
        position: relative;
        display: flex;
        align-items: center;
        justify-content: center;
        min-height: 470px;
        aspect-ratio: 16 / 10;
        overflow: hidden;
        background:
          radial-gradient(circle at center, rgba(31, 41, 55, .9), rgba(3, 7, 18, 1) 68%),
          #05070A;

        .ct-placeholder {
          width: min(520px, calc(100% - 40px));
          color: #94A3B8;
          text-align: center;

          .el-icon {
            margin-bottom: 10px;
            color: #64748B;
          }

          p {
            margin: 0 0 4px;
            color: #E2E8F0;
            font-size: 14px;

            &.sub {
              margin-bottom: 16px;
              color: #94A3B8;
              font-size: 12px;
            }
          }

          :deep(.el-upload-dragger) {
            padding: 0;
            background: transparent;
            border: 0;
          }

          .upload-area {
            padding: 24px 32px;
            background: rgba(15, 23, 42, .72);
            border: 1px dashed #64748B;
            border-radius: 8px;
            cursor: pointer;
            transition: border-color .2s, background .2s;

            &:hover {
              background: rgba(22, 97, 77, .18);
              border-color: #34D399;
            }

            p {
              margin: 8px 0 0;
              color: #CBD5E1;
              font-size: 13px;

              &.sub {
                margin-top: 4px;
                color: #94A3B8;
                font-size: 11px;
              }
            }
          }
        }

        .ct-loading {
          width: min(360px, calc(100% - 40px));
          color: #CBD5E1;
          text-align: center;

          .is-loading {
            margin-bottom: 16px;
            color: #34D399;
            animation: rotating 1.5s linear infinite;
          }

          p {
            margin: 0 0 12px;
            font-size: 14px;
          }

          .sub {
            margin-top: 8px;
            color: #94A3B8;
            font-size: 12px;
          }
        }

        .ct-image {
          max-width: 100%;
          max-height: 100%;
          transition: transform .2s ease;
        }

        .ai-overlay {
          position: absolute;
          inset: 0;
          pointer-events: none;

          .detection-box {
            position: absolute;
            display: flex;
            align-items: flex-start;
            flex-direction: column;
            padding: 2px 4px;
            background: rgba(245, 158, 11, .15);
            border: 2px solid #F59E0B;
            border-radius: 4px;
            cursor: pointer;
            pointer-events: auto;

            .det-label,
            .det-conf {
              color: #FFFFFF;
              font-size: 10px;
              border-radius: 2px;
            }

            .det-label {
              padding: 1px 4px;
              background: #F59E0B;
            }

            .det-conf {
              margin-top: 2px;
              padding: 1px 4px;
              background: rgba(0, 0, 0, .72);
            }

            &:hover {
              background: rgba(239, 68, 68, .2);
              border-color: #EF4444;
            }
          }
        }
      }

      .series-nav {
        display: grid;
        grid-template-columns: auto minmax(0, 1fr) auto minmax(110px, auto);
        align-items: center;
        gap: 10px;
        padding: 12px 14px;
        background: #F8FAFC;
        border-top: 1px solid #E2E8F0;

        .nav-center {
          display: flex;
          min-width: 0;
          flex-direction: column;
          gap: 2px;

          .nav-slice-label {
            color: #475569;
            font-size: 12px;

            strong {
              color: #0F172A;
              font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
            }
          }

          .nav-z-info {
            color: #94A3B8;
            font-size: 10px;
          }

          .nav-slider {
            margin-top: 2px;
          }
        }

        .nav-filename {
          overflow: hidden;
          max-width: 180px;
          color: #64748B;
          font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
          font-size: 11px;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }

    .ai-panel {
      flex: 0 0 auto;
      min-height: 0;
      margin-bottom: 0;

      .ai-empty {
        min-height: 150px;
        padding: 34px 18px;
        color: #64748B;
        text-align: center;

        .el-icon {
          margin-bottom: 12px;
          color: #94A3B8;
        }

        p {
          margin: 0 0 4px;
          font-size: 14px;

          &.sub {
            color: #94A3B8;
            font-size: 12px;
          }
        }
      }

      .ai-result {
        .result-section {
          margin-bottom: 16px;

          &:last-child {
            margin-bottom: 0;
          }

          h4 {
            margin: 0 0 10px;
            padding-left: 8px;
            color: #0F172A;
            font-size: 13px;
            font-weight: 700;
            border-left: 3px solid #16614D;
          }

          .findings-list {
            display: flex;
            flex-direction: column;
            gap: 8px;

            .finding-item {
              display: flex;
              align-items: flex-start;
              gap: 10px;
              padding: 10px;
              background: #F8FAFC;
              border: 1px solid #E2E8F0;
              border-radius: 6px;
              cursor: pointer;
              transition: border-color .2s, background .2s;

              &.active,
              &:hover {
                background: #EDF8F4;
                border-color: #66BFA4;
              }

              .finding-icon {
                display: flex;
                align-items: center;
                justify-content: center;
                width: 28px;
                height: 28px;
                border-radius: 50%;

                &.high {
                  color: #DC2626;
                  background: #FEE2E2;
                }

                &.medium {
                  color: #D97706;
                  background: #FEF3C7;
                }

                .el-icon {
                  font-size: 14px;
                }
              }

              .finding-content {
                flex: 1;
                min-width: 0;

                .finding-title {
                  margin-bottom: 2px;
                  color: #0F172A;
                  font-size: 13px;
                  font-weight: 700;
                }

                .finding-desc {
                  color: #64748B;
                  font-size: 12px;
                  line-height: 1.45;
                }

                .finding-meta {
                  margin-top: 8px;
                  display: flex;
                  align-items: center;
                  gap: 6px;
                  flex-wrap: wrap;
                }
              }

              .finding-conf {
                color: #16614D;
                font-size: 12px;
                font-weight: 700;
                min-width: 40px;
                text-align: right;
              }
            }
          }

          .suggestion-box {
            padding: 12px;
            background: #EDF8F4;
            border-left: 4px solid #16614D;
            border-radius: 0 6px 6px 0;

            p {
              margin: 0;
              color: #1E293B;
              font-size: 13px;
              line-height: 1.6;
            }
          }
        }
      }
    }

    .diagnosis-panel {
      flex: 1;
      min-height: 0;
      margin-bottom: 0;

      .panel-body {
        flex: 1;
        overflow: auto;
      }

      :deep(.el-form-item) {
        margin-bottom: 14px;
      }

      :deep(.el-form-item__label) {
        padding-bottom: 4px;
        color: #475569;
        font-weight: 600;
      }

      :deep(.el-textarea__inner) {
        resize: vertical;
      }

      :deep(.el-radio-group) {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: 8px 10px;
      }

      :deep(.el-radio) {
        height: auto;
        margin-right: 0;
        line-height: 1.35;
        white-space: normal;
      }

      :deep(.el-checkbox-group) {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: 8px 10px;
      }

      :deep(.el-checkbox) {
        height: auto;
        margin-right: 0;
        line-height: 1.35;
        white-space: normal;
      }
    }
  }

  .annotation-section {
    grid-row: 4;
    grid-column: 1;
    display: flex;
    flex-direction: column;
    height: 100%;
    margin-top: 0;
    overflow: hidden;
    background: #FFFFFF;
    border: 1px solid #E2E8F0;
    border-radius: 8px;
    box-shadow: 0 8px 24px rgba(15, 23, 42, .06);

    .annotation-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      flex-wrap: wrap;
      gap: 12px;
      padding: 14px 16px;
      background: linear-gradient(135deg, #E8F3EF 0%, #F7FBF9 100%);
      border-bottom: 1px solid #CFE5DC;

      .annotation-title {
        display: flex;
        align-items: center;
        gap: 10px;
        color: #145744;
        font-size: 16px;
        font-weight: 700;

        .el-icon {
          color: #16614D;
        }
      }

      .annotation-stats {
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        gap: 8px;

        .stat-badge {
          display: flex;
          align-items: center;
          gap: 8px;
          min-width: 88px;
          padding: 6px 10px;
          background: rgba(255, 255, 255, .72);
          border: 1px solid #CFE5DC;
          border-radius: 6px;

          .badge-num {
            font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
            font-size: 18px;
            font-weight: 800;
            line-height: 1;
          }

          .badge-txt {
            color: #5B766E;
            font-size: 11px;
          }

          &.hemorrhage {
            color: #14804A;
          }

          &.artifact {
            color: #C2410C;
          }

          &.metal {
            color: #0F766E;
          }

          &.info {
            color: #16614D;
          }
        }

        .stat-hint {
          color: #4F6F66;
          font-size: 13px;
        }
      }
    }

    .annotation-grid {
      flex: 1;
      padding: 12px;

      :deep(.el-col) {
        display: flex;
        width: 100%;
        max-width: 100%;
        margin-bottom: 12px;
        flex: 0 0 100%;

        &:last-child {
          margin-bottom: 0;
        }
      }
    }

    .annotation-panel {
      display: flex;
      flex-direction: column;
      width: 100%;
      height: 100%;
      min-height: 410px;
      overflow: hidden;
      background: #FFFFFF;
      border: 1px solid #E2E8F0;
      border-radius: 8px;

      .annotation-panel-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 12px;
        min-height: 48px;
        padding: 10px 12px;
        background: #F8FAFC;
        border-bottom: 1px solid #E2E8F0;

        .panel-label-group {
          display: flex;
          align-items: center;
          min-width: 0;
          gap: 8px;
          color: #0F172A;
          font-size: 13px;
          font-weight: 700;

          .color-dot {
            flex: 0 0 auto;
            width: 9px;
            height: 9px;
            border-radius: 50%;
          }
        }

        .annotation-nav {
          display: flex;
          align-items: center;
          flex: 0 0 auto;
          gap: 6px;

          .nav-label {
            min-width: 54px;
            color: #64748B;
            font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
            font-size: 11px;
            text-align: center;
            white-space: nowrap;
          }

          .nav-slider {
            width: 92px;
          }
        }
      }

      .annotation-image-area {
        position: relative;
        display: flex;
        align-items: center;
        justify-content: center;
        flex: 1;
        min-height: 320px;
        background: #0A0F1A;

        .annotation-image-wrapper {
          position: relative;
          display: flex;
          align-items: center;
          justify-content: center;
          width: 100%;
          height: 100%;

          .annotation-image {
            max-width: 100%;
            max-height: 100%;
            object-fit: contain;
          }

          .image-overlay-info {
            position: absolute;
            right: 10px;
            bottom: 8px;
            padding: 3px 8px;
            color: #38BDF8;
            font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
            font-size: 10px;
            background: rgba(2, 6, 23, .78);
            border: 1px solid rgba(56, 189, 248, .22);
            border-radius: 999px;
          }
        }

        .annotation-empty {
          color: #64748B;
          text-align: center;

          .el-icon {
            margin-bottom: 8px;
            color: #475569;
            opacity: .55;
          }

          p {
            margin: 0;
            color: #94A3B8;
            font-size: 13px;
          }
        }
      }

      .annotation-legend {
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        gap: 10px;
        min-height: 38px;
        padding: 8px 12px;
        color: #64748B;
        background: #F8FAFC;
        border-top: 1px solid #E2E8F0;
        font-size: 11px;

        .legend-dot {
          flex: 0 0 auto;
          width: 9px;
          height: 9px;
          border-radius: 2px;
        }

        .legend-status {
          margin-left: auto;
          color: #64748B;
          font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
          font-size: 10px;
        }
      }
    }
  }
}

@media (min-width: 1200px) {
  .brain-ct-page {
    .main-content {
      .side-col {
        position: sticky;
        top: 12px;
        max-height: calc(100vh - 24px);
        overflow-y: auto;
        overflow-x: hidden;
        overscroll-behavior: contain;
        padding-right: 4px !important;
      }
    }
  }
}

@media (max-width: 1199px) {
  .brain-ct-page {
    display: block;

    .page-header,
    .patient-bar {
      margin-bottom: 14px;
    }

    .main-content {
      display: flex;

      .viewer-col,
      .side-col {
        width: 100% !important;
        max-width: 100% !important;
        flex: 0 0 100% !important;
      }

      .side-col {
        margin-top: 0;
      }
    }

    .annotation-section {
      margin-top: 14px;
    }
  }
}

@media (max-width: 992px) {
  .brain-ct-page {
    padding: 12px;

    .page-header {
      align-items: stretch;
      flex-direction: column;

      .header-left {
        align-items: flex-start;
        flex-direction: column;
        gap: 10px;
      }

      .header-title h1 {
        white-space: normal;
      }

      .header-actions {
        justify-content: flex-start;
      }
    }

    .patient-bar {
      grid-template-columns: repeat(2, minmax(0, 1fr));

      .patient-item.wide {
        grid-column: span 2;
      }
    }

    .main-content {
      .ct-panel {
        .ct-image-container {
          min-height: 360px;
          aspect-ratio: 4 / 3;
        }
      }
    }
  }
}

@media (max-width: 640px) {
  .brain-ct-page {
    .patient-bar {
      grid-template-columns: 1fr;

      .patient-item.wide {
        grid-column: auto;
      }
    }

    .main-content {
      .ct-panel {
        .series-nav {
          grid-template-columns: auto minmax(0, 1fr) auto;

          .nav-filename {
            grid-column: 1 / -1;
            max-width: 100%;
          }
        }
      }

      .diagnosis-panel {
        :deep(.el-radio-group),
        :deep(.el-checkbox-group) {
          grid-template-columns: 1fr;
        }
      }
    }

    .annotation-section {
      .annotation-header {
        align-items: flex-start;
        flex-direction: column;
      }

      .annotation-panel {
        .annotation-panel-header {
          align-items: stretch;
          flex-direction: column;

          .annotation-nav {
            width: 100%;

            .nav-slider {
              flex: 1;
              width: auto;
            }
          }
        }
      }
    }
  }
}

.annotation-panel-tools {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.ai-dialog-layout {
  display: grid;
  gap: 14px;
}

.ai-slice-preview {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 14px;
  align-items: center;
  padding: 12px;
  border: 1px solid rgba(148, 163, 184, 0.24);
  border-radius: 8px;
  background: #f8fafc;
}

.ai-slice-image {
  height: 160px;
  border-radius: 6px;
  overflow: hidden;
  background: #0f172a;
  display: flex;
  align-items: center;
  justify-content: center;

  img {
    width: 100%;
    height: 100%;
    object-fit: contain;
  }
}

.ai-slice-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.ai-question-form {
  :deep(.el-form-item) {
    margin-bottom: 0;
  }
}

.ai-dialog-messages {
  display: grid;
  gap: 10px;
  max-height: 260px;
  overflow: auto;
}

.ai-dialog-message {
  padding: 10px 12px;
  border-radius: 8px;
  border: 1px solid rgba(148, 163, 184, 0.24);
  background: #fff;

  &.user {
    background: #eff6ff;
    border-color: rgba(59, 130, 246, 0.24);
  }

  &.assistant {
    background: #f8fafc;
  }
}

.message-head {
  font-size: 12px;
  font-weight: 700;
  color: #475569;
  margin-bottom: 6px;
}

.message-body {
  white-space: pre-wrap;
  line-height: 1.65;
  color: #1e293b;
}

.ai-answer-card {
  padding: 12px;
  border-radius: 8px;
  background: #ecfdf5;
  border: 1px solid rgba(16, 185, 129, 0.22);
}

.answer-label {
  font-size: 12px;
  font-weight: 700;
  color: #047857;
  margin-bottom: 6px;
}

.answer-text {
  color: #0f172a;
  line-height: 1.7;
}

.dialog-findings {
  display: grid;
  gap: 8px;
  margin-top: 10px;
}

.dialog-finding-card {
  padding: 12px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #e2e8f0;
}

.dialog-finding-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 8px;
}

.dialog-finding-title {
  font-weight: 700;
  color: #1e293b;
}

.dialog-finding-badges {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.dialog-finding-desc {
  color: #475569;
  line-height: 1.65;
}

.ai-report-brief {
  display: grid;
  gap: 8px;
  margin-top: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  background: #f8fafc;
  border: 1px dashed #cbd5e1;
  color: #334155;
  line-height: 1.6;

  span {
    display: inline-block;
    margin-right: 8px;
    font-weight: 700;
    color: #0f766e;
  }
}

@media (max-width: 768px) {
  .annotation-panel-tools {
    width: 100%;
    flex-wrap: wrap;
    justify-content: flex-end;
  }

  .ai-slice-preview {
    grid-template-columns: 1fr;
  }
}

@keyframes rotating {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
