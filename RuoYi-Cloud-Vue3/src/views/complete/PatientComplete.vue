<template>
  <div class="patient-complete-page">
    <div class="complete-shell">
      <section class="complete-hero">
        <div>
          <span class="hero-kicker">首次登录资料完善</span>
          <h1>完善个人信息</h1>
          <p>补齐就诊所需的基础资料，方便医生快速核对身份和联系信息。</p>
        </div>
        <div class="hero-profile">
          <div class="profile-avatar">{{ profileInitial }}</div>
          <div>
            <div class="profile-name">{{ form.name || '患者' }}</div>
            <div class="profile-phone">{{ form.phone || '手机号待同步' }}</div>
          </div>
        </div>
      </section>

      <el-form ref="patientRef" :model="form" :rules="rules" label-position="top" class="complete-form">
        <section class="form-section">
          <div class="section-title">
            <span>01</span>
            <div>
              <h2>基础身份</h2>
              <p>姓名和手机号已从注册信息自动带入。</p>
            </div>
          </div>
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12">
              <el-form-item label="患者姓名" prop="name">
                <el-input v-model="form.name" placeholder="请输入患者姓名" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="form.phone" placeholder="注册手机号" disabled />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="性别" prop="gender">
                <el-segmented v-model="form.gender" :options="genderOptions" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="身份证号" prop="idCard">
                <el-input v-model="form.idCard" placeholder="请输入身份证号" maxlength="18" />
              </el-form-item>
            </el-col>
          </el-row>
        </section>

        <section class="form-section">
          <div class="section-title">
            <span>02</span>
            <div>
              <h2>出生与体征</h2>
              <p>选择出生日期后，系统会自动计算年龄。</p>
            </div>
          </div>
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12">
              <el-form-item label="出生日期" prop="birthday">
                <el-date-picker
                  v-model="form.birthday"
                  clearable
                  type="date"
                  value-format="YYYY-MM-DD"
                  placeholder="请选择出生日期"
                  :disabled-date="disableFutureBirthday"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="年龄" prop="age">
                <el-input v-model="form.age" placeholder="由出生日期自动计算" disabled>
                  <template #suffix>岁</template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="血型" prop="bloodType">
                <el-select v-model="form.bloodType" placeholder="请选择血型" clearable style="width: 100%">
                  <el-option label="A型" value="A" />
                  <el-option label="B型" value="B" />
                  <el-option label="AB型" value="AB" />
                  <el-option label="O型" value="O" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="婚姻状态" prop="maritalStatus">
                <el-select v-model="form.maritalStatus" placeholder="请选择婚姻状态" clearable style="width: 100%">
                  <el-option label="未婚" value="未婚" />
                  <el-option label="已婚" value="已婚" />
                  <el-option label="离异" value="离异" />
                  <el-option label="丧偶" value="丧偶" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </section>

        <section class="form-section">
          <div class="section-title">
            <span>03</span>
            <div>
              <h2>联系与病史</h2>
              <p>紧急联系人和病史会用于就诊时的安全核对。</p>
            </div>
          </div>
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="家庭住址" prop="address">
                <el-input v-model="form.address" placeholder="请输入家庭住址" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="紧急联系人" prop="emergencyContact">
                <el-input v-model="form.emergencyContact" placeholder="请输入紧急联系人" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="紧急联系电话" prop="emergencyPhone">
                <el-input v-model="form.emergencyPhone" placeholder="请输入紧急联系电话" maxlength="11" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="过敏史" prop="allergyHistory">
                <el-input v-model="form.allergyHistory" type="textarea" :rows="4" placeholder="如：青霉素过敏" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="既往史" prop="pastHistory">
                <el-input v-model="form.pastHistory" type="textarea" :rows="4" placeholder="如：高血压、糖尿病" />
              </el-form-item>
            </el-col>
          </el-row>
        </section>

        <div class="form-actions">
          <el-button @click="logout">退出登录</el-button>
          <el-button @click="goHome">返回首页</el-button>
          <el-button @click="resetForm">重置</el-button>
          <el-button type="primary" @click="submitForm" :loading="loading">
            确认保存
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup name="PatientComplete">
import { completePatient } from "@/api/patient/patient"
import useUserStore from '@/store/modules/user'
import router from '@/router'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

const loading = ref(false)
const patientRef = ref()
const genderOptions = ['男', '女']

const form = ref({
  name: '',
  gender: '',
  birthday: '',
  age: '',
  phone: '',
  idCard: '',
  bloodType: '',
  maritalStatus: '',
  address: '',
  emergencyContact: '',
  emergencyPhone: '',
  allergyHistory: '',
  pastHistory: ''
})

const profileInitial = computed(() => (form.value.name || userStore.nickName || '患').slice(0, 1))

const phoneRule = /^1[3-9]\d{9}$/

const rules = ref({
  name: [{ required: true, message: "患者姓名不能为空", trigger: "blur" }],
  gender: [{ required: true, message: "性别不能为空", trigger: "change" }],
  birthday: [{ required: true, message: "出生日期不能为空", trigger: "change" }],
  phone: [
    { required: true, message: "手机号不能为空", trigger: "blur" },
    { pattern: phoneRule, message: "手机号格式不正确", trigger: "blur" }
  ],
  emergencyPhone: [{ pattern: phoneRule, message: "紧急联系电话格式不正确", trigger: "blur" }]
})

watch(
  () => form.value.birthday,
  birthday => {
    form.value.age = calculateAge(birthday)
  }
)

