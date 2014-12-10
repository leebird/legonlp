from runners.runner import Runner

from components.split.nltksplit.splitter import NLTKSplitter

class NLTKRunner(Runner):
    
    runnerName = 'NLTKSplit'

    def __init__(self):
        pass

    def run(self, inputs, outputs, docList):
        pairs = self.get_io_files(inputs, outputs, docList)
        splitter = NLTKSplitter()

        for inputFile, outputFile in pairs:
            text = self.read_file(inputFile)
            sents = splitter.split(text)
            res = json.dumps(sents)
            self.write_file(res, outputFile)
