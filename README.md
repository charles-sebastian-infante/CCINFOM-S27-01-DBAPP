# Bus Terminal Management System

**PROJECT IN DEVELOPMENT**

A database application for managing bus terminal operations including ticket sales, route scheduling, and fleet management.

## Project Information

**Course:** CCINFOM Database Application Project  
**Academic Year:** 2025-2026, Term 1  
**Institution:** De La Salle University  
**Current Status:** Proposal Approved, Submitted

## Team Members

- **GUARIN, Raine Louise R.** - Ticket Purchase & Bus Utilization
- **INFANTE, Charles Sebastian V.** - Staff Reassignment & Route Usage
- **MIRANDA, Bien Aouien C.** - Bus Scheduling & Scheduled Trips
- **RANARA, Ramil Carlos B.** - Bus Maintenance & Maintenance Report

## Project Overview

Based on our approved proposal, this system will address the limitations of Excel-based bus terminal operations by providing:

- Real-time ticket management with capacity tracking
- Multi-terminal coordination for route scheduling
- Automated fare calculation with discount handling
- Fleet management with bus assignment and tracking
- Comprehensive reporting for operational insights

## Planned Features

### Core Record Management (From Proposal)
- **Terminal Records** - Location and operational details
- **Bus Records** - Fleet information and status tracking
- **Staff Records** - Staff information and shift tracking
- **Route Records** - Origin/destination pairs

### Planned Transactions
1. **Bus Maintenance Process** - Refund processing with policy enforcement
2. **Staff Reassignment Process** - Reallocation of staff across terminal network
3. **Ticket Purchase Process** - Complete booking workflow with capacity validation
4. **Bus Scheduling Process** - Bus and driver assignment to routes

### Planned Reports
- Scheduled trips dashboard
- Route usage analysis 
- Bus maintenance summary
- Bus utilization tracking

## Technology Stack

- **Backend:** Java (Servlets, JSP)
- **Frontend:** Jakarta Server Pages (JSP), HTML, CSS, JavaScript
- **Database:** MySQL
- **Architecture:** 3-tier system design (required by course)
- **Deployment:** Local deployment via Apache Tomcat 9

## Current Status

### Completed
- [x] Project proposal submitted and approved
- [x] Team member responsibilities assigned
- [x] Core database tables defined
- [x] Transaction workflows planned
- [x] Report requirements specified
- [X] Database system selection
- [X] UI framework decision
- [X] Database schema implementation
- [X] Development environment setup
- [X] Database design and creation
- [X] Sample data population (minimum 10 records per table)
- [X] Core record management implementation
- [X] Transaction development
- [X] Report generation features
- [X] UI development and integration
- [X] Testing and debugging

## Getting Started

### Prerequisite Downloads
- Apache Tomcat 9
- MySQL
- JDK Version 21 or Higher
- IntelliJ IDEA (Highly Recommended)

### Setup Instructions
*Note: This setup instruction assumes you have the IntelliJ IDEA IDE.*

1. Clone the repository
```
git clone https://github.com/charles-sebastian-infante/CCINFOM-S27-01-DBAPP.git
cd CCINFOM-S27-01-DBAPP
```

2. Set up database
- Create a MySQL Database named bus_terminal_management
- Run `bus_terminal_database.sql` and `dbapp_dummy_data_2.sql` (located in `sql/`)
- Configure `DBConnection.java` (located in `src/main/java/com.busterminal/utils`)

```
URL = "{your jdbc connection here}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
USER = "{your username}";
PASSWORD = "{your password}";
```
3. Download the "Smart Tomcat" plugin for IntelliJ IDEA

4. Configure Smart Tomcat instructions 
```
Tomcat server: {your tomcat folder}
Deployment directory : {/src/main/webapp}
Use classpath of module: {this directory}
```

5. Copy the MySQL JAR connector

