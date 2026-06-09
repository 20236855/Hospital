-- 查看当前正在运行的事务
SELECT * FROM information_schema.INNODB_TRX;

-- 查看当前锁等待情况
SELECT * FROM information_schema.INNODB_LOCKS;

-- 查看锁等待的详细信息
SELECT * FROM information_schema.INNODB_LOCK_WAITS;

-- 查看当前锁定的表
SHOW OPEN TABLES WHERE In_use > 0;

-- 查看当前进程
SHOW PROCESSLIST;