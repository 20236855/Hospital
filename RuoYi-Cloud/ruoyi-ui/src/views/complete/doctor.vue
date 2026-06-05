<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>医生信息完善</span>
      </div>
      <div style="text-align: center; padding: 60px 20px;">
        <i class="el-icon-info" style="font-size: 80px; color: #409EFF;" />
        <h2 style="margin-top: 30px; color: #303133;">请等待管理员分配科室</h2>
        <p style="margin-top: 20px; color: #909399; font-size: 16px;">
          您的账号已注册成功，请联系管理员为您分配科室后即可正常使用系统
        </p>
        <div style="margin-top: 40px;">
          <el-button @click="logout">退出登录</el-button>
          <el-button type="primary" @click="goHome">返回首页</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'CompleteDoctor',
  methods: {
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
