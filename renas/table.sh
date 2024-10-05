#!/usr/bin/env bash

set -e
archive=${1%/}

shift $(expr $OPTIND - 1) # remove option args
JARPARSEPATH="AbbrExpansion/code/ParseCode"
JARSEMANTICPATH="AbbrExpansion/code/SemanticExpand"
PARSECODE="out/libs/ParseCode-all.jar"
SEMANTICEXPAND="out/libs/SemanticExpand-all.jar"
IDTABLE="idTable.csv"
EXTABLE="exTable.csv"
RECORD="record.json"
CLASS_RECORD="classRecord.json"

# renas
echo "${archive}"
echo "start creating table."

# remove jar signature
#if [ -n "$(zipinfo -1 ${JARPATH}/${PARSECODE} | grep META-INF/.*SF)" ]; then
#    echo "rm META-INF/*SF"
#    zip -d "${JARPATH}/${PARSECODE}" 'META-INF/*SF'
#fi

if [ ! -e "${JARPARSEPATH}/${PARSECODE}" ]; then
    "${JARPARSEPATH}/gradlew" shadowJar -b "${JARPARSEPATH}/build.gradle"
fi
if [ ! -e "${JARSEMANTICPATH}/${SEMANTICEXPAND}" ]; then
    "${JARPARSEPATH}/gradlew" shadowJar -b "${JARSEMANTICPATH}/build.gradle"
fi

# create table
echo "${archive} Run ParseCode"
repo="${archive}/repo"
# parse code
java -jar "${JARPARSEPATH}/${PARSECODE}" "${archive}" 2>/dev/null

# semantic expand
cd "${JARSEMANTICPATH}"
java -jar "${SEMANTICEXPAND}" "/work/${archive}"
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
