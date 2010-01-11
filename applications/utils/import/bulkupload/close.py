#! /usr/bin/python3
# vim: fileencoding=utf-8 encoding=utf-8 et sw=4

# Copyright (C) 2009 Jacek Konieczny <jajcus@jajcus.net>
# Copyright (C) 2009 Andrzej Zaborowski <balrogg@gmail.com>
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA


"""
Closes a changeset, given id.
"""

__version__ = "$Revision: 21 $"

import os
import subprocess
import sys
import traceback
import base64

import http.client as httplib
import xml.etree.cElementTree as ElementTree
import urllib.parse as urlparse

class HTTPError(Exception):
    pass

class OSM_API(object):
    url = 'http://api.openstreetmap.org/'
    def __init__(self, username = None, password = None):
        if username and password:
            self.username = username
            self.password = password
        else:
            self.username = ""
            self.password = ""
        self.changeset = None
        self.progress_msg = None

    def __del__(self):
        if self.changeset is not None:
            self.close_changeset()

    def msg(self, mesg):
        sys.stderr.write("\r%s…                        " % (self.progress_msg))
        sys.stderr.write("\r%s… %s" % (self.progress_msg, mesg))
        sys.stderr.flush()

    def request(self, conn, method, url, body, headers, progress):
        if progress:
            self.msg("making request")
            conn.putrequest(method, url)
            self.msg("sending headers")
            if body:
                conn.putheader('Content-Length', str(len(body)))
            for hdr, value in headers.iteritems():
                conn.putheader(hdr, value)
            self.msg("end of headers")
            conn.endheaders()
            self.msg(" 0%")
            if body:
                start = 0
                size = len(body)
                chunk = size / 100
                if chunk < 16384:
                    chunk = 16384
                while start < size:
                    end = min(size, start + chunk)
                    conn.send(body[start:end])
                    start = end
                    self.msg("%2i%%" % (start * 100 / size))
        else:
            self.msg(" ")
            conn.request(method, url, body, headers)

    def _run_request(self, method, url, body = None, progress = 0, content_type = "text/xml"):
        url = urlparse.urljoin(self.url, url)
        purl = urlparse.urlparse(url)
        if purl.scheme != "http":
            raise ValueError("Unsupported url scheme: %r" % (purl.scheme,))
        if ":" in purl.netloc:
            host, port = purl.netloc.split(":", 1)
            port = int(port)
        else:
            host = purl.netloc
            port = None
        url = purl.path
        if purl.query:
            url += "?" + query
        headers = {}
        if body:
            headers["Content-Type"] = content_type

        try_no_auth = 0

        if not try_no_auth and not self.username:
            raise HTTPError("Need a username")

        try:
            self.msg("connecting")
            conn = httplib.HTTPConnection(host, port)
#            conn.set_debuglevel(10)

            if try_no_auth:
                self.request(conn, method, url, body, headers, progress)
                self.msg("waiting for status")
                response = conn.getresponse()

            if not try_no_auth or (response.status == httplib.UNAUTHORIZED and
                    self.username):
                if try_no_auth:
                    conn.close()
                    self.msg("re-connecting")
                    conn = httplib.HTTPConnection(host, port)
#                    conn.set_debuglevel(10)

                creds = self.username + ":" + self.password
                headers["Authorization"] = "Basic " + \
                        base64.b64encode(bytes(creds, "utf8")).decode("utf8")
                        # ^ Seems to be broken in python3 (even the raw
                        # documentation examples don't run for base64)
                self.request(conn, method, url, body, headers, progress)
                self.msg("waiting for status")
                response = conn.getresponse()

            if response.status == httplib.OK:
                self.msg("reading response")
                sys.stderr.flush()
                response_body = response.read()
            else:
                raise HTTPError("%02i: %s (%s)" % (response.status,
                        response.reason, response.read()))
        finally:
            conn.close()
        return response_body

    def create_changeset(self, created_by, comment):
        if self.changeset is not None:
            raise RuntimeError("Changeset already opened")
        self.progress_msg = "I'm creating the changeset"
        self.msg("")
        sys.stderr.flush()
        root = ElementTree.Element("osm")
        tree = ElementTree.ElementTree(root)
        element = ElementTree.SubElement(root, "changeset")
        ElementTree.SubElement(element, "tag", {"k": "created_by", "v": created_by})
        ElementTree.SubElement(element, "tag", {"k": "comment", "v": comment})
        body = ElementTree.tostring(root, "utf-8")
        reply = self._run_request("PUT", "/api/0.6/changeset/create", body)
        changeset = int(reply.strip())
        self.msg("done. Id: %i" % (changeset,))
        sys.stderr("\n")
        self.changeset = changeset

    def upload(self, change):
        if self.changeset is None:
            raise RuntimeError("Changeset not opened")
        self.progress_msg = "Now I'm sending changes"
        self.msg("")
        sys.stderr.flush()
        for operation in change:
            if operation.tag not in ("create", "modify", "delete"):
                continue
            for element in operation:
                element.attrib["changeset"] = str(self.changeset)
        body = ElementTree.tostring(change, "utf-8")
        reply = self._run_request("POST", "/api/0.6/changeset/%i/upload"
                                                % (self.changeset,), body, 1)
        self.msg("done.")
        sys.stderr.write("\n")
        return reply

    def close_changeset(self):
        if self.changeset is None:
            raise RuntimeError("Changeset not opened")
        self.progress_msg = "Closing"
        self.msg("")
        sys.stderr.flush()
        sys.stderr.flush()
        reply = self._run_request("PUT", "/api/0.6/changeset/%i/close"
                                                    % (self.changeset,))
        self.changeset = None
        self.msg("done, too.")
        sys.stderr.write("\n")

try:
    this_dir = os.path.dirname(__file__)
    try:
        version = int(subprocess.Popen(["svnversion", this_dir], stdout = subprocess.PIPE).communicate()[0].strip())
    except:
        version = 1
    if len(sys.argv) != 2:
        sys.stderr.write("Synopsis:\n")
        sys.stderr.write("    %s <changeset-id>\n", sys.argv[0])
        sys.exit(1)

    login = input("OSM login: ")
    if not login:
        sys.exit(1)
    password = input("OSM password: ")
    if not login:
        sys.exit(1)

    api = OSM_API(login, password)
    api.changeset = int(sys.argv[1])
    api.close_changeset()
except HTTPError as err:
    sys.stderr.write(str(err) + "\n")
    sys.exit(1)
except Exception as err:
    sys.stderr.write(str(err) + "\n")
    traceback.print_exc(file=sys.stderr)
    sys.exit(1)
