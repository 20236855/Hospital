-- 半小时预约号源：保留 schedule 作为医生半天排班，schedule_slot 作为患者可预约时间片。
CREATE TABLE IF NOT EXISTS `schedule_slot` (
  `slot_id` bigint NOT NULL AUTO_INCREMENT COMMENT '时间片ID',
  `schedule_id` bigint NOT NULL COMMENT '排班ID',
  `doctor_id` bigint NOT NULL COMMENT '医生ID',
  `schedule_date` date NOT NULL COMMENT '排班日期',
  `start_time` time NOT NULL COMMENT '开始时间',
  `end_time` time NOT NULL COMMENT '结束时间',
  `max_number` int NULL DEFAULT 1 COMMENT '最大挂号数',
  `reserved_number` int NULL DEFAULT 0 COMMENT '已预约人数',
  `status` char(1) NULL DEFAULT '0' COMMENT '状态 0可约 1满号 2停诊/关闭',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`slot_id`) USING BTREE,
  KEY `idx_schedule_slot_schedule` (`schedule_id`) USING BTREE,
  KEY `idx_schedule_slot_doctor_date` (`doctor_id`, `schedule_date`) USING BTREE,
  KEY `idx_schedule_slot_date` (`schedule_date`) USING BTREE,
  CONSTRAINT `fk_schedule_slot_schedule` FOREIGN KEY (`schedule_id`) REFERENCES `schedule` (`schedule_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_schedule_slot_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`doctor_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '医生排班时间片表' ROW_FORMAT = Dynamic;

ALTER TABLE `register`
  ADD COLUMN `slot_id` bigint NULL COMMENT '预约时间片ID' AFTER `schedule_id`,
  ADD KEY `idx_register_slot` (`slot_id`);

-- 为已有 morning/afternoon 半天排班回填半小时号源。
INSERT INTO `schedule_slot`
  (`schedule_id`, `doctor_id`, `schedule_date`, `start_time`, `end_time`, `max_number`, `reserved_number`, `status`, `create_time`, `update_time`)
SELECT
  s.schedule_id,
  s.doctor_id,
  s.schedule_date,
  addtime(case when s.time_slot = 'morning' then '08:00:00' else '14:00:00' end, sec_to_time(n.n * 1800)) as start_time,
  addtime(case when s.time_slot = 'morning' then '08:00:00' else '14:00:00' end, sec_to_time((n.n + 1) * 1800)) as end_time,
  floor(ifnull(s.max_number, 0) / case when s.time_slot = 'morning' then 8 else 7 end)
    + case when n.n < mod(ifnull(s.max_number, 0), case when s.time_slot = 'morning' then 8 else 7 end) then 1 else 0 end as max_number,
  0 as reserved_number,
  s.status,
  now(),
  now()
from schedule s
join (
  select 0 n union all select 1 union all select 2 union all select 3
  union all select 4 union all select 5 union all select 6 union all select 7
) n
where s.time_slot in ('morning', 'afternoon')
  and n.n < case when s.time_slot = 'morning' then 8 else 7 end
  and n.n < least(ifnull(s.max_number, 0), case when s.time_slot = 'morning' then 8 else 7 end)
  and ifnull(s.max_number, 0) > 0
  and not exists (select 1 from schedule_slot ss where ss.schedule_id = s.schedule_id);
