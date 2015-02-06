import json
from runners.runner import Runner

from components.ner.fake.replacer import Faker
from annotate.writers import AnnWriter
from annotate.readers import AnnReader

class FakeRunner(Runner):
    
    runnerName = 'Fake'

    def __init__(self):
        pass

    def run(self, inputs, outputs, docList):
        '''
        inputs: input text, input annotation
        outputs: repalced text, entity map
        '''

        faker = Faker()
        tuples = self.get_io_files(inputs+outputs, docList)

        reader = AnnReader()

        for files in tuples:
            inTextFile, inAnnFile, outTextFile, outMapFile = files
            text = self.read_file(inTextFile)
            anno = reader.parse_file(inAnnFile)
            
            faked = faker.fake(text, anno)

            self.write_file(faked['fake_text'], outTextFile)            
            self.write_file(json.dumps(faked['entity_map']),outMapFile)
