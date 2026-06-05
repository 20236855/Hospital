-- 修改register表，让schedule_id允许NULL
ALTER TABLE register MODIFY COLUMN schedule_id BIGINT NULL;

-- 验证修改
DESCRIBE register;
