-- Canonical NERV Event PostgreSQL migration 002-create-inbox.sql, owned and run by this application.
create table nerv_inbox_event (
  event_id varchar(256) not null, event_type varchar(256) not null, event_timestamp timestamp with time zone not null,
  source varchar(512) not null, correlation_id varchar(256), payload text not null, content_type varchar(128) not null,
  status varchar(32) not null, attempt_count integer not null default 0, received_at timestamp with time zone not null,
  available_at timestamp with time zone, processing_at timestamp with time zone, processing_by varchar(128),
  processed_at timestamp with time zone, failed_at timestamp with time zone, last_error varchar(2048),
  created_at timestamp with time zone not null, updated_at timestamp with time zone not null, version bigint not null default 0,
  constraint pk_nerv_inbox_event primary key (event_id),
  constraint chk_nerv_inbox_attempt_count_nonnegative check (attempt_count >= 0),
  constraint chk_nerv_inbox_version_nonnegative check (version >= 0)
);
