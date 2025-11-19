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
(1, 'MAM-1001', 45, 'In Transit', 1),      -- Currently on the road
(2, 'MAM-1002', 45, 'Scheduled', 1),       -- Has upcoming schedule
(3, 'MAM-1003', 50, 'Available', 1),       -- Ready for assignment
(4, 'MAM-1004', 45, 'Scheduled', 1),       -- Has upcoming schedule
(5, 'MAM-1005', 40, 'Scheduled', 1),       -- Has morning schedule
(6, 'MAM-1006', 50, 'Available', 1),       -- Ready for assignment
(7, 'MAM-1007', 45, 'Scheduled', 1),       -- Has afternoon schedule
(8, 'MAM-1008', 50, 'Maintenance', 1),     -- Under maintenance

-- Provincial terminal buses
(9, 'MAM-2001', 45, 'In Transit', 2),      -- Currently traveling back to Manila
(10, 'MAM-2002', 40, 'Available', 2),      -- At Lucena, ready
(11, 'MAM-3001', 40, 'Maintenance', 3),    -- Under maintenance at Lipa
(12, 'MAM-3002', 45, 'Available', 3),      -- At Lipa, ready
(13, 'MAM-4001', 45, 'Scheduled', 4),      -- At Calamba, has schedule
(14, 'MAM-5001', 50, 'Available', 5),      -- At Calapan, ready
(15, 'MAM-6001', 45, 'Scheduled', 6),      -- At Naga, has schedule
(16, 'MAM-7001', 45, 'Available', 7),      -- At Legazpi, ready
(17, 'MAM-8001', 50, 'Scheduled', 8),      -- At Baguio, has morning schedule
(18, 'MAM-8002', 45, 'Available', 8),      -- At Baguio, ready
(19, 'MAM-9001', 40, 'Available', 9),      -- At Dagupan, ready
(20, 'MAM-10001', 45, 'Available', 10);    -- At Tarlac, ready

-- =====================================================
-- 5.0 SCHEDULE DATA
-- Bus schedules around Nov 21, 2025, 8:30 AM
-- Current time reference: Nov 21, 2025, 8:30 AM
-- =====================================================
INSERT INTO schedule (schedule_id, bus_id, route_id, departure_time, arrival_time, status) VALUES
-- DEPARTED - Left early morning (6:00-7:30 AM), currently in transit
(1, 1, 7, '2025-11-21 06:00:00', '2025-11-21 12:00:00', 'Departed'),     -- Manila to Baguio (6 hrs)
(2, 5, 5, '2025-11-21 06:30:00', '2025-11-21 13:30:00', 'Departed'),     -- Manila to Naga (7 hrs)
(3, 9, 16, '2025-11-21 07:00:00', '2025-11-21 10:30:00', 'Departed'),    -- Lucena to Manila (3.5 hrs)
(4, 17, 17, '2025-11-21 07:30:00', '2025-11-21 13:30:00', 'Departed'),   -- Baguio to Manila (6 hrs)

-- SCHEDULED - Departing soon or later today (9:00 AM onwards)
(5, 2, 1, '2025-11-21 09:00:00', '2025-11-21 12:30:00', 'Scheduled'),    -- Manila to Lucena (3.5 hrs)
(6, 4, 2, '2025-11-21 09:30:00', '2025-11-21 12:00:00', 'Scheduled'),    -- Manila to Lipa (2.5 hrs)
(7, 7, 8, '2025-11-21 10:00:00', '2025-11-21 15:00:00', 'Scheduled'),    -- Manila to Dagupan (5 hrs)
(8, 13, 11, '2025-11-21 11:00:00', '2025-11-21 12:00:00', 'Scheduled'),  -- Calamba to Lipa (1 hr)
(9, 15, 18, '2025-11-21 12:00:00', '2025-11-21 19:00:00', 'Scheduled'),  -- Naga to Manila (7 hrs)
(10, 2, 16, '2025-11-21 14:00:00', '2025-11-21 17:30:00', 'Scheduled'),  -- Return trip for bus 2 (3.5 hrs)
(11, 4, 1, '2025-11-21 15:00:00', '2025-11-21 18:30:00', 'Scheduled'),   -- Return trip for bus 4 (3.5 hrs)

