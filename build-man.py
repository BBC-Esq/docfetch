#!/usr/bin/python3

import os, shutil, platform, sys
from os.path import exists, join, isfile, isdir

if len(sys.argv) <= 1:
	print('Must specify a language ID, for example: en, de, fr, ...')
	exit(0)

is_windows = 'windows' in platform.system().lower()
classpath_sep = ';' if is_windows else ':'

print('Cleaning build directory...')
class_dir = 'build/man/classes'
if not exists(class_dir):
	os.makedirs(class_dir)
for filename in os.listdir(class_dir):
	path = join(class_dir, filename)
	if isfile(path):
		os.remove(path)
	elif isdir(path):
		shutil.rmtree(path)

def execute(cmd_parts):
	os.system(' '.join(cmd_parts))

# Recursively collect library jars
jars = []
for root, dirs, files in os.walk('lib'):
	for filename in files:
		if not filename.endswith('.jar'): continue
		jars.append(join(root, filename))

print('Compiling sources...')
execute([
	'javac',
	'-sourcepath src',
	'-classpath \"%s\"' % classpath_sep.join(jars),
	'-nowarn',
	'-d %s' % class_dir,
	'src/net/sourceforge/docfetcher/man/Manual.java'
])

jar_path = 'build/man/docfetcher-man-builder.jar'
main_class = 'net.sourceforge.docfetcher.man.Manual'

print('Creating builder jar...')
execute([
	'jar cfe',
	jar_path,
	main_class,
	'-C %s net' % class_dir
])

print('Launching builder...')
print('-' * 40)
jars.append(jar_path)
execute([
	'java',
	'-classpath \"%s\"' % classpath_sep.join(jars),
	main_class,
	sys.argv[1]
])
