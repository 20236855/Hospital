-- 删除doctor表中的outpatient_fee字段
-- 现在挂号费从regist_level表获取

-- 删除outpatient_fee字段
ALTER TABLE doctor DROP COLUMN outpatient_fee;

-- 验证修改
SELECT doctor_id, doctor_name, level_id FROM doctor LIMIT 10;
