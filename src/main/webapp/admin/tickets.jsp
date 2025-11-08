<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Tickets</title>
    <link rel="stylesheet" href="../assets/css/admin.css">
    <style>
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ccc; padding: 8px; text-align: left; }
        th { background: #f4f4f4; }
        .form-block { margin: 20px 0; padding: 12px; border: 1px solid #ddd; background:#fafafa; }
        .btn { padding:6px 10px; margin-right:6px; cursor: pointer; }
        .btn-book { background:#27ae60; color:#fff; border:none; }
        .btn-cancel { background:#95a5a6; color:#fff; border:none; }
        .btn-delete { background:#e74c3c; color:#fff; border:none; }
        .available { color: #27ae60; font-weight: bold; }
        .limited { color: #f39c12; font-weight: bold; }
        .full { color: #e74c3c; font-weight: bold; }
        .status-scheduled { color: #27ae60; }
        .status-departed { color: #3498db; }
        .status-completed { color: #95a5a6; }
        .status-cancelled { color: #e74c3c; }
    </style>
    <script>
        var currentPrice = 0;
        var currentCapacity = 0;

        function showBookingForm(scheduleID, busNumber, route, departureTime, arrivalTime, availableSeats, price) {
            if (availableSeats === 0) {
                alert('This bus is fully booked! Please choose another bus.');
                return;
            }

            currentPrice = price;
            currentCapacity = availableSeats;

            document.getElementById('bookingForm').style.display = 'block';
            document.getElementById('scheduleID').value = scheduleID;
            document.getElementById('busDetailsDisplay').innerHTML =
                '<strong>Bus:</strong> ' + busNumber + '<br>' +
                '<strong>Route:</strong> ' + route + '<br>' +
                '<strong>Departure:</strong> ' + departureTime + '<br>' +
                '<strong>Arrival:</strong> ' + arrivalTime + '<br>' +
                '<strong>Available Seats:</strong> ' + availableSeats + '<br>' +
                '<strong>Price per Ticket:</strong> ₱' + price.toFixed(2);

            document.getElementById('ticketQuantity').max = availableSeats;
            document.getElementById('maxSeats').textContent = availableSeats;
            document.getElementById('ticketQuantity').value = 1;
            calculateTotal();
        }

        function calculateTotal() {
            var quantity = parseInt(document.getElementById('ticketQuantity').value) || 1;
            var discounted = document.getElementById('discounted').checked;

            // Checks if the quantity exceeds the available seats
            if (quantity > currentCapacity) {
                document.getElementById('ticketQuantity').value = currentCapacity;
                quantity = currentCapacity;
            }

            var pricePerTicket = discounted ? currentPrice * 0.8 : currentPrice; // 20% discount
            var totalPrice = pricePerTicket * quantity;

            document.getElementById('pricePerTicket').textContent = '₱' + pricePerTicket.toFixed(2);
            document.getElementById('totalPrice').textContent = '₱' + totalPrice.toFixed(2);
        }

        function cancelBooking() {
            document.getElementById('bookingForm').style.display = 'none';
            document.getElementById('ticketQuantity').value = 1;
            document.getElementById('discounted').checked = false;
            calculateTotal();
        }

        function validateBooking(form) {
            var quantity = parseInt(form.ticketQuantity.value);

            if (quantity < 1) {
                alert('You must purchase at least 1 ticket.');
                return false;
            }

            if (quantity > currentCapacity) {
                alert('Cannot book ' + quantity + ' tickets. Only ' + currentCapacity + ' seats are available.');
                return false;
            }

            if (confirm('Book ' + quantity + ' ticket(s)?')) {
                return true;
            }
            return false;
        }

        function confirmDeleteTicket(ticketNumber) {
            return confirm('Are you sure you want to cancel ticket ' + ticketNumber + '?');
        }
    </script>
</head>

<body>
    <h1> Trip TimeTable & Ticket Booking</h1>

    <% if(request.getAttribute("message") != null) { %>
    <p style="color:green;"><%= request.getAttribute("message") %></p>
    <% } %>

    <% if(request.getAttribute("error") != null) { %>
    <p style="color:red;"><%= request.getAttribute("error") %></p>
    <% } %>

    <!-- Available Schedules Table -->
    <% if(request.getAttribute("schedules") != null) {
        java.util.List<com.busterminal.model.Schedule> scheduleList =
                (java.util.List<com.busterminal.model.Schedule>) request.getAttribute("schedules");

        if(scheduleList.size() == 0) { %>
    <p>There are no buses available today.</p>
    <% } else { %>
    <h2>Today's TimeTable</h2>
    <table>
        <thead>
        <tr>
            <th>Bus Number</th>
            <th>Route</th>
            <th>Departure Time</th>
            <th>Arrival Time</th>
            <th>Capacity</th>
            <th>Available Seats</th>
            <th>Price</th>
            <th>Status</th>
            <th>Action</th>
        </tr>
        </thead>
        <tbody>
        <% for(com.busterminal.model.Schedule schedule : scheduleList) {
            // Get the count for booked tickets for this schedule
            int bookedSeats = 0;
            if(request.getAttribute("bookingCounts") != null) {
                java.util.Map<Integer, Integer> bookingCounts =
                        (java.util.Map<Integer, Integer>) request.getAttribute("bookingCounts");
                bookedSeats = bookingCounts.getOrDefault(schedule.scheduleID, 0);
            }

            int availableSeats = bus.capacity - bookedSeats;
            String availabilityClass = availableSeats == 0 ? "full" :
                    availableSeats <= 5 ? "limited" : "available";
            String availabilityText = availableSeats == 0 ? "FULL" :
                    availableSeats + " seats";
            String statusClass = "status-" + schedule.status.toLowerCase().replace(" ", "-");
        %>
        <tr>
            <td><%= schedule.busID %></td>
            <td><%= route.routeName %></td>
            <td><%= schedule.departureTime %></td>
            <td><%= schedule.arrivalTime %></td>
            <td><%= bus.capacity %></td>
            <td class="<%= availabilityClass %>"><%= availabilityText %></td>
            <td>₱<%= String.format("%.2f", route.baseFare) %></td>
            <td class="<%= statusClass %>"><%= schedule.status %></td>
            <td>
                <% if(availableSeats > 0 && "Scheduled".equals(schedule.status)) { %>
                <button class="btn btn-book"
                        onclick="showBookingForm(<%= schedule.scheduleID %>,
                                '<%= schedule.busID %>',
                                '<%= route.routeName %>',
                                '<%= schedule.departureTime %>',
                                '<%= schedule.arrivalTime %>',
                            <%= availableSeats %>,
                            <%= route.baseFare %>)">
                    Book Ticket
                </button>
                <% } else if(availableSeats == 0) { %>
                <span class="full">FULLY BOOKED</span>
                <% } else { %>
                <span>Not Available</span>
                <% } %>
            </td>
        </tr>
        <% } %>
        </tbody>
    </table>
    <% }
    } %>

    <!-- Booking Form (Hidden by default) -->
    <div id="bookingForm" class="form-block" style="display:none;">
        <h2>Book Tickets</h2>
        <div id="busDetailsDisplay" style="margin-bottom: 15px; line-height: 1.8;"></div>

        <form method="POST" action="<%= request.getContextPath() %>/ticket" onsubmit="return validateBooking(this)">
            <input type="hidden" name="action" value="book">
            <input type="hidden" name="scheduleID" id="scheduleID">

            <label>Number of Tickets:<br>
                <input type="number"
                       name="ticketQuantity"
                       id="ticketQuantity"
                       min="1"
                       value="1"
                       required
                       style="width: 100px;"
                       onchange="calculateTotal()"
                       oninput="calculateTotal()">
                <span style="color:#666; margin-left:10px;">(Max: <span id="maxSeats"></span> seats)</span>
            </label><br><br>

            <label>
                <input type="checkbox" name="discounted" id="discounted" value="1" onchange="calculateTotal()">
            </label><br><br>

            <div style="font-size: 16px; margin: 15px 0; padding: 10px; background: #f0f0f0; border-radius: 5px;">
                <div><strong>Price per Ticket:</strong> <span id="pricePerTicket">₱0.00</span>
                </div>
                <div style="margin-top: 8px; font-size: 18px;">
                    <strong>Total Price:</strong> <span id="totalPrice" style="color: #27ae60;">₱0.00</span>
                </div>
            </div>

            <button type="submit" class="btn btn-book">Confirm Booking</button>
            <button type="button" class="btn btn-cancel" onclick="cancelBooking()">Cancel</button>
        </form>
    </div>

    <!-- Booked Tickets Section -->
    <% if(request.getAttribute("tickets") != null) {
        java.util.List<com.busterminal.model.Ticket> ticketList =
                (java.util.List<com.busterminal.model.Ticket>) request.getAttribute("tickets");

        if(ticketList.size() > 0) { %>
    <h2>My Booked Tickets</h2>
    <table>
        <thead>
        <tr>
            <th>Ticket Number</th>
            <th>Bus Number</th>
            <th>Route</th>
            <th>Departure</th>
            <th>Discounted</th>
            <th>Status</th>
            <th>Actions</th>
        </tr>
        </thead>
        <tbody>
        <% for(com.busterminal.model.Ticket ticket : ticketList) { %>
        <tr>
            <td><%= ticket.ticketNumber %></td>
            <td><%= bus.busNumber != null ? bus.busNumber : "N/A" %></td>
            <td><%= route.routeName != null ? route.routeName : "N/A" %></td>
            <td><%= schedule.departureTime != null ? ticket.departureTime : "N/A" %></td>
            <td><%= ticket.discounted == 1 ? "Yes (20% off)" : "No" %></td>
            <td class="status-<%= ticket.scheduleStatus != null ? ticket.scheduleStatus.toLowerCase() : "scheduled" %>">
            </td>
            <td>
                <% if("Scheduled".equals(ticket.scheduleStatus) || ticket.scheduleStatus == null) { %>
                <form method="POST"
                      action="<%= request.getContextPath() %>/ticket"
                      style="display:inline;"
                      onsubmit="return confirmDeleteTicket('<%= ticket.ticketNumber %>')">
                    <input type="hidden" name="action" value="cancel">
                    <input type="hidden" name="ticketID" value="<%= ticket.ticketID %>">
                    <button type="submit" class="btn btn-delete">Cancel</button>
                </form>
                <% } else { %>
                <span style="color:#999;">Cannot cancel</span>
                <% } %>
            </td>
        </tr>
        <% } %>
        </tbody>
    </table>
    <% }
    } %>

</body>
</html>