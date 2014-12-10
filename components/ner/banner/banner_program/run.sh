path=$1
fromdir=$2
suffix=$3
docList=$4

# compile
#javac -cp libs/dragontool.jar:libs/heptag.jar:libs/junit-4.4.jar:libs/mallet.jar:libs/medpost.jar:../../../libs/java/google-json/gson.jar:src/:../../../libs/java/stanford-corenlp/stanford-corenlp.jar gm.java

# run
java -cp libs/dragontool.jar:libs/heptag.jar:libs/junit-4.4.jar:libs/mallet.jar:libs/medpost.jar:$path/libs/java/google-json/gson.jar:src/:$path/libs/java/stanford-corenlp/stanford-corenlp.jar:. gm $fromdir $suffix $docList

