<template>
  <div class="app-container">
    <el-form ref="form" :model="form" :rules="rules" label-width="100px">
      <el-card>
        <div slot="header" class="clearfix">
          <span>完善患者信息</span>
        </div>
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
              <el-date-picker
                v-model="form.birthday"
                type="date"
                placeholder="选择日期"
                value-format="yyyy-MM-dd"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input-number v-model="form.age" :min="0" :max="150" placeholder="请输入年龄" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
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
              <el-select v-model="form.bloodType" placeholder="请选择血型" style="width: 100%">
                <el-option label="A型" value="A型" />
                <el-option label="B型" value="B型" />
                <el-option label="AB型" value="AB型" />
                <el-option label="O型" value="O型" />
                <el-option label="Rh阳性" value="Rh阳性" />
                <el-option label="Rh阴性" value="Rh阴性" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="婚姻状态" prop="maritalStatus">
              <el-select v-model="form.maritalStatus" placeholder="请选择婚姻状态" style="width: 100%">
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
              <el-input v-model="form.address" type="textarea" :rows="2" placeholder="请输入家庭住址" />
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
              <el-input v-model="form.allergyHistory" type="textarea" :rows="2" placeholder="请输入过敏史" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="既往史" prop="pastHistory">
              <el-input v-model="form.pastHistory" type="textarea" :rows="2" placeholder="请输入既往史" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item>
          <el-button @click="logout">退出登录</el-button>
          <el-button @click="goHome">返回首页</el-button>
          <el-button @click="resetForm">重置</el-button>
          <el-button type="primary" @click="submitForm">保存</el-button>
        </el-form-item>
      </el-card>
    </el-form>
  </div>
</template>

<script>
import { completePatient } from '@/api/patient'
import { getInfo } from '@/api/login'

export default {
  name: 'CompletePatient',
  data() {
    return {
      form: {
        name: '',
        gender: '',
        birthday: null,
        age: null,
        phone: '',
        idCard: '',
        bloodType: '',
        maritalStatus: '',
        address: '',
        emergencyContact: '',
        emergencyPhone: '',
        allergyHistory: '',
        pastHistory: ''
      },
      rules: {
        name: [
          { required: true, message: '请输入患者姓名', trigger: 'blur' }
        ],
        gender: [
          { required: true, message: '请选择性别', trigger: 'change' }
        ],
        phone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.initUserInfo()
  },
  methods: {
    initUserInfo() {
      // 从store获取用户信息
      const user = this.$store.state.user
      this.form.name = user.nickName || ''
      this.form.phone = ''
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          completePatient(this.form).then(response => {
            this.$modal.msgSuccess('保存成功')
            // 刷新用户信息
            this.$store.dispatch('GetInfo').then(() => {
              this.$router.push('/index')
            })
          })
        }
      })
    },
    resetForm() {
      this.$refs.form.resetFields()
      this.initUserInfo()
    },
    goHome() {
      this.$router.push('/index')
    },
    logout() {
      this.$store.dispatch('LogOut').then(() => {
        this.$router.push('/login')
      })
    }
  }
}
</script>
