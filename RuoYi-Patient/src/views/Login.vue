<template>
  <div class="login-container">
    <div class="header">
      <h1>若依患者端</h1>
      <p>智慧医疗，健康随行</p>
    </div>

    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field
          v-model="loginForm.username"
          name="username"
          label="账号"
          placeholder="请输入账号"
          :rules="[{ required: true, message: '请输入账号' }]"
        />
        <van-field
          v-model="loginForm.password"
          type="password"
          name="password"
          label="密码"
          placeholder="请输入密码"
          :rules="[{ required: true, message: '请输入密码' }]"
        />
        <van-field
          v-if="captchaEnabled"
          v-model="loginForm.code"
          name="code"
          label="验证码"
          placeholder="请输入验证码"
          :rules="[{ required: true, message: '请输入验证码' }]"
        >
          <template #button>
            <img
              v-if="codeUrl"
              :src="codeUrl"
              class="code-img"
              @click="getCode"
            />
          </template>
        </van-field>
      </van-cell-group>
      <div style="margin: 16px;">
        <van-button round block type="primary" native-type="submit" :loading="loading">
          登录
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { login, getCodeImg } from '@/api/login'
import { showToast } from 'vant'

const router = useRouter()
const loading = ref(false)
const captchaEnabled = ref(true)
const codeUrl = ref('')

const loginForm = ref({
  username: '',
  password: '',
  code: '',
  uuid: ''
})

const getCode = async () => {
  try {
    const res = await getCodeImg()
    captchaEnabled.value = res.captchaEnabled === undefined ? true : res.captchaEnabled
    if (captchaEnabled.value) {
      codeUrl.value = 'data:image/gif;base64,' + res.img
      loginForm.value.uuid = res.uuid
    }
  } catch (error) {
    console.error(error)
  }
}

const onSubmit = async (values) => {
  loading.value = true
  try {
    const res = await login(
      values.username,
      values.password,
      values.code,
      loginForm.value.uuid
    )
    const token = res.data.access_token
    localStorage.setItem('token', token)
    showToast('登录成功')
    router.push('/')
  } catch (error) {
    console.error(error)
    if (captchaEnabled.value) {
      getCode()
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getCode()
})
</script>

<style scoped lang="scss">
.login-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #1989fa 0%, #f5f5f5 40%);
  padding: 20px;

  .header {
    text-align: center;
    color: white;
    padding: 60px 0 40px;

    h1 {
      font-size: 28px;
      margin-bottom: 10px;
    }

    p {
      font-size: 14px;
      opacity: 0.9;
    }
  }

  .code-img {
    height: 40px;
    cursor: pointer;
  }
}
</style>
