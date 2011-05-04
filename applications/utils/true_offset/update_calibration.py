#!/usr/bin/env python

"""True Offset Process update_calibration.py

Copyright (c) 2011, Bartosz Fabianowski <bartosz@fabianowski.eu>
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.
* Neither the name of the author nor the names of its contributors may be used
  to endorse or promote products derived from this software without specific
  prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

$Id$
"""
import sys
import urllib2
import xml.parsers.expat

import ppygis
import psycopg2

database = 'dbname=offset_production'
xapi = 'http://open.mapquestapi.com/xapi/'

dry_run = True

reload(sys)
sys.setdefaultencoding('utf-8')

try:
  connection = psycopg2.connect(database)
  cursor = connection.cursor()
except Exception, exception:
  sys.exit('Unable to establish database connection: {0}'.format(exception))

try:
  offsets = urllib2.urlopen('{0}api/0.6/way[calibration=area]'.format(xapi))
except Exception, exception:
  sys.exit('Unable to retrieve offsets from XAPI: {0}'.format(exception))

nodes = dict()
id = None
user = None
boundary = ppygis.LineString([])
tags = dict()

def print_message(message, id, user, name, header='INFO'):
  print header
  print '  Reason:     {0}'.format(message)
  print '  Way:        http://api.openstreetmap.org/api/0.6/way/{0}'.format(id)
  print '  User:       http://www.openstreetmap.org/user/{0}'.format(user)
  if name:
    print '  Area name:  {0}'.format(name)
  print

def process_start(name, attributes):
  global nodes, id, user, boundary, tags

  if name == 'node':
    nodes[attributes['id']] = ppygis.Point(float(attributes['lon']),
                                           float(attributes['lat']))
  elif name == 'way':
    id = attributes['id']
    user = attributes['user']
    boundary = ppygis.LineString([])
    tags = dict()
  elif name == 'nd':
    boundary.points.append(nodes[attributes['ref']])
  elif name == 'tag':
    tags[attributes['k']] = attributes['v']

def process_end(name):
  if name == 'way':
    try:
      name = tags['area_name'] if 'area_name' in tags else None
      provider = tags['data_provider'] if 'data_provider' in tags else None
      zoom_min = tags['zoom_min'] if 'zoom_min' in tags else None
      zoom_max = tags['zoom_max'] if 'zoom_max' in tags else None
      offset_north = tags['offset_north'] if 'offset_north' in tags else None
      offset_east = tags['offset_east'] if 'offset_east' in tags else None

      if name is None:
        print_message('Recommended tag "area_name" missing', id, user, name)

      if provider is None:
        raise Exception('Required tag "data_provider" missing')

      if zoom_min is not None and zoom_max is not None and zoom_min > zoom_max:
        raise Exception('Invalid zoom range ("zoom_min" > "zoom_max")')

      if offset_north and ',' in offset_north:
        print_message('Tag "offset_north" uses comma as decimal separator', id,
                      user, name)
        offset_north = float(offset_north.replace(',', '.'))

      if offset_east and ',' in offset_east:
        print_message('Tag "offset_east" uses comma as decimal separator', id,
                      user, name)
        offset_east = float(offset_east.replace(',', '.'))

      if offset_north is None and offset_east is None:
        raise Exception('Neither tag "offset_north" nor tag "offset_east" ' +
                        'specified')

      offset_north = float(offset_north)
      offset_east = float(offset_east)

      if abs(offset_north) > 1.0:
        raise Exception('Value of tag "offset_north" exceeds one degree')

      if abs(offset_east) > 1.0:
        raise Exception('Value of tag "offset_east" exceeds one degree')

      try:
        cursor.execute('INSERT INTO offsets(name, provider, zoom_min, ' +
                       'zoom_max, offset_north, offset_east, boundary) ' +
                       'VALUES (%s, %s, %s, %s, %s, %s, %s)',
                       (name, provider, zoom_min, zoom_max, offset_north,
                        offset_east, boundary))
      except Exception, exception:
        print_message(exception, id, user, name,
                      'ERROR Entire import aborted due to SQL error')
        sys.exit(1)

    except Exception, exception:
      print_message(exception, id, user, name,
                    'WARNING Calibration area not imported')

cursor.execute('DELETE FROM offsets')

parser = xml.parsers.expat.ParserCreate()
parser.StartElementHandler = process_start
parser.EndElementHandler = process_end
parser.ParseFile(offsets)

if not dry_run:
  connection.commit()
