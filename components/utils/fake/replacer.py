import json
import codecs
import os
import sys

path = os.path.dirname(__file__)
root = os.path.abspath(os.path.join(path,'../..'))
sys.path.append(root)

from annotation.readers import AnnReader

class Faker:

    tmpl = 'N{}ENTITY'    

    def __init__(self):
        pass

    def fake(self, text, anno):
        entityMap = {}
        snippets = []    
        start = 0    

        entities = sorted(anno.get_entities().values(),key=lambda a:a.start)

        for t in entities:
            snippets.append(text[start:t.start])
            entityAttr = {'start': str(t.start),
                          'end': str(t.end),
                          'text': t.text,
                          'type': t.type}

            fakeEnt = self.tmpl.format(t.id[1:])
            entityMap[fakeEnt] = entityAttr

            snippets.append(fakeEnt)
            start = t.end

        snippets.append(text[start:])
        faketext = ''.join(snippets)
        return {'fake_text':faketext, 'entity_map':entityMap}

    def get_text(self, textfile):
        f = codecs.open(textfile,'r','utf-8')
        text = f.read()
        f.close()

        return text
