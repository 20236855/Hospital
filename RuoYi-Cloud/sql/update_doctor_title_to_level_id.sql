-- 修改doctor表：将title字段改为level_id字段，关联regist_level表
-- 执行前请确保已备份数据

-- 1. 首先，我们需要根据现有的title名称找到对应的level_id
-- 注意：这需要根据您实际的regist_level表数据来调整映射关系
-- 假设映射关系如下：
-- "主任医师" -> level_id=3
-- "副主任医师" -> level_id=2
-- "普通医师" -> level_id=1
-- 请根据您的实际数据修改这个映射

-- 2. 添加新的level_id字段
ALTER TABLE doctor ADD COLUMN level_id BIGINT COMMENT '挂号级别ID，关联regist_level表';

-- 3. 根据title字段更新level_id（请根据实际情况修改下面的CASE语句）
UPDATE doctor 
SET level_id = CASE 
    WHEN title = '主任医师' THEN 3
    WHEN title = '副主任医师' THEN 2
    ELSE 1 -- 默认普通号
END;

-- 4. 删除旧的title字段
ALTER TABLE doctor DROP COLUMN title;

-- 5. 如果需要，可以添加外键约束（可选）
-- ALTER TABLE doctor ADD CONSTRAINT fk_doctor_level FOREIGN KEY (level_id) REFERENCES regist_level(level_id);

-- 验证修改
SELECT doctor_id, doctor_name, level_id FROM doctor LIMIT 10;
