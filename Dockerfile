FROM anapsix/alpine-java:8_server-jre

EXPOSE 8080

WORKDIR /app
ADD ./server/target/pack /app
ADD ./client/target/scala-2.12/scalajs-bundler/main/client-fastopt.js /static/
ADD ./client/target/scala-2.12/scalajs-bundler/main/dependencies-library.js /static/
ADD ./client/public/index-fastopt.html /static/
ADD ./client/public/global.css /static/

CMD ["/app/bin/wish-r-application"]
