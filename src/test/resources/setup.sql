CREATE TABLE system_user(ID serial PRIMARY KEY NOT NULL, data jsonb);
CREATE UNIQUE INDEX system_user_id ON system_user ((data->'id'));