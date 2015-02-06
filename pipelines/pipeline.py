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

    tempfolder = config.temp_path

    # pipeline specifies specific input
    # runners should accept general input
    # config connects specific input and general input
    pipeline = [
        ('Banner', {
            'input': [('text', '.txt', 'initial'),
                      ('ner', '.ann', 'initial')],
            'output': [('text', '.txt', 'Banner'),
                       ('ner', '.sgml', 'Banner'),
                       ('ner', '.ann', 'Banner')]
        }),
        ('MiRNARecognizer', {
            'input': [('text', '.txt', 'Banner'),
                      ('ner', '.ann', 'Banner')],
            'output': [('text', '.txt', 'MiRNARecognizer'),
                       ('ner', '.ann', 'MiRNARecognizer')]
        }),
        ('NLTKSplitter', {
            'input': [('text', '.txt', 'MiRNARecognizer')],
            'output': [('split', '.split', 'NLTKSplitter')]
        }),
        ('CharniakParser', {
            'step': 2,
            'process': 2,
            'input': [('split', '.split', 'NLTKSplitter')],
            'output': [('parse', '.parse', 'CharniakParser')]
        }),
        ('TreeIndexer', {
            'input': [('parse', '.parse', 'CharniakParser'),
                      ('text', '.txt', 'MiRNARecognizer')],
            'output': [('parse', '.parse', 'TreeIndexer')]
        }),
        ('AgentThemeMatcher', {
            'input': [('parse', '.parse', 'TreeIndexer')],
            'output': [('tregex', '.at', 'final')]
        }),
        ('CauseEffectMatcher', {
            'input': [('parse', '.parse', 'TreeIndexer')],
            'output': [('tregex', '.ce', 'final')]
        }),
        ('ReferenceMatcher', {
            'input': [('parse', '.parse', 'TreeIndexer')],
            'output': [('tregex', '.ref', 'final')]
        })
    ]
