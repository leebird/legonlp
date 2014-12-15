from utils.runner import Runner
import json
import sys
from nltk import ParentedTree

class Leaf(object):
    def __init__(self, token, start, end):
        """
        leaf class for storing leaf node's token and start/end positions in the text
        :param token: leaf text
        :type token: str
        :param start: leaf starting position
        :type start: int
        :param end: leaf ending position
        :type end: int
        :return: None
        :rtype: None
        """
        self.token = token
        self.start = start
        self.end = end

    def __str__(self):
        return '{0}|{1}|{2}'.format(self.token, self.start, self.end)

    def __repr__(self):
        return str(self)


class LeafReader(object):

    class LeafTokenNotFound(Exception):
        def __init__(self, token):
            self.token = token

        def __str__(self):
            return repr('Leaf token {0} can\'t be found in text.'.format(self.token))

    def __init__(self, text):
        """
        read normal token leaf and add start/end positions
        :param text: the text on which the parse tree is based
        :type text: str
        :return: None
        :rtype: None
        """
        self.text = text
        self.pointer = 0

    def restore_brase(self, token):
        token = token.replace("-LRB-", "(")
        token = token.replace("-RRB-", ")")
        token = token.replace("-LCB-", "{")
        token = token.replace("-RCB-", "}")
        token = token.replace("-LSB-", "[")
        token = token.replace("-RSB-", "]")
        return token

    def read_leaf(self, token):
        """
        read a token and find its start/end positions in the text
        :param token: the token
        :type token: str
        :return: a leaf object
        :rtype: Leaf
        """

        restored_token = self.restore_brase(token)

        start = self.text.find(restored_token, self.pointer)
        if start == -1:
            raise self.LeafTokenNotFound(restored_token)
        end = start + len(restored_token)
        leaf = Leaf(token, start, end)
        self.pointer += 1
        return leaf


class IndexedLeafReader(object):
    def __init__(self):
        pass

    def read_leaf(self, token):
        """
        read an indexed leaf and return a leaf object
        :param token: indexed leaf, seperated by |, e.g., bad|0|3
        :type token: str
        :return: a leaf object
        :rtype: Leaf
        """
        token_text, start, end = token.split('|')
        start, end = int(start), int(end)
        leaf = Leaf(token_text, start, end)
        return leaf

class OffsetTreeRunner(Runner):
    def run(self, args):
        input_text = args["input"]["text"][0]
        input_parse = args["input"]["parse"][0]
        output_parse = args["output"]["parse"][0]
        doc_list = args["doc_list"]
        tuples = self.get_io_files([input_text, input_parse, output_parse], doc_list)

        for files in tuples:
            indexed_parses = []
            in_text_file, in_parse_file, out_parse_file = files

            text = self.read_file(in_text_file)

            parses = self.read_file(in_parse_file)
            parses = json.loads(parses)

            leafreader = LeafReader(text)

            for parse in parses:
                tree = ParentedTree.fromstring(parse, read_leaf=leafreader.read_leaf)
                indexed_parses.append(tree.pprint(margin=float("inf")))

            # in-json parses
            output = json.dumps(indexed_parses)

            self.write_file(output, out_parse_file)


if __name__ == '__main__':
    runner = OffsetTreeRunner()
    args = json.loads(sys.argv[1])
    runner.run(args)