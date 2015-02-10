# -*- coding: utf-8 -*-

from __future__ import print_function
import sys
import re
import os
import codecs
import json
import itertools
from pprint import pprint as pp
from annotation.annotate import *
from annotation.utils import *


class Reader(object):
    def __init__(self):
        self.annotation = Annotation()

    def parse(self, text):
        raise NotImplementedError('Reader.parse()')

    def parse_file(self, filepath):
        raise NotImplementedError('Reader.parse_file()')

    def parse_folder(self, path, suffix):
        res = {}

        for root, _, files in os.walk(path):
            for f in files:
                if not f.endswith(suffix):
                    continue
                docid = f[:-len(suffix)]
                filepath = os.path.join(root, f)
                anno = self.parse_file(filepath)
                res[docid] = anno

        return res

    def read(self):
        raise NotImplementedError('Reader.read()')


class AnnReader(Reader):
    def __init__(self, entity_handler=None, event_handler=None, relation_handler=None):
        super(AnnReader, self).__init__()
        self.entity_handler = entity_handler
        self.event_handler = event_handler
        self.relation_handler = relation_handler

    def parse_entity(self, line, annotation):
        fields = line.split('\t')
        try:
            info = fields[1].split(' ')
            tid = fields[0]
            text = fields[2]
            category = info[0]
            start = int(info[1])
            end = int(info[2])
            entity = annotation.add_entity(category, start, end, text)
            entity.property.add('id', tid)
            
            if self.entity_handler is not None:
                # handle appended information in entity line
                self.entity_handler(entity, fields[3:])
                
        except Entity.EntityIndexError:
            print('entity index error ' + line, file=sys.stderr)

    def parse_event(self, line, annotation):
        fields = line.split('\t')
        tid = fields[0]
        info = fields[1].split(' ')
        category = info[0].split(':')
        trigger_id = category[1]
        category_text = category[0]

        # attributes = {}
        # if len(fields) > 2:
        #     attributes = json.loads(fields[2])

        arguments = []
        for arg in info[1:]:
            arg_category, arg_entity_id = arg.split(':')
            entities = annotation.get_entity_with_property('id', arg_entity_id)
            if len(entities) > 0:
                entity = entities[0]
            else:
                print('Can\'t find entity by id: ' + arg, file=sys.stderr)
                continue
            arguments.append(Node(arg_category, entity))

        entities = annotation.get_entity_with_property('id', trigger_id)
        trigger = entities.pop(0)
        event = annotation.add_event(category_text, trigger, arguments)
        event.property.add('id', tid)

        if self.event_handler is not None:
            # handle appended information in entity line
            self.event_handler(event, fields[2:])

        # if len(fields) > 2:
        #     prop = json.loads(fields[2])
        #     for key, value in prop.items():
        #         event.property.add(key, value)
        #
        # event.property.update(attributes)

    def parse_relation(self, line, annotation):
        fields = line.split('\t')
        rid = fields[0]
        info = fields[1].split(' ')
        category_text = info[0]

        arguments = []
        for arg in info[1:]:
            arg_category, arg_entity_id = arg.split(':')
            entities = annotation.get_entity_with_property('id', arg_entity_id)
            if len(entities) > 0:
                entity = entities[0]
            else:
                print('Can\'t find entity by id: ' + arg, file=sys.stderr)
                continue
            arguments.append(Node(arg_category, entity))

        rel = annotation.add_event(category_text, None, arguments)
        rel.property.add('id', rid)

        if self.relation_handler is not None:
            # handle appended information in entity line
            self.relation_handler(entity, fields[2:])

        # if len(fields) > 2:
        #     prop = json.loads(fields[2])
        #     for key, value in prop.items():
        #         rel.property.add(key, value)

    def parse_special(self, line, annotation):
        annotation.special.append(line)
        
    def parse_file(self, filepath):
        annotation = Annotation()
        f = FileProcessor.open_file(filepath)

        if f is None:
            return annotation

        for line in f:
            line = line.strip('\r\n')
            if line.startswith('T'):
                self.parse_entity(line, annotation)

        # reset file pointer
        f.seek(0)

        for line in f:
            line = line.strip()
            if line.startswith('E'):
                self.parse_event(line, annotation)
            elif line.startswith('R'):
                self.parse_relation(line, annotation)
            elif line.startswith('*'):
                self.parse_special(line, annotation)
                # raise Exception('can not parse: '+line)

        f.close()
        return annotation

    def parse_folder(self, path, suffix):
        res = {}

        for root, _, files in os.walk(path):
            for f in files:
                if not f.endswith(suffix):
                    continue
                docid = f[:-len(suffix)]
                filepath = os.path.join(root, f)
                anno = self.parse_file(filepath)
                res[docid] = anno

        return res


