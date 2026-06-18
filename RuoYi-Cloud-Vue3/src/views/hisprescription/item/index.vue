<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="55px">
      <el-row :gutter="10">
        <el-col :span="6">
          <el-form-item label="处方" prop="prescriptionId">
            <el-input v-model="queryParams.prescriptionId" placeholder="处方ID" clearable @keyup.enter="handleQuery" />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="药品" prop="drugName">
            <el-input v-model="queryParams.drugName" placeholder="药品名称" clearable @keyup.enter="handleQuery" />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="单价" prop="drugPrice">
            <el-input v-model="queryParams.drugPrice" placeholder="单价" clearable @keyup.enter="handleQuery" />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="用法" prop="drugUsage">
            <el-input v-model="queryParams.drugUsage" placeholder="用法" clearable @keyup.enter="handleQuery" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="10">
        <el-col :span="6">
          <el-form-item label="频次" prop="frequency">
            <el-input v-model="queryParams.frequency" placeholder="频次" clearable @keyup.enter="handleQuery" />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="嘱托" prop="itemTip">
            <el-input v-model="queryParams.itemTip" placeholder="嘱托" clearable @keyup.enter="handleQuery" />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['hisprescription:item:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['hisprescription:item:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['hisprescription:item:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['hisprescription:item:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="itemList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="明细ID" align="center" prop="itemId" width="80" />
      <el-table-column label="处方" align="center" prop="prescriptionId" width="70" />
      <el-table-column label="药品" align="center" prop="drugName" min-width="120" show-overflow-tooltip />
      <el-table-column label="单价" align="center" prop="drugPrice" width="70" />
      <el-table-column label="数量" align="center" prop="quantity" width="60" />
      <el-table-column label="小计" align="center" width="80">
        <template #default="{ row }">
          {{ (row.drugPrice || 0) * (row.quantity || 0) }}
        </template>
      </el-table-column>
      <el-table-column label="用法" align="center" prop="drugUsage" width="80" />
      <el-table-column label="用量" align="center" prop="dosage" width="80" />
      <el-table-column label="频次" align="center" prop="frequency" width="80" />
      <el-table-column label="天数" align="center" prop="useDays" width="60" />
      <el-table-column label="嘱托" align="center" prop="itemTip" min-width="100" show-overflow-tooltip />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hisprescription:item:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hisprescription:item:remove']">删除</el-button>
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

    <!-- 添加或修改处方明细对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="itemRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="处方ID" prop="prescriptionId">
              <el-input v-model="form.prescriptionId" placeholder="请输入处方ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="药品ID" prop="drugId">
              <el-input v-model="form.drugId" placeholder="请输入药品ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="药品名称" prop="drugName">
              <el-input v-model="form.drugName" placeholder="请输入药品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="药品单价" prop="drugPrice">
              <el-input v-model="form.drugPrice" placeholder="请输入药品单价" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="药品数量" prop="quantity">
              <el-input v-model="form.quantity" placeholder="请输入药品数量" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="用法" prop="drugUsage">
              <el-input v-model="form.drugUsage" placeholder="请输入用法" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="单次用量" prop="dosage">
              <el-input v-model="form.dosage" placeholder="请输入单次用量" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="服用频次" prop="frequency">
              <el-input v-model="form.frequency" placeholder="请输入服用频次" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="使用天数" prop="useDays">
              <el-input v-model="form.useDays" placeholder="请输入使用天数" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="单种药品嘱托" prop="itemTip">
              <el-input v-model="form.itemTip" placeholder="请输入单种药品嘱托" />
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

<script setup name="Item">
import { listItem, getItem, delItem, addItem, updateItem } from "@/api/hisprescription/item"
import { useRoute } from 'vue-router'

const { proxy } = getCurrentInstance()
const route = useRoute()

const itemList = ref([])
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
    prescriptionId: undefined,
    drugId: undefined,
    drugName: undefined,
    drugPrice: undefined,
    quantity: undefined,
    drugUsage: undefined,
    dosage: undefined,
    frequency: undefined,
    useDays: undefined,
    itemTip: undefined,
  },
  rules: {
    prescriptionId: [
      { required: true, message: "处方ID不能为空", trigger: "blur" }
    ],
    drugId: [
      { required: true, message: "药品ID不能为空", trigger: "blur" }
    ],
    drugName: [
      { required: true, message: "药品名称不能为空", trigger: "blur" }
    ],
    quantity: [
      { required: true, message: "药品数量不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询处方明细列表 */
function getList() {
  loading.value = true
  listItem(queryParams.value).then(response => {
    itemList.value = response.rows
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
    itemId: null,
    prescriptionId: null,
    drugId: null,
    drugName: null,
    drugPrice: null,
    quantity: null,
    drugUsage: null,
    dosage: null,
    frequency: null,
    useDays: null,
    itemTip: null,
    createTime: null
  }
  proxy.resetForm("itemRef")
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
  ids.value = selection.map(item => item.itemId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加处方明细"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _itemId = row.itemId || ids.value
  getItem(_itemId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改处方明细"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["itemRef"].validate(valid => {
    if (valid) {
      if (form.value.itemId != null) {
        updateItem(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addItem(form.value).then(() => {
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
  const _itemIds = row.itemId || ids.value
  proxy.$modal.confirm('是否确认删除处方明细编号为"' + _itemIds + '"的数据项？').then(function() {
    return delItem(_itemIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('hisprescription/item/export', {
    ...queryParams.value
  }, `item_${new Date().getTime()}.xlsx`)
}

if (route.query.prescriptionId) {
  queryParams.value.prescriptionId = Number(route.query.prescriptionId)
}
getList()
</script>
