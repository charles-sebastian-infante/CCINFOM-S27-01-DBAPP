# Bus Terminal Management System

A comprehensive database application for managing bus terminal operations including ticket sales, route scheduling, and fleet management.

## Project Information

**Course:** CCINFOM Database Application Project  
**Academic Year:** 2025-2026, Term 1  
**Institution:** De La Salle University

## Team Members

- **GUARIN, Raine Louise R.** - Terminal Management & Ticket Purchase
- **INFANTE, Charles Sebastian V.** - Route Management & Bus Departure Process  
- **MIRANDA, Bien Aouien C.** - Ticket Management & Bus Assignment
- **RANARA, Ramil Carlos B.** - Bus Management & Schedule Creation

## Project Overview

This system addresses the limitations of Excel-based bus terminal operations by providing:

- **Real-time ticket management** with capacity tracking
- **Multi-terminal coordination** for route scheduling
- **Automated fare calculation** with discount handling
- **Fleet management** with bus assignment and tracking
- **Comprehensive reporting** for operational insights

## System Features

### Core Record Management
- **Terminal Records** - Location and operational details
- **Bus Records** - Fleet information and status tracking
- **Ticket Records** - Passenger bookings with departure details
- **Route Records** - Origin/destination pairs with fare structure

### Key Transactions
1. **Ticket Purchase Process** - Complete booking workflow with capacity validation
2. **Bus Departure Process** - Passenger manifest and status updates
3. **Ticket Cancellation** - Refund processing with policy enforcement
4. **Route Schedule Creation** - Bus and driver assignment to routes

### Reporting Capabilities
- Terminal revenue analysis (daily/monthly)
- Route performance metrics
- Payment method summaries
- Bus utilization tracking

## Technology Stack

## Current Status

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
1. **Week 4-5:** Finalize technology stack decisions
2. **Week 6-7:** Set up development environment and database
3. **Week 8+:** Begin implementation according to proposal

## Development Guidelines

### Commit Message Format
Follow **Conventional Commits** standard:

| Type | Purpose | Example |
|------|---------|---------|
| `feat` | Add new feature (records, transactions, reports) | `feat: implement ticket purchase transaction` |
| `fix` | Fix bugs (database errors, logic issues) | `fix: correct fare calculation with discounts` |
| `refactor` | Improve code without changing behavior | `refactor: optimize database connection handling` |
| `perf` | Optimize performance (faster queries, better memory) | `perf: improve route search query speed` |
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

## Notes

This README will be updated as the project develops. Currently, only the proposal has been finalized and approved by Professor Francia.

For the most up-to-date project requirements, refer to the approved proposal document.

---

**Last Updated:** September 2025  
**Project Status:** Proposal Approved - Development Phase Starting
