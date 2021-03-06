# -*- coding: utf-8 -*-
import os
import re
import sys
import codecs
import requests

path = os.path.dirname(__file__)
root = os.path.abspath(os.path.join(path,'../..'))
sys.path.append(root)

from annotate.annotate import *
from annotate.readers import SGMLReader

class pGenN:

    urlReg = 'http://biotm.cis.udel.edu/ruoyao/pGenN-gang/process.php'
    urlDown = 'http://biotm.cis.udel.edu/ruoyao/pGenN-gang/pmid-tag.txt'

    mapping = {'pro':'Gene','fam':'Family','com':'Complex'}

    def __init__(self):
        pass

    def recognize(self, pmidList):
        '''
        input a list of PMIDs
        '''
        pmidStr = '\n'.join(pmidList);
        data = {'pmid':pmidStr}
        r1 = requests.post(self.urlReg,data=data)
        r2 = requests.get(self.urlDown)
        res = r2.text.strip()

        if len(res) == 0:
            return None

        return self.split(r2.text.strip())

    def split(self,content):
        reader = SGMLReader()
        content = content.replace('\r','')
        abstracts = content.split('\n\n')

        res = {}

        for ab in abstracts:
            ab = ab.strip()
            lines = ab.split('\n');
            pmid = lines[0][7:]
            title = lines[1][5:]
            try:
                abstract = lines[2][5:]
            except:
                abstract = ''
                pass
            
            text = title.strip() + ' ' +abstract.strip()

            for line in lines[3:]:
                text += ' ' + line.strip()
                
            res[pmid] = reader.parse(text,self.mapping)

        return res
