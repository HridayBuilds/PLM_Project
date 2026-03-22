-- Ecova PLM Database Seed Data
-- This script inserts test data for all roles
-- Run this AFTER Hibernate creates the schema (ddl-auto=update)

-- =====================================================
-- USERS (BCrypt hash for 'Password@123')
-- =====================================================
-- Password: Password@123 (meets validation: uppercase, lowercase, number, special char)
-- Password hash: $2b$10$iPKjzxzNTP5NJzz4xLKcke31Da81nkb6K2fsls3Nm8zOqfG5LSI6y

INSERT INTO users (id, login_id, email, password_hash, first_name, last_name, role, status, is_verified, is_active, created_at, updated_at) VALUES
-- Admin User
(UUID_TO_BIN('a1111111-1111-1111-1111-111111111111'), 'ECVADMIN01', 'admin@ecova.com', '$2b$10$iPKjzxzNTP5NJzz4xLKcke31Da81nkb6K2fsls3Nm8zOqfG5LSI6y', 'Admin', 'User', 'ADMIN', 'ACTIVE', TRUE, TRUE, NOW(), NOW()),
-- Engineering Users
(UUID_TO_BIN('a2222222-2222-2222-2222-222222222222'), 'ECVENG001', 'john.engineer@ecova.com', '$2b$10$iPKjzxzNTP5NJzz4xLKcke31Da81nkb6K2fsls3Nm8zOqfG5LSI6y', 'John', 'Engineer', 'ENGINEERING_USER', 'ACTIVE', TRUE, TRUE, NOW(), NOW()),
(UUID_TO_BIN('a3333333-3333-3333-3333-333333333333'), 'ECVENG002', 'sarah.design@ecova.com', '$2b$10$iPKjzxzNTP5NJzz4xLKcke31Da81nkb6K2fsls3Nm8zOqfG5LSI6y', 'Sarah', 'Designer', 'ENGINEERING_USER', 'ACTIVE', TRUE, TRUE, NOW(), NOW()),
-- Approvers
(UUID_TO_BIN('a4444444-4444-4444-4444-444444444444'), 'ECVAPPR01', 'mike.approver@ecova.com', '$2b$10$iPKjzxzNTP5NJzz4xLKcke31Da81nkb6K2fsls3Nm8zOqfG5LSI6y', 'Mike', 'Approver', 'APPROVER', 'ACTIVE', TRUE, TRUE, NOW(), NOW()),
(UUID_TO_BIN('a5555555-5555-5555-5555-555555555555'), 'ECVAPPR02', 'lisa.manager@ecova.com', '$2b$10$iPKjzxzNTP5NJzz4xLKcke31Da81nkb6K2fsls3Nm8zOqfG5LSI6y', 'Lisa', 'Manager', 'APPROVER', 'ACTIVE', TRUE, TRUE, NOW(), NOW()),
-- Operations Users
(UUID_TO_BIN('a6666666-6666-6666-6666-666666666666'), 'ECVOPS001', 'tom.operations@ecova.com', '$2b$10$iPKjzxzNTP5NJzz4xLKcke31Da81nkb6K2fsls3Nm8zOqfG5LSI6y', 'Tom', 'Operations', 'OPERATIONS_USER', 'ACTIVE', TRUE, TRUE, NOW(), NOW());

-- =====================================================
-- ECO STAGES (Workflow stages for ECO approval)
-- =====================================================
INSERT INTO eco_stages (id, name, sequence, is_final, created_at) VALUES
(UUID_TO_BIN('b1111111-1111-1111-1111-111111111111'), 'New', 1, FALSE, NOW()),
(UUID_TO_BIN('b2222222-2222-2222-2222-222222222222'), 'In Progress', 2, FALSE, NOW()),
(UUID_TO_BIN('b3333333-3333-3333-3333-333333333333'), 'Approval', 3, FALSE, NOW()),
(UUID_TO_BIN('b4444444-4444-4444-4444-444444444444'), 'Done', 4, TRUE, NOW()),
(UUID_TO_BIN('b5555555-5555-5555-5555-555555555555'), 'Cancelled', 5, TRUE, NOW());

