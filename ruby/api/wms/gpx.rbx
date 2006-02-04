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

gc = Magick::Draw.new

if !tile_too_big 
  gc.stroke_linejoin('miter')
  gc.stroke('#ffcc00')
  gc.stroke_width(4)
  
  proj = OSM::Mercator.new((bllat + trlat) / 2, (bllon + trlon) / 2, (trlon - bllon) / width, width, height)

  dao = OSM::Dao.instance

  points = dao.get_track_points(bllat, bllon, trlat, trlon, 0)

  points.each do |p|  
    px = proj.x(p.longitude)
    py = proj.y(p.latitude)
    gc.line(px, py-1, px+1, py )
    gc.line(px+1, py, px, py+1 )
    gc.line(px, py+1, px-1, py )
    gc.line(px-1, py, px, py-1 )
  end
end

canvas = Magick::Image.new(width, height) {
        self.background_color = 'pink'
     }
 
gc.draw(canvas)

transparent_canvas = canvas.transparent('pink', Magick::TransparentOpacity)

transparent_canvas.format = 'PNG'
puts transparent_canvas.to_blob 

