<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="级别名称：普通号/专家号/主任号" prop="levelName">
        <el-input
          v-model="queryParams.levelName"
          placeholder="请输入级别名称：普通号/专家号/主任号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="对应挂号费用" prop="fee">
        <el-input
          v-model="queryParams.fee"
          placeholder="请输入对应挂号费用"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="排序号" prop="sort">
        <el-input
          v-model="queryParams.sort"
          placeholder="请输入排序号"
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
          v-hasPermi="['hisdoctor:doctorlevel:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['hisdoctor:doctorlevel:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['hisdoctor:doctorlevel:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['hisdoctor:doctorlevel:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="doctorlevelList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="级别ID(主键)" align="center" prop="levelId" />
      <el-table-column label="级别名称：普通号/专家号/主任号" align="center" prop="levelName" />
      <el-table-column label="对应挂号费用" align="center" prop="fee" />
      <el-table-column label="排序号" align="center" prop="sort" />
      <el-table-column label="状态 0正常 1停用" align="center" prop="status" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hisdoctor:doctorlevel:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hisdoctor:doctorlevel:remove']">删除</el-button>
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

    <!-- 添加或修改挂号级别对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="doctorlevelRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="级别名称：普通号/专家号/主任号" prop="levelName">
              <el-input v-model="form.levelName" placeholder="请输入级别名称：普通号/专家号/主任号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="对应挂号费用" prop="fee">
              <el-input v-model="form.fee" placeholder="请输入对应挂号费用" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="排序号" prop="sort">
              <el-input v-model="form.sort" placeholder="请输入排序号" />
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

<script setup name="Doctorlevel">
import { listDoctorlevel, getDoctorlevel, delDoctorlevel, addDoctorlevel, updateDoctorlevel } from "@/api/hisdoctor/doctorlevel"

const { proxy } = getCurrentInstance()

const doctorlevelList = ref([])
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
    levelName: undefined,
    fee: undefined,
    sort: undefined,
    status: undefined,
  },
  rules: {
    levelName: [
      { required: true, message: "级别名称：普通号/专家号/主任号不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询挂号级别列表 */
function getList() {
  loading.value = true
  listDoctorlevel(queryParams.value).then(response => {
    doctorlevelList.value = response.rows
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
    levelId: null,
    levelName: null,
    fee: null,
    sort: null,
    status: null,
    createTime: null,
    updateTime: null
  }
  proxy.resetForm("doctorlevelRef")
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
  ids.value = selection.map(item => item.levelId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加挂号级别"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _levelId = row.levelId || ids.value
  getDoctorlevel(_levelId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改挂号级别"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["doctorlevelRef"].validate(valid => {
    if (valid) {
      if (form.value.levelId != null) {
        updateDoctorlevel(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addDoctorlevel(form.value).then(() => {
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
  const _levelIds = row.levelId || ids.value
  proxy.$modal.confirm('是否确认删除挂号级别编号为"' + _levelIds + '"的数据项？').then(function() {
    return delDoctorlevel(_levelIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('hisdoctor/doctorlevel/export', {
    ...queryParams.value
  }, `doctorlevel_${new Date().getTime()}.xlsx`)
}

getList()
</script>
