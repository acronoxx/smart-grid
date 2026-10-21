# Smart Grid

REST backend for managing city-scale smart energy grids, built with **Java**, **Vert.x** and **JPA (EclipseLink) / PostgreSQL**.

The API manages grids, the people attached to them, the equipment that produces or consumes electricity, and the time-series measurements sent by that equipment. Simulated solar panels and wind turbines push live data to the API through dedicated ingress routes.

## Domain model

- **Grid**: a name and a description. Holds a set of people and a set of sensors (equipment).
- **Person**: a first name and a last name. Belongs to exactly one grid and can own several sensors; a sensor can have several owners.
- **Sensor** (equipment): a name and a description, attached to one grid. A sensor is either:
  - a **Producer**, with a power source (wind, sun, hydro, geothermal…):
    - **SolarPanel**: efficiency (between 0 and 1)
    - **WindTurbine**: mast height and blade length (meters)
  - a **Consumer**, with a maximum instantaneous power (watts):
    - **EVCharger**: voltage (V), maximum current (A) and connector type
- **Measurement**: a name and a unit, attached to one sensor (e.g. power in W, cell temperature in °C).
- **DataPoint**: a timestamped value belonging to a measurement.

## Tech stack

- Java 17+, Vert.x 5 (`vertx-core`, `vertx-web`)
- Jakarta Persistence 3.2 with EclipseLink 4 as the provider
- PostgreSQL (run through Docker Compose)
- Python + pytest for the API test suite

Dependencies are bundled as jars in `lib/` (no Maven/Gradle build).

## Project structure

```
src/fr/imta/smartgrid/
├── model/            JPA entities (Grid, Person, Sensor and subclasses, Measurement, DataPoint)
├── service/          database access and business logic
└── server/
    ├── VertxServer.java   entry point: database setup, routes, HTTP server on port 8080
    └── handler/           one HTTP handler per resource
src/META-INF/persistence.xml   JPA configuration (database URL and credentials)
lib/                  dependency jars
doc/                  generated Javadoc (open doc/index.html)
init_database.sql     schema and seed data loaded into PostgreSQL at startup
docker-compose.yml    PostgreSQL, Adminer, frontend and sensor simulators
test_api.py           pytest API test suite
conftest.py           shared pytest fixtures (test data cleanup, reference values)
```

## Getting started

### 1. Start the database and the companion services

```bash
docker compose up -d
```

| Service       | Port | Purpose                                                                         |
|---------------|------|---------------------------------------------------------------------------------|
| `db`          | 5432 | PostgreSQL (user `test`, password `test`, database `test`), seeded with `init_database.sql` |
| `adminer`     | 8081 | Web UI to browse the database                                                   |
| `front`       | 8082 | Web frontend using the API                                                      |
| `windturbine` | —    | Simulator posting readings to `/ingress/windturbine` (sensor 2)                 |
| `solar_panel` | —    | Simulator posting readings to `/ingress/solarpanel` (sensor 1)                  |

The frontend and the simulators reach the API running on the host through `host.docker.internal:8080`.

### 2. Build and run the API

From the project root:

```bash
javac -cp "lib/*" -d bin $(find src -name "*.java")
cp -r src/META-INF bin/
java -cp "bin:lib/*" fr.imta.smartgrid.server.VertxServer
```

On Windows, use `;` instead of `:` as the classpath separator (`"bin;lib/*"`).

The server listens on `http://localhost:8080`. Database settings are in `src/META-INF/persistence.xml`.

## API

Following the project specification, `PUT` creates a resource and `POST` updates it.

### Grid

| Method | Route | Description |
|---|---|---|
| PUT | `/grid` | Create a grid |
| GET | `/grids` | List grid IDs |
| GET | `/grid/:id` | Get a grid |
| GET | `/grid/:id/production` | Total energy produced by the grid |
| GET | `/grid/:id/consumption` | Total energy consumed by the grid |
| POST | `/grid/:id` | Update a grid |
| DELETE | `/grid/:id` | Delete a grid |

### Person

| Method | Route | Description |
|---|---|---|
| PUT | `/person` | Create a person |
| GET | `/persons` | List person IDs |
| GET | `/person/:id` | Get a person |
| POST | `/person/:id` | Update a person |
| DELETE | `/person/:id` | Delete a person |

### Sensor

| Method | Route | Description |
|---|---|---|
| PUT | `/sensor` | Create a sensor (`kind`: `SolarPanel`, `WindTurbine` or `EVCharger`) |
| GET | `/sensors` | List sensor IDs |
| GET | `/sensors/:kind` | List sensor IDs of a given kind |
| GET | `/producers` | List producer IDs |
| GET | `/consumers` | List consumer IDs |
| GET | `/sensor/:id` | Get a sensor |
| POST | `/sensor/:id` | Update a sensor |
| DELETE | `/sensor/:id` | Delete a sensor |
| POST | `/ingress/solarpanel` | Receive a solar panel reading (updates total production) |
| POST | `/ingress/windturbine` | Receive a wind turbine reading (updates total production) |

### Measurement

| Method | Route | Description |
|---|---|---|
| PUT | `/measurement` | Create a measurement |
| GET | `/measurements` | List measurement IDs |
| GET | `/measurement/:id` | Get a measurement |
| GET | `/measurement/:id/values` | Get the measurement's datapoints |
| POST | `/measurement/:id` | Update a measurement |
| DELETE | `/measurement/:id` | Delete a measurement |

## Tests

`test_api.py` covers:

- CRUD operations on every entity (grid, person, sensor, measurement)
- error cases: unknown IDs, missing required fields
- the ingress routes
- the grid production and consumption totals, checked against values computed directly in the database

With the database and the API running:

```bash
pip install -r requirements.txt
pytest test_api.py
```

Before the test session starts, `conftest.py` deletes the datapoints created by previous runs (IDs above the seed data) so the reference totals stay consistent.

All routes were also tested manually through the frontend.

## Design notes

- **Layering**: handlers parse the HTTP request and build the response, services hold the database logic, and models expose a `toJSON()` method used by the handlers.
- **JSON keys in one place**: each handler reads its path and body parameters through small private helpers (`getNameFromJSON`, …). The keys come from the API specification, so if one changes, it only needs to be updated in one place.
- **Shared private helpers** in the services avoid duplicating the same queries and checks across CRUD methods.
- **Inheritance**: `Sensor` → `Producer` / `Consumer` → concrete equipment types, mapped with JPA `JOINED` inheritance: one table per class, joined on the sensor ID.

## Documentation

The Javadoc is generated in `doc/` (open `doc/index.html`). To regenerate it:

```bash
javadoc -cp "lib/*" -d doc -sourcepath src -subpackages fr.imta.smartgrid
```
