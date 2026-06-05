# Test API : Test suite for the Smart Grid API, covering CRUD operations for every entities and ensuring data integrity and proper error handling.

# Imports

import pytest
import requests
import psycopg2
import json
import time

# Constants

BASE_URL = "http://localhost:8080"

# Data for testing

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
        "create_payload": {"name": "Test Sensor", "description" : "desc" , "kind" : "SolarPanel", "grid": 1, "owners": [1,2], "available_measurements": [], "power_source": "solar", "efficiency": 0.2},
        "update_payload": {"name": "Test Sensor Changed"},
        "check_field": "name"
    },
    "measurement": {
        "create_payload": {"name": "power", "unit": "kW", "sensor": 1, "datapoints": []},
        "update_payload": {"name": "powerChanged"},
        "check_field": "name",
    }
}

# Fixtures

@pytest.fixture
def created_grid():
    data = ENTITIES["grid"]["create_payload"]
    response = requests.put(f"{BASE_URL}/grid", json=data)
    assert response.status_code == 200
    grid_id = response.json().get("id")
    assert isinstance(grid_id, int)
    yield grid_id
    requests.delete(f"{BASE_URL}/grid/{grid_id}")

@pytest.fixture
def created_person():
    data = ENTITIES["person"]["create_payload"]
    response = requests.put(f"{BASE_URL}/person", json=data)
    assert response.status_code == 200
    person_id = response.json().get("id")
    assert isinstance(person_id, int)
    yield person_id
    requests.delete(f"{BASE_URL}/person/{person_id}")

@pytest.fixture
def created_sensor():
    data = ENTITIES["sensor"]["create_payload"]
    response = requests.put(f"{BASE_URL}/sensor", json=data)
    assert response.status_code == 200
    sensor_id = response.json().get("id")
    assert isinstance(sensor_id, int)
    yield sensor_id
    requests.delete(f"{BASE_URL}/sensor/{sensor_id}")

@pytest.fixture
def created_measurement():
    data = ENTITIES["measurement"]["create_payload"]
    response = requests.put(f"{BASE_URL}/measurement", json=data)
    assert response.status_code == 200
    measurement_id = response.json().get("id")
    assert isinstance(measurement_id, int)
    yield measurement_id
    requests.delete(f"{BASE_URL}/measurement/{measurement_id}")

# ===== General Tests =====

@pytest.mark.parametrize("entity,list_route", [
    ("person", "persons"),
    ("grid", "grids"),
    ("sensor", "sensors"),
    ("measurement", "measurements"),
])
def test_get_ids_returns_valid_ids(entity, list_route):
    response = requests.get(f"{BASE_URL}/{list_route}")
    assert response.status_code == 200

    ids = response.json()
    assert isinstance(ids, list)

    assert len(ids) == len(set(ids)) # Check for duplicates
    
    for i in ids:
        assert isinstance(i, int)

@pytest.mark.parametrize("entity" ,[
    "person",
    "grid",
    "sensor",
    "measurement",
])  
def test_get_nonexistent_entity_returns_404(entity):
    response = requests.get(f"{BASE_URL}/{entity}/9999")
    assert response.status_code == 404


def test_delete_and_verify_removal(entity, created):
    del_response = requests.delete(f"{BASE_URL}/{entity}/{created}")
    assert del_response.status_code == 200

    get_response = requests.get(f"{BASE_URL}/{entity}/{created}")
    assert get_response.status_code == 404


def test_delete_nonexistent_person():
    response = requests.delete(f"{BASE_URL}/person/999999")
    assert response.status_code == 404

# ===== Grid Tests =====


# ===== Person Tests =====



# ===== Sensor Tests =====




# ===== Measurement Tests =====