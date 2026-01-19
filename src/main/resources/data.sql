-- Archivo: src/main/resources/data.sql
-- Datos de prueba para Supermercado API

-- ============================================
-- LIMPIAR TABLAS (opcional, solo si es necesario)
-- ============================================
-- DELETE FROM detalle_venta;
-- DELETE FROM venta;
-- DELETE FROM producto;
-- DELETE FROM sucursal;

-- ============================================
-- INSERTAR SUCURSALES
-- ============================================
INSERT INTO sucursal (id, nombre, direccion) VALUES
                                                 (1, 'Sucursal Centro', 'Avenida Principal 123, Ciudad Central'),
                                                 (2, 'Sucursal Norte', 'Calle Norte 456, Zona Norte'),
                                                 (3, 'Sucursal Sur', 'Avenida Sur 789, Zona Sur'),
                                                 (4, 'Sucursal Este', 'Calle Este 101, Zona Este'),
                                                 (5, 'Sucursal Oeste', 'Avenida Oeste 202, Zona Oeste');

-- ============================================
-- INSERTAR PRODUCTOS (con stock)
-- ============================================
-- Lácteos
INSERT INTO producto (id, nombre, categoria, precio, cantidad) VALUES
                                                                   (1, 'Leche Entera 1L', 'Lácteos', 1200.50, 150),
                                                                   (2, 'Yogurt Natural', 'Lácteos', 800.75, 200),
                                                                   (3, 'Queso Mozzarella 500g', 'Lácteos', 3500.00, 80),
                                                                   (4, 'Mantequilla 250g', 'Lácteos', 1800.25, 120),
                                                                   (5, 'Crema de Leche 200ml', 'Lácteos', 950.00, 100);

-- Frutas y Verduras
INSERT INTO producto (id, nombre, categoria, precio, cantidad) VALUES
                                                                   (6, 'Manzanas 1kg', 'Frutas y Verduras', 1500.00, 300),
                                                                   (7, 'Plátanos 1kg', 'Frutas y Verduras', 1200.00, 250),
                                                                   (8, 'Zanahorias 1kg', 'Frutas y Verduras', 800.50, 180),
                                                                   (9, 'Tomates 1kg', 'Frutas y Verduras', 1800.00, 150),
                                                                   (10, 'Lechuga Unidad', 'Frutas y Verduras', 700.00, 200);

-- Carnes
INSERT INTO producto (id, nombre, categoria, precio, cantidad) VALUES
                                                                   (11, 'Pechuga de Pollo 1kg', 'Carnes', 8500.00, 75),
                                                                   (12, 'Carne Molida 500g', 'Carnes', 4500.50, 60),
                                                                   (13, 'Salchichas 500g', 'Carnes', 3200.00, 90),
                                                                   (14, 'Tocino 250g', 'Carnes', 2800.00, 110),
                                                                   (15, 'Filete de Res 500g', 'Carnes', 12000.00, 40);

-- Bebidas
INSERT INTO producto (id, nombre, categoria, precio, cantidad) VALUES
                                                                   (16, 'Agua Mineral 1.5L', 'Bebidas', 800.00, 300),
                                                                   (17, 'Refresco de Cola 2L', 'Bebidas', 2500.00, 200),
                                                                   (18, 'Jugo de Naranja 1L', 'Bebidas', 1800.50, 150),
                                                                   (19, 'Cerveza Nacional 330ml', 'Bebidas', 1200.00, 180),
                                                                   (20, 'Vino Tinto 750ml', 'Bebidas', 8500.00, 50);

-- Panadería
INSERT INTO producto (id, nombre, categoria, precio, cantidad) VALUES
                                                                   (21, 'Pan Blanco 500g', 'Panadería', 1200.00, 220),
                                                                   (22, 'Pan Integral 500g', 'Panadería', 1500.00, 180),
                                                                   (23, 'Croissants 4 unidades', 'Panadería', 2800.00, 120),
                                                                   (24, 'Galletas de Chocolate', 'Panadería', 950.00, 250),
                                                                   (25, 'Torta de Chocolate', 'Panadería', 12000.00, 20);

-- Limpieza
INSERT INTO producto (id, nombre, categoria, precio, cantidad) VALUES
                                                                   (26, 'Detergente Líquido 1L', 'Limpieza', 4500.00, 80),
                                                                   (27, 'Jabón de Manos', 'Limpieza', 1200.50, 150),
                                                                   (28, 'Papel Higiénico 4 rollos', 'Limpieza', 2800.00, 120),
                                                                   (29, 'Limpiador Multiusos', 'Limpieza', 3200.00, 100),
                                                                   (30, 'Desinfectante 500ml', 'Limpieza', 1800.00, 130);

