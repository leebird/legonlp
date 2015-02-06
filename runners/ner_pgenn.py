import os
from runners.runner import Runner

from components.ner.pGenN.recognizer import pGenN
from annotate.writers import AnnWriter

class PGennRunner(Runner):
    
    runnerName = 'pGenN'

    def __init__(self):
        pass

    def run(self, inputs, outputs, docList):
        '''
        pGenN now is run through web API, so no input dir
        is needed. doc list is the required input.
        '''
        recognizer = pGenN()
        annos = recognizer.recognize(docList)

        outputTextDir, outputTextSux = outputs[0]
        outputAnnDir, outputAnnSux = outputs[1]

        writer = AnnWriter()

        for doc, anno in annos.iteritems():
            outputFile = os.path.join(outputAnnDir, doc+outputAnnSux)
            writer.write(outputFile, anno)

            outputFile = os.path.join(outputTextDir, doc+outputTextSux)
            self.write_file(anno.text, outputFile)
