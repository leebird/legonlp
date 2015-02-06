import os
from runners.runner import Runner
from components.ner.banner.recognizer import Banner
from annotate.writers import AnnWriter

class BannerRunner(Runner):

    runnerName = 'Banner'
    
    def __init__(self):
        pass

    def run(self, inputs, outputs, docList):
        recognizer = Banner()
        inputDir, inputSux = inputs[0]
        annos = recognizer.recognize(inputDir, inputSux, docList)        
        
        writer = AnnWriter()

        '''
        Since input and output files are not zipped, we go through
        doc list to output annotation and text
        '''
        outputTextDir, outputTextSux = outputs[0]
        outputAnnDir, outputAnnSux = outputs[1]

        for doc, anno in annos.iteritems():
            outputFile = os.path.join(outputAnnDir, doc+outputAnnSux)
            writer.write(outputFile, anno)

            outputFile = os.path.join(outputTextDir, doc+outputTextSux)
            self.write_file(anno.text, outputFile)
