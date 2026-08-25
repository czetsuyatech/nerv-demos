-- Application-owned business schema stays separate from the canonical NERV Event migrations above.
create table demo_order (
  id uuid primary key,
  customer_id varchar(128) not null,
  status varchar(32) not null,
  created_at timestamp with time zone not null
);
