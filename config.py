import os

root_path = '/home/leebird/Projects/legonlp'
data_path = os.path.join(root_path, 'data')
test_path = os.path.join(root_path, 'test')
temp_path = os.path.join(root_path, 'tmp')

components_path = os.path.join(root_path, 'components')

libpython_path = os.path.join(root_path, 'lib/python')
libjava_path = os.path.join(root_path, 'lib/java/*')

PYTHONPATH = [
    root_path,
    libpython_path
]

JAVAPATH = [libjava_path]

test_paths = {
    'input': {
        ('text', '.txt', 'initial'): os.path.join(test_path, 'input/raw'),
        ('split', '.split', 'initial'): os.path.join(test_path, 'input/split'),
        ('parse', '.offset', 'initial'): os.path.join(test_path, 'input/offset'),
        ('tregex', '.tregex', 'initial'): os.path.join(test_path, 'output/tregex'),
        ('ner', '.ann', 'initial'): os.path.join(test_path, 'input/ner'),
        ('ner', '.sgml', 'initial'): os.path.join(test_path, 'input/ner'),
    },
    'output': {
        ('text', '.txt', 'final'): os.path.join(test_path, 'output/raw'),
        ('parse', '.offset', 'initial'): os.path.join(test_path, 'output/offset'),
        ('split', '.split', 'final'): os.path.join(test_path, 'output/split'),
        ('parse', '.parse', 'final'): os.path.join(test_path, 'output/parse'),
        ('tregex', '.tregex', 'final'): os.path.join(test_path, 'output/tregex'),
        ('ner', '.ann', 'final'): os.path.join(test_path, 'output/ner'),
        ('ner', '.sgml', 'final'): os.path.join(test_path, 'output/ner'),
    }

}

components = {
    'NewlineSplitter': {
        'interface': 'split/newline/splitter.py',
        'input_category': [('text', '.txt')],
        'output_category': [('split', '.split')],
        'interpreter': 'python3',
        'python_path': PYTHONPATH,
    },

    'NLTKSplitter': {
        'interface': 'split/nltksplit/splitter.py',
        'input_category': [('text', '.txt')],
        'output_category': [('split', '.split')],
        'interpreter': 'python3',
        'python_path': PYTHONPATH,
        'arguments': {
            'nltk_data': os.path.join(data_path, 'nltk_data')
        }
    },

    'MiRNARecognizer': {
        'interface': 'ner/miRNA/recognizer.py',
        'input_category': [('text', '.txt'), ('ner', '.ann')],
        'output_category': [('text', '.txt'), ('ner', '.ann')],
        'interpreter': 'python3',
        'python_path': PYTHONPATH,
    },

    'Banner': {
        'interface': 'ner/banner/recognizer.py',
        'input_category': [('text', '.txt'), ('ner', '.ann')],
        'output_category': [('text', '.txt'), ('ner', '.sgml'), ('ner', '.ann')],
        'interpreter': 'python3',
        'python_path': PYTHONPATH,
        "arguments": {
            'java_path': JAVAPATH + [os.path.join(components_path, 'ner/banner/banner_program'),
                                     os.path.join(components_path, 'ner/banner/banner_program/src')],
        }
    },

    'CharniakParser': {
        'interface': 'parse/charniak/parser.py',
        'input_category': [('split', '.split')],
        'output_category': [('parse', '.parse')],
        'interpreter': 'python',
        'python_path': PYTHONPATH,
        'arguments': {
            'biomodel': os.path.join(data_path, 'biomodel'),
        },
    },

    'TreeIndexer': {
        'interface': 'utils/tree/offset_tree.py',
        'input_category': [('parse', '.parse')],
        'output_category': [('parse', '.offset')],
        'interpreter': 'python3',
        'python_path': PYTHONPATH,
    },

    'TregexMatcher': {
        'interface': 'tregex.Tregex',
        'input_category': [('parse', '.offset')],
        'output_category': [('tregex', '.tregex')],
        # java 1.8 required
        'interpreter': 'java',
        'java_path': JAVAPATH + [os.path.join(components_path, 'utils')],
        'arguments': {
            'pattern_files': [os.path.join(components_path, 'utils/tregex/patterns/agent_patterns.txt'),
                              os.path.join(components_path, 'utils/tregex/patterns/theme_patterns.txt')]
        }
    },
}