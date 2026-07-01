<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="申请单ID" prop="applyId">
        <el-input
          v-model="queryParams.applyId"
          placeholder="请输入申请单ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="项目编码" prop="itemCode">
        <el-input
          v-model="queryParams.itemCode"
          placeholder="请输入项目编码"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="项目名称" prop="itemName">
        <el-input
          v-model="queryParams.itemName"
          placeholder="请输入项目名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="结果值" prop="resultValue">
        <el-input
          v-model="queryParams.resultValue"
          placeholder="请输入结果值"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="异常标识" prop="abnormalFlag">
        <el-select v-model="queryParams.abnormalFlag" placeholder="请选择异常标识" clearable>
          <el-option label="正常" value="0" />
          <el-option label="偏高" value="1" />
          <el-option label="偏低" value="2" />
          <el-option label="阳性" value="3" />
          <el-option label="阴性" value="4" />
        </el-select>
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
          v-hasPermi="['hisexam:result:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['hisexam:result:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['hisexam:result:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['hisexam:result:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="resultList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="结果ID" align="center" prop="resultId" />
      <el-table-column label="申请单ID" align="center" prop="applyId" />
      <el-table-column label="结果类型" align="center" prop="resultType" />
      <el-table-column label="项目编码" align="center" prop="itemCode" />
      <el-table-column label="项目名称" align="center" prop="itemName" />
      <el-table-column label="结果值" align="center" prop="resultValue" />
      <el-table-column label="单位" align="center" prop="resultUnit" />
      <el-table-column label="参考范围" align="center" prop="referenceRange" />
      <el-table-column label="异常标识" align="center" prop="abnormalFlag">
        <template #default="scope">
          <el-tag v-if="scope.row.abnormalFlag === '0'" type="success">正常</el-tag>
          <el-tag v-else-if="scope.row.abnormalFlag === '1'" type="danger">偏高</el-tag>
          <el-tag v-else-if="scope.row.abnormalFlag === '2'" type="warning">偏低</el-tag>
          <el-tag v-else-if="scope.row.abnormalFlag === '3'" type="danger">阳性</el-tag>
          <el-tag v-else-if="scope.row.abnormalFlag === '4'" type="info">阴性</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="影像所见" align="center" prop="imageFind" :show-overflow-tooltip="true" />
      <el-table-column label="诊断意见" align="center" prop="diagnosisOpinion" :show-overflow-tooltip="true" />
      <el-table-column label="结论" align="center" prop="diagnosisResult" />
      <el-table-column label="报告时间" align="center" prop="reportTime" width="110">
        <template #default="scope">
          <span>{{ parseTime(scope.row.reportTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" />
      <el-table-column label="图像" align="center" width="100">
        <template #default="scope">
          <el-button
            v-if="scope.row.imageUrl"
            link
            type="primary"
            @click="previewImage(scope.row)"
          >查看图像</el-button>
          <span v-else style="color:#c0c4cc">-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hisexam:result:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hisexam:result:remove']">删除</el-button>
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

    <!-- 添加或修改检查检验结果对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="resultRef" :model="form" :rules="rules" label-width="80px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="申请单ID" prop="applyId">
              <el-input v-model="form.applyId" placeholder="请输入申请单ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="项目编码" prop="itemCode">
              <el-input v-model="form.itemCode" placeholder="请输入项目编码" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="项目名称" prop="itemName">
              <el-input v-model="form.itemName" placeholder="请输入项目名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="结果值" prop="resultValue">
              <el-input v-model="form.resultValue" placeholder="请输入结果值" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="单位" prop="resultUnit">
              <el-input v-model="form.resultUnit" placeholder="请输入单位" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="参考范围" prop="referenceRange">
              <el-input v-model="form.referenceRange" placeholder="请输入参考范围" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="异常标识" prop="abnormalFlag">
              <el-select v-model="form.abnormalFlag" placeholder="请选择异常标识">
                <el-option label="正常" value="0" />
                <el-option label="偏高" value="1" />
                <el-option label="偏低" value="2" />
                <el-option label="阳性" value="3" />
                <el-option label="阴性" value="4" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="影像所见" prop="imageFind">
              <el-input v-model="form.imageFind" type="textarea" placeholder="请输入影像所见" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="诊断意见" prop="diagnosisOpinion">
              <el-input v-model="form.diagnosisOpinion" type="textarea" placeholder="请输入诊断意见" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="结论" prop="diagnosisResult">
              <el-input v-model="form.diagnosisResult" placeholder="请输入结论分类" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="诊疗建议" prop="suggestion">
              <el-input v-model="form.suggestion" type="textarea" placeholder="请输入诊疗建议" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="排序号" prop="sort">
              <el-input v-model="form.sort" placeholder="请输入排序号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="报告时间" prop="reportTime">
              <el-date-picker clearable
                v-model="form.reportTime"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择报告时间">
              </el-date-picker>
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

    <!-- 图像预览弹窗 -->
    <el-dialog v-model="imagePreviewVisible" title="检查结果图像" width="700px" append-to-body>
      <div style="text-align:center">
        <img v-if="previewImageUrl" :src="previewImageUrl" style="max-width:100%; max-height:70vh; border-radius:6px" alt="检查图像" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="Result">
import { listResult, getResult, delResult, addResult, updateResult } from "@/api/hisexam/result"

const { proxy } = getCurrentInstance()

const resultList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const imagePreviewVisible = ref(false)
const previewImageUrl = ref('')

function previewImage(row) {
  previewImageUrl.value = row.imageUrl
  imagePreviewVisible.value = true
}

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    applyId: undefined,
    resultType: undefined,
    itemType: undefined,
    itemCode: undefined,
    itemName: undefined,
    resultValue: undefined,
    resultUnit: undefined,
    referenceRange: undefined,
    abnormalFlag: undefined,
    imageUrl: undefined,
    imageFind: undefined,
    diagnosisOpinion: undefined,
    diagnosisResult: undefined,
    suggestion: undefined,
    sort: undefined,
    status: undefined,
    doctorId: undefined,
    reportTime: undefined,
  },
  rules: {
    applyId: [
      { required: true, message: "申请单ID不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询检查检验结果列表 */
function getList() {
  loading.value = true
  listResult(queryParams.value).then(response => {
    resultList.value = response.rows
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
    resultId: null,
    applyId: null,
    resultType: null,
    itemType: null,
    itemCode: null,
    itemName: null,
    resultValue: null,
    resultUnit: null,
    referenceRange: null,
    abnormalFlag: null,
    imageUrl: null,
    imageFind: null,
    diagnosisOpinion: null,
    diagnosisResult: null,
    suggestion: null,
    sort: null,
    status: null,
    doctorId: null,
    reportTime: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  }
  proxy.resetForm("resultRef")
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
  ids.value = selection.map(item => item.resultId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加检查检验结果"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _resultId = row.resultId || ids.value
  getResult(_resultId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改检查检验结果"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["resultRef"].validate(valid => {
    if (valid) {
      if (form.value.resultId != null) {
        updateResult(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addResult(form.value).then(() => {
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
  const _resultIds = row.resultId || ids.value
  proxy.$modal.confirm('是否确认删除检查检验结果编号为"' + _resultIds + '"的数据项？').then(function() {
    return delResult(_resultIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('hisexam/result/export', {
    ...queryParams.value
  }, `result_${new Date().getTime()}.xlsx`)
}

getList()
</script>
