# Hotel Booking & Management System

A full-featured **Hotel Booking & Management System** built with **Spring Boot** that allows users to browse hotels, check room availability, make bookings, add guest details, and complete payments securely. The system also provides hotel management features such as room inventory control, booking status handling, and hotel administration operations.

---

## 🚀 Features

### User Features

* Browse hotels and rooms
* View hotel details and room information
* Search room availability based on check-in and check-out dates
* Book rooms by selecting room type and number of rooms
* Add guest details to a booking
* Secure payment integration using **Stripe**
* Track booking status

### Admin / Hotel Management Features

* Manage hotels and room details
* Maintain room inventory by date
* Control room pricing and availability
* View and manage bookings
* Handle booking lifecycle and reservation flow
* Manage hotel-related resources through REST APIs

### Booking Workflow

The booking flow is designed in multiple stages:

1. **Initialize Booking**

   * User selects hotel, room, check-in date, check-out date, and number of rooms.
   * System checks inventory availability.
   * Rooms are reserved temporarily.
   * A booking record is created with status such as `RESERVED`.

2. **Add Guest Details**

   * Guest information is linked to the booking.
   * Booking moves to the next state such as `GUESTS_ADDED`.

3. **Payment Processing**

   * Stripe checkout session is created.
   * User completes payment through Stripe.
   * Stripe webhook confirms the payment.
   * Booking status is updated accordingly.

---

## 🛠 Tech Stack

### Backend

* **Java**
* **Spring Boot**
* **Spring Web**
* **Spring Data JPA**
* **Spring Security**
* **Hibernate**
* **Lombok**
* **ModelMapper**

### Database

* **MySQL**

### Payment Gateway

* **Stripe**

### Build Tool

* **Maven**

---

## 📂 Project Structure

```bash
src/
 ┣ main/
 ┃ ┣ java/com/yourpackage/
 ┃ ┃ ┣ controller/        # REST Controllers
 ┃ ┃ ┣ service/           # Business logic
 ┃ ┃ ┣ repository/        # JPA repositories
 ┃ ┃ ┣ entity/            # Entity classes
 ┃ ┃ ┣ dto/               # Request/response DTOs
 ┃ ┃ ┣ config/            # Security and app configurations
 ┃ ┃ ┣ exception/         # Custom exceptions and handlers
 ┃ ┃ ┗ util/              # Utility/helper classes
 ┃ ┗ resources/
 ┃   ┣ application.properties
 ┃   ┗ data.sql / schema.sql (optional)
 ┗ test/
```

---

## 🧩 Main Modules

### 1. Hotel Module

Handles hotel-related operations such as:

* Creating hotels
* Fetching hotel details
* Managing hotel information
* Hotel ownership/management features

### 2. Room Module

Responsible for:

* Creating room entries for a hotel
* Managing room categories/types
* Defining room capacity, price, and other attributes

### 3. Inventory Module

Maintains date-wise room availability:

* Available room count for each date
* Room reservation locking during booking
* Prevents overbooking

### 4. Booking Module

Handles:

* Booking initialization
* Room reservation
* Booking status updates
* Guest association with bookings

### 5. Guest Module

Stores guest information associated with a booking.

### 6. Payment Module

Handles:

* Stripe checkout session creation
* Stripe webhook verification
* Payment success/failure updates
* Booking confirmation after successful payment

---

## 🔄 Booking Lifecycle

Example booking statuses used in the system:

* `RESERVED` – room is temporarily reserved after availability check
* `GUESTS_ADDED` – guest details are attached to the booking
* `CONFIRMED` / `PAID` – payment successful and booking confirmed
* `CANCELLED` – booking cancelled

> You can update the status names based on your actual enum values used in the project.

---

## 🗃 Database Entities

The project may contain entities similar to the following:

* **User**
* **Hotel**
* **Room**
* **Inventory**
* **Booking**
* **Guest**
* **Payment / Stripe session tracking**

### Example Booking Fields

A booking typically stores:

* `hotelId`
* `roomId`
* `checkedInDate`
* `checkedOutDate`
* `roomsCount`
* `paymentSessionId`
* `bookingStatus`

---

## 🔐 Security

The application uses **Spring Security** for securing endpoints.

Possible security features:

* Authentication and authorization
* Role-based access control
* Protected admin/hotel-owner endpoints
* User-specific booking operations

If implemented in your project, mention whether you use:

* JWT authentication
* Session-based authentication
* OAuth2 / Google login

---

## 💳 Stripe Payment Integration

