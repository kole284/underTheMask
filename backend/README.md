# Under The Mask Backend

Spring Boot backend for the Under The Mask multiplayer word-deduction game.

## Requirements

- Java 17+
- Maven 3.8+
- MySQL 8+

## Local MySQL Setup

Create a local database and user:

```sql
CREATE DATABASE under_the_mask CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'underthemask'@'localhost' IDENTIFIED BY 'underthemask';
GRANT ALL PRIVILEGES ON under_the_mask.* TO 'underthemask'@'localhost';
FLUSH PRIVILEGES;
```

Or run the included database/user setup script:

```bash
mysql -u root -p < database/create_database.sql
```

The default local connection is:

```text
jdbc:mysql://localhost:3306/under_the_mask?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
```

Override it with environment variables when needed:

```bash
export SPRING_DATASOURCE_URL='jdbc:mysql://localhost:3306/under_the_mask'
export SPRING_DATASOURCE_USERNAME='underthemask'
export SPRING_DATASOURCE_PASSWORD='underthemask'
```

## Run

```bash
mvn spring-boot:run
```

Flyway runs automatically on startup and creates the permanent word-content tables.

For manual schema provisioning outside the normal Flyway startup flow, see
`database/init_under_the_mask.sql`.

## Test

```bash
mvn test
```

## Multiplayer Lobby API

- `POST /api/lobbies` creates a waiting lobby and adds the host as the first player.
- `POST /api/lobbies/{code}/players` joins a waiting lobby.
- `GET /api/lobbies/{code}` returns safe public lobby state without reconnect tokens.
- `POST /api/lobbies/{code}/reconnect` reconnects a disconnected player using `Authorization: Bearer <reconnectToken>`.
- `DELETE /api/lobbies/{code}/players/me` leaves a waiting lobby using the bearer token.
- `PATCH /api/lobbies/{code}/settings` updates settings using the host bearer token.

STOMP clients connect to `/ws` and subscribe to `/topic/lobbies/{code}` for `LOBBY_UPDATED` events.

## Game API

All game endpoints require `Authorization: Bearer <reconnectToken>`.

- `POST /api/lobbies/{code}/game/start` starts a round (host only, minimum three players).
- `GET /api/lobbies/{code}/game` returns the authenticated player's private role and current public game state.
- `POST /api/lobbies/{code}/game/clues` submits the current player's clue.
- `POST /api/lobbies/{code}/game/votes` submits the player's selected suspects.
- `POST /api/lobbies/{code}/game/reset` returns a finished round to the waiting lobby (host only).

The existing `/topic/lobbies/{code}` subscription also publishes `GAME_UPDATED`. Its payload contains public state only; clients fetch private state through the authenticated game endpoint.
