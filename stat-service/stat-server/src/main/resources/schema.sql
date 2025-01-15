CREATE TABLE if NOT EXISTS Statistics(
    statistic_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    app VARCHAR(100) NOT NULL,
    ip VARCHAR(100) NOT NULL,
    uri VARCHAR(100) NOT NULL,
    timestamp timestamp WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS apps (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  CONSTRAINT UQ_APPS_NAME UNIQUE(name)
);

CREATE TABLE IF NOT EXISTS endpoint_hit (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  app_id BIGINT NOT NULL,
  uri VARCHAR(2048) NOT NULL,
  ip VARCHAR(15) NOT NULL,
  ts TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  CONSTRAINT FK_ENDPOINT_HIT_ON_APP FOREIGN KEY (app_id) REFERENCES apps (id)
);