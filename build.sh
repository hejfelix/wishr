#!/usr/bin/env bash

set -e

sbt --client "project server" pack
sbt 'client/fullOptJS::webpack'

