#!/usr/bin/python

import subprocess as sb
import string
import os


def find_java_processes():
    out = sb.check_output(["/usr/bin/jps", "-l"])
    procmap = {}
    s = string.split(out, '\n')
    for line in s:
        if line.endswith('sun.tools.jps.Jps'):
            continue
        if len(line) == 0:
            break
        ss = string.split(line)
        '''process_id -> process_name'''
        procmap[ss[0]] = ss[1]

    return procmap


if __name__ == '__main__':
    os.environ["PATH"] = "/install/jdk/bin:" + os.environ["PATH"]
    procmap = find_java_processes()
    if len(procmap) > 0:
        print('hello')
