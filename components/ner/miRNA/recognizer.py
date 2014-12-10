# -*- coding: utf-8 -*-

import re
from annotation.annotation import Annotation

class MiRNARecognizer:
    pattern = re.compile(
        r'(^|\s|[^a-zA-Z])'
        r'(([a-zA-Z]+-)?'
        r'(let|lin|micorR|miR|miRNA|microRNA)'
        r'(-| |x)?[0-9]+[a-zA-Z*]?'
        r'([\-+][b0-9][^\s,]*)?'
        r'((,| and| or) -?[0-9]+[a-c]*)*)',
        re.IGNORECASE)

    def __init__(self):
        pass
        
    def recognize(self, text):
        annotation = Annotation()
        annotation.text = text
        matches = self.pattern.finditer(text)
        for m in matches:
            annotation.add_entity('MiRNA', m.start(2), m.end(2), m.group(2))

        return annotation

    
