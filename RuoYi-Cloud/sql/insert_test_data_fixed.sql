-- 患者端测试数据插入脚本（修复乱码版）
-- 执行前请确保数据库已正确创建并连接
-- 使用UTF-8编码执行脚本

-- ======================== 设置字符集 ========================
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET character_set_connection=utf8mb4;
SET character_set_database=utf8mb4;
SET character_set_results=utf8mb4;
SET character_set_server=utf8mb4;

-- ======================== 清理已有测试数据 ========================
SET FOREIGN_KEY_CHECKS = 0;

-- 清理处方明细数据
DELETE FROM `prescription_item` WHERE `prescription_id` IN (1);

-- 清理处方数据
DELETE FROM `prescription` WHERE `prescription_id` IN (1);

-- 清理医技申请数据（如果有）
-- DELETE FROM `exam_apply` WHERE `patient_id` IN (1,2,3);

-- 清理病历疾病关联数据
DELETE FROM `medical_record_disease` WHERE `record_id` IN (1);

-- 清理电子病历数据
DELETE FROM `medical_record` WHERE `encounter_id` IN (1,2);

-- 清理接诊数据
DELETE FROM `encounter` WHERE `encounter_id` IN (1,2);

-- 清理挂号数据
DELETE FROM `register` WHERE `register_id` IN (1,2,3,4);

-- 清理排班数据
DELETE FROM `schedule` WHERE `schedule_id` IN (1,2,3,4,5,6,7,8,9,10);

-- 清理患者数据
DELETE FROM `patient` WHERE `patient_id` IN (1,2,3);

-- 清理医生数据
DELETE FROM `doctor` WHERE `doctor_id` IN (1,2,3,4,5);

-- 清理挂号级别数据（保留原有数据）
-- 我们不删除，只插入新的（如果不存在）

-- 清理药品数据
DELETE FROM `drug` WHERE `id` IN (1,2,3,4,5);

-- 清理医技项目数据
DELETE FROM `medical_technology` WHERE `id` IN (1,2,3,4,5);

-- 清理疾病数据
DELETE FROM `disease` WHERE `disease_id` IN (1,2,3,4,5);

-- 清理科室数据（删除200以上的测试数据，删除若以公司部门）
DELETE FROM `sys_dept` WHERE `dept_id` BETWEEN 100 AND 299 OR `dept_name` LIKE '%若以%' OR `dept_name` LIKE '%公司%';

SET FOREIGN_KEY_CHECKS = 1;

-- ======================== 1. 科室数据（医院科室） ========================
INSERT IGNORE INTO `sys_dept` (`dept_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`) VALUES
(200, 0, '0', '医院', 1, NULL, NULL, NULL, '0', '0', 'admin', NOW()),
(201, 200, '0,200', '内科', 1, '李医生', '13800138001', 'internal@hospital.com', '0', '0', 'admin', NOW()),
(202, 200, '0,200', '外科', 2, '王医生', '13800138002', 'surgery@hospital.com', '0', '0', 'admin', NOW()),
(203, 200, '0,200', '儿科', 3, '张医生', '13800138003', 'pediatrics@hospital.com', '0', '0', 'admin', NOW()),
(204, 200, '0,200', '妇产科', 4, '刘医生', '13800138004', 'obstetrics@hospital.com', '0', '0', 'admin', NOW()),
(205, 200, '0,200', '骨科', 5, '赵医生', '13800138005', 'orthopedics@hospital.com', '0', '0', 'admin', NOW()),
(206, 200, '0,200', '眼科', 6, '孙医生', '13800138006', 'ophthalmology@hospital.com', '0', '0', 'admin', NOW()),
(207, 200, '0,200', '耳鼻喉科', 7, '周医生', '13800138007', 'ent@hospital.com', '0', '0', 'admin', NOW()),
(208, 200, '0,200', '口腔科', 8, '吴医生', '13800138008', 'dental@hospital.com', '0', '0', 'admin', NOW()),
(209, 200, '0,200', '皮肤科', 9, '郑医生', '13800138009', 'dermatology@hospital.com', '0', '0', 'admin', NOW());

