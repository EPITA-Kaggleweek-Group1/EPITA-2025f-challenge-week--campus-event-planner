from app_factory import create_app
from config.config import get_app_db_config


app = create_app(get_app_db_config())

if __name__ == "__main__":
    print("Starting Campus Event Planner API on http://localhost:5000")
    app.run(host="0.0.0.0", port=5000, debug=True)
