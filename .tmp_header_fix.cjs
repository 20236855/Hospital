const fs = require('fs');
const base = 'c:/Users/Admin/Desktop/实训/hosipital/code/RuoYi-Patient/src/views';

// ===== 1. 四页全局绿色残留清理 =====
const files = ['MyExam.vue', 'MyAppointments.vue', 'DoctorList.vue', 'HealthNews.vue'];
const colorMap = [
  // 青绿/绿色 → 蓝色/蓝灰
  ['#7e98a4', '#64748b'],
  ['#f2f7f8', '#f0f5ff'],
  ['#e6f5f5', '#eff6ff'],
  ['#eaf5f3', '#eff6ff'],
  ['#e0f7f4', '#e0efff'],
  ['#d1f2eb', '#dbeafe'],
  ['#a8e6c8', '#93c5fd'],
  ['#6fdcbf', '#7bb3e8'],
  ['#4fd1b8', '#60a5fa'],
  ['#38b2ac', '#3b82f6'],
  ['#319795', '#2563eb'],
  ['#2c7a7b', '#1d4ed8'],
  ['#285e61', '#1e40af'],
  ['rgba(81, 137, 151, .13)', 'rgba(30, 58, 95, .13)'],
  ['rgba(81, 137, 151, .1)', 'rgba(30, 58, 95, .1)'],
  ['rgba(8, 64, 58, .28)', 'rgba(15, 40, 80, .28)'],
  ['rgba(239, 255, 250, .35)', 'rgba(255, 255, 255, .35)'],
  ['rgba(230, 255, 247, .2)', 'rgba(255, 255, 255, .2)'],
  ['rgba(236, 255, 249, .1)', 'rgba(255, 255, 255, .1)'],
  ['rgba(245, 255, 252, .82)', 'rgba(255, 255, 255, .82)'],
  // HealthNews 绿色渐变
  ['linear-gradient(145deg, #1e3a5f 0%, #2563eb 58%, #93c5fd 100%)', 'linear-gradient(145deg, #1e3a5f 0%, #2563eb 58%, #93c5fd 100%)'],
];

for (const file of files) {
  const fp = base + '/' + file;
  if (!fs.existsSync(fp)) { console.log('SKIP: ' + file); continue; }
  let content = fs.readFileSync(fp, 'utf8');
  for (const [o, n] of colorMap) {
    if (o !== n) content = content.split(o).join(n);
  }
  fs.writeFileSync(fp, content, 'utf8');
  console.log('COLOR: ' + file);
}

// ===== 2. 给 MyExam / MyAppointments / DoctorList 头部加专业蓝色背景 =====
// MyExam.vue
{
  let fp = base + '/MyExam.vue';
  let content = fs.readFileSync(fp, 'utf8');
  // 修改 .page-header 为蓝色头部
  content = content.replace(
    /\.page-header\s*\{[\s\S]*?\n\}/,
    `.page-header {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 14px 18px;
  background: linear-gradient(135deg, #1e3a5f 0%, #2563eb 100%);
  color: #fff;

  p {
    margin: 0 0 2px;
    color: rgba(255, 255, 255, .75);
    font-size: 10px;
    font-weight: 800;
    letter-spacing: 0;
  }

  h1 {
    margin: 0;
    color: #fff;
    font-size: 24px;
    line-height: 1.2;
  }
}`
  );
  // 修改 .back-btn 为白色透明按钮
  content = content.replace(
    /\.back-btn\s*\{[\s\S]*?\n\}/,
    `.back-btn {
  width: 38px;
  height: 38px;
  border: 0;
  border-radius: 10px;
  background: rgba(255, 255, 255, .15);
  color: #fff;
  backdrop-filter: blur(4px);
  box-shadow: 0 4px 12px rgba(15, 40, 80, .2);
}`
  );
  fs.writeFileSync(fp, content, 'utf8');
  console.log('HEADER: MyExam.vue');
}

// MyAppointments.vue
{
  let fp = base + '/MyAppointments.vue';
  let content = fs.readFileSync(fp, 'utf8');
  content = content.replace(
    /\.header-section\s*\{[\s\S]*?\n\}/,
    `.header-section {
  padding: 16px 14px 18px;
  display: flex;
  align-items: center;
  gap: 16px;
  background: linear-gradient(135deg, #1e3a5f 0%, #2563eb 100%);
  color: #fff;
}`
  );
  content = content.replace(
    /\.header-back\s*\{[\s\S]*?\n\s*\}\n\s*\n\.header-title\s*\{[\s\S]*?\n\s*\}/,
    `.header-back {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: rgba(255, 255, 255, .15);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(4px);
  box-shadow: 0 4px 12px rgba(15, 40, 80, .2);

  &:active {
    transform: scale(0.96);
    background: rgba(255, 255, 255, .25);
  }

  .van-icon {
    color: #fff;
    font-size: 20px;
  }
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
  h1 { font-size: 24px; font-weight: 700; color: #fff; margin: 0; }
}`
  );
  fs.writeFileSync(fp, content, 'utf8');
  console.log('HEADER: MyAppointments.vue');
}

// DoctorList.vue
{
  let fp = base + '/DoctorList.vue';
  let content = fs.readFileSync(fp, 'utf8');
  content = content.replace(
    /\.top-bar\s*\{[\s\S]*?\n\}/,
    `.top-bar {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 14px 18px;
  background: linear-gradient(135deg, #1e3a5f 0%, #2563eb 100%);
  color: #fff;

  p {
    margin: 0 0 2px;
    font-size: 10px;
    font-weight: 800;
    color: rgba(255, 255, 255, .75);
    letter-spacing: 0;
  }

  h1 {
    margin: 0;
    font-size: 24px;
    line-height: 1.2;
    color: #fff;
  }
}`
  );
  content = content.replace(
    /\.back-btn\s*\{[\s\S]*?\n\}/,
    `.back-btn {
  width: 38px;
  height: 38px;
  border: 0;
  border-radius: 10px;
  background: rgba(255, 255, 255, .15);
  color: #fff;
  backdrop-filter: blur(4px);
  box-shadow: 0 4px 12px rgba(15, 40, 80, .2);
}`
  );
  fs.writeFileSync(fp, content, 'utf8');
  console.log('HEADER: DoctorList.vue');
}

// HealthNews.vue: 只去掉按钮绿背景
{
  let fp = base + '/HealthNews.vue';
  let content = fs.readFileSync(fp, 'utf8');
  content = content.replace(
    /background:\s*rgba\(8,\s*64,\s*58,\s*\.28\);/,
    'background: rgba(15, 40, 80, .28);'
  );
  content = content.replace(
    /border:\s*1px\s+solid\s+rgba\(239,\s*255,\s*250,\s*\.35\);/,
    'border: 1px solid rgba(255, 255, 255, .35);'
  );
  // 装饰圆环改成白色
  content = content.replace(
    /border:\s*1px\s+solid\s+rgba\(230,\s*255,\s*247,\s*\.2\);/,
    'border: 1px solid rgba(255, 255, 255, .2);'
  );
  content = content.replace(
    /background:\s*rgba\(236,\s*255,\s*249,\s*\.1\);/,
    'background: rgba(255, 255, 255, .1);'
  );
  content = content.replace(
    /color:\s*rgba\(245,\s*255,\s*252,\s*\.82\);/,
    'color: rgba(255, 255, 255, .82);'
  );
  fs.writeFileSync(fp, content, 'utf8');
  console.log('HEADER: HealthNews.vue');
}

console.log('All done!');
