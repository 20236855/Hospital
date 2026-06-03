<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="医生ID" prop="doctorId">
        <el-input
          v-model="queryParams.doctorId"
          placeholder="请输入医生ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="排班日期" prop="scheduleDate">
        <el-date-picker clearable
          v-model="queryParams.scheduleDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择排班日期">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="午别" prop="timeSlot">
        <el-input
          v-model="queryParams.timeSlot"
          placeholder="请输入午别"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="最大挂号数" prop="maxNumber">
        <el-input
          v-model="queryParams.maxNumber"
          placeholder="请输入最大挂号数"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="已预约人数" prop="reservedNumber">
        <el-input
          v-model="queryParams.reservedNumber"
          placeholder="请输入已预约人数"
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
          v-hasPermi="['hisdoctor:schedule:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['hisdoctor:schedule:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['hisdoctor:schedule:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['hisdoctor:schedule:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="scheduleList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="排班ID" align="center" prop="scheduleId" />
      <el-table-column label="医生ID" align="center" prop="doctorId" />
      <el-table-column label="排班日期" align="center" prop="scheduleDate" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.scheduleDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="午别" align="center" prop="timeSlot" />
      <el-table-column label="最大挂号数" align="center" prop="maxNumber" />
      <el-table-column label="已预约人数" align="center" prop="reservedNumber" />
      <el-table-column label="状态" align="center" prop="status" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hisdoctor:schedule:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hisdoctor:schedule:remove']">删除</el-button>
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

    <!-- 添加或修改医生排班对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="scheduleRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="医生ID" prop="doctorId">
              <el-input v-model="form.doctorId" placeholder="请输入医生ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="排班日期" prop="scheduleDate">
              <el-date-picker clearable
                v-model="form.scheduleDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择排班日期">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="午别" prop="timeSlot">
              <el-input v-model="form.timeSlot" placeholder="请输入午别" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="最大挂号数" prop="maxNumber">
              <el-input v-model="form.maxNumber" placeholder="请输入最大挂号数" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="已预约人数" prop="reservedNumber">
              <el-input v-model="form.reservedNumber" placeholder="请输入已预约人数" />
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

<script setup name="Schedule">
import { listSchedule, getSchedule, delSchedule, addSchedule, updateSchedule } from "@/api/hisdoctor/schedule"

const { proxy } = getCurrentInstance()

const scheduleList = ref([])
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
    doctorId: undefined,
    scheduleDate: undefined,
    timeSlot: undefined,
    maxNumber: undefined,
    reservedNumber: undefined,
    status: undefined,
  },
  rules: {
    doctorId: [
      { required: true, message: "医生ID不能为空", trigger: "blur" }
    ],
    scheduleDate: [
      { required: true, message: "排班日期不能为空", trigger: "blur" }
    ],
    timeSlot: [
      { required: true, message: "午别不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询医生排班列表 */
function getList() {
  loading.value = true
  listSchedule(queryParams.value).then(response => {
    scheduleList.value = response.rows
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
    scheduleId: null,
    doctorId: null,
    scheduleDate: null,
    timeSlot: null,
    maxNumber: null,
    reservedNumber: null,
    status: null,
    createTime: null,
    updateTime: null
  }
  proxy.resetForm("scheduleRef")
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
  ids.value = selection.map(item => item.scheduleId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加医生排班"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _scheduleId = row.scheduleId || ids.value
  getSchedule(_scheduleId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改医生排班"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["scheduleRef"].validate(valid => {
    if (valid) {
      if (form.value.scheduleId != null) {
        updateSchedule(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addSchedule(form.value).then(() => {
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
  const _scheduleIds = row.scheduleId || ids.value
  proxy.$modal.confirm('是否确认删除医生排班编号为"' + _scheduleIds + '"的数据项？').then(function() {
    return delSchedule(_scheduleIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('hisdoctor/schedule/export', {
    ...queryParams.value
  }, `schedule_${new Date().getTime()}.xlsx`)
}

getList()
</script>
