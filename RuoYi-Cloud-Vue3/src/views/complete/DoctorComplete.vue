<template>
  <div class="app-container">
    <el-card>
      <div class="complete-container">
        <div class="complete-header">
          <el-icon :size="80" color="#409EFF">
            <InfoFilled />
          </el-icon>
          <h2>请等待管理员分配科室</h2>
          <p>您的账号已注册成功，请联系管理员为您分配科室后即可正常使用系统</p>
        </div>
        <div class="complete-buttons">
          <el-button @click="logout">退出登录</el-button>
          <el-button type="primary" @click="goHome">返回首页</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup name="DoctorComplete">
import useUserStore from '@/store/modules/user'
import router from '@/router'
import { InfoFilled } from '@element-plus/icons-vue'

const userStore = useUserStore()

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
  max-width: 600px;
  margin: 0 auto;
  padding: 60px 20px;
  text-align: center;

  .complete-header {
    margin-bottom: 40px;

    h2 {
      font-size: 28px;
      color: #303133;
      margin-top: 20px;
      margin-bottom: 10px;
    }

    p {
      font-size: 16px;
      color: #909399;
    }
  }

  .complete-buttons {
    display: flex;
    gap: 12px;
    justify-content: center;
  }
}
</style>
