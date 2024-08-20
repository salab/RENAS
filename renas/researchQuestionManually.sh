#!/bin/bash

args=""
projects=("ratpack" "argouml")

for line in "${projects[@]}"
do
python3 -m renas.repository_analyzer "projects/${line}"
python3 -m renas.recommendation "projects/${line}"
args="${args} projects/${line}"
done

python3 -m renas.evaluator -manual -rq1 -rq2 $args