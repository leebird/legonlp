import os
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
    input = config.test_paths['input']

    output = config.test_paths['output']

    tempfolder = os.path.join(config.root_path, 'tmp')

    pipeline = [
        ('Banner',),
        # ('MiRNARecognizer',),
        # ('NLTKSplitter',),
        # ('CharniakParser', {'step': 2, 'process': 2}),
        # ('TreeIndexer',),
        # ('TregexMatcher',)
    ]
