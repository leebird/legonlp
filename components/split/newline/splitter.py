from utils.runner import Runner
import json
import sys


class NewlineSplitter():
    def __init__(self):
        pass

    def split(self, text):
        return text.split('\n')


class NewlineSplitterRunner(Runner):
    def run(self, input_info):
        input_text = input_info['input']['text'][0]
        output_split = input_info['output']['split'][0]
        doc_list = input_info['doc_list']

        tuples = self.get_io_files([input_text, output_split], doc_list)
        splitter = NewlineSplitter()

        for inputFile, outputFile in tuples:
            text = self.read_file(inputFile)
            sents = splitter.split(text)
            res = json.dumps(sents)
            self.write_file(res, outputFile)


if __name__ == '__main__':
    runner = NewlineSplitterRunner()
    args = json.loads(sys.argv[1])
    runner.run(args)