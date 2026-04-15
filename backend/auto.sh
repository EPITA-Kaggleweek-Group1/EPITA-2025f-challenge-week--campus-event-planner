sudo systemctl stop mysql
docker compose down
docker compose up -d
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
cd src
python seed.py
python app.py
