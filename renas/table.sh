#!/usr/bin/env bash

set -e
archive=${1%/}

shift $(expr $OPTIND - 1) # remove option args
JARPATH="AbbrExpansion/out"
PARSECODE="ParseCode-all.jar"
SEMANTICEXPAND="SemanticExpand-all.jar"
IDTABLE="idTable.csv"
EXTABLE="exTable.csv"
RECORD="record.json"
CLASS_RECORD="classRecord.json"

# renas
echo "${archive}"
echo "start creating table."

# remove jar signature
if [ -n "$(zipinfo -1 ${JARPATH}/${PARSECODE} | grep META-INF/.*SF)" ]; then
    echo "rm META-INF/*SF"
    zip -d "${JARPATH}/${PARSECODE}" 'META-INF/*SF'
fi

# create table
echo "${archive} Run ParseCode"
repo="${archive}/repo"
# parse code
java -jar "${JARPATH}/${PARSECODE}" "${archive}" 2>/dev/null

# semantic expand
cd "AbbrExpansion/code/SemanticExpand"
java -jar "out/libs/${SEMANTICEXPAND}" "/work/${archive}"
cd ../../..
# normalize
python3 -m renas.relationship.normalize "${archive}"
# gzip
echo "gzip tables"

gzip -f "${archive}/${EXTABLE}"
gzip -f "${archive}/${RECORD}"
gzip -f "${archive}/${CLASS_RECORD}"

echo "delete unnecessary file"
rm "${archive}/${IDTABLE}"
rm -rf "${repo}"