-- ============================================
-- INSERTAR VENTAS
-- ============================================
-- Ventas para Sucursal Centro (ID: 1)
INSERT INTO venta (id, fecha, estado, total, sucursal_id) VALUES
                                                              (1, '2024-12-01', 'REGISTRADA', 28501.50, 1),
                                                              (2, '2024-12-01', 'REGISTRADA', 12000.00, 1),
                                                              (3, '2024-12-02', 'REGISTRADA', 17600.75, 1),
                                                              (4, '2024-12-02', 'ANULADA', 8500.00, 1),
                                                              (5, '2024-12-03', 'REGISTRADA', 32400.00, 1);

-- Ventas para Sucursal Norte (ID: 2)
INSERT INTO venta (id, fecha, estado, total, sucursal_id) VALUES
                                                              (6, '2024-12-01', 'REGISTRADA', 15800.50, 2),
                                                              (7, '2024-12-02', 'REGISTRADA', 22400.00, 2),
                                                              (8, '2024-12-03', 'REGISTRADA', 3100.00, 2),
                                                              (9, '2024-12-03', 'REGISTRADA', 9850.75, 2),
                                                              (10, '2024-12-04', 'REGISTRADA', 45200.00, 2);

-- Ventas para Sucursal Sur (ID: 3)
INSERT INTO venta (id, fecha, estado, total, sucursal_id) VALUES
                                                              (11, '2024-12-01', 'REGISTRADA', 7200.00, 3),
                                                              (12, '2024-12-02', 'REGISTRADA', 15300.50, 3),
                                                              (13, '2024-12-02', 'REGISTRADA', 26800.00, 3),
                                                              (14, '2024-12-03', 'REGISTRADA', 18950.00, 3),
                                                              (15, '2024-12-04', 'ANULADA', 12500.00, 3);

-- Ventas para Sucursal Este (ID: 4)
INSERT INTO venta (id, fecha, estado, total, sucursal_id) VALUES
                                                              (16, '2024-12-01', 'REGISTRADA', 8200.00, 4),
                                                              (17, '2024-12-02', 'REGISTRADA', 15450.50, 4),
                                                              (18, '2024-12-03', 'REGISTRADA', 23600.00, 4),
                                                              (19, '2024-12-04', 'REGISTRADA', 19200.75, 4),
                                                              (20, '2024-12-04', 'REGISTRADA', 30800.00, 4);

-- Ventas para Sucursal Oeste (ID: 5)
INSERT INTO venta (id, fecha, estado, total, sucursal_id) VALUES
                                                              (21, '2024-12-01', 'REGISTRADA', 11200.00, 5),
                                                              (22, '2024-12-02', 'REGISTRADA', 18650.50, 5),
                                                              (23, '2024-12-03', 'REGISTRADA', 25400.00, 5),
                                                              (24, '2024-12-04', 'REGISTRADA', 17300.75, 5),
                                                              (25, '2024-12-04', 'REGISTRADA', 29600.00, 5);

-- ============================================
-- INSERTAR DETALLES DE VENTA
-- ============================================

-- Detalles para Venta 1 (Sucursal Centro)
INSERT INTO detalle_venta (id, cantidad, precio, venta_id, producto_id) VALUES
                                                                            (1, 2, 1200.50, 1, 1),   -- Leche x2
                                                                            (2, 1, 3500.00, 1, 3),   -- Queso x1
                                                                            (3, 3, 1500.00, 1, 6),   -- Manzanas x3
                                                                            (4, 1, 8500.00, 1, 11),  -- Pollo x1
                                                                            (5, 2, 2500.00, 1, 17);  -- Refresco x2

-- Detalles para Venta 2 (Sucursal Centro) - Solo panadería
INSERT INTO detalle_venta (id, cantidad, precio, venta_id, producto_id) VALUES
    (6, 1, 12000.00, 2, 25); -- Torta de Chocolate

-- Detalles para Venta 3 (Sucursal Centro)
INSERT INTO detalle_venta (id, cantidad, precio, venta_id, producto_id) VALUES
                                                                            (7, 4, 800.75, 3, 2),    -- Yogurt x4
                                                                            (8, 2, 1800.50, 3, 8),   -- Zanahorias x2
                                                                            (9, 1, 4500.50, 3, 12);  -- Carne Molida

