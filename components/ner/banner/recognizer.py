import json
import os
import sys
import re
from subprocess import check_output

path = os.path.dirname(__file__)
root = os.path.abspath(os.path.join(path, '../..'))
sys.path.append(root)

from annotation.readers import SGMLReader


class Banner:
    '''
    mapping from SGML tag name to entity type name
    '''
    mapping = {'GENE': 'Gene'}

    def __init__(self):
        pass

    def recognize(self, fromDir, suffix, docList):
        '''
        return a hash whose key is the doc id, and the value is
        an annotation object.
        '''
        fromDir = os.path.abspath(fromDir)
        base = os.path.abspath(os.path.dirname(__file__))
        root = os.path.abspath(os.path.join(base, '../../..'))
        docs = ','.join(docList)
        cmd = 'cd ' + base + '/banner_program/;sh run.sh ' + root + ' ' + fromDir + ' ' + suffix + ' ' + docs
        out = check_output(cmd, shell=True)

        if len(out.strip()) == 0:
            return None

        reader = SGMLReader()

        res = json.loads(out)
        for docid in res:
            text = self.remove_extra_space(res[docid])
            res[docid] = reader.parse(text, self.mapping)

        return res

    def remove_extra_space(self, text):
        '''
        remove annoying interted spaces by banner
        should be done by alignment in the future
        '''

        # ( p = 0.1
        p1 = r'([\(\[])\s+'

        # end .
        p2 = r'\s+([,;:.)\]\'])'

        # mir - 1
        p3 = r'\s+([-\/])\s+'

        p4 = r'[ \t\r\f\v]+'

        # mir-1,-2,-3 and-4
        p5 = r'(,|and|or)(-)'

        # (p = 0. 001)
        p6 = r'(\(.*?[0-9])( *\. *)([0-9].*?\))'

        text = re.sub(p4, u' ', text, flags=re.U)
        text = re.sub(p1, r'\1', text, flags=re.U)
        text = re.sub(p2, r'\1', text, flags=re.U)
        text = re.sub(p2, r'\1' + u' ', text, flags=re.U)
        text = re.sub(p3, r'\1', text, flags=re.U)
        text = re.sub(p5, r'\1 \2', text, flags=re.U)
        text = re.sub(p6, r'\1.\3', text, flags=re.U)

        return text
