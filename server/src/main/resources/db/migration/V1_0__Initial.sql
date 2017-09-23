CREATE TABLE users (
      firstName          VARCHAR,
      lastName           VARCHAR,
      email              VARCHAR PRIMARY KEY,
      hashedPassword     VARCHAR,
      secretURL          VARCHAR,
      registrationToken  VARCHAR,
      finalized          BOOLEAN
);

CREATE TABLE secrets (
       email            VARCHAR UNIQUE REFERENCES users,
       secret           VARCHAR,
       expirationDate   TIMESTAMP
);

CREATE TABLE wishes (
     email                    VARCHAR,
     heading                  VARCHAR,
     description              VARCHAR,
     imageURL                 VARCHAR,
     index                    INTEGER,
     id                       SERIAL PRIMARY KEY
);
