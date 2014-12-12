import json
import sys

file = sys.argv[1]
category = sys.argv[2]
labels = ['p', 'tr', 'arg']

f = open(file, 'r')
content = f.read()
blocks = content.split('\n\n')
patterns = []

# generate id for each pattern
def get_id():
    i = 1
    while True:
        yield str(i)
        i += 1

ider = get_id()

# parse original pattern file
for block in blocks:
    lines = block.strip().split('\n')
    lines = [line for line in lines if not line.startswith('#')]

    pattern = {
        'category': category,
        'comment': '',
        'labels': labels,
        'id': next(ider)
    }

    for line in lines:
        if line.startswith('name:'):
            pattern['name'] = line[5:].strip()
        elif line.startswith('tregex:'):
            pattern['tregex'] = line[7:].strip()
        else:
            continue
    patterns.append(pattern)

# pretty print json string
print(json.dumps(patterns, indent=4))