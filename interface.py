import subprocess
import json
import shlex
import os
import config

class Interface(object):

    def __init__(self):
        pass

    @classmethod
    def run(cls, component):
        if component in config.components:
            runner_info = config.components[component]
            command = cls.make_cmd(runner_info)
            subprocess.call(command, shell=True)

    @classmethod
    def make_pythonpath(cls, PYTHONPATH):
        return 'PYTHONPATH='+':'.join(PYTHONPATH)+' '

    @classmethod
    def make_cmd(cls, runner_info):
        command = runner_info["interpreter"] +" "+ os.path.join(config.components_path, runner_info["interface"])
        pythonpath = cls.make_pythonpath(config.PYTHONPATH)
        return pythonpath + command + ' ' + shlex.quote(json.dumps(runner_info["arguments"]))

if __name__ == "__main__":
    Interface.run("CharniakParser")