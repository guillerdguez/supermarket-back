CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    action VARCHAR(100) NOT NULL,
    details VARCHAR(1000),
    ip_address VARCHAR(50),
    timestamp DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS branch (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    address VARCHAR(200) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    category VARCHAR(50),
    price DECIMAL(19, 2),
    version INT
);

CREATE TABLE IF NOT EXISTS cash_registers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    branch_id BIGINT NOT NULL,
    opening_balance DECIMAL(19, 2) NOT NULL,
    closing_balance DECIMAL(19, 2),
    opening_time DATETIME NOT NULL,
    closing_time DATETIME,
    status VARCHAR(20) NOT NULL,
    opened_by_id BIGINT NOT NULL,
    closed_by_id BIGINT,
    FOREIGN KEY (branch_id) REFERENCES branch(id),
    FOREIGN KEY (opened_by_id) REFERENCES users(id),
    FOREIGN KEY (closed_by_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS branch_inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    branch_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    stock INT NOT NULL,
    min_stock INT NOT NULL DEFAULT 5,
    last_restock_date DATETIME,
    version BIGINT,
    UNIQUE KEY uk_branch_product (branch_id, product_id),
    FOREIGN KEY (branch_id) REFERENCES branch(id),
    FOREIGN KEY (product_id) REFERENCES product(id)
);

