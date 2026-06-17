<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" v-show="showSearch" label-width="90px">
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
      <el-table-column label="紧急联系电话" align="center" prop="emergencyPhone" width="140" />
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
const NURSE_POST_ID = 2
const isPatientUser = computed(() => userStore.userType === 'patient' || userStore.roles.includes('patient'))
const isNurseUser = computed(() => userStore.roles.includes('doctor') && (userStore.postIds || []).some(postId => Number(postId) === NURSE_POST_ID))

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
    patientList.value = response.rows
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
