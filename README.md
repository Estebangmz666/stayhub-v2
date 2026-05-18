<h1 align="center">StayHub V2</h1>

<p align="center">
  REST backend for accommodation discovery, seasonal pricing, bookings, reviews, notifications, and host operations.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange" alt="Java 21">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0.4-6DB33F" alt="Spring Boot 4.0.4">
  <img src="https://img.shields.io/badge/PostgreSQL-Database-336791" alt="PostgreSQL">
  <img src="https://img.shields.io/badge/OpenAPI-springdoc%203.0.2-blue" alt="OpenAPI">
  <img src="https://img.shields.io/badge/MapStruct-1.6.3-yellow" alt="MapStruct">
  <img src="https://img.shields.io/badge/JWT-jjwt%200.13.0-red" alt="JWT">
  <img src="https://img.shields.io/badge/Render-Basic%20CI%2FCD-46E3B7" alt="Render deployment">
  <img src="https://img.shields.io/badge/License-GPLv3-yellow" alt="GPLv3">
</p>

## Overview

StayHub V2 is a production-oriented Spring Boot backend for a full-accommodation booking platform. It covers authentication, user account management, accommodation management, seasonal rental packages, reservation pricing and booking flows, payment reminders, host analytics, guest reviews, and transactional emails.

The application follows a layered `controller -> service -> repository` architecture, uses stateless JWT authentication, documents its HTTP contract in a static OpenAPI file, and includes background schedulers for reservation lifecycle tasks.

## Current Status

The repository is actively evolving and the current implementation already includes:

- Authenticated user account lifecycle with signup, login, password recovery, password change, profile retrieval, profile update, and account soft delete.
- Accommodation management for hosts, including creation, public search by city, retrieval by id, unavailable-date inspection, and soft delete guarded by reservation rules.
- Seasonal rental packages per accommodation, used to override nightly pricing for specific date ranges.
- Reservation quote generation without persistence, plus full reservation creation and post-booking management.
- Guest payment reminder notifications and automated reservation lifecycle tasks.
- Guest reviews for completed stays and one-time host responses.
- Host-facing accommodation metrics by stay period.
- Docker-based deployment packaging used by the current Render deployment flow.

## Live Entry Point

The frontend is maintained in a separate repository, but it acts as the main application entry point for this backend:

