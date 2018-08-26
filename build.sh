#!/usr/bin/env bash

set -e

sbt "project server" pack
sbt 'client/fullOptJS::webpack'

