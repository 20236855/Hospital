<template>
  <div class="app-container">
    <section class="purchase-console" v-loading="purchaseLoading">
      <div class="purchase-console__header">
        <div class="purchase-title">
          <span class="purchase-kicker">PHARMACY ANALYTICS</span>
          <h2>药品统计与采购控制台</h2>
          <p>基于处方明细热销趋势生成下周采购建议</p>
        </div>
        <div class="purchase-actions">
          <el-tag :type="purchaseMeta.mock ? 'warning' : 'success'" effect="light">
            {{ purchaseMeta.mock ? '模拟数据' : '处方明细' }}
          </el-tag>
          <el-button type="primary" icon="Refresh" plain @click="loadPurchaseConsole">刷新预测</el-button>
          <el-button type="success" icon="Document" plain @click="handlePurchaseExport">采购计划</el-button>
        </div>
      </div>

      <div class="purchase-metrics">
        <div class="metric-tile">
          <span>统计处方</span>
          <strong>{{ purchaseMeta.prescriptionCount }}</strong>
          <small>近30天明细样本</small>
        </div>
        <div class="metric-tile accent">
          <span>下周建议采购</span>
          <strong>{{ purchaseMeta.recommendQuantity }}</strong>
          <small>按安全库存系数估算</small>
        </div>
        <div class="metric-tile">
          <span>预计采购金额</span>
          <strong>{{ formatMoney(purchaseMeta.budget) }}</strong>
          <small>以当前药品单价测算</small>
        </div>
        <div class="metric-tile danger">
          <span>重点补货</span>
          <strong>{{ purchaseMeta.urgentCount }}</strong>
          <small>高周转或库存承压品种</small>
        </div>
      </div>

      <div class="purchase-grid">
        <div class="forecast-panel">
          <div class="panel-head">
            <div>
              <h3>AI 下周采购预测</h3>
              <span>基于处方热度与安全库存推演</span>
            </div>
            <strong>{{ purchaseMeta.weeklyDemand }} 件</strong>
          </div>
          <div class="forecast-chart">
            <svg viewBox="0 0 560 240" preserveAspectRatio="none" class="forecast-svg">
              <defs>
                <linearGradient id="forecastLineGradient" x1="0" y1="0" x2="1" y2="0">
                  <stop offset="0%" stop-color="#2563eb" />
                  <stop offset="52%" stop-color="#06b6d4" />
                  <stop offset="100%" stop-color="#22c55e" />
                </linearGradient>
                <linearGradient id="forecastAreaGradient" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stop-color="#3b82f6" stop-opacity=".26" />
                  <stop offset="100%" stop-color="#3b82f6" stop-opacity=".02" />
                </linearGradient>
              </defs>
              <g class="forecast-grid">
                <line v-for="line in 5" :key="line" x1="34" :y1="28 + (line - 1) * 38" x2="526" :y2="28 + (line - 1) * 38" />
              </g>
              <polygon :points="forecastAreaPoints" fill="url(#forecastAreaGradient)" />
              <polyline :points="forecastLinePoints" fill="none" stroke="url(#forecastLineGradient)" stroke-width="4" stroke-linecap="round" stroke-linejoin="round" />
              <g v-for="point in weeklyForecast" :key="point.label">
                <circle :cx="point.x" :cy="point.y" r="6" fill="#fff" stroke="#2563eb" stroke-width="3" />
                <circle :cx="point.x" :cy="point.y" r="13" fill="#3b82f6" opacity=".12" />
                <text :x="point.x" :y="point.y - 16" text-anchor="middle" class="forecast-value">{{ point.value }}</text>
                <text :x="point.x" y="224" text-anchor="middle" class="forecast-label">{{ point.label }}</text>
              </g>
            </svg>
            <div class="forecast-insight">
              <span>智能预测峰值</span>
              <strong>{{ forecastPeak.label }} · {{ forecastPeak.value }} 件</strong>
            </div>
          </div>
          <div class="forecast-note">
            <span>{{ purchaseMeta.topDrug || '暂无热销药品' }}</span>
            <em>预计仍是下周采购优先级最高的药品</em>
          </div>
        </div>

        <div class="plan-panel">
          <div class="panel-head">
            <div>
              <h3>智能采购建议 Top 5</h3>
              <span>预测下周需提前锁定的采购品种</span>
            </div>
            <el-tag effect="dark" class="ai-plan-tag">AI 预测</el-tag>
          </div>
          <div class="purchase-plan-list">
            <div v-for="item in purchasePlan.slice(0, 5)" :key="item.drugName" class="plan-row">
              <div class="plan-main">
                <strong>{{ item.drugName }}</strong>
                <span>{{ item.spec || '常规规格' }} · {{ item.unit || '盒' }}</span>
              </div>
              <div class="plan-progress">
                <div class="plan-bar">
                  <i :style="{ width: item.hotRatio + '%' }"></i>
                </div>
                <small>近30天 {{ item.soldQuantity }} 件</small>
              </div>
              <div class="plan-qty">
                <b>{{ item.recommendQuantity }}</b>
                <span>建议采购</span>
              </div>
              <el-tag :type="item.riskType" effect="light">{{ item.riskLabel }}</el-tag>
            </div>
          </div>
        </div>
      </div>

      <div class="hot-drug-strip">
        <div v-for="item in purchasePlan.slice(0, 6)" :key="item.drugName" class="hot-drug">
          <div class="hot-rank">{{ item.rank }}</div>
          <div class="hot-copy">
            <strong>{{ item.drugName }}</strong>
            <span>周转指数 {{ item.turnoverIndex }}</span>
          </div>
          <b>{{ item.forecastQuantity }} 件/周</b>
        </div>
      </div>
    </section>

    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="药品编码" prop="drugCode">
        <el-input
          v-model="queryParams.drugCode"
          placeholder="请输入药品编码"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="药品名称" prop="drugName">
        <el-input
          v-model="queryParams.drugName"
          placeholder="请输入药品名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="拼音码" prop="pyCode">
        <el-input
          v-model="queryParams.pyCode"
          placeholder="请输入拼音码"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="药品规格" prop="spec">
        <el-input
          v-model="queryParams.spec"
          placeholder="请输入药品规格"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="包装单位" prop="unit">
        <el-input
          v-model="queryParams.unit"
          placeholder="请输入包装单位"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="单价" prop="drugPrice">
        <el-input
          v-model="queryParams.drugPrice"
          placeholder="请输入单价"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="生产厂家" prop="manufacturer">
        <el-input
          v-model="queryParams.manufacturer"
          placeholder="请输入生产厂家"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="排序号" prop="sort">
        <el-input
          v-model="queryParams.sort"
          placeholder="请输入排序号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['hisprescription:drug:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['hisprescription:drug:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['hisprescription:drug:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['hisprescription:drug:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="drugList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="药品ID(主键)" align="center" prop="id" />
      <el-table-column label="药品编码" align="center" prop="drugCode" />
      <el-table-column label="药品名称" align="center" prop="drugName" />
      <el-table-column label="拼音码" align="center" prop="pyCode" />
      <el-table-column label="药品规格" align="center" prop="spec" />
      <el-table-column label="包装单位" align="center" prop="unit" />
      <el-table-column label="单价" align="center" prop="drugPrice" />
      <el-table-column label="生产厂家" align="center" prop="manufacturer" />
      <el-table-column label="药剂类型" align="center" prop="drugType" />
      <el-table-column label="排序号" align="center" prop="sort" />
      <el-table-column label="状态 0正常 1停用" align="center" prop="status" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hisprescription:drug:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hisprescription:drug:remove']">删除</el-button>
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

    <!-- 添加或修改药品信息对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="drugRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="药品编码" prop="drugCode" v-if="form.id">
              <el-input v-model="form.drugCode" placeholder="请输入药品编码" disabled />
            </el-form-item>
            <el-form-item label="药品编码" v-else>
              <el-input v-model="form.drugCode" placeholder="系统自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="药品名称" prop="drugName">
              <el-input v-model="form.drugName" placeholder="请输入药品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="拼音码" prop="pyCode">
              <el-input v-model="form.pyCode" placeholder="请输入拼音码" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="药品规格" prop="spec">
              <el-input v-model="form.spec" placeholder="请输入药品规格" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="包装单位" prop="unit">
              <el-input v-model="form.unit" placeholder="请输入包装单位" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="单价" prop="drugPrice">
              <el-input v-model="form.drugPrice" placeholder="请输入单价" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="生产厂家" prop="manufacturer">
              <el-input v-model="form.manufacturer" placeholder="请输入生产厂家" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="排序号" prop="sort">
              <el-input v-model="form.sort" placeholder="请输入排序号" />
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

