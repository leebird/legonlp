import os
from bllipparser import RerankingParser
from utils.runner import Runner
import json
import sys

class CharniakParser:
    def __init__(self, biomodel):
        self.parser = RerankingParser.from_unified_model_dir(biomodel.encode('utf-8'))

    def parse(self, sentences):
        """
        parse a list of sentences and return a list of parses
        :param sentences: a list of sentences
        :type sentences: list
        :return: a list of parses
        :rtype: list
        """
        parses = []
        for sent in sentences:
            nbest_list = self.parser.parse(sent.encode('utf-8'))
            if len(nbest_list) == 0:
                parses.append('(PARSE_ERROR)')
                continue
            else:
                parse = nbest_list[0].ptb_parse
                parse = str(parse).decode('utf-8')
                parses.append(parse)

        return parses


class CharniakRunner(Runner):

    def __init__(self):
        pass

    def run(self, args):
        """
        run Charniak parser to process input directory and
        write parses into output directory
        :param args: a dict containing model path, input/output path and doc list
        :type args: dict
        :return: None
        :rtype: None
        """
        parser = CharniakParser(args['biomodel'])
        input_split = args['input']['split'][0]
        output_parse = args['output']['parse'][0]
        doc_list = args['doc_list']

        tuples = self.get_io_files([input_split, output_parse], doc_list)

        for files in tuples:
            inFile, outFile = files
            sents = self.read_file(inFile)
            sents = json.loads(sents)
            parses = parser.parse(sents)
            # newline separated parses
            # output = '\n'.join(parses)

            # in-json parses
            output = json.dumps(parses)

            self.write_file(output, outFile)


if __name__ == '__main__':
    args = json.loads(sys.argv[1])
    runner = CharniakRunner()
    runner.run(args)
    # parser = CharniakParser(args['biomodel'])
    # use PYTHONPATH to run runner directly