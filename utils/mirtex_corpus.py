# drop all relations except equiv relation

import os
from annotation.readers import AnnReader
from annotation.writers import AnnWriter

dev_path = '/home/leebird/Desktop/miRTex/miRTex/train'
test_path = '/home/leebird/Desktop/miRTex/miRTex/test'

dev_to_path = '/home/leebird/Desktop/miRTex/miRTex_ent/development'
test_to_path = '/home/leebird/Desktop/miRTex/miRTex_ent/test'

reader = AnnReader()
writer = AnnWriter()

annotations = reader.parse_folder(dev_path,'.ann')

for pmid, annotation in annotations.items():
    events = []
    for event in annotation.events:
        if event.category == '*':
            events.append(event)
    annotation.events = events
    writer.write(os.path.join(dev_to_path,pmid+'.ann'),annotation)
