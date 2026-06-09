-- 1. 查看当前正在运行的事务
SELECT 
    trx_id,
    trx_state,
    trx_started,
    trx_requested_lock_id,
    trx_wait_started,
    trx_mysql_thread_id,
    trx_query
FROM information_schema.INNODB_TRX;

-- 2. 查看当前锁等待情况
SELECT 
    lock_id,
    lock_trx_id,
    lock_mode,
    lock_type,
    lock_table,
    lock_index,
    lock_space,
    lock_page,
    lock_rec,
    lock_data
FROM information_schema.INNODB_LOCKS;

-- 3. 查看锁等待的详细信息
SELECT 
    requesting_trx_id,
    requested_lock_id,
    blocking_trx_id,
    blocking_lock_id
FROM information_schema.INNODB_LOCK_WAITS;

-- 4. 查看当前锁定的表
SHOW OPEN TABLES WHERE In_use > 0;

-- 5. 查看当前进程
SHOW PROCESSLIST;

-- 6. 查看MySQL的锁等待超时设置
SHOW VARIABLES LIKE 'innodb_lock_wait_timeout';

-- 7. 查看事务隔离级别
SELECT @@GLOBAL.tx_isolation, @@SESSION.tx_isolation;