FROM alpine:3.5

EXPOSE 8080

RUN apk update
RUN apk add openjdk8-jre
RUN apk add certbot
RUN apk add nginx
RUN apk add libcap
WORKDIR /app
ADD ./server/target/pack /app
ADD ./docker_cmd.sh /app
COPY ./default.conf /etc/nginx/conf.d

RUN setcap 'CAP_NET_BIND_SERVICE=+ep' /usr/sbin/nginx

CMD ["/bin/sh", "/app/docker_cmd.sh"]
