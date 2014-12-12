import json
import subprocess
import shlex

# test data for tregex.java
data = {
    'parse': '(S1 (S (S (NP (PRP I)) (VP (VBP have) (NP (DT a) (NN book)))) (. .)))',
    'tregex': 'NP=n',
    'labels': ['n']
}

# dump data into json string
dump = shlex.quote(json.dumps(data))

# call tregex.java
subprocess.call('java -cp .:../lib/java/stanford-corenlp-3.5.0.jar:../lib/java/stanford-tregex-3.5.0.jar:../lib/java/AppleJavaExtensions.jar:../lib/java/gson-2.3.1.jar Tregex '+dump,shell=True)