-- COMPLETED - Yesterday's trips (Nov 20)
(12, 1, 1, '2025-11-20 08:00:00', '2025-11-20 11:30:00', 'Completed'),   -- Manila to Lucena (3.5 hrs)
(13, 2, 2, '2025-11-20 09:00:00', '2025-11-20 11:30:00', 'Completed'),   -- Manila to Lipa (2.5 hrs)
(14, 5, 8, '2025-11-20 10:00:00', '2025-11-20 15:00:00', 'Completed'),   -- Manila to Dagupan (5 hrs)
(15, 17, 7, '2025-11-20 06:00:00', '2025-11-20 12:00:00', 'Completed'),  -- Manila to Baguio (6 hrs)

-- COMPLETED - Day before yesterday (Nov 19)
(16, 4, 9, '2025-11-19 07:30:00', '2025-11-19 10:30:00', 'Completed'),   -- Manila to Tarlac (3 hrs)
(17, 7, 1, '2025-11-19 14:00:00', '2025-11-19 17:30:00', 'Completed'),   -- Manila to Lucena (3.5 hrs)
(18, 13, 3, '2025-11-19 09:00:00', '2025-11-19 11:00:00', 'Completed'),  -- Manila to Calamba (2 hrs)

-- FUTURE SCHEDULES - Tomorrow (Nov 22)
(19, 1, 1, '2025-11-22 08:00:00', '2025-11-22 11:30:00', 'Scheduled'),   -- Manila to Lucena (3.5 hrs)
(20, 2, 7, '2025-11-22 06:00:00', '2025-11-22 12:00:00', 'Scheduled'),   -- Manila to Baguio (6 hrs)
(21, 5, 2, '2025-11-22 09:00:00', '2025-11-22 11:30:00', 'Scheduled'),   -- Manila to Lipa (2.5 hrs)
(22, 7, 8, '2025-11-22 10:00:00', '2025-11-22 15:00:00', 'Scheduled');   -- Manila to Dagupan (5 hrs)

-- =====================================================
-- 6.0 STAFF DATA
-- Drivers, conductors, mechanics, and managers
-- Role IDs: 1=Driver, 2=Conductor, 3=Manager, 4=Mechanic
-- =====================================================
INSERT INTO staff (staff_id, staff_name, role_id, assigned_terminal, assigned_bus, shift, contact) VALUES
-- Manila Terminal Staff
(1, 'Juan Dela Cruz', 1, 1, 1, 'Morning', '09171234567'),          -- Driver for bus 1
(2, 'Pedro Santos', 2, 1, 1, 'Morning', '09181234568'),             -- Conductor for bus 1
(3, 'Ricardo Morales', 1, 1, 2, 'Morning', '09171234569'),          -- Driver for bus 2
(4, 'Ana Garcia', 2, 1, 2, 'Morning', '09181234570'),               -- Conductor for bus 2
(5, 'Maria Lopez', 3, 1, NULL, 'Morning', '09192223334'),           -- Manager
(6, 'Carlos Mendoza', 1, 1, 4, 'Morning', '09171234571'),           -- Driver for bus 4
(7, 'Teresa Cruz', 2, 1, 4, 'Morning', '09181234572'),              -- Conductor for bus 4
(8, 'Roberto Gonzales', 1, 1, 5, 'Morning', '09171234573'),         -- Driver for bus 5
(9, 'Lisa Fernandez', 2, 1, 5, 'Morning', '09181234574'),           -- Conductor for bus 5
(10, 'Miguel Torres', 4, 1, NULL, 'Morning', '09183335555'),        -- Mechanic
(11, 'Jose Valencia', 1, 1, 7, 'Morning', '09171234580'),           -- Driver for bus 7
(12, 'Carmen Reyes', 2, 1, 7, 'Morning', '09181234581'),            -- Conductor for bus 7

