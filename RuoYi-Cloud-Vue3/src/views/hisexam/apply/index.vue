<template>
  <div class="app-container hisexam-apply">
    <!-- ==================== 三大专业分类卡片区域 ==================== -->
    <div class="category-section">
      <div class="section-header">
        <div class="header-decoration">
          <span class="deco-line"></span>
          <span class="deco-icon"><el-icon :size="22"><Plus /></el-icon></span>
          <span class="deco-line"></span>
        </div>
        <h2 class="section-title">医疗项目申请中心</h2>
        <p class="section-subtitle">请选择您需要申请的医疗项目类型，系统将引导您完成检查、检验或处置申请</p>
      </div>
      <el-row :gutter="24" class="category-row">
        <el-col
          v-for="cat in categories"
          :key="cat.type"
          :xs="24"
          :sm="24"
          :md="8"
          class="category-col"
        >
          <div
            class="category-card"
            :style="{ '--card-color': cat.color }"
            @click="goDetail(cat.type)"
            @mouseenter="cat.paused = true"
            @mouseleave="cat.paused = false"
          >
            <!-- 图片轮播区域（所有图片层叠，无缝切换） -->
            <div class="card-banner">
              <div
                v-for="(img, idx) in cat.images"
                :key="idx"
                class="banner-slide"
                :class="{ active: idx === cat.currentIndex }"
                :style="{ backgroundImage: `url(${img})` }"
              ></div>
              <!-- 渐变遮罩 -->
              <div class="banner-overlay"></div>
              <!-- 轮播进度条 -->
              <div class="carousel-progress">
                <span
                  v-for="(img, idx) in cat.images"
                  :key="idx"
                  class="progress-dot"
                  :class="{ active: idx === cat.currentIndex }"
                  @click.stop="switchImage(cat, idx)"
                >
                  <span v-if="idx === cat.currentIndex" class="progress-fill"></span>
                </span>
              </div>
              <!-- 卡片上的类型标识 -->
              <div class="banner-badge" :style="{ background: cat.color }">
                {{ cat.name }}
              </div>
            </div>
            <!-- 卡片内容 -->
            <div class="card-body">
              <div class="card-icon-wrap" :style="{ background: cat.colorBg, color: cat.color }">
                <el-icon :size="24">
                  <component :is="cat.icon" />
                </el-icon>
              </div>
              <div class="card-info">
                <h3 class="card-title">{{ cat.subtitle }}</h3>
                <p class="card-desc">{{ cat.description }}</p>
              </div>
              <div class="card-items">
                <span
                  v-for="item in cat.items"
                  :key="item"
                  class="item-tag"
                  :style="{ color: cat.color, borderColor: cat.color, background: cat.colorBg }"
                >
                  {{ item }}
                </span>
              </div>
              <div class="card-footer">
                <div class="card-stats">
                  <span class="stat" :style="{ color: cat.color }">
                    <el-icon><Document /></el-icon>
                    {{ cat.itemCount }} 个项目
                  </span>
                </div>
                <button
                  class="action-btn"
                  :style="{ background: cat.color, '--btn-hover': cat.color }"
                  @click.stop="goDetail(cat.type)"
                >
                  进入{{ cat.name }}
                  <el-icon class="btn-arrow"><ArrowRight /></el-icon>
                </button>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <DoctorInsightDashboard
      eyebrow="Medical Technology Board"
      title="检查检验申请业务仪表盘"
      subtitle="用当前申请单接口数据展示待执行、完成、支付和项目类型分布，辅助医生判断业务压力"
      tone="diagnostic"
      :focus-value="applyDashboard.focusValue"
      focus-label="完成率"
      :metrics="applyDashboard.metrics"
      :lanes="applyDashboard.lanes"
      :chips="applyDashboard.chips"
    />

    <!-- ==================== 搜索区域 ==================== -->
    <div class="search-panel" v-show="showSearch">
      <el-form :model="queryParams" ref="queryRef" label-width="110px" class="search-form">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="挂号ID" prop="registerId">
              <el-input v-model="queryParams.registerId" placeholder="请输入" clearable @keyup.enter="handleQuery" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="接诊ID" prop="encounterId">
              <el-input v-model="queryParams.encounterId" placeholder="请输入" clearable @keyup.enter="handleQuery" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="患者ID" prop="patientId">
              <el-input v-model="queryParams.patientId" placeholder="请输入" clearable @keyup.enter="handleQuery" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="开单医生ID" prop="doctorId">
              <el-input v-model="queryParams.doctorId" placeholder="请输入" clearable @keyup.enter="handleQuery" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="开单科室ID" prop="deptId">
              <el-input v-model="queryParams.deptId" placeholder="请输入" clearable @keyup.enter="handleQuery" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="医技项目ID" prop="techId">
              <el-input v-model="queryParams.techId" placeholder="请输入" clearable @keyup.enter="handleQuery" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="部位" prop="applyPosition">
              <el-input v-model="queryParams.applyPosition" placeholder="请输入" clearable @keyup.enter="handleQuery" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="执行人员ID" prop="operatorId">
              <el-input v-model="queryParams.operatorId" placeholder="请输入" clearable @keyup.enter="handleQuery" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="结果录入人员ID" prop="inputerId">
              <el-input v-model="queryParams.inputerId" placeholder="请输入" clearable @keyup.enter="handleQuery" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="开立时间" prop="applyTime">
              <el-date-picker clearable v-model="queryParams.applyTime" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="执行时间" prop="examTime">
              <el-date-picker clearable v-model="queryParams.examTime" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label=" " label-width="10px">
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <!-- ==================== 工具栏 ==================== -->
    <el-row :gutter="10" class="toolbar-row">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hisexam:apply:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['hisexam:apply:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['hisexam:apply:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['hisexam:apply:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- ==================== 数据表格 ==================== -->
    <div class="table-wrap">
      <el-table v-loading="loading" :data="applyList" @selection-change="handleSelectionChange" stripe>
        <el-table-column type="selection" width="45" align="center" fixed="left" />
        <el-table-column label="申请ID" align="center" prop="applyId" width="90" show-overflow-tooltip />
        <el-table-column label="类型" align="center" prop="applyType" width="80">
          <template #default="scope">
            <span :class="'type-' + scope.row.applyType">{{ scope.row.applyType }}</span>
          </template>
        </el-table-column>
        <el-table-column label="挂号编号" align="center" prop="registerNo" width="110" show-overflow-tooltip />
        <el-table-column label="接诊ID" align="center" prop="encounterId" width="80" />
        <el-table-column label="患者" align="center" prop="patientName" width="90" show-overflow-tooltip />
        <el-table-column label="开单医生" align="center" prop="doctorName" width="100" show-overflow-tooltip />
        <el-table-column label="开单科室" align="center" prop="deptName" width="110" show-overflow-tooltip />
        <el-table-column label="医技项目" align="center" prop="techName" width="120" show-overflow-tooltip />
        <el-table-column label="目的要求" align="center" prop="applyInfo" min-width="120" show-overflow-tooltip />
        <el-table-column label="部位" align="center" prop="applyPosition" min-width="100" show-overflow-tooltip />
        <el-table-column label="执行人员" align="center" prop="operatorId" width="90" />
        <el-table-column label="录入人员" align="center" prop="inputerId" width="90" />
        <el-table-column label="开立时间" align="center" prop="applyTime" width="110">
          <template #default="scope">
            <span>{{ parseTime(scope.row.applyTime, '{y}-{m}-{d}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="执行时间" align="center" prop="examTime" width="110">
          <template #default="scope">
            <span>{{ parseTime(scope.row.examTime, '{y}-{m}-{d}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" prop="applyStatus" width="90">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.applyStatus)" size="small">{{ scope.row.applyStatus || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="支付" align="center" prop="payStatus" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.payStatus === '已支付' ? 'success' : 'warning'" size="small">{{ scope.row.payStatus || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="140" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hisexam:apply:edit']">修改</el-button>
            <el-button link type="danger" size="small" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hisexam:apply:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改检查/检验/处置申请单对话框 -->
    <el-dialog :title="title" v-model="open" width="640px" append-to-body>
      <el-form ref="applyRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="挂号单" prop="registerId">
              <el-select
                v-model="form.registerId"
                filterable
                remote
                clearable
                :remote-method="searchRegisterOptions"
                :loading="registerLoading"
                placeholder="搜索挂号编号 / 患者姓名"
                style="width:100%"
                @change="onRegisterChange"
              >
                <el-option
                  v-for="r in registerOptions"
                  :key="r.registerId"
                  :label="formatRegisterOption(r)"
                  :value="r.registerId"
                >
                  <span style="font-weight:600">{{ r.registerNo }}</span>
                  <span style="margin-left:8px;color:#64748B">{{ r.patientName }}</span>
                  <span style="margin-left:8px;color:#94A3B8;font-size:12px">{{ r.gender || '-' }} {{ r.age ? r.age + '岁' : '' }}</span>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="接诊ID">
              <el-input v-model="form.encounterId" disabled placeholder="选择挂号后自动填充" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="患者">
              <el-input :model-value="form._patientInfo || '选择挂号后自动填充'" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开单医生">
              <el-input :model-value="form._doctorInfo || '选择挂号后自动填充'" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开单科室">
              <el-input :model-value="form._deptInfo || '选择挂号后自动填充'" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="类型" prop="applyType">
              <el-select v-model="form.applyType" placeholder="请选择类型" style="width:100%" @change="onApplyTypeChange">
                <el-option label="检查" value="CHECK" />
                <el-option label="检验" value="LAB" />
                <el-option label="处置" value="DISPOSAL" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="医技项目" prop="techId">
              <el-select v-model="form.techId" placeholder="请选择医技项目" filterable style="width:100%">
                <el-option
                  v-for="t in techOptions"
                  :key="t.id"
                  :label="t.techName"
                  :value="t.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="目的要求" prop="applyInfo">
              <el-input v-model="form.applyInfo" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="检查/检验/处置部位" prop="applyPosition">
              <el-input v-model="form.applyPosition" placeholder="请输入检查/检验/处置部位" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="开立时间" prop="applyTime">
              <el-date-picker clearable
                v-model="form.applyTime"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择开立时间">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="执行时间" prop="examTime">
              <el-date-picker clearable
                v-model="form.examTime"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择执行时间">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="执行结果" prop="examResult">
              <el-input v-model="form.examResult" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="CT/影像文件访问地址，检验、处置类可留空" prop="imageUrl">
              <el-input v-model="form.imageUrl" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
        </el-row>
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

<script setup name="Apply">
import { ref, reactive, toRefs, getCurrentInstance, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { listApply, getApply, delApply, addApply, updateApply, listApplyRegisterOptions } from "@/api/hisexam/apply"
import { listTechnology, listLabTechnologyOptions } from '@/api/hisexam/technology'
import { Plus, ArrowRight, Document, View, Monitor, FirstAidKit, Clock, CircleCheckFilled, Money } from '@element-plus/icons-vue'
import DoctorInsightDashboard from '@/components/DoctorInsightDashboard/index.vue'

// ============ 分类卡片数据 ============
const router = useRouter()

import checkImg1 from '@/assets/exam/检查1.png'
import checkImg2 from '@/assets/exam/检查2.png'
import checkImg3 from '@/assets/exam/检查3.png'
import inspecImg1 from '@/assets/exam/检验1.png'
import inspecImg2 from '@/assets/exam/检验2.png'
import inspecImg3 from '@/assets/exam/检验3.png'
import disposalImg1 from '@/assets/exam/处置1.png'
import disposalImg3 from '@/assets/exam/处置3.png'

const categories = reactive([
  {
    type: 'CHECK',
    name: '检查',
    subtitle: '影像学检查',
    icon: View,
    description: '利用 CT、超声等影像设备进行医学诊断，包含颅内病变CT、肺部病变CT及皮肤病变等检查项目',
    images: [checkImg1, checkImg2, checkImg3],
    color: '#3D5AB8',
    colorBg: '#EAEDF8',
    items: ['颅内病变CT', '肺部病变CT', '皮肤病变'],
    itemCount: 3,
    currentIndex: 0,
    paused: false,
    timer: null
  },
  {
    type: 'LAB',
    name: '检验',
    subtitle: '实验室检验',
    icon: Monitor,
    description: '通过分析血液、体液等样本提供诊断依据，包含验血、生化分析等实验室检验项目',
    images: [inspecImg1, inspecImg2, inspecImg3],
    color: '#3D5AB8',
    colorBg: '#EAEDF8',
    items: ['验血', '生化检验', '免疫检验'],
    itemCount: 3,
    currentIndex: 0,
    paused: false,
    timer: null
  },
  {
    type: 'DISPOSAL',
    name: '处置',
    subtitle: '临床处置',
    icon: FirstAidKit,
    description: '为患者提供直接的治疗与护理服务，包含手术、住院、注射等临床处置项目',
    images: [disposalImg1, disposalImg3, disposalImg1],
    color: '#3D5AB8',
    colorBg: '#EAEDF8',
    items: ['手术', '住院', '注射'],
    itemCount: 3,
    currentIndex: 0,
    paused: false,
    timer: null
  }
])

// 手动切换图片
function switchImage(cat, idx) {
  cat.currentIndex = idx
  resetTimer(cat)
}

// 启动/重置轮播定时器
function startCarousel(cat) {
  stopCarousel(cat)
  cat.timer = setInterval(() => {
    if (!cat.paused) {
      cat.currentIndex = (cat.currentIndex + 1) % cat.images.length
    }
  }, 3500)
}

function stopCarousel(cat) {
  if (cat.timer) {
    clearInterval(cat.timer)
    cat.timer = null
  }
}

function resetTimer(cat) {
  stopCarousel(cat)
  startCarousel(cat)
}

// 跳转到详情页
function goDetail(type) {
  router.push({ path: '/hisexam/apply-detail', query: { applyType: type } })
}

onMounted(() => {
  categories.forEach(cat => startCarousel(cat))
})

onUnmounted(() => {
  categories.forEach(cat => stopCarousel(cat))
})

// ============ 原有业务逻辑 ============
const { proxy } = getCurrentInstance()

const applyList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const registerOptions = ref([])
const registerLoading = ref(false)
const techOptions = ref([])

const percent = (part, whole) => whole ? Math.round((part / whole) * 100) : 0
const applyDashboard = computed(() => {
  const rows = applyList.value || []
  const listTotal = rows.length
  const pending = rows.filter(item => item.applyStatus === '待执行' || item.applyStatus === '待缴费').length
  const done = rows.filter(item => item.applyStatus === '已完成').length
  const unpaid = rows.filter(item => item.payStatus === '未支付').length
  const check = rows.filter(item => item.applyType === 'CHECK').length
  const lab = rows.filter(item => item.applyType === 'LAB' || item.applyType === 'INSPEC').length
  const disposal = rows.filter(item => item.applyType === 'DISPOSAL').length
  const doneRate = percent(done, listTotal)
  return {
    focusValue: doneRate + '%',
    metrics: [
      { label: '当前申请单', value: listTotal, note: `接口总数 ${total.value || listTotal}`, icon: Document, progress: Math.min(listTotal * 8, 100) },
      { label: '待处理', value: pending, note: pending ? '需要医技执行' : '队列清空', icon: Clock, state: pending ? 'warn' : 'good', progress: percent(pending, listTotal) },
      { label: '已完成', value: done, note: `完成率 ${doneRate}%`, icon: CircleCheckFilled, state: 'good', progress: doneRate },
      { label: '未支付', value: unpaid, note: unpaid ? '影响执行闭环' : '支付正常', icon: Money, state: unpaid ? 'warn' : 'good', progress: percent(unpaid, listTotal) }
    ],
    lanes: [
      { label: '影像检查', value: check, progress: percent(check, listTotal) },
      { label: '实验室检验', value: lab, progress: percent(lab, listTotal) },
      { label: '临床处置', value: disposal, progress: percent(disposal, listTotal) }
    ],
    chips: [
      { label: '检查单', value: check, type: 'info' },
      { label: '检验单', value: lab, type: 'success' },
      { label: '待闭环', value: pending + unpaid, type: pending + unpaid ? 'warning' : 'success' }
    ]
  }
})

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    registerId: undefined,
    encounterId: undefined,
    patientId: undefined,
    doctorId: undefined,
    deptId: undefined,
    applyType: undefined,
    techId: undefined,
    applyInfo: undefined,
    applyPosition: undefined,
    operatorId: undefined,
    inputerId: undefined,
    applyTime: undefined,
    examTime: undefined,
    examResult: undefined,
    imageUrl: undefined,
    applyStatus: undefined,
    payStatus: undefined,
  },
  rules: {
    registerId: [
      { required: true, message: "请选择挂号单", trigger: "change" }
    ],
    applyType: [
      { required: true, message: "请选择类型", trigger: "change" }
    ],
    techId: [
      { required: true, message: "请选择医技项目", trigger: "change" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询检查/检验/处置申请单列表 */
function getList() {
  loading.value = true
  listApply(queryParams.value).then(response => {
    applyList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function formatRegisterOption(item) {
  if (!item) return ''
  return `${item.registerNo || item.registerId} - ${item.patientName || '未知患者'}`
}

function applyRegisterContext(item) {
  if (!item) return
  form.value.registerId = item.registerId
  form.value.encounterId = item.encounterId
  form.value.patientId = item.patientId
  form.value.doctorId = item.doctorId
  form.value.deptId = item.deptId
  form.value._patientInfo = `${item.patientName || '未知患者'} (ID:${item.patientId || '-'})`
  form.value._doctorInfo = `${item.doctorName || '未知医生'} (ID:${item.doctorId || '-'})`
  form.value._deptInfo = `${item.deptName || '未知科室'} (ID:${item.deptId || '-'})`
}

function searchRegisterOptions(keyword = '') {
  registerLoading.value = true
  listApplyRegisterOptions({ keyword })
    .then(res => {
      registerOptions.value = res.data || []
    })
    .finally(() => {
      registerLoading.value = false
    })
}

function onRegisterChange(registerId) {
  if (!registerId) {
    form.value.encounterId = null
    form.value.patientId = null
    form.value.doctorId = null
    form.value.deptId = null
    form.value._patientInfo = ''
    form.value._doctorInfo = ''
    form.value._deptInfo = ''
    return
  }
  const matched = registerOptions.value.find(item => item.registerId === registerId)
  if (matched) applyRegisterContext(matched)
}

function ensureRegisterOption(apply) {
  if (!apply?.registerId) return
  const option = {
    registerId: apply.registerId,
    registerNo: apply.registerNo,
    encounterId: apply.encounterId,
    patientId: apply.patientId,
    patientName: apply.patientName,
    doctorId: apply.doctorId,
    doctorName: apply.doctorName,
    deptId: apply.deptId,
    deptName: apply.deptName
  }
  if (!registerOptions.value.some(item => item.registerId === option.registerId)) {
    registerOptions.value.unshift(option)
  }
  applyRegisterContext(option)
}

function loadTechOptions() {
  const techType = form.value.applyType || undefined
  if (techType === 'LAB') {
    listLabTechnologyOptions().then(res => {
      techOptions.value = res.data || []
    })
    return
  }
  listTechnology({ techType, pageNum: 1, pageSize: 100 })
    .then(res => {
      const rows = res.rows || res.data?.rows || res.data || []
      if (rows.length || techType !== 'LAB') {
        techOptions.value = rows
        return
      }
      return listTechnology({ pageNum: 1, pageSize: 300 }).then(allRes => {
        const allRows = allRes.rows || allRes.data?.rows || allRes.data || []
        const labNames = ['血常规检验', '验血', '血常规', '生化全套检验', '生化检验', '生化全套', '肝功能', '肾功能', '肝肾功能', '血糖血脂', '免疫学检验', '免疫检验', '免疫学']
        techOptions.value = allRows.filter(t => {
          const techName = String(t.techName || '').replace(/\s+/g, '').toLowerCase()
          return labNames.some(name => {
            const target = name.replace(/\s+/g, '').toLowerCase()
            return techName.includes(target) || target.includes(techName)
          })
        })
      })
    })
}

function onApplyTypeChange() {
  form.value.techId = null
  loadTechOptions()
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    applyId: null,
    registerId: null,
    encounterId: null,
    patientId: null,
    doctorId: null,
    deptId: null,
    applyType: null,
    techId: null,
    applyInfo: null,
    applyPosition: null,
    operatorId: null,
    inputerId: null,
    applyTime: null,
    examTime: null,
    examResult: null,
    imageUrl: null,
    applyStatus: null,
    payStatus: null,
    remark: null,
    createTime: null,
    updateTime: null
  }
  registerOptions.value = []
  proxy.resetForm("applyRef")
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.applyId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  searchRegisterOptions('')
  loadTechOptions()
  open.value = true
  title.value = "添加检查/检验/处置申请单"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _applyId = row.applyId || ids.value
  getApply(_applyId).then(response => {
    form.value = response.data
    ensureRegisterOption(response.data)
    loadTechOptions()
    open.value = true
    title.value = "修改检查/检验/处置申请单"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["applyRef"].validate(valid => {
    if (valid) {
      if (form.value.applyId != null) {
        updateApply(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addApply(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _applyIds = row.applyId || ids.value
  proxy.$modal.confirm('是否确认删除检查/检验/处置申请单编号为"' + _applyIds + '"的数据项？').then(function() {
    return delApply(_applyIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('hisexam/apply/export', {
    ...queryParams.value
  }, `apply_${new Date().getTime()}.xlsx`)
}

function statusTagType(status) {
  const map = { '待缴费': 'warning', '待执行': 'info', '已完成': 'success' }
  return map[status] || 'info'
}

getList()
</script>

<style scoped lang="scss">
.hisexam-apply {
  /* ============ 分类卡片区域 ============ */
  .category-section {
    margin-bottom: 28px;
    animation: sectionIn 0.6s ease-out;
  }

  @keyframes sectionIn {
    from { opacity: 0; transform: translateY(20px); }
    to   { opacity: 1; transform: translateY(0); }
  }

  .section-header {
    text-align: center;
    margin-bottom: 32px;

    .header-decoration {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 16px;
      margin-bottom: 16px;

      .deco-line {
        display: block;
        width: 60px;
        height: 2px;
        background: linear-gradient(90deg, transparent, #2F4AA0, transparent);
        border-radius: 1px;
      }

      .deco-icon {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        background: #2F4AA0;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        box-shadow: 0 4px 15px rgba(47, 74, 160, 0.4);
        animation: pulseIcon 2s ease-in-out infinite;
      }
    }

    @keyframes pulseIcon {
      0%, 100% { box-shadow: 0 4px 15px rgba(47, 74, 160, 0.3); }
      50%      { box-shadow: 0 4px 25px rgba(47, 74, 160, 0.55); }
    }

    .section-title {
      font-size: 26px;
      font-weight: 700;
      color: #1E293B;
      margin: 0 0 8px;
      letter-spacing: 2px;
    }

    .section-subtitle {
      font-size: 14px;
      color: #64748B;
      margin: 0;
      letter-spacing: 0.5px;
    }
  }

  .category-row {
    .category-col {
      margin-bottom: 20px;
    }
  }

  /* 分类卡片 */
  .category-card {
    position: relative;
    background: #FFFFFF;
    border-radius: 20px;
    overflow: hidden;
    cursor: pointer;
    box-shadow: 0 2px 20px rgba(0, 0, 0, 0.06);
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    border: 1px solid rgba(0, 0, 0, 0.04);
    height: 100%;
    display: flex;
    flex-direction: column;

    &:hover {
      transform: translateY(-6px);
      box-shadow: 0 20px 40px rgba(0, 0, 0, 0.12);

      .card-banner .banner-badge {
        transform: scale(1.05);
      }

      .action-btn {
        box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
      }
    }
  }

  /* 图片轮播 */
  .card-banner {
    position: relative;
    width: 100%;
    height: 200px;
    overflow: hidden;
    background: #F1F5F9;

    .banner-slide {
      position: absolute;
      inset: 0;
      background-size: cover;
      background-position: center top;
      opacity: 0;
      transition: opacity 0.8s ease;
      &.active {
        opacity: 1;
      }
    }

    .banner-overlay {
      position: absolute;
      inset: 0;
      background: linear-gradient(
        to bottom,
        rgba(0, 0, 0, 0.05) 0%,
        rgba(0, 0, 0, 0.02) 40%,
        rgba(0, 0, 0, 0.45) 100%
      );
    }

    .banner-badge {
      position: absolute;
      top: 16px;
      right: 16px;
      padding: 6px 18px;
      border-radius: 20px;
      color: #FFFFFF;
      font-size: 13px;
      font-weight: 600;
      letter-spacing: 2px;
      backdrop-filter: blur(10px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
      transition: transform 0.3s ease;
      z-index: 2;
    }

    /* 轮播进度点 */
    .carousel-progress {
      position: absolute;
      bottom: 12px;
      left: 50%;
      transform: translateX(-50%);
      display: flex;
      gap: 8px;
      z-index: 2;

      .progress-dot {
        width: 24px;
        height: 3px;
        border-radius: 2px;
        background: rgba(255, 255, 255, 0.4);
        cursor: pointer;
        transition: all 0.3s ease;
        overflow: hidden;

        &.active {
          width: 36px;
          background: rgba(255, 255, 255, 0.8);
        }

        .progress-fill {
          display: block;
          height: 100%;
          background: #FFFFFF;
          border-radius: 2px;
          animation: progressFill 3.5s linear forwards;
        }
      }
    }

    @keyframes progressFill {
      from { width: 0%; }
      to   { width: 100%; }
    }
  }

  /* 轮播淡入淡出 */
  /* 卡片内容 */
  .card-body {
    padding: 20px 24px 24px;
    position: relative;
    z-index: 1;
    flex: 1;
    display: flex;
    flex-direction: column;
  }

  .card-icon-wrap {
    width: 48px;
    height: 48px;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 14px;
    transition: transform 0.3s ease;

    .category-card:hover & {
      transform: rotate(-5deg) scale(1.1);
    }
  }

  .card-info {
    .card-title {
      font-size: 18px;
      font-weight: 700;
      color: #1E293B;
      margin: 0 0 6px;
    }

    .card-desc {
      font-size: 13px;
      color: #64748B;
      line-height: 1.6;
      margin: 0 0 14px;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
  }

  .card-items {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 18px;

    .item-tag {
      display: inline-block;
      padding: 2px 10px;
      border-radius: 6px;
      font-size: 12px;
      font-weight: 500;
      border: 1px solid;
      line-height: 20px;
      transition: all 0.2s;
    }
  }

  .card-footer {
    margin-top: auto;
    display: flex;
    align-items: center;
    justify-content: space-between;

    .card-stats {
      .stat {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 13px;
        font-weight: 500;
      }
    }

    .action-btn {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 8px 20px;
      border: none;
      border-radius: 20px;
      color: #FFFFFF;
      font-size: 14px;
      font-weight: 600;
      letter-spacing: 0.5px;
      cursor: pointer;
      transition: all 0.3s ease;

      &:hover {
        filter: brightness(0.9);
        transform: translateY(-1px);
      }

      .btn-arrow {
        transition: transform 0.3s ease;
      }

      &:hover .btn-arrow {
        transform: translateX(3px);
      }
    }
  }

  /* ===== 搜索面板 ===== */
  .search-panel {
    background: #F8FAFC;
    border-radius: 12px;
    padding: 20px 20px 4px;
    margin-bottom: 14px;
    border: 1px solid #E2E8F0;

    .search-form {
      :deep(.el-form-item) {
        margin-bottom: 16px;
      }

      :deep(.el-form-item__label) {
        color: #475569;
        font-weight: 500;
      }

      :deep(.el-input__wrapper) {
        box-shadow: 0 0 0 1px #E2E8F0 inset;
        &:hover { box-shadow: 0 0 0 1px #CBD5E1 inset; }
      }
    }
  }

  /* ===== 工具栏 ===== */
  .toolbar-row {
    margin-bottom: 14px;
  }

  /* ===== 表格 ===== */
  .table-wrap {
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
    border: 1px solid #E2E8F0;

    :deep(.el-table) {
      font-size: 13px;

      th.el-table__cell {
        background: #F1F5F9;
        color: #475569;
        font-weight: 600;
        font-size: 12px;
        padding: 10px 0;
        border-bottom: 2px solid #E2E8F0;
      }

      td.el-table__cell {
        padding: 8px 0;
        color: #334155;
      }

      .el-table__body tr:hover > td {
        background: #F8FAFC;
      }

      /* 类型标识 */
      .type-CHECK,
      .type-INSPEC,
      .type-LAB,
      .type-DISPOSAL {
        display: inline-block;
        padding: 2px 8px;
        border-radius: 4px;
        background: #E8F3EF;
        color: #1A6B54;
        font-size: 12px;
        font-weight: 600;
      }
    }

    :deep(.el-table__fixed-right) {
      box-shadow: -2px 0 8px rgba(0, 0, 0, 0.06);
    }
  }
}
</style>