class SGMLReader(Reader):
    def __init__(self, mapping=None, tag_handler=None):
        '''
        set mapping from tag name to entity type
        if <pro> means Protein, then a mapping from
        'pro' to 'Protein' should be set to have
        the entity typed 'Protein' in the output
        '''
        super(SGMLReader, self).__init__()
        if mapping is not None:
            self.mapping = mapping
        else:
            self.mapping = {}
        
        self.tag_handler = tag_handler

    def get_open_bracket(self, text):
        return TextProcessor.pattern_open_bracket.finditer(text)

    def get_close_bracket(self, text):
        return TextProcessor.pattern_close_bracket.finditer(text)

    def is_close(self, tag):
        return tag.startswith('</')

    def get_text_snippet(self, text, tags):
        '''
        get text snippets between each two tags
        entity text will be between an open-tag and a close-tag
        others are normal text
        '''
        snippets = {}
        length = len(text)
        start = 0
        for tag in tags:
            end = tag.start()
            snippets[(start, end)] = text[start:end]
            start = tag.end()
        snippets[(start, length)] = text[start:length]
        return snippets

    def get_entity_by_index(self, snippets, start, end):
        '''
        get entity by its tags' positions
        '''
        entityText, entityStart, entityEnd = '', -1, -1
        missingEnd = False
        currentPos = 0

        '''
        sort indices from text start to text end
        '''
        indices = sorted(snippets.keys(), key=lambda a: a[0])

        for pos in indices:
            text = snippets[pos]
            if pos[0] == start and pos[1] == end:
                entityText += text
                entityStart = currentPos
                entityEnd = currentPos + len(text)
                break
            elif pos[0] == start:
                entityText += text
                entityStart = currentPos
                missingEnd = True
            elif pos[1] == end:
                entityText += text
                entityEnd = currentPos + len(text)
                break
            elif missingEnd:
                entityText += text

            currentPos += len(text)

        return (entityText, entityStart, entityEnd)

    def parse_file(self, filepath):
        text = FileProcessor.read_file(filepath)
        anno = self.parse(text)
        return anno

    def parse(self, text):
        annotation = Annotation()
        openTags = self.get_open_bracket(text)
        closeTags = self.get_close_bracket(text)
        tags = list(openTags) + list(closeTags)
        orderedTags = sorted(tags, key=lambda a: a.start())

        snippets = self.get_text_snippet(text, orderedTags)

        openTagStack = []
        closeTagStack = []

        for tag in orderedTags:
            tagText = tag.group(1)
            tagFull = tag.group(0)
            if self.is_close(tagFull):
                startTag = openTagStack.pop()
                startTagText = startTag.group(1)
                
                # a tag handler to extract attrs or other stuff
                if self.tag_handler is not None:
                    tag_info = self.tag_handler(startTagText)
                    if 'tag' in tag_info:
                        startTagText = tag_info['tag']
                    
                '''
                open-tag and close-tag should have the same tag name
                if not skip this pair and continue
                if this happens, at least two entities are skip
                '''
                if startTagText != tagText:
                    print('different open and close tags', startTagText, tagText, file=sys.stderr)
                    continue

                start = startTag.end()
                end = tag.start()
                entity_text, start, end = self.get_entity_by_index(snippets, start, end)
                
                if self.tag_handler is not None and 'category' in tag_info:
                    category = tag_info['category']
                else:
                    try:
                        category = self.mapping[startTagText]
                    except KeyError:
                        category = startTagText

                annotation.add_entity(category, start, end, entity_text)
            else:
                openTagStack.append(tag)

        textpured = TextProcessor.remove_tags(text)
        annotation.text = textpured
        return annotation


