-- ======================== 医生排班数据 - 2026年第24周（6月9日-6月15日） ========================

SET FOREIGN_KEY_CHECKS = 0;

-- 清空现有排班数据（可选）
-- TRUNCATE TABLE schedule;

-- ======================== 心血管内科（dept_id=201）- 4位医生 ========================
-- 李明 (doctor_id=1) - 主任医师 - 周一、周三、周五全天
INSERT INTO `schedule` (`doctor_id`, `schedule_date`, `time_slot`, `max_number`, `reserved_number`, `status`, `create_time`, `update_time`) VALUES
(1, '2026-06-09', 'morning', 20, 0, '0', NOW(), NOW()),
(1, '2026-06-09', 'afternoon', 15, 0, '0', NOW(), NOW()),
(1, '2026-06-11', 'morning', 20, 0, '0', NOW(), NOW()),
(1, '2026-06-11', 'afternoon', 15, 0, '0', NOW(), NOW()),
(1, '2026-06-13', 'morning', 20, 0, '0', NOW(), NOW()),
(1, '2026-06-13', 'afternoon', 15, 0, '0', NOW(), NOW());

-- 王芳 (doctor_id=2) - 副主任医师 - 周二、周四全天，周六上午
INSERT INTO `schedule` (`doctor_id`, `schedule_date`, `time_slot`, `max_number`, `reserved_number`, `status`, `create_time`, `update_time`) VALUES
(2, '2026-06-10', 'morning', 15, 0, '0', NOW(), NOW()),
(2, '2026-06-10', 'afternoon', 12, 0, '0', NOW(), NOW()),
(2, '2026-06-12', 'morning', 15, 0, '0', NOW(), NOW()),
(2, '2026-06-12', 'afternoon', 12, 0, '0', NOW(), NOW()),
(2, '2026-06-14', 'morning', 15, 0, '0', NOW(), NOW());

-- 赵强 (doctor_id=6) - 普通医生 - 周一、周四、周五上午
INSERT INTO `schedule` (`doctor_id`, `schedule_date`, `time_slot`, `max_number`, `reserved_number`, `status`, `create_time`, `update_time`) VALUES
(6, '2026-06-09', 'morning', 25, 0, '0', NOW(), NOW()),
(6, '2026-06-12', 'morning', 25, 0, '0', NOW(), NOW()),
(6, '2026-06-13', 'morning', 25, 0, '0', NOW(), NOW());

-- 刘敏 (doctor_id=7) - 副主任医师 - 周二、周三下午，周日上午
INSERT INTO `schedule` (`doctor_id`, `schedule_date`, `time_slot`, `max_number`, `reserved_number`, `status`, `create_time`, `update_time`) VALUES
(7, '2026-06-10', 'afternoon', 12, 0, '0', NOW(), NOW()),
(7, '2026-06-11', 'afternoon', 12, 0, '0', NOW(), NOW()),
(7, '2026-06-15', 'morning', 15, 0, '0', NOW(), NOW());

-- ======================== 普外科（dept_id=202）- 3位医生 ========================
-- 张伟 (doctor_id=3) - 主任医师 - 周一、周二、周四全天
INSERT INTO `schedule` (`doctor_id`, `schedule_date`, `time_slot`, `max_number`, `reserved_number`, `status`, `create_time`, `update_time`) VALUES
(3, '2026-06-09', 'morning', 20, 0, '0', NOW(), NOW()),
(3, '2026-06-09', 'afternoon', 15, 0, '0', NOW(), NOW()),
(3, '2026-06-10', 'morning', 20, 0, '0', NOW(), NOW()),
(3, '2026-06-10', 'afternoon', 15, 0, '0', NOW(), NOW()),
(3, '2026-06-12', 'morning', 20, 0, '0', NOW(), NOW()),
(3, '2026-06-12', 'afternoon', 15, 0, '0', NOW(), NOW());

-- 孙丽 (doctor_id=8) - 主任医师 - 周三、周五全天，周六上午
INSERT INTO `schedule` (`doctor_id`, `schedule_date`, `time_slot`, `max_number`, `reserved_number`, `status`, `create_time`, `update_time`) VALUES
(8, '2026-06-11', 'morning', 20, 0, '0', NOW(), NOW()),
(8, '2026-06-11', 'afternoon', 15, 0, '0', NOW(), NOW()),
(8, '2026-06-13', 'morning', 20, 0, '0', NOW(), NOW()),
(8, '2026-06-13', 'afternoon', 15, 0, '0', NOW(), NOW()),
(8, '2026-06-14', 'morning', 20, 0, '0', NOW(), NOW());

