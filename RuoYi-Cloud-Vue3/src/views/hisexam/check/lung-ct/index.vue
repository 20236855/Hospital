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
              <el-upload
                v-if="ctImageDisplay || uploadedFileName"
                class="ct-reupload"
                :auto-upload="false"
                :show-file-list="false"
                :on-change="handleFileSelect"
                accept=".zip,.mhd,.nii,.nii.gz,.dcm"
              >
                <el-button link>
                  <el-icon><Upload /></el-icon>
                  <span>重新上传</span>
                </el-button>
              </el-upload>
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
              <img v-if="ctImageDisplay && !loading" :src="ctImageDisplay" class="ct-image" :style="imageStyle" @error="handleMainImageError" />
              <div class="representative-ribbon" v-if="ctImageDisplay && !loading">
                服务端代表切片
              </div>
            </div>

            <!-- 序列导航 -->
            <div class="series-nav" v-if="ctImageDisplay || uploadedFileName || totalSlices > 0">
              <el-button text circle @click="prevSlice" :disabled="currentSlice <= 1">
                <el-icon><ArrowLeft /></el-icon>
              </el-button>
              <div class="nav-center">
                <span class="nav-slice-label">
                  原始切片 <strong>{{ currentSlice }}</strong> / {{ totalSlices || 1 }}
                  <el-tag v-if="isRepresentativeSlice" size="small" type="success" effect="plain">代表切片</el-tag>
                </span>
                <span class="nav-z-info" v-if="analysisResult">
                  Z层 {{ currentSliceZ }} / 原始 {{ analysisResult.totalSlicesOrig || totalSlices }} 层
                </span>
                <el-slider
                  v-model="currentSlice"
                  :min="1"
                  :max="Math.max(totalSlices || 1, 1)"
                  :disabled="(totalSlices || 1) <= 1"
                  :show-tooltip="false"
                  class="nav-slider"
                />
              </div>
              <el-button text circle @click="nextSlice" :disabled="currentSlice >= (totalSlices || 1)">
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
                <el-icon color="#4a90d9"><CircleCheck /></el-icon> 已保存，将随诊断结果一起提交
              </span>
            </div>
          </div>
        </div>

        <!-- ==================== 三阶段图像工具区 ==================== -->
        <div class="stage-image-section" v-if="fileSelected || analysisResult || analyzing">
          <div
            v-for="item in stagePanelItems"
            :key="item.no"
            class="stage-image-panel"
            :class="{ running: item.running, done: item.done, waiting: item.waiting }"
          >
            <div class="stage-image-head">
              <div>
                <div class="stage-step">第 {{ item.no }} 步</div>
                <h3>{{ item.title }}</h3>
              </div>
              <el-tag :type="item.tag" size="small" effect="plain">{{ item.status }}</el-tag>
            </div>
            <p class="stage-note">{{ item.note }}</p>
            <div class="stage-image-slot">
              <div v-if="item.visible && item.imageSrc" class="stage-slice-view">
                <img :src="item.imageSrc" :alt="item.title" />
                <svg
                  v-if="item.overlayItems.length"
                  class="stage-overlay"
                  :viewBox="imageViewBox"
                  preserveAspectRatio="xMidYMid meet"
                >
                  <template v-for="(det, idx) in item.overlayItems" :key="idx">
                    <rect
                      v-if="item.key !== 'segmentation'"
                      :x="det.bbox_x1"
                      :y="det.bbox_y1"
                      :width="Math.max(1, det.bbox_x2 - det.bbox_x1)"
                      :height="Math.max(1, det.bbox_y2 - det.bbox_y1)"
                      :class="['stage-box', item.key, riskClass(det)]"
                    />
                    <polygon
                      v-if="item.key === 'segmentation' && contourPoints(det)"
                      :points="contourPoints(det)"
                      class="stage-contour-fill"
                    />
                    <polyline
                      v-if="item.key === 'segmentation' && contourPoints(det)"
                      :points="contourPoints(det)"
                      class="stage-contour-line"
                    />
                    <text
                      v-if="item.key !== 'segmentation'"
                      :x="det.bbox_x1"
                      :y="Math.max(12, det.bbox_y1 - 5)"
                      :class="['stage-label', item.key, riskClass(det)]"
                    >
                      {{ overlayLabel(item.key, det) }}
                    </text>
                  </template>
                </svg>
              </div>
              <div v-else-if="item.running" class="stage-slot-state">
                <el-icon class="is-loading"><Loading /></el-icon>
                <span>{{ item.runningText }}</span>
              </div>
              <div v-else class="stage-slot-state muted">
                <el-icon><Picture /></el-icon>
                <span>{{ item.waitingText }}</span>
              </div>
            </div>
            <div class="stage-slice-nav" v-if="item.visible && item.totalSlices > 1">
              <el-button text circle @click="prevStageSlice(item.key)" :disabled="item.slice <= 1">
                <el-icon><ArrowLeft /></el-icon>
              </el-button>
              <div class="stage-nav-center">
                <span>切片 <strong>{{ item.slice }}</strong> / {{ item.totalSlices }}</span>
                <span class="stage-z">Z层 {{ item.zIndex }}</span>
                <el-slider
                  v-model="stageSlice[item.key]"
                  :min="1"
                  :max="item.totalSlices"
                  :show-tooltip="false"
                  class="stage-slider"
                />
              </div>
              <el-button text circle @click="nextStageSlice(item.key)" :disabled="item.slice >= item.totalSlices">
                <el-icon><ArrowRight /></el-icon>
              </el-button>
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
              <div class="stat-badge info">
                <span class="badge-num">{{ stats.rawCandidateRegions }}</span>
                <span class="badge-txt">原始候选</span>
              </div>
              <div class="stat-badge solid">
                <span class="badge-num">{{ stats.totalNoduleCandidates }}</span>
                <span class="badge-txt">真结节 {{ formatPercent(stats.trueNoduleRatioActual) }}</span>
              </div>
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
                <span class="badge-txt">高风险 {{ formatPercent(stats.highRiskRatioActual) }}</span>
              </div>
              <div class="stat-badge info">
                <span class="badge-num">{{ totalSlices }}</span>
                <span class="badge-txt">切片数</span>
              </div>
            </div>
          </div>

          <div class="nodule-table-area" v-if="allCandidates.length > 0">
            <h4>
              <span class="sec-dot"></span>筛选后保留 {{ allCandidates.length }} 个可信真结节
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
              size="small"
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
              <div class="stage-card" :class="{ active: currentStage === 1, done: stage1Done, running: pipelineRevealing && currentStage === 1 && !stage1Done }">
                <div class="stage-num">01</div>
                <div class="stage-info">
                  <div class="stage-name">候选筛选与结节分类 <el-tag size="small" type="success" effect="plain">CT值算法</el-tag></div>
                  <div class="stage-model">HU-Morphology Detector / AutoDL YOLOv8接口预留</div>
                  <div class="stage-desc">先检测候选区域，再按大小、圆度、离心率和密度筛选可信结节并分类</div>
                </div>
                <div class="stage-status">
                  <el-icon v-if="stage1Done" class="done"><CircleCheckFilled /></el-icon>
                  <el-icon v-else-if="pipelineRevealing && currentStage === 1" class="run"><Loading /></el-icon>
                  <el-icon v-else class="wait"><Clock /></el-icon>
                </div>
              </div>
              <!-- Stage 02: 结节分割（CT值算法 - 已实现） -->
              <div class="stage-card" :class="{ active: currentStage === 2, done: stage2Done, running: pipelineRevealing && currentStage === 2 && !stage2Done }">
                <div class="stage-num">02</div>
                <div class="stage-info">
                  <div class="stage-name">结节精确分割 <el-tag size="small" type="success" effect="plain">CT值算法</el-tag></div>
                  <div class="stage-model">Algorithm: HU阈值 + 形态学分析</div>
                  <div class="stage-desc">基于CT亨氏单位(HU)值，结合肺实质提取、连通域分析和形态学特征，实现结节候选区域检测与分割</div>
                </div>
                <div class="stage-status">
                  <el-icon v-if="stage2Done" class="done"><CircleCheckFilled /></el-icon>
                  <el-icon v-else-if="pipelineRevealing && currentStage === 2" class="run"><Loading /></el-icon>
                  <el-icon v-else class="wait"><Clock /></el-icon>
                </div>
              </div>
              <!-- Stage 03: 良恶性诊断 -->
              <div class="stage-card" :class="{ active: currentStage === 3, done: stage3Done, running: pipelineRevealing && currentStage === 3 && !stage3Done }">
                <div class="stage-num">03</div>
                <div class="stage-info">
                  <div class="stage-name">良恶性鉴别诊断 <el-tag size="small" type="warning" effect="plain">{{ malignancySummary.overallRisk }}</el-tag></div>
                  <div class="stage-model">HU-Morphology Risk / AutoDL DenseNet-121接口预留</div>
                  <div class="stage-desc">综合结节大小、密度、圆度、离心率和HU离散度，输出恶性风险概率与随访建议</div>
                  <div class="stage-desc risk-line" v-if="analysisDone">{{ malignancySummary.conclusion }}</div>
                </div>
                <div class="stage-status">
                  <el-icon v-if="stage3Done" class="done"><CircleCheckFilled /></el-icon>
                  <el-icon v-else-if="pipelineRevealing && currentStage === 3" class="run"><Loading /></el-icon>
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
            <el-tag type="success" size="small">结果写入检查检验表</el-tag>
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
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft, ArrowRight, Cpu, DocumentChecked, Picture, ZoomIn, ZoomOut,
  RefreshRight, Upload, Loading, DataAnalysis, EditPen, Clock,
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
const stageSlice = reactive({
  detection: 1,
  segmentation: 1,
  malignancy: 1
})
const totalSlices = ref(0)
const zoom = ref(1)
const brokenMainImageSrc = ref('')

