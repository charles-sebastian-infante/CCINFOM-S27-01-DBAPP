# CCINFOM Database Application Project Proposal
**Version 1.2, subject to minor revisions**  
**Bus Terminal Management System**  
**AY 2025-2026, Term 1**  
*initially approved 2025-09-17*

## Section 1.0: Group Composition
- **GUARIN, Raine Louise R.**
- **INFANTE, Charles Sebastian V.**
- **MIRANDA, Bien Aouien C.**
- **RANARA, Ramil Carlos B.**

## Section 2.0: Rationale
Bus terminal operations require coordinated management of routes, passenger bookings, schedules, and staff assignments, requiring real-time coordination across terminals. Excel is limited in handling concurrent sales, preventing double-bookings and managing dynamic schedules, which can cause errors and passenger dissatisfaction. A dedicated database application ensures data consistency, prevents booking conflicts, enables real-time seat tracking, and provides reliable transaction management.

## Section 3.0: Records Management 
| Core Record Table | Fields | Description | Assigned Member |
|-|-|-|-|
|Terminal Record Management|terminal_id <br> terminal_name <br> address <br> phone<br>| Viewing a terminal record and the list of all bus routes operating from that terminal.| GUARIN, Raine Louise R.
|Bus Record Management|bus_id<br>bus_number<br>capacity<br>current_terminal<br>status|Viewing a bus record and the list of all trips completed by that bus.| RANARA, Ramil Carlos B.
|Staff Record Management| staff_id<br>staff_name<br>roleID<br>assigned_terminal<br>assigned_bus<br>shift<br>contact<br>| Viewing a member of staff and the list of assigned bus and terminals. | MIRANDA, Bien Aouien C.
|Route Record Management|route_id<br>route_name<br>origin_id<br>destination_id<br>distance<br>travel_time<br>base_fare<br>|Viewing a route record and the list of scheduled departures for that route. |INFANTE, Charles Sebastian V.


## Section 4.0: Transactions
**Transaction 1: Bus Maintenance Process assigned to RANARA**
1. Reading the maintenance record to verify maintenance status
2. Updating the maintenance record’s status from “Pending” to “Completed”
3. Updating the bus status from “Maintenance” to “Available”

<br>**Transaction 2: Staff Reassignment Process assigned to INFANTE**
1. Reading the Staff record to check the staff role and status
2. Checking other tables (bus or maintenance) that has an unoccupied spot for the role
3. Validating with staff shift and current schedule of the bus or maintenance
4. Reallocating staff to the new assigned bus or maintenance

<br>**Transaction 3: Ticket Purchase Process assigned to GUARIN**
1. Reading available departure schedules for the requested route and travel date.
2. Checking ticket availability for the selected departure
3. Calculating the final ticket price including any applicable discounts
4. Recording the ticket purchase with route, bus and schedule information, and fare details directly in ticket record
5. Recording booking date and transaction details in the ticket record
6. Generating unique ticket number with booking confirmation

<br> **Transaction 4: Bus Scheduling Process assigned to MIRANDA**
1. Reading route record to verify route details and operational requirements  
2. Reading available buses to find suitable bus for assignment based on capacity and status   
3. Updating bus record with assigned route and scheduled departure information  
4. Updating bus status to "Scheduled" for the assigned route and departure time  
5. Recording schedule assignment details and confirmation


## Section 5.0: Reports to be Generated
|Report|Description|Assigned To|
|-|-|-|
|Trip Schedule Report|(departure & arrival time, bus information, and route) per terminal for a given day. |MIRANDA|
|Route Usage Analysis|(number and total amount of passenger bookings) per route for a given year and month. |INFANTE|
|Bus Maintenance Summary|(total payment amounts and transaction count) per day for a given year and month. | RANARA|
|Bus Utilization Report|(number of trips and total passengers carried) per bus for a given year and month. | GUARIN|

## Section 6.0: Declaration of Generative AI Use
Group 1 declares that Generative AI was used only for improving the writing and grammar for the Database App Proposal. The group did not use generative AI for ideating, changing, or adding any information to the proposal.