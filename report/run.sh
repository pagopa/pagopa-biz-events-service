#!/bin/bash

# example: sh ./run_integration_test.sh <local|dev|uat|prod>
set -e

# run integration tests
cd ./src || exit
yarn install
echo $2
DATE_RANGE=monthly yarn report:"$1"