const imageStyle = computed(() => ({ transform: `scale(${zoom.value})` }))

const sliceCountFromResult = (data) => {
  const browseLen = Array.isArray(data?.sliceImages) && data.sliceImages.length
    ? data.sliceImages.length
    : (Array.isArray(data?.rawSlices) && data.rawSlices.length
        ? data.rawSlices.length
        : (Array.isArray(data?.slices) ? data.slices.length : 0))
  const total = Number(browseLen || data?.totalSlices || data?.totalSlicesOrig || 1)
  return Number.isFinite(total) && total > 0 ? total : 1
}

const stripDataUrlPrefix = (src) => {
  if (typeof src !== 'string') return ''
  const text = src.trim()
  if (!text) return ''
  if (text.startsWith('data:image')) return text.split(',')[1] || ''
  return text
}

const normalizeImageSrc = (src) => {
  if (typeof src !== 'string') return ''
  const text = src.trim()
  if (!text || text === 'null' || text === 'undefined' || text === 'false') return ''
  if (text.startsWith('/') || text.startsWith('http') || text.startsWith('blob:') || text.startsWith('data:image')) return text
  if (/^(profile|dev-api|prod-api|stage-api)\//i.test(text)) return `/${text}`
  if (/\.(png|jpe?g|webp|gif|bmp)(\?.*)?$/i.test(text)) return text.startsWith('/') ? text : `/${text}`
  if (/^[A-Za-z0-9+/=\r\n]+$/.test(text) && text.length > 100) return 'data:image/png;base64,' + text
  return ''
}

const looksLikeImageString = (value) => {
  if (typeof value !== 'string') return false
  const text = value.trim()
  if (!text || text === 'null' || text === 'undefined' || text === 'false') return false
  if (
    text.startsWith('data:image') ||
    text.startsWith('http') ||
    text.startsWith('blob:') ||
    text.startsWith('/') ||
    /^(profile|dev-api|prod-api|stage-api)\//i.test(text) ||
    /\.(png|jpe?g|webp|gif|bmp)(\?.*)?$/i.test(text)
  ) return true
  return text.length > 100 && /^[A-Za-z0-9+/=\r\n]+$/.test(text)
}

const imageFromValue = (value, depth = 0) => {
  if (!value || depth > 4) return ''
  if (looksLikeImageString(value)) return value
  if (Array.isArray(value)) {
    for (const item of value) {
      const image = imageFromValue(item, depth + 1)
      if (image) return image
    }
    return ''
  }
  if (typeof value === 'object') {
    const preferredKeys = [
      'image', 'imageData', 'imageBase64', 'base64', 'src', 'url',
      'sliceImage', 'rawImage', 'annotatedImage', 'annotated',
      'previewImage', 'detectionImage', 'segmentationImage', 'malignancyImage'
    ]
    for (const key of preferredKeys) {
      const image = imageFromValue(value[key], depth + 1)
      if (image) return image
    }
    for (const key of Object.keys(value)) {
      if (/image|slice|png|base64|preview|detect|segment|risk|malign/i.test(key)) {
        const image = imageFromValue(value[key], depth + 1)
        if (image) return image
      }
    }
  }
  return ''
}

const imageFromArrayAt = (value, index = 0) => {
  if (!Array.isArray(value) || value.length === 0) return imageFromValue(value)
  const safeIndex = Math.min(Math.max(Number(index) || 0, 0), value.length - 1)
  return imageFromValue(value[safeIndex]) || imageFromValue(value)
}

const getByPath = (source, path) => {
  return path.split('.').reduce((obj, key) => (obj && obj[key] !== undefined ? obj[key] : undefined), source)
}

const pickImageByKeys = (source, keys) => {
  if (!source) return ''
  for (const key of keys) {
    const direct = source[key]
    const nested = key.includes('.') ? getByPath(source, key) : direct
    const image = imageFromValue(nested)
    if (image) return image
  }
  return ''
}

const pickStageImage = (stageOutputs, stage, fallback = '') => {
  const aliases = {
    detection: [
      'detectionImage', 'detection_image', 'detection', 'detectImage', 'detect_image',
      'classificationImage', 'classification_image', 'noduleDetectionImage',
      'nodule_detection_image', 'stage1Image', 'stage01Image', 'step1Image', 'step01Image',
      'stage1.image', 'stage01.image', 'step1.image', 'step01.image'
    ],
    segmentation: [
      'segmentationImage', 'segmentation_image', 'segmentation', 'segmentImage', 'segment_image',
      'maskImage', 'mask_image', 'segmentedImage', 'segmented_image',
      'stage2Image', 'stage02Image', 'step2Image', 'step02Image',
      'stage2.image', 'stage02.image', 'step2.image', 'step02.image'
    ],
    malignancy: [
      'malignancyImage', 'malignancy_image', 'malignancy', 'riskImage', 'risk_image',
      'riskAssessmentImage', 'risk_assessment_image', 'diagnosisImage', 'diagnosis_image',
      'stage3Image', 'stage03Image', 'step3Image', 'step03Image',
      'stage3.image', 'stage03.image', 'step3.image', 'step03.image'
    ]
  }
  const byAlias = pickImageByKeys(stageOutputs, aliases[stage] || [])
  if (byAlias) return byAlias
  if (Array.isArray(stageOutputs)) {
    const pattern = stage === 'detection'
      ? /detect|class|01|1/i
      : stage === 'segmentation'
        ? /segment|mask|02|2/i
        : /risk|malign|diagnos|03|3/i
    const match = stageOutputs.find(item => pattern.test(String(item?.stage || item?.type || item?.name || item?.title || item?.key || '')))
    const image = imageFromValue(match)
    if (image) return image
  }
  return fallback || ''
}

const pickPreviewImage = (result, stageOutputs) => {
  const direct = pickImageByKeys(result, [
    'previewImage', 'preview_image', 'representativeImage', 'representative_image',
    'representativeSliceImage', 'representative_slice_image', 'mainPreviewImage',
    'main_preview_image', 'preview', 'image'
  ])
  if (direct) return direct
  const sliceIndex = stageOutputs?.sliceIndex ?? stageOutputs?.slice_index ?? result.previewSliceIndex ?? result.preview_slice_index ?? 0
  const fromSliceImages = imageFromArrayAt(result.sliceImages || result.slice_images, sliceIndex)
  if (fromSliceImages) return fromSliceImages
  return imageFromArrayAt(result.slices || result.annotatedSlices || result.annotated_slices || result.rawSlices || result.raw_slices, sliceIndex)
}

const unwrapAnalyzePayload = (res) => {
  const root = res?.data ?? res
  if (root && typeof root === 'object' && ('code' in root || 'msg' in root) && root.data && typeof root.data === 'object') {
    return root.data
  }
  return root && typeof root === 'object' ? root : {}
}

const normalizeAnalyzeResult = (data) => {
  const result = { ...(data || {}) }
  const stageOutputs = result.stageOutputs || result.stage_outputs || {}
  const previewImage = pickPreviewImage(result, stageOutputs)
  result.previewImage = previewImage
  result.previewSliceIndex = result.previewSliceIndex ?? result.preview_slice_index ?? 0
  result.previewSliceZ = result.previewSliceZ ?? result.preview_slice_z ?? 0
  result.totalSlices = result.totalSlices ?? result.total_slices ?? 0
  result.totalSlicesOrig = result.totalSlicesOrig ?? result.total_slices_orig ?? result.totalSlices ?? 0
  result.imageShape = result.imageShape || result.image_shape || []
  result.sliceIndices = result.sliceIndices || result.slice_indices || []
  result.rawSlices = result.rawSlices || result.raw_slices || []
  result.slices = result.slices || result.annotatedSlices || result.annotated_slices || []
  result.sliceImages = result.sliceImages || result.slice_images || []
  result.stageOutputs = {
    detectionImage: pickStageImage(stageOutputs, 'detection', previewImage),
    segmentationImage: pickStageImage(stageOutputs, 'segmentation', previewImage),
    malignancyImage: pickStageImage(stageOutputs, 'malignancy', previewImage),
    sliceIndex: stageOutputs.sliceIndex ?? stageOutputs.slice_index ?? result.previewSliceIndex ?? 0,
    sliceZ: stageOutputs.sliceZ ?? stageOutputs.slice_z ?? result.previewSliceZ ?? 0,
    candidateCount: stageOutputs.candidateCount ?? stageOutputs.candidate_count ?? 0
  }
  return result
}

const logLungCtDebug = (label, payload) => {
  try {
    console.log(`[LUNG-CT] ${label}`, payload)
  } catch (e) {
    console.log(`[LUNG-CT] ${label}`)
  }
}

const realtimeBrowseSlices = computed(() => {
  if (!analysisResult.value) return []
  const source = analysisResult.value.sliceImages?.length
    ? analysisResult.value.sliceImages
    : (analysisResult.value.rawSlices?.length ? analysisResult.value.rawSlices : analysisResult.value.slices)
  return Array.isArray(source) ? source : []
})

const imageViewBox = computed(() => {
  const shape = analysisResult.value?.imageShape || []
  const height = Number(shape[0]) || 512
  const width = Number(shape[1]) || 512
  return `0 0 ${width} ${height}`
})

const isRepresentativeSlice = computed(() => {
  if (!analysisResult.value) return false
  return currentSlice.value === Math.min(Math.max((analysisResult.value.previewSliceIndex ?? 0) + 1, 1), totalSlices.value || 1)
})

const representativeImageSrc = () => {
  if (!analysisResult.value) return ''
  const outputs = analysisResult.value.stageOutputs || {}
  return normalizeImageSrc(
    analysisResult.value.previewImage ||
    outputs.detectionImage ||
    outputs.segmentationImage ||
    outputs.malignancyImage ||
    ''
  )
}

const displayableImageSrc = (src) => {
  const normalized = normalizeImageSrc(src)
  return normalized && normalized !== brokenMainImageSrc.value ? normalized : ''
}

// 当前显示的CT图像：默认定位代表切片，同时支持用滑条浏览返回的原始切片。
const ctImageDisplay = computed(() => {
  if (!analysisResult.value) return ''
  if (savedSlicesLoaded.value && savedSlices.value.length > 0) {
    const idx = Math.min(Math.max(currentSlice.value - 1, 0), savedSlices.value.length - 1)
    const slice = savedSlices.value[idx]
    return displayableImageSrc(slice.rawImagePath || slice.imagePath) || displayableImageSrc(representativeImageSrc())
  }
  const slices = realtimeBrowseSlices.value
  if (slices.length) {
    const idx = Math.min(Math.max(currentSlice.value - 1, 0), slices.length - 1)
    return displayableImageSrc(imageFromValue(slices[idx])) || displayableImageSrc(representativeImageSrc())
  }
  return displayableImageSrc(representativeImageSrc())
})

const stageOutputSrc = (stage) => {
  if (stage === 'detection' && stageDetectionSrc.value) return stageDetectionSrc.value
  if (stage === 'segmentation' && stageSegmentationSrc.value) return stageSegmentationSrc.value
  if (stage === 'malignancy' && stageMalignancySrc.value) return stageMalignancySrc.value
  if (!analysisResult.value) return ''
  const keyMap = {
    detection: 'detectionImage',
    segmentation: 'segmentationImage',
    malignancy: 'malignancyImage'
  }
  return normalizeImageSrc((analysisResult.value.stageOutputs || {})[keyMap[stage]] || analysisResult.value.previewImage)
}

// 当前Z层
const currentSliceZ = computed(() => {
  if (!analysisResult.value) return currentSlice.value
  if (!analysisResult.value.sliceIndices || !analysisResult.value.sliceIndices.length) {
    return analysisResult.value.previewSliceZ ?? currentSlice.value
  }
  const idx = currentSlice.value - 1
  return idx >= 0 && idx < analysisResult.value.sliceIndices.length
    ? analysisResult.value.sliceIndices[idx]
    : (analysisResult.value.previewSliceZ ?? currentSlice.value)
})

const zoomIn = () => { zoom.value = Math.min(zoom.value + 0.2, 3) }
const zoomOut = () => { zoom.value = Math.max(zoom.value - 0.2, 0.5) }
const resetZoom = () => { zoom.value = 1 }
const prevSlice = () => { if (currentSlice.value > 1) currentSlice.value-- }
const nextSlice = () => { if (currentSlice.value < (totalSlices.value || 1)) currentSlice.value++ }
const prevStageSlice = (key) => { if (stageSlice[key] > 1) stageSlice[key]-- }
const nextStageSlice = (key) => { if (stageSlice[key] < (totalSlices.value || 1)) stageSlice[key]++ }

const handleMainImageError = () => {
  const badSrc = ctImageDisplay.value
  if (badSrc) brokenMainImageSrc.value = badSrc
  const previewNo = Math.min(Math.max((analysisResult.value?.previewSliceIndex ?? 0) + 1, 1), totalSlices.value || 1)
  if (currentSlice.value !== previewNo) currentSlice.value = previewNo
  if (fileSelected.value || uploadedFileName.value) {
    ElMessage.warning('当前切片图片加载失败，已尝试切换到代表切片')
  }
}

// ============ 分析状态 ============
const loading = ref(false)
const loadingProgress = ref(0)
const loadingText = ref('正在上传CT文件...')
const analyzing = ref(false)
const analysisDone = ref(false)
const analysisResult = ref(null)
const pipelineRevealing = ref(false)
const saving = ref(false)
const savingSlice = ref(false)
const savedSliceImageUrl = ref('')
const currentStage = ref(0)
const pipelineStageIndex = ref(0)
const stage1Done = ref(false)
const stage2Done = ref(false)
const stage3Done = ref(false)
const stageDetectionSrc = ref('')
const stageSegmentationSrc = ref('')
const stageMalignancySrc = ref('')

const pipelineStatus = computed(() => {
  if (pipelineRevealing.value) {
    return currentStage.value > 0
      ? { tag: 'warning', text: `第 ${String(currentStage.value).padStart(2, '0')} 步出图中...` }
      : { tag: 'warning', text: '准备出图...' }
  }
  if (analyzing.value) return { tag: 'warning', text: '分析中...' }
  if (analysisDone.value) return { tag: 'success', text: '已完成' }
  if (fileSelected.value) return { tag: 'info', text: '待分析' }
  return { tag: '', text: '未开始' }
})

const normalizeCandidate = (c = {}) => {
  const bbox = c.bbox || []
  return {
    ...c,
    slice_index: Number(c.slice_index ?? c.sliceIndex ?? 0),
    slice_z: c.slice_z ?? c.sliceZ ?? c.zIndex ?? c.slice_index ?? c.sliceIndex ?? 0,
    bbox_y1: Number(c.bbox_y1 ?? bbox[0] ?? 0),
    bbox_x1: Number(c.bbox_x1 ?? bbox[1] ?? 0),
    bbox_y2: Number(c.bbox_y2 ?? bbox[2] ?? 0),
    bbox_x2: Number(c.bbox_x2 ?? bbox[3] ?? 0),
    centroid_y: Number(c.centroid_y ?? c.centroidY ?? 0),
    centroid_x: Number(c.centroid_x ?? c.centroidX ?? 0),
    diameter_mm: Number(c.diameter_mm ?? c.diameterMm ?? 0),
    area_mm2: Number(c.area_mm2 ?? c.areaMm2 ?? 0),
    hu_mean: Number(c.hu_mean ?? c.huMean ?? 0)
  }
}

const stageSliceImage = (sliceNo) => {
  const slices = realtimeBrowseSlices.value
  if (!slices.length) return normalizeImageSrc(analysisResult.value?.previewImage || '')
  const idx = Math.min(Math.max(sliceNo - 1, 0), slices.length - 1)
  return normalizeImageSrc(imageFromValue(slices[idx]))
}

const candidatesForSlice = (sliceNo) => {
  const idx = sliceNo - 1
  return allCandidates.value
    .map(normalizeCandidate)
    .filter(c => Number(c.slice_index) === idx)
}

const contourPoints = (det) => {
  const contour = det?.segmentation?.displayContour || det?.segmentation?.contour || []
  if (!Array.isArray(contour) || contour.length < 3) return ''
  return contour
    .map(p => Array.isArray(p) ? `${Number(p[1])},${Number(p[0])}` : '')
    .filter(Boolean)
    .join(' ')
}

const riskClass = (det) => {
  if (det?.malignancyRisk === '高风险') return 'high'
  if (det?.malignancyRisk === '中风险') return 'medium'
  return 'low'
}

const overlayLabel = (stage, det) => {
  if (stage === 'segmentation') return 'SEG'
  if (stage === 'malignancy') return `${det.malignancyRisk || '低风险'} ${Math.round((det.malignancyProbability || 0) * 100)}%`
  const typeLabel = det.type === 'solid' ? '实性' : det.type === 'ggo' ? '磨玻璃' : det.type === 'calcified' ? '钙化' : '候选'
  return `${typeLabel} ${det.diameter_mm || 0}mm`
}

const stagePanelItems = computed(() => {
  const stageMeta = [
    {
      no: '01',
      index: 1,
      key: 'detection',
      title: '肺结节候选筛选与分类',
      note: '先找候选区域，再过滤血管/纹理等假阳性，输出可信结节框、类型和大小分类。',
      runningText: '正在筛选可信肺结节...',
      waitingText: '等待第 01 步输出检测/分类图'
    },
    {
      no: '02',
      index: 2,
      key: 'segmentation',
      title: '结节精确分割',
      note: '输出红色结节轮廓和掩码区域；前一步检测图会继续保留。',
      runningText: '正在生成红色分割结果...',
      waitingText: '等待第 01 步完成后输出红色分割图'
    },
    {
      no: '03',
      index: 3,
      key: 'malignancy',
      title: '良恶性鉴别诊断',
      note: '标出全部低/中/高风险结节区域，颜色区分风险等级。',
      runningText: '正在计算良恶性风险...',
      waitingText: '等待第 02 步完成后输出风险评估图'
    }
  ]
  const doneMap = { 1: stage1Done.value, 2: stage2Done.value, 3: stage3Done.value }
  return stageMeta.map(item => {
    const src = stageOutputSrc(item.key)
    const slice = Math.min(Math.max(stageSlice[item.key] || 1, 1), totalSlices.value || 1)
    const overlayItems = candidatesForSlice(slice)
    const visible = pipelineStageIndex.value >= item.index && !!src
    const running = pipelineRevealing.value && currentStage.value === item.index && !visible
    const done = Boolean(doneMap[item.index] || visible)
    return {
      ...item,
      src,
      imageSrc: stageSliceImage(slice),
      overlayItems,
      slice,
      totalSlices: totalSlices.value || 1,
      zIndex: analysisResult.value?.sliceIndices?.[slice - 1] ?? slice - 1,
      visible,
      running,
      done,
      waiting: !running && !done,
      status: done ? '已出图' : (running ? '运行中' : '等待中'),
      tag: done ? 'success' : (running ? 'warning' : 'info')
    }
  })
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
  if (!analysisResult.value) {
    return {
      rawCandidateRegions: 0,
      totalNoduleCandidates: 0,
      solidNodules: 0,
      ggoNodules: 0,
      calcifiedNodules: 0,
      highRiskNodules: 0,
      trueNoduleRatioActual: 0,
      highRiskRatioActual: 0
    }
  }
  const s = analysisResult.value.stats || {}
  return {
    rawCandidateRegions: s.rawCandidateRegions || 0,
    totalNoduleCandidates: s.totalNoduleCandidates || 0,
    solidNodules: s.solidNodules || 0,
    ggoNodules: s.ggoNodules || 0,
    calcifiedNodules: s.calcifiedNodules || 0,
    highRiskNodules: s.highRiskNodules || 0,
    trueNoduleRatioActual: s.trueNoduleRatioActual || 0,
    highRiskRatioActual: s.highRiskRatioActual || 0
  }
})

const formatPercent = (value) => {
  const n = Number(value)
  if (!Number.isFinite(n)) return '0%'
  return `${Math.round(n * 100)}%`
}

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
const riskTag = getRiskTag

const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms))

