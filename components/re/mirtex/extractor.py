# -*- coding: utf-8 -*-

import sys
import os
import tempfile
import shutil
from subprocess import call
import re
import codecs
import json

from annotation.annotation import Annotation
from annotation.readers import AnnReader

class MiRTex:

    fakePat = re.compile(ur'N[0-9]+ENTITY')

    def __init__(self):
        self.root = os.path.dirname(__file__)
        self.script = os.path.join(self.root,'run.sh')
        self.base = os.path.join(self.root,'data')    
        self.dataroot = tempfile.mkdtemp(dir=self.base)
        self.raw = os.path.join(self.dataroot,'text')
        self.parse = os.path.join(self.dataroot,'parse')
        self.simp = os.path.join(self.dataroot,'simp')
        self.ref = os.path.join(self.dataroot,'ref')
        os.mkdir(self.raw)
        os.mkdir(self.parse)
        os.mkdir(self.simp)
        os.mkdir(self.ref)

    def extract(self, inputs, docList):
        '''
        rawDir: .txt
        rawSplit .split
        nerDir: .ann .txt .entmap
        splitDir: .split
        parseDir: .parse
        '''
        raw, rawsplit, ner, fake, entmap, split, parse = inputs
        rawDir, rawSux = raw
        rawsplitDir, rawsplitSux = rawsplit
        nerDir, nerSux = ner
        fakeDir, fakeSux = fake
        mapDir, mapSux = entmap
        splitDir, splitSux = split
        parseDir, parseSux = parse        

        for doc in docList:
            rawFile = os.path.join(rawDir, doc+rawSux)
            rawsplitFile = os.path.join(rawsplitDir, doc+rawsplitSux)
            nerFile = os.path.join(nerDir, doc+nerSux)
            fakeFile = os.path.join(fakeDir, doc+fakeSux)
            mapFile = os.path.join(mapDir, doc+mapSux)            
            splitFile = os.path.join(splitDir, doc+splitSux)
            parseFile = os.path.join(parseDir, doc+parseSux)            

            outRawFile = os.path.join(self.raw, doc+'.ori')
            outRawsplitFile = os.path.join(self.raw, doc+'.split')
            outNerFile = os.path.join(self.raw, doc+'.a1')
            outFakeFile = os.path.join(self.raw, doc+'.txt')
            outMapFile = os.path.join(self.raw, doc+'.entmap')            
            outSplitFile = os.path.join(self.parse, doc+'.split')
            outParseFile = os.path.join(self.parse, doc+'.ptb')

            try:
                shutil.copyfile(rawFile,outRawFile)
                shutil.copyfile(rawsplitFile,outRawsplitFile)
                shutil.copyfile(splitFile,outSplitFile)
                shutil.copyfile(parseFile,outParseFile)
                shutil.copyfile(nerFile,outNerFile)
                shutil.copyfile(fakeFile,outFakeFile)
                shutil.copyfile(mapFile,outMapFile)
            except Exception:
                continue
            
        cmd = self.make_cmd()
        call(cmd, shell=True)

        try:
            annos = self.to_brat(docList)
        except Exception, err:
            print Exception, err
            annos = {}

        shutil.rmtree(self.dataroot)
        return annos

    def make_cmd(self):
        tmpl = 'cd {0}; sh {1} MiRNATarget {2}/;'
        return tmpl.format(self.root, self.script, self.dataroot)

    def to_brat(self, docList):
        reader = AnnReader()
        res = {}

        for doc in docList:
            
            text = self.get_text(os.path.join(self.raw, doc+'.txt'))
            oritext = self.get_text(os.path.join(self.raw, doc+'.ori'))

            entmapFile = codecs.open(os.path.join(self.raw, doc+'.entmap'),'r','utf-8')
            entmap = json.load(entmapFile)
            entmapFile.close()

            anno = reader.parse_file(os.path.join(self.raw, doc+'.a1'))
            anno.text = oritext

            a2File = os.path.join(self.raw,doc+'.a2')

            if not os.path.isfile(a2File):
                continue

            a2 = codecs.open(a2File,'r','utf-8')
            anno2 = a2.read().strip()
            a2.close()
            lines = anno2.split('\n')
            
            for line in lines:
                tokens = line.split('\t')
                typing = tokens[-1]
                direction = tokens[-2]
                direct = tokens[-3]
                if typing == 'R':
                    trigger = tokens[0].split(' ')
                    triggerStart = self.get_ori_index(int(trigger[0]),text,entmap)
                    triggerEnd = self.get_ori_index(int(trigger[1]),text,entmap)
                    triggerText = oritext[triggerStart:triggerEnd]

                    agent = tokens[1].split(' ')
                    agentText = oritext[int(agent[0]):int(agent[1])]
                    agentStart = int(agent[0])
                    agentEnd = int(agent[1])

                    theme = tokens[2].split(' ')
                    themeText = oritext[int(theme[0]):int(theme[1])]
                    themeStart = int(theme[0])
                    themeEnd = int(theme[1])
                
                    trigger = anno.add_entity('Trigger',triggerStart,triggerEnd,triggerText)
                    
                    if direction == 'M2G':                        
                        agent = anno.has_entity_prop('MiRNA',agentStart,agentEnd,agentText)
                        theme = anno.has_entity_prop('Gene',themeStart,themeEnd,themeText)
                    else:
                        agent = anno.has_entity_prop('Gene',agentStart,agentEnd,agentText)
                        theme = anno.has_entity_prop('MiRNA',themeStart,themeEnd,themeText)

                    if theme is None and direction == 'M2G':
                        theme = anno.has_entity_prop('Complex',themeStart,themeEnd,themeText)

                    if theme is None or agent is None:
                        continue

                    event = anno.add_event('Regulation',trigger,[('Agent',agent),('Theme',theme)])
                    event.add_prop('direction',direction)
                
                    if direction == 'M2G':
                        event.add_prop('direct',direct)

                else:
                    agent = tokens[0].split(' ')
                    agentText = oritext[int(agent[0]):int(agent[1])]
                    agentStart = int(agent[0])
                    agentEnd = int(agent[1])

                    theme = tokens[1].split(' ')
                    themeText = oritext[int(theme[0]):int(theme[1])]
                    themeStart = int(theme[0])
                    themeEnd = int(theme[1])

                    agent = anno.has_entity_prop('MiRNA',agentStart,agentEnd,agentText)
                    theme = anno.has_entity_prop('Gene',themeStart,themeEnd,themeText)

                    if theme is None:
                        theme = anno.has_entity_prop('Complex',themeStart,themeEnd,themeText)
                    if theme is None or agent is None:
                        continue
                    
                    rel = anno.add_relation('Regulation',agent,theme)
                    rel.add_prop('direction',direction)
                    if direction == 'M2G':
                        rel.add_prop('direct',direct)
            
            sentences = self.get_text(os.path.join(self.raw, doc+'.split'))
            sentences = json.loads(sentences)

            pos = 0
            for sent in sentences:
                sent = sent.strip()
                start = oritext.find(sent,pos)
                if start != -1:
                    end = start + len(sent)
                    anno.add_entity('Sentence',start,end,sent)
                    pos = end + 1
                
            res[doc] = anno

        return res
    
    def get_ori_index(self, index,text,entmap):
        snippet = text[:index]
        fakes = self.fakePat.findall(snippet)
        offset = 0

        for fake in fakes:
            length = len(entmap[fake]['text'])
            offset += (length - len(fake))
        return offset+index

    def get_text(self, textfile):
        f = codecs.open(textfile,'r','utf-8')
        text = f.read()
        f.close()

        return text
