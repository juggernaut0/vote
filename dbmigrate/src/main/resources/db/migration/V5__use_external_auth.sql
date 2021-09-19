ALTER TABLE poll DROP CONSTRAINT poll_created_by_fkey;
ALTER TABLE response DROP CONSTRAINT response_voter_id_fkey;

DROP TABLE vote_user;
