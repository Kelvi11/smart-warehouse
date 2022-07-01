DROP TABLE orders if EXISTS;

CREATE TABLE orders(
  uuid VARCHAR(36) NOT NULL PRIMARY KEY,
  submitted_date DATE,
  deadline_date DATE,
  status VARCHAR(100)
);