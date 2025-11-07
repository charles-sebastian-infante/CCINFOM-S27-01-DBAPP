-- =====================================================
-- 1.0 ROLE DATA
-- Staff roles in the system
-- =====================================================
INSERT INTO role (role_id, role_name) VALUES
(1, 'Driver'),
(2, 'Conductor'),
(3, 'Manager'),
(4, 'Mechanic');

-- =====================================================
-- 2.0 TERMINAL DATA
-- Major terminals across Luzon provinces
-- =====================================================
INSERT INTO terminal (terminal_id, terminal_name, address, phone) VALUES
(1, 'Buendia Terminal', 'Sen. Gil Puyat Ave, Pasay City, Manila', '028331122'),
(2, 'Lucena Grand Terminal', 'Dalahican Rd, Lucena City, Quezon', '0427101234'),
(3, 'Lipa City Terminal', 'J.P. Laurel Hwy, Lipa City, Batangas', '0437560912'),
(4, 'Calamba Terminal', 'National Highway, Calamba, Laguna', '0495021412'),
(5, 'Calapan Port Terminal', 'Magsaysay St, Calapan City, Oriental Mindoro', '0433881567'),
(6, 'Naga City Terminal', 'Maharlika Hwy, Naga City, Camarines Sur', '0548115234'),
(7, 'Legazpi Central', 'Rizal St, Legazpi City, Albay', '0522804455'),
(8, 'Baguio Central', 'Governor Pack Rd, Baguio City, Benguet', '0744422334'),
(9, 'Dagupan Terminal', 'A.B. Fernandez Ave, Dagupan, Pangasinan', '0755226789'),
(10, 'Tarlac City Terminal', 'MacArthur Hwy, Tarlac City, Tarlac', '0452825567');

-- =====================================================
-- 3.0 ROUTE DATA
-- Routes connecting major provincial cities
-- =====================================================
INSERT INTO route (route_id, route_name, origin_id, destination_id, distance, travel_time, base_fare) VALUES
-- Manila-based routes
(1, 'Manila to Lucena', 1, 2, 130.50, '03:30:00', 350.00),
(2, 'Manila to Lipa', 1, 3, 85.20, '02:30:00', 280.00),
(3, 'Manila to Calamba', 1, 4, 60.00, '02:00:00', 200.00),
(4, 'Manila to Calapan', 1, 5, 145.00, '04:00:00', 420.00),
(5, 'Manila to Naga', 1, 6, 380.00, '08:00:00', 650.00),
(6, 'Manila to Legazpi', 1, 7, 480.00, '10:00:00', 850.00),
(7, 'Manila to Baguio', 1, 8, 250.00, '06:00:00', 550.00),
(8, 'Manila to Dagupan', 1, 9, 210.00, '05:00:00', 450.00),
(9, 'Manila to Tarlac', 1, 10, 125.00, '03:00:00', 300.00),
-- Inter-provincial routes
(10, 'Lipa to Lucena', 3, 2, 90.00, '02:15:00', 250.00),
(11, 'Calamba to Lipa', 4, 3, 45.00, '01:00:00', 150.00),
(12, 'Lucena to Naga', 2, 6, 250.00, '05:30:00', 450.00),
(13, 'Naga to Legazpi', 6, 7, 110.00, '02:30:00', 280.00),
(14, 'Tarlac to Baguio', 10, 8, 130.00, '03:30:00', 350.00),
(15, 'Dagupan to Baguio', 9, 8, 90.00, '02:00:00', 250.00),
-- Return routes
(16, 'Lucena to Manila', 2, 1, 130.50, '03:30:00', 350.00),
(17, 'Baguio to Manila', 8, 1, 250.00, '06:00:00', 550.00),
(18, 'Naga to Manila', 6, 1, 380.00, '08:00:00', 650.00),
(19, 'Legazpi to Manila', 7, 1, 480.00, '10:00:00', 850.00),
(20, 'Calapan to Manila', 5, 1, 145.00, '04:00:00', 420.00);

