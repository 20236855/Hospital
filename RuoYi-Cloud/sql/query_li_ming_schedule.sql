-- 查询李明医生(doctor_id=1)的所有排班记录
SELECT 
    schedule_id,
    doctor_id,
    schedule_date,
    time_slot,
    max_number,
    reserved_number,
    available_number,
    status,
    create_time,
    update_time
FROM schedule
WHERE doctor_id = 1
ORDER BY schedule_date ASC, 
         CASE time_slot WHEN 'morning' THEN 1 WHEN 'afternoon' THEN 2 END ASC;

-- 查询李明医生未来几天的排班记录（今天及以后）
SELECT 
    schedule_id,
    doctor_id,
    schedule_date,
    time_slot,
    max_number,
    reserved_number,
    available_number,
    status,
    create_time,
    update_time
FROM schedule
WHERE doctor_id = 1
  AND schedule_date >= CURDATE()
ORDER BY schedule_date ASC, 
         CASE time_slot WHEN 'morning' THEN 1 WHEN 'afternoon' THEN 2 END ASC;

-- 统计李明医生每天的排班数量
SELECT 
    schedule_date,
    COUNT(*) as total_schedules,
    SUM(CASE WHEN time_slot = 'morning' THEN 1 ELSE 0 END) as morning_count,
    SUM(CASE WHEN time_slot = 'afternoon' THEN 1 ELSE 0 END) as afternoon_count,
    SUM(reserved_number) as total_reserved,
    SUM(max_number) as total_max
FROM schedule
WHERE doctor_id = 1
GROUP BY schedule_date
ORDER BY schedule_date ASC;