#!/usr/bin/python
from bulk_upload import bulk_upload
from parse import Feature
import os
import pickle

class IdMap(bulk_upload.IdMap):
    noparentarea = None
    pafilename = None
    def __init__(self, filenamebase):
        self.pafilename = filenamebase + '.noparentarea.pkl'
        bulk_upload.IdMap.__init__(self, filenamebase + '.osm.db')

    def load(self):
        bulk_upload.IdMap.load(self)
        try:
            if os.stat(self.pafilename):
                f=open(self.pafilename, "r")
                self.noparentarea = pickle.load(f)
                f.close()
        except IOError:
            pass

    def save(self):
        bulk_upload.IdMap.save(self)
        for arr in self.noparentarea.values():
            # Scanning the no parent areas each time will be quicker than the idMap
            for ft in arr:
                try:
                    ft.id = self.idMap[ft.type][ft.id]
                except KeyError:
                    pass

        f=open(self.pafilename+".tmp","w")
        pickle.dump(self.noparentarea,f)
        f.close()
        os.rename(self.pafilename+".tmp", self.pafilename)

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
        'uploaded_by': bulk_upload.user_agent,
        'comment': comment,
        'source': 'naptan_import',
        'original_filename': infilebase+'.xml'
    }
    importProcessor = bulk_upload.ImportProcessor("NaPTAN",getpass.getpass('NaPTAN user password: '),idMap,tags)
    importProcessor.parse(infile)
