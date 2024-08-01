#!/bin/bash

args=""
projects=("baasbox" "cordova-plugin-local-notifications" "morphia" "spring-integration")

for line in "${projects[@]}"
do
args="${args} projects/${line}"
done

python3 -m renas.repository_analyzer $args
python3 -m renas.recommendation $args
python3 -m renas.evaluator -sim -pre $args