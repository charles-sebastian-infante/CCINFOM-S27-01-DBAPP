-- =====================================================
-- 1.0 TERMINAL DATA
-- Major terminals across Luzon provinces
-- =====================================================
INSERT INTO Terminal (terminal_name, location, city, phone) VALUES
('Buendia Terminal', 'Sen. Gil Puyat Ave, Pasay City', 'Manila', '028331122'),
('Lucena Grand Terminal', 'Dalahican Rd, Lucena City', 'Lucena', '0427101234'),
('Lipa City Terminal', 'J.P. Laurel Hwy, Lipa City', 'Batangas', '0437560912'),
('Calamba Terminal', 'National Highway, Calamba', 'Laguna', '0495021412'),
('Calapan Port Terminal', 'Magsaysay St, Calapan City', 'Oriental Mindoro', '0433881567'),
('Naga City Terminal', 'Maharlika Hwy, Naga City', 'Camarines Sur', '0548115234'),
('Legazpi Central', 'Rizal St, Legazpi City', 'Albay', '0522804455'),
('Baguio Central', 'Governor Pack Rd, Baguio City', 'Benguet', '0744422334'),
('Dagupan Terminal', 'A.B. Fernandez Ave, Dagupan', 'Pangasinan', '0755226789'),
('Tarlac City Terminal', 'MacArthur Hwy, Tarlac City', 'Tarlac', '0452825567');

-- =====================================================
-- 2.0 ROUTE DATA
-- Routes connecting major provincial cities
-- =====================================================
INSERT INTO Route (route_name, origin_id, destination_id, distance, travel_time, base_fare) VALUES
-- Manila-based routes
('Manila to Lucena', 1, 2, 130.50, '03:30:00', 350.00),
('Manila to Lipa', 1, 3, 85.20, '02:30:00', 280.00),
('Manila to Calamba', 1, 4, 60.00, '02:00:00', 200.00),
('Manila to Calapan', 1, 5, 145.00, '04:00:00', 420.00),
('Manila to Naga', 1, 6, 380.00, '08:00:00', 650.00),
('Manila to Legazpi', 1, 7, 480.00, '10:00:00', 850.00),
('Manila to Baguio', 1, 8, 250.00, '06:00:00', 550.00),
('Manila to Dagupan', 1, 9, 210.00, '05:00:00', 450.00),
('Manila to Tarlac', 1, 10, 125.00, '03:00:00', 300.00),
-- Inter-provincial routes
('Lipa to Lucena', 3, 2, 90.00, '02:15:00', 250.00),
('Calamba to Lipa', 4, 3, 45.00, '01:00:00', 150.00),
('Lucena to Naga', 2, 6, 250.00, '05:30:00', 450.00),
('Naga to Legazpi', 6, 7, 110.00, '02:30:00', 280.00),
('Tarlac to Baguio', 10, 8, 130.00, '03:30:00', 350.00),
('Dagupan to Baguio', 9, 8, 90.00, '02:00:00', 250.00),
-- Return routes
('Lucena to Manila', 2, 1, 130.50, '03:30:00', 350.00),
('Baguio to Manila', 8, 1, 250.00, '06:00:00', 550.00),
('Naga to Manila', 6, 1, 380.00, '08:00:00', 650.00),
('Legazpi to Manila', 7, 1, 480.00, '10:00:00', 850.00),
('Calapan to Manila', 5, 1, 145.00, '04:00:00', 420.00);

