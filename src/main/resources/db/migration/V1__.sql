CREATE TABLE pet
(
    id                   BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name                 VARCHAR(50),
    species              VARCHAR(50),
    available            BOOLEAN                                 NOT NULL,
    age                  INTEGER                                 NOT NULL,
    owner                VARCHAR(255),
    location             VARCHAR(255),
    profile_picture_path VARCHAR(255),
    CONSTRAINT pk_pet PRIMARY KEY (id)
);

CREATE TABLE user_table (
                            id SERIAL PRIMARY KEY,
                            username VARCHAR(255),
                            email VARCHAR(255) UNIQUE NOT NULL,
                            password VARCHAR(255) DEFAULT 'oauth_dummy',
                            phone_number VARCHAR(20) DEFAULT '0000000000',
                            profile_picture_path TEXT
);