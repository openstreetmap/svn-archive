#!/usr/bin/python
"""Replace strings in argument file as defined by stdin file.
Replace: With"""
import sys

file = open(sys.argv[1], 'r')
text = file.read()
file.close()

for line in sys.stdin:
    line = line.partition(': ')
    text = text.replace(line[0], line[2].strip())

file = open(sys.argv[1], 'w')
file.write(text)
file.close()