-- Lucena Terminal Staff
(13, 'Antonio Reyes', 1, 2, 9, 'Morning', '09171234575'),           -- Driver for bus 9
(14, 'Carmen Silva', 2, 2, 9, 'Morning', '09181234576'),            -- Conductor for bus 9
(15, 'Francisco Ramos', 3, 2, NULL, 'Evening', '09192223335'),      -- Manager
(16, 'Gabriel Santos', 4, 2, NULL, 'Evening', '09183335557'),       -- Mechanic

-- Lipa Terminal Staff
(17, 'Jose Reyes', 4, 3, NULL, 'Evening', '09183335556'),           -- Mechanic
(18, 'Daniel Castro', 1, 3, 12, 'Evening', '09171234577'),          -- Driver for bus 12
(19, 'Elena Martinez', 2, 3, 12, 'Evening', '09181234578'),         -- Conductor for bus 12

-- Calamba Terminal Staff
(20, 'Rafael Domingo', 1, 4, 13, 'Morning', '09171234579'),         -- Driver for bus 13
(21, 'Sofia Hernandez', 2, 4, 13, 'Morning', '09181234580'),        -- Conductor for bus 13

-- Naga Terminal Staff
(22, 'Luis Ventura', 1, 6, 15, 'Morning', '09171234581'),           -- Driver for bus 15
(23, 'Patricia Romero', 2, 6, 15, 'Morning', '09181234582'),        -- Conductor for bus 15
(24, 'Alejandro Perez', 3, 6, NULL, 'Evening', '09192223336'),      -- Manager

-- Legazpi Terminal Staff
(25, 'Fernando Diaz', 1, 7, 16, 'Morning', '09171234583'),          -- Driver for bus 16
(26, 'Isabella Cruz', 2, 7, 16, 'Morning', '09181234584'),          -- Conductor for bus 16

-- Baguio Terminal Staff
(27, 'Eduardo Lopez', 1, 8, 17, 'Morning', '09171234585'),          -- Driver for bus 17
(28, 'Monica Rivera', 2, 8, 17, 'Morning', '09181234586'),          -- Conductor for bus 17
(29, 'Santiago Flores', 3, 8, NULL, 'Evening', '09192223337'),      -- Manager
(30, 'Victor Aguilar', 4, 8, NULL, 'Evening', '09183335558'),       -- Mechanic

-- Dagupan Terminal Staff
(31, 'Ramón Torres', 1, 9, 19, 'Morning', '09171234587'),           -- Driver for bus 19
(32, 'Andrea Castillo', 2, 9, 19, 'Morning', '09181234588');        -- Conductor for bus 19

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
-- Maintenance records for buses (around Nov 21, 2025)
-- =====================================================
INSERT INTO maintenance (maintenance_id, bus_id, assigned_mechanic, maintenance_type_id, starting_date, completion_time) VALUES
-- ONGOING MAINTENANCE (completion_time is NULL) - Started today or recently
(1, 11, 17, 1, '2025-11-21 08:00:00', NULL),     -- MAM-3001 Engine Oil Change
(2, 11, 17, 2, '2025-11-21 08:00:00', NULL),     -- MAM-3001 Brake Inspection
(3, 8, 10, 3, '2025-11-20 14:00:00', NULL),      -- MAM-1008 Transmission Check (started yesterday)
(4, 8, 10, 4, '2025-11-20 14:00:00', NULL),      -- MAM-1008 Tire Replacement (started yesterday)
(5, 11, 17, 5, '2025-11-21 13:00:00', NULL),     -- MAM-3001 AC Repair (scheduled for later today)

