# -*- coding: utf-8 -*-

import string, cgi, time
from os import curdir, sep
from BaseHTTPServer import BaseHTTPRequestHandler, HTTPServer
import urlparse

class osrayHandler(BaseHTTPRequestHandler):

    def do_GET(self):
        try:
            theurl = self.path
            if theurl == "/favicon.ico":
                self.send_response(200)
                self.end_headers()
                self.wfile.write("")
                return
            baseurl = urlparse.urlparse(theurl)[2]
            if baseurl.endswith(".png"):
                f = open(curdir + sep + 'scene-osray.png')
                self.send_response(200)
                self.send_header('Content-type','image/png')
                self.end_headers()
                self.wfile.write(f.read())
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
