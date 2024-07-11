# --- !Ups
CREATE TABLE user_name (
    id int AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

# --- !Downs
DROP TABLE user_name;
