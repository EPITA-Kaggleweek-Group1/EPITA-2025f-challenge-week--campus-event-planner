sudo systemctl stop mysql
docker compose up --build -d
docker compose exec app python src/seed.py
