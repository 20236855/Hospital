<template>
  <section class="doctor-insight" :class="`tone-${tone}`">
    <div class="insight-head">
      <div>
        <span class="eyebrow">{{ eyebrow }}</span>
        <h2>{{ title }}</h2>
        <p>{{ subtitle }}</p>
      </div>
      <div class="pulse-ring">
        <span class="ring-orbit"></span>
        <div class="ring-copy">
          <strong>{{ focusValue }}</strong>
          <em>{{ focusLabel }}</em>
        </div>
      </div>
    </div>

    <div class="metric-grid">
      <article v-for="item in metrics" :key="item.label" class="metric-tile">
        <div class="metric-icon" v-if="item.icon">
          <el-icon><component :is="item.icon" /></el-icon>
        </div>
        <div class="metric-copy">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <em :class="item.state || 'neutral'">{{ item.note }}</em>
        </div>
        <div class="metric-bar">
          <i :style="{ width: clampPercent(item.progress) + '%' }"></i>
        </div>
      </article>
    </div>

    <div class="analytics-panel" v-if="metrics.length || lanes.length">
      <div class="chart-card donut-card">
        <div class="chart-title">
          <span>业务闭环</span>
          <strong>{{ focusProgress }}%</strong>
        </div>
        <div class="donut-chart" :style="donutStyle">
          <div class="donut-hole">
            <strong>{{ focusProgress }}%</strong>
            <em>完成指数</em>
          </div>
        </div>
        <div class="donut-legend">
          <span v-for="item in donutLegend" :key="item.label">
            <i></i>{{ item.label }} <strong>{{ item.value }}%</strong>
          </span>
        </div>
      </div>

      <div class="chart-card trend-card">
        <div class="chart-title">
          <span>流程趋势</span>
          <strong>{{ trendSummary }}</strong>
        </div>
        <svg class="line-chart" viewBox="0 0 420 150" preserveAspectRatio="none" aria-hidden="true">
          <defs>
            <linearGradient :id="gradientId" x1="0" x2="0" y1="0" y2="1">
              <stop offset="0%" stop-color="var(--accent)" stop-opacity=".22" />
              <stop offset="100%" stop-color="var(--accent)" stop-opacity="0" />
            </linearGradient>
          </defs>
          <path class="chart-grid" d="M20 32 H400 M20 75 H400 M20 118 H400" />
          <polygon class="chart-area" :points="areaPoints" :fill="`url(#${gradientId})`" />
          <polyline class="chart-line" :points="linePoints" />
          <circle v-for="point in chartDots" :key="point.key" class="chart-dot" :cx="point.x" :cy="point.y" r="4" />
        </svg>
        <div class="trend-axis">
          <span v-for="item in chartLabels" :key="item">{{ item }}</span>
        </div>
      </div>
    </div>

    <div class="insight-bottom" v-if="lanes.length || chips.length">
      <div class="lane-wrap" v-if="lanes.length">
        <div v-for="lane in lanes" :key="lane.label" class="lane-row">
          <div class="lane-meta">
            <span>{{ lane.label }}</span>
            <strong>{{ lane.value }}</strong>
          </div>
          <div class="lane-track">
            <i :style="{ width: clampPercent(lane.progress) + '%' }"></i>
          </div>
        </div>
      </div>
      <div class="chip-wrap" v-if="chips.length">
        <span v-for="chip in chips" :key="chip.label" class="insight-chip" :class="chip.type || 'info'">
          {{ chip.label }} <strong>{{ chip.value }}</strong>
        </span>
      </div>
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
  focusLabel: { type: String, default: '实时' },
  tone: { type: String, default: 'clinical' },
  metrics: { type: Array, default: () => [] },
  lanes: { type: Array, default: () => [] },
  chips: { type: Array, default: () => [] }
})

function clampPercent(value) {
  const n = Number(value)
  if (!Number.isFinite(n)) return 0
  return Math.max(0, Math.min(100, n))
}

const gradientId = `insightGradient-${Math.random().toString(36).slice(2, 9)}`

const laneValues = computed(() => {
  const source = props.lanes.length ? props.lanes : props.metrics
  return source.map(item => ({
    label: item.label,
    value: clampPercent(item.progress)
  }))
})

