#!/bin/bash

args=""
projects=("testng" "jackson-databind" "restli" "activiti" "thunderbird-android" "genie" "eucalyptus" "graylog2-server" "core" "gnucash-android" "giraph")

for line in "${projects[@]}"
do
python3 -m renas.repository_analyzer "projects/${line}"
python3 -m renas.recommendation "projects/${line}"
args="${args} projects/${line}"
done

python3 -m renas.evaluator -rq2 -rq1 $args