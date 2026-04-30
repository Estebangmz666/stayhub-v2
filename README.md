# StayHub V2

Backend REST API for managing users, accommodations, reservations, reviews, deposit reminders, and guest-host interactions.

## Overview

StayHub V2 is a Spring Boot backend for an accommodation booking platform. Guests can register, authenticate, create and manage reservations, pay reservation deposits through a simulated flow, and leave reviews after completed stays. Hosts can create accommodations, view accommodation reservations through reservation listings, deactivate their properties when allowed, and respond to reviews.

The project follows a layered architecture with JWT-based authentication, role-based authorization, static OpenAPI documentation, scheduled jobs, domain events, and email notifications.

## Current Features

- User signup and login with JWT authentication
- Password recovery and password change flows
- Role-based access with `HOST` and `GUEST`
- Accommodation creation for authenticated hosts
- Accommodation search by city with pagination
- Accommodation detail retrieval
- Accommodation soft delete with future-reservation validation
- Reservation creation for the full accommodation
- Reservation detail retrieval for guest owner or accommodation host
- Paginated reservation listing for the authenticated user
- Reservation update and cancellation flows with business rules
- Deposit payment confirmation flow
- Guest reviews for completed reservations
- One-time host responses to reviews
- Pending deposit notifications for the authenticated guest
- Email notifications for reservation creation, cancellation, and deposit reminders
- Scheduled reminder and automatic cancellation jobs for unpaid deposits
- Static OpenAPI contract in `src/main/resources/static/openapi.yaml`

## Business Rules

- Reservations are made for the whole accommodation. There is no room-level model.
- New reservations must be created at least 72 hours before check-in.
- A reservation date change that moves check-in within the next 72 hours requires the deposit to be already paid.
- Active reservations can only be cancelled at least 48 hours before check-in.
- Only the guest owner can update, cancel, or mark the deposit as paid for a reservation.
- A review can only be created for a `COMPLETED` reservation.
- Only one review is allowed per reservation.
- Only the host owner of the accommodation can answer a review, and only once.
- An accommodation cannot be deactivated if it has future active reservations.

## Tech Stack

- Java 21
- Spring Boot 4.0.4
- Spring Security
- Spring Data JPA
- PostgreSQL
- MapStruct 1.6.3
- Lombok
- JWT with `jjwt 0.13.0`
- springdoc OpenAPI Starter WebMVC UI 3.0.2
- Thymeleaf, used for email templates
- spring-dotenv 5.1.0
- Maven Wrapper

## Project Structure

```text
stayhub-v2/
|-- src/
|   |-- main/
|   |   |-- java/edu/uniquindio/stayhub_v2/
|   |   |   |-- config/
|   |   |   |-- controller/
|   |   |   |-- dto/
|   |   |   |-- event/
|   |   |   |-- exception/
|   |   |   |-- listener/
|   |   |   |-- mapper/
|   |   |   |-- model/
|   |   |   |-- repository/
|   |   |   |-- scheduler/
|   |   |   `-- service/
|   |   `-- resources/
|   |       |-- static/openapi.yaml
|   |       |-- templates/
|   |       |-- application.properties
|   |       `-- application-dev.properties
|   `-- test/
|       `-- java/edu/uniquindio/stayhub_v2/
|-- .mvn/
|-- mvnw
|-- mvnw.cmd
|-- pom.xml
|-- AGENTS.md
`-- README.md
```

## Architecture

```text
Controller -> Service -> Repository
                 |
                 -> DTO
                 -> Mapper
                 -> Domain events
                 -> Email listeners
                 -> Scheduled jobs
```

Responsibilities:

- Controllers expose REST endpoints and HTTP contracts.
- Services implement business rules and authorization-sensitive flows.
- Repositories handle persistence and custom JPA queries.
- DTOs define public request and response payloads.
- Mappers convert between entities and DTOs.
- Events and listeners decouple reservation side effects such as email notifications.
- Schedulers process deposit reminders and automatic cancellation logic.

## API Surface

### Users

- `POST /api/v2/users/auth/signup`
- `POST /api/v2/users/auth/login`
- `POST /api/v2/users/auth/forgot-password`
- `POST /api/v2/users/auth/reset-password`
- `PUT /api/v2/users/auth/change-password`

### Accommodations

- `POST /api/v2/accommodations`
- `GET /api/v2/accommodations?city=&page=&size=`
- `GET /api/v2/accommodations/{id}`
- `DELETE /api/v2/accommodations/{id}`

### Bookings

- `POST /api/v2/bookings/book`
- `GET /api/v2/bookings/{reservationId}`
- `GET /api/v2/bookings/my-reservations?page=&scope=`
- `PUT /api/v2/bookings/{reservationId}`
- `PATCH /api/v2/bookings/{reservationId}/cancel`
- `PATCH /api/v2/bookings/{reservationId}/deposit-paid`