const focusProgress = computed(() => {
  const valueText = String(props.focusValue ?? '')
  if (valueText.includes('%')) return clampPercent(parseFloat(valueText))
  if (!laneValues.value.length) return 0
  const total = laneValues.value.reduce((sum, item) => sum + item.value, 0)
  return Math.round(total / laneValues.value.length)
})

const donutStyle = computed(() => ({
  '--donut-angle': `${focusProgress.value * 3.6}deg`
}))

const donutLegend = computed(() => laneValues.value.slice(0, 4))

const chartSeries = computed(() => {
  const values = laneValues.value.slice(0, 6)
  if (values.length >= 2) return values
  if (values.length === 1) return [{ label: '起点', value: 0 }, values[0]]
  return [{ label: '起点', value: 0 }, { label: '当前', value: focusProgress.value }]
})

const chartDots = computed(() => {
  const width = 420
  const height = 150
  const padX = 22
  const padY = 22
  const innerW = width - padX * 2
  const innerH = height - padY * 2
  const count = chartSeries.value.length
  return chartSeries.value.map((item, index) => ({
    key: `${item.label}-${index}`,
    x: count === 1 ? width / 2 : padX + (innerW / (count - 1)) * index,
    y: padY + innerH - (clampPercent(item.value) / 100) * innerH,
    label: item.label,
    value: item.value
  }))
})

const linePoints = computed(() => chartDots.value.map(point => `${point.x},${point.y}`).join(' '))
const areaPoints = computed(() => {
  const dots = chartDots.value
  if (!dots.length) return ''
  return `22,128 ${dots.map(point => `${point.x},${point.y}`).join(' ')} 398,128`
})
const chartLabels = computed(() => chartSeries.value.map(item => item.label))
const trendSummary = computed(() => {
  const values = chartSeries.value
  if (!values.length) return '--'
  return `${values[values.length - 1].value}%`
})
</script>

<style scoped lang="scss">
.doctor-insight {
  --accent: #0f8f7a;
  --accent-2: #14b8a6;
  --accent-soft: #e8f7f3;
  --surface: #ffffff;
  --surface-soft: #f6f8f7;
  --line: #e6eeeb;
  --text: #10231f;
  --muted: #6a7c76;
  position: relative;
  overflow: hidden;
  margin-bottom: 22px;
  padding: 22px;
  border: 1px solid var(--line);
  border-radius: 20px;
  background: var(--surface);
  box-shadow: 0 18px 48px rgba(20, 45, 38, .08);
  color: var(--text);
  font-family: Inter, "PingFang SC", "Microsoft YaHei", Arial, sans-serif;

  &::before {
    position: absolute;
    inset: 0;
    pointer-events: none;
    content: "";
    background:
      linear-gradient(180deg, rgba(248, 252, 250, .86), rgba(255, 255, 255, 0) 42%),
      radial-gradient(circle at 92% 12%, rgba(20, 184, 166, .12), transparent 28%);
  }

  > * {
    position: relative;
    z-index: 1;
  }
}

.tone-diagnostic {
  --accent: #0e7490;
  --accent-2: #10b981;
  --accent-soft: #e7f7f8;
}

.tone-lab {
  --accent: #047857;
  --accent-2: #0d9488;
  --accent-soft: #e8f7ef;
}

.tone-risk {
  --accent: #b7791f;
  --accent-2: #10b981;
  --accent-soft: #fff7ed;
}

.insight-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 20px;

  .eyebrow {
    display: inline-flex;
    align-items: center;
    height: 26px;
    padding: 0 10px;
    border: 1px solid rgba(15, 143, 122, .16);
    border-radius: 999px;
    background: var(--accent-soft);
    color: var(--accent);
    font-size: 11px;
    font-weight: 700;
    letter-spacing: 0;
  }

  h2 {
    margin: 10px 0 6px;
    font-size: 24px;
    font-weight: 750;
    line-height: 1.18;
    letter-spacing: 0;
  }

  p {
    margin: 0;
    max-width: 760px;
    color: var(--muted);
    font-size: 13px;
    line-height: 1.7;
  }
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.metric-tile {
  position: relative;
  overflow: hidden;
  min-width: 0;
  min-height: 132px;
  padding: 16px;
  border: 1px solid var(--line);
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff, #fbfdfc);
  box-shadow: 0 10px 26px rgba(26, 52, 45, .055);
  transition: transform .22s ease, border-color .22s ease, box-shadow .22s ease;

  &:hover {
    border-color: rgba(15, 143, 122, .28);
    box-shadow: 0 18px 38px rgba(26, 52, 45, .1);
    transform: translateY(-3px);
  }

  &::after {
    position: absolute;
    top: 0;
    right: 0;
    width: 88px;
    height: 88px;
    border-radius: 0 18px 0 88px;
    content: "";
    background: var(--accent-soft);
    opacity: .52;
  }
}

