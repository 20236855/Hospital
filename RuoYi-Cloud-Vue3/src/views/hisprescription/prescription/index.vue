<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="挂号ID" prop="registerId">
        <el-input
          v-model="queryParams.registerId"
          placeholder="请输入挂号ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="接诊ID" prop="encounterId">
        <el-input
          v-model="queryParams.encounterId"
          placeholder="请输入接诊ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="患者ID" prop="patientId">
        <el-input
          v-model="queryParams.patientId"
          placeholder="请输入患者ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="开方医生ID" prop="doctorId">
        <el-input
          v-model="queryParams.doctorId"
          placeholder="请输入开方医生ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="开方科室ID" prop="deptId">
        <el-input
          v-model="queryParams.deptId"
          placeholder="请输入开方科室ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="处方单号" prop="prescriptionNo">
        <el-input
          v-model="queryParams.prescriptionNo"
          placeholder="请输入处方单号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="处方总金额" prop="totalAmount">
        <el-input
          v-model="queryParams.totalAmount"
          placeholder="请输入处方总金额"
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
      <el-table-column label="处方ID(主键)" align="center" prop="prescriptionId" />
      <el-table-column label="挂号ID" align="center" prop="registerId" />
      <el-table-column label="接诊ID" align="center" prop="encounterId" />
      <el-table-column label="患者ID" align="center" prop="patientId" />
      <el-table-column label="开方医生ID" align="center" prop="doctorId" />
      <el-table-column label="开方科室ID" align="center" prop="deptId" />
      <el-table-column label="处方单号" align="center" prop="prescriptionNo" />
      <el-table-column label="处方总金额" align="center" prop="totalAmount" />
      <el-table-column label="状态：待缴费/已缴费/已发药/已退方" align="center" prop="prescriptionStatus" />
      <el-table-column label="支付状态" align="center" prop="payStatus" />
      <el-table-column label="整体用药嘱托" align="center" prop="drugTip" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hisprescription:prescription:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hisprescription:prescription:remove']">删除</el-button>
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
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="prescriptionRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="挂号ID" prop="registerId">
              <el-input v-model="form.registerId" placeholder="请输入挂号ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="接诊ID" prop="encounterId">
              <el-input v-model="form.encounterId" placeholder="请输入接诊ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="患者ID" prop="patientId">
              <el-input v-model="form.patientId" placeholder="请输入患者ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="开方医生ID" prop="doctorId">
              <el-input v-model="form.doctorId" placeholder="请输入开方医生ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="开方科室ID" prop="deptId">
              <el-input v-model="form.deptId" placeholder="请输入开方科室ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="处方单号" prop="prescriptionNo">
              <el-input v-model="form.prescriptionNo" placeholder="请输入处方单号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="处方总金额" prop="totalAmount">
              <el-input v-model="form.totalAmount" placeholder="请输入处方总金额" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="整体用药嘱托" prop="drugTip">
              <el-input v-model="form.drugTip" type="textarea" placeholder="请输入内容" />
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

<script setup name="Prescription">
import { listPrescription, getPrescription, delPrescription, addPrescription, updatePrescription } from "@/api/hisprescription/prescription"

const { proxy } = getCurrentInstance()

const prescriptionList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

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
      { required: true, message: "挂号ID不能为空", trigger: "blur" }
    ],
    encounterId: [
      { required: true, message: "接诊ID不能为空", trigger: "blur" }
    ],
    patientId: [
      { required: true, message: "患者ID不能为空", trigger: "blur" }
    ],
    doctorId: [
      { required: true, message: "开方医生ID不能为空", trigger: "blur" }
    ],
    deptId: [
      { required: true, message: "开方科室ID不能为空", trigger: "blur" }
    ],
    prescriptionNo: [
      { required: true, message: "处方单号不能为空", trigger: "blur" }
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

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加处方主"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _prescriptionId = row.prescriptionId || ids.value
  getPrescription(_prescriptionId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改处方主"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["prescriptionRef"].validate(valid => {
    if (valid) {
      if (form.value.prescriptionId != null) {
        updatePrescription(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addPrescription(form.value).then(() => {
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
