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
    terminal_name VARCHAR(100) NOT NULL,
    location VARCHAR(150),
    city VARCHAR(100),
    phone VARCHAR(20)
);

-- =====================================================
-- 3.0 Table: Route
-- =====================================================
CREATE TABLE Route (
    route_id INT PRIMARY KEY AUTO_INCREMENT,
    route_name VARCHAR(100) NOT NULL,
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
    route_id INT,  -- predetermined route
    FOREIGN KEY (current_terminal) REFERENCES Terminal(terminal_id)
        ON UPDATE CASCADE
        ON DELETE SET NULL,
    FOREIGN KEY (route_id) REFERENCES Route(route_id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

-- =====================================================
-- 4.0 Table: Schedule
-- A schedule defines the route and time details
-- =====================================================

CREATE TABLE Schedule (
    schedule_id INT PRIMARY KEY AUTO_INCREMENT,
    bus_id INT NOT NULL,
    departure_datetime DATETIME NOT NULL,
    expected_arrival DATETIME,
    actual_departure DATETIME,
    actual_arrival DATETIME,
    status ENUM('Scheduled', 'Departed', 'Completed', 'Cancelled') DEFAULT 'Scheduled',
    FOREIGN KEY (bus_id) REFERENCES Bus(bus_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);



-- =====================================================
-- 6.0 Table: Staff
-- =====================================================
CREATE TABLE Staff (
    staff_id INT PRIMARY KEY AUTO_INCREMENT,
    staff_name VARCHAR(100) NOT NULL,
    role ENUM('Driver', 'Conductor', 'Mechanic', 'Manager') NOT NULL,
    assigned_terminal INT,
    assigned_bus INT,
    shift VARCHAR(50),
    contact VARCHAR(50),
    FOREIGN KEY (assigned_terminal) REFERENCES Terminal(terminal_id)
        ON UPDATE CASCADE
        ON DELETE SET NULL,
    FOREIGN KEY (assigned_bus) REFERENCES Bus(bus_id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

-- =====================================================
-- 7.0 Table: Ticket
-- Tickets are tied to bus and its schedule
-- =====================================================
CREATE TABLE Ticket (
    ticket_id INT PRIMARY KEY AUTO_INCREMENT,
    ticket_number VARCHAR(50) UNIQUE NOT NULL,
    bus_id INT NOT NULL,
    schedule_id INT NOT NULL,
    departure_date DATETIME,
    type ENUM('Regular', 'Discounted', 'Free') DEFAULT 'Regular', 
    discount DECIMAL(10,2) DEFAULT 20.00,  -- Discount is 20%
    final_amount DECIMAL(10,2),
    route_id  INT,
    staff_id INT,
    FOREIGN KEY (bus_id) REFERENCES Bus(bus_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    FOREIGN KEY (schedule_id) REFERENCES Schedule(schedule_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
	FOREIGN KEY (staff_id) REFERENCES Staff(staff_id)
		ON UPDATE CASCADE
        ON DELETE CASCADE,
	FOREIGN KEY (route_id) REFERENCES Route(route_id)
		ON UPDATE CASCADE
        ON DELETE CASCADE
	
);

-- =====================================================
-- 8.0 Table: Maintenance
-- =====================================================
CREATE TABLE Maintenance (
    maintenance_id INT PRIMARY KEY AUTO_INCREMENT,
    bus_id INT NOT NULL,
    maintenance_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    description TEXT,
    status ENUM('Pending', 'Completed') DEFAULT 'Pending',
    FOREIGN KEY (bus_id) REFERENCES Bus(bus_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);


-- drop database bus_terminal_management;


