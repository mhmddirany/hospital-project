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

- JDK matching the version set in `pom.xml`
- Maven (or use the included `mvnw` / `mvnw.cmd` wrapper — no local Maven install needed)

### Run locally

The app starts at `http://localhost:8080`.

### API Documentation

Once running, Swagger UI is available at `http://localhost:8080/swagger-ui.html`.

## Roadmap

- Wire up REST endpoints for doctors, nurses, patients, and receptionists
- Add appointment conflict detection to prevent double-booking
- Move from in-memory storage to JPA + H2/PostgreSQL persistence

## License

MIT — see [LICENSE](LICENSE).

## Author

Mohamad Eldirany — [@mhmddirany](https://github.com/mhmddirany)
