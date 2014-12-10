import os

'''
A component can have multiple source of inputs, but 
it can only have one output folder. It's user's responsibility 
to use different suffices (types) to differentiate 
the output files
'''

class Pipeline:

    step = 100

    process = 1

    inputs = None

    output = None

    tempFolder = os.path.join(os.path.dirname(os.path.dirname(__file__)),'tmp')

    pipeline = [
        ('Newline1',
         'Newline',
         [('user.input.0','.txt')], 
         [(None,'.split')]),
        
        ('miRNAMention',
         'miRNAMention',
         [('user.input.0','.txt'),('user.input.0','.ann')],
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
