# -*- coding: utf-8 -*-

import re
import json
import sys
from annotation.annotation import *
from annotation.readers import AnnReader
from annotation.writers import AnnWriter
from utils.runner import Runner

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


class MiRNARecognizerRunner(Runner):
    def __init__(self):
        self.overwrite = True

    def run(self, task_info):
        input_text = task_info['input']['text'][0]
        input_ner = task_info['input']['ner'][0]
        output_text = task_info['output']['text'][0]
        output_ner = task_info['output']['ner'][0]
        doc_list = task_info['doc_list']

        recognizer = MiRNARecognizer()
        tuples = self.get_io_files([input_text, input_ner, output_text, output_ner], doc_list)

        writer = AnnWriter()
        reader = AnnReader()
        for in_text_file, in_ann_file, out_text_file, out_ann_file in tuples:
            print(in_ann_file)
            text = self.read_file(in_text_file)
            annotation = recognizer.recognize(text)

            prev_annotation = reader.parse_file(in_ann_file)
            annotation.add_entities(prev_annotation.entities)

            if self.overwrite:
                annotation.remove_overlap('MiRNA', 'Gene')
                annotation.remove_overlap('MiRNA', 'Complex')
                annotation.remove_included()

            writer.write(out_ann_file, annotation)
            self.write_file(annotation.text, out_text_file)

if __name__ == '__main__':
    runner = MiRNARecognizerRunner()
    args = json.loads(sys.argv[1])
    runner.run(args)