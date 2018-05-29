#!/usr/bin/zsh
docker-compose down && docker-compose up -d

sleep 5
export PGPASSWORD=password
psql -U pg --host=localhost -p 5432 < ../dumps/backup.sql

pushd ./src/main/resources/db/
~/flyway-5.0.7/flyway -configFiles=./flyway.conf -baselineOnMigrate=true migrate 
popd
