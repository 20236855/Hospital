<template>
  <div class="navbar" :class="'nav' + settingsStore.navType">
    <hamburger id="hamburger-container" :is-active="appStore.sidebar.opened" class="hamburger-container" @toggleClick="toggleSideBar" />

    <div v-if="settingsStore.navType == 1" class="navbar-workspace">
      <div class="workspace-title">
        <span>云医智联 工作台</span>
        <strong>AI-Driven Healthcare</strong>
      </div>
      <breadcrumb id="breadcrumb-container" class="breadcrumb-container" />
    </div>

    <top-nav v-if="settingsStore.navType == 2" id="topmenu-container" class="topmenu-container" />
    <template v-if="settingsStore.navType == 3">
      <logo v-show="settingsStore.sidebarLogo" :collapse="false" />
      <top-bar id="topbar-container" class="topbar-container" />
    </template>

    <div class="right-menu">
      <template v-if="appStore.device !== 'mobile'">
        <div class="shift-chip">
          <el-icon><OfficeBuilding /></el-icon>
          总院门诊
        </div>
        <div class="shift-chip shift-chip--soft">
          <el-icon><Clock /></el-icon>
          日间班
        </div>

        <header-search id="header-search" class="right-menu-item" />

        <el-tooltip content="全屏工作区" effect="dark" placement="bottom">
          <screenfull id="screenfull" class="right-menu-item hover-effect" />
        </el-tooltip>

        <el-tooltip content="主题模式" effect="dark" placement="bottom">
          <div class="right-menu-item hover-effect theme-switch-wrapper" @click="toggleTheme">
            <svg-icon v-if="settingsStore.isDark" icon-class="sunny" />
            <svg-icon v-if="!settingsStore.isDark" icon-class="moon" />
          </div>
        </el-tooltip>

        <el-tooltip content="界面密度" effect="dark" placement="bottom">
          <size-select id="size-select" class="right-menu-item hover-effect" />
        </el-tooltip>

        <el-tooltip content="业务通知" effect="dark" placement="bottom">
          <header-notice id="header-notice" class="right-menu-item hover-effect" />
        </el-tooltip>
      </template>

      <el-dropdown @command="handleCommand" class="avatar-container right-menu-item hover-effect" trigger="hover">
        <div class="avatar-wrapper">
          <img :src="userStore.avatar" class="user-avatar" @error="onAvatarError" />
          <div class="user-info">
            <span class="user-nickname">{{ userStore.nickName }}</span>
            <span class="user-role">{{ getUserRoleText(userStore.roles) }}</span>
          </div>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <router-link to="/user/profile">
              <el-dropdown-item>个人中心</el-dropdown-item>
            </router-link>
            <el-dropdown-item v-if="settingsStore.showSettings" command="setLayout">
              <span>界面设置</span>
            </el-dropdown-item>
            <el-dropdown-item command="lockScreen">
              <span>锁定屏幕</span>
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <span>退出登录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { ElMessageBox } from 'element-plus'
import { Clock, OfficeBuilding } from '@element-plus/icons-vue'
import Breadcrumb from '@/components/Breadcrumb'
import TopNav from './TopNav'
import TopBar from './TopBar'
import Logo from './Sidebar/Logo'
import Hamburger from '@/components/Hamburger'
import Screenfull from '@/components/Screenfull'
import SizeSelect from '@/components/SizeSelect'
import HeaderSearch from '@/components/HeaderSearch'
import HeaderNotice from './HeaderNotice'
import useAppStore from '@/store/modules/app'
import useUserStore from '@/store/modules/user'
import useLockStore from '@/store/modules/lock'
import useSettingsStore from '@/store/modules/settings'
import defAva from '@/assets/images/profile.jpg'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()
const lockStore = useLockStore()
const settingsStore = useSettingsStore()

function toggleSideBar() {
  appStore.toggleSideBar()
}

function handleCommand(command) {
  switch (command) {
    case 'setLayout':
      setLayout()
      break
    case 'lockScreen':
      lockScreen()
      break
    case 'logout':
      logout()
      break
    default:
      break
  }
}

function logout() {
  ElMessageBox.confirm('确定注销并退出系统吗？', '操作确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logOut().then(() => {
      location.href = '/index'
    })
  }).catch(() => {})
}

const emits = defineEmits(['setLayout'])
function setLayout() {
  emits('setLayout')
}

function lockScreen() {
  const currentPath = route.fullPath
  lockStore.lockScreen(currentPath)
  router.push('/lock')
}

function onAvatarError() {
  userStore.avatar = defAva
}

function getUserRoleText(roles) {
  if (!roles || roles.length === 0) {
    return ''
  }
  const roleMap = {
    'admin': '超级管理员',
    'doctor': '医护人员',
    'patient': '就诊患者'
  }
  // 查找匹配的角色
  for (const role of roles) {
    if (roleMap[role]) {
      return roleMap[role]
    }
  }
  return ''
}