-- COMPLETED MAINTENANCE - Recent history (last 7 days)
(6, 1, 10, 6, '2025-11-20 14:00:00', '2025-11-20 16:30:00'),   -- Bus 1 routine checkup
(7, 2, 10, 1, '2025-11-19 09:00:00', '2025-11-19 11:00:00'),   -- Bus 2 oil change
(8, 2, 10, 7, '2025-11-19 09:00:00', '2025-11-19 11:00:00'),   -- Bus 2 filter replacement
(9, 4, 10, 8, '2025-11-18 10:00:00', '2025-11-18 13:00:00'),   -- Bus 4 brake pads
(10, 5, 10, 9, '2025-11-17 08:00:00', '2025-11-17 11:30:00'),  -- Bus 5 suspension
(11, 7, 10, 10, '2025-11-16 11:00:00', '2025-11-16 13:00:00'), -- Bus 7 battery
(12, 7, 10, 11, '2025-11-16 11:00:00', '2025-11-16 13:00:00'), -- Bus 7 electrical check
(13, 9, 16, 12, '2025-11-15 13:00:00', '2025-11-15 17:00:00'), -- Bus 9 engine tune-up
(14, 17, 30, 13, '2025-11-15 15:00:00', '2025-11-15 16:30:00'),-- Bus 17 tire rotation
(15, 17, 30, 14, '2025-11-15 15:00:00', '2025-11-15 16:30:00'),-- Bus 17 wheel alignment
(16, 12, 17, 15, '2025-11-14 09:00:00', '2025-11-14 12:00:00'),-- Bus 12 cooling system
(17, 15, 24, 16, '2025-11-14 10:00:00', '2025-11-14 14:00:00'),-- Bus 15 interior cleaning
(18, 15, 24, 17, '2025-11-14 10:00:00', '2025-11-14 14:00:00'),-- Bus 15 sanitization
(19, 6, 10, 18, '2025-11-13 14:00:00', '2025-11-13 18:00:00'); -- Bus 6 windshield

-- =====================================================
-- 9.0 TICKET DATA
-- Sample ticket bookings for Nov 21, 2025 schedules
-- =====================================================
INSERT INTO ticket (ticket_id, ticket_number, schedule_id, discounted) VALUES
-- Tickets for departed trips (schedules 1-4)
(1, 'TKT-20251121-001', 1, 0),    -- Manila to Baguio
(2, 'TKT-20251121-002', 1, 1),    -- Manila to Baguio (discounted)
(3, 'TKT-20251121-003', 2, 0),    -- Manila to Naga
(4, 'TKT-20251121-004', 2, 1),    -- Manila to Naga (discounted)
(5, 'TKT-20251121-005', 3, 0),    -- Lucena to Manila
(6, 'TKT-20251121-006', 4, 0),    -- Baguio to Manila

-- Tickets for upcoming trips (schedules 5-11)
(7, 'TKT-20251121-007', 5, 0),    -- Manila to Lucena (9:00 AM)
(8, 'TKT-20251121-008', 5, 1),    -- Manila to Lucena (discounted)
(9, 'TKT-20251121-009', 6, 0),    -- Manila to Lipa (9:30 AM)
(10, 'TKT-20251121-010', 7, 0),   -- Manila to Dagupan (10:00 AM)
(11, 'TKT-20251121-011', 8, 1),   -- Calamba to Lipa (discounted)
(12, 'TKT-20251121-012', 9, 0),   -- Naga to Manila (12:00 PM)

-- Tickets for yesterday's completed trips (Nov 20)
(13, 'TKT-20251120-001', 12, 0),  -- Completed trip
(14, 'TKT-20251120-002', 13, 1),  -- Completed trip (discounted)
(15, 'TKT-20251120-003', 14, 0),  -- Completed trip
(16, 'TKT-20251120-004', 15, 0);  -- Completed trip