onMounted(() => {
  fillRegisteredInfo()
})

function fillRegisteredInfo() {
  form.value.name = userStore.nickName || ''
  form.value.phone = userStore.phonenumber || ''
}

function calculateAge(birthday) {
  if (!birthday) {
    return ''
  }
  const birthDate = new Date(birthday)
  if (Number.isNaN(birthDate.getTime())) {
    return ''
  }
  const today = new Date()
  let age = today.getFullYear() - birthDate.getFullYear()
  const hasHadBirthday =
    today.getMonth() > birthDate.getMonth() ||
    (today.getMonth() === birthDate.getMonth() && today.getDate() >= birthDate.getDate())
  if (!hasHadBirthday) {
    age -= 1
  }
  return Math.max(age, 0)
}

function disableFutureBirthday(date) {
  return date.getTime() > Date.now()
}

function submitForm() {
  patientRef.value.validate(valid => {
    if (valid) {
      loading.value = true
      completePatient(form.value).then(() => {
        ElMessage.success("完善信息成功")
        loading.value = false
        userStore.getInfo().then(() => {
          router.push('/')
        })
      }).catch(() => {
        loading.value = false
      })
    }
  })
}

function resetForm() {
  patientRef.value?.resetFields()
  fillRegisteredInfo()
  form.value.age = calculateAge(form.value.birthday)
}

function goHome() {
  router.push('/')
}

function logout() {
  userStore.logOut().then(() => {
    router.push('/login')
  })
}
</script>

<style scoped lang="scss">
.patient-complete-page {
  min-height: calc(100vh - 84px);
  padding: 28px 16px 40px;
  background:
    linear-gradient(135deg, rgba(229, 246, 255, 0.92), rgba(246, 251, 249, 0.94) 46%, rgba(255, 249, 238, 0.9)),
    #f6fafb;
}

.complete-shell {
  max-width: 1040px;
  margin: 0 auto;
}

.complete-hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  padding: 30px 34px;
  border: 1px solid rgba(41, 121, 134, 0.12);
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 18px 48px rgba(42, 83, 101, 0.12);

  h1 {
    margin: 8px 0 10px;
    color: #173b46;
    font-size: 30px;
    font-weight: 700;
    line-height: 1.25;
  }

  p {
    max-width: 520px;
    margin: 0;
    color: #60727b;
    font-size: 14px;
    line-height: 1.8;
  }
}

.hero-kicker {
  color: #15809a;
  font-size: 13px;
  font-weight: 700;
}

.hero-profile {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 230px;
  padding: 16px;
  border-radius: 8px;
  background: #f2faf8;
  border: 1px solid #d8eee9;
}

.profile-avatar {
  display: grid;
  place-items: center;
  width: 48px;
  height: 48px;
  flex: 0 0 48px;
  border-radius: 50%;
  color: #fff;
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #1590a3, #23a96f);
}

.profile-name {
  color: #183943;
  font-size: 16px;
  font-weight: 700;
}

.profile-phone {
  margin-top: 4px;
  color: #6a7c84;
  font-size: 13px;
}

.complete-form {
  margin-top: 18px;
}

.form-section {
  padding: 26px 30px 12px;
  margin-top: 16px;
  border: 1px solid rgba(41, 121, 134, 0.1);
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 12px 34px rgba(42, 83, 101, 0.08);
}

.section-title {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  margin-bottom: 20px;

  span {
    display: grid;
    place-items: center;
    width: 34px;
    height: 34px;
    flex: 0 0 34px;
    border-radius: 50%;
    color: #0d7185;
    font-size: 13px;
    font-weight: 700;
    background: #e7f6f8;
  }

  h2 {
    margin: 0;
    color: #173b46;
    font-size: 18px;
    line-height: 1.3;
  }

  p {
    margin: 5px 0 0;
    color: #7a8a91;
    font-size: 13px;
  }
}

.form-actions {
  position: sticky;
  bottom: 0;
  z-index: 2;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 18px 0 0;
  margin-top: 4px;
  background: linear-gradient(180deg, rgba(246, 250, 251, 0), #f6fafb 30%);
}

:deep(.el-form-item) {
  margin-bottom: 22px;
}

:deep(.el-form-item__label) {
  color: #35525c;
  font-weight: 600;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner),
:deep(.el-select__wrapper) {
  border-radius: 6px;
  box-shadow: 0 0 0 1px #d9e5e8 inset;
}

:deep(.el-input.is-disabled .el-input__wrapper) {
  background: #f5f8f8;
  box-shadow: 0 0 0 1px #dce8e8 inset;
}

:deep(.el-segmented) {
  width: 100%;
  padding: 4px;
  border-radius: 6px;
  background: #f0f6f7;
}

:deep(.el-segmented__item) {
  min-height: 32px;
  border-radius: 5px;
}

:deep(.el-button) {
  border-radius: 6px;
}

@media (max-width: 768px) {
  .patient-complete-page {
    padding: 14px 10px 28px;
  }

  .complete-hero {
    display: block;
    padding: 24px 20px;

    h1 {
      font-size: 24px;
    }
  }

  .hero-profile {
    min-width: 0;
    margin-top: 20px;
  }

  .form-section {
    padding: 22px 18px 6px;
  }

  .form-actions {
    position: static;
    flex-wrap: wrap;
    justify-content: stretch;

    :deep(.el-button) {
      flex: 1 1 calc(50% - 6px);
      margin-left: 0;
    }
  }
}
</style>
