import os

'''
A component can have multiple source of inputs, but 
it can only have one output folder. It's user's responsibility 
to use different suffices (types) to differentiate 
the output files
'''

user = {
    'step': 75,
    'process': 2,
    'input': ['/home/leebird/test/input/'],
    'output': '/home/leebird/test/output/'
    }

tempfolder = os.path.join(os.path.dirname(__file__),'tmp')

'''
('Banner',
'Banner',
[('user.input.0','.txt')], 
[(None,'.txt'),(None,'.ann')]),

('Newline1',
'Newline',
[('Banner','.txt')], 
[(None,'.split')]),

('miRNAMention',
'miRNAMention',
[('Banner','.txt'),('Banner','.ann')],
[(None,'.txt'),(None,'.ann')]),
'''


pipeline = [
    ('Fake',
     'Fake',
     [('user.input.0','.txt'),('user.input.0','.ann')],
     [(None,'.txt'),(None,'.entmap')]),

    ('Newline2',
     'Newline',
     [('Fake','.txt')],
     [(None,'.split')]),

    ('Charniak',
     'Charniak',
     [('Newline2','.split')],
     [(None,'.ptb')]),

    ('miRTex',
     'miRTex',
     [('miRNAMention','.txt'),('Newline1','.split'),('miRNAMention','.ann'),
      ('Fake','.txt'),('Fake','.entmap'),
      ('Newline2','.split'),('Charniak','.ptb')],
     [('user.output','.txt'),('user.output','.ann')])
    ]
