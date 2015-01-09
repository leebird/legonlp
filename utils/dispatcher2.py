import os
import sys
import glob
import tempfile
import shlex
import json
import subprocess
from multiprocessing import Pool
import pprint
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

    # print(command)
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
        self.default_input = pipeline.input
        self.default_output = pipeline.output
        # runtime temporary folders
        self.runtime_folders = {}
        self.runtime_input = self.default_input.copy()
        self.runtime_folders = {}


    def doc_list(self, input_info):
        """
        get doc list from input information
        :param input_category: (category, suffix) tuple
        :type input_category: tuple
        :return: a list of doc names without suffix
        :rtype: list
        """
        dirname = self.runtime_folders[input_info]
        suffix = input_info[1]
        files = glob.iglob(os.path.join(dirname, '*' + suffix))
        return [os.path.basename(f)[:-len(suffix)] for f in files]

    def iter_docs(self, component):
        start = 0

        # get the component-specific step if it is set
        step = self.step if "step" not in component else component["step"]

        arguments = component['arguments']

        arguments.update({
            'input': {},
            'output': {}
        })

        for needle in component['input']:
            dirname = self.runtime_folders[needle]
            component_category, suffix, component_name = needle

            if component_category in arguments['input']:
                arguments['input'][component_category].append((dirname, suffix, component_name))
            else:
                arguments['input'][component_category] = [(dirname, suffix, component_name)]

        for needle in component['output']:
            dirname = self.runtime_folders[needle]
            component_category, suffix, component_name = needle

            if component_category in arguments['output']:
                arguments['output'][component_category].append((dirname, suffix, component_name))
            else:
                arguments['output'][component_category] = [(dirname, suffix, component_name)]

        while True:
            files = self.files[start:start + step]

            if len(files) == 0:
                break

            component["arguments"].update({'doc_list': files})

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

    def make_temp(self, prefix):
        return tempfile.mkdtemp(dir=self.tempfolder, prefix=prefix + '_')

    @staticmethod
    def make_pythonpath(python_path):
        return 'PYTHONPATH=' + ':'.join(python_path) + ' '

    @staticmethod
    def make_javapath(java_path):
        return ' -cp "' + ':'.join(java_path) + '" '

    def make_temp_folders(self):
        """
        create temporary folders for all the components in pipeline
        :return: None
        :rtype: None
        """
        self.runtime_folders.update(self.default_input)
        self.runtime_folders.update(self.default_output)

        for task in self.pipeline[:-1]:
            # aggregate information for the component
            component_name = task[0]
            component_config = task[1]

            try:
                component = config.components[component_name]
            except KeyError:
                raise self.ComponentNotDefined(component_name)

            # get runtime output directory and suffix
            # create temp folder only it's not in runtime_folders
            for needle in component_config['output']:
                if needle not in self.runtime_folders:
                    folder = self.make_temp(component_name)
                    self.runtime_folders[needle] = folder

    def dispatch(self):
        self.make_temp_folders()

        for task in self.pipeline:
            # aggregate information for the component
            component_name = task[0]

            print(component_name, file=sys.stderr)

            if component_name not in config.components:
                raise self.ComponentNotDefined(component_name)
            component = config.components[component_name]
            if "arguments" not in component:
                component["arguments"] = {}
            component_config = task[1] if len(task) > 1 else {}
            component.update(component_config)

            # get input doc files
            self.files = self.doc_list(component["input"][0])

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


if __name__ == '__main__':
    dispatcher = Dispatcher(pipeline.Pipeline)
    dispatcher.dispatch()
