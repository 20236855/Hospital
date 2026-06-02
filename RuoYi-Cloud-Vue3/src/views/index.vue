<template>
  <div class="his-home">
    <section class="home-header">
      <div class="header-copy">
        <div class="eyebrow">
          <el-icon><OfficeBuilding /></el-icon>
          门诊一体化运营工作台
        </div>
        <h1>今日门诊全业务运行概览</h1>
        <p>聚合挂号、诊疗、医技检查、收费与药房发药关键节点，辅助门诊大厅实时调度。</p>
      </div>
      <div class="header-status">
        <div class="status-card">
          <span>当前班次</span>
          <strong>日间门诊</strong>
        </div>
        <div class="status-card">
          <span>系统状态</span>
          <strong>运行平稳</strong>
        </div>
        <div class="status-card">
          <span>刷新时间</span>
          <strong>{{ currentTime }}</strong>
        </div>
      </div>
    </section>

    <section class="metric-grid">
      <article v-for="item in coreMetrics" :key="item.label" class="metric-card">
        <div class="metric-icon" :class="item.tone">
          <el-icon><component :is="item.icon" /></el-icon>
        </div>
        <div class="metric-content">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <small>{{ item.trend }}</small>
        </div>
      </article>
    </section>

    <section class="dashboard-layout">
      <div class="panel business-flow">
        <div class="panel-head">
          <div>
            <span class="panel-kicker">业务闭环</span>
            <h2>门诊流程</h2>
          </div>
          <el-tag type="primary" effect="plain">实时队列</el-tag>
        </div>
        <div class="flow-list compact">
          <div v-for="step in outpatientFlow" :key="step.name" class="flow-item compact">
            <div class="flow-marker" :class="step.tone">
              <el-icon><component :is="step.icon" /></el-icon>
            </div>
            <div class="flow-info">
              <div class="flow-title">
                <strong>{{ step.name }}</strong>
                <span>{{ step.count }}</span>
              </div>
              <el-progress :percentage="step.progress" :stroke-width="6" :show-text="false" />
              <p>{{ step.note }}</p>
            </div>
          </div>
        </div>
      </div>

      <div class="panel queue-panel">
        <div class="panel-head">
          <div>
            <span class="panel-kicker">诊区动态</span>
            <h2>候诊与在诊</h2>
          </div>
          <el-icon class="panel-head-icon"><DataLine /></el-icon>
        </div>
        <div class="queue-summary">
          <div>
            <span>平均候诊</span>
            <strong>18 min</strong>
          </div>
          <div>
            <span>医生接诊率</span>
            <strong>86%</strong>
          </div>
        </div>
        <div class="dept-list">
          <div v-for="dept in departments" :key="dept.name" class="dept-row">
            <div>
              <strong>{{ dept.name }}</strong>
              <span>{{ dept.doctor }} 位医生出诊</span>
            </div>
            <div class="dept-count">
              <strong>{{ dept.waiting }}</strong>
              <span>候诊</span>
            </div>
          </div>
        </div>
      </div>

      <div class="panel finance-panel">
        <div class="panel-head">
          <div>
            <span class="panel-kicker">收费与药房</span>
            <h2>结算发药状态</h2>
          </div>
          <el-icon class="panel-head-icon"><Coin /></el-icon>
        </div>
        <div class="finance-grid">
          <div v-for="item in financeStats" :key="item.label">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
            <small>{{ item.note }}</small>
          </div>
        </div>
        <div class="pharmacy-line">
          <div>
            <span>待发药处方</span>
            <strong>72</strong>
          </div>
          <el-progress :percentage="68" :stroke-width="10" :show-text="false" />
        </div>
      </div>

      <div class="panel task-panel">
        <div class="panel-head">
          <div>
            <span class="panel-kicker">待办提醒</span>
            <h2>需要处理的单据</h2>
          </div>
          <el-icon class="panel-head-icon"><BellFilled /></el-icon>
        </div>
        <div class="task-list">
          <div v-for="task in pendingTasks" :key="task.title" class="task-row">
            <div class="task-badge" :class="task.tone">
              <el-icon><component :is="task.icon" /></el-icon>
            </div>
            <div>
              <strong>{{ task.title }}</strong>
              <span>{{ task.desc }}</span>
            </div>
            <em>{{ task.count }}</em>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup name="Index">
