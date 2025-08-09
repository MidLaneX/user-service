# User Service

A microservice for managing user profiles and data within the project management system. This service handles user creation through Kafka events and provides profile management capabilities.

## System Architecture

The User Service follows a microservices architecture pattern and integrates with other services through:
- **Event-driven communication** via Apache Kafka
- **REST API** for direct service interactions
- **PostgreSQL database** for data persistence

## Data Model

### User Entity
```
- id: Long (Primary Key)
- authServiceUserId: Long (Auth Service Reference)
- email: String (Unique)
- firstName: String (Optional)
- lastName: String (Optional)
- teamIds: List<String>
- managedTeamId: String
- status: UserStatus (ACTIVE, INACTIVE, PENDING, SUSPENDED)
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

## Kafka Integration

### Consumer Configuration
- **Topic**: `user.added`
- **Consumer Group**: `user-service-group`
- **Event Type**: `USER_CREATED`

### Event Structure
```json
{
  "user_id": 1,
  "email": "user@example.com",
  "event_type": "USER_CREATED"
}
```

### Event Processing Flow
1. Auth service publishes user creation event to `user.added` topic
2. User service consumes the event and creates a minimal user record with `PENDING` status
3. User completes profile via REST API, status changes to `ACTIVE`
4. Duplicate prevention: checks both email and authServiceUserId

## API Endpoints

### Base URL: `/api/v1/users`

### User Management
| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| `GET` | `/` | Get all users | `List<UserResponse>` |
| `GET` | `/{id}` | Get user by ID | `UserResponse` |
| `POST` | `/` | Create user manually | `UserResponse` |
| `PUT` | `/{id}` | Update user by ID | `UserResponse` |
| `DELETE` | `/{id}` | Delete user | `204 No Content` |

### Profile Management
| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| `GET` | `/email/{email}` | Get user by email | `UserResponse` |
| `GET` | `/auth-service-id/{authServiceUserId}` | Get user by auth service ID | `UserResponse` |
| `PUT` | `/profile/{email}` | Complete/update user profile | `UserResponse` |
| `GET` | `/profile-complete/{email}` | Check if profile is complete | `Boolean` |

## Request/Response DTOs

### UserResponse
```json
{
  "id": 1,
  "authServiceUserId": 123,
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "status": "ACTIVE",
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00"
}
```

### UpdateUserProfileRequest
```json
{
  "firstName": "John",
  "lastName": "Doe"
}
```

### CreateUserRequest
```json
{
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe"
}
```

## User Lifecycle

### 1. Registration Flow (Kafka-driven)
```
Auth Service → Kafka Event → User Service
```
- User registers in auth service
- Auth service publishes `USER_CREATED` event
- User service creates user with `PENDING` status
- firstName and lastName are null initially

### 2. Profile Completion
```
Frontend → REST API → User Service
```
- User completes profile via `PUT /profile/{email}`
- Status changes from `PENDING` to `ACTIVE`
- firstName and lastName are populated

### 3. Profile Updates
- Users can update their profile information
- Status remains `ACTIVE` after completion

## Error Handling

### Common HTTP Status Codes
- `200 OK` - Successful operation
- `201 Created` - User created successfully
- `204 No Content` - User deleted successfully
- `400 Bad Request` - Invalid request data
- `404 Not Found` - User not found
- `409 Conflict` - Email already exists

### Kafka Event Handling
- Duplicate events are ignored (idempotent processing)
- Invalid event types are logged and skipped
- Failed processing triggers retry mechanism

## Database Schema

### users table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    auth_service_user_id BIGINT UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    managed_team_id VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### user_team_ids table
```sql
CREATE TABLE user_team_ids (
    user_id BIGINT REFERENCES users(id),
    team_id VARCHAR(255)
);
```

## Technology Stack

- **Framework**: Spring Boot 3.5.3
- **Language**: Java 21
- **Database**: PostgreSQL
- **Message Broker**: Apache Kafka
- **Build Tool**: Maven
- **Containerization**: Docker

## Service Dependencies

### Required Services
- **Auth Service** - Publishes user creation events
- **PostgreSQL Database** - Data persistence
- **Apache Kafka** - Event streaming

### Environment Variables
- `DB_URL` - PostgreSQL connection URL
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password
- `KAFKA_BOOTSTRAP_SERVERS` - Kafka broker addresses
- `SERVER_PORT` - Service port (default: 8082)

## Health Monitoring

- **Health Check Endpoint**: `/actuator/health`
- **Metrics**: Available via `/actuator/metrics`
- **Docker Health Check**: Configured with 30s intervals

## Security Considerations

- Non-root user execution in Docker container
- Input validation on all endpoints
- Email format validation
- Duplicate prevention mechanisms
- Transactional data integrity