-- Detalles para Venta 4 (Sucursal Centro - ANULADA)
INSERT INTO detalle_venta (id, cantidad, precio, venta_id, producto_id) VALUES
    (10, 1, 8500.00, 4, 11); -- Pollo x1

-- Detalles para Venta 5 (Sucursal Centro) - Compra grande
INSERT INTO detalle_venta (id, cantidad, precio, venta_id, producto_id) VALUES
                                                                            (11, 5, 1200.50, 5, 1),   -- Leche x5
                                                                            (12, 2, 3500.00, 5, 3),   -- Queso x2
                                                                            (13, 1, 12000.00, 5, 15); -- Filete de Res

-- Detalles para Venta 6 (Sucursal Norte)
INSERT INTO detalle_venta (id, cantidad, precio, venta_id, producto_id) VALUES
                                                                            (14, 2, 800.00, 6, 16),   -- Agua x2
                                                                            (15, 3, 1200.00, 6, 7),   -- Plátanos x3
                                                                            (16, 1, 8500.00, 6, 11);  -- Pollo x1

-- Detalles para Venta 7 (Sucursal Norte)
INSERT INTO detalle_venta (id, cantidad, precio, venta_id, producto_id) VALUES
                                                                            (17, 1, 4500.50, 7, 12),  -- Carne Molida
                                                                            (18, 2, 2800.00, 7, 23),  -- Croissants
                                                                            (19, 3, 950.00, 7, 24);   -- Galletas

-- Detalles para Venta 8 (Sucursal Norte) - Pequeña compra
INSERT INTO detalle_venta (id, cantidad, precio, venta_id, producto_id) VALUES
                                                                            (20, 2, 950.00, 8, 5),    -- Crema x2
                                                                            (21, 1, 1200.00, 8, 22);  -- Pan Integral

-- Detalles para Venta 9 (Sucursal Norte)
INSERT INTO detalle_venta (id, cantidad, precio, venta_id, producto_id) VALUES
                                                                            (22, 1, 8500.00, 9, 11),  -- Pollo
                                                                            (23, 2, 675.00, 9, 10);   -- Lechuga (precio ajustado)

-- Detalles para Venta 10 (Sucursal Norte) - Compra grande de carnes
INSERT INTO detalle_venta (id, cantidad, precio, venta_id, producto_id) VALUES
                                                                            (24, 2, 8500.00, 10, 11), -- Pollo x2
                                                                            (25, 1, 12000.00, 10, 15),-- Filete de Res
                                                                            (26, 3, 4500.50, 10, 12), -- Carne Molida x3
                                                                            (27, 2, 3200.00, 10, 13); -- Salchichas x2

-- Detalles para Venta 11 (Sucursal Sur)
INSERT INTO detalle_venta (id, cantidad, precio, venta_id, producto_id) VALUES
                                                                            (28, 3, 1200.50, 11, 1),  -- Leche x3
                                                                            (29, 2, 800.00, 11, 16);  -- Agua x2

-- Detalles para Venta 12 (Sucursal Sur)
INSERT INTO detalle_venta (id, cantidad, precio, venta_id, producto_id) VALUES
                                                                            (30, 1, 8500.00, 12, 11), -- Pollo
                                                                            (31, 4, 950.00, 12, 5),   -- Crema x4
                                                                            (32, 2, 800.50, 12, 8);   -- Zanahorias x2

-- Detalles para Venta 13 (Sucursal Sur) - Limpieza
INSERT INTO detalle_venta (id, cantidad, precio, venta_id, producto_id) VALUES
                                                                            (33, 1, 4500.00, 13, 26), -- Detergente
                                                                            (34, 2, 2800.00, 13, 28), -- Papel Higiénico
                                                                            (35, 1, 3200.00, 13, 29), -- Limpiador
                                                                            (36, 3, 1200.50, 13, 27); -- Jabón x3

-- Detalles para Venta 14 (Sucursal Sur)
INSERT INTO detalle_venta (id, cantidad, precio, venta_id, producto_id) VALUES
                                                                            (37, 2, 3500.00, 14, 3),  -- Queso x2
                                                                            (38, 1, 1800.25, 14, 4),  -- Mantequilla
                                                                            (39, 3, 800.75, 14, 2);   -- Yogurt x3

-- Detalles para Venta 15 (Sucursal Sur - ANULADA)
INSERT INTO detalle_venta (id, cantidad, precio, venta_id, producto_id) VALUES
    (40, 1, 12500.00, 15, 15); -- Filete de Res

