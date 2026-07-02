<template>
  <div class="his-home">
    <!-- 固定视频背景 -->
    <video class="home-video-bg" autoplay loop muted playsinline>
      <source src="/running.mp4" type="video/mp4" />
    </video>
    <div class="home-video-overlay"></div>

    <!-- Hero Section -->
    <section class="home-hero">
      <div class="hero-content">
        <h2 class="hero-heading">
          <span v-for="(word, i) in heroWords" :key="i"
            class="hero-word" :style="{ animationDelay: (0.15 + i * 0.08) + 's' }">{{ word }}</span>
        </h2>
        <p class="hero-sub">云医智联 — 用心守护每一次就医</p>
        <div class="hero-scroll-hint"><span></span></div>
      </div>
    </section>

    <!-- 医疗资源展示区 -->
    <section class="home-resources">
      <div class="resources-header">
        <h2><i v-html="svgResources"></i>医院资源展示</h2>
        <span class="resources-badge">临床实力 · 设施保障</span>
      </div>
      <div class="resources-grid">
        <!-- 重点科室 -->
        <div class="res-card dept-card">
          <h3><i v-html="svgDept"></i>重点科室</h3>
          <el-carousel class="dept-carousel" height="260px" indicator-position="outside" arrow="hover" :interval="3000">
            <el-carousel-item v-for="dept in departmentSlides" :key="dept.name">
              <div class="dept-slide">
                <img class="dept-image" :src="dept.image" :alt="dept.name" />
                <div class="dept-shade"></div>
                <div class="dept-content">
                  <span class="dept-label">{{ dept.label }}</span>
                  <h4>{{ dept.name }}</h4>
                  <p>{{ dept.desc }}</p>
                  <div class="dept-metrics">
                    <span><strong>{{ dept.doctors }}</strong> 专家团队</span>
                    <span><strong>{{ dept.beds }}</strong> 开放床位</span>
                  </div>
                </div>
              </div>
            </el-carousel-item>
          </el-carousel>
          <div class="card-intro">
            <p>我院临床科室设置齐全，拥有<b>国家级重点专科4个</b>、<b>省级重点专科12个</b>，涵盖内科、外科、妇产科、儿科、神经科、肿瘤科、耳鼻喉科等全领域学科集群。依托<b>多学科联合诊疗（MDT）</b>模式，打破传统单科诊疗局限，汇聚各领域专家智慧，为患者提供精准、高效的一站式诊疗方案。近年来，我院重点推进<b>胸痛中心、卒中中心、创伤中心</b>等五大中心建设，急诊救治能力大幅提升，<b>急危重症抢救成功率</b>持续保持在区域内领先水平。</p>
          </div>
        </div>

        <!-- 大型设备 -->
        <div class="res-card equip-card">
          <h3><i v-html="svgEquip"></i>大型设备</h3>
          <el-carousel class="equip-carousel" height="260px" indicator-position="outside" arrow="hover" :interval="3000">
            <el-carousel-item v-for="eq in equipmentSlides" :key="eq.name">
              <div class="dept-slide">
                <img class="dept-image" :src="eq.image" :alt="eq.name" />
                <div class="dept-shade"></div>
                <div class="dept-content">
                  <span class="dept-label">{{ eq.label }}</span>
                  <h4>{{ eq.name }}</h4>
                  <p>{{ eq.desc }}</p>
                </div>
              </div>
            </el-carousel-item>
          </el-carousel>
          <div class="card-intro">
            <p>医院配备国际领先的医疗设备体系，拥有<b>西门子、GE、飞利浦</b>等进口高端影像及治疗设备近百台，涵盖<b>3.0T MRI、256排CT、DSA数字减影血管造影、达芬奇手术机器人、PET-CT、直线加速器</b>等国际前沿装备。其中3.0T MRI支持全身高清弥散与波谱分析，256排CT实现亚毫米精度成像与冠脉CTA快速分诊，达芬奇Xi手术机器人可完成泌尿、妇科、普外科等多领域微创精准操作。强大的硬件支撑体系为<b>临床精准诊断与微创治疗</b>奠定了坚实基础。</p>
          </div>
        </div>

        <!-- 教学科研人才 -->
        <div class="res-card teach-card">
          <h3><i v-html="svgTeach"></i>教学科研人才</h3>
          <el-carousel class="teach-carousel" height="260px" indicator-position="outside" arrow="hover" :interval="3000">
            <el-carousel-item v-for="t in teachingSlides" :key="t.name">
              <div class="dept-slide">
                <img class="dept-image" :src="t.image" :alt="t.name" />
                <div class="dept-shade"></div>
                <div class="dept-content">
                  <span class="dept-label">{{ t.label }}</span>
                  <h4>{{ t.name }}</h4>
                  <p>{{ t.desc }}</p>
                  <div class="dept-metrics">
                    <span><strong>{{ t.count1 }}</strong> {{ t.meta1 }}</span>
                    <span><strong>{{ t.count2 }}</strong> {{ t.meta2 }}</span>
                  </div>
                </div>
              </div>
            </el-carousel-item>
          </el-carousel>
          <div class="card-intro">
            <p>作为多所知名医学院校附属医院和<b>国家级住院医师规范化培训基地</b>，我院汇聚<b>硕博导师48名</b>，<b>在培学员320余人</b>，年均接收临床实习生及进修医师逾600人次。建有<b>省部级重点实验室3个</b>、<b>临床研究中心5个</b>，聚焦肿瘤精准诊疗、心血管疾病防治、神经科学前沿及再生医学等方向，<b>在研项目156项</b>，年发表<b>SCI论文近百篇</b>。与<b>哈佛医学院、东京大学</b>等<b>26家国际顶尖机构</b>建立深度合作关系，定期举办国际学术会议与专家互访项目，持续推动临床与科研深度融合。</p>
          </div>
        </div>

        <!-- 质量趋势 — 轮播 -->
        <div class="res-card quality-card">
          <h3><i v-html="svgQuality"></i>质量趋势</h3>
          <el-carousel class="quality-carousel" height="260px" indicator-position="outside" arrow="hover" :interval="3000">
            <el-carousel-item v-for="q in qualitySlides" :key="q.name">
              <div class="dept-slide">
                <img class="dept-image" :src="q.image" :alt="q.name" />
                <div class="dept-shade"></div>
                <div class="dept-content">
                  <span class="dept-label">{{ q.label }}</span>
                  <h4>{{ q.name }}</h4>
                  <p>{{ q.desc }}</p>
                  <div class="dept-metrics">
                    <span><strong>{{ q.count1 }}</strong> {{ q.meta1 }}</span>
                    <span><strong>{{ q.count2 }}</strong> {{ q.meta2 }}</span>
                  </div>
                </div>
              </div>
            </el-carousel-item>
          </el-carousel>
          <div class="card-intro">
            <p>我院建立全流程医疗质量管理体系，围绕<b>诊断质量、治疗效果、患者体验</b>三大核心维度，实施院科组<b>三级质控与PDCA持续改进</b>机制。临床与病理<b>诊断符合率达98.6%</b>，住院患者<b>治愈好转率达96.2%</b>，<b>患者综合满意度达97.8分</b>，各项质量指标稳步提升。依托电子病历系统和临床路径管理，实现诊疗行为标准化与可追溯，综合医疗质量在区域同级医院中长期保持领先地位，<b>连续五年获评省级医疗质量管理先进单位</b>。</p>
          </div>
        </div>
      </div>

      <!-- ==================== 数据运营中心看板 ==================== -->
      <section class="data-dashboard">
        <div class="dash-header">
          <h2><i v-html="svgChart"></i>数据运营中心</h2>
          <span class="dash-badge">实时更新 · 智能分析</span>
        </div>

        <!-- 第一行：门诊流量面积图（宽） | 手术分类横向柱状图（窄） -->
        <div class="dash-row dash-row-flow">
          <!-- 门诊流量趋势 — 面积折线图 -->
          <div class="dash-card dash-main">
            <div class="dash-head">
              <div class="dash-title-group">
                <h4>门诊流量趋势</h4>
                <span class="dash-period">近12个月数据统计</span>
              </div>
              <div class="dash-tabs">
                <span :class="{ active: outpatientTab === 'day' }" @click="outpatientTab = '日'">按日</span>
                <span :class="{ active: outpatientTab === 'week' }" @click="outpatientTab = '周'">按周</span>
                <span :class="{ active: outpatientTab === 'month' }" @click="outpatientTab = '月'">按月</span>
              </div>
            </div>
            <div class="dash-body">
              <svg viewBox="0 0 720 280" preserveAspectRatio="xMidYMid meet" class="area-svg">
                <defs>
                  <linearGradient id="areaG1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="rgb(249,115,22)" stop-opacity=".32"/><stop offset="100%" stop-color="rgb(249,115,22)" stop-opacity=".02"/></linearGradient>
                  <linearGradient id="areaG2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="rgb(59,130,246)" stop-opacity=".26"/><stop offset="100%" stop-color="rgb(59,130,246)" stop-opacity=".02"/></linearGradient>
                  <linearGradient id="areaG3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="rgb(34,197,94)" stop-opacity=".22"/><stop offset="100%" stop-color="rgb(34,197,94)" stop-opacity=".02"/></linearGradient>
                </defs>
                <!-- 网格 -->
                <g stroke="#e2eeea" stroke-width="0.6" opacity=".8">
                  <line x1="56" y1="20" x2="708" y2="20"/><line x1="56" y1="64" x2="708" y2="64"/>
                  <line x1="56" y1="108" x2="708" y2="108"/><line x1="56" y1="152" x2="708" y2="152"/>
                  <line x1="56" y1="196" x2="708" y2="196"/><line x1="56" y1="240" x2="708" y2="240"/>
                </g>
                <!-- Y轴 -->
                <g font-size="11" fill="#7ba89e" text-anchor="end" font-weight="500">
                  <text x="48" y="25">7000</text><text x="48" y="69">6000</text>
                  <text x="48" y="113">5000</text><text x="48"y="157">4000</text>
                  <text x="48" y="201">3000</text><text x="48" y="245">2000</text>
                </g>
                <!-- X轴 -->
                <g font-size="11" fill="#7ba89e" text-anchor="middle" font-weight="500">
                  <text x="76" y="264">1月</text><text x="132" y="264">2月</text><text x="188" y="264">3月</text>
                  <text x="244" y="264">4月</text><text x="300" y="264">5月</text><text x="356" y="264">6月</text>
                  <text x="412" y="264">7月</text><text x="468" y="264">8月</text><text x="524" y="264">9月</text>
                  <text x="580" y="264">10月</text><text x="636" y="264">11月</text><text x="692" y="264">12月</text>
                </g>
                <!-- 面积填充 - 门诊总量 -->
                <polygon fill="url(#areaG1)" points="
                  76,188 132,164 188,140 244,152 300,120 356,96 412,72 468,88 524,56 580,68 636,40 692,32 692,240 76,240"/>
                <!-- 面积填充 - 急诊 -->
                <polygon fill="url(#areaG2)" points="
                  76,216 132,200 188,184 244,192 300,168 356,148 412,128 468,140 524,116 580,124 636,104 692,92 692,240 76,240"/>
                <!-- 面积填充 - 专家号 -->
                <polygon fill="url(#areaG3)" points="
                  76,228 132,218 188,204 244,212 300,192 356,176 412,160 468,172 524,148 580,156 636,136 692,124 692,240 76,240"/>
                <!-- 折线 - 门诊总量 -->
                <polyline fill="none" stroke="#f97316" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"
                  points="76,188 132,164 188,140 244,152 300,120 356,96 412,72 468,88 524,56 580,68 636,40 692,32"/>
                <!-- 折线 - 急诊 -->
                <polyline fill="none" stroke="#3b82f6" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
                  points="76,216 132,200 188,184 244,192 300,168 356,148 412,128 468,140 524,116 580,124 636,104 692,92"/>
                <!-- 折线 - 专家号 -->
                <polyline fill="none" stroke="#22c55e" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"
                  points="76,228 132,218 188,204 244,212 300,192 356,176 412,160 468,172 524,148 580,156 636,136 692,124"/>
                <!-- 数据点 - 门诊总量 -->
                <g fill="#f97316">
                  <circle cx="76" cy="188" r="3"/><circle cx="132" cy="164" r="3"/><circle cx="188" cy="140" r="3"/>
                  <circle cx="244" cy="152" r="3"/><circle cx="300" cy="120" r="3"/><circle cx="356" cy="96" r="3"/>
                  <circle cx="412" cy="72" r="3"/><circle cx="468" cy="88" r="3"/><circle cx="524" cy="56" r="3"/>
                  <circle cx="580" cy="68" r="3"/><circle cx="636" cy="40" r="3"/><circle cx="692" cy="32" r="4.5"/>
                  <circle cx="692" cy="32" r="9" fill="#f97316" opacity=".18"/>
                </g>
                <text x="692" y="22" font-size="13" fill="#c2410c" font-weight="800" text-anchor="middle">6,847</text>
                <!-- 数据点 - 急诊 -->
                <g fill="#3b82f6">
                  <circle cx="76" cy="216" r="2.5"/><circle cx="132" cy="200" r="2.5"/><circle cx="188" cy="184" r="2.5"/>
                  <circle cx="244" cy="192" r="2.5"/><circle cx="300" cy="168" r="2.5"/><circle cx="356" cy="148" r="2.5"/>
                  <circle cx="412" cy="128" r="2.5"/><circle cx="468" cy="140" r="2.5"/><circle cx="524" cy="116" r="2.5"/>
                  <circle cx="580" cy="124" r="2.5"/><circle cx="636" cy="104" r="2.5"/><circle cx="692" cy="92" r="4"/>
                </g>
                <text x="692" y="84" font-size="11" fill="#1d4ed8" font-weight="700" text-anchor="middle">4,120</text>
                <!-- 数据点 - 专家号 -->
                <g fill="#22c55e">
                  <circle cx="76" cy="228" r="2.5"/><circle cx="132" cy="218" r="2.5"/><circle cx="188" cy="204" r="2.5"/>
                  <circle cx="244" cy="212" r="2.5"/><circle cx="300" cy="192" r="2.5"/><circle cx="356" cy="176" r="2.5"/>
                  <circle cx="412" cy="160" r="2.5"/><circle cx="468" cy="172" r="2.5"/><circle cx="524" cy="148" r="2.5"/>
                  <circle cx="580" cy="156" r="2.5"/><circle cx="636" cy="136" r="2.5"/><circle cx="692" cy="124" r="4"/>
                </g>
                <text x="692" y="118" font-size="11" fill="#15803d" font-weight="700" text-anchor="middle">2,956</text>
              </svg>
              <div class="area-legend">
                <span class="al-item"><em style="background:#f97316"></em>门急诊总量</span>
                <span class="al-item"><em style="background:#3b82f6"></em>急诊人次</span>
                <span class="al-item"><em style="background:#22c55e"></em>专家门诊</span>
              </div>
              <div class="area-summary">
                <div class="as-col">
                  <div class="as-num" style="color:#f97316">186.4<small>万</small></div>
                  <div class="as-label">年门急诊总量</div>
                </div>
                <div class="as-divider"></div>
                <div class="as-col">
                  <div class="as-num" style="color:#f97316">+12.6<small>%</small></div>
                  <div class="as-label">同比增幅</div>
                </div>
                <div class="as-divider"></div>
                <div class="as-col">
                  <div class="as-num" style="color:#3b82f6">5,127</div>
                  <div class="as-label">日均接诊</div>
                </div>
                <div class="as-divider"></div>
                <div class="as-col">
                  <div class="as-num" style="color:#22c55e">98.2<small>%</small></div>
                  <div class="as-label">预约就诊率</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 手术分类统计 — 横向柱状图 -->
          <div class="dash-card dash-side">
            <div class="dash-head">
              <h4>手术分类统计</h4>
              <span class="dash-unit">年度数据</span>
            </div>
            <div class="dash-body">
              <svg viewBox="0 0 320 260" preserveAspectRatio="xMidYMid meet" class="h-bar-svg">
                <g v-for="(item, idx) in surgeryData" :key="item.name">
                  <text :x="2" :y="22 + idx * 40" font-size="12.5" fill="#2d5a4e" font-weight="600">{{ item.name }}</text>
                  <text :x="2" :y="37 + idx * 40" font-size="10" fill="#8ba8a0">{{ item.sub }} · {{ item.count }}台</text>
                  <rect :y="14 + idx * 40" x="110" :width="item.pct * 195" height="20" rx="5"
                    :fill="item.color" opacity=".6" />
                  <text :x="310 + item.pct * 195" :y="29 + idx * 40" font-size="13"
                    :fill="item.color" font-weight="800" text-anchor="end">{{ item.value }}</text>
                </g>
              </svg>
              <div class="surgery-total">
                <strong>48,260</strong><span>台/年</span>
                <small>较去年增长 <em>+8.3%</em></small>
              </div>
            </div>
          </div>
        </div>

        <!-- 第二行：科室分布环形图（左）| 运营KPI仪表盘（右） -->
        <div class="dash-row">
          <!-- 科室分布 — 环形图 + 排行榜 -->
          <div class="dash-card dash-dept">
            <div class="dash-head">
              <h4>科室业务分布</h4>
              <span class="dash-unit">按门诊量占比</span>
            </div>
            <div class="dash-body dash-flex-row">
              <div class="donut-wrap">
                <svg viewBox="0 0 180 180" class="donut-svg">
                  <!-- 环形底色 -->
                  <circle cx="90" cy="90" r="72" fill="none" stroke="#eef5f3" stroke-width="22"/>
                  <!-- 各科室弧段 - 使用stroke-dasharray模拟 -->
                  <circle cx="90" cy="90" r="72" fill="none" stroke="#ef4444" stroke-width="22"
                    stroke-dasharray="113.1 452.39" stroke-dashoffset="0"
                    transform="rotate(-90 90 90)" stroke-linecap="round"/>
                  <circle cx="90" cy="90" r="72" fill="none" stroke="#f97316" stroke-width="22"
                    stroke-dasharray="81.65 452.39" stroke-dashoffset="-113.1"
                    transform="rotate(-90 90 90)" stroke-linecap="round"/>
                  <circle cx="90" cy="90" r="72" fill="none" stroke="#f59e0b" stroke-width="22"
                    stroke-dasharray="63.62 452.39" stroke-dashoffset="-194.75"
                    transform="rotate(-90 90 90)" stroke-linecap="round"/>
                  <circle cx="90" cy="90" r="72" fill="none" stroke="#22c55e" stroke-width="22"
                    stroke-dasharray="49.87 452.39" stroke-dashoffset="-258.37"
                    transform="rotate(-90 90 90)" stroke-linecap="round"/>
                  <circle cx="90" cy="90" r="72" fill="none" stroke="#14b8a6" stroke-width="22"
                    stroke-dasharray="45.24 452.39" stroke-dashoffset="-308.24"
                    transform="rotate(-90 90 90)" stroke-linecap="round"/>
                  <circle cx="90" cy="90" r="72" fill="none" stroke="#3b82f6" stroke-width="22"
                    stroke-dasharray="40.72 452.39" stroke-dashoffset="-353.48"
                    transform="rotate(-90 90 90)" stroke-linecap="round"/>
                  <circle cx="90" cy="90" r="72" fill="none" stroke="#a855f7" stroke-width="22"
                    stroke-dasharray="57.91 452.39" stroke-dashoffset="-394.2"
                    transform="rotate(-90 90 90)" stroke-linecap="round"/>
                  <!-- 中心文字 -->
                  <text x="90" y="82" text-anchor="middle" font-size="26" fill="#1a5248" font-weight="800">2,200</text>
                  <text x="90" y="102" text-anchor="middle" font-size="11" fill="#7ba89e">总床位数</text>
                </svg>
              </div>
              <div class="donut-list">
                <div class="dl-item" v-for="(d, i) in deptDistData" :key="d.name">
                  <span class="dl-dot" :style="{ background: d.color }"></span>
                  <span class="dl-name">{{ d.name }}</span>
                  <span class="dl-pct">{{ d.pct }}%</span>
                  <div class="dl-bar-bg"><div class="dl-bar-fg" :style="{ width: d.pct + '%', background: d.color }"></div></div>
                </div>
              </div>
            </div>
          </div>

          <!-- 运营指标仪表板 -->
          <div class="dash-card dash-kpi">
            <div class="dash-head">
              <h4>核心运营指标</h4>
              <span class="dash-unit">实时监控</span>
            </div>
            <div class="kpi-grid">
              <div class="kpi-item" v-for="k in kpiData" :key="k.label">
                <div class="kpi-top">
                  <span class="kpi-icon" :style="{ background: k.bgColor }" v-html="k.icon"></span>
                  <span class="kpi-trend" :class="k.up ? 'up' : 'down'">
                    {{ k.up ? '&#9650;' : '&#9660;' }}{{ k.trend }}
                  </span>
                </div>
                <strong class="kpi-value" :style="{ color: k.color }">{{ k.value }}</strong>
                <span class="kpi-label">{{ k.label }}</span>
                <!-- 微型折线图 -->
                <svg :viewBox="`0 0 ${k.spark.length * 6} 24`" class="sparkline" :class="{ up: k.up }">
                  <polyline :points="k.sparkPoints" fill="none" :stroke="k.color" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
            </div>
          </div>
        </div>

        <!-- 第三行：床位使用率 -->
        <div class="dash-row dash-row-full">
          <!-- 床位使用率 — 堆叠柱状图 -->
          <div class="dash-card dash-wide">
            <div class="dash-head">
              <h4>各科室床位使用率</h4>
              <div class="dash-tabs">
                <span :class="{ active: bedTab === 'week' }" @click="bedTab = '周'">本周</span>
                <span :class="{ active: bedTab === 'month' }" @click="bedTab = '月'">本月</span>
                <span :class="{ active: bedTab === 'quarter' }" @click="bedTab = '季'">本季度</span>
              </div>
            </div>
            <div class="dash-body">
              <svg viewBox="0 0 860 220" preserveAspectRatio="xMidYMid meet" class="stack-svg">
                <!-- 背景 -->
                <rect x="130" y="14" width="660" height="176" rx="6" fill="#f4faf8"/>
                <!-- 网格线 -->
                <g stroke="#dce8e4" stroke-width="0.5">
                  <line x1="130" y1="52" x2="790" y2="52"/>
                  <line x1="130" y1="90" x2="790" y2="90"/>
                  <line x1="130" y1="128" x2="790" y2="128"/>
                  <line x1="130" y1="166" x2="790" y2="166"/>
                </g>
                <!-- 标签 -->
                <g font-size="10.5" fill="#7ba89e" text-anchor="end">
                  <text x="122" y="36">100%</text><text x="122" y="74">75%</text>
                  <text x="122" y="112">50%</text><text x="122" y="150">25%</text>
                  <text x="122" y="184">0%</text>
                </g>
                <!-- 科室名 -->
                <g font-size="11" fill="#2d5a4e" text-anchor="middle" font-weight="600">
                  <text v-for="(bd, bi) in bedData" :key="'label'+bi" :x="151 + bi * 56" y="208">{{ bd.name }}</text>
                </g>
                <!-- 堆叠柱状图 -->
                <template v-for="(bd, bi) in bedData" :key="bd.name">
                  <!-- 使用中 -->
                  <rect :x="135 + bi * 56" :y="190 - bd.used * 1.72" width="32" :height="bd.used * 1.72"
                    rx="3" :fill="bd.used >= 90 ? '#ef4444' : bd.used > 80 ? '#f59e0b' : bd.used > 70 ? '#3b82f6' : '#22c55e'" opacity=".6"/>
                  <!-- 占用(非使用) -->
                  <rect :x="135 + bi * 56" :y="190 - (bd.used + bd.occupied) * 1.72" width="32"
                    :height="bd.occupied * 1.72" rx="0" fill="#cbd5e1" opacity=".35"/>
                  <!-- 数值标签 -->
                  <text :x="151 + bi * 56" :y="182 - bd.used * 1.72" font-size="10"
                    :fill="bd.used >= 90 ? '#b91c1c' : bd.used > 80 ? '#b45309' : bd.used > 70 ? '#1d4ed8' : '#15803d'"
                    font-weight="800" text-anchor="middle">{{ bd.used }}%</text>
                </template>
              </svg>
              <div class="stack-legend">
                <span><i style="background:#22c55e"></i>正常使用 (&le;70%)</span>
                <span><i style="background:#3b82f6"></i>高负荷 (70-80%)</span>
                <span><i style="background:#f59e0b"></i>较高负荷 (80-90%)</span>
                <span><i style="background:#ef4444"></i>满载超负荷 (&ge;90%)</span>
                <span><i style="background:#cbd5e1;opacity:.4"></i>已占用未使用</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 第四行：药品库存监控 + 设备运行状态 -->
        <div class="dash-row">
          <!-- 药品库存 -->
          <div class="dash-card dash-mid">
            <div class="dash-head">
              <h4>药品库存预警</h4>
              <span class="dash-alert" v-if="stockAlertCount > 0">{{ stockAlertCount }}项需补货</span>
            </div>
            <div class="dash-body stock-body">
              <div class="stock-item" v-for="st in stockData" :key="st.name" :class="{ warn: st.level <= 20, danger: st.level <= 10 }">
                <div class="stock-info">
                  <strong>{{ st.name }}</strong>
                  <span>{{ st.spec }}</span>
                </div>
                <div class="stock-bar-wrap">
                  <div class="stock-bar">
                    <div class="stock-fill" :style="{ width: st.level + '%', background: st.level <= 10 ? '#ef4444' : st.level <= 20 ? '#f59e0b' : '#22c55e' }"></div>
                  </div>
                  <span class="stock-pct" :style="{ color: st.level <= 10 ? '#ef4444' : st.level <= 20 ? '#f59e0b' : '#22c55e' }">{{ st.level }}%</span>
                </div>
                <span class="stock-count">{{ st.count }}{{ st.unit }}</span>
              </div>
            </div>
          </div>

          <!-- 设备运行状态 -->
          <div class="dash-card dash-mid">
            <div class="dash-head">
              <h4>设备运行状态</h4>
              <span class="dash-unit">{{ onlineEquip }}/{{ totalEquip }} 在线</span>
            </div>
            <div class="dash-body equip-body">
              <div class="equip-item" v-for="eq in equipStatus" :key="eq.name">
                <div class="equip-status-dot" :class="eq.status"></div>
                <div class="equip-info">
                  <strong>{{ eq.name }}</strong>
                  <span>{{ eq.room }}</span>
                </div>
                <div class="equip-meta">
                  <span class="equip-run" :class="eq.status">{{ eq.runTime }}</span>
                  <span class="equip-util" :style="{ color: eq.util > 80 ? '#ef4444' : eq.util > 60 ? '#f59e0b' : '#22c55e' }">{{ eq.util }}%</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </section>

    <!-- 完善信息弹窗 -->
    <el-dialog v-model="showCompleteInfoDialog" title="完善个人信息" width="420px" :close-on-click-modal="false" center>
      <div class="complete-info-content">
        <div class="complete-info-icon">
          <svg viewBox="0 0 24 24" width="36" height="36" fill="none" stroke="#fff" stroke-width="2" stroke-linecap="round"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
        </div>
        <p>完善您的个人信息可以为您提供更好的就诊服务</p>
      </div>
      <template #footer>
        <div class="complete-info-footer">
          <el-button @click="showCompleteInfoDialog = false">稍后完善</el-button>
          <el-button type="primary" @click="goToCompleteInfo">立即完善</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Index">
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import useUserStore from '@/store/modules/user'
import { getPendingMedicalStaff } from '@/api/system/user'

