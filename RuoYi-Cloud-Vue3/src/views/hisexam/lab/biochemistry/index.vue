<template>
  <LabResultPage :config="config" />
</template>

<script setup>
import LabResultPage from '../common/LabResultPage.vue'

const config = {
  title: '生化全套检验',
  category: '临床化学',
  method: '血清样本经离心后进行肝功能、肾功能、糖脂代谢和电解质多模块检测。',
  instrument: 'Roche Cobas c702 全自动生化分析仪',
  samplePrefix: 'BIO',
  specimens: ['血清', '肝素抗凝血浆'],
  tests: [
    { code: 'ALT', name: '丙氨酸氨基转移酶', unit: 'U/L', range: '7-40', low: 7, high: 40, defaultValue: 56, meaning: '肝细胞损伤敏感指标' },
    { code: 'AST', name: '天门冬氨酸氨基转移酶', unit: 'U/L', range: '13-35', low: 13, high: 35, defaultValue: 42, meaning: '肝细胞、心肌和骨骼肌损伤参考' },
    { code: 'TBIL', name: '总胆红素', unit: 'umol/L', range: '3.4-20.5', low: 3.4, high: 20.5, defaultValue: 18.6, meaning: '黄疸、胆汁淤积及肝代谢功能评估' },
    { code: 'ALB', name: '白蛋白', unit: 'g/L', range: '40-55', low: 40, high: 55, defaultValue: 39.2, meaning: '营养状态、肝合成功能和炎症消耗评估' },
    { code: 'CREA', name: '肌酐', unit: 'umol/L', range: '45-84', low: 45, high: 84, criticalHigh: 400, defaultValue: 76, meaning: '肾小球滤过功能评估' },
    { code: 'UREA', name: '尿素', unit: 'mmol/L', range: '2.9-7.1', low: 2.9, high: 7.1, defaultValue: 6.8, meaning: '肾功能、蛋白代谢和脱水状态参考' },
    { code: 'GLU', name: '葡萄糖', unit: 'mmol/L', range: '3.9-6.1', low: 3.9, high: 6.1, criticalLow: 2.8, criticalHigh: 22.2, defaultValue: 7.4, meaning: '糖代谢异常筛查和糖尿病管理' },
    { code: 'TC', name: '总胆固醇', unit: 'mmol/L', range: '0-5.2', low: 0, high: 5.2, defaultValue: 5.8, meaning: '动脉粥样硬化风险评估' },
    { code: 'TG', name: '甘油三酯', unit: 'mmol/L', range: '0-1.7', low: 0, high: 1.7, defaultValue: 2.1, meaning: '脂代谢异常和胰腺炎风险参考' }
  ]
}
</script>
