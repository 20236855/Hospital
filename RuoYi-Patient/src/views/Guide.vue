<template>
  <div class="guide-page">
    <div class="guide-skip" @click="nextOrLogin">
      {{ current === slides.length - 1 ? '进入' : '下一步' }}
    </div>

    <div class="guide-slider" ref="sliderRef">
      <div class="guide-track" :style="{ transform: `translateX(-${current * 100}%)` }">
        <div class="guide-slide" v-for="(img, idx) in slides" :key="idx">
          <img :src="img" :alt="'引导页' + (idx + 1)" class="guide-img" />
        </div>
      </div>
    </div>

    <div class="guide-bottom">
      <div class="guide-dots">
        <span v-for="(_, idx) in slides" :key="idx" class="dot" :class="{ active: current === idx }" @click="goTo(idx)"></span>
      </div>
      <button class="guide-btn" @click="goLogin">立即体验</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import guide1 from '@/assets/guide4.png'
import guide2 from '@/assets/guide5.png'
import guide3 from '@/assets/guide6.png'

const router = useRouter()
const slides = [guide1, guide2, guide3]
const current = ref(0)
let timer = null

function startAutoPlay() {
  stopAutoPlay()
  timer = setInterval(() => {
    current.value = (current.value + 1) % slides.length
  }, 3000)
}

function stopAutoPlay() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

function goTo(idx) {
  current.value = idx
  startAutoPlay()
}

function nextOrLogin() {
  if (current.value === slides.length - 1) {
    goLogin()
  } else {
    current.value++
    startAutoPlay()
  }
}

function goLogin() {
  stopAutoPlay()
  localStorage.setItem('guideSeen', '1')
  router.replace('/login')
}

onMounted(() => {
  startAutoPlay()
})

onBeforeUnmount(() => {
  stopAutoPlay()
})
</script>

<style scoped lang="scss">
.guide-skip {
  position: fixed;
  top: 16px;
  right: 16px;
  z-index: 10;
  padding: 8px 18px;
  border-radius: 20px;
  background: rgba(0, 0, 0, 0.35);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s ease;

  &:active {
    background: rgba(0, 0, 0, 0.55);
  }
}

.guide-page {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  position: relative;
  background: #000;
}

.guide-slider {
  width: 100%;
  height: 100%;
}

.guide-track {
  display: flex;
  height: 100%;
  transition: transform 0.5s ease;
}

.guide-slide {
  flex: 0 0 100%;
  height: 100%;
}

.guide-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: top;
  display: block;
}

.guide-bottom {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 0 32px 48px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 28px;
  pointer-events: none; // 让底部不阻挡图片点击
}

.guide-dots {
  display: flex;
  gap: 10px;
  pointer-events: auto;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.4);
  transition: all 0.3s ease;
  cursor: pointer;

  &.active {
    width: 24px;
    border-radius: 4px;
    background: #fff;
  }
}

.guide-btn {
  pointer-events: auto;
  width: 260px;
  height: 50px;
  border-radius: 25px;
  border: none;
  background: rgba(255, 255, 255, 0.95);
  color: #4f7380;
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 1px;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  &:active {
    transform: scale(0.96);
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  }
}
</style>