-- =====================================================
-- 3.0 BUS DATA
-- Fleet of buses assigned to different routes
-- =====================================================
INSERT INTO Bus (bus_number, capacity, status, current_terminal, route_id) VALUES
-- Manila terminal buses
('MAM-1001', 45, 'Scheduled', 1, 1),
('MAM-1002', 45, 'Available', 1, 2),
('MAM-1003', 50, 'Scheduled', 1, 5),
('MAM-1004', 45, 'In Transit', 1, 7),
('MAM-1005', 40, 'Available', 1, 8),
('MAM-1006', 50, 'Scheduled', 1, 9),
-- Provincial terminal buses
('MAM-2001', 45, 'In Transit', 2, 16),
('MAM-2002', 40, 'Available', 2, 12),
('MAM-3001', 40, 'Maintenance', 3, 10),
('MAM-3002', 45, 'Available', 3, 11),
('MAM-4001', 45, 'Scheduled', 4, 11),
('MAM-5001', 50, 'Available', 5, 20),
('MAM-6001', 45, 'Scheduled', 6, 18),
('MAM-7001', 45, 'In Transit', 7, 19),
('MAM-8001', 50, 'Scheduled', 8, 17),
('MAM-8002', 45, 'Available', 8, 14),
('MAM-9001', 40, 'Scheduled', 9, 15),
('MAM-10001', 45, 'Available', 10, 14),
('MAM-1007', 45, 'Available', 1, 3),
('MAM-1008', 50, 'Maintenance', 1, 4);

-- =====================================================
-- 4.0 SCHEDULE DATA
-- Bus schedules for today and upcoming trips
-- =====================================================
INSERT INTO Schedule (bus_id, departure_time, arrival_time, status) VALUES
-- Scheduled trips
(1, '2025-10-27 08:00:00', '2025-10-27 11:30:00', 'Scheduled'),
(2, '2025-10-27 09:00:00', '2025-10-27 11:30:00', 'Scheduled'),
(3, '2025-10-27 06:00:00', '2025-10-27 14:00:00', 'Scheduled'),
(6, '2025-10-27 07:30:00', '2025-10-27 12:30:00', 'Scheduled'),
(11, '2025-10-27 10:00:00', '2025-10-27 11:00:00', 'Scheduled'),
(13, '2025-10-27 09:30:00', '2025-10-27 17:30:00', 'Scheduled'),
(15, '2025-10-27 05:00:00', '2025-10-27 11:00:00', 'Scheduled'),
(17, '2025-10-27 08:00:00', '2025-10-27 10:00:00', 'Scheduled'),
-- Departed trips
(4, '2025-10-27 05:10:00', '2025-10-27 11:00:00', 'Departed'),
(7, '2025-10-27 07:05:00', '2025-10-27 10:30:00', 'Departed'),
(14, '2025-10-27 06:35:00', '2025-10-27 16:30:00', 'Departed'),
-- Completed trips
(1, '2025-10-26 08:05:00', '2025-10-26 11:45:00', 'Completed'),
(2, '2025-10-26 14:10:00', '2025-10-26 16:40:00', 'Completed'),
(15, '2025-10-25 05:00:00', '2025-10-25 11:15:00', 'Completed'),
-- Cancelled trip
(19, '2025-10-27 12:00:00', '2025-10-27 14:00:00', 'Cancelled'),
-- Future schedules
(1, '2025-10-27 14:00:00', '2025-10-27 17:30:00', 'Scheduled'),
(2, '2025-10-27 15:00:00', '2025-10-27 17:30:00', 'Scheduled'),
(5, '2025-10-28 06:00:00', '2025-10-28 12:00:00', 'Scheduled'),
(6, '2025-10-28 07:00:00', '2025-10-28 12:00:00', 'Scheduled'),
(8, '2025-10-28 08:00:00', '2025-10-28 13:30:00', 'Scheduled');

