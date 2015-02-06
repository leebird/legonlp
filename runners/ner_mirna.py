from runners.runner import Runner
from components.ner.miRNA.recognizer import MiRNARecognizer
from annotate.writers import AnnWriter
from annotate.readers import AnnReader


class MiRNARunner(Runner):
    runnerName = 'miRNAMention'

    def __init__(self):
        self.overwrite = True

    def set_overwrite(self, overwrite):
        self.overwrite = overwrite

    def run(self, inputs, outputs, docList):
        recognizer = MiRNARecognizer()
        tuples = self.get_io_files(inputs + outputs, docList)
        writer = AnnWriter()
        reader = AnnReader()

        for inputFile, inputAnnFile, outputTextFile, outputAnnFile in tuples:
            text = self.read_file(inputFile)
            anno = recognizer.recognize(text)

            prevAnno = reader.parse_file(inputAnnFile)
            anno.add_entities(prevAnno)

            if self.overwrite:
                anno.remove_overlap('MiRNA', 'Gene')
                anno.remove_overlap('MiRNA', 'Complex')
                anno.remove_included()

            writer.write(outputAnnFile, anno)
            self.write_file(anno.text, outputTextFile)
            
