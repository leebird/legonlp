import os
import json
from runners.runner import Runner
from components.re.mirtex.extractor import MiRTex
from annotate.writers import AnnWriter

class MiRTexRunner(Runner):
    
    runnerName = 'miRTex'

    def __init__(self):
        pass

    def run(self, inputs, outputs, docList):
        mirtex = MiRTex()
        annos = mirtex.extract(inputs,docList)
        outText, outAnn = outputs        
        outTextDir, outTextSux = outText
        outAnnDir, outAnnSux = outAnn        

        writer = AnnWriter()

        for doc, anno in annos.iteritems():
            outTextPath = os.path.join(outTextDir, doc+outTextSux)
            self.write_file(anno.text, outTextPath)
            outAnnPath = os.path.join(outAnnDir, doc+outAnnSux)
            writer.write(outAnnPath,anno)
