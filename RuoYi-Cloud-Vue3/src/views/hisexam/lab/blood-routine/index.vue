<template>
  <LabResultPage :config="config" />
</template>

<script setup>
import LabResultPage from '../common/LabResultPage.vue'

const config = {
  title: '血常规检验',
  category: '血液学',
  method: '全自动血液分析仪五分类检测，结合白细胞分类、红细胞参数和血小板参数进行审核。',
  instrument: 'Sysmex XN-1000 全自动血液分析仪',
  samplePrefix: 'CBC',
  specimens: ['EDTA-K2 抗凝全血', '末梢全血'],
  tests: [
    { code: 'WBC', name: '白细胞计数', unit: '10^9/L', range: '3.5-9.5', low: 3.5, high: 9.5, criticalLow: 1.0, criticalHigh: 30, defaultValue: 8.7, meaning: '感染、炎症、骨髓抑制及血液系统疾病筛查' },
    { code: 'NEUT%', name: '中性粒细胞百分比', unit: '%', range: '40-75', low: 40, high: 75, defaultValue: 78.4, meaning: '细菌感染、应激、炎症活动度评估' },
    { code: 'LYMPH%', name: '淋巴细胞百分比', unit: '%', range: '20-50', low: 20, high: 50, defaultValue: 15.8, meaning: '病毒感染、免疫状态及白细胞分类参考' },
    { code: 'RBC', name: '红细胞计数', unit: '10^12/L', range: '3.8-5.1', low: 3.8, high: 5.1, defaultValue: 4.42, meaning: '贫血、红细胞增多症及血液浓缩判断' },
    { code: 'HGB', name: '血红蛋白', unit: 'g/L', range: '115-150', low: 115, high: 150, criticalLow: 60, defaultValue: 126, meaning: '贫血程度和携氧能力评估' },
    { code: 'HCT', name: '红细胞压积', unit: '%', range: '35-45', low: 35, high: 45, defaultValue: 38.6, meaning: '贫血、脱水及血液浓缩参考' },
    { code: 'PLT', name: '血小板计数', unit: '10^9/L', range: '125-350', low: 125, high: 350, criticalLow: 30, criticalHigh: 1000, defaultValue: 286, meaning: '出血风险、血栓风险及骨髓造血状态评估' },
    { code: 'CRP', name: 'C反应蛋白', unit: 'mg/L', range: '0-10', low: 0, high: 10, defaultValue: 18.2, meaning: '急性炎症和感染辅助判断' }
  ]
}
</script>
