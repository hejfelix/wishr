#!/bin/bash
set -x #make script verbose

docker pull 1science/sbt:0.13.8-oracle-jre-8
docker pull schodemeiss/npm-tools

#Build server / client
docker run -ti --rm -v "$PWD:/app" -v "$HOME/.ivy2":/root/.ivy2 1science/sbt:0.13.8-oracle-jre-8 \
\sbt clean compile client/fastOptJS server/pack

#Fetch JS dependencies
docker run --rm -it -v "$PWD":/app schodemeiss/npm-tools npm install

#Webpack JS dependencies
docker run --rm -it -v "$PWD":/app schodemeiss/npm-tools webpack

cp *.html ./server/target/pack/
cp styles.css ./server/target/pack/
mkdir -p ./server/target/pack/client/target/scala-2.11/
cp ./client/target/scala-2.11/client-fastopt.js ./server/target/pack/client/target/scala-2.11/client-fastopt.js
cp -r ./assets ./server/target/pack/
cp -r ./graphics ./server/target/pack
