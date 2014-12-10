from recognizer import MiRNARecognizer
import codecs
from pprint import pprint

inputDir = 'test'
inputSux = '.txt'
docList = ['test']

r = MiRNARecognizer()
pprint(r.recognize(inputDir, inputSux, docList))
