# Test API : Test suite for the Smart Grid API, covering CRUD operations for every entities and ensuring data integrity and proper error handling.

# Imports

import pytest
import requests
import psycopg2
import json
import time

# Constants

BASE_URL = "http://localhost:8080"

# ===== Grid Tests =====

@pytest.fixture
def new_grid_data():
    return {
        "name": "Test Grid",
        "description": "A grid created for testing purposes"
    }


@pytest.fixture
def created_grid(new_grid_data):
    response = requests.put(f"{BASE_URL}/grid", json=new_grid_data)
    assert response.status_code == 200
    grid_id = response.json().get("id")
    assert isinstance(grid_id, int)
    yield grid_id
    requests.delete(f"{BASE_URL}/person/{grid_id}")


# ===== Person Tests =====

@pytest.fixture
def new_person_data():
    return {
        "first_name": "Test",
        "last_name": "User",
        "grid": 1,
        "owned_sensors": []
    }

@pytest.fixture
def created_person(new_person_data):
    response = requests.put(f"{BASE_URL}/person", json=new_person_data)
    assert response.status_code == 200
    person_id = response.json().get("id")
    assert isinstance(person_id, int)
    yield person_id
    requests.delete(f"{BASE_URL}/person/{person_id}")


def test_get_persons_ids_are_valid():
    response = requests.get(f"{BASE_URL}/persons")
    assert response.status_code == 200
    persons = response.json()
    assert isinstance(persons, list)

    for pid in persons:
        assert isinstance(pid, int)
        get_response = requests.get(f"{BASE_URL}/person/{pid}")
        assert get_response.status_code == 200
        person = get_response.json()
        assert person["id"] == pid


def test_created_person_has_correct_id_and_data(created_person, new_person_data):
    response = requests.get(f"{BASE_URL}/person/{created_person}")
    assert response.status_code == 200
    data = response.json()
    for key in new_person_data:
        assert data[key] == new_person_data[key]


def test_delete_person_and_verify_removal(created_person):
    del_response = requests.delete(f"{BASE_URL}/person/{created_person}")
    assert del_response.status_code == 200

    get_response = requests.get(f"{BASE_URL}/person/{created_person}")
    assert get_response.status_code == 404


def test_delete_nonexistent_person():
    response = requests.delete(f"{BASE_URL}/person/999999")
    assert response.status_code == 404


def test_update_person_and_verify():
    person_data = {
        "first_name": "Before",
        "last_name": "Update",
        "grid": 1,
        "owned_sensors": []
    }
    put_response = requests.put(f"{BASE_URL}/person", json=person_data)
    assert put_response.status_code == 200
    person_id = put_response.json().get("id")
    assert isinstance(person_id, int)

    update_data = {
        "first_name": "After",
        "last_name": "Changed"
    }
    post_response = requests.post(f"{BASE_URL}/person/{person_id}", json=update_data)
    assert post_response.status_code == 200

    get_response = requests.get(f"{BASE_URL}/person/{person_id}")
    assert get_response.status_code == 200
    person = get_response.json()
    assert person["first_name"] == "After"
    assert person["last_name"] == "Changed"

    requests.delete(f"{BASE_URL}/person/{person_id}")

# ===== Sensor Tests =====




# ===== Measurement Tests =====