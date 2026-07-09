<template>
  <section class="doctor-insight" :class="`tone-${tone}`">
    <div class="insight-head">
      <div>
        <span class="eyebrow">{{ eyebrow }}</span>
        <h2>{{ title }}</h2>
        <p>{{ subtitle }}</p>
      </div>
      <div class="head-kpis" v-if="metrics.length">
        <span v-for="item in metrics.slice(0, 4)" :key="item.label" :class="item.state || 'info'">
          <em>{{ item.label }}</em>
          <strong>{{ item.value }}</strong>
          <small v-if="item.note">{{ item.note }}</small>
        </span>
      </div>
    </div>

    <div class="distribution">
      <div class="donut-zone">
        <div class="donut-chart" :style="donutStyle">
          <div class="donut-hole">
            <strong>{{ totalText }}</strong>
            <em>{{ focusLabel }}</em>
          </div>
        </div>
      </div>

      <div class="distribution-list">
        <div class="list-head">
          <strong>业务状态分布</strong>
          <span>实时列表占比</span>
        </div>
        <div v-for="item in distributionItems" :key="item.label" class="dist-row">
          <i :style="{ backgroundColor: item.color }"></i>
          <span class="dist-name">{{ item.label }}</span>
          <strong>{{ item.percent }}%</strong>
          <div class="dist-track">
            <b :style="{ width: item.percent + '%', backgroundColor: item.color }"></b>
          </div>
        </div>
      </div>
    </div>

    <div class="chip-wrap" v-if="chips.length">
      <span v-for="chip in chips" :key="chip.label" class="insight-chip" :class="chip.type || 'info'">
        {{ chip.label }} <strong>{{ chip.value }}</strong>
      </span>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  eyebrow: { type: String, default: 'Doctor Console' },
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  focusValue: { type: [String, Number], default: '--' },
  focusLabel: { type: String, default: '总接诊数' },
  tone: { type: String, default: 'clinical' },
  metrics: { type: Array, default: () => [] },
  lanes: { type: Array, default: () => [] },
  chips: { type: Array, default: () => [] }
})

const palette = ['#0876C9', '#37A9EA', '#3D5AB8', '#7EA7D8', '#94A3B8']

function toNumber(value) {
  const n = Number(value)
  return Number.isFinite(n) ? n : 0
}

const rawItems = computed(() => {
  const source = props.lanes.length ? props.lanes : props.metrics
  return source.map((item, index) => ({
    label: item.label,
    value: toNumber(item.value),
    progress: toNumber(item.progress),
    color: item.color || palette[index % palette.length]
  }))
})

const total = computed(() => rawItems.value.reduce((sum, item) => sum + item.value, 0))

const distributionItems = computed(() => {
  return rawItems.value.map(item => {
    const percent = total.value > 0 ? Math.round((item.value / total.value) * 100) : Math.max(0, Math.min(100, item.progress))
    return { ...item, percent }
  })
})

const totalText = computed(() => {
  if (total.value > 0) return total.value.toLocaleString()
  return props.focusValue
})

const donutStyle = computed(() => {
  if (!distributionItems.value.length || total.value === 0) {
    return { background: '#eef3f8' }
  }
  let start = 0
  const segments = distributionItems.value.map((item, index) => {
    const sweep = index === distributionItems.value.length - 1 ? 100 - start : item.percent
    const end = Math.min(100, start + Math.max(0, sweep))
    const segment = `${item.color} ${start * 3.6}deg ${end * 3.6}deg`
    start = end
    return segment
  })
  return {
    background: `conic-gradient(${segments.join(', ')})`
  }
})
</script>

<style scoped lang="scss">
.doctor-insight {
  --primary: #0876C9;
  --primary-soft: #E9F8FF;
  --primary-line: #BFE7FF;
  --line: #DCEAF5;
  --track: #EAF3FA;
  --text: #102A43;
  --muted: #64748B;
  margin-bottom: 18px;
  padding: 22px 24px 24px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, .98) 0%, rgba(246, 251, 255, .98) 52%, rgba(233, 248, 255, .92) 100%);
  box-shadow: 0 16px 36px rgba(8, 118, 201, .08), 0 1px 0 rgba(255, 255, 255, .9) inset;
  color: var(--text);
  position: relative;
  overflow: hidden;

  &::before {
    position: absolute;
    top: 0;
    right: 0;
    left: 0;
    height: 3px;
    content: "";
    background: linear-gradient(90deg, #0876C9 0%, #37A9EA 55%, #D9F1FF 100%);
  }
}

.insight-head {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 20px;
  position: relative;
  z-index: 1;
}

.eyebrow {
  display: inline-flex;
  align-items: center;
  margin-bottom: 8px;
  padding: 3px 9px;
  border: 1px solid var(--primary-line);
  border-radius: 999px;
  background: rgba(233, 248, 255, .72);
  color: var(--primary);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: .2px;
}

h2 {
  margin: 0;
  font-size: 21px;
  line-height: 1.25;
  font-weight: 800;
  letter-spacing: 0;
  color: #0F2537;
}

