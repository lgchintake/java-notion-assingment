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
postman request 'http://localhost:8081/api/v1/students/1/report' \
  --header 'Access-Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MSwicm9sZSI6ImFkbWluIiwicm9sZUlkIjoxLCJjc3JmX2htYWMiOiJlMGJhOGRiNzliOTdkOWEwMzRjMTE3ZTA4NDU4NTYwYzEzODdjZmFhODdmZTQ3OGNkOWVmZDdiMjI4ZDViZjIyIiwiaWF0IjoxNzg1MjM0NDE0LCJleHAiOjE3ODUyMzUzMTR9.wkVE7SxxHO4s_qlUO_Pjs_2cJZlHZ53lYsKhLZSp_MA' \
  --header 'Refresh-Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MSwicm9sZSI6ImFkbWluIiwicm9sZUlkIjoxLCJpYXQiOjE3ODUyMzQ0MTQsImV4cCI6MTc4NTI2MzIxNH0.8KRyTu4tadf5ELAUfqquUbUYN40ZJmhlAuXpg1A806M' \
  --header 'Csrf-Token: 46273af7-620f-4307-9b81-a0c1c6941ebf' \
  --header 'Cookie: accessToken=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MSwicm9sZSI6ImFkbWluIiwicm9sZUlkIjoxLCJjc3JmX2htYWMiOiJlMGJhOGRiNzliOTdkOWEwMzRjMTE3ZTA4NDU4NTYwYzEzODdjZmFhODdmZTQ3OGNkOWVmZDdiMjI4ZDViZjIyIiwiaWF0IjoxNzg1MjM0NDE0LCJleHAiOjE3ODUyMzUzMTR9.wkVE7SxxHO4s_qlUO_Pjs_2cJZlHZ53lYsKhLZSp_MA; csrfToken=46273af7-620f-4307-9b81-a0c1c6941ebf; refreshToken=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MSwicm9sZSI6ImFkbWluIiwicm9sZUlkIjoxLCJpYXQiOjE3ODUyMzQ0MTQsImV4cCI6MTc4NTI2MzIxNH0.8KRyTu4tadf5ELAUfqquUbUYN40ZJmhlAuXpg1A806M'
```
