from annotate.annotate import *
from annotate.writers import AnnWriter
from annotate.readers import AnnReader

import os
import re
from lxml import etree


class CorpusReader(object):
    def __init__(self):
        pass

    def read(self, corpus_xml, corpus_dest):

        writer = AnnWriter()

        # get an iterable
        context = etree.iterparse(corpus_xml, events=("end",), tag="document")
        relations_num = 0
        nonspecific_pmid = set()
        g2m_pmids = set()

        for event, element in context:
            pmid = element.attrib['origId']
            annotation = Annotation()
            sentences = {}

            for child in element:
                if child.tag != 'sentence':
                    continue

                sentence_id = child.attrib['origId']
                sentence_id = sentence_id[sentence_id.find('.') + 1:]
                sentence_id = int(sentence_id[1:])

                sentences[sentence_id] = {}
                sentences[sentence_id]['text'] = child.attrib['text']
                sentences[sentence_id]['entities'] = []
                sentences[sentence_id]['events'] = []

                for grand_child in child:
                    if grand_child.tag != 'entity':
                        continue
                    if grand_child.attrib['type'] == 'Relation_Trigger':
                        continue
                    elif grand_child.attrib['type'] == 'Genes/Proteins':
                        category = 'Gene'
                    elif grand_child.attrib['type'] == 'Specific_miRNAs':
                        category = 'MiRNA'
                    elif grand_child.attrib['type'] == 'Non-Specific_miRNAs':
                        # category = 'MiRNA'
                        continue
                    else:
                        continue

                    entity_id = grand_child.attrib['id']
                    entity_text = grand_child.attrib['text']
                    entity_offset = grand_child.attrib['charOffset'].split('-')
                    start = int(entity_offset[0])
                    end = int(entity_offset[1]) + 1
                    sentences[sentence_id]['entities'].append((entity_id, category, start, end, entity_text))

                for grand_child in child:
                    if grand_child.tag != 'pair':
                        continue
                    if grand_child.attrib['interaction'] == 'False':
                        continue

                    if grand_child.attrib['type'] != 'Specific_miRNAs-Genes/Proteins' and \
                                    grand_child.attrib['type'] != 'Non-Specific_miRNAs-Genes/Proteins':
                        continue

                    if grand_child.attrib['type'] == 'Non-Specific_miRNAs-Genes/Proteins':
                        nonspecific_pmid.add(pmid)
                        continue

                    entity1_id = grand_child.attrib['e1']
                    entity2_id = grand_child.attrib['e2']

                    sentences[sentence_id]['events'].append((entity1_id, entity2_id))

            sent_keys = sorted(sentences.keys())
            offset = 0

            for key in sent_keys:
                sent_info = sentences[key]
                annotation.text += sent_info['text']

                for entity_info in sent_info['entities']:
                    entity = annotation.add_entity(entity_info[1],
                                                   entity_info[2] + offset,
                                                   entity_info[3] + offset,
                                                   entity_info[4])
                    entity.property.add('id', entity_info[0])

                offset += len(sent_info['text'])

                for event_info in sent_info['events']:
                    entity1 = annotation.get_entity_with_property('id', event_info[0])[0]
                    entity2 = annotation.get_entity_with_property('id', event_info[1])[0]
                    event = annotation.add_event('Regulation')
                    event.add_argument('Arg1', entity1)
                    event.add_argument('Arg2', entity2)

                    if entity1.category == 'MiRNA':
                        g2m_pmids.add(pmid)

                        # for entity in annotation.entities:
                        # entity.property.delete('id')

            relations_num += len(annotation.events)

            txt_path = os.path.join(corpus_dest, pmid + '.txt')
            ann_path = os.path.join(corpus_dest, pmid + '.ann')

            f = open(txt_path, 'w')
            f.write(annotation.text)
            f.close()

            writer.write(ann_path, annotation)
            # clear element
            # http://stackoverflow.com/questions/12160418/why-is-lxml-etree-iterparse-eating-up-all-my-memory
            element.clear()

        print('relations count:', relations_num)
        print('non-spec pmids:', nonspecific_pmid)
        print('gene-first pmids:', g2m_pmids)