const router = useRouter()
const userStore = useUserStore()

const heroWords = ['云医智联', 'AI 驱动', '温暖守护', '每一次就医']
const showCompleteInfoDialog = ref(false)
const pendingMedicalCount = ref(0)
const outpatientTab = ref('month')
const bedTab = ref('month')

const isAdmin = computed(() => userStore.roles?.some(role => role.roleKey?.includes('admin')) || false)

// SVG 图标
const svgDept = '<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" style="vertical-align:middle;margin-right:6px"><path d="M3 21h18M5 21V5a2 2 0 012-2h10a2 2 0 012 2v16"/><line x1="9" y1="21" x2="9" y2="17"/><line x1="15" y1="21" x2="15" y2="17"/></svg>'
const svgEquip = '<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" style="vertical-align:middle;margin-right:6px"><rect x="4" y="4" width="16" height="16" rx="3"/><path d="M9 4v16M15 4v16M4 9h16M4 15h16"/></svg>'
const svgService = '<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" style="vertical-align:middle;margin-right:6px"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>'
const svgQuality = '<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" style="vertical-align:middle;margin-right:6px"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>'
const svgChart = '<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" style="vertical-align:middle;margin-right:8px"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/><line x1="2" y1="20" x2="22" y2="20"/></svg>'
const svgTeach = '<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" style="vertical-align:middle;margin-right:6px"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/></svg>'
const svgResources = '<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" style="vertical-align:middle;margin-right:8px"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>'