-- =====================================================
-- 5.0 STAFF DATA
-- Drivers, conductors, mechanics, and managers
-- =====================================================
INSERT INTO Staff (staff_name, role, assigned_terminal, assigned_bus, shift, contact) VALUES
-- Manila Terminal Staff
('Juan Dela Cruz', 'Driver', 1, 1, 'Morning', '09171234567'),
('Pedro Santos', 'Conductor', 1, 1, 'Morning', '09181234568'),
('Ricardo Morales', 'Driver', 1, 2, 'Morning', '09171234569'),
('Ana Garcia', 'Conductor', 1, 2, 'Morning', '09181234570'),
('Maria Lopez', 'Manager', 1, NULL, 'Full Day', '09192223334'),
('Carlos Mendoza', 'Driver', 1, 3, 'Early Morning', '09171234571'),
('Teresa Cruz', 'Conductor', 1, 3, 'Early Morning', '09181234572'),
('Roberto Gonzales', 'Driver', 1, 4, 'Early Morning', '09171234573'),
('Lisa Fernandez', 'Conductor', 1, 4, 'Early Morning', '09181234574'),
('Miguel Torres', 'Mechanic', 1, NULL, 'Full Day', '09183335555'),
-- Lucena Terminal Staff
('Antonio Reyes', 'Driver', 2, 7, 'Morning', '09171234575'),
('Carmen Silva', 'Conductor', 2, 7, 'Morning', '09181234576'),
('Francisco Ramos', 'Manager', 2, NULL, 'Full Day', '09192223335'),
('Gabriel Santos', 'Mechanic', 2, NULL, 'Day', '09183335557'),
-- Lipa Terminal Staff
('Jose Reyes', 'Mechanic', 3, 9, 'Day', '09183335556'),
('Daniel Castro', 'Driver', 3, 10, 'Afternoon', '09171234577'),
('Elena Martinez', 'Conductor', 3, 10, 'Afternoon', '09181234578'),
-- Calamba Terminal Staff
('Rafael Domingo', 'Driver', 4, 11, 'Morning', '09171234579'),
('Sofia Hernandez', 'Conductor', 4, 11, 'Morning', '09181234580'),
-- Naga Terminal Staff
('Luis Ventura', 'Driver', 6, 13, 'Morning', '09171234581'),
('Patricia Romero', 'Conductor', 6, 13, 'Morning', '09181234582'),
('Alejandro Perez', 'Manager', 6, NULL, 'Full Day', '09192223336'),
-- Legazpi Terminal Staff
('Fernando Diaz', 'Driver', 7, 14, 'Early Morning', '09171234583'),
('Isabella Cruz', 'Conductor', 7, 14, 'Early Morning', '09181234584'),
-- Baguio Terminal Staff
('Eduardo Lopez', 'Driver', 8, 15, 'Early Morning', '09171234585'),
('Monica Rivera', 'Conductor', 8, 15, 'Early Morning', '09181234586'),
('Santiago Flores', 'Manager', 8, NULL, 'Full Day', '09192223337'),
('Victor Aguilar', 'Mechanic', 8, NULL, 'Day', '09183335558'),
-- Dagupan Terminal Staff
('Ramón Torres', 'Driver', 9, 17, 'Morning', '09171234587'),
('Andrea Castillo', 'Conductor', 9, 17, 'Morning', '09181234588');

