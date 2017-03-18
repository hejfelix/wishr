#!/bin/bash
/app/bin/wish-r-application&
sleep 10
certbot certonly --staging -n --agree-tos --email $WISHR_CERTIFICATE_EMAIL --webroot -w /app/ -d wishr.lambdaminute.com
nginx