Stripe is used for online payment processing.

### Payment Flow

1. User completes booking details.
2. Backend creates a Stripe Checkout Session.
3. Stripe redirects user to payment page.
4. On successful payment, Stripe sends a webhook event.
5. Backend verifies the event and updates the booking/payment status.

### Common Stripe Events Used

* `checkout.session.completed`

### Stripe Integration Notes

* Store Stripe secret key in environment variables or application properties.
* Use webhook signing secret to validate webhook events.
* Avoid hardcoding keys in source code.

---

## ⚙️ API Endpoints (Sample)

> Replace these with your actual endpoints.

### Hotel APIs

```http
GET    /api/hotels
GET    /api/hotels/{id}
POST   /api/hotels
PUT    /api/hotels/{id}
DELETE /api/hotels/{id}
```

### Room APIs

```http
GET    /api/rooms
GET    /api/rooms/{id}
POST   /api/rooms
PUT    /api/rooms/{id}
DELETE /api/rooms/{id}
```

### Booking APIs

```http
POST   /api/bookings/init
POST   /api/bookings/{bookingId}/guests
GET    /api/bookings/{bookingId}
GET    /api/bookings/user
```

### Payment APIs

```http
POST   /api/payments/checkout
POST   /api/payments/webhook
```

---

## ▶️ How to Run the Project

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/hotel-booking-management-system.git
cd hotel-booking-management-system
```

### 2. Configure Database

Create a MySQL database, for example:

```sql
CREATE DATABASE hotel_booking_db;
```

### 3. Update `application.properties`

Configure your database and Stripe credentials.

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hotel_booking_db
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Stripe
stripe.secret.key=your_stripe_secret_key
stripe.webhook.secret=your_stripe_webhook_secret
```

### 4. Build the Project

```bash
mvn clean install
```

### 5. Run the Application

```bash
mvn spring-boot:run
```

The application will start at:

```bash
http://localhost:8080
```

---

## 🧪 Testing the Application

You can test the APIs using:

* **Postman**
* **Swagger** (if integrated)

Suggested test flow:

1. Create hotel and room data
2. Add inventory for rooms
3. Initialize booking with dates and room count
4. Add guest details
5. Create Stripe checkout session
6. Trigger or test Stripe webhook
7. Verify booking status update in database

---

## 📌 Important Business Logic

### Availability Check

Before creating a booking, the system checks:

* Whether the selected room exists
* Whether enough rooms are available for all selected dates
* Whether booking dates are valid

### Inventory Locking / Reservation

When booking is initialized:

* Room inventory is reserved for the selected date range
* Prevents multiple users from booking the same rooms beyond capacity

### Booking Validation

The system should validate:

* `checkedInDate` is not null
* `checkedOutDate` is not null
* `checkedOutDate` is after `checkedInDate`
* `roomsCount` is greater than 0

---

## ❗ Common Errors & Fixes

### 1. Null check-in/check-out date issue

If you get errors related to date calculations, make sure:

* `checkedInDate` and `checkedOutDate` are properly sent in the request body
* DTO fields match the JSON field names exactly
* date format is correct (e.g. `yyyy-MM-dd`)

### 2. Stripe webhook deserialization issue

If Stripe webhook data is not being parsed properly:

* Ensure the Stripe Java SDK version is compatible
* Verify the webhook event payload
* Use the correct event deserialization logic for `checkout.session.completed`

### 3. Foreign key / user issue in booking

If booking creation fails due to user relation:

* Make sure the authenticated user exists in the database
* Avoid using dummy user objects in production logic
* Fetch the current user from the Spring Security context

---

## 🌱 Future Enhancements

Possible improvements for the project:

* Email notifications after booking confirmation
* Booking cancellation and refund flow
* Hotel image upload support
* Review and rating system
* Advanced search and filtering
* Admin dashboard and analytics
* PDF invoice generation
* Redis caching for hotel search
* Docker deployment
* CI/CD pipeline integration

---

## 📸 Screenshots

You can add screenshots here if you have a frontend or API documentation UI.

Example:

* Home page
* Hotel listing page
* Booking page
* Stripe checkout page
* Admin dashboard

---

## 🤝 Contributing

Contributions are welcome.

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to your branch
5. Open a pull request

---

## 📄 License

This project is for educational and learning purposes.
You can choose a license such as **MIT License** if you want to make it open source.

---

## 👨‍💻 Author

**Ashirbada Mohanty**

If you want, you can also add:

* GitHub profile link
* LinkedIn profile link
* Portfolio link

---