class A1A2Reader(Reader):
    def __init__(self, a1path, a1file, a2path, a2file):
        self.a1filepath = os.path.join(a1path, a1file)
        self.a2filepath = os.path.join(a2path, a2file)
        # print self.filepath

    def parse_entity(self, line):
        fields = line.split('\t')
        info = fields[1].split(' ')
        tid = fields[0]
        text = fields[2]
        typing = info[0]
        start = int(info[1])
        end = int(info[2])
        self.annotation.add_exist_entity(tid, typing, start, end, text)

    def parse_event(self, line):
        fields = line.split('\t')
        tid = fields[0]
        info = fields[1].split(' ')
        typing = info[0].split(':')
        typeId = typing[1]
        typeText = typing[0]

        args = [arg.split(':') for arg in info[1:]]

        self.annotation.add_exist_event(tid, typeText, typeId, args)

    def parse_relation(self, line):
        fields = line.split('\t')
        rid = fields[0]
        info = fields[1].split(' ')
        typeText = info[0]

        args = [arg.split(':') for arg in info[1:]]

        self.annotation.add_exist_relation(rid, typeText, args[0], args[1])

    def parse(self):
        f = self.open_file(self.a1filepath)
        for line in f:
            line = line.strip()
            if line.startswith(Entity.linestart):
                self.parse_entity(line)
        f.close()

        f = self.open_file(self.a2filepath)
        for line in f:
            line = line.strip()
            if line.startswith(Entity.linestart):
                entity = self.parse_entity(line)

        # reset file pointer
        f.seek(0)

        for line in f:
            line = line.strip()
            if line.startswith(Event.linestart):
                self.parse_event(line)
            elif line.startswith(Relation.linestart):
                self.parse_relation(line)
                # raise Exception('can not parse: '+line)

        f.close()
        return annotation


