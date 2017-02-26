FROM anapsix/alpine-java:8_server-jre

EXPOSE 8080

WORKDIR /app
ADD ./server/target/pack /app

CMD ["/app/bin/wish-r-application"]
