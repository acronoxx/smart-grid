# SMART GRID PROJECT


## Objective

The goal is to develop software for managing smart energy grids.

The system manages electricity grids at the scale of a city. It also manages the users of a grid, the equipment that produces or consumes electricity within a grid, and time-series measurement data for the equipment (e.g., the instantaneous production/consumption of the equipment in watts).

The system provides services to:

* **Manage an energy grid**: creation and modification of a grid. A grid has a name and a description. It contains a set of users and a set of equipment.
* **Manage a user**: creation, modification, and deletion of a user. A user has a first name and a last name. They belong to a single grid. A user has access (for management purposes, not yet detailed) to a set of equipment. A piece of equipment can be managed by several users.
* **Manage a piece of equipment**: there are multiple types of equipment, which will evolve later based on future changes to the specifications. A piece of equipment has a name and a description. A piece of equipment is either a consumer or a producer of electricity. A piece of equipment is attached to only one grid.
  * A producer has an energy source (wind, sun, hydraulic dam, geothermal, etc.).
  * A consumer has a maximum instantaneous power consumption in watts.
  * There is currently only one type of consumer: electric vehicle charging stations. A charging station has a voltage in volts, a maximum current in amperes, and a type (a string).
  * Current producers are either solar panels or wind turbines.
    * A solar panel has an efficiency (a real value between 0 and 1).
    * A wind turbine has the height of its mast and the length of its blades, in meters.
* **Manage the time-series data of equipment**: each piece of equipment has a set of measurements. A measurement has a name and a unit. Each measurement of a piece of equipment contains a set of time-stamped values. A time-stamped value consists of a real value (the measured value) and a measurement date. (The measured data varies; e.g., instantaneous watts, wind turbine rotation speed, solar panel cell temperature, etc.)


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

We've also created automatic tests that covers most of the functionnalities of the project, using a python file (test_api.py) and the libraries in the file requirements.txt, every test was passed.
The basic tests but then expanded with AI to finish faster with every other handlers because it was redundant and we just wanted to see if everything seemed to work well.

## Bugs
There should not be any bugs on our side. Everything that was reported to us should have been treated properly.


## Notes
All CRUD (Create, Read, Update, Delete) methods have been added for the main classes
We sticked to the API Routes Details, despite the fact that the HTTP method POST and PUT have been swapped?

Private utility methods have been added to reduce redundancy. Others utility methods have been added to retrieve parameters in the path or JSON, because those methods were redundant and depend on the key that we must define according to the Api Route Details, so we think it is better to put that in a single method so that we can easily change those keys.

Javadoc have been generated in the doc folder.