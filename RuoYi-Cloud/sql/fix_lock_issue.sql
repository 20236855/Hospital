-- 1. 查看MySQL的锁等待超时设置（默认50秒）
SHOW VARIABLES LIKE 'innodb_lock_wait_timeout';

-- 2. 查看当前正在运行的事务
SELECT * FROM information_schema.INNODB_TRX;

-- 3. 查看当前锁等待情况
SELECT * FROM information_schema.INNODB_LOCKS;

-- 4. 查看锁等待的详细信息
SELECT * FROM information_schema.INNODB_LOCK_WAITS;

-- 5. 查看当前锁定的表
SHOW OPEN TABLES WHERE In_use > 0;

-- 6. 查看当前进程
SHOW PROCESSLIST;

-- 7. 如果发现有长时间运行的事务，可以强制终止（谨慎使用！）
-- KILL <process_id>;

-- 8. 临时增加锁等待超时时间（可选）
-- SET GLOBAL innodb_lock_wait_timeout = 120;