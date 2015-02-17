# -*- coding: utf-8 -*-
import os
import sys
import re
import codecs

from alignment import Alignment,Hirschberg
from readers import AnnParser
from writers import AnnWriter

writer = AnnWriter()

def get_phrase(text):
    p = re.compile(ur'[a-zA-Z]+|[0-9]+|\s+|[.,;!\(\)]+')
    lista = []
    pre = 0
    for m in p.finditer(text):
        start = m.start()
        end = m.end()
        if pre < start:
            lista.append(text[pre:start])
        lista.append(text[start:end])
        pre = end
    return lista

for root,_,files in os.walk('input'):
    for f in files:        
        if not f.endswith('.txt'):
            continue
        pmid = f[:-4]
        print pmid
        alter = os.path.join(root,pmid+'.txt')
        alterFile = codecs.open(alter,'r','utf-8')
        alterText = alterFile.read().strip()
        alterFile.close()

        reader = AnnParser(root,pmid+'.ann')
        annotation = reader.parse()
        
        if len(annotation['T']) == 0:
            writer.write('output',pmid+'.ann',annotation)
            continue

        gold = os.path.join('output',pmid+'.txt')
        goldFile = codecs.open(gold,'r','utf-8')
        goldText = goldFile.read().strip()
        goldFile.close()

        entities = annotation['T']

        goldPhrases = get_phrase(goldText)
        alterPhrases = get_phrase(alterText)
        h = Hirschberg(goldPhrases,alterPhrases)
        #h = Hirschberg(list(goldText),list(alterText))
        alignGold,alignAlter = h.align()
        #print ''.join(alignGold)
        #print ''.join(alignAlter)
        alter2gold = h.map_alignment(''.join(alignGold),''.join(alignAlter))
        
        for k,e in entities.iteritems():    
            start = int(e.start)
            end = int(e.end)

            e.start = alter2gold[start]
            if alter2gold[end] - alter2gold[end-1] > 1:
                e.end = alter2gold[end-1]+1
            else:
                e.end = alter2gold[end]
            e.text = goldText[e.start:e.end]

        writer.write('output',pmid+'.ann',annotation)