const departmentSlides = [
  { name: '内科', label: '综合诊疗', image: '/内科.png', desc: '覆盖常见病、慢病和多系统疾病管理，提供连续、规范的诊疗服务。', doctors: '38 位', beds: '220 张' },
  { name: '神经外科', label: '精准手术', image: '/神经外科.png', desc: '聚焦脑血管、颅脑创伤和神经系统肿瘤，推进显微与微创治疗。', doctors: '24 位', beds: '142 张' },
  { name: '心血管内科', label: '胸痛中心', image: '/心血管内科.png', desc: '建设急危重症绿色通道，完善介入、康复与长期随访一体化服务。', doctors: '32 位', beds: '186 张' },
  { name: '骨科', label: '运动修复', image: '/骨科.png', desc: '开展关节、脊柱、创伤和运动医学诊疗，重视术后康复管理。', doctors: '28 位', beds: '168 张' }
]

const equipmentSlides = [
  { name: '3.0T MRI', label: '磁共振', image: '/仪器1.png', desc: 'Siemens Vida · 3台 · 全身超导磁共振，支持高清弥散与波谱分析' },
  { name: '256排CT', label: 'CT影像', image: '/仪器02.png', desc: 'GE Revolution · 4台 · 亚毫米精度成像，冠脉CTA与卒中快速分诊' },
  { name: 'DSA', label: 'DSA', image: '/仪器03.png', desc: 'Philips Azurion · 3台 · 数字减影血管造影，介入诊疗一体化平台' },
  { name: '达芬奇机器人', label: '手术机器人', image: '/仪器04.png', desc: 'Da Vinci Xi · 2台 · 微创精准操作，泌尿、妇科、普外科' }
]

