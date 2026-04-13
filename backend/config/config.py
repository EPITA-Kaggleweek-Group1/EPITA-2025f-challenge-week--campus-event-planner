import os


def get_app_db_config():
    return {
        "host": os.getenv("DB_HOST", "127.0.0.1"),
        "port": int(os.getenv("DB_PORT", "3306")),
        "user": os.getenv("APP_DB_USER"),
        "password": os.getenv("APP_DB_PASSWORD"),
        "database": os.getenv("APP_DB_NAME", "campus_events"),
    }


def get_test_db_config():
    return {
        "host": os.getenv("DB_HOST", "127.0.0.1"),
        "port": int(os.getenv("DB_PORT", "3306")),
        "user": os.getenv("TEST_DB_USER"),
        "password": os.getenv("TEST_DB_PASSWORD"),
        "database": os.getenv("TEST_DB_NAME", "campus_events_test"),
    }
