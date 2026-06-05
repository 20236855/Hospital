
-- 更新schedule的status
UPDATE schedule SET status = 1 WHERE reserved_number >= max_number;
UPDATE schedule SET status = 0 WHERE reserved_number < max_number;

-- 验证
SELECT schedule_id, doctor_id, schedule_date, time_slot, max_number, reserved_number, status
FROM schedule
ORDER BY doctor_id, schedule_date
LIMIT 20;
