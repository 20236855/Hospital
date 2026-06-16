<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="医生姓名" prop="doctorName">
        <el-input
          v-model="queryParams.doctorName"
          placeholder="请输入医生姓名"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="所属科室" prop="deptId">
        <el-tree-select
          v-model="queryParams.deptId"
          :data="deptOptions"
          :props="{ value: 'deptId', label: 'deptName', children: 'children' }"
          value-key="deptId"
          placeholder="请选择所属科室"
          clearable
          check-strictly
          style="width: 220px"
        />
      </el-form-item>
      <el-form-item label="挂号级别" prop="levelId">
        <el-select
          v-model="queryParams.levelId"
          placeholder="请选择挂号级别"
          clearable
          style="width: 180px"
        >
          <el-option
            v-for="item in levelList"
            :key="item.levelId"
            :label="item.levelName"
            :value="item.levelId"
          />
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
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <div class="doctor-grid" v-loading="loading">
      <div v-for="doctor in doctorList" :key="doctor.doctorId" class="doctor-card"
           @click="handleView(doctor)">
        <div class="doctor-avatar-wrap">
          <el-image
            :src="getAvatar(doctor)"
            :preview-src-list="[getAvatar(doctor)]"
            fit="cover"
            class="doctor-avatar-lg"
          >
            <template #error>
              <div class="image-slot">
                <el-icon :size="60"><Icon /></el-icon>
              </div>
            </template>
          </el-image>
          <div class="doctor-badge" v-if="doctor.status === '0'">在职</div>
          <div class="doctor-badge doctor-badge-off" v-else>停诊</div>
        </div>
        <div class="doctor-info">
          <div class="doctor-header">
            <span class="doctor-name">{{ doctor.doctorName }}</span>
            <span class="doctor-level">{{ doctor.levelName }}</span>
          </div>
          <div class="doctor-dept">
            <el-icon><OfficeBuilding /></el-icon>
            <span>{{ getDeptName(doctor.deptId) }}</span>
          </div>
          <div class="doctor-meta">
            <span class="meta-item" v-if="doctor.gender">
              <el-icon><User /></el-icon>{{ doctor.gender }}
            </span>
            <span class="meta-item" v-if="doctor.phone">
              <el-icon><Phone /></el-icon>{{ doctor.phone }}
            </span>
            <span class="meta-item" v-if="doctor.doctorNo">
              <el-icon><Document /></el-icon>{{ doctor.doctorNo }}
            </span>
          </div>
          <div class="doctor-specialty" v-if="doctor.specialty">
            <el-icon><MagicStick /></el-icon>
            <span class="label">擅长：</span>
            <span class="content">{{ doctor.specialty }}</span>
          </div>
          <div class="doctor-intro" v-if="doctor.introduction">
            <el-icon><Reading /></el-icon>
            <span class="label">简介：</span>
            <span class="content">{{ doctor.introduction }}</span>
          </div>
        </div>
        <div class="doctor-actions">
          <el-button size="small" type="primary" plain @click.stop="handleUpdate(doctor)"
                     v-hasPermi="['hisdoctor:doctor:edit']">
            <el-icon><Edit /></el-icon>编辑
          </el-button>
          <el-button size="small" type="danger" plain @click.stop="handleDelete(doctor)"
                     v-hasPermi="['hisdoctor:doctor:remove']">
            <el-icon><Delete /></el-icon>删除
          </el-button>
        </div>
      </div>
      <el-empty v-if="!loading && doctorList.length === 0" description="暂无医生数据" />
    </div>

    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改医生信息对话框 -->
    <el-dialog :title="title" v-model="open" width="680px" append-to-body>
      <el-form ref="doctorRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24" class="avatar-col">
            <el-form-item label="医生头像" prop="avatar">
              <div class="avatar-upload-container">
                <div class="avatar-preview">
                  <img :src="form.avatar || defaultAvatar" @error="onAvatarError" class="avatar-img" />
                </div>
                <div class="avatar-actions">
                  <el-upload
                    class="avatar-uploader"
                    action="#"
                    :show-file-list="false"
                    :before-upload="beforeAvatarUpload"
                    :http-request="handleAvatarUpload"
                    accept="image/*"
                  >
                    <el-button type="primary" size="small" icon="Upload">上传头像</el-button>
                  </el-upload>
                  <el-button size="small" plain @click="clearAvatar">清除</el-button>
                  <div class="upload-tip">
                    <el-icon><InfoFilled /></el-icon>
                    建议上传 200x200 像素以上的图片，支持 JPG/PNG 格式
                  </div>
                </div>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="医生姓名" prop="userId">
              <el-select
                v-model="form.userId"
                placeholder="请选择医生账号"
                filterable
                clearable
                :disabled="form.doctorId != null"
                style="width: 100%"
                @change="handleDoctorUserChange"
              >
                <el-option
                  v-for="item in doctorUserOptions"
                  :key="item.userId"
                  :label="item.nickName"
                  :value="item.userId"
                >
                  <span>{{ item.nickName }}</span>
                  <span class="doctor-user-option">{{ item.userName }}</span>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属科室" prop="deptId">
              <el-tree-select
                v-model="form.deptId"
                :data="deptOptions"
                :props="{ value: 'deptId', label: 'deptName', children: 'children' }"
                value-key="deptId"
                placeholder="请选择所属科室"
                clearable
                check-strictly
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="医生工号" prop="doctorNo">
              <el-input v-model="form.doctorNo" :placeholder="doctorNoPlaceholder" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio value="男">男</el-radio>
                <el-radio value="女">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
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
              <el-input v-model="form.specialty" type="textarea" :rows="2" placeholder="请输入医生擅长的专业领域" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="医生简介" prop="introduction">
              <el-input v-model="form.introduction" type="textarea" :rows="3" placeholder="请输入医生的详细介绍" />
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

    <!-- 医生详情对话框 -->
    <el-dialog :title="doctorDetail?.doctorName || '医生详情'" v-model="detailOpen" width="780px" append-to-body>
      <div class="doctor-detail" v-if="doctorDetail">
        <div class="detail-hero">
          <div class="detail-avatar-wrap">
            <el-image
              :src="getAvatar(doctorDetail)"
              :preview-src-list="[getAvatar(doctorDetail)]"
              fit="cover"
              class="detail-avatar"
            >
              <template #error>
                <div class="image-slot-lg">
                  <el-icon :size="60"><Icon /></el-icon>
                </div>
              </template>
            </el-image>
            <el-tag class="detail-status" :type="doctorDetail.status === '0' ? 'success' : 'info'" effect="dark">
              {{ doctorDetail.status === '0' ? '在职' : '停诊' }}
            </el-tag>
          </div>
          <div class="detail-main">
            <div class="detail-title-row">
              <h2 class="detail-name">{{ doctorDetail.doctorName }}</h2>
              <el-tag :type="doctorDetail.status === '0' ? 'success' : 'info'" size="large" effect="dark">
                {{ doctorDetail.status === '0' ? '在职' : '停诊' }}
              </el-tag>
            </div>
            <div class="detail-level-row">
              <el-tag type="warning" size="default" effect="plain">{{ doctorDetail.levelName }}</el-tag>
              <span class="detail-dept">
                <el-icon><OfficeBuilding /></el-icon>
                {{ getDeptName(doctorDetail.deptId) }}
              </span>
            </div>
            <div class="detail-meta-row">
              <span class="meta-pill" v-if="doctorDetail.gender">
                <el-icon><User /></el-icon>{{ doctorDetail.gender }}
              </span>
              <span class="meta-pill" v-if="doctorDetail.phone">
                <el-icon><Phone /></el-icon>{{ doctorDetail.phone }}
              </span>
              <span class="meta-pill" v-if="doctorDetail.doctorNo">
                <el-icon><Document /></el-icon>{{ doctorDetail.doctorNo }}
              </span>
            </div>
          </div>
        </div>

        <div class="detail-section" v-if="doctorDetail.specialty">
          <div class="section-header">
            <el-icon><MagicStick /></el-icon>
            <span>擅长领域</span>
          </div>
          <div class="section-body">
            <p>{{ doctorDetail.specialty }}</p>
          </div>
        </div>

        <div class="detail-section" v-if="doctorDetail.introduction">
          <div class="section-header">
            <el-icon><Reading /></el-icon>
            <span>医生简介</span>
          </div>
          <div class="section-body">
            <p>{{ doctorDetail.introduction }}</p>
          </div>
        </div>

        <div class="detail-section detail-info-grid">
          <div class="info-item">
            <span class="info-label">医生ID</span>
            <span class="info-value">{{ doctorDetail.doctorId }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">系统用户ID</span>
            <span class="info-value">{{ doctorDetail.userId }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">创建时间</span>
            <span class="info-value">{{ doctorDetail.createTime || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">更新时间</span>
            <span class="info-value">{{ doctorDetail.updateTime || '-' }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="detailOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Doctor">
import { listDoctor, getDoctor, delDoctor, addDoctor, updateDoctor, listAvailableDoctorUsers } from "@/api/hisdoctor/doctor"
import { listDept } from "@/api/system/dept"
import { listDoctorlevel } from "@/api/hisdoctor/doctorlevel"
import { upload } from "@/api/hisdoctor/doctor"
import defAva from '@/assets/images/profile.jpg'

const { proxy } = getCurrentInstance()

const defaultAvatar = defAva
const levelList = ref([])
const doctorList = ref([])
const open = ref(false)
const detailOpen = ref(false)
const doctorDetail = ref(null)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const deptOptions = ref([])
const deptList = ref([])
const doctorUserOptions = ref([])

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
      { required: true, message: "医生账号不能为空", trigger: "change" }
    ],
    deptId: [
      { required: true, message: "所属科室不能为空", trigger: "change" }
    ],
    levelId: [
      { required: true, message: "挂号级别不能为空", trigger: "change" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

const doctorNoPlaceholder = computed(() => {
  return form.value.deptId ? `保存后自动生成：D${form.value.deptId}******` : "选择科室后保存自动生成"
})

function getAvatar(doctor) {
  return doctor?.avatar || defaultAvatar
}

function onAvatarError(e) {
  e.target.src = defaultAvatar
}

/** 查询部门下拉树 */
function getDeptOptions() {
  listDept().then(response => {
    const rows = response.data || []
    deptList.value = rows
    deptOptions.value = proxy.handleTree(JSON.parse(JSON.stringify(rows)), "deptId")
  })
}

function getDeptName(deptId) {
  const dept = deptList.value.find(item => String(item.deptId) === String(deptId))
  return dept ? dept.deptName : deptId
}

function getDoctorUserOptions() {
  return listAvailableDoctorUsers().then(response => {
    doctorUserOptions.value = response.data || []
  })
}

function handleDoctorUserChange(userId) {
  const user = doctorUserOptions.value.find(item => String(item.userId) === String(userId))
  form.value.doctorName = user ? user.nickName : null
  if (user && !form.value.phone) {
    form.value.phone = user.phonenumber || null
  }
}

function ensureDoctorUserOption(row) {
  if (!row?.userId) {
    return
  }
  const exists = doctorUserOptions.value.some(item => String(item.userId) === String(row.userId))
  if (!exists) {
    doctorUserOptions.value.push({
      userId: row.userId,
      nickName: row.doctorName || row.userId,
      userName: row.userId,
      phonenumber: row.phone
    })
  }
}

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

/** 头像上传前验证 */
function beforeAvatarUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    proxy.$modal.msgError('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    proxy.$modal.msgError('图片大小不能超过 5MB!')
    return false
  }
  return true
}

/** 头像上传 */
function handleAvatarUpload(uploadFile) {
  const formData = new FormData()
  formData.append('file', uploadFile.file)
  upload(formData).then(response => {
    const imgUrl = response.url || response.data?.url || response.imgUrl
    if (imgUrl) {
      form.value.avatar = imgUrl
      proxy.$modal.msgSuccess('头像上传成功')
    }
  }).catch(() => {
    proxy.$modal.msgError('头像上传失败，请重试')
  })
}

/** 清除头像 */
function clearAvatar() {
  form.value.avatar = null
}

/** 查看医生详情 */
function handleView(row) {
  getDoctor(row.doctorId).then(response => {
    doctorDetail.value = response.data
    detailOpen.value = true
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
  getDoctorUserOptions()
  open.value = true
  title.value = "添加医生信息"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _doctorId = row.doctorId || ids.value
  getDoctor(_doctorId).then(response => {
    form.value = response.data
    ensureDoctorUserOption(form.value)
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
        form.value.doctorNo = null
        addDoctor(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getDoctorUserOptions()
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
    getDoctorUserOptions()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

getDeptOptions()
getDoctorUserOptions()
getList()
getLevelList()
</script>

<style scoped lang="scss">
.doctor-user-option {
  float: right;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

/* 医生卡片网格布局 */
.doctor-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  padding: 10px 0;
}

.doctor-card {
  display: flex;
  flex-direction: column;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;

  &:hover {
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
    transform: translateY(-3px);
    border-color: #c0c4cc;
  }
}

/* 头像区域 */
.doctor-avatar-wrap {
  position: relative;
  width: 100%;
  height: 280px;
  background: linear-gradient(135deg, rgb(185, 225, 205) 0%, rgb(223, 246, 239) 100%);
  overflow: hidden;
}

.doctor-avatar-lg {
  width: 100%;
  height: 100%;
  background: transparent;
  object-fit: cover;
  object-position: top center;
}

.image-slot {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: transparent;
  color: rgba(0, 0, 0, 0.4);
}

.doctor-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 12px;
  background: rgba(103, 194, 58, 0.9);
  color: #fff;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.doctor-badge-off {
  background: rgba(144, 147, 153, 0.9);
}

/* 信息区域 */
.doctor-info {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.doctor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.doctor-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.doctor-level {
  font-size: 13px;
  color: #e6a23c;
  background: #fdf6ec;
  padding: 4px 10px;
  border-radius: 4px;
  font-weight: 500;
}

.doctor-dept {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  font-size: 14px;

  .el-icon {
    color: rgb(26, 77, 69);
  }
}

.doctor-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  color: #909399;
  font-size: 13px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;

  .el-icon {
    font-size: 14px;
  }
}

.doctor-specialty, .doctor-intro {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  color: #606266;
  font-size: 13px;
  line-height: 1.6;

  .el-icon {
    color: rgb(185, 225, 205);
    flex-shrink: 0;
    margin-top: 3px;
  }

  .label {
    color: #909399;
    flex-shrink: 0;
  }

  .content {
    flex: 1;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
  }
}

/* 操作按钮 */
.doctor-actions {
  padding: 12px 16px;
  border-top: 1px solid #ebeef5;
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  background: #fafafa;
}

/* 头像上传区域 */
.avatar-col {
  margin-bottom: 10px;
}

.avatar-upload-container {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.avatar-preview {
  flex-shrink: 0;
}

.avatar-img {
  width: 120px;
  height: 120px;
  border-radius: 12px;
  object-fit: cover;
  border: 2px solid #ebeef5;
  background: #f5f7fa;
}

.avatar-actions {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.avatar-uploader {
  display: inline-block;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;

  .el-icon {
    color: #409eff;
  }
}

/* 医生详情样式 */
.doctor-detail {
  padding: 10px 0;
}

.detail-hero {
  display: flex;
  gap: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 20px;
}

.detail-avatar-wrap {
  flex-shrink: 0;
  position: relative;
}

.detail-avatar {
  width: 180px;
  height: 220px;
  border-radius: 16px;
  border: 4px solid #fff;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  background: linear-gradient(135deg, rgb(185, 225, 205) 0%, rgb(223, 246, 239) 100%);
  overflow: hidden;
  object-fit: cover;
  object-position: top center;
}

.image-slot-lg {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 180px;
  height: 220px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgb(185, 225, 205) 0%, rgb(223, 246, 239) 100%);
  color: #c0c4cc;
}

.detail-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-top: 8px;
}

.detail-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.detail-name {
  font-size: 26px;
  font-weight: 700;
  color: #303133;
  margin: 0;
}

.detail-level-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.detail-dept {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  font-size: 15px;

  .el-icon {
    color: rgb(26, 77, 69);
  }
}

.detail-meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 4px;
}

.meta-pill {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: #f5f7fa;
  border-radius: 16px;
  font-size: 13px;
  color: #606266;

  .el-icon {
    color: rgb(26, 77, 69);
  }
}

.detail-section {
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  padding-bottom: 8px;
  border-bottom: 2px solid rgb(185, 225, 205);

  .el-icon {
    color: rgb(26, 77, 69);
    font-size: 18px;
  }
}

.section-body {
  padding: 0 4px;

  p {
    margin: 0;
    color: #606266;
    font-size: 14px;
    line-height: 1.8;
  }
}

.detail-info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-top: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.info-label {
  color: #909399;
  font-size: 13px;
  margin-right: 12px;
  flex-shrink: 0;
  min-width: 80px;
}

.info-value {
  color: #303133;
  font-size: 14px;
  font-weight: 500;
}

/* 响应式 */
@media (max-width: 992px) {
  .doctor-grid {
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 16px;
  }

  .doctor-avatar-wrap {
    height: 160px;
  }

  .doctor-avatar-lg {
    width: 110px;
    height: 110px;
  }

  .detail-hero {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .detail-main {
    align-items: center;
  }

  .detail-level-row {
    justify-content: center;
  }

  .detail-meta-row {
    justify-content: center;
  }

  .detail-info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
