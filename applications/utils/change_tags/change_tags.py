#!/usr/bin/python

"""change_tags.py, an OSM tool.

   This application is designed to convert tags, based on a downloaded .osm
   file. To use, open the .py file and edit the contents of the 'converter'
   function to change the tags in the way you want to, then comment out the
   'converter = False' line immediately after the function body. Once you have
   done that, Typically, usage would be:

       python tag_changer.py --dry-run -f file.osm --verbose

   followed by  manual inspection of the output XML. At the end, it will report
   how many total nodes/elements there are, and how many would be changed by
   the conversion.

   Once you've done that, call:

       python tag_changer.py -f file.osm -u <username> -p <password> 

   This should iterate through and perform the updates against the API,
   reporting skipped nodes or errors at the end.

   If you are working only with your own data, you should usually use 
    
       --only-mine displayName

   Which will skip any updates which do not have you as the last author.
"""   

__author__  = "Christopher Schmidt <crschmidt@crschmidt.net>"
__version__ = "0.2"
__revision__ = "$Id$"

import dbm
import sys, re, xml.sax
from xml.sax.handler import ContentHandler

requires = []

try:
    from xml.etree.cElementTree import Element, SubElement, tostring 
except ImportError:
    requires.append("Requires xml.etree.cElementTree. Try Python2.5?")

try:
    import httplib2
except ImportError, E:
    requires.append("No httplib2! Try easy_install httplib2 (%s)" % E)

def converter(tags, type):
    """Pass in tags dict and object type. Function must return True (object
    changed, upload to server) or False (nothing changed, don't reupload).
    Change the tags dict in place.
    
    By default, this function simply returns 'False' (nothing changed);
    however, it has some examples of what can be done as alternatives.

    This is the function you should edit in order to change the tags you
    wish to change.

    By default, adds a created_by tag identifying the source of the change.
    """

    return False 
    
    changed = False
    
    if type == "node":
        return False 
    
    # Fix a typo
    if 'name' in tags and 'Playgroung' in tags['name']:
        tags['name'] = tags['name'].replace("Playgroung", "Playground")
        changed = True    
    
    # adjust a tag based on a name
    if 'name' in tags and ('park' in tags['name'].lower() or
       'playground' in tags['name'].lower()):
        tags['leisure'] = 'park'
        changed = True
   
    # change a key in a tag
    if 'leisure' in tags and tags['leisure'] == 'recreation_ground':
        del tags['leisure']
        if not 'landuse' in tags:
            tags['landuse'] = 'recreation_ground'
        else:
            tags['landuse'] = "%s; recreation_ground" % tags['landuse']
        changed = True

    tags['created_by'] = 'change_tags.py %s' % __version__

    return changed    

# Comment this line out once you have edited the converter above.
converter = False

#### DO NOT CHANGE CODE AFTER THIS LINE #####


def indent(elem, level=0):
    """Used for pretty printing XML."""
    i = "\n" + level*"  "
    if len(elem):
        if not elem.text or not elem.text.strip():
            elem.text = i + "  "
        for e in elem:
            indent(e, level+1)
            if not e.tail or not e.tail.strip():
                e.tail = i + "  "
        if not e.tail or not e.tail.strip():
            e.tail = i
    else:
        if level and (not elem.tail or not elem.tail.strip()):
            elem.tail = i

class changeTags (ContentHandler):
    
    def __init__ (self, converter, db=None, user=None, password=None, dry_run=None, noisy_errors=None, only_mine=None, verbose=None):
        ContentHandler.__init__(self)
        
        self.converter = converter
        self.user = user
        self.password = password
        self.dry_run = dry_run
        self.noisy_errors = noisy_errors
        self.verbose = verbose
        self.only_mine = only_mine
        self.db = db

        self.changes = {'node': 0, 'way': 0} 
        self.already_changed = {'node': 0, 'way': 0} 
        self.read = {'node': 0, 'way': 0, 'tag': 0} 
        self.errors = []
        self.skipped = []
        
        if not self.dry_run:
            if self.user and self.password:
                self.h = httplib2.Http()
                self.h.add_credentials(self.user, self.password)
            else:
                raise Exception("Username and password required.")

    def upload(self, xml):
        if self.only_mine and self.current['user'] != self.only_mine:
            self.skipped.append({'id': self.current['id'], 'type':self.current['type'], 'reason': "User %s doesn't match." % self.current['user']})
            return
        db_key = "%s:%s" % (self.current['type'], self.current['id']) 
        if self.db and self.db.has_key(db_key):
            self.already_changed[self.current['type']] += 1
            return
        url = "http://api.openstreetmap.org/api/0.5/%s/%s" % (self.current['type'], self.current['id'])
        if self.dry_run:
            if self.verbose:
                print "URL:  %s" % url
                print "XML:\n%s" % xml
            self.changes[self.current['type']] += 1
        else:
            try:
                if self.verbose: 
                    print "Opening URL %s" % url
                resp, content = self.h.request(url, "PUT", body=xml)
                if resp.status != '200':
                    error = {'item': self.current, 'code': resp.status, 'data': content}
                    if self.noisy_errors: print "Error occurred! %s" % error
                    self.errors.append(error)
                else:
                    self.db[db_key] = "1" 
                    self.changes[self.current['type']] += 1

            except Exception, E:
                error = {'item': self.current, 'code': -1, 'data': str(E)}
                if self.noisy_errors: print "Error occurred! %s" % error
                self.errors.append(error)

    def startElement (self, name, attr):
        if name == 'node':
            self.current = {'type': 'node', 'id': attr['id'],
                'lon':attr["lon"], 'lat':attr["lat"], 'tags': {}}
        elif name == 'way':
            self.current = {'type': 'way', 'id': attr['id'], 'nodes':[], 'tags': {}}
        elif name =='nd' and self.current:
            self.current['nodes'].append(attr["ref"])
        elif name == 'tag' and self.current:
            self.current['tags'][attr['k']] = attr['v']
        if 'user' in attr and self.current:
            self.current['user'] = attr['user']
        
        if name in ['node', 'way', 'tag']:
            self.read[name] += 1

    def endElement (self, name):
        
        if name == 'way':
            new_tags = converter(self.current['tags'], 'way')
            if new_tags:
                osm = Element('osm', {'version': '0.5'})

                parent = SubElement(osm, 'way', {'id': self.current['id']})
                for n in self.current['nodes']:
                    SubElement(parent, "nd", {'ref': n})
                keys = self.current['tags'].keys()
                keys.sort()
                for key in keys:
                    SubElement(parent, "tag", {'k': key, 'v': self.current['tags'][key]})
                indent(osm)
                self.upload(tostring(osm))
        
        elif name == 'node':
            new_tags = converter(self.current['tags'], 'node')
            if new_tags:
                osm = Element('osm', {'version': '0.5'})

                parent = SubElement(osm, 'node', {'id': self.current['id'], 'lat': self.current['lat'], 'lon': self.current['lon']})
                keys = self.current['tags'].keys()
                keys.sort()
                for key in keys:
                    SubElement(parent, "tag", {'k': key, 'v': self.current['tags'][key]})
                
                indent(osm)
                self.upload(tostring(osm))


