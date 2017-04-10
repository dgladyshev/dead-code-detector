# dead-code-detector

[![Build Status](https://travis-ci.org/dgladyshev/dead-code-detector.svg?branch=master)](https://travis-ci.org/dgladyshev/dead-code-detector) [![Coverage Status](https://coveralls.io/repos/github/dgladyshev/dead-code-detector/badge.svg?branch=master)](https://coveralls.io/github/dgladyshev/dead-code-detector?branch=master)

Application that detects dead code in any public git repository. 
Given time.

## Run locally:

gradle bootRun

## Run locally in docker:

docker-compose up --build dead-code-detector-dev

## Java environment

LOGSTASH_HOST
LOGSTASH_PORT

## Container environment
MEMORY (in megabytes)