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
      <el-form-item label="排队号" prop="queueNo">
        <el-input
          v-model="queryParams.queueNo"
          placeholder="请输入排队号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="签到时间" prop="checkInTime">
        <el-date-picker clearable
          v-model="queryParams.checkInTime"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择签到时间">
        </el-date-picker>
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
          v-hasPermi="['register:in:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5" v-if="!isPatientUser">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['register:in:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5" v-if="!isPatientUser">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['register:in:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5" v-if="!isPatientUser">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['register:in:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="inList" @selection-change="handleSelectionChange">
      <el-table-column v-if="!isPatientUser" type="selection" width="55" align="center" />
      <el-table-column label="签到ID" align="center" prop="checkInId" />
      <el-table-column label="挂号ID" align="center" prop="registerId" />
      <el-table-column label="排队号" align="center" prop="queueNo" />
      <el-table-column label="签到时间" align="center" prop="checkInTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.checkInTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="签到状态" align="center" prop="status" />
      <el-table-column v-if="!isPatientUser" label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['register:in:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['register:in:remove']">删除</el-button>
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

    <!-- 添加或修改签到对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="inRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="挂号ID" prop="registerId">
              <el-input v-model="form.registerId" placeholder="请输入挂号ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="排队号" prop="queueNo">
              <el-input v-model="form.queueNo" placeholder="请输入排队号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="签到时间" prop="checkInTime">
              <el-date-picker clearable
                v-model="form.checkInTime"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择签到时间">
              </el-date-picker>
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

<script setup name="In">
import { listIn, getIn, delIn, addIn, updateIn } from "@/api/register/in"
import useUserStore from "@/store/modules/user"

const { proxy } = getCurrentInstance()
const userStore = useUserStore()
const isPatientUser = computed(() => userStore.userType === "patient" || userStore.roles.includes("patient"))

const inList = ref([])
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
    queueNo: undefined,
    checkInTime: undefined,
    status: undefined,
  },
  rules: {
    registerId: [
      { required: true, message: "挂号ID不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询签到列表 */
function getList() {
  loading.value = true
  listIn(queryParams.value).then(response => {
    inList.value = response.rows
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
    checkInId: null,
    registerId: null,
    queueNo: null,
    checkInTime: null,
    status: null,
    createTime: null,
    updateTime: null
  }
  proxy.resetForm("inRef")
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
  ids.value = selection.map(item => item.checkInId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  if (isPatientUser.value) return
  reset()
  open.value = true
  title.value = "添加签到"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  if (isPatientUser.value) return
  reset()
  const _checkInId = row.checkInId || ids.value
  getIn(_checkInId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改签到"
  })
}

/** 提交按钮 */
function submitForm() {
  if (isPatientUser.value) return
  proxy.$refs["inRef"].validate(valid => {
    if (valid) {
      if (form.value.checkInId != null) {
        updateIn(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addIn(form.value).then(() => {
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
  const _checkInIds = row.checkInId || ids.value
  proxy.$modal.confirm('是否确认删除签到编号为"' + _checkInIds + '"的数据项？').then(function() {
    return delIn(_checkInIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  if (isPatientUser.value) return
  proxy.download('register/in/export', {
    ...queryParams.value
  }, `in_${new Date().getTime()}.xlsx`)
}

getList()
</script>
