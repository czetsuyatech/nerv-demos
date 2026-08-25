-- Canonical NERV Event PostgreSQL migration 001-create-outbox.sql, owned and run by this application.
create table nerv_outbox_event (
  id varchar(128) not null, event_id varchar(256) not null, event_type varchar(256) not null,
  source varchar(512) not null, correlation_id varchar(256), event_timestamp timestamp with time zone not null,
  destination varchar(512) not null, payload text not null, status varchar(32) not null,
  attempt_count integer not null default 0, available_at timestamp with time zone not null,
  locked_at timestamp with time zone, locked_by varchar(128), last_error varchar(2048),
  created_at timestamp with time zone not null, updated_at timestamp with time zone not null,
  published_at timestamp with time zone, version bigint not null default 0,
  constraint pk_nerv_outbox_event primary key (id),
  constraint chk_nerv_outbox_attempt_count_nonnegative check (attempt_count >= 0),
  constraint chk_nerv_outbox_version_nonnegative check (version >= 0)
);
