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