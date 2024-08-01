#!/bin/bash

set -e
args=""

for line in  `cat renas/projects.txt`
do
args="${args} projects/${line}"
python3 -m renas.repository_analyzer $line
python3 -m renas.recommendation $line
done

#python3 -m renas.evaluator **option** $args