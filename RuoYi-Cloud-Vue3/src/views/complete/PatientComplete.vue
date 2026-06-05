<template>
  <div class="app-container">
    <div class="complete-container">
      <div class="complete-header">
        <h2>完善个人信息</h2>
        <p>请填写完整的个人信息，以便更好地为您提供服务</p>
      </div>
      <el-form ref="patientRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="患者姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入患者姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" placeholder="请选择性别" style="width: 100%">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="出生日期" prop="birthday">
              <el-date-picker clearable
                v-model="form.birthday"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择出生日期"
                style="width: 100%">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input v-model="form.age" placeholder="请输入年龄" type="number" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="form.idCard" placeholder="请输入身份证号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="血型" prop="bloodType">
              <el-select v-model="form.bloodType" placeholder="请选择血型" clearable style="width: 100%">
                <el-option label="A型" value="A" />
                <el-option label="B型" value="B" />
                <el-option label="AB型" value="AB" />
                <el-option label="O型" value="O" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
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
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="家庭住址" prop="address">
              <el-input v-model="form.address" placeholder="请输入家庭住址" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="紧急联系人" prop="emergencyContact">
              <el-input v-model="form.emergencyContact" placeholder="请输入紧急联系人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="紧急联系电话" prop="emergencyPhone">
              <el-input v-model="form.emergencyPhone" placeholder="请输入紧急联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="过敏史" prop="allergyHistory">
              <el-input v-model="form.allergyHistory" type="textarea" :rows="3" placeholder="请输入过敏史，如：青霉素过敏" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="既往史" prop="pastHistory">
              <el-input v-model="form.pastHistory" type="textarea" :rows="3" placeholder="请输入既往病史，如：高血压、糖尿病" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item>
          <el-button @click="logout">退出登录</el-button>
          <el-button @click="goHome">返回首页</el-button>
          <el-button @click="resetForm">重置</el-button>
          <el-button type="primary" @click="submitForm" :loading="loading">
            确 认 保 存
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup name="PatientComplete">
import { completePatient } from "@/api/patient/patient"
import useUserStore from '@/store/modules/user'
import router from '@/router'
import { ElMessage } from 'element-plus'

const { proxy } = getCurrentInstance()

const userStore = useUserStore()

const loading = ref(false)
const patientRef = ref()

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

const rules = ref({
  name: [{ required: true, message: "患者姓名不能为空", trigger: "blur" }],
  gender: [{ required: true, message: "性别不能为空", trigger: "change" }],
  phone: [{ required: true, message: "手机号不能为空", trigger: "blur" }]
})

onMounted(() => {
  // 自动填充用户信息
  form.value.name = userStore.nickName
  form.value.phone = userStore.name || ''
})

function submitForm() {
  patientRef.value.validate(valid => {
    if (valid) {
      loading.value = true
      completePatient(form.value).then(response => {
        ElMessage.success("完善信息成功")
        loading.value = false
        // 刷新用户信息，清除完善状态
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
  form.value.name = userStore.nickName
  form.value.phone = userStore.name || ''
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
.complete-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 40px 20px;

  .complete-header {
    text-align: center;
    margin-bottom: 40px;

    h2 {
      font-size: 28px;
      color: #333;
      margin-bottom: 10px;
    }

    p {
      font-size: 14px;
      color: #666;
    }
  }
}
</style>
