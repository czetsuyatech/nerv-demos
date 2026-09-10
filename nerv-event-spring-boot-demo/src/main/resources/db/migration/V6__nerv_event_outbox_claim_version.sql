-- Canonical NERV Event PostgreSQL migration 005-add-outbox-claim-version.sql.
-- V6 is used here because V5 already belongs to the demo application's business schema.
alter table nerv_outbox_event
  add column claim_version bigint not null default 0;

alter table nerv_outbox_event
  add constraint chk_nerv_outbox_claim_version_nonnegative check (claim_version >= 0);
