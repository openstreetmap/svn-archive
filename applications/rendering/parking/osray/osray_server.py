# -*- coding: utf-8 -*-

import string, cgi, time
from os import curdir, sep
from BaseHTTPServer import BaseHTTPRequestHandler, HTTPServer
import urlparse
import osray
import math

options = {'height': 100, 'dsn': 'dbname=gis', 'width': 100, 'prefix': 'planet_osm', 'quick': False, 'hq': False}
options['bbox']='9.94861 49.79293,9.96912 49.80629' # Europastern
options['bbox']='9.92498 49.78816,9.93955 49.8002' # Innenstadt
#options['bbox']='12.13344 54.08574,12.14387 54.09182' # Rostock
#options['bbox']='12.03344 53.98574,12.24387 54.19182' # Rostock-xx
#options['bbox']='2.822157 41.983275,2.827371 41.987123' # Girona River
#options['bbox']='8.9771580802 47.2703623267,13.8350427083 50.5644529365' # bayern
#options['bbox']='-43.1806 -22.91442,-43.17034 -22.90488' # Rio
# for tile names and coordinates:
# http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames

def avg(a,b): return (a+b)/2.0

class osrayHandler(BaseHTTPRequestHandler):


    def scale_bbox(self,old_bbox,scale):
        old_bbox = old_bbox.replace(' ',',')
        print "old_bbox ",old_bbox
        pointlist = map(lambda coord: float(coord), old_bbox.split(','))
        print "pointlist ",pointlist
        xmin = pointlist[0]
        ymin = pointlist[1]
        xmax = pointlist[2]
        ymax = pointlist[3]
        xmid = avg(xmin,xmax)
        ymid = avg(ymin,ymax)
        xradius = xmid-xmin
        yradius = ymid-ymin
        xradius *= scale
        yradius *= scale
        xmin = xmid-xradius
        xmax = xmid+xradius
        ymin = ymid-yradius
        ymax = ymid+yradius
        return str(xmin)+" "+str(ymin)+","+str(xmax)+" "+str(ymax)

    def num2deg(xtile, ytile, zoom):
        n = 2.0 ** zoom
        lon_deg = xtile / n * 360.0 - 180.0
        lat_rad = math.atan(math.sinh(math.pi * (1 - 2 * ytile / n)))
        lat_deg = math.degrees(lat_rad)
        return(lat_deg, lon_deg)
    def do_GET(self):
        try:
            theurl = self.path
            if theurl == "/favicon.ico":
                self.send_response(200)
                self.end_headers()
                self.wfile.write("")
                return
            urlcomponents = urlparse.urlparse(theurl)
            print urlcomponents
            baseurl = urlcomponents[2]
            print "parse=",urlparse.urlparse(theurl)
            print "base=",baseurl
            urlqs = urlparse.urlparse(theurl)[4]
            print "URL qs:", urlqs
            queryparams = urlparse.parse_qs(urlqs)
            print queryparams

            options['bbox']=self.scale_bbox(options['bbox'],float(2**-1))

            if baseurl.startswith("povtile"):
                pass
                # PLEASE IMPLEMENT HERE - FIXME
            
            if baseurl.endswith(".png"):
                if queryparams.has_key('width'):
                    options['width']=queryparams['width'][0]
                if queryparams.has_key('height'):
                    options['height']=queryparams['height'][0]
                if queryparams.has_key('hq'):
                    options['hq']=(str(queryparams['hq'][0])=='1')
                print "--- calling osray"
                osray.main(options)
                print "--- calling osray ends"
                f = open(curdir + sep + 'scene-osray.png')
                print "--- send_response"
                self.send_response(200)
                print "--- send_header"
                self.send_header('Content-type','image/png')
                print "--- send_end_headers"
                self.end_headers()
                print "--- send_write"
                self.wfile.write(f.read())
                print "--- close"
                f.close()
                return
            print "URL was ", theurl
            urlqs = urlparse.urlparse(theurl)[4]
            print "URL qs:", urlqs
            queryparams = urlparse.parse_qs(urlqs)
            print queryparams
            self.send_response(200)
            self.send_header('Content-type', 'text/html')
            self.end_headers()
            self.wfile.write("hey, today is the" + str(time.localtime()[7]))
            self.wfile.write(" day in the year " + str(time.localtime()[0]))
            return
        except IOError:
            self.send_error(404, 'File Not Found: %s' % self.path)


"""
    def do_POST(self):
        global rootnode
        try:
            ctype, pdict = cgi.parse_header(self.headers.getheader('content-type'))
            if ctype == 'multipart/form-data':
                query = cgi.parse_multipart(self.rfile, pdict)
            self.send_response(301)
            
            self.end_headers()
            upfilecontent = query.get('upfile')
            print "filecontent", upfilecontent[0]
            self.wfile.write("<HTML>POST OK.<BR><BR>");
            self.wfile.write(upfilecontent[0]);
            
        except :
            pass
"""

def main():
    try:
        server = HTTPServer(('', 8087), osrayHandler)
        print 'started osray server...'
        server.serve_forever()
    except KeyboardInterrupt:
        print 'shutting down server'
        server.socket.close()

if __name__ == '__main__':
    main()
