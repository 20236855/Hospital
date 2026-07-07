<template>
  <div :class="['sidebar-theme-wrapper', {'has-logo':showLogo}, sideTheme]" class="sidebar-container">
    <logo v-if="showLogo" :collapse="isCollapse" />
    <el-scrollbar wrap-class="scrollbar-wrapper">
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :background-color="getMenuBackground"
        :text-color="getMenuTextColor"
        :unique-opened="true"
        :active-text-color="getActiveTextColor"
        :collapse-transition="false"
        mode="vertical"
        :class="sideTheme"
      >
        <sidebar-item
          v-for="(route, index) in sidebarRouters"
          :key="route.path + index"
          :item="route"
          :base-path="route.path"
        />
      </el-menu>
    </el-scrollbar>
    <div class="sidebar-bottom-image" v-if="!isCollapse">
      <img src="@/assets/images/image.png" alt="sidebar decoration" />
    </div>
  </div>
</template>

<script setup>
import Logo from './Logo'
import SidebarItem from './SidebarItem'
import variables from '@/assets/styles/variables.module.scss'
import useAppStore from '@/store/modules/app'
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'

const route = useRoute()
const appStore = useAppStore()
const settingsStore = useSettingsStore()
const permissionStore = usePermissionStore()

const sidebarRouters = computed(() => permissionStore.sidebarRouters)
const showLogo = computed(() => settingsStore.sidebarLogo)
const sideTheme = computed(() => settingsStore.sideTheme)
const isCollapse = computed(() => !appStore.sidebar.opened)

// 获取菜单背景色
const getMenuBackground = computed(() => {
  if (settingsStore.isDark) {
    return 'var(--sidebar-bg)'
  }
  return sideTheme.value === 'theme-dark' ? variables.menuBg : variables.menuLightBg
})

// 获取菜单文字颜色
const getMenuTextColor = computed(() => {
  if (settingsStore.isDark) {
    return 'var(--sidebar-text)'
  }
  return sideTheme.value === 'theme-dark' ? variables.menuText : variables.menuLightText
})

// 获取激活态文字颜色
const getActiveTextColor = computed(() => {
  if (sideTheme.value === 'theme-dark') {
    return '#ffffff'
  }
  return '#1E3A5F'
})

const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) {
    return meta.activeMenu
  }
  return path
})
</script>

<style lang="scss" scoped>
.sidebar-container {
  display: flex;
  flex-direction: column;
  background-color: v-bind(getMenuBackground);

  .scrollbar-wrapper {
    background-color: v-bind(getMenuBackground);
  }

  .el-scrollbar {
    flex: 1;
  }

  &.has-logo {
    .el-scrollbar {
      flex: 1;
      height: auto !important;
    }
  }

  .el-menu {
    border: none;
    height: 100%;
    width: 100% !important;

    .el-menu-item, .el-sub-menu__title {
      &:hover {
        background-color: rgba(59, 130, 246, 0.20) !important;
        color: #ffffff !important;
      }
    }

    .el-menu-item {
      color: v-bind(getMenuTextColor);

      &.is-active {
        color: v-bind(getActiveTextColor) !important;
        background-color: rgba(37, 99, 235, 0.30) !important;
      }
    }

    .el-sub-menu__title {
      color: v-bind(getMenuTextColor);
    }
  }

  .sidebar-bottom-image {
    flex-shrink: 0;
    overflow: hidden;
    line-height: 0;
    margin-top: 0;
    
    img {
      width: 100%;
      height: auto;
      display: block;
    }
  }
}
</style>
