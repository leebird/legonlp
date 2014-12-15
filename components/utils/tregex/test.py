import json
import subprocess
import shlex

# test data for tregex.java
data = {
    'inputDir': 'test',
    'outputDir': 'test',
    'inputSux': '.txt',
    'outputSux': '.tregex',
    'docList': ['test_parses'],
    'patternFiles': ['patterns/agent_patterns.txt','patterns/theme_patterns.txt'],
}

# dump data into json string
dump = shlex.quote(json.dumps(data))

# call tregex.java
subprocess.call('java -cp "..:.:/home/leebird/Projects/legonlp/lib/java/*" tregex.Tregex '+dump,shell=True)