- Host dashboard: [https://stay-hub-xi.vercel.app/login](https://stay-hub-xi.vercel.app/login)

This repository focuses on the backend API, scheduled jobs, business rules, and email workflows that power that experience.

## Main Features

### Authentication and users

- User signup with role assignment.
- JWT login.
- Forgot-password and reset-password flow.
- Authenticated password change.
- Authenticated profile retrieval with `GET /api/v2/users/me`.
- Authenticated profile update with `PATCH /api/v2/users/me`.
- Account soft delete with `DELETE /api/v2/users/me`.

### Accommodations

- Host-only accommodation creation.
- Paginated city search for available accommodations.
- Accommodation retrieval by id.
- Unavailable date range listing for booking UX.
- Soft delete validation that blocks deactivation when future active reservations exist.

### Rental packages and pricing

- Seasonal pricing packages per accommodation.
- Package CRUD for hosts under the accommodation scope.
- Nightly pricing override using `pricePerNight` for dates covered by a package.
- Shared pricing engine used by both quote generation and reservation creation.

### Reservations

- Reservation quote generation with per-night pricing breakdown.
- Full-accommodation reservation creation.
- Reservation retrieval for the guest owner or host owner.
- Paginated reservation listing for the authenticated user.
- Reservation date update under business rules.
- Reservation cancellation under business rules.
- Deposit payment confirmation by the authenticated guest.

### Reviews and notifications

- Guest reviews only for completed reservations.
- One review per reservation.
- One host response per review.
- Pending payment notifications for guests.
- Transactional emails for booking, cancellation, payment reminders, and completed stays.

### Host operations

- Host performance metrics by accommodation and stay period.

## Business Rules

- Reservations are always for the whole accommodation. There is no room-level model.
- New reservations must be created at least 72 hours before check-in.
- If a reservation date change moves check-in within the next 72 hours, the deposit must already be paid.
- Active reservations can only be cancelled up to 48 hours before check-in.
- Only the authenticated guest owner can update, cancel, or mark a reservation deposit as paid.
- Reviews can only be created for `COMPLETED` reservations.
- Only one review is allowed per reservation.
- Only the accommodation host can answer a review, and only once.
- An accommodation cannot be soft-deleted if it has future active reservations.
- User self-deletion currently performs a soft delete, not irreversible physical removal.

## API Surface

### Users

- `POST /api/v2/users/auth/signup`
- `POST /api/v2/users/auth/login`
- `POST /api/v2/users/auth/forgot-password`
- `POST /api/v2/users/auth/reset-password`
- `PUT /api/v2/users/auth/change-password`
- `GET /api/v2/users/me`
- `PATCH /api/v2/users/me`
- `DELETE /api/v2/users/me`

### Accommodations

- `POST /api/v2/accommodations`
- `GET /api/v2/accommodations?city=&page=&size=`
- `GET /api/v2/accommodations/{id}`
- `GET /api/v2/accommodations/{id}/unavailable-dates?page=&size=`
- `DELETE /api/v2/accommodations/{id}`

### Rental packages

- `POST /api/v2/accommodations/{accommodationId}/packages`
- `GET /api/v2/accommodations/{accommodationId}/packages`
- `PUT /api/v2/accommodations/{accommodationId}/packages/{packageId}`
- `DELETE /api/v2/accommodations/{accommodationId}/packages/{packageId}`

### Bookings

- `POST /api/v2/bookings/book`
- `POST /api/v2/bookings/quote`
- `GET /api/v2/bookings/{reservationId}`
- `GET /api/v2/bookings/my-reservations?page=&scope=`
- `PUT /api/v2/bookings/{reservationId}`
- `PATCH /api/v2/bookings/{reservationId}/cancel`
- `PATCH /api/v2/bookings/{reservationId}/deposit-paid`

### Reviews

- `POST /api/v2/accommodations/{accommodationId}/reviews`
- `GET /api/v2/accommodations/{accommodationId}/reviews`
- `POST /api/v2/reviews/{reviewId}/response`

### Host metrics

- `GET /api/v2/hosts/me/accommodations/metrics?startDate=&endDate=&page=&size=`

### Notifications

- `GET /api/v2/notifications/payment-pending`

## Tech Stack

- Java 21
- Spring Boot 4.0.4
- Spring Security
- Spring Data JPA
- Spring Validation
- PostgreSQL
- MapStruct 1.6.3
- Lombok
- JWT with `jjwt 0.13.0`
- springdoc OpenAPI Starter WebMVC UI 3.0.2
- Thymeleaf for HTML email templates
- Spring Mail
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
|   |       |-- application-dev.properties
|   |       `-- application-prod.properties
|   `-- test/
|       `-- java/edu/uniquindio/stayhub_v2/
|-- .mvn/
|-- Dockerfile
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
- Services enforce business rules and authorization-sensitive flows.
- Repositories encapsulate persistence and custom queries.
- DTOs define the public request and response contracts.
- Mappers convert entities into transport-friendly payloads.
- Events and listeners decouple reservation side effects.
- Schedulers execute recurring lifecycle operations.

## OpenAPI Documentation

The repository keeps a static contract file that must remain aligned with the code:

```text
src/main/resources/static/openapi.yaml
```

When the application is running in a profile that exposes SpringDoc, the following routes are useful:

```text
http://localhost:8080/v3/api-docs
http://localhost:8080/swagger-ui/index.html
```

Important note:

- The repository currently defaults to the `prod` profile.
- In `prod`, SpringDoc is disabled in `application-prod.properties`.
- For local documentation work, you may prefer running with the `dev` profile locally, but that profile is not part of the remote production deployment flow.

## Environment and Profiles

Current profile behavior:

- `application.properties` sets `spring.profiles.active=prod`.
- The remote deployment should be understood as `prod`-oriented.
- `application-dev.properties` exists for local development convenience, but it is not the profile currently used by the remote repository flow.

Core environment variables expected by the application:

```env
DB_URL=jdbc:postgresql://localhost:5432/stayhub
DB_USERNAME=your_database_username
DB_PASSWORD=your_database_password

JWT_SECRET_KEY=your_jwt_secret_key
JWT_TIME_EXPIRATION=86400000

MAIL_HOST=sandbox.smtp.mailtrap.io
MAIL_PORT=2525
MAIL_USERNAME=your_mail_username
MAIL_PASSWORD=your_mail_password

FRONTEND_URL=https://stay-hub-xi.vercel.app
PAYMENT_BANK_ACCOUNT=3001234567890
PORT=8080
```

Relevant payment settings:

- `stayhub.payment.deposit-percentage=20`
- `stayhub.payment.deadline-days=3`

## Local Run

### 1. Clone the repository

```bash
git clone https://github.com/Estebangmz666/stayhub-v2.git
cd stayhub-v2
```

### 2. Create the database

```sql
CREATE DATABASE stayhub;
```

### 3. Provide environment variables

Create a `.env` file in the project root or configure the variables from your IDE or shell.

### 4. Run the application

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

On Unix-like systems:

```bash
./mvnw spring-boot:run
```

By default, the app starts on:

```text
http://localhost:8080
```

If you want to force a local development profile explicitly, use your IDE run configuration or override Spring profile/environment settings before startup.

## Docker and Deployment

This repository includes a multi-stage `Dockerfile` that:

- Builds the Spring Boot application with Maven and Java 21.
- Packages the generated JAR into a lightweight JRE image.
- Reads the HTTP port from `PORT`, defaulting to `8080`.

Current deployment context:

- The project uses a very basic CI/CD flow with Render.
- A commit pushed to the `dev` branch triggers a deploy event in Render.
- The `Dockerfile` is part of that deployment path and is important to keep aligned with the backend runtime requirements.

## Scheduled Jobs and Emails

Current email templates under `src/main/resources/templates`:

- `reservation-confirmation.html`
- `host-reservation-notification.html`
- `host-reservation-cancellation.html`
- `payment-reminder.html`
- `reservation-completed-guest.html`
- `reservation-completed-host.html`

Current scheduler responsibilities include:

- Sending payment reminder emails.
- Transitioning reservation lifecycle states.
- Handling automatic reservation cancellation flows when applicable.

## Testing

Run the full test suite:

```powershell
.\mvnw.cmd test
```

Useful targeted runs in this repository:

```powershell
.\mvnw.cmd -q -Dtest=ReservationServiceTest test
.\mvnw.cmd -q -Dtest=ReviewServiceTest test
.\mvnw.cmd -q -Dtest=HostMetricsServiceTest test
.\mvnw.cmd -q -Dtest=QuotingServiceTest test
.\mvnw.cmd -q -DskipTests compile
```

## Known Limitations and Planned Work

- Payment handling is simulated and does not integrate a real payment gateway.
- The frontend lives outside this repository.
- User self-deletion is currently implemented as soft delete.
- A future improvement is planned to anonymize private user data after a configurable retention period using a scheduled cron-based job.

## License

`GNU GPL V3.0 License`

## Authors

- Esteban Gomez Leon
- Juan Pablo Galeano
- Daniel Garcia
- Walter Granada
- Valentina Gonzalez

---

<p align="center"> Made with <3 by Stayhub Dev Team </p>
