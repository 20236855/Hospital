<template>
  <div class="sidebar-logo-container" :class="{ collapse }">
    <transition name="sidebarLogoFade">
      <router-link v-if="collapse" key="collapse" class="sidebar-logo-link" to="/">
        <img src="/logo1.png" alt="logo" class="brand-logo-img" />
      </router-link>
      <router-link v-else key="expand" class="sidebar-logo-link" to="/">
        <span class="logo-title-wrapper">
          <img src="/logo3.png" alt="logo" class="brand-logo-img" />
          <span class="brand-title">
            <strong>门诊 HIS</strong>
          </span>
        </span>
        <small class="brand-desc">医院门诊信息系统</small>
      </router-link>
    </transition>
  </div>
</template>

<script setup>
defineProps({
  collapse: {
    type: Boolean,
    required: true
  }
})
</script>

<style lang="scss" scoped>
.sidebarLogoFade-enter-active {
  transition: opacity 0.35s;
}
.sidebarLogoFade-enter,
.sidebarLogoFade-leave-to {
  opacity: 0;
}

.sidebar-logo-container {
  position: relative;
  height: auto;
  padding: 14px 0;
  overflow: hidden;
  border-bottom: 1px solid rgba(165, 213, 196, 0.18);
  background: #1a4d45;

  .sidebar-logo-link {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    width: 100%;
    padding: 0 18px;
    text-decoration: none;
    gap: 6px;
  }

  .logo-title-wrapper {
    display: flex;
    align-items: center;
    gap: 10px;
    width: 100%;
  }

  .brand-logo-img {
    flex-shrink: 0;
    width: 42px;
    height: 42px;
    border-radius: 8px;
    object-fit: contain;
  }

  .brand-title {
    display: flex;
    align-items: center;
    
    strong {
      color: #ffffff;
      font-size: 16px;
      font-weight: 600;
      letter-spacing: 0.5px;
      line-height: 1;
    }
  }

  .brand-desc {
    color: rgba(221, 248, 242, 0.76);
    font-size: 12px;
    line-height: 1.4;
    padding-left: 52px; /* logo宽度 + gap */
  }

  &.collapse {
    padding: 10px 0;
    
    .sidebar-logo-link {
      justify-content: center;
      align-items: center;
      padding: 0;
    }
    
    .logo-title-wrapper {
      display: none;
    }
    
    .brand-logo-img {
      width: 36px;
      height: 36px;
    }
  }
}
</style>