# Test suite for the Smart Grid API, covering CRUD operations for every entity
# and ensuring data integrity, ingress, and analytics.

import pytest
import requests

BASE_URL = "http://localhost:8080"

ENTITIES = {
    "person": {
        "create_payload": {"first_name": "Test", "last_name": "User", "grid": 1, "owned_sensors": []},
        "update_payload": {"last_name": "User Changed"},
        "check_field": "last_name",
    },
    "grid": {
        "create_payload": {"name": "Test Grid", "description": "desc"},
        "update_payload": {"name": "Test Grid Changed"},
        "check_field": "name",
    },
    "sensor": {
        "create_payload": {"name": "Test Sensor", "description": "desc", "kind": "SolarPanel", "grid": 1, "owners": [], "available_measurements": [], "power_source": "solar", "efficiency": 0.2},
        "update_payload": {"name": "Test Sensor Changed"},
        "check_field": "name",
    },
    "measurement": {
        "create_payload": {"name": "power", "unit": "W", "sensor": 1, "datapoints": []},
        "update_payload": {"name": "powerChanged"},
        "check_field": "name",
    },
}

# Reset test data before running tests to ensure a clean state. This is done at the session level



# ===== Fixtures =====

@pytest.fixture
def created_grid():
    response = requests.put(f"{BASE_URL}/grid", json=ENTITIES["grid"]["create_payload"])
    assert response.status_code == 200
    grid_id = response.json().get("id")
    assert isinstance(grid_id, int)
    yield grid_id
    requests.delete(f"{BASE_URL}/grid/{grid_id}")

@pytest.fixture
def created_person():
    response = requests.put(f"{BASE_URL}/person", json=ENTITIES["person"]["create_payload"])
    assert response.status_code == 200
    person_id = response.json().get("id")
    assert isinstance(person_id, int)
    yield person_id
    requests.delete(f"{BASE_URL}/person/{person_id}")

@pytest.fixture
def created_sensor():
    response = requests.put(f"{BASE_URL}/sensor", json=ENTITIES["sensor"]["create_payload"])
    assert response.status_code == 200
    sensor_id = response.json().get("id")
    assert isinstance(sensor_id, int)
    yield sensor_id
    requests.delete(f"{BASE_URL}/sensor/{sensor_id}")

@pytest.fixture
def created_measurement():
    response = requests.put(f"{BASE_URL}/measurement", json=ENTITIES["measurement"]["create_payload"])
    assert response.status_code == 200
    measurement_id = response.json().get("id")
    assert isinstance(measurement_id, int)
    yield measurement_id
    requests.delete(f"{BASE_URL}/measurement/{measurement_id}")

# ===== General Tests =====

@pytest.mark.parametrize("list_route", ["persons", "grids", "sensors", "measurements"])
def test_list_returns_array_of_ints(list_route):
    response = requests.get(f"{BASE_URL}/{list_route}")
    assert response.status_code == 200
    ids = response.json()
    assert isinstance(ids, list)
    assert len(ids) == len(set(ids)), "Duplicate IDs in list"
    for i in ids:
        assert isinstance(i, int)

@pytest.mark.parametrize("entity", ["person", "grid", "sensor", "measurement"])
def test_get_nonexistent_returns_404(entity):
    response = requests.get(f"{BASE_URL}/{entity}/999999")
    assert response.status_code == 404

@pytest.mark.parametrize("entity", ["person", "grid", "sensor", "measurement"])
def test_delete_nonexistent_returns_404(entity):
    response = requests.delete(f"{BASE_URL}/{entity}/999999")
    assert response.status_code == 404

# ===== Grid Tests =====

def test_create_grid_returns_id(created_grid):
    assert isinstance(created_grid, int)

def test_get_grid_by_id(created_grid):
    response = requests.get(f"{BASE_URL}/grid/{created_grid}")
    assert response.status_code == 200
    data = response.json()
    assert data["id"] == created_grid
    assert data["name"] == ENTITIES["grid"]["create_payload"]["name"]
    assert "description" in data
    assert "users" in data
    assert "sensors" in data
    assert isinstance(data["users"], list)
    assert isinstance(data["sensors"], list)

def test_update_grid(created_grid):
    response = requests.post(f"{BASE_URL}/grid/{created_grid}", json=ENTITIES["grid"]["update_payload"])
    assert response.status_code == 200
    data = response.json()
    assert data["name"] == ENTITIES["grid"]["update_payload"]["name"]

