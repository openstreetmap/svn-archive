#!/usr/bin/ruby

require 'cgi'
require 'RMagick'
require 'osm/dao.rb'
require 'bigdecimal'

r = Apache.request
r.content_type = 'image/png'
cgi = CGI.new


bbox = cgi['bbox']

bbox = cgi['BBOX'] if bbox == ''
bbox = '-0.1824371,51.5108931667899,-0.1124637,51.54200083321096' if bbox == ''

bbox.gsub!('%2D', '-')
bbox.gsub!('%2E', '.')
bbox.gsub!('%2C', ',')

bbox = bbox.split(',')

bllon = bbox[0].to_f
bllat = bbox[1].to_f
trlon = bbox[2].to_f
trlat = bbox[3].to_f

width = cgi['width'].to_i
width = cgi['WIDTH'].to_i if width == 0

height = cgi['height'].to_i
height = cgi['HEIGHT'].to_i if height == 0

tile_too_big = width > 256 || height > 256 || ( (trlon - bllon) * (trlat - bllat) ) > 0.0025

n = rand

`mkdir /tmp/#{n}`
`curl --user steve@fractalus.com:tboadd149 'http://www.openstreetmap.org/api/0.3/map?#{cgi['bbox']}' > /tmp/#{n}/data.osm`
`cd /tmp/#{n} && /usr/local/j2sdk1.4.2/bin/java -cp /var/www/tile/ruby/xalan.jar org.apache.xalan.xslt.Process -in  /var/www/tile/ruby/osm-map-features.xml -out data.svg`
`xvfb-run /usr/local/j2sdk1.4.1/bin/java -Xmx256m -jar /usr/src/batik-1.6/batik-rasterizer.jar /tmp/#{n}/data.svg`


