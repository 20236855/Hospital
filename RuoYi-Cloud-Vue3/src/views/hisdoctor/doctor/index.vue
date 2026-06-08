<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="系统用户ID" prop="userId">
        <el-input
          v-model="queryParams.userId"
          placeholder="请输入系统用户ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="所属科室" prop="deptId">
        <el-input
          v-model="queryParams.deptId"
          placeholder="请输入所属科室"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="医生工号" prop="doctorNo">
        <el-input
          v-model="queryParams.doctorNo"
          placeholder="请输入医生工号"
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
      <el-form-item label="性别" prop="gender">
        <el-input
          v-model="queryParams.gender"
          placeholder="请输入性别"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input
          v-model="queryParams.phone"
          placeholder="请输入手机号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="挂号级别" prop="levelId">
        <el-select
          v-model="queryParams.levelId"
          placeholder="请选择挂号级别"
          clearable
          @keyup.enter="handleQuery"
        >
          <el-option
            v-for="item in levelList"
            :key="item.levelId"
            :label="item.levelName"
            :value="item.levelId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="头像" prop="avatar">
        <el-input
          v-model="queryParams.avatar"
          placeholder="请输入头像"
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
          v-hasPermi="['hisdoctor:doctor:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['hisdoctor:doctor:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['hisdoctor:doctor:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['hisdoctor:doctor:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="doctorList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="医生ID" align="center" prop="doctorId" />
      <el-table-column label="系统用户ID" align="center" prop="userId" />
      <el-table-column label="所属科室" align="center" prop="deptId" />
      <el-table-column label="医生工号" align="center" prop="doctorNo" />
      <el-table-column label="医生姓名" align="center" prop="doctorName" />
      <el-table-column label="性别" align="center" prop="gender" />
      <el-table-column label="手机号" align="center" prop="phone" />
      <el-table-column label="挂号级别" align="center" prop="levelName" />
      <el-table-column label="擅长领域" align="center" prop="specialty" />
      <el-table-column label="医生简介" align="center" prop="introduction" />
      <el-table-column label="头像" align="center" prop="avatar" />
      <el-table-column label="状态" align="center" prop="status" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hisdoctor:doctor:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hisdoctor:doctor:remove']">删除</el-button>
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

    <!-- 添加或修改医生信息对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="doctorRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="系统用户ID" prop="userId">
              <el-input v-model="form.userId" placeholder="请输入系统用户ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="所属科室" prop="deptId">
              <el-input v-model="form.deptId" placeholder="请输入所属科室" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="医生工号" prop="doctorNo">
              <el-input v-model="form.doctorNo" placeholder="请输入医生工号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="医生姓名" prop="doctorName">
              <el-input v-model="form.doctorName" placeholder="请输入医生姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="性别" prop="gender">
              <el-input v-model="form.gender" placeholder="请输入性别" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="挂号级别" prop="levelId">
              <el-select v-model="form.levelId" placeholder="请选择挂号级别" style="width: 100%">
                <el-option
                  v-for="item in levelList"
                  :key="item.levelId"
                  :label="item.levelName"
                  :value="item.levelId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="擅长领域" prop="specialty">
              <el-input v-model="form.specialty" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="医生简介" prop="introduction">
              <el-input v-model="form.introduction" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="头像" prop="avatar">
              <el-input v-model="form.avatar" placeholder="请输入头像" />
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

<script setup name="Doctor">
import { listDoctor, getDoctor, delDoctor, addDoctor, updateDoctor } from "@/api/hisdoctor/doctor"
import { listDoctorlevel } from "@/api/hisdoctor/doctorlevel"

const { proxy } = getCurrentInstance()

// 挂号级别列表
const levelList = ref([])

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
    userId: undefined,
    deptId: undefined,
    doctorNo: undefined,
    doctorName: undefined,
    gender: undefined,
    phone: undefined,
    levelId: undefined,
    specialty: undefined,
    introduction: undefined,
    avatar: undefined,
    status: undefined,
  },
  rules: {
    userId: [
      { required: true, message: "系统用户ID不能为空", trigger: "blur" }
    ],
    deptId: [
      { required: true, message: "所属科室不能为空", trigger: "blur" }
    ],
    doctorNo: [
      { required: true, message: "医生工号不能为空", trigger: "blur" }
    ],
    doctorName: [
      { required: true, message: "医生姓名不能为空", trigger: "blur" }
    ],
    levelId: [
      { required: true, message: "挂号级别不能为空", trigger: "change" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询医生信息列表 */
function getList() {
  loading.value = true
  listDoctor(queryParams.value).then(response => {
    doctorList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 获取挂号级别列表 */
function getLevelList() {
  listDoctorlevel().then(response => {
    levelList.value = response.rows
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
    doctorId: null,
    userId: null,
    deptId: null,
    doctorNo: null,
    doctorName: null,
    gender: null,
    phone: null,
    levelId: null,
    specialty: null,
    introduction: null,
    avatar: null,
    status: null,
    createTime: null,
    updateTime: null
  }
  proxy.resetForm("doctorRef")
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
  ids.value = selection.map(item => item.doctorId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加医生信息"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _doctorId = row.doctorId || ids.value
  getDoctor(_doctorId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改医生信息"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["doctorRef"].validate(valid => {
    if (valid) {
      if (form.value.doctorId != null) {
        updateDoctor(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addDoctor(form.value).then(() => {
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
  const _doctorIds = row.doctorId || ids.value
  proxy.$modal.confirm('是否确认删除医生信息编号为"' + _doctorIds + '"的数据项？').then(function() {
    return delDoctor(_doctorIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('hisdoctor/doctor/export', {
    ...queryParams.value
  }, `doctor_${new Date().getTime()}.xlsx`)
}

getList()
getLevelList()
</script>