def test_delete_grid(created_grid):
    response = requests.delete(f"{BASE_URL}/grid/{created_grid}")
    assert response.status_code == 200
    response = requests.get(f"{BASE_URL}/grid/{created_grid}")
    assert response.status_code == 404

def test_grid_create_missing_name_returns_500():
    response = requests.put(f"{BASE_URL}/grid", json={"description": "no name"})
    assert response.status_code == 500

def test_grid_create_missing_description_returns_500():
    response = requests.put(f"{BASE_URL}/grid", json={"name": "no desc"})
    assert response.status_code == 500

def test_grid_production_returns_number():
    response = requests.get(f"{BASE_URL}/grid/1/production")
    assert response.status_code == 200
    assert isinstance(response.json(), (int, float))

def test_grid_consumption_returns_number():
    response = requests.get(f"{BASE_URL}/grid/1/consumption")
    assert response.status_code == 200
    assert isinstance(response.json(), (int, float))

def test_grid_production_reference_value(grid1_production):
    response = requests.get(f"{BASE_URL}/grid/1/production")
    assert response.status_code == 200
    assert abs(response.json() - grid1_production) < 1.0

def test_grid_consumption_reference_value(grid1_consumption):
    response = requests.get(f"{BASE_URL}/grid/1/consumption")
    assert response.status_code == 200
    assert abs(response.json() - grid1_consumption) < 1.0

def test_grid_production_nonexistent_returns_404():
    response = requests.get(f"{BASE_URL}/grid/999999/production")
    assert response.status_code == 404

def test_grid_consumption_nonexistent_returns_404():
    response = requests.get(f"{BASE_URL}/grid/999999/consumption")
    assert response.status_code == 404

# ===== Person Tests =====

def test_create_person_returns_id(created_person):
    assert isinstance(created_person, int)

def test_get_person_by_id(created_person):
    response = requests.get(f"{BASE_URL}/person/{created_person}")
    assert response.status_code == 200
    data = response.json()
    assert data["id"] == created_person
    assert data["first_name"] == ENTITIES["person"]["create_payload"]["first_name"]
    assert data["last_name"] == ENTITIES["person"]["create_payload"]["last_name"]
    assert "grid" in data
    assert "owned_sensors" in data
    assert isinstance(data["owned_sensors"], list)

def test_update_person(created_person):
    response = requests.post(f"{BASE_URL}/person/{created_person}", json=ENTITIES["person"]["update_payload"])
    assert response.status_code == 200
    data = response.json()
    assert data["last_name"] == ENTITIES["person"]["update_payload"]["last_name"]

def test_delete_person(created_person):
    response = requests.delete(f"{BASE_URL}/person/{created_person}")
    assert response.status_code == 200
    response = requests.get(f"{BASE_URL}/person/{created_person}")
    assert response.status_code == 404

def test_person_create_missing_first_name_returns_500():
    response = requests.put(f"{BASE_URL}/person", json={"last_name": "x", "grid": 1})
    assert response.status_code == 500

def test_person_create_missing_last_name_returns_500():
    response = requests.put(f"{BASE_URL}/person", json={"first_name": "x", "grid": 1})
    assert response.status_code == 500

def test_person_create_missing_grid_returns_500():
    response = requests.put(f"{BASE_URL}/person", json={"first_name": "x", "last_name": "y"})
    assert response.status_code == 500

def test_person_is_in_list(created_person):
    response = requests.get(f"{BASE_URL}/persons")
    assert response.status_code == 200
    assert created_person in response.json()

# ===== Sensor Tests =====

def test_create_sensor_returns_id(created_sensor):
    assert isinstance(created_sensor, int)

def test_get_sensor_by_id(created_sensor):
    response = requests.get(f"{BASE_URL}/sensor/{created_sensor}")
    assert response.status_code == 200
    data = response.json()
    assert data["id"] == created_sensor
    assert data["name"] == ENTITIES["sensor"]["create_payload"]["name"]
    assert data["kind"] == "SolarPanel"
    assert "grid" in data
    assert "available_measurements" in data
    assert "owners" in data

def test_update_sensor(created_sensor):
    response = requests.post(f"{BASE_URL}/sensor/{created_sensor}", json=ENTITIES["sensor"]["update_payload"])
    assert response.status_code == 200
    data = response.json()
    assert data["name"] == ENTITIES["sensor"]["update_payload"]["name"]

