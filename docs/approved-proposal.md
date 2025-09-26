# CCINFOM Database Application Project Proposal
**Version 1.1, subject to minor revisions**  
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
|Terminal Record Management|terminal_id <br> terminal_name <br> location <br> city<br>phone<br>operating_hours<br> | Viewing a terminal record and the list of all bus routes operating from that terminal.| GUARIN, Raine Louise R.
|Bus Record Management|bus_id<br>bus_number<br>capacity<br>bus_type<br>current_terminal<br>status|Viewing a bus record and the list of all trips completed by that bus.| RANARA, Ramil Carlos B.
|Staff Record Management| staff_id<br>staff_name<br>role<br>assigned_terminal<br>assigned_bus<br>shift<br>contact<br>| Viewing a member of staff and the list of assigned bus and terminals | MIRANDA, Bien Aouien C.
|Route Record Management|route_id<br>route_name<br>origin_id<br>destination_id<br>distance<br>travel_time<br>base_fare<br>|Viewing a route record and the list of scheduled departures for that route|INFANTE, Charles Sebastian V.


## Section 4.0: Transactions
**Transaction 1: Ticket Purchase Process - RANARA**  
1. Reading available departure schedules for the requested route and travel date.
2. Checking ticket availability for the selected departure.
3. Calculating the final ticket price including any applicable discounts.
4. Recording the ticket purchase with passenger details, route, bus, departure information, and fare details in the ticket record.
5. Recordings booking date and transaction details in the ticket record.
6. Generating unique ticket number with booking confirmation.

<br>**Transaction 2: Bus Departure Process - INFANTE**  
1. Reading the route record to verify route and timing information.
2. Reading all ticket records for the specific route, bus, departure date and time to get passenger manifest
3. Updating all ticket statuses from "Booked" to "Departed" for the specific departure
4. Recording the actual departure time in bus record
5. Updating the bus status to "In Transit"
6. Notifying relevant staff and updating terminal information systems

<br>**Transaction 3: Ticket Cancellation and Refund Process - GUARIN**  
1. Reading the original ticket record to verify booking details and passenger information
2. Checking cancellation eligibility based on departure time and terminal policies
3. Calculating refund amount based on standard cancellation fees
4. Updating the ticket status to “Cancelled” with cancellation timestamp
5. Recording refund transaction details by updating final amount in the ticket record
6. Updating the ticket capacity to reflect returned cancelled ticket
7. Generating cancellation confirmation receipt

<br>**Transaction 4: Bus Maintenance and Route Assignment  - MIRANDA**
1. Reading route record to verify route details and operational requirements
2. Reading available buses to find suitable bus for assignment based on capacity and status
3. Reading existing ticket records to check for scheduling conflicts
4. Updating bus record with assigned route and scheduled departure information
5. Updating bus status to "Scheduled" for the assigned route and departure time
6. Recording schedule assignment details and confirmation

## Section 5.0: Reports to be Generated
|Report|Description|Assigned To|
|-|-|-|
|Terminal Revenue Analysis|The total and average ticket sales per day, for a given year and month.|MIRANDA|
|Route Performance Analysis|The number and total amount of passenger bookings per route for a given year and month.|INFANTE|
|Daily Payment Summary|Total payment amounts and transaction counts by payment method per day for a given year and month. | RANARA|
|Bus Utilization Report|The number of trips and total carried passengers per bus for a given year and month.| GUARIN|

## Section 6.0: Declaration of Generative AI Use
Group 1 declares that Generative AI was used only for improving the writing and grammar for the Database App Proposal. The group did not use generative AI for ideating, changing, or adding any information to the proposal.