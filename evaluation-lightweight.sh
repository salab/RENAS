#!/bin/bash

args=""
projects="projects/baasbox"

python3 -m renas.repository_analyzer $projects
python3 -m renas.recommendation $projects
python3 -m renas.evaluator -sim -pre $projects