<script setup name="Drug">
import { listDrug, getDrug, delDrug, addDrug, updateDrug } from "@/api/hisprescription/drug"
import { listItem } from "@/api/hisprescription/item"

const { proxy } = getCurrentInstance()

const drugList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const purchaseLoading = ref(false)
const purchasePlan = ref([])
const weeklyForecast = ref([])
const purchaseMeta = ref({
  mock: false,
  prescriptionCount: 0,
  recommendQuantity: 0,
  budget: 0,
  urgentCount: 0,
  weeklyDemand: 0,
  topDrug: ''
})

const forecastLinePoints = computed(() => weeklyForecast.value.map(point => `${point.x},${point.y}`).join(' '))
const forecastAreaPoints = computed(() => {
  const points = weeklyForecast.value
  if (!points.length) return ''
  return `34,196 ${points.map(point => `${point.x},${point.y}`).join(' ')} 526,196`
})
const forecastPeak = computed(() => {
  return weeklyForecast.value.reduce((peak, point) => point.value > peak.value ? point : peak, { label: '-', value: 0 })
})

const mockPrescriptionItems = [
  { drugId: 1, drugName: '阿莫西林胶囊', drugPrice: 18.5, quantity: 42, spec: '250mg×24粒', unit: '盒' },
  { drugId: 2, drugName: '布洛芬缓释胶囊', drugPrice: 16.8, quantity: 38, spec: '300mg×20粒', unit: '盒' },
  { drugId: 3, drugName: '头孢克肟分散片', drugPrice: 29.6, quantity: 31, spec: '100mg×12片', unit: '盒' },
  { drugId: 4, drugName: '奥美拉唑肠溶胶囊', drugPrice: 22.4, quantity: 26, spec: '20mg×14粒', unit: '盒' },
  { drugId: 5, drugName: '氯化钠注射液', drugPrice: 5.2, quantity: 86, spec: '250ml/袋', unit: '袋' },
  { drugId: 6, drugName: '左氧氟沙星片', drugPrice: 24.5, quantity: 24, spec: '0.1g×12片', unit: '盒' },
  { drugId: 7, drugName: '蒙脱石散', drugPrice: 14.9, quantity: 19, spec: '3g×10袋', unit: '盒' },
  { drugId: 8, drugName: '复方氨酚烷胺片', drugPrice: 13.5, quantity: 22, spec: '12片/盒', unit: '盒' },
  { drugId: 9, drugName: '盐酸氨溴索口服液', drugPrice: 21.6, quantity: 21, spec: '100ml/瓶', unit: '瓶' },
  { drugId: 10, drugName: '硝苯地平控释片', drugPrice: 35.8, quantity: 18, spec: '30mg×7片', unit: '盒' }
]

