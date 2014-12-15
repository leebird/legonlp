import os
import glob
import tempfile
import shlex
import json
import subprocess
from multiprocessing import Pool

from pipelines import pipeline
import config


def run_command(command):
    """
    the singled out function used in multiprocessing.pool.map()
    :param command:
    :type command:
    :return:
    :rtype:
    """

    #print(command)
    subprocess.call(command, shell=True)


class Dispatcher:
    class ComponentNotDefined(Exception):
        def __init__(self, component):
            self.component = component

        def __str__(self):
            return 'Component {0} not found in config'.format(self.component)

    class InputDirNotFound(Exception):
        def __init__(self, category):
            self.category = category

        def __str__(self):
            return 'Input category {0} not found in runtime'.format(self.category)

    class OutputDirNotFound(Exception):
        def __init__(self, category, suffix):
            self.category = category
            self.suffix = suffix

        def __str__(self):
            return 'Input category {0} {1} not found in runtime'.format(self.category, self.suffix)

    def __init__(self, pipeline):
        """
        initialize dispatcher
        :param pipeline: a pipeline containing tasks information
        :type pipeline: Pipeline
        :return: None
        :rtype: None
        """
        self.pipeline = pipeline.pipeline
        self.tempfolder = pipeline.tempfolder
        self.step = pipeline.step
        self.process = pipeline.process
        self.input = pipeline.input
        self.output = pipeline.output
        self.runtime_input = self.input.copy()
        self.runtime_output = {}


    def doc_list(self, input_category):
        """
        get doc list from input information
        :param input_category: (category, suffix) tuple
        :type input_category: tuple
        :return: a list of doc names without suffix
        :rtype: list
        """
        category, suffix = input_category

        dirname = None
        if category in self.runtime_input:
            for input_info in self.runtime_input[category]:
                if input_info[1] == suffix:
                    dirname = input_info[0]

        if dirname is None:
            raise self.InputDirNotFound(category)

        files = glob.iglob(os.path.join(dirname, '*' + suffix))
        return [os.path.basename(f)[:-len(suffix)] for f in files]

    def iter_docs(self, component):
        start = 0

        # get the component-specific step if it is set
        step = self.step if "step" not in component else component["step"]

        while True:
            files = self.files[start:start + step]
            if len(files) == 0:
                break

            component["arguments"].update({
                'input': self.runtime_input,
                'output': self.runtime_output,
                'doc_list': files
            })

            if "python_path" in component:
                # add python path
                command = component["interpreter"] + ' ' + os.path.join(config.components_path, component["interface"])
                python_path = self.make_pythonpath(component["python_path"])
                command = python_path + command
            elif "java_path" in component:
                # add java path
                java_path = self.make_javapath(component["java_path"])
                command = component["interpreter"] + ' ' + java_path + ' ' + component['interface']
            else:
                command = component["interpreter"] + ' ' + os.path.join(config.components_path, component["interface"])

            yield command + ' ' + shlex.quote(json.dumps(component['arguments']))
            start += step

    def update_runtime_input(self, component_name, component, tempfolder):
        category = component["output_category"]
        for c, suffix in category:
            if c in self.runtime_input:
                self.runtime_input[c].append((tempfolder, suffix, component_name))
            else:
                self.runtime_input[c] = [(tempfolder, suffix, component_name)]

    def make_temp(self, prefix):
        return tempfile.mkdtemp(dir=self.tempfolder, prefix=prefix + '_')

    @staticmethod
    def make_pythonpath(python_path):
        return 'PYTHONPATH=' + ':'.join(python_path) + ' '

    @staticmethod
    def make_javapath(java_path):
        return ' -cp "' + ':'.join(java_path) + '" '

    @staticmethod
    def lookahead(iterable):
        """
        look ahead to find out the last element
        :param iterable: iterable data
        :type iterable: list | iter
        :return: (element, boolean), boolean = True if the element is the last one
        :rtype: tuple
        """
        it = iter(iterable)
        last = next(it)  # next(it) in Python 3
        for val in it:
            yield last, False
            last = val
        yield last, True

    def dispatch(self):

        for task, last in self.lookahead(self.pipeline):
            # aggregate information for the component
            component_name = task[0]
            if component_name not in config.components:
                raise self.ComponentNotDefined(component_name)
            component = config.components[component_name]
            if "arguments" not in component:
                component["arguments"] = {}
            component_config = task[1] if len(task) > 1 else {}
            component.update(component_config)

            # get input doc files
            self.files = self.doc_list(component["input_category"][0])

            # get run time output directory and suffix
            # TODO: enable multiple output folders
            self.runtime_output = {}
            for output_category, output_suffix in component['output_category']:
                folder = None
                if last:
                    folders = self.output[output_category]
                    for folder_info in folders:
                        if output_suffix == folder_info[1]:
                            folder = folder_info[0]
                            break
                else:
                    folder = self.make_temp(component_name + '_' + output_category)

                # raise exception is no output folder is matched
                if folder is None:
                    raise self.OutputDirNotFound(output_category, output_suffix)

                if output_category in self.runtime_output:
                    self.runtime_output[output_category].append((folder, output_suffix))
                else:
                    self.runtime_output[output_category] = [(folder, output_suffix)]

            # get command iterator
            command = self.iter_docs(component)

            # get process num and step
            process = self.process if "process" not in component else component["process"]

            # pool set up
            pool = Pool(processes=process, maxtasksperchild=1)

            # process docs in parallel
            pool.map(run_command, command)
            pool.close()
            pool.join()

            self.update_runtime_input(component_name, component, folder)


if __name__ == '__main__':
    dispatcher = Dispatcher(pipeline.Pipeline)
    dispatcher.dispatch()
