CREATE TABLE stock (
  sku VARCHAR(50) PRIMARY KEY,
  available INTEGER NOT NULL CHECK (available >= 0)
);
CREATE TABLE reservation (
  tenant VARCHAR(80),
  order_id BIGINT,
  state VARCHAR(20) NOT NULL,
  items TEXT NOT NULL,
  PRIMARY KEY (tenant, order_id)
);
