#!/usr/bin/ruby

require 'cgi'
require 'cairo'
load 'osm/dao.rb'
require 'bigdecimal'

include Cairo
include Math

class Mercator

  def initialize(lat, lon, degrees_per_pixel, width, height)
    #init me with your centre lat/lon, the number of degrees per pixel and the size of your image
    @clat = lat
    @clon = lon
    @degrees_per_pixel = degrees_per_pixel
    @width = width
    @height = height
    @dlon = width / 2 * degrees_per_pixel 
    @dlat = height / 2 * degrees_per_pixel  * cos(@clat * PI / 180)

    @tx = xsheet(@clon - @dlon)
    @ty = ysheet(@clat - @dlat)

    @bx = xsheet(@clon + @dlon)
    @by = ysheet(@clat + @dlat)

  end

  
  #the following two functions will give you the x/y on the entire sheet

  def kilometerinpixels
    return 40008.0  / 360.0 * @degrees_per_pixel
  end

  def ysheet(lat)
    log(tan(PI / 4 +  (lat  * PI / 180 / 2)))
  
  end

  def xsheet(lon)
    lon
  end

  #and these two will give you the right points on your image. all the constants can be reduced to speed things up. FIXME

  def y(lat)
    return @height - ((ysheet(lat) - @ty) / (@by - @ty) * @height) 
  end

  def x(lon)
    return  ((xsheet(lon) - @tx) / (@bx - @tx) * @width)  
   
  end

end



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
#  exit BAD_REQUEST
end
  

width = cgi['width'].to_i

if width == 0
  width = cgi['WIDTH'].to_i
end

height = cgi['height'].to_i

if height == 0
  height = cgi['HEIGHT'].to_i
end

# test if the box is too big

tile_too_big = width > 256 || height > 256 || ( (trlon - bllon) * (trlat - bllat) ) > 0.0025

if !tile_too_big 
  proj = Mercator.new((bllat + trlat) / 2, (bllon + trlon) / 2, (trlon - bllon) / width, width, height)

  dao = OSM::Dao.instance

  nodes = dao.getnodes(trlat, bllon, bllat, trlon)

  if nodes && nodes.length > 0
    linesegments = dao.getlines(nodes)
  end

  if linesegments
    linesegments.each do |key, l|
      #the next 2 lines of code are just so unbelievably sexy its just not funny. Fuck that java shit!
      nodes[l.node_a_id] = dao.getnode(l.node_a_id) unless nodes[l.node_a_id]
      nodes[l.node_b_id] = dao.getnode(l.node_b_id) unless nodes[l.node_b_id]
    end
  end
end

fname = '/tmp/' + rand.to_s  + '_tmpimg'


File.open(fname, "wb") {|stream|
  cr = Context.new
  cr.set_target_png(stream, FORMAT_ARGB32, width, height);

  if linesegments && !tile_too_big
    linesegments.each do |key, l|
      node_a = nodes[l.node_a_id]
      node_b = nodes[l.node_b_id]
      
      if node_a.visible == true && node_b.visible == true
        cr.new_path
        cr.move_to(proj.x(node_a.longitude) , proj.y(node_a.latitude) )
        cr.line_to(proj.x(node_b.longitude) , proj.y(node_b.latitude) )
        cr.close_path
        cr.set_rgb_color(0.0, 0.0, 0.0)
        cr.line_join = LINE_JOIN_MITER
        cr.line_width = 3
        cr.stroke
      end
    end

    linesegments.each do |key, l|
      node_a = nodes[l.node_a_id]
      node_b = nodes[l.node_b_id]
      
      if node_a.visible == true && node_b.visible == true
        cr.new_path
        cr.move_to(proj.x(nodes[l.node_a_id].longitude) , proj.y(nodes[l.node_a_id].latitude) )
        cr.line_to(proj.x(nodes[l.node_b_id].longitude) , proj.y(nodes[l.node_b_id].latitude) )
        cr.close_path
        cr.set_rgb_color(1.0, 1.0, 1.0)
        cr.line_join = LINE_JOIN_MITER
        cr.line_width = 1
        cr.stroke
      end
    end
  end

 cr.show_page
}

File::open( fname, 'r' ) {|ofh|
  r.send_fd(ofh)
}

#now delete it. sigh
File::delete( fname )

GC.start

exit

