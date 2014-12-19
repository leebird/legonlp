import sys
import os
import codecs


class Runner(object):
    runnerName = None

    def __init__(self):
        '''
        read input files and process
        run directly on input files
        run directly on input dir
        process output
        '''
        pass

    def run(self, args):
        '''
        inputs: a list of (dir, suffix) pairs
        outputs: a list of (dir, suffix) pairs
        Note that dir should be an absolute path
        '''
        raise NotImplementedError

    def read_file(self, filepath):
        if not os.path.isfile(filepath):
            print >> sys.stderr, 'file not found: ' + filepath
            return None

        f = codecs.open(filepath, 'r', 'utf-8')
        text = f.read().strip()
        f.close()

        return text

    def write_file(self, content, filepath):
        f = codecs.open(filepath, 'w', 'utf-8')
        f.write(content)
        f.close()

    def get_files(self, dirname, sux, docList):
        '''
        get a list of path for the docList
        '''
        return [os.path.join(dirname, doc + sux) for doc in docList]

    def get_io_files(self, dirsux, docList):
        """
        get a zipped list of paths for all the dirs and the docList
        :param dirsux: a list of (dir, suffix) pairs
        :type dirsux: list
        :param docList: a list of doc name
        :type docList: list
        :return: a zipped list of dir+file+suffix tuples
        :rtype: list
        """
        res = []

        for ds in dirsux:
            dirname, sux = ds[:2]
            res.append(self.get_files(dirname, sux, docList))

        return zip(*res)

    