-- =====================================================
-- PRODUCTS
-- =====================================================
INSERT INTO products (id, name, description, sale_price, cost_price, version, status, created_by, created_at, updated_at) VALUES
-- Main Products
(UUID_TO_BIN('c1111111-1111-1111-1111-111111111111'), 'Electric Motor Assembly', 'High-efficiency electric motor for industrial applications', 25000.00, 15000.00, 2, 'ACTIVE', UUID_TO_BIN('a2222222-2222-2222-2222-222222222222'), NOW(), NOW()),
(UUID_TO_BIN('c2222222-2222-2222-2222-222222222222'), 'Control Panel Unit', 'Industrial control panel with PLC integration', 45000.00, 28000.00, 1, 'ACTIVE', UUID_TO_BIN('a2222222-2222-2222-2222-222222222222'), NOW(), NOW()),
(UUID_TO_BIN('c3333333-3333-3333-3333-333333333333'), 'Hydraulic Pump System', 'Heavy-duty hydraulic pump for manufacturing', 85000.00, 55000.00, 3, 'ACTIVE', UUID_TO_BIN('a3333333-3333-3333-3333-333333333333'), NOW(), NOW()),
(UUID_TO_BIN('c4444444-4444-4444-4444-444444444444'), 'Sensor Module Kit', 'Multi-sensor module for automation systems', 12000.00, 7500.00, 1, 'ACTIVE', UUID_TO_BIN('a3333333-3333-3333-3333-333333333333'), NOW(), NOW()),
-- Component Products (used in BOMs)
(UUID_TO_BIN('d1111111-1111-1111-1111-111111111111'), 'Copper Winding Coil', 'High-grade copper coil for motors', 3500.00, 2000.00, 1, 'ACTIVE', UUID_TO_BIN('a2222222-2222-2222-2222-222222222222'), NOW(), NOW()),
(UUID_TO_BIN('d2222222-2222-2222-2222-222222222222'), 'Steel Housing', 'Precision machined steel housing', 4500.00, 2800.00, 1, 'ACTIVE', UUID_TO_BIN('a2222222-2222-2222-2222-222222222222'), NOW(), NOW()),
(UUID_TO_BIN('d3333333-3333-3333-3333-333333333333'), 'Ball Bearing Set', 'Industrial grade ball bearings', 1200.00, 650.00, 1, 'ACTIVE', UUID_TO_BIN('a2222222-2222-2222-2222-222222222222'), NOW(), NOW()),
(UUID_TO_BIN('d4444444-4444-4444-4444-444444444444'), 'PLC Controller', 'Programmable logic controller', 18000.00, 12000.00, 2, 'ACTIVE', UUID_TO_BIN('a3333333-3333-3333-3333-333333333333'), NOW(), NOW()),
(UUID_TO_BIN('d5555555-5555-5555-5555-555555555555'), 'Touch Display Panel', '7-inch industrial touch display', 8500.00, 5500.00, 1, 'ACTIVE', UUID_TO_BIN('a3333333-3333-3333-3333-333333333333'), NOW(), NOW()),
(UUID_TO_BIN('d6666666-6666-6666-6666-666666666666'), 'Wiring Harness', 'Pre-assembled wiring harness', 2200.00, 1400.00, 1, 'ACTIVE', UUID_TO_BIN('a2222222-2222-2222-2222-222222222222'), NOW(), NOW());

-- =====================================================
-- BOMS (Bill of Materials)
-- =====================================================
INSERT INTO boms (id, product_id, reference, version, status, created_by, created_at, updated_at) VALUES
(UUID_TO_BIN('e1111111-1111-1111-1111-111111111111'), UUID_TO_BIN('c1111111-1111-1111-1111-111111111111'), 'BOM-MOTOR-001', 2, 'ACTIVE', UUID_TO_BIN('a2222222-2222-2222-2222-222222222222'), NOW(), NOW()),
(UUID_TO_BIN('e2222222-2222-2222-2222-222222222222'), UUID_TO_BIN('c2222222-2222-2222-2222-222222222222'), 'BOM-PANEL-001', 1, 'ACTIVE', UUID_TO_BIN('a2222222-2222-2222-2222-222222222222'), NOW(), NOW()),
(UUID_TO_BIN('e3333333-3333-3333-3333-333333333333'), UUID_TO_BIN('c3333333-3333-3333-3333-333333333333'), 'BOM-PUMP-001', 3, 'ACTIVE', UUID_TO_BIN('a3333333-3333-3333-3333-333333333333'), NOW(), NOW());

