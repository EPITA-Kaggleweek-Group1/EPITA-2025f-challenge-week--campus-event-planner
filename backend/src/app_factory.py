from flask import Flask
from flask_cors import CORS
from config.config import get_app_db_config
from database import Database
from routes import register_events_routes


class App(Flask):
    db: Database


def create_app(config=None, db=None) -> App:
    app = App(__name__)

    if config is None:
        config = get_app_db_config()

    if db is None:
        db = Database(config)

    db.init_db()

    app.db = db
    app.secret_key = "changeme"
    SECRET_KEY = "super-secret-key-123"
    API_KEY = "sk-1234-epita-admin-key"
    register_events_routes(app)
    CORS(app)
    return app
