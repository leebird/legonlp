import os
import imp
import glob
import pkgutil
import inspect
import tempfile
from multiprocessing import Pool
import itertools
import shutil

def make_temp(prefix, tempfolder):
    return tempfile.mkdtemp(dir=tempfolder, prefix=prefix+'_')

def get_inputs(inputs, tempfolders, userInputs):
    inputsInfo = []

    for inputName, inputSux in inputs:
        if tempfolders.has_key(inputName):
            inputsInfo.append((tempfolders[inputName],inputSux))
        else:
            tokens = inputName.split('.')
            idx = int(tokens[-1])
            userInput = os.path.abspath(userInputs[idx])
            inputsInfo.append((userInput,inputSux))

    return inputsInfo

def get_outputs(runnerName, outputs, tempfolders, userOutput, tempfolder):
    if outputs[0][0] is None:
        temp = make_temp(runnerName, tempfolder)
        tempfolders[runnerName] = temp
        outputs = [(temp,o[1]) for o in outputs]
    elif outputs[0][0] == 'user.output':
        userOutput = os.path.abspath(userOutput)
        outputs = [(userOutput,o[1]) for o in outputs]

    return outputs


def init_runners(runnersRepo):
    runnerCls = {}
    modules = pkgutil.iter_modules([runnersRepo])
    for loader, name, ispkg in modules:
        mod = loader.find_module(name).load_module(name)
        clses = inspect.getmembers(mod, inspect.isclass)
        for clsName, cls in clses:
            if hasattr(cls, 'runnerName'):
                if cls.runnerName is not None:
                    runnerCls[cls.runnerName] = cls
    return runnerCls

def run_pipeline(taskinfo):
    '''
    this function can only have one argument, or the pool can't pass the
    argument correctly. Passed (arg1, arg2) would become ((arg1, arg2),) 
    if you let the function have two arguments.
    '''
    pipeline, userInputs, userOutput, tempfolder, docList = taskinfo
    runnerCls = init_runners('runners')
    tempfolders = {}
        
    for stepName, runnerName, inputs, outputs in pipeline:
        outputs = get_outputs(stepName, outputs, tempfolders, userOutput, tempfolder)
        inputs = get_inputs(inputs, tempfolders, userInputs)

        runner = runnerCls[runnerName]
        runner().run(inputs,outputs,docList)

    '''
    delete all temp folders
    '''
    for stepName, temp in tempfolders.iteritems():
        #pass
        shutil.rmtree(temp)


def run_pipeline2(runnerName):
    print(len(runnerName))
    print('hehe')

class Dispatcher:

    def __init__(self, pipeline):
        self.pipeline = pipeline.pipeline
        self.tempfolder = pipeline.tempFolder
        self.step = pipeline.step
        self.process = pipeline.process
        self.userInputs = pipeline.inputs
        self.userOutput = pipeline.output
        self.pool = Pool(processes=self.process, maxtasksperchild=1)
        self.get_docs()

    def init_runners(self, runnersRepo):
        self.runnerCls = {}
        modules = pkgutil.iter_modules([runnersRepo])
        for loader, name, ispkg in modules:
            mod = loader.find_module(name).load_module(name)
            clses = inspect.getmembers(mod, inspect.isclass)
            for clsName, cls in clses:
                if hasattr(cls, 'runnerName'):
                    if cls.runnerName is not None:
                        self.runnerCls[cls.runnerName] = cls

    def get_docs(self):
        inputs = self.get_inputs(self.pipeline[0][2], {})
        dirname, suffix = inputs[0][0],inputs[0][1]
        self.files = glob.iglob(os.path.join(dirname,'*'+suffix))

    def iter_docs(self):
        inputs = self.pipeline[0][2]
        suffix = inputs[0][1]
        suxLen = len(suffix)

        while True:
            files = itertools.islice(self.files, 0, self.step)
            files = list(files)

            if len(files) == 0:
                break

            yield(self.pipeline,
                  self.userInputs,
                  self.userOutput,
                  self.tempfolder,
                  [os.path.basename(f)[:-suxLen] for f in files])
            

    def dispatch(self):
        '''
        get initial inputs
        '''
        docs = self.iter_docs()

        self.pool.imap_unordered(run_pipeline, docs)
        self.pool.close()
        self.pool.join()

    def dispatch_test(self):
        docs = self.iter_docs()
        arg = docs.next()
        run_pipeline(arg)

    def run_pipeline(self, docList):
        tempfolders = {}
        
        for stepName, runnerName, inputs, outputs in self.pipeline:
            outputs = self.get_outputs(stepName, outputs, tempfolders)
            inputs = self.get_inputs(inputs, tempfolders)

            runner = self.runnerCls[runnerName]
            runner().run(inputs,outputs,docList)

    def get_inputs(self, inputs, tempfolders):
        inputsInfo = []
        for inputName, inputSux in inputs:
            if inputName in tempfolders:
                inputsInfo.append((tempfolders[inputName],inputSux))
            else:
                tokens = inputName.split('.')
                idx = int(tokens[-1])
                userInput = self.userInputs[idx]
                inputsInfo.append((userInput,inputSux))

        return inputsInfo

    def get_outputs(self, runnerName, outputs, tempfolders):
        if outputs[0][0] is None:
            temp = self.make_temp(runnerName)
            tempfolders[runnerName] = temp
            outputs = [(temp,o[1]) for o in outputs]
        elif outputs[0][0] == 'user.output':
            userOutput = self.userOutput
            outputs = [(userOutput,o[1]) for o in outputs]

        return outputs

    def make_temp(self, prefix):
        return tempfile.mkdtemp(dir=self.tempfolder, prefix=prefix+'_')
                        
