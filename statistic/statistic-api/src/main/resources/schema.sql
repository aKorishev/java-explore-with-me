CREATE TABLE if NOT EXISTS Statistics(
    statistic_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    app VARCHAR(100) NOT NULL,
    ip VARCHAR(100) NOT NULL,
    uri VARCHAR(100) NOT NULL,
    timestamp timestamp WITH TIME ZONE NOT NULL
);