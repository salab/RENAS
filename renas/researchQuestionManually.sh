#!/bin/bash

args=""
projects=("ratpack" "argouml")

for line in "${projects[@]}"
do
args="${args} projects/${line}"
done

python3 -m renas.repository_analyzer $args
python3 -m renas.recommendation $args
python3 -m renas.evaluator -manual -rq1 -rq2 $args