-- =====================================================
-- 4.0 BUS DATA
-- Fleet of buses assigned to different terminals
-- =====================================================
INSERT INTO bus (bus_id, bus_number, capacity, status, current_terminal) VALUES
-- Manila terminal buses
(1, 'MAM-1001', 45, 'Scheduled', 1),
(2, 'MAM-1002', 45, 'Available', 1),
(3, 'MAM-1003', 50, 'Scheduled', 1),
(4, 'MAM-1004', 45, 'In Transit', 1),
(5, 'MAM-1005', 40, 'Available', 1),
(6, 'MAM-1006', 50, 'Scheduled', 1),

-- Provincial terminal buses
(7, 'MAM-2001', 45, 'In Transit', 2),
(8, 'MAM-2002', 40, 'Available', 2),
(9, 'MAM-3001', 40, 'Maintenance', 3),
(10, 'MAM-3002', 45, 'Available', 3),
(11, 'MAM-4001', 45, 'Scheduled', 4),
(12, 'MAM-5001', 50, 'Available', 5),
(13, 'MAM-6001', 45, 'Scheduled', 6),
(14, 'MAM-7001', 45, 'In Transit', 7),
(15, 'MAM-8001', 50, 'Scheduled', 8),
(16, 'MAM-8002', 45, 'Available', 8),
(17, 'MAM-9001', 40, 'Scheduled', 9),
(18, 'MAM-10001', 45, 'Available', 10),
(19, 'MAM-1007', 45, 'Available', 1),
(20, 'MAM-1008', 50, 'Maintenance', 1);

-- =====================================================
-- 5.0 SCHEDULE DATA
-- Bus schedules for today and upcoming trips
-- =====================================================
INSERT INTO schedule (schedule_id, bus_id, route_id, departure_time, arrival_time, status) VALUES
-- Scheduled trips
(1, 1, 1, '2025-10-27 08:00:00', '2025-10-27 11:30:00', 'Scheduled'),
(2, 2, 2, '2025-10-27 09:00:00', '2025-10-27 11:30:00', 'Scheduled'),
(3, 3, 5, '2025-10-27 06:00:00', '2025-10-27 14:00:00', 'Scheduled'),
(4, 6, 9, '2025-10-27 07:30:00', '2025-10-27 12:30:00', 'Scheduled'),
(5, 11, 11, '2025-10-27 10:00:00', '2025-10-27 11:00:00', 'Scheduled'),
(6, 13, 18, '2025-10-27 09:30:00', '2025-10-27 17:30:00', 'Scheduled'),
(7, 15, 17, '2025-10-27 05:00:00', '2025-10-27 11:00:00', 'Scheduled'),
(8, 17, 15, '2025-10-27 08:00:00', '2025-10-27 10:00:00', 'Scheduled'),

-- Departed trips
(9, 4, 7, '2025-10-27 05:10:00', '2025-10-27 11:00:00', 'Departed'),
(10, 7, 16, '2025-10-27 07:05:00', '2025-10-27 10:30:00', 'Departed'),
(11, 14, 19, '2025-10-27 06:35:00', '2025-10-27 16:30:00', 'Departed'),

-- Completed trips
(12, 1, 1, '2025-10-26 08:05:00', '2025-10-26 11:45:00', 'Completed'),
(13, 2, 2, '2025-10-26 14:10:00', '2025-10-26 16:40:00', 'Completed'),
(14, 15, 17, '2025-10-25 05:00:00', '2025-10-25 11:15:00', 'Completed'),

-- Cancelled trip
(15, 19, 14, '2025-10-27 12:00:00', '2025-10-27 14:00:00', 'Cancelled'),

-- Future schedules
(16, 1, 1, '2025-10-27 14:00:00', '2025-10-27 17:30:00', 'Scheduled'),
(17, 2, 2, '2025-10-27 15:00:00', '2025-10-27 17:30:00', 'Scheduled'),
(18, 5, 8, '2025-10-28 06:00:00', '2025-10-28 12:00:00', 'Scheduled'),
(19, 6, 9, '2025-10-28 07:00:00', '2025-10-28 12:00:00', 'Scheduled'),
(20, 8, 12, '2025-10-28 08:00:00', '2025-10-28 13:30:00', 'Scheduled');

