INSERT INTO
    branch (id, name, address)
VALUES
    (
        1,
        'Central Branch',
        '123 Main Avenue, Central City'
    ),
    (2, 'North Branch', '456 North Street, North Zone'),
    (3, 'South Branch', '789 South Avenue, South Zone'),
    (4, 'East Branch', '101 East Street, East Zone'),
    (5, 'West Branch', '202 West Avenue, West Zone');

INSERT INTO
    product (id, name, category, price, quantity)
VALUES
    (1, 'Whole Milk 1L', 'Dairy', 1200.50, 150),
    (2, 'Natural Yogurt', 'Dairy', 800.75, 200),
    (3, 'Mozzarella Cheese 500g', 'Dairy', 3500.00, 80),
    (4, 'Butter 250g', 'Dairy', 1800.25, 120),
    (5, 'Heavy Cream 200ml', 'Dairy', 950.00, 100);

INSERT INTO
    product (id, name, category, price, quantity)
VALUES
    (
        6,
        'Apples 1kg',
        'Fruits and Vegetables',
        1500.00,
        300
    ),
    (
        7,
        'Bananas 1kg',
        'Fruits and Vegetables',
        1200.00,
        250
    ),
    (
        8,
        'Carrots 1kg',
        'Fruits and Vegetables',
        800.50,
        180
    ),
    (
        9,
        'Tomatoes 1kg',
        'Fruits and Vegetables',
        1800.00,
        150
    ),
    (
        10,
        'Lettuce Unit',
        'Fruits and Vegetables',
        700.00,
        200
    );

INSERT INTO
    product (id, name, category, price, quantity)
VALUES
    (11, 'Chicken Breast 1kg', 'Meats', 8500.00, 75),
    (12, 'Ground Beef 500g', 'Meats', 4500.50, 60),
    (13, 'Sausages 500g', 'Meats', 3200.00, 90),
    (14, 'Bacon 250g', 'Meats', 2800.00, 110),
    (15, 'Beef Steak 500g', 'Meats', 12000.00, 40);

INSERT INTO
    product (id, name, category, price, quantity)
VALUES
    (
        16,
        'Mineral Water 1.5L',
        'Beverages',
        800.00,
        300
    ),
    (17, 'Cola Soda 2L', 'Beverages', 2500.00, 200),
    (18, 'Orange Juice 1L', 'Beverages', 1800.50, 150),
    (
        19,
        'National Beer 330ml',
        'Beverages',
        1200.00,
        180
    ),
    (20, 'Red Wine 750ml', 'Beverages', 8500.00, 50);

INSERT INTO
    product (id, name, category, price, quantity)
VALUES
    (21, 'White Bread 500g', 'Bakery', 1200.00, 220),
    (
        22,
        'Whole Wheat Bread 500g',
        'Bakery',
        1500.00,
        180
    ),
    (23, 'Croissants 4 units', 'Bakery', 2800.00, 120),
    (24, 'Chocolate Cookies', 'Bakery', 950.00, 250),
    (25, 'Chocolate Cake', 'Bakery', 12000.00, 20);

INSERT INTO
    product (id, name, category, price, quantity)
VALUES
    (
        26,
        'Liquid Detergent 1L',
        'Cleaning',
        4500.00,
        80
    ),
    (27, 'Hand Soap', 'Cleaning', 1200.50, 150),
    (
        28,
        'Toilet Paper 4 rolls',
        'Cleaning',
        2800.00,
        120
    ),
    (
        29,
        'All-purpose Cleaner',
        'Cleaning',
        3200.00,
        100
    ),
    (
        30,
        'Disinfectant 500ml',
        'Cleaning',
        1800.00,
        130
    );

INSERT INTO
    Sale (id, date, status, total, branch_id)
VALUES
    (1, '2024-12-01', 'REGISTERED', 28501.50, 1),
    (2, '2024-12-01', 'REGISTERED', 12000.00, 1),
    (3, '2024-12-02', 'REGISTERED', 17600.75, 1),
    (4, '2024-12-02', 'CANCELLED', 8500.00, 1),
    (5, '2024-12-03', 'REGISTERED', 32400.00, 1);