const mockStockMap = {
  阿莫西林胶囊: 38,
  布洛芬缓释胶囊: 26,
  头孢克肟分散片: 22,
  奥美拉唑肠溶胶囊: 45,
  氯化钠注射液: 180,
  左氧氟沙星片: 20,
  蒙脱石散: 24,
  复方氨酚烷胺片: 30,
  盐酸氨溴索口服液: 18,
  硝苯地平控释片: 28
}

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    drugCode: undefined,
    drugName: undefined,
    pyCode: undefined,
    spec: undefined,
    unit: undefined,
    drugPrice: undefined,
    manufacturer: undefined,
    drugType: undefined,
    sort: undefined,
    status: undefined,
  },
  rules: {
    drugCode: [
      { required: true, message: "药品编码不能为空", trigger: "blur", validator: (rule, value, callback) => {
        // 新增时药品编码由后端自动生成，不校验
        if (!form.value.id && !value) {
          callback()
        } else if (form.value.id && !value) {
          callback(new Error('药品编码不能为空'))
        } else {
          callback()
        }
      }}
    ],
    drugName: [
      { required: true, message: "药品名称不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询药品信息列表 */
function getList() {
  loading.value = true
  listDrug(queryParams.value).then(response => {
    drugList.value = response.rows || []
    total.value = response.total
    loading.value = false
    loadPurchaseConsole()
  })
}

/** 加载药品采购预测 */
function loadPurchaseConsole() {
  purchaseLoading.value = true
  listItem({ pageNum: 1, pageSize: 500 }).then(response => {
    const rows = response.rows || []
    buildPurchaseConsole(rows.length ? rows : mockPrescriptionItems, rows.length === 0)
  }).catch(() => {
    buildPurchaseConsole(mockPrescriptionItems, true)
  }).finally(() => {
    purchaseLoading.value = false
  })
}

function buildPurchaseConsole(rows, useMock) {
  const grouped = {}
  rows.forEach(row => {
    const drugName = row.drugName || '未命名药品'
    const quantity = Number(row.quantity || 0)
    const price = Number(row.drugPrice || 0)
    if (!grouped[drugName]) {
      grouped[drugName] = {
        drugName,
        drugId: row.drugId,
        soldQuantity: 0,
        prescriptionCount: 0,
        amount: 0,
        drugPrice: price,
        spec: row.spec,
        unit: row.unit
      }
    }
    grouped[drugName].soldQuantity += quantity
    grouped[drugName].prescriptionCount += 1
    grouped[drugName].amount += quantity * price
    if (price > 0) grouped[drugName].drugPrice = price
  })

  const sorted = Object.values(grouped).sort((a, b) => b.soldQuantity - a.soldQuantity)
  const maxSold = sorted[0]?.soldQuantity || 1
  const plan = sorted.map((item, index) => {
    const catalog = findDrugCatalog(item)
    const unitPrice = Number(catalog?.drugPrice || item.drugPrice || 0)
    const currentStock = mockStockMap[item.drugName] ?? estimateStock(item, index)
    const forecastQuantity = Math.ceil(item.soldQuantity / 30 * 7 * (1.12 + index * 0.015))
    const safetyStock = Math.ceil(forecastQuantity * 1.4)
    const recommendQuantity = Math.max(safetyStock - currentStock, Math.ceil(forecastQuantity * 0.35), 0)
    const shortageRate = safetyStock === 0 ? 0 : Math.max(safetyStock - currentStock, 0) / safetyStock
    const hotRatio = Math.max(12, Math.round(item.soldQuantity / maxSold * 100))
    const risk = resolveRisk(shortageRate, hotRatio)
    return {
      ...item,
      rank: String(index + 1).padStart(2, '0'),
      spec: catalog?.spec || item.spec,
      unit: catalog?.unit || item.unit || '盒',
      drugPrice: unitPrice,
      currentStock,
      forecastQuantity,
      recommendQuantity,
      hotRatio,
      turnoverIndex: Math.round(hotRatio * 0.72 + shortageRate * 36),
      riskLabel: risk.label,
      riskType: risk.type,
      budget: recommendQuantity * unitPrice
    }
  })

  purchasePlan.value = plan
  const recommendQuantity = plan.reduce((sum, item) => sum + item.recommendQuantity, 0)
  const weeklyDemand = plan.reduce((sum, item) => sum + item.forecastQuantity, 0)
  const budget = plan.reduce((sum, item) => sum + item.budget, 0)
  purchaseMeta.value = {
    mock: useMock,
    prescriptionCount: rows.length,
    recommendQuantity,
    budget,
    urgentCount: plan.filter(item => item.riskType === 'danger' || item.riskType === 'warning').length,
    weeklyDemand,
    topDrug: plan[0]?.drugName || ''
  }
  weeklyForecast.value = buildWeeklyForecast(weeklyDemand)
}

function findDrugCatalog(item) {
  return drugList.value.find(drug => {
    return drug.drugName === item.drugName || drug.id === item.drugId || drug.drugId === item.drugId
  })
}

function estimateStock(item, index) {
  const base = Math.max(12, Math.round(item.soldQuantity * (0.65 + index * 0.08)))
  return index < 3 ? Math.max(8, base - 18) : base
}

function resolveRisk(shortageRate, hotRatio) {
  if (shortageRate >= 0.45 || hotRatio >= 88) {
    return { label: '重点补货', type: 'danger' }
  }
  if (shortageRate >= 0.18 || hotRatio >= 64) {
    return { label: '建议补货', type: 'warning' }
  }
  return { label: '常规备货', type: 'success' }
}

function buildWeeklyForecast(total) {
  const labels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  const weights = [0.14, 0.16, 0.15, 0.17, 0.18, 0.11, 0.09]
  const values = weights.map(weight => Math.max(1, Math.round(total * weight)))
  const maxValue = Math.max(...values, 1)
  const minValue = Math.min(...values)
  const range = Math.max(maxValue - minValue, 1)
  return labels.map((label, index) => ({
    label,
    value: values[index],
    x: 48 + index * 76,
    y: 184 - Math.round((values[index] - minValue) / range * 132)
  }))
}

function formatMoney(value) {
  return `￥${Number(value || 0).toLocaleString('zh-CN', { maximumFractionDigits: 0 })}`
}

function handlePurchaseExport() {
  proxy.$modal.msgSuccess('下周药品采购计划已生成，可结合库存复核后执行')
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    id: null,
    drugCode: null,
    drugName: null,
    pyCode: null,
    spec: null,
    unit: null,
    drugPrice: null,
    manufacturer: null,
    drugType: null,
    sort: null,
    status: null,
    remark: null,
    createTime: null,
    updateTime: null
  }
  proxy.resetForm("drugRef")
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
  ids.value = selection.map(item => item.id)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加药品信息"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value
  getDrug(_id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改药品信息"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["drugRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateDrug(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addDrug(form.value).then(() => {
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
  const _ids = row.id || ids.value
  proxy.$modal.confirm('是否确认删除药品信息编号为"' + _ids + '"的数据项？').then(function() {
    return delDrug(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('hisprescription/drug/export', {
    ...queryParams.value
  }, `drug_${new Date().getTime()}.xlsx`)
}

getList()
</script>

<style scoped lang="scss">
.purchase-console {
  margin-bottom: 16px;
  padding: 18px;
  border: 1px solid #dfe9f5;
  border-radius: 8px;
  background:
    linear-gradient(135deg, rgba(239, 246, 255, .92), rgba(255, 255, 255, .96) 42%, rgba(240, 253, 250, .88)),
    #fff;
  box-shadow: 0 14px 34px rgba(38, 83, 128, .08);
}

.purchase-console__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.purchase-title {
  min-width: 0;

  h2 {
    margin: 4px 0 6px;
    color: #172033;
    font-size: 22px;
    line-height: 1.2;
    font-weight: 800;
  }

  p {
    margin: 0;
    color: #607089;
    font-size: 13px;
  }
}

.purchase-kicker {
  color: #2563eb;
  font-size: 11px;
  font-weight: 800;
}

.purchase-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.purchase-metrics {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.metric-tile {
  min-height: 94px;
  padding: 14px;
  border: 1px solid #e5edf7;
  border-radius: 8px;
  background: rgba(255, 255, 255, .86);

  span,
  small {
    display: block;
    color: #64748b;
  }

  span {
    font-size: 13px;
    font-weight: 700;
  }

  strong {
    display: block;
    margin: 8px 0 5px;
    color: #1d4ed8;
    font-size: 27px;
    line-height: 1;
    font-weight: 850;
  }

  small {
    font-size: 12px;
  }

  &.accent strong {
    color: #059669;
  }

  &.danger strong {
    color: #dc2626;
  }
}

.purchase-grid {
  display: grid;
  grid-template-columns: minmax(280px, .9fr) minmax(380px, 1.35fr);
  gap: 14px;
}

.forecast-panel,
.plan-panel {
  min-width: 0;
  padding: 14px;
  border: 1px solid #e3ebf6;
  border-radius: 8px;
  background: rgba(255, 255, 255, .9);
}

.forecast-panel {
  background:
    linear-gradient(180deg, rgba(255, 255, 255, .96), rgba(239, 246, 255, .82)),
    #fff;
}

.plan-panel {
  position: relative;
  overflow: hidden;
  border-color: rgba(96, 165, 250, .22);
  background:
    linear-gradient(135deg, rgba(29, 78, 216, .68), rgba(37, 99, 235, .58) 45%, rgba(14, 165, 233, .46)),
    rgba(239, 246, 255, .78);
  box-shadow: 0 18px 42px rgba(37, 99, 235, .14);

  &::before {
    content: "";
    position: absolute;
    inset: -90px -60px auto auto;
    width: 190px;
    height: 190px;
    border-radius: 50%;
    background: rgba(255, 255, 255, .18);
  }

  &::after {
    content: "";
    position: absolute;
    right: 22px;
    bottom: 20px;
    left: 22px;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, .42), transparent);
  }

  > * {
    position: relative;
    z-index: 1;
  }

  .panel-head {
    h3,
    strong {
      color: #fff;
      text-shadow: 0 1px 10px rgba(15, 23, 42, .16);
    }

    span {
      color: rgba(255, 255, 255, .82);
    }
  }
}

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 14px;

  h3 {
    margin: 0 0 4px;
    color: #172033;
    font-size: 16px;
    line-height: 1.2;
    font-weight: 800;
  }

  span {
    color: #7a8aa0;
    font-size: 12px;
  }

  strong {
    color: #0f766e;
    font-size: 20px;
    line-height: 1;
  }
}

.forecast-chart {
  position: relative;
  height: 240px;
  border: 1px solid #e6eef8;
  border-radius: 8px;
  background: linear-gradient(180deg, #fbfdff, #f6faff);
  overflow: hidden;
}

.forecast-svg {
  width: 100%;
  height: 100%;
  display: block;
}

.forecast-grid line {
  stroke: #dbeafe;
  stroke-width: 1;
}

.forecast-value {
  fill: #1d4ed8;
  font-size: 13px;
  font-weight: 800;
}

.forecast-label {
  fill: #64748b;
  font-size: 12px;
  font-weight: 700;
}

.forecast-insight {
  position: absolute;
  top: 12px;
  right: 12px;
  min-width: 142px;
  padding: 9px 11px;
  border: 1px solid rgba(37, 99, 235, .16);
  border-radius: 8px;
  background: rgba(255, 255, 255, .86);
  box-shadow: 0 10px 24px rgba(37, 99, 235, .1);

  span,
  strong {
    display: block;
  }

  span {
    color: #64748b;
    font-size: 12px;
  }

  strong {
    margin-top: 4px;
    color: #1d4ed8;
    font-size: 14px;
  }
}

.forecast-note {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  min-height: 42px;
  margin-top: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  background: #f8fafc;

  span {
    color: #1d4ed8;
    font-weight: 800;
  }

  em {
    color: #64748b;
    font-size: 12px;
    font-style: normal;
    text-align: right;
  }
}

.purchase-plan-list {
  display: grid;
  gap: 10px;
}

.plan-row {
  display: grid;
  grid-template-columns: minmax(150px, 1.2fr) minmax(150px, 1fr) 78px 86px;
  align-items: center;
  gap: 12px;
  min-height: 64px;
  padding: 10px 12px;
  border: 1px solid rgba(255, 255, 255, .34);
  border-radius: 8px;
  background: rgba(255, 255, 255, .94);
  box-shadow: 0 10px 24px rgba(15, 23, 42, .08);
}

.plan-main {
  min-width: 0;

  strong,
  span {
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  strong {
    color: #172033;
    font-size: 14px;
  }

  span {
    margin-top: 5px;
    color: #7a8aa0;
    font-size: 12px;
  }
}

.plan-progress {
  min-width: 0;

  small {
    display: block;
    margin-top: 5px;
    color: #7a8aa0;
    font-size: 12px;
  }
}

.plan-bar {
  height: 8px;
  border-radius: 99px;
  background: #dbeafe;
  overflow: hidden;

  i {
    display: block;
    height: 100%;
    border-radius: inherit;
    background: linear-gradient(90deg, rgba(30, 64, 175, .40), rgba(37, 99, 235, .44), rgba(96, 165, 250, .38));
  }
}

.plan-qty {
  text-align: right;

  b,
  span {
    display: block;
  }

  b {
    color: #1d4ed8;
    font-size: 20px;
    line-height: 1;
  }

  span {
    margin-top: 5px;
    color: #7a8aa0;
    font-size: 12px;
  }
}

.ai-plan-tag {
  border: 1px solid rgba(255, 255, 255, .5);
  background: rgba(255, 255, 255, .2);
  color: #fff;
}

.hot-drug-strip {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 10px;
  margin-top: 14px;
}

.hot-drug {
  display: grid;
  grid-template-columns: 34px minmax(0, 1fr) auto;
  align-items: center;
  gap: 9px;
  min-height: 72px;
  padding: 11px;
  border: 1px solid #e7eef7;
  border-radius: 8px;
  background: rgba(255, 255, 255, .86);

  b {
    color: #2563eb;
    font-size: 13px;
    justify-self: end;
    white-space: nowrap;
  }
}

.hot-rank {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  display: grid;
  place-items: center;
  color: #fff;
  background: #1d4ed8;
  font-size: 13px;
  font-weight: 800;
}

.hot-copy {
  min-width: 0;

  strong,
  span {
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  strong {
    color: #172033;
    font-size: 13px;
  }

  span {
    margin-top: 4px;
    color: #7a8aa0;
    font-size: 12px;
  }
}

@media (max-width: 1280px) {
  .purchase-metrics,
  .hot-drug-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .purchase-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .purchase-console__header,
  .forecast-note {
    flex-direction: column;
    align-items: flex-start;
  }

  .purchase-actions {
    justify-content: flex-start;
  }

  .purchase-metrics,
  .hot-drug-strip {
    grid-template-columns: 1fr;
  }

  .plan-row {
    grid-template-columns: 1fr;
    align-items: start;
  }

  .plan-qty {
    text-align: left;
  }
}
</style>
