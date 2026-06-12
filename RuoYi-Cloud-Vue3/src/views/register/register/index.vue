<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="90px">
      <el-form-item label="挂号单号" prop="registerNo">
        <el-input
          v-model="queryParams.registerNo"
          placeholder="请输入挂号单号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="身份证号" prop="idCard">
        <el-input
          v-model="queryParams.idCard"
          placeholder="请输入身份证号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="患者姓名" prop="patientName">
        <el-input
          v-model="queryParams.patientName"
          placeholder="请输入患者姓名"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="科室名称" prop="deptName">
        <el-input
          v-model="queryParams.deptName"
          placeholder="请输入科室名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="医生姓名" prop="doctorName">
        <el-input
          v-model="queryParams.doctorName"
          placeholder="请输入医生姓名"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="挂号级别" prop="levelName">
        <el-input
          v-model="queryParams.levelName"
          placeholder="请输入挂号级别"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="挂号类型" prop="registerType">
        <el-select
          v-model="queryParams.registerType"
          placeholder="请选择挂号类型"
          clearable
          style="width: 150px"
        >
          <el-option label="线上" value="online" />
          <el-option label="线下" value="offline" />
        </el-select>
      </el-form-item>
      <el-form-item label="挂号状态" prop="registerStatus">
        <el-select
          v-model="queryParams.registerStatus"
          placeholder="请选择挂号状态"
          clearable
          style="width: 150px"
        >
          <el-option label="已挂号" value="registered" />
          <el-option label="已退号" value="cancel" />
          <el-option label="已就诊" value="finish" />
        </el-select>
      </el-form-item>
      <el-form-item label="支付状态" prop="payStatus">
        <el-select
          v-model="queryParams.payStatus"
          placeholder="请选择支付状态"
          clearable
          style="width: 150px"
        >
          <el-option label="已支付" value="paid" />
          <el-option label="待支付" value="unpaid" />
        </el-select>
      </el-form-item>
      <el-form-item label="挂号时间" prop="registerTime">
        <el-date-picker
          clearable
          v-model="queryParams.registerTime"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择挂号时间"
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
          v-hasPermi="['register:register:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['register:register:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5" v-if="!isNurseUser">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['register:register:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5" v-if="!isNurseUser">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['register:register:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="registerList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="挂号ID" align="center" prop="registerId" width="100" />
      <el-table-column label="挂号单号" align="center" prop="registerNo" width="130" />
      <el-table-column label="身份证号" align="center" prop="idCard" width="180" />
      <el-table-column label="患者姓名" align="center" prop="patientName" width="120" />
      <el-table-column label="科室" align="center" prop="deptName" width="120" />
      <el-table-column label="医生" align="center" prop="doctorName" width="120" />
      <el-table-column label="挂号级别" align="center" prop="levelName" width="120" />
      <el-table-column label="挂号类型" align="center" width="120">
        <template #default="scope">
          {{ scope.row.registerType === 'online' ? '线上挂号' : '线下挂号' }}
        </template>
      </el-table-column>
      <el-table-column label="挂号状态" align="center" width="120">
        <template #default="scope">
          <span :class="scope.row.registerStatus == 'cancel' ? 'text-red' : scope.row.registerStatus == 'finish' ? 'text-green' : ''">
            {{
              scope.row.registerStatus === 'registered'
                ? '已挂号'
                : scope.row.registerStatus === 'cancel'
                ? '已退号'
                : '已就诊'
            }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="支付状态" align="center" width="120">
        <template #default="scope">
          {{ scope.row.payStatus === 'paid' ? '已支付' : '待支付' }}
        </template>
      </el-table-column>
      <el-table-column label="挂号费用" align="center" prop="registerFee" width="100" />
      <el-table-column label="挂号时间" align="center" prop="registerTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.registerTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" width="200" show-overflow-tooltip />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['register:register:edit']">编辑</el-button>
          <el-button
            v-if="scope.row.registerStatus === 'registered'"
            link
            type="danger"
            icon="Close"
            @click="handleCancelRegister(scope.row)"
            v-hasPermi="['register:register:edit']"
          >退号</el-button>
          <el-button v-if="!isNurseUser" link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['register:register:remove']">删除</el-button>
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
      <el-form ref="registerRef" :model="form" :rules="rules" label-width="115px">
        <input type="hidden" v-model="form.patientId" />
        <input type="hidden" v-model="form.source" />
        <input type="hidden" v-model="form.scheduleId" />

        <el-row :gutter="26">
          <el-col v-if="form.registerId != null" :span="12">
            <el-form-item label="挂号单号" prop="registerNo">
              <el-input v-model="form.registerNo" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input
                v-model="form.idCard"
                placeholder="请输入患者身份证号"
                :disabled="form.registerId != null"
                @blur="handleIdCardBlur"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="26">
          <el-col :span="12">
            <el-form-item label="科室" prop="deptId">
              <el-select
                v-model="form.deptId"
                placeholder="请选择科室"
                filterable
                clearable
                style="width: 100%"
                @change="handleDeptChange"
              >
                <el-option
                  v-for="item in deptList"
                  :key="item.deptId"
                  :label="item.deptName"
                  :value="item.deptId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="医生" prop="doctorId">
              <el-select
                v-model="form.doctorId"
                placeholder="请选择医生"
                filterable
                clearable
                :disabled="!form.deptId"
                style="width: 100%"
              >
                <el-option
                  v-for="item in doctorList"
                  :key="item.doctorId"
                  :label="item.doctorName"
                  :value="item.doctorId"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="26">
          <el-col :span="12">
            <el-form-item label="挂号级别" prop="levelId">
              <el-select
                v-model="form.levelId"
                placeholder="请选择挂号级别"
                filterable
                clearable
                style="width: 100%"
                @change="handleLevelChange"
              >
                <el-option
                  v-for="item in levelList"
                  :key="item.id"
                  :label="item.levelName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="挂号费用" prop="registerFee">
              <el-input v-model="form.registerFee" disabled placeholder="选择级别后自动带出" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 挂号状态隐藏，新增时后端自动赋值 -->
        <input type="hidden" v-model="form.registerStatus" />
        <!-- 挂号类型隐藏，后端固定offline -->
        <input type="hidden" v-model="form.registerType" />
        <el-row :gutter="26">
          <el-col :span="12">
            <el-form-item label="支付状态" prop="payStatus">
              <el-select
                v-model="form.payStatus"
                placeholder="选择支付状态"
                clearable
                style="width: 100%"
              >
                <el-option label="待支付" value="unpaid" />
                <el-option label="已支付" value="paid" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col v-if="form.registerId != null" :span="12">
            <el-form-item label="挂号状态" prop="registerStatus">
              <el-select
                v-model="form.registerStatus"
                placeholder="请选择挂号状态"
                clearable
                disabled
                style="width: 100%"
              >
                <el-option label="已挂号" value="registered" />
                <el-option label="已退号" value="cancel" />
                <el-option label="已就诊" value="finish" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="26">
          <el-col :span="12">
            <el-form-item label="挂号时间" prop="registerTime">
              <el-date-picker
                clearable
                v-model="form.registerTime"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择挂号时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="26">
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
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

<script setup name="Register">
import {
  listRegister,
  getRegister,
  delRegister,
  addRegister,
  updateRegister,
  listRegisterLevel,
  listRegisterDept,
  listRegisterDoctor,
  cancelRegister,
  getRegisterFee
} from "@/api/register/register"
import { getPatientByIdCard } from "@/api/patient/patient"
import useUserStore from "@/store/modules/user"

const { proxy } = getCurrentInstance()
const userStore = useUserStore()
const NURSE_POST_ID = 2
const isNurseUser = computed(() => userStore.roles.includes('doctor') && (userStore.postIds || []).some(postId => Number(postId) === NURSE_POST_ID))

const registerList = ref([])
const levelList = ref([])
const deptList = ref([])
const doctorList = ref([])
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
    registerNo: undefined,
    idCard: undefined,
    patientName: undefined,
    deptName: undefined,
    doctorName: undefined,
    levelName: undefined,
    registerType: undefined,
    registerStatus: undefined,
    payStatus: undefined,
    registerTime: undefined,
  },
  rules: {
    idCard: [
      { required: true, message: "身份证号不能为空", trigger: "blur" }
    ],
    deptId: [
      { required: true, message: "科室不能为空", trigger: "change" }
    ],
    doctorId: [
      { required: true, message: "医生不能为空", trigger: "change" }
    ],
    levelId: [
      { required: true, message: "挂号级别不能为空", trigger: "change" }
    ],
    registerTime: [
      { required: true, message: "挂号时间不能为空", trigger: "change" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询挂号列表 */
function getList() {
  loading.value = true
  listRegister(queryParams.value).then(response => {
    registerList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function getLevelOptions() {
  listRegisterLevel().then(response => {
    levelList.value = response.data || []
  })
}

function getDeptOptions() {
  listRegisterDept().then(response => {
    deptList.value = response.data || []
  })
}

function getDoctorOptions(deptId) {
  if (!deptId) {
    doctorList.value = []
    return Promise.resolve()
  }
  return listRegisterDoctor(deptId).then(response => {
    doctorList.value = response.data || []
  })
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  doctorList.value = []
  form.value = {
    registerId: null,
    registerNo: null,
    idCard: null,
    patientId: null,
    appUserId: null,
    scheduleId: null,
    doctorId: null,
    deptId: null,
    source: 1,
    levelId: null,
    registerType: null,
    registerStatus: null,
    payStatus: 'unpaid',
    registerFee: null,
    registerTime: null,
    remark: null,
    createTime: null,
    updateTime: null
  }
  proxy.resetForm("registerRef")
}

/** 退号按钮操作 */
async function handleCancelRegister(row) {
  proxy.$modal.confirm('确认对该患者进行退号？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await cancelRegister(row.registerId)
    proxy.$modal.msgSuccess('退号成功')
    getList()
  }).catch(() => {})
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
  ids.value = selection.map(item => item.registerId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加挂号"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _registerId = row.registerId || ids.value
  getRegister(_registerId).then(response => {
    form.value = response.data
    getDoctorOptions(form.value.deptId)
    open.value = true
    title.value = "修改挂号"
  })
}

function handleDeptChange(deptId) {
  form.value.doctorId = null
  getDoctorOptions(deptId)
}

async function handleLevelChange(levelId) {
  if (!levelId) {
    form.value.registerFee = null
    return
  }
  const res = await getRegisterFee(levelId)
  form.value.registerFee = res.data
}

async function handleIdCardBlur() {
  if (form.value.registerId != null) {
    return true
  }
  return ensurePatientByIdCard()
}

async function ensurePatientByIdCard() {
  if (!form.value.idCard) {
    form.value.patientId = null
    return false
  }
  const response = await getPatientByIdCard(form.value.idCard)
  const patient = response.data
  if (patient && patient.patientId) {
    form.value.patientId = patient.patientId
    return true
  }
  form.value.patientId = null
  proxy.$modal.msgWarning("患者未建档，请前往患者模块新增")
  return false
}

/** 提交按钮 */
async function submitForm() {
  if (form.value.registerId == null) {
    const patientExists = await ensurePatientByIdCard()
    if (!patientExists) {
      return
    }
  }
  proxy.$refs["registerRef"].validate(valid => {
    if (valid) {
      if (form.value.registerId != null) {
        updateRegister(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addRegister(form.value).then(() => {
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
  if (isNurseUser.value) {
    proxy.$modal.msgWarning("护士岗位不能删除挂号信息，请使用退号功能")
    return
  }
  const _registerIds = row.registerId || ids.value
  proxy.$modal.confirm('是否确认删除挂号编号为"' + _registerIds + '"的数据项？').then(function() {
    return delRegister(_registerIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  if (isNurseUser.value) {
    proxy.$modal.msgWarning("护士岗位不能导出挂号信息")
    return
  }
  proxy.download('register/register/export', {
    ...queryParams.value
  }, `register_${new Date().getTime()}.xlsx`)
}

getList()
getLevelOptions()
getDeptOptions()
</script>
