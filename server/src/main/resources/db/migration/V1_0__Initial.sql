CREATE TABLE users (
      firstName          VARCHAR NOT NULL,
      lastName           VARCHAR NOT NULL,
      email              VARCHAR PRIMARY KEY,
      hashedPassword     VARCHAR NOT NULL,
      secretURL          VARCHAR NOT NULL,
      registrationToken  VARCHAR NOT NULL,
      finalized          BOOLEAN NOT NULL
);

CREATE TABLE secrets (
       email            VARCHAR UNIQUE REFERENCES users,
       secret           VARCHAR NOT NULL,
       expirationDate   TIMESTAMP NOT NULL
);

CREATE TABLE wishes (
     email                    VARCHAR NOT NULL,
     heading                  VARCHAR NOT NULL,
     description              VARCHAR NOT NULL,
     imageURL                 VARCHAR,
     index                    INTEGER NOT NULL,
     granted                  BOOLEAN NOT NULL,
     id                       SERIAL PRIMARY KEY
);