INSERT INTO
    Sale (id, date, status, total, branch_id)
VALUES
    (6, '2024-12-01', 'REGISTERED', 15800.50, 2),
    (7, '2024-12-02', 'REGISTERED', 22400.00, 2),
    (8, '2024-12-03', 'REGISTERED', 3100.00, 2),
    (9, '2024-12-03', 'REGISTERED', 9850.75, 2),
    (10, '2024-12-04', 'REGISTERED', 45200.00, 2);

INSERT INTO
    Sale (id, date, status, total, branch_id)
VALUES
    (11, '2024-12-01', 'REGISTERED', 7200.00, 3),
    (12, '2024-12-02', 'REGISTERED', 15300.50, 3),
    (13, '2024-12-02', 'REGISTERED', 26800.00, 3),
    (14, '2024-12-03', 'REGISTERED', 18950.00, 3),
    (15, '2024-12-04', 'CANCELLED', 12500.00, 3);

INSERT INTO
    Sale (id, date, status, total, branch_id)
VALUES
    (16, '2024-12-01', 'REGISTERED', 8200.00, 4),
    (17, '2024-12-02', 'REGISTERED', 15450.50, 4),
    (18, '2024-12-03', 'REGISTERED', 23600.00, 4),
    (19, '2024-12-04', 'REGISTERED', 19200.75, 4),
    (20, '2024-12-04', 'REGISTERED', 30800.00, 4);

INSERT INTO
    Sale (id, date, status, total, branch_id)
VALUES
    (21, '2024-12-01', 'REGISTERED', 11200.00, 5),
    (22, '2024-12-02', 'REGISTERED', 18650.50, 5),
    (23, '2024-12-03', 'REGISTERED', 25400.00, 5),
    (24, '2024-12-04', 'REGISTERED', 17300.75, 5),
    (25, '2024-12-04', 'REGISTERED', 29600.00, 5);

INSERT INTO
    sale_detail (id, quantity, price, sale_id, product_id)
VALUES
    (1, 2, 1200.50, 1, 1),
    (2, 1, 3500.00, 1, 3),
    (3, 3, 1500.00, 1, 6),
    (4, 1, 8500.00, 1, 11),
    (5, 2, 2500.00, 1, 17);

INSERT INTO
    sale_detail (id, quantity, price, sale_id, product_id)
VALUES
    (6, 1, 12000.00, 2, 25);

INSERT INTO
    sale_detail (id, quantity, price, sale_id, product_id)
VALUES
    (7, 4, 800.75, 3, 2),
    (8, 2, 1800.50, 3, 8),
    (9, 1, 4500.50, 3, 12);

INSERT INTO
    sale_detail (id, quantity, price, sale_id, product_id)
VALUES
    (10, 1, 8500.00, 4, 11);

INSERT INTO
    sale_detail (id, quantity, price, sale_id, product_id)
VALUES
    (11, 5, 1200.50, 5, 1),
    (12, 2, 3500.00, 5, 3),
    (13, 1, 12000.00, 5, 15);

INSERT INTO
    sale_detail (id, quantity, price, sale_id, product_id)
VALUES
    (14, 2, 800.00, 6, 16),
    (15, 3, 1200.00, 6, 7),
    (16, 1, 8500.00, 6, 11);

INSERT INTO
    sale_detail (id, quantity, price, sale_id, product_id)
VALUES
    (17, 1, 4500.50, 7, 12),
    (18, 2, 2800.00, 7, 23),
    (19, 3, 950.00, 7, 24);

INSERT INTO
    sale_detail (id, quantity, price, sale_id, product_id)
VALUES
    (20, 2, 950.00, 8, 5),
    (21, 1, 1200.00, 8, 22);

INSERT INTO
    sale_detail (id, quantity, price, sale_id, product_id)
VALUES
    (22, 1, 8500.00, 9, 11),
    (23, 2, 675.00, 9, 10);

INSERT INTO
    sale_detail (id, quantity, price, sale_id, product_id)