-- 周杰 (doctor_id=9) - 普通医生 - 周一、周三、周五下午
INSERT INTO `schedule` (`doctor_id`, `schedule_date`, `time_slot`, `max_number`, `reserved_number`, `status`, `create_time`, `update_time`) VALUES
(9, '2026-06-09', 'afternoon', 25, 0, '0', NOW(), NOW()),
(9, '2026-06-11', 'afternoon', 25, 0, '0', NOW(), NOW()),
(9, '2026-06-13', 'afternoon', 25, 0, '0', NOW(), NOW());

-- ======================== 儿科（dept_id=203）- 3位医生 ========================
-- 刘静 (doctor_id=4) - 副主任医师 - 周一、周三、周六全天
INSERT INTO `schedule` (`doctor_id`, `schedule_date`, `time_slot`, `max_number`, `reserved_number`, `status`, `create_time`, `update_time`) VALUES
(4, '2026-06-09', 'morning', 25, 0, '0', NOW(), NOW()),
(4, '2026-06-09', 'afternoon', 20, 0, '0', NOW(), NOW()),
(4, '2026-06-11', 'morning', 25, 0, '0', NOW(), NOW()),
(4, '2026-06-11', 'afternoon', 20, 0, '0', NOW(), NOW()),
(4, '2026-06-14', 'morning', 25, 0, '0', NOW(), NOW()),
(4, '2026-06-14', 'afternoon', 20, 0, '0', NOW(), NOW());

-- 吴涛 (doctor_id=10) - 主任医师 - 周二、周四全天，周日上午
INSERT INTO `schedule` (`doctor_id`, `schedule_date`, `time_slot`, `max_number`, `reserved_number`, `status`, `create_time`, `update_time`) VALUES
(10, '2026-06-10', 'morning', 30, 0, '0', NOW(), NOW()),
(10, '2026-06-10', 'afternoon', 25, 0, '0', NOW(), NOW()),
(10, '2026-06-12', 'morning', 30, 0, '0', NOW(), NOW()),
(10, '2026-06-12', 'afternoon', 25, 0, '0', NOW(), NOW()),
(10, '2026-06-15', 'morning', 30, 0, '0', NOW(), NOW());

-- 郑雪 (doctor_id=11) - 副主任医师 - 周二、周五、周六下午
INSERT INTO `schedule` (`doctor_id`, `schedule_date`, `time_slot`, `max_number`, `reserved_number`, `status`, `create_time`, `update_time`) VALUES
(11, '2026-06-10', 'afternoon', 20, 0, '0', NOW(), NOW()),
(11, '2026-06-13', 'afternoon', 20, 0, '0', NOW(), NOW()),
(11, '2026-06-14', 'afternoon', 20, 0, '0', NOW(), NOW());

-- ======================== 妇产科（dept_id=204）- 3位医生 ========================
-- 陈红 (doctor_id=5) - 主任医师 - 周一、周三、周五全天
INSERT INTO `schedule` (`doctor_id`, `schedule_date`, `time_slot`, `max_number`, `reserved_number`, `status`, `create_time`, `update_time`) VALUES
(5, '2026-06-09', 'morning', 20, 0, '0', NOW(), NOW()),
(5, '2026-06-09', 'afternoon', 15, 0, '0', NOW(), NOW()),
(5, '2026-06-11', 'morning', 20, 0, '0', NOW(), NOW()),
(5, '2026-06-11', 'afternoon', 15, 0, '0', NOW(), NOW()),
(5, '2026-06-13', 'morning', 20, 0, '0', NOW(), NOW()),
(5, '2026-06-13', 'afternoon', 15, 0, '0', NOW(), NOW());