### Current Repository Structure
```
CCINFOM-S27-01-DBAPP/
├── sql/                                # Database scripts
│   ├── bus_terminal_database.sql       # Main database schema
│   ├── dbapp_dummy_data.sql            # Sample data (version 1)
│   ├── dbapp_dummy_data_2.sql          # Sample data (version 2)
│   └── incomplete db.sql               # Work in progress
│
├── src/main/
│   ├── java/com/busterminal/           # Java source code
│   │   ├── controller/                 # Controllers
│   │   │   ├── BusController.java
│   │   │   ├── MaintenanceController.java
│   │   │   ├── ReportController.java
│   │   │   ├── RouteController.java
│   │   │   ├── ScheduleController.java
│   │   │   ├── StaffController.java
│   │   │   ├── TerminalController.java
│   │   │   └── TicketController.java
│   │   │
│   │   ├── model/                      # Data models
│   │   │   ├── Bus.java
│   │   │   ├── Maintenance.java
│   │   │   ├── MaintenanceType.java
│   │   │   ├── Role.java
│   │   │   ├── Route.java
│   │   │   ├── Schedule.java
│   │   │   ├── Staff.java
│   │   │   ├── Terminal.java
│   │   │   └── Ticket.java
│   │   │
│   │   ├── service/                    # Business logic services
│   │   │   ├── BusDepartureService.java
│   │   │   ├── BusMaintenanceAssignment.java
│   │   │   ├── ReportService.java
│   │   │   ├── ScheduleService.java
│   │   │   ├── StaffReassignmentService.java
│   │   │   ├── TicketCancellation.java
│   │   │   └── TicketPurchaseService.java
│   │   │
│   │   └── utils/                      # Utility classes
│   │       └── DBConnection.java
│   │
│   └── webapp/                         # Web application files
│       ├── admin/                      # Admin management pages
│       │   ├── maintenance_list.jsp
│       │   ├── manage_bus.jsp
│       │   ├── manage_reports.jsp
│       │   ├── manage_routes.jsp
│       │   ├── manage_schedules.jsp
│       │   ├── manage_staff.jsp
│       │   ├── manage_terminals.jsp
│       │   └── tickets.jsp
│       │
│       ├── style/                      # CSS stylesheets
│       │   ├── global.css
│       │   ├── index.css
│       │   ├── maintenance.css
│       │   ├── manage_bus.css
│       │   ├── manage_reports.css
│       │   ├── manage_routes.css
│       │   ├── manage_schedules.css
│       │   ├── manage_staff.css
│       │   ├── manage_terminals.css
│       │   └── tickets.css
│       │
│       ├── index.jsp                  # Home page
│       ├── test-db.jsp                # Database connection test
│       └── WEB-INF/
│           └── web.xml                # Web application deployment descriptor
│
├── docs/                               # Documentation
|   ├── approved-proposal.md            # Project proposal                         
│   └── database-schema.md              # Entity-Relation diagram
│
├── build.gradle                        # Gradle build configuration
├── gradle.properties                   # Gradle properties
├── gradlew                             # Gradle wrapper script (Unix)
├── gradlew.bat                         # Gradle wrapper script (Windows)
├── settings.gradle                     # Gradle settings
├── README.md                           # Project README
├── mysql-connector-java-8.0.13.jar     # MySQL JDBC driver
└── .git                                # Git repository (branch: main)
```

### Next Steps for Team
N/A

## Development Guidelines

### Commit Message Format
Follow **Conventional Commits** standard:

| Type | Purpose | Example |
|------|---------|---------|
| `feat` | Add new feature (records, transactions, reports) | `feat: implement ticket purchase transaction` |
| `fix` | Fix bugs (database errors, logic issues) | `fix: correct fare calculation with discounts` |
| `refactor` | Improve code without changing behavior | `refactor: optimize database connection handling` |
| `perf` | Optimize performance (faster queries, better memory) | `perf(database): improve route search query speed` |
| `style` | Formatting changes (indentation, comments) | `style: fix indentation in TicketDAO.java` |
| `test` | Add or update test cases | `test: add validation tests for bus capacity limits` |
| `build` | Modify build files or compilation setup | `build: update Maven dependencies for JDBC` |
| `docs` | Update README, specs, or comments | `docs: update database schema documentation` |
| `chore` | Non-code maintenance (renaming files, config) | `chore: update .gitignore for database files` |

**Format:**
```
<type>: <description>
[optional body]
[optional footer]
```

### Proposal Requirements (Must Implement)
- **Minimum 4 core record tables** with full CRUD operations
- **4 transactions** as specified in approved proposal  
- **4 reports** with time dimensions (Year/Month filtering)
- **3-tier architecture** implementation
- **Minimum 10 records per table** for demonstration

### Team Responsibilities
Each member is assigned specific components as per the approved proposal. See proposal document for detailed breakdown.

## Documentation

- [Completed] Project Proposal (docs/proposal.md) - Approved by Professor Francia
- [Completed] Database Design (docs/database-schema.png)

## Project Timeline

- **Week 4-5:** Proposal approved with minor feedback to address
- **Week 6-7:** Database design and development environment setup
- **Week 8-9:** Core record management development
- **Week 10-11:** Transaction implementation and testing
- **Week 12:** UI integration and final testing

## Notes

**Project Status:** The initial project has been accomplished.

For the most up-to-date project requirements, refer to the approved proposal document and any additional feedback from Professor Francia.

---

**Last Updated:** November 2025  
**Project Status:** Project has been submitted to Professor Francia. Subject for evaluation.
