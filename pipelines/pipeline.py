import os

'''
A component can have multiple source of inputs, but 
it can only have one output folder. It's user's responsibility 
to use different suffices (types) to differentiate 
the output files
'''

class Pipeline:

    user = {
        'step': 3,
        'process': 2,
        'input': ['data/raw'],
        'output': 'data/re'
        }

    tempfolder = os.path.join(os.path.dirname(__file__),'tmp')

    pipeline = [
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

        ('Fake',
         'Fake',
         [('miRNAMention','.txt'),('miRNAMention','.ann')],
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