import {
  BellFilled,
  Coin,
  DataLine,
  DocumentChecked,
  FirstAidKit,
  Histogram,
  OfficeBuilding,
  Operation,
  Tickets,
  UserFilled,
  Van,
  WarningFilled
} from '@element-plus/icons-vue'

const currentTime = computed(() => {
  const now = new Date()
  return now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
})

const coreMetrics = [
  { label: '今日挂号', value: '1,286', trend: '较昨日 +8.6%', icon: Tickets, tone: 'blue' },
  { label: '在诊人数', value: '146', trend: '高峰诊区 3 个', icon: UserFilled, tone: 'cyan' },
  { label: '待检查单据', value: '93', trend: '超 30 分钟 11 张', icon: DocumentChecked, tone: 'teal' },
  { label: '待发药处方', value: '72', trend: '平均等待 9 分钟', icon: FirstAidKit, tone: 'navy' }
]

const outpatientFlow = [
  { name: '挂号分诊', count: '312 人排队', progress: 76, note: '自助机与窗口分流正常', icon: Tickets, tone: 'blue' },
  { name: '医生诊疗', count: '146 人在诊', progress: 68, note: '内科诊区接诊压力偏高', icon: Operation, tone: 'cyan' },
  { name: '医技检查', count: '93 张待检', progress: 54, note: '检验、超声队列需关注', icon: Histogram, tone: 'teal' }
]

const departments = [
  { name: '普通内科', doctor: 8, waiting: 42 },
  { name: '儿科门诊', doctor: 5, waiting: 31 },
  { name: '骨科门诊', doctor: 4, waiting: 18 },
  { name: '妇产科', doctor: 6, waiting: 24 }
]

const pendingTasks = [
  { title: '待检查申请', desc: '检验、影像、超声未执行', count: '93', icon: DocumentChecked, tone: 'blue' },
  { title: '异常候诊提醒', desc: '超过目标候诊时间', count: '11', icon: WarningFilled, tone: 'amber' },
  { title: '待结算费用', desc: '诊间支付与窗口缴费', count: '218', icon: Coin, tone: 'cyan' },
  { title: '转运协同', desc: '需导诊协助患者', count: '6', icon: Van, tone: 'teal' }
]

const financeStats = [
  { label: '今日收费', value: '¥486,920', note: '门诊收入' },
  { label: '退费审核', value: '12', note: '待财务确认' },
  { label: '医保结算', value: '74%', note: '占总交易' },
  { label: '处方完成', value: '91%', note: '发药完成率' }
]
</script>

<style scoped lang="scss">
.his-home {
  min-height: 100%;
  padding: 24px;
  background:
    linear-gradient(180deg, rgba(239, 248, 252, 0.92) 0%, rgba(247, 251, 253, 0) 240px),
    #f6fafc;
  color: #18304a;
}

.home-header {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 24px;
  align-items: stretch;
  margin-bottom: 20px;
  padding: 28px;
  border: 1px solid rgba(118, 166, 190, 0.24);
  border-radius: 8px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(228, 244, 250, 0.86)),
    #ffffff;
  box-shadow: 0 14px 36px rgba(32, 75, 105, 0.08);
}

.header-copy {
  .eyebrow {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    color: #0c6f91;
    font-size: 13px;
    font-weight: 700;
  }

  h1 {
    margin: 12px 0 10px;
    color: #113a5c;
    font-size: 28px;
    line-height: 1.25;
    font-weight: 700;
  }

  p {
    margin: 0;
    max-width: 680px;
    color: #607487;
    font-size: 14px;
    line-height: 1.7;
  }
}

.header-status {
  display: grid;
  grid-template-columns: repeat(3, 132px);
  gap: 12px;
}

.status-card {
  padding: 16px;
  border: 1px solid rgba(111, 167, 190, 0.2);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.78);

  span {
    display: block;
    color: #6b7f91;
    font-size: 12px;
  }

  strong {
    display: block;
    margin-top: 10px;
    color: #12395b;
    font-size: 17px;
  }
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.metric-card,
.panel {
  border: 1px solid rgba(121, 162, 184, 0.22);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 10px 28px rgba(23, 67, 98, 0.07);
}

.metric-card {
  display: flex;
  gap: 14px;
  align-items: center;
  min-height: 116px;
  padding: 18px;
}

.metric-icon,
.flow-marker,
.task-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #ffffff;
  border-radius: 8px;

  &.blue {
    background: #2e77a8;
  }

  &.cyan {
    background: #2c8fa6;
  }

  &.teal {
    background: #3d9a9a;
  }

  &.navy {
    background: #173f66;
  }

  &.amber {
    background: #b7893f;
  }
}

