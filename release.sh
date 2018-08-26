#!/usr/bin/env bash
heroku container:login
heroku container:push web --app wishr-lambdaminute
heroku container:release web --app wishr-lambdaminute
