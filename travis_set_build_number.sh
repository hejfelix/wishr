#!/bin/bash

sed -i s/0[.]0[.]0/0.1.$TRAVIS_BUILD_NUMBER/ ./server/src/main/twirl/index.scala.html
