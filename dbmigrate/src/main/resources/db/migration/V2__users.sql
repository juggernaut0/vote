CREATE TABLE vote_user (
    id uuid UNIQUE PRIMARY KEY,
    google_id text UNIQUE NOT NULL,
    email text
);
CREATE INDEX vote_user_google_id_idx ON vote_user(google_id);

ALTER TABLE poll ADD COLUMN created_by uuid REFERENCES vote_user(id);
ALTER TABLE poll ADD COLUMN created_dt timestamp with time zone;

ALTER TABLE response ADD COLUMN voter_id uuid REFERENCES vote_user(id);

-- Fix for existing nullable FK
ALTER TABLE response ALTER COLUMN poll_id SET NOT NULL;