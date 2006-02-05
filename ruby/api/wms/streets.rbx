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

  proj = OSM::Mercator.new((bllat + trlat) / 2, (bllon + trlon) / 2, (trlon - bllon) / width, width, height)

  dao = OSM::Dao.instance

  nodes = dao.getnodes(trlat, bllon, bllat, trlon)

  if nodes && nodes.length > 0
    linesegments = dao.getlines(nodes)
  end

  if linesegments
    linesegments.each do |key, l|
      nodes[l.node_a_id] = dao.getnode(l.node_a_id) unless nodes[l.node_a_id]
      nodes[l.node_b_id] = dao.getnode(l.node_b_id) unless nodes[l.node_b_id]
    end
  
    if !tile_too_big # draw things
      gc.stroke('black')
      gc.stroke_width(4)
      
      # draw black lines
      linesegments.each do |key, l|
        node_a = nodes[l.node_a_id]
        node_b = nodes[l.node_b_id]
        if node_a.visible == true && node_b.visible == true
          gc.line(proj.x(node_a.longitude).to_i , proj.y(node_a.latitude).to_i, proj.x(node_b.longitude).to_i , proj.y(node_b.latitude).to_i )
        end
      end
      
      #draw white lines on top
      gc.stroke('white')
      gc.stroke_width(0)
      linesegments.each do |key, l|
        node_a = nodes[l.node_a_id]
        node_b = nodes[l.node_b_id]
   
        if node_a.visible == true && node_b.visible == true
          gc.line(proj.x(nodes[l.node_a_id].longitude).to_i , proj.y(nodes[l.node_a_id].latitude).to_i, proj.x(nodes[l.node_b_id].longitude).to_i , proj.y(nodes[l.node_b_id].latitude).to_i )
        end
      end
    end
  end
end

canvas = Magick::Image.new(width, height) {
        self.background_color = 'pink'
     }

begin 
	gc.draw(canvas)
rescue ArgumentError
end

transparent_canvas = canvas.transparent('pink', Magick::TransparentOpacity)

transparent_canvas.format = 'PNG'
puts transparent_canvas.to_blob
