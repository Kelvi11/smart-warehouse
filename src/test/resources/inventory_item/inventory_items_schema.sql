DROP TABLE inventory_items if EXISTS;

CREATE TABLE inventory_items(
  uuid VARCHAR(36) NOT NULL PRIMARY KEY,
  name VARCHAR(255),
  quantity INTEGER,
  unit_price NUMERIC(19,2),
  package_volume NUMERIC(19,2)
);