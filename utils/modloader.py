import os
import sys
import importlib.machinery

class ModLoader:
    """
    load module at gZ
    """
    def load_module(self, mod_name, mod_path):
        pass

base = os.path.dirname(__file__)
sys.path.append(base)

if len(sys.argv) < 2:
    pplPath = [os.path.join(base, 'pipelines')]
    pplName = 'pipeline'
else:
    pplPath = [os.path.abspath(os.path.dirname(sys.argv[1]))]
    pplName = os.path.basename(sys.argv[1])

# https://docs.python.org/3/library/importlib.html#importlib.machinery.PathFinder.find_module
# load module from file
spec = importlib.machinery.PathFinder.find_spec(pplName, pplPath)
ppl = spec.loader.load_module()

from utils.dispatcher import Dispatcher

d = Dispatcher(ppl.Pipeline)
d.init_runners('runners')
# d.dispatch()
