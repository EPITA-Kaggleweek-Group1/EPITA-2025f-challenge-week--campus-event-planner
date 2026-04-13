"""
Database helper module for the Campus Event Planner.

Provides MySQL connection management and schema initialization.
"""

import os
import mysql.connector

DB_CONFIG = {
    "host": os.environ.get("DB_HOST", "localhost"),
    "port": int(os.environ.get("DB_PORT", "3306")),
    "user": os.environ.get("DB_USER", "root"),
    "password": os.environ.get("DB_PASSWORD", ""),
    "database": os.environ.get("DB_NAME", "campus_events"),
}


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

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS events (
            id INT AUTO_INCREMENT PRIMARY KEY,
            title VARCHAR(255) NOT NULL,
            description TEXT,
            date VARCHAR(30) NOT NULL,
            location VARCHAR(255),
            capacity INT DEFAULT 50,
            image_url TEXT,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    """)

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS registrations (
            id INT AUTO_INCREMENT PRIMARY KEY,
            event_id INT NOT NULL,
            user_name VARCHAR(255) NOT NULL,
            email VARCHAR(255) NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (event_id) REFERENCES events(id)
        )
    """)

    conn.commit()
    cursor.close()
    conn.close()
