CREATE TABLE poll (
    id uuid UNIQUE PRIMARY KEY,
    title text NOT NULL,
    version integer NOT NULL,
    questions jsonb NOT NULL
);

CREATE TABLE response (
    id uuid UNIQUE PRIMARY KEY,
    poll_id uuid REFERENCES poll(id),
    version integer NOT NULL,
    responses jsonb NOT NULL
);