-- Detalles para Venta 16 (Sucursal Este)
INSERT INTO detalle_venta (id, cantidad, precio, venta_id, producto_id) VALUES
                                                                            (41, 2, 1200.50, 16, 1),  -- Leche x2
                                                                            (42, 1, 3500.00, 16, 3),  -- Queso
                                                                            (43, 1, 1800.00, 16, 9);  -- Tomates

-- Detalles para Venta 17 (Sucursal Este)
INSERT INTO detalle_venta (id, cantidad, precio, venta_id, producto_id) VALUES
                                                                            (44, 3, 1500.00, 17, 6),  -- Manzanas x3
                                                                            (45, 2, 1200.00, 17, 7),  -- Plátanos x2
                                                                            (46, 1, 4500.50, 17, 12); -- Carne Molida

-- Detalles para Venta 18 (Sucursal Este) - Bebidas
INSERT INTO detalle_venta (id, cantidad, precio, venta_id, producto_id) VALUES
                                                                            (47, 4, 2500.00, 18, 17), -- Refresco x4
                                                                            (48, 2, 1800.50, 18, 18), -- Jugo x2
                                                                            (49, 6, 1200.00, 18, 19); -- Cerveza x6

-- Detalles para Venta 19 (Sucursal Este)
INSERT INTO detalle_venta (id, cantidad, precio, venta_id, producto_id) VALUES
                                                                            (50, 1, 8500.00, 19, 11), -- Pollo
                                                                            (51, 2, 2800.00, 19, 14), -- Tocino x2
                                                                            (52, 1, 1200.00, 19, 21); -- Pan Blanco

-- Detalles para Venta 20 (Sucursal Este) - Variedad
INSERT INTO detalle_venta (id, cantidad, precio, venta_id, producto_id) VALUES
                                                                            (53, 5, 1200.50, 20, 1),  -- Leche x5 (producto más vendido)
                                                                            (54, 2, 3500.00, 20, 3),  -- Queso x2
                                                                            (55, 3, 1800.00, 20, 9);  -- Tomates x3

-- Detalles para Venta 21 (Sucursal Oeste)
INSERT INTO detalle_venta (id, cantidad, precio, venta_id, producto_id) VALUES
                                                                            (56, 2, 1200.50, 21, 1),  -- Leche x2
                                                                            (57, 3, 800.00, 21, 16),  -- Agua x3
                                                                            (58, 1, 3200.00, 21, 13); -- Salchichas

-- Detalles para Venta 22 (Sucursal Oeste)
INSERT INTO detalle_venta (id, cantidad, precio, venta_id, producto_id) VALUES
                                                                            (59, 1, 8500.00, 22, 11), -- Pollo
                                                                            (60, 2, 4500.50, 22, 12), -- Carne Molida x2
                                                                            (61, 1, 700.00, 22, 10);  -- Lechuga

-- Detalles para Venta 23 (Sucursal Oeste)
INSERT INTO detalle_venta (id, cantidad, precio, venta_id, producto_id) VALUES
                                                                            (62, 2, 8500.00, 23, 11), -- Pollo x2
                                                                            (63, 1, 2800.00, 23, 14), -- Tocino
                                                                            (64, 4, 950.00, 23, 5);   -- Crema x4

-- Detalles para Venta 24 (Sucursal Oeste) - Panadería
INSERT INTO detalle_venta (id, cantidad, precio, venta_id, producto_id) VALUES
                                                                            (65, 3, 1200.00, 24, 21), -- Pan Blanco x3
                                                                            (66, 2, 1500.00, 24, 22), -- Pan Integral x2
                                                                            (67, 1, 2800.00, 24, 23); -- Croissants

-- Detalles para Venta 25 (Sucursal Oeste) - Limpieza y bebidas
INSERT INTO detalle_venta (id, cantidad, precio, venta_id, producto_id) VALUES
                                                                            (68, 2, 4500.00, 25, 26), -- Detergente x2
                                                                            (69, 1, 8500.00, 25, 11), -- Pollo
                                                                            (70, 3, 2500.00, 25, 17); -- Refresco x3

-- ============================================
-- RESET DE SECUENCIAS (opcional, pero recomendado)
-- ============================================
ALTER TABLE sucursal ALTER COLUMN id RESTART WITH 26;
ALTER TABLE producto ALTER COLUMN id RESTART WITH 31;
ALTER TABLE venta ALTER COLUMN id RESTART WITH 26;
ALTER TABLE detalle_venta ALTER COLUMN id RESTART WITH 71;