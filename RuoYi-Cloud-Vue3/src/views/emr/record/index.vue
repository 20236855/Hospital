<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="接诊ID" prop="encounterId">
        <el-input
          v-model="queryParams.encounterId"
          placeholder="请输入接诊ID"
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
      <el-col :span="1.5" v-if="!isPatientUser">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['emr:record:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5" v-if="!isPatientUser">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['emr:record:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5" v-if="!isPatientUser">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['emr:record:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5" v-if="!isPatientUser">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['emr:record:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="recordList" @selection-change="handleSelectionChange">
      <el-table-column v-if="!isPatientUser" type="selection" width="55" align="center" />
      <el-table-column label="病历ID" align="center" prop="recordId" width="80" />
      <el-table-column label="接诊ID" align="center" prop="encounterId" width="80" />
      <el-table-column label="主诉" align="center" prop="chiefComplaint" width="200">
        <template #default="scope"><div class="cell-wrap">{{ scope.row.chiefComplaint }}</div></template>
      </el-table-column>
      <el-table-column label="现病史" align="center" prop="presentIllness" width="240">
        <template #default="scope"><div class="cell-wrap">{{ scope.row.presentIllness }}</div></template>
      </el-table-column>
      <el-table-column label="既往史" align="center" prop="pastHistory" width="200">
        <template #default="scope"><div class="cell-wrap">{{ scope.row.pastHistory }}</div></template>
      </el-table-column>
      <el-table-column label="过敏史" align="center" prop="allergyHistory" width="180">
        <template #default="scope"><div class="cell-wrap">{{ scope.row.allergyHistory }}</div></template>
      </el-table-column>
      <el-table-column label="体格检查" align="center" prop="physicalExam" width="200">
        <template #default="scope"><div class="cell-wrap">{{ scope.row.physicalExam }}</div></template>
      </el-table-column>
      <el-table-column label="诊断意见" align="center" prop="diagnosisOpinion" width="200">
        <template #default="scope"><div class="cell-wrap">{{ scope.row.diagnosisOpinion }}</div></template>
      </el-table-column>
      <el-table-column label="治疗方案" align="center" prop="treatmentPlan" width="240">
        <template #default="scope"><div class="cell-wrap">{{ scope.row.treatmentPlan }}</div></template>
      </el-table-column>
      <el-table-column label="医生建议" align="center" prop="doctorAdvice" width="200">
        <template #default="scope"><div class="cell-wrap">{{ scope.row.doctorAdvice }}</div></template>
      </el-table-column>
      <el-table-column v-if="!isPatientUser" label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['emr:record:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['emr:record:remove']">删除</el-button>
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

    <!-- 添加或修改电子病历对话框 -->
    <el-dialog :title="title" v-model="open" width="900px" append-to-body>
      <el-form ref="recordRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="接诊ID" prop="encounterId">
              <el-input v-model="form.encounterId" placeholder="请输入接诊ID" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="主诉" prop="chiefComplaint">
              <el-input v-model="form.chiefComplaint" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="现病史" prop="presentIllness">
              <el-input v-model="form.presentIllness" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="既往史" prop="pastHistory">
              <el-input v-model="form.pastHistory" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="过敏史" prop="allergyHistory">
              <el-input v-model="form.allergyHistory" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="体格检查" prop="physicalExam">
              <el-input v-model="form.physicalExam" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="诊断意见" prop="diagnosisOpinion">
              <el-input v-model="form.diagnosisOpinion" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="治疗方案" prop="treatmentPlan">
              <el-input v-model="form.treatmentPlan" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="医生建议" prop="doctorAdvice">
              <el-input v-model="form.doctorAdvice" type="textarea" placeholder="请输入内容" />
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

<script setup name="Record">
import { listRecord, getRecord, delRecord, addRecord, updateRecord } from "@/api/emr/record"
import useUserStore from "@/store/modules/user"

const { proxy } = getCurrentInstance()
const userStore = useUserStore()
const isPatientUser = computed(() => userStore.userType === "patient" || userStore.roles.includes("patient"))

const recordList = ref([])
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
    encounterId: undefined,
    chiefComplaint: undefined,
    presentIllness: undefined,
    pastHistory: undefined,
    allergyHistory: undefined,
    physicalExam: undefined,
    diagnosisOpinion: undefined,
    treatmentPlan: undefined,
    doctorAdvice: undefined,
  },
  rules: {
    encounterId: [
      { required: true, message: "接诊ID不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询电子病历列表 */
function getList() {
  loading.value = true
  listRecord(queryParams.value).then(response => {
    recordList.value = response.rows
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
    recordId: null,
    encounterId: null,
    chiefComplaint: null,
    presentIllness: null,
    pastHistory: null,
    allergyHistory: null,
    physicalExam: null,
    diagnosisOpinion: null,
    treatmentPlan: null,
    doctorAdvice: null,
    createTime: null,
    updateTime: null
  }
  proxy.resetForm("recordRef")
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
  ids.value = selection.map(item => item.recordId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  if (isPatientUser.value) return
  reset()
  open.value = true
  title.value = "添加电子病历"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  if (isPatientUser.value) return
  reset()
  const _recordId = row.recordId || ids.value
  getRecord(_recordId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改电子病历"
  })
}

/** 提交按钮 */
function submitForm() {
  if (isPatientUser.value) return
  proxy.$refs["recordRef"].validate(valid => {
    if (valid) {
      if (form.value.recordId != null) {
        updateRecord(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addRecord(form.value).then(() => {
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
  if (isPatientUser.value) return
  const _recordIds = row.recordId || ids.value
  proxy.$modal.confirm('是否确认删除电子病历编号为"' + _recordIds + '"的数据项？').then(function() {
    return delRecord(_recordIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  if (isPatientUser.value) return
  proxy.download('emr/record/export', {
    ...queryParams.value
  }, `record_${new Date().getTime()}.xlsx`)
}

getList()
</script>

<style scoped>
.cell-wrap {
  white-space: normal;
  word-break: break-all;
  line-height: 1.5;
  padding: 4px 0;
}
</style>