const waitForImageReady = (src, timeout = 1800) => {
  if (!src) return Promise.resolve(false)
  return new Promise(resolve => {
    const image = new Image()
    const timer = setTimeout(() => resolve(false), timeout)
    image.onload = () => {
      clearTimeout(timer)
      resolve(true)
    }
    image.onerror = () => {
      clearTimeout(timer)
      resolve(false)
    }
    image.src = src
  })
}

async function revealPipelineOutputs(data) {
  const outputs = data.stageOutputs || {}
  const detectionSrc = normalizeImageSrc(outputs.detectionImage || data.previewImage)
  const segmentationSrc = normalizeImageSrc(outputs.segmentationImage || outputs.detectionImage || data.previewImage)
  const malignancySrc = normalizeImageSrc(outputs.malignancyImage || outputs.segmentationImage || outputs.detectionImage || data.previewImage)

  pipelineRevealing.value = true
  analysisDone.value = false
  currentStage.value = 0
  pipelineStageIndex.value = 0
  stage1Done.value = false
  stage2Done.value = false
  stage3Done.value = false
  stageDetectionSrc.value = ''
  stageSegmentationSrc.value = ''
  stageMalignancySrc.value = ''

  logLungCtDebug('pipeline.start', {
    pipelineStageIndex: pipelineStageIndex.value,
    currentStage: currentStage.value,
    stageOutputKeys: Object.keys(outputs || {}),
    hasDetection: !!outputs.detectionImage,
    hasSegmentation: !!outputs.segmentationImage,
    hasMalignancy: !!outputs.malignancyImage,
    previewImage: !!data.previewImage,
    resolved: {
      detection: !!detectionSrc,
      segmentation: !!segmentationSrc,
      malignancy: !!malignancySrc
    }
  })
  await nextTick()

  currentStage.value = 1
  loadingText.value = '肺结节检测与分类完成，正在输出检测图...'
  await nextTick()
  await sleep(650)
  stageDetectionSrc.value = detectionSrc
  pipelineStageIndex.value = 1
  await nextTick()
  await waitForImageReady(stageDetectionSrc.value)
  stage1Done.value = !!stageDetectionSrc.value
  logLungCtDebug('pipeline.stage1', {
    currentStage: currentStage.value,
    pipelineStageIndex: pipelineStageIndex.value,
    stage1Done: stage1Done.value,
    visible: !!stageOutputSrc('detection')
  })
  await sleep(900)

  currentStage.value = 2
  loadingText.value = '结节精确分割完成，正在输出红色分割图...'
  await nextTick()
  await sleep(650)
  stageSegmentationSrc.value = segmentationSrc
  pipelineStageIndex.value = 2
  await nextTick()
  await waitForImageReady(stageSegmentationSrc.value)
  stage2Done.value = !!stageSegmentationSrc.value
  logLungCtDebug('pipeline.stage2', {
    currentStage: currentStage.value,
    pipelineStageIndex: pipelineStageIndex.value,
    stage2Done: stage2Done.value,
    visible: !!stageOutputSrc('segmentation')
  })
  await sleep(900)

  currentStage.value = 3
  loadingText.value = '良恶性风险评估完成，正在输出风险图...'
  await nextTick()
  await sleep(650)
  stageMalignancySrc.value = malignancySrc
  pipelineStageIndex.value = 3
  await nextTick()
  await waitForImageReady(stageMalignancySrc.value)
  stage3Done.value = !!stageMalignancySrc.value
  logLungCtDebug('pipeline.stage3', {
    currentStage: currentStage.value,
    pipelineStageIndex: pipelineStageIndex.value,
    stage3Done: stage3Done.value,
    visible: !!stageOutputSrc('malignancy')
  })
  await sleep(600)
  analysisDone.value = true
  pipelineRevealing.value = false
  logLungCtDebug('pipeline.finish', {
    analysisDone: analysisDone.value,
    pipelineRevealing: pipelineRevealing.value,
    currentStage: currentStage.value,
    pipelineStageIndex: pipelineStageIndex.value,
    stage1Done: stage1Done.value,
    stage2Done: stage2Done.value,
    stage3Done: stage3Done.value
  })
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
  brokenMainImageSrc.value = ''
  savedSlicesLoaded.value = false
  savedSlices.value = []

  // 自动开始分析
  await doAnalyze(file)
}

