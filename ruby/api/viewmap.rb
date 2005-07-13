#!/usr/bin/ruby -w


require 'cgi'
require 'cairo'
require 'osm/dao'
require 'bigdecimal'

include Cairo
include Math


r = Apache.request
cgi = CGI.new

clat  = cgi['lat'].to_f
clon  = cgi['lon'].to_f
scale = cgi['scale'].to_f

if clat == 0.0 then clat = 51.526447 end
if clon == 0.0 then clon = -0.14746371 end
if scale == 0.0 then scale = 0.0001 end


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
    return ((xsheet(lon) - @tx) / (@bx - @tx) * @width)  
   
  end

end


WIDTH = 800
HEIGHT = 600
BARPX = 20
BASELINE = -1
OFFSET = 1

dao = OSM::Dao.new

#clat = 51.5356450000 #center lat and lon
#clon = -0.1405966667

#pixelindegrees = 0.00005

dlon = WIDTH / 2 * scale
dlat = HEIGHT / 2 * scale


nodes = dao.getnodes(clat + dlat, clon - dlon, clat - dlat, clon + dlon)

linesegments = dao.getlines(nodes)


linesegments.each do |key, l|
  #the next 2 lines of code are just so unbelievably sexy its just not funny. Fuck that java shit!
  nodes[l.node_a_uid] = dao.getnode(l.node_a_uid) unless nodes[l.node_a_uid]
  nodes[l.node_b_uid] = dao.getnode(l.node_b_uid) unless nodes[l.node_b_uid]

end

#get the file etc

fname = '/tmp/' + rand.to_s  + '_tmpimg'

File.open(fname, "wb") {|stream|
  begin
  cr = Context.new
  cr.set_target_png(stream, FORMAT_ARGB32, WIDTH, HEIGHT);

  proj = Mercator.new(clat,clon,scale, WIDTH, HEIGHT)


  #paint a white background
  cr.new_path
  cr.rectangle(0, 0, WIDTH, HEIGHT)
  cr.set_rgb_color(1.0, 1.0, 1.0)
  cr.fill


  linesegments.each do |key, l|    
    cr.new_path
    cr.move_to(proj.x(nodes[l.node_a_uid].longitude) , proj.y(nodes[l.node_a_uid].latitude) )
    cr.line_to(proj.x(nodes[l.node_b_uid].longitude) , proj.y(nodes[l.node_b_uid].latitude) )
    cr.close_path
    cr.set_rgb_color(0.0, 0.0, 0.0)
    cr.line_join = LINE_JOIN_MITER
    cr.line_width = 1
    cr.stroke
  end

  len = proj.kilometerinpixels * WIDTH  / 2.0


  hum =  BigDecimal::new(len.to_s).exponent - 1

  
  len = (10.0 ** hum ) / proj.kilometerinpixels

  cr.new_path
  cr.rectangle(WIDTH - OFFSET - len, HEIGHT + BASELINE - BARPX, len, BARPX)
  cr.set_rgb_color(0.8,0.8,0.8)
  cr.close_path
  cr.fill

  cr.new_path
  cr.select_font "Sans", Cairo::FONT_WEIGHT_NORMAL, Cairo::FONT_SLANT_NORMAL
  cr.scale_font 20
  cr.move_to(WIDTH - ((OFFSET + OFFSET + len) / 2) - 20, HEIGHT + BASELINE - 5 ).text_path("#{10.0 ** hum} km")
  cr.close_path
  cr.set_rgb_color(0.0,0.0,0.0)
  cr.fill

  cr.new_path
  cr.line_to(WIDTH - OFFSET, HEIGHT + BASELINE)
  cr.line_to(WIDTH - OFFSET, HEIGHT + BASELINE - BARPX)
  cr.line_to(WIDTH - OFFSET, HEIGHT + BASELINE)
  cr.line_to(WIDTH - OFFSET - len, HEIGHT + BASELINE)
  cr.line_to(WIDTH - OFFSET - len, HEIGHT + BASELINE - BARPX)
  cr.line_to(WIDTH - OFFSET - len, HEIGHT + BASELINE)
  
  cr.close_path
  cr.set_rgb_color(0.0,0.0,0.0)
  cr.line_join = LINE_JOIN_MITER
  cr.line_width = 1
  cr.stroke
  

  cr.show_page
#  ensure
#    stream.close
  end
}


#read the file back in and send it to the browser
File::open( fname, 'r' ) {|ofh|
  r.send_fd(ofh)
}

#now delete it. sigh
File::delete( fname )

