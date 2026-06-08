-- AI问诊长短期记忆表
-- 若数据库中已存在 ai_chat_record，请确认字段名与后端 mapper 保持一致。
create table if not exists ai_chat_record (
  record_id bigint not null auto_increment comment '记录ID',
  user_id bigint not null comment '系统用户ID',
  patient_id bigint not null comment '患者ID',
  session_id varchar(64) not null comment '会话ID',
  `role` varchar(20) not null comment '消息角色：user/assistant/system',
  content text not null comment '记忆内容',
  memory_type varchar(20) not null default 'short' comment '记忆类型：short短期对话/long长期记忆',
  create_time datetime default current_timestamp comment '创建时间',
  update_time datetime default null on update current_timestamp comment '更新时间',
  primary key (record_id),
  key idx_ai_chat_patient_session (patient_id, session_id, create_time),
  key idx_ai_chat_patient_memory (patient_id, memory_type, create_time),
  key idx_ai_chat_user (user_id)
) engine=InnoDB default charset=utf8mb4 comment='AI问诊记忆记录';
