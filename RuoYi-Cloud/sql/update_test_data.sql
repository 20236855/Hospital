
-- ======================== 扩展医生数据（每个科室至少2名医生）=======================
INSERT IGNORE INTO `doctor` (`doctor_id`, `user_id`, `dept_id`, `doctor_no`, `doctor_name`, `gender`, `phone`, `title`, `specialty`, `outpatient_fee`, `introduction`, `status`, `create_time`, `update_time`) VALUES
-- 内科（已有李明、王芳，再加2名）
(6, 1, 201, 'DOC006', '赵强', '0', '13900000006', '主治医师', '擅长心血管疾病，冠心病介入治疗', 25.00, '从事心内科临床工作15年，经验丰富', '0', NOW(), NOW()),
(7, 1, 201, 'DOC007', '刘敏', '1', '13900000007', '副主任医师', '擅长高血压、心律失常的诊治', 30.00, '毕业于北京协和医学院，博士学位', '0', NOW(), NOW()),

-- 外科（已有张伟，再加2名）
(8, 1, 202, 'DOC008', '孙丽', '1', '13900000008', '主任医师', '擅长普外科微创手术，腹腔镜手术', 35.00, '从事外科临床工作20年', '0', NOW(), NOW()),
(9, 1, 202, 'DOC009', '周杰', '0', '13900000009', '主治医师', '擅长胃肠外科疾病诊治', 20.00, '外科青年骨干医生', '0', NOW(), NOW()),

-- 儿科（已有刘静，再加2名）
(10, 1, 203, 'DOC010', '吴涛', '0', '13900000010', '主任医师', '擅长小儿呼吸系统疾病', 30.00, '儿科专家，从事临床25年', '0', NOW(), NOW()),
(11, 1, 203, 'DOC011', '郑雪', '1', '13900000011', '副主任医师', '擅长小儿消化系统疾病', 25.00, '深受患儿家长信赖', '0', NOW(), NOW()),

-- 妇产科（已有陈红，再加2名）
(12, 1, 204, 'DOC012', '王芳', '1', '13900000012', '主任医师', '擅长产科、高危妊娠管理', 35.00, '妇产科主任', '0', NOW(), NOW()),
(13, 1, 204, 'DOC013', '李娜', '1', '13900000013', '主治医师', '擅长妇科常见病、多发病', 20.00, '从事妇产科临床工作10年', '0', NOW(), NOW()),

-- 骨科（新加科室）
(14, 1, 205, 'DOC014', '张军', '0', '13900000014', '主任医师', '擅长骨科创伤、关节置换', 40.00, '骨科主任，从事临床28年', '0', NOW(), NOW()),
(15, 1, 205, 'DOC015', '刘强', '0', '13900000015', '副主任医师', '擅长脊柱外科、颈腰椎病', 30.00, '骨科副主任', '0', NOW(), NOW()),

-- 眼科（新加科室）
(16, 1, 206, 'DOC016', '陈莉', '1', '13900000016', '主任医师', '擅长白内障、青光眼手术', 35.00, '眼科主任', '0', NOW(), NOW()),
(17, 1, 206, 'DOC017', '林鹏', '0', '13900000017', '主治医师', '擅长近视、远视矫正', 20.00, '眼科青年骨干', '0', NOW(), NOW());