.metric-icon {
  position: relative;
  z-index: 1;
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  margin-bottom: 14px;
  border: 1px solid rgba(15, 143, 122, .12);
  border-radius: 14px;
  background: var(--accent-soft);
  color: var(--accent);
  font-size: 18px;
}

.metric-copy {
  position: relative;
  z-index: 1;
  display: grid;
  gap: 5px;

  span {
    color: var(--muted);
    font-size: 12px;
    font-weight: 650;
  }

  strong {
    overflow: hidden;
    color: var(--text);
    font-size: 28px;
    font-weight: 760;
    font-variant-numeric: tabular-nums;
    line-height: 1.1;
    letter-spacing: 0;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  em {
    min-height: 18px;
    color: #7a8984;
    font-size: 12px;
    font-style: normal;
  }

  .good { color: #059669; }
  .warn { color: #d97706; }
  .risk { color: #dc2626; }
}

.metric-bar,
.lane-track {
  overflow: hidden;
  height: 6px;
  margin-top: 14px;
  border-radius: 999px;
  background: #edf3f1;

  i {
    display: block;
    height: 100%;
    border-radius: inherit;
    background: linear-gradient(90deg, var(--accent), var(--accent-2));
    transition: width .6s ease;
  }
}

.analytics-panel {
  display: grid;
  grid-template-columns: minmax(280px, .82fr) minmax(0, 1.18fr);
  gap: 14px;
  margin-top: 14px;
}

.chart-card {
  min-width: 0;
  padding: 18px;
  border: 1px solid var(--line);
  border-radius: 20px;
  background: linear-gradient(180deg, #ffffff, #fbfdfc);
  box-shadow: 0 12px 30px rgba(26, 52, 45, .055);
}

.chart-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;

  span {
    color: var(--muted);
    font-size: 13px;
    font-weight: 700;
  }

  strong {
    color: var(--text);
    font-size: 18px;
    font-weight: 780;
    font-variant-numeric: tabular-nums;
  }
}

.donut-card {
  display: grid;
  grid-template-columns: 132px minmax(0, 1fr);
  align-items: center;
  column-gap: 18px;

  .chart-title {
    grid-column: 1 / -1;
  }
}

.donut-chart {
  position: relative;
  display: grid;
  place-items: center;
  width: 132px;
  height: 132px;
  border-radius: 50%;
  background:
    conic-gradient(var(--accent) 0deg, var(--accent-2) var(--donut-angle), #edf4f2 var(--donut-angle), #edf4f2 360deg);
  box-shadow: inset 0 0 0 1px rgba(15, 143, 122, .08), 0 14px 28px rgba(26, 52, 45, .08);

  &::after {
    position: absolute;
    inset: 13px;
    border-radius: 50%;
    content: "";
    background: #fff;
    box-shadow: inset 0 0 0 1px var(--line);
  }
}

.donut-hole {
  position: relative;
  z-index: 1;
  display: grid;
  place-items: center;
  gap: 3px;

  strong {
    color: var(--text);
    font-size: 24px;
    font-weight: 800;
    font-variant-numeric: tabular-nums;
    line-height: 1;
  }

  em {
    color: var(--muted);
    font-size: 11px;
    font-style: normal;
    font-weight: 700;
  }
}

.donut-legend {
  display: grid;
  gap: 10px;
  min-width: 0;

  span {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 10px;
    min-width: 0;
    color: var(--muted);
    font-size: 12px;
    font-weight: 650;
  }

  i {
    flex: 0 0 8px;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: var(--accent);
    box-shadow: 0 0 0 4px var(--accent-soft);
  }

  strong {
    margin-left: auto;
    color: var(--text);
    font-variant-numeric: tabular-nums;
  }
}

.trend-card {
  display: grid;
  grid-template-rows: auto minmax(150px, 1fr) auto;
}

.line-chart {
  width: 100%;
  height: 150px;
}

.chart-grid {
  fill: none;
  stroke: #edf3f1;
  stroke-width: 1;
}

.chart-area {
  opacity: 1;
}

.chart-line {
  fill: none;
  stroke: var(--accent);
  stroke-linecap: round;
  stroke-linejoin: round;
  stroke-width: 4;
  filter: drop-shadow(0 8px 10px rgba(15, 143, 122, .18));
}

.chart-dot {
  fill: #fff;
  stroke: var(--accent-2);
  stroke-width: 3;
}

.trend-axis {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(56px, 1fr));
  gap: 8px;
  color: var(--muted);
  font-size: 11px;
  font-weight: 650;

  span {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.insight-bottom {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(260px, .8fr);
  gap: 14px;
  margin-top: 14px;
}

.lane-wrap {
  display: grid;
  gap: 12px;
  padding: 16px;
  border: 1px solid var(--line);
  border-radius: 18px;
  background: var(--surface-soft);
}

.lane-row {
  display: grid;
  grid-template-columns: 132px minmax(0, 1fr);
  align-items: center;
  gap: 14px;
}

.lane-meta {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  color: var(--muted);
  font-size: 12px;
  font-weight: 650;

  strong {
    color: var(--text);
    font-variant-numeric: tabular-nums;
  }
}

.chip-wrap {
  display: flex;
  align-content: flex-start;
  flex-wrap: wrap;
  gap: 10px;
  padding: 16px;
  border: 1px solid var(--line);
  border-radius: 18px;
  background: var(--surface-soft);
}

.insight-chip {
  padding: 9px 12px;
  border: 1px solid #dce8e4;
  border-radius: 999px;
  background: #fff;
  color: var(--muted);
  font-size: 12px;
  font-weight: 650;

  strong {
    margin-left: 6px;
    color: var(--text);
    font-variant-numeric: tabular-nums;
  }

  &.success { border-color: #bdebd7; background: #effaf5; color: #047857; }
  &.warning { border-color: #f5dfaa; background: #fffaf0; color: #9a6b16; }
  &.danger { border-color: #f3c8c8; background: #fff5f5; color: #b42318; }
}

.pulse-ring {
  position: relative;
  display: grid;
  place-items: center;
  flex: 0 0 104px;
  width: 104px;
  height: 104px;
  border: 1px solid var(--line);
  border-radius: 28px;
  background: #fff;
  box-shadow: inset 0 0 0 8px #f4faf8, 0 12px 28px rgba(26, 52, 45, .08);

  .ring-orbit {
    position: absolute;
    inset: 12px;
    border-radius: 24px;
    background: conic-gradient(from 180deg, var(--accent), var(--accent-2), #dff3ef, var(--accent));
    mask: radial-gradient(circle, transparent 56%, #000 58%);
    animation: spin 8s linear infinite;
    opacity: .92;
  }

  .ring-copy {
    position: relative;
    z-index: 1;
    display: grid;
    place-items: center;
    gap: 4px;
    min-width: 0;
    text-align: center;
  }

  strong {
    max-width: 78px;
    overflow: hidden;
    color: var(--text);
    font-size: 22px;
    font-weight: 780;
    font-variant-numeric: tabular-nums;
    line-height: 1;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  em {
    margin-top: 4px;
    color: var(--muted);
    font-size: 11px;
    font-style: normal;
    font-weight: 650;
  }
}

@media (max-width: 1180px) {
  .metric-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .analytics-panel { grid-template-columns: 1fr; }
  .insight-bottom { grid-template-columns: 1fr; }
}

@media (max-width: 640px) {
  .insight-head { align-items: flex-start; flex-direction: column; }
  .doctor-insight { padding: 16px; border-radius: 18px; }
  .pulse-ring { width: 88px; height: 88px; border-radius: 24px; }
  .metric-grid { grid-template-columns: 1fr; }
  .donut-card { grid-template-columns: 1fr; }
  .donut-chart { margin: 0 auto; }
  .lane-row { grid-template-columns: 1fr; }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
