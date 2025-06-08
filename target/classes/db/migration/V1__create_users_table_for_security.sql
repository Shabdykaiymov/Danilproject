CREATE TABLE users (
  "id" UUID,
  "username" Varchar(60) NOT NULL,
  "password" Varchar(100) NOT NULL,
  "email" Varchar(100) NOT NULL,
  "firstname" Varchar(60) NOT NULL,
  "lastname" Varchar(60) NOT NULL,
  "enabled" BOOLEAN NOT NULL,
  PRIMARY KEY ("id")
);


ALTER TABLE users
  ADD CONSTRAINT unique_username UNIQUE (username);

CREATE TABLE authorities (
    username VARCHAR(50) NOT NULL,
    authority  VARCHAR(50) NOT NULL,
  CONSTRAINT FK_AUTHORITIES_USERS FOREIGN KEY (username) REFERENCES users (username)
);

CREATE UNIQUE INDEX IX_AUTH_USERNAME ON AUTHORITIES (username, authority);

