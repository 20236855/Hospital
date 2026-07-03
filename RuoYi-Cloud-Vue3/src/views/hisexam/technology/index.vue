<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="90px">
      <el-form-item label="项目编码" prop="techCode">
        <el-input
          v-model="queryParams.techCode"
          placeholder="请输入项目编码"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="项目名称" prop="techName">
        <el-input
          v-model="queryParams.techName"
          placeholder="请输入项目名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="规格" prop="techFormat">
        <el-input
          v-model="queryParams.techFormat"
          placeholder="请输入规格"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="单价" prop="techPrice">
        <el-input
          v-model="queryParams.techPrice"
          placeholder="请输入单价"
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
          v-hasPermi="['hisexam:technology:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['hisexam:technology:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['hisexam:technology:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['hisexam:technology:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="technologyList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键ID" align="center" prop="id" />
      <el-table-column label="项目编码" align="center" prop="techCode" />
      <el-table-column label="项目名称" align="center" prop="techName" />
      <el-table-column label="规格" align="center" prop="techFormat" />
      <el-table-column label="单价" align="center" prop="techPrice" />
      <el-table-column label="项目类型" align="center" prop="techType" />
      <el-table-column label="排序号" align="center" prop="sort" />
      <el-table-column label="状态" align="center" prop="status" />
      <el-table-column label="项目备注说明" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hisexam:technology:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hisexam:technology:remove']">删除</el-button>
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

    <!-- 添加或修改医技项目对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="technologyRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="项目编码" prop="techCode">
              <el-input v-model="form.techCode" placeholder="请输入项目编码" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="项目名称" prop="techName">
              <el-input v-model="form.techName" placeholder="请输入项目名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="规格" prop="techFormat">
              <el-input v-model="form.techFormat" placeholder="请输入规格" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="单价" prop="techPrice">
              <el-input v-model="form.techPrice" placeholder="请输入单价" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="排序号" prop="sort">
              <el-input v-model="form.sort" placeholder="请输入排序号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="项目备注说明" prop="remark">
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

<script setup name="Technology">
import { listTechnology, getTechnology, delTechnology, addTechnology, updateTechnology } from "@/api/hisexam/technology"

const { proxy } = getCurrentInstance()

const technologyList = ref([])
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
    techCode: undefined,
    techName: undefined,
    techFormat: undefined,
    techPrice: undefined,
    techType: undefined,
    priceType: undefined,
    deptmentId: undefined,
    pyCode: undefined,
    sort: undefined,
    status: undefined,
  },
  rules: {
    techCode: [
      { required: true, message: "项目编码不能为空", trigger: "blur" }
    ],
    techName: [
      { required: true, message: "项目名称不能为空", trigger: "blur" }
    ],
    techType: [
      { required: true, message: "项目类型：CHECK检查/INSPEC检验/DISPOSAL处置不能为空", trigger: "change" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询医技项目列表 */
function getList() {
  loading.value = true
  listTechnology(queryParams.value).then(response => {
    technologyList.value = response.rows
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
    id: null,
    techCode: null,
    techName: null,
    techFormat: null,
    techPrice: null,
    techType: null,
    priceType: null,
    deptmentId: null,
    pyCode: null,
    sort: null,
    status: null,
    remark: null,
    createTime: null,
    updateTime: null
  }
  proxy.resetForm("technologyRef")
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
  ids.value = selection.map(item => item.id)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加医技项目"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value
  getTechnology(_id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改医技项目"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["technologyRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateTechnology(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addTechnology(form.value).then(() => {
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
  const _ids = row.id || ids.value
  proxy.$modal.confirm('是否确认删除医技项目编号为"' + _ids + '"的数据项？').then(function() {
    return delTechnology(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('hisexam/technology/export', {
    ...queryParams.value
  }, `technology_${new Date().getTime()}.xlsx`)
}

getList()
</script>