-- ======================== 2. 挂号级别数据 ========================
INSERT IGNORE INTO `regist_level` (`level_id`, `level_name`, `fee`, `sort`, `status`, `create_time`, `update_time`) VALUES
(1, '普通号', 10.00, 1, '0', NOW(), NOW()),
(2, '专家号', 30.00, 2, '0', NOW(), NOW()),
(3, '特需号', 100.00, 3, '0', NOW(), NOW());

-- ======================== 3. 医生数据 ========================
INSERT IGNORE INTO `doctor` (`doctor_id`, `user_id`, `dept_id`, `doctor_no`, `doctor_name`, `gender`, `phone`, `title`, `specialty`, `outpatient_fee`, `introduction`, `status`, `create_time`, `update_time`) VALUES
(1, 1, 201, 'DOC001', '李明', '0', '13900000001', '主任医师', '擅长心血管疾病、高血压、冠心病的诊断与治疗，从事临床工作20余年。', 30.00, '毕业于北京医科大学，硕士学位，主任医师，心血管内科副主任。', '0', NOW(), NOW()),
(2, 1, 201, 'DOC002', '王芳', '1', '13900000002', '副主任医师', '擅长消化系统疾病，包括胃炎、胃溃疡、肠炎等疾病的诊疗。', 20.00, '毕业于上海医科大学，博士学位，副主任医师。', '0', NOW(), NOW()),
(3, 1, 202, 'DOC003', '张伟', '0', '13900000003', '主任医师', '擅长普外科疾病，包括疝气、阑尾炎、胆囊结石等微创手术。', 30.00, '从事外科临床工作25年，经验丰富。', '0', NOW(), NOW()),
(4, 1, 203, 'DOC004', '刘静', '1', '13900000004', '副主任医师', '擅长小儿常见病、多发病，如感冒、发烧、咳嗽、腹泻等。', 20.00, '儿科专家，深受患儿家长信赖。', '0', NOW(), NOW()),
(5, 1, 204, 'DOC005', '陈红', '1', '13900000005', '主任医师', '擅长妇科疾病、不孕不育、产前检查等。', 30.00, '妇产科主任，从事临床工作30年。', '0', NOW(), NOW());

-- ======================== 4. 患者数据 ========================
INSERT IGNORE INTO `patient` (`patient_id`, `patient_no`, `name`, `gender`, `birthday`, `age`, `phone`, `id_card`, `blood_type`, `marital_status`, `address`, `emergency_contact`, `emergency_phone`, `allergy_history`, `past_history`, `status`, `create_time`, `update_time`) VALUES
(1, 'PAT001', '张三', '0', '1990-05-15', 35, '13600000001', '110101199005151234', 'A型', '已婚', '北京市朝阳区建国路88号', '李四', '13700000001', '青霉素过敏', '无', '0', NOW(), NOW()),
(2, 'PAT002', '李四', '1', '1995-08-20', 30, '13600000002', '110102199508202345', 'B型', '未婚', '北京市海淀区中关村大街100号', '王五', '13700000002', '无', '无', '0', NOW(), NOW()),
(3, 'PAT003', '王五', '0', '1985-12-01', 40, '13600000003', '110103198512013456', 'O型', '已婚', '北京市西城区金融街50号', '赵六', '13700000003', '海鲜过敏', '高血压病史5年', '0', NOW(), NOW());

-- ======================== 5. 医生排班数据 ========================
INSERT IGNORE INTO `schedule` (`schedule_id`, `doctor_id`, `schedule_date`, `time_slot`, `max_number`, `reserved_number`, `status`, `create_time`, `update_time`) VALUES
-- 李明医生的排班
(1, 1, CURDATE(), 'morning', 20, 5, '0', NOW(), NOW()),
(2, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'afternoon', 15, 3, '0', NOW(), NOW()),
(3, 1, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'morning', 20, 8, '0', NOW(), NOW()),
-- 王芳医生的排班
(4, 2, CURDATE(), 'afternoon', 15, 2, '0', NOW(), NOW()),
(5, 2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'morning', 20, 4, '0', NOW(), NOW()),
-- 张伟医生的排班
(6, 3, CURDATE(), 'morning', 15, 6, '0', NOW(), NOW()),
(7, 3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'afternoon', 15, 1, '0', NOW(), NOW()),
-- 刘静医生的排班
(8, 4, CURDATE(), 'morning', 20, 10, '0', NOW(), NOW()),
-- 陈红医生的排班
(9, 5, CURDATE(), 'afternoon', 15, 3, '0', NOW(), NOW()),
(10, 5, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'morning', 20, 5, '0', NOW(), NOW());

