
INSERT INTO users (id, username, email, password, first_name, last_name, role, active) VALUES
(100, 'admin-test', 'admin@test.com', '$2a$10$Xx9Q8LrOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin', 'Test', 'ADMIN', true),
(101, 'manager-test', 'manager@test.com', '$2a$10$Yy9Q8LrOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhXz', 'Manager', 'Test', 'MANAGER', true),
(102, 'cashier-test', 'cashier@test.com', '$2a$10$Zz9Q8LrOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhYa', 'Cashier', 'Test', 'CASHIER', true);

INSERT INTO branch (id, name, address) VALUES
(100, 'Test Branch', '123 Test Street');

INSERT INTO product (id, name, category, price, version) VALUES
(100, 'Test Product', 'Test Category', 10.50, 0);

INSERT INTO branch_inventory (branch_id, product_id, stock, min_stock, last_restock_date, version)
VALUES (100, 100, 100, 5, NOW(), 0);

ALTER TABLE users AUTO_INCREMENT = 200;
ALTER TABLE branch AUTO_INCREMENT = 200;
ALTER TABLE product AUTO_INCREMENT = 200;
ALTER TABLE branch_inventory AUTO_INCREMENT = 200;
ALTER TABLE sale AUTO_INCREMENT = 200;
ALTER TABLE sale_detail AUTO_INCREMENT = 200;