class RlimsReader(Reader):
    def __init__(self, *args):
        super(RlimsReader, self).__init__(*args)
        self.separator = '{NP_1}PMID'
        self.hdOutput = 'OUTPUT '
        self.hdTrigger = 'PTM ='
        self.hdInducer = 'Inducer ='
        self.hdKinase = 'Kinase ='
        self.hdSubstrate = 'Substrate ='
        self.hdSite = 'Site ='
        self.hdNorm = 'NORM='
        self.hdSynonym = 'SYNONYM='
        self.rePMID = re.compile(r'PMID{/NP_1}.*?\{CP_2\}([0-9]*?)\{/CP_2\}')
        self.reTrigger = re.compile(r'\(\{(.*?)\};(.*?)\)')
        self.reArg = re.compile(r'\{(.*?)\}(.*?)\{/(.*?)\}')

        self.reAmino = re.compile(r'^\(\{(.*?)\}(.*?)\{/(.*?)\};')
        self.reSite = re.compile(r';\{(.*?)\}(.*?)\{/(.*?)\};')
        self.reSiteOther = re.compile(r';\{(.*?)\}(.*?)\{/(.*?)\}\)$')

        self.reTagged = re.compile(r'(\{(.*?)\})(.*?)(\{/.*?\})')

        self.status = 0
        self.mask = {11: 'kinase',
                     12: 'substrate',
                     13: 'site',
                     14: 'trigger',
                     15: 'inducer'}

    def split(self, text):
        blocks = text.split(self.separator)
        blocks = [self.separator + b for b in blocks[1:]]
        return blocks

    def init_output(self):
        output = {'trigger': [],
                  'kinase': [],
                  'inducer': [],
                  'substrate': [],
                  'site': []}
        return output

    def _parse(self, blocks):
        res = {}
        for b in blocks:
            lines = b.split('\n')
            # empty line is used to seperate outputs & sentences
            if len(lines) == 0:
                continue

            match = self.rePMID.search(lines[0])
            if match:
                self.pmid = match.group(1)
            else:
                self.pmid = 'unknown'
                raise PMIDUnknown(lines[0])

            res[self.pmid] = self.parse_block(lines[1:])
            sens = res[self.pmid]['sentence']
            res[self.pmid]['tag_indices'] = self.index_tag(sens)

        return res

    def parse_block(self, lines):
        res = {'sentence': [], 'output': [], 'norm': []}
        for l in lines:
            self.process_line(l, res)
        return res

    def process_line(self, l, res):
        if l.startswith(self.hdOutput):
            self.status = 1
            output = self.init_output()
            res['output'].append(output)
        elif l.startswith(self.hdNorm):
            self.status = 2
            res['norm'].append([l])
        elif l.startswith(self.hdSynonym):
            self.status = 3
            res['norm'][-1].append(l)
        elif l.startswith(self.hdKinase):
            self.status = 11
        elif l.startswith(self.hdSubstrate):
            self.status = 12
        elif l.startswith(self.hdSite):
            self.status = 13
        elif l.startswith(self.hdTrigger):
            self.status = 14
        elif l.startswith(self.hdInducer):
            self.status = 15
        elif len(l.strip()) == 0:
            self.status = 0

        if self.status == 0:
            if len(l.strip()) > 0:
                res['sentence'].append(l)
        elif self.status > 10:
            tokens = self.parse_line(l)
            needle = self.mask[self.status]
            if tokens is not None:
                res['output'][-1][needle].append(tokens)

    def parse_line(self, line):
        res = None
        if self.status == 14:
            match = self.reTrigger.search(line)
            if match:
                tag = match.group(1)
                text = match.group(2)
                text = self.remove_tags(text)
                res = (tag, text)
        elif self.status == 13:
            tag = None
            text = None
            amino = None
            siteOther = None
            match = self.reSite.search(line)
            if match:
                tag = match.group(1)
                text = match.group(2)
                text = self.remove_tags(text)
            match = self.reAmino.search(line)
            if match:
                amino = match.group(2)
            match = self.reSiteOther.search(line)
            if match:
                if text is None:
                    text = match.group(2)
                    text = self.remove_tags(text)
                if tag is None:
                    tag = match.group(1)

            if tag is not None and text is not None:
                res = (tag, text, amino)

        elif self.status == 11 or self.status == 12 \
                or self.status == 15:
            match = self.reArg.search(line)
            if match:
                tag = match.group(1)
                text = match.group(2)
                text = self.remove_tags(text)
                res = (tag, text)
        return res

    def parse(self):
        f = self.open_file(self.filepath)
        text = f.read()
        f.close()
        blocks = self.split(text)
        res = self._parse(blocks)
        return res

    def index_tag(self, taggedSens):
        tagIndices = {}
        sens = [self.remove_bracket(s) for s in taggedSens]
        braced = ' '.join(sens)
        sens = [self.remove_tags(s) for s in taggedSens]
        text = ' '.join(sens)
        match = self.reTagged.search(braced)
        while (match):
            match = self.reTagged.search(braced)
            tag = match.group(2)
            openTag = match.group(1)
            closeTag = match.group(4)
            phrase = match.group(3)
            start = match.start(1)
            end = start + len(phrase)
            tagIndices[tag] = (start, end, phrase)
            braced = braced.replace(openTag, '')
            braced = braced.replace(closeTag, '')
            match = self.reTagged.search(braced)
        return tagIndices


