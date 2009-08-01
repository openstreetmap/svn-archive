#!/usr/bin/python
from bulk_upload import bulk_upload
from parse import Feature
import os

class IdMap(bulk_upload.IdMap):
    def __init__(self, filenamebase):
        self.pafilename = filenamebase + '.noparentarea.pkl'
        bulk_upload.IdMap.__init__(self, filenamebase + '.osm.db')
        print "Init naptan idmap"
        print self.idMap

    def save(self):
        bulk_upload.IdMap.save(self)
        
        try:
        if os.stat(self.pafilename):
            f=open(self.pafilename, "r")
            noparentarea = pickle.load(f)
            f.close()

            for arr in noparentarea.values():
                print arr
                for ft in arr:
                    ft.id = self.idMap[ft.type][ft.id]

            f=open(self.pafilename+".tmp","w")
            pickle.dump(self.noparentarea,f)
            f.close()
            os.rename(self.pafilename+".tmp", self.pafilename)
        except IOError:
            pass

if __name__ == "__main__":
    import optparse
    import getpass

    usage = "usage: %prog [options] file.osm"

    parser = optparse.OptionParser(usage)
    parser.add_option("-c", "--county", dest="county", help="County name for comment message")
    parser.add_option("-C", "--comment", dest="comment", help="Custom comment message")
    #parser.add_option("-p", "--password", dest="infile")
    (options, args) = parser.parse_args()

    if len(args) < 1:
        parser.error("Too few arguments passed")
    infile = args[0]

    comment = options.comment or ("Uploading NaPTAN data for %s" % options.county if options.county else "")

    infilebase = infile.rpartition('.')[0]
    idMap = IdMap(infilebase)
    tags = {
        'created_by': 'naptan2osm',
        'uploaded_by': 'bulk_upload.py',
        'comment': comment,
        'source': 'naptan_import',
        'original_filename': infilebase+'.xml'
    }
    importProcessor = bulk_upload.ImportProcessor("NaPTAN",getpass.getpass('NaPTAN user password: '),idMap,tags)
    importProcessor.parse(infile)
