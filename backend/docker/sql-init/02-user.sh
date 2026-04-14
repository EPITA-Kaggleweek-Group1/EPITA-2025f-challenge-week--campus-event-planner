#!/bin/bash
set -e

echo ">>> Creating users and granting permissions..."

mysql -uroot -p"$MYSQL_ROOT_PASSWORD" <<EOF

-- app user (least privileges)
CREATE USER IF NOT EXISTS '$APP_DB_USER'@'%' IDENTIFIED BY '$APP_DB_PASSWORD';
GRANT ALL PRIVILEGES ON campus_events.* TO '$APP_DB_USER'@'%';

-- test user (permissions)
CREATE USER IF NOT EXISTS '$TEST_DB_USER'@'%' IDENTIFIED BY '$TEST_DB_PASSWORD';
GRANT ALL PRIVILEGES ON *.* TO '$TEST_DB_USER'@'%';

FLUSH PRIVILEGES;

EOF

echo ">>> Done creating users"
