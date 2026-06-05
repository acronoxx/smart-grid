import pytest
import psycopg2

SEED_MAX_DATAPOINT_ID = 3300

@pytest.fixture(scope="session", autouse=True)
def clean_test_data():
    conn = psycopg2.connect(host="localhost", port=5432, dbname="test", user="test", password="test")
    cur = conn.cursor()
    cur.execute("DELETE FROM datapoint WHERE id > %s", (SEED_MAX_DATAPOINT_ID,))
    conn.commit()
    cur.close()
    conn.close()
    yield