// 教学科研人才轮播数据
const teachingSlides = [
  {
    name: '医学教育中心',
    label: '人才培养',
    image: '/教学科研人才1.png',
    desc: '承担多所医学院校临床教学任务，建立规范化培训体系，培养高素质医疗人才。',
    count1: '320+', meta1: '在培学员',
    count2: '48', meta2: '硕博导师'
  },
  {
    name: '临床研究中心',
    label: '科研创新',
    image: '/教学科研人才2.png',
    desc: '聚焦肿瘤、心血管、神经科学等前沿领域，开展多中心临床试验与转化医学研究。',
    count1: '156', meta1: '在研项目',
    count2: '89', meta2: 'SCI论文/年'
  },
  {
    name: '学术交流平台',
    label: '国际合作',
    image: '/教学科研人才3.png',
    desc: '与国内外顶尖医疗机构建立合作关系，定期举办学术会议与专家互访项目。',
    count1: '26', meta1: '合作机构',
    count2: '12', meta2: '国际会议/年'
  }
]

// 质量趋势轮播数据
const qualitySlides = [
  {
    name: '诊断符合率',
    label: '质量指标',
    image: '/环境1.png',
    desc: '临床诊断与病理诊断符合率持续保持在98.6%以上，严格执行三级质控体系确保诊断质量。',
    count1: '98.6%', meta1: '诊断符合率',
    count2: '99.1%', meta2: '近7月最高'
  },
  {
    name: '治愈好转率',
    label: '疗效指标',
    image: '/环境2.png',
    desc: '住院患者治愈好转率达96.2%，多学科联合诊疗模式显著提升了疑难杂症的救治成功率。',
    count1: '96.2%', meta1: '治愈好转率',
    count2: '97.2%', meta2: '近7月最高'
  },
  {
    name: '患者满意度',
    label: '服务指标',
    image: '/环境3.png',
    desc: '患者综合满意度达97.8分，持续优化就医流程，改善就诊体验，构建和谐医患关系。',
    count1: '97.8', meta1: '满意度（分）',
    count2: '98.4', meta2: '近7月最高'
  }
]

