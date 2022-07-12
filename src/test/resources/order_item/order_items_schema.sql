DROP TABLE orders if EXISTS;

CREATE TABLE order_items(
  uuid VARCHAR(36) NOT NULL PRIMARY KEY,
  item_uuid VARCHAR(36),
  order_uuid VARCHAR(36),
  quantity INTEGER
);