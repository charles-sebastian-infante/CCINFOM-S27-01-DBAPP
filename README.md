# Bus Terminal Management System

**PROJECT IN DEVELOPMENT**

A database application for managing bus terminal operations including ticket sales, route scheduling, and fleet management.

## Project Information

**Course:** CCINFOM Database Application Project  
**Academic Year:** 2025-2026, Term 1  
**Institution:** De La Salle University  
**Current Status:** Proposal Approved - Development Phase

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
- Route usage analysis (monthly/yearly)
- Bus maintenance summary (monthly)
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

### In Progress
- [ ] Core record management implementation
- [ ] Transaction development
- [ ] Report generation features
- [ ] UI development and integration
- [ ] Testing and debugging

## Getting Started

**Note: Setup instructions will be added as technology decisions are finalized**

### Current Repository Structure
```
bus-terminal-management/
├── docs/
│   └── proposal.md          # Approved project proposal
├── .gitignore              # Git ignore rules
└── README.md               # This file
```

### Next Steps for Team
1. **Week 8+:** Begin implementation according to proposal

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
- [Coming Soon] Database Design (docs/database-design.md)
- [Coming Soon] API Documentation (docs/api-reference.md)
- [Coming Soon] User Manual (docs/user-manual.md)

## Project Timeline

- **Week 4-5:** Proposal approved with minor feedback to address
- **Week 6-7:** Database design and development environment setup
- **Week 8-9:** Core record management development
- **Week 10-11:** Transaction implementation and testing
- **Week 12:** UI integration and final testing

## Notes

**Project Status:** The team has begun with developing the app. The backend has been slightly refactored and frontend for the most part needs styling.

For the most up-to-date project requirements, refer to the approved proposal document and any additional feedback from Professor Francia.

---

**Last Updated:** November 2025  
**Project Status:** In Development