const services = [
  { name: '日均门诊人次', desc: '含急诊与专科门诊', value: '5,200', pct: 1, color: '#6ba3a0' },
  { name: '日均手术台次', desc: '含择期与急诊手术', value: '132', pct: 0.35, color: '#d4a878' },
  { name: '日均检查量', desc: 'CT/MRI/超声/检验', value: '2,860', pct: 0.72, color: '#6d8fad' },
  { name: '远程会诊/年', desc: '覆盖医联体单位', value: '3,400', pct: 0.82, color: '#bb8f95' }
]

// ========== 数据看板数据 ==========

// 手术分类数据
const surgeryData = [
  { name: '普外科手术', sub: '腹腔镜·开腹', count: 12640, value: '12,640 台', pct: 1, color: '#ef4444' },
  { name: '骨科手术', sub: '关节置换·脊柱', count: 8620, value: '8,620 台', pct: 0.68, color: '#f97316' },
  { name: '心血管介入', sub: 'PCI·起搏器', count: 7380, value: '7,380 台', pct: 0.58, color: '#f59e0b' },
  { name: '神经外科手术', sub: '显微·血管内', count: 5140, value: '5,140 台', pct: 0.41, color: '#22c55e' },
  { name: '妇产科手术', sub: '剖宫产·子宫切除', count: 8920, value: '8,920 台', pct: 0.71, color: '#3b82f6' },
  { name: '其他手术', sub: '五官科·整形等', count: 5560, value: '5,560 台', pct: 0.44, color: '#a855f7' }
]

// 科室分布数据
const deptDistData = [
  { name: '内科系统', pct: 25, color: '#ef4444' },
  { name: '外科系统', pct: 18, color: '#f97316' },
  { name: '妇产科', pct: 14, color: '#f59e0b' },
  { name: '儿科中心', pct: 11, color: '#22c55e' },
  { name: '骨科运动', pct: 10, color: '#14b8a6' },
  { name: '急诊重症', pct: 9, color: '#3b82f6' },
  { name: '其他科室', pct: 13, color: '#a855f7' }
]

// KPI 数据
const kpiData = [
  {
    label: '平均住院日', value: '6.8天', trend: '0.3', up: false,
    color: '#22c55e', bgColor: 'rgba(34,197,94,.10)',
    icon: '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="#22c55e" stroke-width="2" stroke-linecap="round"><rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4M8 2v4M3 10h18"/></svg>',
    spark: [45, 38, 42, 35, 30, 32, 28],
    sparkPoints: '0,17.5 6,23.3 12,19.8 18,26.3 24,31.5 30,29 36,33.3'
  },
  {
    label: '床位周转率', value: '42.6次/年', trend: '2.1', up: true,
    color: '#3b82f6', bgColor: 'rgba(59,130,246,.10)',
    icon: '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="#3b82f6" stroke-width="2" stroke-linecap="round"><path d="M3 3v18h18"/><path d="m18.7 8-5.1 5.2-2.8-2.7L6 15.3"/></svg>',
    spark: [20, 28, 25, 35, 32, 42, 45],
    sparkPoints: '0,31.5 6,23.3 12,26.3 18,15.8 24,18.9 30,9.5 36,5.3'
  },
  {
    label: '药占比', value: '26.4%', trend: '1.8', up: false,
    color: '#f59e0b', bgColor: 'rgba(245,158,11,.10)',
    icon: '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="#f59e0b" stroke-width="2" stroke-linecap="round"><path d="M10.5 20.5L4 14l3-3 3.5 3.5L17 8l3 3-9.5 9.5z"/></svg>',
    spark: [35, 32, 30, 28, 29, 27, 26],
    sparkPoints: '0,15.8 6,18.9 12,21 18,23.1 24,21.9 30,24.1 36,25.2'
  },
  {
    label: '检查阳性率', value: '73.2%', trend: '3.5', up: true,
    color: '#a855f7', bgColor: 'rgba(168,85,247,.10)',
    icon: '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="#a855f7" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>',
    spark: [55, 58, 62, 60, 65, 70, 73],
    sparkPoints: '0,26.3 6,23.3 12,19.8 18,21.9 24,16.8 30,11.6 36,7.4'
  },
  {
    label: '院感发生率', value: '0.32%', trend: '0.05', up: false,
    color: '#ef4444', bgColor: 'rgba(239,68,68,.08)',
    icon: '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="#ef4444" stroke-width="2" stroke-linecap="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>',
    spark: [0.55, 0.48, 0.51, 0.43, 0.40, 0.36, 0.32],
    sparkPoints: '0,10.5 6,14.7 12,12.6 18,18.9 24,21 30,23.1 36,25.2'
  },
  {
    label: '医保控费达标', value: '97.8%', trend: '1.2', up: true,
    color: '#14b8a6', bgColor: 'rgba(20,184,166,.10)',
    icon: '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="#14b8a6" stroke-width="2" stroke-linecap="round"><path d="M12 2l2.4 7.2h7.6l-6 4.8 2.4 7.2-6-4.8-6 4.8 2.4-7.2-6-4.8h7.6z"/></svg>',
    spark: [92, 93, 95, 94, 96, 97, 98],
    sparkPoints: '0,26.3 6,25.2 12,23.1 18,24.1 24,21.9 30,20 36,18.9'
  }
]

