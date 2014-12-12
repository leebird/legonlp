#!/bin/sh

# tregex and corenlp 3.5 require java 1.8
# http://www.webupd8.org/2012/09/install-oracle-java-8-in-ubuntu-via-ppa.html

# compile
javac -cp ../lib/java/stanford-corenlp-3.5.0.jar:../lib/java/stanford-tregex-3.5.0.jar:../lib/java/AppleJavaExtensions.jar:../lib/java/gson-2.3.1.jar:../lib/java/commons-lang3-3.3.2.jar tregex.java

# run
# java -cp .:../lib/java/stanford-corenlp-3.5.0.jar:../lib/java/stanford-tregex-3.5.0.jar:../lib/java/AppleJavaExtensions.jar:../lib/java/gson-2.3.1.jar Tregex $1

#for NUM in `seq 1 1 100`
#do
#  python3 test.py
#done

python3 test.py