-- =====================================================
-- BOM COMPONENTS
-- =====================================================
INSERT INTO bom_components (id, bom_id, component_product_id, quantity, unit) VALUES
-- Motor Assembly BOM components
(UUID_TO_BIN('f1111111-1111-1111-1111-111111111111'), UUID_TO_BIN('e1111111-1111-1111-1111-111111111111'), UUID_TO_BIN('d1111111-1111-1111-1111-111111111111'), 4.0000, 'pcs'),
(UUID_TO_BIN('f2222222-2222-2222-2222-222222222222'), UUID_TO_BIN('e1111111-1111-1111-1111-111111111111'), UUID_TO_BIN('d2222222-2222-2222-2222-222222222222'), 1.0000, 'pcs'),
(UUID_TO_BIN('f3333333-3333-3333-3333-333333333333'), UUID_TO_BIN('e1111111-1111-1111-1111-111111111111'), UUID_TO_BIN('d3333333-3333-3333-3333-333333333333'), 2.0000, 'sets'),
-- Control Panel BOM components
(UUID_TO_BIN('f4444444-4444-4444-4444-444444444444'), UUID_TO_BIN('e2222222-2222-2222-2222-222222222222'), UUID_TO_BIN('d4444444-4444-4444-4444-444444444444'), 1.0000, 'pcs'),
(UUID_TO_BIN('f5555555-5555-5555-5555-555555555555'), UUID_TO_BIN('e2222222-2222-2222-2222-222222222222'), UUID_TO_BIN('d5555555-5555-5555-5555-555555555555'), 1.0000, 'pcs'),
(UUID_TO_BIN('f6666666-6666-6666-6666-666666666666'), UUID_TO_BIN('e2222222-2222-2222-2222-222222222222'), UUID_TO_BIN('d6666666-6666-6666-6666-666666666666'), 3.0000, 'pcs');

-- =====================================================
-- BOM OPERATIONS
-- =====================================================
INSERT INTO bom_operations (id, bom_id, name, work_center, expected_duration_minutes, sequence) VALUES
-- Motor Assembly operations
(UUID_TO_BIN('07111111-1111-1111-1111-111111111111'), UUID_TO_BIN('e1111111-1111-1111-1111-111111111111'), 'Coil Winding', 'Winding Station 1', 45.00, 1),
(UUID_TO_BIN('07222222-2222-2222-2222-222222222222'), UUID_TO_BIN('e1111111-1111-1111-1111-111111111111'), 'Housing Assembly', 'Assembly Line A', 30.00, 2),
(UUID_TO_BIN('07333333-3333-3333-3333-333333333333'), UUID_TO_BIN('e1111111-1111-1111-1111-111111111111'), 'Bearing Installation', 'Assembly Line A', 15.00, 3),
(UUID_TO_BIN('07444444-4444-4444-4444-444444444444'), UUID_TO_BIN('e1111111-1111-1111-1111-111111111111'), 'Quality Testing', 'QC Station', 20.00, 4),
-- Control Panel operations
(UUID_TO_BIN('07555555-5555-5555-5555-555555555555'), UUID_TO_BIN('e2222222-2222-2222-2222-222222222222'), 'Panel Mounting', 'Electronics Bay', 25.00, 1),
(UUID_TO_BIN('07666666-6666-6666-6666-666666666666'), UUID_TO_BIN('e2222222-2222-2222-2222-222222222222'), 'Wiring Integration', 'Electronics Bay', 40.00, 2),
(UUID_TO_BIN('07777777-7777-7777-7777-777777777777'), UUID_TO_BIN('e2222222-2222-2222-2222-222222222222'), 'PLC Programming', 'Programming Station', 60.00, 3);

