-- ----------------------------
-- 检查/检验结果表
-- 用途：
-- 1. 检查类：颅脑CT、胸部CT、皮肤检测等，可一张图片一条记录
-- 2. 检验类：血常规、免疫检测等，可一个检验指标一条记录
-- 3. 同一个 apply_id 可对应多条结果记录，按 sort 排序展示
-- ----------------------------

DROP TABLE IF EXISTS `exam_result`;

CREATE TABLE `exam_result` (
  `result_id` bigint NOT NULL AUTO_INCREMENT COMMENT '结果ID',
  `apply_id` bigint NOT NULL COMMENT '关联exam_apply主键',

  `result_type` varchar(32) DEFAULT NULL COMMENT '结果类型：CHECK检查 LAB检验',
  `item_type` varchar(32) DEFAULT NULL COMMENT '明细类型：IMAGE图片 ITEM指标 TEXT文本',
  `item_code` varchar(64) DEFAULT NULL COMMENT '项目编码，如WBC/RBC/HGB',
  `item_name` varchar(128) DEFAULT NULL COMMENT '项目名称，如白细胞计数、胸部CT图片',

  `result_value` varchar(255) DEFAULT NULL COMMENT '结果值',
  `result_unit` varchar(64) DEFAULT NULL COMMENT '单位',
  `reference_range` varchar(128) DEFAULT NULL COMMENT '参考范围',
  `abnormal_flag` char(1) DEFAULT NULL COMMENT '异常标识：0正常 1偏高 2偏低 3阳性 4阴性',

  `image_url` varchar(512) DEFAULT NULL COMMENT '图片访问地址',
  `image_find` text COMMENT '影像所见/检查所见',
  `diagnosis_opinion` text COMMENT '诊断意见/检验意见',
  `diagnosis_result` varchar(64) DEFAULT NULL COMMENT '结论分类',
  `suggestion` text COMMENT '诊疗建议',

  `sort` int DEFAULT 0 COMMENT '排序号',
  `status` char(1) DEFAULT '0' COMMENT '状态：0草稿 1已发布',
  `doctor_id` bigint DEFAULT NULL COMMENT '检查/诊断医生ID',
  `report_time` datetime DEFAULT NULL COMMENT '报告时间',

  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',

  PRIMARY KEY (`result_id`) USING BTREE,
  KEY `idx_exam_result_apply_id` (`apply_id`) USING BTREE,
  KEY `idx_exam_result_type` (`result_type`, `item_type`) USING BTREE,
  KEY `idx_exam_result_doctor_id` (`doctor_id`) USING BTREE,
  CONSTRAINT `fk_exam_result_apply` FOREIGN KEY (`apply_id`) REFERENCES `exam_apply` (`apply_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='检查检验结果表' ROW_FORMAT=Dynamic;