class RlimsVerboseReader(RlimsReader):
    def __init__(self, *args):
        super(RlimsVerboseReader, self).__init__(*args)
        self.hdMethod = '\tMethod='
        self.reMethod = re.compile(r'\[(.*?)\]')
        self.isMethod = False
        self.reTag = re.compile(r'\{/(.*?)\}')
        self.annotation = Annotation()

    def init_output(self):
        output = {'trigger': [],
                  'kinase': [],
                  'inducer': [],
                  'substrate': [],
                  'site': [],
                  'trigger_med': [],
                  'kinase_med': [],
                  'inducer_med': [],
                  'substrate_med': [],
                  'site_med': []}
        return output

    def parse_line(self, line):
        if self.isMethod:
            match = self.reMethod.search(line)
            if match:
                med = match.group(1)
            else:
                return None

            tokens = med.split('\r\r')

            for t in tokens:
                subtokens = t.split('\r')
                length = len(t)

                if length == 0:
                    return None

                phrase = self.remove_tags(subtokens[0])
                match = self.reTag.search(subtokens[0])
                if match:
                    tag = match.group(1)
                else:
                    raise NPPhraseNotFound(t)

                res = []
                for st in subtokens[1:]:
                    elements = st.split('|')
                    position = elements[0].split('..')
                    variances = elements[1:]
                    variances = [self.remove_tags(v) for v in variances]
                    start = int(position[0])
                    end = int(position[1])
                    res.append((tag, phrase, start, end, variances))
                if len(res) == 0:
                    res.append((tag, phrase, -1, -1, [phrase]))
            return res
        else:
            return super(RlimsVerboseReader, self).parse_line(line)


    def process_line(self, l, res):
        if l.startswith(self.hdMethod):
            self.isMethod = True
            tokens = self.parse_line(l)
            needle = self.mask[self.status] + '_med'
            if tokens is not None:
                res['output'][-1][needle] += tokens
        elif l.startswith('\t'):
            pass
        else:
            self.isMethod = False
            super(RlimsVerboseReader, self).process_line(l, res)

    def toBionlp(self, res):

        for pmid, v in res.iteritems():
            output = v['output']
            sens = v['sentence']
            tagIdx = v['tag_indices']

            sens = [self.remove_tags(s) for s in sens]
            abstract = ' '.join(sens)

            self.entities = {}
            self.events = {}
            self.entityIdx = 1
            self.eventIdx = 1

            for o in output:
                o = self.fake_method(o, 'trigger')
                o = self.fake_method(o, 'kinase')
                o = self.fake_method(o, 'substrate')
                o = self.fake_method(o, 'site')

                tri = o['trigger']
                triMed = o['trigger_med']
                sub = o['substrate']
                subMed = o['substrate_med']
                site = o['site']
                siteMed = o['site_med']

                indicesTri = self.reindex(tri, triMed, tagIdx)
                indicesSub = self.reindex(sub, subMed, tagIdx)
                indicesSite = self.reindex(site, siteMed, tagIdx, isSite=True)

                '''
                indicesTri = self.reindex_method(triMed,tagIdx)
                indicesSub = self.reindex_method(subMed,tagIdx)
                indicesSite = self.reindex_method(siteMed,tagIdx)
                '''

                if (len(indicesSub) == 0):
                    continue

                triggers = self.add_entities(indicesTri, 'Phosphorylation')
                proteins = self.add_entities(indicesSub, 'Protein')
                sites = self.add_entities(indicesSite, 'Entity')

                args = {'Theme': proteins, 'Site': sites}
                self.add_events(triggers[0], args, 'Phosphorylation')

                # self.rehash_entities()
                # self.rehash_events()
                # ann[pmid] = {'T':self.entities,'E':self.events,'text':abstract}

        return text, self.annotation

    def fake_method(self, output, needle):
        '''
        add method lines if there is none
        '''
        if len(output[needle + '_med']) == 0:
            sites = output[needle]
            fake = []
            for s in sites:
                fake.append((s[0], s[1], -1, -1, [s[1]]))
            output[needle + '_med'] = fake
        return output

    def add_events(self, trigger, args, eventType):
        done = False

        for t in args['Theme']:
            for s in args['Site']:
                arg = (('Theme', t), ('Site', s))
                done = True

                if not self.annotation.has_event_prop(eventType, trigger, arg):
                    self.annotation.add_event(eventType, trigger, arg)

                '''
                if self.events.has_key((trigger.id,arg)):
                    continue

                eid = 'E' + str(self.eventIdx)
                self.eventIdx += 1
                
                event = Event(eid,eventType,trigger.id,arg)
                self.events[(trigger.id,arg)] = event
                '''

        if not done:
            for t in args['Theme']:
                arg = (('Theme', t),)

                if not self.annotation.has_event_prop(eventType, trigger, arg):
                    self.annotation.add_event(eventType, trigger, arg)

                '''
                if self.events.has_key((trigger.id,arg)):
                    continue

                eid = 'E' + str(self.eventIdx)
                self.eventIdx += 1
                event = Event(eid,eventType,trigger.id,arg)
                self.events[(trigger.id,arg)] = event
                '''
                done = True

    def rehash_entities(self):
        '''
        change key from position tuple to entity index, e.g., T1
        '''
        entities = {}
        for t in self.entities.values():
            entities[t.id] = t
        del self.entities
        self.entities = entities

    def rehash_events(self):
        '''
        change key from tuple to event index, e.g., E1
        '''
        events = {}
        for e in self.events.values():
            events[e.id] = e
        del self.events
        self.events = events

    def add_entities(self, indices, entityType):
        '''
        create and add new entities
        indices format:
        [(start,end,text),...]
        '''
        res = []
        for i in indices:
            start = i[0]
            end = i[1]
            text = i[2]
            if not self.annotation.has_entity_prop(entityType, start, end, text):
                entity = self.annotation.add_entity(entityType, start, end, text)
                self.append(entity)
            '''
            if not self.entities.has_key((start,end)):
                tid = 'T' + str(self.entityIdx)
                self.entityIdx += 1
                entity = Entity(tid,entityType,start,end,text)
                self.entities[(start,end)] = entity
            res.append(self.entities[(start,end)])
            '''

        return res


    def reindex(self, annos, meds, tagIdx, isSite=False):
        '''
        update position index for various situations
        1. position in method line is present
        2. position is -1
        3. the extracted span not matched with the argument
        '''
        res = []
        for a, m in itertools.product(annos, meds):
            # check the phrase tags, they should be the same
            if a[0] != m[0]:
                continue

            # check the annotations, they should be the same
            if not isSite and a[1] != m[-1][-1]:
                continue

                # get information from annotation line and method line
            tag = a[0]

            '''
            get argument from method line, instead of annotation line
            this can fix the site case
            '''
            # argument = a[1]

            argument = m[-1][0]

            phrase = m[1]
            inStart = m[2]
            inEnd = m[3] + 1
            tagStart, tagEnd, phrase = tagIdx[tag]

            if inStart == -1:
                '''
                search argument in phrase if there is no position
                information in the method line
                '''
                inStart = phrase.find(argument)
                start = tagStart + inStart
                end = start + len(argument)
            else:
                '''
                recount position if there is position information
                in the method line
                '''
                inStart, inEnd = self.recount(phrase, inStart, inEnd)
                extracted = phrase[inStart:inEnd]
                if extracted != argument:
                    '''
                    search argument in the phrase if the position
                    is not matched with the argument
                    '''
                    inStart = phrase.find(argument)
                    start = tagStart + inStart
                    end = start + len(argument)
                else:
                    start = tagStart + inStart
                    end = tagStart + inEnd

            res.append((start, end, argument))

        return res


    def reindex_method(self, meds, tagIdx):
        '''
        update position index for various situations
        1. position in method line is present
        2. position is -1
        3. the extracted span not matched with the argument
        '''
        res = []
        for m in meds:

            # get information from the method line
            print(m)

            tag = m[0]
            argument = m[-1][0]
            phrase = m[1]
            inStart = m[2]
            inEnd = m[3] + 1
            tagStart, tagEnd, phrase = tagIdx[tag]

            if inStart == -1:
                '''
                search argument in phrase if there is no position
                information in the method line
                '''
                inStart = phrase.find(argument)
                start = tagStart + inStart
                end = start + len(argument)
            else:
                '''
                recount position if there is position information
                in the method line
                '''
                inStart, inEnd = self.recount(phrase, inStart, inEnd)
                extracted = phrase[inStart:inEnd]
                if extracted != argument:
                    '''
                    search argument in the phrase if the position
                    is not matched with the argument
                    '''
                    inStart = phrase.find(argument)
                    start = tagStart + inStart
                    end = start + len(argument)
                else:
                    start = tagStart + inStart
                    end = tagStart + inEnd

            if (start, end, argument) not in res:
                res.append((start, end, argument))

        return res

    def recount(self, text, start, end):
        '''
        update index based on actual string, including space
        RLIMS-P verbose output file's original index excludes
        the space.
        '''
        for i, c in enumerate(list(text)):
            if c == ' ' and i <= start:
                start += 1
            if c == ' ' and i < end:
                end += 1
        return start, end


