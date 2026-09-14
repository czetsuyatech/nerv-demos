-- Render user_hobby_aud as a trusted, quoted SQL identifier for each entity AND collection audit table.
-- Apply in the revision table schema; this template is not an automatically discovered Flyway migration.
CREATE TABLE user_hobby_aud (
    audit_row_id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id bigint NOT NULL,
    rev integer NOT NULL REFERENCES revinfo (rev),
    revtype smallint NOT NULL CHECK (revtype BETWEEN 0 AND 2),
    field_name varchar(255),
    old_value text,
    new_value text,
    updated_by varchar(255),
    updated timestamp with time zone NOT NULL
);
-- History lookup by entity id/revision; table selection supplies entity type.
CREATE INDEX ON user_hobby_aud (id, rev);
-- Revision-only filtering and the FK's referencing side.
CREATE INDEX ON user_hobby_aud (rev);
-- Default ordering and time-range filters.
CREATE INDEX ON user_hobby_aud (updated);