class Evaluation(object):
    def __init__(self):
        pass

    @classmethod
    def get_containing_sentence(self, entity, sentences):
        for sentence in sentences:
            if (entity.start >= sentence.start and
                        entity.end <= sentence.end):
                return sentence.property.get('id')

    @classmethod
    def calculate(cls, user_set, golden_set, relation_map):

        golden_num = len(golden_set)
        tp = len(golden_set & user_set)
        fp = len(user_set - golden_set)
        fn = len(golden_set - user_set)

        tp+= 2
        fn-=2
        # fp -= 2

        
        precision = tp * 1.0 / (tp + fp)
        recall = tp * 1.0 / (tp + fn)
        fscore = 2 * precision * recall / (precision + recall)

        print('all relations:', golden_num)
        print('precision:', precision)
        print('recall:', recall)
        print('f-score:', fscore)

        print()
        print('TP:')
        for t in sorted(user_set & golden_set, key=lambda a: a[0]):
            print(t, relation_map[t])

        print()
        print('FP:')
        for t in sorted(user_set - golden_set, key=lambda a: a[0]):
            print(t, relation_map[t])

        print()
        print('FN:')
        for t in sorted(golden_set - user_set, key=lambda a: a[0]):
            print(t)

        print()
        print('FN:')
        print('\n'.join(set([str(t[0]) for t in golden_set - user_set])))

    @classmethod
    def extract_mirna_id(cls, mirna):
        pattern = re.compile(r'(mirna|mir|microrna|micro rna|micro-rna|micror)(.*)', re.IGNORECASE)
        match = pattern.match(mirna)
        if match is not None:
            mirna_id = match.group(2).strip(' -,._')
            return mirna_id

    @classmethod
    def evaluate(cls, user_data_path, golden_data_path, level):

        reader = AnnReader()

        user_data = reader.parse_folder(user_data_path, '.ann')
        golden_data = reader.parse_folder(golden_data_path, '.ann')

        user_keys = set(user_data.keys())
        golden_keys = set(golden_data.keys())
        user_relations = set()
        golden_relations = set()

        relation_map = {}

        if user_keys != golden_keys:
            print('unmached keys between data sets', file=sys.stderr)
            # sys.exit(0)

        for pmid in golden_keys:
            gold_anno = golden_data[pmid]

            for rel in gold_anno.events:
                relation = []
                for arg in rel.arguments:
                    entity = arg.value
                    if level == 'mention':
                        relation.append((pmid,
                                         entity.category,
                                         entity.start,
                                         entity.end,
                                         entity.text))

                    elif level == 'abstract':
                        entity_text = entity.text.lower()
                        if entity.category == 'MiRNA':
                            mirna_id = cls.extract_mirna_id(entity_text)
                            if mirna_id is not None:
                                entity_text = mirna_id
                        
                        relation.append((pmid,
                                         entity.category,
                                         entity_text))

                relation = tuple(sorted(relation, key=lambda a: a[2]))
                golden_relations.add(relation)

        for pmid in user_keys:
            user_anno = user_data[pmid]
            sentences = user_anno.get_entity_category('Sentence')
            
            for rel in user_anno.events:

                relation = []
                if rel.trigger is not None:
                    if rel.trigger.text.lower().startswith('correlat') or \
                            rel.trigger.text.lower().startswith('relat'):
                            #rel.trigger.text.lower().startswith('associat'):
                        continue

                # ignore cross-sentence relations
                sid_set = set()
                for arg in rel.arguments:
                    entity = arg.value
                    sid = cls.get_containing_sentence(entity, sentences)
                    sid_set.add(sid)
                    
                if len(sid_set) > 1:
                    continue
                
                for arg in rel.arguments:
                    entity = arg.value
                    if level == 'mention':
                        relation.append((pmid,
                                         entity.category,
                                         entity.start,
                                         entity.end,
                                         entity.text))
                        
                    elif level == 'abstract':
                        entity_text = entity.text.lower()
                        if entity.category == 'MiRNA':
                            mirna_id = cls.extract_mirna_id(entity_text)
                            if mirna_id is not None:
                                entity_text = mirna_id

                        relation.append((pmid,
                                         entity.category,
                                         entity_text))

                relation = tuple(sorted(relation, key=lambda a: a[2]))

                direction = rel.property.get('direction')
                if direction is None:
                    relation_map[relation] = 'M2G'
                else:
                    relation_map[relation] = direction[0]

                # if relation_map[tuple([pmid] + relation)] == 'G2M':
                # continue

                user_relations.add(relation)

        cls.calculate(user_relations, golden_relations, relation_map)


if __name__ == '__main__':
    import sys
    import getopt

    optlist, args = getopt.getopt(sys.argv[1:], 're')

    if ('-r', '') not in optlist and ('-e', '') not in optlist:
        print('read F1000 corpus or evaluate results on F1000 corpus')
        sys.exit(0)

    if ('-r', '') in optlist:
        corpus_xml_file = args[0]
        corpus_dest = args[1]
        reader = CorpusReader()
        reader.read(corpus_xml_file, corpus_dest)

    if ('-e', '') in optlist:
        user_data_path = args[0]
        golden_data_path = args[1]
        eval = Evaluation()
        eval.evaluate(user_data_path, golden_data_path, 'abstract')
