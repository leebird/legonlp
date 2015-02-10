import sys
import os
from lxml import etree
import json
from pprint import pprint
from annotation.annotate import Annotation
from annotation.writers import AnnWriter
from annotation.utils import FileProcessor


def parse(filepath):
    # get an iterable
    context = etree.iterparse(filepath, events=("end",), tag="document")
    annotations = {}
    for event, element in context:
        # print(element.tag)
        annotation = Annotation()
        pmid = ''
        for child in element:
            if child.tag == 'id':
                pmid = child.text
            elif child.tag == 'passage':
                passage_type = ''
                for grand_child in child:
                    if grand_child.tag == 'infon':
                        passage_type = grand_child.text
                    elif grand_child.tag == 'text':
                        text = grand_child.text
                        annotation.text += text
                    elif grand_child.tag == 'annotation':
                        aid = grand_child.attrib['id']
                        category, start, end, entity_text = '', 0, 0, ''
                        for gg_child in grand_child:
                            if gg_child.tag == 'infon' and gg_child.attrib['key'] == 'type':
                                category = gg_child.text.title()
                                category = category.replace(' ','_')
                            elif gg_child.tag == 'location':
                                start = int(gg_child.attrib['offset'])
                                if passage_type == 'abstract':
                                    # wired position counting in BioQRator
                                    # in title it's correct, in abstract it's 1 extra
                                    start -= 1
                                end = start + int(gg_child.attrib['length'])
                            elif gg_child.tag == 'text':
                                entity_text = gg_child.text
                        entity = annotation.add_entity(category, start, end, entity_text)
                        entity.property.add('aid', aid)
            elif child.tag == 'relation':
                category, trigger, arguments, negated = '', None, [], False
                rid = child.attrib['id']
                for gg_child in child:
                    if gg_child.tag == 'infon' and gg_child.attrib['key'] == 'type':
                        category = gg_child.text.title()
                        if category.startswith('Negated_'):
                            negated = True
                            category = category[8:]
                    elif gg_child.tag == 'node':
                        node_aid = gg_child.attrib['refid']
                        entity = annotation.get_entity_with_property('aid', node_aid)[0]
                        node_role = gg_child.attrib['role'].title()

                        if node_role == '':
                            node_role = entity.category

                        if node_role == 'Trigger':
                            trigger = entity
                            trigger.category = category
                            continue

                        if node_role.lower() in ['acetylation', 'phosphorylation', 'ubiquitination', 'glycosylation',
                                         'methylation', 'sumoylation','neddylation']:
                            trigger = entity
                            entity.category = node_role.title()
                            continue

                        elif node_role == 'Ptm Enzyme':
                            node_role = 'Enzyme'

                        elif node_role != 'Substrate' and node_role != 'Site':
                            node_role = entity.category.title()
                        
                        node_role = node_role.replace(' ','_')
                        arguments.append(annotation.make_argument(node_role, entity))
                print(trigger)
                # sometimes event category is ubiquitination and trigger category
                # is sumoylation/neddylation, we should use the trigger category
                category = trigger.category
                event = annotation.add_event(category, trigger, arguments)
                if trigger is None:
                    print('No trigger: ' + pmid + ' ' + rid)
                if negated == True:
                    event.property.add('negated', 'negated')
            else:
                pass
        annotations[pmid] = annotation
        # clear element
        # http://stackoverflow.com/questions/12160418/why-is-lxml-etree-iterparse-eating-up-all-my-memory
        element.clear()
    return annotations


if __name__ == '__main__':
    if len(sys.argv) < 3:
        print('Specify RLIMS BioC file and output folder')
        sys.exit(0)

    rlims_xml = sys.argv[1]
    output_folder = sys.argv[2]
    annotations = parse(rlims_xml)
    writer = AnnWriter()
    for pmid, annotation in annotations.items():
        writer.write(os.path.join(output_folder, pmid + '.ann'), annotation)
        FileProcessor.write_file(os.path.join(output_folder, pmid + '.txt'), annotation.text)
