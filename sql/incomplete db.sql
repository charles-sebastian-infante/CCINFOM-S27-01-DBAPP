-- =====================================================
-- 0.0 Reset
-- =====================================================
-- 1.0 Create Database

CREATE DATABASE bus_terminal_management;
USE bus_terminal_management; 

-- =====================================================
-- 2.0 Table: Terminal
-- =====================================================
CREATE TABLE Terminal (
    terminal_id INT PRIMARY KEY AUTO_INCREMENT,
    terminal_name VARCHAR(100) UNIQUE NOT NULL,
    address VARCHAR(255) NOT NULL,
    phone VARCHAR(20) 
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
    base_fare DECIMAL(10,2), -- this is where we get the price
    FOREIGN KEY (origin_id) REFERENCES Terminal(terminal_id)
        ON UPDATE CASCADE
        ON DELETE SET NULL,
    FOREIGN KEY (destination_id) REFERENCES Terminal(terminal_id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

-- =====================================================
-- 5.0 Table: Bus
-- Each bus is linked to a predetermined schedule
-- =====================================================
CREATE TABLE Bus (
    bus_id INT PRIMARY KEY AUTO_INCREMENT,
    bus_number VARCHAR(20) UNIQUE NOT NULL,
    capacity INT NOT NULL DEFAULT 45,
    status ENUM('Available', 'In Transit', 'Scheduled', 'Maintenance') DEFAULT 'Available',
    current_terminal INT,
    FOREIGN KEY (current_terminal) REFERENCES Terminal(terminal_id)
        ON UPDATE CASCADE
        ON DELETE SET NULL,
	CHECK (capacity BETWEEN 10 AND 100)
);

-- =====================================================
-- 4.0 Table: Schedule
-- =====================================================

CREATE TABLE Schedule (
    schedule_id INT PRIMARY KEY AUTO_INCREMENT,
    bus_id INT NOT NULL,
	route_id INT NOT NULL,
    departure_time DATETIME NOT NULL,
    arrival_time DATETIME,
    status ENUM('Scheduled', 'Departed', 'Completed', 'Cancelled') DEFAULT 'Scheduled',
    FOREIGN KEY (bus_id) REFERENCES Bus(bus_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
	FOREIGN KEY (route_id) REFERENCES Route(route_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
	CONSTRAINT chk_valid_time CHECK (arrival_time > departure_time)
);

-- =====================================================
-- 5.0 Table: Ticket
-- =====================================================
CREATE TABLE Ticket (
    ticket_id INT PRIMARY KEY AUTO_INCREMENT,
    ticket_number VARCHAR(20) UNIQUE NOT NULL,
    schedule_id INT NOT NULL,
    discounted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (schedule_id) REFERENCES Schedule(schedule_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- =====================================================
-- 6.0 Table: Staff
-- =====================================================
CREATE TABLE Role (
    role_id INT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) -- ('Driver', 'Conductor', 'Mechanic', 'Manager')
);

CREATE TABLE Staff (
    staff_id INT PRIMARY KEY AUTO_INCREMENT,
    staff_name VARCHAR(100) NOT NULL,
    role_id INT,
    assigned_terminal INT,
    assigned_bus INT,
    shift ENUM('Morning', 'Evening'),
    contact VARCHAR(50),
    FOREIGN KEY (assigned_terminal) REFERENCES Terminal(terminal_id)
        ON UPDATE CASCADE
        ON DELETE SET NULL,
    FOREIGN KEY (assigned_bus) REFERENCES Bus(bus_id)
        ON UPDATE CASCADE
        ON DELETE SET NULL,
	FOREIGN KEY (role_id) REFERENCES Role(role_id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

-- =====================================================
-- 8.0 Table: Maintenance
-- =====================================================

CREATE TABLE Maintenance_Type (
    maintenance_type_id INT PRIMARY KEY AUTO_INCREMENT,
    type_name VARCHAR(100) NOT NULL UNIQUE,
    maintenance_cost DECIMAL(10,2) NOT NULL
);

CREATE TABLE Maintenance (
    maintenance_id INT PRIMARY KEY AUTO_INCREMENT,
    bus_id INT NOT NULL,
    assigned_mechanic INT,
	maintenance_type_id INT,
    starting_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    completion_time DATETIME,
    FOREIGN KEY (bus_id) REFERENCES Bus(bus_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
	FOREIGN KEY (maintenance_type_id) REFERENCES Maintenance_Type(maintenance_type_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
	FOREIGN KEY (assigned_mechanic) REFERENCES Staff(staff_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
	CHECK (completion_time IS NULL OR completion_time > starting_date)
);





