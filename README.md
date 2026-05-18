# SMART GRID PROJECT
## Groupe : BARET Alexis, BA Marieme, HARINTWARI Irénée


## Structure
The project (fr\imta\smartgrid) is structured in the following way :
- model : Contains classes and basic methods (Construcors, Setters, Getters, toJSON)
- service : Contains SQL requests and complex methods
- server : Contains handlers to manage HTTP requests, and the server file


## Routes Details

### Grid
- PUT `/grid` — Create a new grid
- GET `/grids` — Get all grid IDs
- GET `/grid/:id` — Get grid details
- GET `/grid/:id/production` — Get total energy production of the grid
- GET `/grid/:id/consumption` — Get total energy consumption of the grid
- POST `/grid/:id` — Update a grid
- DELETE `/grid/:id` — Delete a grid

### Person
- PUT `/person` — Create a new person
- GET `/persons` — Get all person IDs
- GET `/person/:id` — Get person details
- POST `/person/:id` — Update a person
- DELETE `/person/:id` — Delete a person

### Sensor
- PUT `/sensor` — Create a new sensor
- GET `/sensors` — Get all sensor IDs
- GET `/producers` — Get all producer IDs
- GET `/consumers` — Get all consumer IDs
- GET `/sensors/:kind` — Get sensor IDs by kind
- GET `/sensor/:id` — Get sensor details
- POST `/ingress/solarpanel` — Receive a solar panel measurement (updates total production)
- POST `/ingress/windturbine` — Receive a wind turbine measurement (updates total production)
- POST `/sensor/:id` — Update a sensor
- DELETE `/sensor/:id` — Delete a sensor

### Measurement
- PUT `/measurement` — Create a new measurement
- GET `/measurements` — Get all measurement IDs
- GET `/measurement/:id` — Get measurement details
- GET `/measurement/:id/values` — Get measurement datapoints
- POST `/measurement/:id` — Update a measurement
- DELETE `/measurement/:id` — Delete a measurement


## Tests Details
We tried every route on the page that lists all routes to implement. Everything seemed to work fine.
We also tested every operations on the frontend and everything was working clearly fine.


## Bugs
There should not be any bugs on our side.


## Notes
All CRUD (Create, Read, Update, Delete) methods have been added for the main classes
We sticked to the API Routes Details, despite the fact that the HTTP method POST and PUT have been swapped?
Private utility methods have been added to reduce redundancy. Others utility methods have been added to retrieve parameters in the path or JSON, because those methods were redundant and depend on the key that we must define according to the Api Route Details, so we think it is better to put that in a single method so that we can easily change those keys.