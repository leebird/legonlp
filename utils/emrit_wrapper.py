import sys
import os

path = __file__
base = os.path.abspath(os.path.join(os.path.dirname(path),'..'))
sys.path.append(base)
runners = os.path.join(base,'runners')

import re
import codecs
import tempfile
import shutil
from subprocess import call
from annotate.annotate import *
from annotate.readers import AnnReader
from annotate.writers import AnnWriter
from pipelines.emrit import Pipeline
from utils.dispatcher import Dispatcher

if len(sys.argv) < 3:
    print 'Usage: python '+sys.argv[0]+' sentences_file result_file'
    sys.exit(0)

senFile = sys.argv[1]
f = codecs.open(senFile,'r','utf-8')
text = f.read().strip()
f.close()

toFile = sys.argv[2]
resFile = codecs.open(toFile,'a','utf-8')

blocks = text.split('\n\n')
tmpl = '{0}~@~{1}~@~{2}~@~{3}~@~{4}~@~{5}~@~{6}\n'
pattern = re.compile(r'[0-9]+')

writer = AnnWriter()
reader = AnnReader()

for block in blocks:

    inputFolder = tempfile.mkdtemp(dir=Pipeline.tempFolder)
    outputFolder = tempfile.mkdtemp(dir=Pipeline.tempFolder)

    Pipeline.inputs = [inputFolder]
    Pipeline.output = outputFolder

    lines = block.strip().split('\n')
    mirLine, itermLine, variationsLine = lines[:3]
    
    mir = mirLine[4:].strip()
    iterm = itermLine[6:].strip()
    variations = variationsLine[11:].strip()
    match = pattern.search(mir)
    if match is None:
        continue
    mirNum = match.group()

    print mir,iterm

    genes = variations.split(', ')
    sentences = lines[3:]
    
    for sentence in sentences:
        needle = sentence.find(' ')
        sid = sentence[:needle].strip()
        text = sentence[needle:].strip()

        f = codecs.open(os.path.join(inputFolder,sid+'.txt'),
                        'w','utf-8')
        f.write(text)
        f.close()

        annotation = Annotation()

        for gene in genes:
            indices = re.finditer(re.escape(' '+gene+' '),
                                  text,re.IGNORECASE)
            for index in indices:
                start = index.start() + 1
                end = index.end() - 1
                annotation.add_entity('Gene',
                                      start,end,text[start:end])

            if text.lower().startswith(gene.lower()+' '):
                annotation.add_entity('Gene',
                                      0,len(gene),text[:len(gene)])

        writer.write(os.path.join(inputFolder,sid+'.ann'),annotation)
        

    d = Dispatcher(Pipeline)
    d.init_runners(runners)
    d.dispatch()
    
    for root,_,files in os.walk(outputFolder):
        for f in files:
            if not f.endswith('.ann'):
                continue
            
            docid = f[:-4]
            filepath = os.path.join(root, f)
            annotation = reader.parse_file(filepath)
            entities = annotation.get_entities()
            events = annotation.get_events().values()
            relations = annotation.get_relations().values()

            for event in events:
                args = event.args                
                agent = [a for a in args if a[0] == 'Agent'][0][1]
                theme = [a for a in args if a[0] == 'Theme'][0][1]
                direction = event.prop.get_prop('direction')[0]
                direct = event.prop.get_prop('direct')
                if direct is None:
                    direct = 'U'
                else:
                    direct = direct[0]
                if direction is None:
                    continue
                
                if direction == 'M2G':
                    mirna = agent.text
                    gene = theme.text
                elif direction == 'G2M':
                    mirna = theme.text
                    gene = agent.text
                else:
                    continue
                
                match = pattern.search(mirna)
                if match is None:
                    continue
                number = match.group()
                if number != mirNum:
                    continue
                
                line = tmpl.format(mir,iterm,docid,mirna,gene,direct,direction)
                resFile.write(line)


            for relation in relations:
                agent = relation.arg1[1]
                theme = relation.arg2[1]
                direction = relation.prop.get_prop('direction')[0]
                direct = relation.prop.get_prop('direct')[0]

                if direct is None:
                    direct = 'U'
                if direction is None:
                    continue

                if direction == 'M2G':
                    mirna = agent.text
                    gene = theme.text
                elif direction == 'G2M':
                    mirna = theme.text
                    gene = agent.text
                else:
                    continue

                match = pattern.search(mirna)

                if match is None:
                    continue
                number = match.group()
                if number != mirNum:
                    continue
                
                line = tmpl.format(mir,iterm,docid,mirna,gene,direct,direction)

                resFile.write(line)
    shutil.rmtree(inputFolder)
    shutil.rmtree(outputFolder)

resFile.close()
