-- 把满了的排班status设为1
UPDATE schedule SET status = 1 WHERE reserved_number &gt;= max_number;

-- 把没满的排班status设为0
UPDATE schedule SET status = 0 WHERE reserved_number &lt; max_number;

-- 验证修改
SELECT schedule_id, doctor_id, schedule_date, time_slot, max_number, reserved_number, status 
FROM schedule 
ORDER BY doctor_id, schedule_date
LIMIT 20;
