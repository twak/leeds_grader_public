#!/usr/bin/env python
""" Usage: call with <filename> <typename>
"""

import sys
import clang.cindex
from clang.cindex import TokenKind
from clang.cindex import CursorKind
from os.path import dirname
import os

def find_typerefs(node, file, functions, variables, const_variables, classes, indent = 0):
    """ Find all references to the type named 'typename'
    """
    # if node.spelling == typename:
        # print ( dirname(node.location.file.name) )
    # print('Found %s %s [line=%s, col=%s] ' % (node.kind, node.spelling, node.location.line, node.location.column))


    if node.location.file is not None and dirname(node.location.file.name) == file and node.spelling != "namespace":
        if node.location.file.name[-2:] == ".h":
            if node.kind == CursorKind.CLASS_DECL:
                classes[node.spelling] = 1

        # if node.location.file.name[-2:] == ".h":
        if node.kind == CursorKind.CXX_METHOD or \
              node.kind == CursorKind.CONSTRUCTOR or \
              node.kind == CursorKind.DESTRUCTOR or\
              node.kind == CursorKind.FUNCTION_DECL:
            functions[node.spelling] =1
            # print ( "function length: ", node.displayname, " ", str(node.extent.end.line - node.extent.start.line - 1) )

        if node.kind == CursorKind.VAR_DECL:
            # print ("found declaration " + node.spelling)
            if node.type.is_const_qualified():
                const_variables[node.spelling]=1
            else:
                variables[node.spelling]=1

    # Recurse for children of this node
    for c in node.get_children():
        # print (c.spelling)
        find_typerefs(c,  file, functions, variables, const_variables, classes, indent + 1)

# class names, fn names, variable names, and const?
#clang.cindex.Config.set_library_path("/usr/local/lib")



functions = {}
variables = {}
const_variables = {}
classes = {}
filenames = {}

dir = os.getcwd() #// "/home/twak/code/cw2_grader/sc18j3b/"

for filename in os.listdir(dir):
    if not filename.startswith("screenshot.") and not filename.startswith("moc_") and not filename.startswith("folder_compressor") and not filename.startswith("main.")  and (filename.endswith(".cpp") or filename.endswith(".h")):
        filenames[os.path.splitext(filename)[0]] = 1
        index = clang.cindex.Index.create()
        # target = "/home/twak/code/cw2_grader/sc18j3b/responsive_label.h"
        tu = index.parse( os.path.join (dir, filename), args='-x c++ --std=c++11'.split())
        find_typerefs(tu.cursor, dir, functions, variables, const_variables, classes)
        continue
    else:
        continue

const_variables = [x for x in const_variables if not x.startswith("qt_meta_") and not x.startswith("staticMetaObject")]


print("<h4>functions</h4><p>")
print (', '.join(functions), )
print("</p>")
print("<h4>classes</h4><p>")
print (', '.join(classes), )
print("</p>")
print("<h4>(most) vars</h4><p>")
print (', '.join(variables), )
print("</p>")
print("<h4/>consts</h4><p>")
print (', '.join(const_variables), )
print("</p>")
print("<h4>filenames:</h4><p>")
print (', '.join(filenames), )
print("</p>")
