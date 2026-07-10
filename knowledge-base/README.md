# 医院知识库（knowledge-base）

本目录是「云医智联」医院知识库的**单一事实源**，用于支撑基于 Chroma 向量数据库的 RAG 检索增强问答。原始知识以 Markdown（`.md`）维护，由 `scripts/build_index.py` 切分、向量化后写入 Chroma。

## 目录结构

```
knowledge-base/
├─ registration/   挂号流程（预约/现场/退号/改签）
├─ resources/      医院资源（科室介绍、检查项目、报告查询、导诊）
├─ policy/         医院政策（医保、病历、隐私、上级政策解读）
├─ faq/            高频问答
├─ scripts/        入库脚本 build_index.py（读 .md → 切分 → 嵌入 → 写 Chroma）
└─ chroma/         Chroma 持久化目录（由脚本生成，已 gitignore）
```

## 单篇知识文件规范

每篇 `.md` 文件开头使用 YAML frontmatter 写入结构化元数据，供 Chroma 做过滤检索：

```markdown
---
title: 文章标题
category: 挂号流程 | 医院资源 | 医院政策 | 高频问答
department: 责任部门或来源
source: 来源（如 国家卫生健康委网站、院内发文）
publish_date: 2026-04-01
effective_date: 2026-07-01
version: v1
tags: [关键词1, 关键词2]
---

# 正文（用 Markdown 标题层级组织，标题即语义切分边界）
```

## 维护约定

- 知识源 `.md` 纳入 git 版本控制；`chroma/` 为派生产物，不提交。
- 政策类文件请保留 `effective_date` 与 `source`，便于过期过滤与溯源引用。
- 新增知识：在对应分类目录新建 `.md` 并按上述规范填写，随后运行入库脚本重建索引。

## 入库流程（概要）

1. 遍历各分类目录下的 `.md`；
2. 解析 frontmatter 作为 metadata；
3. 按 Markdown 标题切块；
4. 用 Embedding 模型向量化；
5. 写入 Chroma collection（`documents=正文, metadatas=frontmatter, ids=文件+块号`）。

> 入库脚本 `scripts/build_index.py` 待后续实现。
