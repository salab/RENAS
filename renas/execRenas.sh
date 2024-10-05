#!/bin/bash

set -e

for line in  `cat renas/projects.txt`
do
python3 -m renas.repository_analyzer "projects/${line}"
python3 -m renas.recommendation "projects/${line}"
done

#python3 -m renas.evaluator **option** $args