// 床位使用率
const bedData = [
  { name: '心内科', used: 92, occupied: 5 },
  { name: '神经外科', used: 78, occupied: 8 },
  { name: '骨科', used: 85, occupied: 6 },
  { name: '妇产科', used: 71, occupied: 12 },
  { name: '肿瘤科', used: 96, occupied: 3 },
  { name: '儿科', used: 63, occupied: 10 },
  { name: '呼吸科', used: 74, occupied: 9 },
  { name: '消化内科', used: 82, occupied: 7 },
  { name: '泌尿外科', used: 68, occupied: 11 },
  { name: '内分泌科', used: 73, occupied: 8 },
  { name: '血液科', used: 88, occupied: 4 },
  { name: '康复科', used: 55, occupied: 15 }
]

// 药品库存
const stockAlertCount = computed(() => stockData.filter(s => s.level <= 20).length)
const stockData = [
  { name: '阿莫西林胶囊', spec: '250mg×24粒', level: 8, count: 420, unit: '盒' },
  { name: '头孢曲松钠', spec: '1.0g/支', level: 15, count: 186, unit: '支' },
  { name: '氯化钠注射液', spec: '250ml/袋', level: 72, count: 2400, unit: '袋' },
  { name: '布洛芬缓释片', spec: '300mg×20片', level: 6, count: 85, unit: '盒' },
  { name: '奥美拉唑肠溶', spec: '20mg×14粒', level: 34, count: 520, unit: '盒' },
  { name: '人血白蛋白', spec: '12.5g/瓶', level: 12, count: 28, unit: '瓶' },
  { name: '甘露醇注射液', spec: '250ml/瓶', level: 58, count: 860, unit: '瓶' }
]

// 设备状态
const totalEquip = ref(13)
const onlineEquip = ref(12)
const equipStatus = [
  { name: '3.0T MRI-01', room: '影像楼1F-A区', status: 'online', runTime: '1,246h', util: 87 },
  { name: '256排CT-01', room: '影像楼1F-B区', status: 'online', runTime: '2,103h', util: 92 },
  { name: 'DSA-01', room: '介入中心', status: 'online', runTime: '1,890h', util: 78 },
  { name: '达芬奇Xi-01', room: '手术室8F', status: 'online', runTime: '856h', util: 65 },
  { name: 'PET-CT', room: '核医学科B1', status: 'maintenance', runTime: '维护中', util: 0 },
  { name: '直线加速器', room: '放疗中心', status: 'online', runTime: '1,560h', util: 83 }
]

onMounted(() => {
  nextTick(() => {
    if (isAdmin.value) {
      getPendingMedicalStaff().then(res => {
        if (res.data !== undefined && res.data !== null) pendingMedicalCount.value = res.data
      }).catch(() => {})
    }
    setTimeout(() => {
      if (userStore.needCompleteInfo && userStore.userType) {
        showCompleteInfoDialog.value = true
      }
    }, 500)
  })
})

const goToCompleteInfo = () => {
  showCompleteInfoDialog.value = false
  if (userStore.userType === 'doctor') router.push('/complete/doctor')
  else if (userStore.userType === 'patient') router.push('/complete/patient')
}
</script>

<style scoped lang="scss">
.his-home {
  position: relative;
  min-height: 100vh;
  overflow-x: hidden;
  color: #fff;
}

/* 视频背景 */
.home-video-bg { position: fixed; top: 0; left: 0; width: 100%; height: 100vh; object-fit: cover; z-index: 0; }

.home-video-overlay {
  position: fixed; inset: 0; z-index: 0;
  background: linear-gradient(135deg, rgba(120,180,165,.22), rgba(100,160,140,.18), rgba(60,120,100,.30));
  pointer-events: none;
}

/* === Hero === */
.home-hero {
  position: relative; z-index: 1;
  display: flex; flex-direction: column; justify-content: flex-start;
  height: calc(100vh - 50px);
  padding: 40px 32px 32px 32px;
}
.hero-content { display: flex; flex-direction: column; align-items: flex-start; max-width: 720px; margin-top: 20px; }
.hero-heading { display: flex; flex-wrap: wrap; gap: 0.25em; margin: 0; padding: 0; list-style: none; }
.hero-word {
  display: inline-block; font-size: clamp(26px, 3vw, 42px); font-weight: 700;
  line-height: 1.08; letter-spacing: -0.01em; color: #fff;
  text-shadow: 0 2px 12px rgba(0,0,0,.3);
  opacity: 0; transform: translateY(32px);
  animation: heroFadeUp .7s cubic-bezier(.22,1,.36,1) forwards;
}
.hero-sub {
  margin-top: 24px; font-size: 14px; line-height: 1.65; color: rgba(255,255,255,0.88); max-width: 260px;
  opacity: 0; transform: translateY(24px);
  animation: heroFadeUp .7s cubic-bezier(.22,1,.36,1) forwards; animation-delay: 0.9s;
}
.hero-scroll-hint {
  position: absolute; bottom: 40px; left: 50%; transform: translateX(-50%);
  span {
    display: block; width: 24px; height: 24px;
    border-right: 2px solid rgba(255,255,255,.6); border-bottom: 2px solid rgba(255,255,255,.6);
    transform: rotate(45deg); animation: scrollBounce 2s ease-in-out infinite;
  }
}
@keyframes scrollBounce { 0%,100%{opacity:.5;transform:rotate(45deg)translate(0,0)} 50%{opacity:1;transform:rotate(45deg)translate(4px,4px)} }
@keyframes heroFadeUp { to { opacity:1; transform:translateY(0) } }

/* === 资源区 === */
.home-resources {
  position: relative; z-index: 1;
  padding: 48px 32px 68px;
  background: linear-gradient(180deg, rgba(240,249,250,.78) 0%, rgba(220,240,244,.66) 42%, rgba(187,219,225,.56) 100%);
  backdrop-filter: blur(18px);
}

