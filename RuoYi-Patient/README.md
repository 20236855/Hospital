# 若依患者端

## 项目简介

基于 Vue3 + Vite + Vant 的手机自适应患者端，实现线上挂号、查看电子病历等功能。

## 技术栈

- Vue 3
- Vite 6
- Vant 4
- Vue Router 4
- Pinia
- Axios

## 项目结构

```
RuoYi-Patient/
├── public/
├── src/
│   ├── api/           # API 接口
│   │   ├── login.js
│   │   ├── patient.js
│   │   ├── register.js
│   │   └── emr.js
│   ├── router/        # 路由配置
│   │   └── index.js
│   ├── styles/        # 样式文件
│   │   └── index.scss
│   ├── utils/         # 工具函数
│   │   └── request.js
│   ├── views/         # 页面
│   │   ├── Home.vue
│   │   ├── Login.vue
│   │   ├── Register.vue
│   │   ├── Record.vue
│   │   └── Profile.vue
│   ├── App.vue
│   └── main.js
├── index.html
├── package.json
├── vite.config.js
├── .env.development
└── .env.production
```

## 快速开始

### 安装依赖

```bash
cd RuoYi-Patient
npm install
```

### 开发运行

```bash
npm run dev
```

访问 http://localhost:81

### 生产构建

```bash
npm run build:prod
```

## 功能模块

- 登录
- 首页
- 预约挂号
- 电子病历查看
- 个人中心

## 后端接口

所有请求通过 RuoYi-Cloud 的 Gateway（8080端口）转发。

API 前缀：
- `/dev-api` - 开发环境
- `/prod-api` - 生产环境

主要业务接口：
- `/auth/login` - 登录
- `/hisdoctor/doctor/list` - 医生列表
- `/hisdoctor/schedule/list` - 排班列表
- `/register/register` - 挂号
- `/emr/record/list` - 病历列表
