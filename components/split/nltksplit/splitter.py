import sys
import json
args = json.loads(sys.argv[1])
nltk_data = args['nltk_data']

import os
os.environ['NLTK_DATA'] = nltk_data

import nltk.data
from utils.runner import Runner

class NLTKSplitter:
    splitter = None

    def __init__(self):
        if self.splitter is None:
            self.splitter = nltk.data.load('tokenizers/punkt/english.pickle')

    def split(self, text):
        sents = self.splitter.tokenize(text)
        return sents


class NLTKSplitterRunner(Runner):
    def run(self, args):
        input_text = args['input']['text'][0]
        output_split = args['output']['split'][0]
        doc_list = args['doc_list']

        tuples = self.get_io_files([input_text, output_split], doc_list)
        splitter = NLTKSplitter()

        for inputFile, outputFile in tuples:
            text = self.read_file(inputFile)
            sents = splitter.split(text)
            res = json.dumps(sents)
            self.write_file(res, outputFile)


if __name__ == '__main__':
    runner = NLTKSplitterRunner()
    args = json.loads(sys.argv[1])
    runner.run(args)