p {
  margin: 6px 0 0;
  color: var(--muted);
  font-size: 13px;
}

.head-kpis {
  display: grid;
  grid-template-columns: repeat(4, minmax(72px, 1fr));
  gap: 8px;
  min-width: 460px;

  span {
    min-width: 0;
    padding: 10px 12px 11px;
    border-radius: 8px;
    background: rgba(255, 255, 255, .82);
    border: 1px solid rgba(191, 231, 255, .72);
    box-shadow: 0 8px 18px rgba(15, 37, 55, .04);

    &.good,
    &.success {
      border-color: rgba(55, 169, 234, .42);
    }

    &.warn,
    &.warning {
      border-color: rgba(245, 158, 11, .34);
      background: rgba(255, 251, 235, .72);
    }
  }

  em,
  strong,
  small {
    display: block;
  }

  em {
    overflow: hidden;
    color: var(--muted);
    font-size: 12px;
    font-style: normal;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  strong {
    margin-top: 4px;
    color: #0F2537;
    font-size: 21px;
    font-weight: 800;
    font-variant-numeric: tabular-nums;
  }

  small {
    overflow: hidden;
    margin-top: 3px;
    color: #7B8DA1;
    font-size: 11px;
    line-height: 1.25;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.distribution {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 24px;
  align-items: center;
  position: relative;
  z-index: 1;
}

.donut-zone {
  display: grid;
  place-items: center;
  min-height: 194px;
  border: 1px solid rgba(191, 231, 255, .62);
  border-radius: 8px;
  background: rgba(255, 255, 255, .62);
}

.donut-chart {
  position: relative;
  display: grid;
  place-items: center;
  width: 172px;
  height: 172px;
  border-radius: 50%;
  box-shadow: 0 18px 34px rgba(8, 118, 201, .14);

  &::after {
    position: absolute;
    inset: 38px;
    content: "";
    border-radius: 50%;
    background: linear-gradient(180deg, #FFFFFF, #F8FCFF);
    box-shadow: inset 0 0 0 1px #E4F0F8;
  }
}

.donut-hole {
  position: relative;
  z-index: 1;
  display: grid;
  place-items: center;
  gap: 6px;

  strong {
    color: #0A4F8E;
    font-size: 31px;
    line-height: 1;
    font-weight: 850;
    font-variant-numeric: tabular-nums;
  }

  em {
    color: #6B7F93;
    font-size: 12px;
    font-style: normal;
    font-weight: 700;
  }
}

.distribution-list {
  display: grid;
  gap: 10px;
  padding: 16px;
  border: 1px solid rgba(220, 234, 245, .86);
  border-radius: 8px;
  background: rgba(255, 255, 255, .72);
}

.list-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 2px;

  strong {
    color: var(--text);
    font-size: 15px;
    font-weight: 800;
  }

  span {
    color: #7B8DA1;
    font-size: 13px;
  }
}

.dist-row {
  display: grid;
  grid-template-columns: 10px 104px 44px minmax(120px, 1fr);
  gap: 10px;
  align-items: center;
  min-width: 0;

  i {
    width: 10px;
    height: 10px;
    border-radius: 3px;
    box-shadow: 0 0 0 3px rgba(8, 118, 201, .08);
  }
}

.dist-name {
  overflow: hidden;
  color: #243B53;
  font-size: 14px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dist-row strong {
  color: #102A43;
  font-size: 15px;
  font-weight: 850;
  text-align: right;
  font-variant-numeric: tabular-nums;
}

.dist-track {
  overflow: hidden;
  height: 7px;
  border-radius: 999px;
  background: var(--track);

  b {
    display: block;
    height: 100%;
    border-radius: inherit;
    box-shadow: 0 0 12px rgba(8, 118, 201, .22);
    transition: width .45s ease;
  }
}

.chip-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid rgba(220, 234, 245, .9);
  position: relative;
  z-index: 1;
}

.insight-chip {
  padding: 7px 11px;
  border: 1px solid #CFE4F5;
  border-radius: 8px;
  background: rgba(255, 255, 255, .72);
  color: #536A7F;
  font-size: 12px;
  font-weight: 700;

  strong {
    margin-left: 6px;
    color: var(--text);
    font-variant-numeric: tabular-nums;
  }

  &.success { border-color: #A9DDF7; background: #F0FAFF; color: #0876C9; }
  &.warning { border-color: #F7D58A; background: #FFFBEB; color: #9A6A08; }
  &.danger { border-color: #F3B7B7; background: #FFF5F5; color: #B42318; }
}

@media (max-width: 1100px) {
  .insight-head { flex-direction: column; }
  .head-kpis { min-width: 0; }
}

@media (max-width: 760px) {
  .doctor-insight { padding: 18px; }
  .head-kpis { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .distribution { grid-template-columns: 1fr; gap: 16px; }
  .dist-row { grid-template-columns: 10px minmax(72px, 1fr) 42px; }
  .dist-track { grid-column: 2 / -1; }
}
</style>
