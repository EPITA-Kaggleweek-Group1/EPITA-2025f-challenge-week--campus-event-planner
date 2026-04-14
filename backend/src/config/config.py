import os
from dotenv import load_dotenv
from pathlib import Path

_loaded = False


def load_env():
    global _loaded
    if _loaded:
        return

    current = Path(__file__).resolve()

    for parent in current.parents:
        env_file = parent / ".env"
        if env_file.exists():
            load_dotenv(env_file)
            _loaded = True
            break


def get_app_db_config():
    load_env()
    return {
        "host": os.getenv("DB_HOST", "127.0.0.1"),
        "port": int(os.getenv("DB_PORT", "3306")),
        "user": os.getenv("APP_DB_USER"),
        "password": os.getenv("APP_DB_PASSWORD"),
        "database": os.getenv("APP_DB_NAME", "campus_events"),
    }


def get_test_db_config():
    load_env()
    return {
        "host": os.getenv("DB_HOST", "127.0.0.1"),
        "port": int(os.getenv("DB_PORT", "3306")),
        "user": os.getenv("TEST_DB_USER"),
        "password": os.getenv("TEST_DB_PASSWORD"),
        "database": os.getenv("TEST_DB_NAME", "campus_events_test"),
    }