class Rlims2Reader(Reader):
    def __init__(self, *args):
        super(Rlims2Reader, self).__init__(*args)
        self.pmid = None
        self.starter = 'date'
        self.rePMID = re.compile(r'PMID{/NP_1}.*?{CP_2}([0-9]*?){/CP_2}')
        self.startPoints = None

    def parse(self):
        res = {}
        f = self.open_file(self.filepath)
        for l in f:
            self.parse_line(l, res)
        f.close()
        self.toBionlp(res)
        self.rehash_entities()
        self.rehash_events()
        return {'T': self.entities, 'E': self.events, 'R': self.relations}

    def parse_line(self, l, res):
        if l.startswith('O'):
            idx = int(l[1:4])
            mid = l.find(' ', 5)
            hd = l[5:mid]
            if hd == self.starter:
                res[self.pmid]['output'].append({})
            res[self.pmid]['output'][-1][hd] = l[mid:].strip()
        elif l.startswith('S'):
            idx = int(l[1:4])
            sentence = l[4:].strip()
            if idx == 0:
                match = self.rePMID.search(sentence)
                if match:
                    self.pmid = match.group(1)
                    res[self.pmid] = {'output': [],
                                      'sentence': []}
                    return
                else:
                    raise PMIDNotFoundError(l)
            res[self.pmid]['sentence'].append(sentence)
        else:
            pass

    def toBionlp(self, res):
        for pmid, v in res.iteritems():
            self.entityIdx = 1
            self.eventIdx = 1
            self.relationId = 1
            self.entities = {}
            self.events = {}
            self.relations = {}

            self.sens = [self.remove_tags(s) for s in v['sentence']]
            lens = [len(s) for s in self.sens]
            self.abstract = ' '.join(self.sens)

            self.startPoints = [0]
            for l in lens[:-1]:
                self.startPoints.append(l + 1 + self.startPoints[-1])

            annotation = v['output']
            for a in annotation:
                trigger = self.parse_annotation(a['trigger'])
                kinases = self.parse_annotation(a['kinase'])
                substrates = self.parse_annotation(a['substrate'])
                sites = self.parse_annotation(a['site'])

                trigger = trigger[0]
                self.add_entities(trigger, 'Phosphorylation')

                proteins = [a[0:1] if len(a) == 1 else a[1:] for a in kinases]
                proteins = [p for pp in proteins for p in pp]
                self.add_entities(proteins, 'Protein')

                anaphora = [a[0:1] for a in kinases if len(a) > 1]
                anaphora = [p for pp in anaphora for p in pp]
                self.add_entities(anaphora, 'Anaphora')

                proteins = [a[0:1] if len(a) == 1 else a[1:] for a in substrates]
                proteins = [p for pp in proteins for p in pp]
                self.add_entities(proteins, 'Protein')

                anaphora = [a[0:1] for a in substrates if len(a) > 1]
                anaphora = [p for pp in anaphora for p in pp]
                self.add_entities(anaphora, 'Anaphora')

                phosSite = [a[0:1] if len(a) == 1 else a[1:] for a in sites]
                phosSite = [p for pp in phosSite for p in pp]
                self.add_entities(phosSite, 'Site')

                anaphora = [a[0:1] for a in sites if len(a) > 1]
                anaphora = [p for pp in anaphora for p in pp]
                self.add_entities(anaphora, 'Anaphora')

                argKinases = [a[0] for a in kinases]
                argSubstrates = [a[0] for a in substrates]
                argSites = [a[0] for a in sites]

                combine = [(tri, sub, kinase, site) for tri in trigger
                           for sub in argSubstrates
                           for kinase in argKinases
                           for site in argSites]

                if len(combine) == 0:
                    combine = [(tri, sub, kinase, None) for tri in trigger
                               for sub in argSubstrates
                               for kinase in argKinases]

                if len(combine) == 0:
                    combine = [(tri, sub, None, site) for tri in trigger
                               for sub in argSubstrates
                               for site in argSites]

                if len(combine) == 0:
                    combine = [(tri, sub, None, None) for tri in trigger
                               for sub in argSubstrates]
                if len(combine) == 0:
                    continue

                self.add_events(combine, 'Phosphorylation')
                self.add_relations(kinases, 'Coreference')
                self.add_relations(substrates, 'Coreference')
                self.add_relations(sites, 'Coreference')
                # self._toBionlp()

    def parse_annotation(self, annotation):
        annotation = annotation.strip()

        if len(annotation) == 0:
            return ()

        res = []
        args = annotation.split('|')

        for arg in args:
            subargs = arg.split(':')
            subargs = [subarg.split(' ') for subarg in subargs]
            subargs = [map(int, a) for a in subargs]
            subargs = self.get_positions(subargs)
            subargs = [tuple(a) for a in subargs]
            res.append(subargs)

        return res

    def add_entities(self, entities, entityRole):
        for t in entities:
            self.add_entity(t, entityRole)

    def add_entity(self, entity, entityRole):
        if not self.entities.has_key(entity):
            tIdx = 'T' + str(self.entityIdx)
            self.entityIdx += 1
            text = self.abstract[entity[0]:entity[1]]
            start = entity[0]
            end = entity[1]
            self.entities[entity] = Entity(tIdx, entityRole, start, end, text)

    def add_events(self, events, eventType):
        for e in events:
            self.add_event(e, eventType)

    def add_event(self, event, eventType):
        if not self.events.has_key(event):
            eIdx = 'E' + str(self.eventIdx)
            self.eventIdx += 1
            entities = []
            trigger = self.entities[event[0]]
            theme = self.entities[event[1]]
            args = [('Theme', theme)]
            if event[2] is not None:
                kinase = self.entities[event[2]]
                args.append(('Cause', kinase))
            if event[3] is not None:
                try:
                    site = self.entities[event[3]]
                    args.append(('Site', site))
                except:
                    print(self.filename)
                    print(self.abstract[event[3][0]:event[3][1]])
                    pp(self.entities)
                    print(event)

            self.events[event] = Event(eIdx, eventType, trigger.id, args)

    def add_relations(self, relations, relationType):
        for r in relations:
            if len(r) > 1:
                for p in r[1:]:
                    self.add_relation((r[0], p), relationType)


    def add_relation(self, relation, relationType):
        if not self.relations.has_key(relation):
            rid = 'R' + str(self.relationId)
            self.relationId += 1
            arg1 = self.entities[relation[0]]
            arg2 = self.entities[relation[1]]

            self.relations[relation] = Relation(rid, relationType, arg1, arg2)

    def get_positions(self, oldIndices):
        # print oldIndices
        return [self.get_position(i) for i in oldIndices]

    def get_position(self, oldIndex):
        base = self.startPoints[oldIndex[0] - 1]
        sen = self.sens[oldIndex[0] - 1]
        start = oldIndex[1]
        length = oldIndex[2]

        for i, c in enumerate(list(sen)):

            if i - start >= length:
                break

            if i <= start:
                if c == ' ':
                    start += 1
                continue

            if i > start and c == ' ':
                length += 1

        start = base + start
        end = start + length
        return (start, end)

    def rehash_entities(self):
        '''
        change key from position tuple to entity index, e.g., T1
        '''
        entities = {}
        for t in self.entities.values():
            entities[t.id] = t
        del self.entities
        self.entities = entities

    def rehash_events(self):
        '''
        change key from tuple to event index, e.g., E1
        '''
        events = {}
        for e in self.events.values():
            events[e.id] = e
        del self.events
        self.events = events


class MedlineReader(Reader):
    mapHead = {'PMID-': 'pmid',
               'TI  -': 'title',
               'AB  -': 'abstract',
               'DP  -': 'date',
               'AU  -': 'author',
               'TA  -': 'journal',
               '     ': 'previous'}

    def __init__(self):
        self.abstracts = {}

    def parse(self, text):
        lines = text.strip().split('\n')
        return self.iterparse(lines)

    def iterparse(self, iterator):
        currpmid = None
        needle = None
        self.abstracts = {}
        for line in iterator:
            line = line.rstrip()
            if len(line) == 0:
                pass

            head = line[:5]
            linetext = line[5:].strip()
            if len(linetext) == 0:
                continue

            if self.mapHead.has_key(head):
                if self.mapHead[head] == 'previous' and needle is not None:
                    if needle == 'abstract':
                        linetext = ' ' + linetext
                    self.abstracts[currpmid][needle] += linetext
                else:
                    needle = self.mapHead[head]
                    if needle == 'pmid':
                        currpmid = linetext
                        self.abstracts[currpmid] = {}
                    else:
                        self.abstracts[currpmid][needle] = linetext
            else:
                needle = None

        return self.abstracts
        
