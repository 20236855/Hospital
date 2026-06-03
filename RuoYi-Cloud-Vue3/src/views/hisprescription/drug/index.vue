<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="药品编码" prop="drugCode">
        <el-input
          v-model="queryParams.drugCode"
          placeholder="请输入药品编码"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="药品名称" prop="drugName">
        <el-input
          v-model="queryParams.drugName"
          placeholder="请输入药品名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="拼音码(检索用)" prop="pyCode">
        <el-input
          v-model="queryParams.pyCode"
          placeholder="请输入拼音码(检索用)"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="药品规格" prop="spec">
        <el-input
          v-model="queryParams.spec"
          placeholder="请输入药品规格"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="包装单位" prop="unit">
        <el-input
          v-model="queryParams.unit"
          placeholder="请输入包装单位"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="单价" prop="drugPrice">
        <el-input
          v-model="queryParams.drugPrice"
          placeholder="请输入单价"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="生产厂家" prop="manufacturer">
        <el-input
          v-model="queryParams.manufacturer"
          placeholder="请输入生产厂家"
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
          v-hasPermi="['hisprescription:drug:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['hisprescription:drug:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['hisprescription:drug:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['hisprescription:drug:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="drugList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="药品ID(主键)" align="center" prop="id" />
      <el-table-column label="药品编码" align="center" prop="drugCode" />
      <el-table-column label="药品名称" align="center" prop="drugName" />
      <el-table-column label="拼音码(检索用)" align="center" prop="pyCode" />
      <el-table-column label="药品规格" align="center" prop="spec" />
      <el-table-column label="包装单位" align="center" prop="unit" />
      <el-table-column label="单价" align="center" prop="drugPrice" />
      <el-table-column label="生产厂家" align="center" prop="manufacturer" />
      <el-table-column label="药剂类型" align="center" prop="drugType" />
      <el-table-column label="排序号" align="center" prop="sort" />
      <el-table-column label="状态 0正常 1停用" align="center" prop="status" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hisprescription:drug:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hisprescription:drug:remove']">删除</el-button>
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

    <!-- 添加或修改药品信息对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="drugRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="药品编码" prop="drugCode">
              <el-input v-model="form.drugCode" placeholder="请输入药品编码" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="药品名称" prop="drugName">
              <el-input v-model="form.drugName" placeholder="请输入药品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="拼音码(检索用)" prop="pyCode">
              <el-input v-model="form.pyCode" placeholder="请输入拼音码(检索用)" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="药品规格" prop="spec">
              <el-input v-model="form.spec" placeholder="请输入药品规格" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="包装单位" prop="unit">
              <el-input v-model="form.unit" placeholder="请输入包装单位" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="单价" prop="drugPrice">
              <el-input v-model="form.drugPrice" placeholder="请输入单价" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="生产厂家" prop="manufacturer">
              <el-input v-model="form.manufacturer" placeholder="请输入生产厂家" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="排序号" prop="sort">
              <el-input v-model="form.sort" placeholder="请输入排序号" />
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

<script setup name="Drug">
import { listDrug, getDrug, delDrug, addDrug, updateDrug } from "@/api/hisprescription/drug"

const { proxy } = getCurrentInstance()

const drugList = ref([])
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
    drugCode: undefined,
    drugName: undefined,
    pyCode: undefined,
    spec: undefined,
    unit: undefined,
    drugPrice: undefined,
    manufacturer: undefined,
    drugType: undefined,
    sort: undefined,
    status: undefined,
  },
  rules: {
    drugCode: [
      { required: true, message: "药品编码不能为空", trigger: "blur" }
    ],
    drugName: [
      { required: true, message: "药品名称不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询药品信息列表 */
function getList() {
  loading.value = true
  listDrug(queryParams.value).then(response => {
    drugList.value = response.rows
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
    drugCode: null,
    drugName: null,
    pyCode: null,
    spec: null,
    unit: null,
    drugPrice: null,
    manufacturer: null,
    drugType: null,
    sort: null,
    status: null,
    remark: null,
    createTime: null,
    updateTime: null
  }
  proxy.resetForm("drugRef")
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
  title.value = "添加药品信息"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value
  getDrug(_id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改药品信息"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["drugRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateDrug(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addDrug(form.value).then(() => {
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
  proxy.$modal.confirm('是否确认删除药品信息编号为"' + _ids + '"的数据项？').then(function() {
    return delDrug(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('hisprescription/drug/export', {
    ...queryParams.value
  }, `drug_${new Date().getTime()}.xlsx`)
}

getList()
</script>
