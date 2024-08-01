#!/bin/bash

args=""
projects=("testng" "jackson-databind" "restli" "activiti" "thunderbird-android" "genie" "eucalyptus" "graylog2-server" "core" "gnucash-android" "giraph")

for line in "${projects[@]}"
do
args="${args} projects/${line}"
done

python3 -m renas.repository_analyzer -threshold $args
python3 -m renas.recommendation $args
python3 -m renas.evaluator -rq2 -rq1 $args