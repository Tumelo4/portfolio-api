# Portfolio API

A simplified, single-module Maven Spring Boot API for Tumelo Mosomane's
developer portfolio.

This project replaces the previous Gradle modules:

```text
Domain
Repository
Service
Web
```

with one deployable application:

```text
src/main/java/com/tumelo/portfolio/
├── config/
├── controller/
├── dto/
├── exception/
├── model/
├── repository/
├── security/
└── service/
```

## Features

- MongoDB-backed portfolio profile
- MongoDB GridFS storage for images and résumé files
- contact-form email delivery
- request validation
- environment-only credentials
- protected administrative endpoints
- restricted configurable CORS
- structured API errors
- Spring Boot Actuator health checks
- Maven CI
- Docker and Render deployment configuration
- compatibility routes for the existing Next.js frontend

## Requirements

- Java 17 or newer
- Maven 3.6.3 or newer
- MongoDB or MongoDB Atlas
- SMTP credentials for contact-form delivery

## Security notice

The original repository contained database and SMTP credentials in source
control. Do not copy or reuse those values.

Before deploying this version:

1. rotate the MongoDB password;
2. revoke and replace the SMTP app password;
3. remove the old secrets from Git history;
4. inspect MongoDB and SMTP access logs;
5. configure only the replacement values through environment variables.

## Local configuration

Copy the example configuration:

```bash
cp .env.example .env
```

Load it into your shell or IDE. Spring Boot does not automatically load a
`.env` file.

Minimum variables:

```text
SPRING_DATA_MONGODB_URI
SPRING_MAIL_USERNAME
SPRING_MAIL_PASSWORD
APP_MAIL_FROM
APP_MAIL_TO
APP_ADMIN_API_KEY
CORS_ALLOWED_ORIGINS
```

Local MongoDB defaults to:

```text
mongodb://localhost:27017/portfolio
```

## Build

```bash
mvn clean verify
```

The executable JAR is created at:

```text
target/portfolio-api.jar
```

## Run

```bash
mvn spring-boot:run
```

or:

```bash
java -jar target/portfolio-api.jar
```

## Docker

Build:

```bash
docker build -t portfolio-api .
```

Run:

```bash
docker run --rm -p 8080:8080 \
  -e PORT=8080 \
  -e SPRING_DATA_MONGODB_URI='mongodb://host.docker.internal:27017/portfolio' \
  -e APP_ADMIN_API_KEY='replace-me' \
  -e CORS_ALLOWED_ORIGINS='http://localhost:3000' \
  portfolio-api
```

## API

Base path:

```text
/api/portfolio
```

### Public endpoints

| Method | Path | Purpose |
|---|---|---|
| GET | `/user/details` | Retrieve portfolio information |
| POST | `/user/submit` | Submit a contact message |
| GET | `/retrieve/{fileId}` | Legacy Base64 file response |
| GET | `/files/{fileId}` | Preferred binary file response |

### Administrative endpoints

Administrative requests require:

```http
X-Admin-Key: <APP_ADMIN_API_KEY>
```

| Method | Path | Purpose |
|---|---|---|
| PUT | `/admin/portfolio` | Create or update portfolio information |
| POST | `/admin/files` | Upload a PDF, PNG or JPEG |
| POST | `/user/save` | Legacy portfolio-save route |
| POST | `/user/bsonfile` | Legacy upload route |

Admin endpoints return `503 Service Unavailable` when `APP_ADMIN_API_KEY` is
not configured.

## Example portfolio update

```bash
curl -X PUT http://localhost:8080/api/portfolio/admin/portfolio \
  -H 'Content-Type: application/json' \
  -H 'X-Admin-Key: replace-me' \
  -d '{
    "name": "Tumelo",
    "surname": "Mosomane",
    "occupation": "Software Engineer",
    "linkedinLink": "https://linkedin.com/in/example",
    "githubLink": "https://github.com/Tumelo4",
    "instagramLink": "",
    "pdfResumeId": "replace-after-upload",
    "imageId": "replace-after-upload",
    "home_description": "Software engineer focused on practical systems.",
    "about_description": "Longer professional description.",
    "skills": [
      {
        "name": "Java",
        "imageId": "replace-after-upload"
      }
    ],
    "projects": [
      {
        "title": "Portfolio",
        "description": "Personal developer portfolio.",
        "imageId": "replace-after-upload",
        "link": "https://example.com"
      }
    ]
  }'
```

## Example upload

```bash
curl -X POST http://localhost:8080/api/portfolio/admin/files \
  -H 'X-Admin-Key: replace-me' \
  -F 'file=@resume.pdf'
```

## Health check

```text
GET /actuator/health
```

Render should use:

```text
/actuator/health
```

as its health-check path.

## Render deployment

The repository contains both a `Dockerfile` and `render.yaml`.

Using the dashboard:

1. Create a new **Web Service**.
2. Connect the GitHub repository.
3. Select **Docker** as the runtime.
4. Keep the Dockerfile path as `./Dockerfile`.
5. Set the health-check path to `/actuator/health`.
6. add the environment variables from `.env.example`;
7. deploy.

The application binds to `0.0.0.0` and uses Render's `PORT` environment
variable.

## Existing frontend compatibility

The current frontend can continue using:

```text
GET  /api/portfolio/user/details
POST /api/portfolio/user/submit
GET  /api/portfolio/retrieve/{fileId}
```

For new frontend work, prefer `/files/{fileId}` because it returns normal
binary content instead of Base64 text.

## Recommended frontend environment variable

```text
NEXT_PUBLIC_PORTFOLIO_API_URL=https://your-service.onrender.com/api/portfolio
```

## Test coverage

Run:

```bash
mvn verify
```

The JaCoCo report is generated at:

```text
target/site/jacoco/index.html
```
