# -*- coding: utf-8 -*-

import sys
from pprint import pprint as pp
from readers import *
from writers import *

'''
f = open('examples/medline','r')
text = f.read()
f.close()

medline = MedlineReader()
print medline.parse(text)
sys.exit(0)
'''
path = 'examples'

reader = AnnReader()
res = reader.parse_file(path,'17438130.ann')
pp(res)
sys.exit(0)

reader = CorpusReader(path,'ann')
print reader.read()
sys.exit(0)
mapping = {'pro':'Gene','fam':'Family','com':'Complex'}
reader = SGMLReader(path,'24966530.txt')
reader.set_tag_entity_mapping(mapping)
res = reader.parse()

writer = AnnWriter()
writer.write('/var/www/brat/data/test/','24966530.ann',res)
print res['text']