-- ======================== 6. 挂号数据 ========================
INSERT IGNORE INTO `register` (`register_id`, `register_no`, `patient_id`, `schedule_id`, `doctor_id`, `dept_id`, `level_id`, `register_type`, `register_status`, `pay_status`, `register_fee`, `register_time`, `remark`, `create_time`, `update_time`) VALUES
(1, 'REG20250604001', 1, 1, 1, 201, 2, 'online', 'registered', 'paid', 30.00, DATE_SUB(NOW(), INTERVAL 2 DAY), '网上预约挂号', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 'REG20250604002', 1, 4, 2, 201, 1, 'online', 'registered', 'unpaid', 10.00, DATE_SUB(NOW(), INTERVAL 1 DAY), '网上预约挂号', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 'REG20250604003', 2, 6, 3, 202, 2, 'online', 'registered', 'paid', 30.00, DATE_SUB(NOW(), INTERVAL 1 DAY), '网上预约挂号', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 'REG20250604004', 3, 8, 4, 203, 1, 'online', 'registered', 'paid', 10.00, NOW(), '网上预约挂号', NOW(), NOW());

-- ======================== 7. 接诊数据 ========================
INSERT IGNORE INTO `encounter` (`encounter_id`, `register_id`, `patient_id`, `doctor_id`, `dept_id`, `encounter_type`, `encounter_status`, `visit_time`, `finish_time`, `create_time`, `update_time`) VALUES
(1, 1, 1, 1, 201, '门诊', 'completed', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 3, 2, 3, 202, '门诊', 'in_progress', DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

-- ======================== 8. 电子病历数据 ========================
INSERT IGNORE INTO `medical_record` (`record_id`, `encounter_id`, `chief_complaint`, `present_illness`, `past_history`, `allergy_history`, `physical_exam`, `diagnosis_opinion`, `treatment_plan`, `doctor_advice`, `create_time`, `update_time`) VALUES
(1, 1, '反复头晕、头痛1周', '患者1周前无明显诱因出现头晕、头痛，呈间歇性，活动后加重，休息后缓解。无恶心呕吐，无视物旋转。', '既往体健，无高血压、糖尿病史。', '青霉素过敏', 'T:36.5℃, P:80次/分, R:20次/分, BP:130/85mmHg。心肺腹未见明显异常。神经系统检查正常。', '1.高血压病1级 2.血管性头痛', '1.硝苯地平控释片30mg qd 2.头痛宁胶囊3粒 tid', '低盐低脂饮食，定期监测血压，不适随诊', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY));

-- ======================== 9. 病历疾病关联数据 ========================
INSERT IGNORE INTO `medical_record_disease` (`id`, `record_id`, `disease_id`, `diagnosis_type`, `create_time`) VALUES
(1, 1, 1, '主要诊断', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 1, 5, '次要诊断', DATE_SUB(NOW(), INTERVAL 2 DAY));

-- ======================== 10. 疾病数据 ========================
INSERT IGNORE INTO `disease` (`disease_id`, `disease_code`, `disease_name`, `icd_code`, `description`, `status`, `create_time`, `update_time`) VALUES
(1, 'DIS001', '高血压病', 'I10', '以血压升高为主要临床表现的综合征，是心脑血管病的重要危险因素。', '0', NOW(), NOW()),
(2, 'DIS002', '糖尿病', 'E11', '以高血糖为特征的代谢性疾病。', '0', NOW(), NOW()),
(3, 'DIS003', '感冒', 'J00', '急性上呼吸道感染的一种，主要由病毒引起。', '0', NOW(), NOW()),
(4, 'DIS004', '胃炎', 'K29', '胃黏膜的炎症，常见症状包括上腹痛、恶心、呕吐等。', '0', NOW(), NOW()),
(5, 'DIS005', '高血压性头痛', 'G44', '高血压引起的头痛症状。', '0', NOW(), NOW());

