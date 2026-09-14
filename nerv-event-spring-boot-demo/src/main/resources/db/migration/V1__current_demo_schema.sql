-- Fresh NERV Event 2.1.0 and demo schema. Includes claim fencing and ordering keys from creation.
create table nerv_outbox_event (
  id varchar(128) not null, event_id varchar(256) not null, event_type varchar(256) not null,
  source varchar(512) not null, correlation_id varchar(256), event_timestamp timestamp with time zone not null,
  destination varchar(512) not null, payload text not null, status varchar(32) not null,
  attempt_count integer not null default 0, available_at timestamp with time zone not null,
  locked_at timestamp with time zone, locked_by varchar(128), last_error varchar(2048),
  created_at timestamp with time zone not null, updated_at timestamp with time zone not null,
  published_at timestamp with time zone, version bigint not null default 0,
  claim_version bigint not null default 0, ordering_key varchar(512),
  constraint pk_nerv_outbox_event primary key (id),
  constraint chk_nerv_outbox_attempt_count_nonnegative check (attempt_count >= 0),
  constraint chk_nerv_outbox_version_nonnegative check (version >= 0),
  constraint chk_nerv_outbox_claim_version_nonnegative check (claim_version >= 0)
);

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

-- Canonical NERV Event PostgreSQL migration 003-create-trace-context.sql.
create table nerv_event_trace_context (
  event_id varchar(256) not null, context_json text not null, created_at timestamp with time zone not null,
  constraint pk_nerv_event_trace_context primary key (event_id)
);

-- Canonical NERV Event PostgreSQL migration 004-create-indexes.sql.
create index idx_nerv_outbox_status_available on nerv_outbox_event (status, available_at);
create index idx_nerv_outbox_status_locked on nerv_outbox_event (status, locked_at);
create index idx_nerv_outbox_status_published on nerv_outbox_event (status, published_at);
create index idx_nerv_outbox_event_id on nerv_outbox_event (event_id);
create index idx_nerv_outbox_created_at on nerv_outbox_event (created_at);
create index idx_nerv_outbox_status_updated on nerv_outbox_event (status, updated_at, id);
create index idx_nerv_inbox_status_available on nerv_inbox_event (status, available_at);
create index idx_nerv_inbox_status_processing on nerv_inbox_event (status, processing_at);
create index idx_nerv_inbox_status_processed on nerv_inbox_event (status, processed_at);
create index idx_nerv_inbox_received_at on nerv_inbox_event (received_at);
create index idx_nerv_inbox_event_type on nerv_inbox_event (event_type);
create index idx_nerv_inbox_status_updated on nerv_inbox_event (status, updated_at, event_id);

-- Application-owned business schema stays separate from the canonical NERV Event migrations above.
create table demo_order (
  id uuid primary key,
  customer_id varchar(128) not null,
  status varchar(32) not null,
  created_at timestamp with time zone not null
);

create index idx_nerv_outbox_ordering_sequence
  on nerv_outbox_event (ordering_key, created_at, id)
  where ordering_key is not null and status in ('PENDING', 'PROCESSING');
