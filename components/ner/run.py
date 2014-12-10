import os
import sys
import json
import codecs
from miRNA.recognizer import MiRNARecognizer
from pGenN.recognizer import pGenN
from banner.recognizer import Banner
from fake.replacer import Faker

path = os.path.dirname(__file__)
root = os.path.abspath(os.path.join(path,'..'))
sys.path.append(root)

from annotation.writers import AnnWriter
from settings import ner, pos_ner

# get input parameters
if len(sys.argv) < 6:
    print ('Usage: python run.py '
           'input_folder output_folder '
           'input_suffix output_suffix id_list')
    sys.exit(1)

inputDir, outputDir = [os.path.abspath(a) for a in sys.argv[1:3]]
inputSux, outputSux = sys.argv[3:5]
docList = sys.argv[5].split(',')

# init all recognizers
mirna = MiRNARecognizer()
pgenn = pGenN()
banner = Banner()
faker = Faker()

# recognizing on the order in settings.py
annos = None
for component in ner:
    if component == 'banner':
        recognizer = Banner()
    elif component == 'pGenN':
        recognizer = pGenN()
    elif component == 'miRNA':
        recognizer = MiRNARecognizer()
        
    if annos is None:
        annos = recognizer.recognize(inputDir, inputSux, docList)
    else:
        annos2add = recognizer.recognize(inputDir, inputSux, docList)
        for doc, anno in annos.iteritems():
            if annos2add.has_key(doc):
                anno.add_entities(annos2add[doc])
    
    '''
    update altered text. Save the original text!
    '''
    for doc, anno in annos.iteritems():
        text = anno.text
        f = codecs.open(os.path.join(inputDir,doc+inputSux),'w','utf-8')
        f.write(text)
        f.close()

# output
writer = AnnWriter()
for doc, anno in annos.iteritems():
    anno.remove_overlap('MiRNA','Gene')
    anno.remove_overlap('MiRNA','Complex')
    anno.remove_included()
    writer.write(outputDir, doc+outputSux, anno)

# fake post-process
for component in pos_ner:
    if component == 'fake':
        faked = faker.fake([inputDir,outputDir],[inputSux,outputSux],docList)
        for doc, fakedContent in faked.iteritems():
            f = codecs.open(os.path.join(outputDir,doc+'.txt'),'w','utf-8')
            f.write(fakedContent['fake_text'])
            f.close()

            f = codecs.open(os.path.join(outputDir,doc+'.entmap'),'w','utf-8')
            json.dump(fakedContent['entity_map'],f)
            f.close()

