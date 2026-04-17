import os
from flask import Flask
from flask_cors import CORS
from config.config import get_app_db_config
from database import Database
from routes import register_events_routes
from extension import limiter

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
    app.secret_key = os.environ.get("FLASK_SECRET_KEY")
    SECRET_KEY = os.environ.get("JWT_SECRET_KEY")
    API_KEY = os.environ.get("ADMIN_API_KEY")
    
    limiter.init_app(app)
    register_events_routes(app)
    CORS(app, origins=[os.environ.get("ALLOWED_ORIGINS", "http://localhost:3000")])
    return app