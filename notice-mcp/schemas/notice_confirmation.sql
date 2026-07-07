create table if not exists notice_confirmation_log (
    id integer primary key autoincrement,
    user_id text,
    patient_id text,
    session_id text,
    notice_version text not null,
    confirmed integer not null,
    high_risk_acknowledged integer not null default 0,
    source text,
    ip text,
    user_agent text,
    symptoms_text text,
    metadata_json text,
    created_at text not null
);

create index if not exists idx_notice_confirmation_user
    on notice_confirmation_log(user_id, patient_id, session_id);
