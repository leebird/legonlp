import json
from runners.runner import Runner
from components.parse.charniak.parser import CharniakParser

class CharniakRunner(Runner):
    
    runnerName = 'Charniak'

    def __init__(self):
        pass

    def run(self, inputs, outputs, docList):
        parser = CharniakParser()

        tuples = self.get_io_files(inputs+outputs, docList)

        for files in tuples:
            inFile, outFile = files
            sents = self.read_file(inFile)
            sents = json.loads(sents)
            parses = parser.parse(sents)
            output = '\n'.join(parses)
            self.write_file(output, outFile)
