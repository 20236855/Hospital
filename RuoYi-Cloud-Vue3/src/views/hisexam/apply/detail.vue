<template>
  <div class="app-container apply-detail-page">
    <!-- 返回按钮 -->
    <div class="back-nav">
      <el-button link @click="goBack" class="back-btn">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回申请列表</span>
      </el-button>
    </div>

    <!-- 顶部 Hero 区域 -->
    <div class="detail-hero" v-if="currentCategory">
      <div class="hero-bg" :style="{ background: currentCategory.color }"></div>
      <div class="hero-pattern"></div>
      <div class="hero-content">
        <div class="hero-badge">{{ currentCategory.name }}</div>
        <h1 class="hero-title">{{ currentCategory.subtitle }}</h1>
        <p class="hero-desc">{{ currentCategory.description }}</p>
        <div class="hero-meta">
          <el-tag v-for="item in currentCategory.items" :key="item" effect="dark" round size="large" class="hero-tag">
            {{ item }}
          </el-tag>
        </div>
      </div>
      <!-- Hero 区域轮播（所有图片层叠，无缝切换） -->
      <div class="hero-carousel" v-if="currentCategory">
        <div
          v-for="(img, idx) in currentCategory.images"
          :key="idx"
          class="hero-slide"
          :class="{ active: idx === heroIndex }"
          :style="{ backgroundImage: `url(${img})` }"
        ></div>
        <div class="hero-carousel-dots">
          <span
            v-for="(img, idx) in currentCategory.images"
            :key="idx"
            class="hero-dot"
            :class="{ active: idx === heroIndex }"
            @click="heroIndex = idx"
          ></span>
        </div>
      </div>
    </div>

    <!-- 统计概览 -->
    <el-row :gutter="20" class="stats-row" v-if="currentCategory">
      <el-col :xs="24" :sm="8">
        <div class="stat-card" :style="{ '--stat-color': currentCategory.color }">
          <div class="stat-icon-wrap">
            <el-icon :size="32"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ tableData.length }}</div>
            <div class="stat-label">申请单总数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8">
        <div class="stat-card" :style="{ '--stat-color': currentCategory.color }">
          <div class="stat-icon-wrap">
            <el-icon :size="32"><Clock /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ pendingCount }}</div>
            <div class="stat-label">待处理</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8">
        <div class="stat-card" :style="{ '--stat-color': currentCategory.color }">
          <div class="stat-icon-wrap">
            <el-icon :size="32"><CircleCheckFilled /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ doneCount }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 项目列表 -->
    <div class="detail-content" v-if="currentCategory">
      <div class="content-header">
        <h3 class="content-title">{{ currentCategory.name }}项目明细</h3>
        <el-button :color="currentCategory.color" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新建{{ currentCategory.name }}申请单
        </el-button>
      </div>

      <el-row :gutter="20">
        <el-col
          v-for="(item, index) in subItems"
          :key="index"
          :xs="24"
          :sm="12"
          :md="8"
          class="sub-item-col"
        >
          <div
            class="sub-item-card"
            :style="{ '--item-color': currentCategory.color }"
            :class="{ 'has-shadow': index === 0 }"
          >
            <div class="sub-item-header">
              <span class="sub-item-index">{{ String(index + 1).padStart(2, '0') }}</span>
              <div class="sub-item-badge" :style="{ background: currentCategory.color + '15', color: currentCategory.color }">
                {{ currentCategory.name }}
              </div>
            </div>
            <h4 class="sub-item-name">{{ item.name }}</h4>
            <p class="sub-item-desc">{{ item.description }}</p>
            <div class="sub-item-footer">
              <span class="sub-item-duration" :style="{ color: currentCategory.color }">
                <el-icon><Timer /></el-icon>
                预计 {{ item.duration }}
              </span>
              <el-button
                :color="currentCategory.color"
                size="small"
                round
                @click="createApplyForItem(item)"
              >
                立即申请
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 申请列表表格 -->
    <div class="table-section" v-if="currentCategory">
      <div class="content-header">
        <h3 class="content-title">已创建的{{ currentCategory.name }}申请单</h3>
      </div>
      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
        <el-table-column label="申请ID" align="center" prop="applyId" width="80" />
        <el-table-column label="挂号编号" align="center" prop="registerNo" width="110" show-overflow-tooltip />
        <el-table-column label="患者" align="center" prop="patientName" min-width="80" show-overflow-tooltip />
        <el-table-column label="开单医生" align="center" prop="doctorName" min-width="90" show-overflow-tooltip />
        <el-table-column label="医技项目" align="center" prop="techName" min-width="120" show-overflow-tooltip />
        <el-table-column label="部位" align="center" prop="applyPosition" min-width="140" show-overflow-tooltip />
        <el-table-column label="目的" align="center" prop="applyInfo" min-width="140" show-overflow-tooltip />
        <el-table-column label="开立时间" align="center" prop="applyTime" width="120">
          <template #default="scope">
            <span>{{ parseTime(scope.row.applyTime, '{y}-{m}-{d}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" prop="applyStatus" width="120">
          <template #default="scope">
            <el-tag :type="statusType(scope.row.applyStatus)" size="small">
              {{ scope.row.applyStatus || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="支付状态" align="center" prop="payStatus" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.payStatus === '已支付' ? 'success' : 'warning'" size="small">
              {{ scope.row.payStatus || '未支付' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="160" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" icon="View" @click="handleView(scope.row)">查看</el-button>
            <el-button link type="primary" size="small" icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="fetchList"
      />
    </div>

    <!-- 新建/编辑申请单对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogOpen" width="560px" append-to-body destroy-on-close>
      <el-form ref="applyFormRef" :model="applyForm" :rules="formRules" label-width="100px">
        <el-row :gutter="16">
          <!-- 挂号单下拉框 → 选择后自动填充以下只读字段 -->
          <el-col :span="24">
            <el-form-item label="挂号单" prop="registerId">
              <el-select
                v-model="applyForm.registerId"
                filterable
                remote
                :remote-method="searchRegistersForDialog"
                :loading="regLoading"
                placeholder="搜索挂号编号 / 患者姓名"
                clearable
                style="width:100%"
                @change="onDialogRegisterChange"
              >
                <el-option
                  v-for="r in registerOptions"
                  :key="r.registerId"
                  :label="r.registerNo + ' - ' + r.patientName"
                  :value="r.registerId"
                >
                  <span style="font-weight:600">{{ r.registerNo }}</span>
                  <span style="margin-left:8px;color:#64748B">{{ r.patientName }}</span>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <!-- 接诊ID 自动填充 -->
          <el-col :span="12">
            <el-form-item label="接诊ID">
              <el-input v-model="applyForm.encounterId" placeholder="选择挂号后自动填充" disabled />
            </el-form-item>
          </el-col>
          <!-- 患者ID 自动填充 -->
          <el-col :span="12">
            <el-form-item label="患者ID">
              <el-input v-model="applyForm.patientId" placeholder="选择挂号后自动填充" disabled />
            </el-form-item>
          </el-col>
          <!-- 开单医生（自动填充 + 只读显示姓名） -->
          <el-col :span="12">
            <el-form-item label="开单医生">
              <el-input :model-value="applyForm._doctorInfo || '选择挂号后自动填充'" disabled />
            </el-form-item>
          </el-col>
          <!-- 开单科室（自动填充 + 只读显示名称） -->
          <el-col :span="12">
            <el-form-item label="开单科室">
              <el-input :model-value="applyForm._deptInfo || '选择挂号后自动填充'" disabled />
            </el-form-item>
          </el-col>
          <!-- 医技项目下拉框 -->
          <el-col :span="24">
            <el-form-item label="医技项目" prop="techId">
              <el-select
                v-model="applyForm.techId"
                placeholder="选择医技项目"
                style="width:100%"
                filterable
              >
                <el-option
                  v-for="t in dialogTechList"
                  :key="t.id"
                  :label="t.techName"
                  :value="t.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="检查/检验部位" prop="applyPosition">
              <el-input v-model="applyForm.applyPosition" placeholder="请输入检查/检验/处置部位" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="目的要求" prop="applyInfo">
              <el-input v-model="applyForm.applyInfo" type="textarea" :rows="2" placeholder="请输入目的要求" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogOpen = false">取消</el-button>
        <el-button type="primary" @click="submitApply" :loading="submitting">确定提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ApplyDetail">
import { ref, reactive, computed, getCurrentInstance, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listApply, getApply, addApply, updateApply } from "@/api/hisexam/apply"
import { listRegister, getRegister } from '@/api/register/register'
import { listTechnology } from '@/api/hisexam/technology'
import { listEncounter } from '@/api/emr/encounter'
import useUserStore from '@/store/modules/user'
import { ArrowLeft, ArrowRight, Document, Clock, CircleCheckFilled, Timer, Plus, View, Edit } from '@element-plus/icons-vue'

import checkImg1 from '@/assets/exam/检查1.png'
import checkImg2 from '@/assets/exam/检查2.png'
import checkImg3 from '@/assets/exam/检查3.png'
import inspecImg1 from '@/assets/exam/检验1.png'
import inspecImg2 from '@/assets/exam/检验2.png'
import inspecImg3 from '@/assets/exam/检验3.png'
import disposalImg1 from '@/assets/exam/处置1.png'
import disposalImg3 from '@/assets/exam/处置3.png'

const router = useRouter()
const route = useRoute()
const { proxy } = getCurrentInstance()
const userStore = useUserStore()

// 判断是否为检查检验医生(role_id=7, roleKey='lab')
const isExamDoctor = computed(() => {
  const roles = userStore.roles
  if (!roles || !roles.length) return false
  return roles.some(r => {
    if (typeof r === 'string') return r === 'lab'
    if (typeof r === 'object' && r !== null) {
      if (Number(r.roleId) === 7 || Number(r.role_id) === 7) return true
      return r.roleKey === 'lab' || r.role_key === 'lab'
    }
    return false
  })
})

// ============ 分类定义 ============
const categoryMap = {
  CHECK: {
    type: 'CHECK',
    name: '检查',
    subtitle: '影像学检查',
    description: '利用 CT、超声等影像设备进行医学诊断，包含颅内病变CT检查、肺部病变CT检查及皮肤病变检查等项目',
    images: [checkImg1, checkImg2, checkImg3],
    color: '#1A6B54',
    colorBg: '#E8F3EF',
    items: ['颅内病变CT', '肺部病变CT', '皮肤病变']
  },
  INSPEC: {
    type: 'INSPEC',
    name: '检验',
    subtitle: '实验室检验',
    description: '通过分析血液、体液等样本提供诊断依据，包含验血、生化分析等实验室检验项目',
    images: [inspecImg1, inspecImg2, inspecImg3],
    color: '#1A6B54',
    colorBg: '#E8F3EF',
    items: ['验血', '生化检验', '免疫检验']
  },
  DISPOSAL: {
    type: 'DISPOSAL',
    name: '处置',
    subtitle: '临床处置',
    description: '为患者提供直接的治疗与护理服务，包含手术、住院、注射等临床处置项目',
    images: [disposalImg1, disposalImg3, disposalImg1],
    color: '#1A6B54',
    colorBg: '#E8F3EF',
    items: ['手术', '住院', '注射']
  }
}

// ============ 子项目明细数据 ============
const subItemMap = {
  CHECK: [
    { name: '颅内病变CT检查', description: '通过计算机断层扫描对颅脑结构进行高精度成像，筛查肿瘤、出血、梗塞等颅内病变', duration: '30分钟', route: '/hisexam/check/brain-ct' },
    { name: '肺部病变CT检查', description: '利用低剂量螺旋CT技术对肺部进行薄层扫描，检测结节、炎症、纤维化等肺部疾病', duration: '20分钟', route: '/hisexam/check/lung-ct' },
    { name: '皮肤病变检查', description: '通过皮肤镜、病理活检等手段对皮肤病灶进行形态学与组织学评估', duration: '15分钟', route: '/hisexam/check/skin' }
  ],
  INSPEC: [
    { name: '血常规检验', description: '通过全自动血液分析仪检测红细胞、白细胞、血小板等指标，评估血液系统健康状况', duration: '2小时', route: '' },
    { name: '生化全套检验', description: '检测肝功能、肾功能、血糖血脂等多项生化指标，全面评估代谢与脏器功能', duration: '4小时', route: '' },
    { name: '免疫学检验', description: '通过免疫学方法检测抗原抗体反应、自身抗体、补体等，辅助诊断免疫相关疾病', duration: '6小时', route: '' }
  ],
  DISPOSAL: [
    { name: '外科手术', description: '由专业外科团队执行各类手术治疗，包括术前准备、术中操作与术后监护全流程管理', duration: '2-4小时', route: '' },
    { name: '住院治疗', description: '提供全天候住院监护与综合治疗服务，包括药物治疗、生命体征监测、营养支持等', duration: '按方案', route: '' },
    { name: '注射治疗', description: '执行静脉注射、肌肉注射等给药操作，确保药物精确输注并观察不良反应', duration: '30分钟', route: '' }
  ]
}

// ============ 当前分类 ============
const applyType = computed(() => route.query.applyType || 'CHECK')
const currentCategory = computed(() => categoryMap[applyType.value] || categoryMap.CHECK)
const subItems = computed(() => subItemMap[applyType.value] || [])

// ============ Hero 轮播 ============
const heroIndex = ref(0)
let heroTimer = null
function startHeroCarousel() {
  stopHeroCarousel()
  heroTimer = setInterval(() => {
    if (currentCategory.value) {
      heroIndex.value = (heroIndex.value + 1) % currentCategory.value.images.length
    }
  }, 4000)
}
function stopHeroCarousel() {
  if (heroTimer) { clearInterval(heroTimer); heroTimer = null }
}
onMounted(() => { startHeroCarousel(); fetchList() })
onUnmounted(() => { stopHeroCarousel() })

// ============ 返回 ============
function goBack() {
  router.back()
}

// ============ 列表数据 ============
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  applyType: undefined
})

const pendingCount = computed(() => tableData.value.filter(r => r.applyStatus === '待执行' || r.applyStatus === '待缴费').length)
const doneCount = computed(() => tableData.value.filter(r => r.applyStatus === '已完成').length)

function fetchList() {
  loading.value = true
  queryParams.applyType = applyType.value
  listApply(queryParams).then(res => {
    tableData.value = res.rows || []
    total.value = res.total || 0
    loading.value = false
  }).catch(() => { loading.value = false })
}

function statusType(status) {
  const map = { '待缴费': 'warning', '待执行': 'info', '已完成': 'success' }
  return map[status] || 'info'
}

// ============ 新建/编辑申请单 ============
const dialogOpen = ref(false)
const dialogTitle = ref('')
const submitting = ref(false)
const isEdit = ref(false)
const applyFormRef = ref(null)
const applyForm = reactive({
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
  examTime: null
})

const formRules = {
  registerId: [{ required: true, message: '挂号ID不能为空', trigger: 'blur' }],
  patientId: [{ required: true, message: '患者ID不能为空', trigger: 'blur' }],
  doctorId: [{ required: true, message: '开单医生ID不能为空', trigger: 'blur' }],
  deptId: [{ required: true, message: '开单科室ID不能为空', trigger: 'blur' }],
  techId: [{ required: true, message: '医技项目ID不能为空', trigger: 'blur' }]
}

function resetApplyForm() {
  Object.keys(applyForm).forEach(k => applyForm[k] = null)
  applyForm._doctorInfo = ''
  applyForm._deptInfo = ''
  proxy.resetForm && proxy.resetForm("applyFormRef")
}

// ============ 对话框：挂号下拉框 ============
const registerOptions = ref([])
const regLoading = ref(false)

const searchRegistersForDialog = (keyword) => {
  regLoading.value = true
  listRegister({ registerNo: keyword || undefined, pageNum: 1, pageSize: 30 })
    .then(res => { registerOptions.value = res.rows || res.data?.rows || [] })
    .finally(() => { regLoading.value = false })
}

const onDialogRegisterChange = async (registerId) => {
  if (!registerId) return
  try {
    const res = await getRegister(registerId)
    const data = res.data || res
    if (data) {
      applyForm.patientId = data.patientId || ''
      applyForm.doctorId = data.doctorId || ''
      applyForm.deptId = data.deptId || ''
      // 执行人员默认使用挂号的开单医生
      applyForm.operatorId = data.doctorId || ''
      // 录入人员默认使用挂号的开单医生
      applyForm.inputerId = data.doctorId || ''
      // 执行时间默认当前时间
      applyForm.examTime = new Date().toISOString()
      // 尝试通过挂号查找接诊ID
      applyForm.encounterId = data.encounterId || ''
      if (!applyForm.encounterId) {
        try {
          const encRes = await listEncounter({ registerId, pageNum: 1, pageSize: 1 })
          const encList = encRes.rows || encRes.data?.rows || []
          if (encList.length > 0) applyForm.encounterId = encList[0].encounterId
        } catch (e) {}
      }
      applyForm._doctorInfo = (data.doctorName || '') + ' (ID:' + (data.doctorId || '') + ')'
      applyForm._deptInfo = (data.deptName || '') + ' (ID:' + (data.deptId || '') + ')'
    }
  } catch (e) { console.error('获取挂号详情失败:', e) }
}

// ============ 对话框：医技项目下拉框 ============
const dialogTechList = ref([])

const loadDialogTechList = () => {
  // 根据当前分类加载对应类型的医技项目
  const techType = applyType.value // CHECK / INSPEC / DISPOSAL
  listTechnology({ techType, status: '0', pageNum: 1, pageSize: 100 })
    .then(res => { dialogTechList.value = res.rows || res.data?.rows || [] })
}

function handleCreate() {
  isEdit.value = false
  resetApplyForm()
  applyForm.applyType = applyType.value
  dialogTitle.value = `新建${currentCategory.value?.name}申请单`
  loadDialogTechList()
  dialogOpen.value = true
}

function createApplyForItem(item) {
  // 仅 role_id=7(roleKey='lab') 的检查检验医生可跳转检查页面
  if (isExamDoctor.value && item.route) {
    router.push({
      path: item.route,
      query: {
        applyType: applyType.value,
        techName: item.name,         // 医技项目名称，目标页自动匹配下拉框
        itemName: item.name
      }
    })
    return
  }
  // 其他角色弹出对话框创建申请单
  isEdit.value = false
  resetApplyForm()
  applyForm.applyType = applyType.value
  applyForm.applyPosition = item.name
  applyForm.applyInfo = item.description
  dialogTitle.value = `申请：${item.name}`
  loadDialogTechList()  // 加载对应类型的医技项目
  // 自动匹配医技项目名称
  setTimeout(() => {
    const matched = dialogTechList.value.find(t => item.name.includes(t.techName) || t.techName.includes(item.name))
    if (matched) applyForm.techId = matched.id
  }, 300)
  dialogOpen.value = true
}

function handleView(row) {
  getApply(row.applyId).then(res => {
    const d = res.data
    proxy.$modal.alert(
      `<div style="line-height:2">
        <p><b>申请ID：</b>${d.applyId}</p>
        <p><b>类型：</b>${d.applyType}</p>
        <p><b>挂号编号：</b>${d.registerNo || d.registerId || '-'}</p>
        <p><b>患者：</b>${d.patientName || d.patientId || '-'}</p>
        <p><b>开单医生：</b>${d.doctorName || d.doctorId || '-'}</p>
        <p><b>开单科室：</b>${d.deptName || d.deptId || '-'}</p>
        <p><b>医技项目：</b>${d.techName || d.techId || '-'}</p>
        <p><b>部位：</b>${d.applyPosition || '-'}</p>
        <p><b>目的：</b>${d.applyInfo || '-'}</p>
        <p><b>状态：</b>${d.applyStatus || '-'}</p>
        <p><b>结果：</b>${d.examResult || '-'}</p>
      </div>`,
      '申请单详情',
      { dangerouslyUseHTMLString: true }
    )
  })
}

function handleEdit(row) {
  isEdit.value = true
  getApply(row.applyId).then(res => {
    Object.assign(applyForm, res.data)
    dialogTitle.value = `编辑${currentCategory.value?.name}申请单`
    dialogOpen.value = true
  })
}

function submitApply() {
  proxy.$refs["applyFormRef"]?.validate(valid => {
    if (!valid) return
    submitting.value = true
    const action = isEdit.value ? updateApply(applyForm) : addApply(applyForm)
    action.then(() => {
      proxy.$modal.msgSuccess(isEdit.value ? '修改成功' : '新增成功')
      dialogOpen.value = false
      fetchList()
      submitting.value = false
    }).catch(() => { submitting.value = false })
  })
}
</script>

<style scoped lang="scss">
.apply-detail-page {
  animation: fadeIn 0.5s ease;

  @keyframes fadeIn {
    from { opacity: 0; transform: translateY(10px); }
    to   { opacity: 1; transform: translateY(0); }
  }

  /* ========== 返回导航 ========== */
  .back-nav {
    margin-bottom: 20px;
    .back-btn {
      font-size: 14px;
      color: #64748B;
      display: inline-flex;
      align-items: center;
      gap: 6px;
      transition: color 0.3s;
      &:hover { color: #1A6B54; }
    }
  }

  /* ========== Hero 区域 ========== */
  .detail-hero {
    position: relative;
    border-radius: 20px;
    overflow: hidden;
    display: flex;
    min-height: 200px;
    margin-bottom: 24px;

    .hero-bg {
      position: absolute;
      inset: 0;
      z-index: 0;
    }

    .hero-pattern {
      position: absolute;
      inset: 0;
      z-index: 1;
      background: radial-gradient(circle at 30% 60%, rgba(255,255,255,0.08) 0%, transparent 60%);
      opacity: 0.5;
    }

    .hero-content {
      position: relative;
      z-index: 2;
      flex: 1;
      padding: 28px 32px;
      display: flex;
      flex-direction: column;
      justify-content: center;
    }

    .hero-badge {
      display: inline-block;
      padding: 4px 14px;
      background: rgba(255, 255, 255, 0.18);
      backdrop-filter: blur(8px);
      border-radius: 16px;
      color: #FFFFFF;
      font-size: 12px;
      font-weight: 600;
      letter-spacing: 2px;
      width: fit-content;
      margin-bottom: 10px;
    }

    .hero-title {
      font-size: 26px;
      font-weight: 700;
      color: #FFFFFF;
      margin: 0 0 6px;
    }

    .hero-desc {
      font-size: 13px;
      color: rgba(255, 255, 255, 0.8);
      margin: 0 0 12px;
      max-width: 440px;
      line-height: 1.5;
    }

    .hero-meta {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      .hero-tag {
        background: rgba(255, 255, 255, 0.15) !important;
        border-color: rgba(255, 255, 255, 0.25) !important;
        color: #FFFFFF !important;
        font-weight: 500;
        font-size: 12px;
      }
    }

    /* Hero 轮播图 */
    .hero-carousel {
      position: relative;
      z-index: 2;
      width: 55%;
      min-height: 200px;
      overflow: hidden;

      .hero-slide {
        position: absolute;
        inset: 0;
        background-size: cover;
        background-position: center;
        opacity: 0;
        transition: opacity 0.8s ease;
        &.active {
          opacity: 1;
        }
      }

      .hero-carousel-dots {
        position: absolute;
        bottom: 12px;
        left: 50%;
        transform: translateX(-50%);
        display: flex;
        gap: 8px;
        z-index: 3;

        .hero-dot {
          width: 8px;
          height: 8px;
          border-radius: 50%;
          background: rgba(255, 255, 255, 0.4);
          cursor: pointer;
          transition: all 0.3s;
          &.active {
            background: #FFFFFF;
            box-shadow: 0 0 6px rgba(255, 255, 255, 0.5);
            width: 24px;
            border-radius: 4px;
          }
        }
      }
    }

  }

  /* ========== 统计卡片 ========== */
  .stats-row {
    margin-bottom: 28px;

    .stat-card {
      background: #FFFFFF;
      border-radius: 16px;
      padding: 20px 24px;
      display: flex;
      align-items: center;
      gap: 16px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
      border: 1px solid #F1F5F9;
      transition: all 0.3s ease;

      &:hover {
        box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
        transform: translateY(-2px);
      }

      .stat-icon-wrap {
        width: 60px;
        height: 60px;
        border-radius: 14px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: color-mix(in srgb, var(--stat-color) 12%, transparent);
        color: var(--stat-color);
      }

      .stat-info {
        .stat-value {
          font-size: 28px;
          font-weight: 700;
          color: #1E293B;
          line-height: 1;
        }
        .stat-label {
          font-size: 13px;
          color: #64748B;
          margin-top: 4px;
        }
      }
    }
  }

  /* ========== 内容区域 ========== */
  .detail-content {
    margin-bottom: 28px;

    .content-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 20px;

      .content-title {
        font-size: 18px;
        font-weight: 700;
        color: #1E293B;
        margin: 0;
        position: relative;
        padding-left: 14px;

        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 50%;
          transform: translateY(-50%);
          width: 4px;
          height: 20px;
          border-radius: 2px;
          background: v-bind("currentCategory?.color || '#1A6B54'");
        }
      }
    }

    .sub-item-col {
      margin-bottom: 16px;
    }

    .sub-item-card {
      background: #FFFFFF;
      border-radius: 16px;
      padding: 24px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
      border: 1px solid #F1F5F9;
      transition: all 0.35s ease;
      height: 100%;
      display: flex;
      flex-direction: column;

      &:hover {
        box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
        border-color: var(--item-color);
        transform: translateY(-3px);
      }

      .sub-item-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 14px;

        .sub-item-index {
          font-size: 28px;
          font-weight: 800;
          color: #E2E8F0;
          letter-spacing: -1px;
          line-height: 1;
        }

        .sub-item-badge {
          padding: 3px 12px;
          border-radius: 12px;
          font-size: 12px;
          font-weight: 600;
        }
      }

      .sub-item-name {
        font-size: 17px;
        font-weight: 700;
        color: #1E293B;
        margin: 0 0 8px;
      }

      .sub-item-desc {
        font-size: 13px;
        color: #64748B;
        line-height: 1.6;
        margin: 0 0 18px;
        flex: 1;
      }

      .sub-item-footer {
        display: flex;
        align-items: center;
        justify-content: space-between;

        .sub-item-duration {
          font-size: 13px;
          display: flex;
          align-items: center;
          gap: 4px;
          font-weight: 500;
        }
      }
    }
  }

  /* ========== 表格区域 ========== */
  .table-section {
    background: #FFFFFF;
    border-radius: 16px;
    padding: 20px 24px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
    margin-bottom: 20px;

    .content-header {
      margin-bottom: 16px;
      .content-title {
        font-size: 18px;
        font-weight: 700;
        color: #1E293B;
        margin: 0;
        padding-left: 14px;
        position: relative;

        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 50%;
          transform: translateY(-50%);
          width: 4px;
          height: 20px;
          border-radius: 2px;
          background: v-bind("currentCategory?.color || '#1A6B54'");
        }
      }
    }
  }
}
</style>
