import pytest
import psycopg2

SEED_MAX_DATAPOINT_ID = 3300
DB = {"host": "localhost", "port": 5432, "dbname": "test", "user": "test", "password": "test"}


@pytest.fixture(scope="session", autouse=True)
def clean_test_data():
    conn = psycopg2.connect(**DB)
    cur = conn.cursor()
    cur.execute("DELETE FROM datapoint WHERE id > %s", (SEED_MAX_DATAPOINT_ID,))
    conn.commit()
    cur.close()
    conn.close()
    yield


@pytest.fixture(scope="session")
def grid1_production(clean_test_data):
    conn = psycopg2.connect(**DB)
    cur = conn.cursor()
    cur.execute("""
        SELECT COALESCE(SUM(max_val), 0) FROM (
            SELECT MAX(d.value) AS max_val
            FROM datapoint d
            JOIN measurement m ON d.measurement = m.id
            WHERE m.name = 'total_energy_produced'
              AND m.sensor IN (
                  SELECT p.id FROM producer p
                  JOIN sensor s ON p.id = s.id
                  WHERE s.grid = 1
              )
            GROUP BY m.id
        ) sub
    """)
    val = float(cur.fetchone()[0])
    cur.close()
    conn.close()
    return val


@pytest.fixture(scope="session")
def grid1_consumption(clean_test_data):
    conn = psycopg2.connect(**DB)
    cur = conn.cursor()
    cur.execute("""
        SELECT COALESCE(SUM(max_val), 0) FROM (
            SELECT MAX(d.value) AS max_val
            FROM datapoint d
            JOIN measurement m ON d.measurement = m.id
            WHERE m.name = 'total_energy_consumed'
              AND m.sensor IN (
                  SELECT c.id FROM consumer c
                  JOIN sensor s ON c.id = s.id
                  WHERE s.grid = 1
              )
            GROUP BY m.id
        ) sub
    """)
    val = float(cur.fetchone()[0])
    cur.close()
    conn.close()
    return val