-- =====================================================
-- ECOs (Engineering Change Orders)
-- =====================================================
INSERT INTO ecos (id, title, description, eco_type, product_id, bom_id, created_by, current_stage_id, version_update, effective_date, status, created_at, updated_at) VALUES
-- Draft ECO
(UUID_TO_BIN('08111111-1111-1111-1111-111111111111'), 'Motor Efficiency Upgrade', 'Upgrading copper coils for better efficiency', 'BOM', UUID_TO_BIN('c1111111-1111-1111-1111-111111111111'), UUID_TO_BIN('e1111111-1111-1111-1111-111111111111'), UUID_TO_BIN('a2222222-2222-2222-2222-222222222222'), UUID_TO_BIN('b1111111-1111-1111-1111-111111111111'), TRUE, '2026-04-01', 'DRAFT', NOW(), NOW()),
-- In Progress ECO
(UUID_TO_BIN('08222222-2222-2222-2222-222222222222'), 'Control Panel Display Update', 'Replacing 7-inch display with 10-inch version', 'BOM', UUID_TO_BIN('c2222222-2222-2222-2222-222222222222'), UUID_TO_BIN('e2222222-2222-2222-2222-222222222222'), UUID_TO_BIN('a3333333-3333-3333-3333-333333333333'), UUID_TO_BIN('b2222222-2222-2222-2222-222222222222'), FALSE, '2026-04-15', 'IN_PROGRESS', NOW(), NOW()),
-- Approval Stage ECO
(UUID_TO_BIN('08333333-3333-3333-3333-333333333333'), 'Pump Seal Improvement', 'Upgrading hydraulic seals for better durability', 'PRODUCT', UUID_TO_BIN('c3333333-3333-3333-3333-333333333333'), NULL, UUID_TO_BIN('a2222222-2222-2222-2222-222222222222'), UUID_TO_BIN('b3333333-3333-3333-3333-333333333333'), TRUE, '2026-03-25', 'IN_PROGRESS', NOW(), NOW()),
-- Completed ECO
(UUID_TO_BIN('08444444-4444-4444-4444-444444444444'), 'Sensor Module Calibration Update', 'Updated calibration parameters for better accuracy', 'PRODUCT', UUID_TO_BIN('c4444444-4444-4444-4444-444444444444'), NULL, UUID_TO_BIN('a3333333-3333-3333-3333-333333333333'), UUID_TO_BIN('b4444444-4444-4444-4444-444444444444'), FALSE, '2026-03-10', 'APPROVED', NOW(), NOW()),
-- Cancelled ECO
(UUID_TO_BIN('08555555-5555-5555-5555-555555555555'), 'Bearing Supplier Change', 'Switching to alternate bearing supplier', 'BOM', UUID_TO_BIN('c1111111-1111-1111-1111-111111111111'), UUID_TO_BIN('e1111111-1111-1111-1111-111111111111'), UUID_TO_BIN('a2222222-2222-2222-2222-222222222222'), UUID_TO_BIN('b5555555-5555-5555-5555-555555555555'), FALSE, NULL, 'CANCELLED', NOW(), NOW());

-- =====================================================
-- ECO APPROVAL RULES (Who can approve at each stage)
-- =====================================================
INSERT INTO eco_approval_rules (id, eco_stage_id, approver_user_id, category, created_at) VALUES
-- Approval stage rules
(UUID_TO_BIN('09111111-1111-1111-1111-111111111111'), UUID_TO_BIN('b3333333-3333-3333-3333-333333333333'), UUID_TO_BIN('a4444444-4444-4444-4444-444444444444'), 'REQUIRED', NOW()),
(UUID_TO_BIN('09222222-2222-2222-2222-222222222222'), UUID_TO_BIN('b3333333-3333-3333-3333-333333333333'), UUID_TO_BIN('a5555555-5555-5555-5555-555555555555'), 'OPTIONAL', NOW()),
-- Done stage rules
(UUID_TO_BIN('09333333-3333-3333-3333-333333333333'), UUID_TO_BIN('b4444444-4444-4444-4444-444444444444'), UUID_TO_BIN('a1111111-1111-1111-1111-111111111111'), 'REQUIRED', NOW());

-- =====================================================
-- ECO APPROVALS (Actual approval records)
-- =====================================================
INSERT INTO eco_approvals (id, eco_id, approver_user_id, stage_id, decision, comments, approved_at) VALUES
-- Approval for completed ECO
(UUID_TO_BIN('0a111111-1111-1111-1111-111111111111'), UUID_TO_BIN('08444444-4444-4444-4444-444444444444'), UUID_TO_BIN('a4444444-4444-4444-4444-444444444444'), UUID_TO_BIN('b3333333-3333-3333-3333-333333333333'), 'APPROVED', 'Calibration changes verified and approved', NOW()),
(UUID_TO_BIN('0a222222-2222-2222-2222-222222222222'), UUID_TO_BIN('08444444-4444-4444-4444-444444444444'), UUID_TO_BIN('a1111111-1111-1111-1111-111111111111'), UUID_TO_BIN('b4444444-4444-4444-4444-444444444444'), 'APPROVED', 'Final approval granted', NOW());

