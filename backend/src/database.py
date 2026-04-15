"""
Database helper module for the Campus Event Planner.

Provides MySQL connection management and schema initialization.
"""

from config.config import get_app_db_config
import mysql.connector


DB_CONFIG = get_app_db_config()


def get_db():
    """
    Return a MySQL connection with dictionary cursor support.
    Rows are returned as dictionaries for easy JSON serialization.
    """
    conn = mysql.connector.connect(**DB_CONFIG)
    return conn


def init_db():
    """
    Create the database and tables if they do not already exist.

    Tables:
      - events: stores campus event information
      - registrations: stores user registrations for events
        (endpoints for this table are NOT yet implemented — students must build them)
    """
    # First connect without database to create it if needed
    config_no_db = {k: v for k, v in DB_CONFIG.items() if k != "database"}
    conn = mysql.connector.connect(**config_no_db)
    cursor = conn.cursor()
    cursor.execute(f"CREATE DATABASE IF NOT EXISTS {DB_CONFIG['database']}")
    cursor.close()
    conn.close()

    conn = get_db()
    cursor = conn.cursor()

    # NOTE: We use VARCHAR(1024) for url. Since TEXT has maximum 64KB of data,
    # that means it's not good for storage and indexing.
    # We change it to VARCHAR(1024)
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS events (
            id INT AUTO_INCREMENT PRIMARY KEY,
            title VARCHAR(255) NOT NULL,
            description TEXT,
            date VARCHAR(30) NOT NULL,
            location VARCHAR(255),
            capacity INT NOT NULL DEFAULT 50,
            image_url VARCHAR(1024),
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    """)

    # Please refer to https://dev.mysql.com/doc/refman/8.0/en/create-table-foreign-keys.html
    # for ON DELETE CASCADE
    # We add UNIQUE KEY so that each user could only register once for one event
    # TODO: We need to create an user table. And change email/user_name to FOREIGN KEY
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS registrations (
            id INT AUTO_INCREMENT PRIMARY KEY,
            event_id INT NOT NULL,
            user_name VARCHAR(255) NOT NULL,
            email VARCHAR(255) NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            INDEX idx_event_id (event_id),
            UNIQUE KEY unique_event_email (event_id, email),
            FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
        )
    """)

    conn.commit()
    cursor.close()
    conn.close()
