#!/bin/bash

sed -i .bk 's/0.0.0/0.0.'"$TRAVIS_BUILD_NUMBER"'/' index.html