-- ======================== 扩展排班数据（每个医生都有排班）=======================
INSERT IGNORE INTO `schedule` (`schedule_id`, `doctor_id`, `schedule_date`, `time_slot`, `max_number`, `reserved_number`, `status`, `create_time`, `update_time`) VALUES
-- 李明医生（1）- 过去、今天、未来
(11, 1, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'morning', 20, 18, '0', NOW(), NOW()),
(12, 1, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'afternoon', 15, 15, '0', NOW(), NOW()),
(13, 1, CURDATE(), 'morning', 20, 3, '0', NOW(), NOW()),
(14, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'afternoon', 15, 2, '0', NOW(), NOW()),
(15, 1, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'morning', 20, 5, '0', NOW(), NOW()),
(16, 1, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'afternoon', 15, 0, '0', NOW(), NOW()),
-- 王芳医生（2）- 过去、今天、未来
(17, 2, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'morning', 20, 20, '0', NOW(), NOW()),
(18, 2, CURDATE(), 'afternoon', 15, 1, '0', NOW(), NOW()),
(19, 2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'morning', 20, 4, '0', NOW(), NOW()),
(20, 2, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'afternoon', 15, 0, '0', NOW(), NOW()),
-- 张伟医生（3）- 过去、今天、未来
(21, 3, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'afternoon', 15, 12, '0', NOW(), NOW()),
(22, 3, CURDATE(), 'morning', 15, 2, '0', NOW(), NOW()),
(23, 3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'afternoon', 15, 0, '0', NOW(), NOW()),
(24, 3, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'morning', 15, 1, '0', NOW(), NOW()),
-- 刘静医生（4）- 过去、今天、未来
(25, 4, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'afternoon', 15, 15, '0', NOW(), NOW()),
(26, 4, CURDATE(), 'morning', 20, 8, '0', NOW(), NOW()),
(27, 4, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'afternoon', 15, 3, '0', NOW(), NOW()),
(28, 4, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'morning', 20, 0, '0', NOW(), NOW()),
-- 陈红医生（5）- 过去、今天、未来
(29, 5, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'morning', 15, 10, '0', NOW(), NOW()),
(30, 5, CURDATE(), 'afternoon', 15, 2, '0', NOW(), NOW()),
(31, 5, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'morning', 20, 6, '0', NOW(), NOW()),
(32, 5, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'afternoon', 15, 0, '0', NOW(), NOW()),
-- 赵强医生（6）- 过去、今天、未来
(33, 6, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'afternoon', 15, 14, '0', NOW(), NOW()),
(34, 6, CURDATE(), 'morning', 20, 4, '0', NOW(), NOW()),
(35, 6, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'afternoon', 15, 1, '0', NOW(), NOW()),
(36, 6, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'morning', 20, 0, '0', NOW(), NOW()),
-- 刘敏医生（7）- 过去、今天、未来
(37, 7, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'morning', 20, 18, '0', NOW(), NOW()),
(38, 7, CURDATE(), 'afternoon', 15, 0, '0', NOW(), NOW()),
(39, 7, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'morning', 20, 3, '0', NOW(), NOW()),
(40, 7, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'afternoon', 15, 1, '0', NOW(), NOW()),
-- 孙丽医生（8）- 过去、今天、未来
(41, 8, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'morning', 15, 15, '0', NOW(), NOW()),
(42, 8, CURDATE(), 'morning', 15, 2, '0', NOW(), NOW()),
(43, 8, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'afternoon', 15, 1, '0', NOW(), NOW()),
(44, 8, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'morning', 15, 0, '0', NOW(), NOW()),
-- 周杰医生（9）- 过去、今天、未来
(45, 9, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'afternoon', 15, 15, '0', NOW(), NOW()),
(46, 9, CURDATE(), 'afternoon', 15, 0, '0', NOW(), NOW()),
(47, 9, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'morning', 15, 2, '0', NOW(), NOW()),
(48, 9, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'afternoon', 15, 0, '0', NOW(), NOW()),
-- 吴涛医生（10）- 过去、今天、未来
(49, 10, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'morning', 20, 20, '0', NOW(), NOW()),
(50, 10, CURDATE(), 'morning', 20, 5, '0', NOW(), NOW()),
(51, 10, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'afternoon', 15, 2, '0', NOW(), NOW()),
(52, 10, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'morning', 20, 0, '0', NOW(), NOW()),
-- 郑雪医生（11）- 过去、今天、未来
(53, 11, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'afternoon', 15, 10, '0', NOW(), NOW()),
(54, 11, CURDATE(), 'afternoon', 15, 1, '0', NOW(), NOW()),
(55, 11, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'morning', 20, 4, '0', NOW(), NOW()),
(56, 11, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'afternoon', 15, 0, '0', NOW(), NOW()),
-- 王芳（妇产科12）- 过去、今天、未来
(57, 12, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'afternoon', 15, 14, '0', NOW(), NOW()),
(58, 12, CURDATE(), 'morning', 15, 3, '0', NOW(), NOW()),
(59, 12, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'afternoon', 15, 1, '0', NOW(), NOW()),
(60, 12, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'morning', 15, 0, '0', NOW(), NOW()),
-- 李娜医生（13）- 过去、今天、未来
(61, 13, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'morning', 15, 15, '0', NOW(), NOW()),
(62, 13, CURDATE(), 'afternoon', 15, 0, '0', NOW(), NOW()),
(63, 13, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'morning', 15, 2, '0', NOW(), NOW()),
(64, 13, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'afternoon', 15, 0, '0', NOW(), NOW()),
-- 张军医生（14）- 过去、今天、未来
(65, 14, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'afternoon', 10, 10, '0', NOW(), NOW()),
(66, 14, CURDATE(), 'morning', 10, 1, '0', NOW(), NOW()),
(67, 14, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'afternoon', 10, 0, '0', NOW(), NOW()),
(68, 14, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'morning', 10, 0, '0', NOW(), NOW()),
-- 刘强医生（15）- 过去、今天、未来
(69, 15, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'morning', 10, 8, '0', NOW(), NOW()),
(70, 15, CURDATE(), 'afternoon', 10, 0, '0', NOW(), NOW()),
(71, 15, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'morning', 10, 2, '0', NOW(), NOW()),
(72, 15, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'afternoon', 10, 0, '0', NOW(), NOW()),
-- 陈莉医生（16）- 过去、今天、未来
(73, 16, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'afternoon', 15, 15, '0', NOW(), NOW()),
(74, 16, CURDATE(), 'morning', 15, 3, '0', NOW(), NOW()),
(75, 16, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'afternoon', 15, 1, '0', NOW(), NOW()),
(76, 16, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'morning', 15, 0, '0', NOW(), NOW()),
-- 林鹏医生（17）- 过去、今天、未来
(77, 17, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'morning', 15, 12, '0', NOW(), NOW()),
(78, 17, CURDATE(), 'afternoon', 15, 0, '0', NOW(), NOW()),
(79, 17, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'morning', 15, 2, '0', NOW(), NOW()),
(80, 17, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'afternoon', 15, 0, '0', NOW(), NOW());

-- ======================== 数据更新完成 ========================
SELECT '测试数据扩展完成！' AS message;
SELECT COUNT(*) AS '医生总数量' FROM doctor;
SELECT COUNT(*) AS '排班总数量' FROM schedule;

