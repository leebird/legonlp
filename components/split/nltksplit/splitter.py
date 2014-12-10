import nltk.data

class NLTKSplitter:

    splitter = None

    def __init__(self):
        if self.splitter is None:
            self.splitter = nltk.data.load('tokenizers/punkt/english.pickle')
        
    def split(self, text):
        sents = self.splitter.tokenize(text)
        return sents
