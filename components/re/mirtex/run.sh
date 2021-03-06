myclasspath="bin:lib/*"

find src -name '*.java' > sources_list.txt
javac -target 1.6 -source 1.6 -Xlint -d "./bin" -classpath $myclasspath -sourcepath "./src" @sources_list.txt
rm -f sources_list.txt

java -cp $myclasspath mods.bmc.BatchProcessorBmcTest $1 $2
#java -cp $myclasspath -ea pengyifan.bionlpst.v2.ptb.PtbAligment
#java -cp $myclasspath -ea pengyifan.bionlpst.parser.CharniakParser

exit;

single() {
file="PMID-8622636"
java -Xms1024M -cp $myclasspath -ea pengyifan.bionlpst.v3.simp.GenerateSimplification $file
java -cp $myclasspath -ea ParCooSimplification $file
java -cp $myclasspath -ea pengyifan.bionlpst.v3.extractor.Extractor $file
java -cp $myclasspath -ea pengyifan.bionlpst.v3.extractor.RefExtractor $file
java -cp $myclasspath -ea pengyifan.bionlpst.v3.link.LinkArgument $file
#java -cp $myclasspath -ea pengyifan.bionlpst.v2.filter.BindingFilter $file
#java -cp $myclasspath -ea pengyifan.bionlpst.v2.filter.LocalizationFilter $file
java -cp $myclasspath -ea pengyifan.bionlpst.v2.filter.TranscriptionFilter $file
#java -cp $myclasspath -ea pengyifan.bionlpst.v2.filter.GeneExpressionFilter $file
#java -cp $myclasspath -ea pengyifan.bionlpst.v2.filter.PhosphorylationFilter $file
#java -cp $myclasspath -ea pengyifan.bionlpst.v2.filter.CatabolismFilter $file
java -cp $myclasspath -ea pengyifan.bionlpst.v2.filter.ActivityFilter $file
java -cp $myclasspath -ea pengyifan.bionlpst.v2.eval.Merge2A4 $file
java -cp $myclasspath -ea pengyifan.bionlpst.v2.filter.EventFilter $file
java -cp $myclasspath -ea pengyifan.bionlpst.bmc.EvalPrinter
#java -cp $myclasspath -ea pengyifan.bionlpst.v2.results.EventHtmlByTrigger
}

java -cp $myclasspath -ea pengyifan.bionlpst.bmc.EvalPrinter

batch() {
#java -cp $myclasspath -ea BatchProcessorTest GenerateSimplification
#java -cp $myclasspath -ea BatchProcessorTest ParCooSimplification
#java -cp $myclasspath -ea pengyifan.bionlpst.bmc.BatchProcessorBmcTest Extractor
#java -cp $myclasspath -ea pengyifan.bionlpst.bmc.BatchProcessorBmcTest RefExtractor
#java -cp $myclasspath -ea pengyifan.bionlpst.bmc.BatchProcessorBmcTest LinkArgument
java -cp $myclasspath -ea pengyifan.bionlpst.bmc.BatchProcessorBmcTest BindingFilter
java -cp $myclasspath -ea pengyifan.bionlpst.bmc.BatchProcessorBmcTest GeneExpressionFilter
java -cp $myclasspath -ea pengyifan.bionlpst.bmc.BatchProcessorBmcTest TranscriptionFilter
java -cp $myclasspath -ea pengyifan.bionlpst.bmc.BatchProcessorBmcTest LocalizationFilter
java -cp $myclasspath -ea pengyifan.bionlpst.bmc.BatchProcessorBmcTest PhosphorylationFilter
java -cp $myclasspath -ea pengyifan.bionlpst.bmc.BatchProcessorBmcTest CatabolismFilter
java -cp $myclasspath -ea pengyifan.bionlpst.bmc.BatchProcessorBmcTest ActivityFilter
java -cp $myclasspath -ea pengyifan.bionlpst.bmc.BatchProcessorBmcTest Merge2A4
java -cp $myclasspath -ea pengyifan.bionlpst.bmc.BatchProcessorBmcTest EventFilter
java -cp $myclasspath -ea pengyifan.bionlpst.bmc.EvalPrinter
java -cp $myclasspath -ea pengyifan.bionlpst.v2.results.EventHtmlByTrigger
}

if [ "$1" == 'single' ]; then
  single
elif [ "$1" == 'batch' ]; then
  batch
else
  echo "nothing is running"
  echo "bash run.sh single/batch"
fi
