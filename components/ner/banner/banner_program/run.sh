#!/bin/sh
path=$1
fromdir=$2
suffix=$3
docList=$4

# compile
javac -cp "/home/leebird/Projects/legonlp/lib/java/*:/home/leebird/Projects/legonlp/components/ner/banner/banner_program:/home/leebird/Projects/legonlp/components/ner/banner/banner_program/src" gm.java

# run
#java -cp  gm $fromdir $suffix $docList

