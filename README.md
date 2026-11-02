# Notification System

A Spring Boot notification intake service. It accepts notification requests, stores them with idempotency protection, and routes each requested channel to its configured provider implementations.

## Technology

- Java 25 and Spring Boot
- MySQL with Flyway migrations and Spring Data JPA
- OpenAPI-generated request/response DTOs
- Docker and Docker Compose

## Architecture

```text
NotificationService
  -> ChannelDispatcher
      -> OtpChannelSender  -> OtpProviderDispatcher  -> WhatsApp / Twilio / AWS SNS
      -> EmailChannelSender -> EmailProviderDispatcher -> SMTP / SendGrid
      -> PushChannelSender -> PushProviderDispatcher -> Firebase / OneSignal
```

Channel routing lives in `src/main/java/com/example/notificationsystem/notification/channel`.
Each channel owns its provider enum, dispatcher, provider contract, and concrete provider classes.

## Run with Docker Compose

The Docker image packages the JAR already present in `build/libs`, so build the application before rebuilding the image.

```bash
./gradlew bootJar
docker compose up --build -d
```

The service is available at `http://localhost:8080`. MySQL is exposed on port `3306`.

Stop the stack:

```bash
docker compose down
```

To also remove the local MySQL data volume:

```bash
docker compose down -v
```

## Run locally

Start MySQL through Compose, then run the application from Gradle:

```bash
docker compose up -d mysql
./gradlew bootRun
```

The default local database configuration is in `src/main/resources/application.properties`. Override `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, and `DB_PASSWORD` as needed.

## API

### Create a notification

`POST /api/v1/notifications`

The request requires a unique `x-header-idempotency-key`. Repeating a request with the same key and payload returns the original accepted notification; reusing it with a different payload returns `409 Conflict`.

```bash
curl --request POST http://localhost:8080/api/v1/notifications \
  --header 'Content-Type: application/json' \
  --header 'x-header-idempotency-key: login-otp-001' \
  --data '{
    "templateId": "LOGIN_OTP",
    "recipient": {
      "customerId": "C123",
      "phoneNumber": "+919876543210",
      "emailAddress": "abc@example.com"
    },
    "delivery": {
      "channels": ["SMS", "EMAIL", "PUSH"],
      "strategy": "PARALLEL"
    },
    "variables": {
      "otp": "483921"
    },
    "locale": "en-IN",
    "correlationId": "login-flow-001"
  }'
```

The API channel values are mapped internally as follows:

| API channel | Internal channel |
| --- | --- |
| `SMS` | `OTP` |
| `EMAIL` | `EMAIL` |
| `PUSH` | `PUSH_NOTIFICATION` |

### Get notification status

`GET /api/v1/notifications/{notificationId}`

## Logs

Follow application logs from the running container:

```bash
docker compose logs -f app
```

Provider dispatches are logged in a structured form, for example:

```text
Dispatching notification [channel=OTP, provider=TWILIO]
Notification dispatch completed [channel=OTP]
```

## OpenAPI contract

The API contract is in `src/main/resources/openapi/api-contract.yaml`. DTOs are generated during the Gradle build under `build/generated/openapi`.

## Build and test

```bash
./gradlew compileJava
./gradlew test
```
