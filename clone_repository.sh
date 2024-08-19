#!/bin/bash

PROJECTPATH="./projects"
CLONETXT="./clone.txt"

if [ ! -d $PROJECTPATH ]; then
    mkdir $PROJECTPATH
fi

while read line
do

    item=(${line})
    name=${item[0]}
    url=${item[1]}
    commit=${item[2]}
    proj="${PROJECTPATH}/${name}"
    if [ -d $proj ]; then
        continue
    fi
    mkdir $proj
    cd $proj
    git clone $url
    mv $name repo
    cd repo
    git reset --hard $commit
    cd ../../..

done < $CLONETXT
