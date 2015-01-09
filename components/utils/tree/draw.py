import sys

if len(sys.argv) < 2:
    print('Usage: python3 draw.py "parse tree"')
    sys.exit(0)

sys.path.append('../../..')
from offset_tree import *

parse = sys.argv[1]
leafreader = IndexedLeafReader()
tree = ParentedTree.fromstring(parse, read_leaf=leafreader.read_leaf)
tree.draw()
