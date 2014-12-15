import os
import glob
import config

'''
A component can have multiple source of inputs, but
it can only have one output folder. It's user's responsibility
to use different suffices (types) to differentiate
the output files
'''


class Pipeline:
    step = 3

    process = 2

    # category : input <dir, suffix> list
    input = {
        #'split': [(os.path.join(config.root_path, 'test/split'), '.split')],
        #'text': [(os.path.join(config.root_path, 'test/raw'), '.txt')],
        'parse': [(os.path.join(config.root_path, 'test/offset'), '.offset')]
    }

    output = {
        #'parse': [(os.path.join(config.root_path, 'test/parse'), '.parse')]
        'tregex': [(os.path.join(config.root_path, 'test/tregex'), '.tregex')]
    }

    tempfolder = os.path.join(config.root_path, 'tmp')

    pipeline = [
        #('NLTKSplitter',),
        #('CharniakParser', {'step': 2, 'process': 2}),
        #('TreeIndexer',),
        ('TregexMatcher',)
    ]
