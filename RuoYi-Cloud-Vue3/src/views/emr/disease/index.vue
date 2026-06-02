<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="疾病编码" prop="diseaseCode">
        <el-input
          v-model="queryParams.diseaseCode"
          placeholder="请输入疾病编码"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="疾病名称" prop="diseaseName">
        <el-input
          v-model="queryParams.diseaseName"
          placeholder="请输入疾病名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="ICD编码" prop="icdCode">
        <el-input
          v-model="queryParams.icdCode"
          placeholder="请输入ICD编码"
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
          v-hasPermi="['emr:disease:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['emr:disease:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['emr:disease:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['emr:disease:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="diseaseList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="疾病ID" align="center" prop="diseaseId" />
      <el-table-column label="疾病编码" align="center" prop="diseaseCode" />
      <el-table-column label="疾病名称" align="center" prop="diseaseName" />
      <el-table-column label="ICD编码" align="center" prop="icdCode" />
      <el-table-column label="疾病描述" align="center" prop="description" />
      <el-table-column label="状态" align="center" prop="status" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['emr:disease:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['emr:disease:remove']">删除</el-button>
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

    <!-- 添加或修改疾病字典对话框 -->
    <el-dialog :title="title" v-model="open" width="900px" append-to-body>
      <el-form ref="diseaseRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="疾病编码" prop="diseaseCode">
              <el-input v-model="form.diseaseCode" placeholder="请输入疾病编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="疾病名称" prop="diseaseName">
              <el-input v-model="form.diseaseName" placeholder="请输入疾病名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="ICD编码" prop="icdCode">
              <el-input v-model="form.icdCode" placeholder="请输入ICD编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-input v-model="form.status" placeholder="请输入状态" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="疾病描述" prop="description">
              <el-input v-model="form.description" type="textarea" placeholder="请输入内容" />
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

<script setup name="Disease">
import { listDisease, getDisease, delDisease, addDisease, updateDisease } from "@/api/emr/disease"

const { proxy } = getCurrentInstance()

const diseaseList = ref([])
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
    diseaseCode: undefined,
    diseaseName: undefined,
    icdCode: undefined,
    description: undefined,
    status: undefined,
  },
  rules: {
    diseaseName: [
      { required: true, message: "疾病名称不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询疾病字典列表 */
function getList() {
  loading.value = true
  listDisease(queryParams.value).then(response => {
    diseaseList.value = response.rows
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
    diseaseId: null,
    diseaseCode: null,
    diseaseName: null,
    icdCode: null,
    description: null,
    status: null,
    createTime: null,
    updateTime: null
  }
  proxy.resetForm("diseaseRef")
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
  ids.value = selection.map(item => item.diseaseId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加疾病字典"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _diseaseId = row.diseaseId || ids.value
  getDisease(_diseaseId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改疾病字典"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["diseaseRef"].validate(valid => {
    if (valid) {
      if (form.value.diseaseId != null) {
        updateDisease(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addDisease(form.value).then(() => {
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
  const _diseaseIds = row.diseaseId || ids.value
  proxy.$modal.confirm('是否确认删除疾病字典编号为"' + _diseaseIds + '"的数据项？').then(function() {
    return delDisease(_diseaseIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('emr/disease/export', {
    ...queryParams.value
  }, `disease_${new Date().getTime()}.xlsx`)
}

getList()
</script>
