import unittest

from nltk import ParentedTree

from offset_tree import *

class TestIndexedTree(unittest.TestCase):
    def test_read_normal_tree(self):
        leafreader = LeafReader('I have a book.')
        tree = ParentedTree.fromstring('(S1 (S (S (NP (PRP I)) (VP (VBP have) (NP (DT a) (NN book)))) (. .)))',
                               read_leaf=leafreader.read_leaf)
        print(tree.pprint(margin=float("inf")))

    def test_read_indexed_tree(self):
        leafreader = IndexedLeafReader()
        tree = ParentedTree.fromstring('(S1 (S (S (NP (PRP I|0|1)) (VP (VBP have|2|6) (NP (DT a|3|4) (NN book|9|13)))) (. .|13|14)))',
                                       read_leaf=leafreader.read_leaf)
        print(tree.pprint(margin=float("inf")))
        # tree = ParentedTree.fromstring('')

if __name__ == '__main__':
    unittest.main()