-- ======================== 11. 药品数据 ========================
INSERT IGNORE INTO `drug` (`id`, `drug_code`, `drug_name`, `py_code`, `spec`, `unit`, `drug_price`, `manufacturer`, `drug_type`, `sort`, `status`, `remark`, `create_time`, `update_time`) VALUES
(1, 'MED001', '硝苯地平控释片', 'XBPDKSP', '30mg*7片', '盒', 45.00, '拜耳医药', '西药', 1, '0', '降压药', NOW(), NOW()),
(2, 'MED002', '头痛宁胶囊', 'TTNJN', '0.4g*36粒', '盒', 38.00, '陕西步长', '中成药', 2, '0', '治疗头痛', NOW(), NOW()),
(3, 'MED003', '复方氨酚烷胺片', 'FFAFWAP', '12片/盒', '盒', 15.00, '感康药业', '西药', 3, '0', '感冒药', NOW(), NOW()),
(4, 'MED004', '奥美拉唑肠溶胶囊', 'AMLAZRJN', '20mg*14粒', '盒', 28.00, '阿斯利康', '西药', 4, '0', '胃药', NOW(), NOW()),
(5, 'MED005', '阿莫西林胶囊', 'AMXJLN', '0.5g*24粒', '盒', 25.00, '华北制药', '西药', 5, '0', '抗生素', NOW(), NOW());

-- ======================== 12. 医技项目数据 ========================
INSERT IGNORE INTO `medical_technology` (`id`, `tech_code`, `tech_name`, `tech_type`, `tech_format`, `tech_price`, `remark`, `status`, `create_time`, `update_time`) VALUES
(1, 'TECH001', '血常规', 'LAB', '次', 25.00, '血液常规检查', '0', NOW(), NOW()),
(2, 'TECH002', '尿常规', 'LAB', '次', 20.00, '尿液常规检查', '0', NOW(), NOW()),
(3, 'TECH003', '肝功能', 'LAB', '次', 80.00, '肝功能检查', '0', NOW(), NOW()),
(4, 'TECH004', '胸部X光', 'CHECK', '次', 120.00, '胸部X光检查', '0', NOW(), NOW()),
(5, 'TECH005', '心电图', 'CHECK', '次', 30.00, '心电图检查', '0', NOW(), NOW());

-- ======================== 13. 处方数据 ========================
INSERT IGNORE INTO `prescription` (`prescription_id`, `register_id`, `encounter_id`, `patient_id`, `doctor_id`, `dept_id`, `prescription_no`, `total_amount`, `prescription_status`, `pay_status`, `drug_tip`, `create_time`, `update_time`) VALUES
(1, 1, 1, 1, 1, 201, 'PRES20250604001', 121.00, '已发药', 'paid', '饭后服用，如有不适请及时就医', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY));

-- ======================== 14. 处方明细数据 ========================
INSERT IGNORE INTO `prescription_item` (`item_id`, `prescription_id`, `drug_id`, `drug_name`, `drug_price`, `quantity`, `drug_usage`, `dosage`, `frequency`, `use_days`, `item_tip`, `create_time`) VALUES
(1, 1, 1, '硝苯地平控释片', 45.00, 1, '口服', '30mg', 'qd', 7, '早晨起床后服用', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 1, 2, '头痛宁胶囊', 38.00, 2, '口服', '1.2g', 'tid', 7, '饭后半小时服用', DATE_SUB(NOW(), INTERVAL 2 DAY));

-- ======================== 数据插入完成 ========================
SELECT '测试数据插入完成！' AS message;
SELECT COUNT(*) AS '医生数量' FROM doctor;
SELECT COUNT(*) AS '患者数量' FROM patient;
SELECT COUNT(*) AS '挂号数量' FROM register;
SELECT COUNT(*) AS '排班数量' FROM schedule;
