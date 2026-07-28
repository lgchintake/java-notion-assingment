# Java Student Report Service

Standalone Spring Boot microservice for generating student PDF reports by consuming the existing Node.js backend API.

## Prerequisites

- PostgreSQL database is running.
- Node.js backend is running and connected to PostgreSQL.
- A valid logged-in session from the Node backend is available, because `/api/v1/students/:id` is protected by cookie auth and CSRF.

## Configuration

Defaults are defined in `src/main/resources/application.yaml`.

```bash
PORT=8081
NODE_API_BASE_URL=http://localhost:5007
UI_URL=http://localhost:5173
```

## Run

```bash
./gradlew bootRun
```

## Endpoint

```http
GET /api/v1/students/{id}/report
```

The service forwards the caller's `Cookie`, `X-CSRF-TOKEN`, and `Authorization` headers to the Node backend, fetches `/api/v1/students/{id}`, and returns a downloadable PDF.

Example:

```bash
curl --location 'http://localhost:8081/api/v1/students/2/report' \
--header 'Access-Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MSwicm9sZSI6ImFkbWluIiwicm9sZUlkIjoxLCJjc3JmX2htYWMiOiIwZTVjNjY0NDJmOTk3Mzc1NTY1ZDVkNTE2NjQ0NWI5MmZlMmE3YmZhZmQ0NTY4ZjEyOWVhNDYyMzFkZTVjMzc5IiwiaWF0IjoxNzg1MjM5MjExLCJleHAiOjE3ODUyNDAxMTF9.6AsqP_UgHie-HAv4rLlW9rEJh0ENcW69WfdhwYirt6M' \
--header 'Refresh-Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MSwicm9sZSI6ImFkbWluIiwicm9sZUlkIjoxLCJpYXQiOjE3ODUyMzkyMTEsImV4cCI6MTc4NTI2ODAxMX0.4Sm4fa4XB3FQ9aWI-Q7Qb-_VSkJLCHc6FGjpRMZ9lLQ' \
--header 'Csrf-Token: ecea7afc-b0f2-4b5b-90c1-944a9d4afcaf' \
--header 'Cookie: accessToken=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MSwicm9sZSI6ImFkbWluIiwicm9sZUlkIjoxLCJjc3JmX2htYWMiOiIwZTVjNjY0NDJmOTk3Mzc1NTY1ZDVkNTE2NjQ0NWI5MmZlMmE3YmZhZmQ0NTY4ZjEyOWVhNDYyMzFkZTVjMzc5IiwiaWF0IjoxNzg1MjM5MjExLCJleHAiOjE3ODUyNDAxMTF9.6AsqP_UgHie-HAv4rLlW9rEJh0ENcW69WfdhwYirt6M; csrfToken=ecea7afc-b0f2-4b5b-90c1-944a9d4afcaf; refreshToken=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MSwicm9sZSI6ImFkbWluIiwicm9sZUlkIjoxLCJpYXQiOjE3ODUyMzkyMTEsImV4cCI6MTc4NTI2ODAxMX0.4Sm4fa4XB3FQ9aWI-Q7Qb-_VSkJLCHc6FGjpRMZ9lLQ'
```
