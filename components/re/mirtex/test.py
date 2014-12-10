from extractor import MiRNATargetExtractor

dirs = ['test/raw','test/ner','test/split','test/parse']
suxes = ['.txt','.ann', '.txt', '.entmap', '.split', '.ptb']

extractor = MiRNATargetExtractor()
print extractor.extract(dirs, suxes, ['test'])