-- =====================================================
-- 6.0 TICKET DATA
-- Sample tickets for various trips
-- =====================================================
INSERT INTO Ticket (ticket_number, bus_id, schedule_id, departure_date, type, discount, final_amount, route_id, staff_id) VALUES
-- Today's scheduled trips tickets
('TCK-20251027-0001', 1, 1, '2025-10-27 08:00:00', 'Regular', 0.00, 350.00, 1, 2),
('TCK-20251027-0002', 1, 1, '2025-10-27 08:00:00', 'Discounted', 20.00, 280.00, 1, 2),
('TCK-20251027-0003', 1, 1, '2025-10-27 08:00:00', 'Discounted', 20.00, 280.00, 1, 2),
('TCK-20251027-0004', 1, 1, '2025-10-27 08:00:00', 'Regular', 0.00, 350.00, 1, 2),
('TCK-20251027-0005', 2, 2, '2025-10-27 09:00:00', 'Regular', 0.00, 280.00, 2, 4),
('TCK-20251027-0006', 2, 2, '2025-10-27 09:00:00', 'Discounted', 20.00, 224.00, 2, 4),
('TCK-20251027-0007', 2, 2, '2025-10-27 09:00:00', 'Regular', 0.00, 280.00, 2, 4),
('TCK-20251027-0008', 3, 3, '2025-10-27 06:00:00', 'Regular', 0.00, 650.00, 5, 7),
('TCK-20251027-0009', 3, 3, '2025-10-27 06:00:00', 'Discounted', 20.00, 520.00, 5, 7),
('TCK-20251027-0010', 3, 3, '2025-10-27 06:00:00', 'Regular', 0.00, 650.00, 5, 7),
('TCK-20251027-0011', 6, 4, '2025-10-27 07:30:00', 'Regular', 0.00, 450.00, 9, 2),
('TCK-20251027-0012', 6, 4, '2025-10-27 07:30:00', 'Discounted', 20.00, 360.00, 9, 2),
-- Departed trips tickets
('TCK-20251027-0013', 4, 9, '2025-10-27 05:00:00', 'Regular', 0.00, 550.00, 7, 9),
('TCK-20251027-0014', 4, 9, '2025-10-27 05:00:00', 'Discounted', 20.00, 440.00, 7, 9),
('TCK-20251027-0015', 4, 9, '2025-10-27 05:00:00', 'Free', 0.00, 0.00, 7, 9),
('TCK-20251027-0016', 7, 10, '2025-10-27 07:00:00', 'Regular', 0.00, 350.00, 16, 12),
('TCK-20251027-0017', 7, 10, '2025-10-27 07:00:00', 'Discounted', 20.00, 280.00, 16, 12),
-- Yesterday's completed trips
('TCK-20251026-0001', 1, 12, '2025-10-26 08:00:00', 'Regular', 0.00, 350.00, 1, 2),
('TCK-20251026-0002', 1, 12, '2025-10-26 08:00:00', 'Discounted', 20.00, 280.00, 1, 2),
('TCK-20251026-0003', 1, 12, '2025-10-26 08:00:00', 'Regular', 0.00, 350.00, 1, 2),
('TCK-20251026-0004', 1, 12, '2025-10-26 08:00:00', 'Discounted', 20.00, 280.00, 1, 2),
('TCK-20251026-0005', 2, 13, '2025-10-26 14:00:00', 'Regular', 0.00, 280.00, 2, 4),
('TCK-20251026-0006', 2, 13, '2025-10-26 14:00:00', 'Regular', 0.00, 280.00, 2, 4),
-- Future scheduled trips
('TCK-20251027-0018', 1, 16, '2025-10-27 14:00:00', 'Regular', 0.00, 350.00, 1, 2),
('TCK-20251027-0019', 2, 17, '2025-10-27 15:00:00', 'Discounted', 20.00, 224.00, 2, 4),
('TCK-20251028-0001', 5, 18, '2025-10-28 06:00:00', 'Regular', 0.00, 550.00, 8, 2),
('TCK-20251028-0002', 5, 18, '2025-10-28 06:00:00', 'Discounted', 20.00, 440.00, 8, 2),
('TCK-20251028-0003', 6, 19, '2025-10-28 07:00:00', 'Regular', 0.00, 450.00, 9, 2);

-- =====================================================
-- 7.0 MAINTENANCE DATA
-- Maintenance records for buses
-- =====================================================
INSERT INTO Maintenance (bus_id, maintenance_date, description, status) VALUES
-- Pending maintenance
(9, '2025-10-27 09:00:00', 'Engine oil change and brake inspection', 'Pending'),
(20, '2025-10-27 10:00:00', 'Transmission check and tire replacement', 'Pending'),
(9, '2025-10-27 14:00:00', 'Air conditioning system repair', 'Pending'),
-- Completed maintenance
(1, '2025-10-20 14:00:00', 'Routine check-up completed', 'Completed'),
(2, '2025-10-22 09:00:00', 'Oil change and filter replacement', 'Completed'),
(3, '2025-10-23 10:00:00', 'Brake pad replacement', 'Completed'),
(4, '2025-10-24 08:00:00', 'Suspension system inspection', 'Completed'),
(5, '2025-10-25 11:00:00', 'Battery replacement and electrical check', 'Completed'),
(7, '2025-10-21 13:00:00', 'Engine tune-up', 'Completed'),
(8, '2025-10-19 15:00:00', 'Tire rotation and alignment', 'Completed'),
(10, '2025-10-18 09:00:00', 'Cooling system flush', 'Completed'),
(15, '2025-10-17 10:00:00', 'Complete interior cleaning and sanitization', 'Completed'),
(6, '2025-10-16 14:00:00', 'Windshield replacement', 'Completed');
