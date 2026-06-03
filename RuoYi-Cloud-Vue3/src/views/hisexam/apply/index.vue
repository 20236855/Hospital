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
      <el-form-item label="开单医生ID" prop="doctorId">
        <el-input
          v-model="queryParams.doctorId"
          placeholder="请输入开单医生ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="开单科室ID" prop="deptId">
        <el-input
          v-model="queryParams.deptId"
          placeholder="请输入开单科室ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="医技项目ID" prop="techId">
        <el-input
          v-model="queryParams.techId"
          placeholder="请输入医技项目ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="检查/检验/处置部位" prop="applyPosition">
        <el-input
          v-model="queryParams.applyPosition"
          placeholder="请输入检查/检验/处置部位"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="执行人员ID" prop="operatorId">
        <el-input
          v-model="queryParams.operatorId"
          placeholder="请输入执行人员ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="结果录入人员ID" prop="inputerId">
        <el-input
          v-model="queryParams.inputerId"
          placeholder="请输入结果录入人员ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="开立时间" prop="applyTime">
        <el-date-picker clearable
          v-model="queryParams.applyTime"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择开立时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="执行时间" prop="examTime">
        <el-date-picker clearable
          v-model="queryParams.examTime"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择执行时间">
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
          v-hasPermi="['hisexam:apply:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['hisexam:apply:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['hisexam:apply:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['hisexam:apply:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="applyList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="申请ID(主键)" align="center" prop="applyId" />
      <el-table-column label="挂号ID" align="center" prop="registerId" />
      <el-table-column label="接诊ID" align="center" prop="encounterId" />
      <el-table-column label="患者ID" align="center" prop="patientId" />
      <el-table-column label="开单医生ID" align="center" prop="doctorId" />
      <el-table-column label="开单科室ID" align="center" prop="deptId" />
      <el-table-column label="类型：CHECK检查/INSPEC检验/DISPOSAL处置" align="center" prop="applyType" />
      <el-table-column label="医技项目ID" align="center" prop="techId" />
      <el-table-column label="目的要求" align="center" prop="applyInfo" />
      <el-table-column label="检查/检验/处置部位" align="center" prop="applyPosition" />
      <el-table-column label="执行人员ID" align="center" prop="operatorId" />
      <el-table-column label="结果录入人员ID" align="center" prop="inputerId" />
      <el-table-column label="开立时间" align="center" prop="applyTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.applyTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="执行时间" align="center" prop="examTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.examTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="执行结果" align="center" prop="examResult" />
      <el-table-column label="CT/影像文件访问地址，检验、处置类可留空" align="center" prop="imageUrl" />
      <el-table-column label="状态：待缴费/待执行/已完成" align="center" prop="applyStatus" />
      <el-table-column label="支付状态" align="center" prop="payStatus" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hisexam:apply:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hisexam:apply:remove']">删除</el-button>
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

    <!-- 添加或修改检查/检验/处置申请单对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="applyRef" :model="form" :rules="rules" label-width="100px">
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
            <el-form-item label="开单医生ID" prop="doctorId">
              <el-input v-model="form.doctorId" placeholder="请输入开单医生ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="开单科室ID" prop="deptId">
              <el-input v-model="form.deptId" placeholder="请输入开单科室ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="医技项目ID" prop="techId">
              <el-input v-model="form.techId" placeholder="请输入医技项目ID" />
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
            <el-form-item label="执行人员ID" prop="operatorId">
              <el-input v-model="form.operatorId" placeholder="请输入执行人员ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="结果录入人员ID" prop="inputerId">
              <el-input v-model="form.inputerId" placeholder="请输入结果录入人员ID" />
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
import { listApply, getApply, delApply, addApply, updateApply } from "@/api/hisexam/apply"

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
      { required: true, message: "挂号ID不能为空", trigger: "blur" }
    ],
    encounterId: [
      { required: true, message: "接诊ID不能为空", trigger: "blur" }
    ],
    patientId: [
      { required: true, message: "患者ID不能为空", trigger: "blur" }
    ],
    doctorId: [
      { required: true, message: "开单医生ID不能为空", trigger: "blur" }
    ],
    deptId: [
      { required: true, message: "开单科室ID不能为空", trigger: "blur" }
    ],
    applyType: [
      { required: true, message: "类型：CHECK检查/INSPEC检验/DISPOSAL处置不能为空", trigger: "change" }
    ],
    techId: [
      { required: true, message: "医技项目ID不能为空", trigger: "blur" }
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
  open.value = true
  title.value = "添加检查/检验/处置申请单"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _applyId = row.applyId || ids.value
  getApply(_applyId).then(response => {
    form.value = response.data
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

getList()
</script>
