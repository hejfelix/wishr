#!/usr/bin/env bash

set -e

rm -v latest*

heroku pg:backups:capture --app wishr-lambdaminute
heroku pg:backups:download --app wishr-lambdaminute

pg_restore -f latest.sql latest.dump

cp -v latest.sql ../dumps/backup.sql

rm -v latest*
