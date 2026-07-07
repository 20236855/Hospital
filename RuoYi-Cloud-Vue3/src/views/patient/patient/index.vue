<template>
  <div class="app-container patient-console">
    <section class="patient-dashboard">
      <div class="dashboard-bg" aria-hidden="true">
        <span class="scan-line"></span>
        <span class="pulse-ring ring-one"></span>
        <span class="pulse-ring ring-two"></span>
        <span class="float-icon fi-a">
          <svg viewBox="0 0 40 40" fill="none"><rect x="8" y="8" width="24" height="24" rx="6" stroke="currentColor" stroke-width="2.5"/><path d="M20 14v12M14 20h12" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/></svg>
        </span>
        <span class="float-icon fi-b">
          <svg viewBox="0 0 40 40" fill="none"><circle cx="20" cy="20" r="12" stroke="currentColor" stroke-width="2.5"/><path d="M20 14v12M14 20h12" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/></svg>
        </span>
        <span class="float-icon fi-c">
          <svg viewBox="0 0 40 40" fill="none"><path d="M8 32L20 12L32 32H8z" stroke="currentColor" stroke-width="2.5" stroke-linejoin="round"/><circle cx="20" cy="24" r="2" fill="currentColor"/></svg>
        </span>
      </div>
      <div class="dashboard-head">
        <div>
          <div class="dashboard-kicker">
            <span class="live-dot"></span>
            Patient Data Command Center
          </div>
          <h2>患者数据驾驶舱</h2>
          <p>聚合患者建档、年龄结构、联系完整度与血型分布，为门诊接诊和档案治理提供实时洞察。</p>
        </div>
        <div class="dashboard-health">
          <span>档案完整度</span>
          <strong>{{ dashboardStats.completeRate }}%</strong>
          <i :style="{ width: `${dashboardStats.completeRate}%` }"></i>
        </div>
      </div>

      <div class="metric-grid">
        <article v-for="(item, index) in dashboardCards" :key="item.label" class="metric-card" :style="{ '--delay': `${index * 90}ms` }">
          <div class="metric-icon" :class="item.tone">
            <el-icon><component :is="item.icon" /></el-icon>
          </div>
          <div class="metric-copy">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
            <small>{{ item.note }}</small>
          </div>
          <div class="metric-spark">
            <span v-for="bar in item.spark" :key="bar" :style="{ height: `${bar}%` }"></span>
          </div>
        </article>
      </div>

      <div class="dashboard-panels">
        <div class="panel age-panel">
          <div class="panel-title">
            <span>Age Distribution</span>
            <strong>年龄结构</strong>
          </div>
          <div class="age-chart">
            <svg :viewBox="`0 0 ${ageLineChart.w} ${ageLineChart.h}`" class="age-svg">
              <defs>
                <linearGradient id="ageArea" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stop-color="#1f54c8" stop-opacity="0.28" />
                  <stop offset="100%" stop-color="#1f54c8" stop-opacity="0" />
                </linearGradient>
              </defs>
              <path :d="ageLineChart.area" fill="url(#ageArea)" stroke="none" />
              <polyline :points="ageLineChart.line" fill="none" stroke="#1f54c8" stroke-width="2.5" stroke-linejoin="round" stroke-linecap="round" />
              <g v-for="(p, idx) in ageLineChart.points" :key="idx">
                <circle :cx="p.x" :cy="p.y" r="4" fill="#ffffff" stroke="#1f54c8" stroke-width="2.5" />
                <text :x="p.x" :y="p.y - 12" text-anchor="middle" class="age-val">{{ p.count }}</text>
                <text :x="p.x" :y="ageLineChart.h - 6" text-anchor="middle" class="age-label">{{ p.label }}</text>
              </g>
            </svg>
          </div>
        </div>

        <div class="panel gender-panel">
          <div class="panel-title">
            <span>Gender Ratio</span>
            <strong>性别占比</strong>
          </div>
          <div class="donut-wrap">
            <div class="gender-donut" :style="{ '--male': `${dashboardStats.maleRate}%` }">
              <div>
                <strong>{{ dashboardStats.maleRate }}%</strong>
                <span>男性</span>
              </div>
            </div>
            <div class="legend-list">
              <span><i class="male"></i>男 {{ dashboardStats.genderMale }}</span>
              <span><i class="female"></i>女 {{ dashboardStats.genderFemale }}</span>
              <span><i class="unknown"></i>未知 {{ dashboardStats.genderUnknown }}</span>
            </div>
          </div>
        </div>

        <div class="panel blood-panel">
          <div class="panel-title">
            <span>Blood Type</span>
            <strong>血型分布</strong>
          </div>
          <div class="blood-grid">
            <div v-for="item in bloodTypes" :key="item.label" class="blood-item">
              <span>{{ item.label }}</span>
              <strong>{{ item.count }}</strong>
              <i :style="{ height: `${item.percent}%` }"></i>
            </div>
          </div>
        </div>
      </div>
    </section>

    <el-form :model="queryParams" ref="queryRef" v-show="showSearch" label-width="100px">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-form-item label="病历号" prop="patientNo">
            <el-input
              v-model="queryParams.patientNo"
              placeholder="请输入病历号"
              clearable
              @keyup.enter="handleQuery"
            />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="患者姓名" prop="name">
            <el-input
              v-model="queryParams.name"
              placeholder="请输入患者姓名"
              clearable
              @keyup.enter="handleQuery"
            />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="性别" prop="gender">
            <el-select v-model="queryParams.gender" placeholder="请选择性别" clearable style="width: 100%">
              <el-option label="男" value="男" />
              <el-option label="女" value="女" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="出生日期" prop="birthday">
            <el-date-picker clearable
              v-model="queryParams.birthday"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="请选择出生日期"
              style="width: 100%">
            </el-date-picker>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="6">
          <el-form-item label="年龄" prop="age">
            <el-input
              v-model="queryParams.age"
              placeholder="请输入年龄"
              clearable
              @keyup.enter="handleQuery"
            />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="手机号" prop="phone">
            <el-input
              v-model="queryParams.phone"
              placeholder="请输入手机号"
              clearable
              @keyup.enter="handleQuery"
            />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="身份证号" prop="idCard">
            <el-input
              v-model="queryParams.idCard"
              placeholder="请输入身份证号"
              clearable
              @keyup.enter="handleQuery"
            />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="家庭住址" prop="address">
            <el-input
              v-model="queryParams.address"
              placeholder="请输入家庭住址"
              clearable
              @keyup.enter="handleQuery"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="6">
          <el-form-item label="紧急联系人" prop="emergencyContact">
            <el-input
              v-model="queryParams.emergencyContact"
              placeholder="请输入紧急联系人"
              clearable
              @keyup.enter="handleQuery"
            />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="紧急联系电话" prop="emergencyPhone">
            <el-input
              v-model="queryParams.emergencyPhone"
              placeholder="请输入紧急联系电话"
              clearable
              @keyup.enter="handleQuery"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5" v-if="!isPatientUser">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['patient:patient:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5" v-if="!isPatientUser">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['patient:patient:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5" v-if="!isPatientUser && !isNurseUser">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['patient:patient:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5" v-if="!isNurseUser">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['patient:patient:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="patientList" @selection-change="handleSelectionChange">
      <el-table-column v-if="!isPatientUser" type="selection" width="55" align="center" />
      <el-table-column label="病历号" align="center" prop="patientNo" width="120" />
      <el-table-column label="患者姓名" align="center" prop="name" width="100" />
      <el-table-column label="性别" align="center" prop="gender" width="80" />
      <el-table-column label="出生日期" align="center" prop="birthday" width="120">
        <template #default="scope">
          <span>{{ parseTime(scope.row.birthday, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="年龄" align="center" prop="age" width="80" />
      <el-table-column label="手机号" align="center" prop="phone" width="130" />
      <el-table-column label="身份证号" align="center" prop="idCard" width="180" />
      <el-table-column label="血型" align="center" prop="bloodType" width="80" />
      <el-table-column label="婚姻状态" align="center" prop="maritalStatus" width="100" />
      <el-table-column label="家庭住址" align="center" prop="address" width="200" show-overflow-tooltip />
      <el-table-column label="紧急联系人" align="center" prop="emergencyContact" width="120" />
      <el-table-column label="紧急联系电话" align="center" prop="emergencyPhone" width="160" />
      <el-table-column label="状态" align="center" prop="status" width="80" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['patient:patient:edit']">修改</el-button>
          <el-button v-if="!isPatientUser && !isNurseUser" link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['patient:patient:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改患者对话框 -->
    <el-dialog :title="title" v-model="open" width="900px" append-to-body>
      <el-form ref="patientRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="病历号" prop="patientNo" v-if="title === '修改患者'">
              <el-input v-model="form.patientNo" placeholder="请输入病历号" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="title === '修改患者'">
            <el-form-item label="患者姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入患者姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="24" v-else>
            <el-form-item label="患者姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入患者姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" placeholder="请选择性别" clearable style="width: 100%">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出生日期" prop="birthday">
              <el-date-picker clearable
                v-model="form.birthday"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择出生日期"
                @change="calcAge"
                style="width: 100%">
              </el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input v-model="form.age" placeholder="选择出生日期自动计算" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="form.idCard" placeholder="请输入身份证号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="家庭住址" prop="address">
              <el-input v-model="form.address" placeholder="请输入家庭住址" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="紧急联系人" prop="emergencyContact">
              <el-input v-model="form.emergencyContact" placeholder="请输入紧急联系人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="紧急联系电话" prop="emergencyPhone">
              <el-input v-model="form.emergencyPhone" placeholder="请输入紧急联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="血型" prop="bloodType">
              <el-select v-model="form.bloodType" placeholder="请选择血型" clearable style="width: 100%">
                <el-option label="A型" value="A" />
                <el-option label="B型" value="B" />
                <el-option label="AB型" value="AB" />
                <el-option label="O型" value="O" />
                <el-option label="Rh阳性" value="RH+" />
                <el-option label="Rh阴性" value="RH-" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="婚姻状态" prop="maritalStatus">
              <el-select v-model="form.maritalStatus" placeholder="请选择婚姻状态" clearable style="width: 100%">
                <el-option label="未婚" value="未婚" />
                <el-option label="已婚" value="已婚" />
                <el-option label="离异" value="离异" />
                <el-option label="丧偶" value="丧偶" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="过敏史" prop="allergyHistory">
              <el-input v-model="form.allergyHistory" type="textarea" :rows="3" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="既往史" prop="pastHistory">
              <el-input v-model="form.pastHistory" type="textarea" :rows="3" placeholder="请输入内容" />
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

<script setup name="Patient">
import useUserStore from '@/store/modules/user'
import { listPatient, getPatient, delPatient, addPatient, updatePatient } from "@/api/patient/patient"
import { Histogram, Iphone, WarningFilled, UserFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const userStore = useUserStore()

const patientList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const dashboardPatients = ref([])
const NURSE_POST_ID = 2
const isPatientUser = computed(() => userStore.userType === 'patient' || userStore.roles.includes('patient'))
const isNurseUser = computed(() => userStore.roles.includes('doctor') && (userStore.postIds || []).some(postId => Number(postId) === NURSE_POST_ID))

const dashboardStats = computed(() => {
  const source = dashboardPatients.value.length ? dashboardPatients.value : patientList.value
  const sourceTotal = source.length
  const male = source.filter(item => item.gender === '男').length
  const female = source.filter(item => item.gender === '女').length
  const phoneReady = source.filter(item => Boolean(item.phone)).length
  const emergencyReady = source.filter(item => Boolean(item.emergencyContact && item.emergencyPhone)).length
  const completeReady = source.filter(item => Boolean(item.name && item.gender && item.phone && item.idCard && item.address)).length
  const elder = source.filter(item => getPatientAge(item) >= 60).length
  const child = source.filter(item => {
    const age = getPatientAge(item)
    return age >= 0 && age < 18
  }).length
  const maleRate = sourceTotal ? Math.round((male / sourceTotal) * 100) : 0

  return {
    archiveTotal: total.value || sourceTotal,
    sourceTotal,
    genderMale: male,
    genderFemale: female,
    genderUnknown: Math.max(sourceTotal - male - female, 0),
    maleRate,
    phoneReady,
    emergencyReady,
    completeRate: sourceTotal ? Math.round((completeReady / sourceTotal) * 100) : 0,
    elder,
    child
  }
})

const dashboardCards = computed(() => [
  {
    label: '患者档案总量',
    value: dashboardStats.value.archiveTotal,
    note: `当前视图样本 ${dashboardStats.value.sourceTotal} 份`,
    icon: Histogram,
    tone: 'blue',
    spark: [34, 48, 42, 62, 56, 72, 86]
  },
  {
    label: '联系方式可达',
    value: dashboardStats.value.phoneReady,
    note: `手机留存率 ${calcRate(dashboardStats.value.phoneReady, dashboardStats.value.sourceTotal)}%`,
    icon: Iphone,
    tone: 'cyan',
    spark: [42, 54, 50, 68, 74, 78, 88]
  },
  {
    label: '高龄重点关注',
    value: dashboardStats.value.elder,
    note: '60 岁及以上患者',
    icon: WarningFilled,
    tone: 'orange',
    spark: [24, 38, 46, 44, 52, 64, 70]
  },
  {
    label: '应急联系人',
    value: dashboardStats.value.emergencyReady,
    note: `应急信息完整 ${calcRate(dashboardStats.value.emergencyReady, dashboardStats.value.sourceTotal)}%`,
    icon: UserFilled,
    tone: 'green',
    spark: [28, 36, 44, 58, 66, 60, 76]
  }
])

const ageGroups = computed(() => {
  const groups = [
    { label: '0-17', min: 0, max: 17, count: 0 },
    { label: '18-35', min: 18, max: 35, count: 0 },
    { label: '36-59', min: 36, max: 59, count: 0 },
    { label: '60+', min: 60, max: 200, count: 0 }
  ]
  const source = dashboardPatients.value.length ? dashboardPatients.value : patientList.value
  source.forEach(item => {
    const age = getPatientAge(item)
    const group = groups.find(entry => age >= entry.min && age <= entry.max)
    if (group) group.count += 1
  })
  const max = Math.max(...groups.map(item => item.count), 1)
  return groups.map(item => ({
    ...item,
    percent: Math.max(Math.round((item.count / max) * 100), item.count ? 8 : 0)
  }))
})

const ageLineChart = computed(() => {
  const items = ageGroups.value
  const w = 320, h = 170, padX = 28, padY = 30
  const max = Math.max(...items.map(i => i.count), 1)
  const innerW = w - padX * 2
  const innerH = h - padY * 2
  const step = items.length > 1 ? innerW / (items.length - 1) : 0
  const bottomY = padY + innerH
  const points = items.map((it, idx) => ({
    x: padX + step * idx,
    y: bottomY - (it.count / max) * innerH,
    count: it.count,
    label: it.label
  }))
  const line = points.map(p => `${p.x},${p.y}`).join(' ')
  const area = `M ${padX} ${bottomY} L ` + points.map(p => `${p.x} ${p.y}`).join(' L ') + ` L ${points[points.length - 1].x} ${bottomY} Z`
  return { w, h, points, line, area }
})

const bloodTypes = computed(() => {
  const source = dashboardPatients.value.length ? dashboardPatients.value : patientList.value
  const labels = ['A', 'B', 'AB', 'O', 'RH+', 'RH-']
  const rows = labels.map(label => ({
    label,
    count: source.filter(item => item.bloodType === label).length
  }))
  const max = Math.max(...rows.map(item => item.count), 1)
  return rows.map(item => ({
    ...item,
    percent: Math.max(Math.round((item.count / max) * 100), item.count ? 16 : 4)
  }))
})

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    patientNo: undefined,
    name: undefined,
    gender: undefined,
    birthday: undefined,
    age: undefined,
    phone: undefined,
    idCard: undefined,
    bloodType: undefined,
    maritalStatus: undefined,
    address: undefined,
    emergencyContact: undefined,
    emergencyPhone: undefined,
    allergyHistory: undefined,
    pastHistory: undefined,
    status: undefined,
    userId: undefined,
  },
  rules: {
    patientNo: [
      { required: true, message: "病历号不能为空", trigger: "blur", validator: (rule, value, callback) => {
        // 新增患者时不需要验证病历号
        if (title.value === '添加患者') {
          callback()
        } else {
          if (!value) {
            callback(new Error('病历号不能为空'))
          } else {
            callback()
          }
        }
      } }
    ],
    name: [
      { required: true, message: "患者姓名不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询患者列表 */
function getList() {
  loading.value = true
  const params = { ...queryParams.value }
  if (isPatientUser.value) {
    params.userId = userStore.id
  }
  listPatient(params).then(response => {
    patientList.value = response.rows || []
    total.value = response.total
    loading.value = false
    loadDashboardPatients()
  })
}

function loadDashboardPatients() {
  const params = {
    ...queryParams.value,
    pageNum: 1,
    pageSize: 1000
  }
  if (isPatientUser.value) {
    params.userId = userStore.id
  }
  listPatient(params).then(response => {
    dashboardPatients.value = response.rows || []
  })
}

function calcRate(value, base) {
  return base ? Math.round((value / base) * 100) : 0
}

function getPatientAge(patient) {
  if (patient.age !== undefined && patient.age !== null && patient.age !== '') {
    return Number(patient.age)
  }
  if (!patient.birthday) {
    return -1
  }
  const birthday = new Date(patient.birthday)
  if (Number.isNaN(birthday.getTime())) {
    return -1
  }
  const today = new Date()
  let age = today.getFullYear() - birthday.getFullYear()
  const monthDiff = today.getMonth() - birthday.getMonth()
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthday.getDate())) {
    age--
  }
  return age
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    patientId: null,
    patientNo: null,
    name: null,
    gender: null,
    birthday: null,
    age: null,
    phone: null,
    idCard: null,
    bloodType: null,
    maritalStatus: null,
    address: null,
    emergencyContact: null,
    emergencyPhone: null,
    allergyHistory: null,
    pastHistory: null,
    status: null,
    createTime: null,
    updateTime: null,
    userId: null
  }
  proxy.resetForm("patientRef")
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
  if (isPatientUser.value) {
    return
  }
  ids.value = selection.map(item => item.patientId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  if (isPatientUser.value) {
    proxy.$modal.msgWarning("患者账号只能修改自己的信息")
    return
  }
  reset()
  open.value = true
  title.value = "添加患者"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _patientId = row.patientId || ids.value
  getPatient(_patientId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改患者"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["patientRef"].validate(valid => {
    if (valid) {
      // 新增患者时，删除patientNo属性，由后端自动生成
      if (form.value.patientId == null) {
        delete form.value.patientNo
      }
      
      if (form.value.patientId != null) {
        updatePatient(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addPatient(form.value).then(() => {
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
  if (isPatientUser.value) {
    proxy.$modal.msgWarning("患者账号不能删除患者档案")
    return
  }
  if (isNurseUser.value) {
    proxy.$modal.msgWarning("护士岗位不能删除患者档案")
    return
  }
  const _patientIds = row.patientId || ids.value
  proxy.$modal.confirm('是否确认删除患者编号为"' + _patientIds + '"的数据项？').then(function() {
    return delPatient(_patientIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  if (isPatientUser.value) {
    proxy.$modal.msgWarning("患者账号不能导出患者档案")
    return
  }
  if (isNurseUser.value) {
    proxy.$modal.msgWarning("护士岗位不能导出患者档案")
    return
  }
  proxy.download('patient/patient/export', {
    ...queryParams.value
  }, `patient_${new Date().getTime()}.xlsx`)
}

/** 根据出生日期自动计算年龄 */
function calcAge(dateStr) {
  if (!dateStr) {
    form.value.age = null
    return
  }
  const birthday = new Date(dateStr)
  const today = new Date()
  let age = today.getFullYear() - birthday.getFullYear()
  const monthDiff = today.getMonth() - birthday.getMonth()
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthday.getDate())) {
    age--
  }
  form.value.age = age > 0 ? age : 0
}

getList()
</script>

<style scoped lang="scss">
// ========== 主色调（深蓝主色 + 多色点缀）==========
$mint: #1f54c8;
$mint-light: #4a7fe0;
$sky: #2bb3c0;
$sky-light: #7fd3dd;
$white-card: rgba(255, 255, 255, 0.92);
$border: rgba(150, 185, 220, 0.55);
$text-primary: #29405e;
$text-secondary: #4a5b73;
$text-light: #6b7c93;
$shadow: 0 12px 48px rgba(31, 84, 200, 0.08);
$radius: 8px;

.patient-console {
  min-height: calc(100vh - 84px);
  padding: 18px;
  background: #e8edf2;
}

.patient-dashboard {
  position: relative;
  overflow: hidden;
  margin-bottom: 18px;
  padding: 26px 24px 22px;
  border: 1px solid $border;
  border-radius: $radius;
  color: $text-primary;
  background: $white-card;
  backdrop-filter: blur(16px);
  box-shadow: $shadow;
  animation: dashEnter 0.6s ease-out;
}

.patient-dashboard::before {
  content: "";
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    radial-gradient(circle at 15% 25%, rgba(31, 84, 200, 0.06) 0%, transparent 55%),
    radial-gradient(circle at 85% 75%, rgba(43, 179, 192, 0.06) 0%, transparent 55%);
  border-radius: inherit;
}

.patient-dashboard::after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: $mint;
  opacity: 0.4;
  animation: shimmerTop 2s ease-in-out infinite;
}

// ---------- 背景层 ----------
.dashboard-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.scan-line {
  position: absolute;
  top: 0;
  left: -30%;
  width: 34%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(74, 127, 224, 0.1), transparent);
  animation: scanMove 7s linear infinite;
}

.pulse-ring {
  position: absolute;
  width: 240px;
  height: 240px;
  border: 2px solid rgba(74, 127, 224, 0.18);
  border-radius: 50%;
  animation: pulseScale 5s ease-in-out infinite;
  box-shadow: 0 0 40px rgba(74, 127, 224, 0.06);
}

.ring-one { right: 6%; top: -130px; }
.ring-two { right: -90px; bottom: -170px; animation-delay: 2s; width: 310px; height: 310px; }

.float-icon {
  position: absolute;
  width: 44px;
  height: 44px;
  opacity: 0.14;
  pointer-events: none;
  animation: iconDrift 4.8s ease-in-out infinite;

  svg { width: 100%; height: 100%; }
}
.fi-a { top: 18%; right: 14%; color: $mint; animation-delay: 0s; }
.fi-b { top: 55%; left: 8%; color: $sky; animation-delay: 1.6s; width: 36px; height: 36px; }
.fi-c { bottom: 22%; right: 10%; color: $mint; animation-delay: 3.2s; width: 38px; height: 38px; }

// ---------- 头部 ----------
.dashboard-head {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
}

.dashboard-kicker {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  color: $mint;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.live-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: $mint;
  box-shadow: 0 0 0 6px rgba(74, 127, 224, 0.2);
  animation: livePulse 2s ease-in-out infinite;
}

.dashboard-head h2 {
  margin: 0;
  color: $text-primary;
  font-size: 26px;
  font-weight: 800;
}

.dashboard-head p {
  max-width: 720px;
  margin: 8px 0 0;
  color: $text-secondary;
  line-height: 1.7;
  font-size: 14px;
}

.dashboard-health {
  flex: 0 0 190px;
  padding: 16px 18px;
  border: 1px solid $border;
  border-radius: $radius;
  background: $white-card;
  box-shadow: 0 6px 24px rgba(80, 160, 180, 0.06);
  transition: transform 0.25s ease, box-shadow 0.25s ease;

  &:hover {
    transform: translateY(-3px) scale(1.03);
    box-shadow: 0 10px 36px rgba(31, 84, 200, 0.14);
  }
}

.dashboard-health span {
  display: block;
  color: $text-light;
  font-size: 13px;
}

.dashboard-health strong {
  display: block;
  margin: 6px 0;
  color: $mint;
  font-size: 30px;
  font-weight: 800;
  line-height: 1;
}

.dashboard-health i {
  display: block;
  height: 6px;
  border-radius: 999px;
  background: $mint;
  box-shadow: 0 0 14px rgba(31, 84, 200, 0.35);
  animation: barPulse 1.8s ease-in-out infinite;
}

// ---------- 指标卡片网格 ----------
.metric-grid {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-top: 20px;
}

.metric-card {
  display: grid;
  grid-template-columns: 48px 1fr 54px;
  gap: 12px;
  align-items: center;
  min-height: 106px;
  padding: 16px;
  border: 1px solid $border;
  border-radius: $radius;
  background: $white-card;
  box-shadow: 0 3px 18px rgba(31, 84, 200, 0.05);
  transition: border-color 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    box-shadow: 0 6px 22px rgba(31, 84, 200, 0.12);
    border-color: $mint;
  }
}

.metric-icon {
  display: grid;
  width: 46px;
  height: 46px;
  place-items: center;
  border-radius: $radius;
  color: #fff;
  font-size: 22px;

  &.blue   { background: #1f54c8; }
  &.cyan   { background: #2bb3c0; }
  &.orange { background: #f0a23c; }
  &.green  { background: #3bbf8a; }
}

.metric-copy span {
  display: block;
  color: $text-light;
  font-size: 13px;
  font-weight: 500;
}

.metric-copy strong {
  display: block;
  margin-top: 6px;
  color: $text-primary;
  font-size: 28px;
  font-weight: 800;
  line-height: 1;
}

.metric-copy small {
  display: block;
  margin-top: 8px;
  color: $mint;
  font-weight: 600;
}

.metric-spark {
  display: flex;
  align-items: flex-end;
  justify-content: flex-end;
  gap: 4px;
  height: 46px;
}

.metric-spark span {
  width: 5px;
  min-height: 8px;
  border-radius: 3px;
  background: $mint;
  opacity: 0.85;

  &:nth-child(4n+1) { background: $sky; }
}

// ---------- 底部面板 ----------
.dashboard-panels {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1.25fr 0.9fr 1fr;
  gap: 14px;
  margin-top: 14px;
}

.panel {
  min-height: 210px;
  padding: 16px;
  border: 1px solid $border;
  border-radius: $radius;
  background: $white-card;
  box-shadow: 0 3px 18px rgba(80, 160, 180, 0.04);
  transition: transform 0.25s ease, box-shadow 0.25s ease;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 10px 32px rgba(31, 84, 200, 0.1);
  }
}

.panel-title {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.panel-title span {
  color: $mint;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.panel-title strong {
  color: $text-primary;
  font-size: 16px;
  font-weight: 700;
}

// ---------- 年龄结构（折线图）----------
.age-chart {
  width: 100%;
  height: 170px;
  padding: 4px 2px 0;
}

.age-svg {
  width: 100%;
  height: 100%;
  display: block;
  overflow: visible;
}

.age-val {
  fill: #1f54c8;
  font-size: 12px;
  font-weight: 700;
}

.age-label {
  fill: #6b7c93;
  font-size: 11px;
}

// ---------- 性别占比 ----------
.donut-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  min-height: 150px;
}

.gender-donut {
  display: grid;
  width: 120px;
  height: 120px;
  place-items: center;
  border-radius: 50%;
  border: 6px solid;
  border-color: $sky transparent #f0a23c $sky;
  box-shadow: 0 0 24px rgba(43, 179, 192, 0.12);
}

.gender-donut div { text-align: center; }
.gender-donut strong,
.gender-donut span { display: block; }
.gender-donut strong { color: $text-primary; font-size: 24px; font-weight: 800; }
.gender-donut span { margin-top: 4px; color: $text-light; font-size: 12px; }

.legend-list {
  display: grid;
  gap: 10px;
  min-width: 80px;
}

.legend-list span {
  display: flex;
  align-items: center;
  gap: 8px;
  color: $text-secondary;
  font-size: 13px;
}

.legend-list i {
  width: 9px;
  height: 9px;
  border-radius: 50%;
}

.legend-list .male    { background: $sky; }
.legend-list .female  { background: #f0a23c; }
.legend-list .unknown { background: #a5b8c0; }

// ---------- 血型分布 ----------
.blood-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  align-items: end;
  gap: 10px;
  height: 150px;
}

.blood-item {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  height: 100%;
  min-width: 0;
  text-align: center;
}

.blood-item i {
  display: block;
  width: 100%;
  min-height: 8px;
  border-radius: 6px 6px 2px 2px;
  background: $mint;
  opacity: 0.8;
  transition: height 0.45s ease;
}

.blood-item strong {
  margin-bottom: 6px;
  color: $text-primary;
  font-weight: 700;
}

.blood-item span {
  margin-top: 8px;
  color: $text-light;
  font-size: 12px;
}

// ---------- 动画关键帧 ----------
@keyframes scanMove {
  0%   { transform: translateX(0); }
  100% { transform: translateX(380%); }
}

@keyframes pulseScale {
  0%, 100% { transform: scale(0.86); opacity: 0.14; }
  50%      { transform: scale(1.08); opacity: 0.36; }
}

@keyframes livePulse {
  0%, 100% { transform: scale(1); opacity: 0.8; }
  50%      { transform: scale(1.4); opacity: 1; }
}

@keyframes cardRise {
  from { transform: translateY(18px); opacity: 0; }
  to   { transform: translateY(0); opacity: 1; }
}

@keyframes shimmerTop {
  0%, 100% { opacity: 0.3; }
  50%      { opacity: 0.8; }
}

@keyframes barPulse {
  0%, 100% { filter: brightness(1); }
  50%      { filter: brightness(1.4); }
}

@keyframes dashEnter {
  from { opacity: 0; transform: translateY(12px); }
  to   { opacity: 1; transform: translateY(0); }
}

@keyframes donutSpin {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}

@keyframes iconFloat {
  0%, 100% { transform: translateY(0) scale(1); }
  30%      { transform: translateY(-5px) scale(1.06); }
  60%      { transform: translateY(2px) scale(0.98); }
}

@keyframes barPop {
  0%, 100% { transform: scaleY(0.65); }
  30%      { transform: scaleY(1); }
}

@keyframes barShine {
  0%, 100% { opacity: 0.75; }
  50%      { opacity: 1; }
}

@keyframes iconDrift {
  0%, 100% { transform: translate(0, 0) rotate(0deg) scale(1); opacity: 0.12; }
  25%      { transform: translate(6px, -8px) rotate(6deg) scale(1.08); opacity: 0.18; }
  50%      { transform: translate(-4px, -14px) rotate(-3deg) scale(0.94); opacity: 0.1; }
  75%      { transform: translate(-8px, -4px) rotate(4deg) scale(1.04); opacity: 0.16; }
}

@media (max-width: 1360px) {
  .metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .dashboard-panels {
    grid-template-columns: 1fr 1fr;
  }

  .blood-panel {
    grid-column: 1 / -1;
  }
}

@media (max-width: 900px) {
  .patient-console {
    padding: 12px;
  }

  .dashboard-head,
  .donut-wrap {
    flex-direction: column;
  }

  .dashboard-health {
    width: 100%;
    flex-basis: auto;
  }

  .metric-grid,
  .dashboard-panels {
    grid-template-columns: 1fr;
  }

  .blood-panel {
    grid-column: auto;
  }
}
</style>
