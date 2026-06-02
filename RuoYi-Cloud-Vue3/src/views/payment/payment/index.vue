<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="收费单号(唯一)" prop="payNo">
        <el-input
          v-model="queryParams.payNo"
          placeholder="请输入收费单号(唯一)"
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
      <el-form-item label="关联挂号ID" prop="registerId">
        <el-input
          v-model="queryParams.registerId"
          placeholder="请输入关联挂号ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="缴费总金额" prop="totalAmount">
        <el-input
          v-model="queryParams.totalAmount"
          placeholder="请输入缴费总金额"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="收费员ID" prop="operatorId">
        <el-input
          v-model="queryParams.operatorId"
          placeholder="请输入收费员ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="缴费时间" prop="payTime">
        <el-date-picker clearable
          v-model="queryParams.payTime"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择缴费时间">
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
          v-hasPermi="['payment:payment:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['payment:payment:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['payment:payment:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['payment:payment:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="paymentList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="收费单ID" align="center" prop="paymentId" width="120" />
      <el-table-column label="收费单号" align="center" prop="payNo" width="150" />
      <el-table-column label="患者ID" align="center" prop="patientId" width="100" />
      <el-table-column label="关联挂号ID" align="center" prop="registerId" width="120" />
      <el-table-column label="缴费总金额" align="center" prop="totalAmount" width="120" />
      <el-table-column label="支付方式" align="center" prop="payType" width="150" />
      <el-table-column label="缴费状态" align="center" prop="payStatus" width="150" />
      <el-table-column label="收费员ID" align="center" prop="operatorId" width="100" />
      <el-table-column label="缴费时间" align="center" prop="payTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.payTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" width="200" show-overflow-tooltip />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['payment:payment:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['payment:payment:remove']">删除</el-button>
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

    <!-- 添加或修改收费对话框 -->
    <el-dialog :title="title" v-model="open" width="900px" append-to-body>
      <el-form ref="paymentRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="收费单号" prop="payNo">
              <el-input v-model="form.payNo" placeholder="请输入收费单号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="患者ID" prop="patientId">
              <el-input v-model="form.patientId" placeholder="请输入患者ID" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="关联挂号ID" prop="registerId">
              <el-input v-model="form.registerId" placeholder="请输入关联挂号ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="缴费总金额" prop="totalAmount">
              <el-input v-model="form.totalAmount" placeholder="请输入缴费总金额" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="支付方式" prop="payType">
              <el-input v-model="form.payType" placeholder="请输入支付方式" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="缴费状态" prop="payStatus">
              <el-input v-model="form.payStatus" placeholder="请输入缴费状态" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="收费员ID" prop="operatorId">
              <el-input v-model="form.operatorId" placeholder="请输入收费员ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="缴费时间" prop="payTime">
              <el-date-picker clearable
                v-model="form.payTime"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择缴费时间"
                style="width: 100%">
              </el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
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
          <el-button type="default" @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Payment">
import { listPayment, getPayment, delPayment, addPayment, updatePayment } from "@/api/payment/payment"

const { proxy } = getCurrentInstance()

const paymentList = ref([])
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
    payNo: undefined,
    patientId: undefined,
    registerId: undefined,
    totalAmount: undefined,
    payType: undefined,
    payStatus: undefined,
    operatorId: undefined,
    payTime: undefined,
  },
  rules: {
    payNo: [
      { required: true, message: "收费单号(唯一)不能为空", trigger: "blur" }
    ],
    patientId: [
      { required: true, message: "患者ID不能为空", trigger: "blur" }
    ],
    registerId: [
      { required: true, message: "关联挂号ID不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询收费列表 */
function getList() {
  loading.value = true
  listPayment(queryParams.value).then(response => {
    paymentList.value = response.rows
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
    paymentId: null,
    payNo: null,
    patientId: null,
    registerId: null,
    totalAmount: null,
    payType: null,
    payStatus: null,
    operatorId: null,
    payTime: null,
    remark: null,
    createTime: null,
    updateTime: null
  }
  proxy.resetForm("paymentRef")
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
  ids.value = selection.map(item => item.paymentId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加收费"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _paymentId = row.paymentId || ids.value
  getPayment(_paymentId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改收费"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["paymentRef"].validate(valid => {
    if (valid) {
      if (form.value.paymentId != null) {
        updatePayment(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addPayment(form.value).then(() => {
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
  const _paymentIds = row.paymentId || ids.value
  proxy.$modal.confirm('是否确认删除收费编号为"' + _paymentIds + '"的数据项？').then(function() {
    return delPayment(_paymentIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('payment/payment/export', {
    ...queryParams.value
  }, `payment_${new Date().getTime()}.xlsx`)
}

getList()
</script>
