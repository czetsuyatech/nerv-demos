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
