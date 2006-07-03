#!/usr/bin/ruby

require 'cgi'
load 'osm/dao.rb'
require 'mapscript'
include Mapscript

r = Apache.request
r.content_type = 'image/png'
cgi = CGI.new


bbox = cgi['bbox']

if bbox == ''
  bbox = cgi['BBOX']
end


bbox.gsub!('%2D', '-')
bbox.gsub!('%2E', '.')
bbox.gsub!('%2C', ',')

bbox = bbox.split(',')

bllon = bbox[0].to_f
bllat = bbox[1].to_f
trlon = bbox[2].to_f
trlat = bbox[3].to_f

if bllat > trlat || bllon > trlon
  exit BAD_REQUEST
end
  

width = cgi['width'].to_i

if width == 0
  width = cgi['WIDTH'].to_i
end

height = cgi['height'].to_i

if height == 0
  height = cgi['HEIGHT'].to_i
end

map = MapObj.new('')
map.setProjection('init=epsg:4326')
map.height = height
map.width = width

map.extent = RectObj.new(bllon, bllat, trlon, trlat)

gpxlayer = LayerObj.new(map)
gpxlayer.name = 'gpx'
gpxlayer.setProjection('init=epsg:4326')
gpxlayer.type = MS_LAYER_POINT
gpxlayer.status = MS_ON

l = LineObj.new()


dao = OSM::Dao.instance

points = dao.get_track_points(bllon, bllat, trlon, trlat, 0)

points.each do |p|
  l.add( PointObj.new( p.longitude.to_f, p.latitude.to_f) )
end

s = ShapeObj.new(MS_LAYER_POINT)

s.add(l)

gpxlayer.addFeature(s)

cls = ClassObj.new(gpxlayer)
style = StyleObj.new()
color = ColorObj.new()
color.red = 255
color.green = 0
color.blue = 0
style.color = color
cls.insertStyle(style)

#
# Save image
#

map.selectOutputFormat('PNG')
img = map.draw

fname = '/tmp/' + rand.to_s  + '_tmpimg'
img.save(fname)

File::open( fname, 'r' ) {|ofh|
  r.send_fd(ofh)
}

#now delete it. sigh
File::delete( fname )
  

