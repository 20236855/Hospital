<template>
  <div class="app-container brain-ct-page">
    <!-- 页面头部 -->
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
        <el-button type="primary" :loading="analyzing" @click="startAIAnalysis">
          <el-icon><Cpu /></el-icon>
          {{ analyzing ? 'AI分析中...' : '启动AI辅助诊断' }}
        </el-button>
        <el-button type="success" @click="saveDiagnosis">
          <el-icon><DocumentChecked /></el-icon>
          保存诊断结果
        </el-button>
      </div>
    </div>

    <!-- 患者信息栏 -->
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

    <!-- 主体内容区 -->
    <el-row :gutter="16" class="main-content">
      <!-- 左侧：CT影像区 -->
      <el-col :span="14">
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
            <div class="ct-image-container" ref="ctContainer">
              <!-- CT影像占位 - 后续接入真实影像 -->
              <div class="ct-placeholder" v-if="!ctImageUrl">
                <el-icon :size="64"><Picture /></el-icon>
                <p>CT影像加载区域</p>
                <p class="sub">请从PACS系统导入影像或上传DICOM文件</p>
                <el-button type="primary" plain @click="importImage">
                  <el-icon><Upload /></el-icon>
                  导入影像
                </el-button>
              </div>
              <img v-else :src="ctImageUrl" class="ct-image" :style="imageStyle" />
              
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
            <div class="series-nav">
              <el-button link @click="prevSlice"><el-icon><ArrowLeft /></el-icon></el-button>
              <span class="slice-info">层 {{ currentSlice }} / {{ totalSlices }}</span>
              <el-slider v-model="currentSlice" :min="1" :max="totalSlices" :show-tooltip="false" class="slice-slider" />
              <el-button link @click="nextSlice"><el-icon><ArrowRight /></el-icon></el-button>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 右侧：AI分析与诊断 -->
      <el-col :span="10">
        <!-- AI分析结果 -->
        <div class="panel ai-panel" v-loading="analyzing">
          <div class="panel-header">
            <span class="panel-title">
              <el-icon><Cpu /></el-icon>
              AI辅助诊断
            </span>
            <el-tag :type="aiStatus.type" size="small">{{ aiStatus.text }}</el-tag>
          </div>
          <div class="panel-body">
            <div class="ai-empty" v-if="!aiResult && !analyzing">
              <el-icon :size="48"><MagicStick /></el-icon>
              <p>点击"启动AI辅助诊断"开始分析</p>
              <p class="sub">AI将自动识别病灶区域并生成初步诊断建议</p>
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
                    </div>
                    <div class="finding-conf">{{ finding.confidence }}%</div>
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
                <el-input 
                  v-model="diagnosisForm.findings" 
                  type="textarea" 
                  :rows="3"
                  placeholder="描述CT影像所见..."
                />
              </el-form-item>
              
              <el-form-item label="诊断意见">
                <el-input 
                  v-model="diagnosisForm.impression" 
                  type="textarea" 
                  :rows="3"
                  placeholder="给出诊断意见..."
                />
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
                  <el-checkbox label="mri">建议MRI进一步检查</el-checkbox>
                  <el-checkbox label="followup">建议随访复查</el-checkbox>
                  <el-checkbox label="biopsy">建议活检</el-checkbox>
                  <el-checkbox label="surgery">建议手术治疗</el-checkbox>
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
  </div>
</template>

<script setup name="BrainCTCheck">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  ArrowLeft, Cpu, DocumentChecked, Picture, ZoomIn, ZoomOut, 
  RefreshRight, Sunny, Upload, ArrowRight, Warning, InfoFilled,
  MagicStick, EditPen
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

// ============ 患者信息 ============
const patientInfo = reactive({
  patientName: '张三',
  gender: '男',
  age: 58,
  registerId: 'REG20240617001',
  applyId: 'APPLY20240617001',
  doctorName: '李医生',
  purpose: '头痛、头晕3天，怀疑颅内占位性病变'
})

// ============ CT影像 ============
const ctImageUrl = ref('')
const ctContainer = ref(null)
const currentSlice = ref(1)
const totalSlices = ref(128)
const zoom = ref(1)
const imageStyle = computed(() => ({
  transform: `scale(${zoom.value})`
}))

const zoomIn = () => { zoom.value = Math.min(zoom.value + 0.2, 3) }
const zoomOut = () => { zoom.value = Math.max(zoom.value - 0.2, 0.5) }
const resetZoom = () => { zoom.value = 1 }
const prevSlice = () => { if (currentSlice.value > 1) currentSlice.value-- }
const nextSlice = () => { if (currentSlice.value < totalSlices.value) currentSlice.value++ }