VALUES
    (24, 2, 8500.00, 10, 11),
    (25, 1, 12000.00, 10, 15),
    (26, 3, 4500.50, 10, 12),
    (27, 2, 3200.00, 10, 13);

INSERT INTO
    sale_detail (id, quantity, price, sale_id, product_id)
VALUES
    (28, 3, 1200.50, 11, 1),
    (29, 2, 800.00, 11, 16);

INSERT INTO
    sale_detail (id, quantity, price, sale_id, product_id)
VALUES
    (30, 1, 8500.00, 12, 11),
    (31, 4, 950.00, 12, 5),
    (32, 2, 800.50, 12, 8);

INSERT INTO
    sale_detail (id, quantity, price, sale_id, product_id)
VALUES
    (33, 1, 4500.00, 13, 26),
    (34, 2, 2800.00, 13, 28),
    (35, 1, 3200.00, 13, 29),
    (36, 3, 1200.50, 13, 27);

INSERT INTO
    sale_detail (id, quantity, price, sale_id, product_id)
VALUES
    (37, 2, 3500.00, 14, 3),
    (38, 1, 1800.25, 14, 4),
    (39, 3, 800.75, 14, 2);

INSERT INTO
    sale_detail (id, quantity, price, sale_id, product_id)
VALUES
    (40, 1, 12500.00, 15, 15);

INSERT INTO
    sale_detail (id, quantity, price, sale_id, product_id)
VALUES
    (41, 2, 1200.50, 16, 1),
    (42, 1, 3500.00, 16, 3),
    (43, 1, 1800.00, 16, 9);

INSERT INTO
    sale_detail (id, quantity, price, sale_id, product_id)
VALUES
    (44, 3, 1500.00, 17, 6),
    (45, 2, 1200.00, 17, 7),
    (46, 1, 4500.50, 17, 12);

INSERT INTO
    sale_detail (id, quantity, price, sale_id, product_id)
VALUES
    (47, 4, 2500.00, 18, 17),
    (48, 2, 1800.50, 18, 18),
    (49, 6, 1200.00, 18, 19);

INSERT INTO
    sale_detail (id, quantity, price, sale_id, product_id)
VALUES
    (50, 1, 8500.00, 19, 11),
    (51, 2, 2800.00, 19, 14),
    (52, 1, 1200.00, 19, 21);

INSERT INTO
    sale_detail (id, quantity, price, sale_id, product_id)
VALUES
    (53, 5, 1200.50, 20, 1),
    (54, 2, 3500.00, 20, 3),
    (55, 3, 1800.00, 20, 9);

INSERT INTO
    sale_detail (id, quantity, price, sale_id, product_id)
VALUES
    (56, 2, 1200.50, 21, 1),
    (57, 3, 800.00, 21, 16),
    (58, 1, 3200.00, 21, 13);

INSERT INTO
    sale_detail (id, quantity, price, sale_id, product_id)
VALUES
    (59, 1, 8500.00, 22, 11),
    (60, 2, 4500.50, 22, 12),
    (61, 1, 700.00, 22, 10);

INSERT INTO
    sale_detail (id, quantity, price, sale_id, product_id)
VALUES
    (62, 2, 8500.00, 23, 11),
    (63, 1, 2800.00, 23, 14),
    (64, 4, 950.00, 23, 5);

INSERT INTO
    sale_detail (id, quantity, price, sale_id, product_id)
VALUES
    (65, 3, 1200.00, 24, 21),
    (66, 2, 1500.00, 24, 22),
    (67, 1, 2800.00, 24, 23);

INSERT INTO
    sale_detail (id, quantity, price, sale_id, product_id)
VALUES
    (68, 2, 4500.00, 25, 26),
    (69, 1, 8500.00, 25, 11),
    (70, 3, 2500.00, 25, 17);

ALTER TABLE branch
ALTER COLUMN id
RESTART WITH 26;

ALTER TABLE product
ALTER COLUMN id
RESTART WITH 31;

ALTER TABLE Sale
ALTER COLUMN id
RESTART WITH 26;

ALTER TABLE sale_detail
ALTER COLUMN id
RESTART WITH 71;