import os
from bllipparser import RerankingParser, tokenize

class CharniakParser:
    
    def __init__(self):
        root = os.path.dirname(os.path.realpath(__file__))
        self.modelPath = os.path.join(root,'biomodel')
        self.parser = RerankingParser.from_unified_model_dir(self.modelPath)

    def parse(self, sentences):
        '''
        Input will be a list of sentences
        '''
        parses = []
        for sent in sentences:
            nbest_list = self.parser.parse(sent.encode('utf-8'))
            if len(nbest_list) == 0:
                parses.append('(NN PARSE_ERROR)')
                continue
            else:
                parse = nbest_list[0].ptb_parse
                parse = str(parse).decode('utf-8')
                parses.append(parse)        

        return parses