// ============ AI诊断 ============
const analyzing = ref(false)
const aiResult = ref(null)
const selectedFinding = ref(0)
const aiDetections = ref([])

const aiStatus = computed(() => {
  if (analyzing.value) return { type: 'warning', text: '分析中' }
  if (aiResult.value) return { type: 'success', text: '已完成' }
  return { type: 'info', text: '未开始' }
})

// 模拟AI分析
const startAIAnalysis = () => {
  analyzing.value = true
  aiResult.value = null
  aiDetections.value = []
  
  setTimeout(() => {
    analyzing.value = false
    aiResult.value = {
      findings: [
        {
          name: '右侧额叶低密度影',
          description: '约2.3×1.8cm类圆形低密度影，边界尚清，周围可见轻度水肿带',
          confidence: 94,
          severity: 'high'
        },
        {
          name: '中线结构偏移',
          description: '脑室系统受压，中线结构向左偏移约3mm',
          confidence: 88,
          severity: 'medium'
        }
      ],
      suggestion: '考虑右侧额叶占位性病变，建议进一步完善增强MRI检查以明确病变性质。',
      features: {
        lesionCount: 1,
        maxSize: '23×18',
        location: '右侧额叶',
        density: '低密度'
      }
    }
    
    // 模拟检测框位置
    aiDetections.value = [
      { label: '病灶1', confidence: 94, style: { left: '45%', top: '35%', width: '80px', height: '60px' } }
    ]
    
    // 自动填充影像所见
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
const goBack = () => {
  router.back()
}

const importImage = () => {
  // 模拟导入
  ctImageUrl.value = '@/assets/exam/检查1.png'
}

const saveDiagnosis = () => {
  // 保存到病历表
  const diagnosisData = {
    registerId: patientInfo.registerId,
    applyId: patientInfo.applyId,
    checkType: 'BRAIN_CT',
    diagnosis: diagnosisForm,
    aiResult: aiResult.value,
    doctorId: 'DOC007', // role_id=7的医生
    checkTime: new Date().toISOString()
  }
  
  console.log('保存诊断结果:', diagnosisData)
  ElMessage.success('诊断结果已保存')
}

const selectDetection = (det) => {
  console.log('选中检测:', det)
}

onMounted(() => {
  // 从路由参数获取申请单信息
  const { applyId, registerId } = route.query
  if (applyId) {
    patientInfo.applyId = applyId
  }
  if (registerId) {
    patientInfo.registerId = registerId
  }
})
</script>

<style scoped lang="scss">
.brain-ct-page {
  padding: 16px;
  background: #F5F7FA;
  min-height: 100vh;

  /* ===== 页面头部 ===== */
  .page-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;
    background: #FFFFFF;
    padding: 16px 20px;
    border-radius: 12px;
    box-shadow: 0 1px 4px rgba(0,0,0,0.04);

    .header-left {
      display: flex;
      align-items: center;
      gap: 20px;

      .back-btn {
        color: #64748B;
        font-size: 14px;
        
        &:hover { color: #1A6B54; }
      }

      .header-title {
        display: flex;
        align-items: center;
        gap: 12px;

        .title-badge {
          padding: 4px 12px;
          background: #1A6B54;
          color: #FFFFFF;
          border-radius: 4px;
          font-size: 12px;
          font-weight: 600;
        }

        h1 {
          font-size: 20px;
          font-weight: 700;
          color: #1E293B;
          margin: 0;
        }
      }
    }

    .header-actions {
      display: flex;
      gap: 12px;
    }
  }

  /* ===== 患者信息栏 ===== */
  .patient-bar {
    display: flex;
    flex-wrap: wrap;
    gap: 24px;
    background: #FFFFFF;
    padding: 12px 20px;
    border-radius: 8px;
    margin-bottom: 16px;
    box-shadow: 0 1px 4px rgba(0,0,0,0.04);

    .patient-item {
      display: flex;
      align-items: center;
      gap: 8px;

      &.wide {
        flex: 1;
        min-width: 300px;
      }

      .label {
        color: #64748B;
        font-size: 13px;
      }

      .value {
        color: #1E293B;
        font-size: 14px;
        font-weight: 500;
      }
    }
  }

  /* ===== 主体内容 ===== */
  .main-content {
    .panel {
      background: #FFFFFF;
      border-radius: 12px;
      box-shadow: 0 1px 4px rgba(0,0,0,0.04);
      margin-bottom: 16px;

      .panel-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 14px 16px;
        border-bottom: 1px solid #F1F5F9;

        .panel-title {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 15px;
          font-weight: 600;
          color: #1E293B;

          .el-icon {
            color: #1A6B54;
          }
        }

        .panel-tools {
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }

      .panel-body {
        padding: 16px;
      }
    }

    /* CT影像面板 */
    .ct-panel {
      .ct-viewer {
        padding: 0;
        display: flex;
        flex-direction: column;
      }

      .ct-image-container {
        position: relative;
        height: 420px;
        background: #0A0A0A;
        display: flex;
        align-items: center;
        justify-content: center;
        overflow: hidden;

        .ct-placeholder {
          text-align: center;
          color: #64748B;

          .el-icon {
            color: #94A3B8;
            margin-bottom: 12px;
          }

          p {
            margin: 0 0 4px;
            color: #CBD5E1;
            font-size: 14px;

            &.sub {
              font-size: 12px;
              color: #64748B;
              margin-bottom: 16px;
            }
          }
        }

        .ct-image {
          max-width: 100%;
          max-height: 100%;
          transition: transform 0.2s ease;
        }

        .ai-overlay {
          position: absolute;
          inset: 0;
          pointer-events: none;

          .detection-box {
            position: absolute;
            border: 2px solid #F59E0B;
            background: rgba(245, 158, 11, 0.15);
            border-radius: 4px;
            pointer-events: auto;
            cursor: pointer;
            display: flex;
            flex-direction: column;
            align-items: flex-start;
            padding: 2px 4px;

            .det-label {
              background: #F59E0B;
              color: #FFFFFF;
              font-size: 10px;
              padding: 1px 4px;
              border-radius: 2px;
            }

            .det-conf {
              background: rgba(0,0,0,0.7);
              color: #FFFFFF;
              font-size: 10px;
              padding: 1px 4px;
              border-radius: 2px;
              margin-top: 2px;
            }

            &:hover {
              border-color: #EF4444;
              background: rgba(239, 68, 68, 0.2);
            }
          }
        }
      }

      .series-nav {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px 16px;
        background: #F8FAFC;
        border-top: 1px solid #F1F5F9;

        .slice-info {
          font-size: 13px;
          color: #475569;
          white-space: nowrap;
        }

        .slice-slider {
          flex: 1;
        }
      }
    }

    /* AI面板 */
    .ai-panel {
      .ai-empty {
        text-align: center;
        padding: 40px 20px;
        color: #64748B;

        .el-icon {
          color: #94A3B8;
          margin-bottom: 12px;
        }

        p {
          margin: 0 0 4px;
          font-size: 14px;

          &.sub {
            font-size: 12px;
            color: #94A3B8;
          }
        }
      }

      .ai-result {
        .result-section {
          margin-bottom: 16px;

          h4 {
            font-size: 13px;
            font-weight: 600;
            color: #1E293B;
            margin: 0 0 10px;
            padding-left: 8px;
            border-left: 3px solid #1A6B54;
          }

          .findings-list {
            display: flex;
            flex-direction: column;
            gap: 8px;

            .finding-item {
              display: flex;
              align-items: flex-start;
              gap: 10px;
              padding: 10px 12px;
              background: #F8FAFC;
              border-radius: 8px;
              cursor: pointer;
              border: 2px solid transparent;
              transition: all 0.2s;

              &.active {
                border-color: #1A6B54;
                background: #E8F3EF;
              }

              &:hover {
                background: #E8F3EF;
              }

              .finding-icon {
                width: 28px;
                height: 28px;
                border-radius: 50%;
                display: flex;
                align-items: center;
                justify-content: center;

                &.high {
                  background: #FEE2E2;
                  color: #DC2626;
                }

                &.medium {
                  background: #FEF3C7;
                  color: #D97706;
                }

                .el-icon {
                  font-size: 14px;
                }
              }

              .finding-content {
                flex: 1;

                .finding-title {
                  font-size: 13px;
                  font-weight: 600;
                  color: #1E293B;
                  margin-bottom: 2px;
                }

                .finding-desc {
                  font-size: 12px;
                  color: #64748B;
                  line-height: 1.4;
                }
              }

              .finding-conf {
                font-size: 12px;
                font-weight: 600;
                color: #1A6B54;
              }
            }
          }

          .suggestion-box {
            background: #E8F3EF;
            border-left: 4px solid #1A6B54;
            padding: 12px;
            border-radius: 0 8px 8px 0;

            p {
              margin: 0;
              font-size: 13px;
              color: #1E293B;
              line-height: 1.6;
            }
          }
        }
      }
    }

    /* 诊断面板 */
    .diagnosis-panel {
      :deep(.el-form-item__label) {
        font-weight: 500;
        color: #475569;
        padding-bottom: 4px;
      }

      :deep(.el-radio-group) {
        display: flex;
        flex-direction: column;
        gap: 8px;
      }

      :deep(.el-checkbox-group) {
        display: flex;
        flex-wrap: wrap;
        gap: 12px;
      }
    }
  }
}
</style>
