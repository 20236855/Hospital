/*
 Navicat Premium Dump SQL

 Source Server         : mysql
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : localhost:3306
 Source Schema         : ry-cloud

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 04/06/2026 11:54:45
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for check_in
-- ----------------------------
DROP TABLE IF EXISTS `check_in`;
CREATE TABLE `check_in`  (
  `check_in_id` bigint NOT NULL AUTO_INCREMENT COMMENT '签到ID',
  `register_id` bigint NOT NULL COMMENT '挂号ID',
  `queue_no` int NULL DEFAULT NULL COMMENT '排队号',
  `check_in_time` datetime NULL DEFAULT NULL COMMENT '签到时间',
  `status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '签到状态',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`check_in_id`) USING BTREE,
  INDEX `idx_check_in_register`(`register_id` ASC) USING BTREE,
  CONSTRAINT `fk_check_in_register` FOREIGN KEY (`register_id`) REFERENCES `register` (`register_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '签到表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of check_in
-- ----------------------------

-- ----------------------------
-- Table structure for disease
-- ----------------------------
DROP TABLE IF EXISTS `disease`;
CREATE TABLE `disease`  (
  `disease_id` bigint NOT NULL AUTO_INCREMENT COMMENT '疾病ID',
  `disease_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '疾病编码',
  `disease_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '疾病名称',
  `icd_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'ICD编码',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '疾病描述',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '状态',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`disease_id`) USING BTREE,
  INDEX `idx_disease_name`(`disease_name` ASC) USING BTREE,
  INDEX `idx_icd_code`(`icd_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '疾病字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of disease
-- ----------------------------

-- ----------------------------
-- Table structure for doctor
-- ----------------------------
DROP TABLE IF EXISTS `doctor`;
CREATE TABLE `doctor`  (
  `doctor_id` bigint NOT NULL AUTO_INCREMENT COMMENT '医生ID',
  `user_id` bigint NOT NULL COMMENT '系统用户ID（关联若依sys_user）',
  `dept_id` bigint NOT NULL COMMENT '所属科室（关联若依sys_dept）',
  `doctor_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '医生工号',
  `doctor_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '医生姓名',
  `gender` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '性别',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '职称',
  `specialty` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '擅长领域',
  `outpatient_fee` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '门诊挂号费',
  `introduction` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '医生简介',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '状态',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`doctor_id`) USING BTREE,
  UNIQUE INDEX `user_id`(`user_id` ASC) USING BTREE,
  UNIQUE INDEX `doctor_no`(`doctor_no` ASC) USING BTREE,
  INDEX `idx_doctor_dept`(`dept_id` ASC) USING BTREE,
  INDEX `idx_doctor_name`(`doctor_name` ASC) USING BTREE,
  CONSTRAINT `fk_doctor_dept` FOREIGN KEY (`dept_id`) REFERENCES `sys_dept` (`dept_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_doctor_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '医生表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of doctor
-- ----------------------------

-- ----------------------------
-- Table structure for drug
-- ----------------------------
DROP TABLE IF EXISTS `drug`;
CREATE TABLE `drug`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '药品ID(主键)',
  `drug_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '药品编码',
  `drug_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '药品名称',
  `py_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拼音码(检索用)',
  `spec` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '药品规格',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '包装单位',
  `drug_price` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '单价',
  `manufacturer` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '生产厂家',
  `drug_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '药剂类型',
  `sort` int NULL DEFAULT 0 COMMENT '排序号',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '状态 0正常 1停用',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_drug_code`(`drug_code` ASC) USING BTREE,
  INDEX `idx_drug_name`(`drug_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '药品信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of drug
-- ----------------------------

-- ----------------------------
-- Table structure for encounter
-- ----------------------------
DROP TABLE IF EXISTS `encounter`;
CREATE TABLE `encounter`  (
  `encounter_id` bigint NOT NULL AUTO_INCREMENT COMMENT '接诊ID',
  `register_id` bigint NOT NULL COMMENT '挂号ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `doctor_id` bigint NOT NULL COMMENT '医生ID',
  `dept_id` bigint NOT NULL COMMENT '科室ID',
  `encounter_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '接诊类型',
  `encounter_status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'waiting' COMMENT '接诊状态',
  `visit_time` datetime NULL DEFAULT NULL COMMENT '接诊时间',
  `finish_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`encounter_id`) USING BTREE,
  INDEX `fk_encounter_register`(`register_id` ASC) USING BTREE,
  INDEX `fk_encounter_dept`(`dept_id` ASC) USING BTREE,
  INDEX `idx_encounter_patient`(`patient_id` ASC) USING BTREE,
  INDEX `idx_encounter_doctor`(`doctor_id` ASC) USING BTREE,
  INDEX `idx_encounter_status`(`encounter_status` ASC) USING BTREE,
  CONSTRAINT `fk_encounter_dept` FOREIGN KEY (`dept_id`) REFERENCES `sys_dept` (`dept_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_encounter_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`doctor_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_encounter_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_encounter_register` FOREIGN KEY (`register_id`) REFERENCES `register` (`register_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '接诊表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of encounter
-- ----------------------------

-- ----------------------------
-- Table structure for exam_apply
-- ----------------------------
DROP TABLE IF EXISTS `exam_apply`;
CREATE TABLE `exam_apply`  (
  `apply_id` bigint NOT NULL AUTO_INCREMENT COMMENT '申请ID(主键)',
  `register_id` bigint NOT NULL COMMENT '挂号ID',
  `encounter_id` bigint NOT NULL COMMENT '接诊ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `doctor_id` bigint NOT NULL COMMENT '开单医生ID',
  `dept_id` bigint NOT NULL COMMENT '开单科室ID',
  `apply_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型：CHECK检查/INSPEC检验/DISPOSAL处置',
  `tech_id` bigint NOT NULL COMMENT '医技项目ID',
  `apply_info` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目的要求',
  `apply_position` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '检查/检验/处置部位',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '执行人员ID',
  `inputer_id` bigint NULL DEFAULT NULL COMMENT '结果录入人员ID',
  `apply_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开立时间',
  `exam_time` datetime NULL DEFAULT NULL COMMENT '执行时间',
  `exam_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '执行结果',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'CT/影像文件访问地址，检验、处置类可留空',
  `apply_status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PENDING' COMMENT '状态：待缴费/待执行/已完成',
  `pay_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'unpaid' COMMENT '支付状态',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`apply_id`) USING BTREE,
  INDEX `patient_id`(`patient_id` ASC) USING BTREE,
  INDEX `doctor_id`(`doctor_id` ASC) USING BTREE,
  INDEX `tech_id`(`tech_id` ASC) USING BTREE,
  INDEX `idx_register_id`(`register_id` ASC) USING BTREE,
  INDEX `idx_encounter_id`(`encounter_id` ASC) USING BTREE,
  INDEX `idx_apply_type`(`apply_type` ASC) USING BTREE,
  CONSTRAINT `exam_apply_ibfk_1` FOREIGN KEY (`register_id`) REFERENCES `register` (`register_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `exam_apply_ibfk_2` FOREIGN KEY (`encounter_id`) REFERENCES `encounter` (`encounter_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `exam_apply_ibfk_3` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `exam_apply_ibfk_4` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`doctor_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `exam_apply_ibfk_5` FOREIGN KEY (`tech_id`) REFERENCES `medical_technology` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '检查/检验/处置申请单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of exam_apply
-- ----------------------------

-- ----------------------------
-- Table structure for gen_table
-- ----------------------------
DROP TABLE IF EXISTS `gen_table`;
CREATE TABLE `gen_table`  (
  `table_id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '表名称',
  `table_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '表描述',
  `sub_table_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '关联子表的表名',
  `sub_table_fk_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '子表关联的外键名',
  `class_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '实体类名称',
  `tpl_category` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'crud' COMMENT '使用的模板（crud单表操作 tree树表操作）',
  `tpl_web_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '前端模板类型（element-ui模版 element-plus模版）',
  `package_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成包路径',
  `module_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成模块名',
  `business_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成业务名',
  `function_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成功能名',
  `function_author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成功能作者',
  `form_col_num` int NULL DEFAULT 1 COMMENT '表单布局（单列 双列 三列）',
  `gen_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '生成代码方式（0zip压缩包 1自定义路径）',
  `gen_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '/' COMMENT '生成路径（不填默认项目路径）',
  `options` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '其它生成选项',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`table_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '代码生成业务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gen_table
-- ----------------------------
INSERT INTO `gen_table` VALUES (1, 'doctor', '医生表', NULL, NULL, 'Doctor', 'crud', 'element-plus', 'com.ruoyi.hisdoctor', 'hisdoctor', 'doctor', '医生信息', 'ruoyi', 1, '0', '/', '{\"genView\":\"0\",\"parentMenuId\":2000}', 'admin', '2026-05-29 14:52:20', '', '2026-05-30 23:14:18', NULL);
INSERT INTO `gen_table` VALUES (2, 'drug', '药品信息表', NULL, NULL, 'Drug', 'crud', 'element-plus', 'com.ruoyi.hisprescription', 'hisprescription', 'drug', '药品信息', 'ruoyi', 1, '0', '/', '{\"genView\":\"0\",\"parentMenuId\":2007}', 'admin', '2026-06-02 11:18:35', '', '2026-06-02 11:39:22', NULL);
INSERT INTO `gen_table` VALUES (3, 'prescription', '处方主表', NULL, NULL, 'Prescription', 'crud', 'element-plus', 'com.ruoyi.hisprescription', 'hisprescription', 'prescription', '处方主', 'ruoyi', 1, '0', '/', '{\"genView\":\"0\",\"parentMenuId\":2007}', 'admin', '2026-06-02 11:18:35', '', '2026-06-02 16:16:13', NULL);
INSERT INTO `gen_table` VALUES (4, 'prescription_item', '处方明细表', NULL, NULL, 'PrescriptionItem', 'crud', 'element-plus', 'com.ruoyi.hisprescription', 'hisprescription', 'item', '处方明细', 'ruoyi', 1, '0', '/', '{\"genView\":\"0\",\"parentMenuId\":2007}', 'admin', '2026-06-02 11:18:35', '', '2026-06-02 16:17:04', NULL);
INSERT INTO `gen_table` VALUES (5, 'schedule', '医生排班表', NULL, NULL, 'Schedule', 'crud', 'element-plus', 'com.ruoyi.hisdoctor', 'hisdoctor', 'schedule', '医生排班', 'ruoyi', 1, '0', '/', '{\"genView\":\"0\",\"parentMenuId\":2000}', 'admin', '2026-06-02 11:18:53', '', '2026-06-02 15:35:11', NULL);
INSERT INTO `gen_table` VALUES (6, 'regist_level', '挂号级别表', NULL, NULL, 'RegistLevel', 'crud', 'element-plus', 'com.ruoyi.hisdoctor', 'hisdoctor', 'doctorlevel', '挂号级别', 'ruoyi', 1, '0', '/', '{\"genView\":\"0\",\"parentMenuId\":2000}', 'admin', '2026-06-02 11:19:09', '', '2026-06-02 15:54:18', NULL);
INSERT INTO `gen_table` VALUES (7, 'exam_apply', '检查/检验/处置申请单', NULL, NULL, 'ExamApply', 'crud', 'element-plus', 'com.ruoyi.hisexam', 'hisexam', 'apply', '检查/检验/处置申请单', 'ruoyi', 1, '0', '/', '{\"genView\":\"0\",\"parentMenuId\":2014}', 'admin', '2026-06-02 14:25:24', '', '2026-06-02 14:35:25', NULL);
INSERT INTO `gen_table` VALUES (8, 'medical_technology', '医技项目表', NULL, NULL, 'MedicalTechnology', 'crud', 'element-plus', 'com.ruoyi.hisexam', 'hisexam', 'technology', '医技项目', 'ruoyi', 1, '0', '/', '{\"genView\":\"0\",\"parentMenuId\":2014}', 'admin', '2026-06-02 14:25:56', '', '2026-06-02 14:32:32', NULL);

-- ----------------------------
-- Table structure for gen_table_column
-- ----------------------------
DROP TABLE IF EXISTS `gen_table_column`;
CREATE TABLE `gen_table_column`  (
  `column_id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_id` bigint NULL DEFAULT NULL COMMENT '归属表编号',
  `column_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '列名称',
  `column_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '列描述',
  `column_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '列类型',
  `java_type` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'JAVA类型',
  `java_field` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'JAVA字段名',
  `is_pk` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否主键（1是）',
  `is_increment` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否自增（1是）',
  `is_required` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否必填（1是）',
  `is_insert` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否为插入字段（1是）',
  `is_edit` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否编辑字段（1是）',
  `is_list` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否列表字段（1是）',
  `is_query` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否查询字段（1是）',
  `query_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'EQ' COMMENT '查询方式（等于、不等于、大于、小于、范围）',
  `html_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
  `dict_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典类型',
  `sort` int NULL DEFAULT NULL COMMENT '排序',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`column_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 106 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '代码生成业务表字段' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gen_table_column
-- ----------------------------
INSERT INTO `gen_table_column` VALUES (1, 1, 'doctor_id', '医生ID', 'bigint', 'Long', 'doctorId', '1', '1', NULL, '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2026-05-29 14:52:20', '', '2026-05-30 23:14:18');
INSERT INTO `gen_table_column` VALUES (2, 1, 'user_id', '系统用户ID（关联若依sys_user）', 'bigint', 'Long', 'userId', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2026-05-29 14:52:20', '', '2026-05-30 23:14:18');
INSERT INTO `gen_table_column` VALUES (3, 1, 'dept_id', '所属科室（关联若依sys_dept）', 'bigint', 'Long', 'deptId', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 3, 'admin', '2026-05-29 14:52:20', '', '2026-05-30 23:14:18');
INSERT INTO `gen_table_column` VALUES (4, 1, 'doctor_no', '医生工号', 'varchar(50)', 'String', 'doctorNo', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 4, 'admin', '2026-05-29 14:52:20', '', '2026-05-30 23:14:18');
INSERT INTO `gen_table_column` VALUES (5, 1, 'doctor_name', '医生姓名', 'varchar(100)', 'String', 'doctorName', '0', '0', '1', '1', '1', '1', '1', 'LIKE', 'input', '', 5, 'admin', '2026-05-29 14:52:20', '', '2026-05-30 23:14:18');
INSERT INTO `gen_table_column` VALUES (6, 1, 'gender', '性别', 'char(1)', 'String', 'gender', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'input', '', 6, 'admin', '2026-05-29 14:52:20', '', '2026-05-30 23:14:18');
INSERT INTO `gen_table_column` VALUES (7, 1, 'phone', '手机号', 'varchar(20)', 'String', 'phone', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'input', '', 7, 'admin', '2026-05-29 14:52:20', '', '2026-05-30 23:14:18');
INSERT INTO `gen_table_column` VALUES (8, 1, 'title', '职称', 'varchar(100)', 'String', 'title', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'input', '', 8, 'admin', '2026-05-29 14:52:20', '', '2026-05-30 23:14:18');
INSERT INTO `gen_table_column` VALUES (9, 1, 'specialty', '擅长领域', 'text', 'String', 'specialty', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'textarea', '', 9, 'admin', '2026-05-29 14:52:20', '', '2026-05-30 23:14:18');
INSERT INTO `gen_table_column` VALUES (10, 1, 'outpatient_fee', '门诊挂号费', 'decimal(10,2)', 'BigDecimal', 'outpatientFee', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'input', '', 10, 'admin', '2026-05-29 14:52:20', '', '2026-05-30 23:14:18');
INSERT INTO `gen_table_column` VALUES (11, 1, 'introduction', '医生简介', 'text', 'String', 'introduction', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'textarea', '', 11, 'admin', '2026-05-29 14:52:20', '', '2026-05-30 23:14:18');
INSERT INTO `gen_table_column` VALUES (12, 1, 'avatar', '头像', 'varchar(255)', 'String', 'avatar', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'input', '', 12, 'admin', '2026-05-29 14:52:20', '', '2026-05-30 23:14:18');
INSERT INTO `gen_table_column` VALUES (13, 1, 'status', '状态', 'char(1)', 'String', 'status', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'radio', '', 13, 'admin', '2026-05-29 14:52:20', '', '2026-05-30 23:14:18');
INSERT INTO `gen_table_column` VALUES (14, 1, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', NULL, '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 14, 'admin', '2026-05-29 14:52:20', '', '2026-05-30 23:14:18');
INSERT INTO `gen_table_column` VALUES (15, 1, 'update_time', '更新时间', 'datetime', 'Date', 'updateTime', '0', '0', NULL, '1', '1', NULL, NULL, 'EQ', 'datetime', '', 15, 'admin', '2026-05-29 14:52:20', '', '2026-05-30 23:14:18');
INSERT INTO `gen_table_column` VALUES (16, 2, 'id', '药品ID(主键)', 'bigint', 'Long', 'id', '1', '1', NULL, '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 11:39:22');
INSERT INTO `gen_table_column` VALUES (17, 2, 'drug_code', '药品编码', 'varchar(64)', 'String', 'drugCode', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 11:39:22');
INSERT INTO `gen_table_column` VALUES (18, 2, 'drug_name', '药品名称', 'varchar(100)', 'String', 'drugName', '0', '0', '1', '1', '1', '1', '1', 'LIKE', 'input', '', 3, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 11:39:22');
INSERT INTO `gen_table_column` VALUES (19, 2, 'py_code', '拼音码(检索用)', 'varchar(64)', 'String', 'pyCode', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'input', '', 4, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 11:39:22');
INSERT INTO `gen_table_column` VALUES (20, 2, 'spec', '药品规格', 'varchar(64)', 'String', 'spec', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'input', '', 5, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 11:39:22');
INSERT INTO `gen_table_column` VALUES (21, 2, 'unit', '包装单位', 'varchar(20)', 'String', 'unit', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'input', '', 6, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 11:39:22');
INSERT INTO `gen_table_column` VALUES (22, 2, 'drug_price', '单价', 'decimal(10,2)', 'BigDecimal', 'drugPrice', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'input', '', 7, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 11:39:22');
INSERT INTO `gen_table_column` VALUES (23, 2, 'manufacturer', '生产厂家', 'varchar(255)', 'String', 'manufacturer', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'input', '', 8, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 11:39:22');
INSERT INTO `gen_table_column` VALUES (24, 2, 'drug_type', '药剂类型', 'varchar(32)', 'String', 'drugType', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'select', '', 9, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 11:39:22');
INSERT INTO `gen_table_column` VALUES (25, 2, 'sort', '排序号', 'int', 'Long', 'sort', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'input', '', 10, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 11:39:22');
INSERT INTO `gen_table_column` VALUES (26, 2, 'status', '状态 0正常 1停用', 'char(1)', 'String', 'status', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'radio', '', 11, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 11:39:22');
INSERT INTO `gen_table_column` VALUES (27, 2, 'remark', '备注', 'varchar(512)', 'String', 'remark', '0', '0', NULL, '1', '1', '1', NULL, 'EQ', 'textarea', '', 12, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 11:39:22');
INSERT INTO `gen_table_column` VALUES (28, 2, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', NULL, '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 13, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 11:39:22');
INSERT INTO `gen_table_column` VALUES (29, 2, 'update_time', '更新时间', 'datetime', 'Date', 'updateTime', '0', '0', NULL, '1', '1', NULL, NULL, 'EQ', 'datetime', '', 14, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 11:39:22');
INSERT INTO `gen_table_column` VALUES (30, 3, 'prescription_id', '处方ID(主键)', 'bigint', 'Long', 'prescriptionId', '1', '1', NULL, '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 16:16:13');
INSERT INTO `gen_table_column` VALUES (31, 3, 'register_id', '挂号ID', 'bigint', 'Long', 'registerId', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 16:16:13');
INSERT INTO `gen_table_column` VALUES (32, 3, 'encounter_id', '接诊ID', 'bigint', 'Long', 'encounterId', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 3, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 16:16:13');
INSERT INTO `gen_table_column` VALUES (33, 3, 'patient_id', '患者ID', 'bigint', 'Long', 'patientId', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 4, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 16:16:13');
INSERT INTO `gen_table_column` VALUES (34, 3, 'doctor_id', '开方医生ID', 'bigint', 'Long', 'doctorId', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 5, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 16:16:13');
INSERT INTO `gen_table_column` VALUES (35, 3, 'dept_id', '开方科室ID', 'bigint', 'Long', 'deptId', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 6, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 16:16:13');
INSERT INTO `gen_table_column` VALUES (36, 3, 'prescription_no', '处方单号', 'varchar(50)', 'String', 'prescriptionNo', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 7, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 16:16:13');
INSERT INTO `gen_table_column` VALUES (37, 3, 'total_amount', '处方总金额', 'decimal(10,2)', 'BigDecimal', 'totalAmount', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'input', '', 8, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 16:16:13');
INSERT INTO `gen_table_column` VALUES (38, 3, 'prescription_status', '状态：待缴费/已缴费/已发药/已退方', 'varchar(30)', 'String', 'prescriptionStatus', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'radio', '', 9, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 16:16:13');
INSERT INTO `gen_table_column` VALUES (39, 3, 'pay_status', '支付状态', 'varchar(20)', 'String', 'payStatus', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'radio', '', 10, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 16:16:13');
INSERT INTO `gen_table_column` VALUES (40, 3, 'drug_tip', '整体用药嘱托', 'varchar(512)', 'String', 'drugTip', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'textarea', '', 11, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 16:16:13');
INSERT INTO `gen_table_column` VALUES (41, 3, 'create_time', '开方时间', 'datetime', 'Date', 'createTime', '0', '0', NULL, '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 12, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 16:16:13');
INSERT INTO `gen_table_column` VALUES (42, 3, 'update_time', '更新时间', 'datetime', 'Date', 'updateTime', '0', '0', NULL, '1', '1', NULL, NULL, 'EQ', 'datetime', '', 13, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 16:16:13');
INSERT INTO `gen_table_column` VALUES (43, 4, 'item_id', '明细ID(主键)', 'bigint', 'Long', 'itemId', '1', '1', NULL, '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 16:17:04');
INSERT INTO `gen_table_column` VALUES (44, 4, 'prescription_id', '处方ID', 'bigint', 'Long', 'prescriptionId', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 16:17:04');
INSERT INTO `gen_table_column` VALUES (45, 4, 'drug_id', '药品ID', 'bigint', 'Long', 'drugId', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 3, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 16:17:04');
INSERT INTO `gen_table_column` VALUES (46, 4, 'drug_name', '药品名称', 'varchar(100)', 'String', 'drugName', '0', '0', '1', '1', '1', '1', '1', 'LIKE', 'input', '', 4, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 16:17:04');
INSERT INTO `gen_table_column` VALUES (47, 4, 'drug_price', '药品单价', 'decimal(10,2)', 'BigDecimal', 'drugPrice', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'input', '', 5, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 16:17:04');
INSERT INTO `gen_table_column` VALUES (48, 4, 'quantity', '药品数量', 'int', 'Long', 'quantity', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 6, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 16:17:04');
INSERT INTO `gen_table_column` VALUES (49, 4, 'drug_usage', '用法', 'varchar(100)', 'String', 'drugUsage', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'input', '', 7, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 16:17:04');
INSERT INTO `gen_table_column` VALUES (50, 4, 'dosage', '单次用量', 'varchar(100)', 'String', 'dosage', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'input', '', 8, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 16:17:04');
INSERT INTO `gen_table_column` VALUES (51, 4, 'frequency', '服用频次', 'varchar(100)', 'String', 'frequency', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'input', '', 9, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 16:17:04');
INSERT INTO `gen_table_column` VALUES (52, 4, 'use_days', '使用天数', 'int', 'Long', 'useDays', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'input', '', 10, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 16:17:04');
INSERT INTO `gen_table_column` VALUES (53, 4, 'item_tip', '单种药品嘱托', 'varchar(255)', 'String', 'itemTip', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'input', '', 11, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 16:17:04');
INSERT INTO `gen_table_column` VALUES (54, 4, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', NULL, '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 12, 'admin', '2026-06-02 11:18:35', '', '2026-06-02 16:17:04');
INSERT INTO `gen_table_column` VALUES (55, 5, 'schedule_id', '排班ID', 'bigint', 'Long', 'scheduleId', '1', '1', NULL, '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2026-06-02 11:18:53', '', '2026-06-02 15:35:11');
INSERT INTO `gen_table_column` VALUES (56, 5, 'doctor_id', '医生ID', 'bigint', 'Long', 'doctorId', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2026-06-02 11:18:53', '', '2026-06-02 15:35:11');
INSERT INTO `gen_table_column` VALUES (57, 5, 'schedule_date', '排班日期', 'date', 'Date', 'scheduleDate', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'datetime', '', 3, 'admin', '2026-06-02 11:18:53', '', '2026-06-02 15:35:11');
INSERT INTO `gen_table_column` VALUES (58, 5, 'time_slot', '午别（morning/afternoon/evening）', 'varchar(20)', 'String', 'timeSlot', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 4, 'admin', '2026-06-02 11:18:53', '', '2026-06-02 15:35:11');
INSERT INTO `gen_table_column` VALUES (59, 5, 'max_number', '最大挂号数', 'int', 'Long', 'maxNumber', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'input', '', 5, 'admin', '2026-06-02 11:18:53', '', '2026-06-02 15:35:11');
INSERT INTO `gen_table_column` VALUES (60, 5, 'reserved_number', '已预约人数', 'int', 'Long', 'reservedNumber', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'input', '', 6, 'admin', '2026-06-02 11:18:53', '', '2026-06-02 15:35:11');
INSERT INTO `gen_table_column` VALUES (61, 5, 'status', '状态', 'char(1)', 'String', 'status', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'radio', '', 7, 'admin', '2026-06-02 11:18:53', '', '2026-06-02 15:35:11');
INSERT INTO `gen_table_column` VALUES (62, 5, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', NULL, '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 8, 'admin', '2026-06-02 11:18:53', '', '2026-06-02 15:35:11');
INSERT INTO `gen_table_column` VALUES (63, 5, 'update_time', '更新时间', 'datetime', 'Date', 'updateTime', '0', '0', NULL, '1', '1', NULL, NULL, 'EQ', 'datetime', '', 9, 'admin', '2026-06-02 11:18:53', '', '2026-06-02 15:35:11');
INSERT INTO `gen_table_column` VALUES (64, 6, 'level_id', '级别ID(主键)', 'bigint', 'Long', 'levelId', '1', '1', NULL, '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2026-06-02 11:19:09', '', '2026-06-02 15:54:18');
INSERT INTO `gen_table_column` VALUES (65, 6, 'level_name', '级别名称：普通号/专家号/主任号', 'varchar(50)', 'String', 'levelName', '0', '0', '1', '1', '1', '1', '1', 'LIKE', 'input', '', 2, 'admin', '2026-06-02 11:19:09', '', '2026-06-02 15:54:18');
INSERT INTO `gen_table_column` VALUES (66, 6, 'fee', '对应挂号费用', 'decimal(10,2)', 'BigDecimal', 'fee', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'input', '', 3, 'admin', '2026-06-02 11:19:09', '', '2026-06-02 15:54:18');
INSERT INTO `gen_table_column` VALUES (67, 6, 'sort', '排序号', 'int', 'Long', 'sort', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'input', '', 4, 'admin', '2026-06-02 11:19:09', '', '2026-06-02 15:54:18');
INSERT INTO `gen_table_column` VALUES (68, 6, 'status', '状态 0正常 1停用', 'char(1)', 'String', 'status', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'radio', '', 5, 'admin', '2026-06-02 11:19:09', '', '2026-06-02 15:54:18');
INSERT INTO `gen_table_column` VALUES (69, 6, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', NULL, '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 6, 'admin', '2026-06-02 11:19:09', '', '2026-06-02 15:54:18');
INSERT INTO `gen_table_column` VALUES (70, 6, 'update_time', '更新时间', 'datetime', 'Date', 'updateTime', '0', '0', NULL, '1', '1', NULL, NULL, 'EQ', 'datetime', '', 7, 'admin', '2026-06-02 11:19:09', '', '2026-06-02 15:54:18');
INSERT INTO `gen_table_column` VALUES (71, 7, 'apply_id', '申请ID(主键)', 'bigint', 'Long', 'applyId', '1', '1', NULL, '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2026-06-02 14:25:24', '', '2026-06-02 14:35:25');
INSERT INTO `gen_table_column` VALUES (72, 7, 'register_id', '挂号ID', 'bigint', 'Long', 'registerId', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2026-06-02 14:25:24', '', '2026-06-02 14:35:25');
INSERT INTO `gen_table_column` VALUES (73, 7, 'encounter_id', '接诊ID', 'bigint', 'Long', 'encounterId', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 3, 'admin', '2026-06-02 14:25:24', '', '2026-06-02 14:35:25');
INSERT INTO `gen_table_column` VALUES (74, 7, 'patient_id', '患者ID', 'bigint', 'Long', 'patientId', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 4, 'admin', '2026-06-02 14:25:24', '', '2026-06-02 14:35:25');
INSERT INTO `gen_table_column` VALUES (75, 7, 'doctor_id', '开单医生ID', 'bigint', 'Long', 'doctorId', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 5, 'admin', '2026-06-02 14:25:24', '', '2026-06-02 14:35:25');
INSERT INTO `gen_table_column` VALUES (76, 7, 'dept_id', '开单科室ID', 'bigint', 'Long', 'deptId', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 6, 'admin', '2026-06-02 14:25:24', '', '2026-06-02 14:35:25');
INSERT INTO `gen_table_column` VALUES (77, 7, 'apply_type', '类型：CHECK检查/INSPEC检验/DISPOSAL处置', 'varchar(20)', 'String', 'applyType', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'select', '', 7, 'admin', '2026-06-02 14:25:24', '', '2026-06-02 14:35:25');
INSERT INTO `gen_table_column` VALUES (78, 7, 'tech_id', '医技项目ID', 'bigint', 'Long', 'techId', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 8, 'admin', '2026-06-02 14:25:24', '', '2026-06-02 14:35:25');
INSERT INTO `gen_table_column` VALUES (79, 7, 'apply_info', '目的要求', 'varchar(512)', 'String', 'applyInfo', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'textarea', '', 9, 'admin', '2026-06-02 14:25:24', '', '2026-06-02 14:35:25');
INSERT INTO `gen_table_column` VALUES (80, 7, 'apply_position', '检查/检验/处置部位', 'varchar(255)', 'String', 'applyPosition', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'input', '', 10, 'admin', '2026-06-02 14:25:24', '', '2026-06-02 14:35:25');
INSERT INTO `gen_table_column` VALUES (81, 7, 'operator_id', '执行人员ID', 'bigint', 'Long', 'operatorId', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'input', '', 11, 'admin', '2026-06-02 14:25:24', '', '2026-06-02 14:35:25');
INSERT INTO `gen_table_column` VALUES (82, 7, 'inputer_id', '结果录入人员ID', 'bigint', 'Long', 'inputerId', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'input', '', 12, 'admin', '2026-06-02 14:25:24', '', '2026-06-02 14:35:25');
INSERT INTO `gen_table_column` VALUES (83, 7, 'apply_time', '开立时间', 'datetime', 'Date', 'applyTime', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'datetime', '', 13, 'admin', '2026-06-02 14:25:24', '', '2026-06-02 14:35:25');
INSERT INTO `gen_table_column` VALUES (84, 7, 'exam_time', '执行时间', 'datetime', 'Date', 'examTime', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'datetime', '', 14, 'admin', '2026-06-02 14:25:24', '', '2026-06-02 14:35:25');
INSERT INTO `gen_table_column` VALUES (85, 7, 'exam_result', '执行结果', 'text', 'String', 'examResult', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'textarea', '', 15, 'admin', '2026-06-02 14:25:24', '', '2026-06-02 14:35:25');
INSERT INTO `gen_table_column` VALUES (86, 7, 'image_url', 'CT/影像文件访问地址，检验、处置类可留空', 'varchar(500)', 'String', 'imageUrl', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'textarea', '', 16, 'admin', '2026-06-02 14:25:24', '', '2026-06-02 14:35:25');
INSERT INTO `gen_table_column` VALUES (87, 7, 'apply_status', '状态：待缴费/待执行/已完成', 'varchar(30)', 'String', 'applyStatus', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'radio', '', 17, 'admin', '2026-06-02 14:25:24', '', '2026-06-02 14:35:25');
INSERT INTO `gen_table_column` VALUES (88, 7, 'pay_status', '支付状态', 'varchar(20)', 'String', 'payStatus', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'radio', '', 18, 'admin', '2026-06-02 14:25:24', '', '2026-06-02 14:35:25');
INSERT INTO `gen_table_column` VALUES (89, 7, 'remark', '备注', 'varchar(512)', 'String', 'remark', '0', '0', NULL, '1', '1', '1', NULL, 'EQ', 'textarea', '', 19, 'admin', '2026-06-02 14:25:24', '', '2026-06-02 14:35:25');
INSERT INTO `gen_table_column` VALUES (90, 7, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', NULL, '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 20, 'admin', '2026-06-02 14:25:24', '', '2026-06-02 14:35:25');
INSERT INTO `gen_table_column` VALUES (91, 7, 'update_time', '更新时间', 'datetime', 'Date', 'updateTime', '0', '0', NULL, '1', '1', NULL, NULL, 'EQ', 'datetime', '', 21, 'admin', '2026-06-02 14:25:24', '', '2026-06-02 14:35:25');
INSERT INTO `gen_table_column` VALUES (92, 8, 'id', '主键ID', 'bigint', 'Long', 'id', '1', '1', NULL, '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2026-06-02 14:25:56', '', '2026-06-02 14:32:32');
INSERT INTO `gen_table_column` VALUES (93, 8, 'tech_code', '项目编码', 'varchar(64)', 'String', 'techCode', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2026-06-02 14:25:56', '', '2026-06-02 14:32:32');
INSERT INTO `gen_table_column` VALUES (94, 8, 'tech_name', '项目名称', 'varchar(64)', 'String', 'techName', '0', '0', '1', '1', '1', '1', '1', 'LIKE', 'input', '', 3, 'admin', '2026-06-02 14:25:56', '', '2026-06-02 14:32:32');
INSERT INTO `gen_table_column` VALUES (95, 8, 'tech_format', '规格', 'varchar(64)', 'String', 'techFormat', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'input', '', 4, 'admin', '2026-06-02 14:25:56', '', '2026-06-02 14:32:32');
INSERT INTO `gen_table_column` VALUES (96, 8, 'tech_price', '单价', 'decimal(10,2)', 'BigDecimal', 'techPrice', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'input', '', 5, 'admin', '2026-06-02 14:25:56', '', '2026-06-02 14:32:32');
INSERT INTO `gen_table_column` VALUES (97, 8, 'tech_type', '项目类型：CHECK检查/INSPEC检验/DISPOSAL处置', 'varchar(32)', 'String', 'techType', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'select', '', 6, 'admin', '2026-06-02 14:25:56', '', '2026-06-02 14:32:32');
INSERT INTO `gen_table_column` VALUES (98, 8, 'price_type', '费用分类', 'varchar(64)', 'String', 'priceType', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'select', '', 7, 'admin', '2026-06-02 14:25:56', '', '2026-06-02 14:32:32');
INSERT INTO `gen_table_column` VALUES (99, 8, 'deptment_id', '执行科室ID', 'bigint', 'Long', 'deptmentId', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'input', '', 8, 'admin', '2026-06-02 14:25:56', '', '2026-06-02 14:32:32');
INSERT INTO `gen_table_column` VALUES (100, 8, 'py_code', '拼音码(检索用)', 'varchar(64)', 'String', 'pyCode', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'input', '', 9, 'admin', '2026-06-02 14:25:56', '', '2026-06-02 14:32:32');
INSERT INTO `gen_table_column` VALUES (101, 8, 'sort', '排序号', 'int', 'Long', 'sort', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'input', '', 10, 'admin', '2026-06-02 14:25:56', '', '2026-06-02 14:32:32');
INSERT INTO `gen_table_column` VALUES (102, 8, 'status', '状态 0正常 1停用', 'char(1)', 'String', 'status', '0', '0', NULL, '1', '1', '1', '1', 'EQ', 'radio', '', 11, 'admin', '2026-06-02 14:25:56', '', '2026-06-02 14:32:32');
INSERT INTO `gen_table_column` VALUES (103, 8, 'remark', '项目备注说明', 'varchar(512)', 'String', 'remark', '0', '0', NULL, '1', '1', '1', NULL, 'EQ', 'textarea', '', 12, 'admin', '2026-06-02 14:25:56', '', '2026-06-02 14:32:32');
INSERT INTO `gen_table_column` VALUES (104, 8, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', NULL, '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 13, 'admin', '2026-06-02 14:25:56', '', '2026-06-02 14:32:32');
INSERT INTO `gen_table_column` VALUES (105, 8, 'update_time', '更新时间', 'datetime', 'Date', 'updateTime', '0', '0', NULL, '1', '1', NULL, NULL, 'EQ', 'datetime', '', 14, 'admin', '2026-06-02 14:25:56', '', '2026-06-02 14:32:32');

-- ----------------------------
-- Table structure for medical_record
-- ----------------------------
DROP TABLE IF EXISTS `medical_record`;
CREATE TABLE `medical_record`  (
  `record_id` bigint NOT NULL AUTO_INCREMENT COMMENT '病历ID',
  `encounter_id` bigint NOT NULL COMMENT '接诊ID',
  `chief_complaint` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '主诉',
  `present_illness` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '现病史',
  `past_history` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '既往史',
  `allergy_history` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '过敏史',
  `physical_exam` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '体格检查',
  `diagnosis_opinion` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '诊断意见',
  `treatment_plan` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '治疗方案',
  `doctor_advice` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '医生建议',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`record_id`) USING BTREE,
  UNIQUE INDEX `encounter_id`(`encounter_id` ASC) USING BTREE,
  CONSTRAINT `fk_medical_record_encounter` FOREIGN KEY (`encounter_id`) REFERENCES `encounter` (`encounter_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '电子病历表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of medical_record
-- ----------------------------

-- ----------------------------
-- Table structure for medical_record_disease
-- ----------------------------
DROP TABLE IF EXISTS `medical_record_disease`;
CREATE TABLE `medical_record_disease`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `record_id` bigint NOT NULL COMMENT '病历ID',
  `disease_id` bigint NOT NULL COMMENT '疾病ID',
  `diagnosis_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '诊断类型',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_mrd_record`(`record_id` ASC) USING BTREE,
  INDEX `idx_mrd_disease`(`disease_id` ASC) USING BTREE,
  CONSTRAINT `fk_mrd_disease` FOREIGN KEY (`disease_id`) REFERENCES `disease` (`disease_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_mrd_record` FOREIGN KEY (`record_id`) REFERENCES `medical_record` (`record_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '病历疾病关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of medical_record_disease
-- ----------------------------

-- ----------------------------
-- Table structure for medical_technology
-- ----------------------------
DROP TABLE IF EXISTS `medical_technology`;
CREATE TABLE `medical_technology`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tech_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '项目编码',
  `tech_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '项目名称',
  `tech_format` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '规格',
  `tech_price` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '单价',
  `tech_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '项目类型：CHECK检查/INSPEC检验/DISPOSAL处置',
  `price_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '费用分类',
  `deptment_id` bigint NULL DEFAULT NULL COMMENT '执行科室ID',
  `py_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拼音码(检索用)',
  `sort` int NULL DEFAULT 0 COMMENT '排序号',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '状态 0正常 1停用',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '项目备注说明',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tech_code`(`tech_code` ASC) USING BTREE,
  INDEX `idx_tech_type`(`tech_type` ASC) USING BTREE,
  INDEX `idx_deptment_id`(`deptment_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '医技项目表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of medical_technology
-- ----------------------------

-- ----------------------------
-- Table structure for patient
-- ----------------------------
DROP TABLE IF EXISTS `patient`;
CREATE TABLE `patient`  (
  `patient_id` bigint NOT NULL AUTO_INCREMENT COMMENT '患者ID',
  `patient_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '病历号',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '患者姓名',
  `gender` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '性别',
  `birthday` date NULL DEFAULT NULL COMMENT '出生日期',
  `age` int NULL DEFAULT NULL COMMENT '年龄',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `id_card` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证号',
  `blood_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '血型',
  `marital_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '婚姻状态',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '家庭住址',
  `emergency_contact` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '紧急联系人',
  `emergency_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '紧急联系电话',
  `allergy_history` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '过敏史',
  `past_history` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '既往史',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '状态',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`patient_id`) USING BTREE,
  UNIQUE INDEX `patient_no`(`patient_no` ASC) USING BTREE,
  INDEX `idx_patient_no`(`patient_no` ASC) USING BTREE,
  INDEX `idx_patient_name`(`name` ASC) USING BTREE,
  INDEX `idx_patient_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_patient_id_card`(`id_card` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '患者表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of patient
-- ----------------------------

-- ----------------------------
-- Table structure for payment
-- ----------------------------
DROP TABLE IF EXISTS `payment`;
CREATE TABLE `payment`  (
  `payment_id` bigint NOT NULL AUTO_INCREMENT COMMENT '收费单ID(主键)',
  `pay_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收费单号(唯一)',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `register_id` bigint NOT NULL COMMENT '关联挂号ID',
  `total_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '缴费总金额',
  `pay_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支付方式：现金/微信/支付宝/医保',
  `pay_status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'UNPAID' COMMENT '缴费状态：UNPAID待支付/PAID已支付/REFUND已退费',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '收费员ID',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '缴费时间',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`payment_id`) USING BTREE,
  UNIQUE INDEX `pay_no`(`pay_no` ASC) USING BTREE,
  INDEX `idx_patient_id`(`patient_id` ASC) USING BTREE,
  INDEX `idx_register_id`(`register_id` ASC) USING BTREE,
  INDEX `idx_pay_status`(`pay_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '收费主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of payment
-- ----------------------------

-- ----------------------------
-- Table structure for payment_item
-- ----------------------------
DROP TABLE IF EXISTS `payment_item`;
CREATE TABLE `payment_item`  (
  `item_id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID(主键)',
  `payment_id` bigint NOT NULL COMMENT '收费单ID(外键)',
  `biz_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务类型：REGISTER挂号/EXAM医技/PRESCRIPTION处方',
  `biz_id` bigint NOT NULL COMMENT '对应业务单据ID',
  `item_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收费项目名称',
  `unit_price` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '单价',
  `quantity` int NULL DEFAULT 1 COMMENT '数量',
  `subtotal` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '单项小计金额',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`item_id`) USING BTREE,
  INDEX `idx_payment_id`(`payment_id` ASC) USING BTREE,
  INDEX `idx_biz_type`(`biz_type` ASC) USING BTREE,
  CONSTRAINT `fk_payment_item_payment` FOREIGN KEY (`payment_id`) REFERENCES `payment` (`payment_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '收费明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of payment_item
-- ----------------------------

-- ----------------------------
-- Table structure for prescription
-- ----------------------------
DROP TABLE IF EXISTS `prescription`;
CREATE TABLE `prescription`  (
  `prescription_id` bigint NOT NULL AUTO_INCREMENT COMMENT '处方ID(主键)',
  `register_id` bigint NOT NULL COMMENT '挂号ID',
  `encounter_id` bigint NOT NULL COMMENT '接诊ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `doctor_id` bigint NOT NULL COMMENT '开方医生ID',
  `dept_id` bigint NOT NULL COMMENT '开方科室ID',
  `prescription_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '处方单号',
  `total_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '处方总金额',
  `prescription_status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PENDING' COMMENT '状态：待缴费/已缴费/已发药/已退方',
  `pay_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'unpaid' COMMENT '支付状态',
  `drug_tip` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '整体用药嘱托',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开方时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`prescription_id`) USING BTREE,
  UNIQUE INDEX `prescription_no`(`prescription_no` ASC) USING BTREE,
  INDEX `patient_id`(`patient_id` ASC) USING BTREE,
  INDEX `doctor_id`(`doctor_id` ASC) USING BTREE,
  INDEX `idx_register_id`(`register_id` ASC) USING BTREE,
  INDEX `idx_encounter_id`(`encounter_id` ASC) USING BTREE,
  CONSTRAINT `prescription_ibfk_1` FOREIGN KEY (`register_id`) REFERENCES `register` (`register_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `prescription_ibfk_2` FOREIGN KEY (`encounter_id`) REFERENCES `encounter` (`encounter_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `prescription_ibfk_3` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `prescription_ibfk_4` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`doctor_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '处方主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of prescription
-- ----------------------------

-- ----------------------------
-- Table structure for prescription_item
-- ----------------------------
DROP TABLE IF EXISTS `prescription_item`;
CREATE TABLE `prescription_item`  (
  `item_id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID(主键)',
  `prescription_id` bigint NOT NULL COMMENT '处方ID',
  `drug_id` bigint NOT NULL COMMENT '药品ID',
  `drug_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '药品名称',
  `drug_price` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '药品单价',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '药品数量',
  `drug_usage` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用法',
  `dosage` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '单次用量',
  `frequency` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服用频次',
  `use_days` int NULL DEFAULT NULL COMMENT '使用天数',
  `item_tip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '单种药品嘱托',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`item_id`) USING BTREE,
  INDEX `drug_id`(`drug_id` ASC) USING BTREE,
  INDEX `idx_prescription_id`(`prescription_id` ASC) USING BTREE,
  CONSTRAINT `prescription_item_ibfk_1` FOREIGN KEY (`prescription_id`) REFERENCES `prescription` (`prescription_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `prescription_item_ibfk_2` FOREIGN KEY (`drug_id`) REFERENCES `drug` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '处方明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of prescription_item
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_blob_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_blob_triggers`;
CREATE TABLE `qrtz_blob_triggers`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `blob_data` blob NULL COMMENT '存放持久化Trigger对象',
  PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
  CONSTRAINT `qrtz_blob_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'Blob类型的触发器表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_blob_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_calendars
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_calendars`;
CREATE TABLE `qrtz_calendars`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `calendar_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '日历名称',
  `calendar` blob NOT NULL COMMENT '存放持久化calendar对象',
  PRIMARY KEY (`sched_name`, `calendar_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '日历信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_calendars
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_cron_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_cron_triggers`;
CREATE TABLE `qrtz_cron_triggers`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `cron_expression` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'cron表达式',
  `time_zone_id` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '时区',
  PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
  CONSTRAINT `qrtz_cron_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'Cron类型的触发器表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_cron_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_fired_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_fired_triggers`;
CREATE TABLE `qrtz_fired_triggers`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `entry_id` varchar(95) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度器实例id',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `instance_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度器实例名',
  `fired_time` bigint NOT NULL COMMENT '触发的时间',
  `sched_time` bigint NOT NULL COMMENT '定时器制定的时间',
  `priority` int NOT NULL COMMENT '优先级',
  `state` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '状态',
  `job_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任务名称',
  `job_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任务组名',
  `is_nonconcurrent` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否并发',
  `requests_recovery` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否接受恢复执行',
  PRIMARY KEY (`sched_name`, `entry_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '已触发的触发器表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_fired_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_job_details
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_job_details`;
CREATE TABLE `qrtz_job_details`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `job_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务名称',
  `job_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务组名',
  `description` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '相关介绍',
  `job_class_name` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '执行任务类名称',
  `is_durable` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '是否持久化',
  `is_nonconcurrent` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '是否并发',
  `is_update_data` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '是否更新数据',
  `requests_recovery` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '是否接受恢复执行',
  `job_data` blob NULL COMMENT '存放持久化job对象',
  PRIMARY KEY (`sched_name`, `job_name`, `job_group`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '任务详细信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_job_details
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_locks
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_locks`;
CREATE TABLE `qrtz_locks`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `lock_name` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '悲观锁名称',
  PRIMARY KEY (`sched_name`, `lock_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '存储的悲观锁信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_locks
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_paused_trigger_grps
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_paused_trigger_grps`;
CREATE TABLE `qrtz_paused_trigger_grps`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  PRIMARY KEY (`sched_name`, `trigger_group`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '暂停的触发器表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_paused_trigger_grps
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_scheduler_state
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_scheduler_state`;
CREATE TABLE `qrtz_scheduler_state`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `instance_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '实例名称',
  `last_checkin_time` bigint NOT NULL COMMENT '上次检查时间',
  `checkin_interval` bigint NOT NULL COMMENT '检查间隔时间',
  PRIMARY KEY (`sched_name`, `instance_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '调度器状态表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_scheduler_state
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_simple_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_simple_triggers`;
CREATE TABLE `qrtz_simple_triggers`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `repeat_count` bigint NOT NULL COMMENT '重复的次数统计',
  `repeat_interval` bigint NOT NULL COMMENT '重复的间隔时间',
  `times_triggered` bigint NOT NULL COMMENT '已经触发的次数',
  PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
  CONSTRAINT `qrtz_simple_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '简单触发器的信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_simple_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_simprop_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_simprop_triggers`;
CREATE TABLE `qrtz_simprop_triggers`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `str_prop_1` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'String类型的trigger的第一个参数',
  `str_prop_2` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'String类型的trigger的第二个参数',
  `str_prop_3` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'String类型的trigger的第三个参数',
  `int_prop_1` int NULL DEFAULT NULL COMMENT 'int类型的trigger的第一个参数',
  `int_prop_2` int NULL DEFAULT NULL COMMENT 'int类型的trigger的第二个参数',
  `long_prop_1` bigint NULL DEFAULT NULL COMMENT 'long类型的trigger的第一个参数',
  `long_prop_2` bigint NULL DEFAULT NULL COMMENT 'long类型的trigger的第二个参数',
  `dec_prop_1` decimal(13, 4) NULL DEFAULT NULL COMMENT 'decimal类型的trigger的第一个参数',
  `dec_prop_2` decimal(13, 4) NULL DEFAULT NULL COMMENT 'decimal类型的trigger的第二个参数',
  `bool_prop_1` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Boolean类型的trigger的第一个参数',
  `bool_prop_2` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Boolean类型的trigger的第二个参数',
  PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
  CONSTRAINT `qrtz_simprop_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '同步机制的行锁表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_simprop_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_triggers`;
CREATE TABLE `qrtz_triggers`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发器的名字',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发器所属组的名字',
  `job_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_job_details表job_name的外键',
  `job_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_job_details表job_group的外键',
  `description` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '相关介绍',
  `next_fire_time` bigint NULL DEFAULT NULL COMMENT '上一次触发时间（毫秒）',
  `prev_fire_time` bigint NULL DEFAULT NULL COMMENT '下一次触发时间（默认为-1表示不触发）',
  `priority` int NULL DEFAULT NULL COMMENT '优先级',
  `trigger_state` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发器状态',
  `trigger_type` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发器的类型',
  `start_time` bigint NOT NULL COMMENT '开始时间',
  `end_time` bigint NULL DEFAULT NULL COMMENT '结束时间',
  `calendar_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '日程表名称',
  `misfire_instr` smallint NULL DEFAULT NULL COMMENT '补偿执行的策略',
  `job_data` blob NULL COMMENT '存放持久化job对象',
  PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
  INDEX `sched_name`(`sched_name` ASC, `job_name` ASC, `job_group` ASC) USING BTREE,
  CONSTRAINT `qrtz_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `job_name`, `job_group`) REFERENCES `qrtz_job_details` (`sched_name`, `job_name`, `job_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '触发器详细信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for regist_level
-- ----------------------------
DROP TABLE IF EXISTS `regist_level`;
CREATE TABLE `regist_level`  (
  `level_id` bigint NOT NULL AUTO_INCREMENT COMMENT '级别ID(主键)',
  `level_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '级别名称：普通号/专家号/主任号',
  `fee` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '对应挂号费用',
  `sort` int NULL DEFAULT 0 COMMENT '排序号',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '状态 0正常 1停用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`level_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '挂号级别表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of regist_level
-- ----------------------------
INSERT INTO `regist_level` VALUES (1, '普通号', 20.00, 1, '0', '2026-05-29 14:44:41', '2026-05-29 14:44:41');
INSERT INTO `regist_level` VALUES (2, '专家号', 50.00, 2, '0', '2026-05-29 14:44:41', '2026-05-29 14:44:41');
INSERT INTO `regist_level` VALUES (3, '主任号', 80.00, 3, '0', '2026-05-29 14:44:41', '2026-05-29 14:44:41');

-- ----------------------------
-- Table structure for register
-- ----------------------------
DROP TABLE IF EXISTS `register`;
CREATE TABLE `register`  (
  `register_id` bigint NOT NULL AUTO_INCREMENT COMMENT '挂号ID',
  `register_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '挂号单号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `schedule_id` bigint NOT NULL COMMENT '排班ID',
  `doctor_id` bigint NOT NULL COMMENT '医生ID',
  `dept_id` bigint NOT NULL COMMENT '科室ID',
  `level_id` bigint NULL DEFAULT NULL COMMENT '挂号级别ID',
  `register_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '挂号类型（online/offline）',
  `register_status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'registered' COMMENT '挂号状态',
  `pay_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'unpaid' COMMENT '支付状态',
  `register_fee` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '挂号费用',
  `register_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '挂号时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`register_id`) USING BTREE,
  UNIQUE INDEX `register_no`(`register_no` ASC) USING BTREE,
  INDEX `fk_register_patient`(`patient_id` ASC) USING BTREE,
  INDEX `fk_register_schedule`(`schedule_id` ASC) USING BTREE,
  INDEX `fk_register_doctor`(`doctor_id` ASC) USING BTREE,
  INDEX `fk_register_dept`(`dept_id` ASC) USING BTREE,
  CONSTRAINT `fk_register_dept` FOREIGN KEY (`dept_id`) REFERENCES `sys_dept` (`dept_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_register_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`doctor_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_register_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_register_schedule` FOREIGN KEY (`schedule_id`) REFERENCES `schedule` (`schedule_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '挂号表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of register
-- ----------------------------

-- ----------------------------
-- Table structure for schedule
-- ----------------------------
DROP TABLE IF EXISTS `schedule`;
CREATE TABLE `schedule`  (
  `schedule_id` bigint NOT NULL AUTO_INCREMENT COMMENT '排班ID',
  `doctor_id` bigint NOT NULL COMMENT '医生ID',
  `schedule_date` date NOT NULL COMMENT '排班日期',
  `time_slot` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '午别（morning/afternoon/evening）',
  `max_number` int NULL DEFAULT 0 COMMENT '最大挂号数',
  `reserved_number` int NULL DEFAULT 0 COMMENT '已预约人数',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '状态',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`schedule_id`) USING BTREE,
  INDEX `idx_schedule_date`(`schedule_date` ASC) USING BTREE,
  INDEX `idx_schedule_doctor`(`doctor_id` ASC) USING BTREE,
  CONSTRAINT `fk_schedule_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`doctor_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '医生排班表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of schedule
-- ----------------------------

-- ----------------------------
-- Table structure for settle_category
-- ----------------------------
DROP TABLE IF EXISTS `settle_category`;
CREATE TABLE `settle_category`  (
  `category_id` bigint NOT NULL AUTO_INCREMENT COMMENT '结算类别ID(主键)',
  `category_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '结算名称：自费/医保/新农合',
  `sort` int NULL DEFAULT 0 COMMENT '排序号',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '状态 0正常 1停用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`category_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '结算类别表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of settle_category
-- ----------------------------
INSERT INTO `settle_category` VALUES (1, '自费', 1, '0', '2026-05-29 14:44:41', '2026-05-29 14:44:41');
INSERT INTO `settle_category` VALUES (2, '城镇医保', 2, '0', '2026-05-29 14:44:41', '2026-05-29 14:44:41');
INSERT INTO `settle_category` VALUES (3, '新农合', 3, '0', '2026-05-29 14:44:41', '2026-05-29 14:44:41');

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `config_id` int NOT NULL AUTO_INCREMENT COMMENT '参数主键',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '参数名称',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '参数键名',
  `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '参数键值',
  `config_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`config_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '参数配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', 'Y', 'admin', '2026-05-27 11:48:15', '', NULL, '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow');
INSERT INTO `sys_config` VALUES (2, '用户管理-账号初始密码', 'sys.user.initPassword', '123456', 'Y', 'admin', '2026-05-27 11:48:15', '', NULL, '初始化密码 123456');
INSERT INTO `sys_config` VALUES (3, '主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', 'Y', 'admin', '2026-05-27 11:48:15', '', NULL, '深色主题theme-dark，浅色主题theme-light');
INSERT INTO `sys_config` VALUES (4, '账号自助-是否开启用户注册功能', 'sys.account.registerUser', 'false', 'Y', 'admin', '2026-05-27 11:48:15', '', NULL, '是否开启注册用户功能（true开启，false关闭）');
INSERT INTO `sys_config` VALUES (5, '用户登录-黑名单列表', 'sys.login.blackIPList', '', 'Y', 'admin', '2026-05-27 11:48:15', '', NULL, '设置登录IP黑名单限制，多个匹配项以;分隔，支持匹配（*通配、网段）');
INSERT INTO `sys_config` VALUES (6, '用户管理-初始密码修改策略', 'sys.account.initPasswordModify', '1', 'Y', 'admin', '2026-05-27 11:48:15', '', NULL, '0：初始密码修改策略关闭，没有任何提示，1：提醒用户，如果未修改初始密码，则在登录时就会提醒修改密码对话框');
INSERT INTO `sys_config` VALUES (7, '用户管理-账号密码更新周期', 'sys.account.passwordValidateDays', '0', 'Y', 'admin', '2026-05-27 11:48:15', '', NULL, '密码更新周期（填写数字，数据初始化值为0不限制，若修改必须为大于0小于365的正整数），如果超过这个周期登录系统时，则在登录时就会提醒修改密码对话框');
INSERT INTO `sys_config` VALUES (8, '用户管理-密码字符范围', 'sys.account.chrtype', '0', 'Y', 'admin', '2026-05-27 11:48:15', '', NULL, '默认任意字符范围，0任意（密码可以输入任意字符），1数字（密码只能为0-9数字），2英文字母（密码只能为a-z和A-Z字母），3字母和数字（密码必须包含字母，数字）,4字母数字和特殊字符（目前支持的特殊字符包括：~!@#$%^&*()-=_+）');

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
  `dept_id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门id',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父部门id',
  `ancestors` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '祖级列表',
  `dept_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '部门名称',
  `order_num` int NULL DEFAULT 0 COMMENT '显示顺序',
  `leader` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '负责人',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`dept_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 200 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '部门表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO `sys_dept` VALUES (100, 0, '0', '若依科技', 0, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-05-27 11:48:15', '', NULL);
INSERT INTO `sys_dept` VALUES (101, 100, '0,100', '深圳总公司', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-05-27 11:48:15', '', NULL);
INSERT INTO `sys_dept` VALUES (102, 100, '0,100', '长沙分公司', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-05-27 11:48:15', '', NULL);
INSERT INTO `sys_dept` VALUES (103, 101, '0,100,101', '研发部门', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-05-27 11:48:15', '', NULL);
INSERT INTO `sys_dept` VALUES (104, 101, '0,100,101', '市场部门', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-05-27 11:48:15', '', NULL);
INSERT INTO `sys_dept` VALUES (105, 101, '0,100,101', '测试部门', 3, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-05-27 11:48:15', '', NULL);
INSERT INTO `sys_dept` VALUES (106, 101, '0,100,101', '财务部门', 4, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-05-27 11:48:15', '', NULL);
INSERT INTO `sys_dept` VALUES (107, 101, '0,100,101', '运维部门', 5, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-05-27 11:48:15', '', NULL);
INSERT INTO `sys_dept` VALUES (108, 102, '0,100,102', '市场部门', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-05-27 11:48:15', '', NULL);
INSERT INTO `sys_dept` VALUES (109, 102, '0,100,102', '财务部门', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-05-27 11:48:15', '', NULL);

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data`  (
  `dict_code` bigint NOT NULL AUTO_INCREMENT COMMENT '字典编码',
  `dict_sort` int NULL DEFAULT 0 COMMENT '字典排序',
  `dict_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典标签',
  `dict_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典键值',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典类型',
  `css_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
  `list_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '表格回显样式',
  `is_default` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
INSERT INTO `sys_dict_data` VALUES (1, 1, '男', '0', 'sys_user_sex', '', '', 'Y', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '性别男');
INSERT INTO `sys_dict_data` VALUES (2, 2, '女', '1', 'sys_user_sex', '', '', 'N', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '性别女');
INSERT INTO `sys_dict_data` VALUES (3, 3, '未知', '2', 'sys_user_sex', '', '', 'N', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '性别未知');
INSERT INTO `sys_dict_data` VALUES (4, 1, '显示', '0', 'sys_show_hide', '', 'primary', 'Y', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '显示菜单');
INSERT INTO `sys_dict_data` VALUES (5, 2, '隐藏', '1', 'sys_show_hide', '', 'danger', 'N', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '隐藏菜单');
INSERT INTO `sys_dict_data` VALUES (6, 1, '正常', '0', 'sys_normal_disable', '', 'primary', 'Y', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (7, 2, '停用', '1', 'sys_normal_disable', '', 'danger', 'N', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '停用状态');
INSERT INTO `sys_dict_data` VALUES (8, 1, '正常', '0', 'sys_job_status', '', 'primary', 'Y', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (9, 2, '暂停', '1', 'sys_job_status', '', 'danger', 'N', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '停用状态');
INSERT INTO `sys_dict_data` VALUES (10, 1, '默认', 'DEFAULT', 'sys_job_group', '', '', 'Y', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '默认分组');
INSERT INTO `sys_dict_data` VALUES (11, 2, '系统', 'SYSTEM', 'sys_job_group', '', '', 'N', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '系统分组');
INSERT INTO `sys_dict_data` VALUES (12, 1, '是', 'Y', 'sys_yes_no', '', 'primary', 'Y', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '系统默认是');
INSERT INTO `sys_dict_data` VALUES (13, 2, '否', 'N', 'sys_yes_no', '', 'danger', 'N', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '系统默认否');
INSERT INTO `sys_dict_data` VALUES (14, 1, '通知', '1', 'sys_notice_type', '', 'warning', 'Y', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '通知');
INSERT INTO `sys_dict_data` VALUES (15, 2, '公告', '2', 'sys_notice_type', '', 'success', 'N', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '公告');
INSERT INTO `sys_dict_data` VALUES (16, 1, '正常', '0', 'sys_notice_status', '', 'primary', 'Y', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (17, 2, '关闭', '1', 'sys_notice_status', '', 'danger', 'N', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '关闭状态');
INSERT INTO `sys_dict_data` VALUES (18, 99, '其他', '0', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '其他操作');
INSERT INTO `sys_dict_data` VALUES (19, 1, '新增', '1', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '新增操作');
INSERT INTO `sys_dict_data` VALUES (20, 2, '修改', '2', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '修改操作');
INSERT INTO `sys_dict_data` VALUES (21, 3, '删除', '3', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '删除操作');
INSERT INTO `sys_dict_data` VALUES (22, 4, '授权', '4', 'sys_oper_type', '', 'primary', 'N', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '授权操作');
INSERT INTO `sys_dict_data` VALUES (23, 5, '导出', '5', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '导出操作');
INSERT INTO `sys_dict_data` VALUES (24, 6, '导入', '6', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '导入操作');
INSERT INTO `sys_dict_data` VALUES (25, 7, '强退', '7', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '强退操作');
INSERT INTO `sys_dict_data` VALUES (26, 8, '生成代码', '8', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '生成操作');
INSERT INTO `sys_dict_data` VALUES (27, 9, '清空数据', '9', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '清空操作');
INSERT INTO `sys_dict_data` VALUES (28, 1, '成功', '0', 'sys_common_status', '', 'primary', 'N', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (29, 2, '失败', '1', 'sys_common_status', '', 'danger', 'N', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '停用状态');

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
  `dict_id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典名称',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典类型',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_id`) USING BTREE,
  UNIQUE INDEX `dict_type`(`dict_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典类型表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES (1, '用户性别', 'sys_user_sex', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '用户性别列表');
INSERT INTO `sys_dict_type` VALUES (2, '菜单状态', 'sys_show_hide', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '菜单状态列表');
INSERT INTO `sys_dict_type` VALUES (3, '系统开关', 'sys_normal_disable', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '系统开关列表');
INSERT INTO `sys_dict_type` VALUES (4, '任务状态', 'sys_job_status', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '任务状态列表');
INSERT INTO `sys_dict_type` VALUES (5, '任务分组', 'sys_job_group', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '任务分组列表');
INSERT INTO `sys_dict_type` VALUES (6, '系统是否', 'sys_yes_no', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '系统是否列表');
INSERT INTO `sys_dict_type` VALUES (7, '通知类型', 'sys_notice_type', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '通知类型列表');
INSERT INTO `sys_dict_type` VALUES (8, '通知状态', 'sys_notice_status', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '通知状态列表');
INSERT INTO `sys_dict_type` VALUES (9, '操作类型', 'sys_oper_type', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '操作类型列表');
INSERT INTO `sys_dict_type` VALUES (10, '系统状态', 'sys_common_status', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '登录状态列表');

-- ----------------------------
-- Table structure for sys_job
-- ----------------------------
DROP TABLE IF EXISTS `sys_job`;
CREATE TABLE `sys_job`  (
  `job_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `job_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '任务名称',
  `job_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组名',
  `invoke_target` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调用目标字符串',
  `cron_expression` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT 'cron执行表达式',
  `misfire_policy` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '3' COMMENT '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
  `concurrent` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '1' COMMENT '是否并发执行（0允许 1禁止）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1暂停）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '备注信息',
  PRIMARY KEY (`job_id`, `job_name`, `job_group`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '定时任务调度表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_job
-- ----------------------------
INSERT INTO `sys_job` VALUES (1, '系统默认（无参）', 'DEFAULT', 'ryTask.ryNoParams', '0/10 * * * * ?', '3', '1', '1', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_job` VALUES (2, '系统默认（有参）', 'DEFAULT', 'ryTask.ryParams(\'ry\')', '0/15 * * * * ?', '3', '1', '1', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_job` VALUES (3, '系统默认（多参）', 'DEFAULT', 'ryTask.ryMultipleParams(\'ry\', true, 2000L, 316.50D, 100)', '0/20 * * * * ?', '3', '1', '1', 'admin', '2026-05-27 11:48:15', '', NULL, '');

-- ----------------------------
-- Table structure for sys_job_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_job_log`;
CREATE TABLE `sys_job_log`  (
  `job_log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务日志ID',
  `job_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务名称',
  `job_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务组名',
  `invoke_target` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调用目标字符串',
  `job_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '日志信息',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '执行状态（0正常 1失败）',
  `exception_info` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '异常信息',
  `start_time` datetime NULL DEFAULT NULL COMMENT '执行开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '执行结束时间',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`job_log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '定时任务调度日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_job_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_logininfor
-- ----------------------------
DROP TABLE IF EXISTS `sys_logininfor`;
CREATE TABLE `sys_logininfor`  (
  `info_id` bigint NOT NULL AUTO_INCREMENT COMMENT '访问ID',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '用户账号',
  `ipaddr` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '登录IP地址',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '登录状态（0成功 1失败）',
  `msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '提示信息',
  `access_time` datetime NULL DEFAULT NULL COMMENT '访问时间',
  PRIMARY KEY (`info_id`) USING BTREE,
  INDEX `idx_sys_logininfor_s`(`status` ASC) USING BTREE,
  INDEX `idx_sys_logininfor_lt`(`access_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 143 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统访问记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_logininfor
-- ----------------------------
INSERT INTO `sys_logininfor` VALUES (100, 'admin', '10.25.42.185', '0', '登录成功', '2026-05-27 12:25:37');
INSERT INTO `sys_logininfor` VALUES (101, 'admin', '10.25.42.185', '0', '退出成功', '2026-05-28 10:53:21');
INSERT INTO `sys_logininfor` VALUES (102, 'admin', '10.25.42.185', '0', '登录成功', '2026-05-28 10:53:28');
INSERT INTO `sys_logininfor` VALUES (103, 'admin', '127.0.0.1', '0', '退出成功', '2026-05-29 12:59:11');
INSERT INTO `sys_logininfor` VALUES (104, 'admin', '127.0.0.1', '0', '登录成功', '2026-05-29 14:42:24');
INSERT INTO `sys_logininfor` VALUES (105, 'admin', '127.0.0.1', '0', '退出成功', '2026-05-30 21:59:04');
INSERT INTO `sys_logininfor` VALUES (106, 'admin', '127.0.0.1', '0', '登录成功', '2026-05-30 21:59:11');
INSERT INTO `sys_logininfor` VALUES (107, 'admin', '172.22.83.184', '0', '退出成功', '2026-05-31 11:41:47');
INSERT INTO `sys_logininfor` VALUES (108, 'admin', '172.22.83.184', '0', '登录成功', '2026-05-31 11:41:53');
INSERT INTO `sys_logininfor` VALUES (109, 'admin', '127.0.0.1', '0', '退出成功', '2026-06-02 10:43:47');
INSERT INTO `sys_logininfor` VALUES (110, 'admin', '127.0.0.1', '0', '登录成功', '2026-06-02 10:46:47');
INSERT INTO `sys_logininfor` VALUES (111, 'admin', '127.0.0.1', '0', '退出成功', '2026-06-03 09:36:28');
INSERT INTO `sys_logininfor` VALUES (112, 'admin', '127.0.0.1', '0', '登录成功', '2026-06-03 09:36:36');
INSERT INTO `sys_logininfor` VALUES (113, 'admin', '127.0.0.1', '0', '登录成功', '2026-06-03 15:42:05');
INSERT INTO `sys_logininfor` VALUES (114, 'admin', '127.0.0.1', '0', '登录成功', '2026-06-03 15:42:22');
INSERT INTO `sys_logininfor` VALUES (115, 'admin', '127.0.0.1', '0', '登录成功', '2026-06-03 15:49:18');
INSERT INTO `sys_logininfor` VALUES (116, 'admin', '127.0.0.1', '0', '登录成功', '2026-06-03 15:53:29');
INSERT INTO `sys_logininfor` VALUES (117, 'admin', '127.0.0.1', '0', '登录成功', '2026-06-03 15:53:35');
INSERT INTO `sys_logininfor` VALUES (118, 'admin', '127.0.0.1', '0', '退出成功', '2026-06-03 16:10:42');
INSERT INTO `sys_logininfor` VALUES (119, 'admin', '127.0.0.1', '0', '登录成功', '2026-06-03 16:11:16');
INSERT INTO `sys_logininfor` VALUES (120, 'admin', '127.0.0.1', '0', '退出成功', '2026-06-04 10:00:34');
INSERT INTO `sys_logininfor` VALUES (121, 'admin', '127.0.0.1', '0', '登录成功', '2026-06-04 10:00:38');
INSERT INTO `sys_logininfor` VALUES (122, 'admin', '127.0.0.1', '0', '登录成功', '2026-06-04 10:11:42');
INSERT INTO `sys_logininfor` VALUES (123, 'admin', '127.0.0.1', '0', '登录成功', '2026-06-04 10:11:57');
INSERT INTO `sys_logininfor` VALUES (124, 'admin', '127.0.0.1', '0', '退出成功', '2026-06-04 10:12:54');
INSERT INTO `sys_logininfor` VALUES (125, 'admin', '127.0.0.1', '0', '退出成功', '2026-06-04 10:12:56');
INSERT INTO `sys_logininfor` VALUES (126, 'admin', '127.0.0.1', '0', '退出成功', '2026-06-04 10:12:57');
INSERT INTO `sys_logininfor` VALUES (127, 'admin', '127.0.0.1', '0', '退出成功', '2026-06-04 10:12:58');
INSERT INTO `sys_logininfor` VALUES (128, 'admin', '127.0.0.1', '0', '退出成功', '2026-06-04 10:12:58');
INSERT INTO `sys_logininfor` VALUES (129, 'admin', '127.0.0.1', '0', '退出成功', '2026-06-04 10:12:58');
INSERT INTO `sys_logininfor` VALUES (130, 'admin', '127.0.0.1', '0', '登录成功', '2026-06-04 10:13:11');
INSERT INTO `sys_logininfor` VALUES (131, 'admin', '127.0.0.1', '0', '退出成功', '2026-06-04 10:15:53');
INSERT INTO `sys_logininfor` VALUES (132, 'admin', '127.0.0.1', '0', '退出成功', '2026-06-04 10:19:11');
INSERT INTO `sys_logininfor` VALUES (133, 'admin', '127.0.0.1', '0', '退出成功', '2026-06-04 10:19:19');
INSERT INTO `sys_logininfor` VALUES (134, 'admin', '127.0.0.1', '0', '登录成功', '2026-06-04 10:25:09');
INSERT INTO `sys_logininfor` VALUES (135, 'admin', '127.0.0.1', '0', '登录成功', '2026-06-04 10:32:46');
INSERT INTO `sys_logininfor` VALUES (136, 'admin', '127.0.0.1', '0', '退出成功', '2026-06-04 10:57:16');
INSERT INTO `sys_logininfor` VALUES (137, 'admin', '127.0.0.1', '0', '退出成功', '2026-06-04 10:59:56');
INSERT INTO `sys_logininfor` VALUES (138, 'admin', '127.0.0.1', '0', '登录成功', '2026-06-04 11:00:16');
INSERT INTO `sys_logininfor` VALUES (139, 'admin', '127.0.0.1', '0', '登录成功', '2026-06-04 11:01:42');
INSERT INTO `sys_logininfor` VALUES (140, 'admin', '127.0.0.1', '0', '登录成功', '2026-06-04 11:48:09');
INSERT INTO `sys_logininfor` VALUES (141, 'admin', '127.0.0.1', '0', '退出成功', '2026-06-04 11:48:15');
INSERT INTO `sys_logininfor` VALUES (142, 'admin', '127.0.0.1', '0', '登录成功', '2026-06-04 11:48:23');

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `menu_id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '菜单名称',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父菜单ID',
  `order_num` int NULL DEFAULT 0 COMMENT '显示顺序',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '路由地址',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '组件路径',
  `query` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路由参数',
  `route_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '路由名称',
  `is_frame` int NULL DEFAULT 1 COMMENT '是否为外链（0是 1否）',
  `is_cache` int NULL DEFAULT 0 COMMENT '是否缓存（0缓存 1不缓存）',
  `menu_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
  `visible` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
  `perms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '#' COMMENT '菜单图标',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2109 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '菜单权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, '系统管理', 0, 1, 'system', NULL, '', '', 1, 0, 'M', '0', '0', '', 'system', 'admin', '2026-05-27 11:48:15', '', NULL, '系统管理目录');
INSERT INTO `sys_menu` VALUES (2, '系统监控', 0, 9, 'monitor', NULL, '', '', 1, 0, 'M', '0', '0', '', 'monitor', 'admin', '2026-05-27 11:48:15', '', NULL, '系统监控目录');
INSERT INTO `sys_menu` VALUES (3, '系统工具', 0, 8, 'tool', NULL, '', '', 1, 0, 'M', '0', '0', '', 'tool', 'admin', '2026-05-27 11:48:15', '', NULL, '系统工具目录');
INSERT INTO `sys_menu` VALUES (4, '若依官网', 0, 10, 'http://ruoyi.vip', NULL, '', '', 0, 0, 'M', '0', '0', '', 'guide', 'admin', '2026-05-27 11:48:15', '', NULL, '若依官网地址');
INSERT INTO `sys_menu` VALUES (100, '用户管理', 1, 1, 'user', 'system/user/index', '', '', 1, 0, 'C', '0', '0', 'system:user:list', 'user', 'admin', '2026-05-27 11:48:15', '', NULL, '用户管理菜单');
INSERT INTO `sys_menu` VALUES (101, '角色管理', 1, 2, 'role', 'system/role/index', '', '', 1, 0, 'C', '0', '0', 'system:role:list', 'peoples', 'admin', '2026-05-27 11:48:15', '', NULL, '角色管理菜单');
INSERT INTO `sys_menu` VALUES (102, '菜单管理', 1, 3, 'menu', 'system/menu/index', '', '', 1, 0, 'C', '0', '0', 'system:menu:list', 'tree-table', 'admin', '2026-05-27 11:48:15', '', NULL, '菜单管理菜单');
INSERT INTO `sys_menu` VALUES (103, '部门管理', 1, 4, 'dept', 'system/dept/index', '', '', 1, 0, 'C', '0', '0', 'system:dept:list', 'tree', 'admin', '2026-05-27 11:48:15', '', NULL, '部门管理菜单');
INSERT INTO `sys_menu` VALUES (104, '岗位管理', 1, 5, 'post', 'system/post/index', '', '', 1, 0, 'C', '0', '0', 'system:post:list', 'post', 'admin', '2026-05-27 11:48:15', '', NULL, '岗位管理菜单');
INSERT INTO `sys_menu` VALUES (105, '字典管理', 1, 6, 'dict', 'system/dict/index', '', '', 1, 0, 'C', '0', '0', 'system:dict:list', 'dict', 'admin', '2026-05-27 11:48:15', '', NULL, '字典管理菜单');
INSERT INTO `sys_menu` VALUES (106, '参数设置', 1, 7, 'config', 'system/config/index', '', '', 1, 0, 'C', '0', '0', 'system:config:list', 'edit', 'admin', '2026-05-27 11:48:15', '', NULL, '参数设置菜单');
INSERT INTO `sys_menu` VALUES (107, '通知公告', 1, 8, 'notice', 'system/notice/index', '', '', 1, 0, 'C', '0', '0', 'system:notice:list', 'message', 'admin', '2026-05-27 11:48:15', '', NULL, '通知公告菜单');
INSERT INTO `sys_menu` VALUES (108, '日志管理', 1, 9, 'log', '', '', '', 1, 0, 'M', '0', '0', '', 'log', 'admin', '2026-05-27 11:48:15', '', NULL, '日志管理菜单');
INSERT INTO `sys_menu` VALUES (109, '在线用户', 2, 1, 'online', 'monitor/online/index', '', '', 1, 0, 'C', '0', '0', 'monitor:online:list', 'online', 'admin', '2026-05-27 11:48:15', '', NULL, '在线用户菜单');
INSERT INTO `sys_menu` VALUES (110, '定时任务', 2, 2, 'job', 'monitor/job/index', '', '', 1, 0, 'C', '0', '0', 'monitor:job:list', 'job', 'admin', '2026-05-27 11:48:15', '', NULL, '定时任务菜单');
INSERT INTO `sys_menu` VALUES (111, 'Sentinel控制台', 2, 3, 'http://localhost:8718', '', '', '', 0, 0, 'C', '0', '0', 'monitor:sentinel:list', 'sentinel', 'admin', '2026-05-27 11:48:15', '', NULL, '流量控制菜单');
INSERT INTO `sys_menu` VALUES (112, 'Nacos控制台', 2, 4, 'http://localhost:8848/nacos', '', '', '', 0, 0, 'C', '0', '0', 'monitor:nacos:list', 'nacos', 'admin', '2026-05-27 11:48:15', '', NULL, '服务治理菜单');
INSERT INTO `sys_menu` VALUES (113, 'Admin控制台', 2, 5, 'http://localhost:9100/login', '', '', '', 0, 0, 'C', '0', '0', 'monitor:server:list', 'server', 'admin', '2026-05-27 11:48:15', '', NULL, '服务监控菜单');
INSERT INTO `sys_menu` VALUES (114, '表单构建', 3, 1, 'build', 'tool/build/index', '', '', 1, 0, 'C', '0', '0', 'tool:build:list', 'build', 'admin', '2026-05-27 11:48:15', '', NULL, '表单构建菜单');
INSERT INTO `sys_menu` VALUES (115, '代码生成', 3, 2, 'gen', 'tool/gen/index', '', '', 1, 0, 'C', '0', '0', 'tool:gen:list', 'code', 'admin', '2026-05-27 11:48:15', '', NULL, '代码生成菜单');
INSERT INTO `sys_menu` VALUES (116, '系统接口', 3, 3, 'http://localhost:8080/swagger-ui/index.html', '', '', '', 0, 0, 'C', '0', '0', 'tool:swagger:list', 'swagger', 'admin', '2026-05-27 11:48:15', '', NULL, '系统接口菜单');
INSERT INTO `sys_menu` VALUES (500, '操作日志', 108, 1, 'operlog', 'system/operlog/index', '', '', 1, 0, 'C', '0', '0', 'system:operlog:list', 'form', 'admin', '2026-05-27 11:48:15', '', NULL, '操作日志菜单');
INSERT INTO `sys_menu` VALUES (501, '登录日志', 108, 2, 'logininfor', 'system/logininfor/index', '', '', 1, 0, 'C', '0', '0', 'system:logininfor:list', 'logininfor', 'admin', '2026-05-27 11:48:15', '', NULL, '登录日志菜单');
INSERT INTO `sys_menu` VALUES (1000, '用户查询', 100, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:query', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1001, '用户新增', 100, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:add', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1002, '用户修改', 100, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:edit', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1003, '用户删除', 100, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:remove', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1004, '用户导出', 100, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:export', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1005, '用户导入', 100, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:import', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1006, '重置密码', 100, 7, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1007, '角色查询', 101, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:query', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1008, '角色新增', 101, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:add', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1009, '角色修改', 101, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:edit', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1010, '角色删除', 101, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:remove', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1011, '角色导出', 101, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:export', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1012, '菜单查询', 102, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:query', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1013, '菜单新增', 102, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:add', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1014, '菜单修改', 102, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:edit', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1015, '菜单删除', 102, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:remove', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1016, '部门查询', 103, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:query', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1017, '部门新增', 103, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:add', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1018, '部门修改', 103, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:edit', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1019, '部门删除', 103, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:remove', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1020, '岗位查询', 104, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:query', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1021, '岗位新增', 104, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:add', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1022, '岗位修改', 104, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:edit', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1023, '岗位删除', 104, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:remove', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1024, '岗位导出', 104, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:export', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1025, '字典查询', 105, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:query', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1026, '字典新增', 105, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:add', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1027, '字典修改', 105, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:edit', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1028, '字典删除', 105, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:remove', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1029, '字典导出', 105, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:export', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1030, '参数查询', 106, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:query', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1031, '参数新增', 106, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:add', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1032, '参数修改', 106, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:edit', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1033, '参数删除', 106, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:remove', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1034, '参数导出', 106, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:export', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1035, '公告查询', 107, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:query', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1036, '公告新增', 107, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:add', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1037, '公告修改', 107, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:edit', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1038, '公告删除', 107, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:remove', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1039, '操作查询', 500, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:operlog:query', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1040, '操作删除', 500, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:operlog:remove', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1041, '日志导出', 500, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:operlog:export', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1042, '登录查询', 501, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:logininfor:query', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1043, '登录删除', 501, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:logininfor:remove', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1044, '日志导出', 501, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:logininfor:export', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1045, '账户解锁', 501, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:logininfor:unlock', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1046, '在线查询', 109, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:query', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1047, '批量强退', 109, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:batchLogout', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1048, '单条强退', 109, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:forceLogout', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1049, '任务查询', 110, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:query', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1050, '任务新增', 110, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:add', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1051, '任务修改', 110, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:edit', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1052, '任务删除', 110, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:remove', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1053, '状态修改', 110, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:changeStatus', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1054, '任务导出', 110, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:export', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1055, '生成查询', 115, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:query', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1056, '生成修改', 115, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:edit', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1057, '生成删除', 115, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:remove', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1058, '导入代码', 115, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:import', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1059, '预览代码', 115, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:preview', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1060, '生成代码', 115, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:code', '#', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2000, '医生安排', 0, 2, '/hisdoctor', NULL, NULL, '', 1, 0, 'M', '0', '0', NULL, 'user', 'admin', '2026-05-30 23:07:46', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2001, '医生信息', 2000, 1, 'doctor', 'hisdoctor/doctor/index', NULL, '', 1, 0, 'C', '0', '0', 'hisdoctor:doctor:list', '#', 'admin', '2026-05-30 23:38:34', '', NULL, '医生信息菜单');
INSERT INTO `sys_menu` VALUES (2002, '医生信息查询', 2001, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisdoctor:doctor:query', '#', 'admin', '2026-05-30 23:38:34', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2003, '医生信息新增', 2001, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisdoctor:doctor:add', '#', 'admin', '2026-05-30 23:38:34', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2004, '医生信息修改', 2001, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisdoctor:doctor:edit', '#', 'admin', '2026-05-30 23:38:34', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2005, '医生信息删除', 2001, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisdoctor:doctor:remove', '#', 'admin', '2026-05-30 23:38:34', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2006, '医生信息导出', 2001, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisdoctor:doctor:export', '#', 'admin', '2026-05-30 23:38:34', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2007, '处方药品管理', 0, 5, '/hisprescription', NULL, NULL, '', 1, 0, 'M', '0', '0', '', 'tool', 'admin', '2026-06-02 11:07:54', 'admin', '2026-06-02 13:08:23', '');
INSERT INTO `sys_menu` VALUES (2008, '药品信息', 2007, 1, 'drug', 'hisprescription/drug/index', NULL, '', 1, 0, 'C', '0', '0', 'hisprescription:drug:list', '#', 'admin', '2026-06-02 11:42:16', '', NULL, '药品信息菜单');
INSERT INTO `sys_menu` VALUES (2009, '药品信息查询', 2008, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisprescription:drug:query', '#', 'admin', '2026-06-02 11:42:16', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2010, '药品信息新增', 2008, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisprescription:drug:add', '#', 'admin', '2026-06-02 11:42:16', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2011, '药品信息修改', 2008, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisprescription:drug:edit', '#', 'admin', '2026-06-02 11:42:16', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2012, '药品信息删除', 2008, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisprescription:drug:remove', '#', 'admin', '2026-06-02 11:42:16', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2013, '药品信息导出', 2008, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisprescription:drug:export', '#', 'admin', '2026-06-02 11:42:16', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2014, '医院检测', 0, 6, '/hisexam', NULL, NULL, '', 1, 0, 'M', '0', '0', NULL, 'skill', 'admin', '2026-06-02 12:26:17', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2015, '医技项目', 2014, 1, 'technology', 'hisexam/technology/index', NULL, '', 1, 0, 'C', '0', '0', 'hisexam:technology:list', '#', 'admin', '2026-06-02 14:38:25', '', NULL, '医技项目菜单');
INSERT INTO `sys_menu` VALUES (2016, '医技项目查询', 2015, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisexam:technology:query', '#', 'admin', '2026-06-02 14:38:25', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2017, '医技项目新增', 2015, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisexam:technology:add', '#', 'admin', '2026-06-02 14:38:25', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2018, '医技项目修改', 2015, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisexam:technology:edit', '#', 'admin', '2026-06-02 14:38:25', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2019, '医技项目删除', 2015, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisexam:technology:remove', '#', 'admin', '2026-06-02 14:38:25', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2020, '医技项目导出', 2015, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisexam:technology:export', '#', 'admin', '2026-06-02 14:38:25', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2021, '检查/检验/处置申请单', 2014, 1, 'apply', 'hisexam/apply/index', NULL, '', 1, 0, 'C', '0', '0', 'hisexam:apply:list', '#', 'admin', '2026-06-02 14:42:44', '', NULL, '检查/检验/处置申请单菜单');
INSERT INTO `sys_menu` VALUES (2022, '检查/检验/处置申请单查询', 2021, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisexam:apply:query', '#', 'admin', '2026-06-02 14:42:44', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2023, '检查/检验/处置申请单新增', 2021, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisexam:apply:add', '#', 'admin', '2026-06-02 14:42:44', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2024, '检查/检验/处置申请单修改', 2021, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisexam:apply:edit', '#', 'admin', '2026-06-02 14:42:44', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2025, '检查/检验/处置申请单删除', 2021, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisexam:apply:remove', '#', 'admin', '2026-06-02 14:42:44', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2026, '检查/检验/处置申请单导出', 2021, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisexam:apply:export', '#', 'admin', '2026-06-02 14:42:44', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2027, '医生排班', 2000, 1, 'schedule', 'hisdoctor/schedule/index', NULL, '', 1, 0, 'C', '0', '0', 'hisdoctor:schedule:list', '#', 'admin', '2026-06-02 15:41:57', '', NULL, '医生排班菜单');
INSERT INTO `sys_menu` VALUES (2028, '医生排班查询', 2027, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisdoctor:schedule:query', '#', 'admin', '2026-06-02 15:41:57', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2029, '医生排班新增', 2027, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisdoctor:schedule:add', '#', 'admin', '2026-06-02 15:41:57', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2030, '医生排班修改', 2027, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisdoctor:schedule:edit', '#', 'admin', '2026-06-02 15:41:57', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2031, '医生排班删除', 2027, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisdoctor:schedule:remove', '#', 'admin', '2026-06-02 15:41:57', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2032, '医生排班导出', 2027, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisdoctor:schedule:export', '#', 'admin', '2026-06-02 15:41:57', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2033, '挂号级别', 2106, 1, 'doctorlevel', 'hisdoctor/doctorlevel/index', NULL, '', 1, 0, 'C', '0', '0', 'hisdoctor:doctorlevel:list', '#', 'admin', '2026-06-02 15:59:52', 'admin', '2026-06-03 10:31:42', '挂号级别菜单');
INSERT INTO `sys_menu` VALUES (2034, '挂号级别查询', 2033, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisdoctor:doctorlevel:query', '#', 'admin', '2026-06-02 15:59:52', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2035, '挂号级别新增', 2033, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisdoctor:doctorlevel:add', '#', 'admin', '2026-06-02 15:59:52', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2036, '挂号级别修改', 2033, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisdoctor:doctorlevel:edit', '#', 'admin', '2026-06-02 15:59:52', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2037, '挂号级别删除', 2033, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisdoctor:doctorlevel:remove', '#', 'admin', '2026-06-02 15:59:52', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2038, '挂号级别导出', 2033, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisdoctor:doctorlevel:export', '#', 'admin', '2026-06-02 15:59:52', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2039, '处方主', 2007, 1, 'prescription', 'hisprescription/prescription/index', NULL, '', 1, 0, 'C', '0', '0', 'hisprescription:prescription:list', '#', 'admin', '2026-06-02 16:24:20', '', NULL, '处方主菜单');
INSERT INTO `sys_menu` VALUES (2040, '处方主查询', 2039, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisprescription:prescription:query', '#', 'admin', '2026-06-02 16:24:20', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2041, '处方主新增', 2039, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisprescription:prescription:add', '#', 'admin', '2026-06-02 16:24:20', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2042, '处方主修改', 2039, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisprescription:prescription:edit', '#', 'admin', '2026-06-02 16:24:20', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2043, '处方主删除', 2039, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisprescription:prescription:remove', '#', 'admin', '2026-06-02 16:24:20', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2044, '处方主导出', 2039, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisprescription:prescription:export', '#', 'admin', '2026-06-02 16:24:20', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2045, '处方明细', 2007, 1, 'item', 'hisprescription/item/index', NULL, '', 1, 0, 'C', '0', '0', 'hisprescription:item:list', '#', 'admin', '2026-06-03 09:41:08', '', NULL, '处方明细菜单');
INSERT INTO `sys_menu` VALUES (2046, '处方明细查询', 2045, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisprescription:item:query', '#', 'admin', '2026-06-03 09:41:08', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2047, '处方明细新增', 2045, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisprescription:item:add', '#', 'admin', '2026-06-03 09:41:08', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2048, '处方明细修改', 2045, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisprescription:item:edit', '#', 'admin', '2026-06-03 09:41:08', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2049, '处方明细删除', 2045, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisprescription:item:remove', '#', 'admin', '2026-06-03 09:41:08', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2050, '处方明细导出', 2045, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'hisprescription:item:export', '#', 'admin', '2026-06-03 09:41:08', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2051, '接诊', 2107, 1, 'encounter', 'emr/encounter/index', NULL, '', 1, 0, 'C', '0', '0', 'emr:encounter:list', '#', 'admin', '2026-06-03 10:04:11', 'admin', '2026-06-03 10:58:26', '接诊菜单');
INSERT INTO `sys_menu` VALUES (2052, '接诊查询', 2051, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'emr:encounter:query', '#', 'admin', '2026-06-03 10:04:11', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2053, '接诊新增', 2051, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'emr:encounter:add', '#', 'admin', '2026-06-03 10:04:11', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2054, '接诊修改', 2051, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'emr:encounter:edit', '#', 'admin', '2026-06-03 10:04:11', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2055, '接诊删除', 2051, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'emr:encounter:remove', '#', 'admin', '2026-06-03 10:04:11', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2056, '接诊导出', 2051, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'emr:encounter:export', '#', 'admin', '2026-06-03 10:04:11', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2057, '结算类别', 2108, 1, 'category', 'payment/category/index', NULL, '', 1, 0, 'C', '0', '0', 'payment:category:list', '#', 'admin', '2026-06-03 10:05:43', 'admin', '2026-06-03 11:24:03', '结算类别菜单');
INSERT INTO `sys_menu` VALUES (2058, '结算类别查询', 2057, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'payment:category:query', '#', 'admin', '2026-06-03 10:05:43', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2059, '结算类别新增', 2057, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'payment:category:add', '#', 'admin', '2026-06-03 10:05:43', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2060, '结算类别修改', 2057, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'payment:category:edit', '#', 'admin', '2026-06-03 10:05:43', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2061, '结算类别删除', 2057, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'payment:category:remove', '#', 'admin', '2026-06-03 10:05:43', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2062, '结算类别导出', 2057, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'payment:category:export', '#', 'admin', '2026-06-03 10:05:43', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2063, '疾病字典', 2107, 1, 'disease', 'emr/disease/index', NULL, '', 1, 0, 'C', '0', '0', 'emr:disease:list', '#', 'admin', '2026-06-03 10:06:02', 'admin', '2026-06-03 10:58:26', '疾病字典菜单');
INSERT INTO `sys_menu` VALUES (2064, '疾病字典查询', 2063, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'emr:disease:query', '#', 'admin', '2026-06-03 10:06:02', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2065, '疾病字典新增', 2063, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'emr:disease:add', '#', 'admin', '2026-06-03 10:06:02', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2066, '疾病字典修改', 2063, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'emr:disease:edit', '#', 'admin', '2026-06-03 10:06:02', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2067, '疾病字典删除', 2063, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'emr:disease:remove', '#', 'admin', '2026-06-03 10:06:02', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2068, '疾病字典导出', 2063, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'emr:disease:export', '#', 'admin', '2026-06-03 10:06:02', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2069, '签到', 2106, 1, 'in', 'register/in/index', NULL, '', 1, 0, 'C', '0', '0', 'register:in:list', '#', 'admin', '2026-06-03 10:06:13', 'admin', '2026-06-03 10:32:15', '签到菜单');
INSERT INTO `sys_menu` VALUES (2070, '签到查询', 2069, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'register:in:query', '#', 'admin', '2026-06-03 10:06:13', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2071, '签到新增', 2069, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'register:in:add', '#', 'admin', '2026-06-03 10:06:13', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2072, '签到修改', 2069, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'register:in:edit', '#', 'admin', '2026-06-03 10:06:13', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2073, '签到删除', 2069, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'register:in:remove', '#', 'admin', '2026-06-03 10:06:13', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2074, '签到导出', 2069, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'register:in:export', '#', 'admin', '2026-06-03 10:06:13', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2075, '收费明细', 2108, 1, 'paymentItem', 'payment/item/index', NULL, '', 1, 0, 'C', '1', '0', 'payment:item:list', '#', 'admin', '2026-06-03 10:06:18', 'admin', '2026-06-03 11:30:37', '收费明细菜单');
INSERT INTO `sys_menu` VALUES (2076, '收费明细查询', 2075, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'payment:item:query', '#', 'admin', '2026-06-03 10:06:18', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2077, '收费明细新增', 2075, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'payment:item:add', '#', 'admin', '2026-06-03 10:06:18', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2078, '收费明细修改', 2075, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'payment:item:edit', '#', 'admin', '2026-06-03 10:06:18', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2079, '收费明细删除', 2075, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'payment:item:remove', '#', 'admin', '2026-06-03 10:06:18', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2080, '收费明细导出', 2075, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'payment:item:export', '#', 'admin', '2026-06-03 10:06:18', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2081, '患者', 2105, 1, 'patient', 'patient/patient/index', NULL, '', 1, 0, 'C', '0', '0', 'patient:patient:list', '#', 'admin', '2026-06-03 10:06:23', 'admin', '2026-06-03 10:30:37', '患者菜单');
INSERT INTO `sys_menu` VALUES (2082, '患者查询', 2081, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'patient:patient:query', '#', 'admin', '2026-06-03 10:06:23', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2083, '患者新增', 2081, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'patient:patient:add', '#', 'admin', '2026-06-03 10:06:23', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2084, '患者修改', 2081, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'patient:patient:edit', '#', 'admin', '2026-06-03 10:06:23', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2085, '患者删除', 2081, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'patient:patient:remove', '#', 'admin', '2026-06-03 10:06:23', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2086, '患者导出', 2081, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'patient:patient:export', '#', 'admin', '2026-06-03 10:06:23', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2087, '收费', 2108, 1, 'payment', 'payment/payment/index', NULL, '', 1, 0, 'C', '0', '0', 'payment:payment:list', '#', 'admin', '2026-06-03 10:06:28', 'admin', '2026-06-03 10:58:26', '收费菜单');
INSERT INTO `sys_menu` VALUES (2088, '收费查询', 2087, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'payment:payment:query', '#', 'admin', '2026-06-03 10:06:28', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2089, '收费新增', 2087, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'payment:payment:add', '#', 'admin', '2026-06-03 10:06:28', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2090, '收费修改', 2087, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'payment:payment:edit', '#', 'admin', '2026-06-03 10:06:28', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2091, '收费删除', 2087, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'payment:payment:remove', '#', 'admin', '2026-06-03 10:06:28', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2092, '收费导出', 2087, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'payment:payment:export', '#', 'admin', '2026-06-03 10:06:28', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2093, '电子病历', 2107, 1, 'record', 'emr/record/index', NULL, '', 1, 0, 'C', '0', '0', 'emr:record:list', '#', 'admin', '2026-06-03 10:06:32', 'admin', '2026-06-03 10:58:26', '电子病历菜单');
INSERT INTO `sys_menu` VALUES (2094, '电子病历查询', 2093, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'emr:record:query', '#', 'admin', '2026-06-03 10:06:32', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2095, '电子病历新增', 2093, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'emr:record:add', '#', 'admin', '2026-06-03 10:06:32', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2096, '电子病历修改', 2093, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'emr:record:edit', '#', 'admin', '2026-06-03 10:06:32', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2097, '电子病历删除', 2093, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'emr:record:remove', '#', 'admin', '2026-06-03 10:06:32', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2098, '电子病历导出', 2093, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'emr:record:export', '#', 'admin', '2026-06-03 10:06:32', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2099, '挂号', 2106, 1, 'register', 'register/register/index', NULL, '', 1, 0, 'C', '0', '0', 'register:register:list', '#', 'admin', '2026-06-03 10:06:37', 'admin', '2026-06-03 10:32:02', '挂号菜单');
INSERT INTO `sys_menu` VALUES (2100, '挂号查询', 2099, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'register:register:query', '#', 'admin', '2026-06-03 10:06:37', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2101, '挂号新增', 2099, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'register:register:add', '#', 'admin', '2026-06-03 10:06:37', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2102, '挂号修改', 2099, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'register:register:edit', '#', 'admin', '2026-06-03 10:06:37', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2103, '挂号删除', 2099, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'register:register:remove', '#', 'admin', '2026-06-03 10:06:37', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2104, '挂号导出', 2099, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'register:register:export', '#', 'admin', '2026-06-03 10:06:37', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2105, '患者管理', 0, 1, '/patient', NULL, NULL, '', 1, 0, 'M', '0', '0', NULL, 'people', 'admin', '2026-06-03 10:30:12', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2106, '挂号管理', 0, 3, '/regist', NULL, NULL, '', 1, 0, 'M', '0', '0', NULL, 'list', 'admin', '2026-06-03 10:31:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2107, '会诊管理', 0, 4, '/emr', NULL, NULL, '', 1, 0, 'M', '0', '0', '', 'edit', 'admin', '2026-06-03 10:58:26', 'admin', '2026-06-03 11:01:57', 'EMR模块根菜单');
INSERT INTO `sys_menu` VALUES (2108, '收费管理', 0, 7, '/payment', NULL, NULL, '', 1, 0, 'M', '0', '0', '', 'money', 'admin', '2026-06-03 10:58:26', 'admin', '2026-06-03 11:23:49', '收费模块根菜单');

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`  (
  `notice_id` int NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `notice_title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告标题',
  `notice_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告类型（1通知 2公告）',
  `notice_content` longblob NULL COMMENT '公告内容',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '公告状态（0正常 1关闭）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`notice_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '通知公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_notice
-- ----------------------------
INSERT INTO `sys_notice` VALUES (1, '温馨提醒：2018-07-01 若依新版本发布啦', '2', 0xE696B0E78988E69CACE58685E5AEB9, '0', 'admin', '2026-05-27 11:48:15', '', NULL, '管理员');
INSERT INTO `sys_notice` VALUES (2, '维护通知：2018-07-01 若依系统凌晨维护', '1', 0xE7BBB4E68AA4E58685E5AEB9, '0', 'admin', '2026-05-27 11:48:15', '', NULL, '管理员');
INSERT INTO `sys_notice` VALUES (3, '若依开源框架介绍', '1', 0x3C703E3C7370616E207374796C653D22636F6C6F723A20726762283233302C20302C2030293B223EE9A1B9E79BAEE4BB8BE7BB8D3C2F7370616E3E3C2F703E3C703E3C666F6E7420636F6C6F723D2223333333333333223E52756F5969E5BC80E6BA90E9A1B9E79BAEE698AFE4B8BAE4BC81E4B89AE794A8E688B7E5AE9AE588B6E79A84E5908EE58FB0E8849AE6898BE69EB6E6A186E69EB6EFBC8CE4B8BAE4BC81E4B89AE68993E980A0E79A84E4B880E7AB99E5BC8FE8A7A3E586B3E696B9E6A188EFBC8CE9998DE4BD8EE4BC81E4B89AE5BC80E58F91E68890E69CACEFBC8CE68F90E58D87E5BC80E58F91E69588E78E87E38082E4B8BBE8A681E58C85E68BACE794A8E688B7E7AEA1E79086E38081E8A792E889B2E7AEA1E79086E38081E983A8E997A8E7AEA1E79086E38081E88F9CE58D95E7AEA1E79086E38081E58F82E695B0E7AEA1E79086E38081E5AD97E585B8E7AEA1E79086E380813C2F666F6E743E3C7370616E207374796C653D22636F6C6F723A207267622835312C2035312C203531293B223EE5B297E4BD8DE7AEA1E790863C2F7370616E3E3C7370616E207374796C653D22636F6C6F723A207267622835312C2035312C203531293B223EE38081E5AE9AE697B6E4BBBBE58AA13C2F7370616E3E3C7370616E207374796C653D22636F6C6F723A207267622835312C2035312C203531293B223EE380813C2F7370616E3E3C7370616E207374796C653D22636F6C6F723A207267622835312C2035312C203531293B223EE69C8DE58AA1E79B91E68EA7E38081E799BBE5BD95E697A5E5BF97E38081E6938DE4BD9CE697A5E5BF97E38081E4BBA3E7A081E7949FE68890E7AD89E58A9FE883BDE38082E585B6E4B8ADEFBC8CE8BF98E694AFE68C81E5A49AE695B0E68DAEE6BA90E38081E695B0E68DAEE69D83E99990E38081E59BBDE99985E58C96E380815265646973E7BC93E5AD98E38081446F636B6572E983A8E7BDB2E38081E6BB91E58AA8E9AA8CE8AF81E7A081E38081E7ACACE4B889E696B9E8AEA4E8AF81E799BBE5BD95E38081E58886E5B883E5BC8FE4BA8BE58AA1E380813C2F7370616E3E3C666F6E7420636F6C6F723D2223333333333333223EE58886E5B883E5BC8FE69687E4BBB6E5AD98E582A83C2F666F6E743E3C7370616E207374796C653D22636F6C6F723A207267622835312C2035312C203531293B223EE38081E58886E5BA93E58886E8A1A8E5A484E79086E7AD89E68A80E69CAFE789B9E782B9E380823C2F7370616E3E3C2F703E3C703E3C696D67207372633D2268747470733A2F2F666F727564612E67697465652E636F6D2F696D616765732F313737333933313834383334323433393033322F61346432323331335F313831353039352E706E6722207374796C653D2277696474683A20363470783B223E3C62723E3C2F703E3C703E3C7370616E207374796C653D22636F6C6F723A20726762283233302C20302C2030293B223EE5AE98E7BD91E58F8AE6BC94E7A4BA3C2F7370616E3E3C2F703E3C703E3C7370616E207374796C653D22636F6C6F723A207267622835312C2035312C203531293B223EE88BA5E4BE9DE5AE98E7BD91E59CB0E59D80EFBC9A266E6273703B3C2F7370616E3E3C6120687265663D22687474703A2F2F72756F79692E76697022207461726765743D225F626C616E6B223E687474703A2F2F72756F79692E7669703C2F613E3C6120687265663D22687474703A2F2F72756F79692E76697022207461726765743D225F626C616E6B223E3C2F613E3C2F703E3C703E3C7370616E207374796C653D22636F6C6F723A207267622835312C2035312C203531293B223EE88BA5E4BE9DE69687E6A1A3E59CB0E59D80EFBC9A266E6273703B3C2F7370616E3E3C6120687265663D22687474703A2F2F646F632E72756F79692E76697022207461726765743D225F626C616E6B223E687474703A2F2F646F632E72756F79692E7669703C2F613E3C62723E3C2F703E3C703E3C7370616E207374796C653D22636F6C6F723A207267622835312C2035312C203531293B223EE6BC94E7A4BAE59CB0E59D80E38090E4B88DE58886E7A6BBE78988E38091EFBC9A266E6273703B3C2F7370616E3E3C6120687265663D22687474703A2F2F64656D6F2E72756F79692E76697022207461726765743D225F626C616E6B223E687474703A2F2F64656D6F2E72756F79692E7669703C2F613E3C2F703E3C703E3C7370616E207374796C653D22636F6C6F723A207267622835312C2035312C203531293B223EE6BC94E7A4BAE59CB0E59D80E38090E58886E7A6BBE78988E69CACE38091EFBC9A266E6273703B3C2F7370616E3E3C6120687265663D22687474703A2F2F7675652E72756F79692E76697022207461726765743D225F626C616E6B223E687474703A2F2F7675652E72756F79692E7669703C2F613E3C2F703E3C703E3C7370616E207374796C653D22636F6C6F723A207267622835312C2035312C203531293B223EE6BC94E7A4BAE59CB0E59D80E38090E5BEAEE69C8DE58AA1E78988E38091EFBC9A266E6273703B3C2F7370616E3E3C6120687265663D22687474703A2F2F636C6F75642E72756F79692E76697022207461726765743D225F626C616E6B223E687474703A2F2F636C6F75642E72756F79692E7669703C2F613E3C2F703E3C703E3C7370616E207374796C653D22636F6C6F723A207267622835312C2035312C203531293B223EE6BC94E7A4BAE59CB0E59D80E38090E7A7BBE58AA8E7ABAFE78988E38091EFBC9A266E6273703B3C2F7370616E3E3C6120687265663D22687474703A2F2F68352E72756F79692E76697022207461726765743D225F626C616E6B223E687474703A2F2F68352E72756F79692E7669703C2F613E3C2F703E3C703E3C6272207374796C653D22636F6C6F723A207267622834382C2034392C203531293B20666F6E742D66616D696C793A202671756F743B48656C766574696361204E6575652671756F743B2C2048656C7665746963612C20417269616C2C2073616E732D73657269663B20666F6E742D73697A653A20313270783B223E3C2F703E, '0', 'admin', '2026-05-27 11:48:15', '', NULL, '管理员');

-- ----------------------------
-- Table structure for sys_notice_read
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice_read`;
CREATE TABLE `sys_notice_read`  (
  `read_id` bigint NOT NULL AUTO_INCREMENT COMMENT '已读主键',
  `notice_id` int NOT NULL COMMENT '公告id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `read_time` datetime NOT NULL COMMENT '阅读时间',
  PRIMARY KEY (`read_id`) USING BTREE,
  UNIQUE INDEX `uk_user_notice`(`user_id` ASC, `notice_id` ASC) USING BTREE COMMENT '同一用户同一公告只记录一次'
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '公告已读记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_notice_read
-- ----------------------------
INSERT INTO `sys_notice_read` VALUES (1, 3, 1, '2026-06-03 14:30:48');
INSERT INTO `sys_notice_read` VALUES (2, 2, 1, '2026-06-03 14:30:48');
INSERT INTO `sys_notice_read` VALUES (3, 1, 1, '2026-06-03 14:30:48');

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log`  (
  `oper_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '模块标题',
  `business_type` int NULL DEFAULT 0 COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '请求方式',
  `operator_type` int NULL DEFAULT 0 COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
  `oper_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '操作人员',
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '部门名称',
  `oper_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '主机地址',
  `oper_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '操作地点',
  `oper_param` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '请求参数',
  `json_result` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '返回参数',
  `status` int NULL DEFAULT 0 COMMENT '操作状态（0正常 1异常）',
  `error_msg` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  `cost_time` bigint NULL DEFAULT 0 COMMENT '消耗时间',
  PRIMARY KEY (`oper_id`) USING BTREE,
  INDEX `idx_sys_oper_log_bt`(`business_type` ASC) USING BTREE,
  INDEX `idx_sys_oper_log_s`(`status` ASC) USING BTREE,
  INDEX `idx_sys_oper_log_ot`(`oper_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 162 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '操作日志记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_oper_log
-- ----------------------------
INSERT INTO `sys_oper_log` VALUES (100, '代码生成', 6, 'com.ruoyi.gen.controller.GenController.importTableSave()', 'POST', 1, 'admin', NULL, '/gen/importTable', '127.0.0.1', '', '{\"tables\":\"doctor\",\"tplWebType\":\"element-plus\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-05-29 14:52:20', 155);
INSERT INTO `sys_oper_log` VALUES (101, '代码生成', 8, 'com.ruoyi.gen.controller.GenController.batchGenCode()', 'GET', 1, 'admin', NULL, '/gen/batchGenCode', '127.0.0.1', '', '{\"tables\":\"doctor\"}', NULL, 0, NULL, '2026-05-29 14:52:29', 375);
INSERT INTO `sys_oper_log` VALUES (102, '代码生成', 2, 'com.ruoyi.gen.controller.GenController.editSave()', 'PUT', 1, 'admin', NULL, '/gen', '127.0.0.1', '', '{\"businessName\":\"his_doctor\",\"className\":\"Doctor\",\"columns\":[{\"capJavaField\":\"DoctorId\",\"columnComment\":\"医生ID\",\"columnId\":1,\"columnName\":\"doctor_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2026-05-29 14:52:20\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":true,\"insert\":true,\"isIncrement\":\"1\",\"isInsert\":\"1\",\"isPk\":\"1\",\"javaField\":\"doctorId\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":true,\"query\":false,\"queryType\":\"EQ\",\"required\":false,\"sort\":1,\"superColumn\":false,\"tableId\":1,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"UserId\",\"columnComment\":\"系统用户ID（关联若依sys_user）\",\"columnId\":2,\"columnName\":\"user_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2026-05-29 14:52:20\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"userId\",\"javaType\":\"Long\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":2,\"superColumn\":false,\"tableId\":1,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"DeptId\",\"columnComment\":\"所属科室（关联若依sys_dept）\",\"columnId\":3,\"columnName\":\"dept_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2026-05-29 14:52:20\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"deptId\",\"javaType\":\"Long\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":3,\"superColumn\":false,\"tableId\":1,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"DoctorNo\",\"columnComment\":\"医生工号\",\"columnId\":4,\"columnName\":\"doctor_no\",\"columnType\":\"varchar(50)\",\"createBy\":\"admin\",\"createTime\":\"2026-05-29 14:52:20\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaF', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-05-30 22:55:52', 183);
INSERT INTO `sys_oper_log` VALUES (103, '代码生成', 2, 'com.ruoyi.gen.controller.GenController.editSave()', 'PUT', 1, 'admin', NULL, '/gen', '127.0.0.1', '', '{\"businessName\":\"his_doctor\",\"className\":\"Doctor\",\"columns\":[{\"capJavaField\":\"DoctorId\",\"columnComment\":\"医生ID\",\"columnId\":1,\"columnName\":\"doctor_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2026-05-29 14:52:20\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":true,\"insert\":true,\"isIncrement\":\"1\",\"isInsert\":\"1\",\"isPk\":\"1\",\"javaField\":\"doctorId\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":true,\"query\":false,\"queryType\":\"EQ\",\"required\":false,\"sort\":1,\"superColumn\":false,\"tableId\":1,\"updateBy\":\"\",\"updateTime\":\"2026-05-30 22:55:51\",\"usableColumn\":false},{\"capJavaField\":\"UserId\",\"columnComment\":\"系统用户ID（关联若依sys_user）\",\"columnId\":2,\"columnName\":\"user_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2026-05-29 14:52:20\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"userId\",\"javaType\":\"Long\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":2,\"superColumn\":false,\"tableId\":1,\"updateBy\":\"\",\"updateTime\":\"2026-05-30 22:55:51\",\"usableColumn\":false},{\"capJavaField\":\"DeptId\",\"columnComment\":\"所属科室（关联若依sys_dept）\",\"columnId\":3,\"columnName\":\"dept_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2026-05-29 14:52:20\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"deptId\",\"javaType\":\"Long\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":3,\"superColumn\":false,\"tableId\":1,\"updateBy\":\"\",\"updateTime\":\"2026-05-30 22:55:51\",\"usableColumn\":false},{\"capJavaField\":\"DoctorNo\",\"columnComment\":\"医生工号\",\"columnId\":4,\"columnName\":\"doctor_no\",\"columnType\":\"varchar(50)\",\"createBy\":\"admin\",\"createTime\":\"2026-05-29 14:52:20\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"i', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-05-30 22:56:58', 27);
INSERT INTO `sys_oper_log` VALUES (104, '代码生成', 2, 'com.ruoyi.gen.controller.GenController.editSave()', 'PUT', 1, 'admin', NULL, '/gen', '127.0.0.1', '', '{\"businessName\":\"his_doctor\",\"className\":\"Doctor\",\"columns\":[{\"capJavaField\":\"DoctorId\",\"columnComment\":\"医生ID\",\"columnId\":1,\"columnName\":\"doctor_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2026-05-29 14:52:20\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":true,\"insert\":true,\"isIncrement\":\"1\",\"isInsert\":\"1\",\"isPk\":\"1\",\"javaField\":\"doctorId\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":true,\"query\":false,\"queryType\":\"EQ\",\"required\":false,\"sort\":1,\"superColumn\":false,\"tableId\":1,\"updateBy\":\"\",\"updateTime\":\"2026-05-30 22:56:58\",\"usableColumn\":false},{\"capJavaField\":\"UserId\",\"columnComment\":\"系统用户ID（关联若依sys_user）\",\"columnId\":2,\"columnName\":\"user_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2026-05-29 14:52:20\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"userId\",\"javaType\":\"Long\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":2,\"superColumn\":false,\"tableId\":1,\"updateBy\":\"\",\"updateTime\":\"2026-05-30 22:56:58\",\"usableColumn\":false},{\"capJavaField\":\"DeptId\",\"columnComment\":\"所属科室（关联若依sys_dept）\",\"columnId\":3,\"columnName\":\"dept_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2026-05-29 14:52:20\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"deptId\",\"javaType\":\"Long\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":3,\"superColumn\":false,\"tableId\":1,\"updateBy\":\"\",\"updateTime\":\"2026-05-30 22:56:58\",\"usableColumn\":false},{\"capJavaField\":\"DoctorNo\",\"columnComment\":\"医生工号\",\"columnId\":4,\"columnName\":\"doctor_no\",\"columnType\":\"varchar(50)\",\"createBy\":\"admin\",\"createTime\":\"2026-05-29 14:52:20\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"i', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-05-30 22:59:22', 25);
INSERT INTO `sys_oper_log` VALUES (105, '菜单管理', 1, 'com.ruoyi.system.controller.SysMenuController.add()', 'POST', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"children\":[],\"createBy\":\"admin\",\"icon\":\"user\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuName\":\"医生安排\",\"menuType\":\"M\",\"orderNum\":1,\"params\":{},\"parentId\":0,\"path\":\"/hisdoctor\",\"status\":\"0\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-05-30 23:07:46', 81);
INSERT INTO `sys_oper_log` VALUES (106, '代码生成', 2, 'com.ruoyi.gen.controller.GenController.editSave()', 'PUT', 1, 'admin', NULL, '/gen', '127.0.0.1', '', '{\"businessName\":\"doctor\",\"className\":\"Doctor\",\"columns\":[{\"capJavaField\":\"DoctorId\",\"columnComment\":\"医生ID\",\"columnId\":1,\"columnName\":\"doctor_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2026-05-29 14:52:20\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":true,\"insert\":true,\"isIncrement\":\"1\",\"isInsert\":\"1\",\"isPk\":\"1\",\"javaField\":\"doctorId\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":true,\"query\":false,\"queryType\":\"EQ\",\"required\":false,\"sort\":1,\"superColumn\":false,\"tableId\":1,\"updateBy\":\"\",\"updateTime\":\"2026-05-30 22:59:22\",\"usableColumn\":false},{\"capJavaField\":\"UserId\",\"columnComment\":\"系统用户ID（关联若依sys_user）\",\"columnId\":2,\"columnName\":\"user_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2026-05-29 14:52:20\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"userId\",\"javaType\":\"Long\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":2,\"superColumn\":false,\"tableId\":1,\"updateBy\":\"\",\"updateTime\":\"2026-05-30 22:59:22\",\"usableColumn\":false},{\"capJavaField\":\"DeptId\",\"columnComment\":\"所属科室（关联若依sys_dept）\",\"columnId\":3,\"columnName\":\"dept_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2026-05-29 14:52:20\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"deptId\",\"javaType\":\"Long\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":3,\"superColumn\":false,\"tableId\":1,\"updateBy\":\"\",\"updateTime\":\"2026-05-30 22:59:22\",\"usableColumn\":false},{\"capJavaField\":\"DoctorNo\",\"columnComment\":\"医生工号\",\"columnId\":4,\"columnName\":\"doctor_no\",\"columnType\":\"varchar(50)\",\"createBy\":\"admin\",\"createTime\":\"2026-05-29 14:52:20\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdi', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-05-30 23:14:18', 23);
INSERT INTO `sys_oper_log` VALUES (107, '代码生成', 8, 'com.ruoyi.gen.controller.GenController.batchGenCode()', 'GET', 1, 'admin', NULL, '/gen/batchGenCode', '127.0.0.1', '', '{\"tables\":\"doctor\"}', NULL, 0, NULL, '2026-05-30 23:14:23', 90);
INSERT INTO `sys_oper_log` VALUES (108, '菜单管理', 1, 'com.ruoyi.system.controller.SysMenuController.add()', 'POST', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"children\":[],\"createBy\":\"admin\",\"icon\":\"tool\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuName\":\"处方药品管理\",\"menuType\":\"M\",\"orderNum\":5,\"params\":{},\"parentId\":0,\"path\":\"/hisprescription\",\"status\":\"0\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-02 11:07:55', 63);
INSERT INTO `sys_oper_log` VALUES (109, '代码生成', 6, 'com.ruoyi.gen.controller.GenController.importTableSave()', 'POST', 1, 'admin', NULL, '/gen/importTable', '127.0.0.1', '', '{\"tables\":\"prescription_item,prescription,drug\",\"tplWebType\":\"element-plus\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-02 11:18:35', 216);
INSERT INTO `sys_oper_log` VALUES (110, '代码生成', 6, 'com.ruoyi.gen.controller.GenController.importTableSave()', 'POST', 1, 'admin', NULL, '/gen/importTable', '127.0.0.1', '', '{\"tables\":\"schedule\",\"tplWebType\":\"element-plus\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-02 11:18:53', 32);
INSERT INTO `sys_oper_log` VALUES (111, '代码生成', 6, 'com.ruoyi.gen.controller.GenController.importTableSave()', 'POST', 1, 'admin', NULL, '/gen/importTable', '127.0.0.1', '', '{\"tables\":\"regist_level\",\"tplWebType\":\"element-plus\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-02 11:19:09', 34);
INSERT INTO `sys_oper_log` VALUES (112, '代码生成', 2, 'com.ruoyi.gen.controller.GenController.editSave()', 'PUT', 1, 'admin', NULL, '/gen', '127.0.0.1', '', '{\"businessName\":\"drug\",\"className\":\"Drug\",\"columns\":[{\"capJavaField\":\"Id\",\"columnComment\":\"药品ID(主键)\",\"columnId\":16,\"columnName\":\"id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2026-06-02 11:18:35\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":true,\"insert\":true,\"isIncrement\":\"1\",\"isInsert\":\"1\",\"isPk\":\"1\",\"javaField\":\"id\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":true,\"query\":false,\"queryType\":\"EQ\",\"required\":false,\"sort\":1,\"superColumn\":false,\"tableId\":2,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"DrugCode\",\"columnComment\":\"药品编码\",\"columnId\":17,\"columnName\":\"drug_code\",\"columnType\":\"varchar(64)\",\"createBy\":\"admin\",\"createTime\":\"2026-06-02 11:18:35\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"drugCode\",\"javaType\":\"String\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":2,\"superColumn\":false,\"tableId\":2,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"DrugName\",\"columnComment\":\"药品名称\",\"columnId\":18,\"columnName\":\"drug_name\",\"columnType\":\"varchar(100)\",\"createBy\":\"admin\",\"createTime\":\"2026-06-02 11:18:35\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"drugName\",\"javaType\":\"String\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"LIKE\",\"required\":true,\"sort\":3,\"superColumn\":false,\"tableId\":2,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"PyCode\",\"columnComment\":\"拼音码(检索用)\",\"columnId\":19,\"columnName\":\"py_code\",\"columnType\":\"varchar(64)\",\"createBy\":\"admin\",\"createTime\":\"2026-06-02 11:18:35\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"javaField\":\"pyCode\",\"javaType\":\"String\",\"l', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-02 11:39:22', 121);
INSERT INTO `sys_oper_log` VALUES (113, '代码生成', 8, 'com.ruoyi.gen.controller.GenController.batchGenCode()', 'GET', 1, 'admin', NULL, '/gen/batchGenCode', '127.0.0.1', '', '{\"tables\":\"drug\"}', NULL, 0, NULL, '2026-06-02 11:39:33', 325);
INSERT INTO `sys_oper_log` VALUES (114, '菜单管理', 1, 'com.ruoyi.system.controller.SysMenuController.add()', 'POST', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"children\":[],\"createBy\":\"admin\",\"icon\":\"skill\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuName\":\"医院检测\",\"menuType\":\"M\",\"orderNum\":6,\"params\":{},\"parentId\":0,\"path\":\"/hisexam\",\"status\":\"0\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-02 12:26:17', 22);
INSERT INTO `sys_oper_log` VALUES (115, '菜单管理', 2, 'com.ruoyi.system.controller.SysMenuController.edit()', 'PUT', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"children\":[],\"createTime\":\"2026-06-02 11:07:54\",\"icon\":\"tool\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2007,\"menuName\":\"处方药品管理\",\"menuType\":\"M\",\"orderNum\":5,\"params\":{},\"parentId\":0,\"path\":\"/hisprescription\",\"perms\":\"\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-02 13:08:23', 18);
INSERT INTO `sys_oper_log` VALUES (116, '代码生成', 6, 'com.ruoyi.gen.controller.GenController.importTableSave()', 'POST', 1, 'admin', NULL, '/gen/importTable', '127.0.0.1', '', '{\"tables\":\"exam_apply\",\"tplWebType\":\"element-plus\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-02 14:25:25', 48);
INSERT INTO `sys_oper_log` VALUES (117, '代码生成', 6, 'com.ruoyi.gen.controller.GenController.importTableSave()', 'POST', 1, 'admin', NULL, '/gen/importTable', '127.0.0.1', '', '{\"tables\":\"medical_technology\",\"tplWebType\":\"element-plus\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-02 14:25:56', 29);
INSERT INTO `sys_oper_log` VALUES (118, '代码生成', 2, 'com.ruoyi.gen.controller.GenController.editSave()', 'PUT', 1, 'admin', NULL, '/gen', '127.0.0.1', '', '{\"businessName\":\"technology\",\"className\":\"MedicalTechnology\",\"columns\":[{\"capJavaField\":\"Id\",\"columnComment\":\"主键ID\",\"columnId\":92,\"columnName\":\"id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2026-06-02 14:25:56\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":true,\"insert\":true,\"isIncrement\":\"1\",\"isInsert\":\"1\",\"isPk\":\"1\",\"javaField\":\"id\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":true,\"query\":false,\"queryType\":\"EQ\",\"required\":false,\"sort\":1,\"superColumn\":false,\"tableId\":8,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"TechCode\",\"columnComment\":\"项目编码\",\"columnId\":93,\"columnName\":\"tech_code\",\"columnType\":\"varchar(64)\",\"createBy\":\"admin\",\"createTime\":\"2026-06-02 14:25:56\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"techCode\",\"javaType\":\"String\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":2,\"superColumn\":false,\"tableId\":8,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"TechName\",\"columnComment\":\"项目名称\",\"columnId\":94,\"columnName\":\"tech_name\",\"columnType\":\"varchar(64)\",\"createBy\":\"admin\",\"createTime\":\"2026-06-02 14:25:56\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"techName\",\"javaType\":\"String\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"LIKE\",\"required\":true,\"sort\":3,\"superColumn\":false,\"tableId\":8,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"TechFormat\",\"columnComment\":\"规格\",\"columnId\":95,\"columnName\":\"tech_format\",\"columnType\":\"varchar(64)\",\"createBy\":\"admin\",\"createTime\":\"2026-06-02 14:25:56\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"javaField\":\"techFormat\",\"j', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-02 14:32:32', 35);
INSERT INTO `sys_oper_log` VALUES (119, '代码生成', 2, 'com.ruoyi.gen.controller.GenController.editSave()', 'PUT', 1, 'admin', NULL, '/gen', '127.0.0.1', '', '{\"businessName\":\"apply\",\"className\":\"ExamApply\",\"columns\":[{\"capJavaField\":\"ApplyId\",\"columnComment\":\"申请ID(主键)\",\"columnId\":71,\"columnName\":\"apply_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2026-06-02 14:25:24\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":true,\"insert\":true,\"isIncrement\":\"1\",\"isInsert\":\"1\",\"isPk\":\"1\",\"javaField\":\"applyId\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":true,\"query\":false,\"queryType\":\"EQ\",\"required\":false,\"sort\":1,\"superColumn\":false,\"tableId\":7,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"RegisterId\",\"columnComment\":\"挂号ID\",\"columnId\":72,\"columnName\":\"register_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2026-06-02 14:25:24\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"registerId\",\"javaType\":\"Long\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":2,\"superColumn\":false,\"tableId\":7,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"EncounterId\",\"columnComment\":\"接诊ID\",\"columnId\":73,\"columnName\":\"encounter_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2026-06-02 14:25:24\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"encounterId\",\"javaType\":\"Long\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":3,\"superColumn\":false,\"tableId\":7,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"PatientId\",\"columnComment\":\"患者ID\",\"columnId\":74,\"columnName\":\"patient_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2026-06-02 14:25:24\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaFiel', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-02 14:35:25', 31);
INSERT INTO `sys_oper_log` VALUES (120, '代码生成', 8, 'com.ruoyi.gen.controller.GenController.batchGenCode()', 'GET', 1, 'admin', NULL, '/gen/batchGenCode', '127.0.0.1', '', '{\"tables\":\"medical_technology\"}', NULL, 0, NULL, '2026-06-02 14:35:29', 81);
INSERT INTO `sys_oper_log` VALUES (121, '代码生成', 8, 'com.ruoyi.gen.controller.GenController.batchGenCode()', 'GET', 1, 'admin', NULL, '/gen/batchGenCode', '127.0.0.1', '', '{\"tables\":\"exam_apply\"}', NULL, 0, NULL, '2026-06-02 14:39:40', 108);
INSERT INTO `sys_oper_log` VALUES (122, '代码生成', 8, 'com.ruoyi.gen.controller.GenController.batchGenCode()', 'GET', 1, 'admin', NULL, '/gen/batchGenCode', '127.0.0.1', '', '{\"tables\":\"exam_apply\"}', NULL, 0, NULL, '2026-06-02 14:40:06', 75);
INSERT INTO `sys_oper_log` VALUES (123, '代码生成', 2, 'com.ruoyi.gen.controller.GenController.editSave()', 'PUT', 1, 'admin', NULL, '/gen', '127.0.0.1', '', '{\"businessName\":\"schedule\",\"className\":\"Schedule\",\"columns\":[{\"capJavaField\":\"ScheduleId\",\"columnComment\":\"排班ID\",\"columnId\":55,\"columnName\":\"schedule_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2026-06-02 11:18:53\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":true,\"insert\":true,\"isIncrement\":\"1\",\"isInsert\":\"1\",\"isPk\":\"1\",\"javaField\":\"scheduleId\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":true,\"query\":false,\"queryType\":\"EQ\",\"required\":false,\"sort\":1,\"superColumn\":false,\"tableId\":5,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"DoctorId\",\"columnComment\":\"医生ID\",\"columnId\":56,\"columnName\":\"doctor_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2026-06-02 11:18:53\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"doctorId\",\"javaType\":\"Long\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":2,\"superColumn\":false,\"tableId\":5,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"ScheduleDate\",\"columnComment\":\"排班日期\",\"columnId\":57,\"columnName\":\"schedule_date\",\"columnType\":\"date\",\"createBy\":\"admin\",\"createTime\":\"2026-06-02 11:18:53\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"datetime\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"scheduleDate\",\"javaType\":\"Date\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":3,\"superColumn\":false,\"tableId\":5,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"TimeSlot\",\"columnComment\":\"午别（morning/afternoon/evening）\",\"columnId\":58,\"columnName\":\"time_slot\",\"columnType\":\"varchar(20)\",\"createBy\":\"admin\",\"createTime\":\"2026-06-02 11:18:53\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuer', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-02 15:35:11', 44);
INSERT INTO `sys_oper_log` VALUES (124, '代码生成', 8, 'com.ruoyi.gen.controller.GenController.batchGenCode()', 'GET', 1, 'admin', NULL, '/gen/batchGenCode', '127.0.0.1', '', '{\"tables\":\"schedule\"}', NULL, 0, NULL, '2026-06-02 15:35:18', 103);
INSERT INTO `sys_oper_log` VALUES (125, '代码生成', 2, 'com.ruoyi.gen.controller.GenController.editSave()', 'PUT', 1, 'admin', NULL, '/gen', '127.0.0.1', '', '{\"businessName\":\"doctorlevel\",\"className\":\"RegistLevel\",\"columns\":[{\"capJavaField\":\"LevelId\",\"columnComment\":\"级别ID(主键)\",\"columnId\":64,\"columnName\":\"level_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2026-06-02 11:19:09\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":true,\"insert\":true,\"isIncrement\":\"1\",\"isInsert\":\"1\",\"isPk\":\"1\",\"javaField\":\"levelId\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":true,\"query\":false,\"queryType\":\"EQ\",\"required\":false,\"sort\":1,\"superColumn\":false,\"tableId\":6,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"LevelName\",\"columnComment\":\"级别名称：普通号/专家号/主任号\",\"columnId\":65,\"columnName\":\"level_name\",\"columnType\":\"varchar(50)\",\"createBy\":\"admin\",\"createTime\":\"2026-06-02 11:19:09\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"levelName\",\"javaType\":\"String\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"LIKE\",\"required\":true,\"sort\":2,\"superColumn\":false,\"tableId\":6,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"Fee\",\"columnComment\":\"对应挂号费用\",\"columnId\":66,\"columnName\":\"fee\",\"columnType\":\"decimal(10,2)\",\"createBy\":\"admin\",\"createTime\":\"2026-06-02 11:19:09\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"javaField\":\"fee\",\"javaType\":\"BigDecimal\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":false,\"sort\":3,\"superColumn\":false,\"tableId\":6,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"Sort\",\"columnComment\":\"排序号\",\"columnId\":67,\"columnName\":\"sort\",\"columnType\":\"int\",\"createBy\":\"admin\",\"createTime\":\"2026-06-02 11:19:09\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"javaField\":\"sort\",\"javaType\":\"Long\",\"lis', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-02 15:50:41', 24);
INSERT INTO `sys_oper_log` VALUES (126, '代码生成', 2, 'com.ruoyi.gen.controller.GenController.editSave()', 'PUT', 1, 'admin', NULL, '/gen', '127.0.0.1', '', '{\"businessName\":\"doctorlevel\",\"className\":\"RegistLevel\",\"columns\":[{\"capJavaField\":\"LevelId\",\"columnComment\":\"级别ID(主键)\",\"columnId\":64,\"columnName\":\"level_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2026-06-02 11:19:09\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":true,\"insert\":true,\"isIncrement\":\"1\",\"isInsert\":\"1\",\"isPk\":\"1\",\"javaField\":\"levelId\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":true,\"query\":false,\"queryType\":\"EQ\",\"required\":false,\"sort\":1,\"superColumn\":false,\"tableId\":6,\"updateBy\":\"\",\"updateTime\":\"2026-06-02 15:50:41\",\"usableColumn\":false},{\"capJavaField\":\"LevelName\",\"columnComment\":\"级别名称：普通号/专家号/主任号\",\"columnId\":65,\"columnName\":\"level_name\",\"columnType\":\"varchar(50)\",\"createBy\":\"admin\",\"createTime\":\"2026-06-02 11:19:09\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"levelName\",\"javaType\":\"String\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"LIKE\",\"required\":true,\"sort\":2,\"superColumn\":false,\"tableId\":6,\"updateBy\":\"\",\"updateTime\":\"2026-06-02 15:50:41\",\"usableColumn\":false},{\"capJavaField\":\"Fee\",\"columnComment\":\"对应挂号费用\",\"columnId\":66,\"columnName\":\"fee\",\"columnType\":\"decimal(10,2)\",\"createBy\":\"admin\",\"createTime\":\"2026-06-02 11:19:09\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"javaField\":\"fee\",\"javaType\":\"BigDecimal\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":false,\"sort\":3,\"superColumn\":false,\"tableId\":6,\"updateBy\":\"\",\"updateTime\":\"2026-06-02 15:50:41\",\"usableColumn\":false},{\"capJavaField\":\"Sort\",\"columnComment\":\"排序号\",\"columnId\":67,\"columnName\":\"sort\",\"columnType\":\"int\",\"createBy\":\"admin\",\"createTime\":\"2026-06-02 11:19:09\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncr', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-02 15:54:18', 30);
INSERT INTO `sys_oper_log` VALUES (127, '代码生成', 8, 'com.ruoyi.gen.controller.GenController.batchGenCode()', 'GET', 1, 'admin', NULL, '/gen/batchGenCode', '127.0.0.1', '', '{\"tables\":\"regist_level\"}', NULL, 0, NULL, '2026-06-02 15:54:47', 82);
INSERT INTO `sys_oper_log` VALUES (128, '代码生成', 2, 'com.ruoyi.gen.controller.GenController.editSave()', 'PUT', 1, 'admin', NULL, '/gen', '127.0.0.1', '', '{\"businessName\":\"prescription\",\"className\":\"Prescription\",\"columns\":[{\"capJavaField\":\"PrescriptionId\",\"columnComment\":\"处方ID(主键)\",\"columnId\":30,\"columnName\":\"prescription_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2026-06-02 11:18:35\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":true,\"insert\":true,\"isIncrement\":\"1\",\"isInsert\":\"1\",\"isPk\":\"1\",\"javaField\":\"prescriptionId\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":true,\"query\":false,\"queryType\":\"EQ\",\"required\":false,\"sort\":1,\"superColumn\":false,\"tableId\":3,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"RegisterId\",\"columnComment\":\"挂号ID\",\"columnId\":31,\"columnName\":\"register_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2026-06-02 11:18:35\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"registerId\",\"javaType\":\"Long\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":2,\"superColumn\":false,\"tableId\":3,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"EncounterId\",\"columnComment\":\"接诊ID\",\"columnId\":32,\"columnName\":\"encounter_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2026-06-02 11:18:35\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"encounterId\",\"javaType\":\"Long\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":3,\"superColumn\":false,\"tableId\":3,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"PatientId\",\"columnComment\":\"患者ID\",\"columnId\":33,\"columnName\":\"patient_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2026-06-02 11:18:35\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\"', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-02 16:16:13', 44);
INSERT INTO `sys_oper_log` VALUES (129, '代码生成', 2, 'com.ruoyi.gen.controller.GenController.editSave()', 'PUT', 1, 'admin', NULL, '/gen', '127.0.0.1', '', '{\"businessName\":\"item\",\"className\":\"PrescriptionItem\",\"columns\":[{\"capJavaField\":\"ItemId\",\"columnComment\":\"明细ID(主键)\",\"columnId\":43,\"columnName\":\"item_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2026-06-02 11:18:35\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":true,\"insert\":true,\"isIncrement\":\"1\",\"isInsert\":\"1\",\"isPk\":\"1\",\"javaField\":\"itemId\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":true,\"query\":false,\"queryType\":\"EQ\",\"required\":false,\"sort\":1,\"superColumn\":false,\"tableId\":4,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"PrescriptionId\",\"columnComment\":\"处方ID\",\"columnId\":44,\"columnName\":\"prescription_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2026-06-02 11:18:35\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"prescriptionId\",\"javaType\":\"Long\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":2,\"superColumn\":false,\"tableId\":4,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"DrugId\",\"columnComment\":\"药品ID\",\"columnId\":45,\"columnName\":\"drug_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2026-06-02 11:18:35\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"drugId\",\"javaType\":\"Long\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":3,\"superColumn\":false,\"tableId\":4,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"DrugName\",\"columnComment\":\"药品名称\",\"columnId\":46,\"columnName\":\"drug_name\",\"columnType\":\"varchar(100)\",\"createBy\":\"admin\",\"createTime\":\"2026-06-02 11:18:35\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"java', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-02 16:17:04', 29);
INSERT INTO `sys_oper_log` VALUES (130, '代码生成', 8, 'com.ruoyi.gen.controller.GenController.batchGenCode()', 'GET', 1, 'admin', NULL, '/gen/batchGenCode', '127.0.0.1', '', '{\"tables\":\"prescription\"}', NULL, 0, NULL, '2026-06-02 16:17:10', 91);
INSERT INTO `sys_oper_log` VALUES (131, '代码生成', 8, 'com.ruoyi.gen.controller.GenController.batchGenCode()', 'GET', 1, 'admin', NULL, '/gen/batchGenCode', '127.0.0.1', '', '{\"tables\":\"prescription_item\"}', NULL, 0, NULL, '2026-06-03 09:38:02', 68);
INSERT INTO `sys_oper_log` VALUES (132, '菜单管理', 1, 'com.ruoyi.system.controller.SysMenuController.add()', 'POST', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"children\":[],\"createBy\":\"admin\",\"icon\":\"people\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuName\":\"患者管理\",\"menuType\":\"M\",\"orderNum\":1,\"params\":{},\"parentId\":0,\"path\":\"/patient\",\"status\":\"0\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-03 10:30:12', 19);
INSERT INTO `sys_oper_log` VALUES (133, '菜单管理', 2, 'com.ruoyi.system.controller.SysMenuController.edit()', 'PUT', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"children\":[],\"component\":\"patient/patient/index\",\"createTime\":\"2026-06-03 10:06:23\",\"icon\":\"#\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2081,\"menuName\":\"患者\",\"menuType\":\"C\",\"orderNum\":1,\"params\":{},\"parentId\":2105,\"path\":\"patient\",\"perms\":\"patient:patient:list\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-03 10:30:37', 13);
INSERT INTO `sys_oper_log` VALUES (134, '菜单管理', 1, 'com.ruoyi.system.controller.SysMenuController.add()', 'POST', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"children\":[],\"createBy\":\"admin\",\"icon\":\"list\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuName\":\"挂号管理\",\"menuType\":\"M\",\"orderNum\":3,\"params\":{},\"parentId\":0,\"path\":\"/regist\",\"status\":\"0\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-03 10:31:30', 7);
INSERT INTO `sys_oper_log` VALUES (135, '菜单管理', 2, 'com.ruoyi.system.controller.SysMenuController.edit()', 'PUT', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"children\":[],\"component\":\"hisdoctor/doctorlevel/index\",\"createTime\":\"2026-06-02 15:59:52\",\"icon\":\"#\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2033,\"menuName\":\"挂号级别\",\"menuType\":\"C\",\"orderNum\":1,\"params\":{},\"parentId\":2106,\"path\":\"doctorlevel\",\"perms\":\"hisdoctor:doctorlevel:list\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-03 10:31:42', 9);
INSERT INTO `sys_oper_log` VALUES (136, '菜单管理', 2, 'com.ruoyi.system.controller.SysMenuController.edit()', 'PUT', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"children\":[],\"component\":\"register/register/index\",\"createTime\":\"2026-06-03 10:06:37\",\"icon\":\"#\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2099,\"menuName\":\"挂号\",\"menuType\":\"C\",\"orderNum\":1,\"params\":{},\"parentId\":2106,\"path\":\"register\",\"perms\":\"register:register:list\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-03 10:32:03', 8);
INSERT INTO `sys_oper_log` VALUES (137, '菜单管理', 2, 'com.ruoyi.system.controller.SysMenuController.edit()', 'PUT', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"children\":[],\"component\":\"register/in/index\",\"createTime\":\"2026-06-03 10:06:13\",\"icon\":\"#\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2069,\"menuName\":\"签到\",\"menuType\":\"C\",\"orderNum\":1,\"params\":{},\"parentId\":2106,\"path\":\"in\",\"perms\":\"register:in:list\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-03 10:32:15', 6);
INSERT INTO `sys_oper_log` VALUES (138, '菜单管理', 2, 'com.ruoyi.system.controller.SysMenuController.edit()', 'PUT', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"children\":[],\"createTime\":\"2026-06-03 10:58:26\",\"icon\":\"edit\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2107,\"menuName\":\"会诊管理\",\"menuType\":\"M\",\"orderNum\":109,\"params\":{},\"parentId\":0,\"path\":\"/emr\",\"perms\":\"\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-03 11:01:57', 9);
INSERT INTO `sys_oper_log` VALUES (139, '菜单管理', 2, 'com.ruoyi.system.controller.SysMenuController.edit()', 'PUT', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"children\":[],\"createTime\":\"2026-06-03 10:58:26\",\"icon\":\"money\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2108,\"menuName\":\"收费管理\",\"menuType\":\"M\",\"orderNum\":110,\"params\":{},\"parentId\":0,\"path\":\"/payment\",\"perms\":\"\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-03 11:02:25', 9);
INSERT INTO `sys_oper_log` VALUES (140, '保存菜单排序', 2, 'com.ruoyi.system.controller.SysMenuController.updateSort()', 'PUT', 1, 'admin', NULL, '/menu/updateSort', '127.0.0.1', '', '{\"menuIds\":\"2107\",\"orderNums\":\"3\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-03 11:03:05', 57);
INSERT INTO `sys_oper_log` VALUES (141, '保存菜单排序', 2, 'com.ruoyi.system.controller.SysMenuController.updateSort()', 'PUT', 1, 'admin', NULL, '/menu/updateSort', '127.0.0.1', '', '{\"menuIds\":\"2000,2,3,4,2107,2108\",\"orderNums\":\"2,9,8,10,4,7\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-03 11:04:35', 12);
INSERT INTO `sys_oper_log` VALUES (142, '菜单管理', 3, 'com.ruoyi.system.controller.SysMenuController.remove()', 'DELETE', 1, 'admin', NULL, '/menu/2075', '127.0.0.1', '', '2075 ', '{\"msg\":\"存在子菜单,不允许删除\",\"code\":601}', 0, NULL, '2026-06-03 11:22:14', 4);
INSERT INTO `sys_oper_log` VALUES (143, '菜单管理', 2, 'com.ruoyi.system.controller.SysMenuController.edit()', 'PUT', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"children\":[],\"component\":\"payment/item/index\",\"createTime\":\"2026-06-03 10:06:18\",\"icon\":\"#\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2075,\"menuName\":\"收费明细\",\"menuType\":\"C\",\"orderNum\":1,\"params\":{},\"parentId\":2108,\"path\":\"item\",\"perms\":\"payment:item:list\",\"routeName\":\"\",\"status\":\"0\",\"visible\":\"1\"} ', '{\"msg\":\"修改菜单\'收费明细\'失败，路由名称或地址已存在\",\"code\":500}', 0, NULL, '2026-06-03 11:22:31', 9);
INSERT INTO `sys_oper_log` VALUES (144, '菜单管理', 2, 'com.ruoyi.system.controller.SysMenuController.edit()', 'PUT', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"children\":[],\"component\":\"payment/item/index\",\"createTime\":\"2026-06-03 10:06:18\",\"icon\":\"#\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2075,\"menuName\":\"收费明细\",\"menuType\":\"C\",\"orderNum\":1,\"params\":{},\"parentId\":2108,\"path\":\"item\",\"perms\":\"payment:item:list\",\"routeName\":\"\",\"status\":\"0\",\"visible\":\"1\"} ', '{\"msg\":\"修改菜单\'收费明细\'失败，路由名称或地址已存在\",\"code\":500}', 0, NULL, '2026-06-03 11:22:36', 5);
INSERT INTO `sys_oper_log` VALUES (145, '菜单管理', 2, 'com.ruoyi.system.controller.SysMenuController.edit()', 'PUT', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"children\":[],\"component\":\"payment/item/index\",\"createTime\":\"2026-06-03 10:06:18\",\"icon\":\"#\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2075,\"menuName\":\"收费明细\",\"menuType\":\"C\",\"orderNum\":1,\"params\":{},\"parentId\":2108,\"path\":\"item\",\"perms\":\"payment:item:list\",\"routeName\":\"\",\"status\":\"1\",\"visible\":\"1\"} ', '{\"msg\":\"修改菜单\'收费明细\'失败，路由名称或地址已存在\",\"code\":500}', 0, NULL, '2026-06-03 11:22:41', 4);
INSERT INTO `sys_oper_log` VALUES (146, '菜单管理', 2, 'com.ruoyi.system.controller.SysMenuController.edit()', 'PUT', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"children\":[],\"component\":\"payment/item/index\",\"createTime\":\"2026-06-03 10:06:18\",\"icon\":\"#\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2075,\"menuName\":\"收费明细\",\"menuType\":\"C\",\"orderNum\":1,\"params\":{},\"parentId\":2108,\"path\":\"item\",\"perms\":\"payment:item:list\",\"routeName\":\"\",\"status\":\"0\",\"visible\":\"0\"} ', '{\"msg\":\"修改菜单\'收费明细\'失败，路由名称或地址已存在\",\"code\":500}', 0, NULL, '2026-06-03 11:22:49', 7);
INSERT INTO `sys_oper_log` VALUES (147, '菜单管理', 2, 'com.ruoyi.system.controller.SysMenuController.edit()', 'PUT', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"children\":[],\"component\":\"payment/item/index\",\"createTime\":\"2026-06-03 10:06:18\",\"icon\":\"#\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2075,\"menuName\":\"收费明细\",\"menuType\":\"C\",\"orderNum\":1,\"params\":{},\"parentId\":2108,\"path\":\"item\",\"perms\":\"payment:item:list\",\"routeName\":\"\",\"status\":\"0\",\"visible\":\"1\"} ', '{\"msg\":\"修改菜单\'收费明细\'失败，路由名称或地址已存在\",\"code\":500}', 0, NULL, '2026-06-03 11:23:12', 7);
INSERT INTO `sys_oper_log` VALUES (148, '菜单管理', 2, 'com.ruoyi.system.controller.SysMenuController.edit()', 'PUT', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"children\":[],\"createTime\":\"2026-06-03 10:58:26\",\"icon\":\"money\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2108,\"menuName\":\"收费管理\",\"menuType\":\"M\",\"orderNum\":7,\"params\":{},\"parentId\":0,\"path\":\"/payment\",\"perms\":\"\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"1\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-03 11:23:43', 15);
INSERT INTO `sys_oper_log` VALUES (149, '菜单管理', 2, 'com.ruoyi.system.controller.SysMenuController.edit()', 'PUT', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"children\":[],\"createTime\":\"2026-06-03 10:58:26\",\"icon\":\"money\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2108,\"menuName\":\"收费管理\",\"menuType\":\"M\",\"orderNum\":7,\"params\":{},\"parentId\":0,\"path\":\"/payment\",\"perms\":\"\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-03 11:23:49', 8);
INSERT INTO `sys_oper_log` VALUES (150, '菜单管理', 2, 'com.ruoyi.system.controller.SysMenuController.edit()', 'PUT', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"children\":[],\"component\":\"payment/category/index\",\"createTime\":\"2026-06-03 10:05:43\",\"icon\":\"#\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2057,\"menuName\":\"结算类别\",\"menuType\":\"C\",\"orderNum\":1,\"params\":{},\"parentId\":2108,\"path\":\"category\",\"perms\":\"payment:category:list\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"1\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-03 11:23:56', 9);
INSERT INTO `sys_oper_log` VALUES (151, '菜单管理', 2, 'com.ruoyi.system.controller.SysMenuController.edit()', 'PUT', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"children\":[],\"component\":\"payment/category/index\",\"createTime\":\"2026-06-03 10:05:43\",\"icon\":\"#\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2057,\"menuName\":\"结算类别\",\"menuType\":\"C\",\"orderNum\":1,\"params\":{},\"parentId\":2108,\"path\":\"category\",\"perms\":\"payment:category:list\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-03 11:24:03', 9);
INSERT INTO `sys_oper_log` VALUES (152, '菜单管理', 2, 'com.ruoyi.system.controller.SysMenuController.edit()', 'PUT', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"children\":[],\"component\":\"payment/item/index\",\"createTime\":\"2026-06-03 10:06:18\",\"icon\":\"#\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2075,\"menuName\":\"收费明细\",\"menuType\":\"C\",\"orderNum\":1,\"params\":{},\"parentId\":2108,\"path\":\"item\",\"perms\":\"payment:item:list\",\"routeName\":\"\",\"status\":\"0\",\"visible\":\"1\"} ', '{\"msg\":\"修改菜单\'收费明细\'失败，路由名称或地址已存在\",\"code\":500}', 0, NULL, '2026-06-03 11:24:11', 6);
INSERT INTO `sys_oper_log` VALUES (153, '菜单管理', 2, 'com.ruoyi.system.controller.SysMenuController.edit()', 'PUT', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"children\":[],\"component\":\"payment/item/index\",\"createTime\":\"2026-06-03 10:06:18\",\"icon\":\"#\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2075,\"menuName\":\"收费明细\",\"menuType\":\"C\",\"orderNum\":1,\"params\":{},\"parentId\":2108,\"path\":\"paymentItem\",\"perms\":\"payment:item:list\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"1\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-03 11:30:28', 9);
INSERT INTO `sys_oper_log` VALUES (154, '菜单管理', 2, 'com.ruoyi.system.controller.SysMenuController.edit()', 'PUT', 1, 'admin', NULL, '/menu', '127.0.0.1', '', '{\"children\":[],\"component\":\"payment/item/index\",\"createTime\":\"2026-06-03 10:06:18\",\"icon\":\"#\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2075,\"menuName\":\"收费明细\",\"menuType\":\"C\",\"orderNum\":1,\"params\":{},\"parentId\":2108,\"path\":\"paymentItem\",\"perms\":\"payment:item:list\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"1\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-03 11:30:37', 8);
INSERT INTO `sys_oper_log` VALUES (155, '用户头像', 2, 'com.ruoyi.system.controller.SysProfileController.avatar()', 'POST', 1, 'admin', NULL, '/user/profile/avatar', '127.0.0.1', '', '', '{\"msg\":\"文件服务异常，请联系管理员\",\"code\":500}', 0, NULL, '2026-06-03 11:51:09', 1411);
INSERT INTO `sys_oper_log` VALUES (156, '用户头像', 2, 'com.ruoyi.system.controller.SysProfileController.avatar()', 'POST', 1, 'admin', NULL, '/user/profile/avatar', '127.0.0.1', '', '', '{\"msg\":\"文件服务异常，请联系管理员\",\"code\":500}', 0, NULL, '2026-06-03 11:51:09', 112);
INSERT INTO `sys_oper_log` VALUES (157, '角色管理', 1, 'com.ruoyi.system.controller.SysRoleController.add()', 'POST', 1, 'admin', NULL, '/role', '127.0.0.1', '', '{\"admin\":false,\"createBy\":\"admin\",\"deptCheckStrictly\":true,\"deptIds\":[],\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[2105,2081,2082,2083,2084,2085,2086,2000,2001,2002,2003,2004,2005,2006,2027,2028,2029,2030,2031,2032,2106,2033,2034,2035,2036,2037,2038,2069,2070,2071,2072,2073,2074,2099,2100,2101,2102,2103,2104,2107,2051,2052,2053,2054,2055,2056,2063,2064,2065,2066,2067,2068,2093,2094,2095,2096,2097,2098,2007,2008,2009,2010,2011,2012,2013,2039,2040,2041,2042,2043,2044,2045,2046,2047,2048,2049,2050,2014,2015,2016,2017,2018,2019,2020,2021,2022,2023,2024,2025,2026,2108,2057,2058,2059,2060,2061,2062,2075,2076,2077,2078,2079,2080,2087,2088,2089,2090,2091,2092],\"params\":{},\"roleId\":100,\"roleKey\":\"doctor\",\"roleName\":\"医护工作者\",\"roleSort\":3,\"status\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-03 16:08:29', 95);
INSERT INTO `sys_oper_log` VALUES (158, '角色管理', 1, 'com.ruoyi.system.controller.SysRoleController.add()', 'POST', 1, 'admin', NULL, '/role', '127.0.0.1', '', '{\"admin\":false,\"createBy\":\"admin\",\"deptCheckStrictly\":true,\"deptIds\":[],\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[2106,2033,2034,2035,2036,2037,2038,2069,2070,2071,2072,2073,2074,2099,2100,2101,2102,2103,2104],\"params\":{},\"roleId\":101,\"roleKey\":\"patient\",\"roleName\":\"患者\",\"roleSort\":4,\"status\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-03 16:09:18', 16);
INSERT INTO `sys_oper_log` VALUES (159, '角色管理', 2, 'com.ruoyi.system.controller.SysRoleController.dataScope()', 'PUT', 1, 'admin', NULL, '/role/dataScope', '127.0.0.1', '', '{\"admin\":false,\"createTime\":\"2026-06-03 16:08:29\",\"dataScope\":\"3\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"deptIds\":[],\"flag\":false,\"menuCheckStrictly\":true,\"params\":{},\"roleId\":100,\"roleKey\":\"doctor\",\"roleName\":\"医护工作者\",\"roleSort\":3,\"status\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-04 10:33:09', 17);
INSERT INTO `sys_oper_log` VALUES (160, '角色管理', 2, 'com.ruoyi.system.controller.SysRoleController.dataScope()', 'PUT', 1, 'admin', NULL, '/role/dataScope', '127.0.0.1', '', '{\"admin\":false,\"createTime\":\"2026-06-03 16:09:18\",\"dataScope\":\"5\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"deptIds\":[],\"flag\":false,\"menuCheckStrictly\":true,\"params\":{},\"roleId\":101,\"roleKey\":\"patient\",\"roleName\":\"患者\",\"roleSort\":4,\"status\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-04 10:33:14', 11);
INSERT INTO `sys_oper_log` VALUES (161, '角色管理', 2, 'com.ruoyi.system.controller.SysRoleController.changeStatus()', 'PUT', 1, 'admin', NULL, '/role/changeStatus', '127.0.0.1', '', '{\"admin\":false,\"deptCheckStrictly\":false,\"flag\":false,\"menuCheckStrictly\":false,\"params\":{},\"roleId\":2,\"status\":\"1\",\"updateBy\":\"admin\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-06-04 10:34:47', 8);

-- ----------------------------
-- Table structure for sys_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post`  (
  `post_id` bigint NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  `post_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '岗位编码',
  `post_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '岗位名称',
  `post_sort` int NOT NULL COMMENT '显示顺序',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`post_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '岗位信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_post
-- ----------------------------
INSERT INTO `sys_post` VALUES (1, 'ceo', '董事长', 1, '0', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_post` VALUES (2, 'se', '项目经理', 2, '0', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_post` VALUES (3, 'hr', '人力资源', 3, '0', 'admin', '2026-05-27 11:48:15', '', NULL, '');
INSERT INTO `sys_post` VALUES (4, 'user', '普通员工', 4, '0', 'admin', '2026-05-27 11:48:15', '', NULL, '');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `role_id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色权限字符串',
  `role_sort` int NOT NULL COMMENT '显示顺序',
  `data_scope` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '1' COMMENT '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
  `menu_check_strictly` tinyint(1) NULL DEFAULT 1 COMMENT '菜单树选择项是否关联显示',
  `dept_check_strictly` tinyint(1) NULL DEFAULT 1 COMMENT '部门树选择项是否关联显示',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 102 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'admin', 1, '1', 1, 1, '0', '0', 'admin', '2026-05-27 11:48:15', '', NULL, '超级管理员');
INSERT INTO `sys_role` VALUES (2, '普通角色', 'common', 2, '2', 0, 0, '1', '0', 'admin', '2026-05-27 11:48:15', 'admin', '2026-06-04 10:34:47', '普通角色');
INSERT INTO `sys_role` VALUES (3, '医护工作者', 'doctor', 3, '3', 1, 1, '0', '0', 'admin', '2026-06-03 16:08:29', '', '2026-06-04 10:33:09', NULL);
INSERT INTO `sys_role` VALUES (4, '患者', 'patient', 4, '5', 1, 1, '0', '0', 'admin', '2026-06-03 16:09:18', '', '2026-06-04 10:33:14', NULL);

-- ----------------------------
-- Table structure for sys_role_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept`  (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `dept_id` bigint NOT NULL COMMENT '部门ID',
  PRIMARY KEY (`role_id`, `dept_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色和部门关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_dept
-- ----------------------------
INSERT INTO `sys_role_dept` VALUES (2, 100);
INSERT INTO `sys_role_dept` VALUES (2, 101);
INSERT INTO `sys_role_dept` VALUES (2, 105);

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`, `menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色和菜单关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (2, 1);
INSERT INTO `sys_role_menu` VALUES (2, 2);
INSERT INTO `sys_role_menu` VALUES (2, 3);
INSERT INTO `sys_role_menu` VALUES (2, 4);
INSERT INTO `sys_role_menu` VALUES (2, 100);
INSERT INTO `sys_role_menu` VALUES (2, 101);
INSERT INTO `sys_role_menu` VALUES (2, 102);
INSERT INTO `sys_role_menu` VALUES (2, 103);
INSERT INTO `sys_role_menu` VALUES (2, 104);
INSERT INTO `sys_role_menu` VALUES (2, 105);
INSERT INTO `sys_role_menu` VALUES (2, 106);
INSERT INTO `sys_role_menu` VALUES (2, 107);
INSERT INTO `sys_role_menu` VALUES (2, 108);
INSERT INTO `sys_role_menu` VALUES (2, 109);
INSERT INTO `sys_role_menu` VALUES (2, 110);
INSERT INTO `sys_role_menu` VALUES (2, 111);
INSERT INTO `sys_role_menu` VALUES (2, 112);
INSERT INTO `sys_role_menu` VALUES (2, 113);
INSERT INTO `sys_role_menu` VALUES (2, 114);
INSERT INTO `sys_role_menu` VALUES (2, 115);
INSERT INTO `sys_role_menu` VALUES (2, 116);
INSERT INTO `sys_role_menu` VALUES (2, 500);
INSERT INTO `sys_role_menu` VALUES (2, 501);
INSERT INTO `sys_role_menu` VALUES (2, 1000);
INSERT INTO `sys_role_menu` VALUES (2, 1001);
INSERT INTO `sys_role_menu` VALUES (2, 1002);
INSERT INTO `sys_role_menu` VALUES (2, 1003);
INSERT INTO `sys_role_menu` VALUES (2, 1004);
INSERT INTO `sys_role_menu` VALUES (2, 1005);
INSERT INTO `sys_role_menu` VALUES (2, 1006);
INSERT INTO `sys_role_menu` VALUES (2, 1007);
INSERT INTO `sys_role_menu` VALUES (2, 1008);
INSERT INTO `sys_role_menu` VALUES (2, 1009);
INSERT INTO `sys_role_menu` VALUES (2, 1010);
INSERT INTO `sys_role_menu` VALUES (2, 1011);
INSERT INTO `sys_role_menu` VALUES (2, 1012);
INSERT INTO `sys_role_menu` VALUES (2, 1013);
INSERT INTO `sys_role_menu` VALUES (2, 1014);
INSERT INTO `sys_role_menu` VALUES (2, 1015);
INSERT INTO `sys_role_menu` VALUES (2, 1016);
INSERT INTO `sys_role_menu` VALUES (2, 1017);
INSERT INTO `sys_role_menu` VALUES (2, 1018);
INSERT INTO `sys_role_menu` VALUES (2, 1019);
INSERT INTO `sys_role_menu` VALUES (2, 1020);
INSERT INTO `sys_role_menu` VALUES (2, 1021);
INSERT INTO `sys_role_menu` VALUES (2, 1022);
INSERT INTO `sys_role_menu` VALUES (2, 1023);
INSERT INTO `sys_role_menu` VALUES (2, 1024);
INSERT INTO `sys_role_menu` VALUES (2, 1025);
INSERT INTO `sys_role_menu` VALUES (2, 1026);
INSERT INTO `sys_role_menu` VALUES (2, 1027);
INSERT INTO `sys_role_menu` VALUES (2, 1028);
INSERT INTO `sys_role_menu` VALUES (2, 1029);
INSERT INTO `sys_role_menu` VALUES (2, 1030);
INSERT INTO `sys_role_menu` VALUES (2, 1031);
INSERT INTO `sys_role_menu` VALUES (2, 1032);
INSERT INTO `sys_role_menu` VALUES (2, 1033);
INSERT INTO `sys_role_menu` VALUES (2, 1034);
INSERT INTO `sys_role_menu` VALUES (2, 1035);
INSERT INTO `sys_role_menu` VALUES (2, 1036);
INSERT INTO `sys_role_menu` VALUES (2, 1037);
INSERT INTO `sys_role_menu` VALUES (2, 1038);
INSERT INTO `sys_role_menu` VALUES (2, 1039);
INSERT INTO `sys_role_menu` VALUES (2, 1040);
INSERT INTO `sys_role_menu` VALUES (2, 1041);
INSERT INTO `sys_role_menu` VALUES (2, 1042);
INSERT INTO `sys_role_menu` VALUES (2, 1043);
INSERT INTO `sys_role_menu` VALUES (2, 1044);
INSERT INTO `sys_role_menu` VALUES (2, 1045);
INSERT INTO `sys_role_menu` VALUES (2, 1046);
INSERT INTO `sys_role_menu` VALUES (2, 1047);
INSERT INTO `sys_role_menu` VALUES (2, 1048);
INSERT INTO `sys_role_menu` VALUES (2, 1049);
INSERT INTO `sys_role_menu` VALUES (2, 1050);
INSERT INTO `sys_role_menu` VALUES (2, 1051);
INSERT INTO `sys_role_menu` VALUES (2, 1052);
INSERT INTO `sys_role_menu` VALUES (2, 1053);
INSERT INTO `sys_role_menu` VALUES (2, 1054);
INSERT INTO `sys_role_menu` VALUES (2, 1055);
INSERT INTO `sys_role_menu` VALUES (2, 1056);
INSERT INTO `sys_role_menu` VALUES (2, 1057);
INSERT INTO `sys_role_menu` VALUES (2, 1058);
INSERT INTO `sys_role_menu` VALUES (2, 1059);
INSERT INTO `sys_role_menu` VALUES (2, 1060);
INSERT INTO `sys_role_menu` VALUES (100, 2000);
INSERT INTO `sys_role_menu` VALUES (100, 2001);
INSERT INTO `sys_role_menu` VALUES (100, 2002);
INSERT INTO `sys_role_menu` VALUES (100, 2003);
INSERT INTO `sys_role_menu` VALUES (100, 2004);
INSERT INTO `sys_role_menu` VALUES (100, 2005);
INSERT INTO `sys_role_menu` VALUES (100, 2006);
INSERT INTO `sys_role_menu` VALUES (100, 2007);
INSERT INTO `sys_role_menu` VALUES (100, 2008);
INSERT INTO `sys_role_menu` VALUES (100, 2009);
INSERT INTO `sys_role_menu` VALUES (100, 2010);
INSERT INTO `sys_role_menu` VALUES (100, 2011);
INSERT INTO `sys_role_menu` VALUES (100, 2012);
INSERT INTO `sys_role_menu` VALUES (100, 2013);
INSERT INTO `sys_role_menu` VALUES (100, 2014);
INSERT INTO `sys_role_menu` VALUES (100, 2015);
INSERT INTO `sys_role_menu` VALUES (100, 2016);
INSERT INTO `sys_role_menu` VALUES (100, 2017);
INSERT INTO `sys_role_menu` VALUES (100, 2018);
INSERT INTO `sys_role_menu` VALUES (100, 2019);
INSERT INTO `sys_role_menu` VALUES (100, 2020);
INSERT INTO `sys_role_menu` VALUES (100, 2021);
INSERT INTO `sys_role_menu` VALUES (100, 2022);
INSERT INTO `sys_role_menu` VALUES (100, 2023);
INSERT INTO `sys_role_menu` VALUES (100, 2024);
INSERT INTO `sys_role_menu` VALUES (100, 2025);
INSERT INTO `sys_role_menu` VALUES (100, 2026);
INSERT INTO `sys_role_menu` VALUES (100, 2027);
INSERT INTO `sys_role_menu` VALUES (100, 2028);
INSERT INTO `sys_role_menu` VALUES (100, 2029);
INSERT INTO `sys_role_menu` VALUES (100, 2030);
INSERT INTO `sys_role_menu` VALUES (100, 2031);
INSERT INTO `sys_role_menu` VALUES (100, 2032);
INSERT INTO `sys_role_menu` VALUES (100, 2033);
INSERT INTO `sys_role_menu` VALUES (100, 2034);
INSERT INTO `sys_role_menu` VALUES (100, 2035);
INSERT INTO `sys_role_menu` VALUES (100, 2036);
INSERT INTO `sys_role_menu` VALUES (100, 2037);
INSERT INTO `sys_role_menu` VALUES (100, 2038);
INSERT INTO `sys_role_menu` VALUES (100, 2039);
INSERT INTO `sys_role_menu` VALUES (100, 2040);
INSERT INTO `sys_role_menu` VALUES (100, 2041);
INSERT INTO `sys_role_menu` VALUES (100, 2042);
INSERT INTO `sys_role_menu` VALUES (100, 2043);
INSERT INTO `sys_role_menu` VALUES (100, 2044);
INSERT INTO `sys_role_menu` VALUES (100, 2045);
INSERT INTO `sys_role_menu` VALUES (100, 2046);
INSERT INTO `sys_role_menu` VALUES (100, 2047);
INSERT INTO `sys_role_menu` VALUES (100, 2048);
INSERT INTO `sys_role_menu` VALUES (100, 2049);
INSERT INTO `sys_role_menu` VALUES (100, 2050);
INSERT INTO `sys_role_menu` VALUES (100, 2051);
INSERT INTO `sys_role_menu` VALUES (100, 2052);
INSERT INTO `sys_role_menu` VALUES (100, 2053);
INSERT INTO `sys_role_menu` VALUES (100, 2054);
INSERT INTO `sys_role_menu` VALUES (100, 2055);
INSERT INTO `sys_role_menu` VALUES (100, 2056);
INSERT INTO `sys_role_menu` VALUES (100, 2057);
INSERT INTO `sys_role_menu` VALUES (100, 2058);
INSERT INTO `sys_role_menu` VALUES (100, 2059);
INSERT INTO `sys_role_menu` VALUES (100, 2060);
INSERT INTO `sys_role_menu` VALUES (100, 2061);
INSERT INTO `sys_role_menu` VALUES (100, 2062);
INSERT INTO `sys_role_menu` VALUES (100, 2063);
INSERT INTO `sys_role_menu` VALUES (100, 2064);
INSERT INTO `sys_role_menu` VALUES (100, 2065);
INSERT INTO `sys_role_menu` VALUES (100, 2066);
INSERT INTO `sys_role_menu` VALUES (100, 2067);
INSERT INTO `sys_role_menu` VALUES (100, 2068);
INSERT INTO `sys_role_menu` VALUES (100, 2069);
INSERT INTO `sys_role_menu` VALUES (100, 2070);
INSERT INTO `sys_role_menu` VALUES (100, 2071);
INSERT INTO `sys_role_menu` VALUES (100, 2072);
INSERT INTO `sys_role_menu` VALUES (100, 2073);
INSERT INTO `sys_role_menu` VALUES (100, 2074);
INSERT INTO `sys_role_menu` VALUES (100, 2075);
INSERT INTO `sys_role_menu` VALUES (100, 2076);
INSERT INTO `sys_role_menu` VALUES (100, 2077);
INSERT INTO `sys_role_menu` VALUES (100, 2078);
INSERT INTO `sys_role_menu` VALUES (100, 2079);
INSERT INTO `sys_role_menu` VALUES (100, 2080);
INSERT INTO `sys_role_menu` VALUES (100, 2081);
INSERT INTO `sys_role_menu` VALUES (100, 2082);
INSERT INTO `sys_role_menu` VALUES (100, 2083);
INSERT INTO `sys_role_menu` VALUES (100, 2084);
INSERT INTO `sys_role_menu` VALUES (100, 2085);
INSERT INTO `sys_role_menu` VALUES (100, 2086);
INSERT INTO `sys_role_menu` VALUES (100, 2087);
INSERT INTO `sys_role_menu` VALUES (100, 2088);
INSERT INTO `sys_role_menu` VALUES (100, 2089);
INSERT INTO `sys_role_menu` VALUES (100, 2090);
INSERT INTO `sys_role_menu` VALUES (100, 2091);
INSERT INTO `sys_role_menu` VALUES (100, 2092);
INSERT INTO `sys_role_menu` VALUES (100, 2093);
INSERT INTO `sys_role_menu` VALUES (100, 2094);
INSERT INTO `sys_role_menu` VALUES (100, 2095);
INSERT INTO `sys_role_menu` VALUES (100, 2096);
INSERT INTO `sys_role_menu` VALUES (100, 2097);
INSERT INTO `sys_role_menu` VALUES (100, 2098);
INSERT INTO `sys_role_menu` VALUES (100, 2099);
INSERT INTO `sys_role_menu` VALUES (100, 2100);
INSERT INTO `sys_role_menu` VALUES (100, 2101);
INSERT INTO `sys_role_menu` VALUES (100, 2102);
INSERT INTO `sys_role_menu` VALUES (100, 2103);
INSERT INTO `sys_role_menu` VALUES (100, 2104);
INSERT INTO `sys_role_menu` VALUES (100, 2105);
INSERT INTO `sys_role_menu` VALUES (100, 2106);
INSERT INTO `sys_role_menu` VALUES (100, 2107);
INSERT INTO `sys_role_menu` VALUES (100, 2108);
INSERT INTO `sys_role_menu` VALUES (101, 2033);
INSERT INTO `sys_role_menu` VALUES (101, 2034);
INSERT INTO `sys_role_menu` VALUES (101, 2035);
INSERT INTO `sys_role_menu` VALUES (101, 2036);
INSERT INTO `sys_role_menu` VALUES (101, 2037);
INSERT INTO `sys_role_menu` VALUES (101, 2038);
INSERT INTO `sys_role_menu` VALUES (101, 2069);
INSERT INTO `sys_role_menu` VALUES (101, 2070);
INSERT INTO `sys_role_menu` VALUES (101, 2071);
INSERT INTO `sys_role_menu` VALUES (101, 2072);
INSERT INTO `sys_role_menu` VALUES (101, 2073);
INSERT INTO `sys_role_menu` VALUES (101, 2074);
INSERT INTO `sys_role_menu` VALUES (101, 2099);
INSERT INTO `sys_role_menu` VALUES (101, 2100);
INSERT INTO `sys_role_menu` VALUES (101, 2101);
INSERT INTO `sys_role_menu` VALUES (101, 2102);
INSERT INTO `sys_role_menu` VALUES (101, 2103);
INSERT INTO `sys_role_menu` VALUES (101, 2104);
INSERT INTO `sys_role_menu` VALUES (101, 2106);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '部门ID',
  `user_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户账号',
  `nick_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户昵称',
  `user_type` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '00' COMMENT '用户类型（00系统用户）',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '用户邮箱',
  `phonenumber` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '手机号码',
  `sex` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
  `avatar` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '头像地址',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '密码',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '账号状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `login_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `pwd_update_date` datetime NULL DEFAULT NULL COMMENT '密码最后更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 103, 'admin', '若依', '00', 'ry@163.com', '15888888888', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', '2026-06-04 11:48:23', '2026-05-27 11:48:15', 'admin', '2026-05-27 11:48:15', '', NULL, '管理员');
INSERT INTO `sys_user` VALUES (2, 105, 'ry', '若依', '00', 'ry@qq.com', '15666666666', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', '2026-05-27 11:48:15', '2026-05-27 11:48:15', 'admin', '2026-05-27 11:48:15', '', NULL, '测试员');

-- ----------------------------
-- Table structure for sys_user_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_post`;
CREATE TABLE `sys_user_post`  (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `post_id` bigint NOT NULL COMMENT '岗位ID',
  PRIMARY KEY (`user_id`, `post_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户与岗位关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_post
-- ----------------------------
INSERT INTO `sys_user_post` VALUES (1, 1);
INSERT INTO `sys_user_post` VALUES (2, 2);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户和角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1);
INSERT INTO `sys_user_role` VALUES (2, 2);

SET FOREIGN_KEY_CHECKS = 1;
