<template>
  <LabResultPage :config="config" />
</template>

<script setup>
import LabResultPage from '../common/LabResultPage.vue'

const config = {
  title: '免疫学检验',
  category: '免疫血清学',
  method: '采用化学发光/免疫比浊法检测感染、炎症和免疫功能相关指标。',
  instrument: 'Abbott Architect i2000SR 化学发光免疫分析仪',
  samplePrefix: 'IMM',
  specimens: ['血清', '血浆'],
  tests: [
    { code: 'PCT', name: '降钙素原', unit: 'ng/mL', range: '0-0.05', low: 0, high: 0.05, criticalHigh: 10, defaultValue: 0.18, meaning: '细菌感染和脓毒症风险评估' },
    { code: 'IL6', name: '白细胞介素-6', unit: 'pg/mL', range: '0-7', low: 0, high: 7, defaultValue: 16.4, meaning: '炎症因子释放和感染活动度参考' },
    { code: 'IgG', name: '免疫球蛋白G', unit: 'g/L', range: '7.0-16.0', low: 7, high: 16, defaultValue: 10.8, meaning: '体液免疫功能评估' },
    { code: 'IgA', name: '免疫球蛋白A', unit: 'g/L', range: '0.7-4.0', low: 0.7, high: 4, defaultValue: 2.1, meaning: '黏膜免疫和慢性炎症参考' },
    { code: 'IgM', name: '免疫球蛋白M', unit: 'g/L', range: '0.4-2.3', low: 0.4, high: 2.3, defaultValue: 1.3, meaning: '近期感染、免疫应答及自身免疫参考' },
    { code: 'C3', name: '补体C3', unit: 'g/L', range: '0.9-1.8', low: 0.9, high: 1.8, defaultValue: 0.82, meaning: '补体消耗、免疫复合物疾病参考' },
    { code: 'C4', name: '补体C4', unit: 'g/L', range: '0.1-0.4', low: 0.1, high: 0.4, defaultValue: 0.18, meaning: '补体系统活性和自身免疫疾病参考' },
    { code: 'RF', name: '类风湿因子', unit: 'IU/mL', range: '0-20', low: 0, high: 20, defaultValue: 28, meaning: '类风湿关节炎及其他自身免疫病辅助判断' }
  ]
}
</script>
