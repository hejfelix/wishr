
docker-compose down
docker-compose up -d

export PGPASSWORD="password"
until psql -h localhost -U "sa" -d postgres -c '\l' &> /dev/null; do
  >&2 echo "Postgres is unavailable - sleeping"
  sleep 1
done

pushd src/main/resources/db
    flyway -configFile=flyway.conf migrate
popd

sbt generateSlickCode
