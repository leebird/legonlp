import os
import sys
import json
import codecs
from miRNA_target.extractor import MiRNATargetExtractor

path = os.path.dirname(__file__)
root = os.path.abspath(os.path.join(path,'..'))
sys.path.append(root)

from annotation.writers import AnnWriter
from settings import ner, pos_ner

# get input parameters
if len(sys.argv) < 12:
    print ('Usage: python extract.py '
           'raw ner split parse output_folder '
           'rawSux nerSux splitSux parseSux output_suffix id_list')
    sys.exit(1)

rawDir, nerDir, splitDir, parseDir, outputDir = [os.path.abspath(a) for a in sys.argv[1:6]]
rawSux, nerSux, splitSux, parseSux, outputSux = sys.argv[6:11]
docList = sys.argv[11].split(',')

# init all extractors
mirtex = MiRNATargetExtractor()

# recognizing on the order in settings.py
annos = None
for component in ner:
    if component == 'miRNA-target':
        recognizer = Banner()
        
    if annos is None:
        annos = extractor.extract([rawDir, nerDir, splitDir, parseDir],
                                  [rawSux, nerSux, splitSux, parseSux],
                                  docList)

# output
writer = AnnWriter()
for doc, anno in annos.iteritems():
    writer.write(outputDir, doc+outputSux, anno)

