<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="60px">
      <el-row :gutter="10">
        <el-col :span="6">
          <el-form-item label="挂号" prop="registerId">
            <el-input v-model="queryParams.registerId" placeholder="挂号ID" clearable @keyup.enter="handleQuery" />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="接诊" prop="encounterId">
            <el-input v-model="queryParams.encounterId" placeholder="接诊ID" clearable @keyup.enter="handleQuery" />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="患者" prop="patientId">
            <el-input v-model="queryParams.patientId" placeholder="患者ID" clearable @keyup.enter="handleQuery" />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="医生" prop="doctorId">
            <el-input v-model="queryParams.doctorId" placeholder="医生ID" clearable @keyup.enter="handleQuery" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="10">
        <el-col :span="6">
          <el-form-item label="科室" prop="deptId">
            <el-input v-model="queryParams.deptId" placeholder="科室ID" clearable @keyup.enter="handleQuery" />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="单号" prop="prescriptionNo">
            <el-input v-model="queryParams.prescriptionNo" placeholder="处方单号" clearable @keyup.enter="handleQuery" />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="金额" prop="totalAmount">
            <el-input v-model="queryParams.totalAmount" placeholder="总金额" clearable @keyup.enter="handleQuery" />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['hisprescription:prescription:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['hisprescription:prescription:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['hisprescription:prescription:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['hisprescription:prescription:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="prescriptionList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="处方ID" align="center" prop="prescriptionId" width="80" />
      <el-table-column label="挂号" align="center" prop="registerId" width="70" />
      <el-table-column label="接诊" align="center" prop="encounterId" width="70" />
      <el-table-column label="患者" align="center" prop="patientId" width="70" />
      <el-table-column label="医生" align="center" prop="doctorId" width="70" />
      <el-table-column label="科室" align="center" prop="deptId" width="70" />
      <el-table-column label="处方单号" align="center" prop="prescriptionNo" width="140" />
      <el-table-column label="总金额" align="center" prop="totalAmount" width="90" />
      <el-table-column label="状态" align="center" prop="prescriptionStatus" />
      <el-table-column label="支付状态" align="center" prop="payStatus" />
      <el-table-column label="用药嘱托" align="center" prop="drugTip" show-overflow-tooltip />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hisprescription:prescription:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hisprescription:prescription:remove']">删除</el-button>
          <el-button link type="warning" icon="Tickets" @click="goToItems(scope.row.prescriptionId)">明细</el-button>
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

    <!-- 添加或修改处方主对话框 -->
    <el-dialog :title="title" v-model="open" width="680px" append-to-body @close="cancel">
      <el-form ref="prescriptionRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="接诊信息" prop="encounterId">
              <el-select v-model="form.encounterId" placeholder="请选择接诊记录" filterable clearable @change="handleEncounterChange" style="width: 100%">
                <el-option
                  v-for="item in encounterOptions"
                  :key="item.encounterId"
                  :label="'接诊#' + item.encounterId + ' - ' + item.patientName"
                  :value="item.encounterId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="患者" prop="patientId">
              <el-input v-model="form.patientId" placeholder="选择接诊自动填入" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开方医生" prop="doctorId">
              <el-input v-model="form.doctorId" :placeholder="isDoctorUser ? '医生登录自动填入' : '请输入开方医生ID'" :disabled="isDoctorUser" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开方科室" prop="deptId">
              <el-input v-model="form.deptId" :placeholder="isDoctorUser ? '医生登录自动填入' : '请输入开方科室ID'" :disabled="isDoctorUser" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="处方单号" prop="prescriptionNo">
              <el-input v-model="form.prescriptionNo" placeholder="系统自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="处方总金额" prop="totalAmount">
              <el-input :model-value="computedTotal" placeholder="自动根据明细计算" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="用药嘱托" prop="drugTip">
              <el-input v-model="form.drugTip" type="textarea" :rows="2" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <!-- 处方明细区域 -->
      <div class="items-area">
        <div class="items-header">
          <span>处方明细 ({{ items.length }} 项)</span>
          <el-button type="primary" size="small" icon="Plus" @click="handleAddItem">新增明细</el-button>
        </div>
        <el-table :data="items" border size="small" max-height="200">
          <el-table-column label="药品名称" prop="drugName" min-width="100" />
          <el-table-column label="单价" prop="drugPrice" width="70" />
          <el-table-column label="数量" prop="quantity" width="60" />
          <el-table-column label="小计" width="70">
            <template #default="{ row }">
              {{ (row.drugPrice || 0) * (row.quantity || 0) }}
            </template>
          </el-table-column>
          <el-table-column label="用法用量" min-width="120">
            <template #default="{ row }">
              {{ row.drugUsage || '-' }} / {{ row.dosage || '-' }} / {{ row.frequency || '-' }} / {{ row.useDays || 0 }}天
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row, $index }">
              <el-button link type="primary" size="small" @click="handleEditItem($index)">编辑</el-button>
              <el-button link type="danger" size="small" @click="handleRemoveItem($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 药品明细编辑弹窗 -->
    <el-dialog :title="itemDialogTitle" v-model="itemDialogOpen" width="560px" append-to-body>
      <el-form ref="itemRef" :model="itemForm" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="药品名称" prop="drugName">
              <el-select v-model="itemForm.drugName" placeholder="请选择药品" filterable clearable @change="onDrugSelect" style="width:100%">
                <el-option
                  v-for="d in drugList"
                  :key="d.drugId"
                  :label="d.drugName"
                  :value="d.drugName"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单价" prop="drugPrice">
              <el-input-number v-model="itemForm.drugPrice" :min="0" :precision="2" style="width:100%" placeholder="0.00" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数量" prop="quantity">
              <el-input-number v-model="itemForm.quantity" :min="1" style="width:100%" placeholder="1" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用法" prop="drugUsage">
              <el-input v-model="itemForm.drugUsage" placeholder="口服/外用等" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用量" prop="dosage">
              <el-input v-model="itemForm.dosage" placeholder="1片/10ml" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="频次" prop="frequency">
              <el-input v-model="itemForm.frequency" placeholder="每日3次" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="天数" prop="useDays">
              <el-input-number v-model="itemForm.useDays" :min="1" style="width:100%" placeholder="7" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="itemTip">
              <el-input v-model="itemForm.itemTip" placeholder="用药嘱托" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmItem">确 定</el-button>
        <el-button @click="itemDialogOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Prescription">
import { listPrescription, getPrescription, delPrescription, updatePrescription, savePrescriptionWithItems, updatePrescriptionWithItems } from "@/api/hisprescription/prescription"
import { listEncounter } from "@/api/emr/encounter"
import { listDrug } from "@/api/hisprescription/drug"
import { getMyDoctorInfo } from "@/api/hisdoctor/doctor"
import { listItem } from "@/api/hisprescription/item"
import useUserStore from "@/store/modules/user"

const { proxy } = getCurrentInstance()
const userStore = useUserStore()
const router = useRouter()

// 明细数据
const items = ref([])
const itemDialogOpen = ref(false)
const itemDialogTitle = ref('新增药品明细')
const editingItemIndex = ref(-1)
const itemForm = ref({ drugName: '', drugPrice: null, quantity: 1, drugUsage: '', dosage: '', frequency: '', useDays: 7, itemTip: '' })

const prescriptionList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const encounterOptions = ref([])
const drugList = ref([])

const isDoctorUser = computed(() => {
  return userStore.roles?.some(r => r === 'doctor' || r.roleKey === 'doctor' || r.roleId === 5) || false
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
    prescriptionNo: undefined,
    totalAmount: undefined,
    prescriptionStatus: undefined,
    payStatus: undefined,
    drugTip: undefined,
  },
  rules: {
    registerId: [
      { required: false, message: "", trigger: "change" }
    ],
    encounterId: [
      { required: true, message: "接诊信息不能为空", trigger: "change" }
    ],
    patientId: [
      { required: true, message: "患者不能为空", trigger: "change" }
    ],
    doctorId: [
      { required: true, message: "开方医生不能为空", trigger: "blur" }
    ],
    deptId: [
      { required: true, message: "开方科室不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询处方主列表 */
function getList() {
  loading.value = true
  listPrescription(queryParams.value).then(response => {
    prescriptionList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    prescriptionId: null,
    registerId: null,
    encounterId: null,
    patientId: null,
    doctorId: null,
    deptId: null,
    prescriptionNo: null,
    totalAmount: null,
    prescriptionStatus: null,
    payStatus: null,
    drugTip: null,
    createTime: null,
    updateTime: null
  }
  items.value = []
  encounterOptions.value = []
  proxy.resetForm("prescriptionRef")
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
  ids.value = selection.map(item => item.prescriptionId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 跳转到处方明细管理页面 */
function goToItems(prescriptionId) {
  router.push({ path: '/hisprescription/item', query: { prescriptionId } })
}

/** 新增按钮操作 */
async function handleAdd() {
  reset()
  encounterOptions.value = []
  loadDrugList()
  title.value = "添加处方"
  if (isDoctorUser.value) {
    if (userStore.deptId) {
      form.value.deptId = userStore.deptId
    }
    try {
      const res = await getMyDoctorInfo()
      if (res.data && res.data.doctorId) {
        form.value.doctorId = res.data.doctorId
        if (!form.value.deptId) form.value.deptId = res.data.deptId
        if (form.value.deptId) loadEncounterOptions(form.value.deptId, res.data.doctorId)
      }
    } catch (e) {
      console.error('获取医生信息失败', e)
    }
  }
  open.value = true
}

/** 加载本科室接诊列表 */
function loadEncounterOptions(deptId, doctorId) {
  const params = { deptId, pageNum: 1, pageSize: 200 }
  if (doctorId) params.doctorId = doctorId
  listEncounter(params).then(res => {
    encounterOptions.value = (res.rows || []).map(e => ({
      encounterId: e.encounterId,
      registerId: e.registerId,
      patientId: e.patientId,
      doctorId: e.doctorId,
      deptId: e.deptId,
      encounterType: e.encounterType || '普通门诊',
      patientName: e.patientName || ('患者' + e.patientId),
      doctorName: e.doctorName || ('医生' + e.doctorId)
    }))
  })
}

/** 选择接诊后自动填充患者ID和挂号ID */
function handleEncounterChange(encounterId) {
  if (!encounterId) {
    form.value.patientId = null
    form.value.registerId = null
    return
  }
  const selected = encounterOptions.value.find(e => e.encounterId === encounterId)
  if (selected) {
    form.value.patientId = selected.patientId
    form.value.registerId = selected.registerId
  }
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  loadDrugList()
  const _prescriptionId = row.prescriptionId || ids.value
  getPrescription(_prescriptionId).then(response => {
    form.value = response.data
    // 加载该处方的已有明细
    listItem({ prescriptionId: _prescriptionId, pageNum: 1, pageSize: 200 }).then(res => {
      items.value = (res.rows || []).map(i => ({
        drugId: i.drugId,
        drugName: i.drugName,
        drugPrice: i.drugPrice,
        quantity: i.quantity,
        drugUsage: i.drugUsage,
        dosage: i.dosage,
        frequency: i.frequency,
        useDays: i.useDays,
        itemTip: i.itemTip
      }))
    })
    open.value = true
    title.value = "修改处方"
  })
}

/** 总金额自动计算 */
const computedTotal = computed(() => {
  return items.value.reduce((sum, i) => sum + (i.drugPrice || 0) * (i.quantity || 0), 0).toFixed(2)
})

/** 加载药品列表 */
function loadDrugList() {
  listDrug({ pageNum: 1, pageSize: 500 }).then(res => {
    drugList.value = res.rows || []
  })
}

/** 选择药品后自动填单价 */
function onDrugSelect(drugName) {
  const drug = drugList.value.find(d => d.drugName === drugName)
  if (drug) {
    itemForm.value.drugId = drug.id || drug.drugId
    itemForm.value.drugPrice = drug.drugPrice || 0
  }
}

/** 新增明细 */
function handleAddItem() {
  if (!drugList.value.length) loadDrugList()
  itemForm.value = { drugId: null, drugName: '', drugPrice: null, quantity: 1, drugUsage: '', dosage: '', frequency: '', useDays: 7, itemTip: '' }
  editingItemIndex.value = -1
  itemDialogTitle.value = '新增药品明细'
  itemDialogOpen.value = true
}

/** 编辑明细 */
function handleEditItem(index) {
  const row = items.value[index]
  itemForm.value = { ...row }
  editingItemIndex.value = index
  itemDialogTitle.value = '编辑药品明细'
  itemDialogOpen.value = true
}

/** 删除明细 */
function handleRemoveItem(index) {
  items.value.splice(index, 1)
}

/** 确认明细 */
function confirmItem() {
  if (!itemForm.value.drugName) {
    proxy.$modal.msgError('请输入药品名称')
    return
  }
  if (editingItemIndex.value >= 0) {
    items.value[editingItemIndex.value] = { ...itemForm.value }
  } else {
    items.value.push({ ...itemForm.value })
  }
  itemDialogOpen.value = false
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["prescriptionRef"].validate(valid => {
    if (valid) {
      if (form.value.prescriptionId != null) {
        updatePrescriptionWithItems({
          prescription: { ...form.value },
          items: items.value
        }).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        savePrescriptionWithItems({
          prescription: { ...form.value },
          items: items.value
        }).then(() => {
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
  const _prescriptionIds = row.prescriptionId || ids.value
  proxy.$modal.confirm('是否确认删除处方主编号为"' + _prescriptionIds + '"的数据项？').then(function() {
    return delPrescription(_prescriptionIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('hisprescription/prescription/export', {
    ...queryParams.value
  }, `prescription_${new Date().getTime()}.xlsx`)
}

getList()
</script>

<style scoped lang="scss">
.items-area {
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid #ebeef5;
}

.items-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;

  span {
    font-weight: 700;
    font-size: 14px;
    color: #303133;
  }
}
</style>
