import os

root_path = '/home/leebird/Projects/legonlp'
data_path = os.path.join(root_path, 'data')
test_path = os.path.join(root_path, 'test')

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
        'text': [(os.path.join(test_path, 'input/raw'), '.txt')],
        'split': [(os.path.join(test_path, 'input/split'), '.split')],
        'parse': [(os.path.join(test_path, 'input/offset'), '.offset')],
        'tregex': [(os.path.join(test_path, 'output/tregex'), '.tregex')],
        'ner': [(os.path.join(test_path, 'input/ner'), '.ann'),
                (os.path.join(test_path, 'input/ner'), '.sgml')]
    },
    'output': {
        'text': [(os.path.join(test_path, 'output/raw'), '.txt')],
        'split': [(os.path.join(test_path, 'output/split'), '.split')],
        'parse': [(os.path.join(test_path, 'output/parse'), '.parse')],
        'tregex': [(os.path.join(test_path, 'output/tregex'), '.tregex')],
        'ner': [(os.path.join(test_path, 'output/ner'), '.ann'),
                (os.path.join(test_path, 'output/ner'), '.sgml')]
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