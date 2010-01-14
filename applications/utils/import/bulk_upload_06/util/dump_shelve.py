import shelve
from sys import argv

if len(argv) < 2:
    print "usage: py dump_shelve.py file.db"
    exit(1)

db = shelve.open(argv[1])
if not db:
    print "Couldn't open shelve db from %s" % argv[1]

for k, v in db.iteritems():
    print "%s: %s" % (k, v)