-- =====================================================
-- ECO BOM CHANGES
-- =====================================================
INSERT INTO eco_bom_changes (id, eco_id, bom_component_id, old_quantity, new_quantity, change_type, unit, new_component_product_id) VALUES
-- Motor Efficiency Upgrade - increasing coil quantity
(UUID_TO_BIN('0b111111-1111-1111-1111-111111111111'), UUID_TO_BIN('08111111-1111-1111-1111-111111111111'), UUID_TO_BIN('f1111111-1111-1111-1111-111111111111'), 4.0000, 6.0000, 'MODIFIED', 'pcs', NULL),
-- Control Panel Display Update - replacing display
(UUID_TO_BIN('0b222222-2222-2222-2222-222222222222'), UUID_TO_BIN('08222222-2222-2222-2222-222222222222'), UUID_TO_BIN('f5555555-5555-5555-5555-555555555555'), 1.0000, 1.0000, 'REMOVED', 'pcs', NULL);

-- =====================================================
-- ECO PRODUCT CHANGES
-- =====================================================
INSERT INTO eco_product_changes (id, eco_id, field_name, old_value, new_value) VALUES
-- Pump Seal Improvement - product field changes
(UUID_TO_BIN('0c111111-1111-1111-1111-111111111111'), UUID_TO_BIN('08333333-3333-3333-3333-333333333333'), 'description', 'Heavy-duty hydraulic pump for manufacturing', 'Heavy-duty hydraulic pump with improved seal design for manufacturing'),
(UUID_TO_BIN('0c222222-2222-2222-2222-222222222222'), UUID_TO_BIN('08333333-3333-3333-3333-333333333333'), 'cost_price', '55000.00', '57500.00'),
-- Sensor Module changes
(UUID_TO_BIN('0c333333-3333-3333-3333-333333333333'), UUID_TO_BIN('08444444-4444-4444-4444-444444444444'), 'description', 'Multi-sensor module for automation systems', 'Multi-sensor module with enhanced calibration for automation systems');

-- =====================================================
-- AUDIT LOGS
-- =====================================================
INSERT INTO audit_logs (id, eco_id, actor_user_id, action, affected_record, old_value, new_value, timestamp) VALUES
-- Motor ECO created
(UUID_TO_BIN('0d111111-1111-1111-1111-111111111111'), UUID_TO_BIN('08111111-1111-1111-1111-111111111111'), UUID_TO_BIN('a2222222-2222-2222-2222-222222222222'), 'ECO_CREATED', 'ECO-08111111', NULL, 'Draft ECO created', NOW()),
-- Control Panel ECO stage advanced
(UUID_TO_BIN('0d222222-2222-2222-2222-222222222222'), UUID_TO_BIN('08222222-2222-2222-2222-222222222222'), UUID_TO_BIN('a3333333-3333-3333-3333-333333333333'), 'STAGE_CHANGED', 'ECO-08222222', 'New', 'In Progress', NOW()),
-- Sensor Module ECO approved
(UUID_TO_BIN('0d333333-3333-3333-3333-333333333333'), UUID_TO_BIN('08444444-4444-4444-4444-444444444444'), UUID_TO_BIN('a4444444-4444-4444-4444-444444444444'), 'ECO_APPROVED', 'ECO-08444444', 'In Progress', 'Approved', NOW()),
-- Bearing ECO cancelled
(UUID_TO_BIN('0d444444-4444-4444-4444-444444444444'), UUID_TO_BIN('08555555-5555-5555-5555-555555555555'), UUID_TO_BIN('a2222222-2222-2222-2222-222222222222'), 'ECO_CANCELLED', 'ECO-08555555', 'Draft', 'Cancelled', NOW());

-- =====================================================
-- VERIFICATION QUERY
-- =====================================================
-- Run these to verify data was inserted correctly:
-- SELECT COUNT(*) as user_count FROM users;
-- SELECT COUNT(*) as product_count FROM products;
-- SELECT COUNT(*) as bom_count FROM boms;
-- SELECT COUNT(*) as eco_count FROM ecos;
-- SELECT login_id, email, role, status FROM users;
