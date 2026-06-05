<template>
  <div class="auth-page register">
    <span class="auth-bg-cross cross-one"></span>
    <span class="auth-bg-cross cross-two"></span>
    <span class="auth-bg-cross cross-three"></span>
    <span class="auth-bg-cross cross-four"></span>

    <div class="auth-shell">
      <AuthMedicalVisual />

      <main class="auth-panel">
        <el-form ref="registerRef" :model="registerForm" :rules="registerRules" class="auth-form register-form">
          <div class="form-sparkles">
            <span></span>
            <span></span>
          </div>
          <div class="brand-lockup">
            <span class="brand-mark"><i class="brand-cross"></i></span>
            <p class="slogan">用心守护，智慧就医</p>
            <h3 class="title">注册{{ title }}</h3>
          </div>
      <el-form-item prop="userType" label="身份">
        <el-radio-group v-model="registerForm.userType">
          <el-radio value="patient">就诊患者</el-radio>
          <el-radio value="doctor">医护人员</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item prop="username">
        <el-input 
          v-model="registerForm.username" 
          type="text" 
          size="large" 
          auto-complete="off" 
          placeholder="账号"
        >
          <template #prefix><svg-icon icon-class="user" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password" :rules="registerPwdValidator">
        <el-input
          v-model="registerForm.password"
          type="password"
          size="large" 
          auto-complete="off"
          placeholder="密码"
          @keyup.enter="handleRegister"
        >
          <template #prefix><svg-icon icon-class="password" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="confirmPassword">
        <el-input
          v-model="registerForm.confirmPassword"
          type="password"
          size="large" 
          auto-complete="off"
          placeholder="确认密码"
          @keyup.enter="handleRegister"
        >
          <template #prefix><svg-icon icon-class="password" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="nickName">
        <el-input 
          v-model="registerForm.nickName" 
          type="text" 
          size="large" 
          auto-complete="off" 
          placeholder="姓名"
        >
          <template #prefix><svg-icon icon-class="user" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="phonenumber">
        <el-input 
          v-model="registerForm.phonenumber" 
          type="text" 
          size="large" 
          auto-complete="off" 
          placeholder="手机号"
        >
          <template #prefix><svg-icon icon-class="phone" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>

      <el-form-item prop="code" v-if="captchaEnabled" class="auth-code-item">
        <el-input
          size="large" 
          v-model="registerForm.code"
          class="auth-code-input"
          auto-complete="off"
          placeholder="验证码"
          @keyup.enter="handleRegister"
        >
          <template #prefix><svg-icon icon-class="validCode" class="el-input__icon input-icon" /></template>
        </el-input>
        <div class="auth-code register-code">
          <img :src="codeUrl" @click="getCode" class="auth-code-img register-code-img" />
        </div>
      </el-form-item>
      <el-form-item>
        <el-button
          class="auth-submit"
          :loading="loading"
          size="large" 
          type="primary"
          @click.prevent="handleRegister"
        >
          <span v-if="!loading">注册</span>
          <span v-else>注册中...</span>
        </el-button>
      </el-form-item>
          <div class="auth-switch">
            <span>已有账号？</span>
            <router-link :to="'/login'">去登录</router-link>
          </div>
        </el-form>
      </main>
    </div>
    <!--  底部  -->
    <div class="el-register-footer">
      <span>{{ footerContent }}</span>
    </div>
  </div>
</template>

<script setup>
import { ElMessageBox } from "element-plus"
import { getCodeImg, register } from "@/api/login"
import defaultSettings from '@/settings'
import { usePasswordRule } from "@/utils/passwordRule"
import AuthMedicalVisual from '@/components/AuthMedicalVisual/index.vue'

const title = import.meta.env.VITE_APP_TITLE
const footerContent = defaultSettings.footerContent
const router = useRouter()
const { proxy } = getCurrentInstance()
const { registerPwdValidator } = usePasswordRule()

const registerForm = ref({
  username: "",
  password: "",
  confirmPassword: "",
  code: "",
  uuid: "",
  userType: "",
  nickName: "",
  phonenumber: ""
})

const equalToPassword = (rule, value, callback) => {
  if (registerForm.value.password !== value) {
    callback(new Error("两次输入的密码不一致"))
  } else {
    callback()
  }
}

const validateUserType = (rule, value, callback) => {
  if (!value) {
    callback(new Error("请选择身份"))
  } else {
    callback()
  }
}

const registerRules = {
  userType: [
    { required: true, validator: validateUserType, trigger: "change" }
  ],
  username: [
    { required: true, trigger: "blur", message: "请输入您的账号" },
    { min: 2, max: 20, message: "用户账号长度必须介于 2 和 20 之间", trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, trigger: "blur", message: "请再次输入您的密码" },
    { required: true, validator: equalToPassword, trigger: "blur" }
  ],
  nickName: [
    { required: true, trigger: "blur", message: "请输入您的姓名" }
  ],
  phonenumber: [
    { required: true, trigger: "blur", message: "请输入您的手机号" },
    { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号", trigger: "blur" }
  ],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }]
}

const codeUrl = ref("")
const loading = ref(false)
const captchaEnabled = ref(true)

function handleRegister() {
  proxy.$refs.registerRef.validate(valid => {
    if (valid) {
      if (registerForm.value.username === 'admin') {
        proxy.$modal.msgError("超级管理员账号不开放自助注册")
        return
      }
      loading.value = true
      const submitData = { ...registerForm.value }
      delete submitData.confirmPassword
      register(submitData).then(res => {
        const username = registerForm.value.username
        const message = registerForm.value.userType === 'doctor'
          ? `<font color='red'>恭喜你，您的账号 ${username} 注册成功！账号需要管理员审核后才能登录。</font>`
          : `<font color='red'>恭喜你，您的账号 ${username} 注册成功！</font>`
        ElMessageBox.alert(message, "系统提示", {
          dangerouslyUseHTMLString: true,
          type: "success",
        }).then(() => {
          router.push("/login")
        }).catch(() => {})
      }).catch(() => {
        loading.value = false
        if (captchaEnabled.value) {
          getCode()
        }
      })
    }
  })
}

function getCode() {
  getCodeImg().then(res => {
    captchaEnabled.value = res.captchaEnabled === undefined ? true : res.captchaEnabled
    if (captchaEnabled.value) {
      codeUrl.value = "data:image/gif;base64," + res.img
      registerForm.value.uuid = res.uuid
    }
  })
}

getCode()
</script>

<style lang='scss'>
@use "@/assets/styles/auth-medical.scss";
</style>