CREATE TABLE IF NOT EXISTS stock_transfers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_branch_id BIGINT NOT NULL,
    target_branch_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    requested_by_id BIGINT NOT NULL,
    approved_by_id BIGINT,
    requested_at DATETIME NOT NULL,
    approved_at DATETIME,
    completed_at DATETIME,
    rejection_reason VARCHAR(255),
    version BIGINT,
    FOREIGN KEY (source_branch_id) REFERENCES branch(id),
    FOREIGN KEY (target_branch_id) REFERENCES branch(id),
    FOREIGN KEY (product_id) REFERENCES product(id),
    FOREIGN KEY (requested_by_id) REFERENCES users(id),
    FOREIGN KEY (approved_by_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS sale (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date DATE,
    status VARCHAR(20),
    total DECIMAL(19, 2),
    branch_id BIGINT NOT NULL,
    cash_register_id BIGINT NOT NULL,
    created_by_id BIGINT,
    created_at DATETIME,
    cancelled_by_id BIGINT,
    cancellation_reason VARCHAR(255),
    cancelled_at DATETIME,
    FOREIGN KEY (branch_id) REFERENCES branch(id),
    FOREIGN KEY (cash_register_id) REFERENCES cash_registers(id),
    FOREIGN KEY (created_by_id) REFERENCES users(id),
    FOREIGN KEY (cancelled_by_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS sale_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stock INT NOT NULL,
    price DECIMAL(19, 2),
    sale_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    FOREIGN KEY (sale_id) REFERENCES sale(id),
    FOREIGN KEY (product_id) REFERENCES product(id)
);

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sale_id BIGINT NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    payment_type VARCHAR(20) NOT NULL,
    payment_date DATETIME NOT NULL,
    reference VARCHAR(255),
    FOREIGN KEY (sale_id) REFERENCES sale(id)
);

INSERT IGNORE INTO users (id, username, email, password, first_name, last_name, role, active) VALUES
(1, 'admin', 'admin@supermarket.com', '$2a$10$Xx9Q8LrOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'System', 'Administrator', 'ADMIN', true),
(2, 'manager1', 'manager@supermarket.com', '$2a$10$Yy9Q8LrOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhXz', 'Store', 'Manager', 'MANAGER', true),
(3, 'cashier1', 'cashier@supermarket.com', '$2a$10$Zz9Q8LrOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhYa', 'John', 'Cashier', 'CASHIER', true),
(4, 'testuser', 'user@supermarket.com', '$2a$10$Aa9Q8LrOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhZb', 'Test', 'User', 'CASHIER', true);

INSERT IGNORE INTO branch (id, name, address) VALUES
(1, 'Central Branch', '123 Main Avenue, Central City'),
(2, 'North Branch', '456 North Street, North Zone'),
(3, 'South Branch', '789 South Avenue, South Zone'),
(4, 'East Branch', '101 East Street, East Zone'),
(5, 'West Branch', '202 West Avenue, West Zone');

INSERT IGNORE INTO product (id, name, category, price, version) VALUES
(1, 'Whole Milk 1L', 'Dairy', 1200.50, 0), (2, 'Natural Yogurt', 'Dairy', 800.75, 0),
(3, 'Mozzarella Cheese 500g', 'Dairy', 3500.00, 0), (4, 'Butter 250g', 'Dairy', 1800.25, 0),
(5, 'Heavy Cream 200ml', 'Dairy', 950.00, 0), (6, 'Apples 1kg', 'Fruits and Vegetables', 1500.00, 0),
(7, 'Bananas 1kg', 'Fruits and Vegetables', 1200.00, 0), (8, 'Carrots 1kg', 'Fruits and Vegetables', 800.50, 0),
(9, 'Tomatoes 1kg', 'Fruits and Vegetables', 1800.00, 0), (10, 'Lettuce Unit', 'Fruits and Vegetables', 700.00, 0),
(11, 'Chicken Breast 1kg', 'Meats', 8500.00, 0), (12, 'Ground Beef 500g', 'Meats', 4500.50, 0),
(13, 'Sausages 500g', 'Meats', 3200.00, 0), (14, 'Bacon 250g', 'Meats', 2800.00, 0),
(15, 'Beef Steak 500g', 'Meats', 12000.00, 0), (16, 'Mineral Water 1.5L', 'Beverages', 800.00, 0),
(17, 'Cola Soda 2L', 'Beverages', 2500.00, 0), (18, 'Orange Juice 1L', 'Beverages', 1800.50, 0),
(19, 'National Beer 330ml', 'Beverages', 1200.00, 0), (20, 'Red Wine 750ml', 'Beverages', 8500.00, 0),
(21, 'White Bread 500g', 'Bakery', 1200.00, 0), (22, 'Whole Wheat Bread 500g', 'Bakery', 1500.00, 0),
(23, 'Croissants 4 units', 'Bakery', 2800.00, 0), (24, 'Chocolate Cookies', 'Bakery', 950.00, 0),
(25, 'Chocolate Cake', 'Bakery', 12000.00, 0), (26, 'Liquid Detergent 1L', 'Cleaning', 4500.00, 0),
(27, 'Hand Soap', 'Cleaning', 1200.50, 0), (28, 'Toilet Paper 4 rolls', 'Cleaning', 2800.00, 0),
(29, 'All-purpose Cleaner', 'Cleaning', 3200.00, 0), (30, 'Disinfectant 500ml', 'Cleaning', 1800.00, 0);

INSERT IGNORE INTO branch_inventory (branch_id, product_id, stock, min_stock, last_restock_date, version)
SELECT b.id, p.id, 50, 10, NOW(), 0 FROM product p CROSS JOIN branch b;

INSERT IGNORE INTO cash_registers (id, branch_id, opening_balance, closing_balance, opening_time, closing_time, status, opened_by_id, closed_by_id) VALUES
(1, 1, 10000.00, 38600.00, '2026-02-21 08:00:00', '2026-02-21 20:00:00', 'CLOSED', 3, 2),
(2, 1, 10000.00, 50100.75, '2026-02-22 08:00:00', '2026-02-22 20:00:00', 'CLOSED', 3, 2),
(3, 1, 10000.00, 42400.00, '2026-02-23 08:00:00', '2026-02-23 20:00:00', 'CLOSED', 3, 2),
(4, 2, 10000.00, 25900.50, '2026-02-21 08:00:00', '2026-02-21 20:00:00', 'CLOSED', 3, 2),
(5, 2, 10000.00, 32300.00, '2026-02-22 08:00:00', '2026-02-22 20:00:00', 'CLOSED', 3, 2),
(6, 3, 10000.00, 17200.00, '2026-02-21 08:00:00', '2026-02-21 20:00:00', 'CLOSED', 4, 2),
(7, 3, 10000.00, 42100.50, '2026-02-22 08:00:00', '2026-02-22 20:00:00', 'CLOSED', 4, 2),
(8, 4, 10000.00, 18200.00, '2026-02-21 08:00:00', '2026-02-21 20:00:00', 'CLOSED', 3, 1),
(9, 5, 10000.00, 21200.00, '2026-02-21 08:00:00', '2026-02-21 20:00:00', 'CLOSED', 4, 1),
(10, 1, 10000.00, NULL, '2026-02-24 08:00:00', NULL, 'OPEN', 3, NULL),
(11, 2, 10000.00, 12950.75, '2026-02-23 08:00:00', '2026-02-23 20:00:00', 'CLOSED', 4, 2),
(12, 2, 10000.00, 55200.00, '2026-02-24 08:00:00', '2026-02-24 20:00:00', 'CLOSED', 3, 2),
(13, 3, 10000.00, 28950.00, '2026-02-23 08:00:00', '2026-02-23 20:00:00', 'CLOSED', 4, 2),
(14, 3, 10000.00, 22500.00, '2026-02-24 08:00:00', '2026-02-24 20:00:00', 'CLOSED', 3, 2),
(15, 4, 10000.00, 25450.50, '2026-02-22 08:00:00', '2026-02-22 20:00:00', 'CLOSED', 4, 2),
(16, 4, 10000.00, 33600.00, '2026-02-23 08:00:00', '2026-02-23 20:00:00', 'CLOSED', 3, 2),
(17, 4, 10000.00, 50000.75, '2026-02-24 08:00:00', '2026-02-24 20:00:00', 'CLOSED', 4, 2),
(18, 5, 10000.00, 28650.50, '2026-02-22 08:00:00', '2026-02-22 20:00:00', 'CLOSED', 3, 2),
(19, 5, 10000.00, 35400.00, '2026-02-23 08:00:00', '2026-02-23 20:00:00', 'CLOSED', 4, 2),
(20, 5, 10000.00, 46900.75, '2026-02-24 08:00:00', '2026-02-24 20:00:00', 'CLOSED', 3, 2);

INSERT IGNORE INTO sale (id, date, status, total, branch_id, cash_register_id, created_by_id, created_at, cancelled_by_id, cancellation_reason, cancelled_at) VALUES
(1,  '2026-02-21', 'REGISTERED', 28501.50, 1, 1, 3, '2026-02-21 09:30:00', NULL, NULL, NULL),
(2,  '2026-02-21', 'REGISTERED', 12000.00, 1, 1, 3, '2026-02-21 11:00:00', NULL, NULL, NULL),
(3,  '2026-02-22', 'REGISTERED', 17600.75, 1, 2, 3, '2026-02-22 10:00:00', NULL, NULL, NULL),
(4,  '2026-02-22', 'CANCELLED',  8500.00,  1, 2, 3, '2026-02-22 12:00:00', 2, 'Customer changed mind', '2026-02-22 12:30:00'),
(5,  '2026-02-23', 'REGISTERED', 32400.00, 1, 3, 3, '2026-02-23 09:00:00', NULL, NULL, NULL),
(6,  '2026-02-21', 'REGISTERED', 15800.50, 2, 4, 4, '2026-02-21 10:00:00', NULL, NULL, NULL),
(7,  '2026-02-22', 'REGISTERED', 22400.00, 2, 5, 4, '2026-02-22 09:30:00', NULL, NULL, NULL),
(8,  '2026-02-23', 'REGISTERED', 3100.00,  2, 11, 4, '2026-02-23 14:00:00', NULL, NULL, NULL),
(9,  '2026-02-23', 'REGISTERED', 9850.75,  2, 11, 3, '2026-02-23 15:30:00', NULL, NULL, NULL),
(10, '2026-02-24', 'REGISTERED', 45200.00, 2, 12, 4, '2026-02-24 10:00:00', NULL, NULL, NULL),
(11, '2026-02-21', 'REGISTERED', 7200.00,  3, 6, 4, '2026-02-21 09:00:00', NULL, NULL, NULL),
(12, '2026-02-22', 'REGISTERED', 15300.50, 3, 7, 4, '2026-02-22 11:00:00', NULL, NULL, NULL),
(13, '2026-02-22', 'REGISTERED', 26800.00, 3, 7, 3, '2026-02-22 13:00:00', NULL, NULL, NULL),
(14, '2026-02-23', 'REGISTERED', 18950.00, 3, 13, 3, '2026-02-23 10:30:00', NULL, NULL, NULL),
(15, '2026-02-24', 'CANCELLED',  12500.00, 3, 14, 4, '2026-02-24 09:00:00', 1, 'Duplicate sale entry', '2026-02-24 09:15:00'),
(16, '2026-02-21', 'REGISTERED', 8200.00,  4, 8, 3, '2026-02-21 10:00:00', NULL, NULL, NULL),
(17, '2026-02-22', 'REGISTERED', 15450.50, 4, 15, 3, '2026-02-22 11:00:00', NULL, NULL, NULL),
(18, '2026-02-23', 'REGISTERED', 23600.00, 4, 16, 4, '2026-02-23 09:30:00', NULL, NULL, NULL),
(19, '2026-02-24', 'REGISTERED', 19200.75, 4, 17, 3, '2026-02-24 10:00:00', NULL, NULL, NULL),
(20, '2026-02-24', 'REGISTERED', 30800.00, 4, 17, 4, '2026-02-24 12:00:00', NULL, NULL, NULL),
(21, '2026-02-21', 'REGISTERED', 11200.00, 5, 9, 3, '2026-02-21 09:30:00', NULL, NULL, NULL),
(22, '2026-02-22', 'REGISTERED', 18650.50, 5, 18, 4, '2026-02-22 10:00:00', NULL, NULL, NULL),
(23, '2026-02-23', 'REGISTERED', 25400.00, 5, 19, 3, '2026-02-23 11:00:00', NULL, NULL, NULL),
(24, '2026-02-24', 'REGISTERED', 17300.75, 5, 20, 4, '2026-02-24 09:30:00', NULL, NULL, NULL),
(25, '2026-02-24', 'REGISTERED', 29600.00, 5, 20, 3, '2026-02-24 11:00:00', NULL, NULL, NULL);

INSERT IGNORE INTO sale_detail (id, stock, price, sale_id, product_id) VALUES
(1, 2, 1200.50, 1, 1), (2, 1, 3500.00, 1, 3), (3, 3, 1500.00, 1, 6), (4, 1, 8500.00, 1, 11), (5, 2, 2500.00, 1, 17),
(6, 1, 12000.00, 2, 25), (7, 4, 800.75, 3, 2), (8, 2, 1800.50, 3, 8), (9, 1, 4500.50, 3, 12), (10, 1, 8500.00, 4, 11),
(11, 5, 1200.50, 5, 1), (12, 2, 3500.00, 5, 3), (13, 1, 12000.00, 5, 15), (14, 2, 800.00, 6, 16), (15, 3, 1200.00, 6, 7),
(16, 1, 8500.00, 6, 11), (17, 1, 4500.50, 7, 12), (18, 2, 2800.00, 7, 23), (19, 3, 950.00, 7, 24), (20, 2, 950.00, 8, 5),
(21, 1, 1200.00, 8, 22), (22, 1, 8500.00, 9, 11), (23, 2, 675.00, 9, 10), (24, 2, 8500.00, 10, 11), (25, 1, 12000.00, 10, 15),
(26, 3, 4500.50, 10, 12), (27, 2, 3200.00, 10, 13), (28, 3, 1200.50, 11, 1), (29, 2, 800.00, 11, 16), (30, 1, 8500.00, 12, 11),
(31, 4, 950.00, 12, 5), (32, 2, 800.50, 12, 8), (33, 1, 4500.00, 13, 26), (34, 2, 2800.00, 13, 28), (35, 1, 3200.00, 13, 29),
(36, 3, 1200.50, 13, 27), (37, 2, 3500.00, 14, 3), (38, 1, 1800.25, 14, 4), (39, 3, 800.75, 14, 2), (40, 1, 12500.00, 15, 15),
(41, 2, 1200.50, 16, 1), (42, 1, 3500.00, 16, 3), (43, 1, 1800.00, 16, 9), (44, 3, 1500.00, 17, 6), (45, 2, 1200.00, 17, 7),
(46, 1, 4500.50, 17, 12), (47, 4, 2500.00, 18, 17), (48, 2, 1800.50, 18, 18), (49, 6, 1200.00, 18, 19), (50, 1, 8500.00, 19, 11),
(51, 2, 2800.00, 19, 14), (52, 1, 1200.00, 19, 21), (53, 5, 1200.50, 20, 1), (54, 2, 3500.00, 20, 3), (55, 3, 1800.00, 20, 9),
(56, 2, 1200.50, 21, 1), (57, 3, 800.00, 21, 16), (58, 1, 3200.00, 21, 13), (59, 1, 8500.00, 22, 11), (60, 2, 4500.50, 22, 12),
(61, 1, 700.00, 22, 10), (62, 2, 8500.00, 23, 11), (63, 1, 2800.00, 23, 14), (64, 4, 950.00, 23, 5), (65, 3, 1200.00, 24, 21),
(66, 2, 1500.00, 24, 22), (67, 1, 2800.00, 24, 23), (68, 2, 4500.00, 25, 26), (69, 1, 8500.00, 25, 11), (70, 3, 2500.00, 25, 17);

INSERT IGNORE INTO payments (id, sale_id, amount, payment_type, payment_date, reference) VALUES
(1,  1,  28501.50, 'CASH',     '2026-02-21 09:35:00', NULL),
(2,  2,  12000.00, 'CARD',     '2026-02-21 11:05:00', 'REF-001'),
(3,  3,  17600.75, 'CASH',     '2026-02-22 10:10:00', NULL),
(4,  5,  20000.00, 'CASH',     '2026-02-23 09:05:00', NULL),
(5,  5,  12400.00, 'CARD',     '2026-02-23 09:07:00', 'REF-002'),
(6,  6,  15800.50, 'TRANSFER', '2026-02-21 10:05:00', 'TRF-001'),
(7,  7,  22400.00, 'CARD',     '2026-02-22 09:35:00', 'REF-003'),
(8,  8,  3100.00,  'CASH',     '2026-02-23 14:05:00', NULL),
(9,  9,  9850.75,  'CASH',     '2026-02-23 15:35:00', NULL),
(10, 10, 45200.00, 'CARD',     '2026-02-24 10:05:00', 'REF-004'),
(11, 11, 7200.00,  'CASH',     '2026-02-21 09:05:00', NULL),
(12, 12, 15300.50, 'CARD',     '2026-02-22 11:05:00', 'REF-005'),
(13, 13, 26800.00, 'CASH',     '2026-02-22 13:05:00', NULL),
(14, 14, 18950.00, 'TRANSFER', '2026-02-23 10:35:00', 'TRF-002'),
(15, 16, 8200.00,  'CASH',     '2026-02-21 10:05:00', NULL),
(16, 17, 15450.50, 'CARD',     '2026-02-22 11:05:00', 'REF-006'),
(17, 18, 23600.00, 'CASH',     '2026-02-23 09:35:00', NULL),
(18, 19, 19200.75, 'CARD',     '2026-02-24 10:05:00', 'REF-007'),
(19, 20, 30800.00, 'CASH',     '2026-02-24 12:05:00', NULL),
(20, 21, 11200.00, 'CASH',     '2026-02-21 09:35:00', NULL),
(21, 22, 18650.50, 'CARD',     '2026-02-22 10:05:00', 'REF-008'),
(22, 23, 25400.00, 'CASH',     '2026-02-23 11:05:00', NULL),
(23, 24, 17300.75, 'TRANSFER', '2026-02-24 09:35:00', 'TRF-003'),
(24, 25, 29600.00, 'CARD',     '2026-02-24 11:05:00', 'REF-009');

INSERT IGNORE INTO stock_transfers (id, source_branch_id, target_branch_id, product_id, quantity, status, requested_by_id, approved_by_id, requested_at, approved_at, completed_at, rejection_reason, version) VALUES
(1, 1, 2, 11, 10, 'COMPLETED', 3, 2, '2026-02-21 07:00:00', '2026-02-21 07:30:00', '2026-02-21 08:00:00', NULL, 0),
(2, 2, 3, 1,  20, 'COMPLETED', 4, 1, '2026-02-21 07:00:00', '2026-02-21 07:30:00', '2026-02-21 08:00:00', NULL, 0),
(3, 3, 4, 5,  15, 'COMPLETED', 4, 2, '2026-02-22 07:00:00', '2026-02-22 07:30:00', '2026-02-22 08:00:00', NULL, 0),
(4, 1, 5, 3,  8,  'COMPLETED', 3, 1, '2026-02-22 07:00:00', '2026-02-22 07:30:00', '2026-02-22 08:00:00', NULL, 0),
(5, 4, 2, 17, 12, 'APPROVED',  3, 2, '2026-02-23 09:00:00', '2026-02-23 09:30:00', NULL, NULL, 0),
(6, 5, 1, 26, 6,  'APPROVED',  4, 1, '2026-02-23 10:00:00', '2026-02-23 10:30:00', NULL, NULL, 0),
(7, 2, 4, 12, 5,  'PENDING',   3, NULL, '2026-02-24 08:00:00', NULL, NULL, NULL, 0),
(8, 3, 5, 7,  10, 'PENDING',   4, NULL, '2026-02-24 09:00:00', NULL, NULL, NULL, 0),
(9, 1, 3, 15, 3,  'REJECTED',  3, 2, '2026-02-23 11:00:00', '2026-02-23 11:30:00', NULL, 'Insufficient demand in target branch', 0),
(10, 5, 2, 20, 4, 'CANCELLED', 4, NULL, '2026-02-24 10:00:00', NULL, NULL, NULL, 0);

ALTER TABLE users AUTO_INCREMENT = 5;
ALTER TABLE audit_logs AUTO_INCREMENT = 1;
ALTER TABLE branch AUTO_INCREMENT = 6;
ALTER TABLE product AUTO_INCREMENT = 31;
ALTER TABLE cash_registers AUTO_INCREMENT = 21;
ALTER TABLE branch_inventory AUTO_INCREMENT = 151;
ALTER TABLE stock_transfers AUTO_INCREMENT = 11;
ALTER TABLE sale AUTO_INCREMENT = 26;
ALTER TABLE sale_detail AUTO_INCREMENT = 71;
ALTER TABLE payments AUTO_INCREMENT = 25;