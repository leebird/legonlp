import os
import sys
import json
import codecs
from runners.runner import Runner
from components.split.newline.splitter import NewlineSplitter

class NewlineRunner(Runner):
    
    runnerName = 'Newline'

    def __init__(self):
        pass

    def run(self, inputs, outputs, docList):
        '''
        inputs: a list of (dir, suffix) pairs
        outputs: a list of (dir, suffix) pairs
        Note that dir should be an absolute path
        '''
        tuples = self.get_io_files(inputs+outputs, docList)
        splitter = NewlineSplitter()

        for inputFile, outputFile in tuples:
            text = self.read_file(inputFile)
            sents = splitter.split(text)
            res = json.dumps(sents)
            self.write_file(res, outputFile)
