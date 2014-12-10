import os
import sys
import imp

base = os.path.dirname(__file__)
sys.path.append(base)

if len(sys.argv) < 2:
    pplPath = [os.path.join(base,'pipelines')]
    pplName = 'pipeline'
else:
    pplPath = [os.path.abspath(os.path.dirname(sys.argv[1]))]
    pplName = os.path.basename(sys.argv[1])

pplFile = imp.find_module(pplName,pplPath)
ppl = imp.load_module('emrit',*pplFile)

from utils.dispatcher import Dispatcher

d = Dispatcher(ppl.Pipeline,os.path.abspath('tmp'))
d.init_runners('runners')
d.dispatch()
