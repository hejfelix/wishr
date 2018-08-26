ALTER TABLE wishes
  ADD COLUMN "granted" BOOLEAN DEFAULT FALSE;

INSERT INTO wishes (email, heading, description, imageurl, index, id, granted)
    SELECT email, heading, description, imageurl, index, id, true
    FROM granted;

DROP TABLE granted;