def test_delete_sensor(created_sensor):
    response = requests.delete(f"{BASE_URL}/sensor/{created_sensor}")
    assert response.status_code == 200
    response = requests.get(f"{BASE_URL}/sensor/{created_sensor}")
    assert response.status_code == 404

def test_get_producers_returns_full_objects():
    response = requests.get(f"{BASE_URL}/producers")
    assert response.status_code == 200
    producers = response.json()
    assert isinstance(producers, list)
    for p in producers:
        assert "id" in p
        assert "kind" in p
        assert p["kind"] in ("SolarPanel", "WindTurbine")

def test_get_consumers_returns_full_objects():
    response = requests.get(f"{BASE_URL}/consumers")
    assert response.status_code == 200
    consumers = response.json()
    assert isinstance(consumers, list)
    for c in consumers:
        assert "id" in c
        assert c["kind"] == "EVCharger"

@pytest.mark.parametrize("kind", ["SolarPanel", "WindTurbine", "EVCharger"])
def test_get_sensors_by_kind(kind):
    response = requests.get(f"{BASE_URL}/sensors/{kind}")
    assert response.status_code == 200
    ids = response.json()
    assert isinstance(ids, list)
    for i in ids:
        assert isinstance(i, int)

def test_get_sensors_by_unknown_kind_returns_400():
    response = requests.get(f"{BASE_URL}/sensors/Unknown")
    assert response.status_code == 400

def test_ingress_solarpanel():
    response = requests.post(f"{BASE_URL}/ingress/solarpanel", data="1:25.0:500.0:1741334062", headers={"Content-Type": "text/plain"})
    assert response.status_code == 200

def test_ingress_windturbine():
    payload = {"windturbine": 2, "timestamp": 1741334062, "data": {"speed": 100.0, "power": 300.0}}
    response = requests.post(f"{BASE_URL}/ingress/windturbine", json=payload)
    assert response.status_code == 200
    assert response.text == "success"

def test_ingress_solarpanel_nonexistent_returns_404():
    response = requests.post(f"{BASE_URL}/ingress/solarpanel", data="999999:25.0:500.0:1741334062", headers={"Content-Type": "text/plain"})
    assert response.status_code == 404

def test_ingress_windturbine_nonexistent_returns_404():
    payload = {"windturbine": 999999, "timestamp": 1741334062, "data": {"speed": 100.0, "power": 300.0}}
    response = requests.post(f"{BASE_URL}/ingress/windturbine", json=payload)
    assert response.status_code == 404

# ===== Measurement Tests =====

def test_create_measurement_returns_id(created_measurement):
    assert isinstance(created_measurement, int)

def test_get_measurement_by_id(created_measurement):
    response = requests.get(f"{BASE_URL}/measurement/{created_measurement}")
    assert response.status_code == 200
    data = response.json()
    assert data["id"] == created_measurement
    assert data["name"] == ENTITIES["measurement"]["create_payload"]["name"]
    assert "sensor" in data
    assert "unit" in data

def test_update_measurement(created_measurement):
    response = requests.post(f"{BASE_URL}/measurement/{created_measurement}", json=ENTITIES["measurement"]["update_payload"])
    assert response.status_code == 200
    data = response.json()
    assert data["name"] == ENTITIES["measurement"]["update_payload"]["name"]

def test_delete_measurement(created_measurement):
    response = requests.delete(f"{BASE_URL}/measurement/{created_measurement}")
    assert response.status_code == 200
    response = requests.get(f"{BASE_URL}/measurement/{created_measurement}")
    assert response.status_code == 404

def test_get_measurement_values_structure():
    response = requests.get(f"{BASE_URL}/measurement/1/values")
    assert response.status_code == 200
    data = response.json()
    assert "sensor_id" in data
    assert "measurement_id" in data
    assert "values" in data
    assert isinstance(data["values"], list)
    for v in data["values"]:
        assert "timestamp" in v
        assert "value" in v

def test_get_measurement_values_with_range():
    response = requests.get(f"{BASE_URL}/measurement/1/values?from=0&to=9999999999")
    assert response.status_code == 200
    data = response.json()
    assert "values" in data

def test_get_measurement_values_nonexistent_returns_404():
    response = requests.get(f"{BASE_URL}/measurement/999999/values")
    assert response.status_code == 404