const startAIAnalysis = async () => {
  if (!fileSelected.value) {
    ElMessage.warning('请先上传CT影像文件')
    return
  }
  // 重新选文件
  document.querySelector('.ct-reupload input[type="file"]')?.click()
    || document.querySelector('.ct-upload input[type="file"]')?.click()
}

const doAnalyze = async (file) => {
  loading.value = true
  analyzing.value = true
  loadingProgress.value = 0
  analysisResult.value = null
  analysisDone.value = false
  pipelineRevealing.value = false
  currentStage.value = 0
  brokenMainImageSrc.value = ''
  stage1Done.value = false
  stage2Done.value = false
  stage3Done.value = false
  pipelineStageIndex.value = 0
  stageSlice.detection = 1
  stageSlice.segmentation = 1
  stageSlice.malignancy = 1
  stageDetectionSrc.value = ''
  stageSegmentationSrc.value = ''
  stageMalignancySrc.value = ''
  allCandidates.value = []

  logLungCtDebug('analyze.request', {
    fileName: file.name,
    fileSize: file.size,
    params: { max_slices: 180, max_candidates: 600, enable_autodl: false, lightweight: true }
  })

  // 模拟进度
  const progressTimer = setInterval(() => {
    if (loadingProgress.value < 90) {
      loadingProgress.value += Math.random() * 10
      if (loadingProgress.value > 20) loadingText.value = '正在读取真实CT体数据并检测结节...'
      if (loadingProgress.value > 48) loadingText.value = '正在提取肺实质掩码并执行结节精确分割...'
      if (loadingProgress.value > 72) loadingText.value = '正在计算良恶性风险概率...'
      if (loadingProgress.value > 86) loadingText.value = '正在生成标注切片和结构化结果...'
    }
  }, 600)

  try {
    const res = await analyzeLungCt(file, { max_slices: 180, max_candidates: 600, enable_autodl: false, lightweight: true })

    clearInterval(progressTimer)
    loadingProgress.value = 100
    loadingText.value = '分析完成！'

    const data = unwrapAnalyzePayload(res)
    logLungCtDebug('analyze.response.raw', {
      keys: Object.keys(res || {}),
      dataKeys: Object.keys((res && res.data) || {})
    })
    logLungCtDebug('analyze.response.unwrapped', {
      keys: Object.keys(data || {}),
      keysText: Object.keys(data || {}).join(','),
      success: data?.success,
      previewImage: !!data?.previewImage,
      previewSliceIndex: data?.previewSliceIndex,
      previewSliceZ: data?.previewSliceZ,
      stageOutputKeys: Object.keys(data?.stageOutputs || {}),
      stageOutputFlags: {
        detection: !!data?.stageOutputs?.detectionImage,
        segmentation: !!data?.stageOutputs?.segmentationImage,
        malignancy: !!data?.stageOutputs?.malignancyImage
      },
      totalSlices: data?.totalSlices,
      totalSlicesOrig: data?.totalSlicesOrig,
      candidateCount: Array.isArray(data?.candidates) ? data.candidates.length : 0,
      rawSlicesLen: Array.isArray(data?.rawSlices) ? data.rawSlices.length : -1,
      slicesLen: Array.isArray(data?.slices) ? data.slices.length : -1
    })
    const normalized = normalizeAnalyzeResult(data)
    logLungCtDebug('analyze.response.normalized', {
      keys: Object.keys(normalized || {}),
      keysText: Object.keys(normalized || {}).join(','),
      previewImage: !!normalized.previewImage,
      previewSliceIndex: normalized.previewSliceIndex,
      previewSliceZ: normalized.previewSliceZ,
      stageOutputKeys: Object.keys(normalized.stageOutputs || {}),
      stageOutputFlags: {
        detection: !!normalized.stageOutputs?.detectionImage,
        segmentation: !!normalized.stageOutputs?.segmentationImage,
        malignancy: !!normalized.stageOutputs?.malignancyImage
      },
      totalSlices: normalized.totalSlices,
      totalSlicesOrig: normalized.totalSlicesOrig,
      rawSlicesLen: Array.isArray(normalized.rawSlices) ? normalized.rawSlices.length : -1,
      slicesLen: Array.isArray(normalized.slices) ? normalized.slices.length : -1
    })
    if (normalized.success) {
      analysisResult.value = normalized
      totalSlices.value = sliceCountFromResult(normalized)
      currentSlice.value = Math.min(Math.max((normalized.previewSliceIndex ?? 0) + 1, 1), totalSlices.value)
      stageSlice.detection = currentSlice.value
      stageSlice.segmentation = currentSlice.value
      stageSlice.malignancy = currentSlice.value
      loading.value = false
      logLungCtDebug('analyze.state.applied', {
        totalSlices: totalSlices.value,
        currentSlice: currentSlice.value,
        currentSliceZ: normalized.previewSliceZ ?? null,
        pipelineStageIndex: pipelineStageIndex.value,
        analysisDone: analysisDone.value
      })

      // 解析结节候选
      if (normalized.candidates && Array.isArray(normalized.candidates)) {
        allCandidates.value = normalized.candidates.map(c => normalizeCandidate({
          ...c,
          _originalSliceIndex: c.slice_index
        }))
        candidatePage.value = 1
      }

      // 自动填充诊断表单
      autoFillDiagnosis(normalized)
      await revealPipelineOutputs(normalized)
      logLungCtDebug('analyze.after.pipeline', {
        pipelineStageIndex: pipelineStageIndex.value,
        stage1Done: stage1Done.value,
        stage2Done: stage2Done.value,
        stage3Done: stage3Done.value,
        stageDetectionSrc: !!stageDetectionSrc.value,
        stageSegmentationSrc: !!stageSegmentationSrc.value,
        stageMalignancySrc: !!stageMalignancySrc.value
      })

      const noduleCount = allCandidates.value.length
      ElMessage.success(`肺部CT全流程完成：检测/分割/良恶性评估已执行，检出 ${noduleCount} 个结节候选，共 ${totalSlices.value} 层切片`)
    } else {
      ElMessage.error(data.error || 'CT分析失败')
    }
  } catch (error) {
    clearInterval(progressTimer)
    pipelineRevealing.value = false
    currentStage.value = 0
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
    pipelineRevealing.value = false
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

function limitText(text, max = 260) {
  const value = String(text || '').trim()
  return value.length > max ? `${value.slice(0, max)}...` : value
}

function buildLungSuggestionText() {
  const candidates = [...(allCandidates.value || [])]
  const highRisk = candidates.filter(c => c.malignancyRisk === '高风险').length
  const midRisk = candidates.filter(c => c.malignancyRisk === '中风险').length
  const advice = analysisResult.value?.malignancyAssessment?.recommendation
    || analysisResult.value?.malignancyAssessment?.advice
    || ''

  if (advice) return limitText(advice, 500)
  if (highRisk > 0) return '存在高风险结节，建议结合临床资料完善增强CT/专科评估，并按肺结节诊疗规范随访或进一步处理。'
  if (midRisk > 0) return '存在中风险结节，建议结合既往影像对比，按医嘱定期复查胸部CT。'
  if (candidates.length > 0) return '当前以低风险结节为主，建议结合症状及危险因素定期随访。'
  return '本次未见明确可评估结节，建议结合临床情况常规随访。'
}

function buildDiagnosisPayload() {
  const candidates = allCandidates.value || []
  const topRisks = [...candidates]
    .sort((a, b) => (b.malignancyProbability || 0) - (a.malignancyProbability || 0))
    .slice(0, 3)
    .map(c => ({
      sliceZ: c.slice_z,
      type: c.type,
      diameterMm: Number(c.diameter_mm || 0).toFixed(1),
      risk: c.malignancyRisk || '低风险',
      probability: Math.round((c.malignancyProbability || 0) * 100)
    }))

  return JSON.stringify({
    checkType: 'LUNG_CT',
    diagnosis: {
      findings: limitText(diagnosisForm.findings, 420),
      impression: limitText(malignancySummary.value.conclusion, 220),
      conclusion: malignancySummary.value.overallRisk,
      remark: limitText(diagnosisForm.remark, 160)
    },
    stats: {
      rawCandidateRegions: stats.value.rawCandidateRegions,
      totalNoduleCandidates: stats.value.totalNoduleCandidates || candidates.length,
      solidNodules: stats.value.solidNodules,
      ggoNodules: stats.value.ggoNodules,
      calcifiedNodules: stats.value.calcifiedNodules,
      highRiskNodules: stats.value.highRiskNodules
    },
    aiResult: {
      overallRisk: malignancySummary.value.overallRisk,
      conclusion: limitText(malignancySummary.value.conclusion, 220),
      topRisks
    },
    fileName: uploadedFileName.value
  })
}

function buildSliceSavePayload(imageUrlSaved) {
  const result = analysisResult.value
  if (!result || savedSlicesLoaded.value) return null
  const slices = Array.isArray(result.slices) ? result.slices : []
  const rawSlices = Array.isArray(result.rawSlices) ? result.rawSlices : []
  const previewBase64 = stripDataUrlPrefix(
    result.previewImage
    || result.stageOutputs?.segmentationImage
    || result.stageOutputs?.detectionImage
    || result.stageOutputs?.malignancyImage
  )
  const representativeIndex = Math.max(0, result.previewSliceIndex ?? 0)
  const representativeStat = result.sliceHuStats?.[representativeIndex] || result.sliceHuStats?.[0] || {}

  if (!slices.length) {
    if (!previewBase64) return null
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
        stageOutputs: result.stageOutputs || null,
        pipeline: result.pipeline || null,
        representativeImageUrl: imageUrlSaved || null,
        representativeImageSource: result.previewImage || null,
        lightweight: true
      }),
      slices: [{
        sliceImage: previewBase64,
        rawImage: previewBase64,
        sliceZ: result.previewSliceZ ?? result.sliceIndices?.[representativeIndex] ?? representativeIndex,
        huMin: representativeStat.hu_min,
        huMax: representativeStat.hu_max,
        huMean: representativeStat.hu_mean,
        sliceThickness: result.sliceThickness,
        pixelSpacingX: result.pixelSpacingX,
        pixelSpacingY: result.pixelSpacingY
      }]
    }
  }
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
      stageOutputs: result.stageOutputs || null,
      pipeline: result.pipeline || null,
      representativeImageUrl: imageUrlSaved || null
    }),
    slices: slices.map((sliceImage, index) => {
      const stat = result.sliceHuStats?.[index] || {}
      return {
        sliceImage,
        rawImage: rawSlices[index] || sliceImage,
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
    if (!imageUrlSaved && ctImageDisplay.value) {
      if (ctImageDisplay.value.startsWith('data:image')) {
        const fileName = `lung_ct_${currentApplyId.value}_slice${currentSlice.value}_${Date.now()}.png`
        const file = dataUrlToFile(ctImageDisplay.value, fileName)
        const res = await uploadFile(file)
        imageUrlSaved = res.data?.url || res.url || null
      } else if (ctImageDisplay.value.startsWith('/') || ctImageDisplay.value.startsWith('http')) {
        imageUrlSaved = ctImageDisplay.value
      }
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
      suggestion: buildLungSuggestionText() || null,
      remark: diagnosisForm.remark || null,
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
    stage1Done.value = false
    stage2Done.value = false
    stage3Done.value = false
    stageDetectionSrc.value = ''
    stageSegmentationSrc.value = ''
    stageMalignancySrc.value = ''
    const res = await getSlicesByApplyId(applyId)
    const data = res.data || res
    logLungCtDebug('savedSlices.response', {
      applyId,
      isArray: Array.isArray(data),
      len: Array.isArray(data) ? data.length : -1,
      keys: data && !Array.isArray(data) ? Object.keys(data) : []
    })
    if (Array.isArray(data)) {
      savedSlices.value = data
      savedSlicesLoaded.value = data.length > 0
      totalSlices.value = data.length
      currentSlice.value = 1
      pipelineStageIndex.value = 0
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
  logLungCtDebug('savedSlices.restore', {
    rowCount: rows.length,
    detectionKeys: Object.keys(detection || {}),
    segmentationKeys: Object.keys(segmentation || {}),
    candidateCount: candidates.length,
    hasStageOutputs: !!detection.stageOutputs,
    stageOutputKeys: Object.keys(detection.stageOutputs || {})
  })
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
    previewImage: rows[0]?.imagePath || rows[0]?.rawImagePath || '',
    previewSliceIndex: 0,
    previewSliceZ: rows[0]?.sliceZ ?? 0,
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
    stageOutputs: detection.stageOutputs || null,
    pipeline: detection.pipeline || {
      noduleDetection: 'completed',
      segmentation: 'completed',
      malignancyClassification: 'completed'
    }
  }
  stageDetectionSrc.value = normalizeImageSrc(detection.stageOutputs?.detectionImage || rows[0]?.imagePath || rows[0]?.rawImagePath || '')
  stageSegmentationSrc.value = normalizeImageSrc(detection.stageOutputs?.segmentationImage || detection.stageOutputs?.detectionImage || rows[0]?.imagePath || rows[0]?.rawImagePath || '')
  stageMalignancySrc.value = normalizeImageSrc(detection.stageOutputs?.malignancyImage || detection.stageOutputs?.segmentationImage || detection.stageOutputs?.detectionImage || rows[0]?.imagePath || rows[0]?.rawImagePath || '')
  pipelineStageIndex.value = 3
  logLungCtDebug('savedSlices.restore.src', {
    detection: !!stageDetectionSrc.value,
    segmentation: !!stageSegmentationSrc.value,
    malignancy: !!stageMalignancySrc.value,
    pipelineStageIndex: pipelineStageIndex.value
  })
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

function resetLungCtWorkspace() {
  uploadedFileName.value = ''
  fileSelected.value = false
  currentSlice.value = 1
  totalSlices.value = 0
  zoom.value = 1
  brokenMainImageSrc.value = ''
  loading.value = false
  analyzing.value = false
  analysisDone.value = false
  analysisResult.value = null
  pipelineRevealing.value = false
  currentStage.value = 0
  pipelineStageIndex.value = 0
  stage1Done.value = false
  stage2Done.value = false
  stage3Done.value = false
  stageSlice.detection = 1
  stageSlice.segmentation = 1
  stageSlice.malignancy = 1
  stageDetectionSrc.value = ''
  stageSegmentationSrc.value = ''
  stageMalignancySrc.value = ''
  allCandidates.value = []
  candidatePage.value = 1
  savedSliceImageUrl.value = ''
  savedSlices.value = []
  savedSlicesLoaded.value = false
}

async function loadApplyDetail() {
  const applyId = route.query.applyId
  if (!applyId) {
    ElMessage.warning('请先从申请单列表选择对应申请单再执行检查')
    router.back()
    return
  }
  currentApplyId.value = applyId
  resetLungCtWorkspace()
  try {
    const res = await getApply(applyId)
    fillPatientFromApply(res.data || {})
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
    .back-btn { flex: 0 0 auto; color: #64748B; font-size: 14px; &:hover { color: #2a66b8; } }
    .header-title {
      display: flex; align-items: center; gap: 12px; min-width: 0;
      .title-badge { flex: 0 0 auto; padding: 4px 10px; background: #2a66b8; color: #FFF; border-radius: 4px; font-size: 12px; font-weight: 700; }
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
        .panel-title { display: flex; align-items: center; gap: 8px; color: #0F172A; font-size: 15px; font-weight: 700; .el-icon { color: #2a66b8; } }
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
            &:hover { background: rgba(22,97,77,.18); border-color: #60a5fa; }
            p { margin: 8px 0 0; color: #CBD5E1; font-size: 13px; &.sub { margin-top: 4px; color: #94A3B8; font-size: 11px; } }
          }
        }
        .ct-loading { width: min(360px, calc(100% - 40px)); color: #CBD5E1; text-align: center;
          .is-loading { margin-bottom: 16px; color: #60a5fa; animation: rotating 1.5s linear infinite; }
          p { margin: 0 0 12px; font-size: 14px; }
          .sub { margin-top: 8px; color: #94A3B8; font-size: 12px; }
        }
        .ct-image { max-width: 100%; max-height: 100%; transition: transform .2s ease; }
        .representative-ribbon {
          position: absolute;
          left: 12px;
          top: 12px;
          padding: 5px 9px;
          color: #d0e3fb;
          font-size: 12px;
          font-weight: 700;
          background: rgba(15,23,42,.72);
          border: 1px solid rgba(96,165,250,.46);
          border-radius: 6px;
          backdrop-filter: blur(6px);
        }
      }
      .series-nav {
        display: grid; grid-template-columns: auto minmax(0,1fr) auto minmax(110px,auto); align-items: center;
        gap: 10px; padding: 12px 14px; background: #F8FAFC; border-top: 1px solid #E2E8F0;
        .nav-center { display: flex; min-width: 0; flex-direction: column; gap: 2px;
          .nav-slice-label { display: flex; align-items: center; gap: 8px; color: #475569; font-size: 12px; strong { color: #0F172A; font-family: ui-monospace, monospace; } }
          .nav-z-info { color: #94A3B8; font-size: 10px; }
          .nav-slider { margin-top: 2px; }
        }
        .nav-filename { overflow: hidden; max-width: 180px; color: #64748B; font-size: 11px; font-family: ui-monospace, monospace; text-overflow: ellipsis; white-space: nowrap; }
      }
      .save-slice-bar {
        display: flex; align-items: center; gap: 10px; padding: 10px 14px; background: #eff6ff; border-top: 1px solid #c4dafa;
        .saved-tip { display: flex; align-items: center; gap: 4px; color: #2563eb; font-size: 12px; }
      }
    }

    .stage-image-section {
      display: grid;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      gap: 12px;
      margin-top: 14px;

      .stage-image-panel {
        display: flex;
        min-width: 0;
        min-height: 360px;
        flex-direction: column;
        overflow: hidden;
        background: #FFFFFF;
        border: 1px solid #DDE7EE;
        border-radius: 8px;
        box-shadow: 0 8px 24px rgba(15,23,42,.05);
        transition: border-color .2s, box-shadow .2s;

        &.running { border-color: #F59E0B; box-shadow: 0 8px 24px rgba(245,158,11,.12); }
        &.done { border-color: #b8d6f7; }

        .stage-image-head {
          display: flex;
          align-items: flex-start;
          justify-content: space-between;
          gap: 8px;
          padding: 12px 12px 9px;
          border-bottom: 1px solid #E2E8F0;
          background: #F8FAFC;

          .stage-step {
            margin-bottom: 3px;
            color: #2a66b8;
            font-size: 12px;
            font-weight: 800;
            font-family: ui-monospace, monospace;
          }
          h3 {
            margin: 0;
            color: #0F172A;
            font-size: 14px;
            line-height: 1.3;
          }
        }

        .stage-note {
          min-height: 54px;
          margin: 0;
          padding: 10px 12px;
          color: #64748B;
          font-size: 12px;
          line-height: 1.5;
          background: #FFFFFF;
        }

        .stage-image-slot {
          position: relative;
          display: flex;
          flex: 1;
          min-height: 220px;
          align-items: center;
          justify-content: center;
          overflow: hidden;
          background: radial-gradient(circle at center, #1F2937 0, #020617 78%);

          .stage-slice-view {
            position: relative;
            display: flex;
            width: 100%;
            height: 100%;
            min-height: 260px;
            align-items: center;
            justify-content: center;

            img {
              display: block;
              width: 100%;
              height: 100%;
              max-height: 300px;
              object-fit: contain;
            }

            .stage-overlay {
              position: absolute;
              inset: 0;
              width: 100%;
              height: 100%;
              pointer-events: none;
            }

            .stage-box {
              fill: none;
              stroke-width: 2.5;
              vector-effect: non-scaling-stroke;
              &.detection { stroke: #60a5fa; }
              &.malignancy.low { stroke: #60a5fa; }
              &.malignancy.medium { stroke: #F59E0B; }
              &.malignancy.high { stroke: #EF4444; }
            }

            .stage-contour-fill {
              fill: rgba(239, 68, 68, .42);
            }

            .stage-contour-line {
              fill: none;
              stroke: #EF4444;
              stroke-width: 2.5;
              vector-effect: non-scaling-stroke;
            }

            .stage-label {
              font-size: 12px;
              font-weight: 800;
              paint-order: stroke;
              stroke: rgba(2, 6, 23, .88);
              stroke-width: 3px;
              &.detection { fill: #c4dafa; }
              &.segmentation { fill: #FECACA; }
              &.malignancy.low { fill: #c4dafa; }
              &.malignancy.medium { fill: #FDE68A; }
              &.malignancy.high { fill: #FECACA; }
            }
          }

          .stage-slot-state {
            display: flex;
            width: 100%;
            min-height: 220px;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            gap: 10px;
            padding: 18px;
            color: #d0e3fb;
            text-align: center;
            font-size: 13px;

            .el-icon { font-size: 28px; color: #60a5fa; }
            &.muted {
              color: #94A3B8;
              .el-icon { color: #64748B; }
            }
          }
        }

        .stage-slice-nav {
          display: grid;
          grid-template-columns: auto minmax(0, 1fr) auto;
          align-items: center;
          gap: 8px;
          padding: 9px 10px;
          background: #F8FAFC;
          border-top: 1px solid #E2E8F0;

          .stage-nav-center {
            display: flex;
            min-width: 0;
            flex-direction: column;
            gap: 2px;
            color: #475569;
            font-size: 12px;

            strong { color: #0F172A; font-family: ui-monospace, monospace; }
            .stage-z { color: #94A3B8; font-size: 10px; }
            .stage-slider { margin-top: 1px; }
          }
        }
      }
    }

    .pipeline-panel {
      flex: 0 0 auto;
      .pipeline-flow { padding: 16px 14px; position: relative; display: flex; flex-direction: column; gap: 0;
        .flow-line { position: absolute; left: 28px; top: 50px; bottom: 50px; width: 2px; background: linear-gradient(180deg, #CBD5E1, #2a66b8, #CBD5E1); }
        .stage-card {
          display: flex; align-items: flex-start; gap: 10px; padding: 12px; margin-bottom: 6px;
          border-radius: 10px; position: relative; z-index: 1; border: 2px solid transparent; transition: all .3s;
          background: #F8FAFC;
          &.active { border-color: #2a66b8; background: #edf2f9; }
          &.done { border-color: #cfdcf3; background: #eff6ff; }
          &.running { border-color: #F59E0B; background: #FFFBEB; }
          &.placeholder { opacity: .55; }
          .stage-num { width: 32px; height: 32px; border-radius: 50%; flex-shrink: 0; display: flex; align-items: center; justify-content: center; background: #E2E8F0; color: #64748B; font-weight: 700; font-size: 12px; }
          &.active .stage-num, &.done .stage-num { background: #2a66b8; color: #fff; }
          &.running .stage-num { background: #F59E0B; color: #fff; }
          &.placeholder .stage-num { background: #E2E8F0; color: #94A3B8; }
          .stage-info { flex: 1;
            .stage-name { font-size: 13px; font-weight: 700; color: #1E293B; margin-bottom: 2px; display: flex; align-items: center; gap: 6px; }
            .stage-model { font-size: 10px; color: #94A3B8; font-family: monospace; margin-bottom: 3px; }
            .stage-desc { font-size: 11px; color: #64748B; line-height: 1.4; }
          }
          .stage-status {
            .done { color: #3b82f6; font-size: 20px; }
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
      padding: 14px 16px; background: linear-gradient(135deg, #edf2f9 0%, #f5f8fc 100%);
      border-bottom: 1px solid #cddaf2;
      .annotation-title { display: flex; align-items: center; gap: 10px; color: #1e3a5f; font-size: 16px; font-weight: 700; .el-icon { color: #2a66b8; } }
      .annotation-stats { display: flex; align-items: center; flex-wrap: wrap; gap: 8px;
        .stat-badge { display: flex; align-items: center; gap: 8px; min-width: 88px; padding: 6px 10px; background: rgba(255,255,255,.72); border: 1px solid #cddaf2; border-radius: 6px;
          .badge-num { font-family: ui-monospace, monospace; font-size: 18px; font-weight: 800; line-height: 1; }
          .badge-txt { color: #5B766E; font-size: 11px; }
          &.solid { color: #C2410C; }
          &.ggo { color: #D97706; }
          &.calc { color: #3b82f6; }
          &.risk { color: #DC2626; }
          &.info { color: #2a66b8; }
        }
      }
    }

    .nodule-table-area {
      padding: 14px 16px;
      h4 { display: flex; align-items: center; gap: 8px; margin: 0 0 12px; color: #0F172A; font-size: 14px;
        .sec-dot { width: 8px; height: 8px; border-radius: 50%; background: #2a66b8; flex-shrink: 0; }
      }
      .hu-badge { padding: 2px 8px; border-radius: 4px; font-size: 11px; font-weight: 600;
        &.hu-high { background: #FEE2E2; color: #DC2626; }
        &.hu-mid { background: #FEF3C7; color: #D97706; }
        &.hu-low { background: #DBEAFE; color: #2563EB; }
      }
      .nodule-pagination { margin-top: 10px; justify-content: center; }
    }
    .nodule-empty { padding: 40px 20px; text-align: center; color: #64748B;
      .el-icon { margin-bottom: 8px; color: #3b82f6; }
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
      .stage-image-section { grid-template-columns: repeat(3, minmax(0, 1fr)); }
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
    .main-content .stage-image-section { grid-template-columns: 1fr; }
    .main-content .diagnosis-panel :deep(.el-radio-group) { grid-template-columns: 1fr; }
  }
}

@keyframes rotating { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
@keyframes spin { to { transform: rotate(360deg); } }
</style>