if __name__ == "__main__":
    if not converter:
        if requires:
            __doc__ = "%s\nRequired dependancies unavailable: \n %s" % (__doc__, ("\n  ".join(requires)))
        print __doc__
        sys.exit(1)
    
    if requires:
        print "Required dependancies unavailable: \n  " % ("\n  ".join(requires))  
        sys.exit(2)

    from optparse import OptionParser
    parser = OptionParser()

    parser.add_option("-f", "--file", help="source file. default is stdin", dest="file")
    parser.add_option("-d", "--dry-run", action="store_true", default=False, help="print URLs and XML for changed items, rather than actually changing them.", dest="dry_run")
    parser.add_option("-u", "--username", help="username for OSM API", dest="username")
    parser.add_option("-p", "--password", help="api password (will prompt if not provided and required)", dest="password")
    parser.add_option("-e", "--noisy-errors", dest="noisy_errors", default=False, action="store_true")
    parser.add_option("--verbose", help="be verbose", dest="verbose", default=False, action="store_true")
    parser.add_option('-o', "--only-mine", dest="only_mine", help="Provide a username/displayname which will be used to check if an edit should be perormed.") 
    parser.add_option("-n", "--no-status", dest="no_status", action="store_true", default=False, help="Don't store status db for recovery of upload (faster; riskier")
    parser.add_option('--profile', dest='profile', action="store_true", help="Report profiler stats", default=False)

    options, args = parser.parse_args()

    f = None
    if options.file:
        f = open(options.file)
        if not options.no_status:
            db = dbm.open("%s.db" % options.file, "c")
        else:
            db = None 
    else:
        f = sys.stdin
        db=None
        if not options.no_status:
            print "Using stdin, unable to create status db"
    if not options.dry_run and options.username:
        import getpass
        options.password = getpass.getpass("Password: ") 

    osmParser = changeTags(
            converter,
            db=db,
            user=options.username, 
            password=options.password, 
            dry_run=options.dry_run, 
            noisy_errors = options.noisy_errors, 
            only_mine=options.only_mine,
            verbose=options.verbose)
   
    prof = None
    if options.profile:
        import hotshot, hotshot.stats
        prof = hotshot.Profile("tagChanger.prof")
    
    try:
        if prof:
            prof.runcall(xml.sax.parse, f, osmParser)
        else:    
            xml.sax.parse( f, osmParser )
    except KeyboardInterrupt:
        print "\nStopping at %s %s due to interrupt"  % (osmParser.current['type'], osmParser.current['id'])
        pass
    except Exception, E:
        print "\nStopping at %s %s due to exception: \n%s"  % (osmParser.current['type'], osmParser.current['id'], E)

    print "Total Read: %s nodes, %s ways, %s tags"  % (osmParser.read['node'], osmParser.read['way'], osmParser.read['tag'])
    print "Total Changed: %s nodes, %s ways"  % (osmParser.changes['node'], osmParser.changes['way'])
    print "Previously Changed: %s nodes, %s ways"  % (osmParser.already_changed['node'], osmParser.already_changed['way'])
    if len(osmParser.errors):
        print "The following %s errors occurred:" % len(osmParser.errors)
    for e in osmParser.errors:
         print "%s: %s. Code: %s. Error Text: %s" % (e['item']['type'],e['item']['id'], e['code'], e['data'])
    if len(osmParser.skipped):
        print "The following %s items were skipped:" % len(osmParser.skipped)
    for e in osmParser.skipped:
         print "%s: %s. Reason: %s" % (e['type'],e['id'], e['reason'])
    if prof:
        stats = hotshot.stats.load("tagChanger.prof")
        stats.strip_dirs()
        stats.sort_stats('time', 'calls')
        stats.print_stats(20)