-- =====================================================
-- 6.0 STAFF DATA
-- Drivers, conductors, mechanics, and managers
-- Role IDs: 1=Driver, 2=Conductor, 3=Manager, 4=Mechanic
-- =====================================================
INSERT INTO staff (staff_id, staff_name, role_id, assigned_terminal, assigned_bus, shift, contact) VALUES
-- Manila Terminal Staff
(1, 'Juan Dela Cruz', 1, 1, 1, 'Morning', '09171234567'),          -- Driver
(2, 'Pedro Santos', 2, 1, 1, 'Morning', '09181234568'),             -- Conductor
(3, 'Ricardo Morales', 1, 1, 2, 'Morning', '09171234569'),          -- Driver
(4, 'Ana Garcia', 2, 1, 2, 'Morning', '09181234570'),               -- Conductor
(5, 'Maria Lopez', 3, 1, NULL, 'Morning', '09192223334'),           -- Manager
(6, 'Carlos Mendoza', 1, 1, 3, 'Morning', '09171234571'),           -- Driver
(7, 'Teresa Cruz', 2, 1, 3, 'Morning', '09181234572'),              -- Conductor
(8, 'Roberto Gonzales', 1, 1, 4, 'Morning', '09171234573'),         -- Driver
(9, 'Lisa Fernandez', 2, 1, 4, 'Morning', '09181234574'),           -- Conductor
(10, 'Miguel Torres', 4, 1, NULL, 'Morning', '09183335555'),        -- Mechanic

-- Lucena Terminal Staff
(11, 'Antonio Reyes', 1, 2, 7, 'Morning', '09171234575'),           -- Driver
(12, 'Carmen Silva', 2, 2, 7, 'Morning', '09181234576'),            -- Conductor
(13, 'Francisco Ramos', 3, 2, NULL, 'Evening', '09192223335'),      -- Manager
(14, 'Gabriel Santos', 4, 2, NULL, 'Evening', '09183335557'),       -- Mechanic

-- Lipa Terminal Staff
(15, 'Jose Reyes', 4, 3, NULL, 'Evening', '09183335556'),           -- Mechanic
(16, 'Daniel Castro', 1, 3, 10, 'Evening', '09171234577'),          -- Driver
(17, 'Elena Martinez', 2, 3, 10, 'Evening', '09181234578'),         -- Conductor

-- Calamba Terminal Staff
(18, 'Rafael Domingo', 1, 4, 11, 'Morning', '09171234579'),         -- Driver
(19, 'Sofia Hernandez', 2, 4, 11, 'Morning', '09181234580'),        -- Conductor

-- Naga Terminal Staff
(20, 'Luis Ventura', 1, 6, 13, 'Morning', '09171234581'),           -- Driver
(21, 'Patricia Romero', 2, 6, 13, 'Morning', '09181234582'),        -- Conductor
(22, 'Alejandro Perez', 3, 6, NULL, 'Evening', '09192223336'),      -- Manager

-- Legazpi Terminal Staff
(23, 'Fernando Diaz', 1, 7, 14, 'Morning', '09171234583'),          -- Driver
(24, 'Isabella Cruz', 2, 7, 14, 'Morning', '09181234584'),          -- Conductor

-- Baguio Terminal Staff
(25, 'Eduardo Lopez', 1, 8, 15, 'Morning', '09171234585'),          -- Driver
(26, 'Monica Rivera', 2, 8, 15, 'Morning', '09181234586'),          -- Conductor
(27, 'Santiago Flores', 3, 8, NULL, 'Evening', '09192223337'),      -- Manager
(28, 'Victor Aguilar', 4, 8, NULL, 'Evening', '09183335558'),       -- Mechanic

-- Dagupan Terminal Staff
(29, 'Ramón Torres', 1, 9, 17, 'Morning', '09171234587'),           -- Driver
(30, 'Andrea Castillo', 2, 9, 17, 'Morning', '09181234588'); 		-- Conductor

