# Email Validator — Email list cleaning web app

A Spring Boot web application to import, check and clean lists of email addresses. Users upload a CSV file, the application checks each address, removes duplicates, classifies the addresses and shows statistics. Valid addresses can then be exported.

> Built during my summer internship at ESPRIT (July – August 2025).

## Features

- **CSV import** (up to 10 MB): one address per line, header line detected automatically
- **Format validation** with a regular expression and a maximum length of 254 characters
- **Duplicate detection**, both inside the uploaded file and against addresses already stored
- **Classification** of each address as `valid`, `obsolete` or `invalid`, using simple domain rules
- **Import report**: total lines, valid, obsolete, invalid, duplicates and saved addresses
- **Email management**: create, read, update, delete and search
- **Statistics dashboard** and **export of valid addresses**
- **Authentication** with a login page and two roles: `USER` (import, manage, export) and `ADMIN` (can also delete)
- **API documentation** with Swagger UI

## Tech stack

| Layer | Technologies |
|---|---|
| Back end | Java 17, Spring Boot 3.2, Spring Web, Spring Data JPA, Bean Validation |
| Security | Spring Security: form login, BCrypt password hashing, method-level authorization (`@PreAuthorize`), one session per user |
| Database | H2 in-memory database |
| Front end | HTML, CSS, JavaScript (static pages served by Spring Boot) |
| Tools | Maven, Swagger / OpenAPI (springdoc), Postman, Logback |

## Architecture

```
email-validator/src/main/java/com/example/email_validator/
  controller/   REST endpoints (/api/emails, /api/auth) and page routing
  service/      business logic: validation, classification, CSV processing, statistics
  repository/   Spring Data JPA queries
  model/        Email entity (address, status, active flag, timestamps)
  config/       security and Swagger configuration
src/main/resources/
  static/       front-end pages (index.html, login.html)
  schema.sql, data.sql   database schema and sample data
```

## Main API endpoints

| Method | Endpoint | Role | Description |
|---|---|---|---|
| POST | `/api/emails/upload-csv` | USER | Import and process a CSV file |
| GET | `/api/emails` | USER | List all addresses |
| GET | `/api/emails/search?q=` | USER | Search addresses |
| GET | `/api/emails/stats` | USER | Statistics by status |
| GET | `/api/emails/export/valid` | USER | Export valid addresses |
| POST / PUT | `/api/emails`, `/api/emails/{id}` | USER | Create or update an address |
| DELETE | `/api/emails/{id}` | ADMIN | Delete an address |

The full documentation is available in Swagger UI once the application is running.

## Getting started

**Requirements:** Java 17. Maven is included through the Maven wrapper.

```bash
cd email-validator
./mvnw spring-boot:run
```

On Windows, use `mvnw.cmd spring-boot:run`.

Then open:
- the application: `http://localhost:8099`
- Swagger UI: `http://localhost:8099/swagger-ui.html`
- H2 console: `http://localhost:8099/h2-console`

Demo accounts for local development are defined in `config/SecurityConfig.java`.

## Limitations and possible improvements

- The `valid` / `obsolete` / `invalid` classification uses simple keyword rules on the domain name. A real check would verify the domain's MX records or use an email verification service.
- Users are stored in memory for development; a production version would store them in a database with proper account management.
- H2 is an in-memory database, so data is lost when the application stops; PostgreSQL or MySQL would be used in production.
- Unit and integration tests for the service layer would be the next step.

## Author

Ons Chehab — computer engineering student at ESPRIT (TWIN track)
[LinkedIn](https://www.linkedin.com/in/ons-chehab-90b298294)