### Reviews

- `POST /api/v2/accommodations/{accommodationId}/reviews`
- `GET /api/v2/accommodations/{accommodationId}/reviews`
- `POST /api/v2/reviews/{reviewId}/response`

### Notifications

- `GET /api/v2/notifications/payment-pending`

## Authentication and Authorization

StayHub uses stateless JWT authentication.

Supported roles:

```text
HOST
GUEST
```

General authorization behavior:

- Auth endpoints are public.
- The rest of the API requires authentication.
- Reservation management endpoints for update, cancellation, and deposit payment are guest-only.
- Review creation is guest-only and tied to the guest's completed reservation.
- Accommodation creation and accommodation deactivation are host-oriented flows.

## Payments and Deposits

StayHub models a simulated deposit flow. There is no integrated payment gateway.

Current payment-related behavior:

- The reservation creation response includes `depositAmount`, `bankAccountNumber`, and `paymentDeadline`.
- Guests can mark the deposit as paid through `PATCH /api/v2/bookings/{reservationId}/deposit-paid`.
- Pending deposit notifications are exposed through `GET /api/v2/notifications/payment-pending`.
- A scheduled job sends reminder emails for upcoming deposit deadlines.
- Another scheduled job cancels expired unpaid reservations.

## Email Notifications

Email delivery is handled through Spring Mail and Thymeleaf templates.

Current templates:

- `reservation-confirmation.html`
- `host-reservation-notification.html`
- `host-reservation-cancellation.html`
- `payment-reminder.html`

The default development setup points to Mailtrap-compatible SMTP configuration through environment variables.

## OpenAPI Documentation

The project maintains a static OpenAPI contract at:

```text
src/main/resources/static/openapi.yaml
```

When the application is running, springdoc endpoints are typically available at:

```text
http://localhost:8080/v3/api-docs
http://localhost:8080/swagger-ui/index.html
```

If an endpoint changes, update the code annotations and the static `openapi.yaml` in the same pass.

## Prerequisites

- Java 21
- PostgreSQL
- Maven is optional if you use the wrapper scripts included in the repository

## Environment Variables

The active `dev` profile expects configuration like the following:

```env
DB_URL=jdbc:postgresql://localhost:5432/stayhub
DB_USERNAME=your_database_username
DB_PASSWORD=your_database_password

JWT_SECRET_KEY=your_jwt_secret_key
JWT_TIME_EXPIRATION=86400000

MAIL_HOST=sandbox.smtp.mailtrap.io
MAIL_PORT=2525
MAIL_USERNAME=your_mailtrap_username
MAIL_PASSWORD=your_mailtrap_password
```

Relevant application properties already present in `application-dev.properties`:

- `JWT.SECRET.KEY=${JWT_SECRET_KEY}`
- `JWT.TIME.EXPIRATION=${JWT_TIME_EXPIRATION}`
- `stayhub.payment.bank-account=3001234567890`
- `stayhub.payment.deposit-percentage=20`
- `stayhub.payment.deadline-days=3`

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/estebangmz666/stayhub-v2.git
cd stayhub-v2
```

### 2. Create the database

```sql
CREATE DATABASE stayhub;
```

### 3. Configure environment variables

Create a `.env` file in the project root or provide the variables through your shell or IDE run configuration.

### 4. Run the application

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

On Unix-like systems:

```bash
./mvnw spring-boot:run
```

The API will be available at:

```text
http://localhost:8080
```

## Running Tests

Run the full test suite:

```powershell
.\mvnw.cmd test
```

Useful targeted runs in this repo:

```powershell
.\mvnw.cmd -q -Dtest=ReservationServiceTest test
.\mvnw.cmd -q -Dtest=ReviewServiceTest test
.\mvnw.cmd -q -DskipTests compile
```

## Current Limitations

- Payment handling is simulated and does not process real money.
- The frontend is maintained separately and is not part of this repository.
- No production-grade payment gateway is integrated.

## Contributing

1. Fork the repository.
2. Create a branch.

```bash
git checkout -b feature/your-feature-name
```

3. Commit your changes.

```bash
git commit -m "Add your feature"
```

4. Push the branch.

```bash
git push origin feature/your-feature-name
```

5. Open a Pull Request.

## License

```text
GNU GPL V3.0 License
```

## Authors

- Esteban Gomez Leon
- Juan Pablo Galeano Correa


- `https://github.com/estebangmz666/stayhub-v2`

---

Built with Java, Spring Boot, JWT security, scheduled jobs, and a layered backend architecture.