-- =====================================================
-- 7.0 MAINTENANCE TYPE DATA
-- Types of maintenance services
-- =====================================================
INSERT INTO maintenance_type (maintenance_type_id, type_name, maintenance_cost) VALUES
(1, 'Engine Oil Change', 2500.00),
(2, 'Brake Inspection', 3000.00),
(3, 'Transmission Check', 5000.00),
(4, 'Tire Replacement', 8000.00),
(5, 'Air Conditioning Repair', 6000.00),
(6, 'Routine Check-up', 1500.00),
(7, 'Filter Replacement', 1200.00),
(8, 'Brake Pad Replacement', 4500.00),
(9, 'Suspension Inspection', 3500.00),
(10, 'Battery Replacement', 4000.00),
(11, 'Electrical Check', 2000.00),
(12, 'Engine Tune-up', 5500.00),
(13, 'Tire Rotation', 1000.00),
(14, 'Wheel Alignment', 2000.00),
(15, 'Cooling System Flush', 3000.00),
(16, 'Interior Cleaning', 2500.00),
(17, 'Sanitization', 1500.00),
(18, 'Windshield Replacement', 7000.00);

-- =====================================================
-- 8.0 MAINTENANCE DATA
-- Maintenance records for buses
-- =====================================================
INSERT INTO maintenance (maintenance_id, bus_id, assigned_mechanic, maintenance_type_id, starting_date, completion_time) VALUES
-- Pending maintenance (completion_time is NULL)
(1, 9, 10, 1, '2025-10-27 09:00:00', NULL),
(2, 9, 10, 2, '2025-10-27 09:00:00', NULL),
(3, 20, 15, 3, '2025-10-27 10:00:00', NULL),
(4, 20, 15, 4, '2025-10-27 10:00:00', NULL),
(5, 9, 10, 5, '2025-10-27 14:00:00', NULL),

-- Completed maintenance
(6, 1, 10, 6, '2025-10-20 14:00:00', '2025-10-20 16:30:00'),
(7, 2, 10, 1, '2025-10-22 09:00:00', '2025-10-22 11:00:00'),
(8, 2, 10, 7, '2025-10-22 09:00:00', '2025-10-22 11:00:00'),
(9, 3, 10, 8, '2025-10-23 10:00:00', '2025-10-23 13:00:00'),
(10, 4, 10, 9, '2025-10-24 08:00:00', '2025-10-24 11:30:00'),
(11, 5, 10, 10, '2025-10-25 11:00:00', '2025-10-25 13:00:00'),
(12, 5, 10, 11, '2025-10-25 11:00:00', '2025-10-25 13:00:00'),
(13, 7, 14, 12, '2025-10-21 13:00:00', '2025-10-21 17:00:00'),
(14, 8, 28, 13, '2025-10-19 15:00:00', '2025-10-19 16:30:00'),
(15, 8, 28, 14, '2025-10-19 15:00:00', '2025-10-19 16:30:00'),
(16, 10, 15, 15, '2025-10-18 09:00:00', '2025-10-18 12:00:00'),
(17, 15, 28, 16, '2025-10-17 10:00:00', '2025-10-17 14:00:00'),
(18, 15, 28, 17, '2025-10-17 10:00:00', '2025-10-17 14:00:00'),
(19, 6, 10, 18, '2025-10-16 14:00:00', '2025-10-16 18:00:00');

-- =====================================================
-- 9.0 TICKET DATA
-- Sample ticket bookings
-- =====================================================
INSERT INTO ticket (ticket_id, ticket_number, schedule_id, discounted) VALUES
(1, 'TKT-20251027-001', 1, 0),
(2, 'TKT-20251027-002', 1, 1),
(3, 'TKT-20251027-003', 2, 0),
(4, 'TKT-20251027-004', 3, 0),
(5, 'TKT-20251027-005', 4, 1),
(6, 'TKT-20251027-006', 5, 0),
(7, 'TKT-20251027-007', 6, 0),
(8, 'TKT-20251027-008', 7, 1),
(9, 'TKT-20251027-009', 8, 0),
(10, 'TKT-20251027-010', 9, 0),
(11, 'TKT-20251026-001', 12, 0),
(12, 'TKT-20251026-002', 13, 1),
(13, 'TKT-20251025-001', 14, 0);