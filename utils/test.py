import os
import sys
from runners.re_mirtex import MiRTexRunner
from runners.ner_fake import FakeRunner
from runners.ner_mirna import MiRNARunner
from runners.ner_banner import BannerRunner
from runners.splitter_newline import NewlineRunner
from runners.parser_charniak import CharniakRunner

raw = os.path.abspath('data/raw')
ner = os.path.abspath('data/ner')
split = os.path.abspath('data/split')
parse = os.path.abspath('data/parse')
re = os.path.abspath('data/re')

# test ner
r = BannerRunner()
r.run([(raw,'.txt')],
      [(ner,'.txt'),(ner,'.ann')],
      ['test'])

r = MiRNARunner()
r.set_overwrite(True)
r.run([(ner,'.txt'),(ner,'.ann')],
      [(ner,'.txt'),(ner,'.ann')],
      ['test'])

# test fake
r = FakeRunner()
r.run([(nerin,'.txt'),(ner,'.ann')],
      [(ner,'.txt'),(ner,'.entmap')],
      ['test'])

# test split
r = NewlineRunner()
r.run([(ner,'.txt')],
      [(split,'.split')],
      ['test'])

# test parse
r = CharniakRunner()
r.run([(split,'.split')],
      [(parse,'.ptb')],
      ['test'])

# test miRTex
r = MiRTexRunner()
r.run([(nerin,'.txt'),
       (ner,'.ann'),
       (ner,'.txt'),
       (ner,'.entmap'),
       (split,'.split'),
       (parse,'.ptb')],
      [(re,'.txt'),(re,'.ann')],
      ['test']
      )