.metric-icon {
  width: 46px;
  height: 46px;
  font-size: 24px;
}

.metric-content {
  min-width: 0;

  span,
  small {
    display: block;
    color: #65798d;
    font-size: 13px;
  }

  strong {
    display: block;
    margin: 7px 0 5px;
    color: #102f4d;
    font-size: 28px;
    line-height: 1;
  }
}

.dashboard-layout {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.panel {
  padding: 20px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;

  h2 {
    margin: 5px 0 0;
    color: #12395b;
    font-size: 18px;
    font-weight: 700;
  }
}

.panel-kicker {
  color: #7b8fa1;
  font-size: 12px;
  font-weight: 700;
}

.panel-head-icon {
  color: #2e77a8;
  font-size: 24px;
}

.flow-list {
  display: grid;
  gap: 18px;

  &.compact {
    gap: 14px;
  }
}

.flow-item {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  gap: 14px;

  &.compact {
    gap: 12px;

    .flow-marker {
      width: 36px;
      height: 36px;
      font-size: 18px;
    }

    .flow-info p {
      display: none;
    }
  }
}

.flow-marker {
  width: 42px;
  height: 42px;
  font-size: 22px;
}

.flow-title {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 9px;
  color: #173a59;

  span {
    color: #2b6f95;
    font-weight: 700;
    font-size: 13px;
  }
}

.flow-info {
  strong {
    font-size: 14px;
  }

  p {
    margin: 8px 0 0;
    color: #6a7f92;
    font-size: 13px;
  }
}

.queue-summary {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;

  div {
    padding: 14px;
    border-radius: 8px;
    background: #edf7fb;
  }

  span,
  strong {
    display: block;
  }

  span {
    color: #6c8293;
    font-size: 12px;
  }

  strong {
    margin-top: 7px;
    color: #133d5f;
    font-size: 22px;
  }
}

.dept-list,
.task-list {
  display: grid;
  gap: 10px;
}

.dept-row,
.task-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 13px 0;
  border-top: 1px solid #e7eff3;
}

.dept-row {
  strong,
  span {
    display: block;
  }

  span {
    margin-top: 4px;
    color: #708395;
    font-size: 12px;
  }
}

.dept-count {
  text-align: right;

  strong {
    color: #0d5f87;
    font-size: 22px;
  }

  span {
    font-size: 11px;
    color: #9baab8;
  }
}

.task-row {
  justify-content: flex-start;

  strong,
  span {
    display: block;
  }

  span {
    margin-top: 4px;
    color: #758897;
    font-size: 12px;
  }

  em {
    margin-left: auto;
    color: #14395b;
    font-size: 22px;
    font-style: normal;
    font-weight: 700;
  }
}

.task-badge {
  width: 36px;
  height: 36px;
  font-size: 18px;
}

.finance-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;

  div {
    padding: 14px;
    border: 1px solid #e3eef3;
    border-radius: 8px;
    background: #fbfdfe;
  }

  span,
  strong,
  small {
    display: block;
  }

  span,
  small {
    color: #6f8394;
    font-size: 12px;
  }

  strong {
    margin: 8px 0 5px;
    color: #133b5e;
    font-size: 20px;
  }
}

.pharmacy-line {
  padding: 16px;
  border-radius: 8px;
  background: #eef8f9;

  div {
    display: flex;
    justify-content: space-between;
    margin-bottom: 10px;
    color: #14395b;
  }

  strong {
    font-size: 22px;
  }
}

:deep(.el-progress-bar__outer) {
  background-color: #e2edf3;
}

:deep(.el-progress-bar__inner) {
  background: linear-gradient(90deg, #2e77a8, #3d9a9a);
}

:deep(.el-tag.el-tag--primary) {
  --el-tag-text-color: #0b6688;
  --el-tag-border-color: #bcdce7;
  --el-tag-bg-color: #eef8fb;
}

@media (max-width: 1200px) {
  .home-header,
  .dashboard-layout {
    grid-template-columns: 1fr;
  }

  .header-status {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .his-home {
    padding: 16px;
  }

  .metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .header-status,
  .finance-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 560px) {
  .metric-grid {
    grid-template-columns: 1fr;
  }

  .home-header {
    padding: 20px;
  }

  .header-copy h1 {
    font-size: 23px;
  }

  .dashboard-layout {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>