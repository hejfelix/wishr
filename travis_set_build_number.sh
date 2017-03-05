#!/bin/bash

sed -i s/0[.]0[.]0/0.$TRAVIS_BUILD_NUMBER.0/ index.html
