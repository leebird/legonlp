import json
import os
import sys
import re
import shlex
from subprocess import check_output
from utils.runner import Runner
from annotation.annotation import *
from annotation.writers import AnnWriter
from annotation.readers import SGMLReader
from annotation.utils import *


class Banner:
    """
    mapping from SGML tag name to entity type name
    """
    mapping = {'GENE': 'Gene'}

    def __init__(self, java_path):
        self.java_path = java_path

    def make_cmd(self, arguments):
        base = os.path.abspath(os.path.dirname(__file__)) + '/banner_program;'
        return 'cd ' + base + 'java -cp "' + ':'.join(self.java_path) + '" GeneMention ' + shlex.quote(arguments)

    def recognize(self, arguments):
        """return a hash whose key is the doc id, and the value is
        an annotation object.
        """

        cmd = self.make_cmd(arguments)
        # print(cmd)
        check_output(cmd, shell=True)

    def remove_extra_space(self, text):
        """
        remove annoying interted spaces by banner
        should be done by alignment in the future
        """

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


class BannerRunner(Runner):
    def run(self, task_info):
        """
        Note that Banner will change the original text,
        so for now it should be run as the very first step.
        """
        input_text = task_info['input']['text'][0]
        input_ner = task_info['input']['ner'][0]
        output_text = task_info['output']['text'][0]

        # get the output folder for .ann, Banner only uses the .sgml folder
        output_ner = task_info['output']['ner'].pop(-1)
        # the first should be .sgml folder
        output_ner_sgml = task_info['output']['ner'][0]

        doc_list = task_info['doc_list']
        java_path = task_info['java_path']

        arguments = {'input': task_info['input'],
                     'output': task_info['output'],
                     'doc_list': task_info['doc_list']}

        recognizer = Banner(java_path)
        recognizer.recognize(json.dumps(arguments))

        """
        Since input and output files are not zipped, we go through
        doc list to output annotation and text
        """
        writer = AnnWriter()
        reader = SGMLReader(recognizer.mapping)

        output_text_dir, output_text_suffix = output_text[:2]
        output_ner_dir, output_ner_suffix = output_ner[:2]
        output_sgml_dir, output_sgml_suffix = output_ner_sgml[:2]

        for docid in doc_list:
            input_sgml_file = os.path.join(output_sgml_dir, docid + output_sgml_suffix)
            if not os.path.isfile(input_sgml_file):
                continue

            sgml_text = FileProcessor.read_file(input_sgml_file)
            sgml_text = recognizer.remove_extra_space(sgml_text)

            annotation = reader.parse(sgml_text)
            output_text_file = os.path.join(output_text_dir, docid + output_text_suffix)
            FileProcessor.write_file(output_text_file, annotation.text)
            output_ner_file = os.path.join(output_ner_dir, docid + output_ner_suffix)
            writer.write(output_ner_file, annotation)
            # writer.write_folder(output_ner_dir, output_ner_suffix, annotations)


if __name__ == '__main__':
    runner = BannerRunner()
    args = json.loads(sys.argv[1])
    runner.run(args)