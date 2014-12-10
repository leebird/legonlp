import os
import sys
sys.path.append('/home/leebird/bitbucket/annotation')
import re
import json
import codecs
from readers import AnnReader
from writers import AnnWriter

inputDir = 'input'
outputDir = 'output'

fakePat = re.compile(ur'N[0-9]+ENTITY')

def get_ori_index(index,text,entmap):
    snippet = text[:index]
    fakes = fakePat.findall(snippet)
    offset = 0

    for fake in fakes:
        length = len(entmap[fake]['text'])
        offset += (length - len(fake))
    return offset+index

reader = AnnReader()
writer = AnnWriter()
for root,_,files in os.walk(inputDir):
    for f in files:
        if not f.endswith('.txt'):
            continue

        pmid = f[:-4]

        txt = codecs.open(os.path.join(root,f),'r','utf-8')
        text = txt.read()
        txt.close()

        txt = codecs.open(os.path.join(root,pmid+'.ori'),'r','utf-8')
        oritext = txt.read()
        txt.close()

        ent = codecs.open(os.path.join(root,pmid+'.entmap'),'r','utf-8')
        entmap = json.load(ent)
        ent.close()

        anno = reader.parse_file(root,pmid+'.a1')

        if(os.path.isfile(os.path.join(root,pmid+'.a2'))):
            a2 = codecs.open(os.path.join(root,pmid+'.a2'),'r','utf-8')
            anno2 = a2.read().strip()
            a2.close()
            lines = anno2.split('\n')
            for line in lines:
                tokens = line.split('\t')
                typing = tokens[-1]
                direct = tokens[-2]
                if typing == 'R':
                    trigger = tokens[0].split(' ')
                    triggerStart = get_ori_index(int(trigger[0]),text,entmap)
                    triggerEnd = get_ori_index(int(trigger[1]),text,entmap)
                    triggerText = oritext[triggerStart:triggerEnd]

                    agent = tokens[1].split(' ')
                    agentText = oritext[int(agent[0]):int(agent[1])]
                    agentStart = int(agent[0])
                    agentEnd = int(agent[1])

                    theme = tokens[2].split(' ')
                    themeText = oritext[int(theme[0]):int(theme[1])]
                    themeStart = int(theme[0])
                    themeEnd = int(theme[1])

                    trigger = anno.add_entity('Target',triggerStart,triggerEnd,triggerText)
                    agent = anno.has_entity_prop('MiRNA',agentStart,agentEnd,agentText)
                    theme = anno.has_entity_prop('Gene',themeStart,themeEnd,themeText)
                    if theme is None:
                        theme = anno.has_entity_prop('Complex',themeStart,themeEnd,themeText)

                    if theme is None or agent is None:
                        continue
                    
                    event = anno.add_event('Target',trigger,[('Agent',agent),('Theme',theme)])
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

                    rel = anno.add_relation('Target',agent,theme)
                    rel.add_prop('direct',direct)
            
        writer.write(outputDir,pmid+'.ann',anno)

