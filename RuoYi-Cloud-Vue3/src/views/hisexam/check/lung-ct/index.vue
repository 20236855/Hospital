<template>
  <div class="lung-ct-page">
    <!-- ==================== 页面头部 ==================== -->
    <div class="page-header">
      <div class="header-left">
        <el-button link @click="goBack" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回检查列表</span>
        </el-button>
        <div class="header-title">
          <span class="title-badge">检查</span>
          <h1>肺部病变CT检查</h1>
        </div>
      </div>
      <div class="header-actions">
        <el-button type="primary" :loading="analyzing" @click="startAIAnalysis" :disabled="!fileSelected">
          <el-icon><Cpu /></el-icon>
          {{ analyzing ? 'CT分析中...' : '启动CT值智能分析' }}
        </el-button>
        <el-button type="success" :disabled="!analysisDone" @click="saveDiagnosis" :loading="saving">
          <el-icon><DocumentChecked /></el-icon>
          保存诊断结果
        </el-button>
      </div>
    </div>

    <!-- ==================== 患者信息 & 业务关联栏 ==================== -->
    <div class="patient-bar">
      <!-- 挂号单下拉框 -->
      <div class="patient-item">
        <span class="label">挂号单</span>
        <el-select
          v-model="selectedRegisterId"
          filterable
          remote
          :remote-method="searchRegisters"
          :loading="registerLoading"
          :disabled="!!currentApplyId"
          placeholder="搜索挂号编号/患者姓名"
          clearable
          class="full-select"
          @change="onRegisterChange"
        >
          <el-option
            v-for="r in registerList"
            :key="r.registerId"
            :label="r.registerNo + ' - ' + r.patientName"
            :value="r.registerId"
          >
            <span style="font-weight:600">{{ r.registerNo }}</span>
            <span style="color:#64748B;margin-left:8px">{{ r.patientName }}</span>
            <span style="color:#94A3B8;font-size:11px;margin-left:4px">{{ r.gender }} {{ r.age }}岁</span>
          </el-option>
        </el-select>
      </div>
      <!-- 医技项目下拉框 -->
      <div class="patient-item">
        <span class="label">医技项目</span>
        <el-select v-model="selectedTechId" placeholder="选择医技项目" clearable class="full-select">
          <el-option
            v-for="t in techList"
            :key="t.id"
            :label="t.techName"
            :value="t.id"
          />
        </el-select>
      </div>
      <!-- 挂号编号(只读) -->
      <div class="patient-item">
        <span class="label">挂号编号</span>
        <span class="value">{{ patientInfo.registerNo || '--' }}</span>
      </div>
      <!-- 患者姓名 -->
      <div class="patient-item">
        <span class="label">患者姓名</span>
        <span class="value">{{ patientInfo.patientName }}</span>
      </div>
      <!-- 性别 -->
      <div class="patient-item">
        <span class="label">性别</span>
        <span class="value">{{ patientInfo.gender }}</span>
      </div>
      <!-- 年龄 -->
      <div class="patient-item">
        <span class="label">年龄</span>
        <span class="value">{{ patientInfo.age ? patientInfo.age + '岁' : '--' }}</span>
      </div>
      <!-- 接诊ID -->
      <div class="patient-item">
        <span class="label">接诊ID</span>
        <span class="value">{{ patientInfo.encounterId || '--' }}</span>
      </div>
      <!-- 患者ID -->
      <div class="patient-item">
        <span class="label">患者ID</span>
        <span class="value">{{ patientInfo.patientId || '--' }}</span>
      </div>
      <!-- 开单医生 -->
      <div class="patient-item">
        <span class="label">开单医生</span>
        <span class="value">{{ patientInfo.doctorName }}</span>
      </div>
      <!-- 开单科室 -->
      <div class="patient-item">
        <span class="label">开单科室</span>
        <span class="value">{{ patientInfo.deptName }}</span>
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
              <el-tooltip content="放大"><el-button link @click="zoomIn"><el-icon><ZoomIn /></el-icon></el-button></el-tooltip>
              <el-tooltip content="缩小"><el-button link @click="zoomOut"><el-icon><ZoomOut /></el-icon></el-button></el-tooltip>
              <el-tooltip content="重置"><el-button link @click="resetZoom"><el-icon><RefreshRight /></el-icon></el-button></el-tooltip>
              <el-divider direction="vertical" />
              <el-tooltip content="原始/标注切换">
                <el-button link @click="showAnnotated = !showAnnotated">
                  <el-icon><Switch /></el-icon>
                </el-button>
              </el-tooltip>
            </div>
          </div>
          <div class="panel-body ct-viewer">
            <div class="ct-image-container" ref="ctContainer">
              <!-- 上传区 -->
              <div class="ct-placeholder" v-if="!ctImageDisplay && !loading">
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
                    <p>拖拽肺部CT文件到此处 或 点击选择</p>
                    <p class="sub">支持 DICOM(.zip)、MHD/RAW、NIfTI(.nii.gz)</p>
                  </div>
                </el-upload>
              </div>
              <!-- 加载中 -->
              <div class="ct-loading" v-if="loading">
                <el-icon :size="48" class="is-loading"><Loading /></el-icon>
                <p>正在分析肺部CT影像...</p>
                <el-progress :percentage="loadingProgress" :stroke-width="8" />
                <p class="sub">{{ loadingText }}</p>
              </div>
              <!-- CT影像展示 -->
              <img v-if="ctImageDisplay && !loading" :src="ctImageDisplay" class="ct-image" :style="imageStyle" />
            </div>

            <!-- 切片导航 -->
            <div class="series-nav" v-if="totalSlices > 0">
              <el-button text circle @click="prevSlice" :disabled="currentSlice <= 1">
                <el-icon><ArrowLeft /></el-icon>
              </el-button>
              <div class="nav-center">
                <span class="nav-slice-label">
                  切片 <strong>{{ currentSlice }}</strong> / {{ totalSlices }}
                </span>
                <span class="nav-z-info" v-if="analysisResult">
                  Z层 {{ currentSliceZ }} / {{ analysisResult.totalSlicesOrig || totalSlices }}
                </span>
                <el-slider v-model="currentSlice" :min="1" :max="totalSlices" :show-tooltip="false" class="nav-slider" />
              </div>
              <el-button text circle @click="nextSlice" :disabled="currentSlice >= totalSlices">
                <el-icon><ArrowRight /></el-icon>
              </el-button>
              <span class="nav-filename" v-if="uploadedFileName">{{ uploadedFileName }}</span>
            </div>

            <!-- 保存当前切片为检测结果 -->
            <div class="save-slice-bar" v-if="totalSlices > 0 && analysisDone">
              <el-button type="primary" @click="saveCurrentLungSlice" :loading="savingSlice" size="small">
                <el-icon><PictureFilled /></el-icon>
                保存当前切片为检测结果
              </el-button>
              <span v-if="savedSliceImageUrl" class="saved-tip">
                <el-icon color="#67C23A"><CircleCheck /></el-icon> 已保存，将随诊断结果一起提交
              </span>
            </div>
          </div>
        </div>

        <!-- ==================== 结节检测结果区 ==================== -->
        <div class="annotation-section" v-if="analysisDone">
          <div class="annotation-header">
            <div class="annotation-title">
              <el-icon><DataAnalysis /></el-icon>
              <span>CT值智能分割结果</span>
              <el-tag type="success" size="small" effect="dark" round>分析完成</el-tag>
            </div>
            <div class="annotation-stats">
              <div class="stat-badge solid">
                <span class="badge-num">{{ stats.solidNodules }}</span>
                <span class="badge-txt">实性结节</span>
              </div>
              <div class="stat-badge ggo">
                <span class="badge-num">{{ stats.ggoNodules }}</span>
                <span class="badge-txt">磨玻璃结节</span>
              </div>
              <div class="stat-badge calc">
                <span class="badge-num">{{ stats.calcifiedNodules }}</span>
                <span class="badge-txt">钙化灶</span>
              </div>
              <div class="stat-badge risk">
                <span class="badge-num">{{ stats.highRiskNodules }}</span>
                <span class="badge-txt">高风险</span>
              </div>
              <div class="stat-badge info">
                <span class="badge-num">{{ totalSlices }}</span>
                <span class="badge-txt">切片数</span>
              </div>
            </div>
          </div>

          <div class="nodule-table-area" v-if="allCandidates.length > 0">
            <h4>
              <span class="sec-dot"></span>检测到 {{ allCandidates.length }} 个结节候选区域
            </h4>
            <el-table :data="paginatedCandidates" size="small" stripe border style="width:100%">
              <el-table-column prop="slice_z" label="Z层" width="60" />
              <el-table-column label="类型" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.type === 'solid' ? 'danger' : row.type === 'ggo' ? 'warning' : 'info'" size="small">
                    {{ row.type === 'solid' ? '实性' : row.type === 'ggo' ? '磨玻璃' : '钙化' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="diameter_mm" label="直径(mm)" width="90" sortable />
              <el-table-column label="恶性风险" width="105" sortable prop="malignancyProbability">
                <template #default="{ row }">
                  <el-tag :type="getRiskTag(row.malignancyRisk)" size="small">
                    {{ row.malignancyRisk || '低风险' }} {{ Math.round((row.malignancyProbability || 0) * 100) }}%
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="area_mm2" label="面积(mm²)" width="90" sortable />
              <el-table-column label="位置(Y,X)" width="110">
                <template #default="{ row }">{{ row.centroid_y.toFixed(0) }}, {{ row.centroid_x.toFixed(0) }}</template>
              </el-table-column>
              <el-table-column prop="hu_mean" label="HU均值" width="80" sortable />
              <el-table-column prop="hu_std" label="HU标准差" width="85" sortable />
              <el-table-column prop="hu_min" label="HU最小" width="75" />
              <el-table-column prop="hu_max" label="HU最大" width="75" />
              <el-table-column prop="circularity" label="圆度" width="70" sortable>
                <template #default="{ row }">{{ row.circularity?.toFixed(2) }}</template>
              </el-table-column>
              <el-table-column label="分割" width="90">
                <template #default="{ row }">
                  <el-tag type="success" size="small">{{ row.segmentation?.contourPointCount || 0 }}点</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="良恶性风险" width="130" sortable>
                <template #default="{ row }">
                  <el-tag :type="riskTag(row.malignancyRisk)" size="small">
                    {{ row.malignancyRisk || '低风险' }} {{ Math.round((row.malignancyProbability || 0) * 100) }}%
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="HU特征" min-width="150">
                <template #default="{ row }">
                  <span class="hu-badge" :class="getHuClass(row.hu_mean)">均值{{ row.hu_mean }}</span>
                  <span style="color:#94A3B8;font-size:11px;margin-left:6px">范围[{{ row.hu_min }}, {{ row.hu_max }}]</span>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              v-if="allCandidates.length > 5"
              v-model:current-page="candidatePage"
              :page-size="5"
              layout="prev, pager, next"
              :total="allCandidates.length"
              small
              class="nodule-pagination"
            />
          </div>
          <div class="nodule-empty" v-else>
            <el-icon :size="36"><SuccessFilled /></el-icon>
            <p>当前切片范围内未检测到明显结节候选区域</p>
            <p class="sub">HU值在 -100 ~ 200 范围内无非空气软组织区域</p>
          </div>
        </div>
      </el-col>

      <!-- 右侧：AI分析与诊断 -->
      <el-col :xs="24" :sm="24" :md="24" :lg="9" :xl="9" class="side-col">
        <!-- AI分析管线 -->
        <div class="panel pipeline-panel">
          <div class="panel-header">
            <span class="panel-title">
              <el-icon><Cpu /></el-icon>
              AI 分析管线
            </span>
            <el-tag :type="pipelineStatus.tag" size="small">{{ pipelineStatus.text }}</el-tag>
          </div>
          <div class="panel-body">
            <!-- 三阶段管线 -->
            <div class="pipeline-flow">
              <div class="flow-line"></div>
              <!-- Stage 01: 肺结节检测与分类 -->
              <div class="stage-card" :class="{ active: currentStage === 1, done: stage1Done, running: analyzing && currentStage === 1 }">
                <div class="stage-num">01</div>
                <div class="stage-info">
                  <div class="stage-name">肺结节检测与分类 <el-tag size="small" type="success" effect="plain">CT值算法</el-tag></div>
                  <div class="stage-model">HU-Morphology Detector / AutoDL YOLOv8接口预留</div>
                  <div class="stage-desc">基于真实CT体数据，自动检测肺结节位置，并按大小、密度和形态进行分类</div>
                </div>
                <div class="stage-status">
                  <el-icon v-if="stage1Done" class="done"><CircleCheckFilled /></el-icon>
                  <el-icon v-else-if="analyzing && currentStage === 1" class="run"><Loading /></el-icon>
                  <el-icon v-else class="wait"><Clock /></el-icon>
                </div>
                <div class="stage-status">
                  <el-icon v-if="stage1Done" class="done"><CircleCheckFilled /></el-icon>
                  <el-icon v-else-if="analyzing && currentStage === 1" class="run"><Loading /></el-icon>
                  <el-icon v-else class="wait"><Clock /></el-icon>
                </div>
              </div>
              <!-- Stage 02: 结节分割（CT值算法 - 已实现） -->
              <div class="stage-card" :class="{ active: currentStage === 2, done: stage2Done, running: analyzing && currentStage === 2 }">
                <div class="stage-num">02</div>
                <div class="stage-info">
                  <div class="stage-name">结节精确分割 <el-tag size="small" type="success" effect="plain">CT值算法</el-tag></div>
                  <div class="stage-model">Algorithm: HU阈值 + 形态学分析</div>
                  <div class="stage-desc">基于CT亨氏单位(HU)值，结合肺实质提取、连通域分析和形态学特征，实现结节候选区域检测与分割</div>
                </div>
                <div class="stage-status">
                  <el-icon v-if="stage2Done" class="done"><CircleCheckFilled /></el-icon>
                  <el-icon v-else-if="analyzing && currentStage === 2" class="run"><Loading /></el-icon>
                  <el-icon v-else class="wait"><Clock /></el-icon>
                </div>
              </div>
              <!-- Stage 03: 良恶性诊断 -->
              <div class="stage-card" :class="{ active: currentStage === 3, done: stage3Done, running: analyzing && currentStage === 3 }">
                <div class="stage-num">03</div>
                <div class="stage-info">
                  <div class="stage-name">良恶性鉴别诊断 <el-tag size="small" type="warning" effect="plain">{{ malignancySummary.overallRisk }}</el-tag></div>
                  <div class="stage-model">HU-Morphology Risk / AutoDL DenseNet-121接口预留</div>
                  <div class="stage-desc">综合结节大小、密度、圆度、离心率和HU离散度，输出恶性风险概率与随访建议</div>
                  <div class="stage-desc risk-line" v-if="analysisDone">{{ malignancySummary.conclusion }}</div>
                </div>
                <div class="stage-status">
                  <el-icon v-if="stage3Done" class="done"><CircleCheckFilled /></el-icon>
                  <el-icon v-else-if="analyzing && currentStage === 3" class="run"><Loading /></el-icon>
                  <el-icon v-else class="wait"><Clock /></el-icon>
                </div>
                <div class="stage-status">
                  <el-icon v-if="stage3Done" class="done"><CircleCheckFilled /></el-icon>
                  <el-icon v-else-if="analyzing && currentStage === 3" class="run"><Loading /></el-icon>
                  <el-icon v-else class="wait"><Clock /></el-icon>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 检验医生备注 -->
        <div class="panel diagnosis-panel">
          <div class="panel-header">
            <span class="panel-title">
              <el-icon><EditPen /></el-icon>
              初步观察 & 备注
            </span>
            <el-tag type="info" size="small">诊断结论由门诊医生出具</el-tag>
          </div>
          <div class="panel-body">
            <el-form :model="diagnosisForm" label-position="top">
              <el-form-item label="影像所见（AI自动生成）">
                <el-input v-model="diagnosisForm.findings" type="textarea" :rows="3" readonly placeholder="上传CT后由AI自动填充..." />
              </el-form-item>
              <el-form-item label="检验医生备注">
                <el-input v-model="diagnosisForm.remark" type="textarea" :rows="4" placeholder="可记录初步观察或特殊说明..." />
              </el-form-item>
              <el-alert
                title="提示"
                type="info"
                :closable="false"
                show-icon
                description="CT影像保存后，门诊医生可在病历系统中查看切片并出具正式诊断报告。"
              />
            </el-form>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="LungCTCheck">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft, ArrowRight, Cpu, DocumentChecked, Picture, ZoomIn, ZoomOut,
  RefreshRight, Upload, Switch, Loading, DataAnalysis, EditPen, Clock,
  CircleCheckFilled, SuccessFilled, PictureFilled, CircleCheck
} from '@element-plus/icons-vue'
import { analyzeLungCt, saveCtSlices, getSlicesByApplyId } from '@/api/hisexam/lungCtAnalysis'
import { listTechnology } from '@/api/hisexam/technology'
import { getApply, updateApply, listApplyRegisterOptions } from '@/api/hisexam/apply'
import { listResult, addResult, updateResult } from '@/api/hisexam/result'
import { uploadFile } from '@/api/system/file'

const route = useRoute()
const router = useRouter()
const currentApplyId = ref(null)

// ============ 挂号下拉框 ============
const selectedRegisterId = ref(null)
const registerList = ref([])
const registerLoading = ref(false)

const searchRegisters = (keyword) => {
  registerLoading.value = true
  listApplyRegisterOptions({ keyword })
    .then(res => {
      const data = res.data || []
      registerList.value = data
    })
    .catch(err => { console.error('搜索挂号单失败:', err) })
    .finally(() => { registerLoading.value = false })
}

const fillPatientFromApply = (data) => {
  selectedRegisterId.value = data.registerId || null
  selectedTechId.value = data.techId || null
  Object.assign(patientInfo, {
    registerId: data.registerId || '',
    registerNo: data.registerNo || '',
    patientId: data.patientId || '',
    patientName: data.patientName || '--',
    gender: data.gender || '--',
    age: data.age || 0,
    doctorId: data.doctorId || '',
    doctorName: data.doctorName || '--',
    deptId: data.deptId || '',
    deptName: data.deptName || '--',
    encounterId: data.encounterId || ''
  })
  if (data.registerId && !registerList.value.some(item => item.registerId === data.registerId)) {
    registerList.value.unshift({
      registerId: data.registerId,
      registerNo: data.registerNo,
      patientName: data.patientName,
      gender: data.gender,
      age: data.age
    })
  }
}

const onRegisterChange = (registerId) => {
  if (!registerId) {
    resetPatientInfo()
    return
  }
  const data = registerList.value.find(item => item.registerId === registerId)
  if (data) fillPatientFromApply(data)
}

const resetPatientInfo = () => {
  Object.assign(patientInfo, {
    registerId: '', registerNo: '', patientId: '', patientName: '--',
    gender: '--', age: 0, doctorId: '', doctorName: '--',
    deptId: '', deptName: '--', encounterId: ''
  })
}

// ============ 患者信息 ============
const patientInfo = reactive({
  registerId: '',
  registerNo: '',
  patientId: '',
  patientName: '--',
  gender: '--',
  age: 0,
  doctorId: '',
  doctorName: '--',
  deptId: '',
  deptName: '--',
  encounterId: ''
})

// ============ 医技项目下拉框 ============
const selectedTechId = ref(null)
const techList = ref([])

const loadTechList = () => {
  listTechnology({ techType: 'CHECK', status: '0', pageNum: 1, pageSize: 100 })
    .then(res => {
      const data = res.rows || (res.data && res.data.rows) || res.data || []
      console.log('医技项目列表:', data)
      techList.value = data
    })
    .catch(err => {
      console.error('加载医技项目失败:', err)
    })
}

// ============ CT文件上传与影像 ============
const ctContainer = ref(null)
const uploadedFileName = ref('')
const fileSelected = ref(false)
const currentSlice = ref(1)
const totalSlices = ref(0)
const zoom = ref(1)
const showAnnotated = ref(true)

const imageStyle = computed(() => ({ transform: `scale(${zoom.value})` }))

// 当前显示的CT图像：支持Base64（实时分析）和URL（加载已保存切片）
const ctImageDisplay = computed(() => {
  if (!analysisResult.value) return ''
  // 情况1：从数据库加载的已保存切片 → 使用 imagePath
  if (savedSlicesLoaded.value && savedSlices.value.length > 0) {
    const idx = currentSlice.value - 1
    if (idx >= 0 && idx < savedSlices.value.length) {
      const slice = savedSlices.value[idx]
      const path = showAnnotated.value ? slice.imagePath : (slice.rawImagePath || slice.imagePath)
      return path || ''
    }
    return ''
  }
  // 情况2：实时分析返回的Base64 → data:image/png;base64,...
  const slices = showAnnotated.value
    ? (analysisResult.value.slices || [])
    : (analysisResult.value.rawSlices || [])
  if (!slices.length) return ''
  const idx = currentSlice.value - 1
  if (idx < 0 || idx >= slices.length) return ''
  const src = slices[idx]
  // 判断是URL还是Base64
  if (src.startsWith('/') || src.startsWith('http')) return src
  return 'data:image/png;base64,' + src
})

// 当前Z层
const currentSliceZ = computed(() => {
  if (!analysisResult.value || !analysisResult.value.sliceIndices) return currentSlice.value
  const idx = currentSlice.value - 1
  return idx >= 0 && idx < analysisResult.value.sliceIndices.length
    ? analysisResult.value.sliceIndices[idx] : currentSlice.value
})

const zoomIn = () => { zoom.value = Math.min(zoom.value + 0.2, 3) }
const zoomOut = () => { zoom.value = Math.max(zoom.value - 0.2, 0.5) }
const resetZoom = () => { zoom.value = 1 }
const prevSlice = () => { if (currentSlice.value > 1) currentSlice.value-- }
const nextSlice = () => { if (currentSlice.value < totalSlices.value) currentSlice.value++ }

// ============ 分析状态 ============
const loading = ref(false)
const loadingProgress = ref(0)
const loadingText = ref('正在上传CT文件...')
const analyzing = ref(false)
const analysisDone = ref(false)
const analysisResult = ref(null)
const saving = ref(false)
const savingSlice = ref(false)
const savedSliceImageUrl = ref('')
const currentStage = ref(0)
const stage1Done = ref(false)
const stage2Done = ref(false)
const stage3Done = ref(false)

const pipelineStatus = computed(() => {
  if (analyzing.value) return { tag: 'warning', text: '分析中...' }
  if (analysisDone.value) return { tag: 'success', text: '已完成' }
  if (fileSelected.value) return { tag: 'info', text: '待分析' }
  return { tag: '', text: '未开始' }
})

// ============ 结节数据 ============
const allCandidates = ref([])
const candidatePage = ref(1)

const paginatedCandidates = computed(() => {
  const start = (candidatePage.value - 1) * 5
  return allCandidates.value.slice(start, start + 5)
})

// 汇总统计
const stats = computed(() => {
  if (!analysisResult.value) return { solidNodules: 0, ggoNodules: 0, calcifiedNodules: 0, highRiskNodules: 0 }
  const s = analysisResult.value.stats || {}
  return {
    solidNodules: s.solidNodules || 0,
    ggoNodules: s.ggoNodules || 0,
    calcifiedNodules: s.calcifiedNodules || 0,
    highRiskNodules: s.highRiskNodules || 0
  }
})

const malignancySummary = computed(() => {
  const m = analysisResult.value?.malignancyAssessment || {}
  return {
    overallRisk: m.overallRisk || '待评估',
    conclusion: m.conclusion || '上传真实CT文件后自动完成风险评估'
  }
})

// HU分类样式
const getHuClass = (huMean) => {
  if (huMean > 100) return 'hu-high'
  if (huMean >= 30) return 'hu-mid'
  return 'hu-low'
}

const getRiskTag = (risk) => {
  if (risk === '高风险') return 'danger'
  if (risk === '中风险') return 'warning'
  return 'success'
}

// ============ 文件选择与分析 ============
const handleFileSelect = async (uploadFile) => {
  const file = uploadFile.raw
  if (!file) return

  const allowedExts = ['.zip', '.mhd', '.nii', '.nii.gz', '.dcm']
  const fileName = file.name.toLowerCase()
  const isValid = allowedExts.some(ext => fileName.endsWith(ext))
  if (!isValid) {
    ElMessage.warning('请上传肺部CT影像文件（DICOM zip / MHD / NIfTI / DCM）')
    return
  }

  if (file.size > 500 * 1024 * 1024) {
    ElMessage.warning('CT文件不能超过500MB')
    return
  }

  uploadedFileName.value = file.name
  fileSelected.value = true

  // 自动开始分析
  await doAnalyze(file)
}

const startAIAnalysis = async () => {
  if (!fileSelected.value) {
    ElMessage.warning('请先上传CT影像文件')
    return
  }
  // 重新选文件
  document.querySelector('.ct-upload input[type="file"]')?.click()
}

const doAnalyze = async (file) => {
  loading.value = true
  analyzing.value = true
  loadingProgress.value = 0
  analysisResult.value = null
  analysisDone.value = false
  stage1Done.value = false
  stage2Done.value = false
  stage3Done.value = false
  allCandidates.value = []

  // 模拟进度
  const progressTimer = setInterval(() => {
    if (loadingProgress.value < 90) {
      loadingProgress.value += Math.random() * 10
      if (loadingProgress.value > 20) { currentStage.value = 1; loadingText.value = '正在读取真实CT体数据并检测结节...' }
      if (loadingProgress.value > 48) { currentStage.value = 2; loadingText.value = '正在提取肺实质掩码并执行结节精确分割...' }
      if (loadingProgress.value > 72) { currentStage.value = 3; loadingText.value = '正在计算良恶性风险概率...' }
      if (loadingProgress.value > 86) loadingText.value = '正在生成标注切片和结构化结果...'
    }
  }, 600)

  try {
    currentStage.value = 1
    const res = await analyzeLungCt(file, { max_slices: 80, enable_autodl: false })

    clearInterval(progressTimer)
    loadingProgress.value = 100
    loadingText.value = '分析完成！'

    const data = res.data || res
    if (data.success) {
      analysisResult.value = data
      totalSlices.value = data.rawSlices ? data.rawSlices.length : data.totalSlices
      currentSlice.value = 1
      stage1Done.value = data.pipeline?.noduleDetection === 'completed'
      stage2Done.value = ['completed', 'completed_no_target'].includes(data.pipeline?.segmentation)
      stage3Done.value = ['completed', 'completed_no_target'].includes(data.pipeline?.malignancyClassification)
      analysisDone.value = true

      // 解析结节候选
      if (data.candidates && Array.isArray(data.candidates)) {
        allCandidates.value = data.candidates.map(c => ({
          ...c,
          _originalSliceIndex: c.slice_index
        }))
        candidatePage.value = 1
      }

      // 自动填充诊断表单
      autoFillDiagnosis(data)

      const noduleCount = allCandidates.value.length
      ElMessage.success(`肺部CT全流程完成：检测/分割/良恶性评估已执行，检出 ${noduleCount} 个结节候选，共 ${totalSlices.value} 层切片`)
    } else {
      ElMessage.error(data.error || 'CT分析失败')
    }
  } catch (error) {
    clearInterval(progressTimer)
    console.error('CT分析失败:', error)
    const msg = error.response?.data?.msg || error.message || ''
    if (msg.includes('ResourceAccess') || msg.includes('Network Error') || msg.includes('timeout')) {
      ElMessage.error('肺部CT分析服务未启动，请先启动Python服务：python lung_ct_segment.py --port 5004')
    } else if (msg.includes('MaxUploadSize')) {
      ElMessage.error('CT文件超过上传限制')
    } else {
      ElMessage.error('CT分析失败: ' + (msg || '未知错误'))
    }
  } finally {
    clearInterval(progressTimer)
    loading.value = false
    analyzing.value = false
    currentStage.value = 0
    loadingProgress.value = 0
  }
}

// 自动填充影像所见（AI客观分析结果，供门诊医生参考）
const autoFillDiagnosis = (data) => {
  const candidates = data.candidates || []
  const totalNodules = candidates.length

  let findingsText = '肺部CT平扫示：\n'
  if (totalNodules === 0) {
    findingsText += '双肺纹理清晰，未见明显异常密度影。肺实质HU值分布正常，未见明确结节或肿块。\n'
    findingsText += '肺结节检测、肺实质/结节分割及良恶性风险评估流程已完成，未触发可评估结节目标。'
  } else {
    const solid = candidates.filter(c => c.type === 'solid')
    const ggo = candidates.filter(c => c.type === 'ggo')
    const calc = candidates.filter(c => c.type === 'calcified')
    const topRisk = [...candidates].sort((a, b) => (b.malignancyProbability || 0) - (a.malignancyProbability || 0))[0]

    if (solid.length > 0) {
      const avgDiam = (solid.reduce((s, c) => s + c.diameter_mm, 0) / solid.length).toFixed(1)
      findingsText += `可见${solid.length}个实性结节，平均直径约${avgDiam}mm，HU均值范围[${solid.map(c => c.hu_mean.toFixed(0)).join(', ')}]。\n`
    }
    if (ggo.length > 0) {
      findingsText += `可见${ggo.length}个磨玻璃密度影(GGO)，需结合临床随访观察。\n`
    }
    if (calc.length > 0) {
      findingsText += `可见${calc.length}个钙化灶，HU>200，符合良性钙化表现。\n`
    }
    findingsText += `已完成结节精确分割，生成${data.segmentationAssessment?.count || totalNodules}个结节轮廓/掩码区域。\n`
    if (topRisk) {
      findingsText += `良恶性风险评估：最高风险结节位于Z层${topRisk.slice_z}，${topRisk.malignancyRisk || '低风险'}，概率约${Math.round((topRisk.malignancyProbability || 0) * 100)}%，提示：${topRisk.riskAdvice || data.malignancyAssessment?.conclusion || ''}`
    }
  }
  diagnosisForm.findings = findingsText
}

// ============ 表单（检验医生仅备注，不写诊断报告） ============
const diagnosisForm = reactive({
  findings: '',
  remark: ''
})

// ============ 保存当前切片为检测图像 ============
function dataUrlToFile(dataUrl, fileName) {
  const [header, body] = dataUrl.split(',')
  const mime = header.match(/data:(.*?);base64/)?.[1] || 'image/png'
  const binary = atob(body)
  const bytes = new Uint8Array(binary.length)
  for (let i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i)
  return new File([bytes], fileName, { type: mime })
}

async function saveCurrentLungSlice() {
  const image = ctImageDisplay.value
  if (!image || !image.startsWith('data:image')) {
    ElMessage.warning('当前切片无可保存的图像')
    return
  }
  savingSlice.value = true
  try {
    const fileName = `lung_ct_${currentApplyId.value || 'apply'}_slice${currentSlice.value}_${Date.now()}.png`
    const file = dataUrlToFile(image, fileName)
    const res = await uploadFile(file)
    savedSliceImageUrl.value = res.data?.url || res.url || ''
    if (savedSliceImageUrl.value) {
      ElMessage.success('当前切片已保存为检测结果图像')
    }
  } catch (e) {
    console.warn('切片上传失败:', e)
    ElMessage.error('切片上传失败，请重试')
  } finally {
    savingSlice.value = false
  }
}

function buildDiagnosisPayload() {
  const candidates = allCandidates.value || []
  return JSON.stringify({
    checkType: 'LUNG_CT',
    findings: diagnosisForm.findings,
    impression: malignancySummary.value.conclusion,
    conclusion: malignancySummary.value.overallRisk,
    remark: diagnosisForm.remark,
    stats: {
      solidNodules: stats.value.solidNodules,
      ggoNodules: stats.value.ggoNodules,
      calcifiedNodules: stats.value.calcifiedNodules,
      highRiskNodules: stats.value.highRiskNodules
    },
    detectionClassification: analysisResult.value?.detectionClassification || null,
    segmentationAssessment: analysisResult.value?.segmentationAssessment || null,
    segmentationData: analysisResult.value?.segmentationData || null,
    detectionResult: candidates.length > 0 ? candidates.map(c => ({
      type: c.type, diameter_mm: c.diameter_mm, sliceZ: c.slice_z,
      classification: c.classification,
      segmentation: c.segmentation,
      malignancyRisk: c.malignancyRisk,
      malignancyProbability: c.malignancyProbability
    })) : null,
    malignancyAssessment: analysisResult.value?.malignancyAssessment || null,
    pipeline: analysisResult.value?.pipeline || null
  })
}

function buildSliceSavePayload(imageUrlSaved) {
  const result = analysisResult.value
  if (!result || savedSlicesLoaded.value) return null
  const slices = result.slices || []
  const rawSlices = result.rawSlices || []
  if (!slices.length) return null
  return {
    applyId: currentApplyId.value,
    techId: selectedTechId.value,
    checkType: 'LUNG_CT',
    patientId: patientInfo.patientId || null,
    doctorId: patientInfo.doctorId || null,
    fileName: uploadedFileName.value || 'lung_ct',
    remark: diagnosisForm.remark,
    segmentationData: JSON.stringify({
      segmentationData: result.segmentationData || {},
      segmentationAssessment: result.segmentationAssessment || null
    }),
    detectionResult: JSON.stringify({
      detectionClassification: result.detectionClassification || null,
      candidates: allCandidates.value || [],
      malignancyAssessment: result.malignancyAssessment || null,
      pipeline: result.pipeline || null,
      representativeImageUrl: imageUrlSaved || null
    }),
    slices: slices.map((sliceImage, index) => {
      const stat = result.sliceHuStats?.[index] || {}
      return {
        sliceImage,
        rawImage: rawSlices[index],
        sliceZ: result.sliceIndices?.[index] ?? index,
        huMin: stat.hu_min,
        huMax: stat.hu_max,
        huMean: stat.hu_mean,
        sliceThickness: result.sliceThickness,
        pixelSpacingX: result.pixelSpacingX,
        pixelSpacingY: result.pixelSpacingY
      }
    })
  }
}

async function saveOrUpdateExamResult(payload) {
  const res = await listResult({ applyId: payload.applyId, sort: 1, pageNum: 1, pageSize: 1 })
  const exists = res.rows?.[0]
  if (exists?.resultId) return updateResult({ ...exists, ...payload, resultId: exists.resultId })
  return addResult(payload)
}

// ============ 保存诊断结果到检查检验结果表 ============
const saveDiagnosis = async () => {
  if (!currentApplyId.value) {
    ElMessage.warning('缺少申请单ID，不能保存检查结果')
    return
  }
  saving.value = true
  try {
    // 优先使用已保存的切片图像URL，否则自动上传当前切片
    let imageUrlSaved = savedSliceImageUrl.value
    if (!imageUrlSaved && ctImageDisplay.value && ctImageDisplay.value.startsWith('data:image')) {
      const fileName = `lung_ct_${currentApplyId.value}_slice${currentSlice.value}_${Date.now()}.png`
      const file = dataUrlToFile(ctImageDisplay.value, fileName)
      const res = await uploadFile(file)
      imageUrlSaved = res.data?.url || res.url || null
    }
    const slicePayload = buildSliceSavePayload(imageUrlSaved)
    if (slicePayload) {
      await saveCtSlices(slicePayload)
    }
    // 更新申请单状态
    await updateApply({
      applyId: currentApplyId.value,
      examResult: buildDiagnosisPayload(),
      imageUrl: imageUrlSaved,
      examTime: formatDateTime(),
      applyStatus: '已完成'
    })
    // 保存到检查检验结果表
    await saveOrUpdateExamResult({
      applyId: currentApplyId.value,
      imageUrl: imageUrlSaved,
      imageFind: diagnosisForm.findings || null,
      diagnosisOpinion: malignancySummary.value.conclusion || null,
      diagnosisResult: malignancySummary.value.overallRisk || null,
      sort: 1,
      status: '1',
      reportTime: formatDateTime()
    })
    savedSliceImageUrl.value = ''
    ElMessage.success('诊断结果已保存到检查检验结果表')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败: ' + (error.response?.data?.msg || error.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

// 已保存切片（从数据库加载）
const savedSlices = ref([])
const savedSlicesLoaded = ref(false)

const loadSavedSlices = async (applyId) => {
  try {
    const res = await getSlicesByApplyId(applyId)
    const data = res.data || res
    if (Array.isArray(data)) {
      savedSlices.value = data
      savedSlicesLoaded.value = data.length > 0
      totalSlices.value = data.length
      currentSlice.value = 1
      if (data.length > 0) {
        restoreSavedAnalysis(data)
        analysisDone.value = true
      }
    }
  } catch (e) {
    console.error('加载切片失败:', e)
  }
}

function safeJsonParse(text, fallback = null) {
  if (!text) return fallback
  try { return JSON.parse(text) } catch { return fallback }
}

function restoreSavedAnalysis(rows) {
  const first = rows[0] || {}
  const detection = safeJsonParse(first.detectionResult, {})
  const segmentation = safeJsonParse(first.segmentationData, {})
  const candidates = detection.candidates || detection.detectionClassification?.items || []
  allCandidates.value = candidates.map(c => ({
    ...c,
    slice_z: c.slice_z ?? c.sliceZ,
    diameter_mm: c.diameter_mm ?? c.diameterMm,
    area_mm2: c.area_mm2 ?? c.areaMm2,
    hu_mean: c.hu_mean ?? c.huMean,
    _originalSliceIndex: c.slice_index ?? c.sliceIndex
  }))
  analysisResult.value = {
    success: true,
    totalSlices: rows.length,
    totalSlicesOrig: rows.length,
    sliceIndices: rows.map((row, index) => row.sliceZ ?? index),
    slices: rows.map(row => row.imagePath).filter(Boolean),
    rawSlices: rows.map(row => row.rawImagePath || row.imagePath).filter(Boolean),
    sliceHuStats: rows.map(row => ({
      slice_index: row.sliceIndex,
      slice_z: row.sliceZ,
      hu_min: row.huMin,
      hu_max: row.huMax,
      hu_mean: row.huMean
    })),
    stats: detection.detectionClassification?.count != null ? {
      solidNodules: allCandidates.value.filter(c => c.type === 'solid').length,
      ggoNodules: allCandidates.value.filter(c => c.type === 'ggo').length,
      calcifiedNodules: allCandidates.value.filter(c => c.type === 'calcified').length,
      highRiskNodules: allCandidates.value.filter(c => c.malignancyRisk === '高风险').length
    } : {},
    candidates: allCandidates.value,
    detectionClassification: detection.detectionClassification || null,
    segmentationData: segmentation.segmentationData || {},
    segmentationAssessment: segmentation.segmentationAssessment || null,
    malignancyAssessment: detection.malignancyAssessment || null,
    pipeline: detection.pipeline || {
      noduleDetection: 'completed',
      segmentation: 'completed',
      malignancyClassification: 'completed'
    }
  }
  stage1Done.value = true
  stage2Done.value = true
  stage3Done.value = true
  candidatePage.value = 1
}

// ============ 导航与生命周期 ============
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
  currentApplyId.value = applyId
  try {
    const res = await getApply(applyId)
    fillPatientFromApply(res.data || {})
    await loadSavedSlices(applyId)
  } catch (e) {
    ElMessage.error('申请单加载失败，无法执行检查')
    router.back()
  }
}

onMounted(() => {
  // 预加载挂号列表
  searchRegisters('')
  // 加载医技项目列表
  loadTechList()
  loadApplyDetail()

  // 从URL参数自动填充：如果是卡片进入，自动匹配医技项目
  const { techName, registerId } = route.query
  if (techName && techList.value.length === 0) {
    // techList还未加载完毕，等加载完再匹配
    const checkAndMatch = setInterval(() => {
      if (techList.value.length > 0) {
        clearInterval(checkAndMatch)
        const matched = techList.value.find(t => t.techName.includes(techName) || techName.includes(t.techName))
        if (matched) selectedTechId.value = matched.id
      }
    }, 200)
    setTimeout(() => clearInterval(checkAndMatch), 3000)
  }
})
</script>

<style scoped lang="scss">
.lung-ct-page {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 380px;
  align-items: start;
  gap: 14px;
  min-height: 100vh;
  padding: 18px;
  background: linear-gradient(180deg, #EAF0F4 0, #F6F8FA 260px, #F5F7FA 100%);
  color: #1E293B;

  .page-header {
    grid-column: 1 / -1;
    display: flex; align-items: center; justify-content: space-between; gap: 16px;
    padding: 16px 18px;
    background: #FFFFFF; border: 1px solid #E2E8F0; border-radius: 8px;
    box-shadow: 0 8px 24px rgba(15,23,42,.06);

    .header-left { display: flex; align-items: center; gap: 18px; min-width: 0; }
    .back-btn { flex: 0 0 auto; color: #64748B; font-size: 14px; &:hover { color: #16614D; } }
    .header-title {
      display: flex; align-items: center; gap: 12px; min-width: 0;
      .title-badge { flex: 0 0 auto; padding: 4px 10px; background: #16614D; color: #FFF; border-radius: 4px; font-size: 12px; font-weight: 700; }
      h1 { margin: 0; overflow: hidden; color: #0F172A; font-size: 20px; font-weight: 700; line-height: 1.3; white-space: nowrap; text-overflow: ellipsis; }
    }
    .header-actions { display: flex; flex: 0 0 auto; flex-wrap: wrap; justify-content: flex-end; gap: 10px; }
  }

  .patient-bar {
    grid-column: 1 / -1;
    display: grid; grid-template-columns: repeat(5, minmax(120px, 1fr)); gap: 10px;
    padding: 12px; background: #FFF; border: 1px solid #E2E8F0; border-radius: 8px;
    box-shadow: 0 6px 18px rgba(15,23,42,.04);
    .patient-item { display: flex; flex-direction: column; gap: 4px; padding: 8px 10px; background: #F8FAFC; border: 1px solid #EEF2F7; border-radius: 6px;
      .label { color: #64748B; font-size: 12px; line-height: 1.2; }
      .value { overflow: hidden; color: #0F172A; font-size: 14px; font-weight: 600; white-space: nowrap; text-overflow: ellipsis; }
      .full-select { width: 100%; }
    }
  }
  .no-patient-bar { grid-column: 1 / -1; }

  .main-content {
    display: contents;
    .viewer-col, .side-col { display: flex; flex-direction: column; width: auto !important; max-width: none !important; padding: 0 !important; flex: none !important; }
    .viewer-col { grid-row: 3; grid-column: 1; }
    .side-col { grid-row: 3 / span 2; grid-column: 2; gap: 14px; }

    .panel {
      display: flex; overflow: hidden; flex-direction: column; min-width: 0;
      background: #FFF; border: 1px solid #E2E8F0; border-radius: 8px;
      box-shadow: 0 8px 24px rgba(15,23,42,.06);
      .panel-header { display: flex; align-items: center; justify-content: space-between; gap: 12px; min-height: 50px; padding: 12px 14px; border-bottom: 1px solid #E2E8F0;
        .panel-title { display: flex; align-items: center; gap: 8px; color: #0F172A; font-size: 15px; font-weight: 700; .el-icon { color: #16614D; } }
        .panel-tools { display: flex; align-items: center; gap: 2px; }
      }
      .panel-body { padding: 14px; }
    }

    .ct-panel {
      flex: 1; height: 100%;
      .ct-viewer { display: flex; flex: 1; flex-direction: column; padding: 0; }
      .ct-image-container {
        position: relative; display: flex; align-items: center; justify-content: center;
        min-height: 470px; aspect-ratio: 16/10; overflow: hidden;
        background: radial-gradient(circle at center, rgba(31,41,55,.9), rgba(3,7,18,1)), #05070A;
        .ct-placeholder { width: min(520px, calc(100% - 40px)); color: #94A3B8; text-align: center;
          .el-icon { margin-bottom: 10px; color: #64748B; }
          p { margin: 0 0 4px; color: #E2E8F0; font-size: 14px; &.sub { margin-bottom: 16px; color: #94A3B8; font-size: 12px; } }
          :deep(.el-upload-dragger) { padding: 0; background: transparent; border: 0; }
          .upload-area { padding: 24px 32px; background: rgba(15,23,42,.72); border: 1px dashed #64748B; border-radius: 8px; cursor: pointer; transition: border-color .2s, background .2s;
            &:hover { background: rgba(22,97,77,.18); border-color: #34D399; }
            p { margin: 8px 0 0; color: #CBD5E1; font-size: 13px; &.sub { margin-top: 4px; color: #94A3B8; font-size: 11px; } }
          }
        }
        .ct-loading { width: min(360px, calc(100% - 40px)); color: #CBD5E1; text-align: center;
          .is-loading { margin-bottom: 16px; color: #34D399; animation: rotating 1.5s linear infinite; }
          p { margin: 0 0 12px; font-size: 14px; }
          .sub { margin-top: 8px; color: #94A3B8; font-size: 12px; }
        }
        .ct-image { max-width: 100%; max-height: 100%; transition: transform .2s ease; }
      }
      .series-nav {
        display: grid; grid-template-columns: auto minmax(0,1fr) auto minmax(110px,auto); align-items: center;
        gap: 10px; padding: 12px 14px; background: #F8FAFC; border-top: 1px solid #E2E8F0;
        .nav-center { display: flex; min-width: 0; flex-direction: column; gap: 2px;
          .nav-slice-label { color: #475569; font-size: 12px; strong { color: #0F172A; font-family: ui-monospace, monospace; } }
          .nav-z-info { color: #94A3B8; font-size: 10px; }
          .nav-slider { margin-top: 2px; }
        }
        .nav-filename { overflow: hidden; max-width: 180px; color: #64748B; font-size: 11px; font-family: ui-monospace, monospace; text-overflow: ellipsis; white-space: nowrap; }
      }
      .save-slice-bar {
        display: flex; align-items: center; gap: 10px; padding: 10px 14px; background: #F0FDF4; border-top: 1px solid #BBF7D0;
        .saved-tip { display: flex; align-items: center; gap: 4px; color: #16A34A; font-size: 12px; }
      }
    }

    .pipeline-panel {
      flex: 0 0 auto;
      .pipeline-flow { padding: 16px 14px; position: relative; display: flex; flex-direction: column; gap: 0;
        .flow-line { position: absolute; left: 28px; top: 50px; bottom: 50px; width: 2px; background: linear-gradient(180deg, #CBD5E1, #1A6B54, #CBD5E1); }
        .stage-card {
          display: flex; align-items: flex-start; gap: 10px; padding: 12px; margin-bottom: 6px;
          border-radius: 10px; position: relative; z-index: 1; border: 2px solid transparent; transition: all .3s;
          background: #F8FAFC;
          &.active { border-color: #1A6B54; background: #E8F3EF; }
          &.done { border-color: #D1E7DD; background: #F0FAF4; }
          &.running { border-color: #F59E0B; background: #FFFBEB; }
          &.placeholder { opacity: .55; }
          .stage-num { width: 32px; height: 32px; border-radius: 50%; flex-shrink: 0; display: flex; align-items: center; justify-content: center; background: #E2E8F0; color: #64748B; font-weight: 700; font-size: 12px; }
          &.active .stage-num, &.done .stage-num { background: #1A6B54; color: #fff; }
          &.running .stage-num { background: #F59E0B; color: #fff; }
          &.placeholder .stage-num { background: #E2E8F0; color: #94A3B8; }
          .stage-info { flex: 1;
            .stage-name { font-size: 13px; font-weight: 700; color: #1E293B; margin-bottom: 2px; display: flex; align-items: center; gap: 6px; }
            .stage-model { font-size: 10px; color: #94A3B8; font-family: monospace; margin-bottom: 3px; }
            .stage-desc { font-size: 11px; color: #64748B; line-height: 1.4; }
          }
          .stage-status {
            .done { color: #27AE60; font-size: 20px; }
            .run { color: #F59E0B; font-size: 20px; animation: spin 1s linear infinite; }
            .wait { color: #CBD5E1; font-size: 16px; }
          }
        }
      }
    }

    .diagnosis-panel {
      flex: 1;
      .panel-body { flex: 1; overflow: auto; }
      :deep(.el-form-item) { margin-bottom: 12px; }
      :deep(.el-form-item__label) { padding-bottom: 4px; color: #475569; font-weight: 600; }
      :deep(.el-textarea__inner) { resize: vertical; }
      :deep(.el-radio-group) { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 6px 8px; }
      :deep(.el-radio) { height: auto; margin-right: 0; line-height: 1.3; white-space: normal; }
      :deep(.el-checkbox-group) { display: grid; grid-template-columns: repeat(1, minmax(0, 1fr)); gap: 4px 0; }
      :deep(.el-checkbox) { height: auto; margin-right: 0; line-height: 1.3; white-space: normal; }
    }
  }

  // ===== 标注结果区 =====
  .annotation-section {
    grid-row: 4; grid-column: 1;
    display: flex; flex-direction: column;
    overflow: hidden; background: #FFFFFF; border: 1px solid #E2E8F0; border-radius: 8px;
    box-shadow: 0 8px 24px rgba(15,23,42,.06);
    margin-top: 14px;

    .annotation-header {
      display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 12px;
      padding: 14px 16px; background: linear-gradient(135deg, #E8F3EF 0%, #F7FBF9 100%);
      border-bottom: 1px solid #CFE5DC;
      .annotation-title { display: flex; align-items: center; gap: 10px; color: #145744; font-size: 16px; font-weight: 700; .el-icon { color: #16614D; } }
      .annotation-stats { display: flex; align-items: center; flex-wrap: wrap; gap: 8px;
        .stat-badge { display: flex; align-items: center; gap: 8px; min-width: 88px; padding: 6px 10px; background: rgba(255,255,255,.72); border: 1px solid #CFE5DC; border-radius: 6px;
          .badge-num { font-family: ui-monospace, monospace; font-size: 18px; font-weight: 800; line-height: 1; }
          .badge-txt { color: #5B766E; font-size: 11px; }
          &.solid { color: #C2410C; }
          &.ggo { color: #D97706; }
          &.calc { color: #0F766E; }
          &.info { color: #16614D; }
        }
      }
    }

    .nodule-table-area {
      padding: 14px 16px;
      h4 { display: flex; align-items: center; gap: 8px; margin: 0 0 12px; color: #0F172A; font-size: 14px;
        .sec-dot { width: 8px; height: 8px; border-radius: 50%; background: #16614D; flex-shrink: 0; }
      }
      .hu-badge { padding: 2px 8px; border-radius: 4px; font-size: 11px; font-weight: 600;
        &.hu-high { background: #FEE2E2; color: #DC2626; }
        &.hu-mid { background: #FEF3C7; color: #D97706; }
        &.hu-low { background: #DBEAFE; color: #2563EB; }
      }
      .nodule-pagination { margin-top: 10px; justify-content: center; }
    }
    .nodule-empty { padding: 40px 20px; text-align: center; color: #64748B;
      .el-icon { margin-bottom: 8px; color: #27AE60; }
      p { margin: 0 0 4px; font-size: 14px; &.sub { color: #94A3B8; font-size: 12px; } }
    }
  }
}

// 响应式
@media (min-width: 1200px) {
  .lung-ct-page .main-content .side-col { position: sticky; top: 12px; max-height: calc(100vh - 24px); }
}

@media (max-width: 1199px) {
  .lung-ct-page {
    display: block;
    .page-header, .patient-bar { margin-bottom: 14px; }
    .main-content { display: flex; flex-direction: column;
      .viewer-col, .side-col { width: 100% !important; max-width: 100% !important; }
      .side-col { margin-top: 14px; }
    }
  }
}

@media (max-width: 640px) {
  .lung-ct-page {
    .page-header { flex-direction: column; align-items: stretch;
      .header-left { flex-direction: column; gap: 10px; }
      .header-title h1 { white-space: normal; }
      .header-actions { justify-content: flex-start; }
    }
    .patient-bar { grid-template-columns: 1fr; }
    .main-content .diagnosis-panel :deep(.el-radio-group) { grid-template-columns: 1fr; }
  }
}

@keyframes rotating { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
@keyframes spin { to { transform: rotate(360deg); } }
</style>
