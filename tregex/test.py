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
subprocess.call('java -cp .:../lib/java/stanford-corenlp-3.5.0.jar:../lib/java/stanford-tregex-3.5.0.jar:../lib/java/AppleJavaExtensions.jar:../lib/java/gson-2.3.1.jar:../lib/java/commons-lang3-3.3.2.jar Tregex '+dump,shell=True)