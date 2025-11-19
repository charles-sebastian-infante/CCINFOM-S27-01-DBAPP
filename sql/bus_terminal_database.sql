-- =====================================================
-- 0.0 Reset
-- =====================================================
DROP DATABASE IF EXISTS bus_terminal_management;
CREATE DATABASE bus_terminal_management;
USE bus_terminal_management; 

-- =====================================================
-- 2.0 Table: Terminal
-- =====================================================
CREATE TABLE Terminal (
    terminal_id INT PRIMARY KEY AUTO_INCREMENT,
    terminal_name VARCHAR(100) UNIQUE NOT NULL,
    address VARCHAR(255) NOT NULL,
    phone VARCHAR(20) UNIQUE  -- ADDED UNIQUE constraint per requirements
);

-- =====================================================
-- 3.0 Table: Route
-- =====================================================
CREATE TABLE Route (	
    route_id INT PRIMARY KEY AUTO_INCREMENT,
    route_name VARCHAR(100) UNIQUE NOT NULL,
    origin_id INT,
    destination_id INT, 
    distance DECIMAL(10,2),
    travel_time TIME,
    base_fare DECIMAL(10,2),
    
    -- BUG FIX #1: Changed from CASCADE to RESTRICT
    -- This prevents terminal deletion if routes exist
    FOREIGN KEY (origin_id) REFERENCES Terminal(terminal_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,  -- CHANGED: Prevents deletion
        
    FOREIGN KEY (destination_id) REFERENCES Terminal(terminal_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT  -- CHANGED: Prevents deletion
    
    -- BUG FIX #2: CHECK constraint removed due to MySQL limitation
    -- Validation handled in RouteController.java (already implemented)
);

-- =====================================================
-- 4.0 Table: Bus
-- =====================================================
CREATE TABLE Bus (
    bus_id INT PRIMARY KEY AUTO_INCREMENT,
    bus_number VARCHAR(20) UNIQUE NOT NULL,
    capacity INT NOT NULL DEFAULT 45,
    status ENUM('Available', 'In Transit', 'Scheduled', 'Maintenance', 'Out of Order') DEFAULT 'Available',
    current_terminal INT,
    
    FOREIGN KEY (current_terminal) REFERENCES Terminal(terminal_id)
        ON UPDATE CASCADE
        ON DELETE SET NULL,  -- Bus can exist without terminal assignment
        
    CHECK (capacity BETWEEN 10 AND 100)
);

-- =====================================================
-- 5.0 Table: Schedule
-- =====================================================
CREATE TABLE Schedule (
    schedule_id INT PRIMARY KEY AUTO_INCREMENT,
    bus_id INT NOT NULL,
    route_id INT NOT NULL,
    departure_time DATETIME NOT NULL,
    arrival_time DATETIME,
    status ENUM('Scheduled', 'Departed', 'In Transit', 'Completed', 'Cancelled') DEFAULT 'Scheduled',
    
    -- CHANGED: RESTRICT prevents deleting bus/route with schedules
    FOREIGN KEY (bus_id) REFERENCES Bus(bus_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,  -- CHANGED: Can't delete bus with schedules
        
    FOREIGN KEY (route_id) REFERENCES Route(route_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,  -- CHANGED: Can't delete route with schedules
        
    CONSTRAINT chk_valid_time CHECK (arrival_time > departure_time)
);

-- =====================================================
-- 6.0 Table: Ticket
-- =====================================================
CREATE TABLE Ticket (
    ticket_id INT PRIMARY KEY AUTO_INCREMENT,
    ticket_number VARCHAR(20) UNIQUE NOT NULL,
    schedule_id INT NOT NULL,
    discounted BOOLEAN DEFAULT FALSE,
    
    FOREIGN KEY (schedule_id) REFERENCES Schedule(schedule_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE  -- OK: Deleting schedule should delete tickets (refund)
);

-- =====================================================
-- 7.0 Table: Role
-- =====================================================
CREATE TABLE Role (
    role_id INT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) UNIQUE NOT NULL  -- ADDED UNIQUE
);

-- =====================================================
-- 8.0 Table: Staff
-- =====================================================
CREATE TABLE Staff (
    staff_id INT PRIMARY KEY AUTO_INCREMENT,
    staff_name VARCHAR(100) NOT NULL,
    role_id INT,
    assigned_terminal INT,
    assigned_bus INT,
    shift ENUM('Morning', 'Evening'),
    contact VARCHAR(50) UNIQUE,  -- ADDED UNIQUE per requirements
    
    FOREIGN KEY (assigned_terminal) REFERENCES Terminal(terminal_id)
        ON UPDATE CASCADE
        ON DELETE SET NULL,
        
    FOREIGN KEY (assigned_bus) REFERENCES Bus(bus_id)
        ON UPDATE CASCADE
        ON DELETE SET NULL,
        
    FOREIGN KEY (role_id) REFERENCES Role(role_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,  -- CHANGED: Can't delete role if staff using it
    
    -- BUG FIX #3: Added UNIQUE constraint
    -- Prevents multiple staff with same role+shift on same bus
    CONSTRAINT unique_bus_role_shift UNIQUE (assigned_bus, role_id, shift)
);

-- =====================================================
-- 9.0 Table: Maintenance_Type
-- =====================================================
CREATE TABLE Maintenance_Type (
    maintenance_type_id INT PRIMARY KEY AUTO_INCREMENT,
    type_name VARCHAR(100) NOT NULL UNIQUE,
    maintenance_cost DECIMAL(10,2) NOT NULL,
    CHECK (maintenance_cost >= 0)
);

-- =====================================================
-- 10.0 Table: Maintenance
-- =====================================================
CREATE TABLE Maintenance (
    maintenance_id INT PRIMARY KEY AUTO_INCREMENT,
    bus_id INT NOT NULL,
    assigned_mechanic INT NOT NULL,
    maintenance_type_id INT,
    starting_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    completion_time DATETIME,
    
    FOREIGN KEY (bus_id) REFERENCES Bus(bus_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,  -- OK: If bus deleted, maintenance records deleted
        
    FOREIGN KEY (maintenance_type_id) REFERENCES Maintenance_Type(maintenance_type_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,  -- Can't delete type if being used
        
    FOREIGN KEY (assigned_mechanic) REFERENCES Staff(staff_id)
        ON UPDATE CASCADE
        ON DELETE  RESTRICT,  -- CHANGED: Maintenance record remains if mechanic leaves
        
    CHECK (completion_time IS NULL OR completion_time > starting_date),

    -- prevents duplicate maintenance types on same bus at same time
    CONSTRAINT unique_maintenance_record UNIQUE (bus_id, maintenance_type_id, starting_date)
);

