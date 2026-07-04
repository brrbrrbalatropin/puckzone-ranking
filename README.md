# puckzone-ranking

Ranking and leaderboards microservice for **PuckZone**, a real-time multiplayer air hockey
web platform for Colombian university students. This is one of six independent microservices;
it keeps track of every player's ELO rating and turns individual results into two leaderboards:
a global one per player and a collaborative one per university.

> PuckZone is an individual project for the Software Architectures course (ARSW) at
> Escuela Colombiana de Ingeniería Julio Garavito, term 2026-i.

## What this service does

- **Receives match results** from the game service when a match ends (matches are always
  played to 7 goals, so there are no draws).
- **Updates ELO ratings** for the winner and the loser using the standard ELO formula.
- **Serves the global leaderboard**: top players ordered by ELO.
- **Serves the university leaderboard**: each university's score is the sum of the ELO of
  all its registered students, so every match a student plays moves their university too.
- **Answers per-player queries**: current ELO, global position, wins and losses.

### Where it sits in the architecture

```
puckzone-game ──HTTP POST /api/ranking/match──▶ puckzone-ranking ◀──GET──── puckzone-gateway
                    (when a match ends)              │                      (frontend queries)
                                                PostgreSQL
```

All six services: `puckzone-auth` (8081) · `puckzone-matchmaking` (8082) · `puckzone-game`
(8083) · **`puckzone-ranking` (8084)** · `puckzone-gateway` (8080) · `puckzone-frontend`.

## How the rating works

**ELO system** (standard formula, K-factor 30, everyone starts at 1200):

```
expected = 1 / (1 + 10^((loserElo - winnerElo) / 400))
delta    = round(30 × (1 - expected))     // at least 1

winner: +delta      loser: -delta (never below 0)
```

In practice: two equal players trade ±15; beating a much stronger opponent is worth close
to +30, while losing to a much stronger opponent costs almost nothing. Players are created
automatically with 1200 ELO the first time they appear in a reported match.

**University ranking** is never stored — it is computed on demand as
`GROUP BY university, SUM(elo)` over the players table, so it can never drift out of sync
with the individual ratings. A university's name is the e-mail segment right before
`.edu.co` (e.g. `correo.unal.edu.co` → `unal`).

Both the initial ELO and the K-factor are configurable in `application.yaml` under
`puckzone.ranking.*`.

## API

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/ranking/match` | Report a finished match (called by the game service) |
| `GET` | `/api/ranking/global` | Global leaderboard, top 50 by default (`?limit=N` to change) |
| `GET` | `/api/ranking/university` | Collaborative leaderboard by university |
| `GET` | `/api/ranking/player/{id}` | One player's ELO, global position and win/loss record |
| `GET` | `/actuator/health` | Liveness/readiness probe |

### `POST /api/ranking/match` — request body

```json
{
  "winnerId":         "uuid of the winner",
  "loserId":          "uuid of the loser",
  "winnerUsername":   "optional — stored/refreshed if present",
  "loserUsername":    "optional — stored/refreshed if present",
  "winnerUniversity": "eci",
  "loserUniversity":  "unal",
  "winnerScore":      7,
  "loserScore":       4,
  "gameDuration":     183
}
```

Validation: `winnerScore` must be exactly 7, `loserScore` between 0 and 6, winner and loser
must be different players. Responds with the applied delta and both new ratings:

```json
{
  "winnerId": "…", "winnerNewElo": 1215,
  "loserId":  "…", "loserNewElo":  1185,
  "eloDelta": 15
}
```

Errors follow RFC 7807 (`ProblemDetail`): `400` for invalid payloads, `404` for unknown
players on the query endpoints.

## Tech stack

- Java 21 · Spring Boot 4.1 (Web MVC, Data JPA, Validation, Actuator)
- PostgreSQL 17 (schema managed by Hibernate)
- Maven · Docker multi-stage build
- Deployment target: Azure Container Apps

### Project layout (package by feature)

```
com.puckzone.ranking
├── config/       RankingProperties (initial ELO, K-factor) + global error handling
├── player/       Player entity, repository, service, global/individual endpoints
├── university/   University leaderboard (computed projection, no table of its own)
└── match/        Match result intake, EloCalculator, transactional processing
```

## Running it locally

Requires Docker. The bundled Postgres is exposed on host port **5434** (5432 is commonly
taken by a local PostgreSQL install; 5433 is used by puckzone-auth).

**Option A — database in Docker, app from your IDE / Maven** (day-to-day development):

```bash
docker compose up -d ranking-db
./mvnw spring-boot:run        # needs JDK 21
```

**Option B — everything in Docker:**

```bash
docker compose up --build
```

Then try it:

```bash
curl http://localhost:8084/actuator/health
curl http://localhost:8084/api/ranking/global
```

Database connection can be overridden with the `POSTGRES_HOST`, `POSTGRES_PORT`,
`POSTGRES_DB`, `POSTGRES_USER` and `POSTGRES_PASSWORD` environment variables
(see `src/main/resources/application.yaml`). Local tweaks to the compose setup go in a
gitignored `docker-compose.override.yml`.

## Known limitations / next steps

- **No idempotency yet**: the payload carries no `matchId`, so a duplicated report (e.g. a
  retry after a timeout) would count twice. Planned together with the extended contract.
- **Usernames are optional** until the game service starts sending them; entries without a
  username show only the player's UUID.
- **Bot matches**: the contract assumes two human players; how (or whether) bot matches
  affect ELO is still to be decided.
- **No authentication yet**: JWT validation at this service (or at the gateway) is pending.
