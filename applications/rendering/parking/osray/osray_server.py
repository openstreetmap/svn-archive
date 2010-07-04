# -*- coding: utf-8 -*-

import string, cgi, time
from os import curdir, sep
from BaseHTTPServer import BaseHTTPRequestHandler, HTTPServer
import urlparse
import osray
from osray_geom import *
import math

options = {'height': 100, 'dsn': 'dbname=gis', 'width': 100, 'prefix': 'planet_osm', 'quick': False, 'hq': False}
#options['bbox']='9.94861 49.79293,9.96912 49.80629' # Europastern
#options['bbox']='9.92498 49.78816,9.93955 49.8002' # Innenstadt
options['bbox']='9.9630547 49.8471708,9.968461999999999 49.8504638' #Technopark
#options['bbox']='12.13344 54.08574,12.14387 54.09182' # Rostock
#options['bbox']='12.03344 53.98574,12.24387 54.19182' # Rostock-xx
#options['bbox']='2.822157 41.983275,2.827371 41.987123' # Girona River
#options['bbox']='8.9771580802 47.2703623267,13.8350427083 50.5644529365' # bayern
#options['bbox']='-43.1806 -22.91442,-43.17034 -22.90488' # Rio
# for tile names and coordinates:
# http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames

class osrayHandler(BaseHTTPRequestHandler):

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

            #options['bbox']=scale_bbox(options['bbox'],float(2**-1))

            print "baseagain=",baseurl

            if baseurl=="/wms": # example URL http://localhost/osray/povtile/16/34576/22282.png
                print "Handling WMS Request with parameters:", queryparams
# example queryparams {
#ignore:
#'LAYERS': ['land'], 
#'SERVICE': ['WMS'], 
#'FORMAT': ['image/png'], 
#'REQUEST': ['GetMap'], 
#'SRS': ['EPSG:3857'], 
#'VERSION': ['1.1.1'], 
#'EXCEPTIONS': ['application/vnd.ogc.se_xml'], 
#'TRANSPARENT': ['FALSE']}
#use:
#'WIDTH': ['2048'], 
#'HEIGHT': ['2048'], 
#'BBOX': ['0,0,20037508.3392,20037508.3392'], 
                options['width']=queryparams['WIDTH'][0]
                options['height']=queryparams['HEIGHT'][0]
                bbox_SRS3857 = queryparams['BBOX'][0]
                print "BBOX=",bbox_SRS3857
                bbox_SRS3857 = bbox_format_3_to_1_comma(bbox_SRS3857)
                options['hq']=False
                options['bbox']=bbox_SRS3857
                options['srs']='3857'
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

            if baseurl.startswith("/povtile/"): # example URL http://localhost/osray/povtile/16/34576/22282.png
                if baseurl.endswith(".png"):
                    zxy = baseurl[9:-4].split('/')
                    if(len(zxy)==3):
                        print "zxy=",zxy
                        zoom = float(zxy[0])
                        xtile = float(zxy[1])
                        ytile = float(zxy[2])
                        bbox = num2bbox(xtile,ytile,zoom)
                        print bbox
                        options['bbox']=bbox
                        options['width']=256
                        options['height']=256
                        options['hq']=True
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
                        # PLEASE IMPLEMENT HERE - FIXME
            
            if baseurl=="/big.png":
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

"""
ParseResult(scheme='', netloc='', path='/wms', params='', query='BBOX=0,0,20037508.3392,20037508.3392&EXCEPTIONS=application/vnd.ogc.se_xml&FORMAT=image/png&HEIGHT=2048&LAYERS=land&REQUEST=GetMap&SERVICE=WMS&SRS=EPSG:3857&STYLES=&TRANSPARENT=FALSE&VERSION=1.1.1&WIDTH=2048', fragment='')
parse= ParseResult(scheme='', netloc='', path='/wms', params='', query='BBOX=0,0,20037508.3392,20037508.3392&EXCEPTIONS=application/vnd.ogc.se_xml&FORMAT=image/png&HEIGHT=2048&LAYERS=land&REQUEST=GetMap&SERVICE=WMS&SRS=EPSG:3857&STYLES=&TRANSPARENT=FALSE&VERSION=1.1.1&WIDTH=2048', fragment='')
base= /wms
URL qs: BBOX=0,0,20037508.3392,20037508.3392&EXCEPTIONS=application/vnd.ogc.se_xml&FORMAT=image/png&HEIGHT=2048&LAYERS=land&REQUEST=GetMap&SERVICE=WMS&SRS=EPSG:3857&STYLES=&TRANSPARENT=FALSE&VERSION=1.1.1&WIDTH=2048
{'LAYERS': ['land'], 'WIDTH': ['2048'], 'SERVICE': ['WMS'], 'FORMAT': ['image/png'], 'REQUEST': ['GetMap'], 'HEIGHT': ['2048'], 'SRS': ['EPSG:3857'], 'VERSION': ['1.1.1'], 'BBOX': ['0,0,20037508.3392,20037508.3392'], 'EXCEPTIONS': ['application/vnd.ogc.se_xml'], 'TRANSPARENT': ['FALSE']}
baseagain= /wms
URL was  /wms?BBOX=0,0,20037508.3392,20037508.3392&EXCEPTIONS=application/vnd.ogc.se_xml&FORMAT=image/png&HEIGHT=2048&LAYERS=land&REQUEST=GetMap&SERVICE=WMS&SRS=EPSG:3857&STYLES=&TRANSPARENT=FALSE&VERSION=1.1.1&WIDTH=2048
URL qs: BBOX=0,0,20037508.3392,20037508.3392&EXCEPTIONS=application/vnd.ogc.se_xml&FORMAT=image/png&HEIGHT=2048&LAYERS=land&REQUEST=GetMap&SERVICE=WMS&SRS=EPSG:3857&STYLES=&TRANSPARENT=FALSE&VERSION=1.1.1&WIDTH=2048
{'LAYERS': ['land'], 'WIDTH': ['2048'], 'SERVICE': ['WMS'], 'FORMAT': ['image/png'], 'REQUEST': ['GetMap'], 'HEIGHT': ['2048'], 'SRS': ['EPSG:3857'], 'VERSION': ['1.1.1'], 'BBOX': ['0,0,20037508.3392,20037508.3392'], 'EXCEPTIONS': ['application/vnd.ogc.se_xml'], 'TRANSPARENT': ['FALSE']}
localhost - - [04/Jul/2010 22:04:44] "GET /wms?BBOX=0,0,20037508.3392,20037508.3392&EXCEPTIONS=application/vnd.ogc.se_xml&FORMAT=image/png&HEIGHT=2048&LAYERS=land&REQUEST=GetMap&SERVICE=WMS&SRS=EPSG:3857&STYLES=&TRANSPARENT=FALSE&VERSION=1.1.1&WIDTH=2048 HTTP/1.1" 200 -
"""