/* 资源展示区标题 */
.resources-header {
  display: flex; align-items: center; justify-content: space-between;
  max-width: 1300px; margin: 0 auto 24px; padding: 0 4px;
  h2 {
    margin: 0; font-size: 22px; font-weight: 800; color: #173943;
    display: flex; align-items: center;
    i { display:inline-flex;align-items:center; }
    :deep(svg){display:inline-block;color:#1f9a8a;}
  }
  .resources-badge {
    padding: 5px 14px; border-radius: 20px; font-size: 12px; font-weight: 600;
    background: linear-gradient(135deg, rgba(39,174,128,.10), rgba(52,152,219,.06));
    color: #1a7a66; border: 1px solid rgba(39,174,128,.18);
  }
}

.resources-grid {
  display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px;
  max-width: 1300px; margin: 0 auto;
}

.res-card {
  position: relative; overflow: hidden; padding: 28px; border-radius: 16px;
  border: 1px solid rgba(35,118,134,.14);
  background: linear-gradient(145deg, rgba(255,255,255,.88), rgba(244,251,252,.70));
  backdrop-filter: blur(14px);
  box-shadow: 0 18px 44px rgba(28,79,94,.14);
  transition: transform .25s ease, box-shadow .25s ease, border-color .25s ease;
  &::before { content:""; position:absolute; top:0; left:0; right:0; height:3px; background: linear-gradient(90deg, #1f9a8a, #4ea3d8); }
  &:hover { transform: translateY(-3px); border-color: rgba(35,118,134,.26); box-shadow: 0 22px 52px rgba(28,79,94,.18); }
  h3 { margin: 0 0 18px; font-size: 18px; font-weight: 700; color: #173943;
    i { display:inline-flex; align-items:center; margin-right:6px; }
    :deep(svg){display:inline-block;}
  }
}
.dept-card::before { background: linear-gradient(90deg, #1f9a8a, #6bc7b4); }
.equip-card::before { background: linear-gradient(90deg, #367fc4, #6fb7e6); }
.service-card::before { background: linear-gradient(90deg, #7b68c8, #46b4c8); }
.teach-card::before { background: linear-gradient(90deg, #c0392b, #e67e22); }
.quality-card::before { background: linear-gradient(90deg, #1f9a8a, #f0a33a); }

/* 轮播通用 */
.dept-carousel { --el-carousel-indicator-width: 18px; }
.dept-carousel :deep(.el-carousel__container) { border-radius: 12px; }
.dept-carousel :deep(.el-carousel__indicators--outside) { margin-top: 8px; }
.dept-carousel :deep(.el-carousel__button) { width:18px;height:4px;border-radius:999px;background:#cbd5e1;opacity:.34; }
.dept-carousel :deep(.is-active .el-carousel__button) { width:28px; opacity:.9; background:#1f9a8a; }

.equip-carousel { --el-carousel-indicator-width: 18px; }
.equip-carousel :deep(.el-carousel__container) { border-radius: 12px; }
.equip-carousel :deep(.el-carousel__indicators--outside) { margin-top: 8px; }
.equip-carousel :deep(.el-carousel__button) { width:18px;height:4px;border-radius:999px;background:#cbd5e1;opacity:.34; }
.equip-carousel :deep(.is-active .el-carousel__button) { width:28px; opacity:.9; background:#367fc4; }

.teach-carousel { --el-carousel-indicator-width: 18px; }
.teach-carousel :deep(.el-carousel__container) { border-radius: 12px; }
.teach-carousel :deep(.el-carousel__indicators--outside) { margin-top: 8px; }
.teach-carousel :deep(.el-carousel__button) { width:18px;height:4px;border-radius:999px;background:#cbd5e1;opacity:.34; }
.teach-carousel :deep(.is-active .el-carousel__button) { width:28px; opacity:.9; background:#c0392b; }

.quality-carousel { --el-carousel-indicator-width: 18px; }
.quality-carousel :deep(.el-carousel__container) { border-radius: 12px; }
.quality-carousel :deep(.el-carousel__indicators--outside) { margin-top: 8px; }
.quality-carousel :deep(.el-carousel__button) { width:18px;height:4px;border-radius:999px;background:#cbd5e1;opacity:.34; }
.quality-carousel :deep(.is-active .el-carousel__button) { width:28px; opacity:.9; background:#1f9a8a; }

.dept-slide { position:relative; height:100%; overflow:hidden; border-radius:14px; background:#dcebee; }
.dept-image { width:100%;height:100%;display:block;object-fit:cover;filter:brightness(1.15); }
.dept-shade {
  position:absolute;inset:0;
  background: linear-gradient(90deg, rgba(8,41,48,.45), rgba(8,41,48,.22) 46%, rgba(8,41,48,.04)),
    linear-gradient(180deg, rgba(0,0,0,.04), rgba(0,0,0,.15));
}
.dept-content { position:absolute; left:26px; right:26px; bottom:24px; color:#fff; }
.dept-label {
  display:inline-flex; align-items:center; height:28px; padding:0 12px; border-radius:999px;
  background:rgba(255,255,255,.18); border:1px solid rgba(255,255,255,.28);
  font-size:13px;font-weight:700;
}
.dept-content h4 { margin:14px 0 10px; color:#fff; font-size:28px; line-height:1.1; }
.dept-content p { max-width:460px;margin:0; color:rgba(255,255,255,.86);font-size:14px;line-height:1.7; }
.dept-metrics { display:flex;gap:12px;margin-top:18px; }
.dept-metrics span {
  min-width:114px;padding:10px 14px;border-radius:10px;background:rgba(255,255,255,.16);
  border:1px solid rgba(255,255,255,.22); color:rgba(255,255,255,.82);font-size:13px;
}
.dept-metrics strong { display:block;margin-bottom:2px;color:#fff;font-size:20px; }

/* 卡片底部介绍文字 */
.card-intro {
  margin-top: 16px; padding: 8px 0;
}
.card-intro p {
  margin: 0; font-size: 14px; line-height: 1.9; color: #1a1a1a; font-weight: 400;
  b { font-weight: 700; color: #000; }
}

/* 柱状图 */

/* ==================== 数据运营中心 ==================== */
.data-dashboard { max-width: 1300px; margin: 36px auto 0; }

.dash-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 22px; padding: 0 4px;
  h2 {
    margin: 0; font-size: 22px; font-weight: 800; color: #173943;
    display: flex; align-items: center;
    i { display:inline-flex;align-items:center; }
    :deep(svg){display:inline-block;color:#1f9a8a}
  }
  .dash-badge {
    padding: 5px 14px; border-radius: 20px; font-size: 12px; font-weight: 600;
    background: linear-gradient(135deg, rgba(39,174,128,.1), rgba(39,174,128,.06));
    color: #1a7a66; border: 1px solid rgba(39,174,128,.18);
  }
}

.dash-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 16px; }
.dash-row-flow { grid-template-columns: 1fr minmax(300px, 340px); }
.dash-row-full { grid-template-columns: 1fr; }

/* 卡片基础 */
.dash-card {
  border-radius: 16px; border: 1px solid rgba(35,118,134,.12);
  background: linear-gradient(145deg, #ffffff, #f8fcfb);
  box-shadow: 0 14px 38px rgba(28,79,94,.09);
  overflow: hidden; transition: transform .25s ease, box-shadow .25s ease;
  &:hover { transform: translateY(-2px); box-shadow: 0 20px 48px rgba(28,79,94,.14); }
}
.dash-main { /* 自动适应行宽 */ }
.dash-side { /* 侧栏 */ }
.dash-dept { /* 左半 */ }
.dash-kpi { /* 右半 */ }
.dash-wide { /* 全宽 */ }
.dash-mid { /* 半宽 */ }

.dash-head {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 22px 0;
  h4 { margin: 0; font-size: 15px; font-weight: 700; color: #173943; }
  .dash-unit { font-size: 11.5px; color: #8ba8a0; font-weight: 500; }
  .dash-alert {
    padding: 3px 10px; border-radius: 10px; font-size: 11px; font-weight: 700;
    background: rgba(231,76,60,.08); color: #c0392b; border: 1px solid rgba(231,76,60,.15);
  }
  .dash-title-group { display: flex; flex-direction: column; gap: 2px;
    .dash-period { font-size: 11.5px; color: #8ba8a0; font-weight: 400; }
  }
}
.dash-tabs { display: flex; gap: 4px; background: #f0f6f4; padding: 3px; border-radius: 10px;
  span { padding: 5px 14px; border-radius: 8px; font-size: 12px; font-weight: 600; color: #7ba89e; cursor:pointer; transition:.2s; }
  span.active { background: #fff; color: #1a7a66; box-shadow: 0 2px 8px rgba(0,0,0,.06); }
}
.dash-body { padding: 14px 22px 20px; }

/* ====== 门诊面积图 ====== */
.area-svg { width: 100%; height: auto; display: block; }
.area-legend {
  display: flex; justify-content: center; gap: 20px; padding: 12px 0 8px;
  .al-item { display:flex; align-items:center; gap:6px; font-size:12px; color:#5e7881; font-weight:500;
    em{display:inline-block;width:12px;height:4px;border-radius:2px;}
  }
}
.area-summary {
  display: flex; align-items: center; justify-content: space-around;
  padding: 14px 20px; margin-top: 4px;
  background: linear-gradient(135deg, rgba(39,174,128,.04), rgba(52,152,219,.03)); border-radius: 12px;
  border: 1px solid rgba(39,174,128,.08);
}
.as-col { text-align: center;
  .as-num { font-size: 22px; font-weight: 800; color: #173943;
    small { font-size: 13px; font-weight: 600; color: #7ba89e; margin-left: 2px; }
  }
  .as-label { font-size: 11px; color: #8ba8a0; margin-top: 2px; }
}
.as-divider { width: 1px; height: 36px; background: rgba(39,174,128,.12); }

/* ====== 手术横向柱状图 ====== */
.h-bar-svg { width: 100%; height: auto; display: block; }
.surgery-total {
  text-align: center; padding: 12px 0 2px;
  strong { font-size: 24px; font-weight: 800; color: #173943; }
  span { font-size: 13px; color: #7ba89e; margin-left: 4px; font-weight: 500; }
  small { display: block; font-size: 11px; color: #8ba8a0; margin-top: 2px;
    em { color: #27ae80; font-style: normal; font-weight: 700; }
  }
}

/* ====== 科室环形图 ====== */
.dash-flex-row { display: flex; gap: 24px; align-items: center; }
.donut-wrap { flex-shrink: 0; width: 180px; }
.donut-svg { width: 180px; height: 180px; display: block; filter: drop-shadow(0 4px 12px rgba(0,0,0,.06)); }
.donut-list { flex: 1; display: flex; flex-direction: column; gap: 6px; }
.dl-item { display: flex; align-items: center; gap: 8px; padding: 5px 0;
  .dl-dot { width: 10px; height: 10px; border-radius: 3px; flex-shrink: 0; }
  .dl-name { font-size: 12.5px; font-weight: 600; color: #2d5a4e; min-width: 72px; }
  .dl-pct { font-size: 13px; font-weight: 800; color: #173943; min-width: 32px; text-align: right; }
  .dl-bar-bg { flex:1;height:6px;border-radius:3px;background:#eef5f3;overflow:hidden;min-width:40px;
    .dl-bar-fg { height:100%;border-radius:3px;transition:width .8s cubic-bezier(.22,1,.36,1);}
  }
}

/* ====== KPI 卡片 ====== */
.kpi-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; }
.kpi-item {
  padding: 14px; border-radius: 12px; background: #f8fcfa; border: 1px solid rgba(39,174,128,.07);
  transition: transform .2s ease, box-shadow .2s ease;
  &:hover { transform: translateY(-2px); box-shadow: 0 8px 20px rgba(0,0,0,.05); }
}
.kpi-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px;
  .kpi-icon { width: 30px; height: 30px; border-radius: 8px; display: flex; align-items: center; justify-content: center; }
  .kpi-trend { font-size: 11px; font-weight: 700; padding: 2px 7px; border-radius: 6px;
    &.up { color: #27ae80; background: rgba(39,174,128,.08); }
    &.down { color: #e74c3c; background: rgba(231,76,60,.08); }
  }
}
.kpi-value { display: block; font-size: 20px; font-weight: 800; margin-bottom: 2px; }
.kpi-label { font-size: 11px; color: #8ba8a0; display: block; margin-bottom: 6px; }
.sparkline { width: 100%; height: 24px; display: block;
  &.up polyline { filter: drop-shadow(0 1px 2px rgba(39,174,128,.3)); }
  &:not(.up) polyline { filter: drop-shadow(0 1px 2px rgba(231,76,60,.3)); }
}

/* ====== 床位堆叠柱状图 ====== */
.stack-svg { width: 100%; height: auto; display: block; }
.stack-legend { display: flex; justify-content: center; gap: 16px; padding: 10px 0 2px; flex-wrap: wrap;
  span { display:flex; align-items:center; gap:5px; font-size:11px;color:#7ba89e;
    i{display:inline-block;width:10px;height:6px;border-radius:2px;}
  }
}

/* ====== 药品库存 ====== */
.stock-body { display: flex; flex-direction: column; gap: 8px; max-height: 280px; overflow-y: auto;
  &::-webkit-scrollbar{width:4px}&::-webkit-scrollbar-thumb{background:rgba(39,174,128,.2);border-radius:2px}
}
.stock-item { display: flex; align-items: center; gap: 12px; padding: 10px 14px; border-radius: 10px; background: #f8fcfa;
  border: 1px solid transparent; transition: all .2s;
  &.warn { border-color: rgba(243,156,18,.3); background: rgba(243,156,18,.04); }
  &.danger { border-color: rgba(231,76,60,.3); background: rgba(231,76,60,.04); }
  .stock-info { min-width: 110px; flex-shrink: 0;
    strong{display:block;font-size:12.5px;font-weight:700;color:#173943}
    span{font-size:10px;color:#8ba8a0}
  }
  .stock-bar-wrap { flex:1;display:flex;flex-direction:column;gap:3px;min-width:0;
    .stock-bar{height:8px;border-radius:4px;background:#eef5f3;overflow:hidden;
      .stock-fill{height:100%;border-radius:4px;transition:width .6s ease}
    }
    .stock-pct{text-align:right;font-size:12px;font-weight:700}
  }
  .stock-count{min-width:56px;text-align:right;font-size:12px;font-weight:700;color:#5e7881}
}

/* ====== 设备状态 ====== */
.equip-body { display: flex; flex-direction: column; gap: 6px; }
.equip-item { display: flex; align-items: center; gap: 10px; padding: 9px 14px; border-radius: 10px; background: #f8fcfa;
  border: 1px solid rgba(39,174,128,.06); transition: background .2s;
  &:hover { background: #f0f6f4; }
  .equip-status-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0;
    &.online { background: #22c55e; box-shadow: 0 0 6px rgba(34,197,94,.5); animation: pulse 2s infinite; }
    &.offline { background: #94a3b8; }
    &.maintenance { background: #f59e0b; box-shadow: 0 0 6px rgba(245,158,11,.4); }
  }
  .equip-info { flex:1; min-width: 0;
    strong{display:block;font-size:12px;font-weight:700;color:#173943}
    span{font-size:10px;color:#8ba8a0}
  }
  .equip-meta { display:flex;flex-direction:column;align-items:flex-end;gap:2px;
    .equip-run{font-size:10px;font-weight:600;color:#5e7881;&.online{color:#22c55e}&.maintenance{color:#f59e0b}}
    .equip-util{font-size:12px;font-weight:800}
  }
}
@keyframes pulse { 0%,100%{box-shadow:0 0 6px rgba(34,197,94,.5)} 50%{box-shadow:0 0 12px rgba(34,197,94,.3),0 0 4px rgba(34,197,94,.6)} }

/* 完善信息弹窗 */
.complete-info-content { text-align: center; padding: 20px 0; }
.complete-info-icon {
  width: 80px; height: 80px; margin: 0 auto 20px; border-radius: 50%;
  background: linear-gradient(135deg, #73d8b9, #49b99d);
  display: flex; align-items: center; justify-content: center; color: #fff; font-size: 40px;
}
.complete-info-content p { font-size: 16px; color: #607f8c; margin: 0; }
.complete-info-footer { display: flex; justify-content: center; gap: 12px; }

/* 响应式 */
@media (max-width: 900px) {
  .home-hero { padding: 90px 18px 32px 18px; }
  .hero-word { font-size: clamp(20px, 5vw, 32px); }
  .home-resources { padding: 40px 18px 50px; }
  .resources-grid { grid-template-columns: 1fr; }
  .dash-row-flow { grid-template-columns: 1fr; }
  .kpi-grid { grid-template-columns: repeat(2, 1fr); }
  .dash-flex-row { flex-direction: column; }
  .dash-header { flex-direction: column; align-items: flex-start; gap: 8px; }
  .dash-row { grid-template-columns: 1fr; }
}
@media (max-width: 600px) {
  .kpi-grid { grid-template-columns: 1fr; }
  .area-summary { flex-wrap: wrap; gap: 12px; }
  .as-divider { display: none; }
}
</style>
