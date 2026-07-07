<template>
  <section class="doctor-insight" :class="`tone-${tone}`">
    <div class="insight-head">
      <div>
        <span class="eyebrow">{{ eyebrow }}</span>
        <h2>{{ title }}</h2>
        <p>{{ subtitle }}</p>
      </div>
      <div class="head-kpis" v-if="metrics.length">
        <span v-for="item in metrics.slice(0, 4)" :key="item.label">
          <em>{{ item.label }}</em>
          <strong>{{ item.value }}</strong>
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
          <strong>接诊状态分布</strong>
          <span>按当前列表占比</span>
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

const palette = ['#3d6ea8', '#5b8fc0', '#4f97a6', '#6b8bb0', '#88a0bd']

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
  --line: #e6edf3;
  --track: #eef3f8;
  --text: #1f3a52;
  --muted: #6f8295;
  margin-bottom: 18px;
  padding: 22px 24px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 16px 44px rgba(31, 58, 82, .08);
  color: var(--text);
}

.insight-head {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 18px;
}

.eyebrow {
  display: block;
  margin-bottom: 6px;
  color: #8597a8;
  font-size: 12px;
  font-weight: 700;
}

h2 {
  margin: 0;
  font-size: 22px;
  line-height: 1.25;
  font-weight: 800;
  letter-spacing: 0;
}

p {
  margin: 6px 0 0;
  color: var(--muted);
  font-size: 13px;
}

.head-kpis {
  display: grid;
  grid-template-columns: repeat(4, minmax(72px, 1fr));
  gap: 10px;
  min-width: 420px;

  span {
    min-width: 0;
    padding: 10px 12px;
    border-radius: 8px;
    background: #f8fbfa;
    border: 1px solid #edf3f1;
  }

  em,
  strong {
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
    margin-top: 5px;
    color: var(--text);
    font-size: 20px;
    font-weight: 800;
    font-variant-numeric: tabular-nums;
  }
}

.distribution {
  display: grid;
  grid-template-columns: 210px minmax(0, 1fr);
  gap: 28px;
  align-items: center;
}

.donut-zone {
  display: grid;
  place-items: center;
  min-height: 190px;
}

.donut-chart {
  position: relative;
  display: grid;
  place-items: center;
  width: 188px;
  height: 188px;
  border-radius: 50%;
  box-shadow: 0 18px 36px rgba(31, 58, 82, .12);

  &::after {
    position: absolute;
    inset: 40px;
    content: "";
    border-radius: 50%;
    background: #fff;
    box-shadow: inset 0 0 0 1px #eef3f8;
  }
}

.donut-hole {
  position: relative;
  z-index: 1;
  display: grid;
  place-items: center;
  gap: 6px;

  strong {
    color: #1f4a6e;
    font-size: 32px;
    line-height: 1;
    font-weight: 850;
    font-variant-numeric: tabular-nums;
  }

  em {
    color: #8aa0b3;
    font-size: 12px;
    font-style: normal;
    font-weight: 700;
  }
}

.distribution-list {
  display: grid;
  gap: 12px;
}

.list-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 2px;

  strong {
    color: var(--text);
    font-size: 15px;
  }

  span {
    color: #8597a8;
    font-size: 13px;
  }
}

.dist-row {
  display: grid;
  grid-template-columns: 10px 92px 44px minmax(120px, 1fr);
  gap: 10px;
  align-items: center;
  min-width: 0;

  i {
    width: 10px;
    height: 10px;
    border-radius: 3px;
  }
}

.dist-name {
  overflow: hidden;
  color: #244a63;
  font-size: 14px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dist-row strong {
  color: #1f3a52;
  font-size: 15px;
  font-weight: 850;
  text-align: right;
  font-variant-numeric: tabular-nums;
}

.dist-track {
  overflow: hidden;
  height: 6px;
  border-radius: 999px;
  background: var(--track);

  b {
    display: block;
    height: 100%;
    border-radius: inherit;
    transition: width .45s ease;
  }
}

.chip-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid #eef3f8;
}

.insight-chip {
  padding: 8px 11px;
  border: 1px solid #dde6ef;
  border-radius: 8px;
  background: #fafcfe;
  color: #5f7286;
  font-size: 12px;
  font-weight: 700;

  strong {
    margin-left: 6px;
    color: var(--text);
    font-variant-numeric: tabular-nums;
  }

  &.success { border-color: #c2dbe0; background: #f1f8f9; color: #2f6f73; }
  &.warning { border-color: #e0d8b8; background: #fbf8ef; color: #8a6d2f; }
  &.danger { border-color: #e0c8c8; background: #fbf5f5; color: #9e4034; }
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
