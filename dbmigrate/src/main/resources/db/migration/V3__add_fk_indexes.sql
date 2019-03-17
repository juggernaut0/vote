CREATE INDEX poll_created_by_idx ON poll(created_by);

CREATE INDEX response_poll_id_idx ON response(poll_id);
CREATE INDEX response_voter_id_idx ON response(voter_id);