-- 王芳 (doctor_id=12) - 主任医师 - 周二、周四全天
INSERT INTO `schedule` (`doctor_id`, `schedule_date`, `time_slot`, `max_number`, `reserved_number`, `status`, `create_time`, `update_time`) VALUES
(12, '2026-06-10', 'morning', 20, 0, '0', NOW(), NOW()),
(12, '2026-06-10', 'afternoon', 15, 0, '0', NOW(), NOW()),
(12, '2026-06-12', 'morning', 20, 0, '0', NOW(), NOW()),
(12, '2026-06-12', 'afternoon', 15, 0, '0', NOW(), NOW());

-- 李娜 (doctor_id=13) - 普通医生 - 周六、周日上午
INSERT INTO `schedule` (`doctor_id`, `schedule_date`, `time_slot`, `max_number`, `reserved_number`, `status`, `create_time`, `update_time`) VALUES
(13, '2026-06-14', 'morning', 25, 0, '0', NOW(), NOW()),
(13, '2026-06-15', 'morning', 25, 0, '0', NOW(), NOW());

-- ======================== 骨科（dept_id=205）- 2位医生 ========================
-- 张军 (doctor_id=14) - 主任医师 - 周一、周二、周五全天
INSERT INTO `schedule` (`doctor_id`, `schedule_date`, `time_slot`, `max_number`, `reserved_number`, `status`, `create_time`, `update_time`) VALUES
(14, '2026-06-09', 'morning', 20, 0, '0', NOW(), NOW()),
(14, '2026-06-09', 'afternoon', 15, 0, '0', NOW(), NOW()),
(14, '2026-06-10', 'morning', 20, 0, '0', NOW(), NOW()),
(14, '2026-06-10', 'afternoon', 15, 0, '0', NOW(), NOW()),
(14, '2026-06-13', 'morning', 20, 0, '0', NOW(), NOW()),
(14, '2026-06-13', 'afternoon', 15, 0, '0', NOW(), NOW());

-- 刘强 (doctor_id=15) - 副主任医师 - 周三、周四全天，周六下午
INSERT INTO `schedule` (`doctor_id`, `schedule_date`, `time_slot`, `max_number`, `reserved_number`, `status`, `create_time`, `update_time`) VALUES
(15, '2026-06-11', 'morning', 15, 0, '0', NOW(), NOW()),
(15, '2026-06-11', 'afternoon', 12, 0, '0', NOW(), NOW()),
(15, '2026-06-12', 'morning', 15, 0, '0', NOW(), NOW()),
(15, '2026-06-12', 'afternoon', 12, 0, '0', NOW(), NOW()),
(15, '2026-06-14', 'afternoon', 15, 0, '0', NOW(), NOW());

-- ======================== 眼科（dept_id=206）- 2位医生 ========================
-- 陈莉 (doctor_id=16) - 主任医师 - 周一、周四、周六全天
INSERT INTO `schedule` (`doctor_id`, `schedule_date`, `time_slot`, `max_number`, `reserved_number`, `status`, `create_time`, `update_time`) VALUES
(16, '2026-06-09', 'morning', 20, 0, '0', NOW(), NOW()),
(16, '2026-06-09', 'afternoon', 15, 0, '0', NOW(), NOW()),
(16, '2026-06-12', 'morning', 20, 0, '0', NOW(), NOW()),
(16, '2026-06-12', 'afternoon', 15, 0, '0', NOW(), NOW()),
(16, '2026-06-14', 'morning', 20, 0, '0', NOW(), NOW()),
(16, '2026-06-14', 'afternoon', 15, 0, '0', NOW(), NOW());

-- 林鹏 (doctor_id=17) - 普通医生 - 周二、周三、周五、周日上午
INSERT INTO `schedule` (`doctor_id`, `schedule_date`, `time_slot`, `max_number`, `reserved_number`, `status`, `create_time`, `update_time`) VALUES
(17, '2026-06-10', 'morning', 25, 0, '0', NOW(), NOW()),
(17, '2026-06-11', 'morning', 25, 0, '0', NOW(), NOW()),
(17, '2026-06-13', 'morning', 25, 0, '0', NOW(), NOW()),
(17, '2026-06-15', 'morning', 25, 0, '0', NOW(), NOW());

SET FOREIGN_KEY_CHECKS = 1;

-- 验证数据
SELECT s.schedule_id, d.doctor_name, s.schedule_date, s.time_slot, s.max_number 
FROM schedule s 
JOIN doctor d ON s.doctor_id = d.doctor_id 
ORDER BY s.schedule_date, s.time_slot, d.doctor_name;
