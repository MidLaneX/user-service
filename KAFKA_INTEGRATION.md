# Kafka Integration for Team Member Events

## Overview
This implementation adds Kafka event publishing when users are added to teams in the user service. The system publishes events with user details and a default ADMIN role to enable other services to react to team membership changes.

## Event Structure
```json
{
  "userId": 123,
  "organizationId": 456,
  "teamId": 789,
  "role": "ADMIN",
  "timestamp": "2024-01-01T12:00:00",
  "eventType": "TEAM_MEMBER_ADDED",
  "teamName": "Development Team",
  "organizationName": "Tech Corp",
  "userEmail": "user@example.com",
  "userName": "John Doe"
}
```

## Kafka Configuration
- **Topic**: `team-member-added`
- **Bootstrap Servers**: `localhost:9092` (configurable via `KAFKA_BOOTSTRAP_SERVERS`)
- **Key Strategy**: `team-{teamId}` for better partitioning
- **Serialization**: JSON serialization for event payloads

## Producer Configuration Features
- **Reliability**: `acks=all`, retries=3, idempotence enabled
- **Ordering**: max.in.flight.requests.per.connection=1
- **Performance**: Optimized batch size and linger settings

## Error Handling
- Events are published asynchronously after successful database transaction
- Failed event publishing does not rollback the team member addition
- Comprehensive logging for monitoring and debugging

## Environment Variables
```properties
KAFKA_BOOTSTRAP_SERVERS=localhost:9092  # Kafka broker addresses
```

## Dependencies Added
- `spring-kafka` - Spring Boot Kafka integration

## Usage
When a user is added to a team via `TeamService.addMember()`, an event is automatically published to the `team-member-added` topic with all relevant details and the default ADMIN role.
