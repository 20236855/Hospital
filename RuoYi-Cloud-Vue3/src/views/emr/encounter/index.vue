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
      <el-form-item label="患者ID" prop="patientId">
        <el-input
          v-model="queryParams.patientId"
          placeholder="请输入患者ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="医生ID" prop="doctorId">
        <el-input
          v-model="queryParams.doctorId"
          placeholder="请输入医生ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="科室ID" prop="deptId">
        <el-input
          v-model="queryParams.deptId"
          placeholder="请输入科室ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="接诊时间" prop="visitTime">
        <el-date-picker clearable
          v-model="queryParams.visitTime"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择接诊时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="结束时间" prop="finishTime">
        <el-date-picker clearable
          v-model="queryParams.finishTime"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择结束时间">
        </el-date-picker>
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
          v-hasPermi="['emr:encounter:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['emr:encounter:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['emr:encounter:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['emr:encounter:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="encounterList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="接诊ID" align="center" prop="encounterId" width="100" />
      <el-table-column label="挂号ID" align="center" prop="registerId" width="120" />
      <el-table-column label="挂号编号" align="center" prop="registerNo" width="140" />
      <el-table-column label="患者ID" align="center" prop="patientId" width="100" />
      <el-table-column label="患者姓名" align="center" prop="patientName" width="120" />
      <el-table-column label="患者手机号" align="center" prop="patientPhone" width="140" />
      <el-table-column label="排队号" align="center" prop="queueNo" width="100" />
      <el-table-column label="签到时间" align="center" prop="checkInTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.checkInTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="医生ID" align="center" prop="doctorId" width="100" />
      <el-table-column label="科室ID" align="center" prop="deptId" width="100" />
      <el-table-column label="接诊类型" align="center" prop="encounterType" width="120" />
      <el-table-column label="接诊状态" align="center" prop="encounterStatus" width="120" />
      <el-table-column label="接诊时间" align="center" prop="visitTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.visitTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="结束时间" align="center" prop="finishTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.finishTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['emr:encounter:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['emr:encounter:remove']">删除</el-button>
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

    <el-dialog :title="title" v-model="open" width="900px" append-to-body>
      <el-form ref="encounterRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="挂号ID" prop="registerId">
              <el-select v-model="form.registerId" placeholder="请选择挂号" filterable clearable style="width: 100%" @change="handleRegisterChange">
                <el-option
                  v-for="item in registerOptions"
                  :key="item.registerId"
                  :label="formatRegisterLabel(item)"
                  :value="item.registerId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="患者ID" prop="patientId">
              <el-select v-model="form.patientId" placeholder="请选择患者" filterable clearable style="width: 100%">
                <el-option
                  v-for="item in patientOptions"
                  :key="item.patientId"
                  :label="formatPatientLabel(item)"
                  :value="item.patientId"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="医生ID" prop="doctorId">
              <el-input v-model="form.doctorId" placeholder="请输入医生ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="科室ID" prop="deptId">
              <el-input v-model="form.deptId" placeholder="请输入科室ID" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="接诊类型" prop="encounterType">
              <el-input v-model="form.encounterType" placeholder="请输入接诊类型" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="接诊状态" prop="encounterStatus">
              <el-input v-model="form.encounterStatus" placeholder="请输入接诊状态" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="接诊时间" prop="visitTime">
              <el-date-picker clearable
                v-model="form.visitTime"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择接诊时间"
                style="width: 100%">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="finishTime">
              <el-date-picker clearable
                v-model="form.finishTime"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择结束时间"
                style="width: 100%">
              </el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button type="default" @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Encounter">
import { listEncounter, getEncounter, delEncounter, addEncounter, updateEncounter } from "@/api/emr/encounter"
import { listPatient } from "@/api/patient/patient"
import { listRegister } from "@/api/register/register"

const { proxy } = getCurrentInstance()

const encounterList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const patientOptions = ref([])
const registerOptions = ref([])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    registerId: undefined,
    patientId: undefined,
    doctorId: undefined,
    deptId: undefined,
    encounterType: undefined,
    encounterStatus: undefined,
    visitTime: undefined,
    finishTime: undefined,
  },
  rules: {
    registerId: [
      { required: true, message: "挂号ID不能为空", trigger: "blur" }
    ],
    patientId: [
      { required: true, message: "患者ID不能为空", trigger: "blur" }
    ],
    doctorId: [
      { required: true, message: "医生ID不能为空", trigger: "blur" }
    ],
    deptId: [
      { required: true, message: "科室ID不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

function formatPatientLabel(item) {
  if (!item) {
    return ""
  }
  return item.phone ? item.name + " / " + item.phone : (item.name || item.patientId)
}

function getPatientOptions() {
  return listPatient({ pageNum: 1, pageSize: 1000 }).then(response => {
    patientOptions.value = response.rows || []
  })
}

function formatRegisterLabel(item) {
  if (!item) {
    return ""
  }
  return item.registerNo ? item.registerNo : item.registerId
}

function getRegisterOptions() {
  return listRegister({ pageNum: 1, pageSize: 1000, registerStatus: "未完成" }).then(response => {
    registerOptions.value = response.rows || []
  })
}

function handleRegisterChange(registerId) {
  if (!registerId) {
    return
  }
  const register = registerOptions.value.find(item => item.registerId === registerId)
  if (register && register.patientId) {
    form.value.patientId = register.patientId
  }
}

function getList() {
  loading.value = true
  listEncounter(queryParams.value).then(response => {
    encounterList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    encounterId: null,
    registerId: null,
    patientId: null,
    doctorId: null,
    deptId: null,
    encounterType: null,
    encounterStatus: null,
    visitTime: null,
    finishTime: null,
    createTime: null,
    updateTime: null
  }
  proxy.resetForm("encounterRef")
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.encounterId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  getPatientOptions()
  getRegisterOptions()
  open.value = true
  title.value = "添加接诊"
}

function handleUpdate(row) {
  reset()
  getPatientOptions()
  getRegisterOptions()
  const _encounterId = row.encounterId || ids.value
  getEncounter(_encounterId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改接诊"
  })
}

function submitForm() {
  proxy.$refs["encounterRef"].validate(valid => {
    if (valid) {
      if (form.value.encounterId != null) {
        updateEncounter(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addEncounter(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const _encounterIds = row.encounterId || ids.value
  proxy.$modal.confirm('是否确认删除接诊编号为"' + _encounterIds + '"的数据项？').then(function() {
    return delEncounter(_encounterIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download('emr/encounter/export', {
    ...queryParams.value
  }, 'encounter_' + new Date().getTime() + '.xlsx')
}

getList()
getPatientOptions()
</script>
