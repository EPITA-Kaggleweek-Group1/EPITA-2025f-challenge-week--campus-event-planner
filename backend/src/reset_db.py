from config.config import get_app_db_config
from database import Database

db = Database(get_app_db_config())


def reset_db():
    conn = db.get_admin_connection()
    cursor = conn.cursor()

    cursor.execute("SET FOREIGN_KEY_CHECKS = 0")
    cursor.execute("TRUNCATE TABLE registrations")
    cursor.execute("TRUNCATE TABLE events")
    cursor.execute("SET FOREIGN_KEY_CHECKS = 1")

    conn.commit()
    cursor.close()
    conn.close()
