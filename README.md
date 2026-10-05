# Hospital Management API

A Spring Boot REST API for managing hospital operations — doctors, nurses, patients, receptionists, and appointments.

## Tech Stack

- Java (Spring Boot 3.5.5)
- Maven
- H2 (in-memory database) / PostgreSQL support
- SpringDoc OpenAPI (Swagger UI)

## Project Structure

## Getting Started

### Prerequisites

- JDK 25 (the version set in `pom.xml` and used to verify the build)
- Maven (or use the included `mvnw` / `mvnw.cmd` wrapper — no local Maven install needed)

### Run locally

The app starts at `http://localhost:8080`.

### API Documentation

Once running, Swagger UI is available at `http://localhost:8080/swagger-ui.html`.

### Authentication

Every endpoint except `/`, `/error`, `/actuator/health`, `POST
/api/auth/login`, and the Swagger UI routes requires a JWT bearer token
(Issue 21). There are five roles -- ADMIN, DOCTOR, NURSE, RECEPTIONIST,
PATIENT -- and what each one can reach is documented in `SecurityConfig`.

Accounts are real `User` entities (see `UserRepository`/`UserService`),
not a hard-coded list -- but something has to be able to create the
first one, so the app seeds exactly one bootstrap ADMIN account at
startup if it doesn't already exist, using credentials read from
`application.properties`:

| Property              | Environment variable | Local default |
|-----------------------|-----------------------|----------------|
| `app.admin.username`  | `APP_ADMIN_USERNAME`  | `admin`        |
| `app.admin.password`  | `APP_ADMIN_PASSWORD`  | `admin123`     |

**The defaults above are for local development only.** Override both
environment variables before deploying anywhere real -- otherwise the
app starts with a well-known admin password.

#### Logging in

```
POST /api/auth/login
Content-Type: application/json

{ "username": "admin", "password": "admin123" }
```

returns a signed JWT in the response body. Send it on every subsequent
request as:

```
Authorization: Bearer <token>
```

Tokens are signed with the secret in `app.jwt.secret`
(`APP_JWT_SECRET` environment variable) and expire after
`app.jwt.expiration-ms` milliseconds (1 hour by default). **The shipped
`app.jwt.secret` default is also for local development only** -- override
it with `APP_JWT_SECRET` before deploying anywhere real.

#### Creating further accounts

Once logged in as an ADMIN, create additional accounts for other
people/roles via:

```
POST /api/users
Authorization: Bearer <admin's token>
Content-Type: application/json

{ "username": "dr.kim", "password": "a-strong-password", "role": "DOCTOR" }
```

`POST`/`GET /api/users/**` are restricted to ADMIN, same as every other
admin-only route in this API. Passwords are always BCrypt-hashed before
being stored; the API never accepts or returns a plaintext or hashed
password outside of login/account creation.

### Notifications (email / SMS)

Appointment reminders (`POST /api/appointments/reminders`, Issue 16) go
out through a `Notifier` per channel. By default both channels use a
**Console** stand-in that only logs what it would have sent -- nothing
actually leaves the app unless you opt in to a real provider:

| Property                        | Values                | Default   |
|----------------------------------|------------------------|-----------|
| `app.notifications.email.provider` | `console`, `smtp`   | `console` |
| `app.notifications.sms.provider`   | `console`, `twilio` | `console` |

Each channel is wired independently, so you can turn on real email
while leaving SMS on the console stand-in (or vice versa).

A reminder is only sent to a patient who has the matching contact info
on file (`email`/`phone` on `Patient`, settable via `POST /api/patients`).
If it's missing or blank, the real notifier logs a warning for that one
patient and moves on -- it doesn't fail the rest of the batch.

#### Email via SMTP

Set `app.notifications.email.provider=smtp` and fill in the standard
Spring Boot mail properties (works with Gmail SMTP, SendGrid, AWS SES,
or any SMTP relay):

| Property                      | Environment variable       |
|--------------------------------|------------------------------|
| `spring.mail.host`             | `SPRING_MAIL_HOST`           |
| `spring.mail.port`             | `SPRING_MAIL_PORT`           |
| `spring.mail.username`         | `SPRING_MAIL_USERNAME`       |
| `spring.mail.password`         | `SPRING_MAIL_PASSWORD`       |
| `app.notifications.email.from` | `APP_NOTIFICATIONS_EMAIL_FROM` |

#### SMS via Twilio

Set `app.notifications.sms.provider=twilio` and supply your Twilio
account details:

| Property                           | Environment variable                  |
|--------------------------------------|------------------------------------------|
| `app.notifications.sms.account-sid`  | `APP_NOTIFICATIONS_SMS_ACCOUNT_SID`     |
| `app.notifications.sms.auth-token`   | `APP_NOTIFICATIONS_SMS_AUTH_TOKEN`      |
| `app.notifications.sms.from-number`  | `APP_NOTIFICATIONS_SMS_FROM_NUMBER`     |

**None of the above ships with real credentials.** Leave both
providers on `console` until you've filled these in -- `smtp`/`twilio`
with blank credentials will fail to start (the SMTP/Twilio client beans
require them).

## Roadmap

- Wire up REST endpoints for doctors, nurses, patients, and receptionists
- Add appointment conflict detection to prevent double-booking
- Move from in-memory storage to JPA + H2/PostgreSQL persistence

## License

MIT — see [LICENSE](LICENSE).

## Author

Mohamad Eldirany — [@mhmddirany](https://github.com/mhmddirany)
