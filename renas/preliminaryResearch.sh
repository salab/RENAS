#!/bin/bash

args=""
projects=("baasbox" "cordova-plugin-local-notifications" "morphia" "spring-integration")

for line in "${projects[@]}"
do
python3 -m renas.repository_analyzer "projects/${line}"
python3 -m renas.recommendation "projects/${line}"
args="${args} projects/${line}"
done

python3 -m renas.evaluator -sim -pre $args