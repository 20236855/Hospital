
-- ======================== 更新电子病历测试数据（建立关联）=======================

-- 1. 先插入一些encounter接诊数据（关联患者、医生、科室）
INSERT IGNORE INTO `encounter` (`encounter_id`, `register_id`, `patient_id`, `doctor_id`, `dept_id`, `encounter_type`, `encounter_status`, `visit_time`, `finish_time`, `create_time`, `update_time`) VALUES
(1, 1, 1, 1, 201, '门诊', 'finished', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), NOW(), NOW()),
(2, 2, 1, 2, 201, '门诊', 'finished', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), NOW(), NOW()),
(3, 3, 2, 3, 202, '门诊', 'finished', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), NOW(), NOW()),
(4, 4, 3, 4, 203, '门诊', 'finished', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(), NOW()),
(5, 5, 4, 5, 204, '门诊', 'finished', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), NOW(), NOW());

-- 2. 更新medical_record表，建立encounter关联
UPDATE `medical_record` SET `encounter_id` = 1 WHERE `record_id` = 1;
UPDATE `medical_record` SET `encounter_id` = 2 WHERE `record_id` = 2;
UPDATE `medical_record` SET `encounter_id` = 3 WHERE `record_id` = 3;
UPDATE `medical_record` SET `encounter_id` = 4 WHERE `record_id` = 4;
UPDATE `medical_record` SET `encounter_id` = 5 WHERE `record_id` = 5;

-- 3. 如果medical_record为空，插入一些测试数据
INSERT IGNORE INTO `medical_record` (`record_id`, `encounter_id`, `chief_complaint`, `present_illness`, `past_history`, `allergy_history`, `physical_exam`, `diagnosis_opinion`, `treatment_plan`, `doctor_advice`, `create_time`, `update_time`) VALUES
(6, 1, '咳嗽、咳痰3天', '患者3天前受凉后出现咳嗽、咳痰，为白色黏痰，量不多，伴有轻微咽痛，无发热、胸闷、气促等不适。自服感冒药（具体不详），症状无明显好转。', '既往体健，否认高血压、糖尿病等慢性病史，否认肝炎、结核等传染病史，否认手术、外伤史，否认输血史。', '否认药物过敏史，否认食物过敏史。', '体温36.5℃，脉搏80次/分，呼吸20次/分，血压120/80mmHg。咽部轻度充血，扁桃体无肿大。双肺呼吸音清，未闻及干湿性啰音。心率80次/分，律齐，未闻及病理性杂音。腹平软，无压痛、反跳痛。', '1. 急性上呼吸道感染；2. 急性支气管炎', '1. 休息，多饮水；2. 止咳化痰对症治疗；3. 如有发热、咳嗽加重等情况，及时复诊。', '1. 氨溴索口服溶液 10ml tid 口服；2. 复方甘草片 3片 tid 口服；3. 清淡饮食，避免辛辣刺激性食物。', NOW(), NOW()),
(7, 2, '头晕、乏力1周', '患者1周前无明显诱因出现头晕、乏力，活动后加重，休息后可缓解，伴有耳鸣，无恶心、呕吐，无头痛、视物旋转等不适。', '既往体健，否认高血压、糖尿病等慢性病史。', '否认药物过敏史。', '体温36.2℃，脉搏72次/分，呼吸18次/分，血压115/75mmHg。神志清，精神尚可。心肺腹未见明显异常。', '1. 头晕待查；2. 脑供血不足？', '1. 注意休息，避免劳累；2. 改善脑循环治疗；3. 完善头颅CT等检查。', '1. 甲磺酸倍他司汀片 6mg tid 口服；2. 注意监测血压；3. 必要时神经内科就诊。', NOW(), NOW());

-- ======================== 数据更新完成 ========================
SELECT '电子病历关联数据更新完成！' AS message;
SELECT 
    mr.record_id,
    mr.encounter_id,
    e.doctor_id,
    e.dept_id,
    d.doctor_name,
    sd.dept_name
FROM medical_record mr
LEFT JOIN encounter e ON mr.encounter_id = e.encounter_id
LEFT JOIN doctor d ON e.doctor_id = d.doctor_id
LEFT JOIN sys_dept sd ON e.dept_id = sd.dept_id
ORDER BY mr.record_id;

