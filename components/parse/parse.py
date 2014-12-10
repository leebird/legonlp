from charniak.parser import CharniakParser
import sys
import os
import codecs

path = os.path.dirname(__file__)
root = os.path.abspath(os.path.join(path,'..'))
sys.path.append(root)

from annotation.writers import AnnWriter
from settings import parse

# get input parameters
if len(sys.argv) < 6:
    print ('Usage: python run.py '
           'input_folder output_folder '
           'input_suffix output_suffix id_list')
    sys.exit(1)

inputDir, outputDir = [os.path.abspath(a) for a in sys.argv[1:3]]
inputSux, outputSux = sys.argv[3:5]
docList = sys.argv[5].split(',')

charniak = CharniakParser()
parses = None

for component in parse:
    if component == 'charniak':
        parser = charniak

    if parses is None:
        parses = parser.parse(inputDir, inputSux, docList)

# output
for doc, parseTrees in parses.iteritems():
    f = codecs.open(os.path.join(outputDir, doc+outputSux), 'w', 'utf-8')
    f.write('\n'.join(parseTrees))
    f.close()
