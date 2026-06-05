import router from '@/router'
import cache from '@/plugins/cache'
import { ElMessageBox, } from 'element-plus'
import { login, logout, getInfo } from '@/api/login'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { isEmpty, isExternal } from "@/utils/validate"
import useLockStore from '@/store/modules/lock'
import defAva from '@/assets/images/profile.jpg'

function resolveAvatar(avatar) {
  if (isEmpty(avatar)) {
    return defAva
  }
  const avatarUrl = String(avatar).trim()
  if (isEmpty(avatarUrl)) {
    return defAva
  }
  if (avatarUrl.startsWith('data:') || isExternal(avatarUrl)) {
    return avatarUrl
  }
  const baseApi = import.meta.env.VITE_APP_BASE_API || ''
  // 移除可能已有的时间戳参数
  const cleanAvatar = avatarUrl.split('?')[0]
  const normalizedAvatar = cleanAvatar.startsWith('/') ? cleanAvatar : `/${cleanAvatar}`
  return `${baseApi}${normalizedAvatar}`
}

const useUserStore = defineStore(
  'user',
  {
    state: () => ({
      token: getToken(),
      id: '',
      name: '',
      nickName: '',
      avatar: '',
      roles: [],
      permissions: [],
      userType: '',
      needCompleteInfo: false,
      deptId: null
    }),
    actions: {
      // 登录
      login(userInfo) {
        const username = userInfo.username.trim()
        const password = userInfo.password
        const code = userInfo.code
        const uuid = userInfo.uuid
        return new Promise((resolve, reject) => {
          login(username, password, code, uuid).then(res => {
            let data = res.data
            setToken(data.access_token)
            this.token = data.access_token
            useLockStore().unlockScreen()
            resolve()
          }).catch(error => {
            reject(error)
          })
        })
      },
      // 获取用户信息
      getInfo() {
        return new Promise((resolve, reject) => {
          getInfo().then(res => {
            const user = res.user
            const avatar = resolveAvatar(user.avatar)
            if (res.roles && res.roles.length > 0) { // 验证返回的roles是否是一个非空数组
              this.roles = res.roles
              this.permissions = res.permissions
            } else {
              this.roles = ['ROLE_DEFAULT']
            }
            this.id = user.userId
            this.name = user.userName
            this.nickName = user.nickName
            this.avatar = avatar
            // 直接使用后端返回的userType
            this.userType = res.userType || ''
            this.needCompleteInfo = res.needCompleteInfo || false
            this.deptId = res.deptId || null
            cache.session.set('pwrChrtype', res.pwdChrtype)
            /* 初始密码提示 */
            if(res.isDefaultModifyPwd) {
              ElMessageBox.confirm('您的密码还是初始密码，请修改密码！',  '安全提示', {  confirmButtonText: '确定',  cancelButtonText: '取消',  type: 'warning' }).then(() => {
                router.push({ name: 'Profile', params: { activeTab: 'resetPwd' } })
              }).catch(() => {})
            }
            /* 过期密码提示 */
            if(!res.isDefaultModifyPwd && res.isPasswordExpired) {
              ElMessageBox.confirm('您的密码已过期，请尽快修改密码！',  '安全提示', {  confirmButtonText: '确定',  cancelButtonText: '取消',  type: 'warning' }).then(() => {
                router.push({ name: 'Profile', params: { activeTab: 'resetPwd' } })
              }).catch(() => {})
            }
            resolve(res)
          }).catch(error => {
            reject(error)
          })
        })
      },
      // 退出系统
      logOut() {
        return new Promise((resolve, reject) => {
          logout(this.token).then(() => {
            this.token = ''
            this.roles = []
            this.permissions = []
            removeToken()
            resolve()
          }).catch(error => {
            reject(error)
          })
        })
      },
      setAvatar(avatar) {
        // 保存原始URL（不含时间戳）到store，刷新页面后重新加载
        const cleanAvatar = avatar ? String(avatar).split('?')[0] : ''
        this.avatar = resolveAvatar(cleanAvatar)
      }
    }
  })

export default useUserStore
