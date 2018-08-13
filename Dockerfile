FROM anapsix/alpine-java:8_server-jre

EXPOSE 8080

WORKDIR /app
ADD ./server/target/pack /app
ADD ./client/build /static/

ARG JAVA_OPTS="-Xmx200m"

CMD ["/app/bin/wish-r-application"]