async function toggleTheme(event) {
  const x = event?.clientX || window.innerWidth / 2
  const y = event?.clientY || window.innerHeight / 2
  const wasDark = settingsStore.isDark

  const isReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  const isSupported = document.startViewTransition && !isReducedMotion

  if (!isSupported) {
    settingsStore.toggleTheme()
    return
  }

  try {
    const transition = document.startViewTransition(async () => {
      await new Promise((resolve) => setTimeout(resolve, 10))
      settingsStore.toggleTheme()
      await nextTick()
    })
    await transition.ready

    const endRadius = Math.hypot(Math.max(x, window.innerWidth - x), Math.max(y, window.innerHeight - y))
    const clipPath = [`circle(0px at ${x}px ${y}px)`, `circle(${endRadius}px at ${x}px ${y}px)`]
    document.documentElement.animate(
      {
        clipPath: !wasDark ? [...clipPath].reverse() : clipPath
      }, {
        duration: 650,
        easing: 'cubic-bezier(0.4, 0, 0.2, 1)',
        fill: 'forwards',
        pseudoElement: !wasDark ? '::view-transition-old(root)' : '::view-transition-new(root)'
      }
    )
    await transition.finished
  } catch (error) {
    console.warn('View transition failed, falling back to immediate toggle:', error)
    settingsStore.toggleTheme()
  }
}
</script>

<style lang="scss" scoped>
.navbar.nav3 {
  .hamburger-container {
    display: none !important;
  }
}

.navbar {
  height: 58px;
  overflow: visible;
  position: relative;
  display: flex;
  align-items: center;
  box-sizing: border-box;
  padding: 0 16px 0 6px;
  border-bottom: 1px solid rgba(147, 197, 253, 0.22);
  background: var(--navbar-bg);
  box-shadow: 0 8px 24px rgba(20, 56, 120, 0.08);

  .hamburger-container {
    display: flex;
    align-items: center;
    flex-shrink: 0;
    height: 40px;
    margin-right: 12px;
    border-radius: 8px;
    color: #1E3A5F;
    cursor: pointer;
    line-height: 58px;
    transition: background 0.2s, color 0.2s;
    -webkit-tap-highlight-color: transparent;

    &:hover {
      background: #EFF6FF;
      color: #1D4ED8;
    }
  }

  .navbar-workspace {
    display: flex;
    align-items: center;
    gap: 18px;
    min-width: 0;
  }

  .workspace-title {
    display: flex;
    flex-direction: column;
    justify-content: center;
    min-width: 168px;
    line-height: 1.2;

    span {
      color: #1E3A5F;
      font-size: 16px;
      font-weight: 700;
    }

    strong {
      margin-top: 3px;
      color: #64748B;
      font-size: 11px;
      font-weight: 600;
      letter-spacing: 0;
    }
  }

  .breadcrumb-container {
    flex-shrink: 0;
  }

  .topmenu-container {
    position: absolute;
    left: 50px;
  }

  .topbar-container {
    display: flex;
    flex: 1;
    align-items: center;
    min-width: 0;
    margin-left: 8px;
    overflow: hidden;
  }

  .right-menu {
    display: flex;
    align-items: center;
    gap: 8px;
    height: 100%;
    margin-left: auto;
    line-height: 58px;

    &:focus {
      outline: none;
    }

    .right-menu-item {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      min-width: 36px;
      height: 36px;
      padding: 0 8px;
      border-radius: 8px;
      color: #1E3A5F;
      font-size: 17px;
      vertical-align: text-bottom;

      &.hover-effect {
        cursor: pointer;
        transition: background 0.2s, color 0.2s;

        &:hover {
          background: #EFF6FF;
          color: #1D4ED8;
        }
      }

      &.theme-switch-wrapper {
        display: flex;
        align-items: center;

        svg {
          transition: transform 0.3s;

          &:hover {
            transform: scale(1.12);
          }
        }
      }
    }

    .avatar-container {
      margin-right: 0;
      padding-right: 0;

      .avatar-wrapper {
          display: flex;
          align-items: center;
          gap: 8px;
          height: auto;
          min-height: 38px;
          padding: 6px 10px 6px 6px;
          border: 1px solid rgba(147, 197, 253, 0.26);
          border-radius: 8px;
          background: #EFF6FF;

        .user-avatar {
          width: 30px;
          height: 30px;
          margin-right: 0;
          border-radius: 50%;
          cursor: pointer;
        }

        .user-info {
          display: flex;
          flex-direction: column;
          gap: 2px;
          
          .user-nickname {
            position: static;
            color: #1E3A5F;
            font-size: 14px;
            font-weight: 700;
            line-height: 1.2;
          }
          
          .user-role {
            position: static;
            color: #64748B;
            font-size: 11px;
            font-weight: 500;
            line-height: 1.2;
          }
        }

        i {
          cursor: pointer;
          position: absolute;
          right: -20px;
          top: 25px;
          font-size: 12px;
        }
      }
    }
  }
}

.shift-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 34px;
  padding: 0 12px;
  border: 1px solid rgba(37, 99, 235, 0.20);
  border-radius: 8px;
  background: #EFF6FF;
  color: #143878;
  font-size: 13px;
  font-weight: 700;

  &--soft {
    background: #F0F4FF;
    color: #1D4ED8;
  }
}

@media (max-width: 1180px) {
  .navbar .workspace-title {
    min-width: 0;

    strong {
      display: none;
    }
  }

  .shift-chip {
    display: none;
  }
}

@media (max-width: 768px) {
  .navbar {
    padding-right: 10px;

    .workspace-title {
      display: none;
    }
  }
}
</style>
